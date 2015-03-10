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

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/** Describes the constructor of a class.
*
* @author Petr Hamernik
*/
public class ConstructorElement extends MemberElement {
    /** Format for the header - used in code generator */
    private static final ElementFormat HEADER_FORMAT =
        new ElementFormat("{m,,\" \"}{n}({a,,,\",\"}){e,\" throws \",}"); // NOI18N

    static final long serialVersionUID =4794977239847390179L;
    /** Create a constructor with an in-memory implementation. */
    public ConstructorElement() {
        this(new Memory(), null);
    }

    /** Create a constructor.
    * @param impl implementation of functionality
    * @param clazz declaring class, or <code>null</code>
    */
    public ConstructorElement(ConstructorElement.Impl impl, ClassElement clazz) {
        super(impl, clazz);
    }

    /** Clone the constructor.
    * @return a new constructor that has same values as the original,
    *   but is represented in memory
    */
    public Object clone () {
        return new ConstructorElement (new Memory (this), null);
    }

    final ConstructorElement.Impl getConstructorImpl() {
        return (ConstructorElement.Impl)impl;
    }

    /* Get the modifiers for this constructor.
     * @return the mask of modifers
     * @see Modifier
     */
    public int getModifiersMask() {
        return Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;
    }

    /** Get the method parameters.
    * @return the parameters
    */
    public final MethodParameter[] getParameters() {
        return getConstructorImpl().getParameters();
    }

    /** Set the method parameters.
    * @param params the new parameters
    * @throws SourceException if impossible
    */
    public final void setParameters(MethodParameter[] params) throws SourceException {
        getConstructorImpl().setParameters(params);
    }

    /** Get the thrown exceptions.
     * @return the exceptions, by name
    */
    public final Identifier[] getExceptions() {
        return getConstructorImpl().getExceptions();
    }

    /** Set the array of thrown exceptions.
    * @param exceptions the new exceptions to throw, by name
    * @throws SourceException if impossible
    */
    public final void setExceptions(Identifier[] exceptions) throws SourceException {
        getConstructorImpl().setExceptions(exceptions);
    }

    /** Set the body of the constructor.
    * @param s the new body (may be <code>null</code> for methods)
    * @throws SourceException if impossible
    * @see #getBody
    */
    public final void setBody (String s) throws SourceException {
        getConstructorImpl ().setBody (s);
    }

    /** Get the body of the constructor.
    * If <code>this</code> is actually a {@link MethodElement}, the body
    * may be <code>null</code> when the method is abstract.
    * A body consisting of an empty string, however, is just a concrete
    * but empty body.
    * @return the body (maye be <code>null</code> for methods)
    */
    public final String getBody () {
        return getConstructorImpl ().getBody ();
    }

    /** Get this constructor's documentation.
    * @return the JavaDoc
    */
    public final JavaDoc.Method getJavaDoc () {
        return getConstructorImpl ().getJavaDoc ();
    }

    /* Print this element into the element printer.
    * @param printer The element printer where to print to
    * @exception ElementPrinterInterruptException if printer cancel the printing
    */
    public void print(ElementPrinter printer) throws ElementPrinterInterruptException {
        printerMark(printer, printer.ELEMENT_BEGIN);

        JavaDoc doc = getJavaDoc();
        if ((doc != null) && !doc.isEmpty()) {
            printerMark(printer, printer.JAVADOC_BEGIN); // JAVADOC begin
            printJavaDoc(doc, printer);
            printerMark(printer, printer.JAVADOC_END); // JAVADOC end
            printer.println(""); // NOI18N
        }

        printerMark(printer, printer.HEADER_BEGIN); // HEADER begin
        printer.print(getFormat().format(this));
        printerMark(printer, printer.HEADER_END); // HEADER end

        String body = getBody();
        ClassElement declClass = getDeclaringClass();

        if (((declClass != null) && declClass.isInterface()) || // in interface
                (Modifier.isAbstract(getModifiers())) || // or abstract
                (body == null)) { // body is null
            printer.print(";"); // NOI18N
        }
        else {
            printer.print(" {"); // NOI18N
            printerMark(printer, printer.BODY_BEGIN); // BODY begin
            printer.print(body);
            printerMark(printer, printer.BODY_END); // BODY end
            printer.print("}"); // NOI18N
        }

        printerMark(printer, printer.ELEMENT_END);
    }

