package de.orat.math.netbeans.ga.algebra;

import de.orat.math.gacalc.api.GAFactory;
import de.orat.math.gacalc.api.GAServiceLoader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Generates the shared TextMate grammar during packaging. */
public final class GaGrammarGenerator {

    private GaGrammarGenerator() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected the Maven output directory as the only argument.");
        }
        Path classesDirectory = Path.of(args[0]);
        Map<String, Set<String>> constantsByAlgebra = new LinkedHashMap<>();
        for (GAFactory factory : GAServiceLoader.instance().getGAFactories()) {
            // Several implementations can provide the same algebra. The grammar
            // has one context per algebra ID, so it highlights their combined constants.
            constantsByAlgebra
                    .computeIfAbsent(factory.getAlgebra(), ignored -> new LinkedHashSet<>())
                    .addAll(factory.getConstants().keySet());
        }
        new GrammarRenderer().render(classesDirectory, constantsByAlgebra);
    }
}
