package de.orat.math.netbeans.ga;

import de.orat.math.netbeans.ga.utils.GaFileUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.JarURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.util.Lookup;

/** Starts the GraalVM LSP in an isolated JVM and connects NetBeans by socket. */
@MimeRegistration(mimeType = GaFileUtils.GA_MIME_TYPE, service = LanguageServerProvider.class)
public final class GenericGraalVMLanguageServer implements LanguageServerProvider {

    private static final Logger LOG = Logger.getLogger(GenericGraalVMLanguageServer.class.getName());
    private static final int STARTUP_TIMEOUT_MILLIS = 10_000;
    private static final String SERVER_MARKER_RESOURCE
            = "META-INF/services/org.graalvm.polyglot.impl.AbstractPolyglotImpl";

    @Override
    public synchronized LanguageServerDescription startServer(Lookup lookup) {
        Process process = null;
        Socket socket = null;
        try {
            int port = findFreeLoopbackPort();
            process = startServerProcess(port);
            socket = connectToServer(process, port);
            return LanguageServerDescription.create(
                    socket.getInputStream(), socket.getOutputStream(), process);
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

    private static Process startServerProcess(int port) throws IOException {
        Path serverJar = findServerJar();
        Path javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java");

        return new ProcessBuilder(javaExecutable.toString(), "-jar", serverJar.toString(), Integer.toString(port))
                .redirectErrorStream(true)
                .start();
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

    private static Socket connectToServer(Process process, int port)
            throws IOException, InterruptedException {
        long deadline = System.nanoTime() + STARTUP_TIMEOUT_MILLIS * 1_000_000L;
        IOException lastFailure = null;

        while (System.nanoTime() < deadline) {
            if (!process.isAlive()) {
                throw new IOException("The GA language server terminated during startup (exit code "
                        + process.exitValue() + "): " + readServerOutput(process));
            }
            Socket socket = new Socket();
            try {
                socket.connect(new java.net.InetSocketAddress("127.0.0.1", port), 250);
                return socket;
            } catch (IOException ex) {
                lastFailure = ex;
                closeQuietly(socket);
                Thread.sleep(100);
            }
        }
        throw new IOException("Timed out waiting for the GA language server.", lastFailure);
    }

    /** Reads the merged standard output and error only after the server has exited. */
    private static String readServerOutput(Process process) {
        try {
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).strip();
            return output.isEmpty() ? "No server output was produced." : output;
        } catch (IOException ex) {
            return "Server output could not be read: " + ex.getMessage();
        }
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
