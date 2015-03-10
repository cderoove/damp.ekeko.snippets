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
import org.openide.options.ContextSystemOption;
import org.openide.actions.StartDebuggerAction;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.execution.NbClassPath;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

/**
* Settings for java debugger.
*
* @author Jan Jancura, Ian Formanek
* @version 0.11, May 16, 1998
*/
public class DebuggerSettings extends SystemOption {

    // static .....................................................................................

    /** generated Serialized Version UID */
    static final long serialVersionUID = 833203088075073629L;

    /** actionOnTraceInto property name */
    public static final String PROP_ACTION_ON_TRACE_INTO = "actionOnTraceInto"; // NOI18N
    /** actionOnTraceIntoSet property name */
    public static final String PROP_ACTION_ON_TRACE_INTO_SET = "actionOnTraceIntoSet"; // NOI18N
    /** desktop property name */
    public static final String PROP_DESKTOP = "desktop"; // NOI18N
    /** remoteDebugger property name */
    public static final String PROP_REMOTE_DEBUGGER = "remoteDebugger"; // NOI18N
    /** Property name of the runCompilation property */
    public static final String PROP_RUN_COMPILATION = "runCompilation"; // NOI18N

    /** Constant for actionOnTraceInto property. */
    public static final int ACTION_ON_TI_STOP = 0;
    /** Constant for actionOnTraceInto property. */
    public static final int ACTION_ON_TI_TRACE_OUT = 1;


    // SystemOption implementation ..................................................................

    /**
    * Returns name of this setings.
    */
    public String displayName () {
        return NbBundle.getBundle (DebuggerSettings.class).getString ("CTL_Debugger_option");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (DebuggerSettings.class);
    }


    // SystemOption implementation ..................................................................

    {
        if (getActionTIConstants () == null) {
            putActionTI (
                ACTION_ON_TI_STOP,
                NbBundle.getBundle (DebuggerSettings.class).getString ("CTL_Action_on_TI_stop")
            );
            putActionTI (
                ACTION_ON_TI_TRACE_OUT,
                NbBundle.getBundle (DebuggerSettings.class).getString ("CTL_Action_on_TI_step_out")
            );
        }
        setActionOnTraceInto (ACTION_ON_TI_TRACE_OUT);
    }

    int[] getActionTIConstants () {
        return (int[]) getProperty ("actionTIConstants"); // NOI18N
    }

    String[] getActionTIComments () {
        return (String[]) getProperty ("actionTIComments"); // NOI18N
    }

    public void putActionTI (int actionTIConstant, String actionTIComments) {
        int[] con = getActionTIConstants ();
        if (con == null) con = new int [] {};
        String[] comm = getActionTIComments ();
        if (comm == null) comm = new String [] {};

        int[] ncon = new int [con.length + 1];
        String[] ncomm = new String [comm.length + 1];
        System.arraycopy (con, 0, ncon, 0, con.length);
        ncon [con.length] = actionTIConstant;
        System.arraycopy (comm, 0, ncomm, 0, comm.length);
        ncomm [comm.length] = actionTIComments;

        putProperty ("actionTIConstants", ncon, false); // NOI18N
        putProperty ("actionTIComments", ncomm, false); // NOI18N
    }

    public void removeActionTI (int actionTIConstant) {
        int[] con = getActionTIConstants ();
        String[] comm = getActionTIComments ();

        int i, k = con.length;
        for (i = 0; i < k; i++)
            if (con [i] == actionTIConstant)
                break;
        if (i == k) return;

        int[] ncon = new int [con.length - 1];
        String[] ncomm = new String [comm.length - 1];
        System.arraycopy (con, 0, ncon, 0, i);
        System.arraycopy (con, i + 1, ncon, i, con.length - i - 1);
        System.arraycopy (comm, 0, ncomm, 0, i);
        System.arraycopy (comm, i + 1, ncomm, 0, comm.length - i - 1);

        putProperty ("actionTIConstants", ncon, false); // NOI18N
        putProperty ("actionTIComments", ncomm, false); // NOI18N
    }


    // properties .................................................................................

