package de.orat.math.netbeans.ga;

import static de.dhbw.rahmlab.dsl4ga.impl.truffle.common.runtime.GeomAlgeLang.LANGUAGE_ID;
import de.orat.math.netbeans.ga.utils.GaFileUtils;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Instrument;

// be careful: This interface must be defined inside this project
import org.graalvm.tools.lsp.api.StreamConfigurationService;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Instead of relying on the "lsp.Port" option string, we start the Context with 
 * "lsp=true", extract the underlying Instrument instance, and pass the piped 
 * streams directly to it via environment services or direct invocation depending 
 * on your specific version architecture. Alternatively, you can use GraalVM's 
 * dedicated MessageEndpoint API or standard pipe redirectors.
 * This is a robust, network-free implementation.
 */

/**
 * Caveat: The language server is started only for files that are inside any project.
 */
@MimeRegistration(mimeType=GaFileUtils.GA_MIME_TYPE, service=LanguageServerProvider.class)
public class GenericGraalVMLanguageServer implements LanguageServerProvider {
    
    private static Context polyglotContext;

    @Override
    public synchronized LanguageServerDescription startServer(Lookup lookup) {
        try {
            // 1. Setup NetBeans <=> GraalVM in-memory pipes
            // NetBeans writes to netbeansOut -> GraalVM reads from graalvmIn
            PipedOutputStream netbeansOut = new PipedOutputStream();
            PipedInputStream graalvmIn = new PipedInputStream(netbeansOut);

            // GraalVM writes to graalvmOut -> NetBeans reads from netbeansIn
            PipedOutputStream graalvmOut = new PipedOutputStream();
            PipedInputStream netbeansIn = new PipedInputStream(graalvmOut);

            // 2. Initialize the GraalVM context enabling the LSP tool without network strings
            polyglotContext = Context.newBuilder()
                    .allowAllAccess(true)
                    .allowExperimentalOptions(true)
                    .option("lsp", "true") 
                    .build();

            // Force initialization of your custom language
            polyglotContext.initialize(LANGUAGE_ID);

            // 3. Extract the LSP Instrument instance directly from the engine
            Instrument lspInstrument = polyglotContext.getEngine().getInstruments().get("lsp");
            if (lspInstrument == null) {
                throw new IllegalStateException("GraalVM LSP tool ('lsp-tool' artifact) is missing from the classpath.");
            }

            // 4. Pass the streams directly into the GraalVM LSP loop
            // We cast or lookup the service interface to feed custom I/O handles
            // (GraalVM provides an internal provider class to bind standard streams)
            Thread lspBridgeThread = new Thread(() -> {
                try {
                    // Use GraalVM's internal dynamic service provider to launch with specific streams
                    lspInstrument.lookup(StreamConfigurationService.class)
                                 .startServer(graalvmIn, graalvmOut);
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }, "GraalVM-LSP-Pipe-Bridge");
            
            lspBridgeThread.setDaemon(true);
            lspBridgeThread.start();

            // 5. Provide the matching endpoints back to NetBeans
            return LanguageServerDescription.create(netbeansIn, netbeansOut, null);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
}