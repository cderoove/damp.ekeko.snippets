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

package org.netbeans.core;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import org.openide.util.datatransfer.PasteType;
import org.openide.util.datatransfer.NewType;
import org.openide.TopManager;
import org.openide.filesystems.Repository;
import org.openide.filesystems.RepositoryListener;
import org.openide.filesystems.RepositoryEvent;
import org.openide.filesystems.RepositoryReorderedEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.netbeans.core.actions.AddJarAction;
import org.netbeans.core.actions.AddFSAction;
import org.netbeans.core.actions.UnmountFSAction;
import org.openide.actions.*;
import org.openide.loaders.InstanceSupport;
import org.openide.cookies.InstanceCookie;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;

import org.netbeans.core.actions.*;

/** This object represents all filesystems in the filesystem pool, allow access
* to their contents.
*
* @author Jaroslav Tulach, Ian Formanek, Petr Hamernik, Ales Novak
*/
class FSPoolNode extends AbstractNode implements RepositoryListener {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -4473075136923511393L;

    /** a reference to FSPoolNode */
    private static FSPoolNode fsPoolNode;

    /** used in constructor and readObject */
    private FSPoolNode (DSMap children) {
        super (children);
        init ();
        setIconBase ("/org/netbeans/core/resources/repository"); // NOI18N
        setName(NbBundle.getBundle(FSPoolNode.class).getString("CTL_FSPool_name"));
        setShortDescription(NbBundle.getBundle(FSPoolNode.class).getString("HINT_FSPool_name"));
        getCookieSet ().add (new Index ());
        getCookieSet ().add (new InstanceSupport.Instance (NbTopManager.getDefault ().getRepository ()));
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (FSPoolNode.class);
    }

    /** Creates new FSPoolNode */
    private static FSPoolNode createFSPoolNode() {
        FSPoolNode ret = new FSPoolNode(new DSMap ());
        return ret;
    }

    /** @return an instance of FSPoolNode */
    public static FSPoolNode getFSPoolNode() {
        if (fsPoolNode == null)
            fsPoolNode = createFSPoolNode();
        return fsPoolNode;
    }

    /** Initialize */
    private void init () {
        Repository rep = NbTopManager.getDefaultRepository ();
        rep.addRepositoryListener (WeakListener.repository (this, rep));
        refresh ();
    }

    /** Gets a handle object for serialization. */
    public Handle getHandle() {
        return new FSNHandle();
    }

    /** @return possible actions on FSPool */
    public SystemAction[] createActions() {
        return new SystemAction[] {
                   SystemAction.get (AddFSAction.class),
                   SystemAction.get (AddJarAction.class),
                   null,
                   SystemAction.get (ReorderAction.class),
                   null,
                   SystemAction.get (PasteAction.class),
                   null,
                   SystemAction.get(NewAction.class),
                   null,
                   SystemAction.get(ToolsAction.class),
                   SystemAction.get(PropertiesAction.class)
               };
    }

    /** @return available new types */
    public NewType[] getNewTypes () {
        return ModuleFSSection.listOfNewTypes(true);
    }

    /** Allows paste of file systems.
    */
    protected void createPasteTypes (Transferable t, List l) {
        InstanceCookie cookie = (InstanceCookie)NodeTransfer.cookie (t,
                                NodeTransfer.DND_COPY | NodeTransfer.CLIPBOARD_COPY, InstanceCookie.class
                                                                    );
        try {
            if (cookie != null && FileSystem.class.isAssignableFrom (cookie.instanceClass ())) {
                l.add (new Paste (cookie));
            }
        } catch (IOException e) {
            // ignore
        } catch (ClassNotFoundException e) {
            // ignore
        }

    }

    /** FSNHandle a handle for serialization */
    static class FSNHandle implements Handle {
        static final long serialVersionUID =-1379725782887327704L;
        public Node getNode() {
            return getFSPoolNode();
        }
    }

    /** Called when new file system is added to the pool.
    * @param ev event describing the action
    */
    public void fileSystemAdded (RepositoryEvent ev) {
        refresh ();
    }

    /** Called when a file system is deleted from the pool.
    * @param ev event describing the action
    */
    public void fileSystemRemoved (RepositoryEvent ev) {
        refresh ();
    }
    /** Called when the fsp is reordered */
    public void fileSystemPoolReordered(RepositoryReorderedEvent ev) {
        refresh ();
    }

    /** Refreshes the pool.
    */
    void refresh () {
        refresh (null);
    }

    /** Refreshes the pool.
    * @param fs file system to remove
    */
    void refresh (FileSystem fs) {
        ((DSMap)getChildren ()).refresh (fs);
    }


