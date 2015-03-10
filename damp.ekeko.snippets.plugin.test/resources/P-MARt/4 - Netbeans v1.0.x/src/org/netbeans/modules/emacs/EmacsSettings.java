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

import java.util.*;
import javax.swing.JEditorPane;
import javax.swing.text.EditorKit;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;

public class EmacsSettings extends SystemOption {

    public static final String PROP_HOST = "host";
    public static final String PROP_PORT = "port";
    public static final String PROP_PASSIVE = "passive";
    public static final String PROP_PASSWORD = "password";
    public static final String PROP_MIME_TYPES = "mimeTypes";
    public static final String PROP_DEBUG = "debug";

    protected void initialize () {
        // XXX FOR TESTING... switch back to false later
        putProperty (PROP_DEBUG, Boolean.TRUE, false);
        Connection.DEBUG = true;
        System.err.println("EmacsSettings.initialize");

        putProperty (PROP_HOST, "localhost", false);
        putProperty (PROP_PORT, new Integer (3219), false);
        putProperty (PROP_PASSIVE, Boolean.TRUE, false);
        putProperty (PROP_PASSWORD, "changeme", false);
        putProperty (PROP_MIME_TYPES, new String[] { });
        setMimeTypes (new String[] { "text/x-java", "text/plain", "text/html", "text/xml",
                                     "text/x-properties", "text/x-dtd", "content/unknown" });
        Connection.startServer (3219);
    }

    public String displayName () {
        return "Emacs";
    }

    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
        // return new HelpCtx (EmacsSettings.class);
    }

    static final EmacsSettings DEFAULT = (EmacsSettings) findObject (EmacsSettings.class, true);

    public String getHost () {
        return (String) getProperty (PROP_HOST);
    }

    public void setHost (String host) {
        if (isDebug ()) System.err.println("setHost: " + host);
        putProperty (PROP_HOST, host, true);
    }

    public int getPort () {
        return ((Integer) getProperty (PROP_PORT)).intValue ();
    }

    public void setPort (int p) {
        if (isDebug ()) System.err.println("setPort: " + p);
        int old = getPort ();
        if (old != p) {
            if (isPassive ()) {
                Connection.stopServer (old);
                Connection.startServer (p);
            }
            putProperty (PROP_PORT, new Integer (p), true);
        }
    }

    public boolean isPassive () {
        return ((Boolean) getProperty (PROP_PASSIVE)).booleanValue ();
    }

    public void setPassive (boolean p) {
        if (isDebug ()) System.err.println("setPassive: " + p);
        boolean old = isPassive ();
        if (old != p) {
            if (p)
                Connection.startServer (getPort ());
            else
                Connection.stopServer (getPort ());
            putProperty (PROP_PASSIVE, new Boolean (p), true);
        }
    }

    public String getPassword () {
        return (String) getProperty (PROP_PASSWORD);
    }

    public void setPassword (String pw) {
        if (isDebug ()) System.err.println("setPassword: " + pw);
        putProperty (PROP_PASSWORD, pw, true);
    }

    public String[] getMimeTypes () {
        return (String[]) getProperty (PROP_MIME_TYPES);
    }

    public void setMimeTypes (String[] mt) {
        if (isDebug ()) System.err.println("setMimeTypes");
        Collection oldColl = Arrays.asList (getMimeTypes ());
        Collection newColl = Arrays.asList (mt);
        Set added = new HashSet (newColl);
        added.removeAll (oldColl);
        Set removed = new HashSet (oldColl);
        removed.removeAll (newColl);
        Iterator it = removed.iterator ();
        while (it.hasNext ()) {
            String type = (String) it.next ();
            if (isDebug ()) System.err.println("\tremoving: " + type);
            JEditorPane.registerEditorKitForContentType (type, "javax.swing.text.DefaultEditorKit");
        }
        it = added.iterator ();
        while (it.hasNext ()) {
            String type = (String) it.next ();
            if (isDebug ()) System.err.println("\tadding: " + type);
            JEditorPane.registerEditorKitForContentType (type, "org.netbeans.modules.emacs.EmacsKit", EmacsKit.class.getClassLoader ());
            EditorKit ek = JEditorPane.createEditorKitForContentType (type);
            if (ek instanceof EmacsKit) {
                // XXX this will not work; need instead to have EmacsKit keep track of its
                // "clone group" so that each time setContentType is called, that will
                // update the content type for all sisters (maybe??)--but then the problem
                // becomes that JEditorPane.setContentType will incorrectly update MIME type
                // for a whole host of kits which are not actually related. Maybe. If anyone
                // ever actually called this for such a purpose. How to resolve??
                ((EmacsKit) ek).setContentType (type);
            } else {
                System.err.println ("WARNING: Emacs editor not installed for content type " + type + ".");
                System.err.println ("Please restart the IDE. This is a JDK bug fixed in 1.3.");
            }
        }
        if (! added.isEmpty () || ! removed.isEmpty ())
            putProperty (PROP_MIME_TYPES, mt, true);
    }

    public boolean isDebug () {
        return ((Boolean) getProperty (PROP_DEBUG)).booleanValue ();
    }

    public void setDebug (boolean d) {
        System.err.println("setDebug: " + d);
        putProperty (PROP_DEBUG, new Boolean (d), true);
        Connection.DEBUG = d;
    }

}
