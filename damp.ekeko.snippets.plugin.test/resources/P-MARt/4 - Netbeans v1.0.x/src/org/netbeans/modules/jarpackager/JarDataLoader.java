/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jarpackager;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.util.ResourceBundle;

import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.FileEntry;
import org.openide.loaders.DataObject;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.execution.NbClassPath;

import org.netbeans.modules.jarpackager.actions.*;
import org.netbeans.modules.jarpackager.options.JarPackagerOption;

/** Data loader which recognizes jar archives.
*
* @author Dafe Simonek, Petr Hamernik, Jaroslav Tulach
*/
public class JarDataLoader extends MultiFileLoader {

    /** serialVersionUID */
    private static final long serialVersionUID = 7727866549739434470L;

    /** The standard extension for JAR archive files. */
    public static final String JAR_EXTENSION = "jar"; // NOI18N
    /** Defaul value for jar content extension (which is primary file) */
    public static final String CONTENT_EXTENSION = "jarContent"; // NOI18N

    /** extension of primary file */
    private String extension;
    /** extensions for archive files */
    private String archiveExt;

    /** Creates new JarDataLoader */
    public JarDataLoader () {
        this(JarDataObject.class);
    }

    /** Constructs jar data loader with given representation class */
    public JarDataLoader (Class repClass) {
        super(repClass);
    }

    /** Does initialization. Initializes display name,
    * extension list and the actions. */
    protected void initialize () {
        setDisplayName(NbBundle.getBundle(JarDataLoader.class).
                       getString("PROP_JarLoader_Name"));
        setActions(new SystemAction[] {
                       SystemAction.get(ManageJarAction.class),
                       SystemAction.get(UpdateJarAction.class),
                       SystemAction.get(DeployJarAction.class),
                       SystemAction.get(FileSystemAction.class),
                       null,
                       SystemAction.get(MountJarAction.class),
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

    /** Creates new JarDataObject for this FileObject.
    * @param fo FileObject
    * @return new JarDataObject
    */
    protected MultiDataObject createMultiObject(final FileObject fo)
    throws IOException {
        return new JarDataObject(fo, this);
    }

    /** For a given file find the primary file.
    * Primary file is jar content file.
    */
    protected FileObject findPrimaryFile (FileObject fo) {
        String ext = fo.getExt();
        String mainExt = getExtension();
        if (mainExt.equals(ext))
            return fo;
        if (getArchiveExt().equals(ext))
            return FileUtil.findBrother(fo, mainExt);
        return null;
    }

    /** Create the primary file entry.
    */
    protected MultiDataObject.Entry createPrimaryEntry (
        MultiDataObject obj, FileObject primaryFile) {
        primaryFile.setImportant(true);
        return new JarContentEntry(obj, primaryFile);
    }

    /** Create a secondary file entry.
    */
    protected MultiDataObject.Entry createSecondaryEntry (
        MultiDataObject obj, FileObject secondaryFile) {
        return new FileEntry(obj, secondaryFile);
    }

    /** Getter for extension of primary file.
    */
    public String getExtension () {
        if (extension == null) {
            extension = CONTENT_EXTENSION;
        }
        return extension;
    }

    /** Setter for extension of primary file.
    */
    public void setExtension (String extension) {
        this.extension = extension;
    }

    /** Getter for extension of primary file.
    */
    public String getArchiveExt () {
        if (archiveExt == null) {
            archiveExt = JAR_EXTENSION;
        }
        return archiveExt;
    }

    /** Setter for extension of primary file.
    */
    public void setArchiveExt (String archiveExt) {
        this.archiveExt = archiveExt;
    }

    /** An entry implementation specialized for jar content.
    * It modifies jar content apropriatelly when copying, moving
    * or creating from template. */
    private static final class JarContentEntry extends FileEntry {

        public JarContentEntry (MultiDataObject obj, FileObject fo) {
            super(obj, fo);
        }

        // PENDING - better to modify directly instead of calling super first

        public FileObject copy (FileObject f, String suffix) throws IOException {
            FileObject result = super.copy(f, suffix);
            syncTargetFileField(result);
            return result;
        }

        public FileObject move (FileObject f, String suffix) throws IOException {
            FileObject result = super.move(f, suffix);
            syncTargetFileField(result);
            return result;
        }

        public FileObject createFromTemplate (FileObject f, String name) throws IOException {
            FileObject result = super.createFromTemplate(f, name);
            syncTargetFileField(result);
            return result;
        }

        /** Modifies given file object which represents jar content.
        * Updates information of target file in jar content to be in
        * sync with current directory */
        void syncTargetFileField (FileObject source) throws IOException {
            // read jar content
            JarContent jc = new JarContent();
            ObjectInputStream ois = new ObjectInputStream(source.getInputStream());
            try {
                jc.readContent(ois);
            } catch (ClassNotFoundException exc) {
                // turn into IOException
                throw new IOException();
            } finally {
                ois.close();
            }
            // modify target file field
            String archiveExt =
                ((JarDataLoader)getDataObject().getLoader()).getArchiveExt();
            String path = NbClassPath.toFile(source).getPath();
            int separatorIndex = path.lastIndexOf('.');
            if (separatorIndex < 0)
                separatorIndex = path.length();
            jc.setTargetFile(new File(
              path.substring(0, separatorIndex) + "." + archiveExt
            ));
            // save back
            FileLock lock = source.lock();
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(source.getOutputStream(lock));
                jc.writeContent(oos);
            } finally {
                if (oos != null) {
                    oos.close();
                }
                lock.releaseLock();
            }
            // System.out.println("Jar content modified..."); // NOI18N
        }

    } // end of JarContentEntry inner class

}

/*
 * <<Log>>
 *  19   Gandalf   1.18        1/25/00  David Simonek   Various bugfixes and 
 *       i18n
 *  18   Gandalf   1.17        1/16/00  David Simonek   i18n
 *  17   Gandalf   1.16        11/9/99  David Simonek   bugfixes and new mount 
 *       jar action
 *  16   Gandalf   1.15        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  15   Gandalf   1.14        10/14/99 David Simonek   manifest updating 
 *       bugfixes
 *  14   Gandalf   1.13        10/13/99 David Simonek   jar content now primary 
 *       file, other small changes
 *  13   Gandalf   1.12        10/4/99  David Simonek   
 *  12   Gandalf   1.11        10/1/99  Martin Balin    Change done by Jarda
 *  11   Gandalf   1.10        9/16/99  David Simonek   a lot of bugfixes (RE 
 *       filters, empty jar content etc)  added templates
 *  10   Gandalf   1.9         9/8/99   David Simonek   new version of jar 
 *       packager
 *  9    Gandalf   1.8         8/31/99  Ian Formanek    Correctly provides 
 *       FileSystemAction on JAR data objects
 *  8    Gandalf   1.7         8/17/99  David Simonek   installations of 
 *       actions, icon changing
 *  7    Gandalf   1.6         6/9/99   Ian Formanek    ToolsAction
 *  6    Gandalf   1.5         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         6/8/99   David Simonek   
 *  4    Gandalf   1.3         6/8/99   David Simonek   bugfixes....
 *  3    Gandalf   1.2         6/4/99   David Simonek   executor properties set 
 *       added
 *  2    Gandalf   1.1         6/4/99   David Simonek   
 *  1    Gandalf   1.0         6/4/99   Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.36        --/--/98 Jan Formanek    reflecting locales move to org.netbeans.modules.locales
 *  0    Tuborg    0.39        --/--/98 Jaroslav Tulach recognizes property files
 */
