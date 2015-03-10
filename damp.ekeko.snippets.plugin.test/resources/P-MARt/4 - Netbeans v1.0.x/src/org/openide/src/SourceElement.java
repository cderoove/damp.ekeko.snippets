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

import java.io.IOException;
import java.io.Reader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;
import java.text.MessageFormat;

import org.openide.util.Task;

/** Describes an entire Java source file.
* Note that there is no standard in-memory implementation of this element;
* every user of the class is expected to have a reasonable
* implementation according to where the source file resides.
* <p>The source element should be parsed in the background using
* {@link #prepare} before any attempts are made to access its properties
* to read or to write, or to call {@link #print};
* otherwise such accesses will block until the parse is finished.
* @author Petr Hamernik, Jaroslav Tulach
*/
public final class SourceElement extends Element {
    /** Status when the source element is not yet prepared.
    */
    public static final int STATUS_NOT = 0;
    /** Status when the source element contains unrecoverable errors.
    */
    public static final int STATUS_ERROR = 1;
    /** Status when the source element contains minor errors.
    */
    public static final int STATUS_PARTIAL = 2;
    /** Status when the source element has been parsed and is error-free.
    */
    public static final int STATUS_OK = 3;

    static final long serialVersionUID =-1439690719608070114L;

    /** Create a new source element.
    * @param impl the pluggable implementation
    */
    public SourceElement(Impl impl) {
        super (impl);
    }

    /** @return implementation for the source
    */
    final Impl getSourceImpl () {
        return (Impl)impl;
    }

    /** Get the parsing status of the element.
    * This is a non-blocking operation.
    * @return one of {@link #STATUS_NOT}, {@link #STATUS_ERROR}, {@link #STATUS_PARTIAL}, or {@link #STATUS_OK}
    */
    public int getStatus () {
        return getSourceImpl ().getStatus ();
    }

    /** Begin parsing this source element.
    * This method is non-blocking; it only returns
    * a task that can be used to control the ongoing parse.
    * Initially the {@link #getStatus} should be {@link #STATUS_NOT}, and change
    * to one of the other three when parsing is complete, according to whether
    * or not errors were encountered, and their severity.
    *
    * @return a task to control the preparation of the element
    */
    public Task prepare () {
        return getSourceImpl ().prepare ();
    }


    // =========================== package section ============================

    /** Set the package of this source file.
    * @param id the package name, or <code>null</code> to use the default package
    * @exception SourceException if the operation cannot proceed
    */
    public void setPackage (Identifier id) throws SourceException {
        getSourceImpl ().setPackage (id);
    }

    /** Get the package of this source file.
    * @return the package name, or <code>null</code> if this source file is in the default package
    */
    public Identifier getPackage () {
        return getSourceImpl ().getPackage ();
    }

    // =========================== imports section ============================

    /** Get all imports.
    *
    * @return the imports
    */
    public Import[] getImports() {
        return getSourceImpl ().getImports ();
    }

    /** Set all imports.
    * The old imports will be replaced.
    * @param imprt the new imports
    * @exception SourceException if the operation cannot proceed
    */
    public void setImports(Import[] imprt) throws SourceException {
        getSourceImpl ().changeImports (imprt, Impl.SET);
    }

    /** Add an import.
    * @param el the import to add
    * @exception SourceException if the operation cannot proceed
    */
    public void addImport (Import el) throws SourceException {
        getSourceImpl ().changeImports (
            new Import[] { el }, Impl.ADD
        );
    }

    /** Add some imports.
    * @param els the imports to add
    * @exception SourceException if the operation cannot proceed
    */
    public void addImports (final Import[] els) throws SourceException {
        getSourceImpl ().changeImports (els, Impl.ADD);
    }

    /** Remove an import.
    * @param el the import to remove
    * @exception SourceException if the operation cannot proceed
    */
    public void removeImport (Import el) throws SourceException {
        getSourceImpl ().changeImports (
            new Import[] { el }, Impl.REMOVE
        );
    }

    /** Remove some imports.
    * @param els the imports to remove
    * @exception SourceException if the operation cannot proceed
    */
    public void removeImports (final Import[] els) throws SourceException {
        getSourceImpl ().changeImports (els, Impl.REMOVE);
    }

    //================== Top-level classes ==========================

    /** Add a new top-level class.
    * @param el the top-level class to add
    * @throws SourceException if impossible
    */
    public void addClass (ClassElement el) throws SourceException {
        if (getClass(el.getName()) != null)
            throwAddException("FMT_EXC_AddClassToSource", el); // NOI18N
        getSourceImpl ().changeClasses (new ClassElement[] { el }, Impl.ADD);
    }