    /** Marks the notable point in the writer.
    * This method calls markConstructor and must be overriden
    * in MethodElement to call markMethod.
    * @exception ElementPrinterInterruptException if printer cancel the printing
    */
    void printerMark(ElementPrinter printer, int what) throws ElementPrinterInterruptException {
        printer.markConstructor(this, what);
    }

    /** Get the printing format.
    * May be overridden in subclasses.
    */
    ElementFormat getFormat() {
        return HEADER_FORMAT;
    }

    /** Implementation of constructors.
    * @see ConstructorElement
    */
    public interface Impl extends MemberElement.Impl {
        static final long serialVersionUID =-8757076629808175158L;
        /** Get the method parameters.
        * @return the parameters
        */
        public MethodParameter[] getParameters();

        /** Set the method parameters.
        * @param params the new parameters
        * @throws SourceException if impossible
        */
        public void setParameters(MethodParameter[] params) throws SourceException;

        /** Get the thrown exceptions.
        * @return the exceptions, by name
        */
        public Identifier[] getExceptions();

        /** Set the thrown exceptions.
        * @param exceptions the new exceptions to be thrown, by name
        * @throws SourceException if impossible
        */
        public void setExceptions(Identifier[] exceptions) throws SourceException;

        /** Set the body.
        * @param s the new body (may be <code>null</code> for methods)
        * @throws SourceException if impossible
        * @see ConstructorElement#getBody
        */
        public void setBody (String s) throws SourceException;

        /** Get the body.
        * @return the body (may be <code>null</code> for methods)
        * @see ConstructorElement#getBody
        */
        public String getBody ();

        /** Get the JavaDoc.
        * @return the JavaDoc
        */
        public JavaDoc.Method getJavaDoc ();
    }

    /** Serves as a key for constructor elements.
    * Enables them to be used in hashtables, etc.
    * @see ConstructorElement
    */
    public static class Key extends Object {
        /** Parameter types */
        private Type[] params;

        /** Construct a key by parameter types.
        * @param params the parameter types
        */
        public Key (final Type[] params) {
            this.params = params;
        }

        /** Construct a key for a constructor.
        * Does not keep a reference.
        * @param ce the constructor
        */
        public Key (final ConstructorElement ce) {
            MethodParameter[] mp = ce.getParameters();
            params = new Type[mp.length];
            for (int i = 0; i < mp.length; i++) {
                params[i] = mp[i].getType();
            }
        }

        /* Returns true if parameters are the same */
        public boolean equals (Object obj) {
            if (!(obj instanceof Key)) return false;
            return Arrays.equals(params, ((Key)obj).params);
        }

        /* Computes hashcode as exclusive or of first and
        * last parameter's names
        * (or only from the first or return some constant
        * for special cases) */
        public int hashCode () {
            int length = params.length;
            if (length == 0) return 0;
            if (length == 1) return params[0].toString().hashCode();
            return params[0].toString().hashCode() ^
                   params[length - 1].toString().hashCode();
        }

    } // end of Key inner class

    static class Memory extends MemberElement.Memory implements Impl {
        /** arguments of the constructor or method */
        private MethodParameter[] parameters;

        /** exceptions throwed by the constructor or method */
        private Identifier[] exceptions;

        /** body */
        private String body;

        /** Java Doc */
        private JavaDoc.Method javaDoc;

        static final long serialVersionUID =-4826478874004410760L;
        Memory() {
            exceptions = new Identifier[0];
            parameters = new MethodParameter[0];
            body = ""; // NOI18N
            javaDoc = JavaDocSupport.createMethodJavaDoc( null );
        }

