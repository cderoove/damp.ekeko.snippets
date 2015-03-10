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

import java.io.Serializable;
import java.io.IOException;
import java.util.*;

import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;

import org.openide.nodes.Children;
import org.openide.src.*;
import org.openide.text.*;

/** Support class that manages set of objects and fires events
* about its changes.
*
* @author Petr Hamernik, Jaroslav Tulach
*/
abstract class ElementsCollection extends Object implements Serializable, ElementProperties {

    static final long serialVersionUID =-4106496722521728236L;

    /** array of objects 
     * @associates Object*/
    LinkedList array;

    /** Object to fire info about changes to */
    ElementImpl owner;

    boolean skipBoundsCreation = false;

    /**
    * @param owner owner of this array to fire changes to
    */
    public ElementsCollection(ElementImpl owner) {
        this.owner = owner;
    }

    /** Must be rewriten by subclasses to obtain the name of the property,
    *  which is fired when any changes of the array happen.
    *
    * @return the property name.
    */
    public abstract String getPropertyName();

    /** Must be rewriten by subclasses to obtain the default (empty) array
    * of the elements.
    *
    * @return the empty array
    */
    public abstract Object[] getEmptyArray();

    /** Changes the content of this object.
    * @param arr array of objects to change
    * @param action the action to do
      @throws SourceException if the source can't be modified.
    */
    public void change (Object[] arr, int action) throws SourceException {
        int oldLen = (array == null) ? 0 : array.size();
        int newLen = arr.length;
        boolean changed = false;

        try {
            switch (action) {
            case ClassElement.Impl.SET:
                if (oldLen > 0) {
                    Iterator it = array.iterator();
                    while (it.hasNext()) {
                        Element element = (Element) it.next();
                        ElementImpl impl = (ElementImpl) element.getCookie(ElementImpl.class);
                        impl.removeFromSource();
                        changed = true;
                    }
                    array = null;
                }

            case ClassElement.Impl.ADD:
                if (newLen > 0) {
                    if (array == null)
                        array = new LinkedList();
                    for (int i = 0; i < newLen; i++) {
                        PositionBounds bounds = skipBoundsCreation ? null : owner.createBoundsFor(this);
                        Element e = createElement(arr[i], bounds);
                        array.add(e);
                        changed = true;
                    }
                }
                return;

            case ClassElement.Impl.REMOVE:
                if ((newLen > 0) && (oldLen > 0)) {
                    for (int i = 0; i < newLen; i++) {
                        Element element = (Element) arr[i];
                        int index = array.indexOf(element);
                        if (index != -1) {
                            ElementImpl impl = (ElementImpl) element.getCookie(ElementImpl.class);
                            //SourceElementImpl.clearBounds(impl.bounds);
                            impl.removeFromSource();
                            array.remove(index);
                            changed = true;
                        }
                    }
                }
                return;
            }
        }
        finally {
            if (changed) {
                fireChange(owner, getPropertyName(), null, null);
            }
        }
    }

    static void fireChange(ElementImpl owner, String propName, Object oldValue, Object newValue) {
        owner.firePropertyChange (propName, null, null);
        if (propName.equals(PROP_CLASSES)) {
            SourceElementImpl src = null;

            if (owner instanceof SourceElementImpl)
                src = (SourceElementImpl) owner;

            if (owner instanceof ClassElementImpl) {
                ClassElementImpl c = (ClassElementImpl) owner;
                if (c.getClassElement() != null) {
                    src = (SourceElementImpl) c.getClassElement().getSource().getCookie(SourceElementImpl.class);
                    for (;;) {
                        if (owner instanceof ClassElementImpl) {
                            c.firePropertyChange(PROP_ALL_CLASSES, null, null);
                            ClassElement outer = c.getClassElement().getDeclaringClass();
                            if (outer == null)
                                break;
                            c = (ClassElementImpl) outer.getCookie(ClassElementImpl.class);
                        }
                    }
                }
            }
            if (src != null) {
                src.firePropertyChange(PROP_ALL_CLASSES, null, null);
            }
        }
    }

