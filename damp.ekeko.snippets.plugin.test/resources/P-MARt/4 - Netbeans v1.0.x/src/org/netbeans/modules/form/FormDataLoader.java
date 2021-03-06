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

package org.netbeans.modules.form;

import org.openide.*;
import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.java.JavaDataLoader;

/** Loader for Forms. Recognizes file with extension .form and .java and with extension class if
* there is their source and form file.
*
* @author Ian Formanek
*/
public class FormDataLoader extends JavaDataLoader {
    /* The standard extensions of the recognized files */
    public static final String FORM_EXTENSION = "form"; // NOI18N

    static final long serialVersionUID =7259146057404524013L;
    /** Constructs a new FormDataLoader */
    public FormDataLoader () {
        super (FormDataObject.class);
        setDisplayName(NbBundle.getBundle(FormDataLoader.class).getString("PROP_FormLoader_Name"));

        setActions(new SystemAction[] {
                       SystemAction.get (OpenAction.class),
                       SystemAction.get (CustomizeBeanAction.class),
                       SystemAction.get (FileSystemAction.class),
                       null,
                       SystemAction.get (CompileAction.class),
                       null,
                       SystemAction.get (ExecuteAction.class),
                       null,
                       SystemAction.get (CutAction.class),
                       SystemAction.get (CopyAction.class),
                       SystemAction.get (PasteAction.class),
                       null,
                       SystemAction.get (DeleteAction.class),
                       SystemAction.get (RenameAction.class),
                       null,
                       SystemAction.get (SaveAsTemplateAction.class),
                       null,
                       SystemAction.get(ToolsAction.class),
                       SystemAction.get (PropertiesAction.class),
                   });

    }

    /** finds file with the same name and specified extension in the same folder as param javaFile */
    static public FileObject findFile(FileObject javaFile, String ext) {
        if (javaFile == null) return null;
        String name = javaFile.getName ();
        int indx = name.indexOf ('$');
        if (indx > 0) {
            name = name.substring (0, indx);
        }
        if (javaFile.getParent() == null) return null; // handle critical situation
        return javaFile.getParent().getFileObject (name, ext);

    }

    /** For a given file finds a primary file.
    * @param fo the file to find primary file for
    *
    * @return the primary file for the file or null if the file is not
    *   recognized by this loader
    */
    protected FileObject findPrimaryFile (FileObject fo) {
        String ext = fo.getExt();
        if (ext.equals(JAVA_EXTENSION))
            return (findFile (fo, FORM_EXTENSION) == null) ? null : fo;

        if (ext.equals (FORM_EXTENSION)) {
            return findFile(fo, JAVA_EXTENSION);
        }

        if (ext.equals(CLASS_EXTENSION)) {
            if (findFile (fo, FORM_EXTENSION) != null)
                return findFile(fo, JAVA_EXTENSION);
            else
                return null;
        }

        return null;
    }

    /** Creates the right data object for given primary file.
    * It is guaranteed that the provided file is realy primary file
    * returned from the method findPrimaryFile.
    *
    * @param primaryFile the primary file
    * @return the data object for this file
    * @exception DataObjectExistsException if the primary file already has data object
    */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, java.io.IOException {
        //    Thread.dumpStack ();
        return new FormDataObject(findFile (primaryFile, FORM_EXTENSION), primaryFile, this);
    }

    /** Creates the right primary entry for given primary file.
    *
    * @param primaryFile primary file recognized by this loader
    * @return primary entry for that file
    * /
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
      return new JavaLoader.JavaEntry(obj, primaryFile);
}

    /** Creates right secondary entry for given file. The file is said to
    * belong to an object created by this loader.
    *
    * @param secondaryFile secondary file for which we want to create entry
    * @return the entry
    */
    protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
        //    Thread.dumpStack ();
        String ext = secondaryFile.getExt();
        if (ext.equals(CLASS_EXTENSION)) {
            secondaryFile.setImportant(false);
            return new FileEntry.Numb (obj, secondaryFile);             // entries for .class file
        } else if (ext.equals (FORM_EXTENSION)) {
            secondaryFile.setImportant(true);
            FileEntry formEntry = new FileEntry (obj, secondaryFile);   // entries for .form files
            ((FormDataObject)obj).formEntry = formEntry;
            return formEntry;
        } else return null;
    }
}

/*
 * Log
 *  21   Gandalf   1.20        1/5/00   Ian Formanek    NOI18N
 *  20   Gandalf   1.19        11/27/99 Patrik Knakal   
 *  19   Gandalf   1.18        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  18   Gandalf   1.17        8/31/99  Ian Formanek    Correctly provides 
 *       FileSystemAction on its data objects
 *  17   Gandalf   1.16        8/2/99   Ian Formanek    Fixes potential 
 *       deserialization problem
 *  16   Gandalf   1.15        7/20/99  Ian Formanek    
 *  15   Gandalf   1.14        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  14   Gandalf   1.13        6/2/99   Ian Formanek    ToolsAction, Reorder
 *  13   Gandalf   1.12        5/14/99  Ian Formanek    extends JavaLoader to 
 *       provide substitutions during createFromTemplate
 *  12   Gandalf   1.11        3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  11   Gandalf   1.10        3/24/99  Ian Formanek    
 *  10   Gandalf   1.9         3/24/99  Ian Formanek    Fixed stealing of plain 
 *       Java objects
 *  9    Gandalf   1.8         3/17/99  Ian Formanek    
 *  8    Gandalf   1.7         3/16/99  Ian Formanek    
 *  7    Gandalf   1.6         3/16/99  Ian Formanek    
 *  6    Gandalf   1.5         3/14/99  Jaroslav Tulach Change of 
 *       MultiDataObject.Entry.
 *  5    Gandalf   1.4         3/11/99  Ian Formanek    fixed order of form and 
 *       java file
 *  4    Gandalf   1.3         3/10/99  Ian Formanek    Gandalf updated
 *  3    Gandalf   1.2         1/25/99  Ian Formanek    First switch to Gandalf
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting change in 
 *       datasystem package
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
