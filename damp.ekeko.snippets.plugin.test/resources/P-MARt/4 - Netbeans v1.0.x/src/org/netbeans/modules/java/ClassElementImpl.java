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

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.text.StyledDocument;
import javax.swing.text.Position;

import org.openide.cookies.ConnectionCookie;
import org.openide.src.*;
import org.openide.text.*;
import org.openide.util.RequestProcessor;

/** Element that describes one class.
*
* @author Petr Hamernik
*/
final class ClassElementImpl extends MemberElementImpl implements ClassElement.Impl {
    /** is class or interface */
    boolean isClass;

    /** property, need not be initialized */
    Identifier superclass;

    /** Interfaces */
    Identifier[] interfaces;

    /** collection of initializers */
    ElementsCollection.Initializer initializers;

    /** collection of constructors */
    ElementsCollection.Constructor constructors;

    /** collection of methods */
    ElementsCollection.Method methods;

    /** collection of fields */
    ElementsCollection.Field fields;

    /** collection of classes */
    ElementsCollection.Class classes;

    /** This variable holds the reference back to SourceElementImpl.DataRef,
    * which is there refernced only using a SoftReference. So, when exists this
    * class it is impossible to garbage collected the data holding
    * information about the source.
    */
    Object hook;

    static final long serialVersionUID =-8212915557419039595L;

    /** Constructor for the parser.
    */
    public ClassElementImpl() {
    }

    /** Copy constructor.
    * @param el element to copy from
    */
    public ClassElementImpl(ClassElement el, PositionBounds bounds) throws SourceException {
        super(el, bounds);
        isClass = el.isClassOrInterface();
        superclass = el.getSuperclass();
        interfaces = el.getInterfaces();
        //    javadoc = new JavaDocImpl.Class(el.getJavaDoc().getRawText(), this);
        copyFrom(el);
        if (bounds != null)
            regenerate(el);
    }

    void updateImpl(ParsingResult.Class c, LinkedList changes, int changesMask) {
        updateImpl(c.impl, changes, changesMask);
        initCollections(c);

        if (initializers != null)
            initializers.updateContent(c.initializers, changes, changesMask);

        if (fields != null)
            fields.updateContent(c.fields, changes, changesMask);

        if (constructors != null)
            constructors.updateContent(c.constructors, changes, changesMask);

        if (methods != null)
            methods.updateContent(c.methods, changes, changesMask);

        if (classes != null)
            classes.updateContent(c.classes, changes, changesMask);
    }

    /** Redefine to supress firing of PROP_BODY after change of the class' text
    */
    protected Object getBodyHash() {
        return null;
    }


    private void initCollections(ParsingResult.Class c) {
        if (c.initializers.size() > 0)
            initInitializers();

        if (c.fields.size() > 0)
            initFields();

        if (c.constructors.size() > 0)
            initConstructors();

        if (c.methods.size() > 0)
            initMethods();

        if (c.classes.size() > 0)
            initClasses();
    }

    void initSubElements(ParsingResult.Class c) {
        ClassElement thisClassElement = (ClassElement) element;
        initCollections(c);
        Iterator it;

        it = c.initializers.iterator();
        while (it.hasNext())
            initializers.add(new InitializerElement((InitializerElementImpl) it.next(), thisClassElement));

        it = c.fields.iterator();
        while (it.hasNext())
            fields.add(new FieldElement((FieldElementImpl) it.next(), thisClassElement));

        it = c.constructors.iterator();
        while (it.hasNext())
            constructors.add(new ConstructorElement((ConstructorElementImpl) it.next(), thisClassElement));

        it = c.methods.iterator();
        while (it.hasNext())
            methods.add(new MethodElement((MethodElementImpl) it.next(), thisClassElement));

        it = c.classes.iterator();
        while (it.hasNext()) {
            ParsingResult.Class addingClass = (ParsingResult.Class) it.next();
            classes.add(new ClassElement(addingClass.impl, thisClassElement));
            addingClass.impl.initSubElements(addingClass);
        }
    }

