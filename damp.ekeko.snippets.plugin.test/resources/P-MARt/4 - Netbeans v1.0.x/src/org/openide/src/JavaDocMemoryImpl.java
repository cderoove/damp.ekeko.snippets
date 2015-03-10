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

import java.util.List;
import java.util.ArrayList;

import org.openide.src.SourceException;
import org.openide.src.JavaDoc;
import org.openide.src.JavaDocTag;

/** Represents a JavaDoc comment block.
*
* @author Jaroslav Tulach, Petr Hrebejk, Petr Hamernik
*/
class JavaDocMemoryImpl implements JavaDoc {

    protected String rawText;

    private   String text;

    // PENDING - clear this

    private static final JavaDocTagMemoryImpl[] EMPTY_ARRAY = new JavaDocTagMemoryImpl[] {};


    /** Constructs the JavaDoc object held in memory. Parses the tags from rawText
     */

    public JavaDocMemoryImpl( String rawText ) {
        this.rawText =  rawText;
    }

    /** Get the entire text of the comment.
    * @return the whole text
    */
    public String getRawText () {
        return rawText;
    }

    /** Set the raw text of the comment.
    * @param s the whole text to set
    * @exception SourceException if the modification cannot be performed
    */
    public void setRawText (String s) throws SourceException {
        rawText = s;
    }

    /** Get the actual text, cleared of all (non-inline) tags.
    * @return the plain text
    */
    public String getText () {
        if ( rawText == null )
            return ""; // NOI18N
        List tmp = new ArrayList();
        parseComment( tmp );
        return text;
    }

    /** Set the actual text.
    * @param s the actual text, without any (non-inline) tags
    * @exception SourceException if the modification cannot be performed
    */
    public void setText (String s) throws SourceException {
        regenerateRawText( s, getTags(), getSeeTags() );
    }

    /** Clears the javadoc from the source.
    */
    public void clearJavaDoc() throws SourceException {
        rawText = null;
    }

    /** Test if this javadoc is empty.
    * @return true if it is not generated to the source.
    */
    public boolean isEmpty() {
        return rawText == null;
    }

    /** Gets all tags from comment.
     */
    public JavaDocTag[] getTags() {
        if ( rawText == null )
            return EMPTY_ARRAY;

        List tagList = new ArrayList();

        parseComment( tagList );

        JavaDocTag[] tagArray = new JavaDocTag[ tagList.size() ];
        tagList.toArray( tagArray );

        return tagArray;
    }

    /** Gets all tags of given name
     */
    public JavaDocTag[] getTags( String name ) {
        JavaDocTag[] allTags = getTags();
        ArrayList resultList = new ArrayList( allTags.length );

        for( int i = 0; i < allTags.length; i++ ) {
            if ( allTags[i].name().equals( name ) )
                resultList.add( allTags[i] );
        }

        JavaDocTag result[] = new JavaDocTag[ resultList.size() ];
        resultList.toArray( result );
        return result;
    }

    /** Adds removes or sets tags used in this comment
     * @param elems the new initializers
     * @param action {@link #ADD}, {@link #REMOVE}, or {@link #SET}
     * @exception SourceException if impossible
     */
    synchronized public void  changeTags( JavaDocTag[] tags, int action ) throws SourceException {

        StringBuffer sb = new StringBuffer();

        switch ( action ) {
        case ADD:
        case SET:
            sb.append( action == ADD ? getRawText() : getText() );
            for( int i = 0; i < tags.length; i++ ) {
                sb.append( "\n" ).append( tags[i].toString() ); // NOI18N
            }
            setRawText( sb.toString() );
            break;

        case REMOVE:
            JavaDocTag currTags[] = getTags();
            sb.append( getText() );

            for( int i = 0; i < currTags.length; i++ ) {
                boolean found = false;
                String strTag = currTags[i].toString();
                for( int j = 0; j < tags.length; j ++ ) {
                    if ( strTag.equals( tags[j].toString() ) ) {
                        found = true;
                        break;
                    }
                }
                if ( !found )
                    sb.append( "\n" ).append( strTag ); // NOI18N
            }
            setRawText( sb.toString() );
            break;
        }
    }

