package de.orat.math.netbeans.ga;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Adapts the TCP transport to the {@link Process} lifecycle expected by the
 * NetBeans LSP SPI. Closing the binding must close both resources.
 */
final class SocketProcess extends Process {

    private final Process child;
    private final Socket socket;

    SocketProcess(Process child, Socket socket) {
        this.child = child;
        this.socket = socket;
    }

    @Override
    public OutputStream getOutputStream() {
        try {
            return socket.getOutputStream();
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot access GA language server socket output.", ex);
        }
    }

    @Override
    public InputStream getInputStream() {
        try {
            return socket.getInputStream();
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot access GA language server socket input.", ex);
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
    public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
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
            // Closing a binding is best effort; the child process is still terminated.
        }
    }
}
