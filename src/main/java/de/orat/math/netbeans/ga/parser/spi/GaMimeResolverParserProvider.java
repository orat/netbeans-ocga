package de.orat.math.netbeans.ga.parser.spi;

/**
 *
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public interface GaMimeResolverParserProvider {

    /**
     * Check whether the first line can be parsed.
     *
     * @param line the fist line of the file
     * @return {@code true} if parsing is possible, otherwise {@code false}
     */
    boolean support(String line);

    /**
     * Get the parser.
     *
     * @param line the first line of the file
     * @return the parser
     */
    GaMimeResolverParser getParser(String line);
}