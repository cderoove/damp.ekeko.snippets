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
import org.openide.util.*;
import org.openide.nodes.Node;

/** Functioning tree view class.
*
* @author   Petr Hamernik, Ian Formanek
* @version  1.00, Aug 14, 1998
*/
public class BeanTreeView extends TreeView {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 3841322840231536380L;

    /** Constructor.
    */
    public BeanTreeView() {
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    }

    /** Create a new model.
    * The default implementation creates a {@link BeanExploreTreeModel}.
    * @return the model
    */
    protected NodeTreeModel createModel() {
        return new NodeTreeModel ();
    }

    /** Can select any nodes.
    */
    protected boolean selectionAccept (Node[] nodes) {
        return true;
    }


    /* Synchronizes selected nodes from the manager of this Explorer.
    */
    protected void showSelection (TreePath[] treePaths) {
        tree.getSelectionModel().setSelectionPaths(treePaths);
        if (treePaths.length == 1)
            showPathWithoutExpansion (treePaths[0]);
    }

    /* Called whenever the value of the selection changes.
    * @param nodes nodes
    * @param em explorer manager
    */
    public void selectionChanged(Node[] nodes, ExplorerManager em) throws PropertyVetoException {

NO_CONTEXT_CHANGE: {
            if (nodes.length > 0) {
                Node context = nodes[0].getParentNode ();
                for (int i = 1; i < nodes.length; i++) {
                    if (context != nodes[i].getParentNode ()) {
                        break NO_CONTEXT_CHANGE;
                    }
                }

                em.setExploredContext (context);
            }
        }

        em.setSelectedNodes (nodes);

        /*
            if (tree.isSelectionEmpty()) {
              try {
                manager.setSelectedNodes(new Node[0]);
              }
              catch (PropertyVetoException e) {
              }
            }
            else {
              TreePath[] treePaths = tree.getSelectionPaths();
        //      debugPath(treePaths[0], 1);
              Node[] contexts = TreeViewUtil.treeToContext(treePaths);
              Node exploredContextBackup = null;
              try {
                if (TreeViewUtil.haveSameParent(contexts)) {
                  Node expl = contexts[0].getParentNode();
                  if ((expl == null) && (!contexts[0].isLeaf()))
                    expl = contexts[0];
                  if (expl != null) {
                    scrollAllowed = false;
                    manager.setExploredContext(expl);
                    manager.setSelectedNodes(contexts);
                    scrollAllowed = true;
                  }
                }
                else {
                  manager.setSelectedNodes(contexts);
                }
              }
              catch (PropertyVetoException e) {
                if (exploredContextBackup != null) {
                  scrollAllowed = false;
                  manager.setExploredContext(exploredContextBackup);
                  scrollAllowed = true;
                }
                contexts = manager.getSelectedNodes();
                treePaths = TreeViewUtil.contextToTree(contexts, manager.getRootContext());
                tree.getSelectionModel().setSelectionPaths(treePaths);
              }
              }
        */
    }

    /*
    void debugPath(TreePath p, int j) {
      System.out.println("Debug Path "+j+":");
      Object[] o = p.getPath();
      for (int i = 0; i < o.length; i++) {
        System.out.println("   "+i+o[i].getClass()+"..."+System.identityHashCode(o[i]));
        Node par = ((Node)o[i]).getParentNode();
        StringBuffer buf = new StringBuffer("   ...parent:");
        if (par != null) {
          buf.append(par.getClass());
          buf.append("/");
          buf.append(System.identityHashCode(par));
        }
        else {
          buf.append(" neni");
        }
        System.out.println(buf.toString());
      }
} */

    /** Expand the given path and makes it visible.
    * @param path the path
    */
    protected void showPath (TreePath path) {
        tree.expandPath(path);
        showPathWithoutExpansion (path);
    }

    /** Make a path visible.
    * @param path the path
    */
    private void showPathWithoutExpansion (TreePath path) {
        Rectangle rect = tree.getPathBounds(path);
        if (rect != null) { //PENDING
            rect.width += rect.x;
            rect.x = 0;
            tree.scrollRectToVisible(rect);
        }
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
 *  0    Tuborg    0.38        --/--/98 Jan Formanek    changed the order of actions on selection change
 *  0    Tuborg    0.40        --/--/98 Jan Formanek    SWITCHED TO NODES
 *  0    Tuborg    0.50        --/--/98 Jan Formanek    reflecting changes in explorer model
 *  0    Tuborg    0.51        --/--/98 Jan Formanek    bugfix - refreshing in the end of initialize()
 *  0    Tuborg    0.51        --/--/98 Jan Formanek    synchronizeXXXContext methods modified
 *  0    Tuborg    0.52        --/--/98 Petr Hamernik   bugfix
 *  0    Tuborg    0.53        --/--/98 Petr Hamernik   bugfix
 *  0    Tuborg    0.54        --/--/98 Petr Hamernik   scrolling bugfix
 */
