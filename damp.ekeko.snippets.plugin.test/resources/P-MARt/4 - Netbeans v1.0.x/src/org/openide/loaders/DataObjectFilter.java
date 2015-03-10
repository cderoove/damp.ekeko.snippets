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

import org.openide.TopManager;
import org.openide.nodes.*;
import org.openide.cookies.FilterCookie;
import org.openide.cookies.ElementCookie;
import org.openide.cookies.SourceCookie;
import org.openide.src.SourceElement;
import org.openide.util.WeakListener;
import org.openide.util.RequestProcessor;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashSet;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
* Supports working with a set of filters defined by data objects.
* Provides a keyed list of children
* taken from a {@link DataFolder}.
* <p>An instance of this filter in its default settings
* accepts all {@link DataObject}s.
* <p>
* Additional specific filters can be added to this filter with {@link #putFilter}
* to filter particular kinds of data objects.
* <P> 
* Also it is possible to registering filter editor classes and to 
* obtain them, so as to allow modifications to the filter by the user.
* <p>
* The effect of this filter is that:
* <ul>
* <li>By default, all data objects in the folder will be "keys"
* (i.e. grouping categories).
* <li>Particular types of data objects can be suppressed as keys
* (and corresponding children), according to the loader's data object
* representation class.
* <li>Data objects not providing {@link ElementCookie} will be shown as the
* sole child for their key.
* <li>Data objects providing <code>ElementCookie</code> will not be shown
* directly; rather their "elements parent" will be consulted. By default
* all the children it provides will be spliced into the children list
* keyed by the data object.
* <li><code>ElementCookie</code>-enabled children will be filtered
* before being used as the children for a key; the filter used will be applied
* to a copy of the elements parent node, so that it may handle the filter
* logic. Filters may be added according to the type of the filter, which
* is specified by a filter representation class.
* </ul>
*/ 
public class DataObjectFilter extends Children.Keys {

    // static ..........................................................................

    /** represen. class (DO) => filter class 
     * @associates Class*/
    private static Hashtable doToFilter = new Hashtable ();
    /** Personal request processor */
    private static RequestProcessor processor = new RequestProcessor ();

    /** Register a new filter type for a given type of data object.
    * @param representationClass the designated super class of all data objects that can use 
    *    this filter
    * @param filterClass the class of a filter to use for such data objects 
    * @see FilterCookie#getFilterClass
    */ 
    public static void registerFilterClass (
        Class representationClass,
        Class filterClass
    ) {
        doToFilter.put (representationClass, filterClass);
    }

    /** Get the filter class currently registered for a data object representation class.
    * @return the proper filter class, or <code>null</code>
    */ 
    public static Class getFilterClass (Class representationClass) {
        return (Class) doToFilter.get (representationClass);
    }


    // variables ......................................................................

    /** represen. class (DO) => represen. class (DO) 
     * @associates Class*/
    private Hashtable     acceptedDOs = new Hashtable ();
    /** filter class => filter 
     * @associates Object*/
    private Hashtable     filters = new Hashtable ();
    /** current representing DF */
    private DataFolder    dataFolder;
    /** Filters DOs */
    private DataFilter    filter;
    /** Listens on DataFolder */
    private PropertyChangeListener  folderFerret;
    /** Keeps listener */
    private PropertyChangeListener  folderFerretKeeper;
    /** Listens Nodes */
    private NodeListener  ferret;
    /** Node => DO 
     * @associates Object*/
    private Hashtable     noToDo = new Hashtable ();
    /** DO => Node */
    //  private Hashtable     doToNo = new Hashtable ();
    /** true if subnodes of this node are visible */
    private boolean       nodesInited = false;


    // init ...........................................................................

    /** Create a new filter which will accept given set of data objects.
    * Initially unattached to any data folder.
    * @param representationClasses representation classes of data objects to be shown
    */
    public DataObjectFilter (Class[] representationClasses) {
        filter = new DataFilter () {
                     public boolean acceptDataObject (DataObject obj) {
                         Enumeration e = acceptedDOs.keys ();
                         Class c = obj.getClass ();
                         while (e.hasMoreElements ()) {
                             if (((Class) e.nextElement ()).isAssignableFrom (c)) {
                                 return true;
                             }
                         }
                         return false;
                     }
                 };
        folderFerret = WeakListener.propertyChange (
                           folderFerretKeeper = new PropertyChangeListener () {
                                                    public void propertyChange (PropertyChangeEvent ev) {
                                                        if (nodesInited) {
                                                            if (ev.getPropertyName().equals(DataFolder.PROP_CHILDREN)) {
                                                                refreshAll ();
                                                            }
                                                        }
                                                    }
                                                }, null);
        ferret = new NodeAdapter () {
                     public void childrenAdded (NodeMemberEvent ev) {
                         refresh ((DataObject) noToDo.get (ev.getSource ()));
                     }
                     public void childrenRemoved (NodeMemberEvent ev) {
                         refresh ((DataObject) noToDo.get (ev.getSource ()));
                     }
                 };
        int i, k = representationClasses.length;
        for (i = 0; i < k; i++)
            acceptedDOs.put (representationClasses [i], representationClasses [i]);
        //System.out.println ("#DOF: " + (++dof)); // NOI18N
    }

    //static int dof = 0;
    //static int refreshAllCounter = 0;
    //static int createNodesCount = 0;

    //  protected void finalize () {   TESTING
    //System.out.println ("#DOF end: " + (--dof)); // NOI18N
    //  }


    /** Create a new filter which will accept all data objects.
    * Initially unattached to any data folder.
    */
    public DataObjectFilter () {
        this (new Class [] {});
        DataLoader[] loaders = TopManager.getDefault ().getLoaderPool ().toArray ();
        int i, k = loaders.length;
        for (i = 0; i < k; i++) {
            Class c = loaders [i].getRepresentationClass ();
            if (DataFolder.class.isAssignableFrom (c)) continue;
            acceptedDOs.put (c, c);
        }
    }

    /** Create a new filter which will accept all data objects in a given folder.
    * @param dataFolder the folder to filter
    */
    public DataObjectFilter (DataFolder dataFolder) {
        this ();
        this.dataFolder = dataFolder;
    }


    // main methods ..................................................................

    /**
    * Add a filter for a certain type of data object.
    * The previous filter, if any, will be removed.
    * @param filterClass the representation class this filter belongs to 
    * @param filter the filter to use for all data objects requesting this type of filter,
    * via {@link FilterCookie#getFilterClass}. May be <code>null</code> to remove.
    */ 
    public void putFilter (Class filterClass, Object filter) {
        //System.out.println ("putFilter: "); // NOI18N
        Object old = filters.get (filterClass);
        if (old == null) {
            if (filter == null) return;
        } else
            if ((filter != null) && filter.equals (old))
                return;
        if (filter == null)
            filters.remove (filterClass);
        else
            filters.put (filterClass, filter);
        if (nodesInited)
            refreshNodes ();
        //System.out.println ("putFilter end: "); // NOI18N
    }

    /**
    * Permit a representation class of data object to be shown
    * (after appropriate filtering).
    * By default all are shown, so this need be used only to counteract
    * {@link #removeLoader}.
    * @param representationClass the data object representation class
    */ 
    public void addLoader (Class representationClass) {
        acceptedDOs.put (representationClass, representationClass);
        if (nodesInited)
            refreshAll ();
    }

    /**
    * Prevent a representation class of data object from being show at all.
    * @param representationClass the data object representation class
    * @see DataLoader#getRepresentationClass
    */ 
    public void removeLoader (Class representationClass) {
        Object filter = doToFilter.get (representationClass);
        if (filter != null) filters.remove (filter);
        acceptedDOs.remove (representationClass);
        if (nodesInited)
            refreshAll ();
    }


    /** Attach the support to a different folder.
    * @param f the new folder
    */
    public synchronized void setDataFolder (DataFolder f) {
        if ((dataFolder != null) && (f != null) &&
                dataFolder.equals (f)
           ) return;
        if (dataFolder != null) {
            dataFolder.removePropertyChangeListener (folderFerret);
            cancel ();
        }
        dataFolder = f;
        //System.out.println ("DOF.setDataFolder: " + ((dataFolder == null) ? "null" : "" + dataFolder.getPrimaryFile ()) + "  " + nodesInited); // NOI18N
        //    if (nodesInited)
        refreshAll ();
        if (dataFolder != null) dataFolder.addPropertyChangeListener (folderFerret);
    }

    /** Get the folder this support is attached to.
    * @return the folder
    */ 
    public DataFolder getDataFolder () {
        return dataFolder;
    }


    // children implementation .........................................................

    /* Overrides initNodes to run the preparation task of the
    * data object filter, call refreshKeys and start to
    * listen to the changes in the element too.
    */
    protected void addNotify () {
        //    setKeysHelper (Collections.EMPTY_SET);
        //    refreshAll ();
        nodesInited = true;
        //System.out.println ("DOF.addNotify"); // NOI18N
    }

    protected void removeNotify () {
        setKeysHelper (Collections.EMPTY_SET);
        nodesInited = false;
        //System.out.println ("DOF.removeNotify"); // NOI18N
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
        DataObject DO = (DataObject) key;
        if (!currentKeys.contains (DO)) {
            //System.out.println ("    createNodes !!!!!!!!!!!! " + DO.getPrimaryFile ()); // NOI18N
            return new Node[] {};
        }
        //System.out.println ("    createNodes: " + (++createNodesCount) + " " + DO.getPrimaryFile ()); // NOI18N
        ElementCookie ec = (ElementCookie) DO.getCookie (ElementCookie.class);
        Node root;
        if (ec == null) {
            root = DO.getNodeDelegate ().cloneNode ();
            //System.out.println ("    createNodes end: " + (--createNodesCount) + " " + DO.getPrimaryFile ()); // NOI18N
            return new Node[] {root};
        }

        root = ec.getElementsParent ();

        FilterCookie fc = (FilterCookie) root.getCookie (FilterCookie.class);
        if (fc != null) {
            Object filter = filters.get (fc.getFilterClass ());
            if (filter != null) fc.setFilter (filter);
        }

        //System.out.println ("    createNodes1 "); // NOI18N
        Node[] n = root.getChildren ().getNodes ();

        // Connect to node...
        root.addNodeListener (ferret);
        noToDo.put (root, key);
        //    doToNo.put (key, root);

        int i, k = n.length;
        Node[] nn = new Node [k];
        for (i = 0; i < k; i++)
            nn [i] = n [i].cloneNode ();
        //System.out.println ("    createNodes end: " + (--createNodesCount) + " " + DO.getPrimaryFile ()); // NOI18N
        return nn;
    }


    // other methods ...................................................................

    /** Helper accessing method.
    *
    Collection getKeys () {
      return keys;
}
    */

    Collection keys;


    /** Refreshs all current keys.
    */
    void refreshAllKeys () {
        setKeys (Collections.EMPTY_SET);
        setKeys (keys);
    }

    /** Helper accessing method.
    */
    void setKeysHelper (Collection l) {
        setKeys (keys = l);
    }

    /** Helper accessing method.
    */
    void refreskKeyHelper (Object l) {
        refreshKey (l);
    }

    private synchronized void cancel () {
        currentTask = null;
        //System.out.println ("cancel: "); // NOI18N
        Enumeration e = noToDo.keys ();
        while (e.hasMoreElements ()) {
            Node n = (Node) e.nextElement ();
            n.removeNodeListener (ferret);
        }
        noToDo = new Hashtable ();
        //    doToNo = new Hashtable ();
        //System.out.println ("cancel end: "); // NOI18N
    }


    private Runnable currentTask;
    private HashSet currentKeys;

    /**
    * Refreshs all dataobjects from current folder.
    */
    private void refreshAll () {

        if (dataFolder == null) {
            setKeysHelper (Collections.EMPTY_SET);
            return;
        }
        //final DataFolder df = dataFolder;
        currentKeys = new HashSet ();
        //System.out.println ("refreshAll prepare: " + df.getPrimaryFile () + " by " + Thread.currentThread ().getName ()); // NOI18N
        processor.postRequest (currentTask = new Runnable () {
                                                 public void run () {
                                                     if (currentTask != this) {
                                                         //System.out.println ("refreshAll !!!!!!" + " " + df.getPrimaryFile ()); // NOI18N
                                                         return;
                                                     }
                                                     //System.out.println ("refreshAll: " + (++refreshAllCounter) + " " + df.getPrimaryFile ()); // NOI18N
                                                     ArrayList newDo = dataFolder.getChildrenList ();
                                                     int i;
                                                     for (i = newDo.size () - 1; i >= 0; i--)
                                                         if (!filter.acceptDataObject ((DataObject) newDo.get (i)))
                                                             newDo.remove (i);
                                                     //        LinkedList ll = new LinkedList (getKeys ());
                                                     if (currentTask != this) {
                                                         //System.out.println ("refreshAll2 !!!!!!" + " " + df.getPrimaryFile ()); // NOI18N
                                                         return;
                                                     }
                                                     //System.out.println ("PARSING " + df.getPrimaryFile () + " ........................................................................."); // NOI18N
                                                     currentKeys = new HashSet (newDo);

                                                     LinkedList ll = new LinkedList ();
                                                     int k = newDo.size ();
                                                     for (i = 0; i < k; i++) {
                                                         DataObject o = (DataObject) newDo.get (i);
                                                         SourceCookie sc = (SourceCookie) o.getCookie (SourceCookie.class);
                                                         if (sc != null) {
                                                             SourceElement se = sc.getSource ();
                                                             se.prepare ().waitFinished ();
                                                             if (currentTask != this) {
                                                                 //System.out.println ("refreshAll3 !!!!!!" + " " + df.getPrimaryFile ()); // NOI18N
                                                                 return;
                                                             }

                                                         }
                                                         ll.add (o);
                                                         setKeysHelper (ll);

                                                         /*          if (!getKeys ().contains (o)) {
                                                                     // Disconnect from node...
                                                                     Node n = (Node) doToNo.remove (o);
                                                                     if (n == null) continue;
                                                                     noToDo.remove (n);
                                                                     n.removeNodeListener (ferret);
                                                                   }
                                                         */
                                                     }
                                                     //System.out.println ("refreshAll end: " + (--refreshAllCounter) + " " + df.getPrimaryFile ()); // NOI18N
                                                 }
                                             });
    }

    /**
    * Refreshs all dataobjects from current folder.
    */
    private void refreshNodes () {
        if (dataFolder == null) {
            setKeysHelper (Collections.EMPTY_SET);
            return;
        }
        processor.postRequest (new Runnable () {
                                   public void run () {
                                       //System.out.println ("  refreshNodes: "); // NOI18N
                                       if (dataFolder == null) return;
                                       /* JST: Not necessary anymore
                                               Collection c = getKeys ();
                                               setKeysHelper (Collections.EMPTY_SET);
                                               setKeysHelper (c);
                                       */        
                                       refreshAllKeys ();
                                       //System.out.println ("  refreshNodes end: "); // NOI18N
                                   }
                               });
    }

    /**
    * Refreshs given dataobject from current folder.
    */
    private void refresh (final DataObject key) {
        if (!currentKeys.contains (key)) {
            //System.out.println ("  refresh key prepare !!!!!: " + key.getPrimaryFile ()); // NOI18N
            return;
        }
        if (!filter.acceptDataObject (key)) return;
        //System.out.println ("  refresh key prepare: " + key.getPrimaryFile () + " by " + Thread.currentThread ().getName ()); // NOI18N
        processor.postRequest (new Runnable () {
                                   public void run () {
                                       if (!currentKeys.contains (key)) {
                                           //System.out.println ("  refresh key !!!!!!: " + key.getPrimaryFile ()); // NOI18N
                                           return;
                                       }
                                       //System.out.println ("  refresh key: " + key.getPrimaryFile ()); // NOI18N
                                       refreskKeyHelper (key);
                                   }
                               });
    }
}





/*
 * Log
 *  20   src-jtulach1.19        1/13/00  Ian Formanek    NOI18N
 *  19   src-jtulach1.18        12/15/99 Jan Jancura     Bug 4013
 *  18   src-jtulach1.17        12/2/99  Jaroslav Tulach Refresh of content of 
 *       folder is now done in special request processor
 *  17   src-jtulach1.16        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  16   src-jtulach1.15        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   src-jtulach1.14        9/10/99  Jaroslav Tulach Children.Keys has keys 
 *       variable no more.
 *  14   src-jtulach1.13        8/27/99  Jaroslav Tulach New threading model & 
 *       Children.
 *  13   src-jtulach1.12        8/10/99  Ales Novak      property children fired 
 *       only iff children has changed
 *  12   src-jtulach1.11        8/9/99   Jan Jancura     
 *  11   src-jtulach1.10        8/5/99   Jan Jancura     
 *  10   src-jtulach1.9         8/5/99   Jan Jancura     
 *  9    src-jtulach1.8         7/16/99  Jan Jancura     Optimalization & 
 *       filtering improved.
 *  8    src-jtulach1.7         7/1/99   Jan Jancura     Support for filtering 
 *       improved
 *  7    src-jtulach1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    src-jtulach1.5         4/22/99  Jaroslav Tulach Does not clone root of 
 *       getElementParent.
 *  5    src-jtulach1.4         4/21/99  Jaroslav Tulach 
 *  4    src-jtulach1.3         4/21/99  Jan Jancura     
 *  3    src-jtulach1.2         4/16/99  Jan Jancura     
 *  2    src-jtulach1.1         4/2/99   Jesse Glick     [JavaDoc]
 *  1    src-jtulach1.0         4/2/99   Jan Jancura     
 * $
 */
