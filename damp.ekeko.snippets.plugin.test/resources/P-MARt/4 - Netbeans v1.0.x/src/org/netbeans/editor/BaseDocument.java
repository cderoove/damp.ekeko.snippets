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

package org.netbeans.editor;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.ArrayList;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.Element;
import javax.swing.text.AttributeSet;
import javax.swing.text.AbstractDocument;
import javax.swing.text.StyleConstants;
import javax.swing.event.EventListenerList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CompoundEdit;

/**
* Document implementation
*
* @author Miloslav Metelka
* @version 1.00
*/

public class BaseDocument extends AbstractDocument implements SettingsChangeListener {

    /** Registry identification property */
    public static final String ID_PROP = "id"; // NOI18N

    /** Line separator property for reading files in */
    public static final String READ_LINE_SEPARATOR_PROP
    = DefaultEditorKit.EndOfLineStringProperty;

    /** Line separator property for reading files in */
    public static final String WRITE_LINE_SEPARATOR_PROP
    = "write-line-separator"; // NOI18N

    /** File name property */
    public static final String FILE_NAME_PROP = "file-name"; // NOI18N

    /** Wrap search mark property */
    public static final String WRAP_SEARCH_MARK_PROP = "wrap-search-mark"; // NOI18N

    /** Undo manager property. This can be used to implement undo
    * in a simple way. Default undo and redo actions try to get this
    * property and perform undo and redo through it.
    */
    public static final String UNDO_MANAGER_PROP = "undo-manager"; // NOI18N

    /** Kit class property. This can become useful for getting
    * the settings that logicaly belonging to the document.
    */
    public static final String KIT_CLASS_PROP = "kit-class"; // NOI18N

    /** String forward finder property */
    public static final String STRING_FINDER_PROP = "string-finder"; // NOI18N

    /** String backward finder property */
    public static final String STRING_BWD_FINDER_PROP = "string-bwd-finder"; // NOI18N

    /** Highlight search finder property. */
    public static final String BLOCKS_FINDER_PROP = "blocks-finder"; // NOI18N

    /** Maximum line width encountered during the initial read operation.
    * This is filled by Analyzer and used by UI to set the correct initial width
    * of the component.
    * Values: java.lang.Integer
    */
    public static final String LINE_LIMIT_PROP = "line-limit"; // NOI18N


    /** Size of the line batch. Line batch can be used at various places
    * especially when processing lines by syntax scanner.
    */
    public static final String LINE_BATCH_SIZE = "line-batch-size"; // NOI18N

    /** Line separator is marked by CR (Macintosh) */
    public static final String  LS_CR = "\r"; // NOI18N

    /** Line separator is marked by LF (Unix) */
    public static final String  LS_LF = "\n"; // NOI18N

    /** Line separator is marked by CR and LF (Windows) */
    public static final String  LS_CRLF = "\r\n"; // NOI18N

    // special signature for change event for layer addition
    static final char[] LAYER_ADDED = new char[0];

    /** Maximum of concurrent read threads (other will wait until
    * one of these will leave).
    */
    private static final int MAX_READ_THREADS = 10;

    /** Write lock without write lock */
    private static final String WRITE_LOCK_MISSING
    = "extWriteUnlock() without extWriteLock()"; // NOI18N

    private static final boolean debugEditor = Boolean.getBoolean("netbeans.debug.editor"); // NOI18N

    /** Document operations support class for this document. It presents
    * the base synchronization level for most of the operations.
    * Some of the operations are available through <tt>Utilities</tt> class.
    */
    DocOp op;

    /** How many times current writer requested writing */
    private int writeDeep;

    /** How many times atomic writer requested writing */
    private int atomicDepth;

    /* Was the document initialized by reading? */
    protected boolean inited;

    /* Was the document modified by doing inert/remove */
    protected boolean modified;

    /** Listener list */
    protected EventListenerList listenerList = new EventListenerList();

    /** Listener to changes in find support */
    PropertyChangeListener findSupportListener;

    /** Default element - lazily inited */
    protected BaseElement defaultRootElem;

    private SyntaxSupport syntaxSupport;

    /** Layer list for document level layers */
    private DrawLayerList drawLayerList = new DrawLayerList();

    /** Chain of document level bookmarks */
    private MarkChain bookmarkChain;

    /** Reset merging next created undoable edit to the last one. */
    protected boolean undoMergeReset;

    /** Kit class stored here */
    Class kitClass;

    /** Undo event for atomic events is fired after the successful
    * atomic operation is finished. The changes are stored in this variable
    * during the atomic operation. If the operation is broken, these edits
    * are used to restore previous state.
    */
    private CompoundEdit atomicEdits;

    private Acceptor identifierAcceptor;

    private Acceptor whitespaceAcceptor;

    /**
     * @associates Syntax 
     */
    private ArrayList syntaxList = new ArrayList();

