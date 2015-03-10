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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.openide.TopManager;
import org.openide.util.datatransfer.*;
import org.openide.filesystems.*;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListener;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/** Watches over a folder and recognizes its children.
*
* @author Jaroslav Tulach
*/
final class FolderList extends Object implements FileChangeListener {
    /** serial version UID */
    static final long serialVersionUID = -592616022226761148L;

    /** priority for tasks that can be run later */
    private static final int LATER_PRIORITY = Thread.NORM_PRIORITY;

    /** request processor for recognizing of folders */
    private static final RequestProcessor PROCESSOR = new RequestProcessor (
                "Folder recognizer" // NOI18N
            );


    /** data folder to work with */
    protected DataFolder folder;

    /** last time when this folder has been refreshed */
    transient private long time;

    /** The task that computes the content of FolderList. There is also
    * only one computation task in the PROCESSOR for each FolderList.
    * Whenever a new change notification arrives (thru file listener)
    * the previous task is canceled (if not running) and new is created.
    */
    transient private RequestProcessor.Task refreshTask;

    /** Primary files in this folder. Maps (FileObject, Reference (DataObject))
    */
    transient private HashMap primaryFiles = null;

    /** order of primary files (FileObject) */
    transient private List order;

    /**
    * @param df data folder to show
    */
    public FolderList (DataFolder df, boolean attach) {
        folder = df;
        if (attach) {
            FileObject fo = df.getPrimaryFile ();

            // creates object that handles all elements in array and
            // assignes it to the
            fo.addFileChangeListener (WeakListener.fileChange (this, fo));
        }
    }

    void reassign(DataFolder df) {
        
        folder = df;
        FileObject fo = df.getPrimaryFile ();

        // creates object that handles all elements in array and
        // assignes it to the
        fo.addFileChangeListener (WeakListener.fileChange (this, fo));
    }

    /** List all children.
    * @return array with children
    */
    public DataObject[] getChildren () {
        ArrayList res = getChildrenList ();
        DataObject[] arr = new DataObject[res.size ()];
        res.toArray (arr);
        return arr;
    }

    /** Computes array of children associated
    * with this folder.
    */
    public ArrayList getChildrenList () {
        ListTask lt = getChildrenList (null);
        lt.task.waitFinished ();
        return lt.result;
    }

    /** Starts computation of children list asynchronously.
    */
    public RequestProcessor.Task computeChildrenList (FolderListListener filter) {
        return getChildrenList (filter).task;
    }

    private ListTask getChildrenList (FolderListListener filter) {
        ListTask lt = new ListTask (filter);
        int priority = Thread.currentThread().getPriority();

        // and then post your read task and wait
        lt.task = PROCESSOR.post (lt, 0, priority);
        return lt;
    }

    /** Setter for sort mode.
    */
    public void changeComparator () {
        PROCESSOR.post (new Runnable () {
                            public void run () {
                                // if has been notified
                                // change mode and regenerated children
                                if (primaryFiles != null) {
                                    // the old children
                                    ArrayList v = getObjects (null);
                                    if (v.size () != 0) {
                                        // the new children - also are stored to be returned next time from getChildrenList ()
                                        order = null;
                                        ArrayList r = getObjects (null);
                                        fireChildrenChange (r, v);
                                    }
                                }
                            }
                        }, 0, Thread.MIN_PRIORITY);
    }

    /** Refreshes the list of children.
    */
    public void refresh () {
        refresh (Long.MAX_VALUE);
    }

    /** Does refreshes if not done.
    */
    private RequestProcessor.Task doRefreshIfNotDone () {
        if (refreshTask == null) {
            synchronized (this) {
                if (refreshTask == null) {
                    refresh ();
                }
            }
        }
        return refreshTask;
    }

    /** Refreshes the list of children but only if the
    * lastest refresh time < the passed argument
    * @param now the time
    */
    private void refresh (final long now) {
        refreshTask = PROCESSOR.post (new Runnable () {
                                          public void run () {
                                              if (now > time) {
                                                  // the change has been done after our last refresh
                                                  // => we have to refresh again
                                                  time = System.currentTimeMillis();
                                                  if (primaryFiles != null) {
                                                      // list of children is created, recreate it for new files
                                                      createBoth (null, true);
                                                  }
                                              }
                                          }
                                      }, 0, LATER_PRIORITY);
    }

    //
    // FileChangeListener methods
    //

