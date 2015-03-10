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

import java.util.HashSet;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.Enumeration;
import java.beans.PropertyVetoException;
import java.text.MessageFormat;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

import org.netbeans.modules.jarpackager.JarContent;
import org.netbeans.modules.jarpackager.options.JarPackagerOption;

/** Responsibility of this class is to inspect given
* jar archive, inspect it and produce resulting instance of
* JarContent which describes content of the archive.
* Also allows for listening to the inspecting progress.
*
* @author  Dafe Simonek
*/
public class JarInspector extends Object {

    /** Message format for progress information */
    private static MessageFormat progressInfo;

    /** Constant for meta information directory */
    private static final String META_INF_DIR = "META-INF"; // NOI18N

    /** set of progress listeners 
     * @associates ProgressListener*/
    private HashSet listeners;

    /** jar archive to inspect */
    private File archiveFile;

    /** Creates new JarInspector with given jat file. */
    public JarInspector (File archiveFile) {
        this.archiveFile = archiveFile;
    }

    /** Incpects asociated archive and creates new jar content
    * describing it.
    * @return newly created jarcontent instance which describes
    * asociated jar archive.
    */
    public JarContent createContent ()
    throws IOException, PropertyVetoException {
        // obtain count of files in the archive
        JarFile jarFile = new JarFile(archiveFile);
        long itemCount = jarFile.size();
        // obtain the manifest from the archive
        JarContent result = new JarContent();
        initializeFromOptions(result);
        fireProgressEvent(0, "MSG_ObtainingManifest"); // NOI18N
        result.setManifest(jarFile.getManifest());
        jarFile.close();
        // make garbage collectable
        jarFile = null;
        // target file
        result.setTargetFile(archiveFile);
        // don't filter anything
        result.setFilter(JarContent.ALL);
        // add to repository (if it's not here already)
        fireProgressEvent(0, "MSG_AddingToRepository"); // NOI18N
        FileSystem jarFs = JarUtils.addJarFSToRepository(archiveFile, false);
        // go through the archive entries and build jar content
        FileObject curFo = null;
        long counter = 0;
        for (Enumeration enum = jarFs.getRoot().getChildren(false);
                enum.hasMoreElements(); counter++) {
            curFo = (FileObject)enum.nextElement();
            try {
                // add all but meta inf dir
                if (!META_INF_DIR.equals(curFo.getName())) {
                    result.putFile(DataObject.find(curFo).getPrimaryFile());
                }
            } catch (DataObjectNotFoundException exc) {
                // we can silently ignore the exception, because
                // it means that file object is not recognized by any loader
                // and as such will not be visible in repository
                if (System.getProperty("netbeans.debug.exceptions") != null) {
                    exc.printStackTrace();
                }
            }
            // notify progress listeners
            fireProgressEvent(
                (int)(counter * 100 / itemCount),
                progressInfo().format( new Object[] { curFo.getName() } )
            );
        }
        return result;
    }

    /* Adds new listener which will be notified about creating progress.
    * @param pl new listener
    */
    public synchronized void addProgressListener (ProgressListener pl) {
        if (listeners == null)
            listeners = new HashSet();
        listeners.add(pl);
    }

    /* Removes specified listener from the listener list.
    * @param pl listener to remove
    */
    public synchronized void removeProgressListener (ProgressListener pl) {
        if (listeners == null)
            return;
        listeners.remove(pl);
    }

    /** Fires notification about creating progress.
    * @param pe progress event to fire off
    */
    protected void fireProgressEvent (int percent, String description) {
        if (listeners == null)
            return;
        HashSet cloned;
        // clone listener list
        synchronized (this) {
            cloned = (HashSet)listeners.clone();
        }
        // fire on cloned list to prevent from modifications when firing
        for (Iterator iter = cloned.iterator(); iter.hasNext(); ) {
            ((ProgressListener)iter.next()).progress(percent, description);
        }
    }

    /** Utility method, initializes given jar content with respect
    * to the current jar packager options values.
    */
    private static void initializeFromOptions (JarContent jc) {
        JarPackagerOption rootOption = JarPackagerOption.singleton();
        jc.setCompressed(rootOption.isCompressed());
        jc.setCompressionLevel(rootOption.getCompressionLevel());
        jc.setManifestFileList(rootOption.isManifestFileList());
        jc.setMainAttributes(rootOption.isMainAttributes());
    }

    /** Getter for progress info message */
    private static MessageFormat progressInfo () {
        // message format for progress message
        if (progressInfo == null) {
            progressInfo = new MessageFormat(
                               NbBundle.getBundle(JarInspector.class).getString("FMT_ProgressInfo")
                           );
        }
        return progressInfo;
    }

}

/*
* <<Log>>
*  4    Gandalf   1.3         1/16/00  David Simonek   i18n
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         10/5/99  David Simonek   various fixes, only 
*       primary entries now resides in 'chosen content'
*  1    Gandalf   1.0         10/4/99  David Simonek   
* $ 
*/ 