    Element createElement(Object o, PositionBounds bounds) throws SourceException {
        throw new SourceException();
    }

    /** Access to the array.
    * @return array of objects contained here
    */
    public Object[] toArray () {
        return (array == null) ? getEmptyArray() : array.toArray(getEmptyArray());
    }

    /** Only parser should call this method. Initializes (if needed) the array
    * and adds the object.
    * @param obj Object to be added.
    */
    void add(final Object obj) {
        if (array == null)
            array = new LinkedList();
        array.add(obj);
    }

    /** @return the first element of the collection or null if empty */
    Element getFirst() {
        return ((array != null) && (array.size() > 0)) ? (Element) array.getFirst() : null;
    }

    Element getLast() {
        return ((array != null) && (array.size() > 0)) ? (Element)array.getLast() : null;
    }

    /** @return the size of this collection */
    int size() {
        return (array != null) ? array.size() : 0;
    }

    /** Finds an element at the given source position.
      @return reference to the most specific element on the given position or null,
        if no such element can be found (the position is outside any element in the source)
    */
    Element findElement(PositionRef pos) {
        return findElement(pos.getOffset());
    }

    /** Finds an element that occupies the given offset in the source text.
      @return Most specific known element that occupies the given offset.
    */
    Element findElement(int offset) {
        if (array == null)
            return null;
        Iterator it = array.iterator();
        Element found;

        while (it.hasNext()) {
            Element e = (Element) it.next();
            ElementImpl impl = (ElementImpl) e.getCookie(ElementImpl.class);
            if (impl.isAtOffset(offset)) {
                // delegate the search to the element itself (in case it have some sub-elements)
                return impl.findElement(offset);
            }
        }
        return null;
    }

    // =================== Initializer ==============================================

    /** Special collection for initializers. Provides declaringClass field.
    */
    static final class Initializer extends ElementsCollection {
        /** Empty array of initializers. */
        private static final InitializerElement[] EMPTY = new InitializerElement[0];

        static final long serialVersionUID =8165296836058034074L;

        /**
        * @param owner owner of this array to fire changes to
        */
        public Initializer (ClassElementImpl owner) {
            super(owner);
        }

        /** @return the property name. */
        public String getPropertyName() {
            return PROP_INITIALIZERS;
        }

        /** @return the empty array of elements. */
        public Object[] getEmptyArray() {
            return EMPTY;
        }

        /** Changes the content of this object.
        * @param c collection of objects to change
        * @param action the action to do
        */
        protected void change (Collection c, int action) throws SourceException {

        }

        Element createElement(Object o, PositionBounds bounds) throws SourceException {
            if (o instanceof InitializerElement) {
                return new InitializerElement(new InitializerElementImpl((InitializerElement)o, bounds),
                                              (ClassElement)owner.element);
            }
            throw new SourceException();
        }

        /** Updates the array of the initializers
        * @param newList The new list of initializers
        */
        void updateContent(final LinkedList newList, LinkedList changes, int changesMask) {
            boolean changed = false;
            if (newList.size() == 0) {
                if ((array == null) || (array.size() == 0))
                    return;
                array = null;
                changed = true;
            }
            else {
                if (array == null)
                    array = new LinkedList();

                Iterator oldIter = array.iterator();
                Iterator newIter = newList.iterator();
                int removeMark = newList.size();
		boolean created = false;
                while (newIter.hasNext()) {
                    InitializerElementImpl impl = (InitializerElementImpl) newIter.next();
                    if (!created && oldIter.hasNext()) {
                        InitializerElement el = (InitializerElement) oldIter.next();
                        ((InitializerElementImpl)el.getCookie(InitializerElementImpl.class)).updateImpl(impl);
                    }
                    else {
                        array.add(new InitializerElement(impl, (ClassElement)owner.element));
                        changed = true;
			created = true;
                    }
                }
                if (array.size() > removeMark) {
                    ListIterator removeIter = array.listIterator(removeMark);
                    while (removeIter.hasNext()) {
                        InitializerElementImpl impl = (InitializerElementImpl)((InitializerElement)removeIter.next()).getCookie(InitializerElementImpl.class);
			removeIter.remove();			
                        impl.setValid(false);
			changed = true;
                    }
                }
            }

            if (changed) {
                ElementsCollection.fireChange(owner, getPropertyName(), null, null);
            }
        }
    }

