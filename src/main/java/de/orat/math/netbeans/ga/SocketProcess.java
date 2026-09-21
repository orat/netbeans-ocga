package de.orat.math.netbeans.ga;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Adapts the TCP transport to the {@link Process} lifecycle expected by the
 * NetBeans LSP SPI. Closing the binding must close both resources.
 */
final class SocketProcess extends Process {

    private static final Duration GRACEFUL_SHUTDOWN_TIMEOUT = Duration.ofSeconds(2);

    private final Process child;
    private final Socket socket;
    private final AtomicBoolean shutdownRequested = new AtomicBoolean();

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
        forceTerminationAfterGracePeriod();
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

    @Override
    public long pid() {
        return child.pid();
    }

    @Override
    public ProcessHandle.Info info() {
        return child.info();
    }

    @Override
    public ProcessHandle toHandle() {
        return child.toHandle();
    }

    @Override
    public boolean supportsNormalTermination() {
        return child.supportsNormalTermination();
    }

    @Override
    public CompletableFuture<Process> onExit() {
        return child.onExit().thenApply(ignored -> this);
    }

    private void forceTerminationAfterGracePeriod() {
        if (!shutdownRequested.compareAndSet(false, true) || !child.isAlive()) {
            return;
        }
        // Apply the timeout to a dependent future, never to the Process-owned
        // onExit future itself. Callers must still observe the real process exit.
        child.onExit()
                .thenRun(() -> {
                })
                .orTimeout(GRACEFUL_SHUTDOWN_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
                .exceptionally(ex -> {
                    if (child.isAlive()) {
                        child.destroyForcibly();
                    }
                    return null;
                });
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException ignored) {
            // Closing a binding is best effort; the child process is still terminated.
        }
    }
}
