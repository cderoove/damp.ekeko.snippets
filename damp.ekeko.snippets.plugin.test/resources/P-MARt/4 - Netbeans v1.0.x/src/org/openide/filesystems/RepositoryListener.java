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

/** Listener to changes in the file system pool.
*
* @author Jaroslav Tulach
* @version 0.10 November 4, 1997
*/
public interface RepositoryListener extends java.util.EventListener {
    /** Called when new file system is added to the pool.
    * @param ev event describing the action
    */
    public void fileSystemAdded (RepositoryEvent ev);

    /** Called when a file system is removed from the pool.
    * @param ev event describing the action
    */
    public void fileSystemRemoved (RepositoryEvent ev);

    /** Called when a file system pool is reordered. */
    public void fileSystemPoolReordered(RepositoryReorderedEvent ev);
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
