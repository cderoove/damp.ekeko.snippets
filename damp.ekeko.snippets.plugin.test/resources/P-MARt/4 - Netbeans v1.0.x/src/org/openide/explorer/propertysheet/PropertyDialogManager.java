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

package org.openide.explorer.propertysheet;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.*;
import java.util.Vector;
import java.beans.*;
import javax.swing.SwingUtilities;
import javax.swing.JButton;

import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

/** Helper dialog box manager for showing custom property editors
* and indexed properties.
*
* @author Jan Jancura, Dafe Simonek
*/
public final class PropertyDialogManager {

    /** Listener to editor property changes. */
    private PropertyChangeListener          listener;
    /** Cache for reverting on cancel. */
    private Object []                       oldValues;
    /** Custom property editor. */
    private PropertyEditor                  editor;
    /** Property displayer. */
    private PropertyDisplayer               displayer;
    /** Synchronization lock. */
    private Object                          lock;
    /** Is property indexed? */
    private boolean                         indexed = false;
    /** Set true when property is changed. */
    private boolean                         changed = false;
    /** Given component stored for test on Enhance property ed. */
    private Component                       component;
    /** Dialog instance. */
    private Dialog                          dialog;


    // init ......................................................................

    /** Create a dialog with the specified content, without
    * any displayer, editor, or lock.
    *
    * @param title title of the dialog
    * @param component component to show
    * @param isModal <code>true</code> if the dialog should be modal
    */
    public PropertyDialogManager (
        final String title,
        final Component component,
        final boolean isModal
    ) {
        this (title, component, isModal, null, null, null);
    }

    /** Create a dialog.
    *
    * @param title title of the dialog
    * @param component component to show
    * @param isModal <code>true</code> if the dialog should be modal
    * @param displayer property displayer. If the editor is not <code>null</code>, then the
    *   displayer must be non-<code>null</code> as well.
    * @param editor custom property editor. May be <code>null</code>.
    * @param lock synchronization lock. May be <code>null</code>.
    */
    public PropertyDialogManager (
        final String title,
        final Component component,
        final boolean isModal,
        final PropertyDisplayer displayer,
        final PropertyEditor editor,
        final Object lock
    ) {
        this.displayer = displayer;
        this.editor = editor;
        this.component = component;
        this.lock = lock;
        if (lock == null) this.lock = new Object ();
        // create dialog instance and initialize listeners
        dialog = createDialog (title, component, isModal);
        initializeListeners ();
    }


    // public methods ............................................................

    /** Get the created dialog instance.
    * @return the dialog instance managed by this class.
    */
    public Dialog getDialog () {
        return dialog;
    }


    // other methods ............................................................

    /** Creates proper DialogDescriptor and obtain dialog instance
    * via TopManager.createDialog() call.
    */
    private Dialog createDialog (
        final String title,
        final Component component,
        final boolean isModal
    ) {
        // prepare our options (buttons)
        Object defaultOption;
        Object[] options;
        if (editor == null) {
            options = new Object[] {
                          PropertySheet.getString ("CTL_Close")
                      };
            defaultOption = options[0];
        } else {
            boolean defaultValue = displayer.supportsDefaultValue ();
            if (editor instanceof IndexedPropertyEditor) {
                options = defaultValue ?
                          new Object[] {
                              PropertySheet.getString ("CTL_Default"),
                              PropertySheet.getString ("CTL_Close")
                          }
                          : new Object[] {
                              PropertySheet.getString ("CTL_Close")
                          };
                defaultOption = options [0];
            } else {
                options = defaultValue ?
                          new Object[] {
                              PropertySheet.getString ("CTL_Default"),
                              PropertySheet.getString ("CTL_OK"),
                              PropertySheet.getString ("CTL_Cancel")
                          }
                          : new Object[] {
                              PropertySheet.getString ("CTL_OK"),
                              PropertySheet.getString ("CTL_Cancel")
                          };
                defaultOption = options[0];
            }
        }
        // create dialog descriptor, create & return the dialog
        DialogDescriptor descriptor =
            new DialogDescriptor(
                component, title, isModal, options, defaultOption,
                DialogDescriptor.DEFAULT_ALIGN, null,
                new ActionListener () {
                    public void actionPerformed (ActionEvent evt) {
                        doButtonPressed (evt);
                    }
                });
        return TopManager.getDefault ().createDialog (descriptor);
    }

