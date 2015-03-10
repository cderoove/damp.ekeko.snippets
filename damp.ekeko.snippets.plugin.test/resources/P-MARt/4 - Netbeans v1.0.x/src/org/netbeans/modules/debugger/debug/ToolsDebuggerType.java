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

import java.util.Enumeration;

import org.openide.TopManager;
import org.openide.execution.ExecInfo;
import org.openide.debugger.DebuggerType;
import org.openide.debugger.DebuggerException;
import org.openide.util.HelpCtx;
import org.openide.debugger.DebuggerType;

import org.netbeans.modules.debugger.support.ProcessDebuggerType;
import org.netbeans.modules.debugger.delegator.DefaultDebuggerType;

/**
* Tools debugger type for sun.tools.debug debugger.
*/
public class ToolsDebuggerType extends ProcessDebuggerType {

    static final long serialVersionUID = 5253430489855859666L;

    private Object readResolve () {
        return new DefaultDebuggerType ();
    }

    /* Gets the display name for this debugger type. */
    public String displayName () {
        return org.openide.util.NbBundle.getBundle (
                   ToolsDebuggerType.class
               ).getString ("LAB_ToolsDebuggerTypeName");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ToolsDebuggerType.class);
    }

    /* Starts the debugger. */
    public void startDebugger (ExecInfo info, boolean stopOnMain) throws DebuggerException {
        TopManager.getDefault ().getDebugger ().startDebugger (
            new ToolsDebuggerInfo (
                info.getClassName (),
                info.getArguments (),
                stopOnMain ? info.getClassName () : null,
                getDebuggerProcess (),
                getClassPath (),
                getBootClassPath (),
                getRepositoryPath (),
                getLibraryPath (),
                isClassic ()
            )
        );
    }

}

/*
* Log
*  4    Gandalf-post-FCS1.2.2.0     4/17/00  Daniel Prusa    autoupdate
*  3    Gandalf   1.2         11/5/99  Jesse Glick     Context help jumbo patch.
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         10/6/99  Jan Jancura     
* $
*/

