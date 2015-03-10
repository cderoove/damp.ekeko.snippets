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

package org.openide.nodes;

import java.util.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/** Support class for storing cookies and
* retriving them by representation class.
* Provides simple notifications about changes
* in cookies.
*
* @author Jaroslav Tulach
*/
public final class CookieSet extends Object {
    /** list of cookies (Class, Node.Cookie) */
    private HashMap map = new HashMap (7);

    /** set of listeners 
     * @associates ChangeListener*/
    private HashSet list = new HashSet (7);

    /** Default constructor. */
    public CookieSet() {}

    /** Add a new cookie to the set. If a cookie of the same
    * <em>actual</em> (not representation!) class is already there,
    * it is replaced.
    * <p>Cookies inserted earlier are given preference during lookup,
    * in case a supplied representation class matches more than one cookie
    * in the set.
    *
    * @param cookie cookie to add
    */
    public synchronized void add (Node.Cookie cookie) {
        Class clazz = cookie.getClass ();
        Node.Cookie previous = (Node.Cookie)map.get (clazz);
        if (previous != null) {
            unregisterCookie (clazz, previous);
        }

        // insert the cookie to be accessible from all superclass
        Class c = cookie.getClass ();
        registerCookie (c, cookie);

        fireChangeEvent ();
    }

    /** Remove a cookie from the set.
    * @param cookie the cookie to remove
    */
    public synchronized void remove (Node.Cookie cookie) {
        unregisterCookie (cookie.getClass (), cookie);

        fireChangeEvent ();
    }

    /** Get a cookie.
    *
    * @param clazz the representation class
    * @return a cookie assignable to the representation class, or <code>null</code> if there is none
    */
    public synchronized Node.Cookie getCookie (Class clazz) {
        return (Node.Cookie)map.get (clazz);
    }

    /** Add a listener to changes in the cookie set.
    * @param l the listener to add
    */
    public synchronized void addChangeListener (ChangeListener l) {
        list.add (l);
    }

    /** Remove a listener to changes in the cookie set.
    * @param l the listener to remove
    */
    public synchronized void removeChangeListener (ChangeListener l) {
        list.remove (l);
    }

    /** Fires change event
    */
    private void fireChangeEvent () {
        Iterator it;
        synchronized (this) {
            it = ((Set)list.clone ()).iterator ();
        }
        ChangeEvent ev = new ChangeEvent (this);

        while (it.hasNext ()) {
            ChangeListener l = (ChangeListener)it.next ();
            l.stateChanged (ev);
        }
    }

    /** Attaches cookie to given class and all its superclasses and
    * superinterfaces.
    *
    * @param c class or null
    * @param cookie cookie to attach
    */
    private void registerCookie (Class c, Node.Cookie cookie) {
        if (c == null) return;

        Object orig = map.put (c, cookie);
        if (orig != null) {
            // return it back and finish
            map.put (c, orig);
            return;
        }

        registerCookie (c.getSuperclass (), cookie);

        Class[] inter = c.getInterfaces ();
        for (int i = 0; i < inter.length; i++) {
            registerCookie (inter[i], cookie);
        }
    }

    /** Removes cookie from the class and all its superclasses and
    * superinterfaces.
    *
    * @param c class or null
    * @param cookie cookie to attach
    */
    private void unregisterCookie (Class c, Node.Cookie cookie) {
        if (c == null) return;

        // if different cookie is attached to class c stop removing
        if (cookie != map.get (c)) return;

        // remove the cookie
        map.remove (c);

        unregisterCookie (c.getSuperclass (), cookie);

        Class[] inter = c.getInterfaces ();
        for (int i = 0; i < inter.length; i++) {
            unregisterCookie (inter[i], cookie);
        }
    }

}

/*
* Log
*  5    Gandalf   1.4         11/23/99 Jaroslav Tulach Uses HashMap (7) instead 
*       of 101
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         3/17/99  Jesse Glick     [JavaDoc]
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
