package de.orat.math.netbeans.ga.lsp;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;

public final class GaLspMain {

    public static void main(String[] args) throws Exception {

        String port = args[0];

        System.err.println(
                "[GA-LSP] Java: "
                + System.getProperty("java.version")
        );

        System.err.println(
                "[GA-LSP] java.home: "
                + System.getProperty("java.home")
        );

        try (Engine engine = Engine.create()) {

            System.err.println("[GA-LSP] Languages:");

            engine.getLanguages().forEach(
                    (id, language) ->
                            System.err.println(
                                    "  "
                                    + id
                                    + " = "
                                    + language.getName()
                            )
            );

            System.err.println("[GA-LSP] Instruments:");

            engine.getInstruments().forEach(
                    (id, instrument) ->
                            System.err.println(
                                    "  "
                                    + id
                                    + " = "
                                    + instrument.getName()
                            )
            );
        }

        try (Context context = Context.newBuilder()
                .allowExperimentalOptions(true)
                .option("lsp", "127.0.0.1:" + port)
                .build()) {

            System.err.println(
                    "[GA-LSP] Starting LSP on 127.0.0.1:"
                    + port
            );

            Thread.currentThread().join();
        }
    }

    private GaLspMain() {
    }
}