    // =================== Member ==============================================

    /** Collection for members. Assignes to each class its
    * members.
    */
    static abstract class Member extends ElementsCollection {
        static final long serialVersionUID =-6900636410396718812L;

        /**
        * @param owner owner of this array to fire changes to
        */
        public Member(ElementImpl owner) {
            super(owner);
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
                if ((id == null || id.compareTo(me.getName(), false)) &&
                        (types == null || equalTypes (types, me))) {
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
                if (!test[i].getType ().compareTo(types[i], false))
                    return false;
            }
            return true;
        }

        public void updateContent(final LinkedList newList, LinkedList changes, int changesMask) {
            boolean changed = false;
            if (newList.size() == 0) {
                if ((array == null) || (array.size() == 0))
                    return;
                array = null;
                changed = true;
            }
            else {
                MemberElement[] oldArray = new MemberElement[(array == null) ? 0 : array.size()];
                if (oldArray.length > 0)
                    array.toArray(oldArray);

                MemberElementImpl[] newArray = new MemberElementImpl[newList.size()];
                newList.toArray(newArray);

                int[] result = ElementsCollection.pairElements(oldArray, newArray, getComparators());
                if (oldArray.length == newArray.length) {
                    for (int i = 0; i < result.length; i++) {
                        if (result[i] != i) {
                            changed = true;
                            break;
                        }
                    }
                }
                else
                    changed = true;

                if (changed) {
                    //          System.out.println("---------changed:"+getClass()+" - "+Integer.toHexString(connectionsMask())); // NOI18N
                    //          System.out.println(" change mask:"+Integer.toHexString(changesMask)); // NOI18N

                    boolean notifyAdd = ((changesMask & connectionsMask() & JavaConnections.ADD_MASK) != 0);
                    boolean notifyRemove = ((changesMask & connectionsMask() & JavaConnections.REMOVE_MASK) != 0);
                    LinkedList added = notifyAdd ? new LinkedList() : null;
                    LinkedList removed = notifyRemove ? new LinkedList() : null;
                    boolean[] remainFlags = new boolean[oldArray.length];

                    //          System.out.println("array:"+getClass()+" / add-remove:"+notifyAdd+"-"+notifyRemove); // NOI18N

                    array = new LinkedList();

                    for (int i = 0; i < newArray.length; i++) {
                        MemberElement addingElement;
                        if (result[i] == -1) {
                            addingElement = createNewElement(newArray[i]);
                            if (notifyAdd)
                                added.add(addingElement);
                        }
                        else {
                            addingElement = oldArray[result[i]];
                            remainFlags[result[i]] = true;
                            updateElementImpl(addingElement, newArray[i], changes, changesMask);
                        }
                        array.add(addingElement);
                    }
                    if (notifyAdd) {
                        Element[] addedElements = (Element[]) added.toArray(new Element[added.size()]);
                        //            System.out.println("ADD SIZE:"+addedElements.length); // NOI18N
                        //            System.out.println("mask:"+Integer.toHexString(connectionsMask() & JavaConnections.ADD_MASK)); // NOI18N
                        changes.add(new JavaConnections.Change(connectionsMask() & JavaConnections.ADD_MASK, addedElements));
                    }
                    for (int i = 0; i < remainFlags.length; i++) {
                        if (!remainFlags[i]) {
                            if (notifyRemove)
                                removed.add(oldArray[i]);
                            ((ElementImpl)oldArray[i].getCookie(ElementImpl.class)).setValid(false);
                        }
                    }
                    if (notifyRemove) {
                        changes.add(new JavaConnections.Change(connectionsMask() & JavaConnections.REMOVE_MASK,
                                                               (Element[])removed.toArray(new Element[removed.size()])));
                    }
                }
                else {
                    Iterator oldIter = array.iterator();
                    Iterator newIter = newList.iterator();
                    while (oldIter.hasNext()) {
                        updateElementImpl((MemberElement) oldIter.next(), (MemberElementImpl) newIter.next(), changes, changesMask);
                    }
                }
            }

            if (changed)
                owner.firePropertyChange (this.getPropertyName(), null, null);
        }

