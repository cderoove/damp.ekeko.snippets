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

package org.openide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.text.MessageFormat;
import javax.swing.event.*;
import javax.swing.undo.*;
import javax.swing.SwingUtilities;

import org.openide.TopManager;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.Workspace;
import org.openide.windows.TopComponent;

/** Undo an edit.
*
* @see UndoRedo
* @author   Ian Formanek, Jaroslav Tulach
*/
public class UndoAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2762036372802427913L;

    /** initialized listener */
    private static Listener listener;

    /** last edit */
    private static UndoRedo last = UndoRedo.NONE;

    /* Construct new undo action */
    public UndoAction() {
        initializeUndoRedo ();
    }

    /** Initializes the object.
    */
    static void initializeUndoRedo () {
        if (listener != null) return;

        listener = new Listener ();
        TopComponent.getRegistry ().addPropertyChangeListener (
            WeakListener.propertyChange (listener, TopComponent.getRegistry ())
        );
        last = getUndoRedo ();
        last.addChangeListener (listener);

        updateStatus ();
    }

    /** Update status of action.
    */
    static synchronized void updateStatus() {
        SwingUtilities.invokeLater(new Runnable() {
                                       public void run() {
                                           UndoAction undo = (UndoAction)findObject (UndoAction.class, false);
                                           RedoAction redo = (RedoAction)findObject (RedoAction.class, false);

                                           UndoRedo ur = getUndoRedo ();
                                           if (undo != null) undo.setEnabled (ur.canUndo ());
                                           if (redo != null) redo.setEnabled (ur.canRedo ());
                                       }
                                   });
    }

    /** Finds current undo/redo.
    */
    static UndoRedo getUndoRedo (){
        TopComponent el = TopComponent.getRegistry ().getActivated ();
        return el == null ? UndoRedo.NONE : el.getUndoRedo ();
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        MessageFormat mformat =  new MessageFormat(ActionConstants.BUNDLE.getString("Undo"));
        return mformat.format(new String[] {getUndoRedo ().getUndoPresentationName()});
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (UndoAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/undo.gif"; // NOI18N
    }

    /* Perform action. Try to do undo operation.
    */
    public void performAction() {
        try {
            getUndoRedo ().undo ();
        } catch (CannotUndoException ex) {
            TopManager.getDefault ().notifyException (ex);
        }
        updateStatus();
    }

    /** Listener on changes of selected workspace element and
    * its changes.
    */
    private static final class Listener extends Object
        implements PropertyChangeListener, ChangeListener {
        public void propertyChange (PropertyChangeEvent ev) {
            updateStatus ();
            last.removeChangeListener (this);
            last = getUndoRedo ();
            last.addChangeListener (this);
        }

        public void stateChanged (ChangeEvent ev) {
            updateStatus ();
        }
    }

}


/*
 * Log
 *  17   Gandalf   1.16        1/12/00  Ian Formanek    NOI18N
 *  16   Gandalf   1.15        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  15   Gandalf   1.14        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   Gandalf   1.13        7/11/99  David Simonek   window system change...
 *  13   Gandalf   1.12        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  12   Gandalf   1.11        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  11   Gandalf   1.10        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  10   Gandalf   1.9         5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  9    Gandalf   1.8         5/17/99  Petr Hamernik   deadlock prevention
 *  8    Gandalf   1.7         5/2/99   Ian Formanek    Fixed last change
 *  7    Gandalf   1.6         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  6    Gandalf   1.5         3/26/99  Jesse Glick     [JavaDoc]
 *  5    Gandalf   1.4         3/19/99  Jaroslav Tulach 
 *  4    Gandalf   1.3         3/10/99  Jaroslav Tulach UndoRedo object
 *  3    Gandalf   1.2         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
