package de.orat.math.netbeans.ocga;

import org.graalvm.polyglot.Context;

// The Polyglot API explicitly supports creating contexts and passing 
// language/tool options programmatically.

// Your language itself should be registered through the normal Truffle language 
// registration mechanism. You don't need to turn it into a CLI executable.
public final class LSPBootstrap {

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(args[0]);

        Context context = Context.newBuilder("mylang")
                .allowAllAccess(true)
                .allowExperimentalOptions(true)
                .build();

        /*
         * This causes your Truffle language to be loaded.
         */
        context.initialize("mylang");

        /*
         * Start Generic LSP.
         */
        startLsp(context, port);
    }

    private static void startLsp(
            Context context,
            int port) throws Exception {

        // See section below.
    }
}
