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

import java.awt.dnd.*;
import java.awt.Point;
import java.awt.datatransfer.*;

import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeCellEditor;
import javax.swing.SwingUtilities;

import org.openide.TopManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/** Implementation of drop support for asociated Tree View.
*
* @author Dafe Simonek
*/
final class TreeViewDropSupport implements DropTargetListener,
    Runnable {

    // Attributes

    /** true if support is active, false otherwise */
    boolean active = false;

    /** Drop target asociated with the tree */
    DropTarget dropTarget;

    /** The path to last node above which we were during
    * DnD operation. */
    TreePath lastPath;


    // Associations

    /** View manager. */
    protected TreeView view;

    /** The component we are supporting with drop support */
    protected JTree tree;

    // Operations
    /** Creates new TreeViewDropSupport */
    public TreeViewDropSupport (TreeView view, JTree tree) {
        this.view = view;
        this.tree = tree;
    }

    /** User is starting to drag over us */
    public void dragEnter (DropTargetDragEvent dtde) {
        //System.out.println("Drag enter...."); // NOI18N
        TreePath tp = getTreePath(dtde);
        if (tp == null) {
            dtde.rejectDrag();
        } else {
            dtde.acceptDrag(dtde.getDropAction());
            lastPath = tp;
            NodeRenderer.dragEnter(tp.getLastPathComponent());
            tree.paintImmediately(tree.getPathBounds(tp));
        }
    }

    /** User drags over us */
    public void dragOver (DropTargetDragEvent dtde) {
        TreePath tp = getTreePath(dtde);
        if (tp == null) {
            //System.out.println("REjecting..."); // NOI18N
            dtde.rejectDrag();
            if (lastPath != null) {
                NodeRenderer.dragExit();
                tree.repaint(tree.getPathBounds(lastPath));
                lastPath = null;
            }
        } else {
            //System.out.println("Accepting..."); // NOI18N
            dtde.acceptDrag(dtde.getDropAction());
            if ((lastPath != null) && (!lastPath.equals(tp))) {
                NodeRenderer.dragExit();
                tree.repaint(tree.getPathBounds(lastPath));
            }
            if (!tp.equals(lastPath)) {
                NodeRenderer.dragEnter(tp.getLastPathComponent());
                tree.repaint(tree.getPathBounds(tp));
                lastPath = tp;
            }
        }
    }

    public void dropActionChanged (DropTargetDragEvent dtde) {
        //System.out.println("Changing drop action..."); // NOI18N
        // PENDING...?
    }

    /** User exits the dragging */
    public void dragExit (DropTargetEvent dte) {
        if (lastPath != null) {
            NodeRenderer.dragExit();
            tree.repaint(tree.getPathBounds(lastPath));
            lastPath = null;
        }
    }

    /** Performs the drop action, if we are dropping on
    * right node and target node agrees.
    */
    public void drop (DropTargetDropEvent dtde) {
        //System.out.println("Dropping!!!"); // NOI18N
        // obtain the node we have cursor on
        Point location = dtde.getLocation();
        TreePath tp = tree.getPathForLocation(location.x, location.y);
        Object obj = null;
        // return if conditions are not satisfied
        int dropAction = dtde.getDropAction();
        if ((tp == null) ||
                ((dropAction & view.getAllowedDropActions()) == 0) ||
                !((obj = tp.getLastPathComponent()) instanceof Node)) {
            dtde.rejectDrop();
            return;
        }
        // get paste types for given transferred transferable
        PasteType[] pt =
            DragDropUtilities.getPasteTypes((Node)obj, dtde.getTransferable());
        if ((pt == null) || (pt.length <= 0)) {
            // notify user
            dtde.dropComplete(false);
            // caused deadlocks, so commented...
            //RequestProcessor.postRequest(this, 500);
            return;
        }
        // finally perform the drop
        dtde.acceptDrop(dropAction);
        if (dropAction == DnDConstants.ACTION_LINK) {
            // show popup menu to the user
            // PENDING
        } else {
            DragDropUtilities.performDrop(pt[0]);
        }
        // notify tree cell editor that DnD operation has ended
        // ??? it's here because source.dragDropEnd is not called
        // when accepting drop
        TreeCellEditor tce = tree.getCellEditor();
        if (tce instanceof TreeViewCellEditor)
            ((TreeViewCellEditor)tce).setDnDActive(false);
        // finished
        dtde.dropComplete(true);
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
        getDropTarget().setActive(active);
    }

    /** Implementation of the runnable interface.
    * Notifies user in AWT thread. */
    public void run () {
        SwingUtilities.invokeLater(
            new Runnable () {
                public void run () {
                    DragDropUtilities.dropNotSuccesfull();
                }
            }
        );
    }

    /** @return The tree path to the node the cursor is above now or
    * null if no such node currently exists or if conditions were not
    * satisfied to continue with DnD operation.
    */    
    TreePath getTreePath (DropTargetDragEvent dtde) {
        int dropAction = dtde.getDropAction();
        // check actions
        if ((dropAction & view.getAllowedDropActions()) == 0)
            return null;
        // check location
        Point location = dtde.getLocation();
        TreePath tp = tree.getPathForLocation(location.x, location.y);
        Object obj = null;
        if ((tp == null) ||
                !((obj = tp.getLastPathComponent()) instanceof Node))
            return null;
        /* JST: Is necessary? Cannot be overriden to use the
        * special Transferable?
        // accept only some data flavors
        if (!dtde.get isDataFlavorSupported(NodeTransfer.nodeCutFlavor) &&
            !dtde.isDataFlavorSupported(NodeTransfer.nodeCopyFlavor) &&
            !dtde.isDataFlavorSupported(ExTransferable.multiFlavor)) {
          return null;
    }  
        */
        // succeeded
        return tp;
    }

    /** Safe accessor to the drop target which is asociated
    * with the tree */
    DropTarget getDropTarget () {
        if (dropTarget == null) {
            dropTarget =
                new DropTarget(tree, view.getAllowedDropActions(),
                               this, false);
        }
        return dropTarget;
    }


} /* end class TreeViewDropSupport */


/*
* Log
*  11   Gandalf   1.10        1/13/00  Ian Formanek    NOI18N
*  10   Gandalf   1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  9    Gandalf   1.8         8/27/99  Jaroslav Tulach New threading model & 
*       Children.
*  8    Gandalf   1.7         6/30/99  Jaroslav Tulach Drag and drop support
*  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  6    Gandalf   1.5         5/16/99  David Simonek   debug prints commented
*  5    Gandalf   1.4         4/30/99  David Simonek   
*  4    Gandalf   1.3         4/28/99  David Simonek   drag and drop in tree now
*       supports multi-selection
*  3    Gandalf   1.2         4/27/99  David Simonek   autoscroll support and 
*       visual feedback in DnD operations added
*  2    Gandalf   1.1         4/22/99  David Simonek   made non-public
*  1    Gandalf   1.0         4/21/99  David Simonek   
* $
*/