    /** Fired when a file has been changed.
    * @param fe the event describing context where action has taken place
    */
    public void fileChanged (FileEvent fe) {
        FileObject fo = fe.getFile ();
        if (fo.isData ()) {
            // when a data on the disk has been changed, look whether we
            // should reparse children
            if (primaryFiles != null) {
                // a file has been changed and the list of files is created
                try {
                    DataObject obj = DataObject.find (fo);
                    if (!primaryFiles.containsKey (obj.getPrimaryFile ())) {
                        // BUGFIX: someone who recognized the file and who isn't registered
                        // yet =>
                        // may be still not O.K.

                        // this primary file is not registered yet
                        // so recreate list of children
                        refresh (fe.getTime ());
                    }
                } catch (DataObjectNotFoundException ex) {
                    // file without data object => no changes
                }
            }
        }
    }

    /** Fired when a file has been deleted.
    * @param fe the event describing context where action has taken place
    */
    public void fileDeleted (FileEvent fe) {
        //    boolean debug = fe.getFile().toString().equals("P"); // NOI18N
        //if (debug) System.out.println ("fileDeleted: " + fe.getFile ()); // NOI18N
        //if (debug) System.out.println ("fileList: " + fileList + " file: " + fileList.get (fe.getFile ())); // NOI18N
        if (primaryFiles == null || primaryFiles.containsKey (fe.getFile ())) {
            // one of main files has been deleted => reparse
            //if (debug) System.out.println ("RecreateChildenList"); // NOI18N
            refresh (fe.getTime ());
            //if (debug) System.out.println ("Done"); // NOI18N
        }
    }

    /** Fired when a new file has been created. This action can only be
    * listened in folders containing the created file up to the root of
    * file system.
    *
    * @param fe the event describing context where action has taken place
    */
    public void fileDataCreated (FileEvent fe) {
        refresh (fe.getTime ());
    }

    /** Fired when a new file has been created. This action can only be
    * listened in folders containing the created file up to the root of
    * file system.
    *
    * @param fe the event describing context where action has taken place
    */
    public void fileFolderCreated (FileEvent fe) {
        refresh (fe.getTime ());
    }

    /** Fired when a new file has been renamed.
    *
    * @param fe the event describing context where action has taken place
    */
    public void fileRenamed (FileRenameEvent fe) {
    }

    /** Fired when a file attribute has been changed.
    *
    * @param fe the event describing context where action has taken place
    */
    public void fileAttributeChanged (FileAttributeEvent fe) {
    }


    //
    // Processing methods
    //







    /** Getter for list of children.
    * @param f filter to be notified about additions
    * @return ArrayList with DataObject types
    */
    private ArrayList getObjects (FolderListListener f) {
        ArrayList res;
        if (primaryFiles == null) {
            res = createBoth (f, false);
        } else {
            if (order != null) {
                res = createObjects (order, primaryFiles, f);
            } else {
                res = createObjects (primaryFiles.keySet (), primaryFiles, f);
                Collections.sort (res, folder.getComparator ());
                order = createOrder (res);
            }
        }
        return res;
        /* createChildrenAndFiles ();/*
        ArrayList v = (Collection)childrenList.get ();
        //if (debug) System.out.println ("Children list xxxxxxxxxxxxxx");
        if (v == null) {
        //if (debug) System.out.println ("Create them xxxxxxxxxxxx");
          v = createChildrenList (f);
        //if (debug) System.out.println ("result: " + v);
    }
        return v;*/
    }

    /** Creates list of primary files from the list of data objects.
    * @param list list of DataObject
    * @return list of FileObject
    */
    private static ArrayList createOrder (ArrayList list) {
        int size = list.size ();
        ArrayList res = new ArrayList (size);

        for (int i = 0; i < size; i++) {
            res.add (((DataObject)list.get (i)).getPrimaryFile ());
        }

        return res;
    }

    /** Creates array of data objects from given order
    * and mapping between files and data objects.
    *
    * @param order list of FileObjects that define the order to use
    * @param map mapping (FileObject, Reference (DataObject)) to create data objects from
    * @param f filter that is notified about additions - only items
    * which are accepted by the filter will be added. Null means no filtering.
    * @return array of data objects
    */
    private static ArrayList createObjects (
        Collection order, Map map, FolderListListener f
    ) {
        int size = order.size ();

        Iterator it = order.iterator ();

        ArrayList res = new ArrayList (size);
        for (int i = 0; i < size; i++) {
            FileObject fo = (FileObject)it.next ();

            Reference ref = (Reference)map.get (fo);
            DataObject obj = (DataObject)ref.get ();

            if (obj == null) {
                // try to find new data object
                try {
                    obj = DataObject.find (fo);
                    ref = new SoftReference (obj);
                } catch (DataObjectNotFoundException ex) {
                }
            }
            // add if accepted
            if (obj != null) {
                if (f == null) {
                    // accept all objects
                    res.add (obj);
                } else {
                    // allow the listener f to filter
                    // objects in the array res
                    f.process (obj, res);
                }
            }
        }

        if (f != null) {
            f.finished (res);
        }

        return res;
    }

