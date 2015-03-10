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

//package org.openide.src;
package org.openide.src;

import org.openide.src.JavaDocTag;


/**
 * Represents a documentation tag, e.g. @since, @author, @version.
 * Given a tag (e.g. "@since 1.2"), holds tag name (e.g. "@since")
 * and tag text (e.g. "1.2").  Tags with structure or which require
 * special processing are handled by special interfaces (JavaDocTag.See, 
 * JavaDocTag.Param, JavaDocTag.Throws and JavaDocTag.SerialField).
 * The interfaces provide subset of methods defined in Tag interfaces
 * in the Doclet API. The implementation holds informations about tags in
 * memory.
 *
 * @author Petr Hrebejk
 * @see JavaDoc#getTags()
 *
 */

class JavaDocTagMemoryImpl implements JavaDocTag {

    String name;
    String text;

    JavaDocTagMemoryImpl( String name, String text ) {
        this.name = name;
        this.text = text;
    }

    public String toString() {
        return name + " " + text; // NOI18N
    }


    // PENDING remove this method

    void Warning( String t1, String t2 ) {
        if (System.getProperty("netbeans.debug.exceptions") != null)
            System.out.println ( "WARNING: " + t1 + t2 ); // NOI18N
    }

    /**
     * Return the name of this tag.
     */
    public String name() {
        return name;
    }

    /**
     * Return the kind of this tag.
     */
    public String kind() {
        return name;
    }

    /**
     * Return the text of this tag, that is, portion beyond tag name.
     */
    public String text() {
        return text;
    }

    // TAG private & utility methods ------------------------------------------------------

    /**
     * for use by subclasses which have two part tag text.
     */
    String[] divideAtWhite() {
        String[] sa = new String[2];
        int len = text.length();
        // if no white space found
        sa[0] = text;
        sa[1] = ""; // NOI18N
        for (int inx = 0; inx < len; ++inx) {
            char ch = text.charAt(inx);
            if (Character.isWhitespace(ch)) {
                sa[0] = text.substring(0, inx);
                for (; inx < len; ++inx) {
                    ch = text.charAt(inx);
                    if (!Character.isWhitespace(ch)) {
                        sa[1] = text.substring(inx, len);
                        break;
                    }
                }
                break;
            }
        }
        return sa;
    }


    // INNER CLASSES ----------------------------------------------------------------------

    /**
     * Represents a see also documentation tag.
     *  
     */

    static class See extends JavaDocTagMemoryImpl implements JavaDocTag.See {

        private String label = ""; // NOI18N

        /** where#what */
        private String where;
        private String what;

        See( String name, String text ) {
            super( name, text );
            parseSeeString();
        }

        /**
         * Return the label of the see tag.
         */
        public String label() {
            return label;
        }

        /**
         * get the class name part of @see, For instance,
         * if the comment is @see String#startsWith(java.lang.String) .
         *      This function returns String.
         * Returns null if format was not that of java reference.
         * Return empty string if class name was not specified..
         */
        public String referencedClassName() {
            return where;
        }

        /**
         * get the name of the member referenced by the prototype part of @see,
         * For instance,
         * if the comment is @see String#startsWith(java.lang.String) .
         *      This function returns "startsWith(java.lang.String)"
         * Returns null if format was not that of java reference.
         * Return empty string if member name was not specified..
         */
        public String referencedMemberName() {
            return what;
        }

        /**
         * Return the kind of this tag.
         */
        public String kind() {
            return "@see"; // NOI18N
        }

        /**
         * Return the rawText for this tag 
         */

        public String toString() {
            StringBuffer sb = new StringBuffer( 100 );

            sb.append( name ).append( " " ); // NOI18N

            if (  referencedClassName() != null )
                sb.append( referencedClassName() );

            sb.append( "#" ); // NOI18N

            if (  referencedMemberName() != null )
                sb.append( referencedMemberName() );

            sb.append( " " ); // NOI18N

            if ( label != null )
                sb.append( label );

            return sb.toString();
        }

        // SEE TAG private & utility methods --------------------------------------------------

