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

package org.netbeans.core.windows.nodes;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;

import org.openide.util.datatransfer.PasteType;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.NewType;
import org.openide.TopManager;
import org.openide.windows.Workspace;
import org.openide.windows.WindowManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

import org.openide.actions.*;
import org.openide.cookies.InstanceCookie;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.*;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.WorkspaceImpl;

/** Node representing all workspaces.
*
* @author Ales Novak, Jan Jancura
*/
public final class WorkspacePoolContext extends AbstractNode {
    /** default instance */
    private static WorkspacePoolContext defaultContext;

    /** Constructs new WorkspacePoolContext object
    */
    private WorkspacePoolContext () {
        // uses workspaces as children
        super (new Workspaces ());
        setName (NbBundle.getBundle (WorkspacePoolContext.class).getString ("Corona_workspaces"));
        setShortDescription (NbBundle.getBundle (WorkspacePoolContext.class).getString ("Corona_workspaces_HINT"));
        setIconBase ("/org/netbeans/core/resources/workspaces"); // NOI18N
    }

    /** Getter for the workspace pool node in the context.
    */
    public static WorkspacePoolContext getDefault () {
        if (defaultContext ==  null) {
            synchronized (WorkspacePoolContext.class) {
                if (defaultContext ==  null) {
                    defaultContext = new WorkspacePoolContext ();
                }
            }
        }
        return defaultContext;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (WorkspacePoolContext.class);
    }

    //************** paste of workspace *******************

    /** Checks for instances of workspace elements.
    */
    protected void createPasteTypes (Transferable t, List s) {
        super.createPasteTypes (t, s);
        Workspace e = element((InstanceCookie)NodeTransfer.cookie (t, NodeTransfer.COPY, InstanceCookie.class));
        if (e != null) {
            // copy flavor
            s.add (new WorkspacePasteType (e, null));
            return;
        }

        // node to be cut
        Node n = NodeTransfer.node(t, NodeTransfer.CLIPBOARD_CUT | NodeTransfer.DND_MOVE);
        if (n != null && n.canDestroy ()) {
            e = element ((InstanceCookie)n.getCookie (InstanceCookie.class));
            s.add (new WorkspacePasteType (e, n));
            return;
        }
    }

    /** Takes workspace from the instance cookie or not.
    * @param cookie instance cookie
    * @return the workspace
    */
    private Workspace element (InstanceCookie cookie) {
        try {
            if (cookie != null && Workspace.class.isAssignableFrom (cookie.instanceClass ())) {
                return (Workspace)cookie.instanceCreate ();
            }
        } catch (Exception ex) {
            if (System.getProperty("netbeans.debug.exceptions") != null) ex.printStackTrace();
        }
        return null;
    }


    /** Paste type for transfering workspaces.
    */
    private class WorkspacePasteType extends PasteType {
        /** the workspace */
        private Workspace workspace;
        /** the node to destroy or null */
        private Node node;

        /** Constructs new WorkspacePasteType for the specific
        * type of operation paste.
        */
        public WorkspacePasteType(Workspace workspace, Node node) {
            this.workspace = workspace;
            this.node = node;
        }

        /* @return Human presentable name of this paste type. */
        public String getName() {
            return NbBundle.getBundle (WorkspacePoolContext.class).getString ("PASTE_Workspace");
        }

        /* @return help */
        public HelpCtx getHelpCtx() {
            return new HelpCtx (WorkspacePasteType.class);
        }

        /** Performs the paste action.
        * @return Transferable which should be inserted into the clipboard after
        *         paste action. It can be null, which means that clipboard content
        *         should stay the same.
        */
        public Transferable paste() throws IOException {
            addWorkspace(workspace);
            if (node != null) {
                node.destroy ();
            }
            return null;
        }
    }

    private static void addWorkspace(Workspace workspace) {
        Workspace[] wss = TopManager.getDefault().getWindowManager().getWorkspaces();
        Workspace[] nwss = new Workspace[wss.length + 1];
        System.arraycopy(wss, 0, nwss, 0, wss.length);
        nwss[wss.length] = workspace;
        ((WindowManagerImpl) TopManager.getDefault().getWindowManager()).setWorkspaces(nwss);
    }

    /**
    * @return an array of NewType that can be created
    */
    public NewType[] getNewTypes() {
        return new NewType[] {new WorkspaceNewType(this)};
    }

    void newChildWorkspace(String name) {
        Workspace dtop = new WorkspaceImpl(name);
        addWorkspace(dtop);
    }

    /** Getter for set of actions that should be present in the
    * popup menu of this node. This set is used in construction of
    * menu returned from getContextMenu and specially when a menu for
    * more nodes is constructed.
    *
    * @return array of system actions that should be in popup menu
    */
    public SystemAction[] createActions () {
        return new SystemAction []{
                   SystemAction.get (PasteAction.class),
                   null,
                   SystemAction.get (NewAction.class),
                   null,
                   SystemAction.get (ToolsAction.class),
                   SystemAction.get (PropertiesAction.class),
               };
    }

    private final static class Workspaces extends Children.Keys
        implements PropertyChangeListener {
        /** Holds listener to the workspace pool content */
        PropertyChangeListener propL;

        public void propertyChange (PropertyChangeEvent che) {
            if (che.getPropertyName().equals(WindowManager.PROP_WORKSPACES)) {
                Workspace[] newDesks = (Workspace[]) che.getNewValue();
                setKeys (newDesks);
            }
        }

        /** Initialize the keys from workspace pool */
        protected void addNotify () {
            WindowManager wp = TopManager.getDefault().getWindowManager();
            setKeys (wp.getWorkspaces());
            if (propL == null) {
                propL = WeakListener.propertyChange(this, wp);
            }
            wp.addPropertyChangeListener(propL);
        }

        /** Clear the keys */
        protected void removeNotify () {
            TopManager.getDefault().getWindowManager().
            removePropertyChangeListener(propL);
            setKeys(new Object[0]);
        }

        /** Creates a node for the workspace. */
        protected Node[] createNodes (Object o) {
            return new Node[] { new WorkspaceContext ((Workspace)o) };
        }
    }

}

/*
 * Log
 *  9    Gandalf   1.8         1/16/00  Jesse Glick     Tool tips.
 *  8    Gandalf   1.7         1/12/00  Ian Formanek    NOI18N
 *  7    Gandalf   1.6         11/6/99  David Simonek   new WeakListener 
 *       strategy followed...
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/10/99 Petr Hamernik   console debug messages 
 *       removed.
 *  4    Gandalf   1.3         7/30/99  David Simonek   window icons, comments 
 *       removed
 *  3    Gandalf   1.2         7/28/99  David Simonek   serialization of window 
 *       system...first draft :-)
 *  2    Gandalf   1.1         7/12/99  Jesse Glick     Context help.
 *  1    Gandalf   1.0         7/11/99  David Simonek   
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Jancura     IndexedPanContextSupport...
 *  0    Tuborg    0.12        --/--/98 Ales Novak      arrays used instead of vectors
 *  0    Tuborg    0.13        --/--/98 Ales Novak      changed to nodes
 *  0    Tuborg    0.15        --/--/98 Jan Formanek    popup menu improved
 */
