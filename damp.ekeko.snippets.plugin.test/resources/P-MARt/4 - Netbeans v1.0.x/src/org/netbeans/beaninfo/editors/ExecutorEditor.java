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

package org.netbeans.beaninfo.editors;

import java.beans.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;

import org.openide.*;
import org.openide.execution.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Support for property editor for Executor.
*
* @author   Jaroslav Tulach
*/
public class ExecutorEditor extends ServiceTypeEditor {

    public static final Executor NO_EXECUTOR = new NoExecutor ();

    public ExecutorEditor () {
        super (Executor.class, "LAB_ChooseExecutor", NO_EXECUTOR); // NOI18N
    }

    public static final class NoExecutor extends Executor {

        static final long serialVersionUID =8115656811934986516L;
        private NoExecutor () {
        }

        protected String displayName () {
            return NbBundle.getBundle (ExecutorEditor.class).getString ("LAB_NoExecutor");
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (NoExecutor.class);
        }

        public ExecutorTask execute (final ExecInfo info) throws IOException {
            throw new IOException ("do not execute") { // NOI18N
                public String getLocalizedMessage () {
                    return MessageFormat.format (NbBundle.getBundle (DebuggerTypeEditor.class).getString ("EXC_NoExecutor"),
                                                 new Object[] { info.getClassName () });
                }
            };
        }

        private Object readResolve () throws ObjectStreamException {
            return NO_EXECUTOR;
        }

    }

    public static final class NoExecutorBeanInfo extends NoServiceTypeBeanInfo {

        protected String iconResource () {
            return "/org/netbeans/beaninfo/editors/resources/noExecutor.gif"; // NOI18N
        }

    }

}


/*
 * Log
 *  10   src-jtulach1.9         1/12/00  Jesse Glick     More user-friendly null 
 *       service types.
 *  9    src-jtulach1.8         11/26/99 Patrik Knakal   
 *  8    src-jtulach1.7         11/9/99  Jesse Glick     Null executor and 
 *       debugger a little more friendly--now terminate successfully, after 
 *       printing a brief message.
 *  7    src-jtulach1.6         11/8/99  Jesse Glick     Context help.
 *  6    src-jtulach1.5         10/29/99 Jesse Glick     Added "(no compiler)" 
 *       etc. to service type selection panel.
 *  5    src-jtulach1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    src-jtulach1.3         9/15/99  Jaroslav Tulach Custom editors for 
 *       services.
 *  3    src-jtulach1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    src-jtulach1.1         5/27/99  Jaroslav Tulach Executors rearanged.
 *  1    src-jtulach1.0         3/19/99  Ales Novak      
 * $
 */
