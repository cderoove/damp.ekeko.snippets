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

package org.openide.filesystems;

import java.beans.*;
import java.io.*;
import java.lang.ref.*;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;

import org.openide.util.RequestProcessor;
import org.openide.util.enum.SequenceEnumeration;
import org.openide.util.enum.SingletonEnumeration;
import org.openide.util.enum.QueueEnumeration;

/** Request for parsing of an filesystem. Can be stoped.
*
* @author Jaroslav Tulach
*/
final class RefreshRequest extends Object implements Runnable {
    /** how much folders refresh at one request */
    private static final int REFRESH_COUNT = 30;

    /** fs to work on */
    private Reference system;

    /** enumeration of folders Reference (FileObjects) to process */
    private Enumeration en;

    /** how often invoke itself */
    private int refreshTime;

    /** task to call us */
    private RequestProcessor.Task task;

    /** Constructor
    * @param fs file system to refresh
    * @param ms refresh time
    */
    public RefreshRequest (AbstractFileSystem fs, int ms) {
        system = new WeakReference (fs);
        refreshTime = ms;
        task = RequestProcessor.postRequest (this, ms, Thread.MIN_PRIORITY);
    }

    /** Getter for the time.
    */
    public int getRefreshTime () {
        return refreshTime;
    }

    /** Stops the task.
    */
    public void stop () {
        refreshTime = 0;
    }


    /** Refreshes the system.
    */
    public void run () {
        int ms = refreshTime;

        if (ms <= 0) {
            // we were stopped
            return;
        }
        AbstractFileSystem system = (AbstractFileSystem)this.system.get ();
        if (system == null) {
            // end for ever the fs does not exist no more
            return;
        }

        if (en == null || !en.hasMoreElements ()) {
            // start again from root
            en = existingFolders (system);
        }

        for (int i = 0; i < REFRESH_COUNT && en.hasMoreElements (); i++) {
            Reference ref = (Reference)en.nextElement ();
            FileObject fo = (FileObject)ref.get ();
            if (fo != null) {
                fo.refresh ();
            }
        }

        // clear the queue
        if (!en.hasMoreElements ()) {
            en = null;
        }

        task.schedule (ms);
    }

    /** Existing folders for abstract file objects.
    */
    private static Enumeration existingFolders (AbstractFileSystem fs) {
        QueueEnumeration en = new QueueEnumeration () {
                                  public void process (Object o) {
                                      Reference ref = (Reference)o;
                                      AbstractFileObject file = (AbstractFileObject)ref.get ();
                                      if (file != null) {
                                          FileObject[] arr = file.subfiles ();
                                          Reference[] to = new Reference[arr.length];

                                          // make the array weak
                                          for (int i = 0; i < arr.length; i++) {
                                              to[i] = new WeakReference (arr[i]);
                                          }

                                          // put it into the enumeration
                                          put (to);
                                      }
                                  }
                              };
        // weak reference to root
        en.put (new WeakReference (fs.getAbstractRoot ()));
        return en;
    }
}

/*
* Log
*  7    src-jtulach1.6         11/29/99 Jaroslav Tulach Removes debug comment.
*  6    src-jtulach1.5         11/24/99 Jaroslav Tulach Weak references to all 
*       files that should be checked.
*  5    src-jtulach1.4         10/29/99 Jaroslav Tulach MultiFileSystem + 
*       FileStatusEvent
*  4    src-jtulach1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    src-jtulach1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    src-jtulach1.1         6/3/99   Jaroslav Tulach Refresh of only opened 
*       files.
*  1    src-jtulach1.0         3/26/99  Jaroslav Tulach 
* $
*/
