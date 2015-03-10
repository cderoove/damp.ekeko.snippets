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

package org.openide.debugger;

import org.openide.src.MethodElement;

/**
* Contains information about a class to debug.
* Consists of these pieces of information:
* <UL>
* <LI>the class to run
* <LI>parameters for its main method
* <LI>a class name to stop execution in, if desired
* </UL>
*
* @author Jan Jancura, Jaroslav Tulach
*/
public class DebuggerInfo extends org.openide.execution.ExecInfo {
    /** class to stop at */
    private String stopClassName;

    /**
    * Construct a new <code>DebuggerInfo</code> with the class to run and its parameters specified.
    * Sets class to stop in to be the class to run.
    *
    * @param className name of debugged class
    * @param argv command-line arguments used for debugging this class; may be empty but not <code>null</code>
    */
    public DebuggerInfo (
        String className,
        String[] argv
    ) {
        this (
            className,
            argv,
            className
        );
    }

    /**
    * Construct a new <code>DebuggerInfo</code> with the class to run, parameters, and a class to stop at.
    *
    * @param className name of debugged class
    * @param argv command-line arguments used for debugging this class; may be empty but not <code>null</code>
    * @param stopClassName name of class to stop in (may be <code>null</code>)
    */
    public DebuggerInfo (
        String className,
        String[] argv,
        String stopClassName
    ) {
        super (className, argv);
        this.stopClassName = stopClassName;
    }

    /** Checks whether the method has declaring class.
    * @param m method
    * @return the same method
    * @exception IllegalArgumentException if the stopMethod does not have
    *   declaring class
    *
    private static MethodElement method (MethodElement m) {
      if (m.getDeclaringClass () == null) {
        throw new IllegalArgumentException ("No declaring class for method: " + m.getName ());
      }
      return m;
}
    */


    /** Get the class to stop execution in.
    *
    * @return the class name or <code>null</code>
    */
    public String getStopClassName () {
        return stopClassName;
    }

    /** Getter for a method to stop at. If the method is not null
    * it has a declaring class. So
    * @return method or null
    *
    public MethodElement getStopMethod () {
      return stopMethod;
}
    */
}

/*
 * Log
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         3/23/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         3/22/99  Jesse Glick     [JavaDoc]
 *  2    Gandalf   1.1         2/26/99  Jaroslav Tulach Open API
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    reflecting changes in ExecInfo
 */
