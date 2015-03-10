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

import java.util.Enumeration;

import org.openide.TopManager;
import org.openide.execution.ExecInfo;
import org.openide.debugger.DebuggerType;
import org.openide.debugger.DebuggerException;
import org.openide.util.HelpCtx;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.modules.debugger.support.ProcessDebuggerType;
import org.netbeans.modules.debugger.support.ProcessDebuggerInfo;
import org.netbeans.modules.debugger.delegator.DefaultDebuggerType;

/**
* Default debugger type for JPDA debugger.
*/
public class JPDADebuggerType extends ProcessDebuggerType {

    static final long serialVersionUID = 5253430489855859777L;

    private Object readResolve () {
        return new DefaultDebuggerType ();
    }

    /* Gets the display name for this debugger type. */
    public String displayName () {
        return org.openide.util.NbBundle.getBundle (
                   JPDADebuggerType.class
               ).getString ("LAB_JPDADebuggerType");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (JPDADebuggerType.class);
    }

    /* Starts the debugger. */
    public void startDebugger (ExecInfo info, boolean stopOnMain) throws DebuggerException {
        if (!JPDADebuggerModule.installed) {
            TopManager.getDefault ().notify (new NotifyDescriptor.Message (
                                                 NbBundle.getBundle (JPDADebuggerModule.class).getString ("EXC_JPDA_not_installed")
                                             ));
            TopManager.getDefault ().getDebugger ().startDebugger (
                new ProcessDebuggerInfo (
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
            return;
        }
        TopManager.getDefault ().getDebugger ().startDebugger (
            new JPDADebuggerInfo (
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
*  9    Gandalf-post-FCS1.7.2.0     4/12/00  Daniel Prusa    deserialization after 
*       autoupdate
*  8    Gandalf   1.7         11/8/99  Jan Jancura     Ser. ver. UID added
*  7    Gandalf   1.6         11/5/99  Jesse Glick     Context help jumbo patch.
*  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         9/10/99  Jaroslav Tulach Changes to services.
*  4    Gandalf   1.3         9/3/99   Jan Jancura     
*  3    Gandalf   1.2         8/9/99   Jan Jancura     Move process settings 
*       from DebuggerSettings to ProcesDebuggerType
*  2    Gandalf   1.1         8/2/99   Jan Jancura     A lot of bugs...
*  1    Gandalf   1.0         7/15/99  Jan Jancura     
* $
*/
