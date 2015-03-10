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

package org.netbeans.modules.emacs;

import java.io.*;
import java.util.*;

import org.openide.util.WeakSet;

public class EmacsProxier implements EmacsListener, Protocol {

    private static int proxierIDCounter = 0;

    private Connection conn;
    private final int proxierID;
    private boolean shutdown = false;

    /**
     * @associates EmacsProxier 
     */
    private static final Set openProxiers = Collections.synchronizedSet (new WeakSet ());

    public EmacsProxier (String host, int port, String auth) throws IOException {
        conn = Connection.addClient (host, port, auth);
        synchronized (EmacsProxier.class) {
            proxierID = proxierIDCounter++;
        }
        conn.addEmacsListener (this);
        call (CMD_create);
        openProxiers.add (this);
    }

    public synchronized void close () {
        if (Connection.DEBUG) System.err.println ("close() on " + this);
        if (conn != null && ! shutdown) {
            shutdown = true;
            call (CMD_close);
            conn.removeEmacsListener (this);
            try {
                conn.removeClient ();
            } catch (IOException ioe) {
                ioe.printStackTrace ();
            }
            conn = null;
        }
        openProxiers.remove (this);
    }

    protected void finalize () throws Exception {
        close ();
    }

    public static void closeAll () {
        if (Connection.DEBUG) System.err.println ("EmacsProxier.closeAll");
        Iterator it;
        synchronized (openProxiers) {
            it = new HashSet (openProxiers).iterator ();
        }
        while (it.hasNext ())
            ((EmacsProxier) it.next ()).close ();
    }

    public Object[] function (String type, Object[] args) throws EmacsException {
        if (conn == null)
            throw new EmacsException (this + " is disabled, cannot call functions on it");
        try {
            return conn.function (proxierID + ":" + type, args);
        } catch (EmacsException ee) {
            close ();
            throw ee;
        }
    }

    public Object[] function (String type) throws EmacsException {
        return function (type, new Object[0]);
    }

    public void call (String type, Object[] args) {
        if (conn == null) return;
        try {
            conn.call (proxierID + ":" + type, args);
        } catch (EmacsException ee) {
            ee.printStackTrace ();
            close ();
        }
    }

    public  void call (String type) {
        call (type, new Object[0]);
    }

    /**
     * @associates EmacsListener 
     */
    private final Set listeners = new HashSet (); // Set<EmacsListener>
    public synchronized void addEmacsListener (EmacsListener l) {
        listeners.add (l);
    }
    public synchronized void removeEmacsListener (EmacsListener l) {
        listeners.remove (l);
    }

    public void callback (EmacsEvent ev) {
        String type = ev.getType ();
        int idx = type.indexOf (':');
        // XXX debug code about who it is being sent to
        if (idx != -1) {
            try {
                int who = Integer.parseInt (type.substring (0, idx));
                if (who == proxierID) {
                    EmacsEvent resend = new EmacsEvent (this, type.substring (idx + 1), ev.getArgs (), ev.isOutOfSequence ());
                    synchronized (this) {
                        Iterator it = listeners.iterator ();
                        while (it.hasNext ())
                            ((EmacsListener) it.next ()).callback (resend);
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace ();
            }
        }
    }

    public String toString () {
        return "EmacsProxier[" + conn + "#" + proxierID + "]";
    }

}
