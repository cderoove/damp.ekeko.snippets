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

package org.netbeans.modules.projects;

import java.awt.*;
import java.io.*;
import javax.swing.*;

import org.openide.*;
import org.openide.cookies.ProjectCookie;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.util.actions.CallableSystemAction;

/**
 * Creates new project.
 * 
 * @author   Jaroslav Tulach
 */
public class NewProjectAction extends CallableSystemAction {

    /**
     * generated Serialized Version UID
     */
    static final long serialVersionUID = -3323132668863606031L;

    {
        setEnabled (true);
    }

    /**
     * @return the action's icon
     */
    public String getName () {
        return ProjectDataObject.getLocalizedString ("ACT_NewProject"); // NOI18N
    }

    /**
     * @return the action's help context
     */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (NewProjectAction.class);
    }

    /**
     * @return the action's icon
     */
    public String iconResource () {
        return "/org/netbeans/modules/projects/resources/newProject.gif"; // NOI18N
    }

    /**
     * Action performed, opens dialog that allows user to choose a project.
     * It is then opened.
     */
    public void performAction () {
        try {
            Panel         panel = new Panel ();
            Node          n =
                TopManager.getDefault ().getNodeOperation ().select (ProjectDataObject.getLocalizedString ("CTL_NewTitle"), // NOI18N
                        ProjectDataObject.getLocalizedString ("CTL_NewDescr"), // NOI18N
                        getProjectRoot (panel), /* filter */ panel, /* component */
                        panel)[0];

            // looking for folder - get deafult
            DataFolder df = (DataFolder) TopManager.getDefault().getPlaces().folders().projects();

            // create new project
            ProjectDataObject pdo = ProjectDataObject.createProject(df, panel.name.getText ());
            PSupport ps = pdo.support;

            ps.openNew();
        } catch (UserCancelException e) {
            //
        } catch (IOException e) {
            TopManager.getDefault ().notifyException (e);
        }
    }

    /**
     * Action performed, opens dialog that allows user to choose a project.
     * It is then opened.
     * @return true when project it's created
     */
    public boolean perform () {
        try {
            Panel panel = new Panel ();
            Node n = TopManager.getDefault ().getNodeOperation ().select (
                         ProjectDataObject.getLocalizedString ("CTL_NewTitle"), // NOI18N
                         ProjectDataObject.getLocalizedString ("CTL_NewDescr"), // NOI18N
                         getProjectRoot (panel), /* filter */
                         panel, /* component */
                         panel
                     )[0];

            // looking for folder
            DataFolder    df = (DataFolder) n.getCookie (DataFolder.class);

            // create new project
            PSupport ps = ProjectDataObject.createProject(df, panel.name.getText ()).support;
            ps.openNew();

            return true;
        } catch (UserCancelException e) {
            return false;
        } catch (IOException e) {
            TopManager.getDefault ().notifyException (e);

            return false;
        }
    }

    /**
     * Create a hierarchy of templates.
     * @return a node representing all possible templates
     */
    private static Node getProjectRoot (DataFilter ff) {
        DataFolder  f =
            TopManager.getDefault ().getPlaces ().folders ().projects ();

        // listener used as filter (has method acceptDataObject)
        Children    ch = f.createNodeChildren (ff);

        // filter the children
        ch = new ProjectChildren (new AbstractNode (ch));

        // create the root
        return new PresentationFilterNode (f.getNodeDelegate (), ch);
    }

    /**
     * Actions listener which instantiates the template
     */
    private static class Panel extends JPanel implements NodeAcceptor,
        DataFilter {

        /**
         * name of the text to show
         */
        public JTextField name;

        /**
         * label
         */
        private JLabel    label;

        /**
         * Creates the dialog
         */
        public Panel () {
            setLayout (new BorderLayout ());

            label = new JLabel ();

            name = new JTextField ();

            name.setText (ProjectDataObject.getLocalizedString ("CTL_NewProject")); // NOI18N

            add (BorderLayout.WEST, label);
            add (BorderLayout.CENTER, name);
        }

        /**
         * Data filter impl.
         */
        public boolean acceptDataObject (DataObject obj) {
            return obj instanceof DataFolder
                   || obj.getCookie (ProjectCookie.class) != null;
        }

        public boolean acceptNodes (Node[] n) {
            if (n.length != 1) {
                return false;
            }

            return n[0].getCookie (DataFolder.class) != null
                   &&!name.getText ().equals (""); // NOI18N
        }

    }

    /**
     * Filter node children, that stops on data objects (does not go futher)
     */
    private static class ProjectChildren extends FilterNode.Children {
        public ProjectChildren (Node or) {
            super (or);
        }

        protected Node copyNode (Node n) {
            /*
              DataFolder df = (DataFolder) n.getCookie (DataFolder.class);

              if (df == null) {

                // on normal nodes stop recursion
                return new FilterNode (n, LEAF);
              } else {

                // on folders use normal filtering
                return new FilterNode (n, new ProjectChildren (n));
              } 
              */
            ProjectDataObject pdo = (ProjectDataObject) n.getCookie (ProjectDataObject.class);
            if (pdo != null) return new PresentationFilterNode(n, LEAF);
            return null;
        }

    }

    private static class PresentationFilterNode extends FilterNode {
        
        public PresentationFilterNode(org.openide.nodes.Node node, org.openide.nodes.Children children) {
            super(node, children);
        }
        
        public PresentationFilterNode(org.openide.nodes.Node node) {
            super(node);
        }

        public boolean canCopy() {
            return false;
        }
        
        public boolean canCut()  {
            return false;
        }
        
        public boolean canDestroy() {
            return false;
        }
        
        public boolean canRename() {
            return false;
        }
    }
}

/*
 * Log
 *  8    Gandalf   1.7         1/19/00  Martin Ryzl     subprojects disabled
 *  7    Gandalf   1.6         1/16/00  Martin Ryzl     
 *  6    Gandalf   1.5         1/15/00  Ian Formanek    NOI18N
 *  5    Gandalf   1.4         1/13/00  Martin Ryzl     heavy localization
 *  4    Gandalf   1.3         1/11/00  Martin Ryzl     
 *  3    Gandalf   1.2         1/10/00  Martin Ryzl     
 *  2    Gandalf   1.1         1/6/00   Martin Ryzl     problems with compiling 
 *       by the old compiler fixed
 *  1    Gandalf   1.0         1/3/00   Martin Ryzl     
 * $
 */