    /** Scans for files in the folder and creates representation for
     * children. Fires info about changes in the nodes.
     *
     * @param filter listener to addition of nodes or null
     * @param notify true if changes in the children should be fired
     * @return vector of children
     */
    private ArrayList createBoth (FolderListListener filter, boolean notify) {
        // map for (FileObject, DataObject)
        final HashMap file = new HashMap ();

        // array list to return from the method
        ArrayList all = new ArrayList ();

        // map of current objects (FileObject, DataObject)
        final HashMap remove = primaryFiles == null ?
                               new HashMap () : (HashMap)primaryFiles.clone ();

        // list of new objects to add
        final ArrayList add = new ArrayList ();

        DataLoaderPool pool = TopManager.getDefault ().getLoaderPool ();

        // hashtable with FileObjects that are marked to be recognized
        // and that is why being out of enumeration
        final HashSet marked = new HashSet ();
        DataLoader.RecognizedFiles recog = new DataLoader.RecognizedFiles () {
                                               /** Adds the file object to the marked hashtable.
                                               * @param fo file object (can be <CODE>null</CODE>)
                                               */
                                               public void markRecognized (FileObject fo) {
                                                   if (fo != null) {
                                                       marked.add (fo);
                                                   }
                                               }
                                           };
        // enumeration of all files in the folder
        Enumeration en = folder.getPrimaryFile ().getChildren (false);
        while (en.hasMoreElements ()) {
            FileObject fo = (FileObject)en.nextElement ();
            if (!marked.contains (fo)) {
                // the object fo has not been yet marked as recognized
                // => continue in computation
                DataObject obj;
                try {
                    obj = pool.findDataObject (fo, recog);
                } catch (DataObjectExistsException ex) {
                    // use existing data object
                    obj = ex.getDataObject ();
                } catch (IOException ex) {
                    // data object not recognized or not found
                    obj = null;
                }

                if (obj != null) {
                    // adds object to data if it is not already there

                    // primary file
                    FileObject primary = obj.getPrimaryFile ();

                    boolean doNotRemovePrimaryFile = false;
                    if (!file.containsKey (primary)) {
                        // realy added object, test if it is new

                        // if we have not created primaryFiles before, then it is new
                        boolean goIn = primaryFiles == null;
                        if (!goIn) {
                            Reference r = (Reference)primaryFiles.get (primary);
                            // if its primary file is not between original primary files
                            // then data object is new
                            goIn = r == null;
                            if (!goIn) {
                                // if the primary file is there, but the previous data object
                                // exists and is different, then this one is new
                                DataObject obj2 = (DataObject)r.get ();
                                goIn = obj2 == null || obj2 != obj;
                                if (goIn) {
                                    doNotRemovePrimaryFile = true;
                                }
                            }
                        }

                        if (goIn) {
                            // realy new
                            add.add (obj);
                            /* JST: In my opinion it should not be here
                            * so I moved this out of this if. Is it ok?

                            if (filter != null) {
                              // fire info about addition
                              filter.acceptDataObject (obj);
                        }
                            */
                        }
                        // adds the object
                        if (filter == null) {
                            all.add (obj);
                        } else {
                            filter.process (obj, all);
                        }
                    }

                    if (!doNotRemovePrimaryFile) {
                        // this object exists it should not be removed
                        remove.remove (primary);
                    }

                    // add it to the list of primary files
                    file.put (primary, new WeakReference (obj));
                } else {
                    // 1. nothing to add to data object list
                    // 2. remove this object if it was in list of previous ones
                    // 3. do not put the file into list of know primary files
                    // => do nothing at all
                }
            }
        }

        // !!! section that fires info about changes should be here !!!

        // now file contains newly inserted files
        // data contains data objects
        // remove contains data objects that should be removed
        // add contains data object that were added

        primaryFiles = file;

        Collections.sort (all, folder.getComparator ());
        order = createOrder (all);

        ////if (debug) System.out.println ("Notified: " + notified + " added: " + add.size () + " removed: " + remove.size ()); // NOI18N
        if (notify) {
            fireChildrenChange (add, createObjects (new ArrayList (remove.keySet ()), remove, null));
        }

        // notify the filter
        if (filter != null) {
            filter.finished (all);
        }

        return all;
    }

