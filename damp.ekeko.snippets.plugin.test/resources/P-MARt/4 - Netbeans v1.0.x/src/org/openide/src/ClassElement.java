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
import java.util.*;
import java.text.MessageFormat;
import java.lang.reflect.Modifier;

import org.openide.TopManager;
import org.openide.cookies.SourceCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileObject;

/** Element that describes one class.
* Note that this is a member element--in fact, it may be either a
* top-level class (held in a source element), or a named inner class
* (held in another class element).
*
* @author Petr Hamernik, Jaroslav Tulach
*/
public final class ClassElement extends MemberElement {
    /** Constant indicating that the class is a real class.
    * @see #isClassOrInterface
    */
    public static final boolean CLASS = true;

    /** Constant indicating that the class is an interface.
    * @see #isClassOrInterface
    */
    public static final boolean INTERFACE = false;

    /** Default class to extend.
    * That is, <code>Object</code>.
    */
    public static final Identifier ROOT_OBJECT = Identifier.create("java.lang.Object", "Object"); // NOI18N

    /** Formats for the header - used in code generator */
    private static final ElementFormat[] HEADER_FORMAT = {
        new ElementFormat("{m,,\" \"}class {n}{s,\" extends \",}{i,\" implements \",}"), // NOI18N
        new ElementFormat("{m,,\" \"}interface {n}{i,\" extends \",}") // NOI18N
    };

    /** source element we are attached to */
    private SourceElement source;

    //================ Constructors of ClassElement =================

    static final long serialVersionUID =1692944638104452533L;

    /** Create a new class element in memory.
    */
    public ClassElement() {
        this(new Memory (), null, null);
    }

    /** Factory constructor for defining embedded classes.
    *
    * @param impl implementation of functionality
    * @param clazz the declaring class, or <code>null</code>
    */
    public ClassElement(Impl impl, ClassElement clazz) {
        this (impl, clazz, clazz.getSource ());
    }

    /** Factory constructor for defining top level classes.
    * @param impl implementation of functionality
    * @param source the source file this class is contained in, or <code>null</code>
    */
    public ClassElement(Impl impl, SourceElement source) {
        this (impl, null, source);
    }

    /**
    * @param impl implementation of functionality
    * @param clazz the declaring class
    * @param source the source file to be presented in
    */
    private ClassElement(Impl impl, ClassElement clazz, SourceElement source) {
        super (impl, clazz);
        this.source = source;
    }

    /** Clone this element.
    * @return new element with the same values as the original,
    *   but represented in memory
    */
    public Object clone () {
        Memory mem = new Memory (this);
        ClassElement el = new ClassElement (mem, null, null);
        mem.copyFrom (this);
        return el;
    }

    /** @return implemetation factory for this class
    */
    final Impl getClassImpl () {
        return (Impl)impl;
    }

    /** Implemented in ClassElement - update names of the constructors.
    */
    void updateConstructorsNames(Identifier name) throws SourceException {
        ConstructorElement[] c = getConstructors();
        Identifier constrName = Identifier.create(name.getName());
        for (int i = 0; i < c.length; i++)
            c[i].setName(constrName);
    }

    //================ Main properties ==============================

    /** Get the source element of this class.
    * @return the source, or <code>null</code> if the class is not attached to any source
    */
    public SourceElement getSource () {
        return source;
    }

    /** Set whether this is really a class, or an interface.
    * @param isClass one of {@link #CLASS} or {@link #INTERFACE}
    * @throws SourceException if impossible
    */
    public void setClassOrInterface(boolean isClass) throws SourceException {
        getClassImpl ().setClassOrInterface (isClass);
    }

    /** Test whether this is really a class, or an interface.
    * @return one of {@link #CLASS} or {@link #INTERFACE}
    */
    public boolean isClassOrInterface() {
        return getClassImpl ().isClassOrInterface ();
    }

    /** Test whether this is really a class.
    * @return <code>true</code> if so
    * @see #isClassOrInterface
    */
    public boolean isClass() {
        return getClassImpl ().isClassOrInterface ();
    }

    /** Test whether this is an interface.
    * @return <code>true</code> if so
    * @see #isClassOrInterface
    */
    public boolean isInterface() {
        return !getClassImpl ().isClassOrInterface ();
    }

    /** Test if this is an inner class.
    * If so, it has a declaring class.
    * @return <code>true</code> if so
    * @see #ClassElement(ClassElement.Impl, ClassElement)
    */
    public boolean isInner() {
        return (getDeclaringClass() != null);
    }

    /* Get the modifiers of the class.
    * @return the mask of possible modifiers for this element
    * @see Modifier
    */
    public int getModifiersMask() {
        int ret = Modifier.PUBLIC | Modifier.ABSTRACT;

        if (isClass()) {
            ret |= Modifier.FINAL;
        }

        if (isInner()) {
            ret |= Modifier.PROTECTED | Modifier.PRIVATE |
                   Modifier.STATIC | Modifier.FINAL;
        }
        return ret;
    }

    /** Set the name of this member.
    * @param name the name
    * @throws SourceException if impossible
    */
    public final void setName(Identifier name) throws SourceException {
        ClassElement c = getDeclaringClass();
        String msg = null;
        if (c != null) {
            ClassElement c1 = c.getClass(name);
            if ((c1 != null) && (c1 != this)) {
                MessageFormat format = new MessageFormat(ElementFormat.bundle.getString("FMT_EXC_RenameClass"));
                msg = format.format(new Object[] { c.getName().getName(), name });
            }
        }
        else {
            if (source != null) {
                ClassElement c1 = source.getClass(name);
                if ((c1 != null) && (c1 != this)) {
                    MessageFormat format = new MessageFormat(ElementFormat.bundle.getString("FMT_EXC_RenameClassInSource"));
                    msg = format.format(new Object[] { name });
                }
            }
        }
        if (msg != null) {
            throw new SourceException(msg);
        }
        super.setName(name);
    }

