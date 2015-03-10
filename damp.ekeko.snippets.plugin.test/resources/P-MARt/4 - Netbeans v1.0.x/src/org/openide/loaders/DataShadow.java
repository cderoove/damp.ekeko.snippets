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

import java.beans.PropertyEditor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.text.MessageFormat;
import java.lang.reflect.*;
import java.awt.datatransfer.Transferable;

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.nodes.FilterNode;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;

/** Default implementation of a shortcut to another data object.
*
* @author Jan Jancura, Jaroslav Tulach
*/
public class DataShadow extends DataObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 6305590675982925167L;

    /** original data object */
    private DataObject original;

    /** Extension name. */
    static final String SHADOW_EXTENSION = "shadow"; // NOI18N

    /** Constructs new data shadow for given primary file and referenced original.
    * Method to allow subclasses of data shadow.
    *
    * @param fo the primary file
    * @param original original data object
    * @param loader the loader that created the object
    */
    protected DataShadow (
        FileObject fo, DataObject original, DataLoader loader
    ) throws DataObjectExistsException {
        super (fo, loader);
        this.original = original;
    }

    /** Constructs new data shadow for given primary file and referenced original.
    * @param fo the primary file
    * @param original original data object
    */
    private DataShadow (FileObject fo, DataObject original) throws DataObjectExistsException {
        this (fo, original, DataLoaderPool.getShadowLoader ());
    }

    /** Method that creates new data shadow in a folder. The name chosen is based
    * on the name of the original object.
    *
    * @param folder target folder to create data in
    * @param original orignal object that should be represented by the shadow
    */
    public static DataShadow create (DataFolder folder, DataObject original)
    throws IOException {
        return create (folder, null, original);
    }

    /** Method that creates new data shadow in a folder. All modifications are
    * done atomicly using fileSystem.runAtomic.
    *
    * @param folder target folder to create data in
    * @param name name to give to the shadow
    * @param original orignal object that should be represented by the shadow
    */
    public static DataShadow create (
        DataFolder folder,
        final String name,
        final DataObject original
    ) throws IOException {
        final FileObject fo = folder.getPrimaryFile ();
        final DataShadow[] arr = new DataShadow[1];

        fo.getFileSystem ().runAtomicAction (new FileSystem.AtomicAction () {
                                                 public void run () throws IOException {
                                                     String n;
                                                     if (name == null) {
                                                         n = FileUtil.findFreeFileName (fo, original.getName (), SHADOW_EXTENSION);
                                                     } else {
                                                         n = name;
                                                     }


                                                     FileObject file = writeOriginal (
                                                                           fo.createData (n, SHADOW_EXTENSION), original
                                                                       );

                                                     DataObject obj = DataObject.find (file);
                                                     if (obj instanceof DataShadow) {
                                                         arr[0] = (DataShadow)obj;
                                                     } else {
                                                         // wrong instance => shadow was not found
                                                         throw new DataObjectNotFoundException (obj.getPrimaryFile ());
                                                     }
                                                 }
                                             });

        return arr[0];
    }

    /** Writes the original into given file.
    * @param fo file to write to
    * @param original data object to store into
    * @return the file
    * @exception IOException on I/O error
    */
    private static FileObject writeOriginal (FileObject fo, DataObject obj)
    throws IOException {
        FileLock lock = fo.lock ();
        try {
            Writer os = new OutputStreamWriter (fo.getOutputStream (lock));
            FileObject pf = obj.getPrimaryFile ();
            os.write (pf.getPackageNameExt ('/', '.'));
            os.write ('\n');
            os.write (pf.getFileSystem ().getSystemName ());
            os.write ('\n');
            os.close ();
        } finally {
            lock.releaseLock ();
        }
        return fo;
    }

    /** Loads proper dataShadow from the file fileObject.
    *
    * @param <CODE>FileObject fileObject</CODE> The file to deserialize shadow from.
    * @param <CODE>DataLoader loader</CODE>
    * @return the shadow in this object
    * @exception IOException error during load
    */
    static DataShadow deserialize (FileObject fileObject) throws java.io.IOException {
        BufferedReader ois = new BufferedReader (new InputStreamReader (fileObject.getInputStream ()));

        try {
            String s = ois.readLine ();
            String fs = ois.readLine ();

            if (s == null) {
                // not found
                throw new java.io.FileNotFoundException (fileObject.getPackageNameExt ('/', '.'));
            }

            Repository rep = org.openide.TopManager.getDefault ().getRepository ();
            FileSystem fileSystem;
            if (fs != null) {
                // try to locate the fs
                fileSystem = rep.findFileSystem (fs);
            } else {
                fileSystem = null;
            }

            FileObject fo;

            if (fileSystem != null) {
                // first of all try to locate the shadow by file system
                fo = fileSystem.findResource (s);
            } else {
                fo = null;
            }

            if (fo == null) {
                fo = rep.findResource (s);
            }

            if (fo == null) {
                throw new java.io.FileNotFoundException (s);
            }

            DataObject original = DataObject.find (fo);

            return new DataShadow (fileObject, original);
        } finally {
            ois.close ();
        }
    }


    /** Return the original shadowed object.
    * @return the data object
    */
    public DataObject getOriginal () {
        return original;
    }

    /* Creates node delegate.
    */
    protected Node createNodeDelegate () {
        return new ShadowNode (this);
    }

    /* Getter for delete action.
    * @return true if the object can be deleted
    */
    public boolean isDeleteAllowed () {
        return !getPrimaryFile ().isReadOnly ();
    }

    /* Getter for copy action.
    * @return true if the object can be copied
    */
    public boolean isCopyAllowed ()  {
        return true;
    }

    /* Getter for move action.
    * @return true if the object can be moved
    */
    public boolean isMoveAllowed ()  {
        return !getPrimaryFile ().isReadOnly ();
    }

    /* Getter for rename action.
    * @return true if the object can be renamed
    */
    public boolean isRenameAllowed () {
        return !getPrimaryFile ().isReadOnly ();
    }

    /* Help context for this object.
    * @return help context
    */
    public HelpCtx getHelpCtx () {
        return getOriginal ().getHelpCtx ();
    }

    /* Handles copy of the data object.
    * @param f target folder
    * @return the new data object
    * @exception IOException if an error occures
    */
    protected DataObject handleCopy (DataFolder f) throws IOException {
        return handleCreateFromTemplate (f, getName ());
    }

    /* Deals with deleting of the object. Must be overriden in children.
    * @exception IOException if an error occures
    */
    protected void handleDelete () throws IOException {
        FileLock lock = getPrimaryFile ().lock ();
        try {
            getPrimaryFile ().delete (lock);
        } finally {
            lock.releaseLock ();
        }
    }

    /* Handles renaming of the object.
    * Must be overriden in children.
    *
    * @param name name to rename the object to
    * @return new primary file of the object
    * @exception IOException if an error occures
    */
    protected FileObject handleRename (String name) throws IOException {
        FileLock lock = getPrimaryFile ().lock ();
        try {
            getPrimaryFile ().rename (lock, name, SHADOW_EXTENSION);
        } finally {
            lock.releaseLock ();
        }
        return getPrimaryFile ();
    }

    /* Handles move of the object. Must be overriden in children.
    *
    * @param f target data folder
    * @return new primary file of the object
    * @exception IOException if an error occures
    */
    protected FileObject handleMove (DataFolder f) throws IOException {
        String name = FileUtil.findFreeFileName (f.getPrimaryFile (), getName (), SHADOW_EXTENSION);
        return FileUtil.moveFile (getPrimaryFile (), f.getPrimaryFile (), name);
    }

    /* Creates shadow for this object in specified folder. The current
    * implementation creates reference data shadow and pastes it into
    * specified folder.
    *
    * @param f the folder to create shortcut in
    * @return the shadow
    */
    protected DataShadow handleCreateShadow (DataFolder f) throws IOException {
        return original.handleCreateShadow (f);
    }


    /* Handles creation of new data object from template. This method should
    * copy content of the template to destination folder and assign new name
    * to the new object.
    *
    * @param f data folder to create object in
    * @param name name to give to the new object (or <CODE>null</CODE>
    *    if the name is up to the template
    * @return new data object
    * @exception IOException if an error occured
    */
    protected DataObject handleCreateFromTemplate (
        DataFolder f, String name
    ) throws IOException {
        if (name == null) name = getName ();

        name = FileUtil.findFreeFileName (f.getPrimaryFile (), name, SHADOW_EXTENSION);
        return new DataShadow (
                   FileUtil.copyFile (getPrimaryFile (), f.getPrimaryFile (), name),
                   original
               );
    }

    /* Scans the orginal bundle */
    public Node.Cookie getCookie (Class c) {
        if (c.isInstance (this)) {
            return this;
        }
        return original.getCookie (c);
    }

    /** Node for a shadow object. */
    protected static class ShadowNode extends FilterNode {
        /** message to create name of node */
        private static MessageFormat format;
        /** message to create short description of node */
        private static MessageFormat descriptionFormat;

        /** shadow */
        private DataShadow obj;

        /** the sheet computed for this node or null */
        private Sheet sheet;

        /** Create a shadowing node.
         * @param shadow the shadow
         */
        public ShadowNode (DataShadow shadow) {
            this (shadow, shadow.getOriginal ().getNodeDelegate ());
        }

        /** Initializes it */
        private ShadowNode (DataShadow shadow, Node node) {
            super (node);
            this.obj = shadow;
        }

        /* Clones the node
        */
        public Node cloneNode () {
            ShadowNode sn = new ShadowNode (obj);
            return sn;
        }

        /* Renames the shadow data object.
        * @param name new name for the object
        * @exception IllegalArgumentException if the rename failed
        */
        public void setName (String name) {
            try {
                obj.rename (name);
                fireDisplayNameChange (null, null);
                fireNameChange (null, null);
            } catch (IOException ex) {
                throw new IllegalArgumentException (ex.getMessage ());
            }
        }

        /** The name of the shadow.
        * @return the name
        */
        public String getName () {
            return obj.getName ();
        }

        /* Creates name based on the original one.
        */
        public String getDisplayName () {
            if (format == null) {
                format = new MessageFormat (NbBundle.getBundle (DataShadow.class).getString ("FMT_shadowName"));
            }
            return format.format (createArguments ());
        }

        /** Creates arguments for given shadow node */
        private Object[] createArguments () {

            return new Object[] {
                       obj.getName (), // name of the shadow
                       super.getDisplayName (), // name of original
                       systemNameOrFileName (obj.getPrimaryFile ()), // full name of file for shadow
                       systemNameOrFileName (obj.getOriginal ().getPrimaryFile ()) // full name of original file
                   };
        }

        /** System name of file name
        */
        private static String systemNameOrFileName (FileObject fo) {
            if (fo.isRoot ()) {
                try {
                    return fo.getFileSystem ().getDisplayName ();
                } catch (FileStateInvalidException ex) {
                }
            }
            return fo.getPackageNameExt ('/', '.');
        }

        /* Creates description based on the original one.
        */
        public String getShortDescription () {
            if (descriptionFormat == null) {
                descriptionFormat = new MessageFormat (
                                        NbBundle.getBundle (DataShadow.class).getString ("FMT_shadowHint")
                                    );
            }
            return descriptionFormat.format (createArguments ());
        }

        /* @return obj.isDeleteAllowed () */
        public boolean canDestroy () {
            return obj.isDeleteAllowed ();
        }

        /* Destroyes the node
        */
        public void destroy () throws IOException {
            obj.delete ();
            //      super.destroy ();
        }

        /** @return true if shadow can be renamed
        */
        public final boolean canRename () {
            return obj.isRenameAllowed ();
        }

        /* Returns true if this object allows copying.
        * @returns true if this object allows copying.
        */
        public final boolean canCopy () {
            return obj.isCopyAllowed ();
        }

        /* Returns true if this object allows cutting.
        * @returns true if this object allows cutting.
        */
        public final boolean canCut () {
            return obj.isMoveAllowed ();
        }

        /* First of all the DataObject.getCookie method is
        * called. If it produces non-null result, it is returned.
        * Otherwise the value returned from super.getCookie
        * method is returned.
        *
        * @return the cookie or null
        */
        public Node.Cookie getCookie (Class cl) {
            Node.Cookie c = obj.getCookie (cl);
            if (c != null) {
                return c;
            } else {
                return super.getCookie (cl);
            }
        }

        /** Returns modified properties of the original node.
        * @return property sets 
        */
        public PropertySet[] getPropertySets () {
            Sheet s = sheet;
            if (s == null) {
                s = sheet = cloneSheet ();
            }
            return s.toArray ();
        }

        /** Copy this node to the clipboard.
        *
        * @return {@link ExTransferable.Single} with one flavor, {@link NodeTransfer#nodeCopyFlavor}
        * @throws IOException if it could not copy
        */
        public Transferable clipboardCopy () throws IOException {
            return NodeTransfer.transferable (this, NodeTransfer.CLIPBOARD_COPY);
        }

        /** Cut this node to the clipboard.
        *
        * @return {@link ExTransferable.Single} with one flavor, {@link NodeTransfer#nodeCopyFlavor}
        * @throws IOException if it could not cut
        */
        public Transferable clipboardCut () throws IOException {
            return NodeTransfer.transferable (this, NodeTransfer.CLIPBOARD_CUT);
        }

        /**
        * This implementation only calls clipboardCopy supposing that 
        * copy to clipboard and copy by d'n'd are similar.
        *
        * @return transferable to represent this node during a drag
        * @exception IOException when the
        *    cut cannot be performed
        */
        public Transferable drag () throws IOException {
            return clipboardCopy ();
        }

        /** Creates a node listener that allows listening on the
        * original node and propagating events to the proxy.
        * <p>Intended for overriding by subclasses, as with {@link #createPropertyChangeListener}.
        *
        * @return a {@link NodeAdapter} in the default implementation
        */
        protected org.openide.nodes.NodeListener createNodeListener () {
            return new PropL (this);
        }

        /** Equal if the o is ShadowNode to the same shadow object.
        */
        public boolean equals (Object o) {
            if (o instanceof ShadowNode) {
                ShadowNode sn = (ShadowNode)o;
                return sn.obj == obj;
            }
            return false;
        }

        /** Hashcode is computed by the represented shadow.
        */
        public int hashCode () {
            return obj.hashCode ();
        }


        /** Clones the property sheet of original node.
        */
        private Sheet cloneSheet () {
            PropertySet[] sets = this.getOriginal ().getPropertySets ();

            Sheet s = new Sheet ();
            for (int i = 0; i < sets.length; i++) {
                Sheet.Set ss = new Sheet.Set ();
                ss.put (sets[i].getProperties ());
                ss.setName (sets[i].getName ());
                ss.setDisplayName (sets[i].getDisplayName ());
                ss.setShortDescription (sets[i].getShortDescription ());

                // modifies the set if it contains name of object property
                modifySheetSet (ss);

                s.put (ss);
            }

            return s;
        }

        /** Modifies the sheet set to contain name of property and name of
        * original object.
        */
        private void modifySheetSet (Sheet.Set ss) {
            Property p = ss.remove (DataObject.PROP_NAME);
            if (p != null) {
                p = new PropertySupport.Name (this);
                ss.put (p);

                p = new Name ();
                ss.put (p);
            }
        }

        /** Class that renames the orginal object and also updates
        * the link
        */
        private final class Name extends PropertySupport.ReadWrite {
            public Name () {
                super (
                    "OriginalName", // NOI18N
                    String.class,
                    DataObject.getString ("PROP_ShadowOriginalName"),
                    DataObject.getString ("HINT_ShadowOriginalName")
                );
            }

            public Object getValue () {
                return obj.getOriginal ().getName();
            }

            public void setValue (Object val) throws IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {
                if (!canWrite())
                    throw new IllegalAccessException();
                if (!(val instanceof String))
                    throw new IllegalArgumentException();

                try {
                    DataObject orig = obj.getOriginal ();
                    orig.rename ((String)val);
                    writeOriginal (obj.getPrimaryFile (), orig);
                } catch (IOException ex) {
                    throw new InvocationTargetException (ex);
                }
            }

            public boolean canWrite () {
                return obj.getOriginal ().isRenameAllowed();
            }
        }

        /** Property listener on data object that delegates all changes of
        * properties to this node.
        */
        private static class PropL extends FilterNode.NodeAdapter {
            public PropL (ShadowNode sn) {
                super (sn);
            }

            /* JST PENDING: Does not compile with oldjavac => ignore it, it not too necessary neither not too much
            * data objects are changing property sets, but uncomment when we switch to new javac.
            *

            protected void propertyChange (FilterNode fn, PropertyChangeEvent ev) {
              if (ev.getPropertyName ().equals (Node.PROP_PROPERTY_SETS)) {
                // clear the sheet
                ShadowNode sn = (ShadowNode)fn;
                sn.sheet = null;
              }
              
              super.propertyChange (fn, ev);
              }
            */
        }

    }
}

/*
 * Log
 *  11   Gandalf   1.10        1/16/00  Jaroslav Tulach Compiles with oldjavac 
 *       but does not work properly.
 *  10   Gandalf   1.9         1/15/00  Jaroslav Tulach #5285  
 *  9    Gandalf   1.8         1/15/00  Jaroslav Tulach DataShadow enhancements
 *  8    Gandalf   1.7         1/12/00  Ian Formanek    NOI18N
 *  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         10/5/99  Jaroslav Tulach DataShadow.create
 *  5    Gandalf   1.4         9/3/99   Jaroslav Tulach #3649
 *  4    Gandalf   1.3         9/2/99   Jaroslav Tulach fire(Display)NameChange
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/14/99  Martin Ryzl     BUG #1623
 *  1    Gandalf   1.0         3/26/99  Ian Formanek    
 * $
 */