        public abstract Comparator[] getComparators();

        public abstract MemberElement createNewElement(MemberElementImpl impl);

        public abstract void updateElementImpl(MemberElement element, MemberElementImpl impl, LinkedList changes, int changesMask);

        public abstract int connectionsMask();
    }

    // =================== Field =======================================

    /** Collection of fields.
    */
    static class Field extends Member {
        /** Empty array of fields. */
        private static final FieldElement[] EMPTY = new FieldElement[0];

        /** Comparators of fields */
        private static final Comparator[] COMPARATORS = new Comparator[] {
                    new MemberNameComparator()
                };

        static final long serialVersionUID =1504645607687248949L;

        /**
        * @param owner owner of this array to fire changes to
        */
        public Field(ClassElementImpl owner) {
            super(owner);
        }

        /** @return the property name. */
        public String getPropertyName() {
            return PROP_FIELDS;
        }

        /** @return the empty array of elements. */
        public Object[] getEmptyArray() {
            return EMPTY;
        }

        public Comparator[] getComparators() {
            return COMPARATORS;
        }

        public int connectionsMask() {
            return JavaConnections.TYPE_FIELDS;
        }

        public MemberElement createNewElement(MemberElementImpl impl) {
            return new FieldElement((FieldElementImpl) impl, (ClassElement)owner.element);
        }

        public void updateElementImpl(MemberElement element, MemberElementImpl impl, LinkedList changes, int changesMask) {
            ((FieldElementImpl)element.getCookie(FieldElementImpl.class)).updateImpl((FieldElementImpl)impl, changes, changesMask);
        }

        Element createElement(Object o, PositionBounds bounds) throws SourceException {
            if (o instanceof FieldElement) {
                return new FieldElement(new FieldElementImpl((FieldElement)o, bounds),
                                        (ClassElement)owner.element);
            }
            throw new SourceException();
        }

    }

    // =================== Constructor =======================================

    /** Collection for constructors. */
    static class Constructor extends Member {
        /** Empty array of constructors. */
        private static final ConstructorElement[] EMPTY = new ConstructorElement[0];

        /** Comparators of constructors */
        private static final Comparator[] COMPARATORS = new Comparator[] {
                    new ConstructorComparator(false, true)
                };

        static final long serialVersionUID =-3770890592948810897L;

        /**
        * @param owner owner of this array to fire changes to
        */
        public Constructor(ClassElementImpl owner) {
            super(owner);
        }

        /** @return the property name. */
        public String getPropertyName() {
            return PROP_CONSTRUCTORS;
        }

        /** @return the empty array of elements. */
        public Object[] getEmptyArray() {
            return EMPTY;
        }

        public int connectionsMask() {
            return JavaConnections.TYPE_CONSTRUCTORS;
        }

        Element createElement(Object o, PositionBounds bounds) throws SourceException {
            if (o instanceof ConstructorElement) {
                return new ConstructorElement(new ConstructorElementImpl((ConstructorElement)o, bounds),
                                              (ClassElement)owner.element);
            }
            throw new SourceException();
        }

        public Comparator[] getComparators() {
            return COMPARATORS;
        }

        public MemberElement createNewElement(MemberElementImpl impl) {
            return new ConstructorElement((ConstructorElementImpl) impl, (ClassElement)owner.element);
        }

