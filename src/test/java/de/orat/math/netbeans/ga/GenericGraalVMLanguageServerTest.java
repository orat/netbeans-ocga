package de.orat.math.netbeans.ga;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GenericGraalVMLanguageServerTest {

    @Test
    void acceptsOwnServerMarker() throws Exception {
        Path marker = Files.createTempFile("dsl4ga-lsp-server", ".properties");
        Files.writeString(marker, "artifact=DSL4GA_LSP\nformat-version=1\n"
                + "main-class=de.orat.math.graalvmlsp.GraalVMLSPStarter\n");

        assertDoesNotThrow(() -> GenericGraalVMLanguageServer.validateServerMarker(marker.toUri().toURL()));
    }

    @Test
    void rejectsInvalidServerMarker() throws Exception {
        Path marker = Files.createTempFile("dsl4ga-lsp-server", ".properties");
        Files.writeString(marker, "artifact=other\nformat-version=1\nmain-class=other.Main\n");

        assertThrows(java.io.IOException.class,
                () -> GenericGraalVMLanguageServer.validateServerMarker(marker.toUri().toURL()));
    }

    @Test
    void validatesConfiguredJavaExecutable() {
        assertDoesNotThrow(GenericGraalVMLanguageServer::javaExecutable);
        assertThrows(java.io.IOException.class,
                () -> GenericGraalVMLanguageServer.validateJavaExecutable(Path.of("missing-java-executable")));
    }
}
