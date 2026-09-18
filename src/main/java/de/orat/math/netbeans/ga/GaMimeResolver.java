package de.orat.math.netbeans.ga;

import de.orat.math.netbeans.ga.utils.GaFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.lookup.ServiceProvider;

/**
 * Geometric Algebra file MIMEResolver for no extension file.
 *
 * @see https://github.com/orat/netbeans-noext-mime-resolver
 * 
 * @author Oliver Rettig
 */
@ServiceProvider(service = MIMEResolver.class)
public final class GaMimeResolver extends MIMEResolver {

    public GaMimeResolver() {
        super(GaFileUtils.GA_MIME_TYPE);
    }

    @Override
    public String findMIMEType(FileObject fo) {
        if (!fo.canRead() || fo.isFolder()) {
            return null;
        }

        // ga file : file ext is ".ga"
        String ext = fo.getExt();
        if (ext.isEmpty() || !ext.equals("ga")) {
            return null;
        }

        return GaFileUtils.GA_MIME_TYPE;
    }

    /*
     * Previous algebra-specific MIME resolution retained as reference. Algebra
     * selection is now performed only by the generated TextMate grammar.
     *
     * <pre>
     * String algebraId = GAFactoryService.getFactory(file.toPath()).getAlgebra();
     * return GaAlgebras.mimeType(algebraId).orElse(GaFileUtils.GA_MIME_TYPE);
     * </pre>
     */
}