    /** Gets all @see tags
     */
    public JavaDocTag.See[] getSeeTags() {

        JavaDocTag[] allTags = getTags();
        ArrayList resultList = new ArrayList( allTags.length );

        for( int i = 0; i < allTags.length; i++ ) {
            if ( allTags[i] instanceof JavaDocTag.See )
                resultList.add( allTags[i] );
        }

        JavaDocTag.See result[] = new JavaDocTag.See[ resultList.size() ];
        resultList.toArray( result );
        return result;
    }


    /** Regenerates the rawText form tags
     */
    protected void regenerateRawText( String text, JavaDocTag[] tags, JavaDocTag.See[] seeTags ) {
        StringBuffer sb = new StringBuffer( text.length() + tags.length * 80 + seeTags.length * 80 );

        sb.append( text );

        for (int i = 0; i < tags.length; i++ ) {
            sb.append( tags[i].toString() );
        }

        for (int i = 0; i < seeTags.length; i++ ) {
            sb.append( seeTags[i].toString() );
        }

        rawText = sb.toString();
    }


    /** The JavaDoc of a class.
    * Class javadoc adds no special tags.
    */
    static class Class extends JavaDocMemoryImpl implements JavaDoc.Class {
        static final long serialVersionUID =3206093459760846163L;
        Class( String rawText ) {
            super( rawText );
        }
    }

    /** The JavaDoc of a field.
    * <p>Currently adds special @SerialField tag
    */
    static class Field extends JavaDocMemoryImpl implements JavaDoc.Field {

        Field ( String rawText ) {
            super( rawText );
        }

        /** Gets SerialField tags.
        */
        public JavaDocTag.SerialField[] getSerialFieldTags() {
            JavaDocTag[] allTags = this.getTags();
            ArrayList resultList = new ArrayList( allTags.length );

            for( int i = 0; i < allTags.length; i++ ) {
                if ( allTags[i] instanceof JavaDocTag.SerialField )
                    resultList.add( allTags[i] );
            }

            JavaDocTag.SerialField result[] = new JavaDocTag.SerialField[ resultList.size() ];
            resultList.toArray( result );
            return result;
        }

    }

    /** The JavaDoc of a method. Adds two special tags: @para tag and @throws tag.
    */
    static class Method extends JavaDocMemoryImpl implements JavaDoc.Method {

        Method ( String rawText ) {
            super( rawText );
        }

        /** Gets param tags.
        */
        public JavaDocTag.Param[] getParamTags() {
            JavaDocTag[] allTags = this.getTags();
            ArrayList resultList = new ArrayList( allTags.length );

            for( int i = 0; i < allTags.length; i++ ) {
                if ( allTags[i] instanceof JavaDocTag.Param )
                    resultList.add( allTags[i] );
            }

            JavaDocTag.Param result[] = new JavaDocTag.Param[ resultList.size() ];
            resultList.toArray( result );
            return result;
        }


        /** Gets throws tags.
        */
        public JavaDocTag.Throws[] getThrowsTags() {
            JavaDocTag[] allTags = this.getTags();
            ArrayList resultList = new ArrayList( allTags.length );

            for( int i = 0; i < allTags.length; i++ ) {
                if ( allTags[i] instanceof JavaDocTag.Throws )
                    resultList.add( allTags[i] );
            }

            JavaDocTag.Throws result[] = new JavaDocTag.Throws[ resultList.size() ];
            resultList.toArray( result );
            return result;
        }
    }

    // PRIVATE & UTILITY METHODS ----------------------------------------------------------

    /**
    * Parses the rawText and generates list of tags;
    */