        /**
         * parse @see part of comment. Determine 'where' and 'what'
         */
        private void parseSeeString( ) {
            int len = text.length();

            if (len == 0) {
                return;
            }

            switch (text.charAt(0)) {
            case '<':
                if (text.charAt(len-1) != '>') {
                    this.Warning("tag.see.no_close_bracket_on_url", text); // NOI18N
                }
                return;
            case '"':
                if (len == 1 || text.charAt(len-1) != '"') {
                    this.Warning("tag.see.no_close_quote", text); // NOI18N
                }
                else {
                    // text = text.substring(1,len-1); // strip quotes
                }
                return;
            }

            // check that the text is one word, with possible parentheses
            // this part of code doesn't allow
            // @see <a href=.....>asfd</a>
            // comment it.

            // the code assumes that there is no initial white space.
            int parens = 0;
            int commentstart = 0;
            int start = 0;

            for (int i = start; i < len ; i++) {
                char ch = text.charAt(i);
                switch (ch) {
                case '(':
                    parens++;
                    break;
                case ')':
                    parens--;
                    break;
    case '[': case ']': case '.': case '#':
                    break;
                case ',':
                    if (parens <= 0) {
                        this.Warning("tag.see.malformed_see_tag", text); // NOI18N
                        return;
                    }
                    break;
        case ' ': case '\t': case '\n':
                    if (parens == 0) { //here onwards the comment starts.
                        commentstart = i;
                        i = len;
                    }
                    break;
                default:
                    if (!Character.isJavaIdentifierPart(ch)) {
                        this.Warning("tag.see.illegal_character",  "" + ch + text); // NOI18N
                        return;
                    }
                    break;
                }
            }
            if (parens != 0) {
                this.Warning("tag.see.malformed_see_tag", text); // NOI18N
                return;
            }

            String seetext = ""; // NOI18N
            String labeltext = ""; // NOI18N

            if (commentstart > 0) {
                seetext = text.substring(start, commentstart);
                labeltext = text.substring(commentstart + 1);
                // strip off the white space which can be between seetext and the
                // actual label.
                for (int i = 0; i < labeltext.length(); i++) {
                    char ch = labeltext.charAt(i);
                    if (!(ch == ' ' || ch == '\t' || ch == '\n')) {
                        label = labeltext.substring(i);
                        break;
                    }
                }
            }
            else {
                seetext = text;
                label = ""; // NOI18N
            }

            int sharp = seetext.indexOf('#');
            if (sharp >= 0) {
                // class#member
                where = seetext.substring(0, sharp);
                what = seetext.substring(sharp + 1);
            }
            else {
                if (seetext.indexOf('(') >= 0) {
                    this.Warning("tag.see.missing_sharp", text); // NOI18N
                    where = ""; // NOI18N
                    what = seetext;
                }
                else {
                    // no member specified, text names class
                    where = seetext;
                    what = null;
                }
            }
        }
    }

    /**
     * Represents an @param documentation tag.
     * The parses and stores the name and comment parts of the
     * method/constructor parameter tag.
     */

    static class Param extends JavaDocTagMemoryImpl implements JavaDocTag.Param {

        private String parameterName;
        private String parameterComment;

        Param( String name, String text )  {
            super( name, text );

            String[] sa = this.divideAtWhite();

            this.parameterName = sa[0];
            this.parameterComment = sa[1];

            /*
            if (!(holder instanceof ExecutableMemberDocImpl)) {
              Warning("tag.not_on_method", " " this.text );
        }
            */
        }

        /**
         * Return the parameter name.
         */
        public String parameterName() {
            return parameterName;
        }

        /**
         * Return the parameter comment.
         */
        public String parameterComment() {
            return parameterComment;
        }

        /**
         * Return the kind of this tag.
         */
        public String kind() {
            return "@param"; // NOI18N
        }


        /**
         * Return the rawText for this tag 
         */
        public String toString() {
            return name + " " + parameterName() + " " + parameterComment(); // NOI18N
        }
    }

    /**
     * Represents a @throws or @exception documentation tag.
     * Parses and holds the exception name and exception comment.
     * Note: @exception is a backwards compatible synonymy for @throws.
     */
    static class Throws extends JavaDocTagMemoryImpl implements JavaDocTag.Throws {

        private String exceptionName;
        private String exceptionComment;

        Throws( String name, String text ) {
            super( name, text );

            /*
            if (runtimeException == null) {
              runtimeException = ClassDocImpl.lookup("java.lang.RuntimeException");
        }
            */
            String[] sa = this.divideAtWhite();
            this.exceptionName = sa[0];
            this.exceptionComment = sa[1];

            /*
            if (!(holder instanceof ExecutableMemberDocImpl)) {
              Res.warning("tag.not_on_method", holder.toString(), this.text);
              exceptionClass = null;
        } 
            else {
              ExecutableMemberDocImpl emd = (ExecutableMemberDocImpl)holder;
              ClassDoc con = emd.containingClass();
              exceptionClass = con.findClass(exceptionName);
              if (exceptionClass == null) {
                  // may just not be in this run 
                  // Res.warning("tag.throws.exception_not_found",
                  //            emd.qualifiedName(), kind(), exceptionName);
              } 
              else {
                if (!isOK(exceptionClass, emd.thrownExceptions())) {
                    Res.warning("tag.throws.does_not_declare", emd.qualifiedName(), exceptionClass.qualifiedName());
                }
              }
        }
            */
        }

