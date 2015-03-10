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
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.openide.explorer.*;
import org.openide.nodes.Node;

/** Context tree view class.
* @author   Petr Hamernik
*/
public class ContextTreeView extends TreeView {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8282594827988436813L;

    /** Constructor.
    */
    public ContextTreeView() {
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    /* @return true if this TreeView accept the selected beans.
    */
    protected boolean selectionAccept(Node[] nodes) {
        if (nodes.length == 0) return true;

        Node parent = nodes[0].getParentNode ();
        for (int i = 1; i < nodes.length; i++) {
            if (nodes[i].getParentNode () != parent) {
                return false;
            }
        }
        return true;
    }


    /* Called whenever the value of the selection changes.
    * @param listSelectionEvent the event that characterizes the change.
    */
    protected void selectionChanged(Node[] nodes, ExplorerManager man)
    throws PropertyVetoException {
        if (nodes.length > 0) {
            man.setExploredContext (nodes[0]);
        }
    }

    /** Expand the given path and makes it visible.
    * @param path the path
    */
    protected void showPath(TreePath path) {
        tree.makeVisible(path);
        Rectangle rect = tree.getPathBounds(path);
        if (rect != null) {
            rect.width += rect.x;
            rect.x = 0;
            tree.scrollRectToVisible(rect);
        }
        tree.setSelectionPath(path);
    }

    /** Shows selection to reflect the current state of the selection in the explorer.
    *
    * @param paths array of paths that should be selected
    */
    protected void showSelection (TreePath[] paths) {
        if (paths.length == 0) {
            tree.setSelectionPaths (new TreePath[0]);
        } else {
            tree.setSelectionPath (paths[0].getParentPath ());
        }
    }

    /** Permit use of explored contexts.
    *
    * @return <code>true</code> always
    */
    protected boolean useExploredContextMenu() {
        return true;
    }

    /** Create model.
    */
    protected NodeTreeModel createModel () {
        return new NodeTreeModel ();
    }
}

/*
 * Log
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/27/99  Jaroslav Tulach New threading model & 
 *       Children.
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         3/22/99  Jesse Glick     [JavaDoc] & made 
 *       ExploreTreeModel & its subclasses public.
 *  2    Gandalf   1.1         3/20/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.40        --/--/98 Jan Formanek    SWITCHED TO NODES
 *  0    Tuborg    0.41        --/--/98 Petr Hamernik   bugfix
 *  0    Tuborg    0.50        --/--/98 Jan Formanek    reflecting changes in explorer model
 *  0    Tuborg    0.51        --/--/98 Petr Hamernik   bugfix- useExploredContextMenu() added
 *  0    Tuborg    0.52        --/--/98 Petr Hamernik   bugfix
 *  0    Tuborg    0.53        --/--/98 Petr Hamernik   scrolling bugfix
 */
