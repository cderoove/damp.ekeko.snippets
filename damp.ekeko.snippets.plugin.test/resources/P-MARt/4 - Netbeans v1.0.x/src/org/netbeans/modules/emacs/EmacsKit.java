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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

// XXX
import org.openide.util.Utilities;
import org.openide.TopManager;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;

public class EmacsKit extends StyledEditorKit implements Protocol {

    // private static final int bufsize = 4096;

    private transient EmacsProxier proxy = null;
    private transient boolean hosedProxy = false;
    private transient EmacsDocument theDoc = null;
    private transient EmacsCaret theCaret = null;

    private String contentType = null;

    /**
     * @associates Document 
     */
    private transient final Map panes = new HashMap (); // Map<JEditorPane, Document>


    /**
     * @associates PropertyChangeListener 
     */
    private transient final Map paneLists = new HashMap (); // Map<JEditorPane, PropertyChangeListener>


    /**
     * @associates ComponentListener 
     */
    private transient final Map paneCompLists = new HashMap (); // Map<JEditorPane, ComponentListener>

    private transient final EmacsListener keyListener = new EmacsListener () {
                public void callback (EmacsEvent ev) {
                    if (EVT_keyCommand.equals (ev.getType ())) {
                        Object[] args = ev.getArgs ();
                        if (args.length != 1) throw new RuntimeException ();
                        String key = (String) args[0];
                        if (Connection.DEBUG) System.err.println("keyboard callback: " + key);
                        KeyStroke ks = Utilities.stringToKey (key);
                        if (Connection.DEBUG) System.err.println("Keystroke: " + ks);
                        if (ks == null) {
                            if (Connection.DEBUG) System.err.println("Warning: bogus key");
                            return;
                        }
                        Action action = TopManager.getDefault ().getGlobalKeymap ().getAction (ks);
                        // XXX many X configs send Meta from the Alt key, so this is translated
                        // here since the IDE conventionally uses Alt keybindings
                        if (action == null && (ks.getModifiers () & InputEvent.META_MASK) != 0) {
                            if (Connection.DEBUG) System.err.println("Meta-bound key not found, trying Alt instead");
                            ks = KeyStroke.getKeyStroke (ks.getKeyCode (), ks.getModifiers () & ~ InputEvent.META_MASK | InputEvent.ALT_MASK);
                            action = TopManager.getDefault ().getGlobalKeymap ().getAction (ks);
                        }
                        if (Connection.DEBUG) System.err.println("Keystroke: " + ks);
                        if (action == null) {
                            if (Connection.DEBUG) System.err.println("Warning: key not defined");
                            return;
                        }
                        // XXX This is not great, either: e.g. Compile will *not* be enabled
                        // often because the node selection is wrong. No public way to test whether
                        // it should be enabled, since enable(Node[]) is protected. Could use
                        // Java reflection I suppose.
                        /*
                        if (! action.isEnabled ()) {
                          if (Connection.DEBUG) System.err.println ("Will not call action because it is disabled: " + action.getValue (Action.NAME));
                          return;
                    }
                        */
                        if (! (action instanceof NodeAction)) {
                            if (Connection.DEBUG) System.err.println("Will call as a simple action: " + action.getValue (Action.NAME));
                            action.actionPerformed (new ActionEvent (EmacsKit.this, ActionEvent.ACTION_PERFORMED, key));
                        } else {
                            if (Connection.DEBUG) System.err.println("Will call as a NodeAction: " + action.getValue (Action.NAME));
                            Set s = panes.keySet ();
                            if (s.isEmpty ()) {
                                if (Connection.DEBUG) System.err.println("no JEditorPane's to handle keystroke");
                                return;
                            }
                            if (s.size () > 1) {
                                if (Connection.DEBUG) System.err.println(">1 JEditorPane's to handle keystroke, ambiguous");
                                return;
                            }
                            JEditorPane pane = (JEditorPane) s.iterator ().next ();
                            TopComponent tc = null;
                            for (Component c = pane; c != null; c = c.getParent ()) {
                                if (c instanceof TopComponent) {
                                    tc = (TopComponent) c;
                                    break;
                                }
                            }
                            if (tc == null) {
                                // XXX could also check for nodeDelegate from ED's DataObject, if any,
                                // but this should never be necessary
                                if (Connection.DEBUG) System.err.println("Warning: JEditorPane was not within a TC, will not handle keystroke");
                                return;
                            }
                            Node[] nodes = tc.getActivatedNodes ();
                            if (nodes.length != 1) {
                                // XXX problem: apparently unselected JavaEditor components
                                // often have no node selection at all!
                                if (Connection.DEBUG) System.err.println("Warning: TC had 0 or >1 activated nodes, will ignore keystroke");
                                return;
                            }
                            action.actionPerformed (new ActionEvent (nodes[0], ActionEvent.ACTION_PERFORMED, key));
                        }
                    }
                }
            };

