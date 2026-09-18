package de.orat.math.netbeans.ga.api;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Oliver Rettig
 */
public enum MimeType {

    //GA("text/ga"),
    PGA("text/ga-pga"), // NOI18N
    CGA("text/ga-cga"), // NOI18N
    G6("text/ga-g6"), // NOI18N
    UNNKOWN(null); // NOI18N

    private final String mimeType;
    private static final Map<String, MimeType> TYPES = new HashMap<>();
    public static final String[] MIME_TYPES = new String[] {
        PGA.getMimeType(),
        CGA.getMimeType(),
        G6.getMimeType()
    };

    static {
        TYPES.put("pga", PGA); // NOI18N
        TYPES.put("cga", CGA); // NOI18N
        TYPES.put("g6", G6); // NOI18N
    }

    private MimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @NonNull
    public String getMimeType() {
        return mimeType;
    }

    public static MimeType valueOfFileType(String fileType) {
        MimeType mime = TYPES.get(fileType);
        if (mime == null) {
            mime = UNNKOWN;
        }
        return mime;
    }
}
