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
import java.awt.datatransfer.*;
import java.awt.Point;
import java.awt.Cursor;
import java.io.IOException;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.TopManager;
import org.openide.util.datatransfer.*;
import org.openide.util.UserCancelException;
import org.openide.util.NbBundle;

/** Class that provides methods for common tasks needed during
* drag and drop when working with explorer views.
*
* @author Dafe Simonek
*/
final class DragDropUtilities extends Object {

    /** No need to instantiate this class */
    private DragDropUtilities () {
    }

    /** Utility method - chooses and returns right cursor
    * for given user drag action.
    */
    static Cursor chooseCursor (int dragAction, boolean canDrop) {
        Cursor result = null;
        switch (dragAction) {
        case DnDConstants.ACTION_COPY:
        case DnDConstants.ACTION_COPY_OR_MOVE:
            result = canDrop ? DragSource.DefaultCopyDrop
                     : DragSource.DefaultCopyNoDrop;
            break;
        case DnDConstants.ACTION_MOVE:
            result = canDrop ? DragSource.DefaultMoveDrop
                     : DragSource.DefaultMoveNoDrop;
            break;
        case DnDConstants.ACTION_LINK:
            result = canDrop ? DragSource.DefaultLinkDrop
                     : DragSource.DefaultLinkNoDrop;
            break;
        default:
            result = canDrop ? DragSource.DefaultCopyDrop
                     : DragSource.DefaultCopyNoDrop;
            break;
        }
        return result;
    }

    /** Utility method.
    * @return true if given node supports given action,
    * false otherwise.
    */
    static boolean checkNodeForAction (Node node, int dragAction) {
        if (node.canCut() &&
                ((dragAction == DnDConstants.ACTION_MOVE) ||
                 (dragAction == DnDConstants.ACTION_COPY_OR_MOVE)))
            return true;
        if (node.canCopy() &&
                ((dragAction == DnDConstants.ACTION_COPY) ||
                 (dragAction == DnDConstants.ACTION_COPY_OR_MOVE) ||
                 (dragAction == DnDConstants.ACTION_LINK) ||
                 (dragAction == DnDConstants.ACTION_REFERENCE)))
            return true;
        // hmmm, conditions not satisfied..
        return false;
    }

    /** Gets right transferable of given nodes (according to given
    * drag action) and also converts the transferable.<br>
    * Can be called only with correct action constant.
    * @return The transferable.
    */
    static Transferable getNodeTransferable (Node[] nodes, int dragAction)
    throws IOException {
        Transferable[] tArray = new Transferable[nodes.length];
        //System.out.println("Sel count: " + nodes.length); // NOI18N
        for (int i = 0; i < nodes.length; i++) {
            ExClipboard cb = TopManager.getDefault ().getClipboard ();
            if (dragAction == DnDConstants.ACTION_MOVE)
                tArray[i] = cb.convert (nodes[i].clipboardCut());
            else
                tArray[i] = cb.convert (nodes[i].clipboardCopy());
        }
        if (tArray.length == 1)
            // only one node, so return regular single transferable
            return tArray[0];
        // enclose the transferables into multi transferable
        return new ExTransferable.Multi(tArray);
    }

    /** Returns transferable of given node
    * @return The transferable.
    */
    static Transferable getNodeTransferable (Node node, int dragAction)
    throws IOException {
        return getNodeTransferable(new Node[] { node }, dragAction);
    }

    /** Performs the drop. Performs paste on given paste type.
    */
    static void performDrop (PasteType type) {
        //System.out.println("performing drop...."); // NOI18N
        try {
            Transferable trans = type.paste();
            /*Clipboard clipboard = TopManager.getDefault().getClipboard();
            if (trans != null) {
              ClipboardOwner owner = trans instanceof ClipboardOwner ?
                (ClipboardOwner)trans
              :
                new StringSelection ("");
              clipboard.setContents(trans, owner);
        }*/
        } catch (UserCancelException exc) {
            // ignore - user just pressed cancel in some dialog....
        } catch (java.io.IOException e) {
            org.openide.TopManager.getDefault().notifyException(e);
        }
    }