    private void updateImpl(ClassElementImpl impl, LinkedList changes, int changesMask) {
        boolean changesMatch = ((changesMask & JavaConnections.TYPE_CLASSES_CHANGE) != 0);
        MemberElement prevElement = super.updateImpl(impl, changesMatch);

        if (isClass != impl.isClass) {
            if (changesMatch && (prevElement == null))
                prevElement = (ClassElement)(((ClassElement)element).clone());
            isClass = impl.isClass;
            firePropertyChange(PROP_CLASS_OR_INTERFACE, new Boolean(!isClass), new Boolean(isClass));
        }

        if ((superclass != impl.superclass) ||
                ((superclass != null) && !superclass.compareTo(impl.superclass, true))) {
            if (changesMatch && (prevElement == null))
                prevElement = (ClassElement)(((ClassElement)element).clone());
            Identifier old = superclass;
            superclass = impl.superclass;
            firePropertyChange(PROP_SUPERCLASS, old, superclass);
        }

        // Interfaces
        boolean changed = (impl.interfaces.length != interfaces.length);
        if (!changed) {
            for (int i = 0; i < interfaces.length; i++) {
                if (!interfaces[i].compareTo(impl.interfaces[i], true)) {
                    changed = true;
                    break;
                }
            }
        }
        if (changed) {
            if (changesMatch && (prevElement != null))
                prevElement = (ClassElement)(((ClassElement)element).clone());
            final Identifier[] old = interfaces;
            interfaces = impl.interfaces;
            firePropertyChange(PROP_INTERFACES, old, interfaces);

            // should be moved to InterfaceConnection
            if (isClass) {
                //        System.out.println ("isClass and has "+old.length+" => "+interfaces.length+" interfaces"); // NOI18N
                final boolean[] oldMatches = new boolean[old.length];
                final boolean[] newMatches = new boolean[interfaces.length];
                for (int i = 0; i < old.length; i++) {
                    //          System.out.println("compare:"+old[i]); // NOI18N
                    for (int j = 0; j < interfaces.length; j++) {
                        //            System.out.println("  with:"+interfaces[j]); // NOI18N
                        if (newMatches[j])
                            continue;
                        if (old[i].compareTo(interfaces[j], false)) {
                            //              System.out.println(" found."); // NOI18N
                            oldMatches[i] = true;
                            newMatches[j] = true;
                            break;
                        }
                    }
                }
                SourceElementImpl.PARSING_RP.post(new Runnable() {
                                                      public void run() {
                                                          for (int i = 0; i < old.length; i++) {
                                                              if (!oldMatches[i]) {
                                                                  //            System.out.println("unregister:"+old[i]); // NOI18N
                                                                  findSourceElementImpl().unregisterForName(old[i], JavaConnections.IMPLEMENTS);
                                                              }
                                                          }

                                                          LinkedList addedInterfaces = new LinkedList();
                                                          for (int i = 0; i < interfaces.length; i++) {
                                                              if (!newMatches[i]) {
                                                                  //            System.out.println("register:"+interfaces[i]); // NOI18N
                                                                  findSourceElementImpl().registerForName(interfaces[i], JavaConnections.IMPLEMENTS);
                                                                  addedInterfaces.add(interfaces[i]);
                                                              }
                                                          }
                                                          InterfaceConnection.interfacesAdded(
                                                              (Identifier[])addedInterfaces.toArray(new Identifier[0]),
                                                              (ClassElement) element,
                                                              findSourceElementImpl()
                                                          );
                                                      }
                                                  });
            }
        }

        if (changesMatch && (prevElement != null)) {
            changes.add(new JavaConnections.Change(JavaConnections.TYPE_CLASSES_CHANGE, prevElement, element));
        }
    }

    void checkInterfaces() {
        for (int i = 0; i < interfaces.length; i++) {
            //      System.out.println("register:"+interfaces[i]); // NOI18N
            findSourceElementImpl().registerForName(interfaces[i], JavaConnections.IMPLEMENTS);
        }
    }

    PositionBounds createBoundsFor(ElementsCollection col) throws SourceException {
        ElementsCollection[] cols = {
            initializers, fields, constructors, methods, classes
        };

        boolean after = false;
        for (int i = 0; i < cols.length; i++) {
            if (after && (cols[i] != null)) {
                Object o = cols[i].getFirst();
                if (o != null) {
                    ElementImpl impl = (ElementImpl)((Element) o).getCookie(ElementImpl.class);
		    PositionRef pos = findSourceElementImpl().findUnguarded(impl.bounds.getBegin(), 
			this.bodyBounds);
                    return SourceElementImpl.createBoundsAt(pos);
                }
                continue;
            }
            if (col == cols[i]) {
                after = true;
            }
        }
        PositionRef where = bounds.getBegin().getEditorSupport().createPositionRef(
	    bodyBounds.getEnd().getOffset(),
	    Position.Bias.Forward);
        return SourceElementImpl.createBoundsAt(where);
    }

