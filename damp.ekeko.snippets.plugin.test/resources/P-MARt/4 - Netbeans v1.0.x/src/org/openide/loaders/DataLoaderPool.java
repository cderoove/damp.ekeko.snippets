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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.openide.filesystems.*;
import org.openide.util.RequestProcessor;
import org.openide.util.enum.ArrayEnumeration;
import org.openide.util.enum.FilterEnumeration;
import org.openide.util.enum.SingletonEnumeration;
import org.openide.util.enum.SequenceEnumeration;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

/** Pool of data loaders.
* Provides access to set of registered
* {@link DataLoader loaders} in the system. They are used to find valid data objects
* for given files.
* <P>
* The default instance can be retrieved with
* {@link org.openide.TopManager#getLoaderPool}.
*
* @author Jaroslav Tulach, Petr Hamernik, Dafe Simonek
*/
public abstract class DataLoaderPool extends Object
    implements java.io.Serializable {
    /** SUID */
    static final long serialVersionUID=-360141823874889956L;
    /** standard system loaders. Accessed by getSystemLoaders method only */
    private static DataLoader[] systemLoaders;
    /** standard default loaders. Accessed by getDefaultLoaders method only */
    private static DataLoader[] defaultLoaders;

    /** Cache of loaders for faster toArray() method. */
    private transient DataLoader[] loaderArray;
    /** maps class names of loaders to the loaders (String, DataLoader) */
    private transient HashMap map;
    /** List of listeners (ChangeListener) 
     * @associates ChangeListener*/
    private transient HashSet listeners;
    /** set of listeners (OperationListener) 
     * @associates OperationListener*/
    private transient HashSet operations;

    /** prefered loader */
    private transient DataLoader preferredLoader;

    /** Create new loader pool.
    */
    protected DataLoaderPool () {
    }

    /** Create new loader pool and set preferred loader.
    * The preferred loader will be asked before any other to recognize files (also before the system
    * loader).
    *
    * @param loader the preferred loader
    */
    protected DataLoaderPool (DataLoader loader) {
        preferredLoader = loader;
    }

    /** Get an enumeration of data loaders.
     * Must be overridden in subclasses to provide a list of additional loaders.
    * The list should <em>not</em> include the preferred loader.
    *
    * @return enumeration of {@link DataLoader}s
    */
    protected abstract Enumeration loaders ();

    /** Add a new listener to the listener list. A listener is notified of
    * any change which was made to the loader pool (add, remove, or reorder).
    *
    * @param chl new listener
    */
    public final synchronized void addChangeListener (ChangeListener chl) {
        if (listeners == null)
            listeners = new HashSet();
        listeners.add(chl);
    }

    /** Remove a listener from the listener list.
    *
    * @param chl listener to remove
    */
    public final synchronized void removeChangeListener (ChangeListener chl) {
        if (listeners == null)
            return;
        listeners.remove(chl);
    }

    // Clears loaderArray before firing a change.
    /** Fire change event to all listeners. Asynchronously.
    * @param che change event
    */
    protected final void fireChangeEvent (final ChangeEvent che) {
        if (listeners == null)
            return;
        loaderArray = null;
        map = null;

        HashSet cloned;
        // clone listener list
        synchronized (this) {
            cloned = (HashSet)listeners.clone();
        }

        // fire on cloned list to prevent from modifications when firing
        Iterator iter = cloned.iterator();
        while (iter.hasNext()) {
            final ChangeListener l = (ChangeListener)iter.next();
            // separates the task to small pieces not to slow down the
            // rest of the IDE
            RequestProcessor.postRequest (new Runnable () {
                                              public void run () {
                                                  l.stateChanged(che);
                                              }
                                          });
        }
    }

    /** Add a listener for operations on data objects.
     * @param l the listener
    */
    public synchronized final void addOperationListener (OperationListener l) {
        if (operations == null) operations = new HashSet ();
        operations.add (l);
    }

    /** Remove a listener for operations on data objects.
     * @param l the listener
    */
    public synchronized final void removeOperationListener (OperationListener l) {
        if (operations != null) {
            operations.remove (l);
        }
    }

    /** Fires operation event to all listeners.
    * Clears loaderArray before firing a change.
    * @param ev event to fire
    * @param type the type of the event
    */
    final void fireOperationEvent (OperationEvent ev, int type) {
        if (operations == null)
            return;
        HashSet cloned;
        // clone listener list
        synchronized (this) {
            cloned = (HashSet)operations.clone();
        }
        // fire on cloned list to prevent from modifications when firing
        for (Iterator iter = cloned.iterator(); iter.hasNext(); ) {
            OperationListener l = (OperationListener)iter.next ();
            switch (type) {
            case OperationEvent.COPY:
                l.operationCopy ((OperationEvent.Copy)ev);
                break;
            case OperationEvent.MOVE:
                l.operationMove ((OperationEvent.Move)ev);
                break;
            case OperationEvent.DELETE:
                l.operationDelete (ev);
                break;
            case OperationEvent.RENAME:
                l.operationRename ((OperationEvent.Rename)ev);
                break;
            case OperationEvent.SHADOW:
                l.operationCreateShadow ((OperationEvent.Copy)ev);
                break;
            case OperationEvent.TEMPL:
                l.operationCreateFromTemplate ((OperationEvent.Copy)ev);
                break;
            case OperationEvent.CREATE:
                l.operationPostCreate (ev);
                break;
            }
        }
    }

    /** Get an enumeration of all loaders, including the preferred and system loaders.
    * This should be the list of loaders as actually used by the system.
    * Typically it will consist of, in this order:
    * <ol>
    * <li>The preferred loader, if any.
    * <li>The system loaders, such as may be used for folders, shadows, etc.
    * <li>Module-specified loaders.
    * <li>The loader for instance data objects.
    * <li>Default loaders, which may handle files not otherwise recognizable.
    * </ol>
    * Applications should not rely on the exact contents of the pool,
    * rather the fact that this contains all the loaders which are
    * capable of recognizing files in the order in which they are
    * called.
    * @return enumeration of {@link DataLoader}s */
    public final Enumeration allLoaders () {
        // enumeration of systemloaders followed by normal loaders
        Enumeration en =new SequenceEnumeration (
                            new SequenceEnumeration (
                                new ArrayEnumeration (getSystemLoaders ()),
                                loaders ()
                            ),
                            new ArrayEnumeration (getDefaultLoaders ())
                        );
        if (preferredLoader != null) {
            // prepends preferred loader
            Enumeration queue = new SingletonEnumeration (preferredLoader);
            return new SequenceEnumeration (queue, en);
        } else {
            // enumeration without pref. loader
            return en;
        }
    }

    /** Get an array of loaders that are currently registered.
    * Does not include special system loaders, etc.
    * @return array of loaders
    * @see #loaders
    */
    public DataLoader[] toArray () {
        DataLoader[] localArray = loaderArray;
        if (localArray != null)
            return localArray;
        ArrayList loaders = new ArrayList ();
        Enumeration en = loaders ();
        while (en.hasMoreElements ()) {
            loaders.add(en.nextElement ());
        }
        localArray = new DataLoader[loaders.size()];
        localArray = (DataLoader[])loaders.toArray(localArray);
        loaderArray = localArray;
        return localArray;
    }

    /** Finds the first producer of a representation class.
    * Scans through the list of all loaders and returns the first one
    * whose representation class is a superclass of <code>clazz</code>.
    *
    * @param clazz class to find producer for
    * @return data loader or <CODE>null</CODE> if there is no loader that
    *   can produce the class
    */
    public final DataLoader firstProducerOf (Class clazz) {
        Enumeration en = allLoaders ();
        while (en.hasMoreElements ()) {
            DataLoader dl = (DataLoader)en.nextElement ();
            if (dl.getRepresentationClass ().isAssignableFrom (clazz)) {
                // representation class is super class of clazz
                return dl;
            }
        }
        return null;
    }

    /** Get an enumeration of all producers of a representation class.
    * @see #firstProducerOf
    *
    * @param clazz class to find producers for
    * @return enumeration of {@link DataLoader}s
    */
    public final Enumeration producersOf (final Class clazz) {
        return new FilterEnumeration (allLoaders ()) {
                   /** Accepts only those loaders that produces superclass of clazz
                   */
                   public boolean accept (Object o) {
                       DataLoader dl = (DataLoader)o;
                       return clazz.isAssignableFrom( dl.getRepresentationClass() );
                   }
               };
    }


    /** private class for next method. Empty implementation of
    * DataLoaderRecognized.
    */
    private static final DataLoader.RecognizedFiles emptyDataLoaderRecognized =
        new DataLoader.RecognizedFiles () {
            /** No op. replacement.
            *
            * @param fo file object to exclude
            */
            public void markRecognized (FileObject fo) {
            }
        };

    /** Find a data object for this file object.
    * All loaders are asked to recognize it according to their priority.
    * <p><em>Note:</em> normally you are looking for the data object, whether one had been created already
    * or not; so do not use this, rather use {@link DataObject#find}!
    * @param fo file object to recognize
    * @return the data object for this object or <CODE>null</CODE> if
    *   no loader recognizes this file
    * @exception DataObjectExistsException if the object for this primary file
    *   already exists
    * @exception IOException if the data object is recognized but
    *   an error occurs during instantiation
    * @see #findDataObject(FileObject, DataLoader.RecognizedFiles)
    */
    public DataObject findDataObject (FileObject fo) throws IOException {
        return findDataObject (fo, emptyDataLoaderRecognized);
    }

    /** Find a data object for this file object, considering already-recognized files.
     * First of all looks at the
    * file extended attribute <code>NetBeansDataLoader</code>; if it is set and it
    * contains the class name of a valid {@link DataLoader}, that loader is given preference.
    * For all loaders used, the first to return non-<code>null</code> from {@link DataLoader#findDataObject}
    * is used.
    * <p><em>Note:</em> normally you are looking for the data object, whether one had been created already
    * or not; so do not use this, rather use {@link DataObject#find}!
    *
    * @param fo file object to recognize
    * @param r recognized files buffer
    * @return the data object for this object
    * @exception DataObjectExistsException if the object for this primary file
    *   already exists
    * @exception IOException if the data object is recognized but
    *   an error occurs during instantiation
    */
    public DataObject findDataObject (
        FileObject fo, DataLoader.RecognizedFiles r
    ) throws IOException {
        // try to find assigned loader
        String assignedLoaderName = (String)fo.getAttribute (DataObject.EA_ASSIGNED_LOADER);
        if (assignedLoaderName != null) {
            DataLoader l = getAssignedDataLoader(assignedLoaderName);
            if (l != null) {
                DataObject obj = l.findDataObject (fo, r);
                if (obj != null) {
                    // notify it
                    fireOperationEvent (new OperationEvent (obj), OperationEvent.CREATE);
                    // file has been recognized
                    return obj;
                }
            }
        }

        // scan through loaders
        java.util.Enumeration en = allLoaders ();
        while (en.hasMoreElements ()) {
            DataLoader l = (DataLoader)en.nextElement ();
            DataObject obj = l.findDataObject (fo, r);
            if (obj != null) {
                // the loader recognized the file

                // notify it
                fireOperationEvent (new OperationEvent (obj), OperationEvent.CREATE);

                return obj;
            }
        }
        return null;
    }

    /** Lazy getter for system loaders.
    */
    private static DataLoader[] getSystemLoaders () {
        if (systemLoaders == null) {
            systemLoaders = new DataLoader [] {
                                new FolderLoader (),
                                new ShadowLoader ()
                            };
        }
        return systemLoaders;
    }

    /** Lazy getter for default loaders.
    */
    private static DataLoader[] getDefaultLoaders () {
        if (defaultLoaders == null) {
            defaultLoaders = new DataLoader [] {
                                 new XMLDataObject.Loader (),
                                 new InstanceLoader (),
                                 new DefaultLoader ()
                             };
        }
        return defaultLoaders;
    }

    /** Getter for default file loader
    * @return the default file loader
    */
    static DataLoader getDefaultFileLoader () {
        return getDefaultLoaders ()[2];
    }

    /** Getter for folder loader
    * @return the folder loader
    */
    static DataLoader getFolderLoader () {
        return getSystemLoaders ()[0];
    }

    /** Getter for shadow loader.
    */
    static DataLoader getShadowLoader () {
        return getSystemLoaders ()[1];
    }

    /** Creates map that maps names of loaders to loaders in the pool.
    * @return map (String, DataLoader)
    */
    private HashMap getMap () {
        HashMap m = map;
        if (m == null) {
            m = new HashMap ();
            Enumeration en = allLoaders ();
            while (en.hasMoreElements ()) {
                Object o = en.nextElement ();
                // adds name of class and the loader
                m.put (o.getClass ().getName (), o);
            }
            map = m;
        }
        return m;
    }
    
    /** Gets assigned loader.
    * @param name - name of the loader
    * @return data loader
    */
    private DataLoader getAssignedDataLoader(final String name) {
        Map map = getMap();
        DataLoader loader = (DataLoader)map.get (name);
        if (loader == null) { // patch reflecting change of packages
            String newName = org.openide.util.Utilities.translate(name);
            loader = (DataLoader)map.get(newName);
        }
        return loader;
    }

    //
    // Default loaders
    //

    /* Loader for folders */
    /** Public only for serialization. */
    public static class FolderLoader extends DataLoader {
        /** Default set of actions on the DataFolder. */
        private static SystemAction[] defaultFolderActions = new SystemAction[] {
                    SystemAction.get (org.openide.actions.OpenLocalExplorerAction.class),
                    SystemAction.get (org.openide.actions.FindAction.class),
                    SystemAction.get (org.openide.actions.FileSystemAction.class),
                    null,
                    SystemAction.get (org.openide.actions.CompileAction.class),
                    SystemAction.get (org.openide.actions.CompileAllAction.class),
                    null,
                    SystemAction.get (org.openide.actions.BuildAction.class),
                    SystemAction.get (org.openide.actions.BuildAllAction.class),
                    null,
                    SystemAction.get (org.openide.actions.CutAction.class),
                    SystemAction.get (org.openide.actions.CopyAction.class),
                    SystemAction.get (org.openide.actions.PasteAction.class),
                    null,
                    SystemAction.get (org.openide.actions.DeleteAction.class),
                    SystemAction.get (org.openide.actions.RenameAction.class),
                    null,
                    // JST: there should be a template Package (DataFolder) instead of this action
                    SystemAction.get (org.openide.actions.NewAction.class),
                    SystemAction.get (org.openide.actions.NewTemplateAction.class),
                    null,
                    SystemAction.get (org.openide.actions.ToolsAction.class),
                    SystemAction.get (org.openide.actions.PropertiesAction.class)
                };

        static final long serialVersionUID =-8325525104047820255L;
        /* Representation class is DataFolder */
        public FolderLoader () {
            super (DataFolder.class);
        }

        protected void initialize () {
            setDisplayName (NbBundle.getBundle (DataLoaderPool.class).getString ("LBL_folder_loader_display_name"));
            setActions (defaultFolderActions);
        }

        protected DataObject handleFindDataObject (
            FileObject fo, DataLoader.RecognizedFiles recognized
        ) throws IOException {
            if (fo.isFolder ()) {
                return new DataFolder (fo);
            } else {
                return null;
            }
        }
    }

    /* Instance loader to recognize instances.
    */
    /** Public only for serialization. */
    public static class InstanceLoader extends UniFileLoader {
        static final long serialVersionUID =-3462727693843631328L;
        /* Creates new InstanceLoader */
        public InstanceLoader() {
            super(InstanceDataObject.class);
        }

        protected void initialize () {
            setDisplayName (NbBundle.getBundle (DataLoaderPool.class).getString ("LBL_instance_loader_display_name"));

            ExtensionList ext = new ExtensionList();
            ext.addExtension ("instance"); // NOI18N
            setExtensions(ext);

            setActions(new SystemAction[] {
                           SystemAction.get (org.openide.actions.CustomizeBeanAction.class),
                           null,
                           SystemAction.get (org.openide.actions.FileSystemAction.class),
                           null,
                           SystemAction.get(org.openide.actions.CutAction.class),
                           SystemAction.get(org.openide.actions.CopyAction.class),
                           SystemAction.get(org.openide.actions.PasteAction.class),
                           null,
                           SystemAction.get(org.openide.actions.DeleteAction.class),
                           //      SystemAction.get(RenameAction.class),
                           null,
                           SystemAction.get (org.openide.actions.ToolsAction.class),
                           SystemAction.get(org.openide.actions.PropertiesAction.class)
                       });
        }

        /* Creates the right data object for given primary file.
        * It is guaranteed that the provided file is realy primary file
        * returned from the method findPrimaryFile.
        *
        * @param primaryFile the primary file
        * @return the data object for this file
        * @exception DataObjectExistsException if the primary file already has data object
        */
        protected MultiDataObject createMultiObject (FileObject primaryFile)
        throws DataObjectExistsException, java.io.IOException {
            return new InstanceDataObject(primaryFile, this);
        }
    }


    /* Loader for file objsects not recognized by any other loader */
    /** Public only for serialization. */
    public static class DefaultLoader extends DataLoader {
        /** Default set of actions on the DefaultDataObject. */
        private static SystemAction[] defaultDataActions = new SystemAction[] {
                    SystemAction.get (org.openide.actions.FileSystemAction.class),
                    null,
                    SystemAction.get (org.openide.actions.CutAction.class),
                    SystemAction.get (org.openide.actions.CopyAction.class),
                    SystemAction.get (org.openide.actions.PasteAction.class),
                    null,
                    SystemAction.get (org.openide.actions.DeleteAction.class),
                    SystemAction.get (org.openide.actions.RenameAction.class),
                    null,
                    SystemAction.get (org.openide.actions.ToolsAction.class),
                    SystemAction.get (org.openide.actions.PropertiesAction.class)
                };

        static final long serialVersionUID =-6761887227412396555L;
        /* Representation class is DataFolder */
        public DefaultLoader () {
            super (DefaultDataObject.class);
        }

        protected void initialize () {
            setDisplayName (NbBundle.getBundle (DataLoaderPool.class).getString ("LBL_default_loader_display_name"));
            setActions (defaultDataActions);
        }

        protected DataObject handleFindDataObject (
            FileObject fo, DataLoader.RecognizedFiles recognized
        ) throws IOException {
            return new DefaultDataObject (fo);
        }
    }

    /* Loader for shadows */
    /** Public only for serialization. */
    public static class ShadowLoader extends DataLoader {
        static final long serialVersionUID =-11013405787959120L;

        /* Representation class is DataShadow */
        public ShadowLoader () {
            super (DataShadow.class);
        }

        protected void initialize () {
            setDisplayName (NbBundle.getBundle (DataLoaderPool.class).getString ("LBL_shadow_loader_display_name"));
        }

        protected DataObject handleFindDataObject (
            FileObject fo, DataLoader.RecognizedFiles recognized
        ) throws IOException {
            if (fo.hasExt (DataShadow.SHADOW_EXTENSION)) {
                return DataShadow.deserialize (fo);
            }
            return null;
        }
    }


}

