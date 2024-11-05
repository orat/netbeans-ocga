package de.orat.math.netbeans.ga;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import org.netbeans.api.scripting.Scripting;

/**
 * CAVEAT
 * Der jvm-switch: -J-Dgraalvm.locatorDisabled=true beim start von netbeans ist 
 * nötig, damit Sprachen im classpath gefunden werden, ohne dass sie explizit 
 * mit dem GraalVM command line tool gu installiert werden müssen. 
 * 
 * Test um überhaupt erst einmal zu überprüfen, dass meine Sprache ga gefunden wird.
 * 
 * Die ScriptingAPI erlaubt nicht den Zugriff auf die Instrumente wie 
 * LSP. 
 * 
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class TestScriptingEngineAPI {
    
    /**
     * The Truffle language implementation framework does not provide a JSR-223 
     * ScriptEngine implementation. The Polyglot API provides more fine-grained 
     * control over Truffle features and we strongly encourage users to use the 
     * org.graalvm.polyglot.Context interface in order to control many of the 
     * settings directly and benefit from finer-grained security settings in 
     * GraalVM.
     * 
     * @param args 
     */
    public static void main(String[] args){
        final ScriptEngineManager manager = Scripting.createManager(); 
        for (ScriptEngineFactory factory: manager.getEngineFactories()){
            final String name = factory.getEngineName();
            final String languageName = factory.getLanguageName();
            System.err.println("found engine name \""+name+"\", language Name \""+languageName+"\"!");
            // found Oracle Nashorn (wird mittlerweile auch nicht mehr gefunden)
            // found NetBeans indentation
            //FIXME
            // die Sprache ga wird nicht gefunden!
        }
    }
}