    /** Late initialization of initialization of copy elements.
    */
    public void copyFrom(ClassElement copyFrom) throws SourceException {
        changeInitializers(copyFrom.getInitializers(), SET, true);
        changeConstructors(copyFrom.getConstructors(), SET, true);
        changeMethods(copyFrom.getMethods(), SET, true);
        changeFields(copyFrom.getFields(), SET, true);
        changeClasses(copyFrom.getClasses(), SET, true);
    }

    /** Getter for the associated class
    * @return the class element for this impl
    */
    final ClassElement getClassElement() {
        return (ClassElement)element;
    }

    /** Find the element at the specified offset in the document.
    * @param offset The position of the element
    * @return the element at the position.
    */
    public Element findElement(int offset) {
        for (int i = 0; i <= 4; i++) {
            ElementsCollection col = null;
            switch (i) {
            case 0: col = classes; break;
            case 1: col = methods; break;
            case 2: col = fields; break;
            case 3: col = constructors; break;
            case 4: col = initializers; break;
            }
            if (col != null) {
                Element retElement = col.findElement(offset);
                if (retElement != null)
                    return retElement;
            }
        }
        return element;
    }

    /** Setter
    */
    public void setSuperclass(Identifier superclass) throws SourceException {
        Identifier old = this.superclass;
        this.superclass = superclass;
        try {
            regenerateHeader();
            firePropertyChange(PROP_SUPERCLASS, old, superclass);
        }
        catch (SourceException e) {
            this.superclass = old;
            throw e;
        }
    }

    /** @return the superclass or empty if this element represents interface. */
    public Identifier getSuperclass() {
        return superclass;
    }

    public void setClassOrInterface(boolean isClass) throws SourceException {
        boolean old = this.isClass;
        this.isClass = isClass;
        try {
            regenerateHeader();
            firePropertyChange(PROP_CLASS_OR_INTERFACE, new Boolean(old), new Boolean(isClass));
        }
        catch (SourceException e) {
            this.isClass = old;
            throw e;
        }
    }

    /** @return true if this element represents the class otherwise
    * for the interfaces returns false.
    */
    public boolean isClassOrInterface() {
        return isClass;
    }

    // ========================= Interfaces ==================================

    /** Changes interfaces this class implements(or extends).
    * @param ids identifiers to change
    */
    public void changeInterfaces(Identifier[] ids, int action) throws SourceException {
        Identifier[] old = interfaces;

        switch (action) {
        case SET:
            interfaces = ids;
            break;
        case REMOVE:
            if ((ids.length == 0) || (old.length == 0))
                return;
            LinkedList list = new LinkedList();
            for (int i = 0; i < old.length; i++) {
                boolean shouldBeRemoved = false;
                for (int j = 0; j < ids.length; j++) {
                    if (old[i].compareTo(ids[j], false)) { // compare full name
                        shouldBeRemoved = true;
                        break;
                    }
                }
                if (!shouldBeRemoved)
                    list.add(old[i]);
            }
            interfaces = new Identifier[list.size()];
            list.toArray(interfaces);
            break;
        case ADD:
            if (ids.length == 0)
                return;
            interfaces = new Identifier[old.length + ids.length];
            System.arraycopy(old, 0, interfaces, 0, old.length);
            System.arraycopy(ids, 0, interfaces, old.length, ids.length);
            break;
        }

        try {
            regenerateHeader();
            firePropertyChange(PROP_INTERFACES, old, interfaces);
        }
        catch (SourceException e) {
            interfaces = old;
            throw e;
        }
    }

    /** @return all interfaces which the class implements or interface extends.
    */
    public synchronized Identifier[] getInterfaces() {
        Identifier[] ret = new Identifier[interfaces.length];
        System.arraycopy(interfaces, 0, ret, 0, interfaces.length);
        return ret;
    }

    // ========================= Initializers ==================================

    /** Changes set of elements.
    * @param elems elements to change
    * @param action the action to do (ADD, REMOVE, SET)
    * @exception SourceException if the action cannot be handled
    */
    public synchronized void changeInitializers(InitializerElement[] elems, int action) throws SourceException {
        changeInitializers(elems, action, false);
    }

