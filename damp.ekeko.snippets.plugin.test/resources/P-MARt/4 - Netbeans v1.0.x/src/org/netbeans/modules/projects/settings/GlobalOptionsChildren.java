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

package org.netbeans.modules.projects.settings;

import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.util.*;

import org.openide.TopManager;
import org.openide.nodes.*;
/**
 *
 * @author  mryzl
 */

public class GlobalOptionsChildren extends Children.Keys {

    static int PROJECT = 1, SESSION = 2;

    private NodeListener nodeL;
    private Node project, session;

    /** Creates new ProjectSettingsNode. */
    public GlobalOptionsChildren() {
        project = TopManager.getDefault().getPlaces().nodes().project();
        session = TopManager.getDefault().getPlaces().nodes().session();
    }

    /** Initializes listening to changes in original node.
    */
    protected void addNotify () {
        // add itself to reflect to changes children of original node
        nodeL = new ChildrenAdapter (this);
        project.addNodeListener (nodeL);
        session.addNodeListener (nodeL);

        updateKeys ();
    }

    /** Closes the listener, if any, on the original node.
    */
    protected void finalize () {
        if (nodeL != null) {
            project.removeNodeListener (nodeL);
            session.removeNodeListener (nodeL);
        }
        nodeL = null;
    }

    protected Node[] createNodes(Object key) {
        OptionItem oi = (OptionItem) key;
        if (oi.type == PROJECT) {
            if (!ProjectSettingsChildren.isProjectNode(oi.node)) {
                return new Node[] {oi.node.cloneNode()};
            }
        } else {
            return new Node[] {oi.node.cloneNode()};
        }
        return new Node[] {};
    }


    /** Get project keys and session keys and connect it together.
    */
    protected void updateKeys() {
        LinkedList list = new LinkedList();

        Node[] nodes;

        nodes = session.getChildren().getNodes();
        for(int i = 0; i < nodes.length; i++) {
            list.add(new OptionItem(SESSION, nodes[i]));
        }
        nodes = project.getChildren().getNodes();
        for(int i = 0; i < nodes.length; i++) {
            list.add(new OptionItem(PROJECT, nodes[i]));
        }
        setKeys(list);
    }

    class OptionItem {

        public final int type;
        public final Node node;

        public OptionItem(int type, Node node) {
            this.type = type;
            this.node = node;
        }

        public boolean equals(Object o) {
            if (o instanceof OptionItem) {
                OptionItem oi = (OptionItem) o;
                return (oi.type == type) && (oi.node.equals(node));
            }
            return false;
        }

        public int hashCode() {
            return node.hashCode();
        }
    }

    // -- Inner classes. --

    private static class ChildrenAdapter extends Object
        implements NodeListener {
        /** children object to notify about addition of children.
        * Can be null. Set from Children's initNodes method.
        */
        private WeakReference children;

        /** Create a new adapter.
        * @param ch the children list
        */
        public ChildrenAdapter (Children ch) {
            this.children = new WeakReference (ch);
        }

        /** Does nothing.
        * @param ev the event
        */
        public void propertyChange (PropertyChangeEvent ev) {
        }

        /* Informs that a set of new children has been added.
        * @param ev event describing the action
        */
        public void childrenAdded (NodeMemberEvent ev) {
            GlobalOptionsChildren children = (GlobalOptionsChildren)this.children.get ();
            if (children == null) return;

            children.updateKeys();
        }

        /* Informs that a set of children has been removed.
        * @param ev event describing the action
        */
        public void childrenRemoved (NodeMemberEvent ev) {
            GlobalOptionsChildren children = (GlobalOptionsChildren)this.children.get ();
            if (children == null) return;

            children.updateKeys();
        }

        /* Informs that a set of children has been reordered.
        * @param ev event describing the action
        */
        public void childrenReordered (NodeReorderEvent ev) {
            GlobalOptionsChildren children = (GlobalOptionsChildren)this.children.get ();
            if (children == null) return;

            children.updateKeys();
        }


        /** Does nothing.
        * @param ev the event
        */
        public void nodeDestroyed (NodeEvent ev) {
        }
    }

}

/*
* Log
*  2    Gandalf   1.1         1/4/00   Martin Ryzl     
*  1    Gandalf   1.0         1/3/00   Martin Ryzl     
* $ 
*/ 
