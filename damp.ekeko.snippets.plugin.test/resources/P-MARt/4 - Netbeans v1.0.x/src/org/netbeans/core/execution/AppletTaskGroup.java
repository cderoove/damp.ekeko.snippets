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

/** Class that signalizes to input/output subsystem that special care
* is needed. Especially - all requests for output are handled by invokeLater
* call. This is important for HotJavaBean where following order of locking is kept
* AWTTreeLock, Document or Document; e.g we must be conscious that all subsequent
* calls that needs AWTTreeLock and already have Document lock can cause deadlock.
* Solution is never acquire the AWTTreeLock in thread that possibly holds Document lock.
* So process such calls as invokeLater.
*
* @author Ales Novak
* @version 0.10, May 29, 1998
*/
class AppletTaskGroup extends TaskThreadGroup {
    AppletTaskGroup(ThreadGroup parent, String name) {
        super(parent, name);
    }
}

/*
 * Log
 *  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