    /** Changes set of elements.
    * @param elems elements to change
    * @param action the action to do (ADD, REMOVE, SET)
    * @exception SourceException if the action cannot be handled
    */
    private synchronized void changeInitializers(InitializerElement[] elems, int action, boolean skipBoundsCreation) throws SourceException {
        initInitializers();
        initializers.skipBoundsCreation = skipBoundsCreation;
        initializers.change(elems, action);
        initializers.skipBoundsCreation = false;
    }

    public synchronized InitializerElement[] getInitializers() {
        initInitializers();
        return (InitializerElement[])initializers.toArray();
    }

    void initInitializers() {
        if (initializers == null) {
            initializers = new ElementsCollection.Initializer(this);
        }
    }

    // ========================= Fields ==================================

    /** Changes set of elements.
    * @param elems elements to change
    * @exception SourceException if the action cannot be handled
    */
    public synchronized void changeFields(FieldElement[] elems, int action) throws SourceException {
        changeFields(elems, action, false);
    }

    /** Changes set of elements.
    * @param elems elements to change
    * @exception SourceException if the action cannot be handled
    */
    public synchronized void changeFields(FieldElement[] elems, int action, boolean skipBoundsCreation) throws SourceException {
        initFields();
        fields.skipBoundsCreation = skipBoundsCreation;
        fields.change(elems, action);
        fields.skipBoundsCreation = false;
    }

    public synchronized FieldElement[] getFields() {
        initFields();
        return (FieldElement[])fields.toArray();
    }

    /** Finds a field with given name.
    * @param name the name of field to look for
    * @return the element or null if field with such name does not exist
    */
    public synchronized FieldElement getField(Identifier name) {
        initFields();
        return (FieldElement)fields.find(name, null);
    }

    void initFields() {
        if (fields == null) {
            fields = new ElementsCollection.Field(this);
        }
    }

    // ========================= Methods ==================================

    /** Changes set of elements.
    * @param elems elements to change
    */
    public synchronized void changeMethods(MethodElement[] elems, int action) throws SourceException {
        changeMethods(elems, action, false);
    }

    /** Changes set of elements.
    * @param elems elements to change
    */
    public synchronized void changeMethods(MethodElement[] elems, int action, boolean skipBoundsCreation) throws SourceException {
        initMethods();
        methods.skipBoundsCreation = skipBoundsCreation;
        methods.change(elems, action);
        methods.skipBoundsCreation = skipBoundsCreation;
    }

    public MethodElement[] getMethods() {
        initMethods();
        return (MethodElement[])methods.toArray();
    }

    /** Finds a method with given name and argument types.
    * @param name the name of field to look for
    * @param arguments for the method
    * @return the element or null if such method does not exist
    */
    public synchronized MethodElement getMethod(Identifier name, Type[] arguments) {
        initMethods();
        return (MethodElement)methods.find(name, arguments);
    }

    void initMethods() {
        if (methods == null) {
            methods = new ElementsCollection.Method(this);
        }
    }

    // ========================= Constructors ==================================

    /** Changes set of elements.
    * @param elems elements to change
    * @exception SourceException if the action cannot be handled
    */
    public synchronized void changeConstructors(ConstructorElement[] elems, int action) throws SourceException {
        changeConstructors(elems, action, false);
    }

    /** Changes set of elements.
    * @param elems elements to change
    * @exception SourceException if the action cannot be handled
    */
    public synchronized void changeConstructors(ConstructorElement[] elems, int action, boolean skipBoundsCreation) throws SourceException {
        initConstructors();
        constructors.skipBoundsCreation = skipBoundsCreation;
        constructors.change(elems, action);
        constructors.skipBoundsCreation = false;
    }

    public synchronized ConstructorElement[] getConstructors() {
        initConstructors();
        return (ConstructorElement[])constructors.toArray();
    }

    /** Finds a constructor with argument types.
    * @param arguments for the method
    * @return the element or null if such method does not exist
    */
    public synchronized ConstructorElement getConstructor(Type[] arguments) {
        initConstructors();
        return (ConstructorElement)constructors.find(null, arguments);
    }

    void initConstructors() {
        if (constructors == null) {
            constructors = new ElementsCollection.Constructor(this);
        }
    }

