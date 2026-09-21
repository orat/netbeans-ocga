package de.orat.math.netbeans.ga.lsp;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openide.modules.InstalledFileLocator;
import java.nio.file.Paths;

public final class GaLspRuntime {

    private GaLspRuntime() {}

    public static List<Path> findRuntimeJars() {
        List<Path> result = new ArrayList<>();

        for (File root : InstalledFileLocator.getDefault()
                .locateAll("modules/ext/graal/truffle-api-24.0.0.jar", null, false)) {
            System.out.println("FOUND: " + root);
        }
        
        /*
         * Adjust these paths to match how your NBM is packaged.
         * 
         */
        //addDirectory(result, "modules/ext/graal");
        result.addAll(findGraalRuntime5a());
        //result.add(findGraalRuntime2().toPath());
        
        //addDirectory(result, "modules/ext/ga");
        // exec.args = 4711
        // Das ga-Modul muss im NetBeans-Cluster bereits installiert sein.
        /*File gaJar = InstalledFileLocator.getDefault().locate(
                "modules/DSL4GAjar_Impl_Truffle.jar",
                "de.dhbw.rahmlab",
                false
        );*/
        //result.add(gaJar.toPath());
        result.add(findGaModule());
        return result;
    }

    private static Path findGaModule() {

        // <groupId>de.dhbw.rahmlab</groupId>
	// <artifactId>DSL4GA_Impl_Truffle</artifactId>
        File file = InstalledFileLocator.getDefault().locate(
                "modules/DSL4GAjar_Impl_Truffle_1.0-SNAPSHOT.jar",
                "de.dhbw.rahmlab.DSL4GA_Impl_Truffle", // Das ist die Code Name Base deines NetBeans-Moduls.
                false
        );

        if (file == null) {
            throw new IllegalStateException(
                    "GA language module not installed"
            );
        }

        return file.toPath();
    }
    
