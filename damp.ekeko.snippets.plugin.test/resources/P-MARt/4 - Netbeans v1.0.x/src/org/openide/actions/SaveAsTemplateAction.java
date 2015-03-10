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

package org.openide.actions;

import java.io.IOException;

import org.openide.util.actions.NodeAction;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.UserCancelException;
import org.openide.nodes.NodeAcceptor;

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.TopManager;
import org.openide.filesystems.*;

/** Saves a data object to a folder under in the
* system's templates area.
* @see org.openide.Places.Folders#templates
*
* @author  Ales Novak, Dafe Simonek
*/
public final class SaveAsTemplateAction extends NodeAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5398459720576212987L;

    /* constructor */
    public SaveAsTemplateAction() {
        super();
    }

    /* Returns resource string to the icon of this action.
    * @return resource string
    */
    public String iconResource () {
        return "/org/openide/resources/actions/empty.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (SaveAsTemplateAction.class);
    }

    public String getName () {
        return ActionConstants.BUNDLE.getString("SaveAsTemplate");
    }

    /* Returns false - action should be disabled when a window with no
    * activated nodes is selected.
    *
    * @return false do not survive the change of focus
    */
    protected boolean surviveFocusChange () {
        return false;
    }

    /* Manages enable/disable logic. Action is enabled only
    * if all activated nodes can be saved as templates.
    * Overrides abstract enable(..) from superclass.
    *
    * @param activatedNodes Array of activated nodes.
    * @return enable status
    */
    protected boolean enable (Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0)
            return false;
        // test if all nodes support saving as template
        DataObject curCookie;
        for (int i = 0; i < activatedNodes.length; i++) {
            curCookie = (DataObject)activatedNodes[i].getCookie(DataObject.class);
            if ((curCookie == null) || (!curCookie.isCopyAllowed()))
                // not supported
                return false;
        }
        return true;
    }

    /* Performs the action - launches new file dialog,
    * saves as a template ...
    * Overrides abstract enable(..) from superclass.
    *
    * @param activatedNodes Array of activated nodes
    */
    protected void performAction (Node[] activatedNodes) {
        // prepare variables
        NodeAcceptor acceptor = FolderNodeAcceptor.getInstance();
        String title = ActionConstants.BUNDLE.getString("SaveAsTemplate");
        String rootTitle = title;
        DataObject templates =
            TopManager.getDefault().getPlaces().folders().templates();
        Node templatesNode = templates.getNodeDelegate();
        Node[] selected;
        // ask user: where to save the templates?
        try {
            selected = TopManager.getDefault().getNodeOperation().
                       select(title, rootTitle, templatesNode, acceptor, null);
        } catch (UserCancelException ex) {
            // user cancelled the operation
            return;
        }
        // create & save them all
        // we know DataFolder and DataObject cookies must be supported
        // so we needn't check for null values
        DataFolder targetFolder =
            (DataFolder)selected[0].getCookie(DataFolder.class);
        for (int i = 0; i < activatedNodes.length; i++ ) {
            createNewTemplate(
                (DataObject)activatedNodes[i].getCookie(DataObject.class),
                targetFolder);
        }
    }

    /** Performs the work of creating a new template */
    private void createNewTemplate(DataObject source,
                                   DataFolder targetFolder) {
        try {
            DataObject newTemplate = source.copy(targetFolder);
            newTemplate.setTemplate(true);
        } catch (IOException ex) {
            TopManager.getDefault().notifyException(ex);
        }
    }

    /** Inner class functioning like node acceptor for
    * user dialogs when selecting where to save as template.
    * Accepts folders only. Singleton.
    */
    static final class FolderNodeAcceptor implements NodeAcceptor {

        /** an instance */
        private static FolderNodeAcceptor instance;

        /** singleton */
        private FolderNodeAcceptor() {
        }

        /** accepts a selected folder */
        public final boolean acceptNodes(Node[] nodes) {
            if (nodes == null || nodes.length != 1) return false;
            return nodes[0].getCookie(DataFolder.class) != null;
        }

        /** getter for an instance */
        static FolderNodeAcceptor getInstance() {
            if (instance == null) instance = new FolderNodeAcceptor();
            return instance;
        }
    } // end of FolderNodeAcceptor inner class

}

/*
 * $Log
 */