    private void parseComment( List tagList ) {

        final int IN_TEXT = 1;
        final int TAG_GAP = 2;
        final int TAG_NAME = 3;

        int state = TAG_GAP;

        boolean newLine = true;

        String tagName = null;

        int tagStart = 0;
        int textStart = 0;
        int lastNonWhite = -1;
        int len = rawText.length();

        for (int inx = 0; inx < len; ++inx) {

            char ch = rawText.charAt(inx);
            boolean isWhite = Character.isWhitespace(ch);

            switch (state)  {
            case TAG_NAME:
                if (isWhite) {
                    tagName = rawText.substring(tagStart, inx);
                    state = TAG_GAP;
                }
                break;
            case TAG_GAP:
                if (isWhite) {
                    break;
                }
                textStart = inx;
                state = IN_TEXT;
                /* fall thru */
            case IN_TEXT:
                if (newLine && ch == '@') {
                    parseCommentComponent(tagList, tagName, textStart, lastNonWhite+1);
                    tagStart = inx;
                    state = TAG_NAME;
                }
                break;
            };


            if (ch == '\n') {
                newLine = true;
            }
            else if (!isWhite) {
                lastNonWhite = inx;
                newLine = false;
            }
        }

        // Finish what's currently being processed
        switch (state)  {
        case TAG_NAME:
            tagName = rawText.substring(tagStart, len);
            /* fall thru */
        case TAG_GAP:
            textStart = len;
            /* fall thru */
        case IN_TEXT:
            parseCommentComponent( tagList, tagName, textStart, lastNonWhite+1 );
            break;
        };

    }

    /**
     * Parses the tag.
     * Saves away the last parsed item.
     */
    private void parseCommentComponent( List tagList, String tagName, int from, int upto) {
        String tx = upto <= from ? "" : rawText.substring(from, upto); // NOI18N
        if (tagName == null) {
            text = tx;
        }
        else {
            JavaDocTagMemoryImpl tag;
            if (tagName.equals("@exception") || tagName.equals("@throws")) { // NOI18N
                warnIfEmpty(tagName, tx);
                tag = new JavaDocTagMemoryImpl.Throws(tagName, tx);
            }
            else if (tagName.equals("@param")) { // NOI18N
                warnIfEmpty(tagName, tx);
                tag = new JavaDocTagMemoryImpl.Param(tagName, tx);
            }
            else if (tagName.equals("@see")) { // NOI18N
                warnIfEmpty( tagName, tx);
                tag = new JavaDocTagMemoryImpl.See(tagName, tx);
            }
            else if (tagName.equals("@serialField")) { // NOI18N
                warnIfEmpty( tagName, tx);
                tag = new JavaDocTagMemoryImpl.SerialField(tagName, tx);
            }
            else if (tagName.equals("@return")) { // NOI18N
                warnIfEmpty(tagName, tx);
                tag = new JavaDocTagMemoryImpl(tagName, tx);
            }
            else if (tagName.equals("@author")) { // NOI18N
                warnIfEmpty(tagName, tx);
                tag = new JavaDocTagMemoryImpl(tagName, tx);
            }
            else if (tagName.equals("@version")) { // NOI18N
                warnIfEmpty( tagName, tx);
                tag = new JavaDocTagMemoryImpl(tagName, tx);
            }
            else {
                tag = new JavaDocTagMemoryImpl(tagName, tx);
            }
            tagList.add(tag);
        }
    }


    // PENDING : REMOVE THIS METHOD
    private void warnIfEmpty( String tagName, String tx) {
        /*
        if (tx.length() == 0) {
          System.out.println("tag.tag_has_no_arguments" + tagName);
    }
        */
    }
}


/*
 * Log
 *  9    Gandalf   1.8         1/16/00  Ian Formanek    Removed semicolons after
 *       methods body to prevent fastjavac from complaining
 *  8    Gandalf   1.7         1/12/00  Petr Hamernik   i18n using perl script 
 *       (//NOI18N comments added)
 *  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         8/17/99  Petr Hrebejk    changeTags meyhod 
 *       implemented
 *  5    Gandalf   1.4         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  4    Gandalf   1.3         7/26/99  Petr Hrebejk    getParamTags, 
 *       getThrowsTag, ....  methods implemented
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/7/99   Petr Hrebejk    Implementations made 
 *       package privet
 *  1    Gandalf   1.0         5/26/99  Petr Hrebejk    
 * $ 
 */ 