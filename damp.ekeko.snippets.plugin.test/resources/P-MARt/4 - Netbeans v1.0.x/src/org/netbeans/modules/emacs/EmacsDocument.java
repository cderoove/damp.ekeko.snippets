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

import java.awt.Color;
import java.awt.Rectangle;
import java.beans.*;
import java.util.*;
import javax.swing.SwingUtilities;
import javax.swing.text.*;
import javax.swing.undo.*;

import org.openide.text.NbDocument;
import org.openide.loaders.DataObject;
import org.openide.util.WeakListener;
import org.openide.cookies.CloseCookie;

public class EmacsDocument extends DefaultStyledDocument implements Protocol, NbDocument.WriteLockable {

    private static final Object GUARDED = NbDocument.GUARDED;

    private static final MutableAttributeSet GUARDED_LOOK = new SimpleAttributeSet ();
    static {
        GUARDED_LOOK.addAttribute (StyleConstants.ColorConstants.Background, Color.cyan);
    }
    private static final MutableAttributeSet UNGUARDED_LOOK = new SimpleAttributeSet ();
    static {
        // XXX is it possible to simply disable the color somehow?
        UNGUARDED_LOOK.addAttribute (StyleConstants.ColorConstants.Background, Color.white);
    }

    public EmacsDocument () {
        setDocumentProperties (new HookProperties (getDocumentProperties ()));
    }

    private transient EmacsProxier proxy = null;
    private transient EmacsListener elistener = null;

    /**
     * @associates Object 
     */
    private transient Vector pendingGuards = null;

    /**
     * @associates Object 
     */
    private transient Vector pendingStyles = null;

    private transient PropertyChangeListener dobPCL = new PropertyChangeListener () {
                public void propertyChange (PropertyChangeEvent ev) {
                    if (DataObject.PROP_MODIFIED.equals (ev.getPropertyName ())) {
                        if (proxy == null) {
                            if (Connection.DEBUG) System.err.println("skipping setModified since proxy == null");
                            return;
                        }
                        proxy.call (CMD_setModified, new Object[] { ev.getNewValue () });
                    }
                }
            };

    private synchronized void addGuard (int off, int len, boolean guard) {
        if (Connection.DEBUG) System.err.println("EmacsDocument.addGuard: off=" + off + " len=" + len + " guard=" + guard);
        if (pendingGuards == null) pendingGuards = new Vector ();
        try {
            if (Connection.DEBUG) System.err.println("Guardable text: " + getText (off, len));
            Position start = NbDocument.createPosition (this, off, Position.Bias.Forward);
            Position end = NbDocument.createPosition (this, off + len, Position.Bias.Backward);
            pendingGuards.add (new Object[] { start, end, new Boolean (guard) });
        } catch (BadLocationException ble) {
            ble.printStackTrace ();
        }
    }

    private synchronized void addStyle (int off, String type) {
        if (Connection.DEBUG) System.err.println("EmacsDocument.addStyle: off=" + off + " type=" + type);
        if (pendingStyles == null) pendingStyles = new Vector ();
        try {
            if (Connection.DEBUG) {
                Element el = getParagraphElement (off);
                System.err.println("Styled text: " + getText (el.getStartOffset (), el.getEndOffset () - el.getStartOffset ()));
            }
            Position pos = NbDocument.createPosition (this, off, Position.Bias.Forward);
            pendingStyles.add (new Object[] { pos, type });
        } catch (BadLocationException ble) {
            ble.printStackTrace ();
        }
    }