    // ================= Super class =======================

    /** Set the superclass of this class.
    * @param superClass the superclass
    * @throws SourceException if that is impossible
    */
    public void setSuperclass(Identifier superClass) throws SourceException {
        getClassImpl ().setSuperclass (superClass);
    }

    /** Get the superclass of this class.
    * @return superclass identifier, or <code>null</code> (for interfaces, or possibly for classes with superclass <code>Object</code>)
    */
    public Identifier getSuperclass() {
        return getClassImpl ().getSuperclass ();
    }

    // ================= Initializers ========================

    /** Add a new initializer block to this class.
    * @param el the block to add
    * @throws SourceException if impossible
    */
    public void addInitializer (InitializerElement el) throws SourceException {
        getClassImpl ().changeInitializers (
            new InitializerElement[] { el }, Impl.ADD
        );
    }

    /** Add some initializer blocks to this class.
    *  @param els the blocks to add
    * @throws SourceException if impossible
    */
    public void addInitializers (final InitializerElement[] els) throws SourceException {
        getClassImpl ().changeInitializers (els, Impl.ADD);
    }

    /** Remove an initializer block from this class.
    *  @param el the block to remove
    * @throws SourceException if impossible
    */
    public void removeInitializer (InitializerElement el) throws SourceException {
        getClassImpl ().changeInitializers (
            new InitializerElement[] { el }, Impl.REMOVE
        );
    }

    /** Remove some initializer blocks from this class.
    *  @param els the blocks to remove
    * @throws SourceException if impossible
    */
    public void removeInitializers (final InitializerElement[] els) throws SourceException {
        getClassImpl ().changeInitializers (els, Impl.REMOVE);
    }

    /** Set the initializer blocks for this class.
    * Any previous ones are just removed.
    *  @param els the new blocks
    * @throws SourceException if impossible
    */
    public void setInitializers (InitializerElement[] els) throws SourceException {
        getClassImpl ().changeInitializers (els, Impl.SET);
    }

    /** Get all the initializer blocks for this class.
    * @return all the blocks
    */
    public InitializerElement[] getInitializers () {
        return getClassImpl ().getInitializers ();
    }

    //================== Fields ===============================

    /** Add a new field to the class.
    *  @param el the field to add
    * @throws SourceException if impossible
    */
    public void addField (FieldElement el) throws SourceException {
        if (getField(el.getName()) != null)
            throwAddException("FMT_EXC_AddField", el); // NOI18N
        getClassImpl ().changeFields (new FieldElement[] { el }, Impl.ADD);
    }

    /** Add some new fields to the class.
    *  @param els the fields to add
    * @throws SourceException if impossible
    */
    public void addFields (final FieldElement[] els) throws SourceException {
        for (int i = 0; i < els.length; i++)
            if (getField(els[i].getName()) != null)
                throwAddException("FMT_EXC_AddField", els[i]); // NOI18N
        getClassImpl ().changeFields (els, Impl.ADD);
    }

    /** Remove a field from the class.
    *  @param el the field to remove
    * @throws SourceException if impossible
    */
    public void removeField (FieldElement el) throws SourceException {
        getClassImpl ().changeFields (
            new FieldElement[] { el }, Impl.REMOVE
        );
    }

    /** Remove some fields from the class.
    *  @param els the fields to remove
    * @throws SourceException if impossible
    */
    public void removeFields (final FieldElement[] els) throws SourceException {
        getClassImpl ().changeFields (els, Impl.REMOVE);
    }

    /** Set the fields for this class.
    * Previous fields are removed.
    * @param els the new fields
    * @throws SourceException if impossible
    */
    public void setFields (FieldElement[] els) throws SourceException {
        getClassImpl ().changeFields (els, Impl.SET);
    }

    /** Get all fields in this class.
    * @return the fields
    */
    public FieldElement[] getFields () {
        return getClassImpl ().getFields ();
    }

    /** Find a field by name.
    * @param name the name of the field to look for
    * @return the element or <code>null</code> if not found
    */
    public FieldElement getField (Identifier name) {
        return getClassImpl ().getField (name);
    }


    //================== Methods =================================

    /** Add a method to this class.
    *  @param el the method to add
    * @throws SourceException if impossible
    */
    public void addMethod (MethodElement el) throws SourceException {
        testMethod(el);
        getClassImpl ().changeMethods (new MethodElement[] { el }, Impl.ADD);
    }

    /** Add some methods to this class.
    *  @param els the methods to add
    * @throws SourceException if impossible
    */
    public void addMethods (final MethodElement[] els) throws SourceException {
        for (int i = 0; i < els.length; i++)
            testMethod(els[i]);
        getClassImpl ().changeMethods (els, Impl.ADD);
    }

    /** Test if the specified method already exists in the class.
    * @param el The tested method
    * @exception SourceException if method already exists in the class
    */
    private void testMethod(MethodElement el) throws SourceException {
        MethodParameter[] params = el.getParameters();
        Type[] types = new Type[params.length];
        for (int i = 0; i < types.length; i++)
            types[i] = params[i].getType();
        if (getMethod(el.getName(), types) != null)
            throwAddException("FMT_EXC_AddMethod", el); // NOI18N
    }

