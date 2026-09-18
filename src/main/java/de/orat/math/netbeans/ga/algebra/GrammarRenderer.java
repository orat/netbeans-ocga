package de.orat.math.netbeans.ga.algebra;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/** Renders one generic grammar with a constant context for each known algebra. */
final class GrammarRenderer {

    private static final String RESOURCE_ROOT = "de/orat/math/netbeans/ga/generated";
    private static final String ALGEBRA_CONTEXTS_PLACEHOLDER
            = "{ \"name\": \"meta.template.generated-algebra-contexts.ga\", \"match\": \"(?!)\" }";
    private final String template;

    GrammarRenderer() throws IOException {
        template = loadTemplate();
    }

    void render(Path classesDirectory, Map<String, Set<String>> constantsByAlgebra) throws IOException {
        // This valid but never-matching rule keeps the JSON template editable in NetBeans.
        String grammar = template.replace(ALGEBRA_CONTEXTS_PLACEHOLDER, algebraContexts(constantsByAlgebra));
        // The source tree contains a minimal resource at this path so NetBeans'
        // annotation processor can validate the registration during compilation.
        // Packaging replaces that placeholder in target/classes with this grammar.
        Path output = classesDirectory.resolve(RESOURCE_ROOT).resolve("ga.tmLanguage.json");
        Files.createDirectories(output.getParent());
        Files.writeString(output, grammar, StandardCharsets.UTF_8);
    }

    private static String loadTemplate() throws IOException {
        try (InputStream stream = GrammarRenderer.class.getResourceAsStream("/de/orat/math/netbeans/ga/ga.tmLanguage.template.json")) {
            if (stream == null) {
                throw new IOException("Missing TextMate grammar template.");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String algebraContexts(Map<String, Set<String>> constantsByAlgebra) {
        return constantsByAlgebra.entrySet().stream()
                .map(entry -> algebraContext(entry.getKey(), entry.getValue()))
                .reduce((left, right) -> left + ",\n      " + right)
                .orElse("");
    }

    private static String algebraContext(String algebraId, Set<String> constants) {
        String escapedId = escapeRegexLiteral(algebraId);
        return """
                {
                  "begin": "^\\\\s*#algebra\\\\s+%s\\\\s*$",
                  "beginCaptures": { "0": { "name": "comment.line.algebra-header.ga" } },
                  "end": "\\\\z",
                  "patterns": [
                    { "include": "#common" },
                    { "name": "constant.language.algebra.ga", "match": "%s" }
                  ]
                }""".formatted(escapedId, constantsPattern(constants)).strip();
    }

    private static String constantsPattern(Set<String> constants) {
        String alternatives = constants.stream()
                // A longer name must be matched before a shorter prefix of it.
                .sorted(Comparator.comparingInt(String::length).reversed())
                .map(GrammarRenderer::escapeRegexLiteral)
                .reduce((left, right) -> left + "|" + right)
                .orElse("(?!)");
        return "(?<!\\\\w)(?:" + alternatives + ")(?!\\\\w)";
    }

    private static String escapeRegexLiteral(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if ("\\.^$|?*+()[]{}".indexOf(character) >= 0) {
                escaped.append('\\');
            }
            escaped.append(character);
        }
        return escaped.toString();
    }
}
