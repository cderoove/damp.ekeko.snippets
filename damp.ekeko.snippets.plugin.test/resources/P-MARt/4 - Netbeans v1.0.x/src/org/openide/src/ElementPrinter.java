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

/** Prints elements in a textual form.
* For example code generators use this.
* <p>Contains three kinds of public members:
* <UL> 
* <LI> Constants to indicate events occurring while an element is printed,
*      e.g. {@link #ELEMENT_BEGIN} and {@link #ELEMENT_END}.
* <LI> Methods to print: {@link #print} and {@link #println}.
* <LI> A method for each kind of element, such as methods: {@link #markMethod}, etc.
*      notify about notable points inside the elements.
*      For example, if a printer implementation wants to print only
*      the header for a method, it may implement {@link #markMethod} to throw
*      {@link ElementPrinterInterruptException} to stop the printing there
*      when it encounters {@link #HEADER_END}.
* </UL>
*
* @author Petr Hamernik
*/
public interface ElementPrinter {
    /** Beginning of whole element. */
    public static final int ELEMENT_BEGIN = 0;
    /** End of whole element. */
    public static final int ELEMENT_END = 1;

    /** Beginning of JavaDoc comment (if any). */
    public static final int JAVADOC_BEGIN = 2;
    /** End of JavaDoc comment (if any). */
    public static final int JAVADOC_END = 3;

    /** Beginning of header.
    * For methods, constructors, and classes.
    */
    public static final int HEADER_BEGIN = 4;
    /** End of header.
    * For methods, constructors, and classes.
    */
    public static final int HEADER_END = 5;

    /** Beginning of body.
    * For initializers, methods, constructors, and classes.
    */
    public static final int BODY_BEGIN = 6;

    /** End of body.
    * For initializers, methods, constructors, and classes.
    */
    public static final int BODY_END = 7;

    /** Print some text.
    * @param text the text
    * @exception ElementPrinterInterruptException - see class description
    */
    public void print(String text) throws ElementPrinterInterruptException;

    /** Print a line of text with a newline.
    * @param text the text
    * @exception ElementPrinterInterruptException - see class description
    */
    public void println(String text) throws ElementPrinterInterruptException;

    /** Mark a notable point in a class element.
    * @param element the element
    * @param what which point
    * @exception ElementPrinterInterruptException - see class description
    */
    public void markClass(ClassElement element, int what)
    throws ElementPrinterInterruptException;

    /** Mark a notable point in a initializer element.
    * @param element the element
    * @param what which point
    * @exception ElementPrinterInterruptException - see class description
    */
    public void markInitializer(InitializerElement element, int what)
    throws ElementPrinterInterruptException;

    /** Mark a notable point in a field element.
    * @param element the element
    * @param what which point
    * @exception ElementPrinterInterruptException - see class description
    */
    public void markField(FieldElement element, int what)
    throws ElementPrinterInterruptException;

    /** Mark a notable point in a constructor element.
    * @param element the element
    * @param what which point
    * @exception ElementPrinterInterruptException - see class description
    */
    public void markConstructor(ConstructorElement element, int what)
    throws ElementPrinterInterruptException;

    /** Mark a notable point in a method element.
    * @param element the element
    * @param what which point
    * @exception ElementPrinterInterruptException - see class description
    */
    public void markMethod(MethodElement element, int what)
    throws ElementPrinterInterruptException;
}

/*
 * Log
 *  4    src-jtulach1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    src-jtulach1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    src-jtulach1.1         3/30/99  Jesse Glick     [JavaDoc]
 *  1    src-jtulach1.0         3/22/99  Petr Hamernik   
 * $
 */