    /** Add some new top-level classes.
    * @param el the top-level classes to add
    * @throws SourceException if impossible
    */
    public void addClasses (final ClassElement[] els) throws SourceException {
        for (int i = 0; i < els.length; i++) {
            if (getClass(els[i].getName()) != null)
                throwAddException("FMT_EXC_AddClassToSource", els[i]); // NOI18N
        }
        getSourceImpl ().changeClasses (els, Impl.ADD);
    }

    /** This method just throws localized exception. It is used during
    * adding class element, which already exists in source.
    * @param formatKey The message format key to localized bundle.
    * @param element The element which can't be added
    * @exception SourceException is alway thrown from this method.
    */
    private void throwAddException(String formatKey, ClassElement element) throws SourceException {
        MessageFormat format = new MessageFormat(ElementFormat.bundle.getString(formatKey));
        String msg = format.format(new Object[] { element.getName().getName() });
        throw new SourceException(msg);
    }

    /** Remove an top-level class.
    * @param el the top-level class to remove
    * @throws SourceException if impossible
    */
    public void removeClass (ClassElement el) throws SourceException {
        getSourceImpl ().changeClasses (new ClassElement[] { el }, Impl.REMOVE);
    }

    /** Remove some top-level classes.
    * @param els the top-level classes to remove
    * @throws SourceException if impossible
    */
    public void removeClasses (final ClassElement[] els) throws SourceException {
        getSourceImpl ().changeClasses (els, Impl.REMOVE);
    }

    /** Set the top-level classes.
    * The old ones will be replaced.
    * @param els the new top-level classes
    * @throws SourceException if impossible
    */
    public void setClasses (ClassElement[] els) throws SourceException {
        getSourceImpl ().changeClasses (els, Impl.SET);
    }

    /** Get the top-level classes.
    * @return all top-level classes
    */
    public ClassElement[] getClasses () {
        return getSourceImpl ().getClasses ();
    }

    /** Find a top-level class by name.
    * @param name the name to look for
    * @return the class, or <code>null</code> if it does not exist
    */
    public ClassElement getClass (Identifier name) {
        return getSourceImpl ().getClass (name);
    }

    /** Get all classes recursively, both top-level and inner.
    * @return all classes
    */
    public ClassElement[] getAllClasses () {
        return getSourceImpl ().getAllClasses ();
    }

    /* Prints the element into the element printer.
    * @param printer The element printer where to print to
    * @exception ElementPrinterInterruptException if printer cancel the printing
    */
    public void print(ElementPrinter printer) throws ElementPrinterInterruptException {
        Identifier pack = getPackage();
        if (pack != null) {
            printer.print("package "); // NOI18N
            printer.print(pack.getFullName());
            printer.println(";"); // NOI18N
            printer.println(""); // NOI18N
        }

        Import[] imp = getImports();
        for(int i = 0; i < imp.length; i++) {
            printer.print(imp[i].toString());
            printer.println(";"); // NOI18N
        }
        if (imp.length > 0)
            printer.println(""); // NOI18N

        print(getClasses(), printer);
    }

    /** Lock the underlaing document to have exclusive access to it and could make changes
    * on this SourceElement.
    *
    * @param run the action to run
    */
    public void runAtomic (Runnable run) {
        getSourceImpl ().runAtomic(run);
    }

    /** Executes given runnable in "user mode" does not allowing any modifications
    * to parts of text marked as guarded. The actions should be run as "atomic" so
    * either happen all at once or none at all (if a guarded block should be modified).
    *
    * @param run the action to run
    * @exception SourceException if a modification of guarded text occured
    *   and that is why no changes to the document has been done.
    */
    public void runAtomicAsUser (Runnable run) throws SourceException {
        getSourceImpl ().runAtomicAsUser(run);
    }

    /** Pluggable behaviour for source elements.
    * @see SourceElement
    */
    public static interface Impl extends Element.Impl {
        /** Add some top-level classes. */
        public static final int ADD = ClassElement.Impl.ADD;
        /** Remove some top-level classes. */
        public static final int REMOVE = ClassElement.Impl.REMOVE;
        /** Set the top-classes. */
        public static final int SET = ClassElement.Impl.SET;

        static final long serialVersionUID =-2181228658756563166L;

