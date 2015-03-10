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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.Iterator;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.openide.execution.NbfsStreamHandlerFactory;
import org.openide.util.actions.SystemAction;
import org.openide.util.Queue;
import org.openide.util.NbBundle;
import org.openide.util.enum.QueueEnumeration;

/** Interface that provides basic information about a virtual
* filesystem in the IDE. Classes that implement it
* should follow JavaBean conventions because when a new
* instance of a file system class is inserted into the system, it should
* permit the user to modify it with standard Bean properties.
* <P>
* Implementing classes should also have associated subclasses of {@link FileObject}.
* <p>Although the class is serializable, only the {@link #isHidden hidden state} and {@link #getSystemName system name}
* are serialized, and the deserialized object is by default {@link #isValid invalid} (and may be a distinct
* object from a valid file system in the Repository). If you wish to safely deserialize a file
* system, you should after deserialization try to replace it with a file system of the
* {@link Repository#findFileSystem same name} in the Repository.
* @author Jaroslav Tulach
*/
public abstract class FileSystem implements java.io.Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8931487924240189180L;

    /** Property name indicating validity of file system. */
    public static final String PROP_VALID = "valid"; // NOI18N
    /** Property name indicating whether file system is hidden. */
    public static final String PROP_HIDDEN = "hidden"; // NOI18N
    /** Property name giving internal system name of file system. */
    public static final String PROP_SYSTEM_NAME = "systemName"; // NOI18N
    /** Property name giving root folder of file system. */
    public static final String PROP_ROOT = "root"; // NOI18N
    /** Property name giving read-only state. */
    public static final String PROP_READ_ONLY = "readOnly"; // NOI18N


    /** is this file system valid?
    * It can be invalid if there is another file system with the
    * same name in the file system pool.
    */
    transient private boolean valid = false;

    /** True if the file system is assigned to pool.
    * Is modified from Repository methods.
    */
    transient boolean assigned = false;

    /** Describes capabilities of the file system.
    */
    private FileSystemCapability capability = new FileSystemCapability.Bean ();


    /** number of requests posted and not processed. to
    * know what to do in sync.
    */
    private static int requests;
    /** List of requests */
    private static QueueEnumeration requestsQueue;

    /** hidden flag */
    private boolean hidden = false;

    /** system name */
    private String systemName = "".intern (); // NOI18N

    /** Utility field used by event firing mechanism. */
    private transient javax.swing.event.EventListenerList listenerList;

    /** Test whether file system is valid.
    * Generally invalidity would be caused by a name conflict in the file system pool.
    * @return true if the file system is valid
    */
    public final boolean isValid () {
        return valid;
    }

    /** Setter for validity. Accessible only from file system pool.
    * @param v the new value
    */
    final void setValid (boolean v) {
        if (v != valid) {
            valid = v;
            firePropertyChange (PROP_VALID, new Boolean (!v), new Boolean (v));
        }
    }

    /** Set hidden state of the object.
     * A hidden file system is not presented to the user in the Repository list (though it may be present in the Repository Settings list).
    *
    * @param hide <code>true</code> if the file system should be hidden
    */
    public final void setHidden (boolean hide) {
        if (hide != hidden) {
            hidden = hide;
            firePropertyChange (PROP_HIDDEN, new Boolean (!hide), new Boolean (hide));
        }
    }

    /** Getter for the hidden property.
    */
    public final boolean isHidden () {
        return hidden;
    }

    /** Tests whether file system will survive reloading of system pool.
    * If true then when
    * {@link Repository} is reloading its content, it preserves this
    * file system in the pool.
    * <P>
    * This can be used when the pool contains system level and user level
    * file systems. The system ones should be preserved when the user changes
    * the content (for example when he is loading a new project).
    * <p>The default implementation returns <code>false</code>.
    *
    * @return true if the file system should be persistent
    */
    protected boolean isPersistent () {
        return false;
    }

    /** Provides a name for the system that can be presented to the user.
    * @return user presentable name of the file system
    */
    public abstract String getDisplayName ();

    /** Internal (system) name of the file system.
    * Should uniquely identify the file system, as it will
    * be used during serialization of its files. The preferred way of doing this is to concatenate the
    * name of the file system type (e.g. the class) and the textual form of its parameters.
    * <P>
    * A change of the system name should be interpreted as a change of the internal
    * state of the file system. For example, if the root directory is moved to different
    * location, one should rebuild representations for all files
    * in the system.
    *
    * @return string with system name
    */
    public final String getSystemName () {
        return systemName;
    }

    /** Changes system name of the file system.
    * This property is bound and constrained: first of all
    * all vetoable listeners are asked whether they agree with the change. If so,
    * the change is made and all change listeners are notified of
    * the change.
    *
    * <p><em>Warning:</em> this method is protected so that only subclasses can change
    *    the system name.
    *
    * @param name new system name
    * @exception PropertyVetoException if the change is not allowed by a listener
    */
    protected final void setSystemName (String name)
    throws PropertyVetoException {
        synchronized (Repository.class) {
            // I must be the only one who works with system pool (that is listening)
            // on this interface
            fireVetoableChange (PROP_SYSTEM_NAME, systemName, name);

            String old = systemName;
            systemName = name.intern ();

            firePropertyChange (PROP_SYSTEM_NAME, old, systemName);
        }
    }

    /** Returns <code>true</code> if the filesystem is default one of the IDE.
     * @see Repository#getDefaultFileSystem
    */
    public final boolean isDefault () {
        return this == org.openide.TopManager.getDefault ().getRepository ().getDefaultFileSystem ();
    }

    /** Test if the filesystem is read-only or not.
    * @return true if the system is read-only
    */
    public abstract boolean isReadOnly ();

    /** Getter for root folder in the filesystem.
    *
    * @return root folder of whole filesystem
    */
    public abstract FileObject getRoot ();

    /** Finds file in the filesystem by name.
    * <P>
    * The default implementation converts dots in the package name into slashes,
    * concatenates the strings, adds any extension prefixed by a dot and calls
    * the {@link #findResource findResource} method.
    *
    * <p><em>Note:</em> when both of <code>name</code> and <code>ext</code> are <CODE>null</CODE> then name and
    *    extension should be ignored and scan should look only for a package.
    *
    * @param aPackage package name where each package component is separated by a dot
    * @param name name of the file (without dots) or <CODE>null</CODE> if
    *    one wants to obtain a folder (package) and not a file in it
    * @param ext extension of the file (without leading dot) or <CODE>null</CODE> if one needs
    *    a package and not a file
    *
    * @return a file object that represents a file with the given name or
    *   <CODE>null</CODE> if the file does not exist
    */
    public FileObject find (String aPackage, String name, String ext) {
        StringBuffer bf = new StringBuffer ();

        // append package and name
        if (!aPackage.equals ("")) { // NOI18N
            String p = aPackage.replace ('.', '/');
            bf.append (p);
            bf.append ('/');
        }

        // append name
        if (name != null) {
            bf.append (name);
        }

        // append extension if there is one
        if (ext != null) {
            bf.append ('.');
            bf.append (ext);
        }
        return findResource (bf.toString ());
    }

    /** Finds file when its resource name is given.
    * The name has the usual format for the {@link ClassLoader#getResource(String)}
    * method. So it may consist of "package1/package2/filename.ext".
    * If there is no package, it may consist only of "filename.ext".
    *
    * @param name resource name
    *
    * @return FileObject that represents file with given name or
    *   <CODE>null</CODE> if the file does not exist
    */
    public abstract FileObject findResource (String name);

    /** Returns an array of actions that can be invoked on any file in
    * this file system.
    * These actions should preferably
    * support the {@link org.openide.util.actions.Presenter.Menu Menu},
    * {@link org.openide.util.actions.Presenter.Popup Popup},
    * and {@link org.openide.util.actions.Presenter.Toolbar Toolbar} presenters.
    *
    * @return array of available actions
    */
    public abstract SystemAction[] getActions ();


    /** Reads object from stream and creates listeners.
    * @param in the input stream to read from
    * @exception IOException error during read
    * @exception ClassNotFoundException when class not found
    */
    private void readObject (java.io.ObjectInputStream in)
    throws java.io.IOException, java.lang.ClassNotFoundException {
        in.defaultReadObject ();

        if (capability == null) {
            capability = new FileSystemCapability.Bean ();
        }
    }

    public String toString () {
        return getSystemName () + "[" + getClass ().getName () + "]"; // NOI18N
    }


    /** Allows filesystems to set up the environment for external execution
    * and compilation.
    * Each filesystem can add its own values that
    * influence the environment. The set of operations that can modify
    * environment is described by the {@link Environment} interface.
    * <P>
    * The default implementation throws an exception to signal that it does not
    * support external compilation or execution.
    *
    * @param env the environment to setup
    * @exception EnvironmentNotSupportedException if external execution
    *    and compilation cannot be supported
    */
    public void prepareEnvironment (Environment env)
    throws EnvironmentNotSupportedException {
        throw new EnvironmentNotSupportedException (this);
    }

    /** Get a status object that can annotate a set of files by changing the names or icons
    * associated with them.
    * <P>
    * The default implementation returns a status object making no modifications.
    *
    * @return the status object for this file system
    */
    public Status getStatus () {
        return STATUS_NONE;
    }

    /** The object describing capabilities of this filesystem.
    * Subclasses can override it.
    */
    public final FileSystemCapability getCapability () {
        return capability;
    }

    /** Allows subclasses to change a set of capabilities of the
    * file system.
    * @param capability the capability to use
    */
    protected final void setCapability (FileSystemCapability capability) {
        this.capability = capability;
    }

    /** Executes atomic action. The atomic action represents a set of
    * operations constituting one logical unit. It is guaranteed that during
    * execution of such an action no events about changes in the file system
    * will be fired.
    * <P>
    * <em>Warning:</em> the action should not take a significant amount of time, and should finish as soon as
    * possible--otherwise all event notifications will be blocked.
    *
    * @param run the action to run
    * @exception IOException if there is an <code>IOException</code> thrown in the actions' {@link AtomicAction#run run}
    *    method
    */
    public final void runAtomicAction (final AtomicAction run) throws IOException {
        try {
            enterAtomicAction ();
            run.run ();
        } finally {
            exitAtomicAction ();
        }
    }

    /** Enters atomic action.
    */
    private static synchronized void enterAtomicAction () {
        if (requests++ == 0) {
            requestsQueue = new QueueEnumeration ();
        }
    }

    /** Exits atomic action.
    */
    private static void exitAtomicAction () {
        java.util.Enumeration myQueue;
        synchronized (FileSystem.class) {
            if (--requests == 0) {
                myQueue = requestsQueue;
                requestsQueue = null;
            } else {
                myQueue = null;
            }
        }

        if (myQueue != null) {
            while (myQueue.hasMoreElements()) {
                Runnable r = (Runnable)myQueue.nextElement();
                r.run();
            }
        }
    }

    /** Adds an event dispatcher to the queue of FS events.
    * @param run dispatcher to run
    */
    static void putEventDispatcher (EventDispatcher run) {
        synchronized (FileSystem.class) {
            if (requestsQueue != null) {
                // run later
                requestsQueue.put (run);
                return;
            }
        }
        // run now!
        run.run ();
    }

    /** Registers FileStatusListener to receive events.
    * The implementation registers the listener only when getStatus () is 
    * overriden to return a special value.
    *
    * @param listener The listener to register.
    */
    public final synchronized void addFileStatusListener (
        org.openide.filesystems.FileStatusListener listener
    ) {
        // JST: Ok? Do not register listeners when the fs cannot change status?
        if (getStatus () == STATUS_NONE) return;

        if (listenerList == null) listenerList = new EventListenerList ();

        listenerList.add (org.openide.filesystems.FileStatusListener.class, listener);
    }

    /** Removes FileStatusListener from the list of listeners.
     *@param listener The listener to remove.
     */
    public final synchronized void removeFileStatusListener (
        org.openide.filesystems.FileStatusListener listener
    ) {
        if (listenerList == null) return;

        listenerList.remove (org.openide.filesystems.FileStatusListener.class, listener);
    }

    /** Notifies all registered listeners about change of status of some files.
    *
    * @param e The event to be fired
    */
    protected final void fireFileStatusChanged(FileStatusEvent event) {
        if (listenerList == null) return;


        Object[] listeners = listenerList.getListenerList ();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==org.openide.filesystems.FileStatusListener.class) {
                ((org.openide.filesystems.FileStatusListener)listeners[i+1]).annotationChanged (event);
            }
        }
    }

    /** Adds listener for the veto of property change.
    * @param listener the listener
    */
    public final synchronized void addVetoableChangeListener(
        java.beans.VetoableChangeListener listener
    ) {
        if (listenerList == null) listenerList = new EventListenerList ();

        listenerList.add (java.beans.VetoableChangeListener.class, listener);
    }

    /** Removes listener for the veto of property change.
    * @param listener the listener
    */
    public final synchronized void removeVetoableChangeListener(
        java.beans.VetoableChangeListener listener
    ) {
        if (listenerList == null) return;

        listenerList.remove (java.beans.VetoableChangeListener.class, listener);
    }

    /** Fires property vetoable event.
    * @param name name of the property
    * @param o old value of the property
    * @param n new value of the property
    * @exception PropertyVetoException if an listener vetoed the change
    */
    protected final void fireVetoableChange (
        java.lang.String name,
        java.lang.Object o,
        java.lang.Object n
    ) throws PropertyVetoException {
        if (listenerList == null) return;


        java.beans.PropertyChangeEvent e = null;
        Object[] listeners = listenerList.getListenerList ();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==java.beans.VetoableChangeListener.class) {
                if (e == null)
                    e = new java.beans.PropertyChangeEvent (this, name, o, n);
                ((java.beans.VetoableChangeListener)listeners[i+1]).vetoableChange (e);
            }
        }
    }

    /** Registers PropertyChangeListener to receive events.
    *@param listener The listener to register.
    */
    public final synchronized void addPropertyChangeListener(
        java.beans.PropertyChangeListener listener
    ) {
        if (listenerList == null) listenerList = new EventListenerList ();

        listenerList.add (java.beans.PropertyChangeListener.class, listener);
    }

    /** Removes PropertyChangeListener from the list of listeners.
    *@param listener The listener to remove.
    */
    public final synchronized void removePropertyChangeListener(
        java.beans.PropertyChangeListener listener
    ) {
        if (listenerList == null) return;

        listenerList.remove (java.beans.PropertyChangeListener.class, listener);
    }

    /** Fires property change event.
    * @param name name of the property
    * @param o old value of the property
    * @param n new value of the property
    */
    protected final void firePropertyChange (String name, Object o, Object n) {
        if (listenerList == null) return;

        java.beans.PropertyChangeEvent e = null;
        Object[] listeners = listenerList.getListenerList ();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==java.beans.PropertyChangeListener.class) {
                if (e == null)
                    e = new java.beans.PropertyChangeEvent (this, name, o, n);
                ((java.beans.PropertyChangeListener)listeners[i+1]).propertyChange (e);
            }
        }
    }

    /** An action that it is to be called atomically with respect to file system event notification.
    * During its execution (via {@link FileSystem#runAtomicAction runAtomicAction})
    * no events about changes in file systems are fired.
    */
    public static interface AtomicAction {
        /** Executed when it is guaranteed that no events about changes
        * in filesystems will be notified.
        *
        * @exception IOException if there is an error during execution
        */
        public void run () throws IOException;
    }

    /** Interface that allows filesystems to set up the Java environment
    * for external execution and compilation.
    * Currently just used to append entries to the external class path.
    */
    public static abstract class Environment extends Object {
        /** Adds one element to the class path environment variable.
        * @param classPathElement string representing the one element
        */
        public void addClassPath (String classPathElement) {
        }
    }

    /** Allows a filesystem to annotate a group of files (typically comprising a data object) with additional markers.
     * <p>This could be useful, for
    * example, for a filesystem supporting version control.
    * It could annotate names and icons of data nodes according to whether the files were current, locked, etc.
    */
    public static interface Status {
        /** Annotate the name of a file cluster.
        * @param name the name suggested by default
        * @param files an immutable set of {@link FileObject}s belonging to this filesystem
        * @return the annotated name (may be the same as the passed-in name)
        * @exception ClassCastException if the files in the set are not of valid types
        */
        public String annotateName (String name, java.util.Set files);

        /** Annotate the icon of a file cluster.
         * <p>Please do <em>not</em> modify the original; create a derivative icon image,
         * using a weak-reference cache if necessary.
        * @param icon the icon suggested by default
        * @param iconType an icon type from {@link java.beans.BeanInfo}
        * @param files an immutable set of {@link FileObject}s belonging to this filesystem
        * @return the annotated icon (may be the same as the passed-in icon)
        * @exception ClassCastException if the files in the set are not of valid types
        */
        public java.awt.Image annotateIcon (java.awt.Image icon, int iconType, java.util.Set files);
    }

    /** Empty status */
    private static final Status STATUS_NONE = new Status () {
                public String annotateName (String name, java.util.Set files) {
                    return name;
                }

                public java.awt.Image annotateIcon (java.awt.Image icon, int iconType, java.util.Set files) {
                    return icon;
                }
            };

    /** Class used to notify events for the file system.
    */
    static abstract class EventDispatcher extends Object implements Runnable {
        public final void run () {
            dispatch ();
        }

        protected abstract void dispatch ();
    }

    /** Getter for the resource string
    * @param s the resource name
    * @return the resource
    */
    static String getString(String s) {
        return NbBundle.getBundle("org.openide.filesystems.Bundle").getString (s);
    }

    /** Creates message for given string property with one parameter.
    * @param s resource name
    * @param obj the parameter to the message
    * @return the string for that text
    */
    static String getString (String s, Object obj) {
        return MessageFormat.format (getString (s), new Object[] { obj });
    }

    /** Creates message for given string property with two parameters.
    * @param s resource name
    * @param obj1 the parameter to the message
    * @param obj2 the parameter to the message
    * @return the string for that text
    */
    static String getString (String s, Object obj1, Object obj2) {
        return MessageFormat.format (getString (s), new Object[] { obj1, obj2 });
    }

    /** Creates message for given string property with three parameters.
    * @param s resource name
    * @param obj1 the parameter to the message
    * @param obj2 the parameter to the message
    * @param obj3 the parameter to the message
    * @return the string for that text
    */
    static String getString (String s, Object obj1, Object obj2, Object obj3) {
        return MessageFormat.format (getString (s), new Object[] { obj1, obj2, obj3 });
    }

}

