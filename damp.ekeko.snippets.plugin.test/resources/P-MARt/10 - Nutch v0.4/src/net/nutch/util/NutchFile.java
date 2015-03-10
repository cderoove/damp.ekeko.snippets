/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.util;

import java.io.*;

/************************************************************
 * A class that names a file in the "NutchFileSpace".  You can 
 * convert a NutchFile to a real file with the help of
 * an instance of NutchFileSystem.
 *
 * @author Mike Cafarella
 *************************************************************/
public class NutchFile {
    String dbName;
    String shareGroupName;
    File name;
    NutchFileSystem nfs;

    /**
     * A NutchFile contains:
     *   dbName, which labels the cooperating NutchFileSystem it
     *           belongs to.
     *   shareGroupName,  which tells the NutchFileSystem which group should get
     *           access to this file.  If the value is null, then no remote 
     *           group will get access.
     *   name, which gives the file a unique name.
     */
    public NutchFile(NutchFileSystem nfs, String dbName, String shareGroupName, File name) {
        this.nfs = nfs;
        this.dbName = dbName;
        this.shareGroupName = shareGroupName;
        this.name = name;
    }

    /**
     * Create a NutchFile from a previous one that is a directory.
     */
    public NutchFile(NutchFile dir, String name) {
        this.nfs = dir.nfs;
        this.dbName = dir.getDBName();
        this.shareGroupName = dir.getShareGroupName();
        this.name = new File(dir.getName(), name);
    }

    /**
     * DB Name the NutchFile lives in.
     */
    public String getDBName() {
        return dbName;
    }

    /**
     * Get the name of the sharegroup this file belongs to.
     */
    public String getShareGroupName() {
        return shareGroupName;
    }

    /**
     * Terminating filename for the NutchFile.
     */
    public File getName() {
        return name;
    }

    /**
     * Grab a handle to the NutchFileSystem
     */
    public NutchFileSystem getFS() {
        return nfs;
    }

    /**
     * Get the almost-fully-qualified name for this NutchFile.
     */
    public String getFilename() {
        File target = new File(new File(dbName), shareGroupName);
        target = new File(target, name.getPath());
        return target.getPath();
    }

    /**
     * Get the almost-fully-qualified name for this NutchFile's
     * 'completed' flag file.
     */
    public String getCompleteFlagName() {
        File db = new File(dbName);
        File target = new File(new File(dbName), shareGroupName);
        target = new File(target, name.getPath() + ".completed");
        return target.getPath();
    }

    /**
     */
    public String toString() {
        return getFilename();
    }
}
