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
import java.awt.dnd.DnDConstants;
import java.beans.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.openide.awt.MouseUtils;
import org.openide.explorer.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.nodes.Children;


/** Tree view abstract class.
*
* @author   Petr Hamernik, Ian Formanek, Jaroslav Tulach
*/
public abstract class TreeView extends JScrollPane {
    //
    // static fields
    //


    /** generated Serialized Version UID */
    static final long serialVersionUID = -1639001987693376168L;

    /** How long it takes before collapsed nodes are released from the tree's cache
    */
    private static final int TIME_TO_COLLAPSE = System.getProperty ("netbeans.debug.heap") != null ? 0 : 15000;


    /** Minimum width of this component. */
    private static final int MIN_TREEVIEW_WIDTH = 400;

    /** Minimum height of this component. */
    private static final int MIN_TREEVIEW_HEIGHT = 400;

    //
    // components
    //

    /** Main <code>JTree</code> component. */
    transient protected JTree tree;

    /** model */
    transient private NodeTreeModel treeModel;




    /** Explorer manager, valid when this view is showing */
    transient private ExplorerManager manager;


    // Attributes

    /** not null if default action on nodes allowed */
    transient ClickAdapter defaultActionListener;

    /** not null if popup menu enabled */
    transient PopupAdapter popupListener;




    /** the most important listener (on four types of events */
    transient TreePropertyListener managerListener = null;
    /** weak variation of the listener for property change on the explorer manager */
    transient PropertyChangeListener wlpc;
    /** weak variation of the listener for vetoable change on the explorer manager */
    transient VetoableChangeListener wlvc;


    /** true if drag support is active */
    transient boolean dragActive = false;
    /** true if drop support is active */
    transient boolean dropActive = false;
    /** Drag support */
    transient TreeViewDragSupport dragSupport;
    /** Drop support */
    transient TreeViewDropSupport dropSupport;



    /** Constructor.
    */
    public TreeView () {
        this (true, true);
    }

    /** Constructor.
    * @param defaultAction should double click on a node open its default action?
    * @param popupAllowed should right-click open popup?
    */
    public TreeView (boolean defaultAction, boolean popupAllowed) {
        initializeTree ();

        // hack - DnD not stable now...
        // setDragSource(true);
        // setDropTarget(true);

        setPopupAllowed (defaultAction);
        setDefaultActionAllowed (popupAllowed);
    }


    /** Initializes the tree & model.
    */
    private void initializeTree () {
        // initilizes the JTree
        treeModel = createModel ();
        tree = new AutoscrollJTree(treeModel) {
                   public void updateUI () {
                       setCellRenderer(new NodeRenderer ());
                       super.updateUI ();
                   }
               };
        setViewportView (tree);

        NodeRenderer rend = NodeRenderer.sharedInstance ();
        tree.setCellRenderer(rend);
        tree.setCellEditor(new TreeViewCellEditor(tree, new NodeRenderer.Tree ()));
        tree.putClientProperty("JTree.lineStyle", "Angled"); // NOI18N
        tree.setEditable(true);

        ToolTipManager.sharedInstance().registerComponent(tree);


        // init listener & attach it to closing of
        managerListener = new TreePropertyListener();
        tree.addTreeExpansionListener (managerListener);

        // do not care about focus
        setRequestFocusEnabled (false);
    }


    /** Is it permitted to display a popup menu?
     * @return <code>true</code> if so
     */
    public boolean isPopupAllowed () {
        return popupListener != null;
    }

    /** Enable/disable displaying popup menus on tree view items.
    * Default is enabled.
    * @param value <code>true</code> to enable
    */
    public void setPopupAllowed (boolean value) {
        if (popupListener == null && value) {
            // on
            popupListener = new PopupAdapter ();
            tree.addMouseListener (popupListener);
            return;
        }
        if (popupListener != null && !value) {
            // off
            tree.removeMouseListener (popupListener);
            popupListener = null;
            return;
        }
    }

    /** Does a double click invoke the default node action?
    * @return <code>true</code> if so
    */
    public boolean isDefaultActionEnabled () {
        return defaultActionListener != null;
    }

    /** Also requests focus for the tree component */
    public void requestFocus () {
        super.requestFocus();
        tree.requestFocus();
    }