    /** List of the positions used by storePosition() 
     * @associates Mark*/
    private ArrayList posList = new ArrayList();

    /** List of the integers marking the free positions in the posList. 
     * @associates Integer*/
    private ArrayList posFreeList = new ArrayList();

    /** Root element of line elements representation */
    protected LineRootElement lineRootElement;

    private LeafElement composedTextElement;

    /** Create base document with a specified syntax.
    * @param kitClass class used to initialize this document with proper settings
    *   category based on the editor kit for which this document is created
    * @param syntax syntax scanner to use with this document
    */
    public BaseDocument(Class kitClass, boolean addToRegistry) {
        this(kitClass, addToRegistry, new DocOp());
    }

    private BaseDocument(Class kitClass, boolean addToRegistry, DocOp op) {
        super(op);
        this.op = op;
        this.kitClass = kitClass;

        settingsChange(null); // initialize variables from settings

        op.setDocument(this);

        // Line separators default to platform ones
        putProperty(READ_LINE_SEPARATOR_PROP, Analyzer.getPlatformLS());
        putProperty(WRITE_LINE_SEPARATOR_PROP, Analyzer.getPlatformLS());

        bookmarkChain = new MarkChain(this, DrawLayerFactory.BOOKMARK_LAYER_NAME);

        // Add document draw-layers
        addLayer(new DrawLayerFactory.SyntaxLayer());
        addLayer(new DrawLayerFactory.HighlightSearchLayer());
        addLayer(new DrawLayerFactory.BookmarkLayer());


        // Possibly add the document to registry
        if (addToRegistry) {
            Registry.addDocument(this); // add if created thru the kit
        }

        // Start listen on find-support
        findSupportListener = new PropertyChangeListener() {
                                  public void propertyChange(PropertyChangeEvent evt) {
                                      findSupportChange(evt);
                                  }
                              };
        FindSupport.getFindSupport().addPropertyChangeListener(findSupportListener);
        findSupportChange(null); // update doc by find settings
    }

    private void findSupportChange(PropertyChangeEvent evt) {
        // set all finders to null
        putProperty(STRING_FINDER_PROP, null);
        putProperty(STRING_BWD_FINDER_PROP, null);
        putProperty(BLOCKS_FINDER_PROP, null);

        DrawLayerFactory.HighlightSearchLayer hsl
        = (DrawLayerFactory.HighlightSearchLayer)findLayer(
              DrawLayerFactory.HIGHLIGHT_SEARCH_LAYER_NAME);

        Boolean b = (Boolean)FindSupport.getFindSupport().getPropertyNoInit(
                        Settings.FIND_HIGHLIGHT_SEARCH);
        hsl.setEnabled((b != null) ? b.booleanValue() : false);

        fireChangedUpdate(createDocumentEvent(0, getLength(),
                                              DocumentEvent.EventType.CHANGE)); // refresh whole document
    }