        public void updateElementImpl(MemberElement element, MemberElementImpl impl, LinkedList changes, int changesMask) {
            ((ConstructorElementImpl)element.getCookie(ConstructorElementImpl.class)).updateImpl((ConstructorElementImpl)impl, changes, changesMask);
        }

    }

    // =================== Method =======================================

    /** Collection for methods.
    */
    static class Method extends Member {
        /** Empty array of methods. */
        private static final MethodElement[] EMPTY = new MethodElement[0];

        static final long serialVersionUID =5384079293590414029L;

        /** Comparators of methods */
        static final Comparator[] COMPARATORS = new Comparator[] {
                                                    new ConstructorComparator(true, true),
                                                    new ConstructorComparator(true, false),
                                                    new ConstructorComparator(false, true)
                                                };

        /**
        * @param owner owner of this array to fire changes to
        */
        public Method(ClassElementImpl owner) {
            super(owner);
        }

        /** @return the property name. */
        public String getPropertyName() {
            return PROP_METHODS;
        }

        /** @return the empty array of elements. */
        public Object[] getEmptyArray() {
            return EMPTY;
        }

        public int connectionsMask() {
            return JavaConnections.TYPE_METHODS;
        }

        Element createElement(Object o, PositionBounds bounds) throws SourceException {
            if (o instanceof MethodElement) {
                return new MethodElement(new MethodElementImpl((MethodElement)o, bounds),
                                         (ClassElement)owner.element);
            }
            throw new SourceException();
        }

        public Comparator[] getComparators() {
            return COMPARATORS;
        }

        public MemberElement createNewElement(MemberElementImpl impl) {
            return new MethodElement((MethodElementImpl) impl, (ClassElement)owner.element);
        }

        public void updateElementImpl(MemberElement element, MemberElementImpl impl, LinkedList changes, int changesMask) {
            ((MethodElementImpl)element.getCookie(MethodElementImpl.class)).updateImpl((MethodElementImpl)impl, changes, changesMask);
        }

    }

    // =================== Class =======================================

    /** Collection of classes.
    */
    static class Class extends Member {
        /** Empty array of classes. */
        private static final ClassElement[] EMPTY = new ClassElement[0];

        /** Comparators of classes */
        static final Comparator[] COMPARATORS = new Comparator[] {
                                                    new MemberNameComparator()
                                                };

        /**
        * @param owner owner of this array to fire changes to
        */
        public Class(ClassElementImpl owner) {
            super(owner);
        }

        static final long serialVersionUID =6106341163321060447L;
        /**
        * @param owner owner of this array to fire changes to
        */
        public Class(SourceElementImpl owner) {
            super(owner);
        }

        /** @return the property name. */
        public String getPropertyName() {
            return PROP_CLASSES;
        }

        /** @return the empty array of elements. */
        public Object[] getEmptyArray() {
            return EMPTY;
        }

        public int connectionsMask() {
            return JavaConnections.TYPE_CLASSES;
        }

        Element createElement(Object o, PositionBounds bounds) throws SourceException {
            if (o instanceof ClassElement) {
                ClassElementImpl newImpl = new ClassElementImpl((ClassElement) o, bounds);
                if (owner.element instanceof ClassElement)
                    return new ClassElement(newImpl, (ClassElement)owner.element);
                else
                    return new ClassElement(newImpl, (SourceElement)owner.element);
            }
            throw new SourceException();
        }

        public Comparator[] getComparators() {
            return COMPARATORS;
        }

