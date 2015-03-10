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

package org.netbeans.modules.debugger.jpda;

import java.beans.*;
import java.util.*;
import javax.swing.SwingUtilities;

import com.sun.jdi.StackFrame;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Method;
import com.sun.jdi.LocalVariable;

import org.openide.debugger.DebuggerException;
import org.openide.text.Line;

import org.netbeans.modules.debugger.support.CallStackFrame;
import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.util.Protector;
import org.netbeans.modules.debugger.support.util.Utils;


/**
* Class representating one line of callstack.
*/
public class JPDACallStackFrame extends CallStackFrame {

    /** Stack frame. */
    private StackFrame                    stackFrame;
    /** Debugger link. */
    private JPDADebugger                  debugger;
    /** Cache for old locales value. */
    HashMap                               oldLocales = new HashMap ();

    /**
    * Creates a new call stack line.
    *
    * @param lineNumber
    * @param methodName
    * @param className
    */
    JPDACallStackFrame (
        JPDADebugger debugger,
        StackFrame stackFrame
    ) {
        this.debugger = debugger;
        this.stackFrame = stackFrame;
    }

    /**
    * Returns line number of this frame in this callstack.
    *
    * @return Returns line number of this frame in this callstack.
    */
    public int getLineNumber () throws DebuggerException {
        try {
            return stackFrame.location ().lineNumber ();
        } catch (Exception e) {
        }
        return -1;
    }

    /**
    * Returns method name of this frame in this callstack.
    *
    * @return Returns method name of this frame in this callstack.
    */
    public String getMethodName () throws DebuggerException {
        try {
            Method m = stackFrame.location ().method ();
            if (m == null) return ""; // NOI18N
            return m.name ();
        } catch (Exception e) {
        }
        return ""; // NOI18N
    }

    /**
    * Returns class name of this frame in this callstack.
    *
    * @return Returns class name of this frame in this callstack.
    */
    public String getClassName () throws DebuggerException {
        try {
            return stackFrame.location ().declaringType ().name ();
        } catch (Exception e) {
        }
        return ""; // NOI18N
    }

    /**
    * Returns name of file of this frame.
    *
    * @return Returns name of file of this frame.
    * @throws DebuggerException if informations about source are not included or some other error
    *   occurres.
    */
    public String getSourceName () throws DebuggerException {
        try {
            return stackFrame.location ().sourceName ();
        } catch (Exception e) {
            throw new DebuggerException (e);
        }
    }

    /**
    * Returns current local variables for this frame stack.
    *
    * @return Current local variables for this frame stack.
    */
    public AbstractVariable[] getLocales () {
        try {
            List l = stackFrame.visibleVariables ();
            HashMap newLocales = new HashMap ();
            int i, k = l.size ();
            JPDAVariable[] variables = new JPDAVariable [k];
            for (i = 0; i < k; i++) {
                LocalVariable lv = (LocalVariable) l.get (i);
                variables [i] = (JPDAVariable) oldLocales.get (lv.name ());
                if (variables [i] == null)
                    variables [i] = new JPDAVariable (
                                        debugger,
                                        lv.name (),
                                        stackFrame.getValue (lv),
                                        lv.typeName (),
                                        stackFrame
                                    );
                else {
                    variables [i].update (
                        lv.name (),
                        stackFrame.getValue (lv),
                        lv.typeName (),
                        stackFrame
                    );
                    variables [i].firePropertyChange ();
                }
                newLocales.put (lv.name (), variables [i]);
            }
            oldLocales = newLocales;
            return variables;
        } catch (Exception e) {
        }
        return new AbstractVariable [0];
    }
}

/*
* Log
*  8    Gandalf-post-FCS1.6.3.0     3/28/00  Daniel Prusa    
*  7    Gandalf   1.6         1/13/00  Daniel Prusa    NOI18N
*  6    Gandalf   1.5         11/8/99  Jan Jancura     Somma classes renamed
*  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         9/15/99  Jan Jancura     
*  3    Gandalf   1.2         9/9/99   Jan Jancura     catching exceptions & 
*       locales repaired
*  2    Gandalf   1.1         9/2/99   Jan Jancura     
*  1    Gandalf   1.0         7/13/99  Jan Jancura     
* $
*/



