package de.orat.math.netbeans.ocga;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public final class LSPProxy {

    public static void main(String[] args)
            throws Exception {

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket =
                     new Socket(host, port)) {

            Thread stdinToSocket = new Thread(() -> {
                try {
                    copy(
                        System.in,
                        socket.getOutputStream()
                    );
                } catch (IOException ex) {
                    // connection closed
                }
            });

            Thread socketToStdout = new Thread(() -> {
                try {
                    copy(
                        socket.getInputStream(),
                        System.out
                    );
                } catch (IOException ex) {
                    // connection closed
                }
            });

            stdinToSocket.start();
            socketToStdout.start();

            stdinToSocket.join();
            socketToStdout.join();
        }
    }

    private static void copy(
            InputStream in,
            OutputStream out) throws IOException {

        byte[] buffer = new byte[8192];

        int n;

        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
            out.flush();
        }
    }
}
