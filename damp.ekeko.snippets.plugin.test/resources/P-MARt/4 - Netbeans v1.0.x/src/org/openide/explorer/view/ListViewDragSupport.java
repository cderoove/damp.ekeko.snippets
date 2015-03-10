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

import java.util.TooManyListenersException;
import java.awt.dnd.*;
import java.awt.Point;
import java.awt.Cursor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JList;

import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.util.datatransfer.ExClipboard;

/**
*
* @author Dafe Simonek
*/
class ListViewDragSupport implements DragSourceListener,
    DragGestureListener {

    // Attributes

    /** True when we are active, false otherwise */
    boolean active = false;

    /** Recognizes default gesture */
    DragGestureRecognizer defaultGesture;

    /** Holds selected indices - it's here only
    * as a workaround for sun's bug */
    /*int[] oldSelection;
    int[] curSelection;*/

    // Associations

    /** The view that manages viewing the data in a tree. */
    protected ListView view;

    /** The tree which we are supporting (our client) */
    protected JList list;


    // Operations

    /** Creates new TreeViewDragSupport, initializes gesture */
    public ListViewDragSupport (ListView view, JList list) {
        this.view = view;
        this.list = list;
    }

    /** Initiating the drag */
    public void dragGestureRecognized (DragGestureEvent dge) {
        int dragAction = dge.getDragAction();
        // check allowed actions
        if ((dragAction & view.getAllowedDragActions()) == 0)
            return;
        // obtain the node(s) to drag
        Node[] nodes = obtainNodes(dge);
        if (nodes == null)
            return;
        // check if all the nodes are willing to do selected action
        for (int i = 0; i < nodes.length; i++) {
            if (!DragDropUtilities.checkNodeForAction(nodes[i], dragAction))
                return;
        }
        // get transferable and start the drag
        try {
            Transferable transferable =
                DragDropUtilities.getNodeTransferable(nodes, dragAction);
            //System.out.println("Transferable: " + transferable); // NOI18N
            dge.startDrag(
                DragDropUtilities.chooseCursor(dragAction, view.isDropTarget()),
                transferable,
                this
            );
        } catch (InvalidDnDOperationException exc) {
            // cannot start the drag, notify user
            TopManager.getDefault().notifyException(exc);
        } catch (IOException exc) {
            // cannot start the drag, notify user
            TopManager.getDefault().notifyException(exc);
        }
    }

    public void dragEnter (DragSourceDragEvent dsde) {
    }

    public void dragOver (DragSourceDragEvent dsde) {
    }

    public void dropActionChanged (DragSourceDragEvent dsde) {
    }

    public void dragExit (DragSourceEvent dse) {
    }

    public void dragDropEnd (DragSourceDropEvent dsde) {
    }

    /** Activates or deactivates Drag support on asociated JTree
    * component
    * @param active true if the support should be active, false
    * otherwise
    */
    public void activate (boolean active) {
        if (this.active == active)
            return;
        this.active = active;
        DragGestureRecognizer dgr = getDefaultGestureRecognizer();
        if (active) {
            dgr.setSourceActions(view.getAllowedDragActions());
            try {
                dgr.removeDragGestureListener(this);
                dgr.addDragGestureListener(this);
            } catch (TooManyListenersException exc) {
                throw new InternalError("Too many listeners for drag gesture."); // NOI18N
            }
        } else {
            dgr.removeDragGestureListener(this);
        }
    }

    /** Safe getter for default gesture<br>
    * (creates the gesture when called for the first time)
    */
    DragGestureRecognizer getDefaultGestureRecognizer () {
        if (defaultGesture == null) {
            DragSource ds = DragSource.getDefaultDragSource();
            defaultGesture = ds.createDefaultDragGestureRecognizer(
                                 list, view.getAllowedDragActions(), this);
        }
        return defaultGesture;
    }

    /** Utility method. Returns either selected nodes in the list
    * (if cursor hotspot is above some selected node) or the node
    * the cursor points to.
    * @return Node array or null if position of the cursor points
    * to no node.
    */
    Node[] obtainNodes (DragGestureEvent dge) {
        Point dragOrigin = dge.getDragOrigin();
        int index = list.locationToIndex(dge.getDragOrigin());
        Object obj;
        // check conditions
        if ((index < 0) ||
                (!((obj = list.getModel().getElementAt(index)) instanceof Node)))
            return null;
        Node[] result = null;
        if (list.isSelectedIndex(index)) {
            // cursor is above selection, so return all selected indices
            Object[] selected = list.getSelectedValues();
            result = new Node[selected.length];
            for (int i = 0; i < selected.length; i++) {
                if (!(selected[i] instanceof Node))
                    return null;
                result[i] = (Node)selected[i];
            }
        } else {
            // return only the node the cursor is above
            result = new Node[] { (Node)obj };
        }
        return result;
    }

    /** Stores last two selections.
    * Workaround for sun's bug */
    /*public void valueChanged (ListSelectionEvent lse) {
      int[] newSelection = list.getSelectedIndices();
      if ((newSelection != null) && (newSelection.length > 0) &&
          (!Arrays.equals(curSelection, newSelection))) {
        oldSelection = (curSelection == null) ? newSelection : curSelection;
        curSelection = newSelection;
        System.out.println("Old selection:: " + oldSelection.length);
      }
}*/

    /** @return True if given index was selected in old selection,
    * false otherwise */
    /*boolean wasSelected (int index) {
      if (oldSelection == null)
        return false;
      for (int i = 0; i < oldSelection.length; i++) {
        if (index == oldSelection[i])
          return true;
      }
      return false;
}*/


} // end of ListViewDragSupport

/*
* Log
*  7    Gandalf   1.6         1/13/00  Ian Formanek    NOI18N
*  6    Gandalf   1.5         1/12/00  Ian Formanek    NOI18N
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  3    Gandalf   1.2         5/17/99  David Simonek   comments removed
*  2    Gandalf   1.1         4/30/99  David Simonek   
*  1    Gandalf   1.0         4/27/99  David Simonek   
* $
*/