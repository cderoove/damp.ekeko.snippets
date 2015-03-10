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

import java.beans.PropertyVetoException;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.SwingUtilities;
import javax.swing.JEditorPane;
import javax.swing.Timer;
import javax.swing.event.*;
import javax.swing.text.*;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.ToggleBreakpointAction;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.SourceCookie;
import org.openide.debugger.Debugger;
import org.openide.debugger.Breakpoint;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.text.*;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.src.SourceElement;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;

import org.netbeans.modules.java.settings.JavaSettings;

/*
* TODO:
* 1) undo support
*/
/** Java source-file extension for handling the Editor.
* The main purpose of this class is to manage guarded sections.
*
*
* @author Petr Hamernik
*/
public class JavaEditor extends EditorSupport implements SourceCookie.Editor {
    /** The prefix of all magic strings */
    private final static String MAGIC_PREFIX = "//GEN-"; // NOI18N

    /** Magic strings - special comments which are inserted during saving
    * and are removed during loading.
    */
    private final static String[] SECTION_MAGICS = {
        MAGIC_PREFIX+"LINE:", MAGIC_PREFIX+"BEGIN:", MAGIC_PREFIX+"END:", // NOI18N
        MAGIC_PREFIX+"HEADER:", MAGIC_PREFIX+"HEADEREND:", // NOI18N
        MAGIC_PREFIX+"FIRST:", MAGIC_PREFIX+"LAST:", // NOI18N
    };

    /** Types of the magic comments. */
    private final static int T_LINE      = 0;
    private final static int T_BEGIN     = 1;
    private final static int T_END       = 2;
    private final static int T_HEADER    = 3;
    private final static int T_HEADEREND = 4;
    // obsoleted - only for backward compatibility
    private final static int T_FIRST     = 5;
    private final static int T_LAST      = 6;

    /** Table of the guarded sections. Keys are the names of the sections
    * and values are the GuardedSection classes. The table is null till
    * while document is not in the memory.
    * @associates GuardedSection
    */
    HashMap sections;

    /** Timer which countdowns the auto-reparsing time. */
    Timer timer;

    /** New lines in this file was delimited by '\n' */
    static final byte NEW_LINE_N = 0;

    /** New lines in this file was delimited by '\r' */
    static final byte NEW_LINE_R = 1;

    /** New lines in this file was delimited by '\r\n' */
    static final byte NEW_LINE_RN = 2;

    /** The type of new lines */
    byte newLineType;

    /** The hook to the parsed hierarchy - it shouldn't be released
    * when document is loaded.
    */
    Object parsedHook;

    /** True, if the document was saved from the last modification.
      This is like isModified(), but does not get cleared after user chooses Don't save during
      close.
    */
    private boolean documentSaved;

    /** False, if the document is parsed for the first time from its opening [the parse started inside
        loadFromStreamToKit(), true otherwise.
        If parsing is completed && no windows are created for the document, the document is saved
        (if needed) and closed.
    */
    private boolean initial = false;

    /** True, if there's a visible editor component flying around.
    */
    private boolean componentsCreated = false;

    /** Java Settings */
    static final JavaSettings settings = new JavaSettings();

    /** Create a new Editor support for the given Java source.
    * @param entry the (primary) file entry representing the Java source file
    */
    public JavaEditor(MultiDataObject.Entry entry) {
        super(entry);

        sections = null;

        // initialize timer
        timer = new Timer(0, new java.awt.event.ActionListener() {
                              public void actionPerformed(java.awt.event.ActionEvent e) {
                                  changeTimeoutElapsed();
                              }
                          });
        timer.setInitialDelay(settings.getAutoParsingDelay());
        timer.setRepeats(false);

        // create document listener
        final DocumentListener docListener = new DocumentListener() {
                                                 public void insertUpdate(DocumentEvent e) { change(e); }
                                                 public void changedUpdate(DocumentEvent e) { }
                                                 public void removeUpdate(DocumentEvent e) { change(e); }

                                                 private void change(DocumentEvent e) {
                                                     documentSaved = false;
                                                     restartTimer(false);
                                                 }
                                             };

        // add change listener
        addChangeListener(new ChangeListener() {
                              public void stateChanged(ChangeEvent evt) {
                                  if (isDocumentLoaded()) {
                                      getDocument().addDocumentListener(docListener);
                                  } else {
									  notifyClose();
								  }
                              }
                          });
    }

    private void changeTimeoutElapsed() {
        SourceElementImpl impl = ((JavaDataObject) getJavaEntry().getDataObject()).getSourceElementImpl();
        // if no visual components were opened, this is the right place to save & close the document:
        if (isModified()) {
            impl.autoParse().addTaskListener(new TaskListener() {
                                                 public void taskFinished(Task t) {
                                                     notifyParsingDone();
                                                 }
                                             });
        } else {
            // shorthand in case there are no changes.
            notifyParsingDone();
        }
    }

    /** Restart the timer which starts the parser after the specified delay.
    * @param onlyIfRunning Restarts the timer only if it is already running
    */
    void restartTimer(boolean onlyIfRunning) {
        if (onlyIfRunning && !timer.isRunning())
            return;

        int delay = settings.getAutoParsingDelay();
        if (delay > 0) {
            timer.setInitialDelay(delay);
            timer.restart();
        }
    }

    /* Calls superclass.
    * @param pos Where to place the caret.
    * @return always non null editor
    */
    protected EditorSupport.Editor openAt(PositionRef pos) {
        return super.openAt(pos);
    }

    /** Notify about the editor closing.
    */
    protected void notifyClose() {
        clearSections();
        JavaDataObject jdo = (JavaDataObject) getJavaEntry().getDataObject();
        if (!documentSaved) {
			documentSaved = true;      
            jdo.getSourceElementImpl().setDirty(true);
            jdo.getSourceElementImpl().afterSaveParse();
        }
        // last component was closed.
        componentsCreated = false;
        parsedHook = null;
    }

    /** Notify that parsing task has been finished; some dependent data may now
      be refreshed from up-to-date parsing info 
    */
    protected void notifyParsingDone() {
    }

    /** Read the file from the stream, filter the guarded section
    * comments, and mark the sections in the editor.
    *
    * @param doc the document to read into
    * @param stream the open stream to read from
    * @param kit the associated editor kit
    * @throws IOException if there was a problem reading the file
    * @throws BadLocationException should not normally be thrown
    * @see #saveFromKitToStream
    */
    protected void loadFromStreamToKit (StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        boolean filling = false;

        if (sections == null) {
            sections = new HashMap(10);
            filling = true;
        }

        if (filling) {
            GuardedReader reader = new GuardedReader(stream, false);
            kit.read(reader, doc, 0);
            fillSections(reader, doc);
            newLineType = reader.getNewLineType();
        }
        else {
            kit.read(stream, doc, 0);
        }
        // stream is not closed, because is it close outside this method

        initial = false;
        // parse it and remember the hook to prevent to garbage collected
        // parsed hierarchy
        SwingUtilities.invokeLater(new Runnable() {
                                       public void run() {
                                           final JavaDataObject jdo = (JavaDataObject) getJavaEntry().getDataObject();
                                           jdo.getSourceElementImpl().setDirty(true);
                                           documentSaved = true;
                                           Task task = jdo.getSourceElementImpl().prepare();
                                           task.addTaskListener(new TaskListener() {
                                                                    public void taskFinished(Task task2) {
                                                                        if (isDocumentLoaded()) {
                                                                            java.lang.ref.Reference ref = jdo.getSourceElementImpl().dataRef;
                                                                            if (ref != null) {
                                                                                parsedHook = ref.get();
                                                                            }
                                                                            notifyParsingDone();
                                                                        }
                                                                    }
                                                                });
                                       }
                                   });
    }