    /** Called when settings were changed. The method is called
    * also in constructor, so the code must count with the evt being null.
    */
    public void settingsChange(SettingsChangeEvent evt) {
        String settingName = (evt != null) ? evt.getSettingName() : null;

        if (settingName == null || Settings.TAB_SIZE.equals(settingName)) {
            putProperty(Settings.TAB_SIZE, SettingsUtil.getValue(kitClass,
                        Settings.TAB_SIZE, DefaultSettings.defaultTabSize));
        }

        // these should not make any visual difference
        if (settingName == null || Settings.READ_BUFFER_SIZE.equals(settingName)) {
            putProperty(Settings.READ_BUFFER_SIZE, SettingsUtil.getValue(kitClass,
                        Settings.READ_BUFFER_SIZE, DefaultSettings.defaultReadBufferSize));
        }

        if (settingName == null || Settings.WRITE_BUFFER_SIZE.equals(settingName)) {
            putProperty(Settings.WRITE_BUFFER_SIZE, SettingsUtil.getValue(kitClass,
                        Settings.WRITE_BUFFER_SIZE, DefaultSettings.defaultWriteBufferSize));
        }

        if (settingName == null || Settings.MARK_DISTANCE.equals(settingName)) {
            putProperty(Settings.MARK_DISTANCE, SettingsUtil.getValue(kitClass,
                        Settings.MARK_DISTANCE, DefaultSettings.defaultMarkDistance));
        }

        if (settingName == null || Settings.MAX_MARK_DISTANCE.equals(settingName)) {
            putProperty(Settings.MAX_MARK_DISTANCE, SettingsUtil.getValue(kitClass,
                        Settings.MAX_MARK_DISTANCE, DefaultSettings.defaultMaxMarkDistance));
        }

        if (settingName == null || Settings.MIN_MARK_DISTANCE.equals(settingName)) {
            putProperty(Settings.MIN_MARK_DISTANCE, SettingsUtil.getValue(kitClass,
                        Settings.MIN_MARK_DISTANCE, DefaultSettings.defaultMinMarkDistance));
        }

        if (settingName == null || Settings.READ_MARK_DISTANCE.equals(settingName)) {
            putProperty(Settings.READ_MARK_DISTANCE, SettingsUtil.getValue(kitClass,
                        Settings.READ_MARK_DISTANCE, DefaultSettings.defaultReadMarkDistance));
        }

        if (settingName == null || Settings.SYNTAX_UPDATE_BATCH_SIZE.equals(settingName)) {
            putProperty(Settings.SYNTAX_UPDATE_BATCH_SIZE, SettingsUtil.getValue(kitClass,
                        Settings.SYNTAX_UPDATE_BATCH_SIZE, DefaultSettings.defaultSyntaxUpdateBatchSize));
        }

        if (settingName == null || Settings.LINE_BATCH_SIZE.equals(settingName)) {
            putProperty(Settings.LINE_BATCH_SIZE, SettingsUtil.getValue(kitClass,
                        Settings.LINE_BATCH_SIZE, DefaultSettings.defaultLineBatchSize));
        }

        if (settingName == null || Settings.IDENTIFIER_ACCEPTOR.equals(settingName)) {
            identifierAcceptor = SettingsUtil.getAcceptor(kitClass,
                                 Settings.IDENTIFIER_ACCEPTOR, AcceptorFactory.LETTER_DIGIT);
        }

        if (settingName == null || Settings.WHITESPACE_ACCEPTOR.equals(settingName)) {
            whitespaceAcceptor = SettingsUtil.getAcceptor(kitClass,
                                 Settings.WHITESPACE_ACCEPTOR, AcceptorFactory.WHITESPACE);
        }

        boolean stopOnEOL = SettingsUtil.getBoolean(kitClass,
                            Settings.WORD_MOVE_NEW_LINE_STOP, true);
        if (settingName == null || Settings.NEXT_WORD_FINDER.equals(settingName)) {
            putProperty(Settings.NEXT_WORD_FINDER,
                        SettingsUtil.getValue(kitClass, Settings.NEXT_WORD_FINDER,
                                              new FinderFactory.NextWordFwdFinder(this, stopOnEOL, false)));
        }

        if (settingName == null || Settings.PREVIOUS_WORD_FINDER.equals(settingName)) {
            putProperty(Settings.PREVIOUS_WORD_FINDER,
                        SettingsUtil.getValue(kitClass, Settings.PREVIOUS_WORD_FINDER,
                                              new FinderFactory.PreviousWordBwdFinder(this, stopOnEOL, false)));
        }

    }

    /** Create the syntax scanner for the document. The default
    * behavior is to call the editor kit createSyntax() method.
    */
    public Syntax createSyntax() {
        return BaseKit.getKit(kitClass).createSyntax(this);
    }

    Syntax getFreeSyntax() {
        synchronized (syntaxList) {
            int cnt = syntaxList.size();
            return (cnt > 0) ? (Syntax)syntaxList.remove(cnt - 1) : createSyntax();
        }
    }

    void releaseSyntax(Syntax syntax) {
        synchronized (syntaxList) {
            syntaxList.add(syntax);
        }
    }

    public SyntaxSupport getSyntaxSupport() {
        if (syntaxSupport == null) {
            syntaxSupport = BaseKit.getKit(kitClass).createSyntaxSupport(this);
        }
        return syntaxSupport;
    }

    /** Perform any generic text processing. The advantage of this method
    * is that it allows the text to processed in line batches. The initial
    * size of the batch is given by the Settings.LINE_BATCH_SIZE.
    * The TextBatchProcessor.processTextBatch() method is called for every
    * text batch. If the method returns true, it means the processing should
    * continue with the next batch of text which will have double line count
    * compared to the previous one. This guarantees there will be not too many
    * batches so the processing should be more efficient.
    * @param tbp text batch processor to be used to process the text batches
    * @param startPos starting position of the processing.
    * @param endPos ending position of the processing. This can be -1 to signal
    *   the end of document. If the endPos is lower than startPos then the batches
    *   are created in the backward direction.
    * @return the returned value from the last tpb.processTextBatch() call.
    *   The -1 will be returned for (startPos == endPos).
    */
    public int processText(TextBatchProcessor tbp, int startPos, int endPos)
    throws BadLocationException {
        if (endPos == -1) {
            endPos = getLength();
        }
        int batchLineCnt = ((Integer)getProperty(Settings.LINE_BATCH_SIZE)).intValue();
        int batchStart = startPos;
        int ret = -1;
        if (startPos < endPos) { // batching in forward direction
            while (ret < 0 && batchStart < endPos) {
                int batchEnd = Math.min(Utilities.getRowStart(this, batchStart, batchLineCnt), endPos);
                if (batchEnd == -1) { // getRowStart() returned -1
                    batchEnd = endPos;
                }
                ret = tbp.processTextBatch(this, batchStart, batchEnd, (batchEnd == endPos));
                batchLineCnt *= 2; // double the scanned area
                batchStart = batchEnd;
            }
        } else {
            while (ret < 0 && batchStart > endPos) {
                int batchEnd = Math.max(Utilities.getRowStart(this, batchStart, -batchLineCnt), endPos);
                ret = tbp.processTextBatch(this, batchStart, batchEnd, (batchEnd == endPos));
                batchLineCnt *= 2; // double the scanned area
                batchStart = batchEnd;
            }
        }
        return ret;
    }