        /** Get the parsing status of the element.
         * This is a non-blocking operation.
         * @return one of {@link #STATUS_NOT}, {@link #STATUS_ERROR}, {@link #STATUS_PARTIAL}, or {@link #STATUS_OK}
         */
        public int getStatus ();


        /** Begin parsing this source element.
         * This method is non-blocking; it only returns
         * a task that can be used to control the ongoing parse.
         * Initially the {@link #getStatus} should be {@link #STATUS_NOT}, and change
         * to one of the other three when parsing is complete, according to whether
         * or not errors were encountered, and their severity.
         *
         * @return a task to control the preparation of the element
         */
        public Task prepare ();


        /** Set the package of this source file.
         * @param id the package name, or <code>null</code> to use the default package
         * @exception SourceException if the operation cannot proceed
         */
        public void setPackage (Identifier id) throws SourceException;

        /** Get the package of this source file.
         * @return the package name, or <code>null</code> if this source file is in the default package
         */
        public Identifier getPackage ();

        // =========================== imports section ============================

        /** Get all imports.
         *
         * @return the imports
         */
        public Import[] getImports();


        /** Change the set of imports.
        * @param elems the imports to change
        * @param action one of {@link #ADD}, {@link #REMOVE}, or {@link #SET}
        * @exception SourceException if the action cannot be handled
        */
        public void changeImports (Import[] elems, int action) throws SourceException;


        /** Change the set of top-level classes.
        * @param elems the classes to change
        * @param action one of {@link #ADD}, {@link #REMOVE}, or {@link #SET}
        * @exception SourceException if the action cannot be handled
        */
        public void changeClasses (ClassElement[] elems, int action) throws SourceException;

        /** Get all top-level classes.
        * @return the classes
        */
        public ClassElement[] getClasses ();

        /** Find a top-level class by name.
        * @param name the name to look for
        * @return the class, or <code>null</code> if it does not exist
        */
        public ClassElement getClass (Identifier name);

        /** Get all classes recursively, both top-level and inner.
         * @return all classes
         */
        public ClassElement[] getAllClasses ();

        /** Lock the underlaing document to have exclusive access to it and could make changes
        * on this SourceElement.
        *
        * @param run the action to run
        */
        public void runAtomic (Runnable run);

        /** Executes given runnable in "user mode" does not allowing any modifications
        * to parts of text marked as guarded. The actions should be run as "atomic" so
        * either happen all at once or none at all (if a guarded block should be modified).
        *
        * @param run the action to run
        * @exception SourceException if a modification of guarded text occured
        *   and that is why no changes to the document has been done.
        */
        public void runAtomicAsUser (Runnable run) throws SourceException;
    }

}

/*
 * Log
 *  20   src-jtulach1.19        1/12/00  Petr Hamernik   i18n using perl script 
 *       (//NOI18N comments added)
 *  19   src-jtulach1.18        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  18   src-jtulach1.17        9/29/99  Petr Hamernik   During adding elements 
 *       is tested if they are already added (fixed bugs #3130, #1706)
 *  17   src-jtulach1.16        9/13/99  Petr Hamernik   runAsUser implemented 
 *       and used
 *  16   src-jtulach1.15        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  15   src-jtulach1.14        7/8/99   Petr Hamernik   runAtomic methods added
 *  14   src-jtulach1.13        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  13   src-jtulach1.12        5/12/99  Petr Hamernik   Identifier 
 *       implementation updated
 *  12   src-jtulach1.11        4/14/99  Petr Hamernik   fixed bug #1523
 *  11   src-jtulach1.10        3/30/99  Jesse Glick     [JavaDoc]
 *  10   src-jtulach1.9         3/30/99  Jan Jancura     Bug in gatAllClasses()
 *  9    src-jtulach1.8         3/22/99  Petr Hamernik   printing changed
 *  8    src-jtulach1.7         2/11/99  Petr Hamernik   
 *  7    src-jtulach1.6         2/10/99  Jaroslav Tulach 
 *  6    src-jtulach1.5         2/4/99   Petr Hamernik   setting of extended file
 *       attributes doesn't require FileLock
 *  5    src-jtulach1.4         1/27/99  Jaroslav Tulach xxxClasses, xxxImports  
 *  4    src-jtulach1.3         1/19/99  Jaroslav Tulach 
 *  3    src-jtulach1.2         1/19/99  Jaroslav Tulach 
 *  2    src-jtulach1.1         1/18/99  David Simonek   property constants added
 *  1    src-jtulach1.0         1/17/99  Jaroslav Tulach 
 * $
 */
