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

package org.openide.explorer.view;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.tree.*;

import org.openide.TopManager;
import org.openide.nodes.*;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/** In-place editor in the tree view component.
*
* @author Petr Hamernik
*/
class TreeViewCellEditor extends DefaultTreeCellEditor {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2171725285964032312L;

    ResourceBundle bundle = NbBundle.getBundle("org.openide.explorer.view.Bundle"); // NOI18N

    // Attributes

    /** Indicates whether is drag and drop currently active or not */
    boolean dndActive = false;

    /** Construct a cell editor.
    * @param tree the tree
    * @param renderer the renderer to use for the cell
    */
    public TreeViewCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
        addCellEditorListener(new CellEditorListener() {
                                  public void editingStopped(ChangeEvent e) {
                                      //CellEditor sometimes(probably after stopCellEditing() call) gains one focus but loses two
                                      if (stopped) {
                                          return;
                                      }

                                      stopped = true;
                                      TreePath lastP = getSuperLastPath();
                                      if (lastP != null) {
                                          Node n = Visualizer.findNode (lastP.getLastPathComponent());
                                          if (n != null && n.canRename ()) {
                                              String newStr = (String) getCellEditorValue();
                                              try {
                                                  n.setName(newStr);
                                              }
                                              catch (IllegalArgumentException exc) {
                                                  String msg = exc.getMessage();
                                                  if ((msg == null) || msg.equals("")) // NOI18N
                                                      msg = bundle.getString("RenamingFailed"); // NOI18N
                                                  TopManager.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                                              }
                                          }
                                      }
                                  }
                                  public void editingCanceled(ChangeEvent e) {
                                      cancelled = true;
                                  }
                              });
    }

    /** True, if the editation was cancelled by the user.
    */
    private boolean cancelled = false;
    /** Stopped is true, if the editation is over (editingStopped is called for the
        first time). The two variables have virtually the same function, but are kept
        separate for code clarity.
    */
    private boolean stopped = false;

    /*
     * This is invoked if a TreeCellEditor is not supplied in the constructor.
     * It returns a TextField editor.
     */
    protected TreeCellEditor createTreeCellEditor() {
        JTextField tf = new JTextField() {

                            public void addNotify() {
                                stopped = cancelled = false;
                                super.addNotify();
                                requestFocus();
                            }
                        };

        tf.registerKeyboardAction(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    cancelled = true;
                    cancelCellEditing();
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
            JComponent.WHEN_FOCUSED
        );

        tf.addFocusListener (new java.awt.event.FocusAdapter () {
                                 public void focusLost (java.awt.event.FocusEvent evt) {
                                     if (stopped || cancelled)
                                         return;
                                     if (!stopCellEditing())
                                         cancelCellEditing();
                                 }

                                 public void focusGained (java.awt.event.FocusEvent evt) {
                                 }
                             }
                            );

        Ed ed = new Ed(tf);
        ed.setClickCountToStart(1);
        return ed;
    }

    /** Encapsulation of protected variable lastPath */
    TreePath getSuperLastPath() {
        return lastPath;
    }

    /*
    * If the realEditor returns true to this message, prepareForEditing
    * is messaged and true is returned.
    */
    public boolean isCellEditable(EventObject event) {
        if ((event != null) && (event instanceof MouseEvent)) {
            if (!SwingUtilities.isLeftMouseButton((MouseEvent)event)) {
                return false;
            }
        }
        if (lastPath != null) {
            Node n = Visualizer.findNode (lastPath.getLastPathComponent());
            if (n == null || !n.canRename ()) {
                return false;
            }
        }
        // disallow editing if we are in DnD operation
        if (dndActive)
            return false;

        return super.isCellEditable(event);
    }

    protected void prepareForEditing() {
        super.prepareForEditing();

        if (lastPath != null) {
            Node node = Visualizer.findNode (lastPath.getLastPathComponent());
            editingIcon = NodeRenderer.getIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16));
        }

    }

    /*** Sets the state od drag and drop operation.
    * It's here only because of JTree's bug which allows to
    * start the editing even if DnD operation occurs
    * (bug # )
    */
    void setDnDActive (boolean dndActive) {
        this.dndActive = dndActive;
    }

    /** Redefined default cell editor to convert nodes to name */
    static class Ed extends DefaultCellEditor {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -6373058702842751408L;

        public Ed(JTextField tf) {
            super(tf);
        }

        /** Main method of the editor.
        * @return component of editor
        */
        public Component getTreeCellEditorComponent(JTree tree, Object value,
                boolean isSelected, boolean expanded,
                boolean leaf, int row) {
            Node ren = Visualizer.findNode (value);
            if ((ren != null) && (ren.canRename ()))
                delegate.setValue(ren.getName());
            else
                delegate.setValue(""); // NOI18N
            ((JTextField) editorComponent).selectAll();
            return editorComponent;
        }
    }
}

/*
 * Log
 *  18   Gandalf-post-FCS1.16.1.0    3/29/00  Svatopluk Dedic Fixed error handling
 *  17   Gandalf   1.16        2/8/00   Radko Najman    fixed bug #5676
 *  16   Gandalf   1.15        1/12/00  Ian Formanek    NOI18N
 *  15   Gandalf   1.14        1/9/00   Radko Najman    
 *  14   Gandalf   1.13        1/8/00   Radko Najman    Exception error message
 *  13   Gandalf   1.12        1/5/00   Radko Najman    Fixed bug #5114
 *  12   Gandalf   1.11        11/1/99  Petr Hamernik   fixed 
 *       NullPointerException
 *  11   Gandalf   1.10        10/28/99 Ian Formanek    Fixed bug #4603 - When 
 *       in-place renaming an item in tree, when the editing is finished by 
 *       clicking outside of the edit line, the item *should* be renamed to the 
 *       current text in the input line.
 *  10   Gandalf   1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         9/24/99  Petr Hamernik   fixed bug #3486
 *  8    Gandalf   1.7         8/27/99  Jaroslav Tulach New threading model & 
 *       Children.
 *  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         4/27/99  David Simonek   autoscroll support and 
 *       visual feedback in DnD operations added
 *  5    Gandalf   1.4         3/20/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         3/18/99  Ian Formanek    
 *  3    Gandalf   1.2         3/17/99  Ian Formanek    Fixed bug with 
 *       CellEditor which caused that it stayed visible even after loss of 
 *       focus.
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach 
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
