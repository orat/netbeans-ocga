package de.orat.math.netbeans.ga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ServerOutputCollectorTest {

    @Test
    void emitsCompleteAndFinalPartialLines() throws Exception {
        List<String> lines = new ArrayList<>();
        ServerOutputCollector collector = new ServerOutputCollector(
                new ByteArrayInputStream("first\r\nsecond\nlast".getBytes(StandardCharsets.UTF_8)), lines::add);

        collector.awaitEnd();

        assertEquals(List.of("first", "second", "last"), lines);
        assertEquals("first\r\nsecond\nlast", collector.snapshot());
    }

    @Test
    void retainsOnlyDiagnosticTail() throws Exception {
        String input = "discarded-prefix" + "x".repeat(ServerOutputCollector.MAX_RETAINED_CHARACTERS);
        ServerOutputCollector collector = new ServerOutputCollector(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), ignored -> {
                });

        collector.awaitEnd();

        assertEquals(ServerOutputCollector.MAX_RETAINED_CHARACTERS, collector.snapshot().length());
        assertFalse(collector.snapshot().contains("discarded-prefix"));
    }
}
