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

import java.util.StringTokenizer;

/** Describes an argument of a method.
*
* @author Petr Hamernik, Jaroslav Tulach
*/
public final class MethodParameter extends Object implements java.io.Serializable {
    /** Name of argument */
    private String name;

    /** Type of argument */
    private Type type;

    /** State of final flag */
    private boolean fin;

    static final long serialVersionUID =-6158959006278766562L;
    /** Create new parameter.
    * @param name the name of the parameter
    * @param type the type of the parameter
    * @param fin <code>true</code> if this parameter is <code>final</code>
    */
    public MethodParameter(String name, Type type, boolean fin) {
        this.name = name;
        this.type = type;
        this.fin = fin;
    }

    /** Create a method parameter by parsing its textual representation.
    * @param text the text to be parsed
    * @return a new method parameter described by the text
    * @exception IllegalArgumentException if the syntax is not recognized
    */
    public static MethodParameter parse(String text) throws IllegalArgumentException {
        StringTokenizer tok = new StringTokenizer(text, " []", true); // NOI18N
        boolean rightBracketExpected = false;

        boolean fin = false;
        Type type = null;
        String name = null;

        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            if (token.equals(" ")) // NOI18N
                continue;

            if (type == null) { // we are before type
                if (token.equals("final")) { // final // NOI18N
                    if (fin) // final already was once
                        throw new IllegalArgumentException();
                    fin = true;
                }
                else { // type
                    type = Type.parse(token);
                }
            }
            else { // we are after type
                if (token.equals("[")) { // left bracket -> right bracket is expected // NOI18N
                    if (rightBracketExpected)
                        throw new IllegalArgumentException();
                    rightBracketExpected = true;
                }
                else if (token.equals("]")) { // right bracket -> create array // NOI18N
                    if (!rightBracketExpected)
                        throw new IllegalArgumentException();
                    type = Type.createArray(type);
                    rightBracketExpected = false;
                }
                else { // it must be name of the parameter
                    if (name != null) //already was!
                        throw new IllegalArgumentException();
                    name = token;
                }
            }
        }
        if ((type == null) || (name == null) || rightBracketExpected)
            throw new IllegalArgumentException();

        return new MethodParameter(name, type, fin);
    }

    /** Get the parameter type.
    * @return the type
    */
    public Type getType() {
        return type;
    }

    /** Set the parameter type.
    * @param type the new type
    */
    public void setType(Type type) {
        this.type = type;
    }

    /** Get the name of the parameter variable.
    * @return the name
    */
    public String getName() {
        return name;
    }

    /** Set the name of the parameter variable.
    * @param name the new name
    */
    public void setName(String name) {
        this.name = name;
    }

    /** Make this parameter final or not.
    * @param fin <code>true</code> to make it final, <code>false</code> to make it unfinal
    */
    public void setFinal(boolean fin) {
        this.fin = fin;
    }

    /** Test whether this parameter is final.
    * @return <code>true</code> if so
    */
    public boolean isFinal() {
        return fin;
    }

    /** Get this MethodParameter as the string.
    * @param appendTo The string buffer where to append to
    * @param source true means getSourceName() will be used, otherwise getFullName()
    * @return the same string buffer which was passed into
    */
    StringBuffer getAsString(StringBuffer appendTo, boolean source) {
        if (fin)
            appendTo.append("final "); // NOI18N
        type.getAsString(appendTo, source);
        appendTo.append(" "); // NOI18N
        appendTo.append(name);
        return appendTo;
    }

    /* @return the text form of the method parameter - fully qualified type.
    */
    public String getFullString() {
        return getAsString(new StringBuffer(), false).toString();
    }

    /* @return the text form of the method parameter - using
    * getSourceString() for the type
    */
    public String getSourceString() {
        return getAsString(new StringBuffer(), true).toString();
    }

    /* @return the text form of the method parameter - fully qualified type.
    */
    public String toString() {
        return getAsString(new StringBuffer(), false).toString();
    }

    /** Compare the specified MethodParameter with this for equality.
    * @param param MethodParameter to be compared with this
    * @param onlyType Compares only the type (not name and final flag)
    * @param source Determine if the source name (for class types)
    *       should be also compared.
    *       If <CODE>false</CODE> only fully qualified name is compared.
    * @return <CODE>true</CODE> if the specified object equals to
    *         specified MethodParameter otherwise <CODE>false</CODE>.
    */
    public boolean compareTo(MethodParameter param, boolean onlyType, boolean source) {
        if (!type.compareTo(param.type, source))
            return false;

        return onlyType ? true : ((param.fin == fin) && (param.name.equals(name)));
    }


    /* @return <CODE>true</CODE> if the given object represents
    *         the same parameter.  Compares only the type.
    */
    public boolean equals(Object o) {
        return (o instanceof MethodParameter) ?
               compareTo((MethodParameter)o, true, false) :
               false;
    }

    /* @return The hashcode of this parameter
    */
    public int hashCode() {
        return name.hashCode() + type.hashCode();
    }
}

/*
 * Log
 *  14   src-jtulach1.13        1/13/00  Petr Hamernik   i18n -(2nd round) - 
 *       script bug fixed.
 *  13   src-jtulach1.12        1/12/00  Petr Hamernik   i18n using perl script 
 *       (//NOI18N comments added)
 *  12   src-jtulach1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   src-jtulach1.10        8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  10   src-jtulach1.9         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    src-jtulach1.8         5/13/99  Petr Hamernik   
 *  8    src-jtulach1.7         5/12/99  Petr Hamernik   Identifier 
 *       implementation updated
 *  7    src-jtulach1.6         5/11/99  Ian Formanek    Got rid of strange chars
 *       at the end
 *  6    src-jtulach1.5         5/11/99  Jesse Glick     [JavaDoc]
 *  5    src-jtulach1.4         5/10/99  Petr Hamernik   equals & hashCode added
 *  4    src-jtulach1.3         3/30/99  Jesse Glick     [JavaDoc]
 *  3    src-jtulach1.2         3/18/99  Petr Hamernik   
 *  2    src-jtulach1.1         3/16/99  Petr Hamernik   properties improvements
 *  1    src-jtulach1.0         1/17/99  Jaroslav Tulach 
 * $
 */