    /** Remove a method from this class.
    *  @param el the method to remove
    * @throws SourceException if impossible
    */
    public void removeMethod (MethodElement el) throws SourceException {
        getClassImpl ().changeMethods (
            new MethodElement[] { el }, Impl.REMOVE
        );
    }

    /** Remove some methods from this class.
    *  @param els the methods to remove
    * @throws SourceException if impossible
    */
    public void removeMethods (final MethodElement[] els) throws SourceException {
        getClassImpl ().changeMethods (els, Impl.REMOVE);
    }

    /** Set the methods for this class.
    * The old ones are removed.
    * @param els the new methods
    * @throws SourceException if impossible
    */
    public void setMethods (MethodElement[] els) throws SourceException {
        getClassImpl ().changeMethods (els, Impl.SET);
    }

    /** Get all methods in this class.
    * @return the methods
    */
    public MethodElement[] getMethods () {
        return getClassImpl ().getMethods ();
    }

    /** Find a method by signature.
    * @param name the method name to look for
    * @param arguments the argument types to look for
    * @return the method, or <code>null</code> if it was not found
    */
    public MethodElement getMethod (Identifier name, Type[] arguments) {
        return getClassImpl ().getMethod (name, arguments);
    }


    //================== Constructors ============================

    /** Add a constructor to this class.
    *  @param el the constructor to add
    * @throws SourceException if impossible
    */
    public void addConstructor (ConstructorElement el) throws SourceException {
        testConstructor(el);
        getClassImpl ().changeConstructors (new ConstructorElement[] { el }, Impl.ADD);
    }

    /** Add some constructors to this class.
    *  @param els the constructors to add
    * @throws SourceException if impossible
    */
    public void addConstructors (final ConstructorElement[] els) throws SourceException {
        for (int i = 0; i < els.length; i++)
            testConstructor(els[i]);
        getClassImpl ().changeConstructors (els, Impl.ADD);
    }

    /** Test if the specified constructor already exists in the class.
    * @param el The tested constuctor
    * @exception SourceException if constructor already exists in the class
    */
    private void testConstructor(ConstructorElement el) throws SourceException {
        MethodParameter[] params = el.getParameters();
        Type[] types = new Type[params.length];
        for (int i = 0; i < types.length; i++)
            types[i] = params[i].getType();
        if (getConstructor(types) != null)
            throwAddException("FMT_EXC_AddConstructor", el); // NOI18N
    }

    /** Remove a constructor from this class.
    *  @param el the constructor to remove
    * @throws SourceException if impossible
    */
    public void removeConstructor (ConstructorElement el) throws SourceException {
        getClassImpl ().changeConstructors (
            new ConstructorElement[] { el }, Impl.REMOVE
        );
    }

    /** Remove some constructors from this class.
    *  @param els the constructors to remove
    * @throws SourceException if impossible
    */
    public void removeConstructors (final ConstructorElement[] els) throws SourceException {
        getClassImpl ().changeConstructors (els, Impl.REMOVE);
    }

    /** Set the constructors for this class.
    * The old ones are replaced.
    * @param els the new constructors
    * @throws SourceException if impossible
    */
    public void setConstructors (ConstructorElement[] els) throws SourceException {
        getClassImpl ().changeConstructors (els, Impl.SET);
    }

    /** Get all constructors in this class.
    * @return the constructors
    */
    public ConstructorElement[] getConstructors () {
        return getClassImpl ().getConstructors ();
    }

    /** Find a constructor by signature.
    * @param arguments the argument types to look for
    * @return the constructor, or <code>null</code> if it does not exist
    */
    public ConstructorElement getConstructor (Type[] arguments) {
        return getClassImpl ().getConstructor (arguments);
    }


    //================== Inner classes ==========================

    /** Add a new inner class to this class.
    * @param el the inner class to add
    * @throws SourceException if impossible
    */
    public void addClass (ClassElement el) throws SourceException {
        if (getClass(el.getName()) != null)
            throwAddException("FMT_EXC_AddClass", el); // NOI18N
        getClassImpl().changeClasses(new ClassElement[] { el }, Impl.ADD);
    }

    /** Add some new inner classes to this class.
    * @param el the inner classes to add
    * @throws SourceException if impossible
    */
    public void addClasses (final ClassElement[] els) throws SourceException {
        for (int i = 0; i < els.length; i++) {
            if (getClass(els[i].getName()) != null)
                throwAddException("FMT_EXC_AddClass", els[i]); // NOI18N
        }
        getClassImpl ().changeClasses (els, Impl.ADD);
    }

    /** Remove an inner class from this class.
    * @param el the inner class to remove
    * @throws SourceException if impossible
    */
    public void removeClass (ClassElement el) throws SourceException {
        getClassImpl ().changeClasses (new ClassElement[] { el }, Impl.REMOVE);
    }

    /** Remove some inner classes from this class.
    * @param els the inner classes to remove
    * @throws SourceException if impossible
    */
    public void removeClasses (final ClassElement[] els) throws SourceException {
        getClassImpl ().changeClasses (els, Impl.REMOVE);
    }

    /** Set the inner classes for this class.
    * The old ones are replaced.
    * @param els the new inner classes
    * @throws SourceException if impossible
    */
    public void setClasses (ClassElement[] els) throws SourceException {
        getClassImpl ().changeClasses (els, Impl.SET);
    }

    /** Get all inner classes for this class.
    * @return the inner classes
    */
    public ClassElement[] getClasses () {
        return getClassImpl ().getClasses ();
    }

