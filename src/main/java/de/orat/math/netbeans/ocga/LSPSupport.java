package de.orat.math.netbeans.ocga;

import com.oracle.truffle.api.instrumentation.TruffleInstrument.Env;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Context.Builder;
import org.graalvm.polyglot.Instrument;
import org.graalvm.polyglot.io.IOAccess;
import org.graalvm.tools.lsp.instrument.EnvironmentProvider;
import org.graalvm.tools.lsp.server.ContextAwareExecutor;
import org.graalvm.tools.lsp.server.LSPFileSystem;
import org.graalvm.tools.lsp.server.TruffleAdapter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * ServiceConfigurationError occur?

   The ServiceConfigurationError exception occurs when something goes wrong while 
   loading service providers like Javax Servlet, Hibernate or Java Swing in your 
   application. However, below are some other related scenarios where this error 
   also can occur:

   - On violation of the specification of the provider-configuration file.
   - An IOException occurs when reading the provider-configuration file.
   - When a concrete provider class named in provider-configuration cannot be found while working.
   - When the concrete provider class is not a subclass of the service class.
   - When the concrete provider class cannot be instantiated or some other kind of error occurs.
   * 
   *  So in order to make your language discoverable by the polyglot API you can 
   *  either drop them in graalvm/jre/languages/yourlanguage or you can add your 
   *  language jar to the truffle classpath using 
   *  -Dtruffle.class.path.append=yourlanguage.jar

 */
// https://github.com/oracle/graal/blob/master/tools/README.md
// TODO herausfinden, wie ich einen Language-server aufbaue und bundle, den ich dann
// mit vergleichbarem code zu dieser Klasse starten kann
// https://www.graalvm.org/latest/tools/lsp/
// GraalVM unterstützt bereits einen LSP-Server für Gastsprachen mit code-completion
// find-usages, etc. 
// TODO Wie kann ich diesen starten?
// Die Idee ist dann alles was spezifisch für die eigenen DSL ist an einen eigenen
// zusätzlichen LSP-Server zu delegieren
// https://github.com/enso-org/enso/tree/develop/tools/enso4igv/src/main/java/org/enso/tools/enso4igv
// da findet sich keine vergleichbare Klasse, aber enso arbeitet trotzdem mit dem GraalVM LSP zusammen
// warum geht das?
// https://www.graalvm.org/latest/graalvm-as-a-platform/implement-language/
// GraalVM's Language Server Protocol implementation can delegate to the existing 
// language servers written specially for the particular languages 
// (using the --lsp.Delegates launcher option) and merge the static data returned
// from these servers with its own dynamic data to a single result.
// https://www.graalvm.org/22.1/tools/vscode/graalvm-extension/polyglot-runtime/
// https://github.com/apache/netbeans/tree/master/cpplite Beispiel für language support
/**
 * Caveat: The language server is started only for files that are inside any project.
 */
@MimeRegistration(mimeType=GAUtilities.GA_MIME_TYPE, service=LanguageServerProvider.class)
public class LSPSupport implements LanguageServerProvider {
    
    static final Logger LOG = Logger.getLogger(LSPSupport.class.getName());
    
    
    protected Engine engine;
    //protected TruffleAdapter truffleAdapter;
    protected Context context;
    
