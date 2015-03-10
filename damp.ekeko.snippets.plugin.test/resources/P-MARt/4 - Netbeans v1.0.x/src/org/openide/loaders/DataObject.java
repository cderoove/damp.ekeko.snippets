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

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;
import java.util.AbstractCollection;
import java.util.ResourceBundle;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;

import org.openide.*;
import org.openide.util.datatransfer.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.*;
import org.openide.util.enum.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.nodes.*;

/** Object that represents one JavaBean in the tree of beans representing
* data systems.
*
* @author Jaroslav Tulach, Petr Hamernik, Jan Jancura, Ian Formanek
*/
public abstract class DataObject extends Object implements Node.Cookie, Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 3328227388376142699L;

    /** Name of the template property. */
    public static final String PROP_TEMPLATE = "template"; // NOI18N

    /** Name of the name property. */
    public static final String PROP_NAME = "name"; // NOI18N

    /** Name of the help context property. */
    public static final String PROP_HELP = "helpCtx"; // NOI18N

    /** Name of the modified property. */
    public static final String PROP_MODIFIED = "modified"; // NOI18N

    /** Name of the property used during notification of changes in the set of cookies attached to this object. */
    public static final String PROP_COOKIE = Node.PROP_COOKIE;

    /** Name of valid property. Allows listening to deletion or disposal of the data object. */
    public static final String PROP_VALID = "valid"; // NOI18N

    /** Name of primary file property. Primary file is changed when the object is moved */
    public static final String PROP_PRIMARY_FILE = "primaryFile"; // NOI18N
    /** Name of files property. Allows listening to set of files handled by this object. */
    public static final String PROP_FILES = "files"; // NOI18N

    /** Extended attribute for holding the class of the loader that should
    * be used to recognize a file object before the normal processing takes
    * place.
    */
    static final String EA_ASSIGNED_LOADER = "NetBeansAttrAssignedLoader"; // NOI18N

    /** all modified data objects contains DataObjects.
    * ! Use syncModified for modifications instead !*/
    private static ModifiedRegistry modified = new ModifiedRegistry();
    /** sync modified data (for modification operations) 
     * @associates DataObject*/
    private static Set syncModified = Collections.synchronizedSet(modified);

    /** Modified flag */
    private boolean modif = false;

    /** the node delegate for this data object */
    private transient Node nodeDelegate;

    /** item with info about this data object */
    DataObjectPool.Item item;

    /** the loader for this data object */
    private DataLoader loader;

    /** property change listener support */
    private EventListenerList listeners;

    /** Create new data object.
    * @param pf primary file object for this data object
    * @param loader loader that created the data object
    * @exception DataObjectExistsException if there is already a data object
    *    for this primary file
    */
    public DataObject (FileObject pf, DataLoader loader) throws DataObjectExistsException {
        this (pf, DataObjectPool.POOL.register (pf, loader), loader);
    }

    /** Private constructor. At this time the constructor receives
    * the primary file and pool item where it should register itself.
    *
    * @param pf primary file
    * @param item the item to register into
    * @param loader loader that created the data object
    */
    private DataObject (FileObject pf, DataObjectPool.Item item, DataLoader loader) {
        this.item = item;
        this.loader = loader;
        item.setDataObject (this);
    }

    /* Finalize. Disposes the handler (calls {@link #dispose}).
    */
    protected final void finalize () {
        DataObjectPool.Item item = this.item;
        if (item != null) {
            item.deregister (false);
        }

        //    System.out.println ("DataObject finalized: " + getPrimaryFile ()); // NOI18N
    }

    // This method first unregisters the object, then calls method unreferenced.
    // After that it asks the parent folder to regenerate its list of children,
    // so different object is usually created for primary file of this object.
    /** Allows subclasses to discard the object. When an object is discarded,
    * it is released from the list of objects registered in the system.
    * Then the contents of the parent folder (if it still exists) are rescanned, which
    * may result in the creation of a new data object for the primary file.
    * <P>
    * The normal use of this method is to change the type of a data object.
    * Because this would usually only be invoked from
    * the original data object, it is protected.
    */
    protected void dispose () {
        DataObjectPool.Item item = this.item;
        if (item != null) {
            item.deregister (true);
            item.setDataObject(null);
            firePropertyChange (PROP_VALID, Boolean.FALSE, Boolean.TRUE);
        }
    }

    /** Setter that allows to destroy this data object. Because such
    * operation can be dangerous and not always possible (if the data object
    * is opened in editor) it can be vetoed. Either by this data object
    * or by any vetoable listener attached to this object (like editor support)
    *
    * @param valid should be false
    * @exception PropertyVetoException if the invalidation has been vetoed
    */
    public void setValid (boolean valid) throws PropertyVetoException {
        if (!valid && isValid ()) {
            fireVetoableChange (PROP_VALID, Boolean.FALSE, Boolean.TRUE);
            dispose ();
        }
    }

    /** Test whether the data object is still valid and usable.
    * <P>
    * The object can become invalid when it is deleted, its files are deleted, or
    * {@link #dispose} is called.
    * <P>
    * When the validity of the object changes a property change event is fired, so
    * anyone can listen and be notified when the object is deleted/disposed.
    */
    public final boolean isValid () {
        return item.isValid ();
    }



    /** Get the loader that created this data object.
    * @return the data loader
    */
    public final DataLoader getLoader () {
        return loader;
    }

    /** Mark all contained files as belonging to this loader.
     * If the files are rescanned (e.g. after a disposal), the current data loader will be given preference.
    */
    protected final void markFiles () throws IOException {
        Iterator en = files ().iterator ();
        while (en.hasNext ()) {
            FileObject fo = (FileObject)en.next ();
            loader.markFile (fo);
        }
    }

    /** Get all contained files.
     * These file objects should ideally have had the {@link FileObject#setImportant important flag} set appropriately.
    * <P>
    * The default implementation returns a set consisting only of the primary file.
    *
    * @return set of {@link FileObject}s
    */
    public Set files () {
        return java.util.Collections.singleton (getPrimaryFile ());
    }


    /** Get the node delegate. Either {@link #createNodeDelegate creates it} (if it does not
    * already exist) or
    * returns a previously created instance of it.
    *
    * @return the node delegate (without parent) for this data object
    */
    public final Node getNodeDelegate () {
        if (nodeDelegate == null) {
            synchronized (this) {
                if (nodeDelegate == null) {
                    nodeDelegate = createNodeDelegate ();
                }
            }
        }
        return nodeDelegate;
    }

    /** This method allows DataFolder to filter its nodes.
    *
    * @param filter filter for subdata objects
    * @return the node delegate (without parent) the node is new instance
    *   of node and can be inserted to any place in the hierarchy
    */
    Node getClonedNodeDelegate (DataFilter filter) {
        return getNodeDelegate ().cloneNode ();
    }

    // When a node for representation
    // in a parent is requested by a call to getNode (parent) it is the exact copy of this node
    // with only parent changed.
    /** Provides node that should represent this data object.
    * <p>The default implementation creates an instance of {@link DataNode}.
    * <P>
    * This method is called only once per data object.
    *
    * @return the node delegate (without parent) for this data object
    */
    protected Node createNodeDelegate () {
        return new DataNode (this, Children.LEAF);
    }

    /** Obtains lock for primary file.
    *
    * @return the lock
    * @exception IOException if taking the lock fails
    */
    protected FileLock takePrimaryFileLock () throws IOException {
        return getPrimaryFile ().lock ();
    }

    /** Package private method to assign template attribute to a file.
    * Used also from FileEntry.
    *
    * @param fo the file
    * @param newTempl is template or not
    * @return true if the value change/false otherwise
    */
    static boolean setTemplate (FileObject fo, boolean newTempl) throws IOException {
        boolean oldTempl = false;

        Object o = fo.getAttribute(DataObject.PROP_TEMPLATE);
        if ((o instanceof Boolean) && ((Boolean)o).booleanValue())
            oldTempl = true;
        if (oldTempl == newTempl)
            return false;

        fo.setAttribute(DataObject.PROP_TEMPLATE, (newTempl ? new Boolean(true) : null));

        return true;
    }

    /** Set the template status of this data object.
    * @param newTempl <code>true</code> if the object should be a template
    * @exception IOException if setting the template state fails
    */
    public final void setTemplate (boolean newTempl) throws IOException {
        if (!setTemplate (getPrimaryFile(), newTempl)) {
            // no change in state
            return;
        }

        firePropertyChange(DataObject.PROP_TEMPLATE, new Boolean(!newTempl), new Boolean(newTempl));
    }

    /** Get the template status of this data object.
    * @return <code>true</code> if it is a template
    */
    public final boolean isTemplate () {
        Object o = getPrimaryFile().getAttribute(PROP_TEMPLATE);
        boolean ret = false;
        if (o instanceof Boolean)
            ret = ((Boolean) o).booleanValue();
        return ret;
    }


    /** Test whether the object may be deleted.
    * @return <code>true</code> if it may
    */
    public abstract boolean isDeleteAllowed ();

    /** Test whether the object may be copied.
    * @return <code>true</code> if it may
    */
    public abstract boolean isCopyAllowed ();

    /** Test whether the object may be moved.
    * @return <code>true</code> if it may
    */
    public abstract boolean isMoveAllowed ();

    /** Test whether the object may create shadows.
     * <p>The default implementation returns <code>true</code>.
    * @return <code>true</code> if it may
    */
    public boolean isShadowAllowed () {
        return true;
    }

    /** Test whether the object may be renamed.
    * @return <code>true</code> if it may
    */
    public abstract boolean isRenameAllowed ();


    /** Test whether the object is modified.
    * @return <code>true</code> if it is modified
    */
    public boolean isModified() {
        return modif;
    }

    /** Set whether the object is considered modified.
     * Also fires a change event.
    * If the new value is <code>true</code>, the data object is added into a {@link #getRegistry registry} of opened data objects.
    * If the new value is <code>false</code>,
    * the data object is removed from the registry.
    */
    public void setModified(boolean modif) {
        if (this.modif != modif) {
            this.modif = modif;
            if (modif) {
                syncModified.add (this);
            } else {
                syncModified.remove (this);
            }
            firePropertyChange(DataObject.PROP_MODIFIED, new Boolean(!modif), new Boolean(modif));
        }
    }

    /** Get help context for this object.
    * @return the help context
    */
    public abstract HelpCtx getHelpCtx ();

    /** Get the primary file for this data object.
     * For example,
    * Java source uses <code>*.java</code> and <code>*.class</code> files but the primary one is
    * always <code>*.java</code>. Please note that two data objects are {@link #equals equivalent} if
    * they use the same primary file.
    * <p><em>Warning:</em> do not call {@link Node#getHandle} or {@link DefaultHandle#createHandle} in this method.
    *
    * @return the primary file
    */
    public final FileObject getPrimaryFile () {
        return item.primaryFile;
    }

    /** Finds the data object for a specified file object.
    * @param fo file object
    * @return the data object for that file
    * @exception DataObjectNotFoundException if the file does not have a
    *   data object
    */
    public static DataObject find (FileObject fo)
    throws DataObjectNotFoundException {
        try {
            // try to scan directly the pool (holds only primary files)
            DataObject obj = DataObjectPool.POOL.find (fo);
            if (obj != null) {
                return obj;
            }

            // try to use the loaders machinery
            obj = TopManager.getDefault ().getLoaderPool ().findDataObject (fo);
            if (obj != null) {
                return obj;
            }
        } catch (DataObjectExistsException ex) {
            return ex.getDataObject ();
        } catch (IOException ex) {
        }
        throw new DataObjectNotFoundException (fo);
    }

    /** Get the registry containing all modified objects.
    *
    * @return the registry
    */
    public static Registry getRegistry () {
        return Registry.INSTANCE;
    }

    /** Get the name of the data object.
    * <p>The default implementation uses the name of the primary file.
    * @return the name
    */
    public String getName () {
        return getPrimaryFile ().getName ();
    }

    /** Get the folder this data object is stored in.
    * @return the folder; <CODE>null</CODE> if the primary file
    *   is the {@link FileObject#isRoot root} of its filesystem
    */
    public final DataFolder getFolder () {
        FileObject fo = getPrimaryFile ().getParent ();
        // could throw IllegalArgumentException but only if fo is not folder
        // => then there is a bug in filesystem implementation
        return fo == null ? null : DataFolder.findFolder (fo);
    }

    /** Copy this object to a folder. The copy of the object is required to
    * be deletable and movable.
    * <p>An event is fired, and atomicity is implemented.
    * @param f the folder to copy the object to
    * @exception IOException if something went wrong
    * @return the new object
    */
    public final DataObject copy (final DataFolder f) throws IOException {
        final DataObject[] result = new DataObject[1];
        FileSystem fs = f.getPrimaryFile ().getFileSystem ();
        fs.runAtomicAction (new FileSystem.AtomicAction () {
                                public void run () throws IOException {
                                    result[0] = handleCopy (f);
                                }
                            });
        fireOperationEvent (
            new OperationEvent.Copy (result[0], this), OperationEvent.COPY
        );
        return result[0];
    }

    /** Copy this object to a folder (implemented by subclasses).
    * @param f target folder
    * @return the new data object
    * @exception IOException if an error occures
    */
    protected abstract DataObject handleCopy (DataFolder f) throws IOException;

    /** Delete this object.
     * <p>Events are fired and atomicity is implemented.
    * @exception IOException if an error occures
    */
    public final synchronized void delete () throws IOException {
        // the object is ready to be closed
        FileSystem fs = getPrimaryFile ().getFileSystem ();
        fs.runAtomicAction (new FileSystem.AtomicAction () {
                                public void run () throws IOException {
                                    handleDelete ();
                                    item.deregister(false);
                                    item.setDataObject(null);
                                }
                            });
        firePropertyChange (PROP_VALID, Boolean.FALSE, Boolean.TRUE);
        fireOperationEvent (
            new OperationEvent (this), OperationEvent.DELETE
        );
    }

    /** Delete this object (implemented by subclasses).
    * @exception IOException if an error occures
    */
    protected abstract void handleDelete () throws IOException;


    /** Rename this object.
     * <p>Events are fired and atomicity is implemented.
    *
    * @param name the new name
    *
    * @exception IOException if an error occures
    */
    public final synchronized void rename (final String name) throws IOException {
        String oldName = getName ();

        if (oldName.equals (name)) return; // the new name is the same as the old one

        FileObject old = getPrimaryFile ();

        // executes atomic action with renaming
        FileSystem fs = old.getFileSystem ();
        fs.runAtomicAction (new FileSystem.AtomicAction () {
                                public void run () throws IOException {
                                    FileObject mf = handleRename (name);
                                    item.changePrimaryFile (mf);
                                }
                            });

        firePropertyChange (PROP_PRIMARY_FILE, old, getPrimaryFile ());
        fireOperationEvent (
            new OperationEvent.Rename (this, oldName), OperationEvent.RENAME
        );

        // rename succeed
        firePropertyChange(DataObject.PROP_NAME, oldName, getName ());
    }

    /** Rename this object (implemented in subclasses).
    *
    * @param name name to rename the object to
    * @return new primary file of the object
    * @exception IOException if an error occures
    */
    protected abstract FileObject handleRename (String name) throws IOException;

    /** Move this object to another folder.
     * <p>An event is fired and atomicity is implemented.
    * @param df folder to move object to
    * @exception IOException if an error occurs
    */
    public final void move (final DataFolder df) throws IOException {
        if ((getFolder () == null)) return; // cannot move filesystem root
        if (df.equals (getFolder ())) return; // if the destination folder is the same as the current one ==>> do nothing

        // executes atomic action for moving
        FileObject old = getPrimaryFile ();
        FileSystem fs = old.getFileSystem ();
        fs.runAtomicAction (new FileSystem.AtomicAction () {
                                public void run () throws IOException {
                                    FileObject mf = handleMove (df);
                                    item.changePrimaryFile (mf);
                                }
                            });

        firePropertyChange (PROP_PRIMARY_FILE, old, getPrimaryFile ());
        fireOperationEvent (
            new OperationEvent.Move (this, old), OperationEvent.MOVE
        );

    }

    /** Move this object to another folder (implemented in subclasses).
    *
    * @param df target data folder
    * @return new primary file of the object
    * @exception IOException if an error occures
    */
    protected abstract FileObject handleMove (DataFolder df) throws IOException;

    /** Creates shadow for this object in specified folder (overridable in subclasses).
     * <p>The default
    * implementation creates a reference data shadow and pastes it into
    * the specified folder.
    *
    * @param f the folder to create a shortcut in
    * @return the shadow
    */
    protected DataShadow handleCreateShadow (DataFolder f) throws IOException {
        return DataShadow.create (f, this);
    }

    /** Creates shadow for this object in specified folder.
     * <p>An event is fired and atomicity is implemented.
    *
    * @param f the folder to create shortcut in
    * @return the shadow
    */
    public final DataShadow createShadow (final DataFolder f) throws IOException {
        final DataShadow[] result = new DataShadow[1];

        FileSystem fs = f.getPrimaryFile ().getFileSystem ();
        fs.runAtomicAction (new FileSystem.AtomicAction () {
                                public void run () throws IOException {
                                    result[0] =  handleCreateShadow (f);
                                }
                            });
        fireOperationEvent (
            new OperationEvent.Copy (result[0], this), OperationEvent.SHADOW
        );
        return result[0];
    }

    /** Create a new object from template (with a name depending on the template).
    *
    * @param f folder to create object in
    * @return new data object based on this one
    * @exception IOException if an error occured
    * @see #createFromTemplate(DataFolder,String)
    */
    public final DataObject createFromTemplate (DataFolder f)
    throws IOException {
        return createFromTemplate (f, null);
    }

    /** Create a new object from template.
    * Asks {@link #handleCreateFromTemplate}.
    *
    * @param f folder to create object in
    * @param name name of object that should be created, or <CODE>null</CODE> if the
    *    name should be same as that of the template (or otherwise mechanically generated)
    * @return the new data object
    * @exception IOException if an error occured
    */
    public final DataObject createFromTemplate (
        final DataFolder f, final String name
    ) throws IOException {
        final DataObject[] result = new DataObject[1];

        FileSystem fs = f.getPrimaryFile ().getFileSystem ();
        fs.runAtomicAction (new FileSystem.AtomicAction () {
                                public void run () throws IOException {
                                    result[0] = handleCreateFromTemplate (f, name);
                                }
                            });

        fireOperationEvent (
            new OperationEvent.Copy (result[0], this), OperationEvent.TEMPL
        );

        return result[0];
    }

    /** Create a new data object from template (implemented in subclasses).
     * This method should
    * copy the content of the template to the destination folder and assign a new name
    * to the new object.
    *
    * @param df data folder to create object in
    * @param name name to give to the new object (or <CODE>null</CODE>
    *    if the name should be chosen according to the template)
    * @return the new data object
    * @exception IOException if an error occured
    */
    protected abstract DataObject handleCreateFromTemplate (
        DataFolder df, String name
    ) throws IOException;


    /** Fires operation event to data loader pool.
    * @param ev the event
    * @param type OperationEvent.XXXX constant
    */
    private static void fireOperationEvent (OperationEvent ev, int type) {
        TopManager.getDefault ().getLoaderPool ().fireOperationEvent (ev, type);
    }

    //
    // Property change support
    //

    /** Getter for event listener list.
    */
    private EventListenerList listeners () {
        if (listeners == null) {
            synchronized (this) {
                if (listeners == null) {
                    listeners = new EventListenerList ();
                }
            }
        }
        return listeners;
    }

    /** @param l the listener
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        listeners ().add (PropertyChangeListener.class, l);
    }

    /** @param l the listener
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        listeners ().remove (PropertyChangeListener.class, l);
    }

    /** Fires property change notification to all listeners registered via
    * {@link #addPropertyChangeListener}.
    *
    * @param name of property
    * @param oldValue old value
    * @param newValue new value
    */
    protected final void firePropertyChange (String name, Object oldValue, Object newValue) {
        // Guaranteed to return a non-null array
        Object[] listeners = listeners ().getListenerList();
        PropertyChangeEvent fooEvent = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == PropertyChangeListener.class) {
                // Lazily create the event:
                if (fooEvent == null) {
                    fooEvent = new PropertyChangeEvent (this, name, oldValue, newValue);
                }

                ((PropertyChangeListener)listeners[i+1]).propertyChange (fooEvent);
            }
        }
    }

    //
    // Property change support
    //

    /** @param l the listener
    */
    public void addVetoableChangeListener (VetoableChangeListener l) {
        listeners ().add (VetoableChangeListener.class, l);
    }

    /** @param l the listener
    */
    public void removeVetoableChangeListener (VetoableChangeListener l) {
        listeners ().remove (VetoableChangeListener.class, l);
    }

    /** Fires vetoable change notification.
    *
    * @param name of property
    * @param oldValue old value
    * @param newValue new value
    * @exception PropertyVetoException if the change has been vetoed
    */
    protected final void fireVetoableChange (
        String name, Object oldValue, Object newValue
    ) throws PropertyVetoException {
        // Guaranteed to return a non-null array
        Object[] listeners = listeners ().getListenerList();
        PropertyChangeEvent fooEvent = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == VetoableChangeListener.class) {
                // Lazily create the event:
                if (fooEvent == null) {
                    fooEvent = new PropertyChangeEvent (this, name, oldValue, newValue);
                }

                ((VetoableChangeListener)listeners[i+1]).vetoableChange (fooEvent);
            }
        }
    }

    //
    // Cookie
    //

    /** Obtain a cookie from the data object.
    * May be overridden by subclasses to extend the behaviour of
    * data objects.
    * <P>
    * The default implementation tests if this object is of the requested class and
    * if so, returns it.
    *
    * @param c class of requested cookie
    * @return a cookie or <code>null</code> if such cookies are not supported
    */
    public Node.Cookie getCookie (Class c) {
        if (c.isInstance (this)) {
            return this;
        }
        return null;
    }


    // =======================
    //  Serialization methods
    //

    /** The Serialization replacement for this object stores the primary file instead.
     * @return a replacement
    */
    public Object writeReplace () {
        return new Replace (this);
    }


    /** The default replace for the data object
    */
    private static final class Replace extends Object implements Serializable {
        /** the primary file */
        private FileObject fo;
        /** the object to return */
        private transient DataObject obj;

        static final long serialVersionUID =-627843044348243058L;
        /** Constructor.
        * @param obj the object to use
        */
        public Replace (DataObject obj) {
            this.obj = obj;
            this.fo = obj.getPrimaryFile ();
        }

        public Object readResolve () {
            return obj;
        }

        /** Read method */
        private void readObject (ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            ois.defaultReadObject ();
            // can also throw an IOException
            obj = find (fo);
        }
    }

    /** Getter for a text from resource bundle.
    */
    static String getString (String name) {
        return NbBundle.getBundle ("org.openide.loaders.Bundle").getString (name);
    }

    /** Registry of modified data objects.
     * The registry permits attaching of a change listener
    * to be informed when the count of modified objects changes.
    */
    public static final class Registry extends Object {
        /** the only instance */
        private static Registry INSTANCE = new Registry ();

        /** Private constructor */
        private Registry () {
        }

        /** Add new listener to changes in the set of modified objects.
        * @param chl listener to add
        */
        public void addChangeListener (final ChangeListener chl) {
            modified.addChangeListener(chl);
        }

        /** Remove a listener to changes in the set of modified objects.
        * @param chl listener to remove
        */
        public void removeChangeListener (final ChangeListener chl) {
            modified.removeChangeListener(chl);
        }

        /** Get a set of modified data objects.
        * @return an unmodifiable set of {@link DataObject}s
        */
        public Set getModifiedSet () {
            return Collections.unmodifiableSet(syncModified);
        }

        /** Get modified objects.
        * @return array of objects
        */
        public DataObject[] getModified () {
            return (DataObject[])modified.toArray (new DataObject[0]);
        }
    }

    private static final class ModifiedRegistry extends HashSet {

        /** Set of listeners listening to changes to the set of modified objs 
         * @associates ChangeListener*/
        private HashSet listeners;

        static final long serialVersionUID =-2861723614638919680L;
        /** Adds new listener.
        * @param chl new listener
        */
        public final synchronized void addChangeListener (final ChangeListener chl) {
            if (listeners == null) listeners = new HashSet(5);
            listeners.add(chl);
        }

        /** Removes listener from the listener list.
        * @param chl listener to remove
        */
        public final synchronized void removeChangeListener (final ChangeListener chl) {
            if (listeners == null) return;
            listeners.remove(chl);
        }

        /***** overriding of methods which change content in order to notify
        * listeners about the content change */

        public boolean add (Object o) {
            boolean result = super.add(o);
            if (result) fireChangeEvent(new ChangeEvent(this));
            return result;
        }

        public boolean remove (Object o) {
            boolean result = super.remove(o);
            if (result) fireChangeEvent(new ChangeEvent(this));
            return result;
        }

        /** Fires change event to all listeners.
        * @param che change event
        */
        protected final void fireChangeEvent (ChangeEvent che) {
            if (listeners == null) return;
            HashSet cloned;
            // clone listener list
            synchronized (this) {
                cloned = (HashSet)listeners.clone();
            }
            // fire on cloned list to prevent from modifications when firing
            for (Iterator iter = cloned.iterator(); iter.hasNext(); ) {
                ((ChangeListener)iter.next()).stateChanged(che);
            }
        }

    }  // end of ModifiedRegistry inner class

}

