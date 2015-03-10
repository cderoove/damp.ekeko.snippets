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
import java.lang.ref.*;
import java.util.*;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.Position;

import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObject;
import org.openide.util.*;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.text.PositionRef;
import org.openide.text.PositionBounds;
import org.openide.text.NbDocument;
import org.openide.text.EditorSupport;
import org.openide.src.*;

import org.netbeans.modules.java.gj.ParserEngine;

/** Implementation of the Source element for java source files.
 * This object mainly takes care about the parsing management.
 *
 * @author Petr Hamernik
 */
class SourceElementImpl extends ElementImpl implements SourceElement.Impl {

    // ======================= Static Part ==================================

    static final long serialVersionUID = -5791016681797582759L;

    /** Request processor used for parsing. */
    static final RequestProcessor PARSING_RP = new RequestProcessor("Java Source Parsing");

    /** Reference to <CODE>null</CODE>
    */
    static final Reference EMPTY_REF = new WeakReference(null);

    // ======================= Instance Part ==================================

    /** Appropriate java data object.
    */
    JavaDataObject jdo;

    /** Current status of the parsing. One of the STATUS_XXX constants.
    */
    int status = SourceElement.STATUS_NOT;

    /** Reference to the data and parsing runnable.
    */
    Reference dataRef = EMPTY_REF;

    /** If the parsing is in the progress this variable is set to the parsing task.
    */
    RequestProcessor.Task parsingTask = null;

    /** Current serial number of last dataRef object created for this object.
    */
    int dataRefCounter = 0;

    /** This flag is set when somebody edit the document and it is
    * cleared after reparsing.
    * It is used by parser to decide if there is required to collect
    * parsing information.
    */
    boolean dirty = false;

    /** This reference holds the dataref when source contains no classes.
    * Otherwise it is null.
    */
    DataRef hookForEmptySources = null;

    private static final boolean DEBUG = false;

    //  static HashMap m = new HashMap();

    private static Reference v8Engine = EMPTY_REF;

    static ParserEngine getCurrentV8Engine() {
        ParserEngine e = (ParserEngine) v8Engine.get();
        if (e == null) {
            e = ParserEngine.makeParsingEngine();
            if (Boolean.getBoolean("netbeans.debug.heap")) {
                v8Engine = new WeakReference(e);
            } else {
                v8Engine = new SoftReference(e);
            }
        }

        return e;
    }

    protected Object getBodyHash() {
        return null;
    }

    // ======================== Public part ====================================

    /** Constructs the implementation of source element for the given
    * java data object.
    */
    public SourceElementImpl(JavaDataObject jdo) {
        super(null);
        this.jdo = jdo;
    }

    /** Getter for the current status of the SourceElement implementation.
    * @return the status - one of the STATUS_XXX constants.
    */
    public int getStatus() {
        int ret = status;
        return ret;
    }

    /** Method that instructs the implementation of the source element
    * to prepare the element. It is non blocking method that returns
    * task that can be used to control if the operation finished or not.
    *
    * @return task to control the preparation of the elemement
    */
    public Task prepare() {
        return parseObject(Thread.MAX_PRIORITY - 1, true);
    }

    /** Sets package to the id. The id can be <CODE>null</CODE> if
    * the package should be set to default package (cleared)
    * @param id the identifier
    * @exception SourceException if the operation cannot proceed
    */
    public void setPackage (Identifier id) throws SourceException {
        final DataRef d = getData();
        try {
            String newValue = (id == null) ? "" : "package "+id.getFullName() + ";";
            PositionBounds bounds;
            final EditorSupport supp = (EditorSupport)jdo.getCookie(JavaEditor.class);

            if (d.packageId != null) {
                if (id != null && id.compareTo(d.packageId, true)) {
                    return;
                }
                bounds = d.packageBounds;
            } else {
                if (id == null) {
                    return;
                }
                bounds = createPackageBounds(supp);
                newValue = newValue + "\n";
            }

            final StyledDocument doc = supp.openDocument();
            final PositionBounds[] holder = new PositionBounds[] { bounds };
            final String text = newValue;

            Util.runAtomic(doc, new Util.ExceptionRunnable() {
                               public void run() throws Exception {
                                   StringWriter stringWriter = new StringWriter();
                                   PositionBounds b = d.packageId == null ? createNewLineBoundsAt(holder[0].getBegin()) : holder[0];
                                   Writer indentWriter = Util.findIndentWriter(doc, b.getBegin().getOffset(), stringWriter);
                                   indentWriter.write(text.toString());
                                   indentWriter.flush();
                                   b.setText(stringWriter.toString());
                                   holder[0] = b;
                               }
                           });
            org.openide.src.Identifier old = d.packageId;
            d.packageId = id;
	    d.packageBounds = holder[0];
            firePropertyChange (PROP_PACKAGE, old, d.packageId);
        }
        catch (IOException e) {
            throw new SourceException(e.getMessage());
        }
    }