    public boolean isIdentifierPart(char ch) {
        return identifierAcceptor.accept(ch);
    }

    public boolean isWhitespace(char ch) {
        return whitespaceAcceptor.accept(ch);
    }

    /** Length of document.
    * @return number of characters >= 0
    */
    public final int getLength() {
        return op.length();
    }

    /** Create the mark for the given position
    * and store it in the list. The position can
    * be later retrieved through its ID.
    */
    int storePosition(int pos) throws BadLocationException {
        Mark mark = op.insertMark(pos, false);
        int ind;
        if (posFreeList.size() > 0) {
            ind = ((Integer)posFreeList.remove(posFreeList.size() - 1)).intValue();
            posList.set(ind, mark);
        } else { // no free indexes
            ind = posList.size();
            posList.add(mark);
        }
        return ind;
    }

    int getStoredPosition(int posID) {
        if (posID < 0 || posID >= posList.size()) {
            return -1;
        }

        try {
            return ((Mark)posList.get(posID)).getOffset();
        } catch (InvalidMarkException e) {
            return -1;
        }
    }

    void removeStoredPosition(int posID) {
        if (posID >= 0 || posID < posList.size()) {
            Mark mark = (Mark)posList.get(posID);
            posList.set(posID, null); // clear the index
            posFreeList.add(new Integer(posID));
        }
    }

    /** Inserts string into document */
    public void insertString(int offset, String text, AttributeSet a)
    throws BadLocationException {
        if (text == null || text.length() == 0) {
            return;
        }

        // Check offset correctness
        if (offset < 0 || offset > getLength()) {
            throw new BadLocationException("Wrong insert position", offset); // NOI18N
        }

        // Check whether the insert is performed because of the input-methods.
        // If so try to handle it through the defaultKeyTypedAction
        /*    if (a != null
                && a.getAttribute(javax.swing.text.StyleConstants.ComposedTextAttribute) != null
            ) {
              javax.swing.text.JTextComponent c = BaseTextUI.getFocusedComponent();
              if (c.getDocument() == this) { // additional check
                BaseKit kit = Utilities.getKit(c);
                if (kit != null) {
                  javax.swing.Action dkta = kit.getActionByName(
                      DefaultEditorKit.defaultKeyTypedAction);

                  if (dkta instanceof BaseKit.DefaultKeyTypedAction) {
                    ((BaseKit.DefaultKeyTypedAction)dkta).actionPerformed(
                        new java.awt.event.ActionEvent(c,
                            java.awt.event.ActionEvent.ACTION_PERFORMED, text), c, true);
                    return;
                  }
                }
              }
            }
        */

        // possible CR-LF conversion
        text = Analyzer.convertLSToLF(text);

        // Perform the insert
        try {
            extWriteLock();
            if (offset < 0 || offset > getLength()) {
                throw new BadLocationException("Wrong insert position", offset); // NOI18N
            }

            preInsertUpdate(offset, text, a);

            // Do the real insert into the content
            UndoableEdit edit = op.insertString(offset, text);

            BaseDocumentEvent evt = createDocumentEvent(offset, text.length(), DocumentEvent.EventType.INSERT);
            if (edit != null) {
                evt.addEdit(edit);
            }

            modified = true;

            if (atomicDepth > 0) {
                if (atomicEdits == null) {
                    atomicEdits = new CompoundEdit();


                }
                atomicEdits.addEdit(evt); // will be added
            }

            insertUpdate(evt, a);

            evt.end();

            fireInsertUpdate(evt);

            boolean isComposedText = ((a != null)
                                      && (a.isDefined(StyleConstants.ComposedTextAttribute)));

            if (atomicDepth == 0) { // !!! should handle the undo && !isComposedText) {
                fireUndoableEditUpdate(new UndoableEditEvent(this, evt));
            }
        } finally {
            extWriteUnlock();
        }
    }

