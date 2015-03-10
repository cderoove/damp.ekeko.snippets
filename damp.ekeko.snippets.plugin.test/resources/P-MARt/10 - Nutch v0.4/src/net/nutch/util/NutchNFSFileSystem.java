/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.util;

import java.io.*;
import java.util.*;
import java.nio.channels.*;

/****************************************************************
 * NutchNFSFileSystem implements NutchFileSystem over the Network File System.
 * We assume all participants are mounting the same drive.
 *
 * @author Mike Cafarella
 *****************************************************************/
public class NutchNFSFileSystem extends NutchGenericFileSystem {
    TreeMap lockDataSet = new TreeMap(), lockObjSet = new TreeMap();

    /**
     * Create the ShareSet automatically, and then go on to
     * the regular constructor.
     */
    public NutchNFSFileSystem(File dbRoot, boolean destructiveCopy) throws IOException {
        this(dbRoot, null, destructiveCopy);
    }

    /**
     * Create a Nutch Filesystem at the indicated mounted
     * directory.  We're given a ShareSet.
     */
    public NutchNFSFileSystem(File dbRoot, ShareSet initShareSet, boolean destructiveCopy) throws IOException {
        super(dbRoot, initShareSet, destructiveCopy);

        // Make sure the shareGroups are in good working order
        for (Iterator it = shareSet.getShareGroups().values().iterator(); it.hasNext(); ) {
            ShareGroup sg = (ShareGroup) it.next();
            String locations[] = sg.getLocations();

            for (int i = 0; i < locations.length; i++) {
                if (locations[i].indexOf(":") >= 0) {
                    throw new IOException("Cannot process non-local locations");
                }
            }
        }
    }

    /**
     * Obtain a lock with the given info.
     */
    public synchronized void lockFile(String locMach, String locStr, String filename, boolean exclusive) throws IOException {
        // NFSFileSystem ignores the locMach value

        File lockTarget = new File(locStr, filename);
        FileInputStream lockData = new FileInputStream(lockTarget);
        FileLock lockObj = lockData.getChannel().lock(0L, Long.MAX_VALUE, exclusive);
        lockDataSet.put(lockTarget, lockData);
        lockObjSet.put(lockTarget, lockObj);
    }

    /**
     * Release the lock for the given NutchFile
     */
    public synchronized void release(String locMach, String locStr, String filename) throws IOException {
        // NFSFileSystem ignores the locMach value
        File lockTarget= new File(locStr, filename);

        FileLock lockObj = (FileLock) lockObjSet.get(lockTarget);
        FileInputStream lockData = (FileInputStream) lockDataSet.get(lockTarget);

        lockObj.release();
        lockData.close();

        lockObjSet.remove(lockTarget);
        lockDataSet.remove(lockTarget);
    }

    /**
     * Copy a file to the right place in the local dir, which assumes
     * NFS-connectivity.
     */
    protected void copyFile(File srcFile, String locMach, String locStr, String filename, boolean overwrite) throws IOException {
        // NFSFileSystem has no locMachine component.
        File target = new File(locStr, filename);
        FileUtil.copyContents(srcFile, target, overwrite);
    }

    /**
     * Remove a file from its current location.  Assumes an NFS-universe.
     */
    protected void deleteFile(String locMach, String locStr, String filename) throws IOException {
        // NFSFileSystem has no machine component
        FileUtil.fullyDelete(new File(locStr, filename));
    }

    /**
     * Rename the existing file or dir to a new location
     */
    protected void renameFile(File srcFile, String locMach, String locStr, String filename, boolean overwrite) throws IOException {
        // NFSFileSystem has no machine component
        File target = new File(locStr, filename);
        srcFile.renameTo(target);
    }
}
