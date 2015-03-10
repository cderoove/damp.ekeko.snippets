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
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;
import java.util.*;

import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.NbDocument;
import org.openide.text.IndentEngine;
import org.openide.util.NbBundle;
import org.openide.src.*;
import org.openide.loaders.DataObject;
import org.openide.cookies.SaveCookie;

import org.netbeans.modules.java.settings.JavaSettings;

/** Miscellaneous utilities for Java data loader.
*
* @author Petr Hamernik, Ales Novak
*/
public final class Util extends Object {
    // ===================== i18n utilities ===========================

    /** ResourceBundle for java-loader package. */
    static final ResourceBundle bundle = NbBundle.getBundle(Util.class);

    /** Computes the localized string for the key.
    * @param key The key of the string.
    * @return the localized string.
    */
    static String getString(String key) {
        return bundle.getString(key);
    }

    // ===================== loader utilities ===========================

    /** Finds file with the same name and specified extension
    * in the same folder as param f
    * @return the found fileobject or null.
    */
    static FileObject findFile(FileObject f, String ext) {

        if (f == null) {
            return null;
        }

        String name = f.getName ();
        int indx = name.indexOf ('$');
        if (indx > 0) {
            name = name.substring (0, indx);
        }
        FileObject ret = f.getParent().getFileObject (name, ext);
        if (ret != null) {
            return ret;
        } else if (isPerfectRecognition() &&
                   JavaDataLoader.CLASS_EXTENSION.equals(f.getExt())) {

            // try it from the current thread - see JavaCompiler - find createData string
            if (f.getSize() == 0) {
                String thrname = Thread.currentThread().getName();
                int idx = thrname.indexOf('.');
                if ((idx < 0) ||
                        (idx == thrname.length() - 1)
                   ) {
                    return null;
                }
                if (! ext.equals(thrname.substring(idx + 1))) {
                    return null;
                }
                name = thrname.substring(0, idx);
                return f.getParent().getFileObject(name, ext);
            }

            // examine class file
            java.io.DataInputStream is = null;
            try {
                sun.tools.java.Environment env = JavaCompiler.createEnvironment(null);
                is = new java.io.DataInputStream(new java.io.BufferedInputStream(f.getInputStream()));
                sun.tools.java.ClassDefinition cdef = sun.tools.java.BinaryClass.load(env, is);
                Object sorc = cdef.getSource();
                if (sorc != null) {
                    String src = sorc.toString();
                    int idx = src.indexOf('.');
                    if ((idx <= 0) ||
                            (idx == src.length() - 1)
                       ) {
                        return null;
                    }
                    name = src.substring(0, idx);
                    String xext = src.substring(idx + 1);
                    if (! xext.equals(ext)) {
                        return null;
                    }
                    return f.getParent().getFileObject(name, ext);
                }
                is.close();
            } catch (java.io.IOException e) {
                notifyException(e, f.toString());
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (java.io.IOException e) {
                        notifyException(e, f.toString());
                    }
                }
            }
        }
        return null;
    }

    /** Notifies about an exception
    *
    * @param msg is ignored
    */
    private static void notifyException(Throwable t, String msg) {
        /*final org.openide.NotifyDescriptor e = new org.openide.NotifyDescriptor.Exception(t, msg);
        Runnable run = new Runnable() {
          public void run() {
            org.openide.TopManager.getDefault().notify(e);
          }
    };
        java.awt.EventQueue.invokeLater(run);
        */
        org.openide.TopManager.getDefault().notifyException(t);
    }

    /**
    * @return the value of the perfectRecognition property of JDO
    */
    private static boolean isPerfectRecognition() {
        JavaSettings jst = (JavaSettings) JavaSettings.findObject(JavaSettings.class, true);
        return jst.isPerfectRecognition();
    }


    // ===================== Indentation util ==============================

    /** Finds the appropriate indentation writer for the java sources.
    * @param doc The document where it will be inserted in
    * @param offset The position in the document
    * @param writer The encapsulated writer
    */
    static Writer findIndentWriter(Document doc, int offset, Writer writer) {
        String mime = FileUtil.getMIMEType(JavaDataLoader.JAVA_EXTENSION);
        IndentEngine engine = IndentEngine.find(mime == null ? "" : mime); // NOI18N
        return engine.createWriter(doc, offset, writer);
    }

    // ===================== Advanced Task suppport =========================

    /** Invokes the runnable using NbDocument.runAtomic.
    * If BadLocationException occured inside, it will be thrown
    * the new SourceException.
    *
    */
    static void runAtomic(StyledDocument doc, ExceptionRunnable run) throws SourceException {
        RunnableSupport support = new RunnableSupport(run);
        NbDocument.runAtomic(doc, support);
        support.throwException();
    }

    /** This interface is used like runnable, but its method run
    * could throw BadLocationException.
    * @exception
    */
    interface ExceptionRunnable {
        public void run() throws Exception;
    }

    /** Encapsulation class for the ExceptionRunnable interface.
    * It implements Runnable, so it can be used everywhere is Runnable
    * accepted.
    */
    private static class RunnableSupport implements Runnable {
        /** Exception which occured in the e*/
        private Exception e;

        /** Encapsulated runnable */
        private ExceptionRunnable runnable;

        /** Creates new class */
        public RunnableSupport(ExceptionRunnable runnable) {
            this.runnable = runnable;
            e = null;
        }

        /** @return true if exception occured. */
        public boolean hasException() {
            return e != null;
        }

        /** @return the exception or null if no exception has occured. */
        public Exception getException() {
            return e;
        }

        /** If a bad position exception occured during running
        * the given ExceptionRunnable, it will be thrown new SourceException
        * with the same message. Otherwise throws nothing.
        */
        public void throwException() throws SourceException {
            if (e != null) {
                throw new SourceException(e.getMessage());
            }
        }

        /** Implementation of the Runnable interface. It calls the encapsulated
        * runnable passed in the constructor and catch the exceptions.
        */
        public void run() {
            try {
                runnable.run();
            }
            catch (Exception e) {
                this.e = e;
            }
        }
    }

    // ================== Compiler utilities ============================

    /** WeakHashtable is used to avoid memory leak in the compiler. */
    static class WeakHashtable extends Hashtable {

        /** Reference queue for garbage collected values. */
        private ReferenceQueue refq;
        /** ref to original Hashtable */
        private Hashtable hash;

        static final long serialVersionUID =-6362145647944898749L;

        /** @param hash is a Hashtable to to replace */
        public WeakHashtable(Hashtable hash) {
            refq = new ReferenceQueue();
            this.hash = hash;
            putAll(hash);
        }

        /**
         * @param key
         * @param value
         */
        public Object put(Object key, Object value) {
            checkQueue();
            WeakRefValue ret = (WeakRefValue) super.put(key,
                               new WeakRefValue(value, refq, key));
            return (ret == null ? null : ret.get());
        }

        /**
         * @param key
         * @return value
         */
        public Object get(Object key) {
            WeakRefValue ret = (WeakRefValue) super.get(key);
            return (ret == null ? null : ret.get());
        }

        /**
         * @param key
         * @return removed object null if none
         */
        public Object remove(Object key) {
            WeakRefValue ret = (WeakRefValue) super.remove(key);
            return (ret == null ? null : ret.get());
        }

        /** removes garbage collected values */
        private void checkQueue() {
            for (;;) {
                WeakRefValue ref = (WeakRefValue) refq.poll();
                if (ref == null) break;
                remove(ref.getKey());
            }
        }
    }

    /** weak ref to value */
    static class WeakRefValue extends WeakReference {

        /** its key */
        private Object key;

        /**
         * @param referenced
         */
        public WeakRefValue(Object referenced, ReferenceQueue q, Object key) {
            super (referenced, q);
            this.key = key;
        }

        /** @return key for this value */
        public Object getKey() {
            return key;
        }
    }

    public static char[] readContents(Reader r) throws IOException {
        int read = 0;
        int total = 0;
        int offset;
        char[] buffer;
        List buflist = new LinkedList();

        do {
            buffer = new char[2048];
            offset = 0;
            while (offset < buffer.length) {
                read = r.read(buffer, offset, buffer.length - offset);
                if (read == -1) break;
                offset += read;
            }
            if (offset > 0) buflist.add(buffer);
            total += offset;
        } while (read >= 0);
        r.close();

        buffer = new char[total];
        Iterator it = buflist.iterator();

        int offset2 = 0;
        while (it.hasNext()) {
            char[] buf = (char[])it.next();
            int size = (it.hasNext()) ? buf.length : offset;
            System.arraycopy(buf, 0, buffer, offset2, size);
            offset2 += size;
        }
        return buffer;
    }

    /** Returns contents of fileobject fo. If the object is opened in the editor,
      the function returns current contents of the edited document. In that case,
      if <b>save</b> is true, the editor content is saved.
      If the file is not opened in a JavaEditor, it is read from the disk and 
      guarded sections are filtered out.
      @return contents of the file/editor document; guarded section markers are filtered out.
    */
    public static char[] getContent(FileObject fo, boolean save, boolean filter, String encoding) throws IOException {
        DataObject obj = DataObject.find(fo);
        JavaEditor editor = null;

        if (obj instanceof JavaDataObject)
            editor = ((JavaDataObject) obj).getJavaEditor();

        if ((editor != null) && (editor.isDocumentLoaded())) {
            // loading from the memory (Document)
            final javax.swing.text.Document doc = editor.getDocument();
            final String[] str = new String[1];
            // safely take the text from the document
            Runnable run = new Runnable() {
                               public void run() {
                                   try {
                                       str[0] = doc.getText(0, doc.getLength());
                                   }
                                   catch (javax.swing.text.BadLocationException e) {
                                       // impossible
                                   }
                               }
                           };
            if (save) {
                SaveCookie cookie = (SaveCookie) obj.getCookie(SaveCookie.class);
                if (cookie != null) {
                    cookie.save();
                }
            }
            doc.render(run);
            return str[0].toCharArray();
        } else {
            // loading from the file
            InputStream is = new BufferedInputStream(fo.getInputStream());
            Reader reader;
            if (filter) {
                reader = new JavaEditor.GuardedReader(is, true);
            } else {
                if (encoding == null) {
                    reader = new InputStreamReader(is);
                } else {
                    reader = new InputStreamReader(is, encoding);
                }
            }
            return readContents(reader);
        }
    }

    /** Creates new input stream from the file object.
    * Finds the java data object, checks if the document is loaded and
    * and create the stream either from the file object either from the document.
    * @param fo fileobject with the source
    * @param store if there is required the building and storing the elements
    *              hierarchy
    * @exception IOException if any i/o problem occured during reading
      @deprecated ParserInputStream that is being created by this function does not
        fit well in I18N environments.
    */
    public static InputStream createInputStream(FileObject fo, boolean save, boolean store) throws IOException {
        DataObject obj = DataObject.find(fo);
        JavaEditor editor = null;

        if (obj instanceof JavaDataObject)
            editor = ((JavaDataObject) obj).getJavaEditor();

        if ((editor != null) && (editor.isDocumentLoaded())) {
            // loading from the memory (Document)
            final javax.swing.text.Document doc = editor.getDocument();
            final String[] str = new String[1];
            // safely take the text from the document
            Runnable run = new Runnable() {
                               public void run() {
                                   try {
                                       str[0] = doc.getText(0, doc.getLength());
                                   }
                                   catch (javax.swing.text.BadLocationException e) {
                                       // impossible
                                   }
                               }
                           };
            if (save) {
                SaveCookie cookie = (SaveCookie) obj.getCookie(SaveCookie.class);
                if (cookie != null) {
                    cookie.save();
                }
            }
            doc.render(run);
            return new ParserInputStream(str[0]);
        }
        else {
            // loading from the file
            InputStream is = new BufferedInputStream(fo.getInputStream());
            if (store) {
                //PENDING - improve performance
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                OutputStreamWriter outWriter = new OutputStreamWriter(byteStream);
                JavaEditor.GuardedReader reader = new JavaEditor.GuardedReader(is, true);
                try {
                    int c;
                    while ((c = reader.read()) != -1) {
                        outWriter.write(c);
                    }
                }
                finally {
                    outWriter.close();
                    is.close();
                }
                is = new ByteArrayInputStream(byteStream.toByteArray());
            }
            return new ParserInputStream(is);
        }
    }

    // ==================== The parser input stream ==========================

    /** The input stream which holds all data which are read in the StringBuffer.
      @deprecated The class doesn't process character data in the stream and
        is not very usable in I18N environments.
    */
    static class ParserInputStream extends InputStream {
        /** The underlaying stream. */
        private InputStream stream;

        /** Whole text */
        private String text;

        /** The string buffer which collect the data. */
        private StringBuffer buffer;

        /** This flag determines if there is used the text field or buffer field.
        * The constructor set it
        */
        private boolean mode;

        /** The counter of read chars */
        private int counter;

        /** Offset of the begins of the lines (e.g. offset of [line,col] is lines[line] + col - 1
        */
        private int[] lines = new int[200];

        /** Current line counter - it is used for filling the lines array in the read method
        */
        int lineCounter = 2;

        /** Length of the current line
        */
        int currentLineLength = 0;

        /** Creates the stream from the text. */
        ParserInputStream(String text) {
            stream = new StringBufferInputStream(text);
            this.text = text;
            mode = false;
            counter = 0;
        }

        /** Creates the stream from the another stream. */
        ParserInputStream(InputStream stream) {
            this.stream = stream;
            buffer = new StringBuffer();
            mode = true;
        }

        /** Gets the part of the text which was already read.
        * @param begin the begin index
        * @param end the end index
        */
        public String getString(int begin, int end) {
            return mode ? buffer.substring(begin, end) : text.substring(begin, end);
        }

        /** Gets the part of the text which was already read.
        * End is last position which was already read.
        * @param begin the begin index
        */
        public String getString(int begin) {
            if (mode) {
                return buffer.substring(begin);
            }
            else {
                int end = Math.min(counter - 1, text.length());
                return text.substring(begin, end);
            }
        }

        /** Read one character from the stream. */
        public int read() throws IOException {
            int x = stream.read();
            if (mode && (x != -1)) {
                buffer.append((char)x);
                counter++;
            }

            // counting line's length
            if (x == (int)'\n') {
                if (lineCounter == lines.length - 1) {
                    int[] newLines = new int[lineCounter + lineCounter];
                    System.arraycopy(lines, 0, newLines, 0, lines.length);
                    lines = newLines;
                }
                lines[lineCounter] = lines[lineCounter - 1] + currentLineLength + 1;
                lineCounter++;
                currentLineLength = 0;
            }
            else {
                currentLineLength++;
            }

            return x;
        }

        /** Closes the stream */
        public void close() throws IOException {
            stream.close();
        }

        /** Compute offset in the stream from line and column.
         * @return the offset
         */
        int getOffset(int line, int column) {
            return lines[line] + column - 1;
        }
    }
}

