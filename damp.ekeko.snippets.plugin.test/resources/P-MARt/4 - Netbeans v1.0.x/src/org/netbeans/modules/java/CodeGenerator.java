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
import java.util.*;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import org.openide.src.*;
import org.openide.text.*;

/** An implementation of ElementPrinter which chaches the tokens
* between marks.
*
* @author Petr Hamernik
*/
class CodeGenerator {

    /** Generates the javadoc for the given element.
    * @param element The element which is used for printing
    * @param impl The implementation where is the printed text inserted
    */
    public static void regenerateJavaDoc(final Element element, final ElementImpl impl) throws SourceException {
        try {
            final PositionRef docBegin;

            if (impl.docBounds != null) {
                docBegin = impl.docBounds.getBegin();
            } else {
                docBegin = null;
            }
            final StyledDocument doc = impl.bounds.getBegin().getEditorSupport().openDocument();
            Util.ExceptionRunnable run = new Util.ExceptionRunnable() {
                                             public void run() throws Exception {
                                                 PositionRef begin;
                                                 PositionBounds bounds;

                                                 if (docBegin != null) {
                                                     begin = docBegin;
                                                     bounds = impl.docBounds;
                                                 } else {
                                                     bounds = SourceElementImpl.createNewLineBoundsAt(impl.getJavaDocPosition());
                                                     begin = bounds.getBegin();
                                                 }
                                                 StringWriter stringWriter = new StringWriter();
                                                 Writer indentWriter = Util.findIndentWriter(doc, begin.getOffset(), stringWriter);
                                                 ElementPrinterImpl printer = new ElementPrinterImpl(indentWriter, element, ElementPrinter.JAVADOC_BEGIN, ElementPrinter.JAVADOC_END);
                                                 try {
                                                     element.print(printer);
                                                 }
                                                 catch (ElementPrinterInterruptException e) {
                                                 }
                                                 bounds.setText(stringWriter.toString());
                                                 if (impl.docBounds == null) {
                                                     impl.docBounds = bounds;
                                                 }
                                             }
                                         };
            Util.runAtomic(doc, run);
        }
        catch (Exception e) {
            throw new SourceException(e.getMessage());
        }
    }

    /** Generates the header for the given element.
    * @param element The element which is used for printing
    * @param impl The implementation where is the printed text inserted
    */
    public static void regenerateHeader(final Element element, final ElementImpl impl) throws SourceException {
        if (impl.headerBounds != null) {
            try {
                final PositionRef headerBegin = impl.headerBounds.getBegin();
                final StyledDocument doc = headerBegin.getEditorSupport().openDocument();
                Util.ExceptionRunnable run = new Util.ExceptionRunnable() {
                                                 public void run() throws Exception {
                                                     StringWriter stringWriter = new StringWriter();
                                                     Writer indentWriter = Util.findIndentWriter(doc, headerBegin.getOffset(), stringWriter);
                                                     ElementPrinterImpl printer = new ElementPrinterImpl(indentWriter, element, ElementPrinter.HEADER_BEGIN, ElementPrinter.HEADER_END);
                                                     try {
                                                         element.print(printer);
                                                     }
                                                     catch (ElementPrinterInterruptException e) {
                                                     }
                                                     impl.headerBounds.setText(stringWriter.toString());
                                                 }
                                             };
                Util.runAtomic(doc, run);
            }
            catch (Exception e) {
                throw new SourceException(e.getMessage());
            }
        }
    }

    /** Generates the header for the given element.
    * @param element The element which is used for printing
    * @param impl The implementation where is the printed text inserted
    */
    public static void regenerateElement(final Element element, final ElementImpl impl) throws SourceException {
        if (impl.bounds != null) {
            try {
                final PositionRef begin = impl.bounds.getBegin();
                final EditorSupport editor = begin.getEditorSupport();
                final StyledDocument doc = editor.openDocument();
                Util.ExceptionRunnable run = new Util.ExceptionRunnable() {
                                                 public void run() throws Exception {
                                                     StringWriter stringWriter = new StringWriter();
                                                     Writer indentWriter = Util.findIndentWriter(doc, begin.getOffset(), stringWriter);
                                                     WholeElementPrinter printer = new WholeElementPrinter(indentWriter, stringWriter, element, impl, editor);
                                                     try {
                                                         element.print(printer);
                                                     }
                                                     catch (ElementPrinterInterruptException e) {
                                                     }
                                                     impl.bounds.setText(stringWriter.toString());
                                                     printer.finish();
                                                 }
                                             };
                Util.runAtomic(doc, run);
            }
            catch (Exception e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    e.printStackTrace();
                SourceException exc = (e instanceof SourceException) ? (SourceException) e : new SourceException(e.getMessage());
                throw exc;
            }
        }
    }

