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

import java.beans.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;

import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.util.SharedClassObject;
import org.openide.util.actions.SystemAction;

/** A data loader recognizes {@link FileObject}s and creates appropriate
* {@link DataObject}s to represent them.
* The created data object must be a subclass
* of the <EM>representation class</EM> provided in the constructor.
* <P>
* Subclasses of <code>DataLoader</code> should be made <EM>JavaBeans</EM> with
* additional parameters, so a user may configure the loaders in the loader pool.
*
* @author Jaroslav Tulach
*/
public abstract class DataLoader extends SharedClassObject {
    /** property name of display name */
    public static final String PROP_DISPLAY_NAME = "displayName"; // NOI18N
    /** property name of list of actions */
    public static final String PROP_ACTIONS = "actions"; // NOI18N
    /** representation class, not public property */
    private static final Object PROP_REPRESENTATION_CLASS = new Object ();

    /** Create a new data loader.
     * Pass its representation class
    * as a parameter to the constructor. The constructor is then allowed
    * to return only subclasses of the representation class as the result of
    * {@link #findDataObject}.
    *
    * @param representationClass the superclass for all objects returned from
    *    {@link #findDataObject}. The class may be anything but
    *    should be chosen to be as close as possible to the actual class of objects returned from the loader,
    *    to best identify the loader's data objects to listeners.
    */
    protected DataLoader (Class representationClass) {
        putProperty (PROP_REPRESENTATION_CLASS, representationClass);
    }

    /** Get the representation class for this data loader.
     * @return the class
    */
    public final Class getRepresentationClass () {
        return (Class)getProperty (PROP_REPRESENTATION_CLASS);
    }

    /** Get actions.
     * These actions are used to compose
    * a popup menu for the data object. Also these actions should
    * be customizable by the user, so he can modify the popup menu on a
    * data object.
    *
    * @return array of system actions or <CODE>null</CODE> if this loader does not have any
    *   actions
    */
    public final SystemAction[] getActions () {
        return (SystemAction[])getProperty (PROP_ACTIONS);
    }

    /** Set actions.
    * <p>Note that this method is public, not protected, so it is possible for anyone
    * to modify the loader's popup actions externally (after finding the loader
    * using {@link DataLoaderPool#firstProducerOf}).
    * While this is possible, anyone doing so must take care to place new actions
    * into sensible positions, including consideration of separators.
    * This may also adversely affect the intended feel of the data objects.
    * A preferable solution is generally to use {@link org.openide.actions.ToolsAction service actions}.
    * @param actions actions for this loader or <CODE>null</CODE> if it should not have any
    * @see #getActions
    */
    public final void setActions (SystemAction[] actions) {
        putProperty (PROP_ACTIONS, actions, true);
    }

    /** Get the current display name of this loader.
    * @return display name
    */
    public final String getDisplayName () {
        return (String)getProperty (PROP_DISPLAY_NAME);
    }

    /** Set the display name for this loader. Only subclasses should set the name.
    * @param displayName new name
    */
    protected final void setDisplayName (final String displayName) {
        putProperty (PROP_DISPLAY_NAME, displayName, true);
    }

    /** Find a data object appropriate to the given file object--the meat of this class.
     * <p>
    * For example: for files with the same basename but extensions <EM>.java</EM> and <EM>.class</EM>, the handler
    * should return the same <code>DataObject</code>.
    * <P>
    * The loader can add all files it has recognized into the <CODE>recognized</CODE>
    * buffer. Then all these files will be excluded from further processing.
    *
    * @param fo file object to recognize
    * @param recognized recognized file buffer
    * @exception DataObjectExistsException if the data object for the
    *    primary file already exists
    * @exception IOException if the object is recognized but cannot be created
    * @exception InvalidClassException if the class is not instance of
    *    {@link #getRepresentationClass}
    *
    * @return suitable data object or <CODE>null</CODE> if the handler cannot
    *   recognize this object (or its group)
    * @see #handleFindDataObject
    */
    public final DataObject findDataObject (
        FileObject fo, RecognizedFiles recognized
    ) throws IOException {
        DataObject obj = handleFindDataObject (fo, recognized);
        if (obj != null && !getRepresentationClass ().isInstance (obj)) {
            // does not fullfil representation class
            throw new java.io.InvalidClassException (obj.getClass ().toString ());
        }
        return obj;
    }

    /** Find a data object appropriate to the given file object (as implemented in subclasses).
     * @see #findDataObject
    * @param fo file object to recognize
    * @param recognized recognized file buffer
    * @exception DataObjectExistsException as in <code>#findDataObject</code>
    * @exception IOException as in <code>#findDataObject</code>
    *
    * @return the data object or <code>null</code>
    */
    protected abstract DataObject handleFindDataObject (
        FileObject fo, RecognizedFiles recognized
    ) throws IOException;

    /** Utility method to mark a file as belonging to this loader.
    * When the file is to be recognized this loader will be used first.
    * <P>
    * This method is used by {@link DataObject#markFiles}.
    *
    * @param fo file to mark
    * @exception IOException if setting the file's attribute failed
    */
    public final void markFile (FileObject fo) throws IOException {
        fo.setAttribute (DataObject.EA_ASSIGNED_LOADER, getClass ().getName ());
    }

    /** Writes nothing to the stream.
    * @param oo ignored
    */
    public void writeExternal (ObjectOutput oo) throws IOException {
        oo.writeObject (getActions ());
        oo.writeUTF (getDisplayName ());
    }

    /** Reads nothing from the stream.
    * @param oi ignored
    */
    public void readExternal (ObjectInput oi)
    throws IOException, ClassNotFoundException {
        setActions ((SystemAction[])oi.readObject ());
        setDisplayName (oi.readUTF ());
    }

    /** Get a registered loader from the pool.
     * @param loaderClass exact class of the loader (<em>not</em> its data object representation class)
     * @return the loader instance, or <code>null</code> if there is no such loader registered
     * @see DataLoaderPool#allLoaders
     */
    public static DataLoader getLoader (Class loaderClass) {
        return (DataLoader)findObject (loaderClass, true);
    }

    // XXX huh? --jglick
    // The parameter can be <CODE>null</CODE> to
    // simplify testing whether the file object fo is valid or not
    /** Buffer holding a list of primary and secondary files marked as already recognized, to prevent further scanning.
    */
    public interface RecognizedFiles {
        /** Mark this file as being recognized. It will be excluded
        * from further processing.
        *
        * @param fo file object to exclude
        */
        public void markRecognized (FileObject fo);
    }

}


/*
 * Log
 *  11   Gandalf   1.10        1/12/00  Ian Formanek    NOI18N
 *  10   Gandalf   1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         9/30/99  Jaroslav Tulach DataLoader is now 
 *       serializable.
 *  8    Gandalf   1.7         9/28/99  Jaroslav Tulach Changes in loader pool 
 *       are reflected in repository.
 *  7    Gandalf   1.6         6/10/99  Jesse Glick     [JavaDoc]
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         4/1/99   Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         4/1/99   Martin Ryzl     getLoader() added
 *  3    Gandalf   1.2         3/9/99   Jesse Glick     [JavaDoc]
 *  2    Gandalf   1.1         2/4/99   Petr Hamernik   setting of extended file
 *       attributes doesn't require FileLock
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    1.10        --/--/98 Jaroslav Tulach Added listener for operations on data object,
 */
