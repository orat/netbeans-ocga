package de.orat.math.netbeans.ga;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Drains a child process stream continuously and retains its diagnostic tail.
 * The line consumer receives complete lines and a possible final partial line.
 */
final class ServerOutputCollector {

    static final int MAX_RETAINED_CHARACTERS = 64 * 1024;

    private final StringBuilder output = new StringBuilder();
    private final Thread readerThread;

    ServerOutputCollector(InputStream input, Consumer<String> lineConsumer) {
        readerThread = new Thread(() -> drain(input, lineConsumer), "GA-LSP-output");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void drain(InputStream input, Consumer<String> lineConsumer) {
        StringBuilder pendingLine = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                String chunk = new String(buffer, 0, read);
                append(chunk);
                emitLines(chunk, pendingLine, lineConsumer);
            }
            if (!pendingLine.isEmpty()) {
                lineConsumer.accept(pendingLine.toString());
            }
        } catch (IOException ex) {
            // The caller owns logging so that this helper remains testable.
            lineConsumer.accept("Cannot read GA language server output: " + ex.getMessage());
        }
    }

    private static void emitLines(String chunk, StringBuilder pendingLine, Consumer<String> lineConsumer) {
        for (int index = 0; index < chunk.length(); index++) {
            char character = chunk.charAt(index);
            if (character == '\n') {
                int length = pendingLine.length();
                if (length > 0 && pendingLine.charAt(length - 1) == '\r') {
                    pendingLine.setLength(length - 1);
                }
                lineConsumer.accept(pendingLine.toString());
                pendingLine.setLength(0);
            } else {
                pendingLine.append(character);
            }
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
