package de.orat.math.netbeans.ga.parser;

import de.orat.math.netbeans.ga.parser.spi.GaMimeResolverParser;
import de.orat.math.netbeans.ga.parser.spi.GaMimeResolverParserProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
@ServiceProvider(service = GaMimeResolverParserProvider.class, position = 100)
public class GaMimeTypeResolverParserProviderImpl implements GaMimeResolverParserProvider {

    @Override
    public boolean support(String line) {
        return GaMimeResolverParserImpl.support(line);
    }

    @Override
    public GaMimeResolverParser getParser(String line) {
        return new GaMimeResolverParserImpl(line);
    }

}