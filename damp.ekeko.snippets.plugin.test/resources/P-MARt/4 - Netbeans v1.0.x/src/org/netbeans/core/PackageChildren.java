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

import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;

import org.openide.TopManager;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.RepositoryListener;
import org.openide.filesystems.RepositoryEvent;
import org.openide.filesystems.RepositoryReorderedEvent;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataLoaderPool;
import org.openide.nodes.FilterNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.Children.SortedMap;
import org.openide.nodes.NodeAdapter;
import org.openide.util.WeakListener;
import org.openide.util.enum.AlterEnumeration;
import org.openide.util.enum.EmptyEnumeration;
import org.openide.util.enum.SingletonEnumeration;
import org.openide.util.enum.SequenceEnumeration;

/** Children that contains list of packages in repository.
*
* @author Jaroslav Tulach, Jan Jancura
*/
final class PackageChildren extends Children.Keys
    implements PropertyChangeListener {
    /** filters which hides folders */
    private static final DataFilter DATA_FILTER = new NoFolderFilter ();

    /** Current DataFilter */
    private DataFilter dataFilter;

    /** holder of all packages */
    private Packages packages;

    /** weak listener */
    private PropertyChangeListener weakPCL;

    // init ..............................................................................

    /** Creates a package node for given filter.
    * @param f the filter to use
    */
    public static Node createNode (DataFilter f) {
        return new AbstractNode (new PackageChildren (f));
    }

    /**
    * Create PackageChildren with given filter.
    */
    private PackageChildren (DataFilter dataFilter) {
        this.dataFilter = dataFilter;
    }

    /* Ask for list of packages
    */
    protected void addNotify () {
        packages = Packages.getDefault ();
        weakPCL = WeakListener.propertyChange (this, packages);
        packages.addPropertyChangeListener (weakPCL);
        packages.update (this);
    }

    /** Clear all nodes */
    protected void removeNotify () {
        setKeys (java.util.Collections.EMPTY_SET);
        packages.removePropertyChangeListener (weakPCL);
        packages = null;
        weakPCL = null;
    }

    public void propertyChange(final java.beans.PropertyChangeEvent p1) {
        if (Packages.PROP_LIST.equals (p1.getPropertyName ())) {
            Packages p = packages;
            if (p != null) {
                p.update (this);
            }
            return;
        }

        if (Packages.PROP_NAME.equals (p1.getPropertyName ())) {
            if (isInitialized ()) {
                Children.MUTEX.readAccess (new Runnable () {
                                               public void run () {
                                                   FileObject changed = (FileObject)p1.getNewValue ();
                                                   Node[] arr = getNodes ();
                                                   for (int i = 0; i < arr.length; i++) {
                                                       DataFolder df = (DataFolder)arr[i].getCookie (DataFolder.class);
                                                       if (df != null && subfolder (changed, df.getPrimaryFile ())) {
                                                           arr[i].setDisplayName (df.getPrimaryFile ().getPackageName ('.'));
                                                       }
                                                   }
                                               }
                                           });

            }
        }
    }

    /** Test whether one file object is subfile of another.
    * @param folder the folder
    * @param subfile the subfile
    * @return true if folder contains subfile
    */
    private static boolean subfolder (FileObject folder, FileObject subfile) {
        for (;;) {
            if (subfile == null) return false;
            if (subfile.equals (folder)) return true;
            subfile = subfile.getParent ();
        }
    }

    /** Callback method from Packages class when it is safe to update
    * packages.
    */
    public void updatePackages (TreeSet set) {
        if (dataFilter == DataFilter.ALL) {
            // use whole set
            setKeys (set);
        } else {
            // filter some nodes
            LinkedList ll = new LinkedList ();
            Iterator it = set.iterator ();
            while (it.hasNext ()) {
                FileObject fo = (FileObject)it.next ();
                try {
                    DataObject obj = DataObject.find (fo);
                    if (dataFilter.acceptDataObject (obj)) {
                        ll.add (fo);
                    }
                } catch (DataObjectNotFoundException ex) {
                    // ignore
                }
            }
            setKeys (ll);
        }
    }

    /** Create children for a data-object key.
    * If {@link ElementCookie} is provided, then the proxy node's children
    * are used for this node's children (for this key), after possible filtering
    * based on the {@link #putFilter current filters}.
    * If <code>ElementCookie</code> is not provided, then
    * (a copy of) this data's object's delegate node is used as the sole child
    * for this key.
    * @param key a {@link DataObject} to create representative children for
    * @return a list of child nodes for this key
    */
    protected Node[] createNodes (Object key) {
        FileObject fo = (FileObject) key;
        try {
            DataObject obj = DataObject.find (fo);
            if (obj instanceof DataFolder) {
                DataFolder df = (DataFolder)obj;
                DataFilter filter =
                    dataFilter == DataFilter.ALL ? DATA_FILTER : dataFilter;

                Node n;
                if (fo.isRoot ()) {
                    n = new RootFolderNode (
                            df, df.createNodeChildren (filter)
                        );
                } else {
                    DataFolder.FolderNode fn = df.new FolderNode (
                                                   df.createNodeChildren (filter)
                                               );
                    fn.setDisplayName (df.getPrimaryFile ().getPackageName ('.'));
                    n = fn;
                }
                return new Node[] { n };
            }
        } catch (DataObjectNotFoundException ee) {
        }
        return new Node[] {};
    }

    /** DataFilter that (does not) accepts data folders.
    */
    private static final class NoFolderFilter extends Object
        implements DataFilter {
        /** Should the data object be displayed or not?
        * @param obj the data object
        * @return <CODE>true</CODE> if the object should be displayed,
        *    <CODE>false</CODE> otherwise
        */
        public boolean acceptDataObject (DataObject obj) {
            return ! (obj instanceof DataFolder);
        }
    }
}

/*
* Log
*  3    Gandalf   1.2         1/9/00   Jaroslav Tulach Renaming works better.
*  2    Gandalf   1.1         11/29/99 Jaroslav Tulach Deleted PNode inner class
*  1    Gandalf   1.0         11/29/99 Jaroslav Tulach 
* $ 
*/ 
