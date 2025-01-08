package de.orat.math.netbeans.ocga;

import com.oracle.truffle.api.instrumentation.TruffleInstrument.Env;
import static de.dhbw.rahmlab.dsl4ga.impl.truffle.common.runtime.GeomAlgeLang.LANGUAGE_ID;
//import static de.dhbw.rahmlab.dsl4ga.impl.truffle.api.Program.LANGUAGE_ID;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
import org.apache.commons.lang3.exception.ExceptionUtils;
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
import org.netbeans.modules.lsp.client.options.MimeTypeInfo;
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
public class GenericGraalVMLanguageServer implements LanguageServerProvider {
    
    static final Logger LOG = Logger.getLogger(GenericGraalVMLanguageServer.class.getName());
    
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
   //@Override
   public LanguageServerDescription startServer2(Lookup lkp) {
       
        // Ausgabe landet in der shell in der Netbeans gestartet wird
        System.out.println("LSP starting...");
        GenericGraalVMLanguageServer.LOG.log(Level.INFO, "LSP starting...");
     
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
            Context context = Context.newBuilder(LANGUAGE_ID).allowAllAccess(true)
               .allowExperimentalOptions(true).options(options).option("lsp", "true").build();//) {

            // da komme ich schon nicht mehr an?
            System.out.println("Context created!");
            GenericGraalVMLanguageServer.LOG.log(Level.INFO, "Context created!");
            Set<String> languages = context.getEngine().getLanguages().keySet();
            for (String id : languages) {
                System.out.println("found "+id);
                GenericGraalVMLanguageServer.LOG.log(Level.INFO, "found: "+id);
            }

            engine = context.getEngine();
            //engine = Engine.create(LANGUAGE_ID);
            // da komme ich schon nicht mehr an.
            System.out.println("Engine created!");
            GenericGraalVMLanguageServer.LOG.log(Level.INFO, "Engine created!");

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
                        GenericGraalVMLanguageServer.LOG.log(Level.INFO, "LSP started!");
                        return LanguageServerDescription.create(env.in(), 
                            env.out(), null);
                    } else {
                        System.err.println("LSP Environment not found!");
                        GenericGraalVMLanguageServer.LOG.log(Level.WARNING, "LSP EnvironmentProvider not found!");
                    }
                } else {
                    System.err.println("LSP EnvironmentProvider not found!");
                    GenericGraalVMLanguageServer.LOG.log(Level.WARNING, "LSP EnvironmentProvider not found!");
                }
            } else {
                System.err.println("LSP instrument not found: "+String.valueOf(instruments.size())+" instruments found!");
                GenericGraalVMLanguageServer.LOG.log(Level.WARNING, "LSP Instrument not found: "+String.valueOf(instruments.size())+" instruments found!");
            }
            
            /*Context.Builder builder = Context.newBuilder(LANGUAGE_ID)
                    .allowAllAccess(true).allowExperimentalOptions(true)
                    .engine(engine);
            System.out.println("Context.Builder created!");
            this.context = builder.build();
            System.out.println("Context created!");
            this.context.initialize(LANGUAGE_ID);
            System.out.println("Context initialized!");
            */
            
                    
            /*Instrument instrument = engine.getInstruments().get("lsp");
            System.out.println("LSP instrument");
            EnvironmentProvider envProvider = instrument.lookup(EnvironmentProvider.class);
            System.out.println("envProvider");
            */
        } catch (Exception | ServiceConfigurationError e){
            System.out.println("LSPSupport failed: "+e.getMessage());
            //String errorMessage = ExceptionUtils.getStackTrace(e); 
            //System.out.println(errorMessage);
            e.printStackTrace(); // liefert seltsamer weise keine Ausgabe auf der Console
        }
        return null;
   }
   
   // Caveat: the language server is started only for files that are inside a project, 
   // so create (any) new project, and put the file with the DSL code in a subfolder
   // of this project
   //@Override
   public LanguageServerDescription startServer3(Lookup lkp) {
       
       //MimeTypeInfo mti = lkp.lookup(MimeTypeInfo.class);
       // if (mti == null){
       //   return null;
       // }
       
       try {
           //TODO
           // so einfach geht das nicht! siehe GenericLanguageServer im netbeans source code
           // https://github.com/apache/netbeans/blob/master/webcommon/typescript.editor/src/org/netbeans/modules/typescript/editor/TypeScriptLSP.java
           // https://github.com/ebresie/python4nb/blob/main/src/main/java/org/apache/netbeans/modules/python4nb/editor/lsp/PythonLsp.java
           // Wo bekomme ich das Working-Directory her, also das Verzeichnis in dem 
           // das bash-script und die dummy-ga-Datei sich befinden sowie das lsp-jar-file und alle anderen jars
           //TODO
           File workingDirectory = new File("/home/oliver/JAVA/PROJECTS/DSL4GeometricAlgebra/target");
          
           ProcessBuilder pb = new ProcessBuilder("./ga1")
                   .directory(workingDirectory); 
           pb.redirectError(ProcessBuilder.Redirect.INHERIT);
           Process p = pb.start();
           return LanguageServerDescription.create(p.getInputStream(), p.getOutputStream(), p);
       } catch (IOException ex) {
           System.out.println(ex.getMessage());
           Exceptions.printStackTrace(ex);
           return null;
       }
   }
   
   int port = 8123;
   // connection via Socket
   @Override
   public LanguageServerDescription startServer(Lookup lkp) {
       try {
           
           File workingDirectory = new File("/home/oliver/JAVA/PROJECTS/DSL4GeometricAlgebra/target");
           ProcessBuilder pb = new ProcessBuilder("./ga2")
                   .directory(workingDirectory); 
           // error des subproess auf die err-Ausgabe von NB umleiten
           pb.redirectError(ProcessBuilder.Redirect.INHERIT);
           pb.redirectOutput(ProcessBuilder.Redirect.
                   appendTo(new File(workingDirectory, "server.log")));
           Process p = pb.start();
           
           //WORKAROUND
           try {
                Thread.sleep(3000);
           } catch (InterruptedException ex){}
                
           try (Socket socket = new Socket(InetAddress.getByName("localhost"), port)) {
                System.out.println("Socket created!");

                // zu testzwecken alles loggen
                OutputStream os = new CopyOutput(socket.getOutputStream(), System.err);
                InputStream is = new CopyInput(socket.getInputStream(), System.err);

                return LanguageServerDescription.create(is, os, p);
            } catch (ConnectException e){
                System.err.println("Error while connecting: "+e.getMessage());
            } catch (SocketTimeoutException e){
                System.err.println("Connection: "+e.getMessage());
            } catch (UnknownHostException uhe) {
                System.err.println("UnknownHostException:");
                uhe.printStackTrace(System.err);
            } catch (IOException ioe) {
                System.err.println("IOException: "+ioe.getMessage());
                ioe.printStackTrace(System.err);
            }
           return null;
       } catch (IOException ex) {
           Exceptions.printStackTrace(ex);
           return null;
       }
   }
   
   private static class CopyInput extends InputStream {

        private final InputStream delegate;
        private final OutputStream log;

        public CopyInput(InputStream delegate, OutputStream log) {
            this.delegate = delegate;
            this.log = log;
        }

        @Override
        public int read() throws IOException {
            int read = delegate.read();
            log.write(read);
            return read;
        }
    }

    private static class CopyOutput extends OutputStream {

        private final OutputStream delegate;
        private final OutputStream log;

        public CopyOutput(OutputStream delegate, OutputStream log) {
            this.delegate = delegate;
            this.log = log;
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
            log.write(b);
            log.flush();
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
            log.flush();
        }
    }
}