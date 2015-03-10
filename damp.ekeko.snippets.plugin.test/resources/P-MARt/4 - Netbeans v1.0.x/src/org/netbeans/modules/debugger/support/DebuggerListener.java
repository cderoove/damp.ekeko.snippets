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

import org.openide.debugger.Breakpoint;
import org.openide.debugger.Watch;

/**
* This listener notifies about changing in the debugger (e. g. adding / removing
* breakpoints, threads, thread groups and watches).
*
* @author   Jan Jancura
* @version  0.11, Jan 30, 1998
*/
public interface DebuggerListener {

    /**
    * Called when some breakpoint is added.
    *
    * @param breakpoint
    */
    public void breakpointAdded (CoreBreakpoint breakpoint);

    /**
    * Called when some breakpoint is removed.
    *
    * @param breakpoint
    */
    public void breakpointRemoved (CoreBreakpoint b);

    /**
    * Called when some thread is added.
    *
    * @param thread
    */
    public void threadCreated (AbstractThread thread);

    /**
    * Called when some thread is removed.
    *
    * @param thread
    */
    public void threadDeath (AbstractThread thread);

    /**
    * Called when some thread group is added.
    *
    * @param thread
    */
    public void threadGroupCreated (AbstractThreadGroup threadGroup);

    /**
    * Called when some thread group is removed.
    *
    * @param thread
    */
    public void threadGroupDeath (AbstractThreadGroup threadGroup);

    /**
    * Called when some watch is added.
    *
    * @param watch
    */
    public void watchAdded (AbstractWatch watch);

    /**
    * Called when some watch is removed.
    *
    * @param watch
    */
    public void watchRemoved (AbstractWatch watch);
}

/*
 * Log
 *  5    Gandalf   1.4         11/8/99  Jan Jancura     Somma classes renamed
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         7/13/99  Jan Jancura     
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 */
