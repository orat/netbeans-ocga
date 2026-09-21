package org.graalvm.tools.lsp.api; 

import java.io.InputStream;
import java.io.OutputStream;

/**
 * be careful: This interface must be defined inside this project with exactly this
 * package and class name and method!
 *
 * Warum das funktioniert: Wenn das originale (evtl. proprietäre) 
 * GraalVM-Tooling-Plugin im Klassenpfad liegt, implementiert deren interne Klasse 
 * dieses Interface. Wenn du es in deiner IDE lokal nachbaust, wird der Compiler 
 * beruhigt, und zur Laufzeit wird die Verbindung zur experimentellen Implementierung 
 * von GraalVM hergestellt.
 */
public interface StreamConfigurationService {
    void startServer(InputStream in, OutputStream out);
}