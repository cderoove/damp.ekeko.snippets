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

import java.io.Serializable;
import java.util.*;

/** Support class that manages set of objects and fires events
* about its changes.
*
* @author Jaroslav Tulach
*/
class MemoryCollection extends Object implements Serializable {
    /** array of objects */
    LinkedList array;

    /** Object to fire info about changes to */
    ClassElement.Memory memory;

    /** name of property to fire */
    private String propertyName;

    /** array template to return */
    private Object[] template;

    static final long serialVersionUID =-9215370960397120952L;

    /**
    * @param memory memory element to fire changes to
    * @param propertyName name of property to fire when array changes
    * @param emptyArray emptyArray instance that provides the type of arrays
    *   that should be returned by toArray method
    */
    public MemoryCollection(
        ClassElement.Memory memory, String propertyName, Object[] emptyArray
    ) {
        this.memory = memory;
        this.propertyName = propertyName;
        this.template = emptyArray;
    }

    /** Changes the content of this object.
    * @param arr array of objects to change
    * @param action the action to do
    */
    public void change (Object[] arr, int action) {
        change (Arrays.asList (arr), action);
    }
    /** Changes the content of this object.
    * @param c collection of objects to change
    * @param action the action to do
    */
    protected void change (Collection c, int action) {
        boolean anChange;
        switch (action) {
        case ClassElement.Impl.ADD:
            anChange = c.size () > 0;
            if (array != null) {
                array.addAll (c);
                break;
            }
            // fall thru to initialize the array
        case ClassElement.Impl.SET:
            // PENDING: better change detection; currently any SET operation will fire change.
            anChange = c.size () > 0 || (array != null && array.size () > 0);
            array = new LinkedList (c);
            break;
        case ClassElement.Impl.REMOVE:
            anChange = array != null && array.removeAll (c);
        default:
            // illegal argument in internal implementation
            throw new InternalError ();
        }

        if (anChange) {
            // do not construct array values if not necessary
            memory.firePropertyChange (propertyName, null, null);
        }
    }

    /** Access to the array.
    * @return array of objects contained here
    */
    public Object[] toArray () {
        if (array == null) {
            return template;
        } else {
            return array.toArray (template);
        }
    }

    /** Special collection for initializers. Provides declaringClass field.
    */
    static final class Initializer extends MemoryCollection {
        private static final InitializerElement[] EMPTY = new InitializerElement[0];

        static final long serialVersionUID =5715072242254795093L;
        /**
        * @param memory memory element to fire changes to
        * @param propertyName name of property to fire when array changes
        * @param emptyArray emptyArray instance that provides the type of arrays
        *   that should be returned by toArray method
        */
        public Initializer (
            ClassElement.Memory memory
        ) {
            super (memory, ElementProperties.PROP_INITIALIZERS, EMPTY);
        }

        /** Changes the content of this object.
        * @param c collection of objects to change
        * @param action the action to do
        */
        protected void change (Collection c, int action) {
            boolean anChange;
            switch (action) {
            case ClassElement.Impl.ADD:
            case ClassElement.Impl.SET:
                // clone the nodes
                Iterator it = c.iterator ();
                c = new ArrayList ();
                while (it.hasNext ()) {
                    InitializerElement el = (InitializerElement)it.next ();
                    // clone the element
                    el = new InitializerElement (new InitializerElement.Memory (
                                                     el
                                                 ), memory.getClassElement ());
                    c.add (el);
                }
            }

            super.change (c, action);
        }
    }

    /** Collection for members. Assignes to each class its
    * members.
    */
    static abstract class Member extends MemoryCollection {
        static final long serialVersionUID =7875426480834524238L;
        /**
        * @param memory memory element to fire changes to
        * @param propertyName name of property to fire when array changes
        * @param emptyArray emptyArray instance that provides the type of arrays
        *   that should be returned by toArray method
        */
        public Member (
            ClassElement.Memory memory, String propertyName, Object[] emptyArray
        ) {
            super (memory, propertyName, emptyArray);
        }

        /** Changes the content of this object.
        * @param c collection of objects to change
        * @param action the action to do
        */
        protected void change (Collection c, int action) {
            boolean anChange;
            switch (action) {
            case ClassElement.Impl.ADD:
            case ClassElement.Impl.SET:
                // clone the nodes
                Iterator it = c.iterator ();
                c = new ArrayList ();
                while (it.hasNext ()) {
                    c.add (clone (it.next ()));
                }
            }

            super.change (c, action);
        }

        /** Find method that looks in member elements
        * @param id the indetifier (or null)
        * @param types array of types to test (or null)
        * @return the element or null
        */
        public MemberElement find (Identifier id, Type[] types) {
            if (array == null)
                return null;

            Iterator it = array.iterator ();
            while (it.hasNext ()) {
                MemberElement me = (MemberElement)it.next ();
                if (
                    (id == null || id.equals(me.getName ()))
                    &&
                    // types can be non-null only for Method or Constructor Elements
                    (types == null || equalTypes (types, me))
                ) {
                    // found element
                    return me;
                }
            }
            // nothing found
            return null;
        }