    /** Enable/disable double click to invoke default action.
    * @param value <code>true</code> to enable
    */
    public void setDefaultActionAllowed (boolean value) {
        if (defaultActionListener == null && value) {
            // on
            defaultActionListener = new ClickAdapter ();
            tree.addMouseListener (defaultActionListener);
            tree.registerKeyboardAction(
                defaultActionListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
                JComponent.WHEN_FOCUSED
            );
            return;
        }
        if (defaultActionListener != null && !value) {
            // off
            tree.removeMouseListener (defaultActionListener);
            tree.unregisterKeyboardAction(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false)
            );
            defaultActionListener = null;
            return;
        }

    }

    /**
    * Is the root node of the tree displayed?
    *
    * @return <code>true</code> if so
    */
    public boolean isRootVisible() {
        return tree.isRootVisible();
    }

    /** Set whether or not the root node from
    * the <code>TreeModel</code> is visible.
    *
    * @param rootVisible <code>true</code> if it is to be displayed
    * @beaninfo
    *        bound: true
    *  description: Whether or not the root node
    *               from the TreeModel is visible.
    */
    public void setRootVisible (boolean visible) {
        tree.setRootVisible (visible);
    }


    /********** Support for the Drag & Drop operations *********/

    /** @return true if dragging from the view is enabled, false
    * otherwise.<br>
    * Drag support is disabled by default.
    */
    public boolean isDragSource () {
        return dragActive;
    }

    /** Enables/disables dragging support.
    * @param state true enables dragging support, false disables it.
    */
    public void setDragSource (boolean state) {
        if (state == dragActive)
            return;
        dragActive = state;
        // create drag support if needed
        if (dragActive && (dragSupport == null))
            dragSupport = new TreeViewDragSupport(this, tree);
        // activate / deactivate support according to the state
        dragSupport.activate(dragActive);
    }

    /** @return true if dropping to the view is enabled, false
    * otherwise<br>
    * Drop support is disabled by default.
    */
    public boolean isDropTarget () {
        return dropActive;
    }

    /** Enables/disables dropping support.
    * @param state true means drops into view are allowed,
    * false forbids any drops into this view.
    */
    public void setDropTarget (boolean state) {
        if (state == dropActive)
            return;
        dropActive = state;
        // create drop support if needed
        if (dropActive && (dropSupport == null))
            dropSupport = new TreeViewDropSupport(this, tree);
        // activate / deactivate support according to the state
        dropSupport.activate(dropActive);
    }

    /** @return Set of actions which are allowed when dragging from
    * asociated component.
    * Actions constants comes from DnDConstants.XXX constants.
    * All actions (copy, move, link) are allowed by default.
    */
    public int getAllowedDragActions () {
        // PENDING
        return DnDConstants.ACTION_MOVE | DnDConstants.ACTION_COPY |
               DnDConstants.ACTION_LINK;
    }

    /** Sets allowed actions for dragging
    * @param actions new drag actions, using DnDConstants.XXX 
    */  
    public void setAllowedDragActions (int actions) {
        // PENDING
    }

    /** @return Set of actions which are allowed when dropping
    * into the asociated component.
    * Actions constants comes from DnDConstants.XXX constants.
    * All actions are allowed by default.
    */
    public int getAllowedDropActions () {
        // PENDING
        return DnDConstants.ACTION_MOVE | DnDConstants.ACTION_COPY |
               DnDConstants.ACTION_LINK;
    }

    /** Sets allowed actions for dropping.
    * @param actions new allowed drop actions, using DnDConstants.XXX 
    */  
    public void setAllowedDropActions (int actions) {
        // PENDING
    }





    //
    // Processing functions
    //

    /* Initializes the component.
    */
    public void addNotify () {
        super.addNotify ();

        // Enter key in the tree


        ExplorerManager newManager = ExplorerManager.find (TreeView.this);

        if (newManager != manager) {
            if (manager != null) {
                manager.removeVetoableChangeListener (wlvc);
                manager.removePropertyChangeListener (wlpc);
            }

            manager = newManager;

            manager.addVetoableChangeListener(wlvc = WeakListener.vetoableChange (managerListener, manager));
            manager.addPropertyChangeListener(wlpc = WeakListener.propertyChange (managerListener, manager));

            synchronizeRootContext ();
            synchronizeExploredContext ();
            synchronizeSelectedNodes ();
        }

        tree.getSelectionModel().addTreeSelectionListener(managerListener);
    }

    /* Deinitializes listeners.
    */
    public void removeNotify () {
        super.removeNotify ();

        //System.out.println ("Calling remove notify..."); // NOI18N
        tree.getSelectionModel().removeTreeSelectionListener(managerListener);
    }

    /* Defines new way how to compute preffered size
    */
    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        if (dim.width < MIN_TREEVIEW_WIDTH)
            dim.width = MIN_TREEVIEW_WIDTH;
        if (dim.height < MIN_TREEVIEW_HEIGHT)
            dim.height = MIN_TREEVIEW_HEIGHT;
        return dim;
    }

    // *************************************
    // Methods to be overriden by subclasses
    // *************************************


    /** Allows subclasses to provide own model for displaying nodes.
    * @return the model to use for this view
    */
    protected abstract NodeTreeModel createModel();


    /** Called to allow subclasses to define the behaviour when a
    * node(s) are selected in the tree.
    *
    * @param nodes the selected nodes
    * @param em explorer manager to work on (change nodes to it)
    * @throws PropertyVetoException if the change cannot be done by the explorer
    *    (the exception is silently consumed)
    */
    protected abstract void selectionChanged (Node[] nodes, ExplorerManager em) throws PropertyVetoException;

    /** Called when explorer manager is about to change the current selection.
    * The view can forbid the change if it is not able to display such
    * selection.
    *
    * @param nodes the nodes to select
    * @return false if the view is not able to change the selection
    */
    protected abstract boolean selectionAccept (Node[] nodes);


    /** Show a given path in the screen. It depends on the kind of <code>TreeView</code>
    * if the path should be expanded or just made visible.
    *
    * @param path the path
    */
    protected abstract void showPath(TreePath path);

    /** Shows selection to reflect the current state of the selection in the explorer.
    *
    * @param paths array of paths that should be selected
    */
    protected abstract void showSelection (TreePath[] paths);


    /** Should a context menu of the explored context be used?
    * Applicable when no nodes are selected and the user wants to invoke
    * a context menu (clicks right mouse button).
    *
    * @return <code>true</code> if so; <code>false</code> in the default implementation
    */
    protected boolean useExploredContextMenu() {
        return false;
    }


    //
    // synchronizations
    //

    /** Called when selection in tree is changed.
    */
    final void callSelectionChanged (Node[] nodes) {
        manager.removePropertyChangeListener (wlpc);
        manager.removeVetoableChangeListener (wlvc);
        try {
            selectionChanged (nodes, manager);
        } catch (PropertyVetoException e) {
            synchronizeSelectedNodes ();
        } finally {
            manager.addPropertyChangeListener (wlpc);
            manager.addVetoableChangeListener (wlvc);
        }
    }


    /** Synchronize the root context from the manager of this Explorer.
    */
    final void synchronizeRootContext() {
        treeModel.setNode (manager.getRootContext ());
    }

    /** Synchronize the explored context from the manager of this Explorer.
    */
    final void synchronizeExploredContext() {
        TreePath treePath = new TreePath (treeModel.getPathToRoot (VisualizerNode.getVisualizer (null, manager.getExploredContext ())));
        showPath(treePath);
    }

    /** Synchronize the selected nodes from the manager of this Explorer.
    * The default implementation does nothing.
    */
    final void synchronizeSelectedNodes() {
        Node[] arr = manager.getSelectedNodes ();
        TreePath[] paths = new TreePath[arr.length];

        for (int i = 0; i < arr.length; i++) {
            TreePath treePath = new TreePath (treeModel.getPathToRoot (VisualizerNode.getVisualizer (null, arr[i])));
            paths[i] = treePath;
        }

        tree.getSelectionModel().removeTreeSelectionListener(managerListener);
        showSelection (paths);
        tree.getSelectionModel().addTreeSelectionListener(managerListener);
    }


    /** Expands all paths.
    */
    public void expandAll () {
        int i = 0, j, k = tree.getRowCount ();
        do {
            do {
                j = tree.getRowCount ();
                tree.expandRow (i);
            } while (j != tree.getRowCount ());
            i++;
        } while (i < tree.getRowCount ());
    }


    /** Listens to the property changes on tree */
    private class TreePropertyListener implements VetoableChangeListener,
                PropertyChangeListener,
                TreeExpansionListener,
                TreeSelectionListener
    {
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                if (!selectionAccept ((Node[])evt.getNewValue ())) {
                    throw new PropertyVetoException ("", evt); // NOI18N
                }
            }
        }

        public final void propertyChange(PropertyChangeEvent evt) {
            if (manager == null) return; // the tree view has been removed before the event got delivered
            if (evt.getPropertyName().equals(ExplorerManager.PROP_ROOT_CONTEXT))
                synchronizeRootContext();
            if (evt.getPropertyName().equals(ExplorerManager.PROP_EXPLORED_CONTEXT))
                synchronizeExploredContext();
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES))
                synchronizeSelectedNodes();
        }

        public void treeExpanded (TreeExpansionEvent ev) {
        }

        public void treeCollapsed (final TreeExpansionEvent ev) {
            RequestProcessor.postRequest (new Runnable () {
                                              public void run () {
                                                  SwingUtilities.invokeLater (new Runnable () {
                                                                                  public void run () {
                                                                                      boolean open = tree.isExpanded (ev.getPath ());
                                                                                      NodeTreeModel t = treeModel;
                                                                                      if (!open && t != null) {
                                                                                          //System.out.println(" collapsing  " + ev.getPath () + ":" + tree.isCollapsed( ev.getPath () ) ); // NOI18N
                                                                                          t.nodeStructureChanged( (TreeNode) ev.getPath().getLastPathComponent() );
                                                                                          //System.out.println(" AND NOW  " + ev.getPath () + ":" + tree.isCollapsed( ev.getPath () ) ); // NOI18N
                                                                                      }
                                                                                  }
                                                                              });
                                              }
                                          }, TIME_TO_COLLAPSE);
        }


        /* Called whenever the value of the selection changes.
        * @param ev the event that characterizes the change.
        */
        public void valueChanged(TreeSelectionEvent ev) {
            LinkedList ll = new LinkedList ();

            TreePath[] paths = tree.getSelectionPaths ();
            if (paths == null) {
                callSelectionChanged (new Node[0]);
            } else {
                for (int i = 0; i < paths.length; i++) {
                    ll.add (Visualizer.findNode (paths[i].getLastPathComponent ()));
                }

                callSelectionChanged ((Node[])ll.toArray (new Node[ll.size ()]));
            }
        }

    } // end of TreePropertyListener


    /** Popup adapter.
    */
    private class PopupAdapter extends MouseUtils.PopupMouseAdapter {
        protected void showPopup (MouseEvent e) {
            int selRow = tree.getRowForLocation(e.getX(), e.getY());

            if (!tree.isRowSelected(selRow)) {
                tree.setSelectionRow(selRow);
            }

            if (selRow != -1) {
                JPopupMenu popup = NodeOp.findContextMenu(manager.getSelectedNodes());
                if ((popup != null) && (popup.getSubElements().length > 0)) {
                    java.awt.Point p = getViewport().getViewPosition();
                    p.x = e.getX() - p.x;
                    p.y = e.getY() - p.y;
                    popup.show(TreeView.this, p.x, p.y);
                }
            }
        }
    }


    /** clicking adapter
    */
    private class ClickAdapter extends MouseAdapter implements ActionListener {
        public void mouseClicked(MouseEvent e) {
            int selRow = tree.getRowForLocation(e.getX(), e.getY());

            //Default action
            if ((selRow != -1) && SwingUtilities.isLeftMouseButton(e)) {

                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                Node node = Visualizer.findNode (selPath.getLastPathComponent());

                if (defaultActionListener != null && MouseUtils.isDoubleClick(e)) {
                    //System.out.println ("Double clicked???"); // NOI18N
                    SystemAction sa = node.getDefaultAction ();
                    if (sa != null) {
                        sa.actionPerformed (new ActionEvent (
                                                node, ActionEvent.ACTION_PERFORMED, "" // NOI18N
                                            ));
                        e.consume ();
                    } else { // this is to be uncommented if we can force
                        // JTree not to expand / collapse on double click

                        Package p = Package.getPackage ("java.lang"); // NOI18N
                        if (p.isCompatibleWith("1.3")) { // NOI18N
                            if (tree.isExpanded(selRow))
                                tree.collapseRow(selRow);
                            else
                                tree.expandRow(selRow);
                        }
                    }
                }
            }
        }

        public void actionPerformed(ActionEvent evt) {
            Node[] nodes = manager.getSelectedNodes();
            if (nodes.length == 1) {
                SystemAction sa = nodes[0].getDefaultAction ();
                if (sa != null) {
                    sa.actionPerformed (new ActionEvent (
                                            nodes[0], ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                }
            }
        }

    }

}

/*
 * Log
 *  41   Gandalf   1.40        3/11/00  Martin Ryzl     menufix [by E.Adams, 
 *       I.Formanek]
 *  40   Gandalf   1.39        1/16/00  Ian Formanek    Fixed last change
 *  39   Gandalf   1.38        1/16/00  Ian Formanek    Removed semicolons after
 *       methods body to prevent fastjavac from complaining
 *  38   Gandalf   1.37        1/13/00  Ian Formanek    NOI18N
 *  37   Gandalf   1.36        1/12/00  Ian Formanek    NOI18N
 *  36   Gandalf   1.35        1/7/00   Jaroslav Tulach #5160, but works 
 *       correctly only on JDK1.3 on JDK1.2 does both, expands the node and also
 *       starts default action. alas.
 *  35   Gandalf   1.34        12/9/99  Jaroslav Tulach Double-click does only 
 *       one action (expand/invoke default).
 *  34   Gandalf   1.33        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  33   Gandalf   1.32        11/3/99  Ian Formanek    Fixed bug 4616 - 
 *       packages are automaticly renamed by name of last selected file
 *  32   Gandalf   1.31        10/29/99 Ian Formanek    Removed dumpStack
 *  31   Gandalf   1.30        10/28/99 Ian Formanek    Fixed bug #4603 - When 
 *       in-place renaming an item in tree, when the editing is finished by 
 *       clicking outside of the edit line, the item *should* be renamed to the 
 *       current text in the input line.
 *  30   Gandalf   1.29        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  29   Gandalf   1.28        10/7/99  David Simonek   focus transferrring 
 *       added
 *  28   Gandalf   1.27        10/6/99  Petr Hamernik   roll-back last change
 *  27   Gandalf   1.26        10/5/99  Petr Hamernik   large model is set - 
 *       then FixedHeightCellCache is used instead of Variable... . (memory leak
 *       prevention)
 *  26   Gandalf   1.25        9/24/99  Petr Hamernik   fixed bug #3486
 *  25   Gandalf   1.24        9/16/99  Petr Hrebejk    Collapsing fixed + -D 
 *       netbeans.debug.heap for immediate tree collapsing added
 *  24   Gandalf   1.23        8/27/99  Jaroslav Tulach New threading model & 
 *       Children.
 *  23   Gandalf   1.22        7/16/99  Ian Formanek    Fixed possible problems 
 *       with timing of closing/event listening
 *  22   Gandalf   1.21        7/16/99  Ales Novak      new win sys
 *  21   Gandalf   1.20        6/28/99  Ian Formanek    Fixed bug 2043 - It is 
 *       virtually impossible to choose lower items of New From Template  from 
 *       popup menu on 1024x768
 *  20   Gandalf   1.19        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  19   Gandalf   1.18        5/26/99  Ian Formanek    Fixed ST problems
 *  18   Gandalf   1.17        5/17/99  Ian Formanek    Fixed last change
 *  17   Gandalf   1.16        5/17/99  David Simonek   DnD switched off :-(
 *  16   Gandalf   1.15        5/11/99  David Simonek   addNotify now run under 
 *       Children.Mutex
 *  15   Gandalf   1.14        4/27/99  David Simonek   autoscroll support and 
 *       visual feedback in DnD operations added
 *  14   Gandalf   1.13        4/21/99  David Simonek   modified to support DnD
 *  13   Gandalf   1.12        4/16/99  Jan Jancura     Object Browser support
 *  12   Gandalf   1.11        4/9/99   Ian Formanek    Removed debug printlns
 *  11   Gandalf   1.10        4/1/99   David Simonek   double click bug fixed
 *  10   Gandalf   1.9         3/22/99  David Simonek   deregistering mouse 
 *       listeners
 *  9    Gandalf   1.8         3/20/99  Jesse Glick     [JavaDoc]
 *  8    Gandalf   1.7         3/18/99  Petr Hamernik   
 *  7    Gandalf   1.6         3/16/99  Petr Hamernik   tooltip improvement
 *  6    Gandalf   1.5         3/15/99  Petr Hamernik   
 *  5    Gandalf   1.4         3/4/99   Jan Jancura     Localization moved
 *  4    Gandalf   1.3         2/11/99  Jaroslav Tulach SystemAction is 
 *       javax.swing.Action
 *  3    Gandalf   1.2         1/6/99   Jaroslav Tulach 
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
