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

import javax.swing.JList;
import javax.swing.SwingUtilities;

import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
*
* @author Dafe Simonek
*/
final class ListViewDropSupport implements DropTargetListener,
    Runnable {

    // Attributes

    /** true if support is active, false otherwise */
    boolean active = false;

    /** Drop target asociated with the tree */
    DropTarget dropTarget;

    /** The index of last item the cursor hotspot was above */
    int lastIndex = -1;

    // Associations

    /** View manager. */
    protected ListView view;

    /** The component we are supporting with drop support */
    protected JList list;

    /** For managing visual appearance of JList cells. */
    protected NodeRenderer.List cellRenderer;

    // Operations
    /** Creates new TreeViewDropSupport */
    public ListViewDropSupport (ListView view, JList list) {
        this.view = view;
        this.list = list;
        //cellRenderer = (NodeListCellRenderer)list.getCellRenderer();
    }

    /** User is starting to drag over us */
    public void dragEnter (DropTargetDragEvent dtde) {
        lastIndex = indexWithCheck(dtde);
        if (lastIndex < 0)
            dtde.rejectDrag();
        else {
            dtde.acceptDrag(dtde.getDropAction());
            NodeRenderer.dragEnter(
                list.getModel().getElementAt(lastIndex));
            list.repaint(list.getCellBounds(lastIndex, lastIndex));
        }
    }

    /** User drags over us */
    public void dragOver (DropTargetDragEvent dtde) {
        int index = indexWithCheck(dtde);
        if (index < 0) {
            dtde.rejectDrag();
            if (lastIndex >= 0) {
                NodeRenderer.dragExit();
                list.repaint(list.getCellBounds(lastIndex, lastIndex));
                lastIndex = -1;
            }
        } else {
            dtde.acceptDrag(dtde.getDropAction());
            if (lastIndex != index) {
                if (lastIndex < 0)
                    lastIndex = index;
                NodeRenderer.dragExit();
                NodeRenderer.dragEnter(list.getModel().getElementAt(index));
                list.repaint(list.getCellBounds(lastIndex, index));
                lastIndex = index;
            }
        }
    }

    public void dropActionChanged (DropTargetDragEvent dtde) {
        //System.out.println("Changing drop action..."); // NOI18N
        // PENDING...?
    }

    /** User exits the dragging */
    public void dragExit (DropTargetEvent dte) {
        if (lastIndex >= 0) {
            NodeRenderer.dragExit();
            list.repaint(list.getCellBounds(lastIndex, lastIndex));
        }
    }

    /** Performs the drop action, if we are dropping on
    * right node and target node agrees.
    */
    public void drop (DropTargetDropEvent dtde) {
        //System.out.println("Dropping!!!"); // NOI18N
        // obtain the node we have cursor on
        int index = list.locationToIndex(dtde.getLocation());
        Object obj = null;
        // return if conditions are not satisfied
        int dropAction = dtde.getDropAction();
        if ((index < 0) ||
                ((dropAction & view.getAllowedDropActions()) == 0) ||
                !((obj = list.getModel().getElementAt(index)) instanceof Node)) {
            dtde.rejectDrop();
            return;
        }
        // get paste types for given transferred transferable
        PasteType[] pt =
            DragDropUtilities.getPasteTypes((Node)obj, dtde.getTransferable());
        if ((pt == null) || (pt.length <= 0)) {
            dtde.dropComplete(false);
            // something is wrong, notify user
            // ugly hack, but if we don't wait, deadlock will come
            // (sun's issue....)
            RequestProcessor.postRequest(this, 500);
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
    int indexWithCheck (DropTargetDragEvent dtde) {
        int dropAction = dtde.getDropAction();
        // check actions
        if ((dropAction & view.getAllowedDropActions()) == 0)
            return -1;
        // check location
        int index = list.locationToIndex(dtde.getLocation());
        Object obj = null;
        if ((index < 0) ||
                !((obj = list.getModel().getElementAt(index)) instanceof Node))
            return -1;
        /* JST: Is necessary? Cannot be replaced by the use of special
        * transferable?
          
        // accept only node data flavors or multi flavor
        if (!dtde.isDataFlavorSupported(NodeTransfer.nodeCutFlavor) &&
            !dtde.isDataFlavorSupported(NodeTransfer.nodeCopyFlavor) &&
            !dtde.isDataFlavorSupported(ExTransferable.multiFlavor))
          return -1;  
        */

        // succeeded
        return index;
    }

    /** Safe accessor to the drop target which is asociated
    * with the tree */
    DropTarget getDropTarget () {
        if (dropTarget == null) {
            dropTarget =
                new DropTarget(list, view.getAllowedDropActions(),
                               this, false);
        }
        return dropTarget;
    }

    /** Safe getter for the cell renderer of asociated list */
    NodeRenderer.List getCellRenderer () {
        if (cellRenderer == null)
            cellRenderer = (NodeRenderer.List)list.getCellRenderer();
        return cellRenderer;
    }


}

/*
* Log
*  8    Gandalf   1.7         1/13/00  Ian Formanek    NOI18N
*  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    Gandalf   1.5         8/27/99  Jaroslav Tulach New threading model & 
*       Children.
*  5    Gandalf   1.4         6/30/99  Jaroslav Tulach Drag and drop support
*  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  3    Gandalf   1.2         5/17/99  David Simonek   comments removed
*  2    Gandalf   1.1         4/30/99  David Simonek   
*  1    Gandalf   1.0         4/27/99  David Simonek   
* $
*/