   // LSP starten ohne über die Kommandozeile zu gehen:
   // https://github.com/oracle/graal/blob/master/tools/src/org.graalvm.tools.lsp.test/src/org/graalvm/tools/lsp/test/server/TruffleLSPTest.java
   //TODO
   // ungetestet
   // Voraussetzung: lsp ist in der GraalVM registriert, d.h. das jar mit der 
   // zugehörigen Implementierung muss via commandline argument in den richtigen
   // class-path eingefügt worden sein?
   // Was bekomme ich über den lookup was davon muss ich weitergeben?
   @Override
   public LanguageServerDescription startServer(Lookup lkp) {
       
        // Ausgabe landet in der shell in der Netbeans gestartet wird
        System.out.println("LSP starting...");
        LSPSupport.LOG.log(Level.INFO, "LSP starting...");
     
        try {
            // engine = Engine.create(LANGUAGE_ID);
            // TODO muss ich die Sprache "ga" noch irgendwo registrieren? geht das so?
            //FIXME build() kommt nicht zum Ende!
            /*engine = Engine.newBuilder(Program.LANGUAGE_ID).//logHandler(System.out).
                    //TODO unklar, ob lsp option so korrek ist, eventuell "true" als value?
                    //TODO zusätzlich "polyglot" option?
                    allowExperimentalOptions(true).option("lsp","").build();
            */
            //Engine.newBuilder(Program.LANGUAGE_ID).allowExperimentalOptions(true);
            //System.out.println("Engine Builder created");
            // das wird noch angezeigt!
            // [INFO] Caused: java.lang.NoClassDefFoundError: Could not initialize class org.graalvm.polyglot.Engine$ImplHolder
            // [INFO] 	at org.graalvm.polyglot.Engine.getImpl(Engine.java:442)
            // [INFO] 	at org.graalvm.polyglot.Engine$Builder.build(Engine.java:740)

            // An execution engine for Graal guest languages that allows to inspect 
            // the the installed guest languages, instruments and their available options.
            // By default every context creates its own engine instance implicitly 
            // when instantiated. Multiple contexts can use an explicit engine 
            // when using a context builder. If contexts share the same engine 
            //instance then they share instruments and their configuration.
            // It can be useful to create an engine instance without a context 
            // to only access meta-data for installed languages, instruments and 
            // their available options.
            //engine = Engine.newBuilder().allowExperimentalOptions(true).build();
            // FIXME hier komme ich schon nicht an!
            //System.out.println("Engine created!");
            
            // -Dfile.encoding=UTF-8 
            // -Dstdout.encoding=UTF-8 
            // -Dsun.stdout.encoding=UTF-8 
            // -Dgraalvm.locatorDisabled=true 
            // --add-exports org.graalvm.truffle/com.oracle.truffle.api.nodes=ALL-UNNAMED 
            // --add-exports org.graalvm.truffle/com.oracle.truffle.api.exception=ALL-UNNAMED 
            // --add-exports org.graalvm.truffle/com.oracle.truffle.api=ALL-UNNAMED 
            // --add-exports org.graalvm.truffle/com.oracle.truffle.api.interop=ALL-UNNAMED
            // mit der Erzeugung des context wird bereits der LSP-Server gestartet
            Map<String, String> options = new HashMap<>();
            options.put("graalvm.locatorDisabled","true");
            options.put("add-exports","org.graalvm.truffle/com.oracle.truffle.api.nodes=ALL-UNNAMED");
            options.put("add-exports","org.graalvm.truffle/com.oracle.truffle.api.exception=ALL-UNNAMED");
            options.put("add-exports","org.graalvm.truffle/com.oracle.truffle.api.interop=ALL-UNNAMED");
            Context context = Context.newBuilder("ga").allowAllAccess(true)
               .allowExperimentalOptions(true).options(options).option("lsp", "true").build();//) {

            // da komme ich schon nicht mehr an?
            System.out.println("Context created!");
            LSPSupport.LOG.log(Level.INFO, "Context created!");
            Set<String> languages = context.getEngine().getLanguages().keySet();
            for (String id : languages) {
                System.out.println("found "+id);
                LSPSupport.LOG.log(Level.INFO, "found: "+id);
            }

            engine = context.getEngine();
            //engine = Engine.create("ga"/*LANGUAGE_ID*/);
            // da komme ich schon nicht mehr an.
            System.out.println("Engine created!");
            LSPSupport.LOG.log(Level.INFO, "Engine created!");

            Map<String, Instrument> instruments = engine.getInstruments();
            Instrument instrument = instruments.get("lsp");
            if (instrument != null){
                //TODO geht das so über das Environment oder muss ich mir den Socket
                // beschaffen und mich mit diesem verbinden?
                EnvironmentProvider envProvider = instrument.lookup(EnvironmentProvider.class);
                if (envProvider != null){
                    Env env = envProvider.getEnvironment();
                    if (env != null){
                        //addTruffleAdapter(engine, env, GA_LANGUAGE_NAME);
                        System.out.println("LSP started!");
                        LSPSupport.LOG.log(Level.INFO, "LSP started!");
                        return LanguageServerDescription.create(env.in(), 
                            env.out(), null);
                    } else {
                        System.err.println("LSP Environment not found!");
                        LSPSupport.LOG.log(Level.WARNING, "LSP EnvironmentProvider not found!");
                    }
                } else {
                    System.err.println("LSP EnvironmentProvider not found!");
                    LSPSupport.LOG.log(Level.WARNING, "LSP EnvironmentProvider not found!");
                }
            } else {
                System.err.println("LSP instrument not found: "+String.valueOf(instruments.size())+" instruments found!");
                LSPSupport.LOG.log(Level.WARNING, "LSP Instrument not found: "+String.valueOf(instruments.size())+" instruments found!");
            }
            
            /*Context.Builder builder = Context.newBuilder("ga")
                    .allowAllAccess(true).allowExperimentalOptions(true)
                    .engine(engine);
            System.out.println("Context.Builder created!");
            this.context = builder.build();
            System.out.println("Context created!");
            this.context.initialize("ga");
            System.out.println("Context initialized!");
            */
            
                    
            /*Instrument instrument = engine.getInstruments().get("lsp");
            System.out.println("LSP instrument");
            EnvironmentProvider envProvider = instrument.lookup(EnvironmentProvider.class);
            System.out.println("envProvider");
            */
        } catch (Exception | ServiceConfigurationError e){
            e.printStackTrace();
            System.out.println("LSPSupport failed: "+e.getMessage());
        }
        return null;
   }
   