    /** Initializes dialog listeners. Must be called after
    * createDialog method call. (dialog variable must not be null)
    */
    private void initializeListeners () {
        // dialog closing reactions
        dialog.addWindowListener (new WindowAdapter () {
                                      /** Ensure that values are reverted when user cancelles dialog
                                      * by clicking on x image */
                                      public void windowClosing (WindowEvent e) {
                                          if (editor != null) cancelValue (); // not if property viewer only
                                          // ensure that resources are released
                                          dialog.setVisible (false);
                                          dialog.dispose ();
                                      }
                                      /** Remove property listener on window close */
                                      public void windowClosed (WindowEvent e) {
                                          if (listener != null) editor.removePropertyChangeListener (listener);
                                      }
                                  });
        // reactions to editor property changes
        indexed = editor instanceof IndexedPropertyEditor;
        if (editor != null) {
            if (!indexed)
                try {
                    oldValues = displayer.getPropertyDetails ().getPropertyValues ();
                } catch (Exception e) {
                    // Ignored, there can be number of exceptions
                    // when asking for old values...
                }
            editor.addPropertyChangeListener (listener =
                                                  new PropertyChangeListener () {
                                                      /** Notify displayer about property change in editor */
                                                      public void propertyChange (PropertyChangeEvent e) {
                                                          Object o = null;
                                                          if (!indexed) o = PropertyDialogManager.this.editor.getValue ();
                                                          synchronized (lock) {
                                                              if (!indexed) {
                                                                  changed = true;
                                                                  PropertyDialogManager.this.displayer.setPropertyValue (o);
                                                              }
                                                              PropertyDialogManager.this.displayer.notifyPropertyChange (e);
                                                          }
                                                      }
                                                  }
                                             );
        }
    }

    /**
    * Reverts to old values. 
    */
    private void cancelValue () {
        if ( (!changed) ||
                (component instanceof EnhancedCustomPropertyEditor)
           ) return;
        synchronized (lock) {
            if ((!indexed) && (oldValues != null)) {
                try {
                    if ( displayer.getPropertyDetails ().getPropertyValues () !=
                            oldValues
                       ) displayer.getPropertyDetails ().setPropertyValues (oldValues);
                } catch (Exception e) {
                    // Ignored, there can be number of exceptions
                    // when asking for old values...
                    displayer.getPropertyDetails ().setPropertyValues (oldValues);
                }
            }
        }
    }

    /** Called when user presses a button on some option (button) in the
    * dialog.
    * @param evt The button press event.
    */
    private void doButtonPressed (ActionEvent evt) {
        String label = evt.getActionCommand ();
        if (label.equals (PropertySheet.getString ("CTL_Cancel")))
            cancelValue ();
        else
            if (label.equals (PropertySheet.getString ("CTL_Default")))
                displayer.restoreDefaultValue();
            else
                if ( label.equals (PropertySheet.getString ("CTL_OK")) &&
                        (component instanceof EnhancedCustomPropertyEditor)
                   ) {
                    synchronized (lock) {
                        try {
                            if (!indexed)
                                displayer.setPropertyValue (
                                    ((EnhancedCustomPropertyEditor) component).getPropertyValue ()
                                );
                            displayer.notifyPropertyChange (
                                new PropertyChangeEvent (this, null, null, null)
                            );
                        } catch (IllegalStateException exc) {
                            // not a valid value
                        }
                    }
                }
        // close the dialog
        changed = false;
        dialog.setVisible (false);
        dialog.dispose ();
    }
}

/*
 * Log
 *  18   Gandalf   1.17        12/15/99 Jan Jancura     
 *  17   Gandalf   1.16        12/10/99 Jan Jancura     Bug 1620
 *  16   Gandalf   1.15        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   Gandalf   1.14        9/29/99  Ian Formanek    Fixed bug 2825 - Closing
 *       Customizer using window close button throws an exception, Fixed Bug 
 *       4104 - A NullPointerException is thrown when custom property editor on 
 *       read-only property is closed via the window's close button
 *  14   Gandalf   1.13        9/15/99  Jaroslav Tulach More private things & 
 *       support for default property.
 *  13   Gandalf   1.12        7/8/99   Jesse Glick     Removing all mention of 
 *       context help from this class--the custom property editor itself should 
 *       bind the context help, and NbDialog should pay attention.
 *  12   Gandalf   1.11        7/8/99   Jesse Glick     Bugfix relating to last 
 *       change.
 *  11   Gandalf   1.10        7/7/99   Jesse Glick     Relying on 
 *       DialogDescriptor's help support, rather than a separate button.
 *  10   Gandalf   1.9         7/2/99   Jesse Glick     Help button added to 
 *       custom proped when it has a help context.
 *  9    Gandalf   1.8         6/30/99  Ian Formanek    reflecting changes of 
 *       enhanced PropertyEditor interfaces
 *  8    Gandalf   1.7         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         4/4/99   Ian Formanek    
 *  5    Gandalf   1.4         3/20/99  Jaroslav Tulach DialogDescriptor has 
 *       only ActionListener
 *  4    Gandalf   1.3         3/20/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         3/18/99  Ian Formanek    Added Help on 
 *       PropertyDialog
 *  2    Gandalf   1.1         3/18/99  Ian Formanek    Changed buttons passed 
 *       into DialogDescriptor from JButton to String
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
