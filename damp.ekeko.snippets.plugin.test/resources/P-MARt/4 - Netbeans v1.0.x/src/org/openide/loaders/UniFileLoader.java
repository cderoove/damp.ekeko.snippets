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

import org.openide.*;
import org.openide.filesystems.*;

/** Support class for loader handling one file at a time.
 * This is used for many file types, e.g. HTML, images, etc.
 * File extensions recognized by the loader may be set.
*
* @author Petr Hamernik, Jaroslav Tulach
*/
public abstract class UniFileLoader extends MultiFileLoader {
    /** name of property with extensions */
    public static final String PROP_EXTENSIONS = "extensions"; // NOI18N

    /** Constructor.
    * @param representationClass class that is produced by this loader
    */
    protected UniFileLoader(Class representationClass) {
        super (representationClass);
    }

    /** Get the primary file.
    * @param fo the file to find the primary file for
    *
    * @return the primary file, or <code>null</code> if its extension is not {@link #getExtensions recognized}
    */
    protected FileObject findPrimaryFile (FileObject fo) {
        return getExtensions().isRegistered(fo) ? fo : null;
    }

    /* Creates the right data object for given primary file.
    * It is guaranteed that the provided file is realy primary file
    * returned from the method findPrimaryFile.
    *
    * @param primaryFile the primary file
    * @return the data object for this file
    * @exception DataObjectExistsException if the primary file already has data object
    */
    protected abstract MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, java.io.IOException;

    /* Creates the right primary entry for given primary file.
    *
    * @param obj requesting object
    * @param primaryFile primary file recognized by this loader
    * @return primary entry for that file
    */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        return new FileEntry (obj, primaryFile);
    }

    /** Do not create a seconday entry.
    *
    * @param obj ignored
    * @param secondaryFile ignored
    * @return never returns
    * @exception UnsupportedOperationException because this loader supports only a primary file object
    */
    protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
        throw new UnsupportedOperationException ();
    }

    /** Does nothing because this loader works only with objects
    * with one file => primary file so it is not necessary to search
    * for anything else.
    * 
    * @param obj the object to test
    */
    void checkFiles (MultiDataObject obj) {
    }

    /** Set the extension list for this data loader.
    * @param ext new list of extensions
    */
    public void setExtensions(ExtensionList ext) {
        putProperty (PROP_EXTENSIONS, ext, true);
    }

    /** Get the extension list for this data loader.
    * @return list of extensions
    */
    public ExtensionList getExtensions() {
        ExtensionList l = (ExtensionList)getProperty (PROP_EXTENSIONS);
        if (l == null) {
            l = new ExtensionList ();
            putProperty (PROP_EXTENSIONS, l, false);
        }
        return l;
    }

    /** Writes extensions to the stream.
    * @param oo ignored
    */
    public void writeExternal (java.io.ObjectOutput oo) throws IOException {
        super.writeExternal (oo);

        oo.writeObject (getProperty (PROP_EXTENSIONS));
    }

    /** Reads nothing from the stream.
    * @param oi ignored
    */
    public void readExternal (java.io.ObjectInput oi)
    throws IOException, ClassNotFoundException {
        super.readExternal (oi);

        setExtensions ((ExtensionList)oi.readObject ());
    }

}


/*
 * Log
 *  12   Gandalf   1.11        1/12/00  Ian Formanek    NOI18N
 *  11   Gandalf   1.10        12/2/99  Jaroslav Tulach DataObject.files () 
 *       should return correct results for all MultiFileObject subclasses that 
 *       collects objects from one folder.
 *  10   Gandalf   1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         10/1/99  Jaroslav Tulach 
 *  8    Gandalf   1.7         9/30/99  Jaroslav Tulach DataLoader is now 
 *       serializable.
 *  7    Gandalf   1.6         9/28/99  Jaroslav Tulach Changes in loader pool 
 *       are reflected in repository.
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         3/15/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         3/14/99  Jaroslav Tulach Change of 
 *       MultiDataObject.Entry.
 *  3    Gandalf   1.2         3/10/99  Jesse Glick     [JavaDoc]
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.14        --/--/98 Jaroslav Tulach MultiTopWindow variable moved to TXTLoader and Image loader
 *  0    Tuborg    0.16        --/--/98 Jaroslav Tulach Extends DataLoader
 */