   /**
    * Das brauche ich sicher nicht, denn der Truffle-Adapter wird intern bereits 
    * erzeugt in LSPInstrument...
    * 
    * @param engine
    * @param env
    * @param gaLanguageName
    * @deprecated
    */
   @Deprecated
   private void addTruffleAdapter(Engine engine, Env env, String gaLanguageName){
        TruffleAdapter truffleAdapter = new TruffleAdapter(env, true);

        Builder contextBuilder = Context.newBuilder();
        contextBuilder.allowAllAccess(true);
        
        //TODO
        contextBuilder.allowIO(IOAccess.newBuilder().
                fileSystem(LSPFileSystem.newReadOnlyFileSystem(truffleAdapter)).build());
        
        contextBuilder.engine(engine);
        context = contextBuilder.build();
        context.enter();

        ContextAwareExecutor executorWrapper = new ContextAwareExecutor() {

            @Override
            public <T> Future<T> executeWithDefaultContext(Callable<T> taskWithResult) {
                try {
                    return CompletableFuture.completedFuture(taskWithResult.call());
                } catch (Exception e) {
                    CompletableFuture<T> cf = new CompletableFuture<>();
                    cf.completeExceptionally(e);
                    return cf;
                }
            }

            @Override
            public <T> Future<T> executeWithNestedContext(Callable<T> taskWithResult, boolean cached) {
                try (Context newContext = contextBuilder.build()) {
                    newContext.enter();
                    newContext.initialize(gaLanguageName);
                    try {
                        return CompletableFuture.completedFuture(taskWithResult.call());
                    } catch (Exception e) {
                        CompletableFuture<T> cf = new CompletableFuture<>();
                        cf.completeExceptionally(e);
                        return cf;
                    } finally {
                        newContext.leave();
                    }
                }
            }

            @Override
            public <T> Future<T> executeWithNestedContext(Callable<T> taskWithResult, int timeoutMillis, Callable<T> onTimeoutTask) {
                if (timeoutMillis <= 0) {
                    return executeWithNestedContext(taskWithResult, false);
                } else {
                    CompletableFuture<Future<T>> future = CompletableFuture.supplyAsync(() -> executeWithNestedContext(taskWithResult, false));
                    try {
                        return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException e) {
                        future.cancel(true);
                        try {
                            return CompletableFuture.completedFuture(onTimeoutTask.call());
                        } catch (Exception timeoutTaskException) {
                            CompletableFuture<T> cf = new CompletableFuture<>();
                            cf.completeExceptionally(timeoutTaskException);
                            return cf;
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        CompletableFuture<T> cf = new CompletableFuture<>();
                        cf.completeExceptionally(e);
                        return cf;
                    }
                }
            }

            @Override
            public void shutdown() {
            }

            @Override
            public void resetContextCache() {
            }
        };
        
        truffleAdapter.register(env, executorWrapper);
   }
   
     // Caveat: the language server is started only for files that are inside a project, 
   // so create (any) new project, and put the file with the DSL code in a subfolder
   // of this project
   //@Override
   public LanguageServerDescription startServerOrig(Lookup lkp) {
       try {
           // [Graal LSP] Starting server and listening on localhost/127.0.0.1:8123
           
           //Process p = new ProcessBuilder("/bin/main.js", "start").start();
           /*Process p = new ProcessBuilder(
            "/usr/local/bin/node",
            "/bin/main.js",
            "start").start();*/
           // entsprechend
           // https://www.graalvm.org/latest/tools/lsp/
           // mit Optionen --experimental-options --lsp
           // TODO
           // wohin die Optionen?
           Process p = new ProcessBuilder(
            "/home/oliver/JAVA/PROJECTS/DSL4GeometricAlgebra/ga",
            "-lsp"," /home/oliver/JAVA/PROJECTS/DSL4GeometricAlgebra/dummy.ocga").start();
           return LanguageServerDescription.create(p.getInputStream(), p.getOutputStream(), p);
       } catch (IOException ex) {
           Exceptions.printStackTrace(ex);
           return null;
       }
   }
}