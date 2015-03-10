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

/** Representation of a method.
* It extends the constructor representation since
* all that is added is the return type.
*
* @author Petr Hamernik
*/
public final class MethodElement extends ConstructorElement {
    /** Format for the header - used in code generator */
    private static final ElementFormat HEADER_FORMAT =
        new ElementFormat("{m,,\" \"}{r} {n}({a,,,\",\"}){e,\" throws \",}"); // NOI18N

    static final long serialVersionUID =2366156788906032138L;

    /** Create a method element held in memory. */
    public MethodElement() {
        this(new Memory(), null);
    }

    /** Create a method element.
    * @param impl implementation of functionality
    * @param clazz declaring class, or <code>null</code>
    */
    public MethodElement(MethodElement.Impl impl, ClassElement clazz) {
        super(impl, clazz);
    }

    /** Clone the method.
    * @return new method with the same values as the original,
    *   but represented in memory
    */
    public Object clone () {
        return new MethodElement (new Memory (this), null);
    }

    final MethodElement.Impl getMethodImpl() {
        return (MethodElement.Impl) impl;
    }

    /** Get the method's return type.
    * @return the return type
    */
    public Type getReturn() {
        return getMethodImpl().getReturn();
    }

    /** Set the method's return type.
    * @param type the new type
    * @throws SourceException if impossible
    */
    public void setReturn (Type ret) throws SourceException {
        getMethodImpl().setReturn(ret);
    }

    /* @return the mask of possible modifiers for this element. */
    public int getModifiersMask() {
        if (isDeclaredInInterface()) {
            return Modifier.PUBLIC | Modifier.ABSTRACT;
        }
        else {
            return Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE |
                   Modifier.ABSTRACT | Modifier.STATIC | Modifier.FINAL |
                   Modifier.NATIVE | Modifier.SYNCHRONIZED;
        }
    }

    /** Set the name of this member.
    * @param name the name
    * @throws SourceException if impossible
    */
    public final void setName(Identifier name) throws SourceException {
        ClassElement c = getDeclaringClass();
        if (c != null) {
            MethodParameter[] params = getParameters();
            Type[] types = new Type[params.length];
            for (int i = 0; i < types.length; i++)
                types[i] = params[i].getType();
            MethodElement m = c.getMethod(name, types);
            if ((m != null) && (m != this)) {
                MessageFormat format = new MessageFormat(ElementFormat.bundle.getString("FMT_EXC_RenameMethod"));
                String msg = format.format(new Object[] { c.getName().getName(), name });
                throw new SourceException(msg);
            }
        }
        super.setName(name);
    }

    /** Get the printing format.
    * May be overridden by subclasses.
    * @return the format
    */
    ElementFormat getFormat() {
        return HEADER_FORMAT;
    }

    /** Marks the notable point in the writer.
    * This method calls markMethod.
    */
    void printerMark(ElementPrinter printer, int what) throws ElementPrinterInterruptException {
        printer.markMethod(this, what);
    }

    /** Pluggable behavior of the method element.
    * @see MethodElement
    */
    public interface Impl extends ConstructorElement.Impl {
        static final long serialVersionUID =7273573865765501815L;
        /** Set the method's return type.
         * @param type the new type
         */
        public Type getReturn();

        /** Get the method's return type.
         * @return the return type
         */
        public void setReturn (Type ret) throws SourceException;
    }

    /** A key for method elements, for use in hash tables etc.
    */
    public static final class Key extends ConstructorElement.Key {
        /** Name of the method */
        private Identifier name;

        /** Constructs a key by name and parameters.
        * @param name the method name
        * @param params the method's parameters
        */
        public Key (final Identifier name, final Type[] params) {
            super(params);
            this.name = name;
        }

        /** Constructs a key for a method.
        * Does not hold any reference to the element.
        * @param me the method element
        */
        public Key (final MethodElement me) {
            super(me);
            this.name = me.getName();
        }

        /* Returns true if parameters are the same */
        public boolean equals (Object obj) {
            if (!(obj instanceof Key)) return false;
            return super.equals(obj) && name.equals(((Key)obj).name);
        }

        /* Computes hashcode as exclusive or of
        * superclass hashcode and return type string hashcode.
        */
        public int hashCode () {
            return super.hashCode() ^ name.getFullName().hashCode();
        }

    } // end of Key inner class

    static class Memory extends ConstructorElement.Memory implements Impl {
        /** Type of exception */
        private Type type;

        static final long serialVersionUID =2015834437815195149L;
        Memory() {
            type = Type.VOID;
        }

        /** Copy constructor */
        Memory (MethodElement el) {
            super (el);
            type = el.getReturn ();
        }

        /** Setter for the return type */
        public Type getReturn() {
            return type;
        }

        /** @return the return type */
        public void setReturn (Type ret) {
            Type t = type;
            type = ret;
            firePropertyChange (PROP_RETURN, t, ret);
        }

        public Object readResolve() {
            return new MethodElement(this, null);
        }

    }

}

/*
 * Log
 *  25   src-jtulach1.24        1/18/00  Petr Hamernik   bugfix of previous 
 *       change
 *  24   src-jtulach1.23        1/18/00  Petr Hamernik   fixed #5309
 *  23   src-jtulach1.22        1/12/00  Petr Hamernik   i18n using perl script 
 *       (//NOI18N comments added)
 *  22   src-jtulach1.21        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  21   src-jtulach1.20        9/28/99  Petr Hamernik   fixed bug #1074
 *  20   src-jtulach1.19        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  19   src-jtulach1.18        8/6/99   Petr Hamernik   roll back
 *  18   src-jtulach1.17        8/6/99   Petr Hamernik   better formating of 
 *       generated text
 *  17   src-jtulach1.16        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  16   src-jtulach1.15        5/14/99  Jesse Glick     [JavaDoc]
 *  15   src-jtulach1.14        5/12/99  Petr Hamernik   Identifier 
 *       implementation updated
 *  14   src-jtulach1.13        4/2/99   Jesse Glick     [JavaDoc]
 *  13   src-jtulach1.12        3/30/99  Petr Hamernik   getFormat is package 
 *       private
 *  12   src-jtulach1.11        3/30/99  Jesse Glick     [JavaDoc]
 *  11   src-jtulach1.10        3/23/99  Petr Hamernik   
 *  10   src-jtulach1.9         3/22/99  Petr Hamernik   printing changed
 *  9    src-jtulach1.8         2/17/99  Petr Hamernik   serialization changed.
 *  8    src-jtulach1.7         2/8/99   Petr Hamernik   
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
