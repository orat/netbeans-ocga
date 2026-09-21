package de.orat.math.netbeans.ocga;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

// However, don't use ServerSocket(0) followed by closing it as your production 
// implementation without accounting for the race. Ideally the bootstrap receives 
// a port selected by NetBeans and immediately binds to it.

public final class GraalLspProcess {

    private Process process;

    private int port;

    public void start() throws Exception {
        port = findFreePort();

        // ProcessBuilder pb = new ProcessBuilder(
        //graalHome.resolve("bin").resolve("polyglot").toString(),
        //"--experimental-options",
        //"--lsp",
        //"--port=" + port

        ProcessBuilder pb = new ProcessBuilder(
                graalJava().toString(),

                // Your bootstrap class:
                "-cp",
                languageClasspath(),

                "com.example.mylang.lsp.LspBootstrap",

                Integer.toString(port)
        );

        // NEVER merge this into stdout.
        //
        // stdout/stderr may be used by the LSP bridge.
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        process = pb.start();

        waitForServer(port);
    }

    private Path graalJava() {
        return Path.of(
                System.getProperty("graalvm.home"),
                "bin",
                "java"
        );
    }

    private String languageClasspath() {
        return System.getProperty("mylang.language.classpath");
    }

    private int findFreePort() throws IOException {
        try (java.net.ServerSocket socket =
                     new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private void waitForServer(int port)
            throws InterruptedException {

        // Replace with a proper connection retry loop.
        //
        // The important point is that we do not tell NetBeans
        // the server is ready until the LSP socket accepts connections.
    }

    public int getPort() {
        return port;
    }

    public Process getProcess() {
        return process;
    }
}