    static class WholeElementPrinter extends ElementPrinterImpl {
        StringWriter stringWriter;
        Element element;
        ElementImpl elementImpl;
        EditorSupport editor;

        HashMap marksMap;
        MarkData current;

        WholeElementPrinter(Writer writer, StringWriter stringWriter,
                            Element element, ElementImpl elementImpl,
                            EditorSupport editor) {
            super(writer);
            this.stringWriter = stringWriter;
            this.element = element;
            this.elementImpl = elementImpl;
            this.editor = editor;

            marksMap = initElementsMap(element, elementImpl);
            current = (MarkData) marksMap.get(element);
        }

        public void markNotify(Element el, int what) {
            if (current.element != el) {
                current = (MarkData) marksMap.get(el);
            }
            current.positions[what] = stringWriter.getBuffer().length();
        }

        private HashMap initElementsMap(Element element, ElementImpl elementImpl) {
            if (element instanceof ClassElement) {
                HashMap map = new HashMap(25);
                // [ph] for creating this map we suppose that structure under element and elementImpl
                // is exactly the same (including order of subelements). This is reasonable assumption
                // because impl hierarchy is created in ElementsCollection class from element hierarchy.
                fillMapForClass(map, (ClassElement) element, (ClassElementImpl) elementImpl);
                return map;
            }
            else {
                HashMap map = new HashMap(2);
                map.put(element, new MarkData(element, elementImpl));
                return map;
            }
        }

        private void fillMapForClass(HashMap map, ClassElement clazz, ClassElementImpl clazzImpl) {
            map.put(clazz, new MarkData(clazz, clazzImpl));
            for (int i = 0; i < 5; i++) {
                Object[] elements = null;
                switch (i) {
                case 0: elements = clazz.getInitializers(); break;
                case 1: elements = clazz.getFields(); break;
                case 2: elements = clazz.getConstructors(); break;
                case 3: elements = clazz.getMethods(); break;
                case 4: elements = clazz.getClasses(); break;
                }
                if ((elements == null) || (elements.length == 0))
                    continue;

                Object[] elementImpls = null;
                switch (i) {
                case 0: elementImpls = clazzImpl.initializers.toArray(); break;
                case 1: elementImpls = clazzImpl.fields.toArray(); break;
                case 2: elementImpls = clazzImpl.constructors.toArray(); break;
                case 3: elementImpls = clazzImpl.methods.toArray(); break;
                case 4: elementImpls = clazzImpl.classes.toArray(); break;
                }
                if (i != 4) {
                    for (int j = 0; j < elements.length; j++) {
                        MarkData mark = new MarkData((Element) elements[j],
                                                     (ElementImpl) ((Element)elementImpls[j]).getCookie(ElementImpl.class));
                        map.put(elements[j], mark);
                    }
                }
                else {
                    for (int j = 0; j < elements.length; j++)
                        fillMapForClass(map, (ClassElement) elements[j],
                                        (ClassElementImpl) ((Element)elementImpls[j]).getCookie(ClassElementImpl.class));
                }
            }
        }

        void finish() {
            int offset = elementImpl.bounds.getBegin().getOffset();
            Iterator it = marksMap.values().iterator();
            while (it.hasNext()) {
                MarkData data = (MarkData) it.next();
                if (element != data.element) {
                    data.elementImpl.bounds = createBounds(data.positions[ELEMENT_BEGIN] + offset,
                                                           data.positions[ELEMENT_END] + offset);
                }
                data.elementImpl.docBounds = createBounds(data.positions[JAVADOC_BEGIN] + offset,
                                             data.positions[JAVADOC_END] + offset);
                data.elementImpl.headerBounds = createBounds(data.positions[HEADER_BEGIN] + offset,
                                                data.positions[HEADER_END] + offset);
                data.elementImpl.bodyBounds = createBounds(data.positions[BODY_BEGIN] + offset,
                                              data.positions[BODY_END] + offset);
            }
        }

        private PositionBounds createBounds(int begin, int end) {
            if ((begin == -1) || (end == -1))
                return null;

            PositionRef posBegin = editor.createPositionRef(begin, Position.Bias.Forward);
            PositionRef posEnd = editor.createPositionRef(end, Position.Bias.Backward);
            return new PositionBounds(posBegin, posEnd);
        }
    }

    static class MarkData {
        Element element;
        ElementImpl elementImpl;
        int[] positions;

