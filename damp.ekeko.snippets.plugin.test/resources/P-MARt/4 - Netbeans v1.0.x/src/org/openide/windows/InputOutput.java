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

package org.openide.windows;

import java.io.Reader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.openide.util.io.NullOutputStream;
import org.openide.util.io.NullInputStream;

/** An I/O connection to one tab on the Output Window.
* @author   Ian Formanek, Jaroslav Tulach, Petr Hamernik, Ales Novak, Jan Jancura
* @version  0.15, Apr 17, 1998
*/
public interface InputOutput {


    /** Null InputOutput */
    public static final InputOutput NULL = new Null();

    /** Acquire an output writer to write to the tab.
    * This is the usual use of a tab--it writes to the main output pane.
    * @return the writer
    */
    public OutputWriter getOut();

    /** Get a reader to read from the tab.
    * If a reader is ever requested, an input line is added to the
    * tab and used to read one line at a time.
    * @return the reader
    */
    public Reader getIn();

    /** Get an output writer to write to the tab in error mode.
    * This might show up in a different color than the regular output, e.g., or
    * appear in a separate pane.
    * @return the writer
    */
    public OutputWriter getErr();

    /** Closes this tab. */
    public void closeInputOutput();

    /** Test whether this tab is closed.
    * @see #closeInputOutput
    * @return <code>true</code> if it is closed
    */
    public boolean isClosed();

    /** Show or hide the standard output pane.
    * @param value <code>true</code> to show, <code>false</code> to hide
    */
    public void setOutputVisible(boolean value);

    /** Show or hide the error pane.
    * If the error is mixed into the output, this may not be useful.
    * @param value <code>true</code> to show, <code>false</code> to hide
    */
    public void setErrVisible(boolean value);

    /** Show or hide the input line.
    * @param value <code>true</code> to show, <code>false</code> to hide
    */
    public void setInputVisible(boolean value);

    /**
    * Make this pane visible.
    * For example, may select this tab in a multi-window.
    */
    public void select ();

    /** Test whether the error output is mixed into the regular output or not.
    * @return <code>true</code> if separate, <code>false</code> if mixed in
    */
    public boolean isErrSeparated();

    /** Set whether the error output should be mixed into the regular output or not.
    * @return <code>true</code> to separate, <code>false</code> to mix in
    */
    public void setErrSeparated(boolean value);

    /** Test whether the output window takes focus when anything is written to it.
    * @return <code>true</code> if so
    */
    public boolean isFocusTaken();

    /** Set whether the output window should take focus when anything is written to it.
    * @return <code>true</code> to take focus
    */
    public void setFocusTaken(boolean value);

    /** Flush pending data in the input-line's reader.
    * Called when the reader is about to be reused.
    * @return the flushed reader
    */
    public java.io.Reader flushReader();

    /** Null InputOutput */
    static final class Null implements InputOutput {
        public OutputWriter getOut() {
            return nullWriter;
        }
        public java.io.Reader getIn() {
            return nullReader;
        }
        public OutputWriter getErr() {
            return nullWriter;
        }
        public void closeInputOutput() {
        }
        public boolean isClosed() {
            return true;
        }
        public void setOutputVisible(boolean value) {
        }
        public void setErrVisible(boolean value) {
        }
        public void setInputVisible(boolean value) {
        }
        public void select () {
        }
        public boolean isErrSeparated() {
            return false;
        }
        public void setErrSeparated(boolean value) {
        }
        public boolean isFocusTaken() {
            return false;
        }
        public void setFocusTaken(boolean value) {
        }
        public java.io.Reader flushReader() {
            return nullReader;
        }
    }

    /** Quietly consumes output */
    static final class NullOutputWriter extends OutputWriter {

        NullOutputWriter() {
            super(new OutputStreamWriter(new NullOutputStream()));
        }
        public void reset() {
        }
        public void println(String s, OutputListener l) {
        }
    }

    /* Return -1 */
    static final Reader nullReader = new InputStreamReader(new NullInputStream());
    /* dev/null */
    static final OutputWriter nullWriter = new NullOutputWriter();

}

/*
 * Log
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         9/30/99  Ales Novak      new constant NULL - null
 *       InputOutput - dev/null
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         4/9/99   Ales Novak      addMenu removed
 *  2    Gandalf   1.1         3/29/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach does not import OutputWriter from org.openide
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    changed name of setOutput to setOutputVisible (err, input)
 *  0    Tuborg    0.13        --/--/98 Petr Hamernik   Focus taken flag added.
 *  0    Tuborg    0.14        --/--/98 Ales Novak      addMouseListener
 *  0    Tuborg    0.15        --/--/98 Jan Jancura     select method added
 */
