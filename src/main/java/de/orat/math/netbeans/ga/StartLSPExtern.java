package de.orat.math.netbeans.ga;

import java.util.Set;
import org.graalvm.polyglot.Context;

/**
 * CAVEAT
 * Has to be invoked via maven with the following commanline arguments:
 * -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 
 * -Dgraalvm.locatorDisabled=true 
 * --add-exports org.graalvm.truffle/com.oracle.truffle.api.nodes=ALL-UNNAMED 
 * --add-exports org.graalvm.truffle/com.oracle.truffle.api.exception=ALL-UNNAMED 
 * --add-exports org.graalvm.truffle/com.oracle.truffle.api=ALL-UNNAMED 
 * --add-exports org.graalvm.truffle/com.oracle.truffle.api.interop=ALL-UNNAMED
 * 
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class StartLSPExtern {
    
    public static void main(String[] args){
        
        try {
            Context context = Context.newBuilder("ga").allowAllAccess(true)
               .allowExperimentalOptions(true).option("lsp", "true").build();//) {
            // https://www.graalvm.org/latest/reference-manual/embed-languages/#dependency-setup
            System.out.println("Context created!");
            Set<String> languages = context.getEngine().getLanguages().keySet();
            for (String id : languages) {
                System.out.println("found \""+id+"\"!");
            }
        } catch (Exception e){
            e.printStackTrace(System.out);
            System.out.println(e.getMessage());
        }
    }
}