    /** Returns array of paste types for given transferable.
    * If given transferable contains multiple transferables,
    * multi paste type which encloses pate types of all contained
    * transferables is returned.
    * Returns empty array if given node did not accepted the transferable
    * (or some sub-transferables in multi transferable)
    * 
    * @param node given node to ask fro paste types
    * @param trans transferable to discover
    */
    static PasteType[] getPasteTypes (Node node, Transferable trans) {
        // find out if given transferable is multi
        boolean isMulti = false;
        try {
            isMulti = trans.isDataFlavorSupported(ExTransferable.multiFlavor);
        } catch (Exception e) {
            // patch to get the Netbeans start under Solaris
            // [PENDINGworkaround]
        }
        if (!isMulti) {
            // only single, so return paste types
            return node.getPasteTypes(trans);
        } else {
            // multi transferable, we must do extra work
            try {
                MultiTransferObject obj = (MultiTransferObject)
                                          trans.getTransferData(ExTransferable.multiFlavor);
                int count = obj.getCount();
                Transferable[] t = new Transferable[count];
                PasteType[] p = new PasteType[count];
                PasteType[] curTypes = null;
                // extract default paste types of transferables
                for (int i = 0; i < count; i++) {
                    t[i] = obj.getTransferableAt(i);
                    curTypes = node.getPasteTypes(t[i]);
                    // return if not accepted
                    if (curTypes.length == 0)
                        return curTypes;
                    p[i] = curTypes[0];
                }
                // return new multi paste type
                return new PasteType[] { new MultiPasteType(t, p) };
            } catch (UnsupportedFlavorException e) {
                // ignore and return empty array
            }
            catch (IOException e) {
                // ignore and return empty array
            }
        }
        return new PasteType[0];
    }

    /** Notifies user that the drop was not succesfull. */
    static void dropNotSuccesfull () {
        TopManager.getDefault().notify(new NotifyDescriptor.Message(
                                           NbBundle.getBundle(TreeViewDropSupport.class).
                                           getString("MSG_NoPasteTypes"),
                                           NotifyDescriptor.WARNING_MESSAGE)
                                      );
    }

    /** Paste type used when in clipbopard is MultiTransferable */
    static final class MultiPasteType extends PasteType {

        // Attributes

        /** Array of transferables */
        Transferable[] t;
        /** Array of paste types */
        PasteType[] p;

        // Operations

        /** Constructs new MultiPasteType for the given
        * transferables and paste types.*/
        MultiPasteType(Transferable[] t, PasteType[] p) {
            this.t = t;
            this.p = p;
        }

        /** Performs the paste action.
        * @return Transferable which should be inserted into the
        *   clipboard after paste action. It can be null, which means
        *   that clipboard content should be cleared.
        */
        public Transferable paste() throws IOException {
            int size = p.length;
            Transferable[] arr = new Transferable[size];
            // perform paste for all source transferables
            for (int i = 0; i < size; i++) {
                //System.out.println("Pasting #" + i); // NOI18N
                arr[i] = p[i].paste();
            }
            return new ExTransferable.Multi(arr);
        }
    } // end of MultiPasteType


}

/*
* Log
*  8    Gandalf   1.7         1/13/00  Ian Formanek    NOI18N
*  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    Gandalf   1.5         9/22/99  Jesse Glick     ExClipboard.Convertor 
*       changed signature to accommodate converting to node selection.
*  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    Gandalf   1.3         5/16/99  David Simonek   debug prints commented
*  3    Gandalf   1.2         4/30/99  David Simonek   
*  2    Gandalf   1.1         4/28/99  David Simonek   drag and drop in tree now
*       supports multi-selection
*  1    Gandalf   1.0         4/27/99  David Simonek   
* $
*/
