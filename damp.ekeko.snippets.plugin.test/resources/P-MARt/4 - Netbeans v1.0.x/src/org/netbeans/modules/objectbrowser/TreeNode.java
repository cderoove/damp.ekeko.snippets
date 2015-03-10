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

package org.netbeans.modules.objectbrowser;

import java.util.*;

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
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.Children.SortedMap;
import org.openide.nodes.NodeAdapter;
import org.openide.util.WeakListener;
import org.openide.util.enum.AlterEnumeration;
import org.openide.util.enum.EmptyEnumeration;
import org.openide.util.enum.SingletonEnumeration;
import org.openide.util.enum.SequenceEnumeration;

/**
* Produces package node tree hierarchy.
*
* @author   Jan Jancura
*/
public class TreeNode extends FilterNode {


    // init ..................................................................................

    /**
    * Creates hierarchy of packages for node representing repository.
    */
    TreeNode (Node original) {
        this (original, new DataFilter () {
                  public boolean acceptDataObject (DataObject d) {
                      return d instanceof DataFolder;
                  }
              });
    }

    /**
    * Creates filtered hierarchy of packages for node representing repository.
    */
    TreeNode (Node original, DataFilter dataFilter) {
        super (original, new RootChildren (dataFilter));
    }


    // innerclasses .........................................................................

    static private class RootChildren extends Children.Keys implements RepositoryListener {

        /** Current DataFilter */
        private DataFilter          dataFilter;


        // init ..............................................................................

        /**
        * Create PackageChildren with given filter.
        */
        RootChildren (DataFilter dataFilter) {
            this.dataFilter = dataFilter;
        }


        // Keys support ......................................................................

        /**
        * Lazy initialization method.
        */
        protected void addNotify () {
            refreshKeys ();
            TopManager.getDefault ().getRepository ().addRepositoryListener (
                new WeakListener.Repository (this)
            );
        }

        /**
        * Finds representing node for given FileSystem.
        */
        protected Node[] createNodes (Object key) {
            try {
                if ((!((FileSystem) key).isHidden ()) &&
                        ((FileSystem) key).isValid ()
                   ) {
                    FileObject fo = ((FileSystem) key).getRoot ();
                    DataFolder df = (DataFolder) DataObject.find (fo);
                    if (dataFilter.acceptDataObject (df))
                        return new Node [] {
                                   //              new RootFolderNode (df, Children.LEAF)
                                   df.new FolderNode (new PackageChildren (fo, dataFilter)) {
                                   String name;
                                   public String getDisplayName () {
                                       try {
                                           if (name != null) return name;
                                           FileObject file;
                                           return name = getDataObject ().getPrimaryFile ().getFileSystem ().
                                                         getDisplayName ();
                                       } catch (FileStateInvalidException ee) {
                                           return "???"; // NOI18N
                                       }
                                   }
                               }
                           };
                }
            } catch (DataObjectNotFoundException ee) {
            }
            return new Node [0];
        }

        /**
        * Release keys.
        */
        protected void removeNotify () {
            setKeys (java.util.Collections.EMPTY_SET);
        }


        // RepositoryListener support .........................................................

        /**
        * Adds packages for given FS.
        */
        public void fileSystemAdded (final RepositoryEvent ev) {
            refreshKeys ();
        }

        /**
        * Removes packages of given FS.
        */
        public void fileSystemRemoved (RepositoryEvent ev) {
            refreshKeys ();
        }

        /**
        * Does nothing.
        */
        public void fileSystemPoolReordered (RepositoryReorderedEvent ev) {
        }


        // other methods .....................................................................

        /**
        * Creates keys.
        */
        void refreshKeys () {
            setKeys (TopManager.getDefault ().getRepository ().toArray ());
        }
    }

    static private class PackageChildren extends Children.Keys {

        /** Representing FileObject */
        private FileObject          fileObject;
        /** Current DataFilter */
        private DataFilter          dataFilter;
        /** Listens on package */
        private FileChangeListener  myFerret = new FileChangeAdapter () {
                                                   public void fileFolderCreated (FileEvent fe) {
                                                       refreshKeys ();
                                                   }
                                                   public void fileDeleted (FileEvent fe) {
                                                       refreshKeys ();
                                                   }
                                               };


        // init ..............................................................................

        /**
        * Create PackageChildren with given filter.
        */
        PackageChildren (FileObject fo, DataFilter dataFilter) {
            fileObject = fo;
            this.dataFilter = dataFilter;
        }


        // Keys support ......................................................................

        /**
        * Lazy initialization method.
        */
        protected void addNotify () {
            refreshKeys ();
            fileObject.addFileChangeListener (myFerret);
        }

        /**
        * Finds representing node for given FileSystem.
        */
        protected Node[] createNodes (Object key) {
            try {
                DataFolder df = (DataFolder) DataObject.find ((FileObject) key);
                if (dataFilter.acceptDataObject (df))
                    return new Node [] {
                               df.new FolderNode (new PackageChildren ((FileObject) key, dataFilter))
                           };
            } catch (DataObjectNotFoundException ee) {
            }
            return new Node [0];
        }

        /**
        * Release keys.
        */
        protected void removeNotify () {
            fileObject.removeFileChangeListener (myFerret);
            setKeys (java.util.Collections.EMPTY_SET);
        }


        // RepositoryListener support .........................................................

        /**
        * Adds packages for given FS.
        */
        public void fileSystemAdded (final RepositoryEvent ev) {
            refreshKeys ();
        }

        /**
        * Removes packages of given FS.
        */
        public void fileSystemRemoved (RepositoryEvent ev) {
            refreshKeys ();
        }

        /**
        * Does nothing.
        */
        public void fileSystemPoolReordered (RepositoryReorderedEvent ev) {
        }


        // other methods .....................................................................

        /**
        * Creates keys.
        */
        void refreshKeys () {
            Enumeration e = fileObject.getFolders (false);
            ArrayList al = new ArrayList ();
            while (e.hasMoreElements ())
                al.add (e.nextElement ());
            setKeys (al.toArray ());
        }
    }
}

/*
 * Log
 *  4    Gandalf   1.3         1/13/00  Radko Najman    I18N
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         5/9/99   Jan Jancura     
 * $
 */
