package de.orat.math.netbeans.ga;

import de.orat.math.netbeans.ga.utils.GaFileUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.JarURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.util.Lookup;

/** Starts the GraalVM LSP in an isolated JVM and connects NetBeans by socket. */
@MimeRegistration(mimeType = GaFileUtils.MIME_TYPE, service = LanguageServerProvider.class)
public final class GenericGraalVMLanguageServer implements LanguageServerProvider {

    private static final Logger LOG = Logger.getLogger(GenericGraalVMLanguageServer.class.getName());
    private static final int STARTUP_TIMEOUT_MILLIS = 15_000;
    private static final int STARTUP_RETRY_DELAY_MILLIS = 50;
    private static final String SERVER_MARKER_RESOURCE = "META-INF/dsl4ga-lsp-server.properties";
    private static final String SERVER_MARKER_ARTIFACT = "DSL4GA_LSP";
    private static final String SERVER_MARKER_FORMAT_VERSION = "1";
    private static final String SERVER_MAIN_CLASS = "de.orat.math.graalvmlsp.GraalVMLSPStarter";

    @Override
    public synchronized LanguageServerDescription startServer(Lookup lookup) {
        Process process = null;
        Socket socket = null;
        try {
            int port = findFreeLoopbackPort();
            StartedServer startedServer = startServerProcess(port);
            process = startedServer.process();
            socket = connectToServer(startedServer, port);
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
        } catch (RuntimeException ex) {
            closeQuietly(socket);
            if (process != null) {
                process.destroyForcibly();
            }
            LOG.log(Level.WARNING, "Unexpected failure while starting the external GA language server.", ex);
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
        ServerOutputCollector output = new ServerOutputCollector(process.getInputStream(),
            line -> LOG.log(Level.INFO, "[GA-LSP] {0}", line));
        return new StartedServer(process, output, serverJar, javaExecutable);
    }

    static Path javaExecutable() throws IOException {
        String executable = System.getProperty("os.name", "").toLowerCase().contains("win")
                ? "java.exe" : "java";
        Path result = Path.of(System.getProperty("java.home"), "bin", executable);
        validateJavaExecutable(result);
        return result;
    }

    static void validateJavaExecutable(Path javaExecutable) throws IOException {
        if (!Files.isRegularFile(javaExecutable) || !Files.isExecutable(javaExecutable)) {
            throw new IOException("No executable Java runtime was found at " + javaExecutable + ".");
        }
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
            validateServerMarker(markerResource);
            if (!(markerResource.openConnection() instanceof JarURLConnection jarConnection)) {
                throw new IOException("The GA LSP server resource is not packaged in a JAR: " + markerResource);
            }
            return Path.of(jarConnection.getJarFileURL().toURI());
        } catch (URISyntaxException ex) {
            throw new IOException("Cannot resolve the GA LSP server JAR: " + markerResource, ex);
        }
    }

    static void validateServerMarker(URL markerResource) throws IOException {
        Properties marker = new Properties();
        try (var input = markerResource.openStream()) {
            marker.load(input);
        }
        if (!SERVER_MARKER_ARTIFACT.equals(marker.getProperty("artifact"))
                || !SERVER_MARKER_FORMAT_VERSION.equals(marker.getProperty("format-version"))
                || !SERVER_MAIN_CLASS.equals(marker.getProperty("main-class"))) {
            throw new IOException("The packaged DSL4GA LSP server marker is invalid: " + markerResource);
        }
    }

    private static Socket connectToServer(StartedServer server, int port)
            throws IOException, InterruptedException {
        Process process = server.process();
        ServerOutputCollector serverOutput = server.output();
        long deadline = System.nanoTime() + STARTUP_TIMEOUT_MILLIS * 1_000_000L;
        IOException lastFailure = null;

        while (System.nanoTime() < deadline) {
            if (!process.isAlive()) {
                serverOutput.awaitEnd();
                throw new IOException("The GA language server terminated during startup (exit code "
                        + process.exitValue() + ") " + server.context(port) + ": "
                        + serverOutput.snapshot());
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
        throw new IOException("Timed out waiting for the GA language server " + server.context(port)
                + ". Last server output: " + serverOutput.snapshot(), lastFailure);
    }

    private record StartedServer(Process process, ServerOutputCollector output,
            Path serverJar, Path javaExecutable) {

        String context(int port) {
            return "on port " + port + " (Java: " + javaExecutable + ", server JAR: " + serverJar
                    + ", operating system: " + System.getProperty("os.name", "unknown") + ")";
        }
    }

    private static void closeQuietly(Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot close GA language server socket.", ex);
        }
    }
}
