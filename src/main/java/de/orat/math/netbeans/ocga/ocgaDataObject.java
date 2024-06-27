package de.orat.math.netbeans.ocga;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

import org.netbeans.modules.textmate.lexer.api.GrammarRegistration;

@Messages({
    "LBL_ocga_LOADER=Files of ocga"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_ocga_LOADER",
        mimeType = "text/x-ocga",
        extension = {"ocga"}
)
@DataObject.Registration(
        mimeType = "text/x-ocga",
        iconBase = "de/orat/math/netbeans/ocga/Letter-G-lg-icon.png",
        displayName = "#LBL_ocga_LOADER",
        position = 300
)
@GrammarRegistration(grammar="ocga.tmLanguage.json", mimeType="text/x-ocga")
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/x-ocga/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/x-ocga/Actions",
            id = @ActionID(category = "Project", id = "org.netbeans.modules.project.ui.RunSingle"),
            position = 230
    ),
    @ActionReference(
            path = "Loaders/text/x-ocga/Actions",
            id = @ActionID(category = "Debug", id = "org.netbeans.modules.debugger.ui.actions.DebugFileAction"),
            position = 270,
            separatorAfter = 290
    ),
    @ActionReference(
            path = "Loaders/text/x-ocga/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/x-ocga/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/x-ocga/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/x-ocga/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/x-ocga/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/text/x-ocga/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/text/x-ocga/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/text/x-ocga/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    ),
    
    // editor popups
    @ActionReference(
            path = "Editors/text/x-ocga/Popup",
            id = @ActionID(category = "Project", id = "org.netbeans.modules.project.ui.RunSingle"),
            position = 30
    ),
    @ActionReference(
            path = "Editors/text/x-ocga/Popup",
            id = @ActionID(category = "Debug", id = "org.netbeans.modules.debugger.ui.actions.DebugFileAction"),
            position = 70,
            separatorAfter = 90
    )
})
public class ocgaDataObject extends MultiDataObject {

    public ocgaDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor("text/x-ocga", true);
        registerTruffleMimeType("text/x-ocga");
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @MultiViewElement.Registration(
            displayName = "#LBL_ocga_EDITOR",
            iconBase = "de/orat/math/netbeans/ocga/Letter-G-lg-icon.png",
            mimeType = "text/x-ocga",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "ocga",
            position = 1000
    )
    @Messages("LBL_ocga_EDITOR=Source")
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }
    
    private void registerTruffleMimeType(String mime) throws IOException {
        ClassLoader all = Lookup.getDefault().lookup(ClassLoader.class);
        if (all == null) {
            all = ocgaDataObject.class.getClassLoader();
        }
        try {
            var clazz = all.loadClass("org.netbeans.modules.debugger.jpda.truffle.MIMETypes");
            var getDefault = clazz.getMethod("getDefault");
            var mimeTypes = getDefault.invoke(null);
            var get = clazz.getMethod("get");
            var toSet = (Set<String>)get.invoke(mimeTypes);
            toSet.add(mime);
        } catch (ReflectiveOperationException ex) {
            Installer.LOG.log(Level.WARNING, "Cannot register breakpoints for Enso", ex);
        }
    }

}