    /** Find an inner class by name.
    * @param name the name to look for
    * @return the inner class, or <code>null</code> if it does not exist
    */
    public ClassElement getClass (Identifier name) {
        return getClassImpl ().getClass (name);
    }


    //================== Implements =============================

    /** Add an interface to this class.
    * I.e., one which this class will implement.
    * @param in the interface to add, by name
    * @throws SourceException if impossible
    */
    public void addInterface(Identifier in) throws SourceException {
        getClassImpl ().changeInterfaces (new Identifier [] { in }, Impl.ADD);
    }

    /** Add some interfaces to this class.
    * @param ins the interfaces to add, by name
    * @throws SourceException if impossible
    */
    public void addInterfaces(final Identifier[] ins) throws SourceException {
        getClassImpl ().changeInterfaces (ins, Impl.ADD);
    }

    /** Remove an interface from this class.
    * @param in the interface to remove. by name
    * @throws SourceException if impossible
    */
    public void removeInterfaces(Identifier in) throws SourceException {
        getClassImpl ().changeInterfaces (new Identifier [] { in }, Impl.REMOVE);
    }

    /** Remove some interfaces from this class.
    * @param ins the interfaces to remove, by name
    * @throws SourceException if impossible
    */
    public void removeInterface(final Identifier[] ins) throws SourceException {
        getClassImpl ().changeInterfaces (ins, Impl.REMOVE);
    }

    /** Set the interfaces for this class.
    * The old ones are replaced.
    * @param ids the new interfaces, by name
    * @throws SourceException if impossible
    */
    public void setInterfaces(Identifier[] ids) throws SourceException {
        getClassImpl ().changeInterfaces (ids, Impl.SET);
    }

    /** Get all interfaces for this class.
    * I.e., all interfaces which this class implements (directly),
    * or (if an interface) all interfaces which it extends (directly).
    * @return the interfaces
    */
    public Identifier[] getInterfaces () {
        return getClassImpl ().getInterfaces ();
    }

    // ================ javadoc =========================================

    /** Get the class documentation.
    * @return the JavaDoc
    */
    public JavaDoc.Class getJavaDoc() {
        return getClassImpl ().getJavaDoc ();
    }

    // ================ printing =========================================

    /* Print this element to an element printer.
    * @param printer the printer to print to
    * @exception ElementPrinterInterruptException if the printer cancels the printing
    */
    public void print(ElementPrinter printer) throws ElementPrinterInterruptException {
        printer.markClass(this, printer.ELEMENT_BEGIN);

        JavaDoc doc = getJavaDoc();
        if ((doc != null) && !doc.isEmpty()) {
            printer.markClass(this, printer.JAVADOC_BEGIN); // JAVADOC begin
            printJavaDoc(doc, printer);
            printer.markClass(this, printer.JAVADOC_END); // JAVADOC end
            printer.println(""); // NOI18N
        }

        printer.markClass(this, printer.HEADER_BEGIN); // HEADER begin
        printer.print(isClass() ?
                      HEADER_FORMAT[0].format(this) :
                      HEADER_FORMAT[1].format(this)
                     );
        printer.markClass(this, printer.HEADER_END); // HEADER end

        printer.println(" {"); // NOI18N
        printer.markClass(this, printer.BODY_BEGIN); // BODY begin

        if (print(getInitializers(), printer)) {
            printer.println(""); // NOI18N
            printer.println(""); // NOI18N
        }

        if (print(getFields(), printer)) {
            printer.println(""); // NOI18N
            printer.println(""); // NOI18N
        }

        if (print(getConstructors(), printer)) {
            printer.println(""); // NOI18N
            printer.println(""); // NOI18N
        }

        if (print(getMethods(), printer)) {
            printer.println(""); // NOI18N
            printer.println(""); // NOI18N
        }

        print(getClasses(), printer);

        printer.markClass(this, printer.BODY_END); // BODY end

        printer.println(""); // NOI18N
        printer.print("}"); // NOI18N

        printer.markClass(this, printer.ELEMENT_END);
    }

    // ================ misc =========================================

    /** This method just throws localized exception. It is used during
    * adding some element, which already exists in class.
    * @param formatKey The message format key to localized bundle.
    * @param element The element which can't be added
    * @exception SourceException is alway thrown from this method.
    */
    private void throwAddException(String formatKey, MemberElement element) throws SourceException {
        MessageFormat format = new MessageFormat(ElementFormat.bundle.getString(formatKey));
        String msg = format.format(new Object[] { getName().getName(), element.getName().getName() });
        throw new SourceException(msg);
    }

