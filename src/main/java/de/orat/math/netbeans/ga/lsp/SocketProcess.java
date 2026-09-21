package de.orat.math.netbeans.ga.lsp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

// This avoids creating another process solely for stdio↔TCP forwarding.
// Because NetBeans' LSP SPI wants streams, we can wrap the TCP socket in a Process.
public final class SocketProcess extends Process {

    private final Process child;
    private final Socket socket;

    public SocketProcess(
            Process child,
            Socket socket) {
        this.child = child;
        this.socket = socket;
    }

    @Override
    public OutputStream getOutputStream() {
        try {
            return socket.getOutputStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public InputStream getInputStream() {
        try {
            return socket.getInputStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public InputStream getErrorStream() {
        return child.getErrorStream();
    }

    @Override
    public int waitFor() throws InterruptedException {
        return child.waitFor();
    }

    @Override
    public boolean waitFor(
            long timeout,
            java.util.concurrent.TimeUnit unit
    ) throws InterruptedException {
        return child.waitFor(timeout, unit);
    }

    @Override
    public int exitValue() {
        return child.exitValue();
    }

    @Override
    public void destroy() {
        closeSocket();
        child.destroy();
    }

    @Override
    public Process destroyForcibly() {
        closeSocket();
        child.destroyForcibly();
        return this;
    }

    @Override
    public boolean isAlive() {
        return child.isAlive();
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}