    private static List<Path> findGraalRuntime4() {
        // InstalledFileLocator funktioniert bei einer normalen Installation.
        File installed = InstalledFileLocator.getDefault().locate(
                "modules/ext/graal",
                "de.orat.math.netbeans.ocga",
                false
        );

        if (installed != null && installed.isDirectory()) {
            return listJars(installed.toPath());
        }

        // Reloadable / Entwicklungsinstallation:
        // Speicherort des eigenen Moduls bestimmen.
        File moduleJar = InstalledFileLocator.getDefault().locate(
                "modules/de-orat-math-netbeans-ocga.jar",
                "de.orat.math.netbeans.ocga",
                false
        );

        if (moduleJar != null) {
            Path cluster = moduleJar.toPath()
                    .getParent()       // modules
                    .getParent();      // extra

            Path developmentDir = cluster
                    .resolve("modules")
                    .resolve("ext")
                    .resolve("graal");

            if (Files.isDirectory(developmentDir)) {
                return listJars(developmentDir);
            }
        }

        throw new IllegalStateException(
                "Graal runtime directory not found"
        );
    }
    
    
    // test der besten variante
    private static List<Path> findGraalRuntime5a() {
    try {
        Path moduleJar = Paths.get(
                GaLspRuntime.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI()
        );

        System.err.println("GaLspRuntime CodeSource = " + moduleJar);
        System.err.println("CodeSource exists        = " + Files.exists(moduleJar));
        System.err.println("CodeSource directory     = " + Files.isDirectory(moduleJar));

        Path clusterDir = moduleJar
                .getParent()
                .getParent();

        Path graalDir = clusterDir
                .resolve("modules")
                .resolve("ext")
                .resolve("graal");

        System.err.println("clusterDir               = " + clusterDir);
        System.err.println("graalDir                 = " + graalDir);
        System.err.println("graalDir exists          = " + Files.exists(graalDir));
        System.err.println("graalDir directory       = " + Files.isDirectory(graalDir));

        if (Files.isDirectory(graalDir)) {
            return listJars(graalDir);
        }

    } catch (Exception ex) {
        ex.printStackTrace();
    }

    throw new IllegalStateException(
            "Graal runtime directory not found"
    );
}
    // vermutlich robustestes Variante
    private static List<Path> findGraalRuntime5() {
        // 1. Reloadable development installation
        try {
            Path moduleJar = Paths.get(
                    GaLspRuntime.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

            // Bei reloadable:
            // .../target/nbm/clusters/extra/modules/de-orat-math-netbeans-ocga.jar
            //
            // -> .../target/nbm/clusters/extra
            Path clusterDir = moduleJar
                    .getParent()   // modules
                    .getParent();  // extra

            Path graalDir = clusterDir
                    .resolve("modules")
                    .resolve("ext")
                    .resolve("graal");

            if (Files.isDirectory(graalDir)) {
                return listJars(graalDir);
            }
        } catch (URISyntaxException | RuntimeException ex) {
            // Nicht die Entwicklungsinstallation oder Pfad konnte
            // nicht bestimmt werden -> normaler Installationsweg.
        }

        // 2. Normal installierte NetBeans-Version
        File installed = InstalledFileLocator.getDefault().locate(
                "modules/ext/graal",
                "de.orat.math.netbeans.ocga",
                false
        );

        if (installed != null && installed.isDirectory()) {
            return listJars(installed.toPath());
        }

        throw new IllegalStateException(
                "Graal runtime directory not found"
        );
    }

    private static List<Path> listJars(Path directory) {
        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Cannot read Graal runtime directory: " + directory,
                    ex
            );
        }
    }
    
    
    private static File findGraalRuntime2() {
        File graalDir = InstalledFileLocator.getDefault().locate(
                "modules/ext/graal",
                "de.orat.math.netbeans-ocga",
                false
        );

        System.out.println(">>> InstalledFileLocator result = " + graalDir);

        if (graalDir != null) {
            System.out.println(">>> exists     = " + graalDir.exists());
            System.out.println(">>> directory  = " + graalDir.isDirectory());
            System.out.println(">>> absolute   = " + graalDir.getAbsolutePath());

            File[] files = graalDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    System.out.println(">>> Graal file = " + f.getAbsolutePath());
                }
            }

            return graalDir;
        }

        throw new IllegalStateException(
                "Graal runtime directory not found: modules/ext/graal"
        );
    }

    private static List<Path> findGraalRuntime() {

        InstalledFileLocator locator = InstalledFileLocator.getDefault();   
        
        /*
        netbeans/modules/ext/graal/collections-24.0.0.jar
   275339  2026-09-19 21:55   netbeans/modules/ext/graal/jniutils-24.0.0.jar
   161626  2026-09-19 21:55   netbeans/modules/ext/graal/json-24.0.0.jar
  1124500  2026-09-19 21:55   netbeans/modules/ext/graal/lsp-tool-24.0.0.jar
    46386  2026-09-19 21:55   netbeans/modules/ext/graal/lsp_api-24.0.0.jar
   218482  2026-09-19 21:55   netbeans/modules/ext/graal/nativeimage-24.0.0.jar
   944201  2026-09-19 21:55   netbeans/modules/ext/graal/polyglot-24.0.0.jar
 16783505  2026-09-19 21:55   netbeans/modules/ext/graal/truffle-api-24.0.0.jar
    61031  2026-09-19 21:55   netbeans/modules/ext/graal/truffle-compiler-24.0.0.jar
  1089802  2026-09-19 21:55   netbeans/modules/ext/graal/truffle-runtime-24.0.0.jar
    22232  2026-09-19 21:55   netbeans/modules/ext/graal/word-24.0.0.jar

        */
        //<groupId>de.orat.math</groupId>
        //<artifactId>netbeans-ocga</artifactId>
        File lspTool = locator.locate(
            "graal/lsp-tool-24.0.0.jar", // "modules/ext/graal/lsp-tool-24.0.0.jar",
            "de.orat.math.netbeans.ocga",
            false
        );

        // >>> Graal lsp-tool = null
        System.out.println(">>> Graal lsp-tool = " + lspTool);
        
        
        File lspToolWithoutCodeName = locator.locate(
                "modules/ext/graal/lsp-tool-24.0.0.jar",
                null,
                false
        );

        // >>> Graal lsp-tool (no code name) = null
        System.out.println(">>> Graal lsp-tool (no code name) = "
                + lspToolWithoutCodeName);

        // >>> netbeans.home  = /snap/netbeans/149/netbeans/platform
        System.out.println(">>> netbeans.home  = "
                + System.getProperty("netbeans.home"));

        // >>> netbeans.dirs  = /snap/netbeans/149/netbeans/nb:/snap/netbeans/149/netbeans/ergonomics:/snap/netbeans/149/netbeans/ide:/snap/netbeans/149/netbeans/extide:/snap/netbeans/149/netbeans/java:/snap/netbeans/149/netbeans/apisupport:/snap/netbeans/149/netbeans/webcommon:/snap/netbeans/149/netbeans/websvccommon:/snap/netbeans/149/netbeans/enterprise:/snap/netbeans/149/netbeans/mobility:/snap/netbeans/149/netbeans/profiler:/snap/netbeans/149/netbeans/python:/snap/netbeans/149/netbeans/php:/snap/netbeans/149/netbeans/identity:/snap/netbeans/149/netbeans/harness:/snap/netbeans/149/netbeans/cnd:/snap/netbeans/149/netbeans/cndext:/snap/netbeans/149/netbeans/cpplite:/snap/netbeans/149/netbeans/dlight:/snap/netbeans/149/netbeans/groovy:/snap/netbeans/149/netbeans/extra:/snap/netbeans/149/netbeans/javacard:/snap/netbeans/149/netbeans/javafx:/snap/netbeans/149/netbeans/rust:
        System.out.println(">>> netbeans.dirs  = "
                + System.getProperty("netbeans.dirs"));

        File directory = InstalledFileLocator.getDefault().locate(
                "modules/ext/graal",
                "de.orat.math.netbeans.ocga",
                false
        );

        // >>> locator result = null
        System.out.println(">>> locator result = " + directory);

        if (directory == null) {
            File home = new File(System.getProperty("netbeans.home"));
            File direct = new File(home, "modules/ext/graal");

            // >>> direct path    = /snap/netbeans/149/netbeans/platform/modules/ext/graal
            // da sind aber die files nicht drin!!
            // >>> direct exists  = false
            // >>> direct dir     = false
            System.out.println(">>> direct path    = " + direct);
            System.out.println(">>> direct exists  = " + direct.exists());
            System.out.println(">>> direct dir     = " + direct.isDirectory());

            if (direct.exists()) {
                File[] files = direct.listFiles();
                if (files != null) {
                    for (File f : files) {
                        System.out.println(">>> direct file    = " + f);
                    }
                }
            }

            // java.lang.IllegalStateException: Graal runtime directory not found: modules/ext/graal
            //FIXME
            throw new IllegalStateException(
                    "Graal runtime directory not found: modules/ext/graal"
            );
        }
        
        
        if (!directory.isDirectory()) {
            throw new IllegalStateException(
                    "Not a directory: " + directory
            );
        }

        try (Stream<Path> files = Files.list(directory.toPath())) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.getFileName()
                                 .toString()
                                 .endsWith(".jar"))
                    .sorted()
                    .collect(Collectors.toList());

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Cannot read Graal runtime directory: "
                    + directory,
                    ex
            );
        }
    }
    
    private static List<Path> findGraalRuntime2a() {
        File directory = InstalledFileLocator.getDefault().locate(
                "modules/ext/graal",
                "de.orat.math.netbeans.ocga",
                false
        );

        if (directory == null) {
            throw new IllegalStateException(
                    "Graal runtime directory not found: modules/ext/graal"
            );
        }

        if (!directory.isDirectory()) {
            throw new IllegalStateException(
                    "Not a directory: " + directory
            );
        }

        try (Stream<Path> files = Files.list(directory.toPath())) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.getFileName()
                                 .toString()
                                 .endsWith(".jar"))
                    .sorted()
                    .collect(Collectors.toList());

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Cannot read Graal runtime directory: "
                    + directory,
                    ex
            );
        }
    }
    
    // "com.example.ga" to your actual NetBeans module code name --> de.orat.math
    // für netbeans-ocga
    private static void addDirectory(List<Path> result, String relativePath) {
        File dir = InstalledFileLocator.getDefault().locate(
                relativePath,
                "de.orat.math",
                false
        );

        if (dir == null || !dir.isDirectory()) {
            throw new IllegalStateException(
                    "Cannot find bundled runtime directory: " + relativePath
            );
        }

        try (var stream = Files.list(dir.toPath())) {
            stream.filter(p ->
                    p.getFileName().toString().endsWith(".jar"))
                  .forEach(result::add);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Cannot read " + dir,
                    ex
            );
        }
    }
}
