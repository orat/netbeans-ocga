package de.orat.math.netbeans.ga.parser;

import de.orat.math.netbeans.ga.api.MimeType;
import de.orat.math.netbeans.ga.parser.spi.GaMimeResolverParser;
import de.orat.math.netbeans.ga.utils.GaFileUtils;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class GaMimeResolverParserImpl implements GaMimeResolverParser {

    private final String gaFirstLine;
    private String fileType;

    public GaMimeResolverParserImpl(String gaFirstLine) {
        this.gaFirstLine = gaFirstLine;
    }

    @Override
    public GaMimeResolverParser parse() {
        if (GaFileUtils.isGaFileFirstLine(gaFirstLine)) {
            String algebraName = GaFileUtils.getAlgebraName(gaFirstLine);
            if (algebraName != null && !algebraName.isEmpty()) {
                fileType = algebraName;
            }
        }
        return this;
    }

    static boolean support(String line) {
        return GaFileUtils.isGaFileFirstLine(line);
    }

    @Override
    public Result getResult() {
        if (fileType == null) {
            return GaMimeResolverParser.UNKOWN_RESULT;
        }
        return new GaFirstLineResult(MimeType.valueOfFileType(fileType));
    }

    private static class GaFirstLineResult implements Result {

        private final MimeType mimeType;

        public GaFirstLineResult(MimeType mimeType) {
            this.mimeType = mimeType;
        }

        @Override
        public String getMimeType() {
            return mimeType.getMimeType();
        }

    }
}