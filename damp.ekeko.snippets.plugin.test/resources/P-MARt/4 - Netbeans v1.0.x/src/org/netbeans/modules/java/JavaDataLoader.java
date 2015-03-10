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

package org.netbeans.modules.java;

import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Date;
import java.util.Map;
import java.util.ArrayList;
import java.text.DateFormat;

import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.util.MapFormat;

import org.netbeans.modules.java.settings.JavaSettings;

/** Data loader which recognizes Java source files.
*
* @author Petr Hamernik
*/
public class JavaDataLoader extends MultiFileLoader {
    /** The standard extension for Java source files. */
    public static final String JAVA_EXTENSION = "java"; // NOI18N

    /** The standard extension for Java class files. */
    public static final String CLASS_EXTENSION = "class"; // NOI18N

    /** The list of parsing listener - the instance is in this class, because JavaDataLoader
    * class is prevented to be garbage collected.
    */
    static ArrayList parsingListeners = new ArrayList();

    static final long serialVersionUID =-6286836352608877232L;
    /** Create the loader.
    * Should <em>not</em> be used by subclasses.
    */
    public JavaDataLoader() {
        this(JavaDataObject.class);
    }

    /** Create the loader from a subclass.
    * @param recognizedObject the class of data object
    *        recognized by the loader
    */
    public JavaDataLoader(Class recognizedObject) {
        super(recognizedObject);
    }

    protected void initialize () {
        setDisplayName(Util.getString("PROP_JavaLoader_Name"));

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
                       SystemAction.get (ToolsAction.class),
                       SystemAction.get (PropertiesAction.class)
                   });
    }

    /** Create the <code>JavaDataObject</code>.
    * Subclasses should rather create their own data object type.
    *
    * @param primaryFile the primary file
    * @return the data object for this file
    * @exception DataObjectExistsException if the primary file already has a data object
    */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, java.io.IOException {
        return new JavaDataObject(primaryFile, this);
    }

    /** For a given file find the primary file.
    * Subclasses should override this, but still look for the {@link #JAVA_EXTENSION},
    * as the Java source file should typically remain the primary file for the data object.
    * @param fo the file to find the primary file for
    *
    * @return the primary file for this file or <code>null</code> if this file is not
    *   recognized by this loader
    */
    protected FileObject findPrimaryFile (FileObject fo) {
        String ext = fo.getExt();
        if (ext.equals(JAVA_EXTENSION))
            return fo;

        if (ext.equals(CLASS_EXTENSION))
            return Util.findFile(fo, JAVA_EXTENSION);

        return null;
    }

    /** Create the primary file entry.
    * Subclasses may override {@link JavaDataLoader.JavaFileEntry} and return a new instance
    * of the overridden entry type.
    *
    * @param primaryFile primary file recognized by this loader
    * @return primary entry for that file
    */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        primaryFile.setImportant(true);
        return new JavaFileEntry(obj, primaryFile);
    }

    /** Create a secondary file entry.
    * By default, {@link FileEntry.Numb} is used for the class files; subclasses wishing to have useful
    * secondary files should override this for those files, typically to {@link FileEntry}.
    *
    * @param secondaryFile secondary file to create entry for
    * @return the entry
    */
    protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
        secondaryFile.setImportant(false);
        return new FileEntry.Numb(obj, secondaryFile);
    }

    /** Create the map of replaceable strings which is used
    * in the <code>JavaFileEntry</code>. This method may be extended in subclasses
    * to provide the appropriate map for other loaders.
    * This implementation gets the map from the Java system option;
    * subclasses may add other key/value pairs which may be created without knowledge of the
    * file itself.
    *
    * @return the map of string which are replaced during instantiation
    *        from template
    */
    protected Map createStringsMap() {
        Map map = ((JavaSettings) JavaSettings.findObject(JavaSettings.class, true)).getReplaceableStringsProps();
        map.put("DATE", DateFormat.getDateInstance(DateFormat.LONG).format(new Date())); // NOI18N
        map.put("TIME", DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date())); // NOI18N
        return map;
    }

    /** This entry defines the format for replacing text during
    * instantiation the data object.
    * Used to substitute keys in the source file.
    */
    public class JavaFileEntry extends FileEntry.Format {

        static final long serialVersionUID =8244159045498569616L;
        /** Creates new entry. */
        public JavaFileEntry(MultiDataObject obj, FileObject file) {
            super(obj, file);
        }

        /** Provide suitable format for substitution of lines.
        * Should not typically be overridden.
        * @param target the target folder of the installation
        * @param n the name the file will have
        * @param e the extension the file will have
        * @return format to use for formating lines
        */
        protected java.text.Format createFormat (FileObject target, String n, String e) {
            Map map = createStringsMap();

            modifyMap(map, target, n, e);

            JMapFormat format = new JMapFormat(map);
            format.setLeftBrace("__"); // NOI18N
            format.setRightBrace("__"); // NOI18N
            format.setCondDelimiter("$"); // NOI18N
            format.setExactMatch(false);
            return format;
        }

        /** Modify the replacement map.
        * May be extended in subclasses to provide additional key/value
        * pairs sensitive to the details of instantiation.
        * @param map the map to add to
        * @param target the destination folder for instantiation
        * @param n the new file name
        * @param e the new file extension
        */
        protected void modifyMap(Map map, FileObject target, String n, String e) {
            map.put("NAME", n); // NOI18N
            // Yes, this is package sans filename (target is a folder).
            map.put("PACKAGE", target.getPackageName('.')); // NOI18N
            map.put("PACKAGE_SLASHES", target.getPackageName('/')); // NOI18N
            map.put("QUOTES","\""); // NOI18N
        }
    }
}

