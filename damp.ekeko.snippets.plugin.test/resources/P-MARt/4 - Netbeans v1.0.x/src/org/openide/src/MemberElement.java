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

/** Superclass for containable Java source members
* (fields, methods and classes). Provides support
* for associating this element with a declaring class.
*
* @author Petr Hamernik, Jaroslav Tulach
*/
public abstract class MemberElement extends Element implements Cloneable {
    /** the class this element belongs to */
    private ClassElement declaringClass;

    static final long serialVersionUID =7896378970641663987L;

    /** Create a member element.
    * @param impl the pluggable implementation
    * @param declaringClass the class this element belongs to, or <code>null</code> if unattached
    */
    protected MemberElement(MemberElement.Impl impl, ClassElement declaringClass) {
        super(impl);
        this.declaringClass = declaringClass;
    }

    /** @return the current implementation. */
    final MemberElement.Impl getMemberImpl() {
        return (MemberElement.Impl) impl;
    }

    // [PENDING] Modifier explicitly disallows assuming its constants
    // are bitwise composable--this is technically illegal
    // (although in fact they are and this will probably never change)

    /** Get the modifier flags for this element.
    * Constrained by {@link #getModifiersMask}.
    * @return disjunction of constants from {@link Modifier}
    */
    public final int getModifiers() {
        return getMemberImpl().getModifiers();
    }

    /** Set the modifier flags for this element.
    * @param mod disjunction of constants from {@link Modifier}
    * @throws SourceException if impossible (e.g. if <code>mod & ~ getModifiersMask() != 0</code>)
    */
    public final void setModifiers(int mod) throws SourceException {
        getMemberImpl().setModifiers(mod);
    }

    /** Get the permitted modifiers for this type of element.
    * @return disjunction of constants from {@link Modifier}
    */
    public abstract int getModifiersMask();

    /** Test whether declaring class is interface or class.
    * @return <CODE>true</CODE> for interfaces otherwise <CODE>false</CODE>
    */
    boolean isDeclaredInInterface() {
        return (declaringClass != null) && (declaringClass.isInterface());
    }

    /** Get the name of this member.
    * @return the name
    */
    public final Identifier getName() {
        return getMemberImpl().getName();
    }

    /** Set the name of this member.
    * @param name the name
    * @throws SourceException if impossible
    */
    public void setName(Identifier name) throws SourceException {
        getMemberImpl().setName(name);
        updateConstructorsNames(name);
    }

    /** Implemented in ClassElement - update names of the constructors.
    */
    void updateConstructorsNames(Identifier name) throws SourceException {
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    // no --jglick
    /* This field is automaticly sychnronized
    * when a MemberElement is added to the class. */
    /** Get the declaring class.
    *
    * @return the class that owns this member element, or <code>null</code> if the element is not
    *    attached to any class
    */
    public final ClassElement getDeclaringClass () {
        return declaringClass;
    }

    /** Pluggable implementation of member elements.
    * @see MemberElement
    */
    public interface Impl extends Element.Impl {
        static final long serialVersionUID =2037286733482347462L;
        /** Get the modifier flags for this element.
         * Constrained by {@link MemberElement#getModifiersMask}.
         * @return disjunction of constants from {@link Modifier}
         */
        public int getModifiers();

        /** Set the modifier flags for this element.
         * @param mod disjunction of constants from {@link Modifier}
         * @throws SourceException if impossible (e.g. if <code>mod & ~ memberElt.getModifiersMask() != 0</code>)
         */
        public void setModifiers(int mod) throws SourceException;

        /** Get the name of this member.
         * @return the name
         */
        public Identifier getName();

        /** Set the name of this member.
         * @param name the name
         * @throws SourceException if impossible
         */
        public void setName(Identifier name) throws SourceException;
    }

    /** Default implementation of the Impl interface.
    * It just holds the property values.
    */
    static abstract class Memory extends Element.Memory implements MemberElement.Impl {
        /** Modifiers for this element */
        private int mod;

        /** Name of this element */
        private Identifier name;

        static final long serialVersionUID =1876531129266668488L;
        /** Default */
        public Memory () {
        }

        /** Copy */
        public Memory (MemberElement el) {
            mod = el.getModifiers ();
            name = el.getName ();
        }

        /** Getter for modifiers for this element.
        * @see java.lang.reflect.Modifier
        * @return constants from <CODE>java.lang.reflect.Modifier</CODE>
        */
        public int getModifiers() {
            return mod;
        }

        /** Setter for modifiers for this element.
        * @see java.lang.reflect.Modifier
        * @param mod constants from <CODE>java.lang.reflect.Modifier</CODE>
        */
        public void setModifiers(int mod) {
            int old = this.mod;
            this.mod = mod;
            firePropertyChange (PROP_MODIFIERS, new Integer (old), new Integer (mod));
        }

        /** Getter for name of the field.
        * @return the name
        */
        public synchronized Identifier getName() {
            if (name == null) {
                // lazy initialization !?
                name = Identifier.create(""); // NOI18N
            }
            return name;
        }

        /** Setter for name of the field.
        * @param name the name of the field
        */
        public synchronized void setName(Identifier name) {
            Identifier old = this.name;
            this.name = name;
            firePropertyChange (PROP_NAME, old, name);
        }
    }
}

/*
 * Log
 *  18   Gandalf-post-FCS1.16.1.0    4/18/00  Svatopluk Dedic PropertyChange event 
 *       firing improved
 *  17   src-jtulach1.16        1/18/00  Petr Hamernik   fixed #5309
 *  16   src-jtulach1.15        1/12/00  Petr Hamernik   i18n using perl script 
 *       (//NOI18N comments added)
 *  15   src-jtulach1.14        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   src-jtulach1.13        9/28/99  Petr Hamernik   fixed bug #1074
 *  13   src-jtulach1.12        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  12   src-jtulach1.11        7/13/99  Petr Hamernik   ConstrainedModifiers 
 *       removed
 *  11   src-jtulach1.10        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  10   src-jtulach1.9         6/2/99   Petr Hamernik   clone added (only to be 
 *       public)
 *  9    src-jtulach1.8         4/30/99  Jesse Glick     [JavaDoc]
 *  8    src-jtulach1.7         4/20/99  Petr Hamernik   synchronization of the 
 *       name - between class and constructors
 *  7    src-jtulach1.6         3/30/99  Jesse Glick     [JavaDoc]
 *  6    src-jtulach1.5         2/17/99  Petr Hamernik   serialization changed.
 *  5    src-jtulach1.4         2/16/99  Petr Hamernik   
 *  4    src-jtulach1.3         2/8/99   Petr Hamernik   
 *  3    src-jtulach1.2         1/19/99  Jaroslav Tulach 
 *  2    src-jtulach1.1         1/19/99  Jaroslav Tulach 
 *  1    src-jtulach1.0         1/17/99  Jaroslav Tulach 
 * $
 */
