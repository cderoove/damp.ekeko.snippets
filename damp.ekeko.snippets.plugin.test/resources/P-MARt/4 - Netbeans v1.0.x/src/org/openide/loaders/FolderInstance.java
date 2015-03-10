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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.*;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.cookies.InstanceCookie;
import org.openide.util.Task;
import org.openide.util.WeakListener;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;

/** Support class for creation of an object from the content
* of a folder.
* It implements <code>InstanceCookie</code>, so it
* can be used as a cookie for a node or data object.
* <P>
* When created on a folder and started by invoking run method,
* it scans its content (in a separate
* thread) and creates a list of instances from which the new
* instance of this object should be composed. The object
* automatically listens to changes of components
* in the folder, and if some change occurs, it allows the subclass to create
* a new object.
*
* @author Jaroslav Tulach
*/
public abstract class FolderInstance extends Task implements InstanceCookie {
    /** a queue to run requests in */
    //  private static final RequestProcessor PROCESSOR = new RequestProcessor ();

    /** Folder to work with. */
    protected DataFolder folder;

    /** map of primary file to their cookies (FileObject, HoldInstance) 
     * @associates InstanceCookie*/
    private HashMap map = new HashMap (17);

    /** object for this cookie. Either the right instance of object or
    * an instance of IOException or ClassNotFoundException
    */
    private Object object;

    /** Listener and runner  for this object */
    private Listener listener;

    /** Create new folder instance.
     * @param df data folder to create instances from
    */
    public FolderInstance (DataFolder df) {
        this (df, new Listener ());
    }

    /** Constructor */
    private FolderInstance (DataFolder df, Listener l) {
        super (l);

        listener = l;

        // link with listener
        l.folderInstance = this;

        folder = df;
        folder.addPropertyChangeListener (
            WeakListener.propertyChange (listener, folder)
        );
    }

    /** Full name of the data folder's primary file, separated by periods.
    * @return the name
    */
    public String instanceName () {
        return folder.getPrimaryFile ().getPackageName ('.');
    }

    /* Returns the root class of all objects.
    * Supposed to be overriden in subclasses.
    *
    * @return Object.class
    * @exception IOException an I/O error occured
    * @exception ClassNotFoundException the class has not been found
    */
    public Class instanceClass ()
    throws java.io.IOException, ClassNotFoundException {
        Object object = this.object;
        if (object != null) {
            if (object instanceof java.io.IOException) {
                throw (java.io.IOException)object;
            }
            if (object instanceof ClassNotFoundException) {
                throw (ClassNotFoundException)object;
            }
            return object.getClass ();
        }

        return Object.class;
    }

    /*
    * @return an object to work with
    * @exception IOException an I/O error occured
    * @exception ClassNotFoundException the class has not been found
    */
    public Object instanceCreate ()
    throws java.io.IOException, ClassNotFoundException {
        instanceFinished ();

        Object object = FolderInstance.this.object;

        if (object instanceof java.io.IOException) {
            throw (java.io.IOException)object;
        }
        if (object instanceof ClassNotFoundException) {
            throw (ClassNotFoundException)object;
        }
        return object;
    }

    /** Wait for instance initialization to finish.
    */
    public final void instanceFinished () {
        waitFinished ();
    }

    /** Allows subclasses to decide whether they want to work with the specified
    * <code>DataObject</code> or not.
    *
    * @param dob a <code>DataObject</code> to test
    * @return the cookie for the <code>DataObject</code> or <code>null</code>
    * if it should not be used
    */
    protected InstanceCookie acceptDataObject(DataObject dob) {
        InstanceCookie cookie;

        if (dob instanceof DataFolder) {
            cookie = acceptFolder((DataFolder)dob);
        } else {
            // test if we accept the instance
            cookie = (InstanceCookie)dob.getCookie (InstanceCookie.class);
            try {
                cookie = cookie == null ? null : acceptCookie (cookie);
            } catch (IOException ex) {
                // an error during a call to acceptCookie
                cookie = null;
            } catch (ClassNotFoundException ex) {
                // an error during a call to acceptCookie
                cookie = null;
            }
        }
        return cookie;
    }

    /** Allows subclasses to decide whether they want to work with
    * the specified <code>InstanceCookie</code> or not.
    * <p>The default implementation simply
    * returns the same cookie, but subclasses may
    * decide to return <code>null</code> or a different cookie.
    *
    * @param cookie the instance cookie to test
    * @return the cookie to use or <code>null</code> if this cookie should not
    *    be used
    * @exception IOException if an I/O error occurred calling a cookie method
    * @exception ClassNotFoundException if a class is not found in a call to a cookie method
    */
    protected InstanceCookie acceptCookie (InstanceCookie cookie)
    throws java.io.IOException, ClassNotFoundException {
        return cookie;
    }

    /** Allows subclasses to decide how they want to work with a
    * provided folder.
    * The default implementation simply returns <code>null</code> to
    * indicate that folders should not be recursed,
    * but subclasses can do otherwise.
    *
    * @param df data folder to create cookie for
    * @return the cookie for this folder or <code>null</code> if this folder should not
    *    be used
    */
    protected InstanceCookie acceptFolder (DataFolder df) {
        return null;
    }

    /** Notifies subclasses that the set of cookies for this folder
    * has changed.
    * A new object representing the folder should
    * be created (or the old one updated).
    * Called both upon initialization of the class, and change of its cookies.
    *
    * @param cookies updated array of instance cookies for the folder
    * @return object to represent these cookies
    *
    * @exception IOException an I/O error occured
    * @exception ClassNotFoundException a class has not been found
    */
    protected abstract Object createInstance (InstanceCookie[] cookies)
    throws java.io.IOException, ClassNotFoundException;

