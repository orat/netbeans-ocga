package de.orat.math.netbeans.ga;

import de.orat.math.netbeans.ga.api.MimeType;
import de.orat.math.netbeans.ocga.*;
import de.orat.math.netbeans.ga.utils.GaFileUtils;
import java.util.function.Consumer;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lsp.Completion;
import org.netbeans.spi.lsp.CompletionCollector;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
@MimeRegistrations({@MimeRegistration(mimeType = "text/ga-pga", service = CompletionCollector.class),
                    @MimeRegistration(mimeType = "text/ga-cga", service = CompletionCollector.class),
                    @MimeRegistration(mimeType = "text/ga-g6", service = CompletionCollector.class)})
public class GaCompletionCollector implements CompletionCollector {

    @Override
    public boolean collectCompletions(Document dcmnt, int i, Completion.Context cntxt, Consumer<Completion> cnsmr) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        for (var e : GaStructure.collectStructure(dcmnt)) {
            cnsmr.accept(CompletionCollector.newBuilder(e.getName()).build());
        }
        return true;
    }
}