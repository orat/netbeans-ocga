package de.orat.math.netbeans.ga.utils;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class GaFileUtils {

    // new
    // All these need to be compatible with GeometricAlgebraLang in my Truffle language impl
    // Otherwise, debugging stops working.
    public static final String LANGUAGE_ID = "ga";
    public static final String FILE_ENDING = ".ga";
    public static final String MIME_TYPE = "text/x-ga"; //"application/x-ga";
        
    // old
    public static final String GA_MIME_TYPE = "text/x-ga"; //FIXME leicht durch const oben ersetzen
    //public static final String GA_LANGUAGE_NAME = "ga";
    // better use LANGUAGE_ID from de.dhbw.rahmlab.dsl4ga.impl.truffle.api.Program

    private GaFileUtils() {
    }
}