        /** Copy constructor */
        Memory (ConstructorElement el) {
            super (el);
            exceptions = el.getExceptions ();
            parameters = el.getParameters ();
            body = el.getBody ();
            javaDoc = el.getJavaDoc().isEmpty() ?
                      JavaDocSupport.createMethodJavaDoc( null ) :
                      JavaDocSupport.createMethodJavaDoc( el.getJavaDoc().getRawText() );
        }

        /** @return the parameters
        */
        public MethodParameter[] getParameters() {
            return parameters;
        }

        /** sets the method parameters
        */
        public void setParameters(MethodParameter[] params) {
            MethodParameter[] m = parameters;
            parameters = params;
            firePropertyChange (PROP_PARAMETERS, m, params);
        }

        /** @return the array of the exceptions throwed by the method.
        */
        public Identifier[] getExceptions() {
            return exceptions;
        }

        /** Sets the array of the exceptions throwed by the method.
        */
        public void setExceptions(Identifier[] exceptions) {
            Identifier[] old = this.exceptions;
            this.exceptions = exceptions;
            firePropertyChange (PROP_EXCEPTIONS, old, exceptions);
        }

        /** Sets body of the element.
        * @param s the body
        */
        public void setBody (String s) throws SourceException {
            String old = body;
            body = s;
            firePropertyChange (PROP_BODY, old, body);
        }

        /** Getter for the body of element.
        * @return the string representing the body
        */
        public String getBody () {
            return body;
        }

        /** Provides access to constructor java doc.
        * @return constructor java doc
        */
        public JavaDoc.Method getJavaDoc () {
            return javaDoc;
        }

        public Object readResolve() {
            return new ConstructorElement(this, null);
        }
    }
}

/*
 * Log
 *  28   Gandalf-post-FCS1.26.2.0    4/18/00  Svatopluk Dedic PropertyChange event 
 *       firing improved
 *  27   src-jtulach1.26        1/12/00  Petr Hamernik   i18n using perl script 
 *       (//NOI18N comments added)
 *  26   src-jtulach1.25        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  25   src-jtulach1.24        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  24   src-jtulach1.23        8/6/99   Petr Hamernik   roll back
 *  23   src-jtulach1.22        8/6/99   Petr Hamernik   better formating of 
 *       generated text
 *  22   src-jtulach1.21        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  21   src-jtulach1.20        6/7/99   Petr Hrebejk    Memory implementations 
 *       added to memory implementations of elements
 *  20   src-jtulach1.19        5/14/99  Jesse Glick     [JavaDoc]
 *  19   src-jtulach1.18        5/10/99  Petr Hamernik   javadoc & printing 
 *       improved
 *  18   src-jtulach1.17        4/30/99  Jesse Glick     [JavaDoc]
 *  17   src-jtulach1.16        4/14/99  Petr Hamernik   printing bug fixed
 *  16   src-jtulach1.15        4/14/99  Petr Hamernik   fixed bug #1522
 *  15   src-jtulach1.14        3/30/99  Petr Hamernik   getFormat is package 
 *       private
 *  14   src-jtulach1.13        3/30/99  Jesse Glick     [JavaDoc]
 *  13   src-jtulach1.12        3/23/99  Petr Hamernik   
 *  12   src-jtulach1.11        3/22/99  Petr Hamernik   printing changed
 *  11   src-jtulach1.10        3/15/99  Petr Hamernik   
 *  10   src-jtulach1.9         2/17/99  Petr Hamernik   serialization changed.
 *  9    src-jtulach1.8         2/8/99   Petr Hamernik   
 *  8    src-jtulach1.7         2/3/99   David Simonek   
 *  7    src-jtulach1.6         2/3/99   David Simonek   little fixes
 *  6    src-jtulach1.5         1/22/99  David Simonek   Key innerclasses 
 *       added...
 *  5    src-jtulach1.4         1/19/99  Jaroslav Tulach 
 *  4    src-jtulach1.3         1/19/99  Jaroslav Tulach 
 *  3    src-jtulach1.2         1/19/99  Jaroslav Tulach 
 *  2    src-jtulach1.1         1/18/99  David Simonek   property constants added
 *  1    src-jtulach1.0         1/17/99  Jaroslav Tulach 
 * $
 */
