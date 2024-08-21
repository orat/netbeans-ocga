package de.orat.math.netbeans.ocga;

import com.oracle.truffle.api.instrumentation.TruffleInstrument.Env;
import java.io.IOException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Context.Builder;
import org.graalvm.polyglot.Instrument;
import org.graalvm.tools.lsp.instrument.EnvironmentProvider;
import org.graalvm.tools.lsp.instrument.LSPInstrument;
import org.graalvm.tools.lsp.server.TruffleAdapter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

// https://github.com/oracle/graal/blob/master/tools/README.md
//@MimeRegistration(mimeType="text/x-ocga", service=LanguageServerProvider.class)
// TODO herausfinden, wie ich einen Language-server aufbaue und bundle, den ich dann
// mit vergleichbarem code zu dieser Klasse starten kann
// https://www.graalvm.org/latest/tools/lsp/
// GraalVM unterstützt bereits einen LSP-Server für Gastsprachen mit code-completion
// find-usages, etc. 
// TODO Wie kann ich diesen starten?
// Die Idee ist dann alles was spezifisch für die eigenen DSL ist an einen eigenen
// zusätzlichen LSP-Server zu delegieren
// https://github.com/enso-org/enso/tree/develop/tools/enso4igv/src/main/java/org/enso/tools/enso4igv
// da findet sich keine vergleichbare Klasse, aber enso arbeitet trotzdem mit dem GraalVM LSP zusammen
// warum geht das?

// GraalVM's Language Server Protocol implementation can delegate to the existing 
// language servers written specially for the particular languages 
// (using the --lsp.Delegates launcher option) and merge the static data returned
// from these servers with its own dynamic data to a single result.
// https://www.graalvm.org/22.1/tools/vscode/graalvm-extension/polyglot-runtime/
public class LSPSupport implements LanguageServerProvider {

   // Caveat: the language server is started only for files that are inside a project, 
   // so create (any) new project, and put the file with the DSL code in a subfolder
   // of this project
   //@Override
   public LanguageServerDescription startServerOrig(Lookup lkp) {
       try {
           //Process p = new ProcessBuilder("/bin/main.js", "start").start();
           /*Process p = new ProcessBuilder(
            "/usr/local/bin/node",
            "/bin/main.js",
            "start").start();*/
           // entsprechend
           // https://www.graalvm.org/latest/tools/lsp/
           // mit Optionen --experimental-options --lsp
           // TODO
           // wohin die Optionen?
           Process p = new ProcessBuilder(
            "/home/oliver/JAVA/PROJECTS/DSL4GeometricAlgebra/ga",
            "-lsp"," /home/oliver/JAVA/PROJECTS/DSL4GeometricAlgebra/dummy.ocga").start();
           return LanguageServerDescription.create(p.getInputStream(), p.getOutputStream(), p);
       } catch (IOException ex) {
           Exceptions.printStackTrace(ex);
           return null;
       }
   }
   
   // LS starten ohne über die Kommandozeile zu gehen:
   // https://github.com/oracle/graal/blob/master/tools/src/org.graalvm.tools.lsp.test/src/org/graalvm/tools/lsp/test/server/TruffleLSPTest.java
   //TODO
   // ungetestet
   // Voraussetzung: lsp ist in der GraalVM registriert, d.h. das jar mit der 
   // zugehörigen Implementierung muss via commandline argument in den richtigen
   // class-path eingefügt worden sein.
   @Override
   public LanguageServerDescription startServer(Lookup lkp) {
        Engine engine = Engine.newBuilder().allowExperimentalOptions(true).build();
        Instrument instrument = engine.getInstruments().get("lsp");
        if (instrument != null){
            EnvironmentProvider envProvider = instrument.lookup(EnvironmentProvider.class);
            if (envProvider != null){
                Env env = envProvider.getEnvironment();
                return LanguageServerDescription.create(env.in(), 
                    env.out(), null);
            } else {
                System.err.println("EinvironmentProvider not found!");
            }
        } else {
            System.err.println("lsp instrument not found!");
        }
        return null;
   }

}