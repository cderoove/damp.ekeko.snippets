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

package org.netbeans.modules.debugger.support;

import org.openide.options.SystemOption;
import org.openide.actions.GoAction;
import org.openide.debugger.DebuggerException;
import org.openide.debugger.DebuggerType;
import org.openide.execution.ExecInfo;
import org.openide.execution.NbProcessDescriptor;
import org.openide.execution.NbClassPath;
import org.openide.filesystems.FileSystemCapability;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.MapFormat;

import org.netbeans.modules.debugger.support.util.Utils;

/**
* Settings for java debugger.
*
* @author Jan Jancura
*/
public class ProcessDebuggerType extends DebuggerType {


    // static .....................................................................................

    /** generated Serialized Version UID */
    static final long serialVersionUID = 833275268075073629L;

    /** Property name of the debuggerProcess property */
    public static final String PROP_DEBUGGER_PROCESS = "debuggerProcess"; // NOI18N
    /** Property name of the classic property */
    public static final String PROP_CLASSIC = "classic"; // NOI18N
    /** Property name of the classPath property */
    public static final String PROP_CLASSPATH = "classPath"; // NOI18N
    /** Property name of the bootClassPath property */
    public static final String PROP_BOOT_CLASSPATH = "bootClassPath"; // NOI18N
    /** Property name of the repositoryPath property */
    public static final String PROP_REPOSITORY = "repositoryPath"; // NOI18N
    /** Property name of the libraryPath property */
    public static final String PROP_LIBRARY = "libraryPath"; // NOI18N
    /** Property name of the name property */
    public static final String PROP_NAME = "name"; // NOI18N

    /** Switch name constant */
    public static final String CLASSIC_SWITCH = "classic"; // NOI18N
    /** Switch name constant */
    public static final String DEBUGGER_OPTIONS = "debuggerOptions"; // NOI18N
    /** Switch name constant */
    public static final String BOOT_CLASS_PATH_SWITCH_SWITCH = "bootclasspathSwitch"; // NOI18N
    /** Switch name constant */
    public static final String BOOT_CLASS_PATH_SWITCH = "bootclasspath"; // NOI18N
    /** Switch name constant */
    public static final String REPOSITORY_SWITCH = "filesystems"; // NOI18N
    /** Switch name constant */
    public static final String LIBRARY_SWITCH = "library"; // NOI18N
    /** Switch name constant */
    public static final String CLASS_PATH_SWITCH = "classpath"; // NOI18N
    /** Switch name constant */
    public static final String MAIN_SWITCH = "main"; // NOI18N
    /** Switch java home */
    public static final String JAVA_HOME_SWITCH = "java.home"; // NOI18N
    /** Switch file name separator */
    public static final String FILE_SEPARATOR_SWITCH = "/"; // NOI18N
    /** Switch path separator */
    public static final String PATH_SEPARATOR_SWITCH = ":"; // NOI18N
    /** Switch quote **/
    public static final String QUOTE_SWITCH = "q"; // NOI18N

    /** The default debugger process and CLASSPATH */
    public static final NbProcessDescriptor DEFAULT_DEBUGGER_PROCESS;

    /** Process bootClassPath. */
    private static String bootClassPath = NbClassPath.createBootClassPath ().getClassPath ();

