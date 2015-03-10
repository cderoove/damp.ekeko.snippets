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

package org.netbeans.modules.debugger.debug;

import org.openide.debugger.DebuggerInfo;
import org.openide.execution.NbProcessDescriptor;

import org.netbeans.modules.debugger.delegator.SessionDebuggerInfo;
import org.netbeans.modules.debugger.support.ProcessDebuggerInfo;

/**
* Contains information about a class to debug.
* Consists of these pieces of information:
* <UL>
* <LI>the class to run
* <LI>parameters for its main method
* <LI>a class name to stop execution in, if desired
* </UL>
* Uses tools debugger.
*
* @author Jan Jancura
*/
public class ToolsDebugger10Info extends ProcessDebuggerInfo implements SessionDebuggerInfo {

    private String javaHome = "";

    /**
    * Construct a new <code>DebuggerInfo</code> with the class to run and its parameters specified.
    * Sets class to stop in to be the class to run.
    *
    * @param className name of debugged class
    * @param argv command-line arguments used for debugging this class; may be empty but not <code>null</code>
    */
    public ToolsDebugger10Info (
        String className,
        String[] argv,
        String stopClassName,
        NbProcessDescriptor processDescriptor,
        String classPath,
        String bootClassPath,
        String repositoryPath,
        String libraryPath,
        boolean classic,
        String javaHome
    ) {
        super (
            className,
            argv,
            stopClassName,
            processDescriptor,
            classPath,
            bootClassPath,
            repositoryPath,
            libraryPath,
            classic
        );
        this.javaHome = javaHome;
    }

    public String getJavaHome () {
        return javaHome;
    }

    /**
    * Returns type of debugger.
    */
    public Class getDebuggerType () {
        return ToolsDebugger.class;
    }
}