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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
* Adapter class for ThreadGroupListener.
*
* @author   Jan Jancura
* @version  0.12, Dec 9, 1998
*/
public class ThreadGroupAdapter implements ThreadGroupListener {

    /**
    * Called when some property is changed in this thread group.
    *
    * @param e event
    */
    public void propertyChange (PropertyChangeEvent e) {}

    /**
    * Called when some thread is created in this thread group.
    *
    * @param t this new thread
    */
    public void threadCreated (AbstractThread t) {}

    /**
    * Called when some thread is destroyed in this thread group.
    *
    * @param t destroyed thread
    */
    public void threadDeath (AbstractThread t) {}

    /**
    * Called when some thread group is created in this thread group.
    *
    * @param t this new thread group.
    */
    public void threadGroupCreated (AbstractThreadGroup g) {}

    /**
    * Called when some thread group is destroyed in this thread group.
    *
    * @param t destroyed thread group.
    */
    public void threadGroupDeath (AbstractThreadGroup g) {}
}

/*
 * Log
 *  4    Gandalf   1.3         11/8/99  Jan Jancura     Somma classes renamed
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         8/17/99  Jan Jancura     Actions for session 
 *       added & Thread group current property
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 */
