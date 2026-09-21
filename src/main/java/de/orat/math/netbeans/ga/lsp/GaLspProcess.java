package de.orat.math.netbeans.ga.lsp;

/**
 * NEU
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class GaLspProcess {

    private final Process process;
    private final Socket socket;

    private GaLspProcess(Process process, Socket socket) {
        this.process = process;
        this.socket = socket;
    }

    public static GaLspProcess start() throws Exception {
        int port = findFreePort();

        Path java = Path.of(
                System.getProperty("java.home"),
                "bin",
                isWindows() ? "java.exe" : "java"
        );

        List<Path> runtimeJars = GaLspRuntime.findRuntimeJars();

        String classPath = buildClasspath(runtimeJars);

        List<String> command = new ArrayList<>();

        command.add(java.toString());
        command.add("-cp");
        command.add(classPath);

        /*
         * Child JVM entry point.
         */
        command.add(GaLspMain.class.getName());

        command.add(Integer.toString(port));

        Process process = new ProcessBuilder(command)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start();

        Socket socket = connectWhenReady(process, port);

        return new GaLspProcess(process, socket);
    }

    public Process process() {
        return process;
    }

    public Socket socket() {
        return socket;
    }

    public void destroy() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }

        process.destroy();

        if (process.isAlive()) {
            process.destroyForcibly();
        }
    }

    private static String buildClasspath(List<Path> jars) {
        /*
         * We also need the jar containing GaLspMain itself.
         *
         * This assumes GaLspMain is packaged in the module's
         * runtime jar and that the current class's code source
         * points at that jar.
         */
        try {
            Path bootstrapJar = Path.of(
                    GaLspMain.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

            List<String> entries = new ArrayList<>();

            entries.add(bootstrapJar.toString());

            for (Path jar : jars) {
                entries.add(jar.toString());
            }

            return String.join(
                    File.pathSeparator,
                    entries
            );

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Cannot construct child JVM classpath",
                    ex
            );
        }
    }

    private static Socket connectWhenReady(
            Process process,
            int port
    ) throws Exception {

        long deadline = System.currentTimeMillis() + 15_000;

        while (System.currentTimeMillis() < deadline) {

            if (!process.isAlive()) {
                throw new IOException(
                        "Graal LSP JVM terminated with exit code "
                        + process.exitValue()
                );
            }

            try {
                Socket socket = new Socket();

                socket.connect(
                        new InetSocketAddress(
                                "127.0.0.1",
                                port
                        ),
                        250
                );

                return socket;

            } catch (IOException ex) {
                Thread.sleep(50);
            }
        }

        process.destroyForcibly();

        throw new IOException(
                "Timed out waiting for Graal LSP on port " + port
        );
    }

    private static int findFreePort() throws IOException {
        try (java.net.ServerSocket socket =
                     new java.net.ServerSocket(0)) {

            return socket.getLocalPort();
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name")
                .toLowerCase()
                .contains("win");
    }
}
