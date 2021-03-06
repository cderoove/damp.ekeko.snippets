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

package org.netbeans.core.execution;

import java.security.PermissionCollection;
import java.security.Permission;
import java.util.Enumeration;

import org.openide.windows.InputOutput;

/** Every running process is represented by several objects in the ide whether
* or not it is executed as a thread or standalone process. The representation
* of a process should be marked by the IOPermissionCollection that gives possibility
* to such process to do its System.out/in operations through the ide.
*
* @author Ales Novak
*/
final class IOPermissionCollection extends PermissionCollection implements java.io.Serializable {

    /** InputOutput for this collection */
    private InputOutput io;
    /** Delegated PermissionCollection. */
    private PermissionCollection delegated;
    /** TaskThreadGroup ref or null */
    final TaskThreadGroup grp;

    static final long serialVersionUID =2046381622544740109L;
    /** Constructs new ExecutionIOPermission. */
    protected IOPermissionCollection(InputOutput io, PermissionCollection delegated, TaskThreadGroup grp) {
        this.io = io;
        this.delegated = delegated;
        this.grp = grp;
    }

    /** Standard implies method see java.security.Permission.
    * @param p a Permission
    */
    public boolean implies(Permission p) {
        return delegated.implies(p);
    }
    /** @return Enumeration of all Permissions in this collection. */
    public Enumeration elements() {
        return delegated.elements();
    }
    /** @param perm a Permission to add. */
    public void add(Permission perm) {
        delegated.add(perm);
    }

    /** @return "" */ // NOI18N
    public InputOutput getIO() {
        return io;
    }
    /** Sets new io for this PermissionCollection */
    public void setIO(InputOutput io) {
        this.io = io;
    }

    public String toString() {
        return delegated.toString();
    }
}

/*
 * Log
 *  8    src-jtulach1.7         1/12/00  Ales Novak      i18n
 *  7    src-jtulach1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    src-jtulach1.5         8/18/99  Ian Formanek    Generated serial version
 *       UID
 *  5    src-jtulach1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    src-jtulach1.3         5/13/99  Ales Novak      bugfix #1453
 *  3    src-jtulach1.2         4/10/99  Ales Novak      
 *  2    src-jtulach1.1         3/30/99  Ales Novak      
 *  1    src-jtulach1.0         3/30/99  Ales Novak      
 * $
 */
