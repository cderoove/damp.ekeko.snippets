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

package org.netbeans.modules.makefile;

import java.io.*;
import java.util.HashMap;
import java.util.Date;
import java.text.DateFormat;

import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.*;

/** Data loader which recognizes Makefile files.
*
* @author Libor Kramolis, Jesse Glick
*/
public class MakefileDataLoader extends UniFileLoader {

    /** generated
     */
    private static final long serialVersionUID = -4166235693662417078L;

    /** Creates new MakefileDataLoader */
    public MakefileDataLoader() {
        super (MakefileDataObject.class);
    }

    /** Does initialization. Initializes display name,
    * extension list and the actions. */
    protected void initialize () {
        super.initialize ();
        setDisplayName (NbBundle.getBundle (MakefileDataLoader.class).
                        getString("PROP_MakefileLoader_Name"));
        ExtensionList exts = new ExtensionList ();
        exts.addExtension ("mak");
        exts.addExtension ("MAK");
        setExtensions (exts);
        setSpecialNames (new String[] { "Makefile", "makefile", "GNUmakefile" });
        setActions(new SystemAction[] {
                       SystemAction.get(OpenAction.class),
                       SystemAction.get(FileSystemAction.class),
                       null,
                       SystemAction.get(CompileAction.class),
                       SystemAction.get(BuildAction.class),
                       null,
                       SystemAction.get (ExecuteAction.class),
                       null,
                       SystemAction.get(CutAction.class),
                       SystemAction.get(CopyAction.class),
                       SystemAction.get(PasteAction.class),
                       null,
                       SystemAction.get(DeleteAction.class),
                       SystemAction.get(RenameAction.class),
                       null,
                       SystemAction.get(SaveAsTemplateAction.class),
                       null,
                       SystemAction.get(ToolsAction.class),
                       SystemAction.get(PropertiesAction.class),
                   });
    }


    /** Create a special file entry for the makefile.
     * For token substitution.
     * @param obj the data object
     * @param primaryFile the only file
     * @return a special file entry
     */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        return new MakefileFileEntry (obj, primaryFile);
    }


    /** Get the primary file.
     * @param fo the file to find the primary file for
     *
     * @return the primary file, or <code>null</code> if its extension is not {@link #getExtensions recognized}
     */
    protected FileObject findPrimaryFile (FileObject fo) {
        FileObject supe = super.findPrimaryFile (fo);
        if (supe != null) return supe;
        String name = fo.getName ();
        String ext = fo.getExt ();
        if (ext != null && ! ext.equals ("")) name += '.' + ext;
        String[] names = getSpecialNames ();
        for (int i = 0; i < names.length; i++)
            if (name.equals (names[i]))
                return fo;
        return null;
    }


    /** Creates the correct kind of object.
     * @return the data object
     * @param fo the file
     * @throws IOException if something fails
     */
    protected MultiDataObject createMultiObject (final FileObject fo)
    throws IOException {
        return new MakefileDataObject (fo, this);
    }

    /** Bean getter.
     * @return the special makefile names
     */
    public String[] getSpecialNames () {
        return (String[]) getProperty ("specialNames");
    }

    /** Bean setter.
     * @param nue the special makefile names
     */
    public void setSpecialNames (String[] nue) {
        putProperty ("specialNames", nue, true);
    }

    /** Store loader state.
     * @param oo the output stream
     * @throws IOException stream errors
     */
    public void writeExternal (ObjectOutput oo) throws IOException {
        super.writeExternal (oo);
        oo.writeObject (getSpecialNames ());
    }

    /** Read loader state.
     * @param oi the input stream
     * @throws IOException stream errors
     * @throws ClassNotFoundException definition errors
     */
    public void readExternal (ObjectInput oi) throws IOException, ClassNotFoundException {
        super.readExternal (oi);
        setSpecialNames ((String[]) oi.readObject ());
    }

    /** This entry defines the format for replacing the text during
     * instantiation the data object.
     */
    static class MakefileFileEntry extends FileEntry.Format {

        /** generated
         */
        private static final long serialVersionUID = 8161687315945799905L;

        /** Creates new file entry.
         * This will handle substitution of tokens.
         * @param obj the data object
         * @param file the makefile
         */
        MakefileFileEntry (MultiDataObject obj, FileObject file) {
            super (obj, file);
        }

        /** Method to provide suitable format for substitution of lines.
         *
         * @param target the target folder of the installation
         * @param n the name the file will have
         * @param e the extension the file will have
         * @return format to use for formating lines
         */
        protected java.text.Format createFormat (FileObject target, String n, String e) {
            HashMap map = new HashMap();
            // [PENDING] this is not really terribly useful unless it matches Java conventions
            map.put ("NAME", n);
            map.put ("DATE", DateFormat.getDateInstance (DateFormat.LONG).format (new Date()));
            map.put ("TIME", DateFormat.getTimeInstance (DateFormat.SHORT).format (new Date()));
            map.put ("USER", System.getProperty ("user.name"));

            MapFormat format = new MapFormat (map);
            format.setLeftBrace ("__");
            format.setRightBrace ("__");
            format.setExactMatch (false);
            return format;
        }
    }
}
