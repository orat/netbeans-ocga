package de.orat.math.netbeans.ocga;
import java.io.IOException;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

// https://blogsarchive.apache.org/netbeans/entry/lsp-client-demo-ba-sh
// --> LSP Client module scheint es nicht mehr zu geben um LanguageServerProvider
// zur Verfügung zu stellen

// https://blogsarchive.apache.org/netbeans/entry/lsp-client-demo-ba-sh

@MimeRegistration(mimeType="text/sh", service=LanguageServerProvider.class)
public class OcgaClient implements LanguageServerProvider {

   @Override
   public LanguageServerDescription startServer(Lookup lkp) {
       try {
           Process p = new ProcessBuilder("/bin/main.js", "start").start();
           return LanguageServerDescription.create(p.getInputStream(), p.getOutputStream(), p);
       } catch (IOException ex) {
           Exceptions.printStackTrace(ex);
           return null;
       }
   }

}