    /** A Node for filesystems. Redefines remove() to implement removing of
    * filesystems
    */
    static class FSNode extends BeanNode implements PropertyChangeListener {
        /** The filesystem represented by this node */
        private FileSystem fs;

        /** Constructs a new FSNode for specified filesystem.
        * @param system the filesystem for which we are constructint the node
        */
        public FSNode(final FileSystem system) throws IntrospectionException {
            super(system);
            fs = system;
            fs.addPropertyChangeListener (WeakListener.propertyChange (this, fs));
            propertyChange (null);
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (FSNode.class);
        }

        public void propertyChange (PropertyChangeEvent ev) {
            super.setName (fs.getDisplayName ());
        }

        /** Creates the sheet.
        */
        protected void createProperties(Object bean, BeanInfo info) {
            super.createProperties (bean, info);

            FileSystemCapability cap = ((FileSystem)bean).getCapability ();
            try {
                if (cap != null) {
                    BeanInfo bi = Introspector.getBeanInfo (cap.getClass (), FileSystemCapability.class);
                    Descriptor d = computeProperties (cap, bi);

                    Sheet.Set ss = new Sheet.Set ();
                    ss.setName ("Capabilities"); // NOI18N
                    ss.setDisplayName (Main.getString ("PROP_Capabilities"));
                    ss.setShortDescription (Main.getString ("HINT_Capabilities"));

                    ss.put (d.property);
                    ss.put (d.expert);

                    getSheet ().put (ss);
                }
            } catch (IntrospectionException e) {
            }
        }


        /** Can remove?
        */
        public boolean canDestroy () {
            return !fs.isDefault ();
        }

        /** Can cut?
        */
        public boolean canCut () {
            return canDestroy ();
        }

        /** Removes the filesystem from Repository.
        */
        public void destroy () {
            NbTopManager.getDefaultRepository ().removeFileSystem (fs);
        }

        /** Clipboard cut removes the fs and does normal copy.
        */
        public Transferable clipboardCut () throws IOException {
            Transferable t = clipboardCopy ();
            destroy ();
            return t;
        }

        /** @return possible actions on this dataobject */
        public SystemAction[] getActions() {
            return new SystemAction[] {
                       SystemAction.get (MoveUpAction.class),
                       SystemAction.get (MoveDownAction.class),
                       null,
                       SystemAction.get (CutAction.class),
                       SystemAction.get (CopyAction.class),
                       null,
                       SystemAction.get(UnmountFSAction.class),
                       null,
                       SystemAction.get(ToolsAction.class),
                       SystemAction.get(PropertiesAction.class)
                   };
        }

        /** Returns the cookie (set of behaviour) for this node.
        * The result can then be checked by <CODE>result instanceof MyCookie</CODE>,
        * this is valid even the returned value is <CODE>null</CODE> because
        * <CODE>null instanceof Anything = false</CODE>.
        *
        * @return this implementation returns <CODE>null</CODE>

        * @see Cookie
        */
        public Node.Cookie getCookie (Class c) {
            if (c == UnmountFSCookie.class && !fs.isDefault ()) {
                return new UnmountFSCookie() {
                           /** Unmounts the filesystem. Calls remove() method */
                           public void unmount() {
                               FSNode.this.destroy ();
                           }
                       };
            }
            return super.getCookie (c);
        }
    }


    /** Children that listens to changes in filesystem pool.
    */
    static class DSMap extends Children.Keys {
        protected Node[] createNodes (Object key) {
            FileSystem fs = (FileSystem)key;
            try {
                return new Node[] { new FSNode (fs) };
            } catch (java.beans.IntrospectionException ex) {
                return new Node[] {};
            }
        }

        /** Refreshes the pool.
        * @param fileSystemPool the pool
        * @param fs file system to remove
        */
        public void refresh (FileSystem fs) {
            Repository fileSystemPool = NbTopManager.getDefaultRepository ();
            Enumeration en = fileSystemPool.getFileSystems ();
            ArrayList list = new ArrayList ();
            while (en.hasMoreElements ()) {
                Object o = en.nextElement ();
                if (fs != o) {
                    list.add (o);
                }
            }
            setKeys (list);
        }
    }


    /** Index support for reordering of file system pool.
    */
    private final class Index extends org.openide.nodes.Index.Support {
        /** Get the nodes; should be overridden if needed.
        * @return the nodes
        * @throws NotImplementedException always
        */
        public Node[] getNodes () {
            return getChildren ().getNodes ();
        }

