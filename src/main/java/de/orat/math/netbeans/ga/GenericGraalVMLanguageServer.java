package de.orat.math.netbeans.ga;

import de.orat.math.netbeans.ga.utils.GaFileUtils;
import static de.orat.math.netbeans.ga.utils.GaFileUtils.LANGUAGE_ID;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ServiceLoader;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Instrument;

// be careful: This interface must be defined inside this project
import org.graalvm.tools.lsp.api.StreamConfigurationService;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Instead of relying on the "lsp.Port" option string, we start the Context with 
 * "lsp=true", extract the underlying Instrument instance, and pass the piped 
 * streams directly to it via environment services or direct invocation depending 
 * on your specific version architecture. Alternatively, you can use GraalVM's 
 * dedicated MessageEndpoint API or standard pipe redirectors.
 * This is a robust, network-free implementation.
 * 
 * But at the moment it results in classloader problems. It it unclear if they can
 * be solved and it seems that the prefered solution is to start the LSP in a seperate JVM.
 */

/**
 * Caveat: The language server is started only for files that are inside any project.
 */
@MimeRegistration(mimeType=GaFileUtils.GA_MIME_TYPE, service=LanguageServerProvider.class)
public class GenericGraalVMLanguageServer implements LanguageServerProvider {
    
    // Experiment: Die Isolation deaktivieren, um den ClassLoader-Fehler 
    // (RegexLanguageProvider) zu beheben
    // SEVERE [org.openide.util.RequestProcessor]: Error in RequestProcessor 
    // org.netbeans.modules.lsp.client.bindings.CustomIndexerImpl$$Lambda/0x000000000e565800
    // org.graalvm.polyglot.PolyglotException: java.util.ServiceConfigurationError: 
    // com.oracle.truffle.api.provider.TruffleLanguageProvider: 
    // com.oracle.truffle.regex.RegexLanguageProvider not a subtype
    // at java.base/java.util.ServiceLoader.fail(ServiceLoader.java:559)
    // muss der Java-Laufzeitumgebung übergeben werden, bevor der Polyglot-Kontext 
    // überhaupt initialisiert wird.
    // Warum kommt der Fehler aus dem CustomIndexerImpl?
    // Das NetBeans-LSP-Modul besitzt einen Hintergrund-Indexer (CustomIndexerImpl). 
    // Sobald Sie ein Projekt öffnen, das Ihre .geolang-Dateien enthält, versucht 
    // dieser Indexer im Hintergrund, syntaktische Informationen zu sammeln.Dabei 
    // greift er auf Truffle-Bibliotheken zu. Da NetBeans ein OSGi-basiertes System ist, 
    // lädt es JAR-Dateien über getrennte Classloader (Modul-Cluster). GraalVM versucht 
    // standardmäßig, seine Sprachen (RegexLanguageProvider etc.) in einem isolierten 
    // Classloader-Bereich abzukapseln. Innerhalb der NetBeans-Modul-Architektur 
    // führt diese Isolation dazu, dass die Klassen sich gegenseitig nicht mehr 
    // als Untertypen ("not a subtype") erkennen. 
    // [1] (https://github.com/oracle/graal/issues/7625)Sobald Wenn der Parameter 
    // -J-Dpolyglotimpl.DisableClassPathIsolation=true erfolgreich beim Start der 
    // IDE geladen wird, bricht GraalVM diese Barriere auf und der Indexer läuft 
    // fehlerfrei durch. Statt die Property via
    /*static {
        System.setProperty("polyglotimpl.DisableClassPathIsolation", "true");
    }*/
    // zu setzen wurde auch versucht diese beim Start der IDE zu übergeben. Das 
    // Probleme konnte damit aber nicht gelöst werden
    
    // Ich brauche org.graalvm.polyglot:plyglot und org.graalvm.tools:lsp-api
    // unklar wie die dependencies formuliert werden müssen
    
    private static Context polyglotContext;

