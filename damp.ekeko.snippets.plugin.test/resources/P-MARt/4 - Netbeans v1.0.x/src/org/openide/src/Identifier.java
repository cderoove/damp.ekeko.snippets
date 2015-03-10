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

import java.lang.ref.WeakReference;
import java.util.HashMap;

/** Represents one identifier.
*
* @author Petr Hamernik, Jaroslav Tulach
*/
public final class Identifier extends Object implements java.io.Serializable {
    /** Full name of identifier. */
    private String fullName;

    /** The name which is either the same like fullName either
    * not fully qualified name - which was taken from the source
    * by java parser.
    */
    private String sourceName;

    /** Position after last dot in the identifier.
    * It is used in getName method.
    */
    private int namePos;

    /** Resolver for sourceName */
    private transient Resolver resolver;

    static final long serialVersionUID =-2614114568575211024L;
    /** New identifier.
    * @param resolver Resolver for fullName 
    * @param sourceName name for code generation
    */
    private Identifier(Resolver resolver, String sourceName) {

        this.fullName = null;
        this.sourceName = sourceName;
        this.resolver = resolver;
    }

    /** Initiates namePos variable */
    private void initNamePos() {
        setFullName();
        namePos = fullName.lastIndexOf("."); // NOI18N
        if (namePos != -1)
            namePos++;
        if (fullName.startsWith(".") || fullName.endsWith(".")) // NOI18N
            throw new IllegalArgumentException(fullName);
    }

    /** Sets fullName variable. */
    private void setFullName() {
        if (fullName == null) {
            fullName = (resolver == null ? sourceName : resolver.resolve().intern());
            resolver = null;  // clear it
            if (fullName == null) { // not resolved?
                fullName = sourceName;
            }
        }
    }


    /** Finds the existing instance */
    private Object readResolve() {
        return create(fullName, sourceName);
    }

    /** Create an identifier with the same source name and fully qualified name.
    *
    * @param name the name of the identifier to create
    * @return the identifier
    */
    public static Identifier create (String name) {
        String n = name.intern();
        return new Identifier(null, n);
    }

    /** Create an identifier.
    *
    * @param fullName fully qualified name
    * @param sourceName name for code generation
    * @return the identifier
    */
    public static Identifier create(String fullName, String sourceName) {
        Identifier ret = new Identifier(null, sourceName.intern());
        ret.fullName = fullName.intern();
        ret.initNamePos();
        return ret;
    }

    /** Create an Identifier
    *
    * @param resolver a Resolver
    * @param name the name of the identifier to create
    */
    public static Identifier create(Resolver resolver, String name) {
        return new Identifier(resolver, name.intern());
    }

    /** Get the simple name within a package.
    * @return the simple name
    */
    public String getName () {
        int pos = sourceName.lastIndexOf("."); // NOI18N
        return (pos == -1) ? sourceName : sourceName.substring(pos + 1);
    }

    /** Get the identifier for the code generation.
    * @return the name from the source code
    */
    public String getSourceName () {
        return sourceName;
    }

    /** Get the package prefix.
    * @return the prefix, with no trailing dot, or an empty string if in the default package
    */
    public String getQualifier () {
        if (fullName == null) {
            initNamePos();
        }
        return (namePos == -1) ? fullName : fullName.substring(0, namePos - 2);
    }

    /** Test whether this identifier is qualified by package.
    * @return <CODE>true</CODE> if so
    */
    public boolean isQualified () {
        if (fullName == null) {
            initNamePos();
        }
        return (namePos != -1);
    }

    /** Get the qualified name with the package prefix (if any).
    * @return the fully qualified name
    */
    public String getFullName () {
        if (fullName == null) {
            initNamePos();
        }
        return fullName;
    }

    /** This function was changed to match the behaviour of {@link Type#toString Type.toString} that
        returns text representation suitable for the source file.
    * @return source name of the identifier.
    */
    public String toString() {
        return getSourceName();
    }

    /** Compare the specified Identifier with this Identifier for equality.
    * @param id Identifier to be compared with this
    * @param source Determine if the source name should be also compared.
    *       If <CODE>false</CODE> only fully qualified name is compared.
    * @return <CODE>true</CODE> if the specified object equals to
    *         specified Identifier otherwise <CODE>false</CODE>.
    */
    public boolean compareTo(Identifier id, boolean source) {
        if (fullName == null) {
            initNamePos();
        }
        if (id.fullName == null) {
            id.initNamePos();
        }
        if (id.fullName == fullName) {
            return source ? (id.sourceName == sourceName) : true;
        }
        return false;
    }

    /** Compare the specified object with this Identifier for equality.
    * There are tested only full qualified name.
    * @param o Object to be compared with this
    * @return <CODE>true</CODE> if the specified object is Identifier
    *         with the same fully qualified name,
    *         otherwise <CODE>false</CODE>.
    */
    public boolean equals(Object o) {
        return (o instanceof Identifier) ? compareTo((Identifier) o, false) : false;
    }

    /** @return the hash code of full name String object.
    */
    public int hashCode() {
        if (fullName == null) {
            initNamePos();
        }
        return fullName.hashCode();
    }

    /** The interface allows lazy resolving of an Identifier to a fully qualified name. */
    public static interface Resolver {

        /**
         * @return fully qualified name
         */
        String resolve();
    }
}

/*
 * Log
 *  16   Gandalf-post-FCS1.14.1.0    4/3/00   Svatopluk Dedic toString semantics 
 *       changed to match Type behaviour
 *  15   src-jtulach1.14        1/12/00  Petr Hamernik   i18n using perl script 
 *       (//NOI18N comments added)
 *  14   src-jtulach1.13        1/3/00   Petr Hrebejk    getClassName does not 
 *       start with "." now
 *  13   src-jtulach1.12        11/29/99 Petr Hamernik   resolving improvement in
 *       method getName()
 *  12   src-jtulach1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   src-jtulach1.10        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  10   src-jtulach1.9         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    src-jtulach1.8         6/4/99   Petr Hamernik   comparing bugfixes
 *  8    src-jtulach1.7         5/28/99  Ales Novak      lazy resolving of 
 *       identifiers added
 *  7    src-jtulach1.6         5/13/99  Petr Hamernik   debug prints removed
 *  6    src-jtulach1.5         5/13/99  Petr Hamernik   
 *  5    src-jtulach1.4         5/12/99  Petr Hamernik   Identifier 
 *       implementation updated
 *  4    src-jtulach1.3         3/30/99  Jesse Glick     [JavaDoc]
 *  3    src-jtulach1.2         3/30/99  Jan Jancura     
 *  2    src-jtulach1.1         2/5/99   Petr Hamernik   
 *  1    src-jtulach1.0         1/17/99  Jaroslav Tulach 
 * $
 */
