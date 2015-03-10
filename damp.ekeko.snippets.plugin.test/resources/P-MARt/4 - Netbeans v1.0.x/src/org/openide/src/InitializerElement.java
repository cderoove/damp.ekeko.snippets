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

/** Element which represents an initializer block.
* This may be a static class initializer, or (as of Java 1.1)
* a nonstatic initializer (usually used in anonymous inner classes).
*
* @author Petr Hamernik
*/
public final class InitializerElement extends Element implements Cloneable {
    /** reference to source element */
    private ClassElement declaringClass;

    static final long serialVersionUID =5768667690932077280L;
    /** Create an initializer represented in memory. */
    public InitializerElement() {
        this(new InitializerElement.Memory(), null);
    }

    /** Create an initializer.
    * @param impl the pluggable implementation
    * @param declaringClass the class containing it, or <code>null</code>
    */
    public InitializerElement(InitializerElement.Impl impl, ClassElement declaringClass) {
        super(impl);
        this.declaringClass = declaringClass;
    }

    /** @return the current implementation. */
    InitializerElement.Impl getInitializerImpl() {
        return (InitializerElement.Impl)impl;
    }

    /** Clone this initializer.
    * @return a new initializer with the same structure, but represented in memory
    */
    public Object clone () {
        return new InitializerElement (new Memory (this), null);
    }

    /** Set the <code>static</code> flag for this initializer.
    * @param stat <code>true</code> to make static
    * @throws SourceException if impossible
    */
    public void setStatic(boolean stat) throws SourceException {
        getInitializerImpl().setStatic(stat);
    }

    /** Test whether this initializer is static.
    * @return <code>true</code> if it is
    */
    public boolean isStatic() {
        return getInitializerImpl().isStatic();
    }

    /** Set the body of this initializer.
    * @param s the new body
    * @throws SourceException if impossible
    */
    public void setBody (String s) throws SourceException {
        getInitializerImpl ().setBody (s);
    }

    /** Get the body of this initializer.
    * @return the string representing the body
    */
    public String getBody () {
        return getInitializerImpl ().getBody ();
    }

    /** Get the class documentation.
    * @return the JavaDoc
    */
    public JavaDoc getJavaDoc() {
        return getInitializerImpl ().getJavaDoc ();
    }

    // no it's not! --jglick
    // This field is automatically updated
    // when a MemberElement is added to the class.
    /** Get the declaring class.
    *
    * @return the class that owns this initializer, or <code>null</code>
    */
    public final ClassElement getDeclaringClass () {
        return declaringClass;
    }

    /* Prints the element into the element printer.
    * @param printer The element printer where to print to
    * @exception ElementPrinterInterruptException if printer cancel the printing
    */
    public void print(ElementPrinter printer) throws ElementPrinterInterruptException {
        printer.markInitializer(this, printer.ELEMENT_BEGIN);

        JavaDoc doc = getJavaDoc();
        if ((doc != null) && !doc.isEmpty()) {
            printer.markInitializer(this, printer.JAVADOC_BEGIN); // JAVADOC begin
            printJavaDoc(doc, printer);
            printer.markInitializer(this, printer.JAVADOC_END); // JAVADOC end
            printer.println(""); // NOI18N
        }

        if (isStatic()) {
            printer.markInitializer(this, printer.HEADER_BEGIN);
            printer.print("static "); // NOI18N
            printer.markInitializer(this, printer.HEADER_END);
        }
        printer.print("{"); // NOI18N
        printer.markInitializer(this, printer.BODY_BEGIN);
        printer.print(getBody());
        printer.markInitializer(this, printer.BODY_END);
        printer.print("}"); // NOI18N
        printer.markInitializer(this, printer.ELEMENT_END);
    }

    /** Pluggable implementation of initializers.
    * @see InitializerElement
    */
    public interface Impl extends Element.Impl {
        static final long serialVersionUID =-3742940543185945549L;
        /** Set the <code>static</code> flag for this initializer.
         * @param stat <code>true</code> to make static
         * @throws SourceException if impossible
         */
        public void setStatic(boolean stat) throws SourceException;

        /** Test whether this initializer is static.
         * @return <code>true</code> if it is
         */
        public boolean isStatic();

        /** Set the body of this initializer.
         * @param s the new body
         * @throws SourceException if impossible
         */
        public void setBody (String s) throws SourceException;

        /** Get the body of this initializer.
         * @return the string representing the body
         */
        public String getBody ();

        /** Get the JavaDoc.
        * @return the JavaDoc
        */
        public JavaDoc getJavaDoc ();
    }

    /** Default implementation of the Impl interface.
    * It just holds the property values.
    */
    static class Memory extends Element.Memory implements InitializerElement.Impl {
        /** Is this block static ? */
        private boolean stat;
        /** body of the element */
        private String body;
        /** java doc comment */
	private JavaDoc javadoc;

        static final long serialVersionUID =1956692952966906280L;
        Memory() {
            stat = false;
            body = ""; // NOI18N
        }

        /** Copy constructor.
        */
        Memory(InitializerElement el) {
            stat = el.isStatic ();
            body = el.getBody ();
	    javadoc = el.getJavaDoc().isEmpty() ? JavaDocSupport.createJavaDoc(null) :
            JavaDocSupport.createJavaDoc(el.getJavaDoc().getRawText());
        }
        /** Sets the 'static' flag for this initializer. */
        public void setStatic(boolean stat) {
            boolean old = stat;
            this.stat = stat;
            firePropertyChange (PROP_STATIC, new Boolean (old), new Boolean (stat));
        }

        /** is this initializer static.
        * @return true if it is.
        */
        public boolean isStatic() {
            return stat;
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

        /** Get the JavaDoc.
        * @return the JavaDoc
        */
        public JavaDoc getJavaDoc () {
            return javadoc;
        }

        public Object readResolve() {
            return new InitializerElement(this, null);
        }
    }
}

/*
 * Log
 *  18   src-jtulach1.17        1/12/00  Petr Hamernik   i18n using perl script 
 *       (//NOI18N comments added)
 *  17   src-jtulach1.16        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   src-jtulach1.15        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  15   src-jtulach1.14        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  14   src-jtulach1.13        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  13   src-jtulach1.12        5/14/99  Jesse Glick     [JavaDoc]
 *  12   src-jtulach1.11        5/10/99  Petr Hamernik   javadoc & printing 
 *       improved
 *  11   src-jtulach1.10        3/30/99  Jesse Glick     [JavaDoc]
 *  10   src-jtulach1.9         3/22/99  Petr Hamernik   printing changed
 *  9    src-jtulach1.8         3/18/99  Petr Hamernik   
 *  8    src-jtulach1.7         2/17/99  Petr Hamernik   serialization changed.
 *  7    src-jtulach1.6         2/16/99  Petr Hamernik   
 *  6    src-jtulach1.5         2/8/99   Petr Hamernik   
 *  5    src-jtulach1.4         1/19/99  Jaroslav Tulach 
 *  4    src-jtulach1.3         1/19/99  Jaroslav Tulach 
 *  3    src-jtulach1.2         1/19/99  Jaroslav Tulach 
 *  2    src-jtulach1.1         1/18/99  David Simonek   property constants added
 *  1    src-jtulach1.0         1/17/99  Jaroslav Tulach 
 * $
 */