        /** Get the node count. Subclasses must provide this.
        * @return the count
        */
        public int getNodesCount () {
            return getNodes ().length;
        }

        /** Reorder by permutation. Subclasses must provide this.
        * @param perm the permutation
        */
        public void reorder (int[] perm) {
            NbTopManager.getDefaultRepository ().reorder (perm);
        }

    }


    /** Pastes an object from to the repository.
    */
    private static final class Paste extends PasteType {
        private InstanceCookie cookie;

        public Paste (InstanceCookie c) {
            cookie = c;
        }

        /** Name of the paste type */
        public String getName () {
            return NbBundle.getBundle (Paste.class).getString ("PT_filesystem");
        }

        /** Creates the instance and puts it into the repository */
        public Transferable paste () throws IOException {
            try {
                FileSystem fs = (FileSystem)cookie.instanceCreate ();
                // PENDING check whether the filesystem is already there or not
                TopManager.getDefault ().getRepository ().addFileSystem (fs);
            } catch (ClassNotFoundException e) {
                throw new org.openide.util.io.OperationException (e);
            } catch (ClassCastException e) {
                throw new org.openide.util.io.OperationException (e);
            }

            // keep content
            return null;
        }
    }
}

/*
 * Log
 *  24   Gandalf   1.23        1/13/00  Jaroslav Tulach I18N
 *  23   Gandalf   1.22        1/11/00  Jesse Glick     Context help.
 *  22   Gandalf   1.21        12/8/99  Jaroslav Tulach Add FS + Jar actions in 
 *       menu
 *  21   Gandalf   1.20        11/11/99 Jesse Glick     Display miscellany.
 *  20   Gandalf   1.19        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  19   Gandalf   1.18        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  18   Gandalf   1.17        9/3/99   Jaroslav Tulach Customize action.
 *  17   Gandalf   1.16        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  16   Gandalf   1.15        7/8/99   Jesse Glick     Context help.
 *  15   Gandalf   1.14        6/30/99  Jaroslav Tulach Drag and drop support
 *  14   Gandalf   1.13        6/9/99   Ian Formanek    ToolsAction
 *  13   Gandalf   1.12        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  12   Gandalf   1.11        6/7/99   Jaroslav Tulach FS capabilities.
 *  11   Gandalf   1.10        5/26/99  Ian Formanek    changed incorrect usage 
 *       of getBundle
 *  10   Gandalf   1.9         5/9/99   Ian Formanek    setDisplayName -> 
 *       setName as recommended for AbstractNode
 *  9    Gandalf   1.8         5/4/99   Jaroslav Tulach No new directory & jar 
 *       in Repository node.
 *  8    Gandalf   1.7         3/27/99  Jaroslav Tulach Support for serializing 
 *       beans into folder + implemented for control panel and repository
 *  7    Gandalf   1.6         3/21/99  Jaroslav Tulach Repository displayed ok.
 *  6    Gandalf   1.5         3/19/99  Jaroslav Tulach TopManager.getDefault 
 *       ().getRegistry ()
 *  5    Gandalf   1.4         3/5/99   Ales Novak      
 *  4    Gandalf   1.3         2/25/99  Jaroslav Tulach Change of clipboard 
 *       management  
 *  3    Gandalf   1.2         2/11/99  Ian Formanek    Renamed FileSystemPool 
 *       -> Repository
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    moved to package org.netbeans.core
 *  0    Tuborg    0.13        --/--/98 Jaroslav Tulach change number of constructors
 *  0    Tuborg    0.14        --/--/98 Jan Formanek    small bug in addChildren/removeChildren fixed
 *  0    Tuborg    0.16        --/--/98 Jaroslav Tulach rename of isLinkAllowed to isShadowAllowed
 *  0    Tuborg    0.20        --/--/98 Jan Formanek    SWITCHED TO NODES
 *  0    Tuborg    0.21        --/--/98 Jan Formanek    bugfix
 *  0    Tuborg    0.22        --/--/98 Ales Novak      serializable
 *  0    Tuborg    0.23        --/--/98 Petr Hamernik   small change
 *  0    Tuborg    0.24        --/--/98 Jan Formanek    bugfix
 *  0    Tuborg    0.25        --/--/98 Jan Formanek    FSNode added for filesystems to achieve removing of FSs
 *  0    Tuborg    0.26        --/--/98 Jaroslav Tulach to new node model
 *  0    Tuborg    0.27        --/--/98 Jaroslav Tulach subclasses SystemObject
 *  0    Tuborg    0.28        --/--/98 Petr Hamernik   changed to Node !!
 */
