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

package org.openide.loaders;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.util.datatransfer.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.actions.ReorderAction;
import org.openide.util.HelpCtx;
import org.openide.nodes.*;
import org.openide.util.enum.QueueEnumeration;
import org.openide.util.enum.SequenceEnumeration;
import org.openide.util.enum.RemoveDuplicatesEnumeration;
import org.openide.util.enum.ArrayEnumeration;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/** A folder containing data objects.
* Is actually itself a data object, whose primary (and only) file object
* is a file folder.
* <p>Has special support for determining the sorting of the folder,
* or even explicit ordering of the children.
*
* @author Jaroslav Tulach, Petr Hamernik
*/
public class DataFolder extends DataObject implements Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8244904281845488751L;

    /** Name of property that holds children of this node. */
    public static final String PROP_CHILDREN = "children"; // NOI18N

    /** Name of property which decides sorting mode. */
    public static final String PROP_SORT_MODE = "sortMode"; // NOI18N

    /** name of extended attribute for order of children */
    private static final String EA_ORDER = "OpenIDE-Folder-Order"; // NOI18N
    /** name of extended attribute for order of children */
    private static final String EA_SORT_MODE = "OpenIDE-Folder-SortMode"; // NOI18N

    /** Name of property for order of children. */
    public static final String PROP_ORDER = "order"; // NOI18N
    /** Name of set with sorting options. */
    public static final String SET_SORTING = "sorting"; // NOI18N

    /** Icon resource string for folder node */
    static final String FOLDER_ICON_BASE =
        "/org/openide/resources/defaultFolder"; // NOI18N

    /** name of a shadow file for a root */
    private static final String ROOT_SHADOW_NAME = "Root"; // NOI18N

    /** listener that contains array of children
    * Also represents the folder as the node delegate.
    */
    private FolderList list;

    /** Sort mode for the folder Reference (SortMode)
    */
    private Reference sortMode = new WeakReference (null);

    /** a reference to hold softly the order for this folder */
    private Reference order = new WeakReference (null);

    /** Create a data folder from a folder file object.
    * @param fo file folder to work on
    * @exception DataObjectExistsException if there is one already
    * @exception IllegalArgumentException if <code>fo</code> is not folder
    */
    public DataFolder (FileObject fo)
    throws DataObjectExistsException, IllegalArgumentException {
        this(fo, DataLoaderPool.getFolderLoader ());
    }

    /** Create a data folder from a folder file object.
    * @param fo file folder to work on
    * @param loader data loader for this data object
    * @exception DataObjectExistsException if there is one already
    * @exception IllegalArgumentException if <code>fo</code> is not folder
    */
    protected DataFolder (FileObject fo, DataLoader loader)
    throws DataObjectExistsException, IllegalArgumentException {
        this (fo, loader, true);
    }

    /** Create a data folder from a folder file object.
    * @param fo file folder to work on
    * @param loader data loader for this data object
    * @exception DataObjectExistsException if there is one already
    * @exception IllegalArgumentException if <code>fo</code> is not folder
    */
    private DataFolder (FileObject fo, DataLoader loader, boolean attach)
    throws DataObjectExistsException, IllegalArgumentException {
        super (fo, loader);
        if (!fo.isFolder ()) {
            // not folder => throw an exception
            new IllegalArgumentException ();
        }
        // creates object that handles all elements in array and
        // assignes it to the
        list =  new FolderList (this, attach);
    }

    /** Helper method to find or create a folder of a given path.
    * Tries to find such a subfolder, or creates it if it needs to.
    *
    * @param folder the folder to start in
    * @param name a subfolder path (e.g. <code>com/mycom/testfolder</code>)
    * @return a folder with the given name
    * @exception IOException if the I/O fails
    */
    public static DataFolder create (DataFolder folder, String name) throws IOException {
        return DataFolder.findFolder (FileUtil.createFolder (folder.getPrimaryFile (), name));
    }

    /** Set the sort mode for the folder.
    * @param mode an constant from {@link DataFolder.SortMode}
    * @exception IOException if the mode cannot be set
    */
    public synchronized final void setSortMode (SortMode mode) throws IOException {
        SortMode old = getSortMode ();

        // store the mode to properties
        mode.write (this);

        sortMode = new WeakReference (mode);

        Order ord = getOrder ();
        // first of all use order, then the comparator
        ord.setComparator (mode);
        list.changeComparator ();

        firePropertyChange (PROP_SORT_MODE, old, sortMode);
    }

    /** Getter for comparator. Accessed from FolderChildren.
    */
    final Comparator getComparator () {
        return getOrder ();
    }


    /** Get the sort mode of the folder.
    * @return the sort mode
    */
    public final SortMode getSortMode () {
        SortMode sm = (SortMode)sortMode.get ();
        if (sm == null) {
            sm = SortMode.read (this);
            sortMode = new WeakReference (sm);
        }
        return sm;
    }

    /** Set the order of the children.
     * The provided array defines
    * the order of some children for the folder. Such children
    * will be returned at the beginning of the array returned from
    * {@link #getChildren}. If there are any other children, they
    * will be appended to the array.
    *
    * @param arr array of data objects (children of this
    *   folder) to define the order; or <code>null</code> if any particular ordering should
    *   be cancelled
    *
    * @exception IOException if the order cannot be set
    *
    */
    public synchronized final void setOrder (DataObject[] arr) throws IOException {
        Order ord = arr == null ? new Order () : new Order (this, arr);

        // write the order
        ord.write ();

        order = new WeakReference (ord);

        // adds the order before the comparator
        ord.setComparator (getSortMode ());
        list.changeComparator ();

        firePropertyChange (PROP_ORDER, null, null);
    }

    /** Getter for order object.
    * @return order of children
    */
    private Order getOrder () {
        Order o = (Order)order.get ();
        if (o == null) {
            o = Order.createFor (this);
            o.setComparator (getSortMode ());
            order = new WeakReference (o);
        }

        return o;
    }


    /** Get the children of this folder.
    * @return array of children
    */
    public DataObject[] getChildren () {
        return list.getChildren ();
    }

    /** Getter for list of children.
    * @param filter filter to notify about addition of new objects
    */
    final ArrayList getChildrenList () {
        return list.getChildrenList ();
    }

    /** Computes list of children asynchronously
    * @param l listener to notify about the progress
    * @return task that will handle the computation
    */
    final RequestProcessor.Task computeChildrenList (FolderListListener l) {
        return list.computeChildrenList (l);
    }


    /** Refreshes the list of children. Called when a child data object is
    * disposed.
    */
    final void refresh () {
        list.refresh ();
    }

    /** Method that allows FolderList to fire info about change of
    * objects
    */
    final void fireChildrenChange (ArrayList add, ArrayList removed) {
        if (!add.isEmpty() || !removed.isEmpty()) {
            firePropertyChange (PROP_CHILDREN, null, null);
        }
    }

    /** Get enumeration of children of this folder.
    * @return enumeration of {@link DataObject}s
    */
    public Enumeration children () {
        return Collections.enumeration (getChildrenList ());
    }

    /** Adds a {@link CompilerCookie compilation cookie}.
    */
    public Node.Cookie getCookie (Class cookie) {
        // is somebody asking for folder compiler?
        if (CompilerCookie.class.isAssignableFrom (cookie)) {
            DataFolderCompiler c = new DataFolderCompiler (this, cookie);
            if (cookie.isInstance (c)) {
                return c;
            }
            // else go on, folder compiler does not implement such cookie
        }
        // does anybody wants to reorder folder?
        if (org.openide.nodes.Index.class.isAssignableFrom(cookie)) {
            return new Index(this);
        }
        // end testing
        return super.getCookie (cookie);
    }

    /** Create node representative for this folder.
    */
    protected synchronized Node createNodeDelegate () {
        return new FolderNode ();
    }

    /** This method allows DataFolder to filter its nodes.
    *
    * @param filter filter for subdata objects
    * @return the node delegate (without parent) for this data object
    */
    final Node getClonedNodeDelegate (DataFilter filter) {
        // creates normal node and filters its children
        Node n = getNodeDelegate ();
        return new FilterNode (n, createNodeChildren (filter));
    }

    /** Support method to obtain a children object that
    * can be added to any {@link Node}. The provided filter can be
    * used to exclude some objects from the list.
    *
    * @param filter filter of data objects
    * @return children object representing content of this folder
    */
    public final Children createNodeChildren (DataFilter filter) {
        return new FolderChildren (this, filter);
    }

    /* Getter for delete action.
    * @return true if the object can be deleted
    */
    public boolean isDeleteAllowed () {
        return isRenameAllowed ();
    }

    /* Getter for copy action.
    * @return true if the object can be copied
    */
    public boolean isCopyAllowed () {
        return true;
    }

    /* Getter for move action.
    * @return true if the object can be moved
    */
    public boolean isMoveAllowed () {
        return isRenameAllowed ();
    }

    /* Getter for rename action.
    * @return true if the object can be renamed
    */
    public boolean isRenameAllowed () {
        FileObject fo = getPrimaryFile ();
        return !fo.isRoot() && !fo.isReadOnly ();
    }

    /* Help context for this object.
    * @return help context
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (DataFolder.class);
    }

    /** Create a folder for a specified file object.
    * @param fo file object
    * @return folder for the file object
    * @exception IllegalArgumentException if the file object is not folder
    */
    public static DataFolder findFolder (FileObject fo) {
        try {
            return (DataFolder)DataObject.find (fo);
        } catch (ClassCastException ex) {
        } catch (DataObjectNotFoundException ex) {
        }
        // either fo is not data folder or
        // has been recognized by something else => system is bad
        throw new IllegalArgumentException ();
    }

    /** Copy this object to a folder.
     * The copy of the object is required to
    * be deletable and movable.
    *
    * @param f the folder to copy object to
    * @exception IOException if something went wrong
    * @return the new object
    */
    protected DataObject handleCopy (DataFolder f) throws IOException {
        String name = getPrimaryFile ().getName ();
        // Fix 4513    name = FileUtil.findFreeFolderName (f.getPrimaryFile (), name);

        FileObject newFile = FileUtil.createFolder (f.getPrimaryFile (), name);

        DataFolder newFolder;
        try {
            newFolder = new DataFolder (newFile);
        } catch (DataObjectExistsException ex) {
            if (ex.getDataObject() instanceof DataFolder) {
                newFolder = (DataFolder)ex.getDataObject ();
            } else {
                throw ex;
            }
        }

        Enumeration en = children ();

        while (en.hasMoreElements ()) {
            try {
                DataObject obj = (DataObject)en.nextElement ();
                obj.copy (newFolder);
            } catch (IOException ex) {
                TopManager.getDefault ().notifyException (ex);
            }
        }

        try {
            FileUtil.copyAttributes (getPrimaryFile (), newFile);
        } catch (IOException ioe) {
            TopManager.getDefault ().notifyException (ioe);
        }

        return newFolder;
    }

    /* Deals with deleting of the object. Must be overriden in children.
    * @exception IOException if an error occures
    */
    protected void handleDelete () throws IOException {
        FileObject fo = getPrimaryFile ();
        FileLock lock = fo.lock ();
        try {
            Enumeration en = children ();

            while (en.hasMoreElements ()) {
                try {
                    DataObject obj = (DataObject)en.nextElement ();
                    if (obj.isValid ()) {
                        obj.delete ();
                    }
                } catch (IOException ex) {
                    TopManager.getDefault ().notifyException (ex);
                }
            }


            fo.delete (lock);
        } finally {
            lock.releaseLock ();
        }
    }

    /* Handles renaming of the object.
    * Must be overriden in children.
    *
    * @param name name to rename the object to
    * @return new primary file of the object
    * @exception IOException if an error occures
    */
    protected FileObject handleRename (String name) throws IOException {
        FileObject fo = getPrimaryFile ();
        FileLock lock = fo.lock ();
        try {
            fo.rename (lock, name, null);
        } finally {
            lock.releaseLock ();
        }
        return fo;
    }

    /* Handles move of the object. Must be overriden in children.
    *
    * @param df target data folder
    * @return new primary file of the object
    * @exception IOException if an error occures
    */
    protected FileObject handleMove (DataFolder df) throws IOException {
        FileObject fo = getPrimaryFile ();
        FileLock lock = fo.lock ();
        DataFolder newFolder = null;

        try {
            String name = getPrimaryFile ().getName ();
            name = FileUtil.findFreeFolderName (df.getPrimaryFile (), name);

            FileObject newFile = df.getPrimaryFile ().createFolder (name);

            // temporary folder for moving into
            newFolder = new DataFolder (newFile);

            Enumeration en = children ();

            while (en.hasMoreElements ()) {
                try {
                    DataObject obj = (DataObject)en.nextElement ();
                    obj.move (newFolder);
                } catch (IOException ex) {
                    TopManager.getDefault ().notifyException (ex);
                }
            }

            try {
                FileUtil.copyAttributes (fo, newFile);
            } catch (IOException ioe) {
                TopManager.getDefault ().notifyException (ioe);
            }

            fo.delete (lock);

            // disposes temporary folder and places itself instead of it
            newFolder.dispose ();

            newFolder.list.reassign(this);
            list = newFolder.list;

            // changes primary file of this folder to the new folder
            return newFile;
        } catch (IOException e) {
            if (newFolder != null) {
                /* in the case there would be the new folder - with a FolderList. 
                * The FolderList is without a FileChangeListener!!!
                */
                newFolder.list.reassign(newFolder);
            }
            throw e;
        } finally {
            lock.releaseLock ();
        }
    }

    /* Creates new object from template.
    * @param f folder to create object in
    * @return new data object
    * @exception IOException if an error occured
    */
    protected DataObject handleCreateFromTemplate (
        DataFolder f, String name
    ) throws IOException {
        if (name == null) name = getPrimaryFile ().getName ();

        FileObject newFile = f.getPrimaryFile ().createFolder (name);
        DataFolder newFolder = new DataFolder (newFile);

        Enumeration en = children ();

        while (en.hasMoreElements ()) {
            try {
                DataObject obj = (DataObject)en.nextElement ();
                obj.copy (newFolder);
            } catch (IOException ex) {
                TopManager.getDefault ().notifyException (ex);
            }
        }

        try {
            FileUtil.copyAttributes (getPrimaryFile (), newFile);
        } catch (IOException ioe) {
            TopManager.getDefault ().notifyException (ioe);
        }
        DataObject.setTemplate (newFile, false);

        return newFolder;
    }

    /** Creates shadow for this object in specified folder (overridable in subclasses).
     * <p>The default
    * implementation creates a reference data shadow and pastes it into
    * the specified folder.
    *
    * @param f the folder to create a shortcut in
    * @return the shadow
    */
    protected DataShadow handleCreateShadow (DataFolder f) throws IOException {
        String name;
        if (getPrimaryFile ().isRoot ()) {
            name = FileUtil.findFreeFileName (
                       f.getPrimaryFile (), ROOT_SHADOW_NAME, DataShadow.SHADOW_EXTENSION
                   );
        } else {
            name = null;
        }

        return DataShadow.create (f, name, this);
    }

    /** Support for index cookie for folder nodes.
    */
    public static class Index extends org.openide.nodes.Index.Support {

        /** Asociated data folder */
        private DataFolder df;
        /** node to be associated with */
        private Node node;
        /** change listener */
        private Listener listener;

        /** Create an index cookie associated with a data folder.
         * @param df the data folder
        */
        public Index(final DataFolder df) {
            this (df, df.getNodeDelegate ());
        }

        /** Create an index cookie associated with a data folder.
        * @param df the data folder
        * @param node node to be associated with. subnodes of this node will be returned, etc.
        */
        public Index(final DataFolder df, Node node) {
            this.df = df;
            this.node = node;
            listener = new Listener ();
            node.addNodeListener (WeakListener.node (listener, node));
        }

        /* Returns the index of given node.
        * @param node Node to find index of.
        * @return Index of the node, -1 if no such node was found.
        */
        public int indexOf (final Node node) {
            Node[] nodes = getNodes();
            for (int i = 0; i < nodes.length; i++) {
                if (node.equals (nodes[i])) return i;
            }
            // not found
            return -1;
        }

        /* Returns count of the nodes.
        */
        public int getNodesCount () {
            return node.getChildren().getNodesCount();
        }

        /* Returns array of subnodes
        * @return array of subnodes
        */
        public Node[] getNodes () {
            return node.getChildren().getNodes();
        }

        /* Reorders all children with given permutation.
        * @param perm permutation with the length of current nodes
        * @exception IllegalArgumentException if the perm is not
        *  valid permutation
        */
        public void reorder (int[] perm) {
            // testing
            /*System.out.println ("Permutation: ");
            for (int i = 0; i < perm.length; i++) {
              System.out.println ("From " + i + " to " + perm[i]);
        }*/
            DataObject[] curObjs = df.getChildren();
            //testing
            /*      System.out.println ("Folder children: ");
                  for (int i = 0; i < curObjs.length; i++) {
                    System.out.println (i + "  " + curObjs[i].toString());
                  } */
            DataObject[] newObjs = new DataObject[curObjs.length];
            // permute data objects
            int targetIndex;
            for (int i = 0; i < curObjs.length; i++) {
                targetIndex = perm[i];
                if (newObjs[targetIndex] != null)
                    throw new IllegalArgumentException("Bad input permutation"); // NOI18N
                newObjs[targetIndex] = curObjs[i];
            }
            try {
                df.setOrder(newObjs);
            } catch (java.io.IOException ex) {
                TopManager.getDefault().notify(
                    new NotifyDescriptor.Exception(ex,
                                                   DataObject.getString("EXC_ReorderFailed")));
            }
        }

        /* Invokes a dialog for reordering subnodes.
        */
        public void reorder () {
            IndexedCustomizer ic = new IndexedCustomizer();
            ic.setObject(this);
            // turn off immediate reorder so that children are reordered
            // at once when closing the dialog
            ic.setImmediateReorder(false);
            ic.show();
        }

        /** Fires notification about reordering to all
        * registered listeners.
        */
        void fireChangeEventAccess () {
            fireChangeEvent (new ChangeEvent (this));
        }

        /** Listener to change of children of the folder.
        */
        private final class Listener extends Object implements NodeListener {
            /** Change of children?
            */
            public void propertyChange (PropertyChangeEvent ev) {
            }
            /** Fired when the node is deleted.
            * @param ev event describing the node
            */
            public void nodeDestroyed(NodeEvent ev) {
            }

            /** Fired when the order of children is changed.
            * @param ev event describing the change
            */
            public void childrenReordered(NodeReorderEvent ev) {
                fireChangeEventAccess ();
            }
            /** Fired when a set of children is removed.
            * @param ev event describing the action
            */
            public void childrenRemoved(NodeMemberEvent ev) {
                fireChangeEventAccess ();
            }
            /** Fired when a set of new children is added.
            * @param ev event describing the action
            */
            public void childrenAdded(NodeMemberEvent ev) {
                fireChangeEventAccess ();
            }
        } // end of Listener

    } // end of Index inner class


    /** Type-safe enumeration of sort modes for data folders.
    */
    public abstract static class SortMode extends Object implements Comparator {
        /** Objects are unsorted. */
        public static final SortMode NONE = new FolderComparator (FolderComparator.NONE);

        /** Objects are sorted by their names. */
        public static final SortMode NAMES = new FolderComparator (FolderComparator.NAMES);

        /** Objects are sorted by their types and then by names. */
        public static final SortMode CLASS = new FolderComparator (FolderComparator.CLASS);

        /** Folders go first (sorted by name) followed by non-folder
        * objects sorted by name.
        */
        public static final SortMode FOLDER_NAMES = new FolderComparator (FolderComparator.FOLDER_NAMES);

        /** Method to write the sort mode to a folder's attributes.
        * @param folder folder write this mode to
        */
        void write (DataFolder f) throws IOException {
            if (f.getPrimaryFile ().getFileSystem ().isReadOnly ()) return; // cannot write to read-only FS

            String x;
            if (this == FOLDER_NAMES) x = null;
            else if (this == NAMES) x = "N"; // NOI18N
            else if (this == CLASS) x = "C"; // NOI18N
            else x = "O"; // NOI18N

            f.getPrimaryFile ().setAttribute (EA_SORT_MODE, x);
        }

        /** Reads sort mode for given folder.
        */
        static SortMode read (DataFolder f) {
            String x = (String)f.getPrimaryFile ().getAttribute (EA_SORT_MODE);
            if (x == null || x.length () != 1) return FOLDER_NAMES;

            char c = x.charAt (0);
            switch (c) {
            case 'N': return NAMES;
            case 'C': return CLASS;
            default: return NONE;
            }
        }
    }

    /** Class that represents order of child data objects.
    */
    private static final class Order extends Object implements Comparator {
        /** map of primary files of objects to their index (FileObject, Integer) 
         * @associates Integer*/
        private HashMap order;
        /** comparator to use when comparing files not in the map */
        private transient Comparator comp;
        /** file to store data in */
        private FileObject folder;

        /** Constructor.
        * @param folder the folder to create order for
        * @param arr array that defines the order
        */
        public Order (DataFolder folder, DataObject[] arr) {
            order = new HashMap (arr.length);

            // each object only once
            RemoveDuplicatesEnumeration en = new RemoveDuplicatesEnumeration (
                                                 new ArrayEnumeration (arr)
                                             );

            int i = 0;
            while (en.hasMoreElements ()) {
                DataObject obj = (DataObject)en.nextElement ();
                if (obj.getFolder () == folder) {
                    // object for my folder
                    FileObject fo = obj.getPrimaryFile ();
                    order.put (fo, new Integer (i++));
                }
            }
            this.folder = folder.getPrimaryFile ();
        }

        /** Constructor.
        * @param s set with ordering
        */
        private Order (HashMap s) {
            order = s;
        }

        /** Constructs empty order.
        */
        public Order () {
            order = new HashMap (1);
        }

        /** Sets the right comparator.
        */
        void setComparator (Comparator c) {
            comp = c;
        }

        /** Compares two data object or two nodes.
        */
        public int compare (Object o1, Object o2) {
            DataObject obj1;
            DataObject obj2;

            if (o1 instanceof Node) {
                obj1 = (DataObject)((Node)o1).getCookie (DataObject.class);
                obj2 = (DataObject)((Node)o2).getCookie (DataObject.class);
            } else {
                obj1 = (DataObject)o1;
                obj2 = (DataObject)o2;
            }

            Integer i1 = (Integer)order.get (obj1.getPrimaryFile ());
            Integer i2 = (Integer)order.get (obj2.getPrimaryFile ());

            if (i1 == null) {
                if (i2 != null) return 1;
                // compare by the provided comparator
                return comp.compare (obj1, obj2);
            } else {
                if (i2 == null) return -1;
                // compare integers
                if (i1.intValue () == i2.intValue ()) return 0;
                if (i1.intValue () < i2.intValue ()) return -1;
                return 1;
            }
        }

        /** Stores the order to files.
        */
        public void write () throws IOException {
            write (false);
        }

        /** Clears an order.
        */
        public void clear () throws IOException {
            write (true);
        }

        /** Stores the order to files.
        * @param clear true if we should only clear the order
        */
        private void write (boolean clear) throws IOException {
            if (folder.getFileSystem ().isReadOnly ()) return; // cannot write to read-only FS
            //System.out.println ("Writing order, clear? " + clear); // NOI18N
            if (clear || order.isEmpty ()) {
                // if we should clear the object or the order is empty
                folder.setAttribute (EA_ORDER, null);
            } else {
                java.util.Iterator it = order.entrySet ().iterator ();

                int size = order.size ();
                String[] names = new String[size];
                String[] exts = new String[size];
                while (it.hasNext ()) {
                    Map.Entry en = (Map.Entry)it.next ();
                    FileObject fo = (FileObject)en.getKey ();
                    int indx = ((Integer)en.getValue ()).intValue ();
                    names[indx] = fo.getName ();
                    exts[indx] = fo.getExt ();
                }
                folder.setAttribute (EA_ORDER, new String[][] { names, exts });
            }
        }

        /** Creates order for given folder object.
        * @param f the folder
        * @return the order
        */
        public static Order createFor (DataFolder f) {
            FileObject folder = f.getPrimaryFile ();

            String[][] namesExts = (String[][])folder.getAttribute (EA_ORDER);

            if (namesExts == null) {
                // empty order
                return new Order ();
            }

            String[] names = namesExts[0];
            String[] exts = namesExts[1];

            if (names == null || exts == null || names.length != exts.length) {
                // empty order
                return new Order ();
            }


            HashMap set = new HashMap (names.length);

            for (int i = 0; i < names.length; i++) {
                FileObject fo = folder.getFileObject (names[i], exts[i]);
                if (fo != null) {
                    // found
                    set.put (fo, new Integer (i));
                }
            }
            return new Order (set);
        }
    }

    /** Node for a folder.
    */
    public class FolderNode extends DataNode {
        /** Create a folder node with some children.
        * @param ch children to use for the node
        */
        public FolderNode (Children ch) {
            super (DataFolder.this, ch);
            setIconBase(FOLDER_ICON_BASE);
        }

        /** Create a folder node with default folder children.
        */
        protected FolderNode () {
            super (DataFolder.this, new FolderChildren (DataFolder.this));
            setIconBase(FOLDER_ICON_BASE);
        }

        /** Renames the folder, but forbids names with a space.
        * @param folderName name to rename folder to
        * @exception IllegalArgumentException if the name is not valid
        */
        public void setName (String folderName) {
            if (!Utilities.isJavaIdentifier (folderName)) {
                throw new IllegalArgumentException (
                    java.text.MessageFormat.format (DataObject.getString("EXC_WrongName"), new Object[] { folderName} )
                );
            }
            super.setName (folderName);
        }

        /** Adds properties for sorting.
         * @return the augmented property sheet
        */
        protected Sheet createSheet () {
            Sheet s = super.createSheet ();

            Sheet.Set ss = new Sheet.Set ();
            ss.setName (SET_SORTING);
            ss.setDisplayName (DataObject.getString ("PROP_sorting"));
            ss.setShortDescription (DataObject.getString ("HINT_sorting"));

            Node.Property p;

            p = new PropertySupport.ReadWrite (
                    PROP_SORT_MODE, SortMode.class,
                    DataObject.getString("PROP_sort"),
                    DataObject.getString("HINT_sort")
                ) {
                    public Object getValue () {
                        return DataFolder.this.getSortMode ();
                    }

                    public void setValue (Object o) throws InvocationTargetException {
                        try {
                            DataFolder.this.setSortMode ((SortMode)o);
                        } catch (IOException ex) {
                            throw new InvocationTargetException (ex);
                        }
                    }

                    public java.beans.PropertyEditor getPropertyEditor () {
                        return new SortModeEditor ();
                    }
                };
            ss.put (p);

            s.put (ss);
            return s;
        }

        /** New type for creating new subfolder.
        * @return array with one element
        */
        public NewType[] getNewTypes () {
            if (getPrimaryFile ().isReadOnly ()) {
                // no new types
                return new NewType[0];
            } else {
                return new NewType[] { new NewFolder () };
            }
        }

        /** Look for one or more nodes with data object cookie in this transferable.
        * @param t the transferable to probe
        * @param actions copy-style or move-style
        * @return a list of data objects (nonempty), or <code>null</code> if these are not present
        */
        private DataObject[] findDataObjectsInTransferable (Transferable t, int actions) {
            Node[] nodes = NodeTransfer.nodes (t, actions);
            //System.err.println ("NodeTransfer.nodes=" + nodes);
            if (nodes == null) return null;
            //System.err.println ("nodes: " + nodes.length);
            //for (int i1 = 0; i1 < nodes.length; i1++)
            //  System.err.println ("\t" + nodes[i1].getDisplayName ());
            DataObject[] dobs = new DataObject[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                DataObject dob = (DataObject) nodes[i].getCookie (DataObject.class);
                if (dob == null)
                    return null;
                else
                    dobs[i] = dob;
            }
            return dobs;
        }

        /** May add some paste types for objects being added to folders.
        * May move data objects; copy them; create links for them; instantiate
        * them as templates; serialize instances; or create instance data objects
        * from instances, according to the abilities of the transferable.
        *
        * @param t transferable to use
        * @param s list of {@link PasteType}s
        */
        protected void createPasteTypes (Transferable t, java.util.List s) {
            super.createPasteTypes (t, s);

            if (!getPrimaryFile ().isReadOnly ()) {

                boolean ok;
                int i;

                // Permit moving of multiple objects, provided that they all declare themselves movable.
                DataObject[] objs = findDataObjectsInTransferable (t, NodeTransfer.MOVE);
                if (objs != null) {
                    //System.err.println ("Got movable objs: " + objs.length);
                    //for (int i1 = 0; i1 < objs.length; i1++)
                    //  System.err.println ("\t" + objs[i1].getName ());
                    ok = true;
                    for (i = 0; i < objs.length; i++) {
                        if (! objs[i].isMoveAllowed ()) {
                            ok = false;
                            break;
                        }
                    }
                    //System.err.println ("[move] ok=" + ok);
                    if (ok) {
                        // add move paste type, the type then clears clipboard
                        s.add (new Paste ("PT_move", objs, true, "move") { // NOI18N
                                   public void handle (DataObject obj2) throws IOException {
                                       obj2.move (DataFolder.this);
                                   }
                               });
                    }
                }

                // Now try copy-style pastes.
                // [PENDING] should NodeTransfer.COPY be used instead?
                objs = findDataObjectsInTransferable (t, NodeTransfer.CLIPBOARD_COPY);
                if (objs != null) {
                    //System.err.println ("Got copyable objs: " + objs.length);
                    //for (int i2 = 0; i2 < objs.length; i2++)
                    //  System.err.println ("\t" + objs[i2].getName ());

                    ok = true;
                    for (i = 0; i < objs.length; i++) {
                        if (! objs[i].isCopyAllowed ()) {
                            ok = false;
                            break;
                        }
                    }
                    //System.err.println ("[copy] ok=" + ok);
                    if (ok) {
                        // copy all objects to this folder
                        s.add (new Paste ("PT_copy", objs, false, "copy") { // NOI18N
                                   public void handle (DataObject obj2) throws IOException {
                                       obj2.copy (DataFolder.this);
                                   }
                               });
                    }

                    ok = true;
                    for (i = 0; i < objs.length; i++) {
                        if (! objs[i].isTemplate ()) {
                            ok = false;
                            break;
                        }
                    }
                    //System.err.println ("[template] ok=" + ok);
                    if (ok) {
                        // instantiate template(s) with default name
                        s.add (new Paste ("PT_instantiate", objs, false, "instantiate") { // NOI18N
                                   public void handle (DataObject obj2) throws IOException {
                                       obj2.createFromTemplate (DataFolder.this);
                                   }
                               });
                    }

                    ok = true;
                    for (i = 0; i < objs.length; i++) {
                        if (! objs[i].isShadowAllowed ()) {
                            ok = false;
                            break;
                        }
                    }
                    //System.err.println ("[shadow] ok=" + ok);
                    if (ok) {
                        // instantiate template
                        s.add (new Paste ("PT_shadow", objs, false, "shadow") { // NOI18N
                                   public void handle (DataObject obj2) throws IOException {
                                       obj2.createShadow (DataFolder.this);
                                   }
                               });
                    }
                }

                // These should only accept single-node transfers, since they require dialogs.
                Node node = NodeTransfer.node (t, NodeTransfer.CLIPBOARD_COPY);

                // lastly try special cookies
                if (node != null) {
                    try {
                        InstanceCookie cookie = (InstanceCookie)node.getCookie (InstanceCookie.class);
                        if (cookie != null && java.io.Serializable.class.isAssignableFrom (cookie.instanceClass ())) {
                            s.add (new SerializePaste (cookie));
                            s.add (new InstantiatePaste (cookie));
                        }
                    } catch (IOException e) {
                    } catch (ClassNotFoundException e) {
                    }
                }
            }
        }
    } // end of FolderNode

    /** New type for creation of new folder.
    */
    private final class NewFolder extends NewType {
        /** Display name for the creation action. This should be
        * presented as an item in a menu.
        *
        * @return the name of the action
        */
        public String getName() {
            return DataObject.getString ("CTL_NewFolder");
        }

        /** Help context for the creation action.
        * @return the help context
        */
        public HelpCtx getHelpCtx() {
            return new HelpCtx (NewFolder.class);
        }

        /** Create the object.
        * @exception IOException if something fails
        */
        public void create () throws IOException {
            NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine (
                                                   DataObject.getString ("CTL_NewFolderName"), DataObject.getString ("CTL_NewFolderTitle")
                                               );
            input.setInputText (DataObject.getString ("CTL_NewFolderValue"));
            if (TopManager.getDefault ().notify (input) == NotifyDescriptor.OK_OPTION) {
                String folderName = input.getInputText ();
                if ("".equals (folderName)) return; // empty name = cancel // NOI18N

                FileObject folder = getPrimaryFile ();
                int dotPos = -1;

                while ((dotPos = folderName.indexOf (".")) != -1) { // NOI18N
                    String subFolder = folderName.substring (0, dotPos);
                    folderName = folderName.substring (dotPos + 1);


                    FileObject existingFile = folder.getFileObject (subFolder);
                    if (existingFile != null) {
                        if (!existingFile.isFolder ()) {
                            TopManager.getDefault ().notify (
                                new NotifyDescriptor.Message (
                                    java.text.MessageFormat.format (DataObject.getString ("MSG_FMT_FileExists"), new Object[] { subFolder, folder.getName () }),
                                    NotifyDescriptor.WARNING_MESSAGE
                                )
                            );
                            return;
                        }
                        folder = existingFile;
                    } else {
                        if (!Utilities.isJavaIdentifier (subFolder)) {
                            throw new IOException(
                                java.text.MessageFormat.format (getString("EXC_WrongName"), new Object[] { subFolder } )
                            );
                        }
                        folder = folder.createFolder (subFolder);
                    }
                }
                if (!"".equals (folderName)) { // NOI18N
                    FileObject existingFile = folder.getFileObject (folderName);
                    if (existingFile != null) {
                        if (existingFile.isFolder ()) {
                            TopManager.getDefault ().notify (
                                new NotifyDescriptor.Message (
                                    java.text.MessageFormat.format (DataObject.getString ("MSG_FMT_FolderExists"), new Object[] { folderName, folder.getName () }),
                                    NotifyDescriptor.INFORMATION_MESSAGE
                                )
                            );
                        } else {
                            TopManager.getDefault ().notify (
                                new NotifyDescriptor.Message (
                                    java.text.MessageFormat.format (DataObject.getString ("MSG_FMT_FileExists"), new Object[] { folderName, folder.getName () }),
                                    NotifyDescriptor.WARNING_MESSAGE
                                )
                            );
                        }
                        return;
                    }

                    if (!Utilities.isJavaIdentifier (folderName)) {
                        throw new IOException(
                            java.text.MessageFormat.format (getString("EXC_WrongName"), new Object[] { folderName} )
                        );
                    }

                    DataObject created = DataObject.find(folder.createFolder (folderName));
                    if (created != null) {
                        TopManager.getDefault().getLoaderPool().fireOperationEvent(
                            new OperationEvent.Copy (created, DataFolder.this), OperationEvent.TEMPL
                        );
                    }
                }
            }
        }
    }

    /** Paste types for data objects.
    */
    private abstract class Paste extends PasteType {
        private String resName;
        private DataObject[] objs;
        private boolean clearClipboard;
        private String helpCtxSuffix;

        /** @param resName resource name for the name
        * @param objs objects to work with
        * @param clear true if we should clear clipboard
        * @param helpCtxSuffix extra info token for context help
        */
        public Paste (String resName, DataObject[] objs, boolean clear, String helpCtxSuffix) {
            this.resName = resName;
            this.objs = objs;
            this.clearClipboard = clear;
            this.helpCtxSuffix = helpCtxSuffix;
            //System.err.println ("Creating paste: resName=" + resName + " objs.length=" + objs.length);
        }

        /** The name is obtained from the bundle.
        * @return the name
        */
        public String getName () {
            return DataObject.getString (resName);
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (Paste.class.getName () + "." + helpCtxSuffix); // NOI18N
        }

        /** Paste.
        */
        public final Transferable paste () throws IOException {
            for (int i = 0; i < objs.length; i++)
                handle (objs[i]);
            // clear clipboard or preserve content
            return clearClipboard ? ExTransferable.EMPTY : null;
        }

        /** Handles the right action
        * @param obj the data object to operate on
        */
        protected abstract void handle (DataObject obj) throws IOException;
    }

    /** Paste types for data objects.
    */
    private final class SerializePaste extends PasteType {
        private InstanceCookie cookie;

        /**
        * @param obj object to work with
        */
        public SerializePaste (InstanceCookie cookie) {
            this.cookie = cookie;
        }

        /** The name is obtained from the bundle.
        * @return the name
        */
        public String getName () {
            return DataObject.getString ("PT_serialize");
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (SerializePaste.class);
        }

        /** Paste.
        */
        public final Transferable paste () throws IOException {
            String name = cookie.instanceName ();
            int i = name.lastIndexOf ('.') + 1;
            if (i != 0 && i != name.length ()) {
                name = name.substring (i);
            }

            name = FileUtil.findFreeFileName (getPrimaryFile (), name, "ser"); // NOI18N


            final NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine (
                                                      DataObject.getString ("SerializeBean_Text"),
                                                      DataObject.getString ("SerializeBean_Title")
                                                  );
            nd.setInputText (name);

            if (NotifyDescriptor.OK_OPTION == TopManager.getDefault ().notify (nd)) {
                getPrimaryFile ().getFileSystem ().runAtomicAction (new FileSystem.AtomicAction () {
                            public void run () throws IOException {
                                FileObject fo = getPrimaryFile ().createData (nd.getInputText (), "ser"); // NOI18N
                                FileLock lock = fo.lock ();
                                ObjectOutputStream oos = null;
                                try {
                                    oos = new ObjectOutputStream (
                                              new java.io.BufferedOutputStream (fo.getOutputStream (lock))
                                          );
                                    oos.writeObject (cookie.instanceCreate ());
                                } catch (ClassNotFoundException e) {
                                    throw new IOException (e.getMessage ());
                                } finally {
                                    if (oos != null) oos.close ();
                                    lock.releaseLock ();
                                }
                            }
                        });
            }

            // preserve clipboard
            return null;
        }
    }

    /** Paste types for data objects.
    */
    private final class InstantiatePaste extends PasteType {
        private InstanceCookie cookie;

        /**
        * @param obj object to work with
        */
        public InstantiatePaste (InstanceCookie cookie) {
            this.cookie = cookie;
        }

        /** The name is obtained from the bundle.
        * @return the name
        */
        public String getName () {
            return DataObject.getString ("PT_instance");
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (InstantiatePaste.class);
        }

        /** Paste.
        */
        public final Transferable paste () throws IOException {
            String name = cookie.instanceName ();

            final NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine (
                                                      DataObject.getString ("InstanceClass_Text"),
                                                      DataObject.getString ("InstanceClass_Title")
                                                  );
            nd.setInputText (name);

            if (NotifyDescriptor.OK_OPTION == TopManager.getDefault ().notify (nd)) {
                name = nd.getInputText ();
                if (name.equals ("")) { // NOI18N
                    name = null;
                }

                try {
                    // create the instance
                    InstanceDataObject.create (DataFolder.this, name, cookie.instanceClass ());
                } catch (ClassNotFoundException ex) {
                    throw new IOException (ex.getMessage ());
                }
            }

            // preserve clipboard
            return null;
        }

    }

}