    /** Test whether this class has a declared main method
    * and so may be executed directly.
    * @return <CODE>true</CODE> if this class contains a
    *         <CODE>public static void main(String[])</CODE>
    *          method, otherwise <CODE>false</CODE>
    */
    public boolean hasMainMethod() {
        MethodElement[] methods = getMethods();
        Identifier mainId = Identifier.create("main"); // NOI18N

        for (int i = 0; i < methods.length; i++) {
            MethodElement m = methods[i];
            if (m.getName().equals(mainId)) {
                if (m.getReturn() == Type.VOID) {
                    if ((m.getModifiers() & ~Modifier.FINAL) == Modifier.PUBLIC + Modifier.STATIC) {
                        MethodParameter[] params = m.getParameters();
                        if (params.length == 1) {
                            Type typ = params[0].getType();
                            if (typ.isArray()) {
                                typ = typ.getElementType();
                                if (typ.isClass()) {
                                    if (typ.getClassName().getFullName().equals("java.lang.String")) { // NOI18N
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    // [PENDING] mustn't it also implement Serializable? --jglick
    /** Test whether this class is a JavaBean.
    * It must:
    * <UL>
    * <LI> be declared as public
    * <LI> not be declared as abstract or an interface
    * <LI> have a default public constructor
    * </UL>
    * Note that these are technical requirements: satisfying them
    * does not imply that the class is really <em>intended</em>
    * to be used as a JavaBean; this could have been accidental.
    * @return <CODE>true</CODE> if this class could be a JavaBean,
    *         otherwise <CODE>false</CODE>
    */
    public boolean isDeclaredAsJavaBean() {
        if (Modifier.isPublic(getModifiers()) && !Modifier.isAbstract(getModifiers())) {
            if (isClass()) {
                ConstructorElement[] constructors = getConstructors();
                for (int i = 0; i < constructors.length; i++) {
                    if (constructors[i].getParameters().length == 0)
                        if (Modifier.isPublic(constructors[i].getModifiers())) {
                            return true;
                        }
                }
                return (constructors.length == 0);
            }
        }
        return false;
    }

    // [PENDING] concrete? public? what about extending [J]Applet indirectly? --jglick
    /** Test whether this class is an applet.
    * It must:
    * <UL>
    * <LI> be a class (not an interface)
    * <LI> extend either {@link java.applet.Applet} or {@link javax.swing.JApplet}
    * </UL>
    * @return <CODE>true</CODE> if this class could be an applet,
    *         otherwise <CODE>false</CODE>.
    */
    public boolean isDeclaredAsApplet() {
        if (isClass()) {
            Identifier superclass = getSuperclass();
            if (superclass != null) {
                String name = superclass.getFullName();
                return name.equals("java.applet.Applet") || name.equals("javax.swing.JApplet"); // NOI18N
            }
        }
        return false;
    }

    // ================ finders =======================================

    /** List of finders 
     * @associates Finder*/
    private static List finders = new LinkedList();

    /** Register a new finder for locating class elements.
    * @param f the finder to add
    */
    public static void register (Finder f) {
        synchronized (finders) {
            finders.add (f);
        }
    }

    /** Unregister a finder for locating class elements.
    * @param f the finder to remove
    */
    public static void unregister (Finder f) {
        synchronized (finders) {
            finders.remove (f);
        }
    }

    /** Search for a class element throughout the system.
    * First goes through repository and find the appropriate DataObject
    * Tests if it has SourceCookie and if there exist the right one,
    * it is returned. Otherwise tries to creates the Class.forName
    * and asks all registered finders if they know where to find such a class.
    *
    * @param name class name separated by dots, e.g. <code>java.lang.String</code>.
    *             For inner classes is accepted delimiting by '$' or by '.'
    *             e.g. inner class A.B in package org.netbeans.test could
    *             be specified like: org.netbeans.A.B or org.netbeans.A$B
    *             Both possibilities are accepted.
    * @return class element for that name, or <code>null</code> if none exists
    * @see ClassElement.Finder
    */
    public static ClassElement forName(String name) {
        String p; // package
        String n; // name

        Repository rep = TopManager.getDefault ().getRepository ();

        // 1. find XXX.class file
        int index = name.lastIndexOf ('.');
        if (index >= 0 && index <= name.length()) {
            p = name.substring (0, index);
            n = name.substring (index + 1);
        }
        else {
            p = ""; // NOI18N
            n = name;
        }
        FileObject fo = rep.find (p, n, "class"); // NOI18N
        ClassElement element = testFileForName(fo, n);
        if (element != null)
            return element;

        // 2. find XXX.java (e.g. org.netbeans.Outer.Inner)
        index = n.indexOf('$');
        if (index != -1) {
            //2a. find inner if it is delimited by '$' e.g. org.netbeans.Outer$Inner
            fo = rep.find(p, n.substring(0, index), "java"); // NOI18N
            element = testFileForName(fo, n);
            if (element != null)
                return element;
        }
        else {
            // 2b. find e.g. for org.netbeans.Outer.Inner
            //     => tries all: package            - className
            //                   ---------------------------------
            //                1) org.netbeans.Outer - Inner
            //                2) org.netbeans       - Outer$Inner
            //                3) com                - netbeans$Outer$Inner
            String nn = n;
            for (;;) {
                fo = rep.find(p, nn, "java"); // NOI18N
                element = testFileForName(fo, n);
                if (element != null)
                    return element;
                index = p.lastIndexOf('.');
                if ((index >= 0) && index <= p.length()) {
                    nn = p.substring(index + 1);
                    n = nn + "." + n; // NOI18N
                    p = p.substring (0, index);
                }
                else {
                    break;
                }
            }
        }

        // 3. find Class.forName() and calls forClass
        try {
            Class clazz = Class.forName(name);
            return forClass(clazz);
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }

    /** Test whether for the specified fileObject exists dataobject,
    * which has SourceCookie and contains the class of the given name.
    * @param fo The tested fileObject, could be null
    * @param name the name of class which should be in the source.
    * @return The classElement for the given parameters or <CODE>null</CODE>.
    */
    private static ClassElement testFileForName(FileObject fo, String name) {
        if (fo != null) {
            try {
                DataObject obj = DataObject.find (fo);
                SourceCookie sc = (SourceCookie) obj.getCookie (SourceCookie.class);
                if (sc != null) {
                    ClassElement[] arr = sc.getSource ().getClasses ();
                    StringTokenizer tukac = new StringTokenizer(name, "$."); // NOI18N
                    while (tukac.hasMoreTokens()) {
                        String token = tukac.nextToken();
                        ClassElement c = null;
                        for (int i = 0; i < arr.length; i++) {
                            Identifier id = arr[i].getName();
                            if (id.getName().equals(token)) {
                                if (!tukac.hasMoreTokens()) {
                                    return arr[i];
                                }
                                else {
                                    c = arr[i];
                                    break;
                                }
                            }
                        }
                        if (c == null)
                            return null;
                        else
                            arr = c.getClasses();
                    }
                }
            }
            catch (DataObjectNotFoundException e) {
            }
        }
        return null;
    }

    /** Search for a class element throughout the system.
    * Asks all registered finders if they know where to find such a class.
    * @param name class name separated by dots, e.g. <code>java.lang.String</code>
    * @return class element for that name, or <code>null</code> if none exists
    * @see ClassElement.Finder
    */
    public static ClassElement forClass(Class clazz) {
        synchronized (finders) {
            Iterator it = finders.iterator ();
            while (it.hasNext ()) {
                ClassElement el = ((Finder)it.next()).find (clazz);
                if (el != null)
                    return el;
            }
        }
        return null;
    }

    /** Provides a "finder" for class elements.
    * A module can provide its own finder to enhance the ability
    * of the IDE to locate a valid class element description for different classes.
    * @see ClassElement#forName
    * @see ClassElement#register
    * @see ClassElement#unregister
    */
    public static interface Finder {
        /** Find a class element description for a class.
        *
        * @param clazz the class to find
        * @return the class element, or <code>null</code> if not found
        */
        public ClassElement find(Class clazz);
    }

    // ================ implementation ===================================

    /** Pluggable behavior for class elements.
    * @see ClassElement
    */
    public static interface Impl extends MemberElement.Impl {
        /** Add some items. */
        public static final int ADD = 1;
        /** Remove some items. */
        public static final int REMOVE = -1;
        /** Set some items, replacing the old ones. */
        public static final int SET = 0;

        static final long serialVersionUID =2564194659099459416L;

        /** Set the superclass for this class.
        * @param superClass the superclass, by name
        * @throws SourceException if impossible
        */
        public void setSuperclass(Identifier superClass) throws SourceException;

        /** Get the superclass for this class.
        * @return the superclass, by name
        */
        public Identifier getSuperclass();

        /** Set whether this is a class or interface.
        * @param isClass either {@link ClassElement#CLASS} or {@link ClassElement#INTERFACE}
        * @throws SourceException if impossible
        */
        public void setClassOrInterface(boolean isClass) throws SourceException;

        /** Test whether this is a class or interface.
        * @return either {@link ClassElement#CLASS} or {@link ClassElement#INTERFACE}
        */
        public boolean isClassOrInterface();

        /** Change the set of initializers.
        * @param elems the new initializers
        * @param action {@link #ADD}, {@link #REMOVE}, or {@link #SET}
        * @exception SourceException if impossible
        */
        public void changeInitializers (InitializerElement[] elems, int action) throws SourceException;

        /** Get all initializers.
        * @return the initializers
        */
        public InitializerElement[] getInitializers ();

        /** Change the set of fields.
        * @param elems the new fields
        * @param action {@link #ADD}, {@link #REMOVE}, or {@link #SET}
        * @exception SourceException if impossible
        */
        public void changeFields (FieldElement[] elems, int action) throws SourceException;

        /** Get all fields.
        * @return the fields
        */
        public FieldElement[] getFields ();

        /** Find a field by name.
        * @param name the name to look for
        * @return the field, or <code>null</code> if it does not exist
        */
        public FieldElement getField (Identifier name);

        /** Change the set of methods.
        * @param elems the new methods
        * @param action {@link #ADD}, {@link #REMOVE}, or {@link #SET}
        * @exception SourceException if impossible
        */
        public void changeMethods (MethodElement[] elems, int action) throws SourceException;

        /** Get all methods.
        * @return the methods
        */
        public MethodElement[] getMethods ();

        /** Finds a method by signature.
        * @param name the name to look for
        * @param arguments the argument types to look for
        * @return the method, or <code>null</code> if it does not exist
        */
        public MethodElement getMethod (Identifier name, Type[] arguments);

        /** Change the set of constructors.
        * @param elems the new constructors
        * @param action {@link #ADD}, {@link #REMOVE}, or {@link #SET}
        * @exception SourceException if impossible
        */
        public void changeConstructors (ConstructorElement[] elems, int action) throws SourceException;

        /** Get all constructors.
        * @return the constructors
        */
        public ConstructorElement[] getConstructors ();

        /** Find a constructor by signature.
        * @param arguments the argument types to look for
        * @return the constructor, or <code>null</code> if it does not exist
        */
        public ConstructorElement getConstructor (Type[] arguments);


        /** Change the set of inner classes.
        * @param elems the new inner classes
        * @param action {@link #ADD}, {@link #REMOVE}, or {@link #SET}
        * @exception SourceException if impossible
        */
        public void changeClasses (ClassElement[] elems, int action) throws SourceException;

        /** Get all inner classes.
        * @return the inner classes
        */
        public ClassElement[] getClasses ();

        /** Find an inner class by name.
        * @param name the name to look for
        * @return the inner class, or <code>null</code> if it does not exist
        */
        public ClassElement getClass (Identifier name);

        /** Change the set of implemented/extended interfaces.
        * @param elems the new interfaces, by name
        * @param action {@link #ADD}, {@link #REMOVE}, or {@link #SET}
        * @exception SourceException if impossible
        */
        public void changeInterfaces (Identifier[] ids, int action) throws SourceException;

        /** Get all implemented/extended interfaces.
        * @return the interfaces, by name
        */
        public Identifier[] getInterfaces ();

        /** Get the class's documentation block.
        * @return JavaDoc for the class (not its members)
        */
        public JavaDoc.Class getJavaDoc();
    }

    /** Memory based implementation of the element factory.
    */
    static final class Memory extends MemberElement.Memory implements Impl {
        /** property, need not be initialized */
        private Identifier superClass;
        /** is class or interface */
        private boolean isClass;

        /** collection of interfaces */
        private MemoryCollection interfaces;

        /** collection of initializers */
        private MemoryCollection.Initializer initializers;

        /** collection of constructors */
        private MemoryCollection.Constructor constructors;
        /** collection of methods */
        private MemoryCollection.Method methods;
        /** collection of fields */
        private MemoryCollection.Field fields;
        /** collection of classes */
        private MemoryCollection.Class classes;

        /** memory implementation of java doc */
        JavaDoc.Class javaDoc = null;

        static final long serialVersionUID =6058485742932189851L;

        public Memory () {
            superClass = null;
            isClass = true;
            javaDoc = JavaDocSupport.createClassJavaDoc( null );
        }

        /** Copy constructor.
        * @param el element to copy from
        */
        public Memory (ClassElement el) {
            super (el);
            superClass = el.getSuperclass ();
            isClass = el.isClassOrInterface ();
            javaDoc = el.getJavaDoc().isEmpty() ?
                      JavaDocSupport.createClassJavaDoc( null ) :
                      JavaDocSupport.createClassJavaDoc( el.getJavaDoc().getRawText() );
        }

        /** Late initialization of initialization of copy elements.
        */
        public void copyFrom (ClassElement copyFrom) {
            changeInterfaces (copyFrom.getInterfaces (), SET);
            changeConstructors (copyFrom.getConstructors (), SET);
            changeMethods (copyFrom.getMethods (), SET);
            changeFields (copyFrom.getFields (), SET);
            changeClasses (copyFrom.getClasses (), SET);
        }

        /** Setter
        */
        public void setSuperclass(Identifier superClass) throws SourceException {
            Identifier old = this.superClass;
            this.superClass = superClass;
            firePropertyChange (PROP_SUPERCLASS, old, superClass);
        }

        public Identifier getSuperclass() {
            return superClass;
        }

        public void setClassOrInterface(boolean isClass) {
            boolean old = this.isClass;
            this.isClass = isClass;
            firePropertyChange (PROP_CLASS_OR_INTERFACE, new Boolean (old), new Boolean (isClass));
        }

        public boolean isClassOrInterface() {
            return isClass;
        }

        /** Changes set of elements.
        * @param elems elements to change
        * @param action the action to do (ADD, REMOVE, SET)
        * @exception SourceException if the action cannot be handled
        */
        public synchronized void changeInitializers (InitializerElement[] elems, int action) {
            initInitializers();
            initializers.change (elems, action);
        }

        public synchronized InitializerElement[] getInitializers () {
            initInitializers();
            return (InitializerElement[])initializers.toArray ();
        }

        void initInitializers() {
            if (initializers == null) {
                initializers = new MemoryCollection.Initializer (this);
            }
        }

        /** Changes set of elements.
        * @param elems elements to change
        * @exception SourceException if the action cannot be handled
        */
        public synchronized void changeFields (FieldElement[] elems, int action) {
            initFields();
            fields.change (elems, action);
        }

        public synchronized FieldElement[] getFields () {
            initFields();
            return (FieldElement[])fields.toArray ();
        }

        /** Finds a field with given name.
        * @param name the name of field to look for
        * @return the element or null if field with such name does not exist
        */
        public synchronized FieldElement getField (Identifier name) {
            initFields();
            return (FieldElement)fields.find (name, null);
        }

        void initFields() {
            if (fields == null) {
                fields = new MemoryCollection.Field (this);
            }
        }

        /** Changes set of elements.
        * @param elems elements to change
        */
        public synchronized void changeMethods (MethodElement[] elems, int action) {
            initMethods();
            methods.change (elems, action);
        }

        public MethodElement[] getMethods () {
            initMethods();
            return (MethodElement[])methods.toArray ();
        }

        /** Finds a method with given name and argument types.
        * @param name the name of field to look for
        * @param arguments for the method
        * @return the element or null if such method does not exist
        */
        public synchronized MethodElement getMethod (Identifier name, Type[] arguments) {
            initMethods();
            return (MethodElement)methods.find (name, arguments);
        }

        void initMethods() {
            if (methods == null) {
                methods = new MemoryCollection.Method (this);
            }
        }

        /** Changes set of elements.
        * @param elems elements to change
        * @exception SourceException if the action cannot be handled
        */
        public synchronized void changeConstructors (ConstructorElement[] elems, int action) {
            initConstructors();
            constructors.change (elems, action);
        }

        public synchronized ConstructorElement[] getConstructors () {
            initConstructors();
            return (ConstructorElement[])constructors.toArray ();
        }

        /** Finds a constructor with argument types.
        * @param arguments for the method
        * @return the element or null if such method does not exist
        */
        public synchronized ConstructorElement getConstructor (Type[] arguments) {
            initConstructors();
            return (ConstructorElement)constructors.find (null, arguments);
        }

        void initConstructors() {
            if (constructors == null) {
                constructors = new MemoryCollection.Constructor (this);
            }
        }

        /** Changes set of elements.
        * @param elems elements to change
        */
        public synchronized void changeClasses (ClassElement[] elems, int action) {
            initClasses();
            classes.change (elems, action);
        }

        public synchronized ClassElement[] getClasses () {
            initClasses();
            return (ClassElement[])classes.toArray ();
        }

        /** Finds an inner class with given name.
        * @param name the name to look for
        * @return the element or null if such class does not exist
        */
        public synchronized ClassElement getClass (Identifier name) {
            initClasses();
            return (ClassElement)classes.find (name, null);
        }

        void initClasses() {
            if (classes == null) {
                classes = new MemoryCollection.Class (this);
            }
        }

        /** Changes interfaces this class implements (or extends).
        * @param ids identifiers to change
        */
        public synchronized void changeInterfaces (Identifier[] ids, int action) {
            initInterfaces();
            interfaces.change (ids, action);
        }

        /** @return all interfaces which this class implements or interface extends.
        */
        public synchronized Identifier[] getInterfaces () {
            initInterfaces();
            return (Identifier[])interfaces.toArray ();
        }

        void initInterfaces() {
            if (interfaces == null) {
                interfaces = new MemoryCollection (this, PROP_INTERFACES, new Identifier[0]);
            }
        }

        // ================ javadoc =========================================

        /** @return class documentation.
        */
        public JavaDoc.Class getJavaDoc() {
            return javaDoc;
        }

        /** Getter for the associated class
        * @return the class element for this impl
        */
        final ClassElement getClassElement () {
            return (ClassElement)element;
        }

        // ================ serialization ======================================

        public Object readResolve() {
            return new ClassElement(this, (SourceElement)null);
        }

    }
}

/*
 * Log
 *  41   Gandalf-post-FCS1.39.3.0    4/3/00   Svatopluk Dedic Bugfix #5821
 *  40   src-jtulach1.39        1/18/00  Petr Hamernik   bugfix of previous 
 *       change
 *  39   src-jtulach1.38        1/18/00  Petr Hamernik   fixed #5309
 *  38   src-jtulach1.37        1/12/00  Petr Hamernik   i18n using perl script 
 *       (//NOI18N comments added)
 *  37   src-jtulach1.36        1/10/00  Petr Hamernik   better formating 
 *       (elements printing)
 *  36   src-jtulach1.35        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  35   src-jtulach1.34        9/29/99  Petr Hamernik   During adding elements 
 *       is tested if they are already added (fixed bugs #3130, #1706)
 *  34   src-jtulach1.33        9/27/99  Petr Hamernik   indenting improved 
 *       (empty lines between methods)
 *  33   src-jtulach1.32        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  32   src-jtulach1.31        7/21/99  Petr Hamernik   nullPointerExc bugfix
 *  31   src-jtulach1.30        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  30   src-jtulach1.29        6/7/99   Petr Hrebejk    Memory implementations 
 *       added to memory implementations of elements
 *  29   src-jtulach1.28        5/14/99  Jesse Glick     [JavaDoc]
 *  28   src-jtulach1.27        5/12/99  Petr Hamernik   Identifier 
 *       implementation updated
 *  27   src-jtulach1.26        5/10/99  Petr Hamernik   javadoc & printing 
 *       improved
 *  26   src-jtulach1.25        5/6/99   Petr Hamernik   finding rewritten
 *  25   src-jtulach1.24        4/27/99  Jesse Glick     [JavaDoc]
 *  24   src-jtulach1.23        4/27/99  Jaroslav Tulach find renamed to forName 
 *       for Class.forName consistency
 *  23   src-jtulach1.22        4/26/99  Jaroslav Tulach 
 *  22   src-jtulach1.21        4/20/99  Petr Hamernik   synchronization of the 
 *       name - between class and constructors
 *  21   src-jtulach1.20        4/8/99   Jesse Glick     [JavaDoc]
 *  20   src-jtulach1.19        4/6/99   Jesse Glick     [JavaDoc]
 *  19   src-jtulach1.18        4/2/99   Petr Hamernik   isApplet added
 *  18   src-jtulach1.17        4/2/99   Petr Hamernik   
 *  17   src-jtulach1.16        4/2/99   Petr Hamernik   
 *  16   src-jtulach1.15        4/1/99   Petr Hamernik   hasMainMethod
 *  15   src-jtulach1.14        3/30/99  Jesse Glick     [JavaDoc]
 *  14   src-jtulach1.13        3/23/99  Petr Hamernik   
 *  13   src-jtulach1.12        3/22/99  Petr Hamernik   printing changed
 *  12   src-jtulach1.11        3/15/99  Petr Hamernik   
 *  11   src-jtulach1.10        3/15/99  Petr Hamernik   
 *  10   src-jtulach1.9         2/26/99  Petr Hamernik   bugfixes
 *  9    src-jtulach1.8         2/17/99  Petr Hamernik   serialization changed.
 *  8    src-jtulach1.7         2/16/99  Petr Hamernik   bugfix - testing null 
 *       values improved
 *  7    src-jtulach1.6         2/8/99   Petr Hamernik   
 *  6    src-jtulach1.5         1/19/99  Jaroslav Tulach 
 *  5    src-jtulach1.4         1/19/99  Jaroslav Tulach 
 *  4    src-jtulach1.3         1/19/99  Jaroslav Tulach 
 *  3    src-jtulach1.2         1/19/99  Jaroslav Tulach 
 *  2    src-jtulach1.1         1/18/99  David Simonek   property constants added
 *  1    src-jtulach1.0         1/17/99  Jaroslav Tulach 
 * $
 */
