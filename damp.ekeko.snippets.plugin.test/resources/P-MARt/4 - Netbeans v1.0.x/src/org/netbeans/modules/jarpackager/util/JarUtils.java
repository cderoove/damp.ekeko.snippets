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

package org.netbeans.modules.jarpackager.util;

import java.util.*;
import java.util.jar.*;
import java.io.File;
import java.io.IOException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyVetoException;

import org.openide.util.actions.NodeAction;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.FileObject;
import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.execution.NbClassPath;

import org.netbeans.modules.jarpackager.JarContent;
import org.netbeans.modules.jarpackager.*;

/** Static utilities used for common purposes by other classes.
*
* @author Dafe Simonek
*/
public final class JarUtils extends Object {

    static final String MANIFEST_VERSION = "1.0"; // NOI18N

    /** No need to instantiate */
    private JarUtils () {
    }

    /** @return true if specified nodes all can be added to the
    * archive described by given jar content. */
    public static boolean canAdd (JarContent jc, Node[] nodes,
                                  String targetName) {
        if ((nodes.length == 0) || (jc == null)) {
            return false;
        }
        // check compatibility of selected nodes
        Node parent = null;
        for (int i = 0; i < nodes.length; i++) {
            parent = nodes[i].getParentNode();
            if ((parent != null) &&
                    (parent.getCookie(DataFolder.class) == null) &&
                    (nodes[i].getCookie(DataFolder.class) == null)) {
                return false;
            }
        }
        // check if it is not part of itself
        DataObject curDo = null;
        DataFolder curFolder = null;
        File targetFile =
            (targetName == null) ? null : new File(targetName);
        FileSystem curFs = null;
        for (int i = 0; i < nodes.length; i++) {
            curDo = (DataObject)nodes[i].getCookie(DataObject.class);
            if (curDo != null) {
                // check if processed item is not archive itself
                if ((curDo instanceof JarDataObject) &&
                        isEqual(targetFile, NbClassPath.toFile(curDo.getPrimaryFile()))) {
                    return false;
                }
                // check if processed item is not from the filesystem of the
                // archive itself
                try {
                    curFs = curDo.getPrimaryFile().getFileSystem();
                } catch (FileStateInvalidException exc) {
                    // file system in invalid state, so does not allow
                    // to add itema to archive from such file system
                    return false;
                }
                if ((curFs instanceof JarFileSystem) &&
                        isEqual(targetFile, ((JarFileSystem)curFs).getJarFile())) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Helper, returns true if given files are the same
    * (their extension can be different) */
    private static boolean isEqual (File file1, File file2) {
        // deal with nulls
        if (file1 == null) {
            return (file2 == null) ? true : false;
        }
        if (file2 == null) {
            return (file1 == null) ? true : false;
        }
        String name1 = file1.getPath();
        String name2 = file2.getPath();
        // strip the extensions
        int index1 = name1.lastIndexOf('.');
        if (index1 < 0) {
            index1 = name1.length();
        }
        int index2 = name2.lastIndexOf('.');
        if (index2 < 0) {
            index2 = name2.length();
        }
        return new File(name1.substring(0, index1)).equals(
                   new File(name2.substring(0, index2)));
    }

    /** Adds list of file objects representing all primary and
    * secondary entries of data objects which are represented by
    * specified nodes. 
    * @param jc jar content to which we want to add created file list
    */
    public static void addFileList (JarContent jc, Node[] nodes) {
        // transform nodes to collection of file objects
        ArrayList result = new ArrayList(nodes.length * 2);
        DataObject curDo = null;
        for (int i = 0; i < nodes.length; i++) {
            curDo = (DataObject)nodes[i].getCookie(DataObject.class);
            if (curDo != null) {
                // add only primary file object
                result.add(curDo.getPrimaryFile());
            }
        }
        // add to the jar content
        jc.putFiles(result);
    }

    /** Removes list of file objects representing all primary and
    * secondary entries of data objects which are represented by
    * specified nodes. 
    * @param jc jar content from which we are removing files
    */
    public static void removeFileList (JarContent jc, Node[] nodes) {
        // transform nodes to collection of file objects
        DataObject curDo = null;
        for (int i = 0; i < nodes.length; i++) {
            curDo = (DataObject)nodes[i].getCookie(DataObject.class);
            if (curDo != null) {
                // remove primary file object
                jc.removeFile(curDo.getPrimaryFile());
            }
        }
    }

    /** Updates the manifest in given jar content -> it means
    * that it synchronizes the content of the manifest with other
    * properties of jar content. More specifically, it regenerates file
    * list, basic identification, versioning etc., according to the 
    * flags in jar content.
    */
    public static void updateManifest (JarContent jc) {
        boolean fileList = jc.isManifestFileList();
        boolean mainAttr = jc.isMainAttributes();
        //System.out.println("File List: " + fileList); // NOI18N
        //System.out.println("Main Attr: " + mainAttr); // NOI18N
        Manifest manifest = jc.getManifest();
        //System.out.println("Man before update: " + manifest); // NOI18N
        if (manifest == null) {
            manifest = new Manifest();
        }
        //System.out.println("Man before main attrs: " + manifest); // NOI18N
        // update main attributes
        if (mainAttr) {
            completeMainAttributes(manifest.getMainAttributes());
        } else {
            removeMainAttributes(manifest.getMainAttributes());
        }
        // update file list
        //System.out.println("Man before previous files: " + manifest); // NOI18N
        removeOldEntries(jc);
        //System.out.println("Man before file list: " + manifest); // NOI18N
        if (fileList) {
            generateFileList(jc);
        } else {
            removeFileList(jc);
        }
        jc.setManifest(manifest);
        //System.out.println("Man after update: " + manifest); // NOI18N
    }

    /** Completes main attributes of the manifest.
    * Adds version and created-by attributes if they are not present.
    */
    public static void completeMainAttributes (Attributes mainAttr) {
        // add version if missing
        if (mainAttr.getValue(Attributes.Name.MANIFEST_VERSION) == null)
            mainAttr.put(Attributes.Name.MANIFEST_VERSION, MANIFEST_VERSION);
        // add created-by if missing
        Attributes.Name cbAttr = new Attributes.Name("Created-By"); // NOI18N
        if (mainAttr.getValue(cbAttr) == null) {
            mainAttr.put(cbAttr, "Forte for Java v. 1.0 beta"); // NOI18N
        }
    }

    /** Remove main attributes of the manifest.
    */
    public static void removeMainAttributes (Attributes mainAttr) {
        mainAttr.remove(Attributes.Name.MANIFEST_VERSION);
        mainAttr.remove(new Attributes.Name("Created-By")); // NOI18N
    }

    /** Generates current list of files of jar content to the manifest
    * of given jar content. If some file entry already exist, it is
    * left untouched.
    */
    public static void generateFileList (JarContent jc) {
        Manifest manifest = jc.getManifest();
        if (manifest == null) {
            return;
        }
        FileObject curFo = null;
        String curName = null;
        Map manifestEntries = manifest.getEntries();
        // go through the files and add manifest entries
        for (Iterator iter = jc.filteredContent().iterator(); iter.hasNext(); ) {
            curFo = (FileObject)iter.next();
            curName = curFo.getPackageNameExt('/', '.');
            // generate entry if not present already
            if (manifestEntries.get(curName) == null) {
                // create and put attributes for entry
                Attributes entryAttr = new Attributes(1);
                //entryAttr.put(Attributes.Name.MAIN_CLASS, curName);
                manifestEntries.put(curName, entryAttr);
            }
        }
    }

    /** Removes all empty per-file entries of the file list */
    public static void removeFileList (JarContent jc) {
        Manifest manifest = jc.getManifest();
        if (manifest == null) {
            return;
        }
        FileObject curFo = null;
        String curName = null;
        Map manifestEntries = manifest.getEntries();
        // go through the files build array of entry names which are candidates
        // for removal (such entries which name doesn't equal to the name
        // of any file object in list)
        for (Iterator iter = jc.filteredContent().iterator(); iter.hasNext(); ) {
            curFo = (FileObject)iter.next();
            curName = curFo.getPackageNameExt('/', '.');
            Attributes attrs = (Attributes)manifestEntries.get(curName);
            if ((attrs != null) && (attrs.size() == 0)) {
                manifestEntries.remove(curName);
            }
        }
    }

    /** Removes from manifest all empty per-file entries for files
    * which are not present in file list.
    * Other entries will remain untouched.
    */
    private static void removeOldEntries (JarContent jc) {
        Manifest manifest = jc.getManifest();
        if (manifest == null) {
            return;
        }
        FileObject curFo = null;
        String curName = null;
        Map manifestEntries = manifest.getEntries();
        HashSet candidates = new HashSet(manifestEntries.keySet());
        // go through the files build array of entry names which are candidates
        // for removal (such entries which name doesn't equal to the name
        // of any file object in list)
        for (Iterator iter = jc.filteredContent().iterator(); iter.hasNext(); ) {
            curFo = (FileObject)iter.next();
            candidates.remove(curFo.getPackageNameExt('/', '.'));
        }
        // go through candidates and remove entries with no attributes
        for (Iterator iter = candidates.iterator(); iter.hasNext(); ) {
            curName = (String)iter.next();
            Attributes attrs = (Attributes)manifestEntries.get(curName);
            if ((attrs != null) && (attrs.size() == 0)) {
                manifestEntries.remove(curName);
            }
        }
    }

    /** Centers given component relative to packaging view.
    * Size of the component must be already known */
    public static void centerRelativeToPV (Component comp) {
        Rectangle modeBounds =
            TopManager.getDefault().getWindowManager().getCurrentWorkspace().
            findMode(PackagingView.getPackagingView()).getBounds();
        Dimension compSize = comp.getSize();
        comp.setLocation(
            modeBounds.x + (modeBounds.width - compSize.width) / 2,
            modeBounds.y + (modeBounds.height - compSize.height) / 2
        );
    }

    /** Utility method that creates new jar file system with given
    * root file and adds created file system to the repository.
    * @param writeable true if file system should be writeable,
    * false if read-only is enough */
    public static FileSystem addJarFSToRepository (File rootFile, boolean writeable)
    throws IOException, PropertyVetoException {
        Repository repo = TopManager.getDefault().getRepository();
        FileSystem foundFS = getMountedJarFS(rootFile);
        if (foundFS == null) {
            JarFileSystem jarFs = new JarFileSystem();
            jarFs.setJarFile(rootFile);
            repo.addFileSystem(jarFs);
            return jarFs;
        }
        return foundFS;
    }

    /** @return FileSystem which has given jar file as its root or
    * null if no such file system could be found in repository */
    public static FileSystem getMountedJarFS (File rootFile) {
        Repository repo = TopManager.getDefault().getRepository();
        // PENDING -> checking is not robust, should be done better..
        // PENDING - writeable file systems not yet supported
        FileSystem[] allFS = repo.toArray();
        for (int i = 0; i < allFS.length; i++) {
            if ((allFS[i] instanceof JarFileSystem) &&
                    rootFile.equals(((JarFileSystem)allFS[i]).getJarFile())) {
                return allFS[i];
            }
        }
        return null;
    }

    /** Tries to inspect given node for JarDataObejct cookie
    * and asks for its jar content.
    * @return JarContent connected with specified node or null
    * if no such jar content can be found.
    */
    public static JarContent jarContentFromNode (Node node) {
        JarDataObject jdo =
            (JarDataObject)(node.getCookie(JarDataObject.class));
        return (jdo != null) ? jdo.getJarContent() : null;
    }

}

/*
* <<Log>>
*  13   Gandalf   1.12        1/25/00  David Simonek   Various bugfixes and i18n
*  12   Gandalf   1.11        1/16/00  David Simonek   i18n
*  11   Gandalf   1.10        12/7/99  David Simonek   
*  10   Gandalf   1.9         11/11/99 David Simonek   add to jar action failure
*       repaired
*  9    Gandalf   1.8         11/9/99  David Simonek   bugfixes and new mount 
*       jar action
*  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems copyright in file comment
*  7    Gandalf   1.6         10/14/99 David Simonek   manifest updating 
*       bugfixes
*  6    Gandalf   1.5         10/13/99 David Simonek   various bugfixes 
*       concerning primarily manifest
*  5    Gandalf   1.4         10/13/99 David Simonek   jar content now primary 
*       file, other small changes
*  4    Gandalf   1.3         10/10/99 Petr Hamernik   console debug messages 
*       removed.
*  3    Gandalf   1.2         10/5/99  David Simonek   various fixes, only 
*       primary entries now resides in 'chosen content'
*  2    Gandalf   1.1         10/4/99  David Simonek   
*  1    Gandalf   1.0         9/8/99   David Simonek   
* $
*/