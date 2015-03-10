/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.util;

import java.io.*;
import java.util.*;
import java.nio.channels.*;

/****************************************************************
 * NutchGenericFileSystem implements the NutchFileSystem interface
 * and adds some generic utility methods for subclasses to use.
 *
 * The standard task any implementor of NutchFileSystem
 *
 * @author Mike Cafarella
 ****************************************************************/
public abstract class NutchGenericFileSystem implements NutchFileSystem {
    File dbRoot, localTmp, flagFile;
    FileInputStream lockData;
    FileLock lock;
    ShareSet shareSet;
    boolean destructivePut;

    /**
     * Create a Nutch Filesystem at the indicated mounted
     * directory.
     */
    public NutchGenericFileSystem(File dbRoot, ShareSet shareSet, boolean destructivePut) throws IOException {
        if (shareSet == null) {
            this.shareSet = new ShareSet(dbRoot);
        } else {
            this.shareSet = shareSet;
        }

        //
        // 1.  Create/find main work area (which will receive files from
        //     other processes and may be shared).
        this.dbRoot = dbRoot;
        if (! dbRoot.exists()) {
            dbRoot.mkdirs();
        }
        if (! dbRoot.isDirectory()) {
            throw new IOException("Directory " + dbRoot + " does not exist.");
        }

        //
        // 2.  Attempt to acquire an exclusive lock on the directory.
        //     If this succeeds, the process should then clear out the
        //     tmp storage area.  If this fails, just continue.
        //
        Vector tmpDirs = new Vector();
        File rootFiles[] = dbRoot.listFiles();
        for (int i = 0; i < rootFiles.length; i++) {
            if (rootFiles[i].isDirectory() && rootFiles[i].getName().startsWith("localtmpdir")) {
                tmpDirs.add(rootFiles[i]);
            }
        }
        // If there are any tmpDirs for us to delete, try to do it.
        if (tmpDirs.size() > 0) {
            File exclusiveLockFile = new File(dbRoot, "nutchfslock");
            exclusiveLockFile.createNewFile();
            FileOutputStream exclusiveLockData = new FileOutputStream(exclusiveLockFile);
            FileLock exclusiveLock = exclusiveLockData.getChannel().tryLock();

            // Once we have the lock, go and delete them
            if (exclusiveLock != null) {
                for (Enumeration e = tmpDirs.elements(); e.hasMoreElements(); ) {
                    FileUtil.fullyDelete((File) e.nextElement());
                }
                exclusiveLock.release();
                exclusiveLockData.close();
            }
        }


        //
        // 3.  Acquire a non-exclusive lock on the directory.  Block
        //     until this is acquired.  (The only thing preventing it
        //     would be another process in step 2.)
        //
        File lockFile = new File(dbRoot, "nutchfslock");
        lockFile.createNewFile();
        this.lockData = new FileInputStream(lockFile);
        this.lock = lockData.getChannel().lock(0L, Long.MAX_VALUE, true);

        //
        // 4.  Create the tmp directory
        //
        this.localTmp = File.createTempFile("localtmpdir", "", dbRoot);
        this.localTmp.delete();
        if (! localTmp.exists()) {
            localTmp.mkdirs();
        }
        if (! localTmp.isDirectory()) {
            throw new IOException("Directory " + localTmp + " does not exist.");
        }

        //
        // 5.  Create the src lock file
        //
        this.flagFile = File.createTempFile("flag", "tmp");

        //
        // 6.  Whether files should be deleted after being copied
        //
        this.destructivePut = destructivePut;
    }

    /**
     * Acquire a real File for a name that's not yet under NutchFS
     * control.  This may improve performance later on when the
     * File is put() under NutchFS control.  It's also handy for
     * finding a file location where there is a lot of extra room.
     */
    public File getWorkingFile() throws IOException {        
        File f = File.createTempFile("tmp", "", localTmp);
        f.delete();
        return f;
    }

    /**
     * Wait for a NutchFile from somewhere in NutchSpace.  Translate 
     * it to a regular old filesystem File.
     *
     * The file should already be in place.  So we wait until it is.
     */
    public File get(NutchFile nutchFile) throws IOException {
        return get(nutchFile, -1);
    }

