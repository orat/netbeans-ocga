package de.orat.math.netbeans.ocga;

import de.orat.math.netbeans.ga.utils.GaFileUtils;
import static de.orat.math.netbeans.ga.utils.GaFileUtils.LANGUAGE_ID;
import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.graalvm.polyglot.Context;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.util.Lookup;

/**
 * Caveat: The language server is started only for files that are inside any project
 * 
 * TODO möglicherweise ist die Verwendung von ExecutionService doch nicht die beste
 * Variante, also doch ProcessBuilder verwenden?
 */
@MimeRegistration(mimeType=GaFileUtils.GA_MIME_TYPE, service=LanguageServerProvider.class)
public class GenericGraalVMLanguageServer2 implements LanguageServerProvider {
    
    // Prozess-Lebenszyklus: Alle über ExecutionService gestarteten Prozesse werden 
    // beim Beenden der IDE automatisch terminiert. controllable(true) ermöglicht 
    // dem Benutzer, den LSP manuell zu stoppen.

    // LSP-Verbindung: Der GraalVM LSP lauscht standardmäßig auf localhost:8123. 
    // Ihr Plugin muss die LSP-Kommunikation (JSON-RPC über TCP) selbst implementieren 
    // oder eine bestehende LSP-Client-Bibliothek nutzen.

    // GraalVM-Installation: Ihre Truffle-Sprache muss als GraalVM-Komponente 
    // installiert sein, damit der polyglot-Launcher sie findet. 
    
    @Override
    public synchronized LanguageServerDescription startServer(Lookup lookup) {
        
            // Der LSP wird über den GraalVM-Sprachlauncher gestartet. Für eine 
            // die Truffle-Sprache, die als GraalVM-Komponente installiert ist, 
            // kann der generische polyglot-Launcher oder der sprachspezifischen 
            // Launcher verwendet werden. 
            
            // 1. Command zusammenbauen
            
            // Pfad zur aktuellen Java-Executable ermitteln
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

            //TODO
            String graalVmHome = "/path/to/graalvm"; // aus Konfiguration
            String launcher = graalVmHome + "/bin/polyglot"; // oder sprachspezifisch
            String userProgram = "/path/to/user/dummy.ga";

            String[] command = {
                launcher, "--experimental-options", // LSP erfordert dieses Flag
                "--lsp", // Aktiviert den Language Server
                "--lsp.Delegates=...", // Optional: Für statische Analyse[citation:1][citation:10]
                "--polyglot",
                "--your-language", // Ihr Sprachkürzel
                userProgram
            };
            
            //old
            // 2. Aktuellen Klassenpfad (Classpath) auslesen
            //String classpath = System.getProperty("java.class.path");

            // 3. Zu startende Hauptklasse definieren
            //String className = "DSL4GAMain";

            // 4. Befehlsliste für den ProcessBuilder zusammenstellen
            /*List<String> command = new ArrayList<>();
            command.add(javaBin);
            command.add("-cp");
            command.add(classpath);
            command.add(className);*/
            // Hier können optional weitere Argumente angehängt werden:
            // command.add("argument1");

            
            // 2. Callable<Process> implementieren

            // Die External Execution API erwartet ein Callable, das den Prozess startet:
            // impl ProcessLaunch  unten

            // 3. ExecutionDescriptor konfigurieren

            // Der Descriptor steuert das Verhalten des Prozesses im IDE-Kontext:
            ExecutionDescriptor descriptor = new ExecutionDescriptor()
                .controllable(true) // Erlaubt Stop/Restart-Buttons im Output-Fenster
                .frontWindow(true)  // Output-Fenster in den Vordergrund bringen
                .preExecution(() -> {
                    // Optional: UI-Status aktualisieren (z.B. "LSP startet...")
                })
                .postExecution(() -> {
                   // Optional: Aufräumen, wenn der Prozess endet
                });

            // 4. ExecutionService starten

            // Der Service übernimmt das Prozessmanagement und die Ausgabeumleitung:

            ExecutionService exeService = ExecutionService.newService(
                new ProcessLaunch(command),
                        descriptor,
                    "GraalVM LSP: " + LANGUAGE_ID // Tab-Name im Output-Fenster
            );

            // Wichtig: NICHT im Event Dispatch Thread aufrufen
            Future<Integer> exitCode = exeService.run();

        return null;
    }
    
        
    private class ProcessLaunch implements Callable<Process> {
        private final String[] commandLine;
        
        public ProcessLaunch(String... commandLine) {
            this.commandLine = commandLine;
        }
        public Process call() throws Exception {
            ProcessBuilder pb = new ProcessBuilder(commandLine);
            pb.redirectErrorStream(true); // stderr mit stdout zusammenführen
            // Optional: Arbeitsverzeichnis setzen
            // pb.directory(new File("/path/to/project"));
            return pb.start();
        }
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