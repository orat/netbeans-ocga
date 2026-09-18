package de.orat.math.netbeans.ocga;

import de.orat.math.netbeans.ga.utils.GaFileUtils;
import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.graalvm.polyglot.Context;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.util.Lookup;

/**
 * Caveat: The language server is started only for files that are inside any project.
 */
@MimeRegistration(mimeType=GaFileUtils.GA_MIME_TYPE, service=LanguageServerProvider.class)
public class GenericGraalVMLanguageServer1 implements LanguageServerProvider {
    
    @Override
    public synchronized LanguageServerDescription startServer(Lookup lookup) {
        
        try {
            // 1. Pfad zur aktuellen Java-Executable ermitteln
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

            // 2. Aktuellen Klassenpfad (Classpath) auslesen
            String classpath = System.getProperty("java.class.path");

            // 3. Zu startende Hauptklasse definieren
            String className = "DSL4GAMain";

            // 4. Befehlsliste für den ProcessBuilder zusammenstellen
            List<String> command = new ArrayList<>();
            command.add(javaBin);
            command.add("-cp");
            command.add(classpath);
            command.add(className);
            // Hier können optional weitere Argumente angehängt werden:
            // command.add("argument1");

            // 5. Prozess konfigurieren und starten
            ProcessBuilder builder = new ProcessBuilder(command);
            
            // Leitet die Standard- und Fehler-Ausgabe der neuen JVM in die Konsole der aktuellen JVM um
            builder.inheritIO(); 

            Process process = builder.start();
            
            // Optional: Auf das Ende des Prozesses warten
            int exitCode = process.waitFor();
            System.out.println("The JVM running the LSP is finished with the code " + exitCode + ".");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    
        
    /*
            // 2. Initialize the GraalVM context enabling the LSP tool without network strings
            polyglotContext = Context.newBuilder()
                    .allowAllAccess(true)
                    .hostClassLoader(unifiedLoader)
                    .allowExperimentalOptions(true)
                    .option("lsp", "true") 
                    .build();

            // Force initialization of your custom language
            polyglotContext.initialize(LANGUAGE_ID);

            // 3. Extract the LSP Instrument instance directly from the engine
            Instrument lspInstrument = polyglotContext.getEngine().getInstruments().get("lsp");
            if (lspInstrument == null) {
                throw new IllegalStateException("GraalVM LSP tool ('lsp-tool' artifact) is missing from the classpath.");
            }

            // 4. Pass the streams directly into the GraalVM LSP loop
            // We cast or lookup the service interface to feed custom I/O handles
            // (GraalVM provides an internal provider class to bind standard streams)
            Thread lspBridgeThread = new Thread(() -> {
                try {
                    // Use GraalVM's internal dynamic service provider to launch with specific streams
                    lspInstrument.lookup(StreamConfigurationService.class)
                                 .startServer(graalvmIn, graalvmOut);
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }, "GraalVM-LSP-Pipe-Bridge");
            
            lspBridgeThread.setDaemon(true);
            lspBridgeThread.start();

            // 5. Provide the matching endpoints back to NetBeans
            return LanguageServerDescription.create(netbeansIn, netbeansOut, null);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            //return null;
            throw new IllegalStateException("Fehler beim Starten des In-Process LSP", ex);
        } finally {
            // WICHTIG: Beim Verlassen der Methode den NetBeans-ClassLoader wiederherstellen
            //Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
    */
    
}