    @Override
    public synchronized LanguageServerDescription startServer(Lookup lookup) {
        
        // Sichere den aktuellen ClassLoader von NetBeans
        //ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            
            //printInstrumentProviders();
            
            System.out.println("=== POLYGLOT IMPL INSTANCE ===");

            try {
                var engine = org.graalvm.polyglot.Engine.create();

                var implField = org.graalvm.polyglot.Engine.class
                        .getDeclaredField("receiver");
                implField.setAccessible(true);

                Object receiver = implField.get(engine);

                System.out.println("Engine receiver = " + receiver);
                System.out.println("Receiver class  = " + receiver.getClass());
                System.out.println("Receiver loader = " + receiver.getClass().getClassLoader());
                System.out.println("Receiver source = " +
                        receiver.getClass().getProtectionDomain().getCodeSource());

            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            }

            
            printPolyglotImplementations();
            
            printTruffleRuntime();
            
            printAvailableLanguageProviders();

            printClassloaders();
            
            
            // 1. Combine the module classloader and the GraalSDK classloader
            ClassLoader unifiedLoader = new ClassLoader(Context.class.getClassLoader()) {
                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    if (name.startsWith("com.oracle.truffle.api.instrumentation")) {
                        // Force redirection to your plugin module's structural context
                        return GenericGraalVMLanguageServer.class.getClassLoader().loadClass(name);
                    }
                    return super.findClass(name);
                }
            };

            // Auf den ClassLoader deines Moduls umstellen, der die Truffle-Klassen kennt
            //Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

            // 1. Setup NetBeans <=> GraalVM in-memory pipes
            // NetBeans writes to netbeansOut -> GraalVM reads from graalvmIn
            PipedOutputStream netbeansOut = new PipedOutputStream();
            PipedInputStream graalvmIn = new PipedInputStream(netbeansOut);

            // GraalVM writes to graalvmOut -> NetBeans reads from netbeansIn
            PipedOutputStream graalvmOut = new PipedOutputStream();
            PipedInputStream netbeansIn = new PipedInputStream(graalvmOut);

            
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
    
    private static void printPolyglotImplementations(){
        System.out.println("=== POLYGLOT IMPLEMENTATION ===");

        System.out.println("Context class       = " +
                org.graalvm.polyglot.Context.class);

        System.out.println("Context loader      = " +
                org.graalvm.polyglot.Context.class.getClassLoader());

        System.out.println("Context source      = " +
                org.graalvm.polyglot.Context.class
                        .getProtectionDomain()
                        .getCodeSource());

        System.out.println("Engine class        = " +
                org.graalvm.polyglot.Engine.class);

        System.out.println("Engine loader       = " +
                org.graalvm.polyglot.Engine.class.getClassLoader());

        System.out.println("Engine source       = " +
                org.graalvm.polyglot.Engine.class
                        .getProtectionDomain()
                        .getCodeSource());

        System.out.println("AbstractPolyglotImpl = " +
                org.graalvm.polyglot.impl.AbstractPolyglotImpl.class);

        System.out.println("Impl loader         = " +
                org.graalvm.polyglot.impl.AbstractPolyglotImpl.class
                        .getClassLoader());

        System.out.println("Impl source         = " +
                org.graalvm.polyglot.impl.AbstractPolyglotImpl.class
                        .getProtectionDomain()
                        .getCodeSource());
    }
    private static void printClassloaders(){
        System.err.println("=== GRAAL CLASSLOADERS ===");

        System.err.println(
            "PolyglotImpl: " +
            com.oracle.truffle.polyglot.PolyglotImpl.class.getProtectionDomain()
                .getCodeSource()
        );

        System.err.println(
            "PolyglotImpl loader: " +
            com.oracle.truffle.polyglot.PolyglotImpl.class.getClassLoader()
        );

        System.err.println(
            "Truffle API loader: " +
            com.oracle.truffle.api.Truffle.class.getClassLoader()
        );

        System.err.println(
            "Truffle API source: " +
            com.oracle.truffle.api.Truffle.class.getProtectionDomain()
                .getCodeSource()
        );

        Class<?> contextClass = org.graalvm.polyglot.Context.class;

        System.err.println(
            "Polyglot Context loader: " +
            contextClass.getClassLoader()
        );

        System.err.println(
            "Polyglot Context source: " +
            contextClass.getProtectionDomain()
                .getCodeSource()
        );

        Package pp = contextClass.getPackage();

        System.err.println("Polyglot Context implementation version = " +
            pp.getImplementationVersion());
        System.err.println("Polyglot Context specification version   = " +
            pp.getSpecificationVersion());

        System.err.println("TruffleLanguage source = " +
              com.oracle.truffle.api.TruffleLanguage.class
                .getProtectionDomain().getCodeSource()
        );

        System.err.println("TruffleLanguage loader = " +
            com.oracle.truffle.api.TruffleLanguage.class
                .getClassLoader()
        );
        
        System.out.println("=== TRUFFLE RUNTIME CLASSES ===");

        Class<?>[] classes = {
            com.oracle.truffle.api.Truffle.class,
            com.oracle.truffle.api.TruffleLanguage.class,
            com.oracle.truffle.api.provider.TruffleLanguageProvider.class,
            //com.oracle.truffle.api.impl.TruffleLocator.class,
            com.oracle.truffle.api.TruffleOptions.class
        };

        for (Class<?> c : classes) {
            var pd = c.getProtectionDomain();
            var cs = pd != null ? pd.getCodeSource() : null;

            System.out.println();
            System.out.println("Class       = " + c.getName());
            System.out.println("Loader      = " + c.getClassLoader());
            System.out.println("Source      = " + (cs != null ? cs.getLocation() : null));
            System.out.println("Package     = " + c.getPackage());
            System.out.println("ImplVersion = " +
                (c.getPackage() != null ? c.getPackage().getImplementationVersion() : null));
            System.out.println("SpecVersion = " +
                (c.getPackage() != null ? c.getPackage().getSpecificationVersion() : null));
        }
    }
    
