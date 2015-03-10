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

import java.net.URL;
import java.util.Enumeration;

import org.openide.filesystems.*;
import org.openide.execution.NbClassLoader;
import org.openide.util.WeakListener;

/** Classloader for the filesystem pool. Attaches itself as a listener to
 * each file a class has been loaded from. If such a file is deleted, modified
 * or renamed clears the global variable that holds "current" classloader, so
 * on next request for current one new is created.
 *
 * @author Jaroslav Tulach
 */
class ClassLoaderSupport extends NbClassLoader
    implements FileChangeListener, RepositoryListener {
    /** change listener */
    private FileChangeListener listener;

    /** the pool */
    private static Repository pool = NbTopManager.getDefaultRepository ();
    /** holds current classloader (or null if not created yet) */
    private static ClassLoaderSupport current;

    /** @return the current classloader for the system */
    static ClassLoader currentClassLoader () {
        ClassLoader c = current;
        if (c == null) {
            c = createClassLoader ();
        }
        return c;
    }



    /** Creates new classloader. Synchronized to allow only
    * one access the current at time.
    */
    synchronized static ClassLoader createClassLoader () {
        if (current == null) {
            current = new ClassLoaderSupport ();
        }
        return current;
    }

    /** Resets the loader.
    */
    synchronized static void resetLoader () {
        if (current != null) {
            current = null;
        }
    }

    /** Constructor that attaches itself to the filesystem pool.
    */
    public ClassLoaderSupport () {
        Repository rep = NbTopManager.getDefaultRepository ();
        rep.addRepositoryListener (WeakListener.repository (this, rep));
        listener = WeakListener.fileChange (this, null);
    }


    /** When looking for a file, register listener on its changes.
    * @param name of the class
    * @return the Class or null
    */
    protected Class findClass (String name) throws ClassNotFoundException {
        Class c = super.findClass (name);
        if (c != null) {
            FileObject fo;
            int lastDot = name.lastIndexOf ('.');
            if (lastDot == -1) {
                fo = pool.find ("", name, "class"); // NOI18N
            } else {
                fo = pool.find (name.substring (0, lastDot), name.substring (lastDot + 1), "class"); // NOI18N
            }
            if (fo != null) {
                // if the file is from the file system pool,
                // register to catch its changes
                fo.addFileChangeListener (listener);
            }
        }
        return c;
    }


    /** Tests whether this object is current loader and if so,
    * clears the loader.
    * @param fo file object that initiated the action
    */
    private void test (FileObject fo) {
        if (current == this) {
            reset ();
        }
        fo.removeFileChangeListener (listener);
    }

    /** Resets the loader, removes it from listneing on all known objects.
    */
    private synchronized void reset () {
        if (current == this) {
            current = null;
        }
    }

    /** If this object is not current classloader, removes it from
    * listening on given file object.
    */
    private void testRemove (FileObject fo) {
        if (current != this) {
            fo.removeFileChangeListener (listener);
        }
    }



    /** Called when new file system is added to the pool.
    * @param ev event describing the action
    */
    public void fileSystemAdded (RepositoryEvent ev) {
        reset ();
    }

    /** Called when a file system is deleted from the pool.
    * @param ev event describing the action
    */
    public void fileSystemRemoved (RepositoryEvent ev) {
        reset ();
    }

    /** Resets the loader.
    */
    public void fileSystemPoolReordered (RepositoryReorderedEvent ev) {
        reset ();
    }


    /** Fired when a new folder has been created. This action can only be
    * listened in folders containing the created file up to the root of
    * file system.
    *
    * @param fe the event describing context where action has taken place
    */
    public void fileFolderCreated (FileEvent fe) {
        testRemove (fe.getFile ());
    }

    /** Fired when a new file has been created. This action can only be
    * listened in folders containing the created file up to the root of
    * file system.
    *
    * @param fe the event describing context where action has taken place
    */
    public void fileDataCreated (FileEvent fe) {
        testRemove (fe.getFile ());
    }

    /** Fired when a file has been changed.
    * @param fe the event describing context where action has taken place
    */
    public void fileChanged (FileEvent fe) {
        test (fe.getFile ());
    }

    /** Fired when a file has been deleted.
    * @param fe the event describing context where action has taken place
    */
    public void fileDeleted (FileEvent fe) {
        test (fe.getFile ());
    }

    /** Fired when a file has been renamed.
    * @param fe the event describing context where action has taken place
    *           and the original name and extension.
    */
    public void fileRenamed (FileRenameEvent fe) {
        test (fe.getFile ());
    }

    /** Fired when a file attribute has been changed.
    * @param fe the event describing context where action has taken place,
    *           the name of attribute and old and new value.
    */
    public void fileAttributeChanged (FileAttributeEvent fe) {
        testRemove (fe.getFile ());
    }
}

/*
* Log
*  11   Gandalf   1.10        1/13/00  Jaroslav Tulach I18N
*  10   Gandalf   1.9         11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  9    Gandalf   1.8         10/27/99 Petr Hrebejk    Testing of modules added
*  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    Gandalf   1.6         6/10/99  Jaroslav Tulach Updates the loaders when 
*       the file is modified.
*  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  5    Gandalf   1.4         3/26/99  Jaroslav Tulach 
*  4    Gandalf   1.3         3/19/99  Jaroslav Tulach TopManager.getDefault 
*       ().getRegistry ()
*  3    Gandalf   1.2         2/11/99  Ian Formanek    Renamed FileSystemPool ->
*       Repository
*  2    Gandalf   1.1         1/12/99  Jaroslav Tulach Modules are loaded by 
*       URLClassLoader
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
