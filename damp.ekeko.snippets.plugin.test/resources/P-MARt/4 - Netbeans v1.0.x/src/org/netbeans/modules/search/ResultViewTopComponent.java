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

package org.netbeans.modules.search;

import java.awt.*;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.beans.*;

import javax.swing.*;
import javax.swing.border.*;

import org.openide.windows.*;
import org.openide.explorer.*;
import org.openide.explorer.view.*;
import org.openide.nodes.*;

import org.openidex.search.*;

import org.netbeans.modules.search.res.*;

/**
 * Display results in Explorer like manner.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class ResultViewTopComponent extends TopComponent implements ExplorerManager.Provider {

    /** My data holder. */
    private ResultModel model;

    /** My ExplorerManager.Provider. */
    private ExplorerPanel exp;

    /** My control panel. */
    private ResultViewPanel buttonPanel;

    /** Listen CURRENT nodes. */
    private PropertyChangeListener managerListener;

    /** Creates new ResultViewTopComponent */
    public ResultViewTopComponent(ResultModel model) {

        setLayout(new BorderLayout());

        ManagerListener managerListener = new ManagerListener();

        // add bean tree view

        exp = new ExplorerPanel();
        exp.setLayout(new BorderLayout());
        exp.setPreferredSize(new Dimension(250,350));
        exp.getExplorerManager().addPropertyChangeListener(managerListener);

        BeanTreeView tree =  new BeanTreeView();
        tree.setBorder(new EtchedBorder());
        exp.add(tree, BorderLayout.CENTER);

        add(exp, BorderLayout.CENTER);

        buttonPanel = new ResultViewPanel(model);
        buttonPanel.setBorder(new EmptyBorder(0,4,0,0));
        add(buttonPanel, BorderLayout.EAST);

        String labela = Res.text("LABEL_SEARCH_RESULTS"); // NOI18N

        // set proper border
        // workaround for empty border erasing of titled one
        setBorder( new CompoundBorder (

                       new TitledBorder( new CompoundBorder (
                                             new EmptyBorder(4,4,4,4),
                                             new EtchedBorder()
                                         ), labela),

                       new EmptyBorder(4,4,4,4)

                   ));


        setName(labela);

        setModel(model);

    }

    /** Set new model. */
    public void setModel(ResultModel model) {

        this.model = model;
        buttonPanel.setModel(model);
        Node root = model.getRoot();
        exp.getExplorerManager().setRootContext(root);

    }

    /** @return search icon.
    */
    public Image getIcon() {
        return Res.image("SEARCH"); // NOI18N
    }

    /** ExplorerPanel.Provider
    */
    public ExplorerManager getExplorerManager() {
        return exp.getExplorerManager();
    }

    /** Stop search before closing.
    */
    public boolean canClose(Workspace workspace, boolean last) {
        model.stop();
        exp.removePropertyChangeListener(managerListener);
        return true;
    }

    /** Listener on the explorer manager properties.
    * Changes selected nodes of this frame.
    */
    private class ManagerListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] nodes = exp.getExplorerManager().getSelectedNodes();
                setActivatedNodes(nodes);

                DetailCookie cake = null;

                if (nodes != null && nodes.length == 1) {
                    cake = (DetailCookie) nodes[0].getCookie(DetailCookie.class);
                }

                buttonPanel.showDetail(cake);

                return;
            }
        }
    }



    // Remove itself from serialization.

    public Object writeReplace() throws ObjectStreamException {
        return new Hack();
    }

    private static class Hack implements Serializable {

        public Object readResolve() throws ObjectStreamException {
            return null;
        }

        public Hack() {
        }
    }

}


/*
* Log
*  7    Gandalf   1.6         1/13/00  Radko Najman    I18N
*  6    Gandalf   1.5         1/11/00  Petr Kuzel      Result details added.
*  5    Gandalf   1.4         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  4    Gandalf   1.3         1/4/00   Petr Kuzel      Bug hunting.
*  3    Gandalf   1.2         12/17/99 Petr Kuzel      Bundling.
*  2    Gandalf   1.1         12/16/99 Petr Kuzel      
*  1    Gandalf   1.0         12/15/99 Petr Kuzel      
* $ 
*/ 