/*
 * Log
 *  29   Gandalf   1.28        1/12/00  Jesse Glick     [JavaDoc]
 *  28   Gandalf   1.27        1/12/00  Ian Formanek    NOI18N
 *  27   Gandalf   1.26        11/30/99 Ales Novak      exitAtomicAction
 *  26   Gandalf   1.25        11/2/99  Jaroslav Tulach Deleted PROP_STATUS
 *  25   Gandalf   1.24        10/29/99 Jaroslav Tulach MultiFileSystem + 
 *       FileStatusEvent
 *  24   Gandalf   1.23        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  23   Gandalf   1.22        10/1/99  Jaroslav Tulach FileObject.move & 
 *       FileObject.copy
 *  22   Gandalf   1.21        9/1/99   Jaroslav Tulach The DataNode reacts to 
 *       changes in FileSystem.getStatus by updating its name and icon.
 *  21   Gandalf   1.20        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  20   Gandalf   1.19        6/7/99   Jaroslav Tulach More backward 
 *       compatible.
 *  19   Gandalf   1.18        6/7/99   Jaroslav Tulach FS capabilities.
 *  18   Gandalf   1.17        6/1/99   Jaroslav Tulach Changes made during run 
 *       of runAtomicAction are fired synchronously at the end of 
 *       runAtomicActions
 *  17   Gandalf   1.16        6/1/99   Jaroslav Tulach synchronization on 
 *       atomic actions of FS
 *  16   Gandalf   1.15        4/20/99  Jesse Glick     [JavaDoc], and added 
 *       toString().
 *  15   Gandalf   1.14        4/12/99  Jesse Glick     [JavaDoc]
 *  14   Gandalf   1.13        3/26/99  Jesse Glick     [JavaDoc]
 *  13   Gandalf   1.12        3/26/99  Jaroslav Tulach 
 *  12   Gandalf   1.11        3/24/99  Jaroslav Tulach 
 *  11   Gandalf   1.10        3/21/99  Jaroslav Tulach Repository displayed ok.
 *  10   Gandalf   1.9         3/19/99  Jaroslav Tulach TopManager.getDefault 
 *       ().getRegistry ()
 *  9    Gandalf   1.8         3/15/99  Jesse Glick     [JavaDoc]
 *  8    Gandalf   1.7         3/13/99  Jaroslav Tulach FileSystem.Status & 
 *       lastModified
 *  7    Gandalf   1.6         3/1/99   Jesse Glick     [JavaDoc]
 *  6    Gandalf   1.5         2/11/99  Ian Formanek    Renamed FileSystemPool 
 *       -> Repository
 *  5    Gandalf   1.4         2/5/99   Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         2/4/99   Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         2/1/99   Jesse Glick     [JavaDoc]
 *  2    Gandalf   1.1         1/11/99  Jaroslav Tulach NbClassLoader extends 
 *       URLClassLoader
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.25        --/--/98 Jan Formanek    added and removed getIcon
 *  0    Tuborg    0.26        --/--/98 Jaroslav Tulach added setter and getter for hidden property, listeners support
 *  0    Tuborg    0.26        --/--/98 Jaroslav Tulach changed
 *  0    Tuborg    0.27        --/--/98 Jaroslav Tulach thread for firing events in filesystem + runAtomicAction method
 *  0    Tuborg    0.28        --/--/98 Jaroslav Tulach environment support
 *  0    Tuborg    0.29        --/--/98 Petr Hamernik   URL protocol
 *  0    Tuborg    0.30        --/--/98 Jaroslav Tulach added method for finding of resources (for use with URL)
 *  0    Tuborg    0.31        --/--/98 Ales Novak      NbfsURLConstants
 *  0    Tuborg    0.32        --/--/98 Jaroslav Tulach cleared filedispatch thread, invokeLater is used instead
 */
