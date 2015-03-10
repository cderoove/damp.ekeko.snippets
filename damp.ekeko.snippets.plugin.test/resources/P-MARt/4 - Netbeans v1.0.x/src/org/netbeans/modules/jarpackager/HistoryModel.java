/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jarpackager;

import java.util.ArrayList;
import javax.swing.JComboBox;
import java.io.*;
import javax.swing.AbstractListModel;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;

/** Manages all actions concerning history of archived
* jar files. Can restore archives from history and 
* add new entries. The depth of the history is also configurable.
* Serves also as a model for the JList control.
*
* @author  Dafe Simonek
*/
public final class HistoryModel extends AbstractListModel {
    /** holds history data - array of HistoryEntry objects 
     * @associates HistoryEntry*/
    ArrayList data;
    /** maximum count of entries in history
    * (will turn to option in future) */
    final int maxCount = 30;

    static final long serialVersionUID =-6781071744108450253L;
    /** Creates new HistoryModel. */
    public HistoryModel () {
        data = new ArrayList(maxCount + 1);
    }

    /********* Implementation of ListModel interface *****/

    /** Returns the value at the specified index. */
    public Object getElementAt (int index) {
        return ((HistoryEntry)data.get(index)).archivePath;
    }

    /** Returns the length of the list. */
    public int getSize () {
        return data.size();
    }

    /***** another methods for manipulating history ******/

    /** Adds new entry into the history. If given entry already
    * exists in history, it is not added. */
    public void addEntry (String archivePath, String contentPath) {
        HistoryEntry he = new HistoryEntry();
        he.archivePath = archivePath;
        he.contentPath = contentPath;
        if (!data.contains(he)) {
            // ensure that we don't exceed maximum capacity
            int oldSize = data.size();
            if (oldSize >= maxCount) {
                HistoryEntry curEntry = null;
                for (int i = data.size() - maxCount; i >= 0; i--) {
                    curEntry = (HistoryEntry)data.remove(i);
                }
                data.add(0, he);
                fireContentsChanged(this, 0, Math.max(oldSize, data.size()));
            } else {
                data.add(0, he);
                fireIntervalAdded(this, 0, 0);
            }
        }
    }

    /** move item at specified index to the front */
    void moveToFront (int index) {
        // update history data
        HistoryEntry foundEntry = (HistoryEntry)data.get(index);
        data.remove(foundEntry);
        data.add(0, foundEntry);
        fireContentsChanged(this, 0, data.size());
    }

    /** removes specified entries which represents given
    * array of archive paths. Items of given array muts be Strings. */
    void remove (Object[] values) {
        int oldSize = data.size();
        HistoryEntry curEntry = null;
        for (int i = 0; i < values.length; i++) {
            curEntry = getEntry((String)values[i]);
            if (curEntry != null) {
                data.remove(curEntry);
            }
        }
        fireContentsChanged(this, 0, oldSize);
    }

    /** clear all data */
    void clear () {
        int oldSize = data.size();
        data.clear();
        fireIntervalRemoved(this, 0, oldSize);
    }

    /** @return entry which contains specified archive path or
    * null if no such entry can be found */
    HistoryEntry getEntry (String archivePath) {
        HistoryEntry he = new HistoryEntry();
        he.archivePath = archivePath;
        he.contentPath = null;
        int foundIndex = data.indexOf(he);
        return foundIndex < 0 ? null : (HistoryEntry)data.get(foundIndex);
    }

    public void readData (ObjectInput in)
    throws IOException, ClassNotFoundException {
        data = (ArrayList)in.readObject();
    }

    public void writeData (ObjectOutput out)
    throws IOException {
        out.writeObject(data);
    }

    /** An entry in history */
    static final class HistoryEntry implements Serializable {
        String archivePath;
        String contentPath;

        static final long serialVersionUID =-5263088252589301437L;
        /** Entries are equal if archivePaths are equal */
        public boolean equals (Object obj) {
            if (archivePath == null) {
                return ((HistoryEntry)obj).archivePath == null;
            }
            return archivePath.equals(((HistoryEntry)obj).archivePath);
        }
    }
}

/*
* <<Log>>
*  4    Gandalf   1.3         11/27/99 Patrik Knakal   
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         10/13/99 David Simonek   various bugfixes 
*       concerning primarily manifest
*  1    Gandalf   1.0         9/8/99   David Simonek   
* $ 
*/ 
