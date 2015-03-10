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

/** Represents a JavaDoc comment block.
*
* @author Jaroslav Tulach, Petr Hrebejk, Petr Hamernik
*/
public interface JavaDoc {

    /** Serves as second parameter in method {@link #changeTags}.
     */
    public static final int ADD = 1;

    /** Serves as second parameter in method {@link #changeTags}.
     */
    public static final int REMOVE = 2;

    /** Serves as second parameter in method {@link #changeTags}.
     */
    public static final int SET = 3;

    /** Get the entire text of the comment.
    * @return the whole text
    */
    public String getRawText ();

    /** Set the raw text of the comment.
    * @param s the whole text to set
    * @exception SourceException if the modification cannot be performed
    */
    public void setRawText (String s) throws SourceException;

    /** Get the actual text, cleared of all (non-inline) tags.
    * @return the plain text
    */
    public String getText ();

    /** Set the actual text.
    * @param s the actual text, without any (non-inline) tags
    * @exception SourceException if the modification cannot be performed
    */
    public void setText (String s) throws SourceException;

    /** Clears the javadoc from the source.
    */
    public void clearJavaDoc() throws SourceException;

    /** Test if this javadoc is empty.
    * @return true if it is not generated to the source.
    */
    public boolean isEmpty();

    /** Gets all tags from comment.
     */
    public JavaDocTag[] getTags();

    /** Gets all tags of given name
     */
    public JavaDocTag[] getTags( String name );

    /** Adds removes or sets tags used in this comment
     * @param elems the new initializers
     * @param action {@link #ADD}, {@link #REMOVE}, or {@link #SET}
     * @exception SourceException if impossible
     */
    public void changeTags( JavaDocTag[] tags, int action ) throws SourceException;

    /** Gets all @see tags
     */
    public JavaDocTag.See[] getSeeTags();

    /** The JavaDoc of a class.
    * Class javadoc adds no special tags.
    */
    public static interface Class extends JavaDoc {

        static final long serialVersionUID =3206093459760846163L;
    }

    /** The JavaDoc of a field.
    * <p>Currently adds special @SerialField tag
    */
    public static interface Field extends JavaDoc {
        /** Gets SerialField tags.
        */
        public JavaDocTag.SerialField[] getSerialFieldTags();

    }

    /** The JavaDoc of a method. Adds two special tags: @para tag and @throws tag.
    */
    public static interface Method extends JavaDoc {
        /** Gets param tags.
        */
        public JavaDocTag.Param[] getParamTags();

        /** Gets throws tags.
        */
        public JavaDocTag.Throws[] getThrowsTags();
    }

}

/*
* Log
*  8    src-jtulach1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    src-jtulach1.6         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  6    src-jtulach1.5         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  5    src-jtulach1.4         5/26/99  Petr Hrebejk    
*  4    src-jtulach1.3         5/7/99   Petr Hamernik   methods for remove 
*       javadoc
*  3    src-jtulach1.2         5/5/99   Petr Hrebejk    New JavaDoc API
*  2    src-jtulach1.1         3/30/99  Jesse Glick     [JavaDoc]
*  1    src-jtulach1.0         1/17/99  Jaroslav Tulach 
* $
*/