/*
 * Log
 *  42   Gandalf   1.41        1/15/00  Jaroslav Tulach SUID
 *  41   Gandalf   1.40        1/13/00  Jesse Glick     All data loaders now 
 *       public for serialization.
 *  40   Gandalf   1.39        1/12/00  Ian Formanek    NOI18N
 *  39   Gandalf   1.38        1/5/00   Jaroslav Tulach Find action for 
 *       DataFolders
 *  38   Gandalf   1.37        11/26/99 Patrik Knakal   
 *  37   Gandalf   1.36        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  36   Gandalf   1.35        9/30/99  Jaroslav Tulach DataLoader is now 
 *       serializable.
 *  35   Gandalf   1.34        7/30/99  Jaroslav Tulach 
 *  34   Gandalf   1.33        7/29/99  Jaroslav Tulach Removed GroupShadow.
 *  33   Gandalf   1.32        7/12/99  Jesse Glick     Wrong index for 
 *       getDefaultFileLoader.
 *  32   Gandalf   1.31        6/10/99  Jaroslav Tulach Fires info 
 *       asynchronously.
 *  31   Gandalf   1.30        6/8/99   Ian Formanek    Minor changes
 *  30   Gandalf   1.29        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  29   Gandalf   1.28        5/24/99  Jaroslav Tulach XML data object in Open 
 *       API
 *  28   Gandalf   1.27        5/20/99  Jesse Glick     [JavaDoc]
 *  27   Gandalf   1.26        5/13/99  Jaroslav Tulach Services changed to 
 *       tools.
 *  26   Gandalf   1.25        5/13/99  Jaroslav Tulach Services.
 *  25   Gandalf   1.24        5/11/99  Jaroslav Tulach 
 *  24   Gandalf   1.23        5/4/99   Martin Ryzl     Group Shadow added
 *  23   Gandalf   1.22        5/3/99   Jesse Glick     [JavaDoc] and deleted 
 *       commented-out firstProducerOf(DataObject) as its possible utility 
 *       expired.
 *  22   Gandalf   1.21        5/2/99   Ian Formanek    Added support for 
 *       recognition of files not recognized by any other loader
 *  21   Gandalf   1.20        4/27/99  Petr Hrebejk    producersOf( Class ) fix
 *  20   Gandalf   1.19        4/26/99  Jesse Glick     Bugfix -- operation 
 *       listeners did not work at all.
 *  19   Gandalf   1.18        3/31/99  Jaroslav Tulach Added 
 *       operationPostCreate to OperationListener
 *  18   Gandalf   1.17        3/30/99  Jaroslav Tulach New Package
 *  17   Gandalf   1.16        3/29/99  Martin Ryzl     
 *  16   Gandalf   1.15        3/26/99  Martin Ryzl     rolled back
 *  15   Gandalf   1.14        3/26/99  Martin Ryzl     firstProducerOf 
 *       parameter type corrected
 *  14   Gandalf   1.13        3/24/99  Ian Formanek    Removed obsoleted code
 *  13   Gandalf   1.12        3/22/99  Jaroslav Tulach FileSystemAction again.
 *  12   Gandalf   1.11        3/18/99  Jesse Glick     [JavaDoc]
 *  11   Gandalf   1.10        3/9/99   Jesse Glick     [JavaDoc]
 *  10   Gandalf   1.9         3/2/99   Jaroslav Tulach 
 *  9    Gandalf   1.8         2/19/99  David Simonek   menu related changes...
 *  8    Gandalf   1.7         2/8/99   Petr Hamernik   OpenLocalExplorer 
 *       renamed to OpenLocalExplorerAction
 *  7    Gandalf   1.6         2/5/99   Jaroslav Tulach Changed new from 
 *       template action
 *  6    Gandalf   1.5         2/1/99   Jesse Glick     [JavaDoc]
 *  5    Gandalf   1.4         1/21/99  Ales Novak      
 *  4    Gandalf   1.3         1/20/99  Jaroslav Tulach 
 *  3    Gandalf   1.2         1/15/99  Jaroslav Tulach 
 *  2    Gandalf   1.1         1/6/99   Ales Novak      
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
