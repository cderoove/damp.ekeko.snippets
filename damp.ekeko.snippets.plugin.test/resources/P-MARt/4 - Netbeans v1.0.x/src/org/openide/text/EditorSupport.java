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

package org.openide.text;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.util.*;
import java.text.MessageFormat;
import java.awt.print.PrinterJob;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterIOException;
import java.awt.print.PrinterAbortException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Component;
import java.lang.ref.WeakReference;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.text.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.undo.CannotUndoException;

import org.openide.awt.UndoRedo;
import org.openide.actions.*;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.PrintCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Support for associating an editor and a Swing {@link Document} to a data object.
 * Can be assigned as a cookie to any editable data object.
 * Then this data object will be capable of being opened in an editor, and there will be ways of retrieving and acting upon the Swing document which is editing it.
*
* @author Jaroslav Tulach
*/
public class EditorSupport extends OpenSupport
    implements EditorCookie, OpenCookie, CloseCookie, PrintCookie {

    /** Common name for editor mode. */
    public static final String EDITOR_MODE = "editor"; // NOI18N

    /** Task for loading the document. */
    private Task loadTask;

    /** Task for preparing the document. Consists for loading a document (runs loadTask),
    * firing </code>stateChange</code> and 
    * initializing it by attaching listeners listening to document changes, such as SavingManager and
    * LineSet. 
    */
    private Task prepareTask;

    /** editor kit to work with */
    private EditorKit kit;

    /** original document for the kit */
    private Document originalDoc;

    /** document we work with */
    private StyledDocument doc;

    /** Non default MIME type used to editing */
    private String mimeType;

    /** default mime type */
    private String defaultMIMEType;

    /** Actions to show in toolbar */
    private SystemAction[] actions;

    /** The flag saying if we should listen to the document modifications */
    private boolean listenToModifs = true;

    /** Listener to the document changes */
    private SavingManager modifL;

    /** the undo/redo manager to use for this document */
    private UndoRedo.Manager undoRedo;

    /** Listeners for the changing of the state - document in memory X closed. 
     * @associates ChangeListener*/
    private HashSet listeners;

    /** lines set for this object */
    private Line.Set lineSet;

    /** position manager */
    private PositionRef.Manager positionManager;

    /** The string which will be appended to the name of top component
    * when top component becomes modified */
    protected String modifiedAppendix = " *"; // NOI18N

    /** last selected editor. */
    private transient Editor lastSelected;

    /** Lock acquired after the first modification and used in save */
    private FileLock fileLock;

    /** Listening on the external changes in the files */
    private FileChangeListener fileChangeL;

    /** The time of the last save to determine the real external modifications */
    private long lastSaveTime;

    /** Support an existing loader entry. The file is taken from the
    * entry and is updated if the entry is moved or renamed.
    * @param entry entry to create instance from
    */
    public EditorSupport(MultiDataObject.Entry entry) {
        super (entry);
        //    System.out.println("ES created for " + getEntry().getFile()); // NOI18N
        //    Thread.dumpStack();
    }

    /* A method to create a new component. Overridden in subclasses.
    * @return the {@link Editor} for this support
    */
    protected CloneableTopComponent createCloneableTopComponent () {

        // initializes the document if not initialized
        prepareDocument ();

        DataObject obj = findDataObject ();
        Editor editor = new Editor (obj);
        return editor;
    }

    /** Create an undo/redo manager.
    * This manager is then attached to the document, and listens to
    * all changes made in it.
    * <P>
    * The default implementation simply uses <code>UndoRedo.Manager</code>.
    *
    * @return the undo/redo manager
    */
    protected UndoRedo.Manager createUndoRedoManager () {
        return new UndoRedo.Manager ();
    }

    /** Getter for undo redo manager. Used only inside this class.
    */
    UndoRedo.Manager getUndoRedo () {
        if (undoRedo == null) {
            synchronized (this) {
                if (undoRedo == null) {
                    undoRedo = createUndoRedoManager ();
                }
            }
        }
        return undoRedo;
    }


    // editor cookie .......................................................................

    /** Closes all opened editors (if the user agrees) and
    * flushes content of the document to the file.
    *
    * @return <code>false</code> if the operation is cancelled
    */
    public boolean close () {
        synchronized (getLock ()) {
            if (doc == null) {
                return true;
            }

            if (!super.close ()) {
                // if not all editors has been closed
                return false;
            }

            closeDocument();
            return true;
        }
    }

    /** Clears all data from memory.
    */
    private void closeDocument () {
        if (loadTask == null)
            return;
        loadTask = null;
        prepareTask = null;
        originalDoc = null;

        // listen to modifs
        if (listenToModifs) {
            getModifL().clearSaveCookie();
            if (doc != null) {
                doc.removeDocumentListener(getModifL());
            }
        }

        if (fileChangeL != null) {
            getEntry().getFile().removeFileChangeListener(fileChangeL);
            fileChangeL = null;
        }

        if (doc != null) {
            getUndoRedo().discardAllEdits();
            doc.removeUndoableEditListener (getUndoRedo ());
            doc = null;
        }

        if (positionManager != null) {
            positionManager.documentClosed ();
            fireStateChangeEvent ();
        }
        kit = null;

        updateLineSet (true);
    }

    /** Load the document into memory. This is done
    * in different thread. A task for the thread is returned
    * so anyone may test whether the loading has been finished
    * or is still in process.
    *
    * @return task for control over loading
    */
    public synchronized Task prepareDocument () {
        if (prepareTask != null)
            return prepareTask;

        // after call to this method the originalDoc and kit are initialized
        // in spite of that the document is not yet fully read in

        kit = createEditorKit ();
        if (originalDoc == null) {
            originalDoc = kit.createDefaultDocument ();
        }

        if (fileChangeL == null) {
            fileChangeL = new FileChangeAdapter() {
                              public void fileChanged(final FileEvent evt) {
                                  if (evt.getTime() > lastSaveTime) {
                                      // post in AWT event thread because of possible dialog popup
                                      SwingUtilities.invokeLater(
                                          new Runnable() {
                                              public void run() {
                                                  checkReload(evt);
                                              }
                                          }
                                      );
                                  }
                              }
                          };
            entry.getFile().addFileChangeListener(fileChangeL);
        }

        loadTask = new Task (new Runnable () {
                                 public void run () {
                                     synchronized (getLock ()) {
                                         try {
                                             doc = loadDocument (kit, originalDoc);
                                         } finally {
                                             if (doc == null) {
                                                 // when this task is finished => the document is never null
                                                 doc = createNetBeansDocument (originalDoc);
                                             }
                                         }
                                         // opening the document, inform position manager
                                         getPositionManager ().documentOpened (doc);

                                         // create new description of lines
                                         updateLineSet (true);

                                         lastSaveTime = System.currentTimeMillis();
                                     }
                                 }
                             });


        prepareTask = new Task (new Runnable () {
                                    public void run () {
                                        loadTask.run ();
                                        fireStateChangeEvent ();
                                        // listen to modifications if appropriate
                                        if (listenToModifs) {
                                            doc.addDocumentListener(getModifL());
                                        }
                                    }
                                });

        // runs the initialization task and then
        // notifies that the document has been opened
        Thread r = new Thread (prepareTask);

        // starts the task
        r.start ();

        return prepareTask;
    }

    private void checkReload(FileEvent evt) {
        boolean doReload = evt.isExpected();
        if (!doReload) {
            MessageFormat fmt = new MessageFormat(NbBundle.getBundle(EditorSupport.class).getString("FMT_External_change")); // NOI18N
            String msg = fmt.format(new Object[] { getEntry().getFile().getPackageNameExt('/', '.')});
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
            Object ret = TopManager.getDefault().notify(nd);

            if (NotifyDescriptor.YES_OPTION.equals(ret)) {
                doReload = true;
            }
        }

        if (doReload) {
            reloadDocument();
        }
    }

    /** Get the document associated with this cookie.
    * It is an instance of Swing's {@link StyledDocument} but it should
    * also understand the NetBeans {@link NbDocument#GUARDED} to
    * prevent certain lines from being edited by the user.
    * <P>
    * If the document is not loaded the method blocks until
    * it is.
    *
    * @return the styled document for this cookie that
    *   understands the guarded attribute
    * @exception IOException if the document could not be loaded
    */
    public StyledDocument openDocument () throws IOException {
        for (;;) {
            // load the document
            prepareDocument ().waitFinished ();
            StyledDocument d = doc;
            if (d != null)
                return d;
        }
    }

    /** Get the document. This method may be called before the document initialization
     * (<code>prepareTask</code>)
     * has been completed, in such a case the document must not be modified.
     * @return document or <code>null</code> if it is not yet loaded
     */
    public StyledDocument getDocument () {
        for (;;) {
            Task t = loadTask;
            if (t != null) {
                // if an task exists
                t.waitFinished ();
                return doc;
            } else {
                return null;
            }
        }
    }

    /** Test whether the document is in memory, or whether loading is still in progress.
    * @return <code>true</code> if document is loaded
    */
    public boolean isDocumentLoaded() {
        return loadTask != null;
    }

    /** Save the document in this thread.
    * Create 'orig' document for the case that the save would fail.
    * @exception IOException on I/O error
    */
    public void saveDocument () throws IOException {
        final FileObject file = entry.getFile();
        file.getFileSystem ().runAtomicAction (
            new org.openide.filesystems.FileSystem.AtomicAction () {
                public void run () throws IOException {
                    FileObject dir = file.getParent();
                    StyledDocument myDoc = getDocument();
                    OutputStream os = null;

                    if (file.isReadOnly()) { // source is read-only
                        MessageFormat format = new MessageFormat(NbBundle.getBundle(EditorSupport.class).getString("EXC_ReadOnlyFile")); // NOI18N
                        String msg = format.format(new Object[] { file.getPackageNameExt('/', '.')});
                        throw new IOException(msg);
                    }

                    // write the document
                    try {
                        acquireFileLock();
                        os = new BufferedOutputStream(file.getOutputStream(fileLock));
                        saveFromKitToStream (myDoc, kit, os);

                        DataObject dataObj = findDataObject();
                        dataObj.setModified(false);
                        releaseFileLock();
                    } catch (BadLocationException ex) {
                        TopManager.getDefault ().notifyException (ex);
                    } finally {
                        if (os != null)
                            os.close();
                    }

                    lastSaveTime = System.currentTimeMillis();
                }
            }
        );

        // update cached info about lines
        updateLineSet (true);
    }

    /**
     * Actually write file data to an output stream from an editor kit's document.
     * Called during a file save by {@link #saveDocument}.
     * <p>The default implementation just calls {@link EditorKit#write(OutputStream, Document, int, int) EditorKit.write(...)}.
     * Subclasses could override this to provide support for persistent guard blocks, for example.
     * @param doc the document to write from
     * @param kit the associated editor kit
     * @param stream the open stream to write to
     * @throws IOException if there was a problem writing the file
     * @throws BadLocationException should not normally be thrown
     * @see #loadFromStreamToKit
     */
    protected void saveFromKitToStream (StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        kit.write(stream, doc, 0, doc.getLength());
    }

    /** Test whether the document is modified.
    * @return <code>true</code> if the document is in memory and is modified;
    *   otherwise <code>false</code>
    */
    public boolean isModified () {
        DataObject dataObj = findDataObject();
        return dataObj == null ? false : dataObj.isModified();
    }

    // Position management methods

    /** Provides access to position manager for the document.
    * It maintains a set of positions even the document is in memory
    * or is on the disk.
    *
    * @return position manager
    */
    final PositionRef.Manager getPositionManager () {
        if (positionManager == null) {
            synchronized (this) {
                if (positionManager == null) {
                    positionManager = new PositionRef.Manager (this);
                }
            }
        }
        return positionManager;
    }

    /** Finds data object the entry belongs to.
    * @return data object or null
    */
    protected MultiDataObject findDataObject () {
        return entry.getDataObject ();
    }

    /** Create a position reference for the given offset.
    * The position moves as the document is modified and
    * reacts to closing and opening of the document.
    *
    * @param offset the offset to create position at
    * @param bias the Position.Bias for new creating position.
    * @return position reference for that offset
    */
    public final PositionRef createPositionRef (int offset, Position.Bias bias) {
        return new PositionRef (getPositionManager (), offset, bias);
    }

    // LineSet methods .....................................................................

    /** Updates the line set.
    * @param clear clear any cached set?
    * @return the set
    */
    Line.Set updateLineSet (boolean clear) {
        if (lineSet != null && !clear) {
            return lineSet;
        }

        if (doc == null) {
            lineSet = new EditorSupportLineSet.Closed (this);
        } else {
            lineSet = new EditorSupportLineSet (this, doc);
        }

        return lineSet;
    }

    /** Get the line set for all paragraphs in the document.
    * @return positions of all paragraphs on last save
    */
    public Line.Set getLineSet () {
        return updateLineSet (false);
    }

    // other public methods ................................................................

    /**
    * Set the MIME type for the document.
    * @param s the new MIME type
    */
    public void setMIMEType (String s) {
        mimeType = s;
    }

    /**
    * Set actions for toolbar.
    * @param actions list of actions
    */
    public void setActions (SystemAction[] actions) {
        this.actions = actions;
    }

    /** Creates editor kit for this source.
    * @return editor kit
    */
    protected EditorKit createEditorKit () {
        if (kit != null) return kit;

        if (mimeType != null) {
            kit = JEditorPane.createEditorKitForContentType (mimeType);
        } else {
            FileObject fo = entry.getFile ();
            defaultMIMEType = fo.getMIMEType ();
            kit = JEditorPane.createEditorKitForContentType (defaultMIMEType);
        }

        if (kit == null || kit.getClass () == javax.swing.text.DefaultEditorKit.class) {
            kit = JEditorPane.createEditorKitForContentType ("text/plain"); // NOI18N
        }

        if (kit == null || kit.getClass () == javax.swing.text.DefaultEditorKit.class) {
            kit = new PlainEditorKit ();
        }

        return kit;
    }

    /** Utility method which enables or disables listening to modifications
    * on asociated document.
    * <P>
    * Could be useful if we have to modify document, but do not want the
    * Save and Save All actions to be enabled/disabled automatically.
    * Initially modifications are listened to.
    * @param listenToModifs whether to listen to modifications
    */
    public void setModificationListening (final boolean listenToModifs) {
        if (this.listenToModifs == listenToModifs) return;
        this.listenToModifs = listenToModifs;
        if (doc == null) return;
        if (listenToModifs)
            doc.addDocumentListener(getModifL());
        else
            doc.removeDocumentListener(getModifL());
    }

    /** Adds a listener for status changes. An event is fired
    * when the document is moved or removed from memory.
    * @param l new listener
    */
    public synchronized void addChangeListener (ChangeListener l) {
        if (listeners == null)
            listeners = new HashSet (8);
        listeners.add (l);
    }

    /** Removes a listener for status changes.
     * @param l listener to remove
    */
    public synchronized void removeChangeListener (ChangeListener l) {
        if (listeners != null)
            listeners.remove (l);
    }

    /** The implementation of @see org.openide.cookies.PrintCookie#print() method. */
    public void print() {
        PrinterJob job = PrinterJob.getPrinterJob();
        try {
            Object o = NbDocument.findPageable(openDocument());
            if (o instanceof Pageable) {
                job.setPageable((Pageable) o);
            } else {
                PageFormat pf = job.pageDialog(job.defaultPage());
                job.setPrintable((Printable) o, pf);
            }
            if (job.printDialog()) {
                job.print();
            }
        } catch (IOException e) {
            TopManager.getDefault().notifyException(e);
        } catch (PrinterAbortException e) { // user exception
            java.awt.EventQueue.invokeLater(new Runnable() { // display in the awt thread
                                                public void run() {
                                                    String msg = NbBundle.getBundle(EditorSupport.class).getString("CTL_Printer_Abort"); // NOI18N
                                                    TopManager.getDefault().notify(new NotifyDescriptor.Message(msg));
                                                }
                                            });
        } catch (PrinterException e) {
            TopManager.getDefault().notifyException(e);
        }
    }


    // non-public functions ................................................................

    private synchronized SavingManager getModifL () {
        if (modifL == null) {
            modifL = new SavingManager();
            DataObject dataObj = findDataObject();
            // listens whether add or remove SaveCookie
            dataObj.addPropertyChangeListener(modifL);
        }
        return modifL;
    }

    /** Loads the document for this object.
    * @param kit kit to use
    * @param d original document to load data into
    * @return netbeans document to put around the original one
    */
    private StyledDocument loadDocument (EditorKit kit, Document d) {
        StyledDocument doc = createNetBeansDocument (d);
        FileObject fo = entry.getFile ();
        try {
            InputStream is = new BufferedInputStream(fo.getInputStream ());
            try {
                // read the document
                loadFromStreamToKit (doc, is, kit);
            } finally {
                is.close ();
            }
            // attach undo/redo manager
            doc.addUndoableEditListener (getUndoRedo ());
            // set document name property
            doc.putProperty(javax.swing.text.Document.TitleProperty,
                            fo.getPackageNameExt('/', '.'));
            // set dataobject to stream desc property
            doc.putProperty(javax.swing.text.Document.StreamDescriptionProperty,
                            findDataObject());

        } catch (BadLocationException ex) {
            TopManager.getDefault ().notifyException (ex);
        } catch (IOException ex) {
            TopManager.getDefault ().notifyException (ex);
        }
        return doc;
    }

    /**
     * Actually read file data into an editor kit's document from an input stream.
     * Called during a file load by {@link #prepareDocument}.
     * <p>The default implementation just calls {@link EditorKit#read(InputStream, Document, int) EditorKit.read(...)}.
     * Subclasses could override this to provide support for persistent guard blocks, for example.
     * @param doc the document to read into
     * @param stream the open stream to read from
     * @param kit the associated editor kit
     * @throws IOException if there was a problem reading the file
     * @throws BadLocationException should not normally be thrown
     * @see #saveFromKitToStream
     */
    protected void loadFromStreamToKit (StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        kit.read(stream, doc, 0);
    }

    private void acquireFileLock() throws IOException {
        if (fileLock == null || !fileLock.isValid()) {
            fileLock = getEntry().takeLock();
        }
    }

    private void releaseFileLock() {
        if (fileLock != null && fileLock.isValid()) {
            fileLock.releaseLock();
        }
    }

    protected void reloadDocument() {
        if (doc != null) {
            synchronized (this) {
                if (listenToModifs) {
                    doc.removeDocumentListener(getModifL());
                    getModifL().clearSaveCookie();
                }

                // Remember caret positions in all opened panes
                final JEditorPane[] panes = getOpenedPanes();
                final int[] carets;
                if (panes != null) {
                    carets = new int[panes.length];
                    for(int i = 0; i < panes.length; i++) {
                        carets[i] =  panes[i].getCaretPosition();
                    }
                } else {
                    carets = new int[0];
                }

                NbDocument.runAtomic(doc,
                                     new Runnable() {
                                         public void run() {
                                             try {
                                                 doc.remove(0, doc.getLength()); // remove all text
                                             } catch (BadLocationException e) {
                                                 if (System.getProperty("netbeans.debug.exceptions") != null) // NOI18N
                                                     e.printStackTrace();
                                             }
                                         }
                                     }
                                    );
                getUndoRedo().discardAllEdits(); // reset undo manager
                prepareTask = null;
                final Task docLoadTask = prepareDocument();

                // Restore caret positions
                Thread caretRestoreThread = new Thread() {
                                                public void run() {
                                                    docLoadTask.waitFinished();
                                                    for (int i = 0; i < panes.length; i++) {
                                                        try {
                                                            panes[i].setCaretPosition(carets[i]);
                                                        } catch (Exception caretException) {
                                                        }
                                                    }
                                                }
                                            };
                caretRestoreThread.start();

                updateLineSet(true);
            }
        }
    }

    /** Creates netbeans document for a given document.
    * @param d document to use as underlaying one
    * @return styled document that could support Guarded.ATTRIBUTE
    */
    private static StyledDocument createNetBeansDocument (Document d) {
        if (d instanceof StyledDocument) {
            return (StyledDocument)d;
        } else {
            // create filter
            return new FilterDocument (d);
        }
    }

    /**
     * @associates Action 
     */
    Hashtable kitActions;
    private Action getAction (String s) {
        if (kitActions == null) {
            Action[] a = kit.getActions ();
            kitActions = new Hashtable (a.length);
            int i, k = a.length;
            for (i = 0; i < k; i++)
                kitActions.put (a [i].getValue (Action.NAME), a [i]);
        }
        return (Action) kitActions.get (s);
    }

    /** Fires a status change event to all listeners. */
    private final void fireStateChangeEvent() {
        if (listeners != null) {
            ChangeEvent event = new ChangeEvent(this);
            HashSet s;
            synchronized (this) {
                s = ((HashSet)listeners.clone ());
            }

            Iterator it = s.iterator ();
            while (it.hasNext ()) {
                ChangeListener l = (ChangeListener) it.next();
                l.stateChanged(event);
            }
        }
    }

    /** Method for access to entry.
    */
    MultiDataObject.Entry getEntry () {
        return entry;
    }

    /** If one or more editors are opened finds one.
    * @return an editor or null if none is opened
    */
    Editor getAnyEditor () {
        try {
            return (Editor)allEditors.getAnyComponent ();
        } catch (java.util.NoSuchElementException e) {
            return null;
        }
    }

    /** Forcibly create one editor component. Then set the caret
    * to the given position.
    * @param pos where to place the caret
    * @return always non-<code>null</code> editor
    */
    protected Editor openAt(PositionRef pos) {
        return openAt(pos, -1);
    }

    /** Forcibly create one editor component. Then set the caret
    * to the given position.
    * @param pos where to place the caret
    * @return always non-<code>null</code> editor
    */
    final Editor openAt(PositionRef pos, int column) {
        Editor e = (Editor) openCloneableTopComponent();
        e.open();
        int offset;
        if (column >= 0) {
            javax.swing.text.Element el = NbDocument.findLineRootElement (getDocument ());
            el = el.getElement (el.getElementIndex (pos.getOffset ()));
            offset = el.getStartOffset () + column;
            if (offset > el.getEndOffset ()) {
                offset = el.getEndOffset ();
            }
        } else {
            offset = pos.getOffset ();
        }
        prepareDocument ().waitFinished ();
        e.pane.getCaret().setDot(offset);
        return e;
    }

    /** Access to lock on operations on the support
    */
    Object getLock () {
        return allEditors;
    }

    /** Should test whether all data is saved, and if not, prompt the user
    * to save.
    *
    * @return <code>true</code> if everything can be closed
    */
    protected boolean canClose () {
        SaveCookie savec = (SaveCookie) entry.getDataObject().getCookie(SaveCookie.class);
        if (savec != null) {
            MessageFormat format = new MessageFormat(NbBundle.getBundle(EditorSupport.class).getString("MSG_SaveFile")); // NOI18N
            String msg = format.format(new Object[] { entry.getDataObject().getName()});
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_CANCEL_OPTION);
            Object ret = TopManager.getDefault().notify(nd);

            if (NotifyDescriptor.CANCEL_OPTION.equals(ret)
                    || NotifyDescriptor.CLOSED_OPTION.equals(ret)
               ) {
                return false;
            }

            if (NotifyDescriptor.YES_OPTION.equals(ret)) {
                try {
                    savec.save();
                }
                catch (IOException e) {
                    TopManager.getDefault().notifyException(e);
                    return false;
                }
            }
        }
        return true;
    }

    /* List of all JEditorPane's opened by this editor support.
    * The first item in the array should represent the component
    * that is currently selected or has been selected lastly.
    *
    * @return array of panes or null if no pane is opened.
    *   In no case empty array is returned.
    */
    public JEditorPane[] getOpenedPanes () {
        LinkedList ll = new LinkedList ();
        Enumeration en = allEditors.getComponents ();
        while (en.hasMoreElements ()) {
            Object o = en.nextElement ();
            if (o instanceof Editor) {
                Editor ed = (Editor)o;
                if (lastSelected == ed) {
                    ll.addFirst (ed.pane);
                } else {
                    ll.add (ed.pane);
                }
            }
        }
        return ll.isEmpty () ?
               null : (JEditorPane[])ll.toArray (new JEditorPane[ll.size ()]);
    }

    /** Cloneable top component to hold the editor kit.
    */
    public static class Editor extends CloneableTopComponent {
        /** editor pane */
        protected JEditorPane pane;
        /** data object to work with */
        protected DataObject obj;
        /** Listener for copy action enabling */
        private PropertyChangeListener copyL;
        /** Listener for cut action enabling */
        private PropertyChangeListener cutL;
        /** Listener for data object's save cookie changes */
        private PropertyChangeListener saveCookieL;
        /** Asociated editor support */
        private EditorSupport support;
        /** ungle helper variable for deserialization - indicates whether
        * top component should be discarded during deserialization or not */
        private boolean discard = false;
        /** keeps the instance of focus event for manipulating with the
        * caret visibility */
        private static WeakReference mutableFocusEvent = new WeakReference (null);


        static final long serialVersionUID =-185739563792410059L;
        /** For externalization of subclasses only */
        public Editor () {
            super();
        }

        /** Constructor
        * @param obj data object we belong to. The appropriate editor support is 
        * acquired as the DataObject's EditorSupport.class cookie.
        */
        public Editor (DataObject obj) {
            super (obj);
            this.obj = obj;
            // asociate editor support
            support = (EditorSupport)obj.getCookie(EditorSupport.class);
            initialize();
        }

        /** Constructor
        * @param obj data object we belong to. 
        * @param support editor support to use.
        */
        public Editor (DataObject obj, EditorSupport support) {
            super (obj);
            this.obj = obj;
            // asociate editor support
            this.support = support;
            initialize();
        }

        /** Get context help for this editor pane.
        * If the registered editor kit provides a help ID in bean info
        * according to the protocol described for {@link InstanceSupport#findHelp},
        * then that it used, else general help on the editor is provided.
        * @return context help
        */
        public HelpCtx getHelpCtx () {
            HelpCtx fromKit = InstanceSupport.findHelp (new InstanceSupport.Instance (support.kit));
            if (fromKit != null)
                return fromKit;
            else
                return new HelpCtx (Editor.class);
        }

        /** Performs needed initialization */
        private void initialize () {
            setCloseOperation (TopComponent.CLOSE_EACH);
            setLayout (new BorderLayout ());
            pane = new JEditorPane ();
            pane.setEditorKit (support.kit);

            updateName();

            if (support.originalDoc instanceof HTMLDocument) {
                try {
                    ((HTMLDocument)support.originalDoc).setBase (
                        support.getEntry ().getFile ().getURL ());
                } catch (FileStateInvalidException e) {
                }
                pane.addHyperlinkListener ( new HyperlinkListener () {
                                                public void hyperlinkUpdate (HyperlinkEvent e) {
                                                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                                                        if (e instanceof HTMLFrameHyperlinkEvent) {
                                                            HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
                                                            HTMLDocument doc = (HTMLDocument)pane.getDocument();
                                                            doc.processHTMLFrameHyperlinkEvent(evt);
                                                        } else {
                                                            try {
                                                                pane.setPage(e.getURL());
                                                            } catch (Exception ex) {
                                                                if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace(); // NOI18N
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                if (support.actions != null)
                    add (SystemAction.createToolbarPresenter (support.actions),
                         BorderLayout.NORTH);
            }
            // listen to save cookie changes in asociated data object
            saveCookieL = new PropertyChangeListener() {
                              public void propertyChange(PropertyChangeEvent evt) {
                                  if (DataObject.PROP_COOKIE.equals(evt.getPropertyName()) ||
                                          DataObject.PROP_NAME.equals(evt.getPropertyName())) {
                                      updateName();
                                  }
                              }
                          };
            this.obj.addPropertyChangeListener(WeakListener.propertyChange(saveCookieL, this.obj));

            pane.setDocument (support.originalDoc);
            if (support.originalDoc instanceof NbDocument.CustomEditor) {
                add(((NbDocument.CustomEditor)support.originalDoc).createEditor(pane),
                    BorderLayout.CENTER);
            } else { // not custom editor
                add (new JScrollPane (pane), BorderLayout.CENTER);
            }
        }

        /* Is called from the clone method to create new component from this one.
        * This implementation only clones the object by calling super.clone method.
        * @return the copy of this object
        */
        protected CloneableTopComponent createClonedObject () {
            return new Editor (support.findDataObject());
        }

        /** Overrides superclass version. Opens top component only if
        * it is in valid state.
        * (Editor top component may become invalid after deserialization).<br>
        * Also tries to open all other top components which are docked
        * in editor mode on given workspace, but not visible.<br>
        */
        public void open (Workspace workspace) {
            if (!discard) {
                Workspace realWorkspace = (workspace == null)
                                          ? TopManager.getDefault().getWindowManager().getCurrentWorkspace()
                                          : workspace;
                dockIfNeeded(realWorkspace);
                boolean modeVisible = false;
                TopComponent[] tcArray = editorMode(realWorkspace).getTopComponents();
                for (int i = 0; i < tcArray.length; i++) {
                    if (tcArray[i].isOpened(realWorkspace)) {
                        modeVisible = true;
                        break;
                    }
                }
                if (!modeVisible) {
                    openOtherEditors(realWorkspace);
                }
                super.open(workspace);
                openOnOtherWorkspaces(realWorkspace);
            }
        }

        /** Dock this top component to editor mode if it is not docked
        * in some mode at this time */
        private void dockIfNeeded (Workspace workspace) {
            // dock into editor mode if possible
            Mode ourMode = workspace.findMode(this);
            if (ourMode == null) {
                editorMode(workspace).dockInto(this);
            }
        }

        private Mode editorMode (Workspace workspace) {
            Mode ourMode = workspace.findMode(this);
            if (ourMode == null) {
                ourMode = workspace.createMode(
                              EDITOR_MODE, getName(),
                              EditorSupport.class.getResource(
                                  "/org/openide/resources/editorMode.gif" // NOI18N
                              )
                          );
            }
            return ourMode;
        }

        /** Utility method, opens this top component on all workspaces
        * where editor mode is visible and which differs from given
        * workspace. */
        private void openOnOtherWorkspaces (Workspace workspace) {
            Workspace[] workspaces =
                TopManager.getDefault().getWindowManager().getWorkspaces();
            Mode curEditorMode = null;
            Mode tcMode = null;
            for (int i = 0; i < workspaces.length; i++) {
                // skip given workspace
                if (workspaces[i].equals(workspace)) {
                    continue;
                }
                curEditorMode = workspaces[i].findMode(EDITOR_MODE);
                tcMode = workspaces[i].findMode(this);
                if (
                    !isOpened(workspaces[i]) &&
                    curEditorMode != null &&
                    (
                        tcMode == null ||
                        tcMode.equals(curEditorMode)
                    )
                ) {
                    // candidate for opening, but mode must be already visible
                    // (= some opened top component in it)
                    TopComponent[] tcArray = curEditorMode.getTopComponents();
                    for (int j = 0; j < tcArray.length; j++) {
                        if (tcArray[j].isOpened(workspaces[i])) {
                            // yep, open this top component on found workspace too
                            pureOpen(this, workspaces[i]);
                            break;
                        }
                    }
                }
            }
        }

        /** Utility method, opens top components which are opened
        * in editor mode on some other workspace.
        * This method should be called only if first top component is
        * being opened in editor mode on given workspace */
        private void openOtherEditors (Workspace workspace) {
            // choose candidates for opening
            Set topComps = new HashSet(15);
            Workspace[] wsArray =
                TopManager.getDefault().getWindowManager().getWorkspaces();
            Mode curEditorMode = null;
            TopComponent[] tcArray = null;
            for (int i = 0; i < wsArray.length; i++) {
                curEditorMode = wsArray[i].findMode(EDITOR_MODE);
                if (curEditorMode != null) {
                    tcArray = curEditorMode.getTopComponents();
                    for (int j = 0; j < tcArray.length; j++) {
                        if (tcArray[j].isOpened(wsArray[i])) {
                            topComps.add(tcArray[j]);
                        }
                    }
                }
            }
            // open choosed candidates
            for (Iterator iter = topComps.iterator(); iter.hasNext(); ) {
                pureOpen((TopComponent)iter.next(), workspace);
            }
        }

        /** Utility method, calls super version of open if given
        * top component is of Editor type, or calls regular open otherwise.
        * The goal is to prevent from cycle open call between
        * Editor top components */
        private void pureOpen (TopComponent tc, Workspace workspace) {
            if (tc instanceof Editor) {
                ((Editor)tc).dockIfNeeded(workspace);
                ((Editor)tc).superOpen(workspace);
            } else {
                tc.open(workspace);
            }
        }

        private void superOpen (Workspace workspace) {
            super.open(workspace);
        }

        /** Make sure the EditorKit knows it is going bye-bye. */
        public boolean canClose (Workspace workspace, boolean last) {
            boolean result = super.canClose(workspace, last);
            if (result) {
                pane.setDocument (pane.getEditorKit ().createDefaultDocument ());
                pane.setEditorKit (null);

                removeAll ();
            }
            return result;
        }

        /** When closing last view, also close the document.
         * @return <code>true</code> if close succeeded
        */
        protected boolean closeLast () {
            if (!support.canClose ()) {
                // if we cannot close the last window
                return false;
            }
            support.closeDocument ();

            if (support.lastSelected == this) {
                support.lastSelected = null;
            }

            return true;
        }

        /** The undo/redo manager of the support.
        * @return the undo/redo manager shared by all editors for this support
        */
        public UndoRedo getUndoRedo () {
            return support.getUndoRedo ();
        }

        /* Returns an array of system actions which will appear in
        * the popup menu of this component.<P>
        * Subclasses are encouraged to override this method to specify
        * their own set of actions.<br>
        * Remember to call superclass when overriding and add your actions
        * to superclass ones (or add superclass actions to yours),
        * because default implementation provides support for standard
        * component actions like save, close, and clone.
        * @return an array of actions of this component
        */
        public SystemAction[] getSystemActions () {
            SystemAction[] sa = super.getSystemActions ();
            if (support.actions == null) return sa;
            return SystemAction.linkActions (sa, support.actions);
        }

        /** Transfer the focus to the editor pane.
        */
        public void requestFocus () {
            super.requestFocus ();
            pane.requestFocus ();
        }

        /** @return Preferred size of editor top component */
        public Dimension getPreferredSize () {
            Rectangle bounds = TopManager.getDefault().getWindowManager().
                               getCurrentWorkspace().getBounds();
            return new Dimension(bounds.width / 2, bounds.height / 2);
        }

        /* This method is called when parent window of this component has focus,
        * and this component is preferred one in it.
        * Override this method to perform special action on component activation.
        * (Typical thing to do here is set performers for your actions)
        * Remember to call superclass to
        */
        protected void componentActivated () {
            final Action copy = support.getAction (DefaultEditorKit.copyAction);
            if (copy != null) {
                final CallbackSystemAction sysCopy
                = ((CallbackSystemAction) SystemAction.get (CopyAction.class));
                sysCopy.setActionPerformer (
                    new ActionPerformer () {
                        public void performAction (SystemAction action) {
                            copy.actionPerformed (new ActionEvent (Editor.this, 0, "")); // NOI18N
                        }
                    }
                );
                copy.addPropertyChangeListener(
                    copyL = new PropertyChangeListener() {
                                public void propertyChange(PropertyChangeEvent evt) {
                                    if ("enabled".equals(evt.getPropertyName())) { // NOI18N
                                        sysCopy.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
                                    }
                                }
                            }
                );
                sysCopy.setEnabled(copy.isEnabled());
            }

            final Action cut = support.getAction (DefaultEditorKit.cutAction);
            if (cut != null) {
                final CallbackSystemAction sysCut
                = ((CallbackSystemAction) SystemAction.get (CutAction.class));
                sysCut.setActionPerformer (
                    new ActionPerformer () {
                        public void performAction (SystemAction action) {
                            cut.actionPerformed (new ActionEvent (Editor.this, 0, "")); // NOI18N
                        }
                    }
                );
                cut.addPropertyChangeListener(
                    cutL = new PropertyChangeListener() {
                               public void propertyChange(PropertyChangeEvent evt) {
                                   if ("enabled".equals(evt.getPropertyName())) { // NOI18N
                                       sysCut.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
                                   }
                               }
                           }
                );
                sysCut.setEnabled(cut.isEnabled());
            }

            final Action paste = support.getAction (DefaultEditorKit.pasteAction);
            if (paste != null) {
                PasteAction sysPaste
                = ((PasteAction) SystemAction.get (PasteAction.class));
                sysPaste.setPasteTypes (
                    new PasteType[] {
                        new PasteType () {
                            public Transferable paste () {
                                paste.actionPerformed (new ActionEvent (Editor.this, 0, "")); // NOI18N
                                return null;
                            }

                        }
                    }
                );
            }
            // commented as this caused some problems in focus managing
            //pane.requestFocus ();
            support.lastSelected = this;
            // HACKING focus problems
            // to prevent from bug #3432 (cursor is invisible)
            // beware, this is UGLY code, ideas welcomed
            SwingUtilities.invokeLater(new Runnable() {
                                           public void run () {
                                               Caret ourCaret = pane.getCaret();
                                               if (ourCaret instanceof FocusListener) {
                                                   ((FocusListener)ourCaret).focusGained(mutableFocusEvent(Editor.this));
                                               }
                                           }
                                       });
        }

        /*
        * This method is called when parent window of this component losts focus,
        * or when this component losts preferrence in the parent window.
        * Override this method to perform special action on component deactivation.
        * (Typical thing to do here is unset performers for your actions)
        */
        protected void componentDeactivated () {
            final Action copy = support.getAction (DefaultEditorKit.copyAction);
            if (copy != null) {
                copy.removePropertyChangeListener(copyL);
            }

            final Action cut = support.getAction (DefaultEditorKit.cutAction);
            if (cut != null) {
                cut.removePropertyChangeListener(cutL);
            }
            // HACKING focus problems
            // to prevent from bug #3432 (cursor is invisible)
            // beware, this is UGLY code, ideas welcomed
            SwingUtilities.invokeLater(new Runnable() {
                                           public void run () {
                                               Caret ourCaret = pane.getCaret();
                                               if (ourCaret instanceof FocusListener) {
                                                   ((FocusListener)ourCaret).focusLost(mutableFocusEvent(Editor.this));
                                               }
                                           }
                                       });
        }

        /** Updates the name of this top component according to
        * the existence of the save cookie in ascoiated data object.
        * Updates tooltip also.
        */
        protected void updateName () {
            // update name
            String name = Editor.this.obj.getNodeDelegate().getDisplayName();
            if (Editor.this.obj.getCookie(SaveCookie.class) != null)
                setName(name + support.modifiedAppendix);
            else
                setName(name);
            // update tooltip
            FileObject fo = support.getEntry().getFile();
            StringBuffer fullName = new StringBuffer(fo.getPackageName('.'));
            String extension = fo.getExt();
            if (extension.length() > 0) {
                fullName.append(" ["); // NOI18N
                fullName.append(extension);
                fullName.append(']');
            }
            setToolTipText(fullName.toString());
        }

        /* Serialize this top component.
        * @param out the stream to serialize to
        */
        public void writeExternal (ObjectOutput out)
        throws IOException {
            super.writeExternal(out);
            // save our data object
            out.writeObject(obj);
            // save cursor position
            out.writeObject(new Integer(pane.getCaret().getDot()));
            // save editor support if it is serializable
            if (support instanceof Serializable) {
                out.writeObject(support);
            }
            else {
                out.writeObject(null);
            }
            //      System.out.println(getName() + " written!"); // NOI18N
        }

        /* Deserialize this top component.
        * @param in the stream to deserialize from
        */
        public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException {
            //      System.out.println("Reading editor TC..."); // NOI18N
            super.readExternal(in);
            // read data object
            obj = (DataObject)in.readObject();
            // load cursor position
            int offset = ((Integer)in.readObject()).intValue();
            // try to load or associate editor support
            Object maybe = in.readObject();
            if (maybe instanceof EditorSupport)
                support = (EditorSupport)maybe;
            else
                support = (EditorSupport)obj.getCookie(EditorSupport.class);
            // ensure that file still can be accessed
            FileObject fo = support.getEntry().getFile();
            try {
                InputStream is = fo.getInputStream();
                is.close();
            } catch (FileNotFoundException exc) {
                // warn user
                String message = NbBundle.getBundle(EditorSupport.class).
                                 getString("FMT_FileNotFound"); // NOI18N
                TopManager.getDefault().getStdOut().println(
                    MessageFormat.format(message, new Object [] {
                                             fo.getPackageNameExt('/', '.')
                                         })
                );
                // signalize to readResolve method that we should
                // deserialize to null -> this will encourage window system
                // to discard this top component
                discard = true;
                return;
                //throw new org.openide.util.io.SafeException(exc);
            }
            support.openDocument();
            initialize();
            pane.getCaret().setDot(offset);
            //      System.out.println(getName() + " read!"); // NOI18N
        }

        protected Object readResolve ()
        throws ObjectStreamException {
            return discard ? null : this;
        }

        /** Accessor to the mutable focus event. We keep the focus
        * event as mutable, because focus chnages may come frequently
        * and there is no need to create the venet again and again. */
        private static FocusEvent mutableFocusEvent (Component source) {
            FocusEvent ev = (FocusEvent)mutableFocusEvent.get ();
            if (ev == null) {
                ev = new FocusEvent(
                         source,
                         FocusEvent.FOCUS_FIRST + FocusEvent.FOCUS_LAST
                     );
                mutableFocusEvent = new WeakReference (ev);
            }
            return ev;
        }

    } // end of Editor inner class

    /** Default editor kit.
    */
    private static final class PlainEditorKit extends DefaultEditorKit
        implements ViewFactory {
        static final long serialVersionUID =-5788777967029507963L;
        /** @return cloned instance
        */
        public Object clone () {
            return new PlainEditorKit ();
        }

        /** @return this (I am the ViewFactory)
        */
        public ViewFactory getViewFactory() {
            return this;
        }

        /** Plain view for the element
        */
        public View create(Element elem) {
            return new WrappedPlainView(elem);
        }
    }

    /** SavingManager manages two tasks concerning saving:<P>
    * 1) It tracks changes in document asociated with data object and
    *    sets modification flag appropriately.<P>
    * 2) This class also implements functionality of SaveCookie interface
    */
    private final class SavingManager implements DocumentListener, SaveCookie, PropertyChangeListener {

        /*********** Implementation of the DocumentListener *******/

        /** Gives notification that an attribute or set of attributes changed.
        * @param ev event describing the action
        */
        public void changedUpdate(DocumentEvent ev) {
            //modified(); (bugfix #1492)
        }

        /** Gives notification that there was an insert into the document.
        * @param ev event describing the action
        */
        public void insertUpdate(DocumentEvent ev) {
            modified();
        }

        /** Gives notification that a portion of the document has been removed.
        * @param ev event describing the action
        */
        public void removeUpdate(DocumentEvent ev) {
            modified();
        }

        /** Gives notification that the DataObject was changed.
        * @param ev PropertyChangeEvent
        */
        public void propertyChange(PropertyChangeEvent ev) {
            if (ev.getSource() == findDataObject()) {
                if (ev.getPropertyName() == DataObject.PROP_MODIFIED) {
                    if (((Boolean) ev.getNewValue()).booleanValue()) {
                        addSaveCookie();
                    } else {
                        removeSaveCookie();
                    }
                }

                if (ev.getPropertyName().equals(DataObject.PROP_PRIMARY_FILE)) {
                    FileObject oldF = (FileObject)ev.getOldValue();
                    FileObject newF = (FileObject)ev.getNewValue();
                    oldF.removeFileChangeListener(fileChangeL);
                    releaseFileLock();
                    newF.addFileChangeListener(fileChangeL);
                    if (isModified()) { // refresh lock
                        try {
                            acquireFileLock();
                        } catch (IOException e) {
                            if (System.getProperty("netbeans.debug.exceptions") != null) // NOI18N
                                e.printStackTrace();
                        }
                    }
                }
            }
        }

        /******* Implementation of the Save Cookie *********/

        public void save () throws IOException {
            saveDocument();
        }

        void clearSaveCookie() {
            DataObject dataObj = findDataObject();
            // remove save cookie (if save was succesfull)
            dataObj.setModified(false);
            releaseFileLock();
        }

        /** Sets modification flag.
        */
        private void modified () {
            DataObject dataObj = findDataObject();
            // try to lock the document first
            try {
                acquireFileLock();
            } catch (IOException e) { // locking failed
                Toolkit.getDefaultToolkit().beep();
                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            UndoRedo ur = getUndoRedo();
                            try {
                                while (ur.canUndo()) {
                                    ur.undo();
                                }
                            } catch (CannotUndoException exc) {
                            }
                        }
                    }
                );
                return;
            }

            dataObj.setModified(true);
        }

        /** Adds save cookie to the DO.
        */
        private void addSaveCookie() {
            DataObject dataObj = findDataObject();
            // add Save cookie to the data object
            if (dataObj instanceof MultiDataObject) {
                if (dataObj.getCookie(SaveCookie.class) == null) {
                    ((MultiDataObject)dataObj).getCookieSet().add(this);
                }
            }
        }
        /** Removes save cookie from the DO.
        */
        private void removeSaveCookie() {
            DataObject dataObj = findDataObject();
            // add Save cookie to the data object
            if (dataObj instanceof MultiDataObject) {
                if (dataObj.getCookie(SaveCookie.class) == this) {
                    ((MultiDataObject)dataObj).getCookieSet().remove(this);
                }
            }
        }

    } // end of SavingManager inner class

}

/*
* Log
*  119  src-jtulach1.118       2/25/00  Petr Jiricka    Bugfix 5603
*  118  src-jtulach1.117       1/19/00  Miloslav Metelka CLOSED_OPTION checking in
*       canClose()
*  117  src-jtulach1.116       1/18/00  Miloslav Metelka reloadDocument() calls 
*       clearSaveCookie()
*  116  src-jtulach1.115       1/16/00  Jaroslav Tulach Memory leak fix.
*  115  src-jtulach1.114       1/14/00  Miloslav Metelka NOI18N
*  114  src-jtulach1.113       1/13/00  Ian Formanek    NOI18N
*  113  src-jtulach1.112       1/10/00  David Simonek   invisible cursor bug 
*       fixed
*  112  src-jtulach1.111       1/5/00   Jaroslav Tulach Fix for 
*       NullPointerException.
*  111  src-jtulach1.110       1/4/00   Miloslav Metelka undo buffer cleared 
*       during close()
*  110  src-jtulach1.109       12/23/99 David Simonek   special editor mode 
*       opening
*  109  src-jtulach1.108       12/21/99 David Simonek   #4844
*  108  src-jtulach1.107       12/17/99 David Simonek   preferred size of editor 
*       top component fixed
*  107  src-jtulach1.106       12/8/99  Jaroslav Tulach TopComponent enhanced.
*  106  src-jtulach1.105       11/29/99 Miloslav Metelka removing fileChangeL on 
*       doc close
*  105  src-jtulach1.104       11/26/99 Miloslav Metelka Backup before overwriting
*       now supported in filesystem
*  104  src-jtulach1.103       11/25/99 Miloslav Metelka File reloading improved
*  103  src-jtulach1.102       11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  102  src-jtulach1.101       11/4/99  David Simonek   tooltips on editor tabs
*  101  src-jtulach1.100       10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  100  src-jtulach1.99        10/10/99 Petr Hamernik   console debug messages 
*       removed.
*  99   src-jtulach1.98        10/7/99  David Simonek   requst focus removed from
*       componentActivated
*  98   src-jtulach1.97        10/7/99  Miloslav Metelka releasing lock when save 
*       was rejected
*  97   src-jtulach1.96        10/6/99  David Simonek   more robust serialization
*       of window system (especially editor TCs)
*  96   src-jtulach1.95        10/6/99  Miloslav Metelka blocking modification 
*       when lock acquiring fails
*  95   src-jtulach1.94        10/6/99  David Simonek   repaired deserialization 
*       when document file not found
*  94   src-jtulach1.93        10/5/99  Miloslav Metelka syncing with external 
*       changes and modification locking
*  93   src-jtulach1.92        9/27/99  Petr Jiricka    Ensured that 
*       closeDocument() is only called once
*  92   src-jtulach1.91        9/22/99  Miloslav Metelka Undo of the last change
*  91   src-jtulach1.90        9/17/99  Miloslav Metelka Syncing and locking on 
*       modify
*  90   src-jtulach1.89        9/13/99  Petr Jiricka    Loading a document 
*       divided in two tasks - fixes problems with modifying the document before
*       appropriate listeners have been added.
*  89   src-jtulach1.88        9/9/99   Petr Hamernik   fixed bug #1922
*  88   src-jtulach1.87        9/1/99   Petr Jiricka    Close calls closeDocument
*  87   src-jtulach1.86        8/27/99  Libor Kramolis  Try create text/plain 
*       editor kit before swing editor kit
*  86   src-jtulach1.85        8/19/99  Ian Formanek    Fixed bug 3520 - 
*       Breakpoint cannot be set in Editing wkspc when editor was closed in 
*       Debugging wkspc.
*  85   src-jtulach1.84        8/17/99  Petr Jiricka    Oops ! Debug prints
*  84   src-jtulach1.83        8/17/99  Petr Jiricka    Fixed serialization of 
*       editor top components constructed by the 2-parameter constructor.
*  83   src-jtulach1.82        8/17/99  Ales Novak      #3414
*  82   src-jtulach1.81        8/9/99   David Simonek   
*  81   src-jtulach1.80        8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  80   src-jtulach1.79        8/9/99   Miloslav Metelka Save failure messages 
*       localized
*  79   src-jtulach1.78        7/30/99  Ales Novak      an impl of Line.show(int,
*       int) does not ignore column now
*  78   src-jtulach1.77        7/29/99  David Simonek   further ws serialization 
*       changes
*  77   src-jtulach1.76        7/28/99  David Simonek   workspace serialization 
*       bugfixes
*  76   src-jtulach1.75        7/28/99  David Simonek   canClose updates
*  75   src-jtulach1.74        7/27/99  Jaroslav Tulach Faster lines.
*  74   src-jtulach1.73        7/25/99  Ian Formanek    Exceptions printed to 
*       console only on "netbeans.debug.exceptions" flag
*  73   src-jtulach1.72        7/21/99  Petr Hamernik   editor mode is public
*  72   src-jtulach1.71        7/21/99  Ales Novak      println removed
*  71   src-jtulach1.70        7/13/99  Ales Novak      changed to new win system
*  70   src-jtulach1.69        7/11/99  David Simonek   window system change...
*  69   src-jtulach1.68        7/8/99   Jesse Glick     Context help.
*  68   src-jtulach1.67        6/30/99  David Simonek   rollback, repaired
*  67   src-jtulach1.66        6/30/99  David Simonek   bugfix #2369
*  66   src-jtulach1.65        6/22/99  Ales Novak      creating of editors is 
*       centralized
*  65   src-jtulach1.64        6/10/99  Petr Hamernik   revision 56 rolled back 
*  64   src-jtulach1.63        6/10/99  Miloslav Metelka saveDocument() asks user
*  63   src-jtulach1.62        6/10/99  Jesse Glick     [JavaDoc]
*  62   src-jtulach1.61        6/9/99   Ian Formanek    Fixed resources for 
*       package change
*  61   src-jtulach1.60        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  60   src-jtulach1.59        6/8/99   Ian Formanek    OK, OK, that was not the 
*       best thing on the world...
*  59   src-jtulach1.58        6/8/99   Ian Formanek    Fixed last change
*  58   src-jtulach1.57        6/8/99   Ian Formanek    Survives if kit for 
*       requested mime type is not registered -> first tries text/plain kit, 
*       then PlainEditorKit is used
*  57   src-jtulach1.56        6/7/99   Jaroslav Tulach EditorCookie.getOpenedPanes
*        ()
*  56   src-jtulach1.55        6/7/99   Jaroslav Tulach Selecting line in 
*       compiler output does not focuses editor.
*  55   src-jtulach1.54        6/5/99   Jesse Glick     [JavaDoc]
*  54   src-jtulach1.53        6/4/99   Petr Jiricka    Added constructor which 
*       explicitly specifies editorsupport to be used
*  53   src-jtulach1.52        6/4/99   Jaroslav Tulach Orig files should not be 
*       seen in the explorer.
*  52   src-jtulach1.51        6/4/99   Ales Novak      # 1970
*  51   src-jtulach1.50        5/17/99  Petr Hamernik   fixed bug #1540 - 
*       canClose() confirmation dialog added
*  50   src-jtulach1.49        5/17/99  Miloslav Metelka closeDocument() non-null 
*       doc check
*  49   src-jtulach1.48        5/17/99  Miloslav Metelka saveDocument uses "orig" 
*       file
*  48   src-jtulach1.47        5/16/99  Petr Hamernik   open() calling adding to 
*       method openAt(..)
*  47   src-jtulach1.46        5/15/99  David Simonek   deserialization reopening
*       bug fixed
*  46   src-jtulach1.45        5/15/99  Petr Hamernik   deadlock prevention
*  45   src-jtulach1.44        5/15/99  David Simonek   bugfix #1492
*  44   src-jtulach1.43        5/14/99  David Simonek   serialization now 
*       hopefully correct...
*  43   src-jtulach1.42        5/14/99  Ales Novak      bugfix #1466
*  42   src-jtulach1.41        5/14/99  David Simonek   serizalization 
*       modifications
*  41   src-jtulach1.40        5/12/99  Ales Novak      PrinterAbortException 
*       handling
*  40   src-jtulach1.39        5/12/99  David Simonek   serialization
*  39   src-jtulach1.38        5/11/99  Miloslav Metelka 
*  38   src-jtulach1.37        5/11/99  Ales Novak      implements PrintCookie
*  37   src-jtulach1.36        4/23/99  Petr Jiricka    
*  36   src-jtulach1.35        4/23/99  Jaroslav Tulach Removed setEditable. 
*       Create uneditable components by subclassing.
*  35   src-jtulach1.34        4/21/99  Petr Hamernik   renaming the component 
*       after rename data object
*  34   src-jtulach1.33        4/21/99  Miloslav Metelka Added CustomEditor 
*       handling
*  33   src-jtulach1.32        4/9/99   Petr Hamernik   closing bugfix
*  32   src-jtulach1.31        4/8/99   David Simonek   debigging comments 
*       removed...
*  31   src-jtulach1.30        4/6/99   David Simonek   now supports modified 
*       flag as suffix to top component's name
*  30   src-jtulach1.29        4/6/99   David Simonek   save enabling bugs 
*       finally fixed (hopefully)
*  29   src-jtulach1.28        4/2/99   Petr Hamernik   bugfix
*  28   src-jtulach1.27        3/30/99  Miloslav Metelka Enabling of 
*       cut/copy/paste actions
*  27   src-jtulach1.26        3/26/99  Jesse Glick     [JavaDoc]
*  26   src-jtulach1.25        3/25/99  David Simonek   another small changes in 
*       window system
*  25   src-jtulach1.24        3/20/99  Petr Hamernik   
*  24   src-jtulach1.23        3/18/99  Jaroslav Tulach 
*  23   src-jtulach1.22        3/18/99  Petr Hamernik   
*  22   src-jtulach1.21        3/17/99  Jesse Glick     [JavaDoc]
*  21   src-jtulach1.20        3/17/99  Jaroslav Tulach Output Window fixing.
*  20   src-jtulach1.19        3/15/99  Jesse Glick     [JavaDoc]
*  19   src-jtulach1.18        3/14/99  Jaroslav Tulach Change of 
*       MultiDataObject.Entry.
*  18   src-jtulach1.17        3/12/99  David Simonek   Editor made public 
*  17   src-jtulach1.16        3/11/99  Jaroslav Tulach Undo/Redo support
*  16   src-jtulach1.15        3/11/99  Jaroslav Tulach Works with plain 
*       document.
*  15   src-jtulach1.14        3/10/99  Petr Hamernik   
*  14   src-jtulach1.13        3/10/99  Jaroslav Tulach Creates line set even the
*       document is not opened
*  13   src-jtulach1.12        3/9/99   Jaroslav Tulach getlock added
*  12   src-jtulach1.11        3/8/99   Petr Hamernik   
*  11   src-jtulach1.10        2/26/99  Jesse Glick     Added 
*       saveFromKitToStream() & loadFromStreamToKit().
*  10   src-jtulach1.9         2/24/99  Jesse Glick     [JavaDoc]
*  9    src-jtulach1.8         2/19/99  Petr Hamernik   changes with 
*       Position.Bias
*  8    src-jtulach1.7         2/17/99  Petr Hamernik   
*  7    src-jtulach1.6         2/15/99  Jaroslav Tulach Jumps to current line
*  6    src-jtulach1.5         2/11/99  Jaroslav Tulach 
*  5    src-jtulach1.4         2/10/99  Jesse Glick     [JavaDoc]
*  4    src-jtulach1.3         2/8/99   Petr Hamernik   synchonization bugfix
*  3    src-jtulach1.2         2/4/99   Petr Hamernik   setting of extended file 
*       attributes doesn't require FileLock
*  2    src-jtulach1.1         2/3/99   Jaroslav Tulach 
*  1    src-jtulach1.0         2/3/99   Jaroslav Tulach 
* $
*/