    /** Removes portion of a document */
    public void remove(int offset, int len) throws BadLocationException {
        if (len > 0) {
            try {
                extWriteLock();
                int docLen = getLength();
                if (offset < 0 || offset > docLen) {
                    throw new BadLocationException("Wrong remove position", offset); // NOI18N
                }
                if (offset + len > docLen) {
                    throw new BadLocationException("End offset of removed text is too big", offset + len); // NOI18N
                }

                BaseDocumentEvent evt = createDocumentEvent(offset, len, DocumentEvent.EventType.REMOVE);

                preRemoveUpdate(evt);
                removeUpdate(evt);

                UndoableEdit edit = op.remove(offset, len);
                if (edit != null) {
                    evt.addEdit(edit);
                }

                if (atomicDepth > 0) { // add edits as soon as possible
                    if (atomicEdits == null) {
                        atomicEdits = new CompoundEdit();


                    }
                    atomicEdits.addEdit(evt); // will be added
                }

                postRemoveUpdate(evt);

                evt.end();

                fireRemoveUpdate(evt);
                if (atomicDepth == 0) {
                    fireUndoableEditUpdate(new UndoableEditEvent(this, evt));
                }
            } finally {
                extWriteUnlock();
            }
        }
    }

    /** This method is called automatically before the document
    * insertion occurs and can be used to revoke the insertion before it occurs
    * by throwing the <tt>BadLocationException</tt>.
    * @param offset position where the insertion will be done
    * @param text string to be inserted
    * @param a attributes of the inserted text
    */
    protected void preInsertUpdate(int offset, String text, AttributeSet a)
    throws BadLocationException {
    }

    /** This method is called automatically before the document
    * removal occurs and can be used to revoke the removal before it occurs
    * by throwing the <tt>BadLocationException</tt>.
    */
    protected void preRemoveUpdate(DefaultDocumentEvent evt)
    throws BadLocationException {
    }

    public String getText(int[] block) throws BadLocationException {
        return getText(block[0], block[1] - block[0]);
    }

    public char[] getChars(int pos, int len) throws BadLocationException {
        return op.getChars(pos, len);
    }

    public char[] getChars(int[] block) throws BadLocationException {
        return getChars(block[0], block[1] - block[0]);
    }

    public void getChars(int pos, char[] ret, int offset, int len)
    throws BadLocationException {
        op.getChars(pos, ret, offset, len);
    }

    /** Find something in document using a finder.
    * @param finder finder to be used for the search
    * @param startPos position in the document where the search will start
    * @param limitPos position where the search will be end with reporting
    *   that nothing was found.
    */
    public int find(Finder finder, int startPos, int limitPos)
    throws BadLocationException {
        if (finder instanceof AdjustFinder) {
            int docLen = getLength();
            if (limitPos == -1) {
                limitPos = docLen;
            }
            if (startPos == -1) {
                startPos = docLen;
            }

            if (startPos == limitPos) { // stop immediately
                finder.reset(); // reset() should be called in all the cases
                return -1; // must stop here because wouldn't know if fwd/bwd search?
            }

            boolean forward = (startPos < limitPos);
            startPos = ((AdjustFinder)finder).adjustStartPos(this, startPos);
            limitPos = ((AdjustFinder)finder).adjustLimitPos(this, limitPos);
            boolean voidSearch = (forward ? (startPos >= limitPos) : (startPos <= limitPos));
            if (voidSearch) {
                finder.reset();
                return -1;
            }
        }

        return op.find(finder, startPos, limitPos);
    }

    public void print(PrintContainer container) {
        try {
            readLock();
            ExtUI extUI = BaseKit.getKit(kitClass).createPrintExtUI(this);
            Drawer.PrintDG printDG = new Drawer.PrintDG(container);
            Drawer.getDrawer().draw(printDG, extUI, 0, getLength(), 0, 0, Integer.MAX_VALUE);
        } catch (BadLocationException e) {
            e.printStackTrace();
        } finally {
            readUnlock();
        }
    }

    /** Create biased position in document */
    public Position createPosition(int offset, Position.Bias bias)
    throws BadLocationException {
        return op.createPosition(offset, bias);
    }

    /** Return array of root elements - usually only one */
    public Element[] getRootElements() {
        Element[] elems = new Element[1];
        elems[0] = getDefaultRootElement();
        return elems;
    }

    /** Return default root element */
    public Element getDefaultRootElement() {
        if (defaultRootElem == null) {
            defaultRootElem = new org.netbeans.editor.LeafElement(this, null, null, 0, getLength(), false, false);
        }
        return defaultRootElem;
    }

    /** Runs the runnable under read lock. */
    public void render(Runnable r) {
        try {
            readLock();
            r.run();
        } finally {
            readUnlock();
        }
    }

    /** Runs the runnable under write lock. This is a stronger version
    * of the runAtomicAsUser() method, because if there any locked sections
    * in the documents this methods breaks the modification locks and modifies
    * the document.
    * If there are any excpeptions thrown during the processing of the runnable,
    * all the document modifications are rolled back automatically.
    */
    public void runAtomic(Runnable r) {
        runAtomicAsUser(r);
    }

    /** Runs the runnable under write lock.
    * If there are any excpeptions thrown during the processing of the runnable,
    * all the document modifications are rolled back automatically.
    */
    public void runAtomicAsUser(Runnable r) {
        boolean completed = false;
        try {
            atomicLock();
            r.run();
            completed = true;
        } finally {
            try {
                if (!completed) {
                    breakAtomicLock();
                }
            } finally {
                atomicUnlock();
            }
        }
    }