    public EmacsKit () {
    }

    public synchronized String toString () {
        if (proxy == null)
            return "EmacsKit[noproxy]";
        else
            return "EmacsKit[" + proxy + "]";
    }

    public synchronized Object clone () {
        if (Connection.DEBUG) {
            try {
                System.err.println("cloning to EmacsKit (possibly from Repository)...");
                Class clazz = org.openide.TopManager.getDefault ().currentClassLoader ().loadClass ("org.netbeans.modules.emacs.EmacsKit");
                Object o = clazz.newInstance ();
                if (contentType != null) {
                    java.lang.reflect.Method m = clazz.getMethod ("setContentType", new Class[] { String.class });
                    m.invoke (o, new Object[] { contentType });
                }
                return o;
            } catch (Exception e) {
                e.printStackTrace ();
                return null;
            }
        } else {
            EmacsKit nue = new EmacsKit ();
            nue.setContentType (getContentType ());
            return nue;
        }
    }

    synchronized EmacsProxier getProxy () {
        if (Connection.DEBUG) System.err.println("maybe getting proxy");
        if (proxy == null && panes.size () > 0 && ! hosedProxy) {
            if (Connection.DEBUG) System.err.println("creating new proxy");
            try {
                EmacsSettings def = EmacsSettings.DEFAULT;
                proxy = new EmacsProxier (def.getHost (),
                                          def.isPassive () ? 0 : def.getPort (),
                                          def.getPassword ());
            } catch (IOException e) {
                e.printStackTrace ();
                hosedProxy = true;
                return null;
            }
            if (contentType != null) {
                proxy.call (CMD_setContentType, new Object[] { contentType });
            }
            getDoc ().init (proxy);
            proxy.addEmacsListener (keyListener);
        }
        return proxy;
    }
    synchronized EmacsDocument getDoc () {
        if (theDoc == null) theDoc = new EmacsDocument ();
        return theDoc;
    }
    synchronized EmacsCaret getCaret () {
        if (theCaret == null) theCaret = new EmacsCaret ();
        return theCaret;
    }

    public synchronized String getContentType () {
        return contentType;
    }
    public synchronized void setContentType (String ct) {
        contentType = ct;
        if (proxy != null) {
            proxy.call (CMD_setContentType, new Object[] { ct });
        }
    }

