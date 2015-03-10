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

package org.netbeans.core;

import java.beans.PropertyVetoException;
import java.io.*;
import org.openide.filesystems.*;
import org.openide.loaders.ExtensionList;

/** Enhanced version of local file system that also takes care about
* backuping of saving files.
*
* @author  Jaroslav Tulach
*/
public final class ExLocalFileSystem extends LocalFileSystem {
    /** serial UID */
    static final long serialVersionUID = -6117993653210115798L;

    /** extension for backuped files */
    private static final String BACKUP_EXT = "~"; // NOI18N

    /** for which extensions create backup? */
    private ExtensionList backupExtensions;

    /** relative name to netbeans.user */
    private String relativeName;

    /** Backups the file if necessary */
    public OutputStream outputStream (String name) throws IOException {
        ExtensionList b = backupExtensions;
        if (b != null && b.isRegistered (name)) {
            InputStream is = super.inputStream(name);
            try {
                OutputStream os = super.outputStream (name + BACKUP_EXT);
                try {
                    FileUtil.copy (is, os);
                } finally {
                    os.close ();
                }
            } finally {
                is.close ();
            }
        }
        return super.outputStream (name);
    }

    /** Filters the backuped files.
    */
    public String[] children (String name) {
        String[] arr = super.children (name);

        if (arr == null) {
            return null;
        }

        int j = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].endsWith (BACKUP_EXT)) {
                arr[i] = null;
            }
        }

        return arr;
    }

    //
    // properties
    //

    /** Setter disabling/enabling backup.
    */
    public void setBackupExtensions (ExtensionList l) {
        backupExtensions = l;
        firePropertyChange ("backupExtensions", null, null); // NOI18N
    }

    /** @return true if backup of files is enabled.
    */
    public ExtensionList getBackupExtensions () {
        if (backupExtensions == null) {
            return new ExtensionList ();
        }
        return backupExtensions;
    }


    private boolean relativeFlag = false;

    /** Relative file name to netbeans.user
    *
    * @param relativeName the relative file name
    */
    public void setRelativeDirectory (String relativeName)
    throws java.beans.PropertyVetoException, IOException {

        String oldRelative = this.relativeName;
        try {
            this.relativeName = relativeName;
            relativeFlag = true;

            String file =
                System.getProperty ("netbeans.user") +
                File.separatorChar +
                relativeName;
            File f = new File (file);

            try {
                f = f.getCanonicalFile();
            } catch (Exception ex) {
                // not important
            }

            setRootDirectory(f);
        } catch (PropertyVetoException ex) {
            this.relativeName = oldRelative;
            throw ex;
        } catch (IOException ex) {
            this.relativeName = oldRelative;
            throw ex;
        } finally {
            relativeFlag = false;
        }
    }

    /** Getter for relative name to netbeans.user or null
    * @return the name or null if not set
    */
    public String getRelativeDirectory () {
        return relativeName;
    }

    /** Compute the system name of this file system for a given root directory.
    * <P>
    * The default implementation simply returns the filename separated by slashes.
    * @see FileSystem#setSystemName
    * @param rootFile root directory for the filesystem
    * @return system name for the filesystem
    */
    protected String computeSystemName (File rootFile) {
        if (relativeName != null) {
            return "{netbeans.user}" + '/' + relativeName; // NOI18N
        } else return super.computeSystemName(rootFile);
    }

    /** Set the root directory of the file system.
    * @param r file to set root to
    * @exception PropertyVetoException if the value if vetoed by someone else (usually
    *    by the {@link org.openide.filesystems.Repository Repository})
    * @exception IOException if the root does not exists or some other error occured
    */
    public synchronized void setRootDirectory (File r) throws PropertyVetoException, IOException {
        String oldRelative = relativeName;
        if (!relativeFlag) relativeName = null;
        try {
            super.setRootDirectory(r);
        } catch (PropertyVetoException ex) {
            relativeName = oldRelative;
            throw ex;
        } catch (IOException ex) {
            relativeName = oldRelative;
            throw ex;
        }
    }

    /** Read object refreshes the relative name.
    */
    private void readObject (ObjectInputStream ois)
    throws IOException, ClassNotFoundException {
        ois.defaultReadObject ();
        if (relativeName != null) {
            try {
                setRelativeDirectory (relativeName);
            } catch (java.beans.PropertyVetoException ex) {
            }
        }
    }
}

/*
* Log
*  6    Gandalf   1.5         1/18/00  Martin Ryzl     relative directory 
*       improvements
*  5    Gandalf   1.4         1/18/00  Jaroslav Tulach relative directory 
*       property.
*  4    Gandalf   1.3         1/13/00  Jaroslav Tulach I18N
*  3    Gandalf   1.2         12/1/99  Jaroslav Tulach Fixed NullPointer at line
*       60
*  2    Gandalf   1.1         11/29/99 Jaroslav Tulach Could this work before?
*  1    Gandalf   1.0         11/25/99 Jaroslav Tulach 
* $ 
*/ 