    /** Insert contents of reader at specified position into document.
    * @param reader reader from which data will be read
    * @param pos on which position that data will be inserted
    */
    public void read(Reader reader, int pos)
    throws IOException, BadLocationException {
        try {
            extWriteLock();

            if (pos < 0 || pos > getLength()) {
                throw new BadLocationException("BaseDocument.read()", pos); // NOI18N
            }

            if (inited || modified) { // was the document already initialized?
                Analyzer.read(this, reader, pos);
            } else { // not initialized yet, we can use initialRead()
                Analyzer.initialRead(this, reader, true);
                BaseDocumentEvent evt = createDocumentEvent(0, 0, DocumentEvent.EventType.INSERT);
                evt.end();
                fireInsertUpdate(evt); // fire the insert event with zero length to notify about the change
                inited = true; // initialized but not modified
            }
        } finally {
            extWriteUnlock();
        }
    }

    /** Write part of the document into specified writer.
    * @param writer writer into which data will be written.
    * @param pos from which position get the data
    * @param len how many characters write
    */
    public void write(Writer writer, int pos, int len)
    throws IOException, BadLocationException {
        try {
            readLock();

            if ((pos < 0) || ((pos + len) > getLength())) {
                readUnlock();
                throw new BadLocationException("BaseDocument.write()", pos); // NOI18N
            }
            Analyzer.write(this, writer, pos, len);
            writer.flush();
        } finally {
            readUnlock();
        }
    }

    /** Get tab size for this document. */
    public int getTabSize() {
        Integer tabSize = (Integer)getProperty(Settings.TAB_SIZE);
        return (tabSize != null) ? tabSize.intValue() : 8;
    }

    public final Class getKitClass() {
        return kitClass;
    }

    /** Extended write locking of the document allowing
    * reentrant write lock acquiring.
    */
    public synchronized final void extWriteLock() {
        if (Thread.currentThread() != getCurrentWriter()) {
            super.writeLock();
        } else { // inner locking block
            writeDeep++; // only increase write deepness
        }
    }

    /** Extended write unlocking.
    * @see extWriteLock()
    */
    public synchronized final void extWriteUnlock() {
        if (Thread.currentThread() != getCurrentWriter()) {
            throw new Error(WRITE_LOCK_MISSING);
        }

        if (writeDeep == 0) { // most outer locking block
            super.writeUnlock();
        } else { // just inner locking block
            writeDeep--;
        }
    }

    public synchronized final void atomicLock() {
        extWriteLock();
        atomicDepth++;
    }

    public synchronized final void atomicUnlock() {
        extWriteUnlock();
        if (atomicDepth == 0) {
            return;
        }
        if (--atomicDepth == 0) { // must fire possible undo event
            if (atomicEdits != null) {
                atomicEdits.end();
                fireUndoableEditUpdate(new UndoableEditEvent(this, atomicEdits));
                atomicEdits = null;
            }
        }
    }

    /** Is the document currently atomically locked?
    * It's not synced as this method must be called only from writer thread.
    */
    public final boolean isAtomicLock() {
        return (atomicDepth > 0);
    }

    /** Break the atomic lock so that doc is no longer in atomic mode.
    * All the performed changes are rolled back automatically.
    * Even after calling this method, the atomicUnlock() must still be called.
    * This method is not synced as it must be called only from writer thread.
    */
    public final void breakAtomicLock() {
        atomicDepth = 0;
        if (atomicEdits != null) {
            atomicEdits.end();
            atomicEdits.undo();
            atomicEdits = null;
        }
    }

    protected final int getAtomicDepth() {
        return atomicDepth;
    }

    protected BaseDocumentEvent createDocumentEvent(int pos, int length,
            DocumentEvent.EventType type) {
        return new BaseDocumentEvent(this, pos, length, type);
    }

    /** Was the document modified by either insert/remove
    * but not the initial read)?
    */
    public boolean isModified() {
        return modified;
    }

    /** Get the layer with the specified name */
    public DrawLayer findLayer(String layerName) {
        return drawLayerList.findLayer(layerName);
    }

    public boolean addLayer(DrawLayer layer) {
        if (drawLayerList.add(layer)) {
            BaseDocumentEvent evt = createDocumentEvent(0, 0, DocumentEvent.EventType.CHANGE);
            evt.addEdit(new BaseDocumentEvent.DrawLayerChange(layer.getName()));
            fireChangedUpdate(evt);
            return true;
        } else {
            return false;
        }
    }

    final DrawLayerList getDrawLayerList() {
        return drawLayerList;
    }

    /** Toggle the bookmark for the current line */
    public boolean toggleBookmark(int pos) throws BadLocationException {
        pos = Utilities.getRowStart(this, pos);
        boolean marked = bookmarkChain.toggleMark(pos);
        fireChangedUpdate(createDocumentEvent(pos, 0, DocumentEvent.EventType.CHANGE));
        return marked;
    }