/*
 * Log
 *  37   Gandalf   1.36        1/16/00  Jesse Glick     Must not call 
 *       SharedClassObject state methods in constructor--clobbers state of 
 *       subclasses.
 *  36   Gandalf   1.35        1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  35   Gandalf   1.34        11/27/99 Patrik Knakal   
 *  34   Gandalf   1.33        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  33   Gandalf   1.32        9/14/99  Petr Kuzel      QUOTAS added  to map
 *  32   Gandalf   1.31        9/14/99  Petr Kuzel      QUOTES added to map 
 *       (i.e. "\"")
 *  31   Gandalf   1.30        9/12/99  Jesse Glick     DOUBLE_QUOTE taken back 
 *       out again.
 *  30   Gandalf   1.29        9/12/99  Jesse Glick     PACKAGE_SLASHES and 
 *       DOUBLE_QUOTE in file entry format.
 *  29   Gandalf   1.28        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  28   Gandalf   1.27        7/23/99  Petr Hamernik   global parsing listener
 *  27   Gandalf   1.26        7/8/99   Michal Fadljevic FileSystemAction added
 *  26   Gandalf   1.25        7/1/99   Martin Ryzl     conditional MapFormat
 *  25   Gandalf   1.24        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  24   Gandalf   1.23        6/2/99   Jesse Glick     Added ToolsAction to 
 *       popup.
 *  23   Gandalf   1.22        5/10/99  Petr Hamernik   setImportant() used for 
 *       recognized files
 *  22   Gandalf   1.21        5/6/99   Jesse Glick     [JavaDoc]
 *  21   Gandalf   1.20        5/5/99   Martin Ryzl     JavaFileEntry 
 *       constructor is now public
 *  20   Gandalf   1.19        4/23/99  Petr Hamernik   JavaFileEntry improved
 *  19   Gandalf   1.18        4/23/99  Petr Hamernik   new constructor added
 *  18   Gandalf   1.17        4/21/99  Michal Fadljevic 
 *  17   Gandalf   1.16        4/2/99   Petr Hamernik   
 *  16   Gandalf   1.15        4/2/99   Petr Hamernik   
 *  15   Gandalf   1.14        4/1/99   Petr Hamernik   
 *  14   Gandalf   1.13        4/1/99   Petr Hamernik   
 *  13   Gandalf   1.12        3/29/99  Petr Hamernik   
 *  12   Gandalf   1.11        3/28/99  Ales Novak      
 *  11   Gandalf   1.10        3/14/99  Petr Hamernik   
 *  10   Gandalf   1.9         3/12/99  Petr Hamernik   
 *  9    Gandalf   1.8         1/26/99  Petr Hamernik   
 *  8    Gandalf   1.7         1/15/99  Petr Hamernik   
 *  7    Gandalf   1.6         1/15/99  Petr Hamernik   
 *  6    Gandalf   1.5         1/14/99  Petr Hamernik   
 *  5    Gandalf   1.4         1/14/99  Petr Hamernik   
 *  4    Gandalf   1.3         1/13/99  Petr Hamernik   
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting change in 
 *       datasystem package
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach Multi window is here (again)
 *  0    Tuborg    0.12        --/--/98 Petr Hamernik   small changes
 *  0    Tuborg    0.14        --/--/98 Jan Formanek    locale change
 *  0    Tuborg    0.15        --/--/98 Petr Hamernik   Multi window is away (again)
 */
