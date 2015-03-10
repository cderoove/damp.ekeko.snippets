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
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.openide.util.datatransfer.PasteType;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.TopManager;
import org.openide.actions.*;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.InstanceSupport;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WorkspaceImpl;
import org.netbeans.core.windows.toolbars.*;

/** Node that displayes content of one workspace.
*
* @author Ales Novak, Jaroslav Tulach
*/
public final class WorkspaceContext extends AbstractNode
    implements InstanceCookie {
    /** Workspace for which we provide context */
    private WorkspaceImpl workspace;
    /** element children */
    private Elements children;

    /** default constructor
    * @param workspace is a workspace to provide context to
    */
    public WorkspaceContext (Workspace workspace) {
        this (new Elements (workspace), workspace);
    }

    private WorkspaceContext (Elements ch, Workspace aworkspace) {
        super (ch);
        this.workspace = (WorkspaceImpl) aworkspace;
        this.children = ch;
        setName (workspace.getDisplayName());
        setShortDescription (NbBundle.getBundle (WorkspaceContext.class).getString ("HINT_WorkspaceContext"));
        setIconBase ("/org/netbeans/core/resources/workspace"); // NOI18N

        // weak listener on changes in children and name
        workspace.addPropertyChangeListener(
            WeakListener.propertyChange(children, workspace)
        );

        getCookieSet ().add (this);
        getCookieSet ().add (new OpenCookie () {
                                 public void open() {
                                     ((WindowManagerImpl) TopManager.getDefault().getWindowManager()).
                                     setCurrentWorkspace(WorkspaceContext.this.workspace);
                                 }
                             });
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (WorkspaceContext.class);
    }

    /**
    * @param x is an Object to compare with
    * @return true iff this and x are equal
    */
    public boolean equals (Object x) {
        if (x instanceof WorkspaceContext)
            return workspace == ((WorkspaceContext)x).workspace;
        return false;
    }

    /**
    * @return hashcode
    */
    public int hashCode () {
        return workspace.hashCode();
    }

    /** sets workspace name
    * @param x is a new name
    */
    public void setName (String x) {
        super.setName(x);
        ((WorkspaceImpl)workspace).setDisplayName(x);
    }

    /** Yep, workspace could be renamed. */
    public boolean canRename () {
        return true;
    }

    /** Creates properties for this data object */
    protected Sheet createSheet () {
        Sheet s = Sheet.createDefault ();
        Sheet.Set ss = s.get (Sheet.PROPERTIES);
        ss.put (new Node.Property[] {
                    new PropertySupport.Name
                    (this,
                     NbBundle.getBundle (WorkspaceContext.class).getString ("PROP_desk_name"),
                     NbBundle.getBundle (WorkspaceContext.class).getString ("HINT_desk_name")
                    ),
                    new ToolbarConfigurationPropertyEditor (this)
                });
        return s;
    }

    /**
     * @return toolbar configuration name
     */
    public String getToolbarConfigName () {
        return workspace.getToolbarConfigName();
    }

    /**
     * @param name of toolbar configuration for this workspace
     */
    public void setToolbarConfigName (String name) {
        workspace.setToolbarConfigName (name);
    }

    /**
    * @return underlying workspace
    */
    public Workspace getWorkspace () {
        return workspace;
    }

    /** Context menu that should be assigned to this Node.
    * @return the popup menu
    */
    public SystemAction[] createActions () {
        return new SystemAction[] {
                   SystemAction.get (OpenAction.class),
                   null,
                   SystemAction.get (CutAction.class),
                   SystemAction.get (CopyAction.class),
                   SystemAction.get (PasteAction.class),
                   null,
                   SystemAction.get (DeleteAction.class),
                   SystemAction.get (RenameAction.class),
                   null,
                   SystemAction.get (ToolsAction.class),
                   SystemAction.get (PropertiesAction.class)
               };
    }

    /** Default action.
    */
    public SystemAction getDefaultAction () {
        return SystemAction.get (OpenAction.class);
    }

    /** Removes the Node from its parent.
    * It is done via setting parent to null. It is responsibility of parent
    * to listen for that change.
    *
    * @exception NodeAccessException if something wrong occures
    */
    public void destroy () throws IOException {
        workspace.remove();
    }

    /** Can this node be removed?
    * @return <CODE>true</CODE> if can, <CODE>false</CODE> otherwise
    */
    public boolean canDestroy () {
        return workspace != TopManager.getDefault().getWindowManager().getCurrentWorkspace();
    }

    // cut & copy of workspaces ******************************************

    /** @return true/false on workspace !=/== currentWorkspace */
    public boolean canCut () {
        return workspace != TopManager.getDefault().getWindowManager().getCurrentWorkspace();
    }

    /** @return true */
    public boolean canCopy () {
        return true;
    }

    /** Checks for instances of workspace elements.
    */
    protected void createPasteTypes (Transferable t, List s) {
        super.createPasteTypes (t, s);

        Mode e;

        e = element ((InstanceCookie)NodeTransfer.cookie (t, NodeTransfer.COPY, InstanceCookie.class));
        if (e != null) {
            // copy flavor
            s.add (new TopFramePasteType (e, null));
            return;
        }

        // node to be cut
        Node n = NodeTransfer.node (t, NodeTransfer.CLIPBOARD_CUT | NodeTransfer.DND_MOVE);
        if (n != null && n.canDestroy ()) {
            e = element ((InstanceCookie)n.getCookie (InstanceCookie.class));
            s.add (new TopFramePasteType (e, n));
            return;
        }
    }

    /** Takes workspace element from the instance cookie or not.
    * @param cookie instance cookie
    * @return the workspace element
    */
    private Mode element (InstanceCookie cookie) {
        try {
            if (cookie != null && Mode.class.isAssignableFrom (cookie.instanceClass ())) {
                return (Mode)cookie.instanceCreate ();
            }
        } catch (Exception ex) {
        }
        return null;
    }

    /********* implementation of the instance cookie *****?

    /** The bean name for the instance.
    * @return the name
    */
    public String instanceName () {
        return workspace.getName();
    }

    /** The representation type that may be created as instances.
    */
    public Class instanceClass ()
    throws java.io.IOException, ClassNotFoundException {
        return workspace.getClass();
    }

    /** Create new instance of represented workspace.
    */
    public Object instanceCreate ()
    throws java.io.IOException, ClassNotFoundException {
        return new WorkspaceImpl(workspace);
    }

    /** Children elements and listener on changes in the workspace.
    */
    private static class Elements extends Children.Keys implements PropertyChangeListener {
        /** workspace we are attached to */
        private Workspace workspace;

        public Elements (Workspace w) {
            // initialize
            setKeys (w.getModes());
            workspace = w;
        }

        /** Creates new node for given element.
        * @param mode the mode on workspace
        * @return node for it
        */
        protected Node[] createNodes (Object mode) {
            if (mode instanceof ModeImpl)
                return new Node[] { new ModeContext((ModeImpl)mode) };
            // let bean node to take care if type unknnown
            try {
                return new Node[] { new BeanNode (mode) };
            } catch (IntrospectionException ex) {
                return new Node[] { new AbstractNode (Children.LEAF) };
            }
        }

        public void propertyChange (PropertyChangeEvent che) {
            if (che.getPropertyName().equals (Workspace.PROP_MODES)) {
                Mode[] modes = (Mode[])che.getNewValue();
                if (modes == null)
                    setKeys (workspace.getModes());
                else
                    setKeys(modes);
            }

            if (che.getPropertyName().equals (Workspace.PROP_NAME)) {
                String name = (String) che.getNewValue ();
                getNode ().setName (name);
            }
        }
    }

    /** Paste type for workspace elements */
    private class TopFramePasteType extends PasteType {
        /** element to work with */
        private Mode element;
        /** node to destroy or null */
        private Node old;

        /** Constructs new TopFramePasteType for the specific type of operation paste.
        */
        public TopFramePasteType (Mode element, Node old) {
            this.element = element;
            this.old = old;
        }

        /* @return Human presentable name of this paste type. */
        public String getName () {
            return NbBundle.getBundle (WorkspaceContext.class).getString ("PASTE_TopFrame");
        }

        /* @return help */
        public HelpCtx getHelpCtx() {
            return new HelpCtx (TopFramePasteType.class);
        }

        /** Performs the paste action.
        * @return Transferable which should be inserted into the clipboard after
        *         paste action. It can be null, which means that clipboard content
        *         should stay the same.
        */
        public Transferable paste() throws IOException {
            if (old != null) {
                old.destroy ();
            }
            // element.attachTo (workspace);
            Mode nevv = workspace.createMode(
                            element.getName(), element.getDisplayName(),
                            ((ModeImpl)element).getIconURL()
                        );
            TopComponent[] tcs = element.getTopComponents();
            for (int i = 0; i < tcs.length; i++) {
                nevv.dockInto(tcs[i]);
            }
            return null;
        }
    }


    /*
    public ClipboardOperation childClipboardOperation (Object child)
      throws IllegalArgumentException {
      if (!(child instanceof TopFrame)) throw new IllegalArgumentException();
      TopFrame frame = (TopFrame) child;
      Enumeration frames = workspace.getFrameList ();
      while (frames.hasMoreElements ()) {
        if (((TopFrame) frames.nextElement()) == frame)
          return new TopFrameClipboardOperation (workspace, frame);
      }
      throw new IllegalArgumentException();
}*/

    /** Gets action that should be invoked in response to double click or press of
    * enter.
    * @return default action or null if the bean is not currently in state that would
    * allow execution of default action.
    */
    //  public org.openide.util.InvocableAction getDefaultAction() {
    //    return new org.openide.util.InvocableAction () {
    /** Invokes the action. This should for example open new
    * window or create new thread for compilation.
    */
    /*      public void invoke (){
            try {
              TopFrame frame = TopFrame.getRegistry().getSelectedFrame();
              java.awt.Rectangle rec = frame.getBounds ();
              TopManager.getDefault().getWorkspacePool().setCurrentWorkspace (workspace);
              frame.setBounds (rec);
              frame.setVisible (true);
            } catch (java.beans.PropertyVetoException ex) {
              TopManager.getDefault().notifyException (ex);
            }
          }

          public String getName () {
            return workspace.getName ();
          }

    */      /** Help context where to find more about the action.
          * @return null
          */
    /*      public org.openide.util.HelpCtx getHelpCtx () {
            return null XXX;
          }
        };
      } */
}