    public synchronized void install (final JEditorPane pane) {
        if (Connection.DEBUG) System.err.println("EmacsKit installed");
        super.install (pane);
        Document doc = pane.getDocument ();
        panes.put (pane, doc);
        if (doc != null && doc instanceof EmacsDocument) {
            if (Connection.DEBUG) System.err.println("Initting doc");
            ((EmacsDocument) doc).init (getProxy ());
            notifyPositioning (pane, doc);
        }
        PropertyChangeListener pclist = new PropertyChangeListener () {
                                            public void propertyChange (PropertyChangeEvent ev) {
                                                if ("document".equals (ev.getPropertyName ())) {
                                                    Document doc1 = (Document) panes.get (pane);
                                                    if (doc1 != null && doc1 instanceof EmacsDocument) {
                                                        ((EmacsDocument) doc1).shutdown ();
                                                    }
                                                    Document doc2 = pane.getDocument ();
                                                    panes.put (pane, doc2);
                                                    if (doc2 != null && doc2 instanceof EmacsDocument) {
                                                        ((EmacsDocument) doc2).init (getProxy ());
                                                        notifyPositioning (pane, doc2);
                                                    }
                                                }
                                            }
                                        };
        paneLists.put (pane, pclist);
        pane.addPropertyChangeListener (pclist);
        ComponentListener complist = new ComponentListener () {
                                         public void componentHidden (ComponentEvent ev) { update (); }
                                         public void componentShown (ComponentEvent ev) { update (); }
                                         public void componentResized (ComponentEvent ev) { update (); }
                                         public void componentMoved (ComponentEvent ev) { update (); }
                                         private void update () {
                                             Document doc3 = pane.getDocument ();
                                             if (doc3 != null) {
                                                 notifyPositioning (pane, doc3);
                                             }
                                         }
                                     };
        paneCompLists.put (pane, complist);
        pane.addComponentListener (complist);
        if (! (pane.getCaret () instanceof EmacsCaret))
            pane.setCaret (getCaret ());
        pane.setKeymap (new AsUserKeymap (pane.getKeymap ()));
    }
    private static void notifyPositioning (JEditorPane pane, Document doc) {
        if (pane.isShowing ()) {
            // Look to see if it is being scrolled, in which case reported position
            // and size are in fact the presumed values, not what is actually seen:
            Component displayed = pane;
            for (Component walk = pane; walk != null; walk = walk.getParent ())
                if (walk instanceof JScrollPane)
                    displayed = walk;
            Point p = displayed.getLocationOnScreen ();
            Dimension d = displayed.getSize ();
            if (p.x < 0 || p.y < 0 || d.height < 0 || d.width < 0) {
                if (Connection.DEBUG) System.err.println ("WARNING: Bad dimensions: " + p + " " + d);
            } else {
                doc.putProperty (PROP_locAndSize, new Rectangle (p, d));
            }
        } else {
            doc.putProperty (PROP_locAndSize, new Rectangle (0, 0, 0, 0));
        }
    }
    public synchronized void deinstall (JEditorPane pane) {
        if (Connection.DEBUG) System.err.println("EmacsKit deinstalled");
        Document doc = pane.getDocument ();
        if (doc != null && doc instanceof EmacsDocument) {
            if (Connection.DEBUG) System.err.println("Switching off doc");
            ((EmacsDocument) doc).shutdown ();
        }
        PropertyChangeListener pclist = (PropertyChangeListener) paneLists.get (pane);
        if (pclist != null) {
            paneLists.remove (pane);
            pane.removePropertyChangeListener (pclist);
        }
        ComponentListener complist = (ComponentListener) paneCompLists.get (pane);
        if (complist != null) {
            paneCompLists.remove (pane);
            pane.removeComponentListener (complist);
        }
        panes.remove (pane);
        if (panes.size () == 0) {
            if (proxy != null) {
                if (Connection.DEBUG) System.err.println("Closing down proxy due to uninstall from last pane");
                // getDoc ().shutdown ();
                proxy.close ();
                proxy = null;
            }
        }
        super.deinstall (pane);
    }

    public synchronized Caret createCaret () {
        if (Connection.DEBUG) System.err.println("EmacsKit: creating caret");
        return getCaret ();
    }

    public Document createDefaultDocument () {
        if (Connection.DEBUG) System.err.println("EmacsKit: getting the doc");
        return getDoc ();
    }

    // ACTION-WRAPPING STUFF

    public Action[] getActions () {
        if (Connection.DEBUG) System.err.println("EmacsKit.getActions");
        return adjustActions (super.getActions ());
    }