    /** Get the position of the next bookmark.
    * @pos position from which to search
    * @wrap wrap around the end of document
    * @return position of the next bookmark or -1 if there is no mark
    */
    public int getNextBookmark(int pos, boolean wrap)
    throws BadLocationException {
        try {
            pos = Utilities.getRowStart(this, pos);
            int rel = bookmarkChain.compareMark(pos);
            MarkFactory.ChainDrawMark mark = bookmarkChain.getCurMark();
            if (rel <= 0) { // right at this line, go next
                if (mark != null) {
                    if (mark.next != null) {
                        return mark.next.getOffset();
                    } else { // last bookmark
                        return (wrap && bookmarkChain.chain != null) ?
                               bookmarkChain.chain.getOffset() : -1;
                    }
                } else { // no marks
                    return -1;
                }
            } else { // mark after pos
                return mark.getOffset();
            }
        } catch (InvalidMarkException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
            return 0;
        }
    }

    private LineRootElement getLineRootElement() {
        if (lineRootElement == null) {
            lineRootElement = new LineRootElement();
        }
        return lineRootElement;
    }

    public Element getParagraphElement(int pos) {
        return getLineRootElement().getElement(
                   getLineRootElement().getElementIndex(pos));
    }

    /** Substitution for (each line = element) behavior */
    class LineRootElement implements Element {

        public Document getDocument() {
            return BaseDocument.this;
        }

        public Element getParentElement() {
            return null;
        }

        public String getName() {
            return "line-elements-root"; // NOI18N
        }

        public AttributeSet getAttributes() {
            return null;
        }

        public int getStartOffset() {
            return 0;
        }

        public int getEndOffset() {
            return getLength();
        }

        public int getElementIndex(int offset) {
            try {
                return Utilities.getLineOffset(BaseDocument.this, offset);
            } catch (BadLocationException e) {
                return 0;
            }
        }

        public int getElementCount() {
            return Utilities.getLineCount(BaseDocument.this);
        }

        public Element getElement(int index) {
            if (index < 0) {
                return null;
            }

            try {
                int startPos = Utilities.getRowStartFromLineOffset(BaseDocument.this, index);
                LineElement elem = null;
                if (startPos >= 0) {
                    MarkFactory.LineMark mark = (MarkFactory.LineMark)op.getOffsetMark(
                                                    startPos, MarkFactory.LineMark.class);
                    if (mark == null || (LineElement)mark.lineElemRef.get() == null) {
                        mark = new MarkFactory.LineMark();
                        try {
                            op.insertMark(mark, startPos);
                        } catch (InvalidMarkException e) {
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                e.printStackTrace();
                            }
                        }
                        elem = new LineElement(mark);
                        mark.lineElemRef = new WeakReference(elem);
                    } else {
                        return (LineElement)mark.lineElemRef.get();
                    }
                }
                return elem;
            } catch (BadLocationException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
                return null;
            }
        }

        public boolean isLeaf() {
            return false;
        }

    }

    /** Line element representation */
    class LineElement implements Element {

        /** Mark at the begining of the line */
        MarkFactory.LineMark startMark;

        LineElement(MarkFactory.LineMark startMark) throws BadLocationException {
            this.startMark = startMark;
        }

        public Document getDocument() {
            return BaseDocument.this;
        }

        public int getStartOffset() {
            try {
                return startMark.getOffset();
            } catch (InvalidMarkException e) {
                return 0;
            }
        }

        public int getEndOffset() {
            try {
                return op.getEOLNL(getStartOffset());
            } catch (BadLocationException e) {
                return 0;
            }
        }

        public Element getParentElement() {
            return lineRootElement;
        }

        public String getName() {
            return "line-element"; // NOI18N
        }

        public AttributeSet getAttributes() {
            return null;
        }

        public int getElementIndex(int offset) {
            return 0;
        }

        public int getElementCount() {
            return 0;
        }

        public Element getElement(int index) {
            return null;
        }

        public boolean isLeaf() {
            return true;
        }

        public void finalize() throws Throwable {
            try {
                startMark.remove();
            } catch (InvalidMarkException e) {
            }
            super.finalize();
        }

        public String toString() {
            return "getStartOffset()=" + getStartOffset() // NOI18N
                   + ", getEndOffset()=" + getEndOffset() // NOI18N
                   + ", getParentElement()=" + getParentElement(); // NOI18N
        }

    }

}

