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

package org.netbeans.modules.debugger.support.nodes;

import java.awt.BorderLayout;
import java.beans.BeanInfo;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.ObjectInput;
import java.io.IOException;

import org.openide.TopManager;
import org.openide.awt.SplittedPanel;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.ListView;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.util.RequestProcessor;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;

import org.netbeans.modules.debugger.support.actions.DebuggerViewAction;
import org.netbeans.modules.debugger.support.DebuggerModule;

/**
* Top component showing breakpoints list together with their properties.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
* @author Jan Jancura
*/
public class DebuggerView extends ExplorerPanel
    implements java.io.ObjectInputValidation {

    /** generated Serialized Version UID */
    static final long             serialVersionUID = - 1173766551812935520L;

    /** Current serialization version. */
    private static final int      SERIAL_VERSION = 1;
    private boolean               tree;
    private boolean               canOpen = true;


    // init ......................................................................

    /** Creates breakpoints top component with
    * default explorer manager.
    */
    public DebuggerView () {
    }

    /** Creates breakpoints top component with
    * default explorer manager.
    */
    public DebuggerView (boolean tree, Node rc) {
        createView (tree, rc);
        this.tree = tree;
    }

    /** Stores the manager */
    public void writeExternal (java.io.ObjectOutput oo) throws java.io.IOException {
        // DO NOT CHANGE SERIALIZATION!!!
        super.writeExternal (oo);
        oo.write (SERIAL_VERSION * 2 + (tree ? 1 : 0));
        switch (SERIAL_VERSION) {
        case 0:
            break;
        }
    }

    /** Sets this tab to DebuggerViewAction.
    * @param in the stream to deserialize from
    */
    public void readExternal (ObjectInput in) throws IOException,
        ClassNotFoundException {
        // DO NOT CHANGE SERIALIZATION!!!
        super.readExternal (in);
        int ver = in.read ();
        tree = (ver % 2) > 0;
        ver = ver / 2;
        switch (ver) {
        case 0:
            // 1.0 FCS serialization version
            ((java.io.ObjectInputStream) in).registerValidation (this, 0);
            break;
        case 1:
            // current version
            ((java.io.ObjectInputStream) in).registerValidation (this, 0);
            break;
        default:
            canOpen = false;
        }
    }

    /**
    * Implementation of ObjectInputValidation
    * Performs initialization of component's attributes
    * after deserialization (component's name, icon etc, 
    * according to the root context) 
    */
    public void validateObject () {
        Node rootNode = getExplorerManager ().getRootContext ();
        Node n = DebuggerModule.getNode (rootNode.getClass ());
        if (n != null) {
            // view is in use
            createView (tree, rootNode);
            DebuggerModule.addView (this, true);
        } else {
            // this view in unsupported now
            DebuggerModule.closeView (this);
            RequestProcessor.postRequest (new Runnable () {
                                              public void run () {
                                                  DebuggerModule.installWorkspaces ();
                                              }
                                          }, 3000);
        }
    }


    // TopComponent implementation ...............................................

    public HelpCtx getHelpCtx () {
        return getHelpCtx (
                   getExplorerManager ().getSelectedNodes (),
                   new HelpCtx (getClass ())
               );
    }

    /**
    * Sets name and iconfor this view.
    */
    protected void updateTitle () {
        setIcon (getExplorerManager ().getRootContext ().getIcon (BeanInfo.ICON_COLOR_16x16));
        final String name =
            getExplorerManager ().getRootContext ().getDisplayName ();
        setName (name == null ? "" : name); // NOI18N
    }

    public void open (Workspace w) {
        if (w == null) w = TopManager.getDefault ().getWindowManager ().
                               getCurrentWorkspace ();
        if (w.findMode (this) == null) {
            Mode mode = w.findMode (DebuggerModule.MODE_NAME);
            if (mode == null)
                mode = DebuggerModule.createMode (w, this);
            mode.dockInto (this);
        }
        super.open (w);
    }


    // private methods ...........................................................

    private void createView (boolean tree, Node rc) {

        SplittedPanel sp = new SplittedPanel ();
        if (tree)
            sp.add (new BeanTreeView (), SplittedPanel.ADD_LEFT);
        else
            sp.add (new ListView (), SplittedPanel.ADD_LEFT);

        PropertySheetView psv = new PropertySheetView ();
        try {
            psv.setSortingMode (PropertySheetView.UNSORTED);
        } catch (java.beans.PropertyVetoException e) {
        }
        sp.add (psv, SplittedPanel.ADD_RIGHT);

        setLayout (new BorderLayout ());
        add (BorderLayout.CENTER, sp);

        ExplorerManager em = getExplorerManager ();
        em.setRootContext (rc);

        setIcon (rc.getIcon (BeanInfo.ICON_COLOR_16x16));
    }
}

/*
* Log
*  5    Gandalf-post-FCS1.1.3.2     3/30/00  Jan Jancura     New serialization
*  4    Gandalf-post-FCS1.1.3.1     3/29/00  Jan Jancura     Serialization of debugger
*       improved
*  3    Gandalf-post-FCS1.1.3.0     3/28/00  Daniel Prusa    
*  2    Gandalf   1.1         1/13/00  Daniel Prusa    NOI18N
*  1    Gandalf   1.0         12/10/99 Jan Jancura     
* $
*/
