package de.orat.math.netbeans.ocga;

import java.util.List;
import javax.swing.text.Document;
// https://github.com/enso-org/enso/blob/develop/lib/rust/parser/generate-java/java/org/enso/syntax2/Parser.java
// https://github.com/enso-org/enso/blob/develop/engine/runtime-parser/src/main/java/org/enso/compiler/core/EnsoParser.java
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lsp.StructureElement;
import org.netbeans.spi.lsp.StructureProvider;

// wird vermutlich nur vom LSPServer gebraucht
//@MimeRegistration(mimeType = "text/x-ocga", service = StructureProvider.class)
public final class OcgaStructure implements StructureProvider {

    @Override
    public List<StructureElement> getStructure(Document dcmnt) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    static List<StructureElement> collectStructure(Document dcmt){
        return null;
        //TODO
    }
}