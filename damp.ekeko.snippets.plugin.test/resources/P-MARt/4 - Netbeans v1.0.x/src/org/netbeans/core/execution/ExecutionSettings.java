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

package org.netbeans.core.execution;

import java.util.ResourceBundle;

import org.openide.options.SystemOption;
import org.openide.actions.ExecuteAction;
import org.openide.util.HelpCtx;

/** Settings for Execution
*
* @author Ales Novak
*/
public class ExecutionSettings extends SystemOption {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4261950851983665892L;

    /** Property name of the workspace property */
    public static final String PROP_WORKSPACE = "workspace"; // NOI18N
    /** Property name of the reuse property */
    public static final String PROP_REUSE = "reuse"; // NOI18N
    /** Property name of the clear property */
    public static final String PROP_CLEAR = "clear"; // NOI18N
    /** Property name of the runCompilation property */
    public static final String PROP_RUN_COMPILATION = "runCompilation"; // NOI18N

    /** flag for reuse TaskIO */
    private static boolean reuse = true;
    /** flag for clearing TaskIO */
    private static boolean clear = true;

    /** @return the display name of Execution Settings */
    public String displayName () {
        return ProcessNode.getBundle().getString("CTL_Execution_option");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ExecutionSettings.class);
    }

    // ------------------------------------------
    // property access methods

    public void setWorkspace (String workspace) {
        String oldValue = getWorkspace ();
        if (workspace.equals (oldValue)) return;
        ExecuteAction.setWorkspace (workspace);
        // fire the PropertyChange
        firePropertyChange (PROP_WORKSPACE, new Boolean (oldValue), new Boolean (workspace));
    }
    public String getWorkspace () {
        return ExecuteAction.getWorkspace ();
    }
    /** setter for reuse */
    public void setReuse(boolean x) {
        boolean old = reuse;
        reuse = x;
        if (old != x)
            firePropertyChange(PROP_REUSE, new Boolean(old), new Boolean(x));
    }
    /** getter for reuse */
    public boolean getReuse() {
        return reuse;
    }
    /** setter for clear */
    public void setClear(boolean x) {
        boolean old = clear;
        clear = x;
        if (old != x)
            firePropertyChange(PROP_CLEAR, new Boolean(old), new Boolean(x));
    }
    /** getter for clear */
    public boolean getClear() {
        return clear;
    }
    /** setter for runCompilation property */
    public void setRunCompilation(boolean value) {
        boolean oldValue = getRunCompilation();
        ExecuteAction.setRunCompilation(value);
        if (oldValue != value)
            firePropertyChange(PROP_RUN_COMPILATION, new Boolean(oldValue), new Boolean(value));
    }
    /** getter for runCompilation property */
    public boolean getRunCompilation() {
        return ExecuteAction.getRunCompilation();
    }
}

/*
 * Log
 *  8    Gandalf   1.7         1/12/00  Ales Novak      i18n
 *  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         7/2/99   Jesse Glick     More help IDs.
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/14/99  Ales Novak      bugfix for #1667 #1598 
 *       #1625
 *  3    Gandalf   1.2         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  2    Gandalf   1.1         1/21/99  Ales Novak      
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