/*
 * Log
 *  19   Gandalf   1.10.1.7    11/12/98 Jaroslav Tulach
 *  18   Gandalf   1.10.1.6    11/09/98 Ian Formanek
 *  17   Gandalf   1.10.1.5    11/05/98 Jaroslav Tulach Special properties for
 *                                                      Folder.
 *  16   Gandalf   1.10.1.4    11/05/98 Jaroslav Tulach Sorting and ordering of
 *                                                      DataFolders.
 *
 *  15   Gandalf   1.10.1.3    11/04/98 Jaroslav Tulach
 *  14   Gandalf   1.10.1.2    11/03/98 Jaroslav Tulach
 *  13   Gandalf   1.10.1.1    10/30/98 Jaroslav Tulach
 *  12   Gandalf   1.10.1.0    10/30/98 Jaroslav Tulach
 *  11   Tuborg    1.10        10/26/98 Ian Formanek    Context Help updated
 *  10   Tuborg    1.9         10/08/98 Jaroslav Tulach Rename, delete, new
 *                                                      disabled on read only
 *                                                      folders.
 *
 *  9    Tuborg    1.8         09/30/98 Ian Formanek    Enhanced "paste-if-exists"
 *                                                      functionality - the "_1"
 *                                                      suffix is added to the
 *                                                      existing name.
 *  8    Tuborg    1.7         08/21/98 Jaroslav Tulach serialVersionUID + compiles
 *  7    Tuborg    1.6         08/21/98 Jaroslav Tulach serialVersionUID
 *  6    Tuborg    1.5         07/28/98 Petr Hamernik   search cookie - hierarchy
 *  5    Tuborg    1.4         07/22/98 Petr Hamernik   Search cookie first
 *                                                      implementation
 *  4    Tuborg    1.3         06/28/98 Jaroslav Tulach Delete allowed for nonroot
 *                                                      fodlers.
 *
 *  3    Tuborg    1.2         06/22/98 Petr Hamernik   bugfix  235
 *  2    Tuborg    1.1         06/15/98 Ian Formanek
 *  1    Tuborg    1.0         06/11/98 David Peroutka
 * $
 * Beta Change History:
 *  0    Tuborg    1.00        --/--/98 Jaroslav Tulach Total redesign
 *  0    Tuborg    1.01        --/--/98 Jaroslav Tulach copy operation works
 *  0    Tuborg    1.02        --/--/98 Jaroslav Tulach rename operation
 *  0    Tuborg    1.03        --/--/98 Petr Hamernik   compilation
 *  0    Tuborg    1.04        --/--/98 Jan Formanek    help context
 *  0    Tuborg    1.05        --/--/98 Jan Formanek    reflecting changes in cookies
 */
