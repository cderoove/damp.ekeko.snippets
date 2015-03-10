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

package org.openide.filesystems;

/** Event describing adding a file system to, or removing a file system from, the file system pool.
*
* @author Jaroslav Tulach
* @version 0.10 November 4, 1997
*/
public class RepositoryEvent extends java.util.EventObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5466690014963965717L;
    /** the modifying file system */
    private FileSystem fileSystem;

    /** added or removed */
    private boolean add;

    /** Create a new file system pool event.
    * @param fsp file system pool that is being modified
    * @param fs file system that is either being added or removed
    * @param add <CODE>true</CODE> if the file system is added,
    *    <CODE>false</CODE> if removed
    */
    public RepositoryEvent (Repository fsp, FileSystem fs, boolean add) {
        super (fsp);
        this.fileSystem = fs;
        this.add = add;
    }

    /** Getter for the file system pool that is modified.
    * @return the file system pool
    */
    public Repository getRepository () {
        return (Repository)getSource ();
    }

    /** Getter for the file system that is added or removed.
    * @return the file system
    */
    public FileSystem getFileSystem () {
        return fileSystem;
    }

    /** Is the file system added or removed?
    * @return <CODE>true</CODE> if the file system is added, <code>false</code> if removed
    */
    public boolean isAdded () {
        return add;
    }
}

/*
 * Log
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         2/11/99  Ian Formanek    
 * $
 */