    static {
        // initialize bootClassPath & DEFAULT_DEBUGGER_PROCESS

        // helper variables
        String fileSeparator = System.getProperty ("file.separator");
        String pathSeparator = System.getProperty ("path.separator");
        String javaRoot = System.getProperty ("java.home") + fileSeparator;
        String netbeansHome = System.getProperty ("netbeans.home");
        String javaRoot1 = javaRoot;
        if (javaRoot.toLowerCase ().endsWith (fileSeparator + "jre" + fileSeparator)) { // NOI18N
            javaRoot1 = javaRoot.substring (0, javaRoot1.length () - 3 - fileSeparator.length ());
        }

        bootClassPath = javaRoot1 + "lib" + fileSeparator + "tools.jar" + pathSeparator + // NOI18N
                        NbClassPath.createBootClassPath ().getClassPath ();

        DEFAULT_DEBUGGER_PROCESS = new NbProcessDescriptor (
                                       "{java.home}{/}..{/}bin{/}java", // NOI18N
                                       "{" + CLASSIC_SWITCH + "}" + // NOI18N
                                       "{" + DEBUGGER_OPTIONS + "}" + // NOI18N
                                       " -Djava.compiler=NONE " + // NOI18N
                                       "{" + QUOTE_SWITCH + "}" + // NOI18N
                                       "{" + BOOT_CLASS_PATH_SWITCH_SWITCH + "}" + // NOI18N
                                       "{" + BOOT_CLASS_PATH_SWITCH + "}" + // NOI18N
                                       "{" + QUOTE_SWITCH + "}" + // NOI18N
                                       " -classpath " +
                                       "{" + QUOTE_SWITCH + "}" + // NOI18N
                                       "{" + REPOSITORY_SWITCH + "}" + // NOI18N
                                       "{" + LIBRARY_SWITCH + "}" + // NOI18N
                                       "{" + CLASS_PATH_SWITCH + "}" + // NOI18N
                                       "{" + QUOTE_SWITCH + "}" + // NOI18N
                                       " {" + MAIN_SWITCH + "}", // NOI18N
                                       NbBundle.getBundle (ProcessDebuggerType.class).getString ("MSG_DebuggerHint")
                                   );
    }


    // variables ...................................................................................

    /** The debugger process and CLASSPATH */
    private NbProcessDescriptor debuggerProcess;
    /** HotSpot is used. */
    protected boolean classic;
    /** Clasic property is initialized. */
    protected boolean classicInited = false;
    /** HotSpot is used. */
    protected boolean setted = false;
    private int serialVer = 1;


    // init ...................................................................................

    /** Read the object.
    */
    private void readObject (java.io.ObjectInputStream oos)
    throws java.io.IOException, ClassNotFoundException {
        oos.defaultReadObject ();
        if (serialVer == 0) {
            setDebuggerProcess (DEFAULT_DEBUGGER_PROCESS);
        }
    }


    // properties .................................................................................

    /**
    * Getter for debuggerProcess property. 
    */
    public NbProcessDescriptor getDebuggerProcess () {
        if (debuggerProcess != null) return debuggerProcess;
        return DEFAULT_DEBUGGER_PROCESS;
    }

    /**
    * Setter for debuggerProcess property. 
    */
    public void setDebuggerProcess (NbProcessDescriptor debugger) {
        NbProcessDescriptor oldValue = getDebuggerProcess ();
        debuggerProcess = debugger;
        if (!setted) {
            boolean old = classic;
            classic = getClassicDefault ();
            firePropertyChange (PROP_CLASSIC, new Boolean (old), new Boolean (classic));
        }
        firePropertyChange (PROP_DEBUGGER_PROCESS, oldValue, debuggerProcess);
    }

    /**
    * Getter method for classic property.
    */
    public boolean isClassic () {
        if (setted || classicInited)
            return classic;
        else {
            classicInited = true;
            return classic = getClassicDefault ();
        }
    }

    /**
    * Setter method for classic property.
    */
    public void setClassic (boolean hs) {
        if (hs == classic) return;
        classic = hs;
        setted = true;
        firePropertyChange (PROP_CLASSIC, new Boolean (!hs), new Boolean (hs));
    }

    /**
    * Getter method for repositoryPath property.
    */
    public String getRepositoryPath () {
        return NbClassPath.createRepositoryPath (FileSystemCapability.DEBUG).getClassPath ();
    }

    /**
    * Setter method for repositoryPath property.
    */
    public void setRepositoryPath (String repositoryPath) {
    }

    /**
    * Getter method for libraryPath property.
    */
    public String getLibraryPath () {
        return NbClassPath.createLibraryPath ().getClassPath ();
    }

    /**
    * Setter method for libraryPath property.
    */
    public void setLibraryPath (String libraryPath) {
    }