        /** Copares given types to types of the element.
        * The element must be ConstructorElement
        *
        * @param types the types
        * @param el the element
        * @return true if types are equal
        */
        private static boolean equalTypes (Type[] types, MemberElement el) {
            // can be called only for ConstructorElement
            MethodParameter[] test = ((ConstructorElement)el).getParameters ();

            // number
            if (test.length != types.length) return false;

            int l = test.length;
            for (int i = 0; i < l; i++) {
                if (!test[i].getType().equals(types[i]))
                    return false;
            }
            return true;
        }

        /** Clones the object.
        * @param obj object to clone
        * @return cloned object
        */
        protected abstract MemberElement clone (Object obj);
    }

    /** Collection for members. Assignes to each class its
    * members.
    */
    static class Constructor extends Member {
        private static final ConstructorElement[] EMPTY = new ConstructorElement[0];

        static final long serialVersionUID =4314343816469864217L;
        /** @param ce class element memory impl to work in
        */
        public Constructor (ClassElement.Memory ce) {
            super (ce, ElementProperties.PROP_CONSTRUCTORS, EMPTY);
        }

        /** Clones the object.
        * @param obj object to clone
        * @return cloned object
        */
        protected MemberElement clone (Object obj) {
	    ConstructorElement.Memory m = new ConstructorElement.Memory ((ConstructorElement)obj);
	    Identifier id = memory.getName();
	    if (id != null) {
		m.setName(Identifier.create(id.getName()));
	    }
	    return new ConstructorElement(m, memory.getClassElement());
        }
    }

    /** Collection for methods.
    */
    static class Method extends Member {
        private static final MethodElement[] EMPTY = new MethodElement[0];

        static final long serialVersionUID =-745714645316747109L;
        /** @param ce class element memory impl to work in
        */
        public Method (ClassElement.Memory ce) {
            super (ce, ElementProperties.PROP_METHODS, EMPTY);
        }

        /** Clones the object.
        * @param obj object to clone
        * @return cloned object
        */
        protected MemberElement clone (Object obj) {
            return new MethodElement (new MethodElement.Memory (
                                          (MethodElement)obj
                                      ), memory.getClassElement ());
        }
    }

    /** Collection of fields.
    */
    static class Field extends Member {
        private static final FieldElement[] EMPTY = new FieldElement[0];

        static final long serialVersionUID =5747776340409139399L;
        /** @param ce class element memory impl to work in
        */
        public Field (ClassElement.Memory ce) {
            super (ce, ElementProperties.PROP_FIELDS, EMPTY);
        }

        /** Clones the object.
        * @param obj object to clone
        * @return cloned object
        */
        protected MemberElement clone (Object obj) {
            return new FieldElement (new FieldElement.Memory (
                                         (FieldElement)obj
                                     ), memory.getClassElement ());
        }
    }

    /** Collection of class.
    */
    static class Class extends Member {
        private static final ClassElement[] EMPTY = new ClassElement[0];

        static final long serialVersionUID =3206093459760846163L;
        /** @param ce class element memory impl to work in
        */
        public Class (ClassElement.Memory ce) {
            super (ce, ElementProperties.PROP_CLASSES, EMPTY);
        }

        /** Clones the object.
        * @param obj object to clone
        * @return cloned object
        */
        protected MemberElement clone (Object obj) {
            ClassElement.Memory ceMem = new ClassElement.Memory (
                                            (ClassElement)obj
                                        );
            ceMem.copyFrom((ClassElement) obj);
            return new ClassElement(ceMem, memory.getClassElement());
        }
    }

}

/*
* Log
*  19   Gandalf-post-FCS1.16.1.1    4/19/00  Svatopluk Dedic 
*  18   Gandalf-post-FCS1.16.1.0    4/18/00  Svatopluk Dedic PropertyChange event 
*       firing improved
*  17   src-jtulach1.16        12/21/99 Ales Novak      the clone method of Class
*       clones also interfaces, methods, etc.
*  16   src-jtulach1.15        12/8/99  Petr Hamernik   compilable by Javac V8 
*       (jdk1.3)
*  15   src-jtulach1.14        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  14   src-jtulach1.13        10/7/99  Petr Hamernik   finding elements bug fix.
*  13   src-jtulach1.12        9/29/99  Petr Hamernik   just indentation
*  12   src-jtulach1.11        8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  11   src-jtulach1.10        8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  10   src-jtulach1.9         8/4/99   Petr Hamernik   fixed bug #3135
*  9    src-jtulach1.8         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  8    src-jtulach1.7         6/7/99   Petr Hamernik   adding bug fixed
*  7    src-jtulach1.6         5/12/99  Petr Hamernik   Identifier implementation
*       updated
*  6    src-jtulach1.5         2/26/99  Petr Hamernik   bugfixes
*  5    src-jtulach1.4         2/17/99  Petr Hamernik   serialization changed.
*  4    src-jtulach1.3         1/19/99  Jaroslav Tulach 
*  3    src-jtulach1.2         1/19/99  Jaroslav Tulach 
*  2    src-jtulach1.1         1/19/99  Jaroslav Tulach 
*  1    src-jtulach1.0         1/19/99  Jaroslav Tulach 
* $
*/