    /** Starts recreation of the instance in special thread.
    */
    public void recreate () {
        //    PROCESSOR.post (listener, 0, Thread.NORM_PRIORITY + 2);
        Mutex.EVENT.writeAccess (listener);
        //    run ();
    }

    /** Starts recreation of the instance immediatelly
    * in current thread.
    */
    public void run () {
        recreateInstance ();
    }

    /** Recreates the instance.
    */
    final void recreateInstance () {
        HashSet toRemove = new HashSet (map.keySet ());
        ArrayList cookies = new ArrayList ();

        DataObject[] list = folder.getChildren ();
        int size = list.length;


        for (int i = 0; i < size; i++) {
            // testing
            InstanceCookie cookie = acceptDataObject(list[i]);
            if (cookie != null) {
                // cookie accepted
                FileObject fo = list[i].getPrimaryFile ();
                if (!toRemove.remove (fo)) {
                    // such cookie is not there yet
                    InstanceCookie hold = new HoldInstance (cookie);
                    map.put (fo, hold);
                    cookies.add (hold);
                } else {
                    // old cookie, already there => only add it to the list of cookies
                    cookies.add (map.get (fo));
                }
            }
        }

        // now remove the cookies that are no longer in the folder
        Iterator it = toRemove.iterator ();
        while (it.hasNext ()) {
            map.remove (it.next ());
        }

        // create the list of cookies
        final InstanceCookie[] all = new InstanceCookie[cookies.size ()];
        cookies.toArray (all);

        try {
            object = createInstance (all);
        } catch (IOException ex) {
            object = ex;
        } catch (ClassNotFoundException ex) {
            object = ex;
        } finally {
            notifyFinishedHelper ();
        }
    }

    /** Helper to provide access to notifyFinished mehtod for innerclasses of this class */
    private void notifyFinishedHelper () {
        notifyFinished ();
    }


    /** Listener on change of folder's children and a starter for the task.
    */
    private static class Listener implements PropertyChangeListener, Runnable {
        /** reference to FolderInstance */
        FolderInstance folderInstance;


        /** Watching children */
        public void propertyChange (PropertyChangeEvent ev) {
            if (DataFolder.PROP_CHILDREN.equals (ev.getPropertyName ())) {
                folderInstance.recreate ();
            }
        }

        /** Start the recreation process.
        */
        public void run () {
            folderInstance.recreateInstance ();
        }
    }

    /** A instance cookie that holds the result of first
    * invocation of the provided cookie.
    *
    * PENDING: In future it could watch over provided FileObject if it
    * changes and if so, start the recreation.
    */
    private final static class HoldInstance extends Object implements InstanceCookie {
        /** the cookie to delegate to */
        private InstanceCookie cookie;
        /** the result or an exception */
        private Object object;

        public HoldInstance (InstanceCookie cookie) {
            this.cookie = cookie;
        }

        /** Full name of the data folder's primary file separated by dots.
        * @return the name
        */
        public String instanceName () {
            return cookie.instanceName ();
        }

        /** Returns the root class of all objects.
        * Supposed to be overriden in subclasses.
        *
        * @return Object.class
        * @exception IOException an I/O error occured
        * @exception ClassNotFoundException the class has not been found
        */
        public Class instanceClass ()
        throws java.io.IOException, ClassNotFoundException {
            if (object instanceof IOException) throw (IOException)object;
            if (object instanceof ClassNotFoundException) throw (ClassNotFoundException)object;
            if (object == null) {
                // delegate
                return cookie.instanceClass ();
            } else {
                // return the object class
                return object.getClass ();
            }
        }

        /**
        * @return an object to work with
        * @exception IOException an I/O error occured
        * @exception ClassNotFoundException the class has not been found
        */
        public Object instanceCreate ()
        throws java.io.IOException, ClassNotFoundException {
            if (object instanceof java.io.IOException) {
                throw (java.io.IOException)object;
            }
            if (object instanceof ClassNotFoundException) {
                throw (ClassNotFoundException)object;
            }
            // create the object if not yet created
            if (object == null) {
                object = cookie.instanceCreate ();
            }
            return object;
        }

    }
}

/*
* Log
*  16   Gandalf   1.15        11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  15   Gandalf   1.14        11/3/99  Jaroslav Tulach Does not use event queue.
*  14   Gandalf   1.13        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  13   Gandalf   1.12        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  12   Gandalf   1.11        5/11/99  Ian Formanek    Fixed to compile
*  11   Gandalf   1.10        5/11/99  Jaroslav Tulach ToolbarPool changed to 
*       look better in Open API
*  10   Gandalf   1.9         5/4/99   Jaroslav Tulach Updates on change of 
*       files.
*  9    Gandalf   1.8         4/24/99  Jaroslav Tulach 
*  8    Gandalf   1.7         3/30/99  Ian Formanek    FolderInstance creation 
*       in single thread
*  7    Gandalf   1.6         3/10/99  Jesse Glick     [JavaDoc]
*  6    Gandalf   1.5         3/2/99   David Simonek   icons repair
*  5    Gandalf   1.4         2/26/99  David Simonek   
*  4    Gandalf   1.3         2/19/99  David Simonek   menu related changes...
*  3    Gandalf   1.2         2/1/99   Jesse Glick     [JavaDoc] 
*       acceptDataObject() does not throw anything.
*  2    Gandalf   1.1         1/20/99  David Peroutka  +acceptDataObject
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
