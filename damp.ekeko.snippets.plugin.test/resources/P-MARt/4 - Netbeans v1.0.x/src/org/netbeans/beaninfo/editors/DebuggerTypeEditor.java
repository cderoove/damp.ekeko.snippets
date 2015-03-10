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

import java.util.ArrayList;
import java.util.Enumeration;
import java.lang.reflect.Array;
import java.io.*;
import java.text.MessageFormat;

import org.openide.debugger.*;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.execution.ExecInfo;
import org.openide.loaders.ExecSupport;

/** Support for property editor for DebuggerType.
*
* @author   Petr Jiricka
*/

public class DebuggerTypeEditor extends ServiceTypeEditor {

    public static final DebuggerType NO_DEBUGGER = new NoDebugger ();

    public DebuggerTypeEditor () {
        super (DebuggerType.class, "LAB_ChooseDebugger", NO_DEBUGGER); // NOI18N
    }

    public static final class NoDebugger extends DebuggerType {

        static final long serialVersionUID =-6872256326426790372L;
        private NoDebugger () {
        }

        protected String displayName () {
            return NbBundle.getBundle (DebuggerTypeEditor.class).getString ("LAB_NoDebugger");
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (NoDebugger.class);
        }

        public void startDebugger (final ExecInfo info, boolean ign) throws DebuggerException {
            throw new DebuggerException ("do not debug") { // NOI18N
                public String getLocalizedMessage () {
                    return MessageFormat.format (NbBundle.getBundle (DebuggerTypeEditor.class).getString ("EXC_NoDebugger"),
                                                 new Object[] { info.getClassName () });
                }
            };
        }

        private Object readResolve () throws ObjectStreamException {
            return NO_DEBUGGER;
        }

    }

    public static final class NoDebuggerBeanInfo extends NoServiceTypeBeanInfo {

        protected String iconResource () {
            return "/org/netbeans/beaninfo/editors/resources/noDebugger.gif"; // NOI18N
        }

    }

}


/*
 * Log
 *  11   Gandalf   1.10        1/12/00  Jesse Glick     More user-friendly null 
 *       service types.
 *  10   Gandalf   1.9         11/26/99 Patrik Knakal   
 *  9    Gandalf   1.8         11/9/99  Jesse Glick     Null executor and 
 *       debugger a little more friendly--now terminate successfully, after 
 *       printing a brief message.
 *  8    Gandalf   1.7         11/8/99  Jesse Glick     Context help.
 *  7    Gandalf   1.6         10/29/99 Jesse Glick     Added "(no compiler)" 
 *       etc. to service type selection panel.
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         9/15/99  Jaroslav Tulach Custom editors for 
 *       services.
 *  4    Gandalf   1.3         6/28/99  Jaroslav Tulach Debugger types are like 
 *       Executors
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/4/99   Petr Jiricka    
 *  1    Gandalf   1.0         5/17/99  Petr Jiricka    
 * $
 */
