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

import java.io.PrintWriter;

/** A trivial implementation of <code>ElementPrinter</code>.
* It is backed by the supplied <code>PrintWriter</code>,
* and by default just prints the text as supplied to that print
* writer.
* It does nothing for any of the mark methods, and never
* throws {@link ElementPrinterInterruptException}.
* Subclasses may use this as an adapter for <code>ElementPrinter</code>,
* typically providing a nontrivial body for one of the mark methods.
*
* @author Petr Hamernik
*/
public class DefaultElementPrinter implements ElementPrinter {
    /** The underlaying writer. */
    private PrintWriter writer;

    /** Create a printer.
    * @param writer the writer to send printed text to
    */
    public DefaultElementPrinter(PrintWriter writer) {
        this.writer = writer;
    }

    /* Prints the given text.
    * @param text The text to write
    */
    public void print(String text) {
        writer.print(text);
    }

    /* Prints the line. New-line character '\n' should be added.
    * @param text The line to write
    */
    public void println(String text) {
        writer.println(text);
    }

    /* Marks the notable point of the class element.
    * @param element The element.
    * @param what The kind of the event. It must be one of the integer
    *             constants from this interface
    */
    public void markClass(ClassElement element, int what) throws ElementPrinterInterruptException {
    }

    /* Marks the notable point of the initializer element.
    * @param element The element.
    * @param what The kind of the event. It must be one of the integer
    *             constants from this interface
    */
    public void markInitializer(InitializerElement element, int what) throws ElementPrinterInterruptException {
    }

    /* Marks the notable point of the field element.
    * @param element The element.
    * @param what The kind of the event. It must be one of the integer
    *             constants from this interface
    */
    public void markField(FieldElement element, int what) throws ElementPrinterInterruptException {
    }

    /* Marks the notable point of the constructor element.
    * @param element The element.
    * @param what The kind of the event. It must be one of the integer
    *             constants from this interface
    */
    public void markConstructor(ConstructorElement element, int what) throws ElementPrinterInterruptException {
    }

    /* Marks the notable point of the method element.
    * @param element The element.
    * @param what The kind of the event. It must be one of the integer
    *             constants from this interface
    */
    public void markMethod(MethodElement element, int what) throws ElementPrinterInterruptException {
    }
}

/*
 * Log
 *  5    src-jtulach1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    src-jtulach1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    src-jtulach1.2         4/30/99  Jesse Glick     Made public. Also 
 *       declared to throw exceptions so that it can be subclassed by someone 
 *       who will.
 *  2    src-jtulach1.1         3/30/99  Petr Hamernik   
 *  1    src-jtulach1.0         3/22/99  Petr Hamernik   
 * $
 */
