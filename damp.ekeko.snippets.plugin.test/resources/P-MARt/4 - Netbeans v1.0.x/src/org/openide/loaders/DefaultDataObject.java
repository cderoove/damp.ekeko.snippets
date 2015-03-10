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

import java.io.*;

import org.openide.filesystems.*;
import org.openide.util.HelpCtx;
import org.openide.nodes.Node;

/** An implementation of a data object which consumes file objects not recognized by any other loaders.
*
* @author Ian Formanek
*/
class DefaultDataObject extends DataObject {
    static final long serialVersionUID =-4936309935667095746L;
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 6305590675982925167L;

    /** Constructs new data shadow for given primary file and referenced original.
    * @param fo the primary file
    * @param original original data object
    */
    DefaultDataObject (FileObject fo) throws DataObjectExistsException {
        super (fo, DataLoaderPool.getDefaultFileLoader ());
    }

    /* Creates node delegate.
    */
    protected Node createNodeDelegate () {
        return new DefaultDataNode (this);
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
        return new HelpCtx (DefaultDataObject.class);
    }

    /* Handles copy of the data object.
    * @param f target folder
    * @return the new data object
    * @exception IOException if an error occures
    */
    protected DataObject handleCopy (DataFolder f) throws IOException {
        String name = FileUtil.findFreeFileName (f.getPrimaryFile (), getName (), getPrimaryFile ().getExt ());
        return new DefaultDataObject (
                   FileUtil.copyFile (getPrimaryFile (), f.getPrimaryFile (), name)
               );
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
            getPrimaryFile ().rename (lock, name, getPrimaryFile ().getExt ());
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
        String name = FileUtil.findFreeFileName (f.getPrimaryFile (), getName (), getPrimaryFile ().getExt ());
        return FileUtil.moveFile (getPrimaryFile (), f.getPrimaryFile (), name);
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

        name = FileUtil.findFreeFileName (f.getPrimaryFile (), name, getPrimaryFile ().getExt ());
        DataObject result = new DefaultDataObject (
                                FileUtil.copyFile (getPrimaryFile (), f.getPrimaryFile (), name)
                            );
        result.setTemplate (false);
        return result;
    }

    /** Node for a default data object. */
    protected static class DefaultDataNode extends DataNode {
        /** Create a default data node.
         * @param ddo the DefaultDataObject for which the node is to be created
         */
        public DefaultDataNode (DefaultDataObject ddo) {
            super (ddo, org.openide.nodes.Children.LEAF);
        }

        /** Get the display name for the node.
         * A filesystem may {@link org.openide.filesystems.FileSystem#getStatus specially alter} this.
         * @return the desired name
        */
        public String getDisplayName () {
            String s = getDataObject ().getName ();
            String ext = getDataObject ().getPrimaryFile ().getExt ();
            if ((ext != null) && (!"".equals (ext))) { // NOI18N
                s = s + "." + ext; // NOI18N
            }

            try {
                s = getDataObject ().getPrimaryFile ().getFileSystem ().getStatus ().annotateName (s, getDataObject ().files ());
            } catch (FileStateInvalidException e) {
                // no fs, do nothing
            }

            return s;
        }

    }
}

/*
 * Log
 *  11   Gandalf   1.10        1/15/00  Jaroslav Tulach annotation applied only 
 *       once.
 *  10   Gandalf   1.9         1/12/00  Ian Formanek    NOI18N
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/7/99  Jesse Glick     Create-from-template 
 *       needs to clear template status on result.
 *  7    Gandalf   1.6         9/3/99   Jaroslav Tulach #3649
 *  6    Gandalf   1.5         8/18/99  Ian Formanek    Generated serial version
 *       UID
 *  5    Gandalf   1.4         7/21/99  Ian Formanek    Fixed display name with 
 *       empty extension
 *  4    Gandalf   1.3         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/3/99   Ian Formanek    Class made 
 *       package-private
 *  1    Gandalf   1.0         5/2/99   Ian Formanek    
 * $
 */