        public void updateContent(final LinkedList newList, LinkedList changes, int changesMask) {
            boolean changed = false;
            if (newList.size() == 0) {
                if ((array == null) || (array.size() == 0))
                    return;
                array = null;
                changed = true;
            }
            else {
                ClassElement[] oldArray = new ClassElement[(array == null) ? 0 : array.size()];
                if (oldArray.length > 0)
                    array.toArray(oldArray);

                ParsingResult.Class[] classResultArray = new ParsingResult.Class[newList.size()];
                newList.toArray(classResultArray);

                ClassElementImpl[] newArray = new ClassElementImpl[newList.size()];
                for (int i = 0; i < classResultArray.length; i++)
                    newArray[i] = classResultArray[i].impl;

                int[] result = ElementsCollection.pairElements(oldArray, newArray, getComparators());
                if (oldArray.length == newArray.length) {
                    for (int i = 0; i < result.length; i++) {
                        if (result[i] != i) {
                            changed = true;
                            break;
                        }
                    }
                }
                else
                    changed = true;

                if (changed) {
                    boolean notifyAdd = ((changesMask & connectionsMask() & JavaConnections.ADD_MASK) != 0);
                    boolean notifyRemove = ((changesMask & connectionsMask() & JavaConnections.REMOVE_MASK) != 0);
                    LinkedList added = notifyAdd ? new LinkedList() : null;
                    LinkedList removed = notifyRemove ? new LinkedList() : null;
                    boolean[] remainFlags = new boolean[oldArray.length];

                    array = new LinkedList();
                    for (int i = 0; i < newArray.length; i++) {
                        ClassElement addingElement;
                        if (result[i] == -1) {
                            addingElement = createNewClassElement(newArray[i]);
                            newArray[i].initSubElements(classResultArray[i]);
                            if (notifyAdd) {
                                added.add(addingElement);
                            }
                        }
                        else {
                            addingElement = oldArray[result[i]];
                            ClassElementImpl updatingImpl =
                                (ClassElementImpl) addingElement.getCookie(ClassElementImpl.class);
                            updatingImpl.updateImpl(classResultArray[i], changes, changesMask);
                            remainFlags[result[i]] = true;
                        }
                        array.add(addingElement);
                    }

                    if (notifyAdd) {
                        Element[] addedElements = (Element[]) added.toArray(new Element[added.size()]);
                        //            System.out.println("ADD SIZE:"+addedElements.length); // NOI18N
                        //            System.out.println("mask:"+Integer.toHexString(connectionsMask() & JavaConnections.ADD_MASK)); // NOI18N
                        changes.add(new JavaConnections.Change(connectionsMask() & JavaConnections.ADD_MASK, addedElements));
                    }
                    for (int i = 0; i < remainFlags.length; i++) {
                        if (!remainFlags[i]) {
                            if (notifyRemove)
                                removed.add(oldArray[i]);
                            ((ElementImpl)oldArray[i].getCookie(ElementImpl.class)).setValid(false);
                        }
                    }
                    if (notifyRemove) {
                        changes.add(new JavaConnections.Change(connectionsMask() & JavaConnections.REMOVE_MASK,
                                                               (Element[])removed.toArray(new Element[removed.size()])));
                    }
                }
                else {
                    Iterator oldIter = array.iterator();
                    Iterator newIter = newList.iterator();
                    while (oldIter.hasNext()) {
                        ((ClassElementImpl)((ClassElement)oldIter.next()).getCookie(ClassElementImpl.class)).updateImpl((ParsingResult.Class) newIter.next(), changes, changesMask);
                    }
                }
            }

            if (changed)
                owner.firePropertyChange (this.getPropertyName(), null, null);
        }

        public MemberElement createNewElement(MemberElementImpl impl) {
            //not used
            return null;
        }

        public ClassElement createNewClassElement(ClassElementImpl impl) {
            Element parent = owner.element;
            if (parent instanceof SourceElement) {
                return new ClassElement((ClassElementImpl) impl, (SourceElement)parent);
            }
            else {
                return new ClassElement((ClassElementImpl) impl, (ClassElement)parent);
            }
        }

        public void updateElementImpl(MemberElement element, MemberElementImpl impl, LinkedList changes, int changesMask) {
            //not used
        }
    }

