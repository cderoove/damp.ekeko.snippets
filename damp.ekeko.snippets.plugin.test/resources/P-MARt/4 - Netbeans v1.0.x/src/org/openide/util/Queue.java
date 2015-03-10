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

package org.openide.util;

import org.openide.util.enum.QueueEnumeration;

/** Queue of objects. When there is no object in the queue the process
* is suspended till some arrives.
*
* @author Jaroslav Tulach
* @version 0.10, Feb 06, 1998
*/
public class Queue extends Object {
    /** Queue enumeration */
    private QueueEnumeration queue = new QueueEnumeration ();

    /** Adds new item.
    * @param o object to add
    */
    public synchronized void put (Object o) {
        queue.put (o);
        notify ();
    }

    /** Gets an object from the queue. If there is no such object the
    * thread is suspended until some object arrives
    *
    * @return object from the queue
    */
    public synchronized Object get () {
        for (;;) {
            try {
                return queue.nextElement ();
            } catch (java.util.NoSuchElementException ex) {
                try {
                    wait ();
                } catch (InterruptedException ex2) {
                }
            }
        }
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach Change of package of 
 *       DataObject
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
