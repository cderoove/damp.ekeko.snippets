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

import org.openide.debugger.DebuggerInfo;
import org.openide.execution.NbProcessDescriptor;

/**
* Contains information about a class to debug.
* Consists of these pieces of information:
* <UL>
* <LI>the class to run
* <LI>parameters for its main method
* <LI>a class name to stop execution in, if desired
* </UL>
*
* @author Jan Jancura
*/
public class ProcessDebuggerInfo extends DebuggerInfo
    implements java.io.Serializable {

    private NbProcessDescriptor         processDescriptor;
    private String                      classPath;
    private String                      bootClassPath;
    private String                      repositoryPath;
    private String                      libraryPath;
    private boolean                     classic;

    /**
    * Construct a new <code>DebuggerInfo</code> with the class to run, parameters, and a class to stop at.
    *
    * @param className name of debugged class
    * @param argv command-line arguments used for debugging this class; may be empty but not <code>null</code>
    * @param stopClassName name of class to stop in (may be <code>null</code>)
    */
    public ProcessDebuggerInfo (
        String className,
        String[] argv,
        String stopClassName,
        NbProcessDescriptor processDescriptor,
        String classPath,
        String bootClassPath,
        String repositoryPath,
        String libraryPath,
        boolean classic
    ) {
        super (className, argv, stopClassName);
        this.processDescriptor = processDescriptor;
        this.classPath = classPath;
        this.bootClassPath = bootClassPath;
        this.repositoryPath = repositoryPath;
        this.libraryPath = libraryPath;
        this.classic = classic;
    }

    /**
    * Getter for debuggerProcess property. 
    */
    public NbProcessDescriptor getDebuggerProcess () {
        return processDescriptor;
    }

    /** Get the classpath or <code>null</code>, if default one must be used.
    *
    * @return classpath or <code>null</code>
    */
    public String getClassPath () {
        return classPath;
    }

    /** Get the boot classpath or <code>null</code>, if default one must be used.
    *
    * @return boot classpath or <code>null</code>
    */
    public String getBootClassPath () {
        return bootClassPath;
    }

    /** Get the repository path or <code>null</code>, if default one must be used.
    *
    * @return repository path  or <code>null</code>
    */
    public String getRepositoryPath () {
        return repositoryPath;
    }

    /** Get the library path or <code>null</code>, if default one must be used.
    *
    * @return library path  or <code>null</code>
    */
    public String getLibraryPath () {
        return libraryPath;
    }

    /**
    * Getter method for classic property.
    */
    public boolean isClassic () {
        return classic;
    }
}

/*
* Log
*  6    Gandalf-post-FCS1.4.2.0     4/18/00  Jan Jancura     Serialization of debugger
*       types changed
*  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         8/9/99   Jan Jancura     Move process settings 
*       from DebuggerSettings to ProcesDebuggerType
*  3    Gandalf   1.2         8/2/99   Jan Jancura     A lot of bugs...
*  2    Gandalf   1.1         7/2/99   Jan Jancura     Session debugging support
*  1    Gandalf   1.0         6/10/99  Jan Jancura     
* $
*/
