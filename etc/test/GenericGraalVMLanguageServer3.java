package de.orat.math.netbeans.ocga;

import de.orat.math.netbeans.ocga.GraalLspProcess;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider.LanguageServerDescription;
import org.openide.util.Lookup;

// NetBeans 31 provides the LSP SPI under org.netbeans.spi.lsp and the client 
// SPI under org.netbeans.modules.lsp.client.spi.

public final class GenericGraalVMLanguageServer3
        implements LanguageServerProvider {

    private static final Logger LOG =
            Logger.getLogger(GenericGraalVMLanguageServer3.class.getName());

    private final AtomicReference<GraalLspProcess> server =
            new AtomicReference<>();

    @Override
    public LanguageServerDescription startServer(Lookup lookup) {
        try {
            GraalLspProcess p = new GraalLspProcess();

            p.start();

            server.set(p);

            return p.createLanguageServerDescription();

        } catch (Exception ex) {
            LOG.log(Level.SEVERE,
                    "Could not start MyLanguage Graal LSP", ex);
            return null;
        }
    }
}