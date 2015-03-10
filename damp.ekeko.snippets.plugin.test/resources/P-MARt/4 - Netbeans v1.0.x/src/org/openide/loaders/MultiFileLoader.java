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
import java.util.*;

import org.openide.*;
import org.openide.filesystems.*;

/** Loader for any kind of <code>MultiDataObject</code>. It provides
* support for recognition of a composite data object and registering
* entries into it.
*
* @author Jaroslav Tulach
*/
public abstract class MultiFileLoader extends DataLoader {
    static final long serialVersionUID=1521919955690157343L;


    /** Creates new multi file loader.
     * @param representationClass the representation class
    */
    protected MultiFileLoader (Class representationClass) {
        super (representationClass);
    }

    /*  Provides standard implementation for recognizing files in the
    * loader. First of all the findEntry method is called to allow the
    * subclass to find right entry for the
    *
    * @param fo file object to recognize
    * @param recognized recognized files buffer.
    * @exception DataObjectExistsException if the data object for specific
    *    primary file already exists (thrown by constructor of DataObject)
    * @exception IOException if the object is recognized but cannot be created
    *
    * @return suitable data object or <CODE>null</CODE> if the handler cannot
    *   recognize this object (or its group)
    */
    protected final DataObject handleFindDataObject (
        FileObject fo, RecognizedFiles recognized ) throws IOException {
        // finds primary file for given file
        FileObject primary = findPrimaryFile (fo);

        // if this loader does not recognizes this file => return
        if (primary == null) return null;

        MultiDataObject obj;
        try {
            // create the multi object
            obj = createMultiObject (primary);
        } catch (DataObjectExistsException ex) {
            // object already exists
            DataObject dataObject = ex.getDataObject ();
            if (!(dataObject instanceof MultiDataObject)) {
                // but if it is not MultiDataObject, propadate the exception
                throw ex;
            }
            obj = (MultiDataObject)dataObject;
        }

        if (obj.getLoader () != this) {
            // this primary file is recognized by a different
            // loader. We should not add entries to it
            return null;
        }

        // mark all secondary entries used
        obj.markSecondaryEntriesRecognized (recognized);

        // if the file is not between
        obj.registerEntry (fo);

        return obj;
    }


    /** For a given file finds the primary file.
    * @param fo the (secondary) file
    *
    * @return the primary file for the file or <code>null</code> if the file is not
    *   recognized by this loader
    */
    protected abstract FileObject findPrimaryFile (FileObject fo);

    /** Creates the right data object for a given primary file.
    * It is guaranteed that the provided file will actually be the primary file
    * returned by {@link #findPrimaryFile}.
    *
    * @param primaryFile the primary file
    * @return the data object for this file
    * @exception DataObjectExistsException if the primary file already has a data object
    */
    protected abstract MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException;

    /** Creates the right primary entry for a given primary file.
    *
    * @param obj requesting object
    * @param primaryFile primary file recognized by this loader
    * @return primary entry for that file
    */
    protected abstract MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile);

    /** Creates a new secondary entry for a given file.
    * Note that separate entries must be created for every secondary
    * file within a given multi-file data object.
    *
    * @param obj requesting object
    * @param secondaryFile a secondary file
    * @return the entry
    */
    protected abstract MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile);

    /** Called before list of files belonging to a data object
    * is returned from MultiDataObject.files () method. This allows 
    * each loader to perform additional tests and update the set of
    * entries for given data object.
    * <P>
    * Current implementation scans all files in directory.
    * 
    * @param obj the object to test
    */
    void checkFiles (MultiDataObject obj) {
        /* JST: Make protected (and rename) when necessary. Do not forget to
        * change UniFileDataLoader too.
        */


        FileObject primary = obj.getPrimaryFile ();
        FileObject parent = primary.getParent ();

        FileObject[] arr = parent.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            FileObject pf = findPrimaryFile (arr[i]);

            if (pf == primary) {
                // this object could belong to this loader
                try {
                    // this will go thru regular process of looking for
                    // data object and register this file with the right (but not
                    // necessary this one) data object
                    DataObject newObj = DataObject.find (arr[i]);
                } catch (DataObjectNotFoundException ex) {
                    // ignore
                }
            }
        }
    }


}

/*
* Log
*  14   Gandalf   1.13        1/18/00  Ales Novak      checkFiles - 'for' loop 
*       indexes fixed
*  13   Gandalf   1.12        12/2/99  Jaroslav Tulach SerialVersionUID
*  12   Gandalf   1.11        12/2/99  Jaroslav Tulach DataObject.files () 
*       should return correct results for all MultiFileObject subclasses that 
*       collects objects from one folder.
*  11   Gandalf   1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  10   Gandalf   1.9         9/30/99  Jaroslav Tulach DataLoader is now 
*       serializable.
*  9    Gandalf   1.8         7/21/99  Jaroslav Tulach MultiDataObject can mark 
*       easily mark secondary entries in constructor as belonging to the object.
*  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  7    Gandalf   1.6         5/7/99   Michal Fadljevic obj.markSecondaryEntriesRecognized()
*        moved after catch() 
*  6    Gandalf   1.5         3/16/99  Jesse Glick     [JavaDoc]
*  5    Gandalf   1.4         3/14/99  Jaroslav Tulach Change of 
*       MultiDataObject.Entry.
*  4    Gandalf   1.3         3/10/99  Jesse Glick     [JavaDoc]
*  3    Gandalf   1.2         1/15/99  Petr Hamernik   bugfix
*  2    Gandalf   1.1         1/6/99   Ian Formanek    
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
