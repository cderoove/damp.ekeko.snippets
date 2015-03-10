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

package org.netbeans.modules.applet;

import java.util.Enumeration;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.openide.modules.ModuleInstall;
import org.openide.execution.Executor;
import org.openide.execution.ExecutorTask;
import org.openide.execution.ProcessExecutor;
import org.openide.util.NbBundle;
import org.openide.loaders.DataObject;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.loaders.DataFolder;


/**
* Module installation class for applet support
*
* @author Petr Jiricka
*/
public class AppletModule extends ModuleInstall {

    static final long serialVersionUID =7084077369777137515L;
    /** Module installed for the first time. */
    public void installed() {
        // 1. copy the security policy file
        copySecurityPolicy ();
    }

    // -----------------------------------------------------------------------------
    // Private methods

    private void copySecurityPolicy () {
        try {
            org.openide.filesystems.FileUtil.extractJar (
                org.openide.TopManager.getDefault ().getRepository().getDefaultFileSystem().getRoot(),
                getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/applet/appletpolicy.jar") // NOI18N
            );
        } catch (java.io.IOException e) {
            org.openide.TopManager.getDefault ().notifyException (e);
        }
    }

}

/*
 * Log
 *  5    Gandalf   1.4         1/12/00  Petr Jiricka    i18n
 *  4    Gandalf   1.3         1/4/00   Ian Formanek    
 *  3    Gandalf   1.2         11/27/99 Patrik Knakal   
 *  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         10/8/99  Petr Jiricka    
 * $
 */