    /**
     * Wait for a NutchFile for the specified amount of time.  Return null
     * if we don't get it before 'timeout' ms have elapsed.
     */
    public File get(NutchFile nutchFile, long timeout) throws IOException {
        long startTime = System.currentTimeMillis();
        int numTries = 0;
        ShareGroup sg = shareSet.getShareGroup(nutchFile);

        File target = new File(dbRoot, nutchFile.getFilename());
        File completeFlag = new File(dbRoot, nutchFile.getCompleteFlagName());
        while (! completeFlag.exists()) {
            try {
                if ((numTries > 0) && 
                    (timeout > 0) && 
                    (System.currentTimeMillis() - startTime > timeout)) {
                    return null;
                }
                Thread.sleep(1000);
                numTries++;
                if (numTries > 10) {
                    System.err.println("NutchGenericFileSystem waiting for file " + completeFlag);
                }
            } catch (InterruptedException ie) {
            }
        }
        return target;
    }

    /**
     * Obtain a lock with the given NutchFile.  This might mean obtaining
     * locks across many different machines/filesystems.  That's fine,
     * as long as every machine always obtains the locks in a standard 
     * ordering.
     */
    public void lock(NutchFile nutchFile, boolean exclusive) throws IOException {
        File lockFile = getWorkingFile();
        lockFile.createNewFile();
        put(nutchFile, lockFile, false);

        ShareGroup sg = shareSet.getShareGroup(nutchFile);
        String locations[] = sg.getLocations();
        for (int i = 0; i < locations.length; i++) {
            String locMach = extractMachine(locations[i]);
            String locStr = extractPath(locations[i]);
            lockFile(locMach, locStr, nutchFile.getFilename(), exclusive);
        }
    }

    /**
     * Release the lock for the given NutchFile
     */
    public void release(NutchFile nutchFile) throws IOException {
        ShareGroup sg = shareSet.getShareGroup(nutchFile);
        String locations[] = sg.getLocations();
        for (int i = 0; i < locations.length; i++) {
            String locMach = extractMachine(locations[i]);
            String locStr = extractPath(locations[i]);
            release(locMach, locStr, nutchFile.getFilename());
        }
    }

    /**
     * Add a single file or a directory of files to the filesystem.
     * If the source File is a directory, we want to reproduce
     * the entire directory structure, rooted at the given
     * NutchFile.
     */
    public void put(NutchFile nutchFile, File workingFile, boolean overwrite) throws IOException {
        if (workingFile.isDirectory()) {
            putDir(nutchFile, workingFile, overwrite);
        } else {
            putFile(nutchFile, workingFile, overwrite);
        }
        FileUtil.fullyDelete(workingFile);
    }

    /**
     * Add a directory and its contents to the filesystem
     */
    void putDir(NutchFile nutchDir, File workingDir, boolean overwrite) throws IOException {    
        File workingFiles[] = workingDir.listFiles();
        NutchFile nutchFiles[] = new NutchFile[workingFiles.length];

        //
        // Remove target dir's completion flag
        //
        ShareGroup sg = shareSet.getShareGroup(nutchDir);
        String locations[] = sg.getLocations();
        for (int i = 0; i < locations.length; i++) {
            String locMach = extractMachine(locations[i]);
            String locStr = extractPath(locations[i]);
            deleteFile(locMach, locStr, nutchDir.getCompleteFlagName());
        }

        //
        // Build a list of all contained items
        //
        for (int i = 0; i < nutchFiles.length; i++) {
            nutchFiles[i] = new NutchFile(nutchDir, workingFiles[i].getName());
        }

        //
        // Put the list to the FS
        //
        for (int i = 0; i < workingFiles.length; i++) {
            put(nutchFiles[i], workingFiles[i], overwrite);
        }

        //
        // We've written dir's contents, so write out completion flag
        //
        for (int i = 0; i < locations.length; i++) {
            String locMach = extractMachine(locations[i]);
            String locStr = extractPath(locations[i]);
            copyFile(flagFile, locMach, locStr, nutchDir.getCompleteFlagName(), true);
        }
    }