    private Action[] lastOriginalActions = null;
    private Action[] lastAdjustedActions = null;
    private synchronized Action[] adjustActions (Action[] original) {
        if (! Utilities.compareObjects (original, lastOriginalActions) &&
                ! Utilities.compareObjects (original, lastAdjustedActions)) {
            lastOriginalActions = original;
            lastAdjustedActions = new Action[original.length];
            for (int i = 0; i < original.length; i++)
                lastAdjustedActions[i] = makeAUA (original[i]);
        }
        return lastAdjustedActions;
    }
    private class AsUserAction implements Action {
        final Action orig;
        AsUserAction (Action orig) {
            this.orig = orig;
        }
        public void actionPerformed (final ActionEvent ev) {
            if (Connection.DEBUG) System.err.println("EmacsKit.AsUserAction.actionPerformed on " + orig.getValue (NAME));
            try {
                // XXX rather than getDoc(), would be better to use e.g.:
                // 1. ev.getSource()
                // 2. try cast to JEditorPane
                // 3. getDocument()
                // 4. try cast to EmacsDocument (or NbDocument.WriteLockable)
                getDoc ().runAtomicAsUser (new Runnable () {
                                               public void run () {
                                                   orig.actionPerformed (ev);
                                               }
                                           });
            } catch (BadLocationException ble) {
                Toolkit.getDefaultToolkit ().beep ();
            }
        }
        public void addPropertyChangeListener (PropertyChangeListener pcl) {
            orig.addPropertyChangeListener (pcl);
        }
        public void removePropertyChangeListener (PropertyChangeListener pcl) {
            orig.removePropertyChangeListener (pcl);
        }
        public Object getValue (String key) {
            return orig.getValue (key);
        }
        public void putValue (String key, Object value) {
            orig.putValue (key, value);
        }
        public boolean isEnabled () {
            return orig.isEnabled ();
        }
        public void setEnabled (boolean b) {
            orig.setEnabled (b);
        }
    }


    /**
     * @associates Action 
     */
    private final Map asUserActionCache = new WeakHashMap ();
    private Action makeAUA (Action a) {
        if (a == null) return null;
        Action res = (Action) asUserActionCache.get (a);
        if (res == null) {
            res = new AsUserAction (a);
            asUserActionCache.put (a, res);
        }
        return res;
    }
    private class AsUserKeymap implements Keymap {
        private Keymap orig;
        AsUserKeymap (Keymap orig) {
            this.orig = orig;
        }
        public String getName () {
            return orig.getName ();
        }
        public Action getDefaultAction () {
            return makeAUA (orig.getDefaultAction ());
        }
        public void setDefaultAction (Action a) {
            orig.setDefaultAction (a);
        }
        public Action getAction (KeyStroke key) {
            return makeAUA (orig.getAction (key));
        }
        public KeyStroke[] getBoundKeyStrokes () {
            return orig.getBoundKeyStrokes ();
        }
        public Action[] getBoundActions () {
            return adjustActions (orig.getBoundActions ());
        }
        public KeyStroke[] getKeyStrokesForAction (Action a) {
            if (a instanceof AsUserAction) a = ((AsUserAction) a).orig;
            return orig.getKeyStrokesForAction (a);
        }
        public boolean isLocallyDefined (KeyStroke key) {
            return orig.isLocallyDefined (key);
        }
        public void addActionForKeyStroke (KeyStroke key, Action a) {
            orig.addActionForKeyStroke (key, a);
        }
        public void removeKeyStrokeBinding (KeyStroke keys) {
            orig.removeKeyStrokeBinding (keys);
        }
        public void removeBindings () {
            orig.removeBindings ();
        }
        public Keymap getResolveParent () {
            return new AsUserKeymap (orig.getResolveParent ());
        }
        public void setResolveParent (Keymap parent) {
            if (parent instanceof AsUserKeymap) parent = ((AsUserKeymap) parent).orig;
            orig.setResolveParent (parent);
        }
    }

}
