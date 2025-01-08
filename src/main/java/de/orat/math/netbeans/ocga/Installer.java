package de.orat.math.netbeans.ocga;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.graalvm.polyglot.Context;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

public class Installer extends ModuleInstall {
    static final Logger LOG = Logger.getLogger(ocgaDataObject.class.getName());
    private static final long serialVersionUID = 1L;

    @Override
    public void restored() {
        try {
            ClassLoader all = Lookup.getDefault().lookup(ClassLoader.class);
            if (all == null) {
                all = Installer.class.getClassLoader();
            }
            var clazz = all.loadClass("org.netbeans.modules.project.ui.api.ProjectActionUtils");
            var pref = NbPreferences.forModule(clazz);
            pref.putBoolean("openSubprojects", true);
            
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.WARNING, "Cannot register breakpoints for ocga", ex);
        }
        
        
        // Language Server starten

        /*try {
            Map<String, String> options = new HashMap<>();
            // -J-Dgraalvm.locatorDisabled=true -J--add-exports=org.graalvm.truffle/com.oracle.truffle.api.nodes=ALL-UNNAMED 
            // -J--add-exports=org.graalvm.truffle/com.oracle.truffle.api.exception=ALL-UNNAMED 
            // -J--add-exports=org.graalvm.truffle/com.oracle.truffle.api=ALL-UNNAMED 
            // -J--add-exports=org.graalvm.truffle/com.oracle.truffle.api.interop=ALL-UNNAMED
            options.put("graalvm.locatorDisabled","true");
            options.put("add-exports","org.graalvm.truffle/com.oracle.truffle.api.nodes=ALL-UNNAMED");
            options.put("add-exports","org.graalvm.truffle/com.oracle.truffle.api.exception=ALL-UNNAMED");
            options.put("add-exports","org.graalvm.truffle/com.oracle.truffle.api.interop=ALL-UNNAMED");
            System.out.println("Installer: Try to get the graalvm context...");
            Context context = Context.newBuilder("js").options(options).allowAllAccess(true)
               .allowExperimentalOptions(true).option("lsp", "true").build();//) {
            // mit .option("","true") kann ich alle benötigten Optionen hineinschleusen
            // ohne dies bereits beim Start von Netbeans übergeben zu müssen...
            // https://www.graalvm.org/latest/reference-manual/embed-languages/#dependency-setup
            // java.util.ServiceConfigurationError: com.oracle.truffle.api.provider.InternalResourceProvider: 
            // com.oracle.truffle.runtime.LibTruffleAttachResourceProvider not a subtype
            // hier komme ich nicht mehr an!
            System.out.println("GA Context created!");
            Set<String> languages = context.getEngine().getLanguages().keySet();
            for (String id : languages) {
                System.out.println("found \""+id+"\"!");
            }
            
        //} 
        } catch (Exception e){
            System.out.println("Failed to get the graalvm context: "+e.getMessage());
            //String errorMessage = ExceptionUtils.getStackTrace(e); 
            //System.out.println(errorMessage);
            e.printStackTrace();
        }*/
    }
}