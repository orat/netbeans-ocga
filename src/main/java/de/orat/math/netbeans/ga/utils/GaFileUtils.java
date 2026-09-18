package de.orat.math.netbeans.ga.utils;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class GaFileUtils {

    // new
    // All these need to be compatible with GeometricAlgebraLang in my Truffle language impl
    // Otherwise, debugging stops working.
    public static final String LANGUAGE_ID = "ocga";
    public static final String FILE_ENDING = ".ocga";
    public static final String MIME_TYPE = "text/x-ocga"; //"application/x-ocga";
        
    // old
    public static final String GA_MIME_TYPE = "text/x-ocga"; //FIXME leicht durch const oben ersetzen
    //public static final String GA_LANGUAGE_NAME = "ga";
    // better use LANGUAGE_ID from de.dhbw.rahmlab.dsl4ga.impl.truffle.api.Program
    
    private static final String ALGEBRA_HASH = "#algebra"; // NOI18N
    
    private GaFileUtils() {
    }

    public static boolean isGaFileFirstLine(String line) {
        if (line == null) {
            return false;
        }
        String commentLine = line.trim();
        return commentLine.startsWith(ALGEBRA_HASH);
    }
    
    /**
     * Get algebra name.
     *
     * @param line ga first line
     * @return algebra name if line is the first line of a ga file, empty string otherwise
     */
    public static String getAlgebraName(String line) {
        if (line == null || !isGaFileFirstLine(line)) {
            return ""; // NOI18N
        }
        String[] split = line.split(" "); // NOI18N
        if (split.length < 2) return "";
        return split[1].trim();
    }
}
