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
import java.util.Enumeration;
import java.io.File;

import org.openide.options.SystemOption;
import org.openide.execution.Executor;
import org.openide.execution.ProcessExecutor;
import org.openide.TopManager;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.HelpCtx;

/** Options for a ProcessExecutor.
*
* @author  Ales Novak, Ian Formanek
* @version 1.00, Sep 23, 98
*/
public final class ExternalExecutionSettings extends SystemOption {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 8869568155974043330L;

    /** reference to installed ProcessExecutor */
    private static ProcessExecutor procExecutor;

    /** not a property  - user defined processors - for serialization */
    private static String[] vals;

    public ExternalExecutionSettings() {
        if (procExecutor == null) {
            Enumeration e = Executor.executors();
            Object o;
            while (e.hasMoreElements()) {
                o = e.nextElement();
                if (o instanceof ProcessExecutor) {
                    procExecutor = (ProcessExecutor) o;
                    break;
                }
            }
        }
    }

    /**
    * @return display name
    */
    public String displayName () {
        return org.openide.util.NbBundle.getBundle (ExternalExecutionSettings.class).
               getString ("CTL_External_execution_option");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ExternalExecutionSettings.class);
    }

    /**
    * @param proc is a new processor
    */
    public void setExternalExecutor (NbProcessDescriptor pd) {
        procExecutor.setExternalExecutor (pd);
        // [PENDING - fire property change]
    }

    /**
    * @return processor
    */
    public NbProcessDescriptor getExternalExecutor() {
        return procExecutor.getExternalExecutor ();
    }

}


/*
 * Log
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         7/2/99   Jesse Glick     More help IDs.
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/9/99   Jan Jancura     Bundles moved.
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */



