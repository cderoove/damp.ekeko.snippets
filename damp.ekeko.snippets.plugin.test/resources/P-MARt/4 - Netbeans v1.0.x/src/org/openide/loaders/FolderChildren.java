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

import java.beans.*;
import java.util.*;
import javax.swing.SwingUtilities;

import org.openide.filesystems.*;
import org.openide.util.Mutex;
import org.openide.util.WeakListener;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/** Watches over a folder and represents its
* child data objects by nodes.
*
* @author Jaroslav Tulach
*/
final class FolderChildren extends Children.Keys implements PropertyChangeListener {
    /** the folder */
    private DataFolder folder;
    /** filter of objects */
    private DataFilter filter;
    /** initialization of children task */
    private Task initTask;
    /** listener on changes in nodes */
    private PropertyChangeListener listener;

    /**
    * @param f folder to display content of
    * @param map map to use for holding of children
    */
    public FolderChildren (DataFolder f) {
        this (f, DataFilter.ALL);
    }

    /**
    * @param f folder to display content of
    * @param filter filter of objects
    */
    public FolderChildren (DataFolder f, DataFilter filter) {
        this.folder = f;
        this.filter = filter;
        this.listener = WeakListener.propertyChange (this, folder);
    }

    /** If the folder changed its children we change our nodes.
    */
    public void propertyChange (final PropertyChangeEvent ev) {
        if (DataFolder.PROP_CHILDREN.equals (ev.getPropertyName ())) {
            refreshChildren ();
            return;
        }
        if (
            DataFolder.PROP_SORT_MODE.equals (ev.getPropertyName ()) ||
            DataFolder.PROP_ORDER.equals (ev.getPropertyName ())
        ) {
            refreshChildren ();
            return;
        }
    }

    /** Refreshes the children.
    */
    void refreshChildren () {
        initialize (true, false);
    }

    /** Refreshes the children.
    * @param ch collection of children data objects
    */
    void refreshChildren (List ch) {
        ListIterator it = ch.listIterator ();
        LinkedList l = new LinkedList ();

        while (it.hasNext ()) {
            DataObject obj = (DataObject)it.next ();
            l.add (createKey (obj));
        }

        setKeys (l);
    }

    /** Creates a key for given data object.
    * This method is here to create something different then data object,
    * because the data object should be finalized when not needed and
    * that is why it should not be used as a key.
    *
    * @param obj data object
    * @return key representing the data object.
    */
    private static Object createKey (DataObject obj) {
        return new Pair (obj.getLoader (), obj.getPrimaryFile ());
    }

    /** This method takes the key created by createKey and converts it
    * into primary file.
    *
    * @param key the key
    * @return primary file of the key
    */
    private static FileObject getFile (Object key) {
        return ((Pair)key).primaryFile;
    }

    /** Create a node for one data object.
    * @param key DataObject
    */
    protected Node[] createNodes (Object key) {
        FileObject fo = getFile (key);
        DataObject obj;
        try {
            obj = DataObject.find (fo);

            if (filter == null || filter.acceptDataObject (obj)) {
                return new Node[] { obj.getClonedNodeDelegate (filter) };
            } else {
                return new Node[0];
            }
        } catch (DataObjectNotFoundException e) {
            return new Node[0];
        }
    }

    /** Improves the searching capability to wait till all children
    * are found and then searching thru them.
    */
    public Node findChild (final String name) {
        // start the initialization
        Node[] forget = getNodes ();
        // waits till the list of children is created
        initialize (false, false).waitFinished ();
        Node node = super.findChild (name);
        return node;
    }

    /** Initializes the children.
    */
    protected void addNotify () {
        initialize (true, true);
        // add as a listener for changes on nodes
        folder.addPropertyChangeListener (listener);
    }

    /** Deinitializes the children.
    */
    protected void removeNotify () {
        // removes the listener
        folder.removePropertyChangeListener (listener);
        setKeys (java.util.Collections.EMPTY_SET);
    }

    /** Ensures that the content of children will be filled sometime.
    * @param force true if the content should be filled immediatelly
    */
    private Task initialize (boolean force, boolean waitFirst) {
        Task t = initTask;
        if (t != null && !force) {
            return t;
        }

        final Addition add = new Addition (waitFirst);
        initTask = t = folder.computeChildrenList (add);
        t.addTaskListener (add);

        if (waitFirst) {
            add.waitFirst ();
        }

        return t;
    }

    /** Display name */
    public String toString () {
        return folder.getPrimaryFile ().toString ();
    }

    /** time delay between two inserts of nodes */
    private static final int TIME_DELAY = 256;

    /** Support for incremental adding of new nodes.
    *
    * <P>
    * There is a deadlock warning:
    * <OL>
    *   <LI>A thread waiting in the waitFirst method can have MUTEX.readAccess
    *   <LI>Thread running run () needs access to MUTEX.writeAccess (because of setKeys)
    *   <LI>Be sure that the thread leaves waitFirst before writeAccess is needed
    * </OL>
    */
    private class Addition extends Object
        implements TaskListener, FolderListListener {
        static final long serialVersionUID =-4194617547214845940L;

        /** last time of addition */
        private long time = System.currentTimeMillis () + TIME_DELAY;
        /** delay */
        private int delay = TIME_DELAY;

        /** update the nodes during processing or only at the end */
        private boolean processingUpdate;

        /** @param processingUpdate update the nodes during
        *  processing or only at the end 
        */
        public Addition (boolean processingUpdate) {
            this.processingUpdate = processingUpdate;
        }

        /** Another object has been recognized.
         * @param obj the object recognized
         * @param arr array where the implementation should add the
         *    object
         */
        public void process(DataObject obj, java.util.List arr) {
            if (!filter.acceptDataObject (obj)) {
                return;
            }

            // first accepted object is notified to the waiting thread in
            boolean first = arr.isEmpty ();
            arr.add (obj);

            if (!processingUpdate) {
                // if we should not notify during processing update
                // skip the rest
                return;
            }

            if (first) {
                synchronized (this) {
                    notify ();
                }
                refreshChildren (arr);
                return;
            }

            if (System.currentTimeMillis () > time) {
                if (!arr.isEmpty ()) {
                    // add the nodes
                    refreshChildren (arr);
                    delay *= 2;
                }

                time = System.currentTimeMillis () + delay;
            }
        }

        /** All objects has been recognized.
         * @param arr list of DataObjects
         */
        public void finished(java.util.List arr) {
            synchronized (this) {
                notify ();
            }
            // change the order because initialize method has already finished
            refreshChildren (arr);
        }

        /** Getter for first map.
        */
        public synchronized void waitFirst () {
            try {
                wait (50);
            } catch (InterruptedException e) {
                // cannot happen
                throw new InternalError ();
            }
        }

        /** Called when a task finishes running.
         * @param task the finished task
         */
        public void taskFinished(Task task) {
            initTask = Task.EMPTY;
        }
    }

    /** Pair of loader and primary file.
    */
    private static final class Pair extends Object {
        public DataLoader loader;
        public FileObject primaryFile;

        public Pair (DataLoader loader, FileObject primaryFile) {
            this.loader = loader;
            this.primaryFile = primaryFile;
        }

        public int hashCode () {
            return loader.hashCode () + 2 * primaryFile.hashCode ();
        }

        public boolean equals (Object o) {
            if (o instanceof Pair) {
                Pair p = (Pair)o;
                return loader == p.loader && primaryFile.equals (p.primaryFile);
            }
            return false;
        }
    }
}

/*
* Log
*  33   Gandalf   1.32        1/9/00   Jaroslav Tulach When refreshing content 
*       of folders the sibling nodes are not collapsed.  
*  32   Gandalf   1.31        1/8/00   Jaroslav Tulach Works better.
*  31   Gandalf   1.30        12/2/99  Jaroslav Tulach Refresh of content of 
*       folder is now done in special request processor
*  30   Gandalf   1.29        11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  29   Gandalf   1.28        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  28   Gandalf   1.27        10/5/99  Jaroslav Tulach Heap debugging 
*       improvement.
*  27   Gandalf   1.26        9/30/99  Jaroslav Tulach ClassCastEx fix.
*  26   Gandalf   1.25        9/28/99  Jaroslav Tulach Changes in loader pool 
*       are reflected in repository.
*  25   Gandalf   1.24        9/22/99  Jaroslav Tulach addNotify really works.
*  24   Gandalf   1.23        9/13/99  Jaroslav Tulach #3730
*  23   Gandalf   1.22        9/1/99   Jaroslav Tulach Mutex.postWriteRequest
*  22   Gandalf   1.21        8/30/99  Jaroslav Tulach Less deadlocks?
*  21   Gandalf   1.20        8/27/99  Jaroslav Tulach Short waiting.
*  20   Gandalf   1.19        8/27/99  Jaroslav Tulach New threading model & 
*       Children.
*  19   Gandalf   1.18        8/18/99  Ian Formanek    Generated serial version 
*       UID
*  18   Gandalf   1.17        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  17   Gandalf   1.16        5/25/99  Jaroslav Tulach Fix #1889
*  16   Gandalf   1.15        4/24/99  Jaroslav Tulach 
*  15   Gandalf   1.14        4/23/99  Jaroslav Tulach Does not ignore filters.
*  14   Gandalf   1.13        4/22/99  Jaroslav Tulach Timeout when waiting for 
*       first child.
*  13   Gandalf   1.12        4/21/99  Jaroslav Tulach DataObjects can be 
*       finalized
*  12   Gandalf   1.11        4/17/99  Jaroslav Tulach Works better with empty 
*       folders
*  11   Gandalf   1.10        4/16/99  Jaroslav Tulach Changes in children.
*  10   Gandalf   1.9         4/8/99   Jaroslav Tulach fix 1441
*  9    Gandalf   1.8         2/18/99  Jaroslav Tulach Lazy initialization of 
*       order of data objects in the folder.
*  8    Gandalf   1.7         2/17/99  Jaroslav Tulach Faster setOrder/getOrder
*  7    Gandalf   1.6         2/16/99  Jaroslav Tulach Better usage of 
*       WeakListeners
*  6    Gandalf   1.5         2/11/99  Jaroslav Tulach SystemAction is 
*       javax.swing.Action
*  5    Gandalf   1.4         2/5/99   Jaroslav Tulach 
*  4    Gandalf   1.3         2/4/99   Jaroslav Tulach Properties and explorer
*  3    Gandalf   1.2         2/4/99   Jaroslav Tulach Compiles with javac
*  2    Gandalf   1.1         2/3/99   Jaroslav Tulach Recognizing of folder 
*       data object on background
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
