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
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JTree;
import javax.swing.tree.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.util.datatransfer.ExClipboard;

/** Support for the drag operations in the TreeView.
*
* @author Dafe Simonek
*/
final class TreeViewDragSupport implements DragSourceListener,
            DragGestureListener,
    TreeSelectionListener {
    // Attributes

    /** True when we are active, false otherwise */
    boolean active = false;

    /** Recognizes default gesture */
    DragGestureRecognizer defaultGesture;

    /** Holds content of the selection in tree.
    * It's here only for workaround of sun's bug */
    TreePath[] curSelection;
    TreePath[] oldSelection;


    // Associations

    /** The view that manages viewing the data in a tree. */
    protected TreeView view;

    /** The tree which we are supporting (our client) */
    protected JTree tree;

    /** Cell renderer - PENDING - do we need it? */
    //protected DnDTreeViewCellRenderer cellRenderer;

    // Operations
    /** Creates new TreeViewDragSupport, initializes gesture */
    public TreeViewDragSupport (TreeView view, JTree tree) {
        this.view = view;
        this.tree = tree;
        // it's here only because of workaround for sun's bug
        tree.addTreeSelectionListener(this);
    }

    /** Initiating the drag */
    public void dragGestureRecognized (DragGestureEvent dge) {
        int dragAction = dge.getDragAction();
        // check allowed actions
        if ((dragAction & view.getAllowedDragActions()) == 0)
            return;
        // obtain the nodes to work with
        Node[] nodes = obtainNodes(dge);
        // check nodes
        if (nodes == null)
            return;
        // check if the nodes are willing to do selected action
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
            // notify tree cell editor that DnD operationm is active
            TreeCellEditor tce = tree.getCellEditor();
            if (tce instanceof TreeViewCellEditor)
                ((TreeViewCellEditor)tce).setDnDActive(true);
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
        //System.out.println("DnD ended..."); // NOI18N
        // notify tree cell editor that DnD operationm is active
        // no more
        TreeCellEditor tce = tree.getCellEditor();
        if (tce instanceof TreeViewCellEditor)
            ((TreeViewCellEditor)tce).setDnDActive(false);
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
                                 tree, view.getAllowedDragActions(), this);
        }
        return defaultGesture;
    }

    /** Utility method. Returns either selected nodes in tree
    * (if cursor hotspot is above some selected node) or the node
    * the cursor points to.
    * @return Node array or null if position of the cursor points
    * to no node.
    */
    Node[] obtainNodes (DragGestureEvent dge) {
        Point dragOrigin = dge.getDragOrigin();
        TreePath tp = tree.getPathForLocation(dragOrigin.x, dragOrigin.y);
        Object obj = null;
        // return if conditions are not satisfied
        if ((tp == null) ||
                !((obj = tp.getLastPathComponent()) instanceof Node))
            return null;
        // workaround for Sun's bug #4165577
        // we must repair the selection before dragging
        if ((oldSelection != null) && wasSelected(obj)) {
            //System.out.println("Repairing..."); // NOI18N
            tree.setSelectionPaths(oldSelection);
            curSelection = null;
        }
        // ---end of workaround
        Node[] result = null;
        if (tree.isPathSelected(tp)) {
            // cursor above selected, so return all selected nodes
            TreePath[] tps = tree.getSelectionPaths();
            result = new Node[tps.length];
            Object curObj = null;
            for (int i = 0; i < tps.length; i++) {
                curObj = tps[i].getLastPathComponent();
                if (!(curObj instanceof Node))
                    return null;
                result[i] = (Node)curObj;
            }
        } else {
            // return only the node the cursor is above
            result = new Node[] { (Node)obj };
        }
        return result;
    }

    /** Stores last two selections.
    * Workaround for sun's bug */
    public void valueChanged (TreeSelectionEvent tse) {
        TreePath[] newSelection = tree.getSelectionPaths();
        if ((newSelection != null) &&
                (!Arrays.equals(curSelection, newSelection))) {
            oldSelection = (curSelection == null) ? newSelection : curSelection;
            curSelection = newSelection;
        }
    }

    /** @return True if given object was selected in old selection,
    * false otherwise */
    boolean wasSelected (Object obj) {
        if (oldSelection == null)
            return false;
        for (int i = 0; i < oldSelection.length; i++) {
            if (obj.equals(oldSelection[i].getLastPathComponent()))
                return true;
        }
        return false;
    }


} /* end class TreeViewDragSupport */


/*
* Log
*  9    Gandalf   1.8         1/13/00  Ian Formanek    NOI18N
*  8    Gandalf   1.7         1/12/00  Ian Formanek    NOI18N
*  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  5    Gandalf   1.4         5/17/99  David Simonek   comments removed
*  4    Gandalf   1.3         4/30/99  David Simonek   
*  3    Gandalf   1.2         4/28/99  David Simonek   drag and drop in tree now
*       supports multi-selection
*  2    Gandalf   1.1         4/27/99  David Simonek   autoscroll support and 
*       visual feedback in DnD operations added
*  1    Gandalf   1.0         4/21/99  David Simonek   
* $
*/