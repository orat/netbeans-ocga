package de.orat.math.netbeans.ga;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

public class Installer extends ModuleInstall {
    static final Logger LOG = Logger.getLogger(GaDataObject.class.getName());
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
            LOG.log(Level.WARNING, "Cannot register breakpoints for ga", ex);
        }
    }
}