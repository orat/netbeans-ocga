package de.orat.math.netbeans.ga.parser.spi;


import static de.orat.math.netbeans.ga.api.MimeType.UNNKOWN;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 *
 * @author Oliver Rettig
 */
public interface GaMimeResolverParser {

    GaMimeResolverParser parse();

    Result getResult();

    public static interface Result {

        @CheckForNull
        String getMimeType();
    }

    public static Result UNKOWN_RESULT = () -> UNNKOWN.getMimeType();
}