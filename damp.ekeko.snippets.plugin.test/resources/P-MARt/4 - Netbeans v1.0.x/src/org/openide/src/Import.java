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

package org.openide.src;

/** Represents one class or package import.
*
* @author Petr Hamernik
*/
public class Import extends Object implements java.io.Serializable {
    /** A package import. */
    public static final boolean PACKAGE = true;
    /** A class import. */
    public static final boolean CLASS = false;

    /** Kind of this Import element. It is true if the import means "whole package"
    * otherwise (if it is import just one class) false.
    */
    private boolean wholePackage;

    /** Identifier which is imported */
    private Identifier id;

    static final long serialVersionUID =-4111760314345461897L;

    /** Create an import.
    * @param id the name of the class or package imported
    * @param wholePackage one of {@link #PACKAGE} or {@link #CLASS}
    */
    public Import(Identifier id, boolean wholePackage) {
        this.wholePackage = wholePackage;
        this.id = id;
    }

    /** Is this a package import?
    * @return <code>true</code> if so
    */
    public boolean isPackage() {
        return wholePackage;
    }

    /** Is this a class import?
    * @return <code>true</code> if so
    */
    public boolean isClass() {
        return (!wholePackage);
    }

    /** Get the name of the import.
    * @return the identifier which is imported
    */
    public Identifier getIdentifier() {
        return id;
    }

    /** Get this import as a string.
    * @return e.g. <code>import com.mycom.Class</code> or <code>import com.mycom.*</code>
    */
    public String toString() {
        StringBuffer buf = new StringBuffer("import "); // NOI18N
        buf.append(id.getFullName());
        if (wholePackage)
            buf.append(".*"); // NOI18N
        return buf.toString();
    }

    /** @return the hash code for this import */
    public int hashCode() {
        return id.getFullName().hashCode();
    }

    /** @return true if the specified object is also Import of the same class or package.
    */
    public boolean equals(Object o) {
        if (o instanceof Import) {
            Import imp = (Import) o;
            return (wholePackage == imp.wholePackage) && (id.equals(imp.id));
        }
        return false;
    }
}

/*
 * Log
 *  11   src-jtulach1.10        1/12/00  Petr Hamernik   i18n using perl script 
 *       (//NOI18N comments added)
 *  10   src-jtulach1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    src-jtulach1.8         9/13/99  Petr Hamernik   equals and hashCode 
 *       methods added
 *  8    src-jtulach1.7         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  7    src-jtulach1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    src-jtulach1.5         5/12/99  Petr Hamernik   Identifier 
 *       implementation updated
 *  5    src-jtulach1.4         4/14/99  Jesse Glick     [JavaDoc]
 *  4    src-jtulach1.3         4/14/99  Petr Hamernik   fixed bug #1523
 *  3    src-jtulach1.2         3/30/99  Jesse Glick     [JavaDoc]
 *  2    src-jtulach1.1         2/5/99   Petr Hamernik   
 *  1    src-jtulach1.0         1/17/99  Jaroslav Tulach 
 * $
 */