    // =================== Comparation routines ============================
    /** Tries to match old and new elements in the collection using (potentionally) several
      comparators.
      The element are matched using the first comparator at first, then the next one is tried on
      elements that weren't matched in the previous round etc.
    @param oldArray array of old elements
    @param newArray array of new elements. These should be either MemberElements or MemberElementImpls
    @param comparators array of comparators that will be used during matching
    @return array ordered like newArray that contains indexes into oldArray to a matching (old) element.
      If no matching old element is found, the entry has value -1.
    */
    static int[] pairElements(MemberElement[] oldArray, Object[] newArray, Comparator[] comparators) {
        int oldSize = oldArray.length;
        int newSize = newArray.length;

	if (newArray.length > 0 && 
	    !(newArray[0] instanceof MemberElementImpl || newArray[0] instanceof MemberElement)) {
	    throw new IllegalArgumentException("Got " + newArray[0].getClass());
	}

        int[] result = new int[newSize];
        for (int i = 0; i < newSize; i++)
            result[i] = -1;

        if ((oldSize == 0) || (newSize == 0))
            return result;

        int match = 0;
        BitSet used = new BitSet(oldArray.length);
        for (int k = 0; (k < comparators.length) && (match < newSize); k++) {
            Comparator comp = comparators[k];
            int i, j;
            for (i = 0, j = 0; (i < newSize) && (j < oldSize);) {
                if (used.get(j)) {
                    j++;
                    continue;
                }
                if (result[i] != -1) {
                    i++;
                    continue;
                }
                if (comp.compare(newArray[i], oldArray[j]) == 0) {
                    result[i] = j;
                    used.set(j);
                    match++;
                }
                i++;
                j++;
            }
            for (i = newSize - 1, j = oldSize - 1; (i >= 0) && (j >= 0);) {
                if (used.get(j)) {
                    j--;
                    continue;
                }
                if (result[i] != -1) {
                    i--;
                    continue;
                }
                if (comp.compare(newArray[i], oldArray[j]) == 0) {
                    result[i] = j;
                    used.set(j);
                    match++;
                }
                i--;
                j--;
            }
            for (i = 0; (i < newSize) && (match < newSize); i++) {
                if (result[i] != -1)
                    continue;

                for (j = 0; j < oldSize; j++) {
                    if (used.get(j))
                        continue;

                    if (comp.compare(newArray[i], oldArray[j]) == 0) {
                        result[i] = j;
                        used.set(j);
                        match++;
                    }
                }
            }
        }
        return result;
    }

    private static int compareNames(Identifier id, MemberElement e2) {
        return
            System.identityHashCode(id.getFullName()) -
            System.identityHashCode(e2.getName().getFullName());
    }

    private static int compareNames(MemberElementImpl e1, MemberElementImpl e2) {
        return
            System.identityHashCode(e1.name.getFullName()) -
            System.identityHashCode(e2.name.getFullName());
    }

    private static int compareParams(MethodParameter[] params1, ConstructorElement e2) {
        MethodParameter[] params2 = e2.getParameters();
        if (params1.length != params2.length)
            return params1.length - params2.length;

        for (int i = 0; i < params1.length; i++) {
            if (params1[i].compareTo(params2[i], true, false))
                continue;

            return params1[i].getType().hashCode() - params2[i].getType().hashCode();
        }
        return 0;
    }

    static class ConstructorComparator extends MemberNameComparator {
        boolean compareName;
        boolean compareParams;
        ConstructorComparator(boolean compareName, boolean compareParams) {
            this.compareName = compareName;
            this.compareParams = compareParams;
        }
        public int compare(Object o1, Object o2) {
            int result = 0;
            if (compareName) {
		result = super.compare(o1, o2);
                if (result != 0)
                    return result;
            }
            if (compareParams) {
		if (o1 instanceof ConstructorElementImpl) {
		    result = compareParams(((ConstructorElementImpl)o1).parameters, (ConstructorElement)o2);
		} else {
            	    result = compareParams(((ConstructorElement)o1).getParameters(), (ConstructorElement)o2);
		}
	    }
    	    return result;
        }
    }