    private static void printInstrumentProviders(){
        System.out.println("=== TRUFFLE INSTRUMENT PROVIDERS ===");

        ServiceLoader
                .load(com.oracle.truffle.api.instrumentation.provider.TruffleInstrumentProvider.class,
                      Thread.currentThread().getContextClassLoader())
                .stream()
                .forEach(p -> {
                    Class<?> c = p.type();

                    System.out.println("Provider     = " + c.getName());
                    System.out.println("ClassLoader  = " + c.getClassLoader());
                    System.out.println("Source       = " +
                            c.getProtectionDomain().getCodeSource());
                    System.out.println("Module       = " + c.getModule());
                    System.out.println();
                });
    }
    private static void printTruffleRuntime(){
        System.out.println("=== ACTUAL TRUFFLE RUNTIME ===");

        com.oracle.truffle.api.TruffleRuntime runtime =
                com.oracle.truffle.api.Truffle.getRuntime();

        Class<?> c = runtime.getClass();

        System.out.println("Runtime object = " + runtime);
        System.out.println("Runtime class  = " + c.getName());
        System.out.println("Runtime loader = " + c.getClassLoader());
        System.out.println("Runtime source = " +
                c.getProtectionDomain().getCodeSource());
        System.out.println("Runtime module = " + c.getModule());
        System.out.println("Runtime package = " + c.getPackage());
        System.out.println("Runtime impl version = " +
                c.getPackage().getImplementationVersion());
        System.out.println("Runtime spec version = " +
                c.getPackage().getSpecificationVersion());
    }
    private static void printAvailableLanguageProviders(){
        System.out.println("=== SERVICE LOADER ===");

        var loader = java.util.ServiceLoader.load(
                com.oracle.truffle.api.provider.TruffleLanguageProvider.class);

        for (var provider : loader) {
            System.out.println("Provider: " + provider.getClass().getName());
            System.out.println("ClassLoader: " + provider.getClass().getClassLoader());
            System.out.println("Source: "
                    + provider.getClass().getProtectionDomain()
                            .getCodeSource().getLocation());
        }
    }
}