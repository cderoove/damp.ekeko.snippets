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
import java.text.MessageFormat;

/** Describes a field (variable) in a class.
*
* @author Petr Hamernik, Jaroslav Tulach
*/
public final class FieldElement extends MemberElement {
    /** Format for the code generator */
    private static final ElementFormat FIELD_FORMAT =
        new ElementFormat("{m,,\" \"}{t} {n}"); // NOI18N

    static final long serialVersionUID =2195820119328567201L;

    /** Create a new field element represented in memory.
    */
    public FieldElement() {
        this(new Memory(), null);
    }

    /** Create a new field element.
    * @param impl the pluggable implementation
    * @param declaringClass declaring class of this field, or <code>null</code>
    */
    public FieldElement(FieldElement.Impl impl, ClassElement declaringClass) {
        super(impl, declaringClass);
    }

    /** Clone the field element.
    * @return a new element that has the same values as the original
    *   but is represented in memory
    */
    public Object clone () {
        return new FieldElement (new Memory (this), null);
    }

    final FieldElement.Impl getFieldImpl() {
        return (FieldElement.Impl) impl;
    }

    /** Get the value type of the field.
    * @return the type
    */
    public Type getType() {
        return getFieldImpl().getType();
    }

    /** Set the value type of the field.
    * @param type the type
    * @throws SourceException if impossible
    */
    public void setType(Type type) throws SourceException {
        getFieldImpl().setType(type);
    }

    /** Get the initial value of the field.
    * @return the initial value (as source text), or an empty string if uninitialized
    */
    public String getInitValue() {
        return getFieldImpl().getInitValue();
    }

    /** Set the initial value of the field.
    * @param value the initial value (as source text), or an empty string if uninitialized
    * @throws SourceException if impossible
    */
    public void setInitValue(String value) throws SourceException {
        getFieldImpl().setInitValue(value);
    }

    /* Get the possible modifiers for the field.
     * @return the mask of possible modifiers for this element. */
    public int getModifiersMask() {
        if (isDeclaredInInterface()) {
            return Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
        }
        else {
            return Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE |
                   Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT |
                   Modifier.VOLATILE;
        }
    }

    /** Set the name of this member.
    * @param name the name
    * @throws SourceException if impossible
    */
    public final void setName(Identifier name) throws SourceException {
        ClassElement c = getDeclaringClass();
        if (c != null) {
            FieldElement f = c.getField(name);
            if ((f != null) && (f != this)) {
                MessageFormat format = new MessageFormat(ElementFormat.bundle.getString("FMT_EXC_RenameField"));
                String msg = format.format(new Object[] { c.getName().getName(), name });
                throw new SourceException(msg);
            }
        }
        super.setName(name);
    }

    /** Get the JavaDoc for the field.
    * @return the JavaDoc
    */
    public JavaDoc.Field getJavaDoc () {
        return getFieldImpl ().getJavaDoc ();
    }

    /* Prints the element into the element printer.
    * @param printer The element printer where to print to
    * @exception ElementPrinterInterruptException if printer cancel the printing
    */
    public void print(ElementPrinter printer) throws ElementPrinterInterruptException {
        printer.markField(this, printer.ELEMENT_BEGIN);

        JavaDoc doc = getJavaDoc();
        if ((doc != null) && !doc.isEmpty()) {
            printer.markField(this, printer.JAVADOC_BEGIN); // JAVADOC begin
            printJavaDoc(doc, printer);
            printer.markField(this, printer.JAVADOC_END); // JAVADOC end
            printer.println(""); // NOI18N
        }

        printer.markField(this, printer.HEADER_BEGIN);
        printer.print(FIELD_FORMAT.format(this));
        printer.markField(this, printer.HEADER_END);

        String init = getInitValue();
        if (init.length() > 0) {
            printer.print(" = "); // NOI18N
            printer.markField(this, printer.BODY_BEGIN);
            printer.print(init);
            printer.markField(this, printer.BODY_END);
        }

        printer.print(";"); // NOI18N

        printer.markField(this, printer.ELEMENT_END);
    }

    /** Implementation of a field element.
    * @see FieldElement
    */
    public interface Impl extends MemberElement.Impl {
        static final long serialVersionUID =-1612065404733395830L;
        /** Get the value type of the field.
         * @return the type
         */
        public Type getType();

        /** Set the value type of the field.
         * @param type the type
         * @throws SourceException if impossible
         */
        public void setType(Type type) throws SourceException;

