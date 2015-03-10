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

import java.awt.event.*;
import java.awt.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.awt.dnd.DnDConstants;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openide.awt.MouseUtils;
import org.openide.explorer.*;
import org.openide.util.WeakListener;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.nodes.*;
import org.openide.util.Utilities;



/** Explorer view to display items in a list.
* @author   Ian Formanek, Jan Jancura, Jaroslav Tulach
*/
public class ListView extends JScrollPane implements Externalizable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7540940974042262975L;

    /** Explorer manager to work with. Is not null only if the component is showing
    * in components hierarchy
    */
    private transient ExplorerManager manager;

    /** The actual JList list */
    transient protected JList list;
    /** model to use */
    transient protected NodeListModel model;


    //
    // listeners
    //

    /** Listener to nearly everything */
    transient Listener managerListener;

    /** weak variation of the listener for property change on the explorer manager */
    transient PropertyChangeListener wlpc;
    /** weak variation of the listener for vetoable change on the explorer manager */
    transient VetoableChangeListener wlvc;

    /** popup */
    transient PopupAdapter popupListener;

    //
    // properties
    //

    /** if true, the icon view displays a popup on right mouse click, if false, the popup is not displayed */
    private boolean popupAllowed = true;
    /** if true, the hierarchy traversal is allowed, if false, it is disabled */
    private boolean traversalAllowed = true;

    /** action preformer */
    private ActionListener defaultProcessor;

    //
    // Dnd
    //

    /** true if drag support is active */
    transient boolean dragActive = false;
    /** true if drop support is active */
    transient boolean dropActive = false;
    /** Drag support */
    transient ListViewDragSupport dragSupport;
    /** Drop support */
    transient ListViewDropSupport dropSupport;




    // init .................................................................................

    /** Default constructor.
    */
    public ListView() {
        initializeList ();

        // DnD not stable now...
        //setDragSource(true);
        //setDropTarget(true);
    }

    /** Initializes the tree & model.
    */
    private void initializeList () {
        // initilizes the JTree
        model = createModel ();
        list = createList ();
        list.setModel (model);

        setViewportView (list);

        {
            AbstractAction action = new GoUpAction ();
            KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
            list.registerKeyboardAction(action, key, JComponent.WHEN_FOCUSED);
        }

        {
            AbstractAction action = new EnterAction ();
            KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
            list.registerKeyboardAction(action, key, JComponent.WHEN_FOCUSED);
        }

        managerListener = new Listener ();
        popupListener = new PopupAdapter ();


        list.addMouseListener(managerListener);
        list.addMouseListener(popupListener);
        list.getSelectionModel().setSelectionMode(
            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        );

        ToolTipManager.sharedInstance ().registerComponent (list);
    }


    /*
    * Write view's state to output stream.
    */
    public void writeExternal (ObjectOutput out) throws IOException {
        out.writeObject (new Boolean (popupAllowed));
        out.writeObject (new Boolean (traversalAllowed));
        out.writeObject (new Integer (getSelectionMode ()));
    }

    /*
    * Reads view's state form output stream.
    */
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        popupAllowed = ((Boolean)in.readObject ()).booleanValue ();
        traversalAllowed = ((Boolean)in.readObject ()).booleanValue ();
        setSelectionMode (((Integer)in.readObject ()).intValue ());
    }


    // properties ...........................................................................

    /** Test whether display of a popup menu is enabled.
     * @return <code>true</code> if so */
    public boolean isPopupAllowed () {
        return popupAllowed;
    }

    /** Enable/disable displaying popup menus on list view items. Default is enabled.
    * @param value <code>true</code> to enable
    */
    public void setPopupAllowed (boolean value) {
        popupAllowed = value;
    }

    /** Test whether hierarchy traversal shortcuts are permitted.
    * @return <code>true</code> if so */
    public boolean isTraversalAllowed () {
        return traversalAllowed;
    }

    /** Enable/disable hierarchy traversal using <code>CTRL+click</code> (down) and <code>Backspace</code> (up), default is enabled.
    * @param value <code>true</code> to enable
    */
    public void setTraversalAllowed (boolean value) {
        traversalAllowed = value;
    }

    /** Get the current processor for default actions.
    * If not <code>null</code>, double-clicks or pressing Enter on 
    * items in the view will not perform the default action on the selected node; rather the processor 
    * will be notified about the event.
    * @return the current default-action processor, or <code>null</code>
    */
    public ActionListener getDefaultProcessor () {
        return defaultProcessor;
    }

    /** Set a new processor for default actions.
    * @param value the new default-action processor, or <code>null</code> to restore use of the selected node's declared default action
    * @see #getDefaultProcessor
    */
    public void setDefaultProcessor (ActionListener value) {
        defaultProcessor = value;
    }

    /**
     * Set whether single-item or multiple-item
     * selections are allowed.
     * @param selectionMode one of {@link ListSelectionModel#SINGLE_SELECTION}, {@link ListSelectionModel#SINGLE_INTERVAL_SELECTION}, or  {@link ListSelectionModel#MULTIPLE_INTERVAL_SELECTION}
     * @see ListSelectionModel#setSelectionMode
     * @beaninfo
     * description: The selection mode.
     *        enum: SINGLE_SELECTION            ListSelectionModel.SINGLE_SELECTION
     *              SINGLE_INTERVAL_SELECTION   ListSelectionModel.SINGLE_INTERVAL_SELECTION
     *              MULTIPLE_INTERVAL_SELECTION ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
     */
    public void setSelectionMode(int selectionMode) {
        list.setSelectionMode(selectionMode);
    }

    /** Get the selection mode.
     * @return the mode
     * @see #setSelectionMode
     */
    public int getSelectionMode() {
        return list.getSelectionMode();
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
            dragSupport = new ListViewDragSupport(this, list);
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
            dropSupport = new ListViewDropSupport(this, list);
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
    // Methods to override
    //

    /** Creates the list that will display the data.
    */
    protected JList createList () {
        JList list = new NbList ();
        list.setCellRenderer(NodeRenderer.sharedInstance ());
        return list;
    }

    /** Allows subclasses to change the default model used for
    * the list.
    */
    protected NodeListModel createModel () {
        return new NodeListModel ();
    }

    /** Called when the list changed selection and the explorer manager
    * should be updated.
    * @param nodes list of nodes that should be selected
    * @param em explorer manager
    * @exception PropertyVetoException if the manager does not allow the
    *   selection
    */
    protected void selectionChanged (Node[] nodes, ExplorerManager em)
    throws PropertyVetoException {
        em.setSelectedNodes (nodes);
    }

    /** Called when explorer manager is about to change the current selection.
    * The view can forbid the change if it is not able to display such
    * selection.
    *
    * @param nodes the nodes to select
    * @return false if the view is not able to change the selection
    */
    protected boolean selectionAccept (Node[] nodes) {
        // if the selection is just the root context, confirm the selection
        if (nodes.length == 1 && manager.getRootContext().equals(nodes[0])) {
            return true;
        }

        Node cntx = manager.getExploredContext ();

        // we do not allow selection in other than the exploredContext
        for (int i = 0; i < nodes.length; i++) {
            VisualizerNode v = VisualizerNode.getVisualizer (null, nodes[i]);
            if (model.getIndex (v) == -1) {
                return false;
            }
        }

        return true;
    }

    /** Shows selection.
    * @param indexes indexes of objects to select
    */
    protected void showSelection (int[] indexes) {
        list.setSelectedIndices (indexes);
    }

    //
    // Working methods
    //


    /* Initilizes the view.
    */
    public void addNotify () {
        super.addNotify ();
        // run under mutex

        ExplorerManager em = ExplorerManager.find (this);

        if (em != manager) {
            if (manager != null) {
                manager.removeVetoableChangeListener (wlvc);
                manager.removePropertyChangeListener (wlpc);
            }

            manager = em;

            manager.addVetoableChangeListener(wlvc = WeakListener.vetoableChange (managerListener, manager));
            manager.addPropertyChangeListener(wlpc = WeakListener.propertyChange (managerListener, manager));

            model.setNode (manager.getExploredContext ());
            updateSelection();
        };

        list.getSelectionModel ().addListSelectionListener (managerListener);
    }

    /** Removes listeners.
    */
    public void removeNotify () {
        super.removeNotify ();
        list.getSelectionModel ().removeListSelectionListener (managerListener);
    }

    /** This method is called when user double-clicks on some object or
    * presses Enter key.
    * @param index Index of object in current explored context
    */
    final void performObjectAt(int index, int modifiers) {
        if (index < 0 || index >= model.getSize ()) {
            return;
        }

        VisualizerNode v = (VisualizerNode)model.getElementAt (index);
        Node node = v.node;

        // if DefaultProcessor is set, the default action is notified to it overriding the default action on nodes
        if (defaultProcessor != null) {
            defaultProcessor.actionPerformed (new ActionEvent (node, 0, null, modifiers));
            return;
        }

        // on double click - invoke default action, if there is any
        // (unless user holds CTRL key what means that we should always dive into the context)
        SystemAction sa = node.getDefaultAction ();
        if (sa != null && (modifiers & java.awt.event.InputEvent.CTRL_MASK) == 0) {
            sa.actionPerformed (new ActionEvent (node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
        }
        // otherwise dive into the context
        else if (traversalAllowed && (!node.isLeaf()))
            manager.setExploredContext (node);
    }

    /** Called when selection has been changed.
    */
    private void updateSelection() {
        Node[] sel = manager.getSelectedNodes ();
        int[] indices = new int[sel.length];

        for (int i = 0; i < sel.length; i++) {
            VisualizerNode v = VisualizerNode.getVisualizer (null, sel[i]);
            indices[i] = model.getIndex (v);
        }

        manager.removePropertyChangeListener (wlpc);
        manager.removeVetoableChangeListener (wlvc);
        try {
            showSelection (indices);
        } finally {
            manager.addPropertyChangeListener (wlpc);
            manager.addVetoableChangeListener (wlvc);
        }
    }


    // innerclasses .........................................................................

    /** Enhancement of standart JList.
    */
    final class NbList extends AutoscrollJList {
        static final long serialVersionUID =-7571829536335024077L;

        /**
         * Overrides JComponent's getToolTipText method in order to allow 
         * renderer's tips to be used if it has text set.
         * <p>
         * NOTE: For JTree to properly display tooltips of its renderers
         *       JTree must be a registered component with the ToolTipManager.
         *       This can be done by invoking
         *       <code>ToolTipManager.sharedInstance().registerComponent(tree)</code>.
         *       This is not done automaticly!
         *
         * @param event the MouseEvent that initiated the ToolTip display
         */
        public String getToolTipText (MouseEvent event) {
            if (event != null) {
                Point p = event.getPoint ();
                int row = locationToIndex (p);
                if (row >= 0) {
                    VisualizerNode v = (VisualizerNode)model.getElementAt (row);
                    String tooltip = v.shortDescription;
                    String displayName = v.displayName;
                    if ((tooltip != null) && !tooltip.equals (displayName))
                        return tooltip;
                }
            }
            return null;
        }
    }

    /** Popup menu listener. */
    private final class PopupAdapter extends
        org.openide.awt.MouseUtils.PopupMouseAdapter {
        protected void showPopup (MouseEvent e) {
            if (manager == null) return;
            int i = list.locationToIndex (new Point (e.getX (), e.getY ()));
            if (!list.isSelectedIndex (i))
                list.setSelectedIndex (i);
            list.requestFocus ();
            if (!popupAllowed) return;
            JPopupMenu popup = NodeOp.findContextMenu(manager.getSelectedNodes());
            if ((popup != null) && (popup.getSubElements().length > 0)) {
                java.awt.Point p = getViewport().getViewPosition();
                p.x = e.getX() - p.x;
                p.y = e.getY() - p.y;
                SwingUtilities.convertPointToScreen (p, ListView.this);
                Dimension popupSize = popup.getPreferredSize ();
                Dimension screenSize = Toolkit.getDefaultToolkit ().getScreenSize ();
                if (p.x + popupSize.width > screenSize.width) p.x = screenSize.width - popupSize.width;
                if (p.y + popupSize.height > screenSize.height) p.y = screenSize.height - popupSize.height;
                SwingUtilities.convertPointFromScreen (p, ListView.this);
                popup.show(ListView.this, p.x, p.y);
            }
        }
    } // end of PopupAdapter

    /**
    */
    private final class Listener extends MouseAdapter
        implements ListSelectionListener, PropertyChangeListener, VetoableChangeListener {
        public void mouseClicked(MouseEvent e) {
            if (MouseUtils.isDoubleClick(e)) {
                int index = list.locationToIndex(e.getPoint());
                performObjectAt(index, e.getModifiers());
            }
        }


        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (manager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] newNodes = (Node[])evt.getNewValue();
                if (!selectionAccept (newNodes)) {
                    throw new PropertyVetoException("", evt); // NOI18N
                }
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (manager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                updateSelection();
                return;
            }

            if (ExplorerManager.PROP_EXPLORED_CONTEXT.equals(evt.getPropertyName())) {
                model.setNode (manager.getExploredContext ());
                //System.out.println("Children: " + java.util.Arrays.asList (list.getValues ())); // NOI18N
                return;
            }
        }

        public void valueChanged(ListSelectionEvent e) {
            Object[] values = list.getSelectedValues ();
            Node[] nodes = new Node[values.length];

            for (int i = 0; i < nodes.length; i++) {
                nodes[i] = Visualizer.findNode (values[i]);
            }

            list.getSelectionModel ().removeListSelectionListener (this);
            try {
                selectionChanged (nodes, manager);
            } catch (java.beans.PropertyVetoException ex) {
                // selection vetoed - restore previous selection
                updateSelection();
            } finally {
                list.getSelectionModel ().addListSelectionListener (this);
            }
        }
    }

    // Backspace jumps to parent folder of explored context
    private final class GoUpAction extends AbstractAction {
        static final long serialVersionUID =1599999335583246715L;
        public GoUpAction () {
            super ("GoUpAction"); // NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            if (traversalAllowed) {
                Node pan = manager.getExploredContext();
                pan = pan.getParentNode();
                if (pan != null)
                    manager.setExploredContext(pan);
            }
        }
        public boolean isEnabled() {
            return true;
        }
    }

    //Enter key performObjectAt selected index.
    private final class EnterAction extends AbstractAction {
        static final long serialVersionUID =-239805141416294016L;
        public EnterAction () {
            super ("Enter"); // NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedIndex();
            performObjectAt(index, e.getModifiers());
        }
        public boolean isEnabled() {
            return true;
        }
    }

}

