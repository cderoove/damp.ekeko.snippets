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

import java.util.*;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystemCapability;
import java.lang.ref.*;

/** Registraction list of all data objects in the system.
* Maps data objects to its handlers.
*
* @author Jaroslav Tulach
*/
final class DataObjectPool extends Object
    implements javax.swing.event.ChangeListener {
    /** hashtable that maps FileObject to DataObjectPool.Item 
     * @associates Item*/
    private HashMap map = new HashMap ();
    /** the pool for all objects */
    static final DataObjectPool POOL = new DataObjectPool ();
    static {
        org.openide.TopManager.getDefault ().getLoaderPool ().addChangeListener (
            POOL
        );
    }

    static boolean debug (FileObject fo) {
        return fo.getParent () != null && fo.getParent ().isRoot ();
    }

    /** validator */
    private static final Validator VALIDATOR = new Validator ();

    /** Checks whether there is a data object with primary file
    * passed thru the parameter.
    *
    * @param fo the file to check
    * @return data object with fo as primary file or null
    */
    public synchronized DataObject find (FileObject fo) {
        Item doh = (Item)map.get (fo);
        return doh == null ? null : doh.getDataObjectOrNull ();
    }

    /** Refresh of all folders.
    */
    private void refreshAllFolders () {
        Set files;
        synchronized (this) {
            files = new HashSet (map.keySet ());
        }

        Iterator it = files.iterator ();
        while (it.hasNext ()) {
            FileObject fo = (FileObject)it.next ();
            if (fo.isFolder ()) {
                DataObject obj = find (fo);
                if (obj instanceof DataFolder) {
                    DataFolder df = (DataFolder)obj;
                    df.refresh ();
                }
            }
        }
    }

    /** Rescans all fileobjects in given set.
    * @param s set of FileObjects
    * @return set of DataObjects that refused to be revalidated
    */
    public Set revalidate (Set s) {
        return VALIDATOR.revalidate (s);
    }

    /** Registers new DataObject instance.
    * @param fo primary file for obj
    * @param loader the loader of the object to be created
    *
    * @return object with common information for this <CODE>DataObject</CODE>
    * @exception DataObjectExistsException if the file object is already registered
    */
    public synchronized Item register (FileObject fo, DataLoader loader)
    throws DataObjectExistsException {
        Item doh = (Item)map.get (fo);
        // if Item for this file has not been created yet
        if (doh == null) {
            doh = new Item (fo);
            map.put (fo, doh);

            VALIDATOR.notifyRegistered (fo);

            return doh;
        } else {
            DataObject obj = doh.getDataObjectOrNull ();

            if (obj == null || VALIDATOR.reregister (obj, loader)) {
                // the item is to be finalize => create new

                doh = new Item (fo);
                map.put (fo, doh);

                return doh;
            }


            // PENDING only for debug
            //      System.err.println ("Original stack (" + doh.getDataObject () + "): ");
            //      System.err.println (doh.toString ());
            //      Thread.dumpStack();
            // throw exception with the existing data object
            final String s = doh.toString ();
            throw new DataObjectExistsException (obj) {
                public String getMessage () {
                    return s;
                }
            };
        }
    }

    /** Deregister.
    * @param item the item with common information to deregister
    * @param refresh true if the parent folder should be refreshed
    */
    private synchronized void deregister (Item item, boolean refresh) {
        FileObject fo = item.primaryFile;

        Item previous = (Item)map.remove (fo);

        if (previous != null && previous != item) {
            // ops, mistake,
            // return back the original
            map.put (fo, previous);
            return;
        }

        // refresh of parent folder
        if (refresh) {
            fo = fo.getParent ();
            if (fo != null) {
                Item item2 = (Item)map.get (fo);
                if (item2 != null) {
                    DataFolder df = (DataFolder) item2.getDataObjectOrNull();
                    if (df != null) {
                        VALIDATOR.refreshFolderOf (df);
                    }
                }
            }
        }
    }

    /** Changes the primary file to new one.
    * @param item the item to change
    * @param newFile new primary file to set
    */
    private synchronized void changePrimaryFile (
        Item item, FileObject newFile
    ) {
        map.remove (item.primaryFile);
        item.primaryFile = newFile;
        map.put (newFile, item);
    }

    /** When the loader pool is changed, then all objects are rescanned.
    */
    public void stateChanged (javax.swing.event.ChangeEvent ev) {
        HashSet set;
        synchronized (this) {
            set = new HashSet (map.keySet ());
        }
        revalidate (set);
    }

    /** One item in object pool.
    */
    static final class Item extends Object {
        /** primary file */
        FileObject primaryFile;
        /** weak reference data object with this primary file */
        private Reference obj;

        // [PENDING] hack to check the stack when the DataObject has been created
        //    private Exception stack;

        /** @param fo primary file
        * @param pool object pool
        */
        public Item (FileObject fo) {
            this.primaryFile = fo;

            // [PENDING] // stores stack
            /*      java.io.StringWriter sw = new java.io.StringWriter ();
                  stack = new Exception ();
                }

                // [PENDING] toString returns original stack
                public String toString () {
                  return stack.toString ();*/
        }

        /** Setter for the data object. Called immediatelly as possible.
        * @param obj the data object for this item
        */
        public void setDataObject (DataObject obj) {
            this.obj = new WeakReference (obj);
        }

        /** Getter for the data object.
        * @return the data object or null
        */
        DataObject getDataObjectOrNull () {
            return (DataObject)this.obj.get ();
        }
        /** Getter for the data object.
        * @return the data object
        * @exception IllegalStateException if the data object has been lost
        *   due to weak references (should not happen)
        */
        public DataObject getDataObject () {
            DataObject obj = getDataObjectOrNull ();
            if (obj == null) {
                throw new IllegalStateException ();
            }
            return obj;
        }

        /** Deregister one reference.
        * @param refresh true if the parent folder should be refreshed
        */
        public void deregister (boolean refresh) {
            POOL.deregister (this, refresh);
        }

        /** Changes the primary file to new one.
        * @param newFile new primary file to set
        */
        public void changePrimaryFile (FileObject newFile) {
            POOL.changePrimaryFile (this, newFile);
        }

        /** Is the item valid?
        */
        public boolean isValid () {
            return POOL.map.containsKey (primaryFile);
        }

        public String toString () {
            DataObject obj = (DataObject)this.obj.get ();
            if (obj == null) {
                return " nothing"; // NOI18N
            }
            return obj.toString ();
        }

    }


    /** Validator to allow rescan of files.
    */
    private static final class Validator extends Object
        implements DataLoader.RecognizedFiles {
        /** set of all files that should be revalidated (FileObject) */
        private Set files;
        /** current thread that is in the validator */
        private Thread current;
        /** number of threads waiting to enter the validation */
        private int waiters;
        /** set of files that has been marked recognized (FileObject) 
         * @associates FileObject*/
        private HashSet recognizedFiles;
        /** set with all objects that refused to be discarded (DataObject) 
         * @associates DataObject*/
        private HashSet refusingObjects;
        /** set of files that has been registered during revalidation 
         * @associates FileObject*/
        private HashSet createdFiles;

        /** Enters the section.
        * @param set set of files that should be processed
        * @return the set of files concatenated with any previous sets
        */
        private synchronized Set enter (Set set) {
            waiters++;
            while (current != null) {
                try {
                    wait ();
                } catch (InterruptedException ex) {
                }
            }
            current = Thread.currentThread ();
            waiters--;

            if (files == null) {
                files = set;
            } else {
                files.addAll (set);
            }

            return files;
        }

        /** Leaves the critical section.
        */
        private synchronized void exit () {
            current = null;
            if (waiters == 0) {
                files = null;
            }
            notify ();
        }

        /** If there is another waiting thread, then I can
        * cancel my computation.
        */
        private synchronized boolean goOn () {
            return waiters == 0;
        }

        /** Called to either refresh folder, or register the folder to be
        * refreshed later is validation is in progress.
        */
        public void refreshFolderOf (DataFolder df) {
            if (createdFiles == null) {
                // no validator in progress
                df.refresh ();
            }
        }

        /** Mark this file as being recognized. It will be excluded
        * from further processing.
        *
        * @param fo file object to exclude
        */
        public void markRecognized (FileObject fo) {
            recognizedFiles.add (fo);
        }

        public void notifyRegistered (FileObject fo) {
            if (createdFiles != null) {
                createdFiles.add (fo);
            }
        }

        /** Reregister new object for already existing file object.
        * @param obj old object existing
        * @param loader loader of new object to create
        * @return true if the old object has been discarded and new one can
        *    be created
        */
        public boolean reregister (DataObject obj, DataLoader loader) {
            if (recognizedFiles == null) {
                // revalidation not in progress
                return false;
            }

            if (obj.getLoader () == loader) {
                // no change in loader =>
                return false;
            }

            if (createdFiles.contains (obj.getPrimaryFile ())) {
                // if the file already has been created
                return false;
            }

            if (refusingObjects.contains (obj)) {
                // the object has been refused before
                return false;
            }

            try {
                obj.setValid (false);
                return true;
            } catch (java.beans.PropertyVetoException ex) {
                refusingObjects.add (obj);
                return false;
            }

        }

        /** Rescans all fileobjects in given set.
        * @param s set of FileObjects
        * @return set of objects that refused to be revalidated
        */
        public Set revalidate (Set s) {
            // holds all created object, so they are not garbage
            // collected till this method ends
            LinkedList createObjects = new LinkedList ();
            try {
                s = enter (s);

                recognizedFiles = new HashSet ();
                refusingObjects = new HashSet ();
                createdFiles = new HashSet ();


                DataLoaderPool pool = org.openide.TopManager.getDefault ().getLoaderPool ();
                Iterator it = s.iterator ();
                while (it.hasNext () && goOn ()) {
                    FileObject fo = (FileObject)it.next ();
                    if (!recognizedFiles.contains (fo)) {
                        try {
                            // findDataObject
                            // is not using method DataObjectPool.find to locate data object
                            // directly for primary file, that is good
                            DataObject obj = pool.findDataObject (fo, this);
                            createObjects.add (obj);

                            // the previous data object should be canceled
                            DataObject orig = POOL.find (fo);

                            if (obj != orig && orig != null) {
                                try {
                                    orig.setValid (false);
                                } catch (java.beans.PropertyVetoException ex) {
                                    refusingObjects.add (orig);
                                }
                            }
                        } catch (java.io.IOException ex) {
                        }
                    }

                }
                return refusingObjects;
            } finally {
                recognizedFiles = null;
                refusingObjects = null;
                createdFiles = null;

                exit ();

                POOL.refreshAllFolders ();
            }
        }

    }
}

