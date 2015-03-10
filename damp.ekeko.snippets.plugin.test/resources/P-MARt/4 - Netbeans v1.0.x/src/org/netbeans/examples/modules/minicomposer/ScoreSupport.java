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

package org.netbeans.examples.modules.minicomposer;
import java.io.*;
import java.util.*;
import javax.swing.SwingUtilities;
import javax.swing.event.*;
import javax.swing.text.*;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.MultiDataObject;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.util.io.FoldingIOException;
public class ScoreSupport implements ScoreCookie, Runnable, DocumentListener, ChangeListener {
    private MultiDataObject.Entry entry;
    private EditorCookie edit;
    private Task prepareTask;

    /**
     * @associates ChangeListener 
     */
    private Set listeners;
    private Score score;
    private IOException parseException;
    public ScoreSupport (MultiDataObject.Entry entry, EditorCookie edit) {
        this.entry = entry;
        this.edit = edit;
        prepareTask = null;
        listeners = new HashSet ();
        score = null;
        parseException = null;
        if (edit instanceof EditorSupport) {
            ((EditorSupport) edit).addChangeListener (WeakListener.change (this, edit));
        }
    }
    public void stateChanged (ChangeEvent e) {
        invalidateLater ();
    }
    public synchronized void addChangeListener (ChangeListener l) {
        listeners.add (l);
    }
    public synchronized void removeChangeListener (ChangeListener l) {
        listeners.remove (l);
    }
    protected synchronized void fireChange () {
        final ChangeEvent ev = new ChangeEvent (this);
        final Set l2 = new HashSet (listeners);
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            Iterator it = l2.iterator ();
                                            while (it.hasNext ())
                                                ((ChangeListener) it.next ()).stateChanged (ev);
                                        }
                                    });
    }
    public synchronized Task prepare () {
        if (prepareTask == null)
            prepareTask = RequestProcessor.postRequest (this);
        return prepareTask;
    }
    public void run () {
        edit.prepareDocument ().waitFinished ();
        final Document doc = edit.getDocument ();
        if (doc == null) {
            // Should not happen:
            System.err.println("WARNING: Doc was null!");
            return;
        }
        doc.render (new Runnable () {
                        public void run () {
                            try {
                                setScoreAndParseException (parse (doc), null);
                            } catch (IOException ioe) {
                                setScoreAndParseException (score, ioe);
                            } catch (BadLocationException ble) {
                                setScoreAndParseException (score, new FoldingIOException (ble));
                            }
                        }
                    });
        doc.addDocumentListener (this);
    }
    private synchronized void setScoreAndParseException (Score s, IOException e) {
        score = s;
        parseException = e;
        fireChange ();
    }
    public boolean isValid () {
        return parseException == null;
    }
    public void changedUpdate (DocumentEvent ev) {
    }
    public void insertUpdate (DocumentEvent ev) {
        invalidateLater ();
    }
    public void removeUpdate (DocumentEvent ev) {
        invalidateLater ();
    }
    private void invalidateLater () {
        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              invalidate ();
                                          }
                                      });
    }
    protected synchronized void invalidate () {
        prepareTask = null;
        fireChange ();
    }
    public synchronized void setScore (final Score s) throws IOException {
        final Score oldScore = score;
        if (s.equals (oldScore)) {
            return;
        }
        prepareTask = Task.EMPTY;
        score = s;
        parseException = null;
        final StyledDocument doc = edit.openDocument ();
        final BadLocationException[] e = new BadLocationException[] { null };
        try {
            NbDocument.runAtomic (doc, new Runnable () {
                                      public void run () {
                                          doc.removeDocumentListener (ScoreSupport.this);
                                          try {
                                              generate (s, oldScore, doc);
                                          } catch (BadLocationException ble) {
                                              e[0] = ble;
                                          } finally {
                                              doc.addDocumentListener (ScoreSupport.this);
                                          }
                                      }
                                  });
            if (e[0] != null) throw e[0];
        } catch (BadLocationException ble) {
            throw new FoldingIOException (ble);
        }
        fireChange ();
    }
    public Score getScore () throws IOException {
        prepare ().waitFinished ();
        synchronized (this) {
            if (parseException != null && ! entry.getDataObject ().isModified ()) {
                throw parseException;
            } else if (score != null) {
                return score;
            } else if (parseException != null) {
                throw parseException;
            } else {
                // Should not happen:
                throw new IOException ("parse did not finish as expected");
            }
        }
    }
    protected Score parse (Document doc) throws IOException, BadLocationException {
        String text = doc.getText (0, doc.getLength ());
        return Score.parse (new StringReader (text));
    }
    protected void generate (Score s, Score oldScore, Document doc) throws BadLocationException {
        CharArrayWriter wr = new CharArrayWriter ();
        try {
            Score.generate (s, wr);
        } catch (IOException ioe) {
            // Should not happen.
            ioe.printStackTrace ();
            return;
        }
        doc.remove (0, doc.getLength ());
        doc.insertString (0, wr.toString (), null);
    }
}
