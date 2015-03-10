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

import java.util.*;

import org.openide.nodes.*;

/** List of Visualizers. This is holded by parent visualizer by a
* weak reference, 
*
* @author Jaroslav Tulach
*/
final class VisualizerChildren extends Object {
    /** parent visualizer */
    public final VisualizerNode parent;
    /** list of all objects here (VisualizerNode) 
     * @associates VisualizerNode*/
    public final List list = new LinkedList ();

    /** Creates new VisualizerChildren.
    * Can be called only from EventQueue.
    */
    public VisualizerChildren (VisualizerNode parent, Node[] nodes) {
        this.parent = parent;
        int s = nodes.length;
        for (int i = 0; i < s; i++) {
            VisualizerNode v = VisualizerNode.getVisualizer (this, nodes[i]);
            list.add (v);
        }
    }

    /*
      protected void finalize () {
        System.err.println("VC finalized for: " + parent.node + " list size: " + list.size () + " holdertype: " + parent.children + " holding: " + parent.children.get ());
      }
    */

    /** Notification of children addded event. Modifies the list of nodes
    * and fires info to all listeners.
    */
    public void added (VisualizerEvent.Added ev) {
        ListIterator it = list.listIterator ();
        boolean empty = !it.hasNext ();

        int[] indxs = ev.getArray ();
        Node[] nodes = ev.getAdded ();

        int current = 0;
        int inIndxs = 0;
        while (inIndxs < indxs.length) {
            while (current++ < indxs[inIndxs]) {
                it.next ();
            }
            it.add (VisualizerNode.getVisualizer (this, nodes[inIndxs]));
            inIndxs++;
        }


        VisualizerNode parent = this.parent;
        while (parent != null) {
            Object[] listeners = parent.getListenerList ();
            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel)listeners[i]).added (ev);
            }
            parent = (VisualizerNode)parent.getParent ();
        }

        if (empty) {
            // change of state
            this.parent.notifyVisualizerChildrenChange (list.size (), this);
        }

    }

    /** Notification that children has been removed. Modifies the list of nodes
    * and fires info to all listeners.
    */
    public void removed (VisualizerEvent.Removed ev) {
        ListIterator it = list.listIterator ();
        int[] indxs = ev.getArray ();

        int current = 0;
        int inIndxs = 0;
        while (inIndxs < indxs.length) {
            Object last;
            do {
                last = it.next ();
            } while (current++ < indxs[inIndxs]);

            ev.removed.add (last);
            it.remove ();

            inIndxs++;
        }

        VisualizerNode parent = this.parent;
        while (parent != null) {
            Object[] listeners = parent.getListenerList ();
            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel)listeners[i]).removed (ev);
            }
            parent = (VisualizerNode)parent.getParent ();
        }

        if (list.isEmpty ()) {
            // now is empty
            this.parent.notifyVisualizerChildrenChange (0, this);
        }
    }

    /** Notification that children has been reordered. Modifies the list of nodes
    * and fires info to all listeners.
    */
    public void reordered (VisualizerEvent.Reordered ev) {
        int[] indxs = ev.getArray ();
        Object[] old = list.toArray ();
        Object[] arr = new Object[old.length];


        int s = indxs.length;
        for (int i = 0; i < s; i++) {
            arr[indxs[i]] = old[i];
        }

        list.clear ();
        list.addAll (Arrays.asList (arr));

        VisualizerNode parent = this.parent;
        while (parent != null) {
            Object[] listeners = parent.getListenerList ();
            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel)listeners[i]).reordered (ev);
            }
            parent = (VisualizerNode)parent.getParent ();
        }
    }

}

/*
* Log
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         9/1/99   Jaroslav Tulach Holding of children is a 
*       bit stronger.
*  1    Gandalf   1.0         8/27/99  Jaroslav Tulach 
* $
*/