        /**
         * Return the exception name.
         */
        public String exceptionName() {
            return exceptionName;
        }

        /**
         * Return the exception comment.
         */
        public String exceptionComment() {
            return exceptionComment;
        }

        /**
         * Return the kind of this tag.  Always "@throws" for instances
         * of ThrowsTagImpl.
         */
        public String kind() {
            return "@throws"; // NOI18N
        }

        /**
         * Return the rawText for this tag 
         */
        public String toString() {
            return name + " " + exceptionName() + " " + exceptionComment(); // NOI18N
        }
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

    static class SerialField extends JavaDocTagMemoryImpl implements JavaDocTag.SerialField {

        private String fieldName;
        private String fieldType;
        private String description;


        SerialField( String name, String text ) {
            super( name, text );
            parseSerialFieldString( text );
        }


        /**
         * Return the serialziable field name.
         */
        public String fieldName() {
            return fieldName;
        }

        /**
         * Return the field type string.
         */
        public String fieldType() {
            return fieldType;
        }

        /**
         * Return the field comment. If there is no serialField comment, return
         * javadoc comment of corresponding FieldDoc.
         */
        public String description() {
            return description;
        }

        /**
         * Return the kind of this tag.
         */
        public String kind() {
            return "@serialField"; // NOI18N
        }

        /**
         * Return the rawText for this tag 
         */

        public String toString() {
            return name + " " + fieldName() + " " + fieldType() + " " + description(); // NOI18N
        }

        /*
        * The serialField tag is composed of three entities.
        *
        *   serialField  serializableFieldName serisliableFieldType
        *                 description of field.
        *
        * The fieldName and fieldType must be legal Java Identifiers.
        */
        private void parseSerialFieldString( String text) {

            int len = text.length();

            // if no white space found
            // Skip white space.
            int inx = 0;
            for (; inx < len && Character.isWhitespace(text.charAt(inx)); inx++) ;

            if ( inx == len ) {
                fieldName = ""; // NOI18N
                fieldType = ""; // NOI18N
                description = ""; // NOI18N
                return;
            }

            // Find first word.
            int first = inx;
            int last = inx;
            if (! Character.isJavaIdentifierStart(text.charAt(inx))) {
                this.Warning("tag.serialField.illegal_character", ""+ text.charAt(inx) + " " + text); // NOI18N
                return;
            }

            for (; inx < len && Character.isJavaIdentifierPart(text.charAt(inx)); inx++);

            if (inx < len && ! Character.isWhitespace(text.charAt(inx))) {
                this.Warning("tag.serialField.illegal_character",  ""+ text.charAt(inx) + " " + text); // NOI18N
                return;
            }

            last = inx;
            fieldName = text.substring(first, last);

            // Skip white space.
            for (; inx < len && Character.isWhitespace(text.charAt(inx)); inx++) ;

            // Find second word.
            first = inx;
            last = inx;

            for (; inx < len && ! Character.isWhitespace(text.charAt(inx)); inx++);
            if (inx < len && ! Character.isWhitespace(text.charAt(inx))) {
                this.Warning("tag.serialField.illegal_character", ""+ text.charAt(inx) + " " + text); // NOI18N
                return;
            }
            last = inx;
            fieldType = text.substring(first, last);

            // Skip leading white space. Rest of string is description for serialField.
            for (; inx < len && Character.isWhitespace(text.charAt(inx)); inx++) ;
            description = text.substring(inx);
        }

    }

}

/*
 * Log
 *  9    Gandalf   1.8         1/16/00  Ian Formanek    Removed semicolons after
 *       methods body to prevent fastjavac from complaining
 *  8    Gandalf   1.7         1/12/00  Petr Hamernik   i18n using perl script 
 *       (//NOI18N comments added)
 *  7    Gandalf   1.6         1/3/00   Petr Hrebejk    Bug fix 4740, 4975
 *  6    Gandalf   1.5         12/8/99  Petr Hamernik   compilable by Javac V8 
 *       (jdk1.3)
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/10/99 Petr Hamernik   console debug messages 
 *       removed.
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/7/99   Petr Hrebejk    Implementations made 
 *       package privet
 *  1    Gandalf   1.0         5/26/99  Petr Hrebejk    
 * $ 
 */ 