/*
 * Log
 *  27   Gandalf   1.26        1/13/00  Ian Formanek    NOI18N
 *  26   Gandalf   1.25        1/12/00  Ian Formanek    NOI18N
 *  25   Gandalf   1.24        1/12/00  Ian Formanek    NOI18N
 *  24   Gandalf   1.23        11/26/99 Patrik Knakal   
 *  23   Gandalf   1.22        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  22   Gandalf   1.21        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  21   Gandalf   1.20        8/27/99  Jaroslav Tulach List model can display 
 *       more levels at once.
 *  20   Gandalf   1.19        8/27/99  Jaroslav Tulach New threading model & 
 *       Children.
 *  19   Gandalf   1.18        8/19/99  Ian Formanek    Fixed bug 3502 - Switch 
 *       between workspaces throws exceptions
 *  18   Gandalf   1.17        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  17   Gandalf   1.16        7/1/99   Jan Jancura     ToolTip support huh..
 *  16   Gandalf   1.15        6/28/99  Ian Formanek    Fixed bug 2043 - It is 
 *       virtually impossible to choose lower items of New From Template  from 
 *       popup menu on 1024x768
 *  15   Gandalf   1.14        6/17/99  David Simonek   various serialization 
 *       bugfixes
 *  14   Gandalf   1.13        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  13   Gandalf   1.12        5/17/99  David Simonek   DnD switched off :-(
 *  12   Gandalf   1.11        5/11/99  David Simonek   addNotify now run under 
 *       Children.Mutex
 *  11   Gandalf   1.10        4/27/99  David Simonek   DnD support in list view
 *       added
 *  10   Gandalf   1.9         4/16/99  Jan Jancura     Object Browser support
 *  9    Gandalf   1.8         4/9/99   Jan Jancura     Bug 1508
 *  8    Gandalf   1.7         4/8/99   Jan Jancura     invoke&wait & some bug
 *  7    Gandalf   1.6         4/7/99   Jesse Glick     [JavaDoc]
 *  6    Gandalf   1.5         4/6/99   Ian Formanek    Added default handler
 *  5    Gandalf   1.4         3/20/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         3/20/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         2/11/99  Jaroslav Tulach SystemAction is 
 *       javax.swing.Action
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.14        --/--/98 Jan Formanek    added readObject
 *  0    Tuborg    0.15        --/--/98 Jan Formanek    multiple selection enabled
 *  0    Tuborg    0.16        --/--/98 Jan Formanek    backspace+enter keyboard processing
 *  0    Tuborg    0.20        --/--/98 Jan Formanek    reflecting changes in ExplorerView (became abstract class)
 *  0    Tuborg    0.20        --/--/98 Jan Formanek    validating scroll pane repaired
 *  0    Tuborg    0.22        --/--/98 Jan Formanek    repaired repainting
 *  0    Tuborg    0.30        --/--/98 Jan Formanek    SWITCHED TO NODES
 *  0    Tuborg    0.32        --/--/98 Jan Formanek    added listeners for subNodes add/remove and name changes
 *  0    Tuborg    0.40        --/--/98 Jan Formanek    reflecting changes in explorer model
 *  0    Tuborg    0.42        --/--/98 Jan Formanek    fixed clearing of selection after changing Node's name (BUG ID: 01000007)
 *  0    Tuborg    0.43        --/--/98 Jan Formanek    reflecting changes in ExplorerView
 *  0    Tuborg    0.45        --/--/98 Petr Hamernik   doubleclick
 *  0    Tuborg    0.46        --/--/98 Petr Hamernik   default action
 *  0    Tuborg    0.47        --/--/98 Jan Formanek    improved context menu invocation
 */
