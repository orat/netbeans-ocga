package de.orat.math.netbeans.ga;

import java.util.Set;
import org.graalvm.polyglot.Context;

/**
 * CAVEAT
 * Netbeans muss mit den folgende commanline arguments aufgerufen werden:
 * -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 
 * -Dgraalvm.locatorDisabled=true 
 * --add-exports org.graalvm.truffle/com.oracle.truffle.api.nodes=ALL-UNNAMED 
 * --add-exports org.graalvm.truffle/com.oracle.truffle.api.exception=ALL-UNNAMED 
 * --add-exports org.graalvm.truffle/com.oracle.truffle.api=ALL-UNNAMED 
 * --add-exports org.graalvm.truffle/com.oracle.truffle.api.interop=ALL-UNNAMED
 * 
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class Test {
    
    public static void main(String[] args){
        
        try {
            Context context = Context.newBuilder("ga").allowAllAccess(true)
               .allowExperimentalOptions(true).option("lsp", "true").build();//) {
            // mit .option("","true") kann ich alle benötigten Optionen hineinschleusen
            // ohne dies bereits beim Start von Netbeans übergeben zu müssen...
            // https://www.graalvm.org/latest/reference-manual/embed-languages/#dependency-setup
            System.out.println("Context created!");
            Set<String> languages = context.getEngine().getLanguages().keySet();
            for (String id : languages) {
                System.out.println("found \""+id+"\"!");
            }
            /*
            Context created!
            found llvm
            found java
            found js
            found python
            found ruby
            found wasm
            [Graal LSP] Starting server and listening on localhost/127.0.0.1:8123
            */
            
            //TODO
            // herausfinden wie ich an in/out-Stream drankomme etc.
        //} 
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