    /** Store the document and add the special comments signifying
    * guarded sections.
    *
    * @param doc the document to write from
    * @param kit the associated editor kit
    * @param stream the open stream to write to
    * @throws IOException if there was a problem writing the file
    * @throws BadLocationException should not normally be thrown
    * @see #loadFromStreamToKit
    */
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        OutputStream os = new NewLineOutputStream(stream, newLineType);
        if (sections != null) {
            ArrayList list = new ArrayList(sections.values());
            if (list.size() > 0) {
                GuardedWriter writer = new GuardedWriter(os, list);
                kit.write(writer, doc, 0, doc.getLength());
                return;
            }
        }
        kit.write(os, doc, 0, doc.getLength());
    }

    /** Reload the document when changed externally */
    protected void reloadDocument() {
        StyledDocument doc = null;
        try {
            doc = openDocument();
        }
        catch (IOException e) {
            // no reload performed in this case
        }

        if (doc != null) {
            clearSections();
            NbDocument.unmarkGuarded(doc, 0, doc.getLength());

            initial = false;
            super.reloadDocument();
        }
    }

    /** Save the document in this thread and start reparsing it.
    * @exception IOException on I/O error
    */
    public void saveDocument () throws IOException {
        saveDocument(true, true);
    }

    /** Save the document in this thread.
    * @param parse true if the parser should be started, otherwise false
    * @exception IOException on I/O error
    */
    protected void saveDocumentIfNecessary(boolean parse) throws IOException {
        saveDocument(parse, false);
    }

    /** Save the document in this thread.
    * @param parse true if the parser should be started, otherwise false
    * @param forceSave if true save always, otherwise only when is modified
    * @exception IOException on I/O error
    */
    private void saveDocument(boolean parse, boolean forceSave) throws IOException {
        if (forceSave || isModified()) {

            super.saveDocument();
            documentSaved = true;
            if (parse) {
                JavaDataObject jdo = (JavaDataObject) entry.getDataObject();
                jdo.getSourceElementImpl().afterSaveParse();
            }
        }
    }

    // ==================== SourceCookie.Editor methods =================

    /** Returns a source element describing the hierarchy of the source.
    * @return the element
    */
    public SourceElement getSource() {
        JavaDataObject jdo = (JavaDataObject) entry.getDataObject();
        return jdo.sourceElement;
    }

    /** Translate a source element to text.
    *
    * @param element an element from the source hierarchy
    * @return a text element
    */
    public javax.swing.text.Element sourceToText(org.openide.src.Element element) {
        ElementImpl impl = (ElementImpl) element.getCookie(ElementImpl.class);
        if (impl == null)
            throw new IllegalArgumentException();

        return impl.getTextElement();
    }

    /** Translate a text element to a source element, if it is possible to do so.
    *
    * @param element a text element
    * @return the element from the source hierarchy
    * @exception NoSuchElementException if the text element doesn't match
    *  any element from the source hierarchy
    */
    public org.openide.src.Element textToSource(javax.swing.text.Element element) throws NoSuchElementException {
        if (element instanceof TextElement) {
            TextElement text = (TextElement) element;
            org.openide.src.Element srcElement = text.element.element;
            if (srcElement != null) {
                return srcElement;
            }
        }
        throw new NoSuchElementException();
    }

    /** Find the element at the specified offset in the document.
    * @param offset The position of the element
    * @return the element at the position.
    */
    public org.openide.src.Element findElement(int offset) {
        JavaDataObject jdo = (JavaDataObject) getJavaEntry().getDataObject();
        return jdo.getSourceElementImpl().findElement(offset);
    }

    // ==================== Guarded sections public methods =================

    /** Create new simple guarded section at a specified place.
    * @param previous section to create the new one after
    * @param name the name of the new section
    * @exception IllegalArgumentException if the name is already in use
    * @exception BadLocationException if it is not possible to create a
    *            new guarded section here
    */
    public SimpleSection createSimpleSectionAfter(final GuardedSection previous,
            final String name)
    throws IllegalArgumentException, BadLocationException {
        StyledDocument loadedDoc = null;
        try {
            loadedDoc = openDocument();
        }
        catch (IOException e) {
            throw new IllegalArgumentException();
        }

        final StyledDocument doc = loadedDoc;
        final SimpleSection[] sect = new SimpleSection[] { null };

        Util.ExceptionRunnable run = new Util.ExceptionRunnable() {
                                         public void run() throws Exception {
                                             PositionBounds bounds;
                                             if (previous instanceof SimpleSection)
                                                 bounds = ((SimpleSection) previous).bounds;
                                             else
                                                 bounds = ((InteriorSection) previous).bottom;
                                             int where = bounds.getEnd().getOffset();
                                             doc.insertString(where, "\n \n", null); // NOI18N
                                             sect[0] = new SimpleSection(name,
                                                                         createBounds(where + 1, where + 3, false)
                                                                        );
                                             sections.put(sect[0].getName(), sect[0]);
                                             sect[0].markGuarded(doc);
                                         }
                                     };
        try {
            Util.runAtomic(doc, run);
        }
        catch (Exception e) {
            if (e instanceof BadLocationException)
                throw (BadLocationException) e;
            else
                throw new IllegalArgumentException();
        }
        return sect[0];
    }

    /** Create new interior guarded section at a specified place.
    * @param previous section to create the new one after
    * @param name the name of the new section
    * @exception IllegalArgumentException if the name is already in use
    * @exception BadLocationException if it is not possible to create a
    *            new guarded section here
    */
    public InteriorSection createInteriorSectionAfter(final GuardedSection previous,
            final String name)
    throws IllegalArgumentException, BadLocationException {
        StyledDocument loadedDoc = null;
        try {
            loadedDoc = openDocument();
        }
        catch (IOException e) {
            throw new IllegalArgumentException();
        }

        if ((previous == null) || (!previous.valid))
            throw new IllegalArgumentException();

        final StyledDocument doc = loadedDoc;
        final InteriorSection[] sect = new InteriorSection[] { null };

        Util.ExceptionRunnable run = new Util.ExceptionRunnable() {
                                         public void run() throws Exception {
                                             PositionBounds bounds;
                                             if (previous instanceof SimpleSection)
                                                 bounds = ((SimpleSection) previous).bounds;
                                             else
                                                 bounds = ((InteriorSection) previous).bottom;
                                             int where = bounds.getEnd().getOffset();
                                             doc.insertString(where, "\n \n \n \n", null); // NOI18N
                                             sect[0] = new InteriorSection(name,
                                                                           createBounds(where + 1, where + 3, false),
                                                                           createBounds(where + 3, where + 5, true),
                                                                           createBounds(where + 5, where + 7, false)
                                                                          );
                                             sections.put(sect[0].getName(), sect[0]);
                                             sect[0].markGuarded(doc);
                                         }
                                     };
        try {
            Util.runAtomic(doc, run);
        }
        catch (Exception e) {
            if (e instanceof BadLocationException)
                throw (BadLocationException) e;
            else
                throw new IllegalArgumentException();
        }
        return sect[0];
    }

    /** Try to find the simple section of the given name.
    * @param name the name of the requested section
    * @return the found guarded section or <code>null</code> if there is no section
    *         of the given name
    */
    public SimpleSection findSimpleSection(String name) {
        GuardedSection s = findSection(name);
        return (s instanceof SimpleSection) ? (SimpleSection) s : null;
    }

    /** Try to find the interior section of the given name.
    * @param name the name of the looked-for section
    * @return the found guarded section or <code>null</code> if there is no section
    *         of the given name
    */
    public InteriorSection findInteriorSection(String name) {
        GuardedSection s = findSection(name);
        return (s instanceof InteriorSection) ? (InteriorSection) s : null;
    }

    /** Try to find the section of the given name.
    * @param name the name of the looked-for section
    * @return the found guarded section or <code>null</code> if there is no section
    *         of the given name
    */
    public GuardedSection findSection(String name) {
        try {
            StyledDocument doc = openDocument ();
            synchronized (this) {
                if (sections != null)
                    return (GuardedSection) sections.get(name);
            }
        }
        catch (IOException e) {
        }
        return null;
    }

    /** Get all sections.
    * @return an iterator of {@link JavaEditor.GuardedSection}s
    */
    public Iterator getGuardedSections() {
        try {
            StyledDocument doc = openDocument ();
            synchronized (this) {
                if (sections != null)
                    return ((HashMap)sections.clone()).values().iterator();
            }
        }
        catch (IOException e) {
        }
        return Collections.EMPTY_SET.iterator();
    }

    /** Get all section names.
    * @return an iterator of {@link String}s
    */
    public Iterator getGuardedSectionNames() {
        try {
            StyledDocument doc = openDocument ();
            synchronized (this) {
                if (sections != null)
                    return ((HashMap)sections.clone()).keySet().iterator();
            }
        }
        catch (IOException e) {
        }
        return Collections.EMPTY_SET.iterator();
    }

    // ==================== Misc not-public methods ========================
    
    PositionRef findUnguarded(PositionRef fromWhere, boolean allowHoles, boolean before) {
	Iterator it = getGuardedSections();
	
	while (it.hasNext()) {
	    GuardedSection sect = (GuardedSection)it.next();
	    if (sect.contains(fromWhere, allowHoles)) {
		if (before) {
		    return sect.getPositionBefore();
		} else {
		    return sect.getPositionAfter();
		}
	    }
	}
	return fromWhere;
    }
    
    /* A method to create a new component. Overridden in subclasses.
    * @return the {@link Editor} for this support
    */
    protected CloneableTopComponent createCloneableTopComponent () {
        // initializes the document if not initialized
        prepareDocument ();

        return createJavaEditorComponent();
    }

    /** Method for creation of the java editor component
    * - accessible from the innerclass.
    */
    JavaEditorComponent createJavaEditorComponent() {
        final DataObject obj = findDataObject ();
        JavaEditorComponent editor = new JavaEditorComponent(obj);

        componentsCreated = true;
        // dock into editor mode if possible
        Workspace current = TopManager.getDefault().getWindowManager().getCurrentWorkspace();
        Mode editorMode = current.findMode(EDITOR_MODE);
        if (editorMode != null)
            editorMode.dockInto(editor);
        return editor;
    }

    /** only accessibility method */
    MultiDataObject.Entry getJavaEntry() {
        return entry;
    }

    /** Set all sections as invalid. It is called from closeLast method
    * of the JavaEditorComponent.
    */
    synchronized void clearSections() {
        if (sections != null) {
            Iterator it = ((HashMap)sections.clone()).values().iterator();
            while (it.hasNext()) {
                GuardedSection sect = (GuardedSection) it.next();
                sect.valid = false;
            }
            sections = null;
        }
    }

    /** The real component of the Java editor.
    * Subclasses should not attempt to work with this;
    * if they require special editing support, separate windows
    * should be created by overriding (e.g.) {@link EditorSupport#open}.
    */
    public static class JavaEditorComponent extends EditorSupport.Editor {
        /** Default delay between cursor movement and updating selected element nodes. */
        static final int SELECTED_NODES_DELAY = 1000;

        /** Timer which countdowns the "update selected element node" time. */ // NOI18N
        Timer timerSelNodes;

        /** The support, subclass of EditorSupport */
        JavaEditor support;

        /** Listener on caret movements */
        CaretListener caretListener;

        Parsing.Listener parsingListener;

        /** The last caret offset position. */
        int lastCaretOffset = -1;

        static final long serialVersionUID =6223349196427270209L;

        /** Only for externalization */
        public JavaEditorComponent () {
            super();
        }

        /** Creates new editor */
        public JavaEditorComponent (DataObject obj) {
            super(obj);
            initialize();
        }

        /** Selects element at the given position. */
        void selectElementsAtOffset(final int offset) {
            final SourceElementImpl source = ((JavaDataObject) support.getJavaEntry().getDataObject()).getSourceElementImpl();

            SourceElementImpl.PARSING_RP.post(new Runnable() {
                                                  public void run() {
                                                      org.openide.src.Element element = support.findElement(offset);
                                                      org.openide.src.nodes.ElementNodeFactory factory = JavaDataObject.getExplorerFactory();
                                                      Node n = null;

                                                      if (element instanceof org.openide.src.MethodElement) {
                                                          n = factory.createMethodNode((org.openide.src.MethodElement)element);
                                                      }
                                                      else if (element instanceof org.openide.src.ClassElement) {
                                                          n = factory.createClassNode((org.openide.src.ClassElement)element);
                                                      }
                                                      else if (element instanceof org.openide.src.ConstructorElement) {
                                                          n = factory.createConstructorNode((org.openide.src.ConstructorElement)element);
                                                      }
                                                      else if (element instanceof org.openide.src.FieldElement) {
                                                          n = factory.createFieldNode((org.openide.src.FieldElement)element);
                                                      }
                                                      else if (element instanceof org.openide.src.InitializerElement) {
                                                          n = factory.createInitializerNode((org.openide.src.InitializerElement)element);
                                                      }
                                                      else if (element instanceof org.openide.src.SourceElement) {
                                                          n = support.getJavaEntry().getDataObject().getNodeDelegate();
                                                      }
                                                      setActivatedNodes((n != null) ? new Node[] { n } : new Node[] {} );
                                                  }
                                              }
                                             );
        }

        protected void notifyParsingDone() {
            if (lastCaretOffset != -1) {
                selectElementsAtOffset(lastCaretOffset);
            }
        }

        /** Obtain a support for this component */
        private void initialize () {
	    // This local is to keep javac 1.2 happy.
	    final DataObject myJavaObject = obj;    
            support = (JavaEditor)obj.getCookie(JavaEditor.class);

            timerSelNodes = new Timer(0, new java.awt.event.ActionListener() {
                                          public void actionPerformed(java.awt.event.ActionEvent e) {
                                              selectElementsAtOffset(lastCaretOffset);
                                          }
                                      });
            timerSelNodes.setInitialDelay(SELECTED_NODES_DELAY);
            timerSelNodes.setRepeats(false);

            caretListener = new CaretListener() {
                                public void caretUpdate(CaretEvent e) {
                                    support.restartTimer(true);
                                    restartTimerSelNodes(e.getDot());
                                }
                            };

            parsingListener = new Parsing.Listener() {
                                  public void objectParsed(Parsing.Event info) {
                                      if (info.getJavaDataObject() == myJavaObject) {
                                          notifyParsingDone();
                                      }
                                  }
                              };
        }

        /** Restart the timer which updates the selected nodes after the specified delay from
        * last caret movement.
        */
        void restartTimerSelNodes(int pos) {
            timerSelNodes.setInitialDelay(SELECTED_NODES_DELAY);
            timerSelNodes.restart();
            lastCaretOffset = pos;
        }

        /** Returns Editor pane for private use.
        * @return Editor pane for private use.
        */
        private JEditorPane getEditorPane () {
            return pane;
        }

        /* Is called from the clone method to create new component from this one.
        * This implementation only clones the object by calling super.clone method.
        * @return the copy of this object
        */
        protected CloneableTopComponent createClonedObject () {
            return support.createJavaEditorComponent();
        }

        /* This method is called when parent window of this component has focus,
        * and this component is preferred one in it. This implementation adds 
        * performer to the ToggleBreakpointAction.
        */
        protected void componentActivated () {
            try {
                final Debugger debugger = TopManager.getDefault ().getDebugger ();
                ((ToggleBreakpointAction) SystemAction.get (ToggleBreakpointAction.class)).
                setActionPerformer (new ActionPerformer () {
                                        public void performAction (SystemAction a) {
                                            int l = NbDocument.findLineNumber (
                                                        support.getDocument (),
                                                        getEditorPane ().getCaret ().getDot ()
                                                    );
                                            Line ll = support.getLineSet ().getCurrent (l);
                                            if (ll.isBreakpoint ()) {
                                                Breakpoint b = debugger.findBreakpoint (ll);
                                                if (b == null) ll.setBreakpoint (false);
                                                else b.remove ();
                                            } else {
                                                ll.setBreakpoint (true);
                                                debugger.createBreakpoint (ll);
                                            }
                                        }
                                    });
            } catch (DebuggerNotFoundException e) {
            }
            getEditorPane().addCaretListener(caretListener);
            Parsing.addParsingListener(parsingListener);
            super.componentActivated ();
        }

        /*
        * This method is called when parent window of this component losts focus,
        * or when this component losts preferrence in the parent window. This 
        * implementation removes performer from the ToggleBreakpointAction.
        */
        protected void componentDeactivated () {
            ((ToggleBreakpointAction) SystemAction.get (ToggleBreakpointAction.class)).
            setActionPerformer (null);
            getEditorPane().removeCaretListener(caretListener);
            Parsing.removeParsingListener(parsingListener);
            super.componentDeactivated ();
        }

        /* When closing last view, also close the document.
         * @return <code>true</code> if close succeeded
        */
        protected boolean closeLast () {
            if (!super.closeLast())
                return false;
			support.componentsCreated = false;
            return true;
        }

        /** Deserialize this top component.
        * @param in the stream to deserialize from
        */
        public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException {
            super.readExternal(in);
            initialize();
        }

    } // end of JavaEditorComponent inner class

    /** Takes the section descriptors from the GuardedReader and
    * fills the table 'sections', also marks as guarded all sections
    * in the given document.
    * @param is Where to take the guarded section descriptions.
    * @param doc Where to mark guarded.
    */
    private void fillSections(GuardedReader is, StyledDocument doc) {
        JavaEditor.SectionDesc descBegin = null;

        Iterator it = is.list.iterator();
        while (it.hasNext()) {
            SectionDesc descCurrent = (SectionDesc) it.next();
            GuardedSection sect = null;
            switch (descCurrent.type) {
            case T_LINE:
                sect = new SimpleSection(descCurrent.name,
                                         createBounds(descCurrent.begin,
                                                      descCurrent.end, false));
                break;

            case T_BEGIN:
            case T_HEADER:
            case T_FIRST:
                descBegin = descCurrent;
                break;

            case T_HEADEREND:
                if ((descBegin != null) &&
                        ((descBegin.type == T_HEADER) || (descBegin.type == T_FIRST)) &&
                        (descCurrent.name.equals(descBegin.name))
                   ) {
                    descBegin.end = descCurrent.end;
                }
                else {
                    //SYNTAX ERROR - ignore it.
                    descBegin = null;
                }
                break;

            case T_END:
            case T_LAST:
                if ((descBegin != null) && (descBegin.name.equals(descCurrent.name))) {
                    if ((descBegin.type == T_BEGIN) && (descCurrent.type == T_END)) {
                        // simple section
                        sect = new SimpleSection(descCurrent.name,
                                                 createBounds(descBegin.begin,
                                                              descCurrent.end, false));
                        break;
                    }
                    if (((descBegin.type == T_FIRST) && (descCurrent.type == T_LAST)) ||
                            ((descBegin.type == T_HEADER) && (descCurrent.type == T_END))) {
                        // interior section
                        sect = new InteriorSection(descCurrent.name,
                                                   createBounds(descBegin.begin, descBegin.end, false),
                                                   createBounds(descBegin.end, descCurrent.begin, true),
                                                   createBounds(descCurrent.begin, descCurrent.end, false)
                                                  );
                        break;
                    }
                }
                //SYNTAX ERROR - ignore it.
                descBegin = null;
                break;
            }

            if (sect != null) {
                sections.put(sect.getName(), sect);
                descBegin = null;
                sect.markGuarded(doc);
            }
        }
    }

    /** Simple creates the bounds for the two offsets. */
    private PositionBounds createBounds(int begin, int end, boolean dir) {
        if (dir) {
            return new PositionBounds(
                       createPositionRef(begin, Position.Bias.Forward),
                       createPositionRef(end, Position.Bias.Backward)
                   );
        }
        else {
            return new PositionBounds(
                       createPositionRef(begin, Position.Bias.Backward),
                       createPositionRef(end, Position.Bias.Forward)
                   );
        }
    }

    // ==================== Public inner classes ========================

    /** Represents one guarded section.
    */
    public abstract class GuardedSection extends Object {
        /** Name of the section. */
        String name;

        /** If the section is valid or if it was removed. */
        boolean valid;

        /** Get the name of the section.
        * @return the name
        */
        public String getName() {
            return name;
        }

        /** Creates new section.
        * @param name Name of the new section.
        */
        GuardedSection(String name) {
            this.name = name;
            valid = true;
        }

        /** Set the name of the section.
        * @param name the new name
        * @exception PropertyVetoException if the new name is already in use
        */
        public void setName(String name) throws PropertyVetoException {
            if (!this.name.equals(name)) {
                synchronized (JavaEditor.this) {
                    if (valid) {
                        if (sections.get(name) != null)
                            throw new PropertyVetoException("", new PropertyChangeEvent(this, "name", this.name, name)); // NOI18N
                        sections.remove(this.name);
                        this.name = name;
                        sections.put(name, this);
                    }
                }
            }
        }

        /** Deletes the text of the section and
        * removes it from the table. The section will then be invalid
        * and it will be impossible to use its methods.
        *
        * @return <code>true</code> if the operation was successful, otherwise <code>false</code>
        */
        public boolean deleteSection() {
            synchronized (JavaEditor.this) {
                if (valid) {
                    try {
                        sections.remove(name);
                        // get document should always return the document, when section
                        // is deleted, because it is still valid (and valid is only
                        // when document is loaded.
                        unmarkGuarded(getDocument());
                        deleteText();
                        valid = false;
                        return true;
                    }
                    catch (BadLocationException e) {
                    }
                    catch (IOException e) {
                    }
                }
                return false;
            }
        }

        /** Delete one new-line character before the specified offset.
        * This method is used when guarded blocks are deleted. When new guarded block is created,
        * there is added one more new-line before it, so this method remove this char in the end of
        * guarded block life cycle.
        * It works only when there is "\n" char before the offset and no problem occured (IOException...)
        * @param offset The begin of removed guarded block.
        */
        void deleteNewLineBeforeBlock(int offset) {
            if (offset > 1) {
                try {
                    PositionBounds b = createBounds(offset - 1, offset, true);
                    String s = b.getText();
                    if (s.equals("\n")) { // NOI18N
                        b.setText(""); // NOI18N
                    }
                }
                catch (IOException e) {
                }
                catch (BadLocationException e) {
                }
            }
        }

        /** Opens the editor and set cursor to this guarded section.
        */
        public void openAt() {
            JavaEditor.this.openAt(getBegin());
        }

        /** Set the text contained in this section.
        * Newlines are automatically added to all text segments handled,
        * unless there was already one.
        * All guarded blocks must consist of entire lines.
        * This applies to the contents of specific guard types as well.
        * @param bounds the bounds indicating where the text should be set
        * @param text the new text
        * @param minLen If true the text has to have length more than 2 chars.
        * @return <code>true</code> if the operation was successful, otherwise <code>false</code>
        */
        protected boolean setText(PositionBounds bounds, String text, boolean minLen) {
            if (!valid)
                return false;

            // modify the text - has to end with new line and the length
            // has to be more then 2 characters
            if (minLen) {
                if (text.length() == 0)
                    text = " \n"; // NOI18N
                else if (text.length() == 1)
                    text = text.equals("\n") ? " \n" : text + "\n"; // NOI18N
            }

            if (!text.endsWith("\n")) // NOI18N
                text = text + "\n"; // NOI18N

            try {
                bounds.setText(text);
                return true;
            }
            catch (BadLocationException e) {
            }
            catch (IOException e) {
            }
            return false;
        }

        /** Marks or unmarks the section as guarded.
        * @param doc The styled document where this section placed in.
        * @param bounds The rangeof text which should be marked or unmarked.
        * @param mark true means mark, false unmark.
        */
        void markGuarded(StyledDocument doc, PositionBounds bounds, boolean mark) {
            int begin = bounds.getBegin().getOffset();
            int end = bounds.getEnd().getOffset();
            if (mark)
                NbDocument.markGuarded(doc, begin, end - begin);
            else
                NbDocument.unmarkGuarded(doc, begin, end - begin);
        }

        /** Marks the section as guarded.
        * @param doc The styled document where this section placed in.
        */
        abstract void markGuarded(StyledDocument doc);

        /** Unmarks the section as guarded.
        * @param doc The styled document where this section placed in.
        */
        abstract void unmarkGuarded(StyledDocument doc);

        /** Deletes the text in the section.
        * @exception BadLocationException
        * @exception IOException
        */
        abstract void deleteText() throws BadLocationException, IOException;

        /** Gets the begin of section. To this position is set the cursor
        * when section is open in the editor.
        */
        public abstract PositionRef getBegin();
	
	/** Returns position before the whole guarded block that is safe for insertions.
	*/
	public abstract PositionRef getPositionBefore();
	
	/** Returns a position after the whole guarded block that is safe for insertions.
	*/	
	public abstract PositionRef getPositionAfter();
	
	/** Assures that a position is not inside the guarded section. Complex guarded sections
	    that contain portions of editable text can return true if the tested position is
	    inside one of such portions provided that permitHoles is true.
	    
	    @param pos position in question
	    @param permitHoles if false, guarded section is taken as a monolithic block
		without any holes in it regadless of its complexity.
	*/
	public abstract boolean contains(PositionRef pos, boolean permitHoles);

        /** Gets the text contained in the section.
        * @return The text contained in the section.
        */
        public abstract String getText();

    }

    /** Represents a simple guarded section.
    * It consists of one contiguous block.
    */
    public final class SimpleSection extends GuardedSection {
        /** Text range of the guarded section. */
        PositionBounds bounds;

        /** Creates new section.
        * @param name Name of the new section.
        * @param bounds The range of the section.
        */
        SimpleSection(String name, PositionBounds bounds) {
            super(name);
            this.bounds = bounds;
        }

        /** Set the text of the section.
        * @param text the new text
        * @return <code>true</code> if the operation was successful, otherwise <code>false</code>
        * @see JavaEditor.GuardedSection#setText
        */
        public boolean setText(String text) {
            return setText(bounds, text, true);
        }

        /** Deletes the text in the section.
        * @exception BadLocationException
        * @exception IOException
        */
        void deleteText() throws BadLocationException, IOException {
            bounds.setText(""); // NOI18N
            deleteNewLineBeforeBlock(bounds.getBegin().getOffset());
        }

        /** Marks the section as guarded.
        * @param doc The styled document where this section placed in.
        */
        void markGuarded(StyledDocument doc) {
            markGuarded(doc, bounds, true);
        }

        /** Unmarks the section as guarded.
        * @param doc The styled document where this section placed in.
        */
        void unmarkGuarded(StyledDocument doc) {
            markGuarded(doc, bounds, false);
        }

        /** Gets the begin of section. To this position is set the cursor
        * when section is open in the editor.
        * @return the begin position of section.
        */
        public PositionRef getBegin() {
            return bounds.getBegin();
        }

        /** Gets the text contained in the section.
        * @return The text contained in the section.
        */
        public String getText() {
            StringBuffer buf = new StringBuffer();
            try {
                buf.append(bounds.getText());
            }
            catch (Exception e) {
            }
            return buf.toString();
        }
	
	public boolean contains(PositionRef pos, boolean allowHoles) {
	    return bounds.getBegin().getOffset() <= pos.getOffset() &&
		bounds.getEnd().getOffset() >= pos.getOffset();
	}
	
	public PositionRef getPositionBefore() {
	    return createPositionRef(bounds.getBegin().getOffset(), Position.Bias.Forward);
	}

	public PositionRef getPositionAfter() {
	    return createPositionRef(bounds.getEnd().getOffset() + 1, Position.Bias.Forward);
	}
        /*
        public String toString() {
          StringBuffer buf = new StringBuffer("SimpleSection:"+name);
          buf.append("\"");
          try {
            buf.append(bounds.getText());
          }
          catch (Exception e) {
            buf.append("EXCEPTION:");
            buf.append(e.getMessage());
          }
          buf.append("\"");
          return buf.toString();
    }*/
    }

    /** Represents an advanced guarded block.
    * It consists of three pieces: a header, body, and footer.
    * The header and footer are guarded but the body is not.
    */
    public final class InteriorSection extends GuardedSection {
        /** Text range of the header. */
        PositionBounds header;

        /** Text range of the header. */
        PositionBounds body;

        /** Text range of the bottom. */
        PositionBounds bottom;

        /** Creates new section.
        * @param name Name of the new section.
        * @param bounds The range of the section.
        */
        InteriorSection(String name, PositionBounds header, PositionBounds body, PositionBounds bottom) {
            super(name);
            this.header = header;
            this.body = body;
            this.bottom = bottom;
        }

        /** Set the text of the body.
        * @param text the new text
        * @return <code>true</code> if the operation was successful, otherwise <code>false</code>
        * @see JavaEditor.GuardedSection#setText
        */
        public boolean setBody(String text) {
            return setText(body, text, false);
        }

        /** Set the text of the header.
        * @param text the new text
        * @return <code>true</code> if the operation was successful, otherwise <code>false</code>
        * @see JavaEditor.GuardedSection#setText
        */
        public boolean setHeader(String text) {
            return setText(header, text, true);
        }

        /** Set the text of the bottom.
        * Note that the bottom of the section must have exactly one line.
        * So, all interior newline characters will be replaced by spaces.
        *
        * @param text the new text
        * @return <code>true</code> if the operation was successful, otherwise <code>false</code>
        * @see JavaEditor.GuardedSection#setText
        */
        public boolean setBottom(String text) {
            boolean endsWithEol = text.endsWith("\n"); // NOI18N
            int firstEol = text.indexOf('\n');
            int lastEol = text.lastIndexOf('\n');

            if ((firstEol != lastEol) || (endsWithEol && (firstEol != -1))) {
                if (endsWithEol) {
                    text = text.substring(0, text.length() - 1);
                }
                text = text.replace('\n', ' ');
            }
            return setText(bottom, text, true);
        }

        /** Gets the begin of section. To this position is set the cursor
        * when section is open in the editor.
        * @return the begin position of the body section - the place where
        *         is possible to edit.
        */
        public PositionRef getBegin() {
            return body.getBegin();
        }

	public PositionRef getPositionBefore() {
	    return createPositionRef(header.getBegin().getOffset(), Position.Bias.Forward);
	}

	public PositionRef getPositionAfter() {
	    return createPositionRef(bottom.getEnd().getOffset() + 1, Position.Bias.Forward);
	}

	public boolean contains(PositionRef pos, boolean allowHoles) {
	    if (!allowHoles) {
    		return header.getBegin().getOffset() <= pos.getOffset() &&
		    bottom.getEnd().getOffset() >= pos.getOffset();
	    } else {
		if (header.getBegin().getOffset() <= pos.getOffset() && 
		    header.getEnd().getOffset() >= pos.getOffset()) {
		    return true;
		}
		return bottom.getBegin().getOffset() <= pos.getOffset() &&
		    bottom.getEnd().getOffset() >= pos.getOffset();
	    }
	}

        /** Deletes the text in the section.
        * @exception BadLocationException
        * @exception IOException
        */
        void deleteText() throws BadLocationException, IOException {
            header.setText(""); // NOI18N
            body.setText(""); // NOI18N
            bottom.setText(""); // NOI18N
            deleteNewLineBeforeBlock(header.getBegin().getOffset());
        }

        /** Marks the section as guarded.
        * @param doc The styled document where this section placed in.
        */
        void markGuarded(StyledDocument doc) {
            markGuarded(doc, header, true);
            markGuarded(doc, bottom, true);
        }

        /** Unmarks the section as guarded.
        * @param doc The styled document where this section placed in.
        */
        void unmarkGuarded(StyledDocument doc) {
            markGuarded(doc, header, false);
            markGuarded(doc, bottom, false);
        }

        /** Gets the text contained in the section.
        * @return The text contained in the section.
        */
        public String getText() {
            StringBuffer buf = new StringBuffer();
            try {
                buf.append(header.getText());
                buf.append(body.getText());
                buf.append(bottom.getText());
            }
            catch (Exception e) {
            }
            return buf.toString();
        }

        /*
        public String toString() {
          StringBuffer buf = new StringBuffer("InteriorSection:"+name);
          try {
            buf.append("HEADER:\"");
            buf.append(header.getText());
            buf.append("\"");
            buf.append("BODY:\"");
            buf.append(body.getText());
            buf.append("\"");
            buf.append("BOTTOM:\"");
            buf.append(bottom.getText());
            buf.append("\"");
          }
          catch (Exception e) {
            buf.append("EXCEPTION:");
            buf.append(e.getMessage());
          }
          return buf.toString();
    }*/
    }

    // ==================== Private inner classes ===========================

    /** Comparator of the guarded sections. It compares the begin position
    * of the sections.
    */
    private static class GuardedPositionComparator implements Comparator {
        /** Compare two objects. Both have to be either SimpleSection
        * either InteriorSection instance.
        */
        public int compare(Object o1, Object o2) {
            return getOffset(o1) - getOffset(o2);
        }

        /** Computes the offset of the begin of the section. */
        private int getOffset(Object o) {
            if (o instanceof SimpleSection) {
                return ((SimpleSection)o).bounds.getBegin().getOffset();
            }
            else {
                return ((InteriorSection)o).header.getBegin().getOffset();
            }
        }
    }

    /** Class for holding information about the one special (guarded)
    * comment. It is created by GuardedReader and used by
    * JavaEditor to creating the guarded sections.
    */
    private static class SectionDesc {
        /** Type - one of T_XXX constant */
        int type;

        /** Name of the section comment */
        String name;

        /** offset of the begin */
        int begin;

        /** offset of the end */
        int end;

        /** Simple constructor */
        SectionDesc(int type) {
            this.type = type;
            name = null;
            begin = 0;
            end = 0;
        }
    }

    /** This stream is able to filter special guarded comments.
    * Holding this information is optional and depends on the construction
    * of this stream - the reason of this feature is that
    * GuardedReader is used also for parser (and it doesn't require
    * the storing the guarded block information - just filter the comments).
    */
    static class GuardedReader extends Reader {
        /** Encapsulated reader */
        BufferedReader reader;

        /** Buffer for reading - current line. */
        StringBuffer buf;

        /** The flag determining if this stream should store the guarded
        * block information (list of the SectionDesc).
        */
        boolean justFilter;

        /** The position at the current line. */
        int position;

        /** Offset counter */
        int counter;

        /** The list of the SectionsDesc. 
         * @associates SectionDesc*/
        LinkedList list;

        /** The count of types new line delimiters used in the file */
        int[] newLineTypes;

        /** Creates new stream.
        * @param is encapsulated input stream.
        * @param justFilter The flag determining if this stream should
        *        store the guarded block information. True means just filter,
        *        false means store the information.
        */
        public GuardedReader(InputStream is, boolean justFilter) {
            reader = new BufferedReader(new InputStreamReader(is));
            this.justFilter = justFilter;
            position = -1;
            counter = 0;
            list = new LinkedList();
            buf = new StringBuffer(120);
            newLineTypes = new int[] { 0, 0, 0 };
        }

        /** Reads and parse the next line. Fill the line field.
        * We cannot use readline method from the BufferedReader, because
        * at the end of the file is impossible to recognize if files
        * ends with the new-line or not.
        */
        private void nextLine() throws IOException {
            if (buf == null)
                return;

            buf.setLength(0);
            boolean addNewLine = false;
            boolean done = false;

            while (!done) {
                int c = reader.read();
                switch (c) {
                case -1:
                    if (buf.length() == 0) {
                        buf = null;
                        return;
                    }
                    done = true;
                    break;

                case (int)'\n':
                case (int)'\r':
                    addNewLine = true;
                    done = true;
                    if (c == (int)'\r') {
                        reader.mark(1);
                        int c2 = reader.read();
                        if (c2 != (int)'\n') {
                            newLineTypes[NEW_LINE_R]++;
                            reader.reset();
                        }
                        else {
                            newLineTypes[NEW_LINE_RN]++;
                        }
                    }
                    else {
                        newLineTypes[NEW_LINE_N]++;
                    }
                    break;

                default:
                    buf.append((char)c);
                    break;
                }
            }

            int index;
            SectionDesc desc = null;
            String lineStr = buf.toString();

            for (int i = 0; i < SECTION_MAGICS.length; i++) {
                if ((index = lineStr.indexOf(SECTION_MAGICS[i])) != -1) {
                    if (!justFilter) {
                        desc = new SectionDesc(i);
                        desc.name = buf.substring(index + SECTION_MAGICS[i].length());
                        desc.begin = counter;
                        desc.end = counter + index + (addNewLine ? 1 : 0);
                        list.add(desc);
                    }
                    buf.setLength(index);
                    break;
                }
            }
            if (addNewLine)
                buf.append('\n');

            position = 0;
        }

        /** Read the array of chars */
        public int read(char[] cbuf, int off, int len) throws IOException {
            for (int i = 0; i < len; i++) {
                int c = readOneChar();
                if (c == -1) {
                    return (i > 0) ? i : -1;
                }
                cbuf[off + i] = (char) c;
            }
            return len;
        }

        /** Reads one character.
        * @return next char or -1 if the end of file was reached.
        * @exception IOException if any problem occured.
        */
        int readOneChar() throws IOException {
            if (buf == null)
                return -1;

            if (buf.length() == 0)
                nextLine();

            int c = -1;
            if (buf != null) {
                c = (int) buf.charAt(position);
                position++;
                counter++;
                if (buf.length() == position)
                    buf.setLength(0);
            }
            return c;
        }

        /** @return most frequently type of new line delimiter */
        byte getNewLineType() {
            if (newLineTypes[0] > newLineTypes[1]) {
                return (newLineTypes[0] > newLineTypes[2]) ? (byte) 0 : 2;
            }
            else {
                return (newLineTypes[1] > newLineTypes[2]) ? (byte) 1 : 2;
            }
        }

        /** Close underlayed writer. */
        public void close() throws IOException {
            reader.close();
        }
    }

    /** This stream is able to insert special guarded comments.
    */
    static class GuardedWriter extends Writer {
        /** Encapsulated writer. */
        BufferedWriter writer;

        /** From this iterator is possible to obtain all section
        * descriptions during writing the document.
        */
        Iterator sections;

        /** Current section from the previous iterator. For filling this
        * field is used method nextSection.
        */
        SectionDesc current;

        /** Current offset in the original document (NOT in the encapsulated
        * output stream.
        */
        int offsetCounter;

        /** This flag is used during writing. It is complicated to explain. */
        boolean wasNewLine;

        /** Creates new GuardedWriter.
        * @param os Encapsulated output stream.
        * @param list The list of the guarded sections.
        */
        public GuardedWriter(OutputStream os, ArrayList list) {
            writer = new BufferedWriter(new OutputStreamWriter(os));
            offsetCounter = 0;
            sections = prepareSections(list);
            nextSection();
            wasNewLine = false;
        }

        /** Writes chars to underlayed writer */
        public void write(char[] cbuf, int off, int len) throws IOException {
            for (int i = 0; i < len; i++) {
                writeOneChar(cbuf[i + off]);
            }
        }

        /** Calls underlayed writer flush */
        public void close() throws IOException {
            writer.flush();
        }

        /** Calls underlayed writer flush */
        public void flush() throws IOException {
            writer.flush();
        }

        /** This method prepares the iterator of the SectionDesc classes
        * @param list The list of the GuardedSection classes.
        * @return iterator of the SectionDesc
        */
        private Iterator prepareSections(ArrayList list) {
            LinkedList dest = new LinkedList();
            Collections.sort(list, new GuardedPositionComparator());

            Iterator it = list.iterator();
            while (it.hasNext()) {
                GuardedSection o = (GuardedSection) it.next();
                if (o instanceof SimpleSection) {
                    SectionDesc desc = new SectionDesc(T_LINE);
                    desc.name = o.name;
                    desc.begin = ((SimpleSection)o).bounds.getBegin().getOffset();
                    desc.end = ((SimpleSection)o).bounds.getEnd().getOffset();
                    dest.add(desc);
                }
                else {
                    SectionDesc desc = new SectionDesc(T_HEADER);
                    desc.begin = (((InteriorSection)o).header).getBegin().getOffset();
                    desc.end = (((InteriorSection)o).header).getEnd().getOffset();
                    desc.name = o.name;
                    dest.add(desc);

                    desc = new SectionDesc(T_END);
                    desc.begin = (((InteriorSection)o).bottom).getBegin().getOffset();
                    desc.end = (((InteriorSection)o).bottom).getEnd().getOffset();
                    desc.name = o.name;
                    dest.add(desc);
                }
            }
            return dest.iterator();
        }

        /** Write one character. If there is a suitable place,
        * some special comments are written to the underlaying stream.
        * @param b char to write.
        */
        void writeOneChar(int b) throws IOException {
            if (b == '\r')
                return;

            if (current != null) {
                if (offsetCounter == current.begin) {
                    wasNewLine = false;
                }
                if ((b == '\n') && (current.begin < offsetCounter)) {
                    switch (current.type) {
                    case T_LINE:
                        if (!wasNewLine) {
                            if (offsetCounter + 1 >= current.end) {
                                writeMagic(T_LINE, current.name);
                                nextSection();
                            }
                            else {
                                writeMagic(T_BEGIN, current.name);
                                wasNewLine = true;
                            }
                        }
                        else {
                            if (offsetCounter + 1 >= current.end) {
                                writeMagic(T_END, current.name);
                                nextSection();
                            }
                        }
                        break;

                    case T_HEADER:
                        if (!wasNewLine) {
                            if (offsetCounter + 1 >= current.end) {
                                writeMagic(T_FIRST, current.name);
                                nextSection();
                            }
                            else {
                                writeMagic(T_FIRST, current.name);
                                wasNewLine = true;
                            }
                        }
                        else {
                            if (offsetCounter + 1 >= current.end) {
                                writeMagic(T_HEADEREND, current.name);
                                nextSection();
                            }
                        }
                        break;

                    case T_END:
                        writeMagic(T_LAST, current.name);
                        nextSection();
                        break;
                    }
                }
            }

            writer.write(b);
            offsetCounter++;
        }

        /** Try to get next sectionDesc from the 'sections'
        * If there is no more section the 'current' will be set to null.
        */
        private void nextSection() {
            current = (SectionDesc) (sections.hasNext() ? sections.next() : null);
        }

        /** Writes the magic to the underlaying stream.
        * @param type The type of the magic section - T_XXX constant.
        * @param name name of the section.
        */
        private void writeMagic(int type, String name) throws IOException {
            writer.write(SECTION_MAGICS[type], 0, SECTION_MAGICS[type].length());
            writer.write(name, 0, name.length());
        }
    }

    /** This stream is used for changing the new line delimiters.
    * It replaces the '\n' by '\n', '\r' or "\r\n"
    */
    private static class NewLineOutputStream extends OutputStream {
        /** Underlaying stream. */
        OutputStream stream;

        /** The type of new line delimiter */
        byte newLineType;

        /** Creates new stream.
        * @param stream Underlaying stream
        * @param newLineType The type of new line delimiter
        */
        public NewLineOutputStream(OutputStream stream, byte newLineType) {
            this.stream = stream;
            this.newLineType = newLineType;
        }

        /** Write one character.
        * @param b char to write.
        */
        public void write(int b) throws IOException {
            if (b == '\n') {
                switch (newLineType) {
                case NEW_LINE_R:
                    stream.write('\r');
                    break;
                case NEW_LINE_RN:
                    stream.write('\r');
                case NEW_LINE_N:
                    stream.write('\n');
                    break;
                }
            }
            else {
                stream.write(b);
            }
        }
    }
}

