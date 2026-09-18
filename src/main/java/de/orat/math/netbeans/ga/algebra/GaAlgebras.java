package de.orat.math.netbeans.ga.algebra;

import java.util.List;

/**
 * Known algebra variants packaged by this module.
 *
 * The distinction is relevant only for syntax highlighting. All {@code .ga}
 * files use the same NetBeans MIME type and editor integration.
 */
public final class GaAlgebras {

    private static final List<String> IDS = List.of("cga", "pga");

    private GaAlgebras() {
    }

    public static List<String> ids() {
        return IDS;
    }
}