/*
* Log
*  16   Gandalf   1.15        1/16/00  Jaroslav Tulach I18N
*  15   Gandalf   1.14        1/16/00  Jaroslav Tulach TemplatesExplorer 
*       removed, startup faster
*  14   Gandalf   1.13        1/12/00  Ian Formanek    NOI18N
*  13   Gandalf   1.12        11/23/99 Jaroslav Tulach WeakReferences instead of
*       Soft ones.
*  12   Gandalf   1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  11   Gandalf   1.10        10/5/99  Jaroslav Tulach Synchronization 
*       improvement.
*  10   Gandalf   1.9         9/30/99  Jaroslav Tulach OpenSupport is attached 
*       to setValid veto change of its data object.
*  9    Gandalf   1.8         9/30/99  Jaroslav Tulach DataLoader is now 
*       serializable.
*  8    Gandalf   1.7         9/28/99  Jaroslav Tulach Changes in loader pool 
*       are reflected in repository.
*  7    Gandalf   1.6         7/23/99  Petr Hamernik   dispose() bugfix
*  6    Gandalf   1.5         7/20/99  Jaroslav Tulach Speed up of 
*       DataObject.find
*  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    Gandalf   1.3         4/22/99  Jaroslav Tulach Better synch with 
*       finalization.
*  3    Gandalf   1.2         4/19/99  Jan Jancura     To remember parsed 
*       informations
*  2    Gandalf   1.1         1/17/99  Jaroslav Tulach isValid test  
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
