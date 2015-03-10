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

import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.*;

import org.openide.TopManager;
import org.openide.cookies.ProjectCookie;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.util.actions.CallableSystemAction;

/** The OpenProject Action.
*
* @author   Jaroslav Tulach
*/
public class OpenProjectAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1785983127137412341L;

    /** @return the action's icon */
    public String getName() {
        return NbBundle.getBundle(OpenProjectAction.class).getString ("OpenProject");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (OpenProjectAction.class);
    }

    /** @return the action's icon */
    public String iconResource() {
        return "/org/openide/resources/actions/openProject.gif"; // NOI18N
    }

    /** Action performed, opens dialog that allows user to choose a project.
    * It is then opened.
    */
    public void performAction () {
        try {
            Panel panel = new Panel ();
            Node n = TopManager.getDefault ().getNodeOperation ().select (
                         NbBundle.getBundle(OpenProjectAction.class).getString ("CTL_OpenTitle"),
                         NbBundle.getBundle(OpenProjectAction.class).getString ("CTL_OpenDescr"),
                         getProjectRoot (panel), /* filter */panel
                     )[0];
            final ProjectCookie c = (ProjectCookie)n.getCookie (ProjectCookie.class);

            org.openide.util.RequestProcessor.postRequest(new Runnable() {
                        public void run() {
                            try {
                                TopManager.getDefault ().openProject (c);
                            } catch (UserCancelException e) {
                            } catch (IOException e) {
                                TopManager.getDefault ().notifyException (e);
                            }
                        }
                    });
        } catch (UserCancelException e) {
        } catch (IOException e) {
            TopManager.getDefault ().notifyException (e);
        }
    }

    /** Create a hierarchy of templates.
    * @return a node representing all possible templates
    */
    private static Node getProjectRoot (DataFilter ff) {
        DataFolder f = TopManager.getDefault ().getPlaces ().folders ().projects ();
        // listener used as filter (has method acceptDataObject)
        Children ch = f.createNodeChildren (ff);
        // filter the children
        ch = new ProjectChildren (new AbstractNode (ch));
        // create the root
        return new PresentationFilterNode (f.getNodeDelegate (), ch);
    }

    /** Actions listener which instantiates the template */
    private static class Panel extends Object
        implements NodeAcceptor, DataFilter {
        static final long serialVersionUID =-3217011757079457914L;
        /** Data filter impl.
        */
        public boolean acceptDataObject (DataObject obj) {
            return obj.getCookie (ProjectCookie.class) != null;
        }

        public boolean acceptNodes (Node[] n) {
            if (n.length != 1) {
                return false;
            }
            return n[0].getCookie (ProjectCookie.class) != null;
        }
    }

    /** Filter node children, that stops on data objects (does not go futher)
    */
    private static class ProjectChildren extends FilterNode.Children {
        public ProjectChildren (Node or) {
            super (or);
        }

        protected Node copyNode (Node n) {
            DataFolder df = (DataFolder)n.getCookie (DataFolder.class);
            if (df == null) {
                // on normal nodes stop recursion
                return new PresentationFilterNode (n, LEAF);
            } else {
                // on folders use normal filtering
                return new PresentationFilterNode (n, new ProjectChildren (n));
            }
        }
    }
    /*
      public static void main (String[] args) {
        new OpenProjectAction ().performAction ();
      }
    */  
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
 *  6    Gandalf   1.5         3/7/00   Martin Ryzl     bugfix #5868
 *  5    Gandalf   1.4         2/8/00   Martin Ryzl     request processor used
 *  4    Gandalf   1.3         1/12/00  Ian Formanek    NOI18N
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  1    Gandalf   1.0         8/9/99   Jaroslav Tulach 
 * $
 */