        MarkData(Element element, ElementImpl elementImpl) {
            this.element = element;
            this.elementImpl = elementImpl;
            positions = new int[8];
            for (int i = 0; i < 8; i++)
                positions[i] = -1;
        }
    }

    // ================= methods ==============================

    static class ElementPrinterImpl implements ElementPrinter {
        PrintWriter writer;

        Element printedElement;
        int beginMark;
        int endMark;

        int status;

        ElementPrinterImpl(Writer writer) {
            this(writer, null, 0, 0);
            status = 1;
        }

        ElementPrinterImpl(Writer writer, Element printedElement, int beginMark, int endMark) {
            this.writer = new PrintWriter(writer);
            this.printedElement = printedElement;
            this.beginMark = beginMark;
            this.endMark = endMark;
            status = 0;
        }

        public boolean isBegin(Element element, int what) {
            return (printedElement == null) ||
                   ((element == printedElement) && (what == beginMark));
        }

        public boolean isEnd(Element element, int what) {
            return (printedElement == element) && (what == endMark);
        }

        public void markNotify(Element element, int what) {
        }

        public String getString() {
            return writer.toString();
        }

        /** Prints the given text.
        * @param text The text to write
        */
        public void print(String text) throws ElementPrinterInterruptException {
            switch (status) {
            case 0:
                return;
            case 1:
                writer.print(text);
                break;
            case 2:
                throw new ElementPrinterInterruptException();
            }
        }

        /** Prints the line. New-line character '\n' should be added.
        * @param text The line to write
        */
        public void println(String text) throws ElementPrinterInterruptException {
            print(text);
            print("\n"); // NOI18N
        }

        /** Add the mark to the list, if the printer is currently caching
        * (status == 1) or this mark is the begin.
        * @param element The element to mark
        * @param what The kind of event
        */
        private void mark(Element element, int what) throws ElementPrinterInterruptException {
            switch (status) {
            case 0:
                if (isBegin(element, what)) {
                    markNotify(element, what);
                    status = 1;
                }
                break;
            case 1:
                writer.flush();
                markNotify(element, what);
                if (isEnd(element, what)) {
                    status = 2;
                    writer.close();
                    throw new ElementPrinterInterruptException();
                }
                break;
            case 2:
                throw new ElementPrinterInterruptException();
            }
        }

        /** Marks the notable point of the class element.
        * @param element The element.
        * @param what The kind of the event. It must be one of the integer
        *             constants from this interface
        */
        public void markClass(ClassElement element, int what) throws ElementPrinterInterruptException {
            mark(element, what);
        }

        /** Marks the notable point of the initializer element.
        * @param element The element.
        * @param what The kind of the event. It must be one of the integer
        *             constants from this interface
        * @return always <CODE>true</CODE>
        */
        public void markInitializer(InitializerElement element, int what) throws ElementPrinterInterruptException {
            mark(element, what);
        }

        /** Marks the notable point of the field element.
        * @param element The element.
        * @param what The kind of the event. It must be one of the integer
        *             constants from this interface
        */
        public void markField(FieldElement element, int what) throws ElementPrinterInterruptException {
            mark(element, what);
        }

        /** Marks the notable point of the constructor element.
        * @param element The element.
        * @param what The kind of the event. It must be one of the integer
        *             constants from this interface
        */
        public void markConstructor(ConstructorElement element, int what) throws ElementPrinterInterruptException {
            mark(element, what);
        }

        /** Marks the notable point of the method element.
        * @param element The element.
        * @param what The kind of the event. It must be one of the integer
        *             constants from this interface
        */
        public void markMethod(MethodElement element, int what) throws ElementPrinterInterruptException {
            mark(element, what);
        }
    }
}

/*
 * Log
 *  11   Gandalf-post-FCS1.9.1.0     2/24/00  Svatopluk Dedic JavaDoc re-creation 
 *       doesn't format method code
 *  10   Gandalf   1.9         1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  9    Gandalf   1.8         1/10/00  Petr Hamernik   regeneration of 
 *       ClassElements improved (AKA #4536)
 *  8    Gandalf   1.7         1/6/00   Petr Hamernik   debug msgs removed
 *  7    Gandalf   1.6         1/6/00   Petr Hamernik   fixed 4321
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         7/16/99  Petr Hamernik   debug printline removed
 *  4    Gandalf   1.3         7/8/99   Petr Hamernik   changes reflecting 
 *       org.openide.src changes
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/2/99   Petr Hamernik   connections of java 
 *       sources
 *  1    Gandalf   1.0         5/10/99  Petr Hamernik   
 * $
 */
