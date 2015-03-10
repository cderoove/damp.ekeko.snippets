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

package org.netbeans.examples.modules.microed;

import org.openide.TopManager;
import java.beans.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

/** The editor kit used for the micro-editor.
 * Mostly just delegates to the underlying useful editor kit, e.g. DefaultEditorKit.
 * @author Jesse Glick
 * @version Date
 */
public class Kit extends EditorKit {
    transient private EditorKit delegate;

    /**
     * @associates JEditorPane 
     */
    transient private Set panes;            // Set<JEditorPane>
    transient PropertyChangeListener listener;

    static final long serialVersionUID =2338293611867877101L;
    public Kit () {
        init ();
    }
    // Serialization support: probably never be used but the transient
    // things should be set correctly just in case:
    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject ();
        init ();
    }
    // Actually set things up.
    private void init () {
        if (Settings.debug) System.err.println ("Creating new custom kit");
        updateDelegate ();
        panes = new HashSet ();
        // Set the font correctly on already-opened windows, so the user
        // can see customizations live. Also make sure that our delegate
        // is kept up-to-date.
        listener = new PropertyChangeListener () {
                       public void propertyChange (PropertyChangeEvent ev) {
                           if (ev.getPropertyName ().equals (Settings.PROP_kitClass))
                               updateDelegate ();
                           else if (ev.getPropertyName ().equals (Settings.PROP_font)) {
                               if (panes != null) {
                                   Iterator it = panes.iterator ();
                                   while (it.hasNext ()) {
                                       JEditorPane pane = (JEditorPane) it.next ();
                                       pane.setFont (Settings.font);
                                       pane.repaint ();
                                   }
                               }
                           }
                       }
                   };
        Settings.DEFAULT.addPropertyChangeListener (listener);
    }
    protected void finalize () throws Exception {
        if (Settings.debug) System.err.println ("Destroying custom kit");
        Settings.DEFAULT.removePropertyChangeListener (listener);
    }
    // Get the delegated-to editor kit. Specified by classname, not
    // prototype; specifying by prototype would be possible but a little
    // harder, and would make it difficult to make a good property
    // editor for.
    private void updateDelegate () {
        try {
            delegate = (EditorKit) Beans.instantiate (TopManager.getDefault ().systemClassLoader (), Settings.kitClass);
            if (Settings.debug) System.err.println ("The delegate kit: " + delegate);
        } catch (Exception e) {
            TopManager.getDefault ().notifyException (e);
        }
    }

    // Here's where the font is set on the editor pane, and we also keep
    // track of the panes in use for future font updates.
    public void install (JEditorPane pane) {
        if (Settings.debug) System.err.println ("Installing kit into pane");
        delegate.install (pane);
        panes.add (pane);
        pane.setFont (Settings.font);
    }
    public void deinstall (JEditorPane pane) {
        if (Settings.debug) System.err.println ("Deinstalling kit from pane");
        panes.remove (pane);
        delegate.deinstall (pane);
    }

    // Editor kits are routinely cloned by the JEditorPane--this must be
    // implemented.
    public Object clone () {
        if (Settings.debug) System.err.println ("Cloning kit");
        return new Kit ();
    }

    // DefaultEditorKit does not provide a view factory. The private
    // class JEditorPane.PlainEditorKit does the exact same thing.
    public ViewFactory getViewFactory () {
        ViewFactory ret = delegate.getViewFactory ();
        if (ret != null) {
            if (Settings.debug) System.err.println ("Delegating getViewFactory: " + ret);
            return ret;
        } else {
            if (Settings.debug) System.err.println ("Providing own view factory");
            return new ViewFactory () {
                       public View create (Element elem) {
                           return new WrappedPlainView (elem);
                       }
                   };
        }
    }

    // Delegated methods:
    public String getContentType () {
        String ret = delegate.getContentType ();
        if (Settings.debug) System.err.println ("Delegating getContentType: " + ret);
        return ret;
    }
    public Action[] getActions () {
        if (Settings.debug) System.err.println ("Delegating getActions");
        return delegate.getActions ();
    }
    public Caret createCaret () {
        if (Settings.debug) System.err.println ("Delegating createCaret");
        return delegate.createCaret ();
    }
    public Document createDefaultDocument () {
        Document ret = delegate.createDefaultDocument ();
        if (Settings.debug) System.err.println ("Delegating createDefaultDocument: " + ret);
        return ret;
    }
    public void read (InputStream is, Document doc, int pos) throws IOException, BadLocationException {
        if (Settings.debug) System.err.println ("Delegating read from InputStream");
        delegate.read (is, doc, pos);
    }
    public void read (Reader rd, Document doc, int pos) throws IOException, BadLocationException {
        if (Settings.debug) System.err.println ("Delegating read from Reader");
        delegate.read (rd, doc, pos);
    }
    public void write (OutputStream os, Document doc, int pos, int length) throws IOException, BadLocationException {
        if (Settings.debug) System.err.println ("Delegating write to OutputStream");
        delegate.write (os, doc, pos, length);
    }
    public void write (Writer wr, Document doc, int pos, int length) throws IOException, BadLocationException {
        if (Settings.debug) System.err.println ("Delegating write to Writer");
        delegate.write (wr, doc, pos, length);
    }
}