    private PositionBounds createPackageBounds(EditorSupport editor) throws SourceException {
        DataRef d = (DataRef)getData();

        if (d.imports != null && d.imports.length > 0) {
            return d.importsBounds[0];
        }
        if (d.classes != null && d.classes.size() > 0) {
            return ((ElementImpl)d.classes.getFirst().getCookie(ElementImpl.class)).bounds;
        }
        return new PositionBounds(editor.createPositionRef(0, Position.Bias.Forward),
                                  editor.createPositionRef(0, Position.Bias.Backward));
    }

    /** @return the package id or <CODE>null</CODE> if we are in default package
    */
    public org.openide.src.Identifier getPackage () {
        try {
            DataRef d = getData();
            return d.packageId;
        }
        catch (SourceException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** @return the imports
    */
    public Import[] getImports() {
        try {
            DataRef d = getData();
            return d.imports;
        }
        catch (SourceException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Changes set of elements.
    * @param elems elements to change
    * @param action the action to do (ADD, REMOVE, SET)
    * @exception SourceException if the action cannot be handled
    */
    public void changeImports (Import[] elems, int action) throws SourceException {
        boolean changed = false;
        DataRef d = getData();
        Import[] oldImports = d.imports;

        try {
            switch (action) {
            case SET:
                {
                    changed = true;
                    for (int i = 0; i < d.imports.length; i++) {
                        clearBounds(d.importsBounds[i]);
                    }
                    d.imports = new Import[0];
                    d.importsBounds = new PositionBounds[0];
                    // FALL THROUGH
                }
            case ADD:
                {
                    int i;
                    List newElems = new LinkedList();

                    for (i = 0; i < elems.length; i++) {
                        Import im = elems[i];

                        if (!hasImport(im)) {
                            newElems.add(im);
                        }
                    }

                    if (newElems.size() > 0) {
                        Import[] newImports = new Import[d.imports.length + newElems.size()];
                        PositionBounds[] newBounds = new PositionBounds[newImports.length];
                        try {
                            if (d.imports != null) {
                                for (i = 0; i < d.imports.length; i++) {
                                    newImports[i] = d.imports[i];
                                    newBounds[i] = d.importsBounds[i];
                                }
                            }

                            final EditorSupport supp = (EditorSupport)jdo.getCookie(JavaEditor.class);
                            final StyledDocument doc = supp.openDocument();
                            Iterator it = newElems.iterator();
                            final PositionBounds[] holder = new PositionBounds[] { createNewImportBounds(supp) };

                            while (it.hasNext()) {
                                final Import im = (Import)it.next();

                                Util.runAtomic(doc, new Util.ExceptionRunnable() {
                                                   public void run() throws Exception {
                                                       StringWriter stringWriter = new StringWriter();
                                                       PositionBounds b = createNewLineBoundsAt(holder[0].getEnd());
                                                       Writer indentWriter = Util.findIndentWriter(doc, b.getBegin().getOffset(), stringWriter);
                                                       indentWriter.write(im.toString() + ";");
                                                       indentWriter.flush();
                                                       b.setText(stringWriter.toString());
                                                       holder[0] = b;
                                                   }
                                               });

                                newImports[i] = im;
                                newBounds[i++] = holder[0];
                                changed = true;
                            }
                            d.imports = newImports;
                            d.importsBounds = newBounds;
                        } finally {
                        }
                    }
                    break;
                }
            case REMOVE:
                {
                    boolean[] removes = new boolean[d.imports == null ? 1 : d.imports.length];
                    int removeCount = 0;
                    try {
                        for (int i = 0; i < elems.length; i++) {
                            Import im = elems[i];
                            for (int j = 0; j < d.imports.length; j++) {
                                if (d.imports[j].equals(im)) {
                                    clearBounds(d.importsBounds[j]);
                                    removes[j] = true;
                                    removeCount++;
                                    changed = true;
                                    break;
                                }
                            }
                        }
                    } finally {
                        if (removeCount > 0) {
                            Import[] newImps = new Import[d.imports.length - removeCount];
                            PositionBounds[] newBounds = new PositionBounds[newImps.length];
                            int index = 0;

                            for (int i = 0; i < d.imports.length; i++) {
                                if (!removes[i]) {
                                    newImps[index] = d.imports[i];
                                    newBounds[index++] = d.importsBounds[i];
                                }
                            }
                        }
                    }
                }
            }
        } catch (SourceException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SourceException(e.getMessage());
        } finally {
            if (changed) {
                firePropertyChange(PROP_IMPORTS, oldImports, d.imports);
            }
        }
    }

    /** Finds out if a particular import is made in the source file.
      @param im Import object that is to be found.
      @throws SourceException if the source file can not be parsed.
    */
    public boolean hasImport(Import im) throws SourceException {
        Import[] ims = getData().imports;
        if (ims == null)
            return false;
        for (int i = 0; i < ims.length; i++) {
            if (ims[i].equals(im)) {
                return true;
            }
        }
        return false;
    }

    /** Changes set of elements.
    * @param elems elements to change
    * @exception SourceException if the action cannot be handled
    */
    public void changeClasses (ClassElement[] elems, int action) throws SourceException {
        DataRef d = getData();
        d.classes.change(elems, action);
        hookForEmptySources = (d.classes.size() == 0) ? d : null;
    }

    /**
    * @return the classes of this source.
    */
    public ClassElement[] getClasses() {
        try {
            DataRef d = getData();
            return (ClassElement[]) d.classes.toArray();
        }
        catch (SourceException e) {
            e.printStackTrace();
            return new ClassElement[] {};
        }
    }

    /** Finds an inner class with given name.
    * @param name the name to look for
    * @return the element or null if such class does not exist
    */
    public ClassElement getClass (org.openide.src.Identifier name) {
        try {
            DataRef d = getData();
            return (ClassElement)d.classes.find(name, null);
        }
        catch (SourceException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Get for all classes (top level and inner classes)
    */
    public ClassElement[] getAllClasses () {
        LinkedList list = new LinkedList();
        ClassElement[] classes = getClasses();
        for (int i = 0; i < classes.length; i++)
            addAllClasses(classes[i], list);

        ClassElement[] allClasses = new ClassElement[list.size()];
        list.toArray(allClasses);
        return allClasses;
    }

    /** Read resolve. */
    public Object readResolve() {
        return new SourceElement(this);
    }

    /** Lock the underlaing document to have exclusive access to it and could make changes
    * on this SourceElement.
    *
    * @param run the action to run
    */
    public void runAtomic (Runnable run) {
        JavaEditor ed = (JavaEditor) jdo.getCookie(JavaEditor.class);
        Task t = ed.prepareDocument();
        t.waitFinished();
        StyledDocument doc = ed.getDocument();
        if (doc == null)
            run.run();
        else
            NbDocument.runAtomic(doc, run);
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
        JavaEditor ed = (JavaEditor) jdo.getCookie(JavaEditor.class);
        Task t = ed.prepareDocument();
        t.waitFinished();
        StyledDocument doc = ed.getDocument();
        if (doc == null)
            run.run();
        else {
            try {
                NbDocument.runAtomicAsUser(doc, run);
            }
            catch (BadLocationException e) {
                throw new SourceException(e.getMessage());
            }
        }
    }

    /** Find the element at the specified offset in the document.
    * @param offset The position of the element
    * @return the element at the position.
    */
    public Element findElement(int offset) {
        try {
            DataRef d = getData();
            Element retElement = d.classes.findElement(offset);
            if (retElement != null)
                return retElement;
        }
        catch (SourceException e) {
        }
        return element;
    }

    public void markCurrent(boolean beforeAfter) {
    }

    /** Get a cookie from the node.
    *
    * @param type the representation class
    * @return the cookie or <code>null</code>
    */
    public Node.Cookie getCookie (Class type) {
        Node.Cookie c = super.getCookie(type);
        if (c == null) {
            if (type.equals(DataObject.class) || type.equals(MultiDataObject.class) ||
                    JavaDataObject.class.isAssignableFrom(type)) {
                return jdo;
            }
        }
        return c;
    }

    // ======================== Package private part ================================

    JavaDataObject getJavaDataObject() {
        return jdo;
    }

    /** Sets the dirty flag - if the document was modified after last parsing. */
    void setDirty(boolean b) {
        dirty = b;
    }

    /** Tests the dirty flag. */
    boolean isDirty() {
        return dirty;
    }

    /** Starts the parsing if the this class is 'dirty' and status != STATUS_NOT
    * and parsing is not running yet.
      @return parsing task so caller may listen on its completion.
    */
    Task autoParse() {
        return parseObject(Thread.MIN_PRIORITY, false);
    }

    /** Starts the parsing if the this class is 'dirty' and status != STATUS_NOT
    * and parsing is not running yet.
      @return parsing task so caller may listen on its completion.
    */
    Task afterSaveParse() {
        return parseObject(Thread.MIN_PRIORITY, true);
    }

    SourceElementImpl findSourceElementImpl() {
        return this;
    }

    void registerForName(Identifier id, JavaConnections.Type type) {
        jdo.registerForName(id, type);
    }

    void unregisterForName(Identifier id, JavaConnections.Type type) {
        jdo.unregisterForName(id, type);
    }

    // ======================== Private part ================================

    private synchronized Task parseObject(int priority, boolean forceParsing) {
        RequestProcessor.Task t = parsingTask;
        DataRef d = (DataRef) dataRef.get();

        if (t == null) {
            if (d == null) {
                d = new DataRef(jdo, ++dataRefCounter);
            }
            d.forceParsing = forceParsing;
            t = PARSING_RP.post(d, 0, priority);
            parsingTask = t;
        }
        else {
            if (t.cancel()) {
                t.schedule(0);
            }
            t.setPriority(priority);
            //PENDING - jenom zvysovat
            //PENDING - set forceParsing
        }

        return t;
    }

    /**
    *
    * @return the DataRef object holding the parsing information
    * @exception SourceException if parsing failed.
    */
    private DataRef getData() throws SourceException {
        DataRef d = (DataRef) dataRef.get();
        if (d != null)
            return d;

        Task t = prepare();
        t.waitFinished();
        d = (DataRef) dataRef.get();
        if (d != null)
            return d;

        //printLog(jdo);
        // IMPORTANT NOTICE:
        // Don't remove this line. It is necessary to hold 't' local variable here, because otherwise
        // sometimes happen that HotSpot optimize this method and garbage collector can release
        // the object holded by dataRef Reference object. [Petr Hamernik]
        t.isFinished();
        // ---------------------------

        //System.out.println ("BLBE:"+jdo.getName());
        throw new SourceException(Util.getString("EXC_CannotParse"));
    }

    /** Adds the class to the list and also all its innerclasses.
    * @param c ClassElement to add
    * @param list where to add.
    */
    private void addAllClasses(ClassElement c, LinkedList list) {
        list.add(c);
        ClassElement[] innerClasses = c.getClasses();
        for (int i = 0; i < innerClasses.length; i++) {
            addAllClasses(innerClasses[i], list);
        }
    }

    /** Informs the SourceElement about releasing data (classes, imports,...)
    * from the memory. This method gets as the parameter DataRef which will be
    * garbage collected and should swap them to the disk.
    */
    private void dataRefReleased(final DataRef releasedData) {
        PARSING_RP.post(new Runnable() {
                            public void run() {
                                if (releasedData.number == dataRefCounter) {
                                    int old = status;
                                    status = SourceElement.STATUS_NOT;
                                    //          String n = jdo.getPrimaryFile().getPackageName('.'); m.remove(n);
                                    firePropertyChange(PROP_STATUS, new Integer(old), new Integer(SourceElement.STATUS_NOT));
                                }
                            }
                        }, Thread.MAX_PRIORITY);
    }

    /** Fire change of cookies to all elements, if this object is parsed.
    * Used for propagating cookies changes from JavaDataObject to ElementNodes.
    */
    void fireCookiesChange() {
        DataRef d = (DataRef) dataRef.get();
        if (d != null) {
            ClassElement[] classes = getAllClasses();
            for (int i = 0; i < classes.length; i++) {
                Element[] els;
                for (int j = 0; j <= 3; j++) {
                    switch (j) {
                    case 0: els = classes[i].getInitializers(); break;
                    case 1: els = classes[i].getFields(); break;
                    case 2: els = classes[i].getConstructors(); break;
                    default: els = classes[i].getMethods(); break;
                    }
                    for (int k = 0; k < els.length; k++) {
                        ElementImpl impl = (ElementImpl) els[k].getCookie(ElementImpl.class);
                        impl.firePropertyChange(Node.PROP_COOKIE, null, null);
                    }
                }
                ElementImpl impl = (ElementImpl) classes[i].getCookie(ElementImpl.class);
                impl.firePropertyChange(Node.PROP_COOKIE, null, null);
            }
        }
    }

    PositionBounds createNewImportBounds(EditorSupport editor) throws SourceException {
        DataRef d = getData();

        if (d.imports != null && d.imports.length > 0) {
            return d.importsBounds[d.imports.length - 1];
        }

        int offset = 0;

        if (d.packageId != null) {
            offset = d.packageBounds.getEnd().getOffset() + 1;
        }
        return new PositionBounds(editor.createPositionRef(offset, Position.Bias.Forward),
                                  editor.createPositionRef(offset, Position.Bias.Backward));
    }
    
    /** Creates bounds for specific element type (currently only class elements)
    */
    PositionBounds createBoundsFor(ElementsCollection col) throws SourceException {
        DataRef d = getData();
        if (col != d.classes) {
            throw new InternalError("Invalid parent for collection " + col.toString());
        }

        ClassElement celem = (ClassElement)d.classes.getLast();
        if (celem != null) {
            ClassElementImpl clazz = (ClassElementImpl)celem.getCookie(ElementImpl.class);
            return createNewLineBoundsAt(clazz.bounds.getEnd());
        }
        try {
            JavaEditor jed = jdo.getJavaEditor();
            javax.swing.text.StyledDocument doc = jed.openDocument();
            Position pos = doc.getEndPosition();
            PositionRef posref = jdo.getJavaEditor().createPositionRef(pos.getOffset(), Position.Bias.Backward);
            return createNewLineBoundsAt(posref);
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptios")) // NOI18N
                e.printStackTrace();
            throw new SourceException(e.getLocalizedMessage());
        }
    }
    
    PositionRef findUnguarded(PositionRef pos, PositionBounds limits) throws SourceException {
	PositionRef result;
	
	result = jdo.getJavaEditor().findUnguarded(pos, false, true);
	if (limits.getBegin().getOffset() > result.getOffset() ||
	    limits.getEnd().getOffset() < result.getOffset()) {
	}
	return result;
    }
    
    /** Creates bounds for a new line after the specified position. If the line containing `where'
      is not empty, insertion point is placed right after the line.
    */
    static PositionBounds createNewLineBoundsAt(PositionRef where) throws SourceException {
        try {
            EditorSupport editor = where.getEditorSupport();
            javax.swing.text.StyledDocument doc = editor.openDocument();

            int beginText = where.getOffset();

            int lineIndex = NbDocument.findLineNumber(doc, beginText);
            javax.swing.text.Element line = NbDocument.findLineRootElement(doc).getElement(lineIndex);
            int lineBegin = line.getStartOffset();
            int lineEnd = line.getEndOffset();

            String lineStr = doc.getText(lineBegin, beginText - lineBegin);
            String trimLine = lineStr.trim();
            int newBlockOffset = lineBegin;
            if (trimLine.length() > 0) {
                // move to the next line ;-)
                newBlockOffset += lineStr.lastIndexOf(trimLine) + trimLine.length();
            }

            PositionRef posBegin = editor.createPositionRef(newBlockOffset, Position.Bias.Forward);
            PositionRef posEnd = editor.createPositionRef(newBlockOffset, Position.Bias.Backward);
            PositionBounds bounds = new PositionBounds(posBegin, posEnd);
            if (newBlockOffset == lineEnd || newBlockOffset == lineBegin) {
                bounds.insertAfter("\n"); // NOI18N
            } else {
                bounds.insertAfter("\n\n"); // NOI18N
            }
            if (newBlockOffset == lineBegin) {
                return bounds;
            }
            posBegin = editor.createPositionRef(newBlockOffset + 1, Position.Bias.Forward);
            posEnd = editor.createPositionRef(newBlockOffset + 1, Position.Bias.Backward);
            return new PositionBounds(posBegin, posEnd);
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                e.printStackTrace();
            throw new SourceException(e.getMessage());
        }
    }

    /** Creates new bounds at the given position. If the line containing `where' is not empty,
      the function skips to the end of the line.
    */
    static PositionBounds createBoundsAt(PositionRef where) throws SourceException {
        try {
            EditorSupport editor = where.getEditorSupport();
            javax.swing.text.StyledDocument doc = editor.openDocument();

            int beginText = where.getOffset();

            int lineIndex = NbDocument.findLineNumber(doc, beginText);
            javax.swing.text.Element line = NbDocument.findLineRootElement(doc).getElement(lineIndex);
            int lineBegin = line.getStartOffset();
            int lineEnd = line.getEndOffset();

            String lineStr = doc.getText(lineBegin, beginText - lineBegin);
            String trimLine = lineStr.trim();
            int newBlockOffset = lineBegin;
            if (trimLine.length() > 0)
                newBlockOffset += lineStr.lastIndexOf(trimLine) + trimLine.length();

            PositionRef posBegin = editor.createPositionRef(newBlockOffset, Position.Bias.Forward);
            PositionRef posEnd = editor.createPositionRef(newBlockOffset, Position.Bias.Backward);
            PositionBounds bounds = new PositionBounds(posBegin, posEnd);
            bounds.insertAfter("\n"); // NOI18N
            return bounds;
        }
        catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptios")) // NOI18N
                e.printStackTrace();
            throw new SourceException(e.getMessage());
        }
    }

    static void clearBounds(final PositionBounds bounds) throws SourceException {
        final Exception[] hold = new Exception[] { null };
        try {
            final javax.swing.text.StyledDocument doc = bounds.getBegin().getEditorSupport().openDocument();

            Runnable run = new Runnable() {
                               public void run() {
                                   // remove the range of the element.
                                   int p1 = bounds.getBegin().getOffset();
                                   int p2 = bounds.getEnd().getOffset();
                                   int p3 = 0;
                                   try {
                                       doc.remove(p1, p2 - p1);
                                   }
                                   catch (BadLocationException e) {
                                       hold[0] = e;
                                       return;
                                   }

                                   // remove empty space where was the element placed
                                   int lineIndex = NbDocument.findLineNumber(doc, p1);
                                   javax.swing.text.Element lineRoot;
                                   lineRoot = NbDocument.findLineRootElement(doc);
                                   int lineCount = lineRoot.getElementCount();
                                   javax.swing.text.Element line = lineRoot.getElement(lineIndex);
                                   p1 = line.getStartOffset();
                                   p2 = line.getEndOffset();
                                   if (lineIndex < lineCount - 1) {
                                       line = NbDocument.findLineRootElement(doc).getElement(lineIndex + 1);
                                       p3 = line.getStartOffset();
                                   } else {
                                       p3 = p2;
                                   }
                                   try {
                                       String lineStr = doc.getText(p1, p2 - p1).trim();
                                       if (lineStr.length() == 0) {
                                           doc.remove(p1, p3 - p1);
                                       }
                                   }
                                   catch (BadLocationException e) {
                                   }
                               }
                           };
            NbDocument.runAtomic (doc, run);
        }
        catch (IOException e) {
            hold[0] = e;
        }
        if (hold[0] != null)
            throw new SourceException(hold[0].getMessage());
    }

    // ======================== Realy data holder ==========================

    /** Class which is used for holding the parsed information.
    * The source element impl holds only Reference to this object.
    */
    private static class DataRef extends Object implements Runnable {
        /** A serial version UID */
        static final long serialVersionUID = 697350931687937673L;

        /** Appropriate java data object.
        */
        JavaDataObject jdo;

        /** The sequence number of the this object created for one SourceElement.
        */
        int number;

        /** This really means `force results to be displayed', not force parsing to happen ;-) */
        boolean forceParsing = true;
        boolean virgin = true;

        // --------------- Data -------------------

        /** Package for the source */
        org.openide.src.Identifier packageId;

        /** The bound of the package declaration. Could be null
        * for default package.
        */
        PositionBounds packageBounds;

        /** Array of the imports. */
        Import[] imports;

        /** Position bounds for each import. */
        PositionBounds[] importsBounds;

        /** Collection of the classes */
        ElementsCollection.Class classes;

        /** Creates new data holder. */
        DataRef(JavaDataObject jdo, int number) {
            this.jdo = jdo;
            this.number = number;

            packageId = null;
            packageBounds = null;
            imports = new Import[] {};
            importsBounds = new PositionBounds[] {};
            classes = new ElementsCollection.Class(jdo.getSourceElementImpl());
        }

        /** Informs the SourceElementImpl about the releasing
        * of this class from the memory.
        */
        public void finalize() throws Throwable {
            jdo.getSourceElementImpl().dataRefReleased(this);
            super.finalize();
        }

        public void run() {
            try {
                doIt();
                // without this, the null value might be overwritten by another thread, that is currently
                // inside parseObject (in case that the parsing has finished *very* quickly)
	    } catch (Exception e) {
		if (Boolean.getBoolean("netbeans.debug.exceptions")) {
		    e.printStackTrace();
		}
            } finally {
                synchronized(jdo.getSourceElementImpl()) {
                    jdo.getSourceElementImpl().parsingTask = null;
                }
            }
        }

        /** Run the task. */
        public void doIt() {
            final SourceElementImpl impl = jdo.getSourceElementImpl();
            if (!virgin) {
                if ((!forceParsing && (!impl.dirty || (impl.status == SourceElement.STATUS_NOT))) ||
                        (forceParsing && !impl.dirty && (impl.status != SourceElement.STATUS_NOT))) {
                    return;
                }
            }

            // parse
            boolean partialError;
            V8ParseRequest request;
            ParsingResult result;
            ParserEngine engine = getCurrentV8Engine();

            synchronized(engine.getSyncObject()) {
                request = new V8ParseRequest(jdo);

		do {
    		    impl.setDirty(false);
            	    engine.parseObject(request);
		    
                    result = request.result;
            	    partialError = request.getSyntaxErrors() > 0;
                    if (!virgin && !forceParsing && (partialError || (result == null))) {
            		// if parsing was forced (== results have to be updated), update even if there are some errors.			
            		return;
		    }
        	} while (impl.isDirty());
            }

            // clear internal compiler tables after parsing...
            //engine.clear();


            virgin = false;
            forceParsing = false;
            int oldStatus = impl.status;
            final LinkedList changes = new LinkedList();
            int changesMask = jdo.getAllListenersMask();

            int newStatus = oldStatus;
            if (result == null) {
                result = new ParsingResult();
                newStatus = SourceElement.STATUS_ERROR;
            }
            else {
                newStatus = partialError ? SourceElement.STATUS_PARTIAL : SourceElement.STATUS_OK;
            }

            boolean checkInterfaces = ((impl.status == SourceElement.STATUS_OK) && (impl.status != oldStatus));

            // set package
            packageBounds = result.packageBounds;
            boolean changed = (packageId == null) && (result.packageId != null);
            changed |= (packageId != null) && (result.packageId == null);
            if (changed || ((packageId != null) && !packageId.equals(result.packageId))) {
                Identifier oldId = packageId;
                packageId = result.packageId;
                if (oldStatus == SourceElement.STATUS_OK)
                    impl.firePropertyChange (PROP_PACKAGE, oldId, packageId);
            }

            // imports
            changed = (imports.length != result.imports.size());
            Import[] newImports = new Import[result.imports.size()];
            result.imports.toArray(newImports);
            if (!changed) {
                int size = imports.length;
                for (int i = 0; i < size; i++) {
                    if (!imports[i].equals(newImports[i])) {
                        changed = true;
                        break;
                    }
                }
            }
            importsBounds = new PositionBounds[result.importsBounds.size()];
            result.importsBounds.toArray(importsBounds);
            if (changed) {
                Import[] oldImports = imports;
                imports = newImports;
                impl.firePropertyChange(PROP_IMPORTS, oldImports, imports);
            }

            // classes
            classes.updateContent(result.classes, changes, changesMask);
            ClassElement[] classArray = (ClassElement[]) classes.toArray();
            for (int i = 0; i < classArray.length; i++) {
                ((ClassElementImpl)classArray[i].getCookie(ClassElementImpl.class)).hook = this;
            }

            impl.dataRef = new WeakReference(this);
            impl.status = newStatus;

            impl.hookForEmptySources = (classes.size() == 0) ? this : null;

            // fires the change of the status - it is required to fire it everytime
            // because of the icon changes in the delegate node.
            impl.firePropertyChange (PROP_STATUS, null, null);
            Parsing.fireEvent(jdo, impl.hookForEmptySources);

            // Connections
            if (changes.size() > 0) {
                PARSING_RP.post(new Runnable() {
                                    public void run() {
                                        fireJavaConnections(changes);
                                    }
                                }, Thread.MIN_PRIORITY);
            }

            if (packageId != null) {
                String tmp = packageId.getFullName();
                if (!tmp.startsWith("java.") && !tmp.startsWith("javax.")) {
                    final Object hookForTheFollowingRunnable = this;
                    if (checkInterfaces && JavaConnections.SETTINGS.isEnabled()) {
                        PARSING_RP.post(new Runnable() {
                                            public void run() {
                                                if (hookForTheFollowingRunnable != null) { //only gc prevention (may be some optim.)
                                                    ClassElement[] classes2 = impl.getAllClasses();
                                                    for (int i = 0; i < classes2.length; i++) {
                                                        ((ClassElementImpl)classes2[i].getCookie(ClassElementImpl.class)).checkInterfaces();
                                                    }
                                                }
                                            }
                                        }, Thread.MIN_PRIORITY);
                    }
                }
            }

            // Store the hook to the editor if it has loaded document.
            JavaEditor ed = jdo.getJavaEditor();
            if (ed.isDocumentLoaded()) {
                ed.parsedHook = this;
            }
        }

        void fireJavaConnections(LinkedList changes) {
            JavaConnections.Change[] arr = new JavaConnections.Change[changes.size()];
            changes.toArray(arr);
            int wholeEventMask = 0;
            for (int i = 0; i < arr.length; i++) {
                wholeEventMask |= arr[i].getChangeType();
            }
            JavaConnections.Type type = new JavaConnections.Type(wholeEventMask);
            JavaConnections.Event event =
                new JavaConnections.Event(jdo.getNodeDelegate(), arr, type);
            jdo.getConnectionSupport().fireEvent(event);
        }
    }

    // ======================== Utility - Debug ==========================

    static HashMap map = new HashMap();
    static void addLog(JavaDataObject jdo, String msg) {
        /*
        String n = jdo.getPrimaryFile().getPackageName('.');
        LinkedList list = (LinkedList) map.get(n);
        if (list == null) {
          list = new LinkedList();
          map.put(n, list);
    }
        String t = Thread.currentThread().getName();
        list.add(t+": "+msg);
        */
    }

    static void printLog(JavaDataObject jdo) {
        /*
        String n = jdo.getPrimaryFile().getPackageName('.');
        System.out.println ("========================= "+n+" ==============================");
        LinkedList list = (LinkedList) map.get(n);
        if (list != null) {
          java.util.Iterator it = list.iterator();
          while (it.hasNext()) {
            System.out.println (it.next());
          }
    }
        */
    }
}

/*
 * Log
 *  79   Gandalf-post-FCS1.65.2.12   4/18/00  Svatopluk Dedic afterSaveParse dirties 
 *       the data so they're parsed in any case.
 *  78   Gandalf-post-FCS1.65.2.11   4/17/00  Svatopluk Dedic Fixed new line insertion
 *       code
 *  77   Gandalf-post-FCS1.65.2.10   4/14/00  Svatopluk Dedic Adjusted dirty flag 
 *       clearing.
 *  76   Gandalf-post-FCS1.65.2.9    4/5/00   Svatopluk Dedic 
 *  75   Gandalf-post-FCS1.65.2.8    4/4/00   Svatopluk Dedic Corrected 
 *       error/up-to-date parsing data checking
 *  74   Gandalf-post-FCS1.65.2.7    4/3/00   Svatopluk Dedic Sync improved
 *  73   Gandalf-post-FCS1.65.2.6    4/3/00   Svatopluk Dedic Improved package 
 *       setting; fixed unnecessary parsing task scheduling.
 *  72   Gandalf-post-FCS1.65.2.5    3/27/00  Svatopluk Dedic Disabled computation of 
 *       body hash; no PROP_BODY change event is ever fired.
 *  71   Gandalf-post-FCS1.65.2.4    3/13/00  Svatopluk Dedic Rest of addlog debugging
 *       removed
 *  70   Gandalf-post-FCS1.65.2.3    3/8/00   Svatopluk Dedic Better error reporting; 
 *       addLogs removed
 *  69   Gandalf-post-FCS1.65.2.2    3/6/00   Svatopluk Dedic Changed 
 *       construction/synchronization of parsing engine
 *  68   Gandalf-post-FCS1.65.2.1    2/24/00  Svatopluk Dedic After autoparse, a node 
 *       corresponding to editor caret's position will be selected
 *  67   Gandalf-post-FCS1.65.2.0    2/24/00  Ian Formanek    Post FCS changes
 *  66   src-jtulach1.65        2/15/00  Svatopluk Dedic Debug output commented 
 *       out
 *  65   src-jtulach1.64        2/14/00  Svatopluk Dedic Fixed #5283
 *  64   src-jtulach1.63        1/18/00  Petr Hamernik   message removed
 *  63   src-jtulach1.62        1/18/00  Petr Hamernik   fixed #2119
 *  62   src-jtulach1.61        1/13/00  Petr Hamernik   i18n -(2nd round) - 
 *       script bug fixed.
 *  61   src-jtulach1.60        1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  60   src-jtulach1.59        11/27/99 Patrik Knakal   
 *  59   src-jtulach1.58        10/27/99 Petr Hamernik   synchronization is 
 *       disabled for java.* and javax.* classes
 *  58   src-jtulach1.57        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  57   src-jtulach1.56        10/7/99  Petr Hamernik   fixed bugs #2946, #3621 
 *       - cookies changes of JavaDO (e.g.save) is propagated to all 
 *       ElementNodes
 *  56   src-jtulach1.55        10/7/99  Petr Hamernik   Java module has its own 
 *       RequestProcessor for source parsing.
 *  55   src-jtulach1.54        9/27/99  Petr Hamernik   debug printing removed
 *  54   src-jtulach1.53        9/24/99  Petr Hamernik   closing stream and other
 *       improvements
 *  53   src-jtulach1.52        9/13/99  Petr Hamernik   fixed bug #2078 - firing
 *       of IMPORT property.
 *  52   src-jtulach1.51        9/13/99  Petr Hamernik   runAtomicAsUser 
 *       implementation
 *  51   src-jtulach1.50        9/10/99  Petr Hamernik   threading model changes,
 *       props of synchronization removed
 *  50   src-jtulach1.49        8/18/99  Petr Hamernik   i18n
 *  49   src-jtulach1.48        8/17/99  Petr Hamernik   parsing is invoked sync 
 *       in AWT-Event-Queue (otherwise is sent to RequestProcessor)
 *  48   src-jtulach1.47        8/6/99   Petr Hamernik   Working with threads 
 *       improved
 *  47   src-jtulach1.46        7/23/99  Petr Hamernik   global parsing listener
 *  46   src-jtulach1.45        7/19/99  Petr Hamernik   findElement(int) 
 *       implemented
 *  45   src-jtulach1.44        7/17/99  Petr Hamernik   deadlock prevention
 *  44   src-jtulach1.43        7/8/99   Petr Hamernik   changes reflecting 
 *       org.openide.src changes
 *  43   src-jtulach1.42        7/3/99   Petr Hamernik   SourceCookie.Editor - 
 *       1st version
 *  42   src-jtulach1.41        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  41   src-jtulach1.40        6/7/99   Petr Hamernik   small improvement
 *  40   src-jtulach1.39        6/5/99   Petr Hamernik   synchronization problem 
 *       fixed
 *  39   src-jtulach1.38        6/4/99   Petr Hamernik   synchronization update
 *  38   src-jtulach1.37        6/2/99   Petr Hamernik   connections of java 
 *       sources
 *  37   src-jtulach1.36        5/21/99  Petr Hamernik   parsing info garbage 
 *       collected too fast - fixed
 *  36   src-jtulach1.35        5/15/99  Petr Hamernik   fixed bug #1752
 *  35   src-jtulach1.34        5/14/99  Petr Hamernik   bufix in parsing after 
 *       close
 *  34   src-jtulach1.33        5/13/99  Petr Hamernik   changes in comparing 
 *       Identifier, Type classes
 *  33   src-jtulach1.32        5/12/99  Petr Hamernik   ide.src.Identifier 
 *       changed
 *  32   src-jtulach1.31        5/10/99  Petr Hamernik   
 *  31   src-jtulach1.30        4/28/99  Petr Hamernik   simple synchronization 
 *       using ConnectionCookie
 *  30   src-jtulach1.29        4/23/99  Petr Hamernik   Mutex synchr improved
 *  29   src-jtulach1.28        4/23/99  Petr Hamernik   MUTEX synchronization 
 *       changed
 *  28   src-jtulach1.27        4/21/99  Petr Hamernik   Java module updated
 *  27   src-jtulach1.26        4/21/99  Petr Hamernik   performance improved
 *  26   src-jtulach1.25        4/16/99  Petr Hamernik   synchronization under 
 *       Nodes.MUTEX
 *  25   src-jtulach1.24        4/15/99  Petr Hamernik   parser improvements
 *  24   src-jtulach1.23        4/7/99   Petr Hamernik   synchronization improved
 *  23   src-jtulach1.22        4/6/99   Petr Hamernik   bugfixes
 *  22   src-jtulach1.21        4/4/99   Ian Formanek    Patched to prevent 
 *       errors during compilation of Form DataObjects
 *  21   src-jtulach1.20        4/1/99   Petr Hamernik   
 *  20   src-jtulach1.19        4/1/99   Petr Hamernik   
 *  19   src-jtulach1.18        3/29/99  Petr Hamernik   
 *  18   src-jtulach1.17        3/29/99  Petr Hamernik   
 *  17   src-jtulach1.16        3/29/99  Petr Hamernik   
 *  16   src-jtulach1.15        3/19/99  Petr Hamernik   simple temp hack 
 *  15   src-jtulach1.14        3/18/99  Petr Hamernik   
 *  14   src-jtulach1.13        3/12/99  Petr Hamernik   
 *  13   src-jtulach1.12        3/10/99  Petr Hamernik   
 *  12   src-jtulach1.11        3/2/99   Jan Jancura     
 *  11   src-jtulach1.10        2/25/99  Petr Hamernik   
 *  10   src-jtulach1.9         2/19/99  Petr Hamernik   
 *  9    src-jtulach1.8         2/18/99  Petr Hamernik   
 *  8    src-jtulach1.7         2/18/99  Petr Hamernik   
 *  7    src-jtulach1.6         2/17/99  Petr Hamernik   
 *  6    src-jtulach1.5         2/17/99  Petr Hamernik   
 *  5    src-jtulach1.4         2/17/99  Petr Hamernik   
 *  4    src-jtulach1.3         2/12/99  Petr Hamernik   
 *  3    src-jtulach1.2         2/12/99  Petr Hamernik   
 *  2    src-jtulach1.1         2/11/99  Petr Hamernik   
 *  1    src-jtulach1.0         2/11/99  Petr Hamernik   
 * $
 */
