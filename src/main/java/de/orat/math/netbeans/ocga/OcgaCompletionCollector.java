/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.orat.math.netbeans.ocga;

import java.util.function.Consumer;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lsp.Completion;
import org.netbeans.spi.lsp.CompletionCollector;

/**
 *
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
@MimeRegistration(mimeType = "text/x-ocga", service = CompletionCollector.class)
public class OcgaCompletionCollector implements CompletionCollector {
  @Override
  public boolean collectCompletions(Document dcmnt, int i, Completion.Context cntxt, Consumer<Completion> cnsmr) {
    for (var e : OcgaStructure.collectStructure(dcmnt)) {
        cnsmr.accept(CompletionCollector.newBuilder(e.getName()).build());
    }
    return true;
  }

}