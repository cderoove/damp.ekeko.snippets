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

import java.util.EventObject;


/** Fired when a file system pool is reordered.
 * @see Repository#reorder
 */
public class RepositoryReorderedEvent extends EventObject {

    /** permutation */
    private int[] perm;

    static final long serialVersionUID =-5473107156345392581L;
    /** Create a new reorder event.
     * @param fsp the file system pool being reordered
     * @param perm the permutation of file systems in the pool
     */
    public RepositoryReorderedEvent(Repository fsp, int[] perm) {
        super(fsp);
        this.perm = perm;
    }

    /** Get the affected file system pool.
     * @return the pool
     */
    public Repository getRepository() {
        return (Repository)getSource();
    }

    /** Get the permutation of file systems.
     * @return the permutation
     */
    public int[] getPermutation() {
        int[] nperm = new int[perm.length];
        System.arraycopy(perm, 0, nperm, 0, perm.length);
        return nperm;
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         2/11/99  Ian Formanek    
 * $
 */
