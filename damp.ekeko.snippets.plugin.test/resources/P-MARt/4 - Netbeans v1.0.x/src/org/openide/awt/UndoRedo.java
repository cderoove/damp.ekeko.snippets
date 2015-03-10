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

package org.openide.awt;

import javax.swing.event.*;
import javax.swing.undo.*;

/** Undo and Redo manager for top components and workspace elements.
* It allows <code>UndoAction</code> and <code>RedoAction</code> to listen to editing changes of active
* components and to changes in their ability to do undo and redo.
*
* @see org.openide.actions.UndoAction
* @see org.openide.actions.RedoAction
* @see org.openide.windows.TopComponent#getUndoRedo
* @see org.openide.windows.Workspace.Element#getUndoRedo
*
* @author Jaroslav Tulach
*/
public interface UndoRedo {

    /** Empty implementation that does not allow
    * any undo or redo actions.
    */
    public static final UndoRedo NONE = new Empty ();

    /** Test whether the component currently has edits which may be undone.
    * @return <code>true</code> if undo is allowed
    */
    public boolean canUndo ();

    /** Test whether the component currently has undone edits which may be redone.
    * @return <code>true</code> if redo is allowed
    */
    public boolean canRedo ();

    /** Undo an edit.
    * @exception CannotUndoException if it fails
    */
    public void undo () throws CannotUndoException;

    /** Redo a previously undone edit.
    * @exception CannotRedoException if it fails
    */
    public void redo () throws CannotRedoException;

    /** Add a change listener.
    * The listener will be notified every time the undo/redo
    * ability of this object changes.
    * @param l the listener to add
    */
    public void addChangeListener (ChangeListener l);

    /** Remove a change listener.
    * @param l the listener to remove
    * @see #addChangeListener
    */
    public void removeChangeListener (ChangeListener l);

    /** Get a human-presentable name describing the
    * undo operation.
    * @return the name
    */
    public String getUndoPresentationName ();

    /** Get a human-presentable name describing the
    * redo operation.
    * @return the name
    */
    public String getRedoPresentationName ();

    /** An undo manager which fires a change event each time it consumes a new undoable edit.
    */
    public static class Manager extends UndoManager implements UndoRedo {
        /** listener list */
        private EventListenerList list;

        static final long serialVersionUID =6721367974521509720L;
        /** Consume an undoable edit.
        * Delegates to superclass and notifies listeners.
        * @param ue the edit
        */
        public void undoableEditHappened (UndoableEditEvent ue) {
            super.undoableEditHappened (ue);

            if (list == null) return;

            Object[] l = list.getListenerList ();

            if (l.length == 0) return;

            ChangeEvent ev = new ChangeEvent (this);
            for (int i = l.length - 1; i >= 0; i -= 2) {
                ((ChangeListener)l[i]).stateChanged (ev);
            }
        }

        /* Attaches change listener to the this object.
        * The listener is notified everytime the undo/redo
        * ability of this object changes.
        */
        public synchronized void addChangeListener (ChangeListener l) {
            if (list == null) {
                list = new EventListenerList ();
            }
            list.add (ChangeListener.class, l);
        }

        /* Removes the listener
        */
        public void removeChangeListener (ChangeListener l) {
            if (list != null) {
                list.remove (ChangeListener.class, l);
            }
        }

        public String getUndoPresentationName() {
            return this.canUndo() ? super.getUndoPresentationName() : ""; // NOI18N
        }

        public String getRedoPresentationName() {
            return this.canRedo() ? super.getRedoPresentationName() : ""; // NOI18N
        }

    }

    // cannot be made private in an interface, f**king Java interface rules are ridiculous --jglick
    /** Empty implementation that does not support any undoable edits.
    * Use {@link UndoRedo#NONE} rather than instantiating this.
    */
    public static final class Empty extends Object implements UndoRedo {
        public boolean canUndo () {
            return false;
        }
        public boolean canRedo () {
            return false;
        }
        public void undo () throws CannotUndoException {
            throw new CannotUndoException ();
        }
        public void redo () throws CannotRedoException {
            throw new CannotRedoException ();
        }
        public void addChangeListener (ChangeListener l) {
        }
        public void removeChangeListener (ChangeListener l) {
        }
        public String getUndoPresentationName () {
            return ""; // NOI18N
        }
        public String getRedoPresentationName () {
            return ""; // NOI18N
        }
    }
}


/*
* Log
*  9    src-jtulach1.8         1/12/00  Ian Formanek    NOI18N
*  8    src-jtulach1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    src-jtulach1.6         8/18/99  Ian Formanek    Generated serial version 
*       UID
*  6    src-jtulach1.5         8/9/99   Miloslav Metelka default undo/redo 
*       presentation names
*  5    src-jtulach1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    src-jtulach1.3         5/14/99  Jesse Glick     [JavaDoc]
*  3    src-jtulach1.2         4/19/99  Jesse Glick     [JavaDoc]
*  2    src-jtulach1.1         3/11/99  Jaroslav Tulach Undo/Redo support
*  1    src-jtulach1.0         3/10/99  Jaroslav Tulach 
* $
*/
