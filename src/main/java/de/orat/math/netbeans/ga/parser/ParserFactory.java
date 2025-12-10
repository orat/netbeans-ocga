package de.orat.math.netbeans.ga.parser;

/**
 *
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */

import de.orat.math.netbeans.ga.parser.spi.GaMimeResolverParser;
import de.orat.math.netbeans.ga.parser.spi.GaMimeResolverParserProvider;
import de.orat.math.netbeans.ga.parser.spi.GaMimeResolverParserProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openide.util.Lookup;

/**
 *
 * @author Oliver Rettig
 */
public final class ParserFactory {

    private ParserFactory() {
    }

    public static List<GaMimeResolverParser> createParsers(String line) {
        List<GaMimeResolverParser> parsers = new ArrayList<>();
        Collection<? extends GaMimeResolverParserProvider> providers = 
                Lookup.getDefault().lookupAll(GaMimeResolverParserProvider.class);
        for (GaMimeResolverParserProvider provider : providers) {
            if (!provider.support(line)) {
                continue;
            }
            GaMimeResolverParser parser = provider.getParser(line);
            if (parser == null) {
                continue;
            }
            parsers.add(parser);
        }
        return parsers;
    }
}