    /**
    * Getter for remoteDebugger property. 
    */
    public AbstractDebugger getRemoteDebugger () throws DebuggerNotFoundException {
        return (AbstractDebugger) TopManager.getDefault ().getDebugger ();
    }

    /**
    * Setter for remoteDebugger property. 
    */
    public void setRemoteDebugger (AbstractDebugger javaDebugger) throws DebuggerNotFoundException {
        AbstractDebugger oldValue = getRemoteDebugger ();
        oldValue.setDebugger (javaDebugger);

        // fire the PropertyChange
        firePropertyChange (PROP_REMOTE_DEBUGGER, oldValue, javaDebugger);
    }

    /**
    * Getter for runCompilation property. 
    */
    public boolean getRunCompilation () {
        return StartDebuggerAction.getRunCompilation ();
    }

    /**
    * Setter for runCompilation property. 
    */
    public void setRunCompilation(boolean value) {
        boolean oldValue = getRunCompilation ();
        StartDebuggerAction.setRunCompilation (value);
        if (oldValue != value)
            firePropertyChange (PROP_RUN_COMPILATION, new Boolean (oldValue), new Boolean (value));
    }

    /**
    * Getter for desktop property. 
    */
    public String getDesktop () {
        return StartDebuggerAction.getWorkspace ();
    }

    /**
    * Setter for desktop property. 
    */
    public void setDesktop (String desktop) {
        String oldValue = getDesktop ();
        if (desktop.equals (oldValue)) return;
        StartDebuggerAction.setWorkspace(desktop);
        // fire the PropertyChange
        firePropertyChange (PROP_DESKTOP, new Boolean (oldValue), new Boolean (desktop));
    }

    /**
    * Getter for actionOnTraceInto property.
    */
    public int getActionOnTraceInto () {
        return ((Integer) getProperty (PROP_ACTION_ON_TRACE_INTO)).intValue ();
    }

    /**
    * Setter for actionOnTraceInto property.
    */
    public void setActionOnTraceInto (int actionOnTraceInto) {
        putProperty (PROP_ACTION_ON_TRACE_INTO, new Integer (actionOnTraceInto), true);
    }

    /**
    * Getter for actionOnTraceIntoSet property.
    */
    public boolean isActionOnTraceIntoSet () {
        Boolean b = (Boolean) getProperty (PROP_ACTION_ON_TRACE_INTO_SET);
        if (b == null) return false;
        return b.booleanValue ();
    }

    /**
    * Setter for actionOnTraceIntoSet property.
    */
    public void setActionOnTraceIntoSet (boolean actionOnTraceIntoSet) {
        putProperty (PROP_ACTION_ON_TRACE_INTO_SET, new Boolean (actionOnTraceIntoSet), true);
    }
}

/*
 * Log
 *  17   Gandalf   1.16        1/18/00  Daniel Prusa    StartDebugger action
 *  16   Gandalf   1.15        1/17/00  Jan Jancura     Some propertie removed 
 *       form DebugerSettings
 *  15   Gandalf   1.14        1/13/00  Daniel Prusa    NOI18N
 *  14   Gandalf   1.13        11/29/99 Jan Jancura     
 *  13   Gandalf   1.12        11/8/99  Jan Jancura     Somma classes renamed
 *  12   Gandalf   1.11        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   Gandalf   1.10        10/5/99  Jan Jancura     Serialization of 
 *       debugger.
 *  10   Gandalf   1.9         8/9/99   Jan Jancura     Move process settings 
 *       from DebuggerSettings to ProcesDebuggerType
 *  9    Gandalf   1.8         8/1/99   Ian Formanek    access modifiers cleaned
 *  8    Gandalf   1.7         7/22/99  Jan Jancura     option -classis is 
 *       defaultly missing
 *  7    Gandalf   1.6         7/21/99  Jan Jancura     
 *  6    Gandalf   1.5         7/13/99  Jan Jancura     
 *  5    Gandalf   1.4         6/10/99  Jan Jancura     
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/4/99   Jan Jancura     
 *  2    Gandalf   1.1         6/4/99   Jan Jancura     
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 */