/*
 * Log
 *  61   Gandalf-post-FCS1.54.1.5    4/13/00  Miloslav Metelka fixing remove from 
 *       guarded blocks
 *  60   Gandalf-post-FCS1.54.1.4    4/6/00   Miloslav Metelka undo for composed text
 *  59   Gandalf-post-FCS1.54.1.3    4/5/00   Miloslav Metelka removed IM patch
 *  58   Gandalf-post-FCS1.54.1.2    4/3/00   Miloslav Metelka undo update
 *  57   Gandalf-post-FCS1.54.1.1    3/9/00   Miloslav Metelka tabsize fix
 *  56   Gandalf-post-FCS1.54.1.0    3/8/00   Miloslav Metelka 
 *  55   Gandalf   1.54        1/19/00  Miloslav Metelka component in 
 *       ActionEvent()
 *  54   Gandalf   1.53        1/16/00  Miloslav Metelka 
 *  53   Gandalf   1.52        1/13/00  Miloslav Metelka 
 *  52   Gandalf   1.51        1/10/00  Miloslav Metelka 
 *  51   Gandalf   1.50        1/7/00   Miloslav Metelka 
 *  50   Gandalf   1.49        1/6/00   Miloslav Metelka 
 *  49   Gandalf   1.48        1/6/00   Miloslav Metelka 
 *  48   Gandalf   1.47        1/6/00   Miloslav Metelka 
 *  47   Gandalf   1.46        1/4/00   Miloslav Metelka 
 *  46   Gandalf   1.45        12/28/99 Miloslav Metelka 
 *  45   Gandalf   1.44        11/14/99 Miloslav Metelka 
 *  44   Gandalf   1.43        11/8/99  Miloslav Metelka 
 *  43   Gandalf   1.42        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  42   Gandalf   1.41        10/10/99 Miloslav Metelka 
 *  41   Gandalf   1.40        10/4/99  Miloslav Metelka 
 *  40   Gandalf   1.39        9/16/99  Miloslav Metelka 
 *  39   Gandalf   1.38        9/15/99  Miloslav Metelka 
 *  38   Gandalf   1.37        9/10/99  Miloslav Metelka 
 *  37   Gandalf   1.36        8/27/99  Miloslav Metelka 
 *  36   Gandalf   1.35        8/17/99  Miloslav Metelka 
 *  35   Gandalf   1.34        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  34   Gandalf   1.33        8/9/99   Miloslav Metelka undo/redo presentation 
 *       names
 *  33   Gandalf   1.32        7/30/99  Miloslav Metelka 
 *  32   Gandalf   1.31        7/29/99  Miloslav Metelka 
 *  31   Gandalf   1.30        7/26/99  Miloslav Metelka 
 *  30   Gandalf   1.29        7/20/99  Miloslav Metelka 
 *  29   Gandalf   1.28        7/9/99   Miloslav Metelka 
 *  28   Gandalf   1.27        7/2/99   Miloslav Metelka 
 *  27   Gandalf   1.26        6/29/99  Miloslav Metelka Scrolling and patches
 *  26   Gandalf   1.25        6/25/99  Miloslav Metelka from floats back to ints
 *  25   Gandalf   1.24        6/1/99   Miloslav Metelka 
 *  24   Gandalf   1.23        5/24/99  Miloslav Metelka 
 *  23   Gandalf   1.22        5/15/99  Miloslav Metelka fixes
 *  22   Gandalf   1.21        5/13/99  Miloslav Metelka 
 *  21   Gandalf   1.20        5/10/99  Miloslav Metelka fix - line elem. mark
 *  20   Gandalf   1.19        5/10/99  Miloslav Metelka bugfix
 *  19   Gandalf   1.18        5/7/99   Miloslav Metelka line numbering and fixes
 *  18   Gandalf   1.17        5/5/99   Miloslav Metelka 
 *  17   Gandalf   1.16        5/5/99   Miloslav Metelka 
 *  16   Gandalf   1.15        5/1/99   Petr Hamernik   bugfix
 *  15   Gandalf   1.14        4/23/99  Miloslav Metelka patched DocumentEvent
 *  14   Gandalf   1.13        4/23/99  Miloslav Metelka changes in settings
 *  13   Gandalf   1.12        4/23/99  Miloslav Metelka Undo added and internal 
 *       improvements
 *  12   Gandalf   1.11        4/8/99   Miloslav Metelka 
 *  11   Gandalf   1.10        4/1/99   Miloslav Metelka 
 *  10   Gandalf   1.9         3/30/99  Miloslav Metelka 
 *  9    Gandalf   1.8         3/27/99  Miloslav Metelka 
 *  8    Gandalf   1.7         3/23/99  Miloslav Metelka 
 *  7    Gandalf   1.6         3/18/99  Miloslav Metelka 
 *  6    Gandalf   1.5         3/18/99  Miloslav Metelka 
 *  5    Gandalf   1.4         3/18/99  Miloslav Metelka 
 *  4    Gandalf   1.3         3/18/99  Miloslav Metelka 
 *  3    Gandalf   1.2         2/13/99  Miloslav Metelka 
 *  2    Gandalf   1.1         2/9/99   Miloslav Metelka 
 *  1    Gandalf   1.0         2/3/99   Miloslav Metelka 
 * $
 */

