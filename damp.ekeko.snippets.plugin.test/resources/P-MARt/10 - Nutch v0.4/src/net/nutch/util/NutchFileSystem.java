/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.util;

import java.io.*;

/****************************************************************
 * NutchFileSystem is an interface for a fairly simple
 * distributed file system.  A Nutch installation might consist
 * of multiple machines, which should swap files transparently.
 * This interface allows other Nutch systems to find and place
 * files into the distributed Nutch-controlled file world.
 *
 * The standard job of NutchFileSystem is to take the location-
 * independent NutchFile objects, and resolve them using local
 * knowledge and local instances of ShareGroup.
 * 
 * @author Mike Cafarella
 *****************************************************************/
public interface NutchFileSystem {

    /**
     * Get a real File for a name that's not yet under NutchFS control.
     * This may improve performance later on when the
     * File is put() under NutchFS control.  It's also handy for
     * finding a file location where there is a lot of extra room.
     */
    public File getWorkingFile() throws IOException;

    /**
     * Associates a NutchFile with a given real-fs File.  The
     * real-world File will be moved to a proper location according
     * to its NutchFile representation.  It will be moved locally
     * or remotely, as appropriate.
     *
     * The given "real" File can no longer be assumed to exist at
     * the given location after the call to putFile().  In the future,
     * the File should only be obtained via its NutchFile identifier.
     * 
     * Returns the File that was there previously, if any.
     */
    public void put(NutchFile nutchFile, File workingFile, boolean overwrite) throws IOException;

    /**
     * Sometimes the NutchFileSystem user constructs a directory of many
     * subparts, often built slowly over time.  However, that highest-level
     * directory might not ever have been put(); instead, its subparts 
     * have been put(), one piece at a time.
     *
     * Eventually, though, all the subdirs will be in place, and the
     * entire directory structure will be complete.  That event is
     * signified by calling "completeDir".  This call will mark
     * the given directory as completed.
     */
    public void completeDir(NutchFile nutchFile) throws IOException;

    /**
     * Obtains the indicated NutchFile, whether remote or local.
     * The function will block until the file is available.
     */
    public File get(NutchFile nutchFile) throws IOException;

    /**
     * Same as above, but expires after the given number of ms, 
     * returning null.
     */
    public File get(NutchFile nutchFile, long timeout) throws IOException;

    /**
     * Obtain a lock with the given NutchFile as the lock object
     */
    public void lock(NutchFile lockFile, boolean exclusive) throws IOException;

    /**
     * Release the lock.  Must be in the lock() state.
     */
    public void release(NutchFile lockFile) throws IOException;

    /**
     * Delete the given NutchFile and everything below it.  This is
     * propagated to the different appropriate machines, the same
     * way a put() operation is.
     */
    public void delete(NutchFile nutchFile) throws IOException;

    /**
     * Rename the given NutchFile to something new.  Files cannot
     * be moved across share-spaces.  The change is propagated 
     * immediately to all participants in the share-space.  The
     * client is responsible for any necessary locking or process
     * synchronization.
     */
    public void renameTo(NutchFile src, NutchFile dst) throws IOException;

    /**
     * Close down the fs.
     */
    public void close() throws IOException;
}