/*
 * Log
 *  27   Gandalf   1.18.1.7    11/12/98 Jaroslav Tulach
 *  26   Gandalf   1.18.1.6    11/11/98 Ian Formanek
 *  25   Gandalf   1.18.1.5    11/09/98 Ian Formanek
 *  24   Gandalf   1.18.1.4    11/05/98 Jaroslav Tulach Special properties for
 *                                                      Folder.
 *  23   Gandalf   1.18.1.3    11/05/98 Jaroslav Tulach Sorting and ordering of
 *                                                      DataFolders.
 *
 *  22   Gandalf   1.18.1.2    11/04/98 Jaroslav Tulach Recognization of folders.
 *  21   Gandalf   1.18.1.1    10/30/98 Jaroslav Tulach
 *  20   Gandalf   1.18.1.0    10/30/98 Jaroslav Tulach
 *  19   Tuborg    1.18        10/13/98 Jaroslav Tulach Can switch template when
 *                                                      text in Java editor is
 *                                                      modified.
 *  18   Tuborg    1.17        09/03/98 Ian Formanek    Fixed bug 596 - If an
 *                                                      object is cutted and pasted
 *                                                      into the same folder, the
 *                                                      explorer displays two nodes
 *                                                      for this object and it does
 *                                                      not  behave correctly.
 *  17   Tuborg    1.16        09/03/98 Ian Formanek    The Rename action on
 *                                                      DataObject chacks whether
 *                                                      the new name is not the
 *                                                      same as the old one
 *  16   Tuborg    1.15        08/21/98 Jaroslav Tulach serialVersionUID + compiles
 *  15   Tuborg    1.14        08/21/98 Jaroslav Tulach serialVersionUID
 *  14   Tuborg    1.13        08/07/98 Ian Formanek    Reflecting Save action
 *                                                      changes (it is now a
 *                                                      CookieAction) - the
 *                                                      SaveAction must be
 *                                                      refreshed after setting
 *                                                      modified flag
 *  13   Tuborg    1.12        07/31/98 Jan Jancura     Customize as in Customize
 *                                                      bean action
 *  12   Tuborg    1.11        07/22/98 Jan Palka       Remove check
 *  11   Tuborg    1.10        07/17/98 Jan Palka       Add check if a given
 *                                                      template name is valid
 *  10   Tuborg    1.9         07/15/98 Ales Novak
 *  9    Tuborg    1.8         07/10/98 Ales Novak
 *  8    Tuborg    1.7         07/10/98 Jaroslav Tulach
 *  7    Tuborg    1.6         07/10/98 Jaroslav Tulach
 *  6    Tuborg    1.5         07/10/98 Jaroslav Tulach
 *  5    Tuborg    1.4         07/10/98 Ales Novak
 *  4    Tuborg    1.3         07/03/98 Ian Formanek    Setting isTemplate property
 *                                                      is back
 *  3    Tuborg    1.2         06/16/98 Jaroslav Tulach
 *  2    Tuborg    1.1         06/15/98 Ian Formanek
 *  1    Tuborg    1.0         06/11/98 David Peroutka
 * $
 * Beta Change History:
 *  0    Tuborg    1.00        --/--/98 Jaroslav Tulach new design, composed DataHandler together with DataObject
 *  0    Tuborg    1.02        --/--/98 Jaroslav Tulach removed line set, created cookie LineCookie
 *  0    Tuborg    1.03        --/--/98 Jaroslav Tulach find does not return null
 *  0    Tuborg    1.04        --/--/98 Jaroslav Tulach sometimes returns FilterNode, sometimes SerializableFilterNode
 *  0    Tuborg    1.05        --/--/98 Jaroslav Tulach defaultInstantiateTemplate method
 *  0    Tuborg    1.06        --/--/98 Jan Formanek    bugfix
 *  0    Tuborg    1.07        --/--/98 Jan Formanek    isModified () added
 *  0    Tuborg    1.08        --/--/98 Petr Hamernik   isModified changed
 *  0    Tuborg    1.09        --/--/98 Petr Hamernik   opened objects and opened views removed, modified objects added
 *  0    Tuborg    1.10        --/--/98 Jan Formanek    "File" PropertySet removed (commented out)
 *  0    Tuborg    1.11        --/--/98 Petr Hamernik   rename cookie
 *  0    Tuborg    1.12        --/--/98 Jan Formanek    reflecting changes in cookies
 *  0    Tuborg    1.14        --/--/98 Jan Formanek    removed full.hack
 *  0    Tuborg    1.15        --/--/98 Jan Formanek    improved focus in createFromTemplate input line
 */