/*
 * Log
 *  11   Gandalf   1.10        1/16/00  Jesse Glick     Tool tips.
 *  10   Gandalf   1.9         1/12/00  Ian Formanek    NOI18N
 *  9    Gandalf   1.8         12/17/99 David Simonek   #3496
 *  8    Gandalf   1.7         11/6/99  David Simonek   new WeakListener 
 *       strategy followed...
 *  7    Gandalf   1.6         11/3/99  David Simonek   completely rewritten 
 *       serialization of windowing system...
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         7/29/99  David Simonek   further ws serialization
 *       changes
 *  4    Gandalf   1.3         7/28/99  David Simonek   serialization of window 
 *       system...first draft :-)
 *  3    Gandalf   1.2         7/23/99  David Simonek   workspaces in explorer 
 *       now nearly functional
 *  2    Gandalf   1.1         7/12/99  Jesse Glick     Context help.
 *  1    Gandalf   1.0         7/11/99  David Simonek   
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jan Jancura     IndexedDiteContext removed
 *  0    Tuborg    0.13        --/--/98 Ales Novak      set/get Name
 *  0    Tuborg    0.14        --/--/98 Ales Novak      Serializable
 *  0    Tuborg    0.21        --/--/98 Jan Jancura     propertySet
 *  0    Tuborg    0.25        --/--/98 Jaroslav Tulach Workspace.Element is now used
 *  0    Tuborg    0.27        --/--/98 Jan Formanek    icons added
 *  0    Tuborg    0.28        --/--/98 Petr Hamernik   rename cookie
 */
