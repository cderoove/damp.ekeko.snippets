/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.settings;

import java.io.*;
import java.util.ResourceBundle;
import java.util.Properties;
import java.util.List;
import java.util.Iterator;

import org.openide.options.ContextSystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.io.ReaderInputStream;
import org.openide.compiler.CompilerType;
import org.openide.ServiceType;
import org.openide.ServiceType.Handle;
import org.openide.TopManager;
import org.openide.execution.Executor;
import org.openide.debugger.DebuggerType;

import org.netbeans.modules.java.FastJavacCompilerType;
import org.netbeans.modules.java.gj.JavaCompilerType;

/** Settings for java data loader and java source parser
*
* @author Ales Novak, Petr Hamernik
*/
public class JavaSettings extends ContextSystemOption {
    /** serial uid */
    static final long serialVersionUID = -8522143676848697297L;

    public static final String PROP_COMPILER = "compiler"; // NOI18N
    public static final String PROP_EXECUTOR = "executor"; // NOI18N
    public static final String PROP_DEBUGGER = "debugger"; // NOI18N

    public static final String PROP_REPLACEABLE_STRINGS_TABLE = "replaceableStringsTable"; // NOI18N

    public static final String PROP_AUTO_PARSING_DELAY = "autoParsingDelay"; // NOI18N

    public static final String PROP_PERFECT_RECOGNITION = "perfectRecognition"; // NOI18N

    /** The resource bundle for the form editor */
    public static ResourceBundle bundle;

    /** property value */
    private static String table = "USER="+System.getProperty("user.name")+"\n";

    /** auto parsing delay */
    private static int autoParsingDelay = 2000;

    /** Should be class files parsed for 'source' property? */
    private static boolean perfectRecognition;

    /** If true then external execution is used */
    public String displayName () {
        return getString("CTL_Java_option");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (JavaSettings.class);
    }

    public JavaSettings() {
        addOption(getJavaSynchronizationSettings());
    }

    public boolean isGlobal() {
        return false;
    }

    /** @return CompilerType */
    public CompilerType getCompiler() {
        CompilerType.Handle compilerType = (CompilerType.Handle) getProperty(PROP_COMPILER);
        return (CompilerType) compilerType.getServiceType();
    }

    /** Usese given CompilerType */
    public void setCompiler(CompilerType ct) {
        putProperty(PROP_COMPILER, new CompilerType.Handle(ct), true);
    }

    /** @return Executor */
    public Executor getExecutor() {
        ServiceType.Handle serviceType = (ServiceType.Handle) getProperty(PROP_EXECUTOR);
        if ((serviceType == null) ||
                (serviceType.getServiceType() == null)) {
            Executor exec = Executor.getDefault();
            serviceType = new ServiceType.Handle(exec);
            putProperty(PROP_EXECUTOR, serviceType, false);
            return exec;
        }
        return (Executor) serviceType.getServiceType();
    }

    /** sets an executor */
    public void setExecutor(Executor ct) {
        putProperty(PROP_EXECUTOR, new ServiceType.Handle(ct), true);
    }

    /** @return debugger */
    public DebuggerType getDebugger() {
        ServiceType.Handle serviceType = (ServiceType.Handle) getProperty(PROP_DEBUGGER);
        if ((serviceType == null) ||
                (serviceType.getServiceType() == null)) {
            DebuggerType debugger = DebuggerType.getDefault();
            serviceType = new ServiceType.Handle(debugger);
            putProperty(PROP_DEBUGGER, serviceType, false);
            return debugger;
        }
        return (DebuggerType) serviceType.getServiceType();
    }

    /** Sets a debugger type */
    public void setDebugger(DebuggerType ct) {
        putProperty(PROP_DEBUGGER, new ServiceType.Handle(ct), true);
    }

    /** Sets the replaceable strings table - used during instantiating
    * from template.
    */
    public void setReplaceableStringsTable(String table) {
        String old = this.table;
        this.table = table;
        firePropertyChange(PROP_REPLACEABLE_STRINGS_TABLE, old, table);
    }

