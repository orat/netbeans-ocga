package de.orat.math.netbeans.ga.lsp;

import de.orat.math.netbeans.ga.utils.GaFileUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;

@MimeRegistrations({
    @MimeRegistration(
            mimeType = "text/x-cga",
            service = LanguageServerProvider.class
    ),
    @MimeRegistration(
            mimeType = "text/x-pga",
            service = LanguageServerProvider.class
    ),
    @MimeRegistration(
            mimeType=GaFileUtils.GA_MIME_TYPE, 
            service=LanguageServerProvider.class)

})
public final class GaLanguageServerProvider
        implements LanguageServerProvider {

    @Override
    public LanguageServerDescription startServer(Lookup lookup) {

        System.err.println(">>> GA LanguageServerProvider.startServer()");
        try {
            GaLspProcess lsp = GaLspProcess.start();
            Process socketProcess =
                    new SocketProcess(
                            lsp.process(),
                            lsp.socket()
                    );

            return LanguageServerDescription.create(
                    socketProcess.getInputStream(),
                    socketProcess.getOutputStream(),
                    socketProcess
            );

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
}