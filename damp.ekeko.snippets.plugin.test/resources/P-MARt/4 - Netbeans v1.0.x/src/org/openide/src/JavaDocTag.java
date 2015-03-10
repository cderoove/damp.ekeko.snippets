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

/**
 * Represents a documentation tag, e.g. @since, @author, @version.
 * Given a tag (e.g. "@since 1.2"), holds tag name (e.g. "@since")
 * and tag text (e.g. "1.2").  Tags with structure or which require
 * special processing are handled by special interfaces (JavaDocTag.See, 
 * JavaDocTag.Param, JavaDocTag.Throws and JavaDocTag.SerialField).
 * The interfaces provide subset of methods defined in Tag interfaces
 * in the Doclet API.
 *
 * @author Petr Hrebejk
 * @see JavaDoc#getTags()
 *
 */

public interface JavaDocTag {

    /**
     * Return the name of this tag.
     */
    String name();

    /**
     * Return the kind of this tag.
     */
    String kind();

    /**
     * Return the text of this tag, that is, portion beyond tag name.
     */
    String text();

    /**
     * Represents a see also documentation tag.
     *  
     */

    public static interface See extends JavaDocTag {

        /**
         * Return the label of the see tag.
         */
        String label();

        /**
         * get the class name part of @see, For instance,
         * if the comment is @see String#startsWith(java.lang.String) .
         *      This function returns String.
         * Returns null if format was not that of java reference.
         * Return empty string if class name was not specified..
         */
        String referencedClassName();

        /**
         * get the name of the member referenced by the prototype part of @see,
         * For instance,
         * if the comment is @see String#startsWith(java.lang.String) .
         *      This function returns "startsWith(java.lang.String)"
         * Returns null if format was not that of java reference.
         * Return empty string if member name was not specified..
         */
        String referencedMemberName();

    }

    /**
     * Represents an @param documentation tag.
     * The parses and stores the name and comment parts of the
     * method/constructor parameter tag.
     */

    public static interface Param extends JavaDocTag {

        /**
         * Return the parameter name.
         */
        String parameterName();

        /**
         * Return the parameter comment.
         */
        String parameterComment();
    }

    /**
     * Represents a @throws or @exception documentation tag.
     * Parses and holds the exception name and exception comment.
     * Note: @exception is a backwards compatible synonymy for @throws.
     */
    public static interface Throws extends JavaDocTag {

        /**
        * Return the exception name.
        */
        String exceptionName();

        /**
         * Return the exception comment.
         */
        String exceptionComment();

    }

    /**
     * Documents a Serializable field defined by an ObjectStreamField.
     * <pre>
     * The class parses and stores the three serialField tag parameters:
     *
     * - field name
     * - field type name
     *      (fully-qualified or visible from the current import context)
     * - description of the valid values for the field

     * </pre>
     * This tag is only allowed in the javadoc for the special member
     *
     * @see java.io.ObjectStreamField
     */

    public static interface SerialField extends JavaDocTag {
        /**
         * Return the serialziable field name.
         */
        public String fieldName();

        /**
         * Return the field type string.
         */
        public String fieldType();

        /**
         * Return the field comment. If there is no serialField comment, return
         * javadoc comment of corresponding FieldDoc.
         */
        public String description();
    }

}

/*
 * Log
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         5/5/99   Petr Hrebejk    
 * $ 
 */ 