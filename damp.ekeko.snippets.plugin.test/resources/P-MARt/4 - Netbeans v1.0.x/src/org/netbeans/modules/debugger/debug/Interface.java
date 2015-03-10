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

import org.netbeans.modules.debugger.support.util.Protector;

import sun.tools.debug.*;


/**
*
* @author   Jan Jancura
*/
class Interface {

    // thread .........................................................................................

    static String getName (
        final ToolsDebugger debugger,
        final RemoteThread thread
    ) throws Exception {
        if (debugger.synchronizer == null) return null;
        return (String) new Protector ("Interface.getName") { // NOI18N
                   public Object protect () throws Exception {
                       return thread.getName ();
                   }
               }.throwAndWait (debugger.synchronizer, debugger.killer);
    }

    static RemoteStackFrame getCurrentFrame (
        final ToolsDebugger debugger,
        final RemoteThread thread
    ) throws Exception {
        if (debugger.synchronizer == null) return null;
        return (RemoteStackFrame) new Protector ("Interface.getCurrentStack") { // NOI18N
                   public Object protect () throws Exception {
                       return thread.getCurrentFrame ();
                   }
               }.throwAndWait (debugger.synchronizer, debugger.killer);
    }

    static boolean isSuspended (
        final ToolsDebugger debugger,
        final RemoteThread thread
    ) {
        if (debugger.synchronizer == null) return false;
        return ((Boolean) new Protector ("Interface.dumpStack") { // NOI18N
                    public Object protect () {
                        return new Boolean (thread.isSuspended ());
                    }
                }.wait (debugger.synchronizer, debugger.killer)).booleanValue ();
    }

    static RemoteStackFrame[] dumpStack (
        final ToolsDebugger debugger,
        final RemoteThread thread
    ) throws Exception {
        if (debugger.synchronizer == null) return null;
        return (RemoteStackFrame[]) new Protector ("Interface.dumpStack") { // NOI18N
                   public Object protect () throws Exception {
                       return thread.dumpStack ();
                   }
               }.throwAndWait (debugger.synchronizer, debugger.killer);
    }

    static void suspend (
        final ToolsDebugger debugger,
        final RemoteThread thread
    ) throws Exception {
        if (debugger.synchronizer == null) return;
        new Protector ("Interface.suspend") { // NOI18N
            public Object protect () throws Exception {
                thread.suspend ();
                return null;
            }
        }.throwAndWait (debugger.synchronizer, debugger.killer);
    }

    static void resume (
        final ToolsDebugger debugger,
        final RemoteThread thread
    ) throws Exception {
        if (debugger.synchronizer == null) return;
        new Protector ("Interface.resume") { // NOI18N
            public Object protect () throws Exception {
                thread.resume ();
                return null;
            }
        }.throwAndWait (debugger.synchronizer, debugger.killer);
    }

    static void cont (
        final ToolsDebugger debugger,
        final RemoteThread thread
    ) throws Exception {
        if (debugger.synchronizer == null) return;
        new Protector ("Interface.cont") { // NOI18N
            public Object protect () throws Exception {
                thread.cont ();
                return null;
            }
        }.throwAndWait (debugger.synchronizer, debugger.killer);
    }


    // thread .........................................................................................

}

/*
* Log
*  3    Gandalf   1.2         1/13/00  Daniel Prusa    NOI18N
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         7/2/99   Jan Jancura     
* $
*/