        /** Get the initial value of the field.
         * @return the initial value (as source text), or an empty string if uninitialized
         */
        public String getInitValue();

        /** Set the initial value of the field.
         * @param value the initial value (as source text), or an empty string if uninitialized
         * @throws SourceException if impossible
         */
        public void setInitValue(String value) throws SourceException;

        /** Get the JavaDoc for the field.
         * @return the JavaDoc
         */
        public JavaDoc.Field getJavaDoc ();
    }

    static class Memory extends MemberElement.Memory implements Impl {
        /** Type of exception */
        private Type type;

        /** Init value of variable */
        private String initValue;

        /** java doc */
        private JavaDoc.Field javaDoc;

        static final long serialVersionUID =1407258001185361107L;
        Memory() {
            type = Type.VOID;
            initValue = ""; // NOI18N
            javaDoc = JavaDocSupport.createFieldJavaDoc( null );
        }

        /** Copy constructor.
        * @param field the object to read values from
        * @param clazz declaring class to use
        */
        Memory (FieldElement field) {
            super (field);
            type = field.getType ();
            initValue = field.getInitValue ();
            javaDoc = field.getJavaDoc().isEmpty() ?
                      JavaDocSupport.createFieldJavaDoc( null ) :
                      JavaDocSupport.createFieldJavaDoc( field.getJavaDoc().getRawText() );
        }

        /** Type of the variable.
        * @return the type
        */
        public Type getType() {
            return type;
        }

        /** Setter for type of the variable.
        * @param type the variable type
        */
        public void setType(Type type) {
            Type old = this.type;
            this.type = type;
            firePropertyChange (PROP_TYPE, old, type);
        }

        /** Getter for the initial value.
        * @return initial value for the variable or empty string if it is not initialized
        */
        public String getInitValue() {
            return initValue;
        }

        /** Setter for the initial value.
        * @param value initial value for the variable
        */
        public void setInitValue(String value) {
            String old = initValue;
            initValue = value;
            firePropertyChange (PROP_INIT_VALUE, old, value);
        }

        /** @return java doc for the field
        */
        public JavaDoc.Field getJavaDoc () {
            return javaDoc;
        }

        public Object readResolve() {
            return new FieldElement(this, null);
        }
    }
}

/*
 * Log
 *  24   Gandalf-post-FCS1.21.1.1    4/18/00  Svatopluk Dedic PropertyChange event 
 *       firing improved
 *  23   Gandalf-post-FCS1.21.1.0    4/14/00  Svatopluk Dedic Equal sign made part of 
 *       field's body.
 *  22   src-jtulach1.21        1/18/00  Petr Hamernik   bugfix of previous 
 *       change
 *  21   src-jtulach1.20        1/18/00  Petr Hamernik   fixed #5309
 *  20   src-jtulach1.19        1/12/00  Petr Hamernik   i18n using perl script 
 *       (//NOI18N comments added)
 *  19   src-jtulach1.18        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  18   src-jtulach1.17        9/28/99  Petr Hamernik   fixed bug #1074
 *  17   src-jtulach1.16        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  16   src-jtulach1.15        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  15   src-jtulach1.14        6/7/99   Petr Hrebejk    Memory implementations 
 *       added to memory implementations of elements
 *  14   src-jtulach1.13        5/14/99  Jesse Glick     [JavaDoc]
 *  13   src-jtulach1.12        5/10/99  Petr Hamernik   javadoc & printing 
 *       improved
 *  12   src-jtulach1.11        5/3/99   Petr Hamernik   bugfix in code printing
 *  11   src-jtulach1.10        4/14/99  Petr Hamernik   fixed bug #1455
 *  10   src-jtulach1.9         3/30/99  Jesse Glick     [JavaDoc]
 *  9    src-jtulach1.8         3/23/99  Petr Hamernik   
 *  8    src-jtulach1.7         3/22/99  Petr Hamernik   printing changed
 *  7    src-jtulach1.6         2/17/99  Petr Hamernik   serialization changed.
 *  6    src-jtulach1.5         2/8/99   Petr Hamernik   
 *  5    src-jtulach1.4         1/19/99  Jaroslav Tulach 
 *  4    src-jtulach1.3         1/19/99  Jaroslav Tulach 
 *  3    src-jtulach1.2         1/19/99  Jaroslav Tulach 
 *  2    src-jtulach1.1         1/18/99  David Simonek   property constants added
 *  1    src-jtulach1.0         1/17/99  Jaroslav Tulach 
 * $
 */