/*
 * Log
 *  24   Gandalf-post-FCS1.22.1.0    3/9/00   Svatopluk Dedic Support for FileObject 
 *       content extraction into char[]
 *  23   src-jtulach1.22        1/26/00  Ales Novak      
 *  22   src-jtulach1.21        1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  21   src-jtulach1.20        12/22/99 Petr Hamernik   Update V8 Parser - old 
 *       one is still used..
 *  20   src-jtulach1.19        12/8/99  Ales Novak      
 *  19   src-jtulach1.18        12/8/99  Ales Novak      createInputStream - 
 *       boolean added
 *  18   src-jtulach1.17        12/6/99  Ales Novak      
 *  17   src-jtulach1.16        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   src-jtulach1.15        9/24/99  Petr Hamernik   debug message removed
 *  15   src-jtulach1.14        9/13/99  Petr Hamernik   minor changes
 *  14   src-jtulach1.13        8/12/99  Ales Novak      class files could be 
 *       'perfectly'  examined about their source file
 *  13   src-jtulach1.12        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  12   src-jtulach1.11        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  11   src-jtulach1.10        5/10/99  Petr Hamernik   
 *  10   src-jtulach1.9         4/30/99  Petr Hamernik   
 *  9    src-jtulach1.8         4/22/99  Petr Hamernik   indentationEngine finder
 *  8    src-jtulach1.7         4/1/99   Petr Hamernik   
 *  7    src-jtulach1.6         3/29/99  Petr Hamernik   
 *  6    src-jtulach1.5         3/29/99  Petr Hamernik   
 *  5    src-jtulach1.4         3/29/99  Ian Formanek    Commented out 
 *       compareClassesNames method to make the code compilable
 *  4    src-jtulach1.3         3/28/99  Ales Novak      
 *  3    src-jtulach1.2         3/28/99  Petr Hamernik   
 *  2    src-jtulach1.1         3/12/99  Petr Hamernik   
 *  1    src-jtulach1.0         3/10/99  Petr Hamernik   
 * $
 */
