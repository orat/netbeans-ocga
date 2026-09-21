package de.orat.math.netbeans.ga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class SocketProcessTest {

    @Test
    void delegatesProcessInformationAndClosesSocketOnDestroy() throws Exception {
        try (Socket socket = new Socket()) {
            ControlledProcess child = new ControlledProcess();
            SocketProcess process = new SocketProcess(child, socket);

            assertEquals(42, process.pid());
            assertTrue(process.info().command().isPresent());
            assertEquals(child.toHandle().pid(), process.toHandle().pid());
            process.destroy();

            assertFalse(child.isAlive());
            assertTrue(socket.isClosed());
            assertSame(process, process.onExit().get(1, TimeUnit.SECONDS));
        }
    }

    private static final class ControlledProcess extends Process {

        private final CompletableFuture<Process> onExit = new CompletableFuture<>();
        private boolean alive = true;

        @Override
        public OutputStream getOutputStream() {
            return new ByteArrayOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return InputStream.nullInputStream();
        }

        @Override
        public InputStream getErrorStream() {
            return InputStream.nullInputStream();
        }

        @Override
        public int waitFor() {
            destroy();
            return 0;
        }

        @Override
        public int exitValue() {
            return alive ? 1 : 0;
        }

        @Override
        public void destroy() {
            alive = false;
            onExit.complete(this);
        }

        @Override
        public boolean isAlive() {
            return alive;
        }

        @Override
        public long pid() {
            return 42;
        }

        @Override
        public ProcessHandle.Info info() {
            return ProcessHandle.current().info();
        }

        @Override
        public ProcessHandle toHandle() {
            return ProcessHandle.current();
        }

        @Override
        public CompletableFuture<Process> onExit() {
            return onExit;
        }
    }
}
