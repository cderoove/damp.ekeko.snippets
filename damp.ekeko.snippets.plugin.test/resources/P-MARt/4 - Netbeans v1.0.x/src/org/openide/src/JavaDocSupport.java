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


/** JavaDocSupport singleton which serves as source of memory implementations
 *  of JavaDoc objects and JavaDocTags
 *  @author Petr Hrebejk
 */
public class JavaDocSupport extends Object {

    /** The class is singleton. */
    private JavaDocSupport() {
    }

    /** Creates new instance of memory implementation of JavaDoc
     * interface.
     * @param text Raw text of JavaDoc comment.
     * @return Instance of memory implementation of the JavaDoc interface.
     */
    public static JavaDoc createJavaDoc(String text) {
        return new JavaDocMemoryImpl( text );
    }

    /** Creates new instance of memory implementation of JavaDoc.Class
     * interface.
     * @param text Raw text of JavaDoc comment.
     * @return Instance of memory implementation of the JavaDoc.Class interface.
     */
    public static JavaDoc.Class createClassJavaDoc( String text ) {
        return new JavaDocMemoryImpl.Class( text );
    }

    /** Creates new instance of memory implementation of JavaDoc.Field
     * interface.
     * @param text Raw text of JavaDoc comment.
     * @return Instance of memory implementation of the JavaDoc.Field interface.
     */
    public static JavaDoc.Field createFieldJavaDoc( String text ) {
        return new JavaDocMemoryImpl.Field( text );
    }

    /** Creates new instance of memory implementation of JavaDoc.Method
     * interface.
     * @param text Raw text of JavaDoc comment.
     * @return Instance of memory implementation of the JavaDoc.Method interface.
     */
    public static JavaDoc.Method createMethodJavaDoc( String text ) {
        return new JavaDocMemoryImpl.Method( text );
  }
  
  /** Creates a new instance of memory implementation of JavaDoc interface.
   *  @param text Raw contents of the comment
   *  @return Instance of memory implementation of the JavaDoc interface.
  */
  public static JavaDoc createInitializerJavaDoc( String text ) {
    return new JavaDocMemoryImpl( text );
    }

    /** Creates new instance of memory implementation of JavaDocTag
     * interface.
     * @param name Name of the tag.
     * @param text Text of the tag.
     * @return Instance of memory implementation of the JavaDocTag interface.
     */
    public static JavaDocTag createTag( String name, String text ) {
        return new JavaDocTagMemoryImpl( name, text );
    }

    /** Creates new instance of memory implementation of JavaDocTag.See
     * interface.
     * @param name Name of the tag.
     * @param text Text of the tag to parse.
     * @return Instance of memory implementation of the JavaDocTag.See interface.
     */
    public static JavaDocTag.See createSeeTag( String name, String text ) {
        return new JavaDocTagMemoryImpl.See( name, text );
    }

    /** Creates new instance of memory implementation of JavaDocTag.Param
     * interface.
     * @param name Name of the tag.
     * @param text Text of the tag to parse.
     * @return Instance of memory implementation of the JavaDocTag.Param interface.
     */
    public static JavaDocTag.Param createParamTag( String name, String text ) {
        return new JavaDocTagMemoryImpl.Param( name, text );
    }

    /** Creates new instance of memory implementation of JavaDocTag.Throws
    * interface.
    * @param name Name of the tag.
    * @param text Text of the tag to parse.
    * @return Instance of memory implementation of the JavaDocTag.Throws interface.
    */
    public static JavaDocTag.Throws createThrowsTag( String name, String text ) {
        return new JavaDocTagMemoryImpl.Throws( name, text );
    }

    /** Creates new instance of memory implementation of JavaDocTag.SerialField
     * interface.
     * @param name Name of the tag.
     * @param text Text of the tag to parse.
     * @return Instance of memory implementation of the JavaDocTag.SerialField interface.
     */
    public static JavaDocTag.SerialField createSerialFieldTag( String name, String text ) {
        return new JavaDocTagMemoryImpl.SerialField( name, text );
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/8/99   Petr Hrebejk    Comments & private 
 *       constructor
 *  1    Gandalf   1.0         5/26/99  Petr Hrebejk    
 * $ 
 */ 