    /** Gets the replacable strings table - used during instantiating
    * from template.
    */
    public String getReplaceableStringsTable() {
        return table;
    }

    /** Gets the replaceable table as the Properties class.
    * @return the properties
    */
    public Properties getReplaceableStringsProps() {
        Properties props = new Properties();
        try {
            props.load(new StringBufferInputStream(table));
        }
        catch (IOException e) {
        }
        return props;
    }

    /** Gets the delay time for the start of the parsing.
    * @return The time in milis
    */
    public int getAutoParsingDelay() {
        return autoParsingDelay;
    }

    /** Sets the delay time for the start of the parsing.
    * @param delay The time in milis
    */
    public void setAutoParsingDelay(int delay) {
        if (delay < 0)
            throw new IllegalArgumentException();
        autoParsingDelay = delay;
    }

    /**
    * @param b Class files are scanned for 'source' property iff <tt>true</tt>
    */
    public void setPerfectRecognition(boolean b) {
        if (b == perfectRecognition) {
            return;
        }
        perfectRecognition = b;
        if (b) {
            firePropertyChange(PROP_PERFECT_RECOGNITION, Boolean.TRUE, Boolean.FALSE);
        } else {
            firePropertyChange(PROP_PERFECT_RECOGNITION, Boolean.FALSE, Boolean.TRUE);
        }
    }

    /**
    * @return <tt>true</tt> iff deep parsing of class files should be done
    */
    public boolean isPerfectRecognition() {
        return perfectRecognition;
    }

    /** @return localized string */
    static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(JavaSettings.class);
        }
        return bundle.getString(s);
    }

    private static JavaSynchronizationSettings getJavaSynchronizationSettings() {
        return (JavaSynchronizationSettings) JavaSynchronizationSettings.findObject(JavaSynchronizationSettings.class, true);
    }

    /** The method is to filter ServiceTypes according to its parameters.
     * 
     * @param known Is fastjavac binary (e.g. fastjavac.exe) present? If so then ensure that it is listed among the service types. If not then delete FastJavacCompiler type from the list.
     * @param set Is to be a descendand of java.JavaCompilerType set as default for java?
     */
    private static void filterCompilerTypes(boolean known, boolean set) {

        Class clz = (known ?
                     FastJavacCompilerType.class : JavaCompilerType.class);
        List list = TopManager.getDefault().getServices().getServiceTypes();
        Iterator iter = list.iterator();
        // set only the first from the "list"
        boolean isset = false;

        // set/remove compiler types
        while (iter.hasNext()) {
            ServiceType stype = (ServiceType) iter.next();
            if (stype instanceof CompilerType) {
                CompilerType ret = (CompilerType) stype;
                Class retClass = ret.getClass();
                if (retClass == clz) {
                    if (set && !isset) {
                        JavaSettings settings = (JavaSettings) JavaSettings.findObject(JavaSettings.class, true);
                        settings.setCompiler(ret);
                        isset = true;
                    }
                    if (known) {
                        // FastJavac is present in the list - OK
                        return;
                    }
                } else if (!known && (retClass == FastJavacCompilerType.class)) {
                    // FastJavac is present in the list - remove it
                    iter.remove();
                }
            }
        }
        iter = null;

        if (known) { // FastJavacCompilerType not installed - install it
            FastJavacCompilerType type = new FastJavacCompilerType();
            JavaSettings settings = (JavaSettings) JavaSettings.findObject(JavaSettings.class, true);
            if (set) {
                settings.setCompiler(type);
            }
            list.add(type);
        }

        TopManager.getDefault().getServices().setServiceTypes(list);
    }
    
    /** Sets default compiler - called from JavaModule */
    public static void setCompiler(boolean restarted) {
        filterCompilerTypes(FastJavacCompilerType.isFastJavacPlatform(), restarted);
    }    
}

/*
 * Log
 */
