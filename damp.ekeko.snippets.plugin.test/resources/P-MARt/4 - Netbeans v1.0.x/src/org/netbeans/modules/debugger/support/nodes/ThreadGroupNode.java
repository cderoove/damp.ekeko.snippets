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

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.ref.WeakReference;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.SwingUtilities;

import org.openide.TopManager;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.debugger.DebuggerException;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.Mutex;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.debugger.support.DebuggerModule;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.AbstractThreadGroup;
import org.netbeans.modules.debugger.support.ThreadGroupAdapter;
import org.netbeans.modules.debugger.support.ThreadGroupListener;
import org.netbeans.modules.debugger.support.actions.SuspendCookie;
import org.netbeans.modules.debugger.support.actions.SuspendAction;
import org.netbeans.modules.debugger.support.actions.ResumeAction;

/**
* This class represents threadGroup as a Node.
* This class is final only for performance reasons,
* can be happily unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class ThreadGroupNode extends AbstractNode implements
    SuspendCookie {
    /** generated Serialized Version UID */
    static final long                   serialVersionUID = -4736310787523747464L;


    // static ....................................................................

    /** Property names */
    public static final String          PROP_GROUP_NAME       = "threadName"; // NOI18N

    protected static Image              threadGroupIcon;
    protected static Image              threadGroupCurrentIcon;

    /** System actions of this node */
    private static SystemAction[]       staticActions;

    /** Icon paths. */
    protected static final String       ICON_CURRENT =
        "/org/netbeans/modules/debugger/resources/threadGroupCurrent"; // NOI18N
    protected static final String       ICON_BASE =
        "/org/netbeans/modules/debugger/resources/threadGroup"; // NOI18N

    private static ResourceBundle       bundle;


    // variables .................................................................

    private transient AbstractThreadGroup threadGroup;
    private transient TGListener        TGListener;

    /** Where to add variables. */
    private VariableHome                variableHome;
    private boolean                     current = false;


    // init ......................................................................

    /**
    * Creates thread group context with given thread group
    */
    public ThreadGroupNode (
        VariableHome variableHome,
        AbstractThreadGroup threadGroup
    ) {
        super (new ThreadGroupContextChildren (variableHome));
        this.variableHome = variableHome;
        this.threadGroup = threadGroup;
        try {
            String s;
            setDisplayName (s = threadGroup.getName ());
            setName (s);
        } catch (DebuggerException e) {
            String s;
            setDisplayName (s = getLocalizedString ("CTL_Thread_group"));
            setName (s);
        }
        initialize();
    }

    /**
    * Remove listeners.
    */
    protected void finalize () {
        threadGroup.removeThreadGroupListener (TGListener);
    }

    private void initialize () {
        current = threadGroup.isCurrent ();
        if (current)
            setIconBase (ICON_CURRENT);
        else
            setIconBase (ICON_BASE);
        createProperties();
        getCookieSet ().add (this);
        if (TGListener == null) TGListener = new TGListener (this);
        threadGroup.addThreadGroupListener (TGListener);
    }


    // Node implementation .......................................................

    /** Getter for set of actions that should be present in the
    * popup menu of this node. This set is used in construction of
    * menu returned from getContextMenu and specially when a menu for
    * more nodes is constructed.
    *
    * @return array of system actions that should be in popup menu
    */
    public SystemAction[] getActions () {
        if (staticActions == null)
            staticActions = new SystemAction[] {
                                SystemAction.get (SuspendAction.class),
                                SystemAction.get (ResumeAction.class),
                                null,
                                SystemAction.get (ToolsAction.class),
                                SystemAction.get (PropertiesAction.class)
                            };
        return staticActions;
    }

    /** Obtain handle for this node (for serialization).
    * The handle can be serialized and {@link Handle#getNode} used after
    * deserialization to obtain the original node.
    *
    * @return the handle, or <code>null</code> if this node is not persistable
    */
    public Node.Handle getHandle () {
        return new ThreadsHandle ();
    }

    /** Creates property sets */
    private void createProperties () {
        // default sheet with "properties" property set // NOI18N
        Sheet sheet = Sheet.createDefault ();
        Sheet.Set ps = sheet.get (Sheet.PROPERTIES);
        ps.put (new PropertySupport.ReadOnly (
                    PROP_GROUP_NAME,
                    String.class,
                    getLocalizedString ("PROP_group_name"),
                    getLocalizedString ("HINT_group_name")
                ) {
                    public Object getValue () throws InvocationTargetException {
                        try {
                            return threadGroup.getName ();
                        } catch (Exception e) {
                            throw new InvocationTargetException (e);
                        }
                    }
                });
        // and set new sheet
        setSheet(sheet);
    }

    public void destroy () throws java.io.IOException {
        super.destroy ();
        threadGroup.removeThreadGroupListener (TGListener);
    }


    // SuspendCookie .............................................................

    /**
    * Returns state of thread.
    */
    public boolean isSuspended () {
        // throw new InternalError ();
        return false;
    }

    /**
    * Sets state of thread.
    */
    public void setSuspended (boolean suspended) {
        threadGroup.setSuspended (suspended);
    }


    // helper methods ............................................................

    /**
    * @return localized string.
    */
    static String getLocalizedString (String s) {
        if (bundle == null)
            bundle = NbBundle.getBundle (ThreadGroupNode.class);
        return bundle.getString (s);
    }

    AbstractThreadGroup getDebuggerThreadGroup () {
        return threadGroup;
    }

    void changeProperties () {
        if ((threadGroup == null) || (current == threadGroup.isCurrent ()))
            return;
        current = threadGroup.isCurrent ();
        if (current)
            setIconBase (ICON_CURRENT);
        else
            setIconBase (ICON_BASE);
    }


    // inner classes .............................................................

    private static class TGListener extends ThreadGroupAdapter {

        private WeakReference             node;
        private AbstractThreadGroup       threadGroup;

        TGListener (ThreadGroupNode n) {
            node = new WeakReference (n);
            threadGroup = n.threadGroup;
        }

        public void threadGroupCreated (AbstractThreadGroup dtg) {
            updateChindren ();
        }

        public void threadGroupDeath (AbstractThreadGroup dtg) {
            updateChindren ();
        }

        public void threadCreated (AbstractThread tg) {
            updateChindren ();
        }

        public void threadDeath (AbstractThread dt) {
            updateChindren ();
        }

        public void propertyChange (PropertyChangeEvent e) {
            ThreadGroupNode tgn = getTG ();
            if (tgn == null) return;
            tgn.changeProperties ();
            updateChindren ();
        }

        private void updateChindren () {
            ThreadGroupNode tgn = getTG ();
            if (tgn == null) return;

            ThreadGroupContextChildren myChildren =
                (ThreadGroupContextChildren) tgn.getChildren ();
            myChildren.update ();
        }

        private ThreadGroupNode getTG () {
            ThreadGroupNode tgn = (ThreadGroupNode) node.get ();
            if (tgn == null)
                threadGroup.removeThreadGroupListener (this);
            return tgn;
        }
    }

    /** Special Thread group context subnodes (children) */
    private static final class ThreadGroupContextChildren extends Children.Keys {

        /** Where to add variables. */
        private VariableHome                variableHome;
        private boolean                     initialized = false;

        ThreadGroupContextChildren (VariableHome variableHome) {
            this.variableHome = variableHome;
        }

        /** Create a node for one data object.
        * @param key DataObject
        */
        protected Node[] createNodes (Object key) {
            if (key instanceof AbstractThreadGroup)
                return new Node [] {
                           new ThreadGroupNode (variableHome, (AbstractThreadGroup) key)
                       };
            else
                return new Node [] {
                           new ThreadNode (variableHome, (AbstractThread) key)
                       };
        }

        /** Initializes the children.
        */
        protected void addNotify () {
            initialized = true;
            AbstractThreadGroup threadGroup = ((ThreadGroupNode) getNode ()).
                                              threadGroup;
            TGListener TGListener = ((ThreadGroupNode) getNode ()).TGListener;
            if (TGListener == null)
                TGListener = ((ThreadGroupNode) getNode ()).TGListener =
                                 new TGListener (
                                     (ThreadGroupNode) getNode ()
                                 );
            threadGroup.addThreadGroupListener (TGListener);
            update ();
        }

        /** Deinitializes the children.
        */
        protected void removeNotify () {
            AbstractThreadGroup threadGroup = ((ThreadGroupNode) getNode ()).
                                              threadGroup;
            TGListener TGListener = ((ThreadGroupNode) getNode ()).TGListener;
            threadGroup.removeThreadGroupListener (TGListener);
            setKeys (java.util.Collections.EMPTY_SET);
            initialized = false;
        }

        void update () {
            if (!initialized) return;
            // add existing threads and groups ...
            AbstractThreadGroup threadGroup = ((ThreadGroupNode) getNode ()).
                                              threadGroup;
            AbstractThreadGroup[] groups = threadGroup.getThreadGroups ();
            AbstractThread[] threads = threadGroup.getThreads ();
            ArrayList l = new ArrayList ();
            l.addAll (Arrays.asList (groups));
            l.addAll (Arrays.asList (threads));

            setKeys (l);
        }
    } // end of ThreadGroupContextChildren inner class

    /**
    * Serializable node reference. 
    */
    private static class ThreadsHandle implements Node.Handle {
        static final long serialVersionUID = -4518262935887219735L;

        /** Reconstitute the node for this handle.
        *
        * @return the node for this handle
        * @exception IOException if the node cannot be created
        */
        public Node getNode () {
            return DebuggerModule.getThreadGroupsRootNode ();
        }
    }
}

/*
 * Log
 *  12   Gandalf-post-FCS1.10.3.0    3/28/00  Daniel Prusa    
 *  11   Gandalf   1.10        1/14/00  Daniel Prusa    NOI18N
 *  10   Gandalf   1.9         1/13/00  Daniel Prusa    NOI18N
 *  9    Gandalf   1.8         1/6/00   Jan Jancura     Refresh of Threads & 
 *       Watches, Weakization of Nodes
 *  8    Gandalf   1.7         12/23/99 Daniel Prusa    Bug 4557
 *  7    Gandalf   1.6         11/8/99  Jan Jancura     Somma classes renamed
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/1/99  Jan Jancura     Bug 3677
 *  4    Gandalf   1.3         9/3/99   Jan Jancura     
 *  3    Gandalf   1.2         9/2/99   Jan Jancura     
 *  2    Gandalf   1.1         8/18/99  Jan Jancura     Localization & Current 
 *       thread & Current session
 *  1    Gandalf   1.0         8/17/99  Jan Jancura     
 * $
 */
