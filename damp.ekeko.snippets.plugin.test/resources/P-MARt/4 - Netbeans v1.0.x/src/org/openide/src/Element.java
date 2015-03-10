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

import java.beans.*;
import java.io.*;
import java.util.StringTokenizer;

import javax.swing.text.DefaultStyledDocument;

import org.openide.nodes.Node;
import org.openide.text.IndentEngine;
import org.openide.filesystems.FileUtil;

/** Base class for representations of elements in the
* Java language.
* All elements are items which are structural features of a class (or the class itself),
* rather than corresponding to bytecode (i.e. statements are not represented).
* The same representation is suited to any language using the Java VM.
*
* @author Jaroslav Tulach, Petr Hamernik
*/
public abstract class Element extends Object
    implements Serializable, ElementProperties, Node.Cookie {

    /** Implementation */
    Impl impl;

    static final long serialVersionUID =967040188302141522L;
    /** Create a new element with the provided implementation. The implementation is
    * responsible for storing all properties of the object.
    *
    * @param impl the implementation to use
    */
    protected Element(Impl impl) {
        this.impl = impl;
        impl.attachedToElement (this);
    }

    /** Add a property change listener.
    * @param l the listener to add
    * @see ElementProperties
    */
    public final void addPropertyChangeListener (PropertyChangeListener l) {
        impl.addPropertyChangeListener (l);
    }

    /** Remove a property change listener.
    * @param l the listener to remove
    * @see ElementProperties
    */
    public final void removePropertyChangeListener (PropertyChangeListener l) {
        impl.removePropertyChangeListener (l);
    }

    /** Mark the current element in the context of this element.
    * The current element means the position for inserting new elements.
    * @param beforeAfter <CODE>true</CODE> means that new element is inserted before
    *        the specified element, <CODE>false</CODE> means after.
    */
    public void markCurrent(boolean beforeAfter) {
        impl.markCurrent(beforeAfter);
    }

    /** Look for a cookie providing added behavior for this element.
    * The request is {@link Impl#getCookie delegated} to the current implementation.
    * Also note that <code>Element</code> implements <code>Node.Cookie</code>, and that
    * if the implementation does not provide a cookie, but the requested cookie class is
    * actually a superclass/interface of this element type, then the element itself may be
    * returned as the cookie.
    * @param type the cookie class to look for
    * @return a cookie assignable to that class, or <code>null</code> if the cookie
    *    is not supported
    */
    public Node.Cookie getCookie(Class type) {
        Node.Cookie c = impl.getCookie(type);
        if ((c == null) && type.isAssignableFrom(getClass()))
            c = this;

        return c;
    }

    private Object writeReplace() {
        return impl;
    }

    /** Print this element (and all its subelements) into an element printer.
    * @param printer the element printer
    * @exception ElementPrinterInterruptException if the printer canceled the printing
    */
    public abstract void print(ElementPrinter printer) throws ElementPrinterInterruptException;

    /** Prints array of elements.
    * @param el the elements
    * @param printer The printer where to write
    * @return true if at least one element was printed
    * @exception ElementPrinterInterruptException if printer cancel the printing
    */
    static boolean print(Element[] el, ElementPrinter printer) throws ElementPrinterInterruptException {
        for (int i = 0; i < el.length; i++) {
            if (i > 0) {
                printer.println(""); // NOI18N
                printer.println(""); // NOI18N
            }
            el[i].print(printer);
        }
        return (el.length > 0);
    }

    /** Prints the javadoc to the printer.
    * It calls doc.getRawText() and inserts the '*' symbols to the begins
    * of lines.
    * @param doc The printed javadoc
    * @param printer The printer where to write
    * @exception ElementPrinterInterruptException if printer cancel the printing
    */
    static void printJavaDoc(JavaDoc doc, ElementPrinter printer) throws ElementPrinterInterruptException {
        if (doc.isEmpty())
            return;

        //PENDING: should be more customizable
        StringTokenizer tukac = new StringTokenizer(doc.getRawText(), "\n", true); // NOI18N
        printer.print("/**");
        boolean oneMoreSpace = true;
        boolean newLine = false;
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            newLine = token.equals("\n");
            if (newLine) {
                printer.println("");
                printer.print(" *");
                oneMoreSpace = true;
            }
            else {
                if (oneMoreSpace && !token.startsWith(" ")) {
                    printer.print(" ");
                    oneMoreSpace = false;
                }
                printer.print(token);
            }
        }
        if (!newLine)
            printer.print(" *");
        printer.print("/");
    }

    /** Get a string representation of the element.
    * @return the string
    * @see #print
    * @see DefaultElementPrinter
    */
    public String toString() {
        StringWriter sw = new StringWriter();
        IndentEngine indentator = IndentEngine.find(FileUtil.getMIMEType("java")); // NOI18N
        PrintWriter pw = new PrintWriter(indentator.createWriter(new DefaultStyledDocument(), 0, sw));
        //    PrintWriter pw = new PrintWriter(sw);
        try {
            print(new DefaultElementPrinter(pw));
        }
        catch (ElementPrinterInterruptException e) {
            // could not happen.
        }
        pw.close();
        return sw.toString();
    }

    /** Pluggable implementation of the storage of element properties.
    * @see Element#Element
    */
    public interface Impl extends Serializable {
        static final long serialVersionUID =-3246061193296761293L;
        /** Called to attach the implementation to a specific
        * element. Will be called in the element's constructor.
        * Allows implementors
        * of this interface to store a reference to the holder class,
        * useful for implementing the property change listeners.
        *
        * @param element the element to attach to
        */
        public void attachedToElement (Element el);

        /** Add a property change listener.
        * @param l the listener to add
        */
        public void addPropertyChangeListener (PropertyChangeListener l);

        /** Remove a property change listener.
        * @param l the listener to remove
        */
        public void removePropertyChangeListener (PropertyChangeListener l);

        /** Implementations must be resolvable.
        * I.e., upon deserialization they must be able to recreate the
        * holder class.
        * @return an instance of the proper subclass of {@link Element}
        * @see Serializable
        */
        public Object readResolve();

        /** Get the support for a cookie, if any.
        * Changes of supported cookies are <em>not</em> fired.
        *
        * @param type the cookie class to look for
        * @return an instance assignable to that class, or <code>null</code> if the cookie
        *    is not supported
        */
        public Node.Cookie getCookie(Class type);

        /** Mark the current element in the context of this element.
        * The current element means the position for inserting new elements.
        * @param beforeAfter <CODE>true</CODE> means that new element is inserted before
        *        the specified element, <CODE>false</CODE> means after.
        */
        public void markCurrent(boolean beforeAfter);
    }

    /** Default implementation of the Impl interface.
    * It just holds the property values.
    */
    static abstract class Memory implements Element.Impl {
        /** the element for this implementation */
        protected Element element;

        /** Property change support */
        private PropertyChangeSupport support;

        static final long serialVersionUID =7734412320645883859L;
        /** Attaches to element */
        public void attachedToElement (Element element) {
            this.element = element;
        }

        /** Fires property change event.
        * @param name property name
        * @param o old value
        * @param n new value
        */
        protected final void firePropertyChange (String name, Object o, Object n) {
            if (support != null) {
                support.firePropertyChange (name, o, n);
            }
        }

        /** Adds property listener */
        public synchronized void addPropertyChangeListener (PropertyChangeListener l) {
            if (support == null) {
                synchronized (this) {
                    // new test under synchronized block
                    if (support == null) {
                        support = new PropertyChangeSupport (element);
                    }
                }
            }
            support.addPropertyChangeListener (l);
        }

        /** Removes property listener */
        public void removePropertyChangeListener (PropertyChangeListener l) {
            if (support != null) {
                support.removePropertyChangeListener (l);
            }
        }

        /** This implementation returns always null.
        * @param type the class to look for
        * @return null.
        */
        public Node.Cookie getCookie(Class type) {
            return null;
        }

        /** Mark the current element in the context of this element.
        * The current element means the position for inserting new elements.
        * @param beforeAfter <CODE>true</CODE> means that new element is inserted before
        *        the specified element, <CODE>false</CODE> means after.
        */
        public void markCurrent(boolean beforeAfter) {
            //PENDING
        }
    }
}

