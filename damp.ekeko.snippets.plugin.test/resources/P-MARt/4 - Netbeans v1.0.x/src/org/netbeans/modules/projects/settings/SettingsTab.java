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

import java.awt.*;
import java.beans.*;

import org.openide.awt.SplittedPanel;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.view.*;
import org.openide.nodes.Node;
import org.openide.util.WeakListener;
import org.openide.windows.Workspace;

/**
 *
 * @author  mryzl
 */

public class SettingsTab extends ExplorerPanel
    implements java.io.ObjectInputValidation {

    private TreeView view;

    /** listeners to the root context and IDE settings */
    private PropertyChangeListener rcListener, weakRcL, weakIdeL;

    /** validity flag */
    private boolean valid = true;

    public SettingsTab () {
        super();
        view = initGui();
    }

    /** Put tree view and property
    * sheet to the splitted panel.
    * @return Tree view that will serve as main view for this explorer.
    */
    protected TreeView initGui () {
        TreeView view = new BeanTreeView();
        SplittedPanel split = new SplittedPanel();
        PropertySheetView propertyView = new PropertySheetView();
        split.add(view, SplittedPanel.ADD_LEFT);
        split.add(propertyView, SplittedPanel.ADD_RIGHT);
        // add to the panel
        setLayout(new BorderLayout());
        add(split, BorderLayout.CENTER);

        return view;
    }

    /** Called when the explored context changes.
    * Overriden - we don't want title to chnage in this style.
    */
    protected void updateTitle () {
        // empty to keep the title unchanged
    }

    /** Request focus also for asociated view */
    public void requestFocus () {
        super.requestFocus();
        view.requestFocus();
    }

    /** Ensures that component is valid before opening */
    public void open (Workspace workspace) {
        performCommand(null);
        super.open(workspace);
    }

    /** Sets new root context to view. Name, icon, tooltip
    * of this top component will be updated properly */
    public void setRootContext (Node rc) {
        // remove old listener, if possible
        if (weakRcL != null) {
            getExplorerManager().getRootContext().
            removePropertyChangeListener(weakRcL);
        }
        getExplorerManager().setRootContext(rc);
        initializeWithRootContext(rc);
    }

    public Node getRootContext () {
        return getExplorerManager().getRootContext();
    }

    /** Overrides superclass version - adds request for initialization
    * of the icon and other attributes, also re-attaches listener to the
    * root context */
    public void readExternal (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        super.readExternal(oi);
        // put a request for later validation
        // we must do this here, because of ExplorerManager's deserialization.
        // Root context of ExplorerManager is validated AFTER all other
        // deserialization, so we must wait for it
        valid = false;
        //  WindowManagerImpl.deferredPerformer().putRequest(this, null);
        //  or replacement by Dafe ...
        ((java.io.ObjectInputStream)oi).registerValidation(this, 0);
    }

    /** Implementation of ObjectInputValidation
    * Performs initialization of component's attributes
    * after deserialization (component's name, icon etc, 
    * according to the root context) */
    public void validateObject() {
        performCommand(null);
    }

    /** Implementation of DeferredPerformer.DeferredCommand
    * Performs initialization of component's attributes
    * after deserialization (component's name, icon etc, 
    * according to the root context) */
    public void performCommand (Object context) {
        if (!valid) {
            valid = true;
            validateRootContext();
        }
    }

    /** Validates root context of this top component after deserialization.
    * It is guaranteed that this method is called at a time when
    * getExplorerManager().getRootContext() call will return valid result.
    * Subclasses can override this method and peform further validation
    * or even set new root context instead of deserialized one.<br>
    * Default implementation just initializes top component with standard
    * deserialized root context. */
    protected void validateRootContext () {
        initializeWithRootContext(getExplorerManager().getRootContext());
    }

    private PropertyChangeListener rcListener () {
        if (rcListener == null) {
            rcListener = new RootContextListener();
        }
        return rcListener;
    }

    /** Initialize this top component properly with information
    * obtained from specified root context node */
    private void initializeWithRootContext (Node rc) {
        // update TC's attributes
        setIcon(rc.getIcon(BeanInfo.ICON_COLOR_16x16));
        setToolTipText(rc.getShortDescription());
        setName(rc.getDisplayName());
        updateTitle();
        // attach listener
        if (weakRcL == null) {
            weakRcL = WeakListener.propertyChange(rcListener(), rc);
        }
        rc.addPropertyChangeListener(weakRcL);
    }

    /** Multi - purpose listener, listens to: <br>
    * 1) Changes of name, icon, short description of root context.
    * 2) Changes of IDE settings, namely delete confirmation settings */
    private final class RootContextListener extends Object
        implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            Object source = evt.getSource();

            // root context node change
            Node n = (Node)source;
            if (Node.PROP_DISPLAY_NAME.equals(propName)) {
                setName(n.getDisplayName());
            } else if (Node.PROP_ICON.equals(propName)) {
                setIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
            } else if (Node.PROP_SHORT_DESCRIPTION.equals(propName)) {
                setToolTipText(n.getShortDescription());
            }
        }
    } // end of RootContextListener inner class

}

/*
* Log
*  3    Gandalf   1.2         1/14/00  Martin Ryzl     
*  2    Gandalf   1.1         1/4/00   Martin Ryzl     
*  1    Gandalf   1.0         1/3/00   Martin Ryzl     
* $ 
*/ 
