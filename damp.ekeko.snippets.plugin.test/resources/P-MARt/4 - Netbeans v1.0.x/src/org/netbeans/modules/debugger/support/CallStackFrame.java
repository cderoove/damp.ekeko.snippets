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

import org.openide.debugger.DebuggerException;
import org.openide.text.Line;
import org.netbeans.modules.debugger.support.util.Utils;


/**
* Class representating one line of callstack.
*/
public abstract class CallStackFrame {

    /**
    * Returns line number of this frame in this callstack.
    *
    * @return Returns line number of this frame in this callstack.
    */
    public abstract int getLineNumber () throws DebuggerException;

    /**
    * Returns method name of this frame in this callstack.
    *
    * @return Returns method name of this frame in this callstack.
    */
    public abstract String getMethodName () throws DebuggerException;

    /**
    * Returns class name of this frame in this callstack.
    *
    * @return Returns class name of this frame in this callstack.
    */
    public abstract String getClassName () throws DebuggerException;

    /**
    * Returns current local variables for this frame stack.
    *
    * @return Current local variables for this frame stack.
    */
    public abstract AbstractVariable[] getLocales ();

    /**
    * Returns name of file of this frame.
    *
    * @return Returns name of file of this frame.
    * @throws DebuggerException if informations about source are not included or some other error
    *   occurres.
    */
    public abstract String getSourceName () throws DebuggerException;

    /**
    * Returns line object representing position in the editor where this thread 
    * callstack line is stopped.
    *
    * @throw DebuggerException if some problem occurs.
    * @return line object representing position where this callstack line stops.
    */
    public org.openide.text.Line getLine () throws DebuggerException {
        try {
            return Utils.getLineForSource (
                       getClassName (),
                       getSourceName (),
                       getLineNumber ()
                   );
        } catch (DebuggerException e) {
            Line l = Utils.getLine (
                         getClassName (),
                         getLineNumber ()
                     );
            if (l != null) return l;
            throw e;
        }
    }
}

/*
* Log
*  6    Gandalf   1.5         11/8/99  Jan Jancura     Somma classes renamed
*  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         9/15/99  Jan Jancura     
*  3    Gandalf   1.2         7/13/99  Jan Jancura     
*  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         6/1/99   Jan Jancura     
* $
*/

