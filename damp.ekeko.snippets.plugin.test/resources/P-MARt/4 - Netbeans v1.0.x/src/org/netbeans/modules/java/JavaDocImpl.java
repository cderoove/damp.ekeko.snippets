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

package org.netbeans.modules.java;

import java.util.StringTokenizer;

import org.openide.src.*;

/** Implementation of JavaDoc comment blocks in Java loader.
*
* @author Petr Hamernik, Petr Hrebejk
*/
class JavaDocImpl extends Object implements JavaDoc {

    /** Holds the memory implementation of JavaDoc */
    JavaDoc javaDoc;

    /** The element which this javadoc belongs to.
    * It is called when javadoc changed and should be regenerated
    */
    ElementImpl impl;

    /** Flag for the empty javadoc (element has NO javadoc
    * in the source code)
    */
    //boolean empty;


    JavaDocImpl( ElementImpl impl ) {
        this.impl = impl;
    }

    /** Creates new JavaDoc
    * @param rawText the pre-parsed text of the javadoc comment
    *         provided by the JavaParser
    * @param impl The implementation
    */
    JavaDocImpl(String rawText, ElementImpl impl) {
        javaDoc = JavaDocSupport.createJavaDoc( rawText );
        this.impl = impl;
    }

    public void clearJavaDoc() throws SourceException {
        javaDoc.clearJavaDoc();
    }

    public boolean isEmpty() {
        return javaDoc.isEmpty();
    }

    /** Get the entire text of the comment.
    * @return the whole text
    */

    public String getRawText () {
        return javaDoc.getRawText();
    }


    /** Set the raw text of the comment.
    * @param s the whole text to set
    * @exception SourceException if the modification cannot be performed
    */
    public void setRawText (String s) throws SourceException {
        javaDoc.setRawText( s );
        updateTags();
        regenerateSource();
    }

    /** Updates internal structures holding the tags... */
    private void updateTags() {
        //PENDING
    }

    /** Get the actual text, cleared of all (non-inline) tags.
    * @return the plain text
    */

    public String getText () {
        return javaDoc.getText();
    }


    /** Set the actual text.
    * @param s the actual text, without any (non-inline) tags
    * @exception SourceException if the modification cannot be performed
    */
    public void setText (String s) throws SourceException {
        javaDoc.setText( s );
        regenerateSource();
        //throw new SourceException();
    }

    /** Gets all tags from comment.
     */

    public JavaDocTag[] getTags() {
        return javaDoc.getTags();
    }

    /** Gets all tags of given name
     */


    public JavaDocTag[] getTags(String name) {
        return javaDoc.getTags( name );
    }

    /** Adds removes or sets tags used in this comment
     * @param elems the new initializers
     * @param action {@link #ADD}, {@link #REMOVE}, or {@link #SET}
     * @exception SourceException if impossible
     */
    public void changeTags(JavaDocTag[] tags, int action) throws SourceException {
        javaDoc.changeTags( tags, action );
        regenerateSource();
        //throw new SourceException();
    }

    /** Gets all @see tags
     */

    public JavaDocTag.See[] getSeeTags() {
        return javaDoc.getSeeTags();
    }

    /** Regenerates the text in the element.
    * @exception SourceException if the modification cannot be performed
    */
    public void regenerateSource() throws SourceException {
        impl.regenerateJavaDoc();
    }

    /** The JavaDoc of a class.
    * Class javadoc adds no special tags.
    */
    static class Class extends JavaDocImpl implements JavaDoc.Class {
        public Class(String rawText, ElementImpl impl) {
            super( impl );
            javaDoc = JavaDocSupport.createClassJavaDoc( rawText );
        }
    }

    /** The JavaDoc of a field.
    * <p>Currently adds special @SerialField tag
    */
    static class Field extends JavaDocImpl implements JavaDoc.Field {

        public Field(String rawText, ElementImpl impl) {
            super( impl );
            javaDoc = JavaDocSupport.createFieldJavaDoc( rawText );
        }

        /** Gets SerialField tags.
        */
        public JavaDocTag.SerialField[] getSerialFieldTags() {
            return  ((JavaDoc.Field)javaDoc).getSerialFieldTags();
        }

    }

    /** The JavaDoc of a method. Adds two special tags: @param tag and @throws tag.
    */
    static class Method extends JavaDocImpl implements JavaDoc.Method {

        public Method(String rawText, ConstructorElementImpl impl) {
            super( impl );
            javaDoc = JavaDocSupport.createMethodJavaDoc( rawText );
        }

        /** Gets param tags.
        */
        public JavaDocTag.Param[] getParamTags() {
            return  ((JavaDoc.Method)javaDoc).getParamTags();
        }

        /** Gets throws tags.
        */
        public JavaDocTag.Throws[] getThrowsTags() {
            return  ((JavaDoc.Method)javaDoc).getThrowsTags();
        }
    }
}

/*
* Log
*  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    Gandalf   1.6         8/27/99  Petr Hrebejk    ChangeTag function fixed
*  6    Gandalf   1.5         8/27/99  Petr Hrebejk    Implementation of 
*       ChangeTags method
*  5    Gandalf   1.4         7/26/99  Petr Hrebejk    Method comment fix
*  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  3    Gandalf   1.2         6/7/99   Petr Hamernik   public -> package private
*  2    Gandalf   1.1         5/26/99  Petr Hrebejk    
*  1    Gandalf   1.0         5/10/99  Petr Hamernik   
* $
*/