    synchronized void init (final EmacsProxier proxy) {
        if (proxy == this.proxy) return;
        if (this.proxy != null) throw new IllegalArgumentException ();
        if (atomicLevel > 0) proxy.call (CMD_startAtomic);
        if (asUserLevel > 0) proxy.call (CMD_setAsUser, new Object[] { Boolean.TRUE });
        if (Connection.DEBUG) System.err.println ("Initing EmacsDocument with proxy " + proxy);
        this.proxy = proxy;
        proxy.addEmacsListener (elistener = new EmacsListener () {
                                                public void callback (EmacsEvent ev) {
                                                    handleCallback (ev);
                                                }
                                            });
        // Assume we are always listening:
        proxy.call (CMD_startDocumentListen);
        maybeSetTitle (getProperty (TitleProperty));
        Object dob = getProperty (StreamDescriptionProperty);
        if (dob != null && dob instanceof DataObject)
            proxy.call (CMD_setModified, new Object[] { new Boolean (((DataObject) dob).isModified ()) });
        render (new Runnable () {
                    public void run () {
                        int len = ((Integer) proxy.function (FUN_getLength)[0]).intValue ();
                        if (len != 0) {
                            // XXX check result
                            proxy.function (FUN_remove, new Object[] { new Integer (0), new Integer (len) });
                        }
                        len = getLength ();
                        if (len != 0) {
                            try {
                                String text = getText (0, len);
                                insertInChunks (0, text);
                            } catch (BadLocationException ble) {
                                ble.printStackTrace ();
                                markAsBogus ("Could not initialize content: " + ble);
                                return;
                            }
                            if (pendingGuards != null) {
                                Iterator it = pendingGuards.iterator ();
                                while (it.hasNext ()) {
                                    Object[] guard = (Object[]) it.next ();
                                    int start = ((Position) guard[0]).getOffset ();
                                    int end = ((Position) guard[1]).getOffset ();
                                    if (end > start) {
                                        proxy.call (((Boolean) guard[2]).booleanValue () ? CMD_guard : CMD_unguard,
                                                    new Object[] { new Integer (start), new Integer (end - start) });
                                    } else {
                                        if (Connection.DEBUG) System.err.println("ignoring empty/backwards guard: start=" + start + " end=" + end);
                                    }
                                }
                                pendingGuards = null;
                            }
                            if (pendingStyles != null) {
                                Iterator it = pendingStyles.iterator ();
                                while (it.hasNext ()) {
                                    Object[] style = (Object[]) it.next ();
                                    proxy.call (CMD_setStyle, new Object[] { new Integer (((Position) style[0]).getOffset ()),
                                                style[1] });
                                }
                                pendingStyles = null;
                            }
                        }
                    }
                });
    }

    synchronized void shutdown () {
        if (proxy == null) return;
        if (Connection.DEBUG) System.err.println ("EmacsDocument dispose");
        proxy.call (CMD_stopDocumentListen);
        proxy.removeEmacsListener (elistener);
        // don't bother turning off asUser, irrelevant
        if (atomicLevel > 0) proxy.call (CMD_endAtomic);
        pendingGuards = null;
        proxy = null;
        elistener = null;
    }

    protected void finalize () throws Exception {
        shutdown ();
    }

    protected void markAsBogus (String msg) {
        System.err.println("*** Document is bogus! ***");
        System.err.println("Reason: " + msg);
        shutdown ();
        // XXX may want to do something like:
        // init (oldProxy);
        // (this is local-is-preferred mode)
    }

    protected void handleCallback (EmacsEvent ev) {
        if (ev.isOutOfSequence ()) {
            if (Connection.DEBUG) System.err.println("Will ignore bad event: " + ev);
            markAsBogus ("Bad event: " + ev);
            return;
        }
        if (EVT_insert.equals (ev.getType ())) {
            Object[] args = ev.getArgs ();
            if (args.length != 2) throw new IllegalArgumentException (ev.toString ());
            int pos = ((Integer) args[0]).intValue ();
            String text = (String) args[1];
            if (text.equals ("")) return;
            handleInsertEvent (pos, text);
        } else if (EVT_remove.equals (ev.getType ())) {
            Object[] args = ev.getArgs ();
            if (args.length != 2) throw new IllegalArgumentException (ev.toString ());
            int pos = ((Integer) args[0]).intValue ();
            int len = ((Integer) args[1]).intValue ();
            if (len == 0) return;
            handleRemoveEvent (pos, len);
        } else if (EVT_killed.equals (ev.getType ())) {
            if (ev.getArgs ().length != 0) throw new IllegalArgumentException (ev.toString ());
            handleKilledEvent ();
        }
    }