    /**
     * Add a single file to the filesystem.
     */
    void putFile(NutchFile nutchFile, File workingFile, boolean overwrite) throws IOException {
        ShareGroup sg = shareSet.getShareGroup(nutchFile);
        String locations[] = sg.getLocations();
        for (int i = 0; i < locations.length; i++) {
            String locMach = extractMachine(locations[i]);
            String locStr = extractPath(locations[i]);

            // Remove 'complete' flag
            deleteFile(locMach, locStr, nutchFile.getCompleteFlagName());

            // Write file, if necessary.
            copyFile(workingFile, locMach, locStr, nutchFile.getFilename(), overwrite);

            // Write 'complete' flag
            copyFile(flagFile, locMach, locStr, nutchFile.getCompleteFlagName(), true);
        }
    }

    /**
     * Complete the given directory
     */
    public void completeDir(NutchFile nutchFile) throws IOException {
        ShareGroup sg = shareSet.getShareGroup(nutchFile);
        String locations[] = sg.getLocations();
        for (int i = 0; i < locations.length; i++) {
            String locMach = extractMachine(locations[i]);
            String locStr = extractPath(locations[i]);

            // Write 'complete' flag
            copyFile(flagFile, locMach, locStr, nutchFile.getCompleteFlagName(), true);
        }
    }

    /**
     * Take the file out of the NutchFileSystem.
     */
    public void delete(NutchFile nutchFile) throws IOException {
        ShareGroup sg = shareSet.getShareGroup(nutchFile);
        String locations[] = sg.getLocations();
        for (int i = 0; i < locations.length; i++) {
            String locMach = extractMachine(locations[i]);
            String locStr = extractPath(locations[i]);

            deleteFile(locMach, locStr, nutchFile.getFilename());
            deleteFile(locMach, locStr, nutchFile.getCompleteFlagName());
        }
    }

    /**
     * Rename the thing.  Usually done at close.
     */
    public void renameTo(NutchFile src, NutchFile dst) throws IOException {
        // Make sure src file is complete
        File srcFile = get(src);

        // Remove src complete flags
        ShareGroup sg = shareSet.getShareGroup(src);
        String locations[] = sg.getLocations();
        for (int i = 0; i < locations.length; i++) {
            String locMach = extractMachine(locations[i]);
            String locStr = extractPath(locations[i]);

            // Remove src complete flags
            deleteFile(locMach, locStr, src.getCompleteFlagName());

            // Rename contents
            renameFile(srcFile, locMach, locStr, dst.getFilename(), true);

            // Create target flags
            copyFile(flagFile, locMach, locStr, dst.getCompleteFlagName(), true);
        }
    }

    /**
     * Close down the Generic File System
     */
    public void close() throws IOException {
        // Get rid of the tmp directory
        FileUtil.fullyDelete(localTmp);
        
        // Get rid of tmp flag file
        FileUtil.fullyDelete(flagFile);

        this.lock.release();
        this.lockData.close();
    }

    /**
     * To be implemented by subclasses
     */
    protected abstract void copyFile(File srcFile, String locationMach, String locationStr, String nutchFileName, boolean overwrite) throws IOException;
    protected abstract void deleteFile(String locationMach, String locationStr, String nutchFileName) throws IOException;
    protected abstract void renameFile(File srcFile, String locationMach, String locationStr, String nutchFileName, boolean overwrite) throws IOException;
    protected abstract void lockFile(String locMach, String locStr, String filename, boolean exclusive) throws IOException;
    protected abstract void release(String locMach, String locStr, String filename) throws IOException;

    /**
     * Utility str-processing of location-string.
     * (format "machinename:path")
     */
    String extractMachine(String location) {
        int colDex = location.indexOf(":");
        if (colDex < 0) {
            return null;
        }
        return location.substring(0, colDex);
    }
    
    /**
     * Utility str-processing of location-string.
     * (format "machinename:path")
     */
    String extractPath(String location) {
        int colDex = location.indexOf(":");
        if (colDex < 0) {
            return location;
        }
        return location.substring(colDex + 1);
    }
}
