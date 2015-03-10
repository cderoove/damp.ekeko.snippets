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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.beans.*;

import org.openide.explorer.*;
import org.openide.nodes.Node;

import javax.swing.*;


/** Explorer view based on a combo box.
*
* @author Jaroslav Tulach
*/
public class ChoiceView extends JComboBox implements Externalizable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 2522310031223476067L;

    /** The local reference to the explorerManager. It is transient
    * because it will be reset in initializeManager() after deserialization.*/
    transient private ExplorerManager manager;
    /** Listens on ExplorerManager. */
    transient private PropertyIL iListener;
    /** model to use */
    transient private NodeListModel model;

    /** Value of property showExploredContext. */
    private boolean showExploredContext = true;

    // init .................................................................................

    /** Default constructor. */
    public ChoiceView () {
        super ();
        initializeChoice ();
    }

    /** Initialize view. */
    private void initializeChoice () {
        setRenderer (new NodeRenderer ());

        setModel (model = createModel ());

        iListener = new PropertyIL();
    }

    /*
    * Write view's state to output stream.
    */
    public void writeExternal (ObjectOutput out) throws IOException {
        out.writeObject (new Boolean (showExploredContext));
    }

    /*
    * Reads view's state form output stream.
    */
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        showExploredContext = ((Boolean)in.readObject ()).booleanValue ();
    }

    //
    // To override
    //

    /** Creates the model that this view should show.
    */
    protected NodeListModel createModel () {
        return new NodeListModel ();
    }


    // main methods .........................................................................

    /** Set showing of explored contexts.
    * @param b <code>true</code> to show the explored context, <code>false</code> the root context
    */
    public void setShowExploredContext (boolean b) {
        showExploredContext = b;
        updateChoice();
    }

    /**
    * Get explored context toggle.
    * @return whether currently showing explored context (default <code>false</code>)
    */
    public boolean getShowExploredContext () {
        return showExploredContext;
    }


    // main methods .........................................................................

    /* Initializes view.
    */
    public void addNotify() {
        manager = ExplorerManager.find (this);
        manager.addVetoableChangeListener(iListener);
        manager.addPropertyChangeListener(iListener);

        updateChoice();

        addItemListener (iListener);

        super.addNotify ();
    }

    /* Deinitializes view.
    */
    public void removeNotify() {
        super.removeNotify ();

        removeItemListener (iListener);

        manager.removeVetoableChangeListener(iListener);
        manager.removePropertyChangeListener(iListener);
    }

    private void updateSelection() {
        Node[] nodes = manager.getSelectedNodes ();

        if (nodes.length > 0) {
            setSelectedItem (
                VisualizerNode.getVisualizer (null, nodes[0])
            );
        } else {
            setSelectedItem (VisualizerNode.EMPTY);
        }
    }

    private void updateChoice() {
        if (showExploredContext) {
            model.setNode (manager.getExploredContext ());
        } else {
            model.setNode (manager.getRootContext ());
        }

        updateSelection ();
    }


    // innerclasses .........................................................................

    /* The inner adaptor class for listening to the ExplorerManager's property and vetoable changes. */
    final class PropertyIL extends Object
                implements PropertyChangeListener, VetoableChangeListener,
        java.awt.event.ItemListener {
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] nodes = (Node[])evt.getNewValue();
                if (nodes.length > 1) {
                    throw new PropertyVetoException("", evt); // we do not allow multiple selection // NOI18N
                }
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            ChoiceView.this.removeItemListener (this);
            try {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    Node[] selectedNodes = (Node[])evt.getNewValue();
                    updateSelection();
                    return;
                }

                if (
                    !showExploredContext &&
                    ExplorerManager.PROP_ROOT_CONTEXT.equals(evt.getPropertyName())
                ) {
                    updateChoice();
                    return;
                }
                if (
                    showExploredContext &&
                    ExplorerManager.PROP_EXPLORED_CONTEXT.equals (evt.getPropertyName())
                ) {
                    updateChoice();
                    return;
                }
            } finally {
                ChoiceView.this.addItemListener (this);
            }
        }

        public void itemStateChanged(ItemEvent e) {
            int s = getSelectedIndex ();
            if (s < 0 || s >= getItemCount ()) {
                return;
            }

            Node n = Visualizer.findNode (model.getElementAt (s));

            manager.removeVetoableChangeListener(this);
            manager.removePropertyChangeListener(this);
            try {
                manager.setSelectedNodes(new Node[] { n });
            } catch (PropertyVetoException ex) {
                updateChoice(); // no selection change allowed
            } finally {
                manager.addVetoableChangeListener(this);
                manager.addPropertyChangeListener(this);
            }
        }
    }
}

/*
 * Log
 *  7    Gandalf   1.6         1/13/00  Ian Formanek    NOI18N
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/27/99  Jaroslav Tulach List model can display 
 *       more levels at once.
 *  4    Gandalf   1.3         8/27/99  Jaroslav Tulach New threading model & 
 *       Children.
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/20/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.50        --/--/98 Jan Formanek    SWITCHED TO NODES
 *  0    Tuborg    0.60        --/--/98 Jan Formanek    reflecting changes in explorer model
 *  0    Tuborg    0.61        --/--/98 Jan Formanek    reflecting changes in ExplorerView
 */