    private transient boolean nofire = false;
    private transient final Object nofireLock = new Object () {
                public String toString () {
                    return "EmacsDocument.nofireLock";
                }
            };

    // Runs runnable without firing changes to Emacs.
    protected final void withoutFiring (Runnable run) {
        synchronized (nofireLock) {
            if (nofire) throw new IllegalStateException ();
            try {
                nofire = true;
                run.run ();
            } finally {
                nofire = false;
            }
        }
    }

    protected void handleInsertEvent (final int pos, final String text) {
        withoutFiring (new Runnable () {
                           public void run () {
                               try {
                                   insertString (pos, text, SimpleAttributeSet.EMPTY);
                               } catch (BadLocationException ble) {
                                   ble.printStackTrace ();
                                   markAsBogus (ble.toString ());
                               }
                           }
                       });
    }

    protected void handleRemoveEvent (final int pos, final int len) {
        withoutFiring (new Runnable () {
                           public void run () {
                               try {
                                   remove (pos, len);
                               } catch (BadLocationException ble) {
                                   ble.printStackTrace ();
                                   markAsBogus (ble.toString ());
                               }
                           }
                       });
    }

    protected void handleKilledEvent () {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            shutdown ();
                                            Object dob = getProperty (StreamDescriptionProperty);
                                            if (dob != null && dob instanceof DataObject) {
                                                CloseCookie cookie = (CloseCookie) ((DataObject) dob).getCookie (CloseCookie.class);
                                                if (cookie != null) cookie.close ();
                                            }
                                        }
                                    });
    }

    private transient int atomicLevel = 0;
    private transient int asUserLevel = 0;
    private transient BadLocationException asUserException = null;
    private transient Thread atomicThread = null;

    protected synchronized void writeLock2 () {
        try {
            while (atomicThread != null && atomicThread != Thread.currentThread ())
                wait ();
            if (atomicLevel++ == 0) atomicThread = Thread.currentThread ();
        } catch (InterruptedException ie) {
            ie.printStackTrace ();
        }
    }

    protected synchronized void writeUnlock2 () {
        if (--atomicLevel == 0) atomicThread = null;
        notifyAll ();
    }

    protected boolean testModification (int off) {
        Element elt = getCharacterElement (off);
        if (elt == null) return true;
        return ! isGuarded (elt.getAttributes ());
    }

    public void insertString (int off, String str, AttributeSet attr) throws BadLocationException {
        if (Connection.DEBUG) System.err.println("insertString: off=" + off + " str=" + str + " attr=" + attr);
        //if (Connection.DEBUG) Thread.dumpStack ();
        try {
            writeLock2 ();
            boolean testModif = testModification (off);
            if (asUserException != null) {
                if (Connection.DEBUG) System.err.println("already had an exception, will do nothing");
            } else if (asUserLevel > 0 && ! testModif) {
                if (Connection.DEBUG) System.err.println("Will queue up exception");
                asUserException = new BadLocationException ("Cannot insert in a guard block", off);
            } else {
                if (! testModif) {
                    if (Connection.DEBUG) System.err.println("Inheriting guard blocks...");
                    MutableAttributeSet attr2 = (attr == null) ? new SimpleAttributeSet () : new SimpleAttributeSet (attr);
                    attr2.addAttribute (GUARDED, Boolean.TRUE);
                    attr2.addAttributes (GUARDED_LOOK);
                    attr = attr2;
                }
                super.insertString (off, str, attr);
            }
        } finally {
            writeUnlock2 ();
        }
    }

    public void remove (int off, int len) throws BadLocationException {
        if (Connection.DEBUG) System.err.println("remove: off=" + off + " len=" + len);
        try {
            writeLock2 ();
            if (asUserException != null) {
                if (Connection.DEBUG) System.err.println("already had an exception, will do nothing");
            } else if (asUserLevel > 0 && ! testModification (off)) {
                if (Connection.DEBUG) System.err.println("Will queue up exception");
                asUserException = new BadLocationException ("Cannot remove from a guard block", off);
            } else {
                super.remove (off, len);
            }
        } finally {
            writeUnlock2 ();
        }
    }

    private static final int CHUNK_MAX = 4096;
    private void insertInChunks (int off, String text) throws BadLocationException {
        int len = text.length ();
        if (Connection.DEBUG) System.err.println("insertInChunks; len=" + len);
        int pos = 0;
        while (pos < len) {
            int toSend = Math.min (len - pos, CHUNK_MAX);
            if (Connection.DEBUG) System.err.println("insertInChunks: toSend=" + toSend);
            Object[] result = proxy.function (FUN_insert,
                                              new Object[] { new Integer (off + pos), text.substring (pos, pos + toSend) });
            if (result.length > 0) {
                if (result.length != 2) throw new RuntimeException ();
                throw new BadLocationException ((String) result[0], ((Integer) result[1]).intValue ());
            }
            pos += toSend;
        }
        if (pos > len) throw new RuntimeException ("should not happen");
    }

    protected void insertUpdate (DefaultDocumentEvent ev, AttributeSet attr) {
        super.insertUpdate (ev, attr);
        if (Connection.DEBUG) System.err.println("insertUpdate: " + ev.getPresentationName () + " attr=" + attr);
        synchronized (nofireLock) {
            if (! nofire) {
                int off = ev.getOffset ();
                int len = ev.getLength ();
                if (proxy != null) {
                    try {
                        String text = ev.getDocument ().getText (off, len);
                        insertInChunks (off, text);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace ();
                        // XXX could try to undo the edit??
                        markAsBogus ("Could not insert: " + ble);
                        return;
                    }
                    // XXX now these will not update char attrs for bg color...
                    // however this case probably does not happen in IDE normally,
                    // since usually text is inserted and then guarded later
                    if (isGuarded (attr)) proxy.call (CMD_guard, new Object[] { new Integer (off), new Integer (len) });
                } else {
                    if (Connection.DEBUG) System.err.println("Skipping insert due to proxy == null");
                    if (isGuarded (attr)) addGuard (off, len, true);
                }
            } else {
                if (Connection.DEBUG) System.err.println("Skipping insertUpdate due to nofire");
            }
        }
        makeEditUndoable (ev);
    }

    protected void postRemoveUpdate (DefaultDocumentEvent ev) {
        super.postRemoveUpdate (ev);
        if (Connection.DEBUG) System.err.println("postRemoveUpdate: " + ev.getPresentationName ());
        synchronized (nofireLock) {
            if (! nofire) {
                if (proxy == null) {
                    if (Connection.DEBUG) System.err.println("Skipping postRemoveUpdate since proxy==null");
                    return;
                }
                Object[] result = proxy.function (FUN_remove,
                                                  new Object[] { new Integer (ev.getOffset ()), new Integer (ev.getLength ()) });
                if (result.length != 0) {
                    if (result.length != 2) throw new RuntimeException ();
                    // XXX could try to undo the edit??
                    markAsBogus ("Could not remove: " + (String) result[0] + " at position #" + (Integer) result[1]);
                }
            } else {
                if (Connection.DEBUG) System.err.println("Skipping postRemoveUpdate due to nofire");
            }
        }
        makeEditUndoable (ev);
    }

    // XXX check if UndoRedo.NONE works as well
    private static final UndoableEdit CANNOT_UNDO = new UndoableEdit () {
                public void undo () throws CannotUndoException {
                    throw new CannotUndoException ();
                }
                public boolean canUndo () {
                    return false;
                }
                public void redo () throws CannotRedoException {
                    throw new CannotRedoException ();
                }
                public boolean canRedo () {
                    return false;
                }
                public void die () {
                }
                public boolean addEdit (UndoableEdit ue2) {
                    return false;
                }
                public boolean replaceEdit (UndoableEdit ue2) {
                    return false;
                }
                public boolean isSignificant () {
                    return true;
                }
                public String getPresentationName () {
                    return "<Cannot be undone>";
                }
                public String getUndoPresentationName () {
                    return getPresentationName ();
                }
                public String getRedoPresentationName () {
                    return getPresentationName ();
                }
            };
    protected void makeEditUndoable (UndoableEdit ue) {
        ue.addEdit (CANNOT_UNDO);
    }

    public void setCharacterAttributes (int off, int len, AttributeSet attrs, boolean repl) {
        super.setCharacterAttributes (off, len, attrs, repl);
        if (Connection.DEBUG) System.err.println("setCharAttrs: off=" + off + " len=" + len + " attrs=" + attrs + " repl=" + repl);
        updateAttrs (off, len, attrs, repl);
    }

    public void setParagraphAttributes (int off, int len, AttributeSet attrs, boolean repl) {
        super.setParagraphAttributes (off, len, attrs, repl);
        if (Connection.DEBUG) System.err.println("setParaAttrs: off=" + off + " len=" + len + " attrs=" + attrs + " repl=" + repl);
        updateAttrs (off, len, attrs, repl);
    }

    protected void updateAttrs (final int off, final int len, AttributeSet attrs, boolean repl) {
        if (isGuarded (attrs)) {
            if (proxy == null)
                addGuard (off, len, true);
            else
                proxy.call (CMD_guard, new Object[] { new Integer (off), new Integer (len) });
            super.setCharacterAttributes (off, len, GUARDED_LOOK, false);
        } else if (repl || isUnguarded (attrs)) {
            // XXX with repl=true, will sometimes gratuitously unguard, but oh well...
            if (proxy == null)
                addGuard (off, len, false);
            else
                proxy.call (CMD_unguard, new Object[] { new Integer (off), new Integer (len) });
            super.setCharacterAttributes (off, len, UNGUARDED_LOOK, false);
        }
    }

    private static boolean isGuarded (AttributeSet attrs) {
        return attrs != null && Boolean.TRUE.equals (attrs.getAttribute (GUARDED));
    }

    private static boolean isUnguarded (AttributeSet attrs) {
        return attrs != null && Boolean.FALSE.equals (attrs.getAttribute (GUARDED));
    }

    public void setLogicalStyle (final int pos, final Style s) {
        if (Connection.DEBUG) System.err.println("setLogicalStyle: pos=" + pos + " s=" + s.getName ());
        // XXX works around DocumentLine bug whereby this is called within a notification
        if (getCurrentWriter () == null)
            super.setLogicalStyle (pos, s);
        else
            SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            if (Connection.DEBUG) System.err.println("setLogicalStyle II");
                                            setLogicalStyle0 (pos, s);
                                        }
                                    });
        String name = (s == null) ? null : s.getName ();
        String toAnnounce = null;
        if (name == null || NbDocument.NORMAL_STYLE_NAME.equals (name))
            toAnnounce = STYLE_NORMAL;
        else if (NbDocument.BREAKPOINT_STYLE_NAME.equals (name))
            toAnnounce = STYLE_BREAKPOINT;
        else if (NbDocument.CURRENT_STYLE_NAME.equals (name))
            toAnnounce = STYLE_CURRENT;
        else if (NbDocument.ERROR_STYLE_NAME.equals (name))
            toAnnounce = STYLE_ERROR;
        else
            toAnnounce = STYLE_NORMAL;
        if (proxy != null)
            proxy.call (CMD_setStyle, new Object[] { new Integer (pos), toAnnounce });
        else
            addStyle (pos, toAnnounce);
    }
    private void setLogicalStyle0 (int pos, Style s) {
        super.setLogicalStyle (pos, s);
    }

    public void runAtomicAsUser (Runnable r) throws BadLocationException {
        if (Connection.DEBUG) System.err.println("runAtomicAsUser (start)");
        try {
            writeLock2 ();
            if (atomicLevel == 1 && proxy != null) proxy.call (CMD_startAtomic);
            if (asUserLevel++ == 0 && proxy != null) proxy.call (CMD_setAsUser, new Object[] { Boolean.TRUE });
            r.run ();
            if (asUserLevel == 1 && asUserException != null) {
                if (Connection.DEBUG) System.err.println("Will actually throw: " + asUserException);
                BadLocationException temp = asUserException;
                asUserException = null;
                throw temp;
            }
        } finally {
            if (--asUserLevel == 0 && proxy != null) proxy.call (CMD_setAsUser, new Object[] { Boolean.FALSE });
            if (atomicLevel == 1 && proxy != null) proxy.call (CMD_endAtomic);
            writeUnlock2 ();
            if (Connection.DEBUG) System.err.println("runAtomicAsUser (end)");
        }
    }

    public void runAtomic (Runnable r) {
        if (Connection.DEBUG) System.err.println("runAtomic (start)");
        try {
            writeLock2 ();
            if (atomicLevel == 1 && proxy != null) proxy.call (CMD_startAtomic);
            r.run ();
        } finally {
            if (atomicLevel == 1 && proxy != null) proxy.call (CMD_endAtomic);
            writeUnlock2 ();
            if (Connection.DEBUG) System.err.println("runAtomic (end)");
        }
    }

    private void maybeSetTitle (Object value) {
        if (value instanceof String && value != null && proxy != null) {
            // XXX better not to use full package name for a title, IMHO
            String sval = (String) value;
            int slash = sval.lastIndexOf ('/');
            if (slash != -1 && slash != sval.length () - 1)
                sval = sval.substring (slash + 1);
            proxy.call (CMD_setTitle, new Object[] { sval });
        }
    }

    private class HookProperties extends Hashtable {

        public HookProperties (Dictionary dict) {
            Enumeration keys = dict.keys ();
            while (keys.hasMoreElements ()) {
                Object key = keys.nextElement ();
                put (key, dict.get (key));
            }
        }

        public Object put (Object key, Object value) {
            if (value != null) {
                if (value.equals (super.get (key))) {
                    if (Connection.DEBUG) System.err.println("Ignoring unchanged doc prop: " + key + "=" + value);
                    return value;
                }
                if (key.equals (TitleProperty)) {
                    maybeSetTitle (value);
                } else if (key.equals (PROP_locAndSize) && value instanceof Rectangle) {
                    Rectangle r = (Rectangle) value;
                    if (proxy != null)
                        proxy.call (CMD_setLocAndSize, new Object[] { new Integer (r.x), new Integer (r.y),
                                    new Integer (r.width), new Integer (r.height) });
                } else if (key.equals (StreamDescriptionProperty) && value instanceof DataObject) {
                    DataObject dob = (DataObject) value;
                    if (proxy != null) proxy.call (CMD_setModified, new Object[] { new Boolean (dob.isModified ()) });
                    dob.addPropertyChangeListener (WeakListener.propertyChange (dobPCL, dob));
                }
            }
            if (Connection.DEBUG) System.err.println("Setting doc prop: " + key + "=" + value);
            return super.put (key, value);
        }

        public Object get (Object key) {
            Object res = super.get (key);
            //if (Connection.DEBUG) System.err.println("Getting doc prop: " + key + "=" + res);
            return res;
        }

    }

}