    /** Fires info about change of children to the folder.
    * @param add added data objects
    * @param removed removed data objects
    */
    private void fireChildrenChange (ArrayList add, ArrayList removed) {
        /*    if (!add.isEmpty () || !removed.isEmpty ()) {
              System.out.println("Firing: " + folder.getPrimaryFile ());
              System.out.println("   add: " + add);
              System.out.println("   rem: " + removed);
            } else {
              System.out.println("No ch.: " + folder.getPrimaryFile ());
            }
        */    
        folder.fireChildrenChange (add, removed);
    }

    /** Task that holds result and also task. Moreover
    * can do the computation.
    */
    private final class ListTask implements Runnable {
        private FolderListListener filter;

        public ListTask (FolderListListener filter) {
            this.filter = filter;
        }

        public ArrayList result;
        public RequestProcessor.Task task;

        public void run () {
            // invokes the refresh task before we do anything else
            if (refreshTask != null) {
                refreshTask.waitFinished ();
            }

            result = getObjects (filter);
        }
    }

}

/*
 * Log
 *  26   Gandalf   1.25        1/16/00  Ian Formanek    Removed semicolons after
 *       methods body to prevent fastjavac from complaining
 *  25   Gandalf   1.24        1/13/00  Ian Formanek    NOI18N
 *  24   Gandalf   1.23        1/12/00  Ian Formanek    NOI18N
 *  23   Gandalf   1.22        12/2/99  Jaroslav Tulach Refresh of content of 
 *       folder is now done in special request processor
 *  22   Gandalf   1.21        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  21   Gandalf   1.20        10/29/99 Jaroslav Tulach MultiFileSystem + 
 *       FileStatusEvent
 *  20   Gandalf   1.19        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  19   Gandalf   1.18        9/28/99  Jaroslav Tulach Changes in loader pool 
 *       are reflected in repository.
 *  18   Gandalf   1.17        8/30/99  Jaroslav Tulach Less deadlocks?
 *  17   Gandalf   1.16        7/30/99  Jaroslav Tulach getOriginal & getCurrent
 *       in LineSet
 *  16   Gandalf   1.15        7/20/99  Jaroslav Tulach Deadlock prevention, 
 *       synchronization over primary file.
 *  15   Gandalf   1.14        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  14   Gandalf   1.13        6/8/99   Jaroslav Tulach Change order.
 *  13   Gandalf   1.12        6/1/99   Jesse Glick     fileDataCreated() calls 
 *       refresh ()--bug #1987.
 *  12   Gandalf   1.11        4/21/99  Jaroslav Tulach DataObjects can be 
 *       finalized
 *  11   Gandalf   1.10        3/28/99  David Simonek   ugly sorting bug fixed
 *  10   Gandalf   1.9         3/21/99  Jaroslav Tulach 
 *  9    Gandalf   1.8         3/4/99   Jaroslav Tulach InternalError not fired
 *  8    Gandalf   1.7         2/26/99  David Simonek   
 *  7    Gandalf   1.6         2/25/99  David Simonek   
 *  6    Gandalf   1.5         2/18/99  Jaroslav Tulach Lazy initialization of 
 *       order of data objects in the folder.
 *  5    Gandalf   1.4         2/16/99  Jaroslav Tulach Better usage of 
 *       WeakListeners
 *  4    Gandalf   1.3         2/5/99   Jaroslav Tulach Changed new from 
 *       template action
 *  3    Gandalf   1.2         2/3/99   Jaroslav Tulach Recognizing of folder 
 *       data object on background
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach Change of package of 
 *       DataObject
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    1.00        --/--/98 Jaroslav Tulach created from DataFolder
 *  0    Tuborg    1.02        --/--/98 Jaroslav Tulach sort mode
 *  0    Tuborg    1.03        --/--/98 Jaroslav Tulach hack to disable shadows
 *  0    Tuborg    1.04        --/--/98 Jaroslav Tulach commented out ViewMode property, new sorting of nodes
 *  0    Tuborg    1.05        --/--/98 Jaroslav Tulach sort mode bug fixed
 *  0    Tuborg    1.06        --/--/98 Jan Formanek    removed full.hack
 *  0    Tuborg    1.07        --/--/98 Jaroslav Tulach map indexed by identity hashcode => remove it after change of equal method of data objects
 *  0    Tuborg    1.08        --/--/98 Jaroslav Tulach new lock for Filter.getSubNodes ()
 */
