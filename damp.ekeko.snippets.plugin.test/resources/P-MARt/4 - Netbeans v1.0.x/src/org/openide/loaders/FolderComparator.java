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

import org.openide.nodes.Node;
import org.openide.TopManager;

/**
*
*
* @author Jaroslav Tulach
*/
class FolderComparator extends DataFolder.SortMode implements Comparator {
    /** modes */
    public static final int NONE = 0;
    public static final int NAMES = 1;
    public static final int CLASS = 2;
    /** first of all DataFolders, then other object, everything sorted
    * by names
    */
    public static final int FOLDER_NAMES = 3;


    /** mode to use */
    private int mode;

    /** New comparator. Sorts folders first and everything by names.
    */
    public FolderComparator () {
        this (FOLDER_NAMES);
    }

    /** New comparator.
    * @param mode one of sorting type constants
    */
    public FolderComparator (int mode) {
        this.mode = mode;
    }

    /** Comparing method. Can compare two DataObjects
    * or two Nodes (if they have data object cookie)
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

        switch (mode) {
        case NONE:
            return 0;
        case NAMES:
            return compareNames (obj1, obj2);
        case CLASS:
            return compareClass (obj1, obj2);
        case FOLDER_NAMES:
        default:
            return compareFoldersFirst (obj1, obj2);
        }
    }


    /** for sorting data objects by names */
    private int compareNames (DataObject obj1, DataObject obj2) {
        return obj1.getName ().compareTo (
                   obj2.getName ()
               );
    }

    /** for sorting folders first and then by names */
    private int compareFoldersFirst (DataObject obj1, DataObject obj2) {
        if (obj1.getClass () != obj2.getClass ()) {
            // if classes are different than the folder goes first
            if (obj1.getClass () == DataFolder.class) {
                return -1;
            }
            if (obj2.getClass () == DataFolder.class) {
                return 1;
            }
        }

        // otherwise compare by names
        return obj1.getName ().compareTo (
                   obj2.getName ()
               );
    }

    /** for sorting data objects by their classes */
    private int compareClass (DataObject obj1, DataObject obj2) {
        Class c1 = obj1.getClass ();
        Class c2 = obj2.getClass ();

        if (c1 == c2) {
            return obj1.getName ().compareTo (
                       obj2.getName ()
                   );
        }

        // sort by classes
        DataLoaderPool dlp = TopManager.getDefault ().getLoaderPool ();
        final Enumeration loaders = dlp.allLoaders ();

        // PENDING, very very slow
        while (loaders.hasMoreElements ()) {
            Class clazz = ((DataLoader) (loaders.nextElement ())).getRepresentationClass ();
            boolean r1 = clazz.isAssignableFrom (c1);
            boolean r2 = clazz.isAssignableFrom (c2);

            if (r1 && r2) return obj1.getName ().compareTo (obj2.getName ());
            if (r1) return -1;
            if (r2) return 1;
        }
        return obj1.getName ().compareTo (
                   obj2.getName ()
               );
    }
}

/*
* Log
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         7/12/99  Jesse Glick     Bugfix #1892--folder sort
*       by type did not handle special types correctly.
*  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         1/6/99   Jaroslav Tulach Change of package of 
*       DataObject
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
