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

package org.netbeans.modules.projects;

import java.io.*;


import org.openide.cookies.*;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;

import org.openidex.projects.*;

import org.netbeans.modules.projects.content.*;

/** Object that provides main functionality for project data loader.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
* @author Jaroslav Tulach
*/
public final class ProjectDataObject extends DataFolder {

    static final long serialVersionUID = 3984322918015509710L;

    public static final String FILES_FOLDER = "Files"; // NOI18N

    final PSupport support;

    public ProjectDataObject (FileObject obj, DataLoader loader) throws DataObjectExistsException {
        super (obj, loader);
        support = new PSupport(this);
    }

    /** Creates new project in specified folder under given name.
    * @param f the folder
    * @param n name of the project
    * @return the project data object
    * @exception IOException if it fails
    */
    public static ProjectDataObject createProject(final DataFolder f, final String n)
    throws IOException {
        final FileObject[] primary = new FileObject[1];
        final FileObject pf = f.getPrimaryFile ();

        pf.getFileSystem ().runAtomicAction (new FileSystem.AtomicAction () {
                                                 public void run () throws IOException {
                                                     // find free name
                                                     String name = FileUtil.findFreeFolderName (
                                                                       f.getPrimaryFile (), n
                                                                   );

                                                     FileObject ff = FileUtil.createFolder (pf, name);

                                                     if (ProjectsModule.DEFAULT_PROJECT_NAME.equals (name)) {
                                                         try {
                                                             ff.setAttribute ("SystemFileSystem.localizingBundle", "org.netbeans.modules.projects.Bundle"); // NOI18N
                                                         } catch (IOException ioe) {
                                                             if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                                                                 ioe.printStackTrace ();
                                                         }
                                                     }

                                                     // mark the folder as belonging to project loader
                                                     ProjectDataLoader.INSTANCE.markFile (ff);
                                                     primary[0] = ff;
                                                 }
                                             });

        ProjectDataObject obj = (ProjectDataObject)DataObject.find (primary[0]);

        // saves the current state of the IDE
        // obj.support.projectSave ();

        return obj;
    }

    /** Add DataObject to the project.
    */
    public void add(DataObject dobj) throws IOException {
        dobj.createShadow(getFileFolder());
    }

    /**
    */
    protected DataFolder getFileFolder() throws java.io.IOException {
        FileObject pf = getPrimaryFile();
        FileObject ff = pf.getFileObject(FILES_FOLDER);
        if (ff == null) {
            ff = pf.createFolder(FILES_FOLDER);
            try {
                ff.setAttribute ("SystemFileSystem.localizingBundle", "org.netbeans.modules.projects.ProjectDataObject$FilesFolderBundle"); // NOI18N
            } catch (IOException ioe) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ioe.printStackTrace ();
            }
        }
        return (DataFolder) DataObject.find(ff);
    }
    public static class FilesFolderBundle extends java.util.ResourceBundle {
        private final String FILES_FOLDER_LOC = NbBundle.getBundle (ProjectDataObject.class).getString ("LBL_files_folder");
        public java.util.Enumeration getKeys () {
            return org.openide.util.enum.EmptyEnumeration.EMPTY;
        }
        protected Object handleGetObject (String key) {
            return FILES_FOLDER_LOC;
        }
    }

    /** Provides node that should represent this data object. When a node for representation
    * in a parent is requested by a call to getNode (parent) it is the exact copy of this node
    * with only parent changed. This implementation creates instance
    * <CODE>DataNode</CODE>.
    * <P>
    * This method is called only once.
    *
    * @return the node representation for this data object
    * @see DataNode
    */
    protected Node createNodeDelegate () {
        return new ProjectDataNode(this);
    }

    public boolean isDeleteAllowed () {
        return super.isDeleteAllowed ();
        // && getNodeDelegate () != TopManager.getDefault ().getPlaces ().nodes ().projectDesktop ();
    }

    public boolean isMoveAllowed () {
        return isDeleteAllowed ();
    }

    /** Special handling for compile cookie
    */
    public Node.Cookie getCookie (Class c) {
        if (CompilerCookie.class.isAssignableFrom(c)) {
            return support.getCompileCookie(c);
        }
        if (c.isInstance(support)) return support;
        if (c.isInstance(this)) return this;

        return super.getCookie (c);
    }

    /** Help context for this object.
    * @return help context
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ProjectDataObject.class);
    }

    /** Getter for bundle string.
    */
    static String getLocalizedString (String s) {
        return NbBundle.getBundle (ProjectDataObject.class).getString (s);
    }

    /** Getter for bundle string.
    */
    static String getLocalizedString (String s, Object val) {
        return java.text.MessageFormat.format (getLocalizedString (s), new Object[] { val });
    }

    /** Test whether the object is accessible from folder.
    */
    public boolean isAccessibleFromFolder(DataObject dobj) throws IOException {
        return isAccessibleFromFolder(getFileFolder(), dobj);
    }

    /** Test whether the object is accessible from folder.
    */
    public static boolean isAccessibleFromFolder(DataFolder folder, DataObject dobj) {
        // get all objects under FilesFolder
        // dobj must be either the non-folder object or the folder should be
        // somewhere on path to the root
        DataObject dobjs[] = folder.getChildren();

        for(int i = 0; i < dobjs.length; i++) {
            FileObject fo = getOriginal(dobj).getPrimaryFile(),
                            ff = getOriginal(dobjs[i]).getPrimaryFile();
            while (fo != null) {
                if (fo.equals(ff)) return true;
                fo = fo.getParent();
            }
        }
        return false;
    }

    /** De-shadow the object.
    * @return return the original if the obj is DataShadow, otherwise return obj
    */
    public static DataObject getOriginal(DataObject obj) {
        while (obj instanceof DataShadow) obj = ((DataShadow) obj).getOriginal();
        return obj;
    }
}

/*
 * Log
 *  12   Gandalf   1.11        1/24/00  Martin Ryzl     fixed #5520, build 
 *       project
 *  11   Gandalf   1.10        1/19/00  Jesse Glick     Localized filenames.
 *  10   Gandalf   1.9         1/17/00  Martin Ryzl     
 *  9    Gandalf   1.8         1/16/00  Martin Ryzl     
 *  8    Gandalf   1.7         1/13/00  Martin Ryzl     heavy localization
 *  7    Gandalf   1.6         1/12/00  Martin Ryzl     
 *  6    Gandalf   1.5         1/9/00   Martin Ryzl     
 *  5    Gandalf   1.4         1/8/00   Martin Ryzl     
 *  4    Gandalf   1.3         1/6/00   Martin Ryzl     problems with compiling 
 *       by the old compiler fixed
 *  3    Gandalf   1.2         1/3/00   Martin Ryzl     
 *  2    Gandalf   1.1         12/28/99 Martin Ryzl     
 *  1    Gandalf   1.0         12/22/99 Martin Ryzl     
 * $
 */
