package de.orat.math.netbeans.ga;

import de.orat.math.netbeans.ga.utils.GaFileUtils;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.JarURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.util.Lookup;

/** Starts the GraalVM LSP in an isolated JVM and connects NetBeans by socket. */
@MimeRegistration(mimeType = GaFileUtils.GA_MIME_TYPE, service = LanguageServerProvider.class)
public final class GenericGraalVMLanguageServer implements LanguageServerProvider {

    private static final Logger LOG = Logger.getLogger(GenericGraalVMLanguageServer.class.getName());
    private static final int STARTUP_TIMEOUT_MILLIS = 15_000;
    private static final int STARTUP_RETRY_DELAY_MILLIS = 50;
    private static final String SERVER_MARKER_RESOURCE
            = "META-INF/services/org.graalvm.polyglot.impl.AbstractPolyglotImpl";

    @Override
    public synchronized LanguageServerDescription startServer(Lookup lookup) {
        Process process = null;
        Socket socket = null;
        ServerOutputCollector serverOutput = null;
        try {
            int port = findFreeLoopbackPort();
            StartedServer startedServer = startServerProcess(port);
            process = startedServer.process();
            serverOutput = startedServer.output();
            socket = connectToServer(process, serverOutput, port);
            return LanguageServerDescription.create(
                    socket.getInputStream(), socket.getOutputStream(),
                    new SocketProcess(process, socket));
        } catch (IOException ex) {
            closeQuietly(socket);
            if (process != null) {
                process.destroyForcibly();
            }
            LOG.log(Level.WARNING, "Cannot start the external GA language server.", ex);
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            closeQuietly(socket);
            if (process != null) {
                process.destroyForcibly();
            }
            LOG.log(Level.WARNING, "Interrupted while starting the external GA language server.", ex);
            return null;
        }
    }

    private static int findFreeLoopbackPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            return socket.getLocalPort();
        }
    }

    private static StartedServer startServerProcess(int port) throws IOException {
        Path serverJar = findServerJar();
        Path javaExecutable = javaExecutable();

        Process process = new ProcessBuilder(javaExecutable.toString(), "-jar", serverJar.toString(), Integer.toString(port))
                .redirectErrorStream(true)
                .start();
        return new StartedServer(process, new ServerOutputCollector(process.getInputStream()));
    }

    private static Path javaExecutable() {
        String executable = System.getProperty("os.name", "").toLowerCase().contains("win")
                ? "java.exe" : "java";
        return Path.of(System.getProperty("java.home"), "bin", executable);
    }

    /**
     * The module class itself can be loaded from a temporary JAR copy, so its
     * location cannot reliably identify sibling runtime JARs. This resource is
     * unique to the packaged LSP server and is resolved by the module loader.
     */
    private static Path findServerJar() throws IOException {
        URL markerResource = GenericGraalVMLanguageServer.class.getClassLoader()
                .getResource(SERVER_MARKER_RESOURCE);
        if (markerResource == null) {
            throw new IOException("The packaged DSL4GA LSP server resource was not found: "
                    + SERVER_MARKER_RESOURCE);
        }
        try {
            if (!(markerResource.openConnection() instanceof JarURLConnection jarConnection)) {
                throw new IOException("The GA LSP server resource is not packaged in a JAR: " + markerResource);
            }
            return Path.of(jarConnection.getJarFileURL().toURI());
        } catch (URISyntaxException ex) {
            throw new IOException("Cannot resolve the GA LSP server JAR: " + markerResource, ex);
        }
    }

    private static Socket connectToServer(Process process, ServerOutputCollector serverOutput, int port)
            throws IOException, InterruptedException {
        long deadline = System.nanoTime() + STARTUP_TIMEOUT_MILLIS * 1_000_000L;
        IOException lastFailure = null;

        while (System.nanoTime() < deadline) {
            if (!process.isAlive()) {
                serverOutput.awaitEnd();
                throw new IOException("The GA language server terminated during startup (exit code "
                        + process.exitValue() + ") on port " + port + ": " + serverOutput.snapshot());
            }
            Socket socket = new Socket();
            try {
                socket.connect(new java.net.InetSocketAddress("127.0.0.1", port), 250);
                return socket;
            } catch (IOException ex) {
                lastFailure = ex;
                closeQuietly(socket);
                Thread.sleep(STARTUP_RETRY_DELAY_MILLIS);
            }
        }
        throw new IOException("Timed out waiting for the GA language server on port " + port
                + ". Last server output: " + serverOutput.snapshot(), lastFailure);
    }

    /**
     * Continuously drains the child process output so the operating-system pipe
     * cannot fill and block the language server. The retained tail makes startup
     * failures actionable without showing ordinary server output in the UI.
     */
    private static final class ServerOutputCollector {

        private static final int MAX_RETAINED_CHARACTERS = 64 * 1024;

        private final StringBuilder output = new StringBuilder();
        private final Thread readerThread;

        ServerOutputCollector(InputStream input) {
            readerThread = new Thread(() -> drain(input), "GA-LSP-output");
            readerThread.setDaemon(true);
            readerThread.start();
        }

        private void drain(InputStream input) {
            try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                char[] buffer = new char[1024];
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    String chunk = new String(buffer, 0, read);
                    append(chunk);
                    LOG.log(Level.FINE, "[GA-LSP] {0}", chunk);
                }
            } catch (IOException ex) {
                LOG.log(Level.FINE, "Cannot read GA language server output.", ex);
            }
        }

        private synchronized void append(String chunk) {
            output.append(chunk);
            int excess = output.length() - MAX_RETAINED_CHARACTERS;
            if (excess > 0) {
                output.delete(0, excess);
            }
        }

        synchronized String snapshot() {
            String result = output.toString().strip();
            return result.isEmpty() ? "No server output was produced." : result;
        }

        void awaitEnd() throws InterruptedException {
            readerThread.join(500);
        }
    }

    private record StartedServer(Process process, ServerOutputCollector output) {
    }

    private static void closeQuietly(Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ex) {
            LOG.log(Level.FINE, "Cannot close GA language server socket.", ex);
        }
    }
}