    /**
    * Getter method for classPath property.
    */
    public String getClassPath () {
        return NbClassPath.createClassPath ().getClassPath ();
    }

    /**
    * Setter method for classPath property.
    */
    public void setClassPath (String classPath) {
    }

    /**
    * Getter method for bootClassPath property.
    */
    public String getBootClassPath () {
        return bootClassPath;
    }

    /**
    * Setter method for bootClassPath property.
    */
    public void setBootClassPath (String bootClassPath) {
    }

    
    /**
    * Determines if classic switch will be used defaultly or not.
    */
    protected boolean getClassicDefault () {
        boolean hasHotSpot = Utils.hasHotSpot ((new MapFormat (Utils.processDebuggerInfo (null, "", ""))) // NOI18N
                             .format (getDebuggerProcess ().getProcessName ()));
        //Utilities.getOperatingSystem () == Utilities.OS_WIN2000
        return hasHotSpot;
    }
    

    // DebuggerType implementation ..........................................................

    /**
    * Returns name of this debugger type.
    */
    public String displayName () {
        return NbBundle.getBundle (ProcessDebuggerType.class).getString ("CTL_Process_debugger_type");
    }

    /**
    * setName was protected.
    * @param name - name
    */
    /*  public void setName (String name) {
        super.setName (name);
      }*/

    /**
    * Returns help fot this debugger type.
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ProcessDebuggerType.class);
    }

    /** Starts debugger. */
    protected void startDebugger (
        String className,
        String[] arguments,
        String stopClassName,
        NbProcessDescriptor process,
        String classPath,
        String bootClassPath,
        String repositoryPath,
        String libraryPath,
        boolean classic,
        ExecInfo info,
        boolean stopOnMain
    ) throws DebuggerException {
        TopManager.getDefault ().getDebugger ().startDebugger (
            new ProcessDebuggerInfo (
                className,
                arguments,
                stopClassName,
                process,
                classPath,
                bootClassPath,
                repositoryPath,
                libraryPath,
                classic
            )
        );
    }

    /* Starts the debugger. */
    public void startDebugger (ExecInfo info, boolean stopOnMain)
    throws DebuggerException {
        if (!setted) {
            boolean old = classic;
            classic = getClassicDefault ();
            firePropertyChange (PROP_CLASSIC, new Boolean (old), new Boolean (classic));
        }
        startDebugger (
            info.getClassName (),
            info.getArguments (),
            stopOnMain ? info.getClassName () : null,
            getDebuggerProcess (),
            getClassPath (),
            getBootClassPath (),
            getRepositoryPath (),
            getLibraryPath (),
            isClassic (),
            info,
            stopOnMain
        );
    }
}

/*
* Log
*  15   Gandalf-post-FCS1.11.3.2    4/18/00  Jan Jancura     Serialization of debugger
*       types changed
*  14   Gandalf-post-FCS1.11.3.1    3/31/00  Martin Ryzl     startDebugger() updated
*  13   Gandalf-post-FCS1.11.3.0    3/28/00  Daniel Prusa    
*  12   Gandalf   1.11        2/15/00  Jan Jancura     Repository renamed to 
*       filesystems
*  11   Gandalf   1.10        1/18/00  Daniel Prusa    {java.home} switch
*  10   Gandalf   1.9         1/13/00  Daniel Prusa    NOI18N
*  9    Gandalf   1.8         1/6/00   Daniel Prusa    Quote character switch 
*       added
*  8    Gandalf   1.7         11/29/99 Jan Jancura     Better support or 
*       detecting HotSpot @ changing of debugging VM version.
*  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    Gandalf   1.5         10/7/99  Jan Jancura     Unification of debugger 
*       types.
*  5    Gandalf   1.4         9/28/99  Jan Jancura     
*  4    Gandalf   1.3         9/16/99  Jan Jancura     Serialization fixed
*  3    Gandalf   1.2         9/15/99  Jan Jancura     
*  2    Gandalf   1.1         9/9/99   Jan Jancura     Filesystem capabilities 
*       bug
*  1    Gandalf   1.0         8/9/99   Jan Jancura     
* $
*/
