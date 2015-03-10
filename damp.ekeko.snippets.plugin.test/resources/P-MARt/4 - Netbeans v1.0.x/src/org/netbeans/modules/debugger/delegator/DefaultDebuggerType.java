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

package org.netbeans.modules.debugger.delegator;

/**
 *
 * @author  dprusa
 * @version 
 */

import java.util.Enumeration;
import java.util.Iterator;

import org.openide.TopManager;
import org.openide.execution.ExecInfo;
import org.openide.execution.NbProcessDescriptor;
import org.openide.debugger.DebuggerType;
import org.openide.debugger.DebuggerException;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.util.HelpCtx;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import org.netbeans.modules.debugger.support.ProcessDebuggerType;
import org.netbeans.modules.debugger.support.ProcessDebuggerInfo;
import org.netbeans.modules.debugger.support.AbstractDebugger;

public class DefaultDebuggerType extends ProcessDebuggerType {

    static final long serialVersionUID = 5121438889855859123L;

    /** Property name of the debuggerType property */
    public static final String PROP_DEBUGGER_TYPE = "debuggerType"; // NOI18N

    // variables .................................................................

    /** debugger type */
    private String                selectedDebuggerType = null;


    // ...........................................................................

    /* Gets the display name for this debugger type. */
    public String displayName () {
        return org.openide.util.NbBundle.getBundle (
                   DefaultDebuggerType.class
               ).getString ("LAB_DefaultDebuggerType");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (DefaultDebuggerType.class);
    }

    /**
    * Setter for debuggerType property.
    */
    public void setDebuggerType (String debuggerType) {
        selectedDebuggerType = debuggerType;
        if (!setted) {
            boolean old = classic;
            classic = getClassicDefault ();
            firePropertyChange (PROP_CLASSIC, new Boolean (old), new Boolean (classic));
        }
    }

    /**
    * Getter for debuggerType property.
    */
    public String getDebuggerType () {
        try {
            DelegatingDebugger debugger = (DelegatingDebugger) TopManager.
                                          getDefault ().getDebugger ();
            Iterator list = debugger.getRegisteredDebuggers ().iterator ();
            String def = null;
            boolean found = false;
            while (list.hasNext ()) {
                AbstractDebugger deb = debugger.createDebugger ((Class) list.next ());
                if (deb != null) {
                    if (def == null)
                        def = deb.getVersion ();
                    if (deb.getVersion ().equals (selectedDebuggerType)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found)
                selectedDebuggerType = def;
        }
        catch (DebuggerNotFoundException e) {
            selectedDebuggerType = null;
        }
        return selectedDebuggerType;
    }

    /**
    * Returns selected dubugger.
    */
    protected Class getDebuggerTypeClass () {
        try {
            DelegatingDebugger debugger = (DelegatingDebugger) TopManager.
                                          getDefault ().getDebugger ();
            Class debuggerClass = null;
            Iterator list = debugger.getRegisteredDebuggers ().iterator ();
            while (list.hasNext ()) {
                debuggerClass = (Class) list.next ();
                AbstractDebugger deb = debugger.createDebugger (debuggerClass);
                if (deb.getVersion ().equals (getDebuggerType ()))
                    break;
            }
            return debuggerClass;
        }
    catch (DebuggerNotFoundException e) {}
        return null;
    }

    /**
    * Determines if classic switch will be used defaultly or not.
    */
    protected boolean getClassicDefault () {
        if (System.getProperty ("java.version").equals ("1.3.0")) { // NOI18N
            getDebuggerType ();
            Class clazz = getDebuggerTypeClass ();
            if ((clazz != null) && 
                (clazz.getName ().equals ("org.netbeans.modules.debugger.jpda.JPDADebugger"))) // NOI18N
                if (Utilities.getOperatingSystem () == Utilities.OS_WIN2000)
                    return false;
                else if (Utilities.getOperatingSystem () == Utilities.OS_SOLARIS)
                    return false;
        }
        return super.getClassicDefault ();
    }
    
    /* Starts the debugger. */
    protected void startDebugger (
        String className,
        String[] arguments,
        String stopClassName,
        NbProcessDescriptor process,
        String classPath,
        String bootClassPath,
        String repositoryPath,
        String libraryPath,
        boolean classic,
        ExecInfo info,
        boolean stopOnMain
    ) throws DebuggerException {
        Class debuggerType = getDebuggerTypeClass ();
        if (debuggerType == null) {
            TopManager.getDefault ().notify (new NotifyDescriptor.Message (
                                                 NbBundle.getBundle (DefaultDebuggerType.class).
                                                 getString ("EXC_Debugger_not_installed")
                                             ));
            return;
        }
        try {
            TopManager.getDefault ().getDebugger ().startDebugger (
                new DefaultDebuggerInfo (
                    debuggerType,
                    className,
                    arguments,
                    stopClassName,
                    process,
                    classPath,
                    bootClassPath,
                    repositoryPath,
                    libraryPath,
                    classic
                )
            );
        }
        catch (DebuggerNotFoundException e) {
        }
    }

}