package de.orat.math.netbeans.ocga;

import java.io.IOException;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

@MimeRegistration(mimeType="text/x-ocga", service=LanguageServerProvider.class)
public class LSPSupport implements LanguageServerProvider {

   // Caveat: the language server is started only for files that are inside a project, 
   // so create (any) new project, and put the file with the DSL code in a subfolder
   // of this project
   @Override
   public LanguageServerDescription startServer(Lookup lkp) {
       try {
           //Process p = new ProcessBuilder("/bin/main.js", "start").start();
           Process p = new ProcessBuilder(
            "/usr/local/bin/node",
            "/bin/main.js",
            "start").start();
           return LanguageServerDescription.create(p.getInputStream(), p.getOutputStream(), p);
       } catch (IOException ex) {
           Exceptions.printStackTrace(ex);
           return null;
       }
   }

}