    static class MemberNameComparator implements Comparator {
        public int compare(Object o1, Object o2) {
	    if (o1 instanceof MemberElementImpl) {
		return compareNames(((MemberElementImpl)o1).name, (MemberElement)o2);
	    } else {
		return compareNames(((MemberElement)o1).getName(), (MemberElement)o2);
	    }
        }
    }
}

/*
* Log
*  38   Gandalf-post-FCS1.32.2.4    4/14/00  Svatopluk Dedic Deletion from source is 
*       overridable in elementImpls
*  37   Gandalf-post-FCS1.32.2.3    4/4/00   Svatopluk Dedic Fixed concurrent modif. 
*       exception
*  36   Gandalf-post-FCS1.32.2.2    3/8/00   Svatopluk Dedic Checks for removed 
*       sub-elements; fires remove events; fires add/change/remove events for 
*       inner classes.
*  35   Gandalf-post-FCS1.32.2.1    2/24/00  Svatopluk Dedic Minor changes
*  34   Gandalf-post-FCS1.32.2.0    2/24/00  Ian Formanek    Post FCS changes
*  33   src-jtulach1.32        2/14/00  Svatopluk Dedic 
*  32   src-jtulach1.31        1/13/00  Petr Hamernik   i18n -(2nd round) - 
*       script bug fixed.
*  31   src-jtulach1.30        1/10/00  Petr Hamernik   regeneration of 
*       ClassElements improved (AKA #4536)
*  30   src-jtulach1.29        11/27/99 Patrik Knakal   
*  29   src-jtulach1.28        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  28   src-jtulach1.27        10/6/99  Miloslav Metelka NullPointerException in 
*       findElement()
*  27   src-jtulach1.26        9/15/99  Petr Hamernik   comparators are stored in
*       static final fields
*  26   src-jtulach1.25        9/13/99  Petr Hamernik   fixed bug #2430
*  25   src-jtulach1.24        8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  24   src-jtulach1.23        7/19/99  Petr Hamernik   findElement(int) 
*       implemented
*  23   src-jtulach1.22        6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  22   src-jtulach1.21        6/2/99   Petr Hamernik   connections of java 
*       sources
*  21   src-jtulach1.20        5/17/99  Petr Hamernik   missing implementation 
*       added
*  20   src-jtulach1.19        5/16/99  Petr Hamernik   fixed bug #1792
*  19   src-jtulach1.18        5/15/99  Petr Hamernik   fixed bug #1752
*  18   src-jtulach1.17        5/13/99  Petr Hamernik   changes in comparing 
*       Identifier, Type classes
*  17   src-jtulach1.16        5/12/99  Petr Hamernik   ide.src.Identifier 
*       changed
*  16   src-jtulach1.15        5/10/99  Petr Hamernik   
*  15   src-jtulach1.14        4/30/99  Petr Hamernik   
*  14   src-jtulach1.13        4/23/99  Petr Hamernik   Mutex synchr improved
*  13   src-jtulach1.12        4/23/99  Petr Hamernik   MUTEX synchronization 
*       changed
*  12   src-jtulach1.11        4/21/99  Petr Hamernik   Java module updated
*  11   src-jtulach1.10        4/16/99  Petr Hamernik   synchronization under 
*       Nodes.MUTEX
*  10   src-jtulach1.9         4/7/99   Petr Hamernik   synchronization improved
*  9    src-jtulach1.8         4/2/99   Petr Hamernik   
*  8    src-jtulach1.7         4/2/99   Petr Hamernik   
*  7    src-jtulach1.6         4/1/99   Petr Hamernik   
*  6    src-jtulach1.5         3/29/99  Petr Hamernik   
*  5    src-jtulach1.4         3/29/99  Petr Hamernik   
*  4    src-jtulach1.3         3/18/99  Petr Hamernik   
*  3    src-jtulach1.2         3/10/99  Petr Hamernik   
*  2    src-jtulach1.1         2/25/99  Petr Hamernik   
*  1    src-jtulach1.0         2/18/99  Petr Hamernik   
* $
*/