/*
 * Log
 *  24   src-jtulach1.23        1/12/00  Petr Hamernik   i18n using perl script 
 *       (//NOI18N comments added)
 *  23   src-jtulach1.22        1/11/00  Petr Hamernik   fixed 5287
 *  22   src-jtulach1.21        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  21   src-jtulach1.20        9/27/99  Petr Hamernik   indenting improved 
 *       (empty lines between methods)
 *  20   src-jtulach1.19        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  19   src-jtulach1.18        7/19/99  Petr Hamernik   IndentEngine usage added
 *  18   src-jtulach1.17        7/8/99   Petr Hamernik   inserting position added
 *  17   src-jtulach1.16        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  16   src-jtulach1.15        6/7/99   Petr Hamernik   indent engine used - 
 *       must be uncommented (depends on text editor change)
 *  15   src-jtulach1.14        5/10/99  Petr Hamernik   javadoc & printing 
 *       improved
 *  14   src-jtulach1.13        4/30/99  Jesse Glick     [JavaDoc]
 *  13   src-jtulach1.12        4/26/99  Jesse Glick     [JavaDoc]
 *  12   src-jtulach1.11        4/26/99  Petr Hamernik   getCookie minor changes
 *  11   src-jtulach1.10        4/13/99  Petr Hamernik   Element implements 
 *       Node.Cookie
 *  10   src-jtulach1.9         3/30/99  Jesse Glick     [JavaDoc]
 *  9    src-jtulach1.8         3/22/99  Petr Hamernik   printing changed
 *  8    src-jtulach1.7         3/18/99  Petr Hamernik   
 *  7    src-jtulach1.6         2/17/99  Petr Hamernik   serialization changed.
 *  6    src-jtulach1.5         2/16/99  Petr Hamernik   
 *  5    src-jtulach1.4         2/8/99   Petr Hamernik   
 *  4    src-jtulach1.3         1/19/99  Jaroslav Tulach 
 *  3    src-jtulach1.2         1/19/99  Jaroslav Tulach 
 *  2    src-jtulach1.1         1/19/99  Jaroslav Tulach 
 *  1    src-jtulach1.0         1/17/99  Jaroslav Tulach 
 * $
 */
