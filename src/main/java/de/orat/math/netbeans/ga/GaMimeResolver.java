package de.orat.math.netbeans.ga;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.lookup.ServiceProvider;
import de.orat.math.netbeans.ga.api.MimeType;
import de.orat.math.netbeans.ga.parser.ParserFactory;
import de.orat.math.netbeans.ga.parser.spi.GaMimeResolverParser;

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
        super(MimeType.MIME_TYPES);
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

        String firstLine = getFirstLine(fo);
        List<GaMimeResolverParser> parsers = ParserFactory.createParsers(firstLine);
        for (GaMimeResolverParser parser : parsers) {
            GaMimeResolverParser.Result result = parser.parse().getResult();
            if (result.getMimeType() != null) {
                return result.getMimeType();
            }
        }
        return null;
    }

    /**
     * Get first line from FileObject.
     *
     * @param fo FileObject
     * @return empty string(i.e. "") if some problems are occurred, first line
     * otherwise
     */
    private String getFirstLine(FileObject fo) {
        try {
            // XXX get encoding?
            for (String line : fo.asLines("UTF-8")) { // NOI18N
                return line;
            }
        } catch (IOException ex) {
            Logger.getLogger(GaMimeResolver.class.getName()).log(Level.WARNING, null, ex);
        }
        return ""; // NOI18N
    }
}