/*
 * Log
 *  63   Gandalf-post-FCS1.53.2.8    4/18/00  Svatopluk Dedic Disambiguated outer 
 *       class (obj) member access
 *  62   Gandalf-post-FCS1.53.2.7    4/18/00  Svatopluk Dedic Document will not be 
 *       closed; external entity is responsible for this
 *  61   Gandalf-post-FCS1.53.2.6    4/17/00  Svatopluk Dedic Component listens on 
 *       parser only if it is activated
 *  60   Gandalf-post-FCS1.53.2.5    4/17/00  Svatopluk Dedic Sources are saved/closed
 *       soon after programmatic changes; bulk cross-package fixed.
 *  59   Gandalf-post-FCS1.53.2.4    4/14/00  Svatopluk Dedic Fixed re-parsing after 
 *       save, reload
 *  58   Gandalf-post-FCS1.53.2.3    4/4/00   Svatopluk Dedic afterParse called after 
 *       the document is saved (low priority parsing)
 *  57   Gandalf-post-FCS1.53.2.2    3/13/00  Svatopluk Dedic Selection of element's 
 *       node doesn't block
 *  56   Gandalf-post-FCS1.53.2.1    2/24/00  Svatopluk Dedic After autoparse, a node 
 *       corresponding to editor caret's position will be selected
 *  55   Gandalf-post-FCS1.53.2.0    2/24/00  Ian Formanek    Post FCS changes
 *  54   src-jtulach1.53        1/15/00  Petr Hamernik   some improvement 
 *       (related to #4348)
 *  53   src-jtulach1.52        1/13/00  Petr Hamernik   i18n -(2nd round) - 
 *       script bug fixed.
 *  52   src-jtulach1.51        1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  51   src-jtulach1.50        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  50   src-jtulach1.49        10/8/99  Petr Hamernik   reloadDocument 
 *       implemented
 *  49   src-jtulach1.48        10/6/99  Petr Hamernik   Streams and Readers 
 *       correction.
 *  48   src-jtulach1.47        10/6/99  Petr Hamernik   1) closing streams 
 *       removed 2) fixed bug #4284
 *  47   src-jtulach1.46        10/1/99  Petr Hamernik   fixed bugs #3892, #4120 
 *       - deleting guarded blocks
 *  46   src-jtulach1.45        9/24/99  Petr Hamernik   closing input stream bug
 *       fixed
 *  45   src-jtulach1.44        8/27/99  Petr Hamernik   optimization
 *  44   src-jtulach1.43        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  43   src-jtulach1.42        7/30/99  Jaroslav Tulach 
 *  42   src-jtulach1.41        7/27/99  Petr Hrebejk    getText method added to 
 *       GuardedSection and ancestors
 *  41   src-jtulach1.40        7/21/99  Petr Hamernik   EDITOR_MODE constant 
 *       usage
 *  40   src-jtulach1.39        7/19/99  Petr Hamernik   debug print removed
 *  39   src-jtulach1.38        7/19/99  Petr Hamernik   CaretListener added
 *  38   src-jtulach1.37        7/13/99  Petr Hamernik   findElement doesn't 
 *       throw NoSuchElementException
 *  37   src-jtulach1.36        7/8/99   Petr Hamernik   changes reflecting 
 *       org.openide.src changes
 *  36   src-jtulach1.35        7/3/99   Petr Hamernik   SourceCookie.Editor - 
 *       1st version
 *  35   src-jtulach1.34        6/27/99  Ian Formanek    getBegin in 
 *       GuardedSection made public to allow FormEditor to use indentation 
 *       engine for code generation
 *  34   src-jtulach1.33        6/10/99  Ian Formanek    method 
 *       saveDocumentIfNecessary made protected to allow overriding in 
 *       FormEditor
 *  33   src-jtulach1.32        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  32   src-jtulach1.31        6/5/99   Petr Hamernik   parsing after open - 
 *       invoked later
 *  31   src-jtulach1.30        6/4/99   Petr Hamernik   parsing after open
 *  30   src-jtulach1.29        5/17/99  Petr Hamernik   canClose moved to 
 *       EditorSupport
 *  29   src-jtulach1.28        5/14/99  Petr Hamernik   deadlock prevention
 *  28   src-jtulach1.27        5/14/99  David Simonek   serizalization 
 *       modifications
 *  27   src-jtulach1.26        5/12/99  Petr Hamernik   notifyClose() added
 *  26   src-jtulach1.25        5/11/99  Petr Hamernik   creationInteriorSectionAfter
 *        implemented
 *  25   src-jtulach1.24        5/11/99  Petr Hamernik   saving doesn't modify 
 *       new line delimiters (CR | CRLF | LF)
 *  24   src-jtulach1.23        5/6/99   Jesse Glick     [JavaDoc]
 *  23   src-jtulach1.22        5/5/99   Petr Hamernik   find methods added, 
 *       synchronization improved
 *  22   src-jtulach1.21        4/23/99  Petr Hamernik   deadlock prevention
 *  21   src-jtulach1.20        4/15/99  Petr Hamernik   parser improvements
 *  20   src-jtulach1.19        4/9/99   Petr Hamernik   bugfix 1458
 *  19   src-jtulach1.18        4/9/99   Petr Hamernik   guarded blocks improved 
 *       - load and save
 *  18   src-jtulach1.17        4/7/99   Petr Hamernik   JavaEditorComponent.clone()
 *        rewriten
 *  17   src-jtulach1.16        4/2/99   Petr Hamernik   canClose
 *  16   src-jtulach1.15        4/2/99   Petr Hamernik   saving changed
 *  15   src-jtulach1.14        4/1/99   Petr Hamernik   
 *  14   src-jtulach1.13        3/29/99  Petr Hamernik   
 *  13   src-jtulach1.12        3/29/99  Petr Hamernik   
 *  12   src-jtulach1.11        3/23/99  Jan Jancura     Toggle breakpoint action
 *       added
 *  11   src-jtulach1.10        3/22/99  Jan Jancura     
 *  10   src-jtulach1.9         3/21/99  Petr Hamernik   
 *  9    src-jtulach1.8         3/20/99  Petr Hamernik   
 *  8    src-jtulach1.7         3/18/99  Petr Hamernik   
 *  7    src-jtulach1.6         3/17/99  Petr Hamernik   
 *  6    src-jtulach1.5         3/11/99  Petr Hamernik   
 *  5    src-jtulach1.4         3/10/99  Petr Hamernik   
 *  4    src-jtulach1.3         2/8/99   Petr Hamernik   
 *  3    src-jtulach1.2         2/4/99   Petr Hamernik   
 *  2    src-jtulach1.1         2/4/99   Petr Hamernik   
 *  1    src-jtulach1.0         2/1/99   Petr Hamernik   
 * $
 */