    // ========================= InnerClasses ==================================

    /** Changes set of elements.
    * @param elems elements to change
    */
    public synchronized void changeClasses(ClassElement[] elems, int action) throws SourceException {
        changeClasses(elems, action, false);
    }

    /** Changes set of elements.
    * @param elems elements to change
    */
    public synchronized void changeClasses(ClassElement[] elems, int action, boolean skipBoundsCreation) throws SourceException {
        initClasses();
        classes.skipBoundsCreation = skipBoundsCreation;
        classes.change(elems, action);
        classes.skipBoundsCreation = false;
    }

    public synchronized ClassElement[] getClasses() {
        initClasses();
        return (ClassElement[])classes.toArray();
    }

    /** Finds an inner class with given name.
    * @param name the name to look for
    * @return the element or null if such class does not exist
    */
    public synchronized ClassElement getClass(Identifier name) {
        initClasses();
        return (ClassElement)classes.find(name, null);
    }

    void initClasses() {
        if (classes == null) {
            classes = new ElementsCollection.Class(this);
        }
    }

    // ================ javadoc =========================================

    /** @return class documentation.
    */
    public JavaDoc.Class getJavaDoc() {
        return (JavaDoc.Class) javadoc;
    }

    // ================ serialization ======================================

    public Object readResolve() {
        return new ClassElement(this, (SourceElement)null);
    }
}

/*
 * Log
 *  30   Gandalf-post-FCS1.27.1.1    4/18/00  Svatopluk Dedic Fixed firing property 
 *       changes
 *  29   Gandalf-post-FCS1.27.1.0    3/27/00  Svatopluk Dedic Disabled computation of 
 *       body hash; no PROP_BODY change event is ever fired.
 *  28   src-jtulach1.27        2/14/00  Svatopluk Dedic 
 *  27   src-jtulach1.26        1/13/00  Petr Hamernik   i18n -(2nd round) - 
 *       script bug fixed.
 *  26   src-jtulach1.25        1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  25   src-jtulach1.24        1/10/00  Petr Hamernik   regeneration of 
 *       ClassElements improved (AKA #4536)
 *  24   src-jtulach1.23        1/6/00   Petr Hamernik   fixed 4321
 *  23   src-jtulach1.22        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  22   src-jtulach1.21        10/7/99  Petr Hamernik   Java module has its own 
 *       RequestProcessor for source parsing.
 *  21   src-jtulach1.20        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  20   src-jtulach1.19        7/30/99  Petr Hamernik   hopefully fixed bugs 
 *       #2933 and #2943.
 *  19   src-jtulach1.18        7/19/99  Petr Hamernik   findElement(int) 
 *       implemented
 *  18   src-jtulach1.17        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  17   src-jtulach1.16        6/2/99   Petr Hamernik   connections of java 
 *       sources
 *  16   src-jtulach1.15        5/17/99  Petr Hamernik   missing implementation 
 *       added
 *  15   src-jtulach1.14        5/14/99  Petr Hamernik   getters improved (clone 
 *       the array before return)
 *  14   src-jtulach1.13        5/13/99  Petr Hamernik   changes in comparing 
 *       Identifier, Type classes
 *  13   src-jtulach1.12        5/12/99  Petr Hamernik   ide.src.Identifier 
 *       changed
 *  12   src-jtulach1.11        5/10/99  Petr Hamernik   
 *  11   src-jtulach1.10        4/28/99  Petr Hamernik   simple synchronization 
 *       using ConnectionCookie
 *  10   src-jtulach1.9         4/21/99  Petr Hamernik   Java module updated
 *  9    src-jtulach1.8         4/2/99   Petr Hamernik   
 *  8    src-jtulach1.7         4/1/99   Petr Hamernik   
 *  7    src-jtulach1.6         3/29/99  Petr Hamernik   
 *  6    src-jtulach1.5         3/29/99  Petr Hamernik   
 *  5    src-jtulach1.4         3/29/99  Petr Hamernik   
 *  4    src-jtulach1.3         3/15/99  Petr Hamernik   
 *  3    src-jtulach1.2         3/10/99  Petr Hamernik   
 *  2    src-jtulach1.1         2/25/99  Petr Hamernik   
 *  1    src-jtulach1.0         2/18/99  Petr Hamernik   
 * $
 */
