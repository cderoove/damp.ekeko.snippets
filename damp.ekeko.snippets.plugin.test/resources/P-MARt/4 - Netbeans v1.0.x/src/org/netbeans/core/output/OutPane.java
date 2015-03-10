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

package org.netbeans.core.output;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Event;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.event.*;

import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.cookies.EditorCookie;
import org.openide.text.Line;
import org.openide.windows.*;
import org.openide.actions.CopyAction;
import org.netbeans.core.actions.NextOutJumpAction;
import org.netbeans.core.actions.PreviousOutJumpAction;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.awt.JPopupMenuPlus;

/** This class represents one of output panes in one OutputTab (It can be
* either stdout either errout). It extends JComponent. Inside this component
* is inserted org.openide.text.view.ViewManager.
*
* @author Petr Hamernik, Jaroslav Tulach
*/
public final class OutPane extends JList
            implements MouseListener, ListSelectionListener, KeyListener,
    ActionPerformer, FocusListener, ActionListener {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -633812069000420549L;
    /** Information channel for this pane */
    OutputWriterImpl writer;
    /** model for the list */
    private PaneWriter model;

    /** My parent output tab */
    OutputTab tab;

    /** Boolean flag - First jump line was set or not */
    boolean jumpLineSet;

    /** Copy action */
    private static CopyAction copyAction = (CopyAction)CopyAction.get (CopyAction.class);

    /** Private instance of Next jump action */
    private static NextOutJumpAction nextAction = (NextOutJumpAction)NextOutJumpAction.get (NextOutJumpAction.class);

    /** Private instance of Previous jump action */
    private static PreviousOutJumpAction previousAction = (PreviousOutJumpAction)PreviousOutJumpAction.get (PreviousOutJumpAction.class);

    /** output settings */
    private static OutputSettings outputSettings = (OutputSettings)OutputSettings.findObject (OutputSettings.class, true);

    /** Performer for jump actions */
    private JumpActionPerformer jumpPerformer = new JumpActionPerformer();

    /** Mapping from the line numbers to listeners (Integer, Listener) */
    private TreeMap listeners = new TreeMap ();
    /** Map of lineNO:ide.text.Line 
     * @associates Map*/
    private Map int2Line;
    /** Message format for exception parsing */
    private static MessageFormat formatOfException;
    /** PopupMenu */
    JPopupMenu jPopup;
    private JMenuItem copyItem;
    private JMenuItem clearItem;

    /** Creates pane without association to a tab.
    */
    public OutPane() {
        this (null);
    }

    /** Creates new OutPane in the specific OutputTab */
    public OutPane(OutputTab tab) {
        this.tab = tab;
        int2Line = new WeakHashMap(29);
        this.model = new PaneWriter ();
        this.writer = new OutputWriterImpl (model);
        setModel (model);
        setCellRenderer (jumpPerformer);
        setBackground (outputSettings.getBaseBackground ());
        setSelectionMode (ListSelectionModel.SINGLE_SELECTION);

        jumpLineSet = false;

        addMouseListener(this);
        addListSelectionListener (this);
        addKeyListener(this);
        addFocusListener(this);
        jPopup = new JPopupMenuPlus();

        // add copy
        copyItem = new JMenuItem(org.openide.util.NbBundle.getBundle (OutPane.class).getString ("CTL_ClipboardCopy"));
        copyItem.addActionListener(this);
        jPopup.add(copyItem);
        jPopup.addSeparator();
        // add clear
        clearItem = new JMenuItem(org.openide.util.NbBundle.getBundle (OutPane.class).getString ("CTL_Clear"));
        clearItem.addActionListener(this);
        jPopup.add(clearItem);

        add(jPopup);
    }

    /** The writer for this pane.
    */
    public OutputWriter getOut () {
        return writer;
    }

    private OutputListener getListenerForPosition(int pos) {
        return (OutputListener)listeners.get (new Integer (pos));
    }

    /** Checks (sets/unsets) performer for CopyAction */
    final void checkCopyAction() {
        ActionPerformer aperf = (getSelectedIndex () != -1 ? this : null);
        copyAction.setActionPerformer(aperf);
    }

    void checkNextPrevActions() {
        //    if (tab.amISelected(this)) {
        nextAction.setActionPerformer((nextJump() != -1) ? jumpPerformer : null);
        previousAction.setActionPerformer((previousJump() != -1) ? jumpPerformer : null);
        checkCopyAction();
        //    }
        // Those comments are part of a bugfix - checkNextPrevAction doesn't work
        // when you are pressing alt-F8/alt-F7 in the editor
    }

    private int nextJump() {
        Integer i = new Integer (getSelectedIndex () + 1);

        synchronized (listeners) {
            SortedMap sm = listeners.tailMap (i);
            if (sm.isEmpty ()) return -1;
            i = (Integer)sm.firstKey ();
        }
        return i.intValue ();
    }

    private int previousJump() {
        Integer i = new Integer (getSelectedIndex () - 1);

        synchronized (listeners) {
            SortedMap sm = listeners.headMap (i);
            if (sm.isEmpty ()) {
                return -1;
            }
            i = (Integer)sm.lastKey ();
        }
        return i.intValue ();
    }

    /** This method is called whenever jump line is written. It tests if this line
    * is first and if it is sets the cursor to this position
    */
    private void stopAtFirstJumpLine(int index) {
        if (!jumpLineSet) {
            setSelectedIndex (index);
            jumpLineSet = true;
        }
    }

    /** Clears whole lines table */
    void clearLineTable() {
        synchronized (listeners) {
            Iterator it = listeners.entrySet ().iterator ();
            while (it.hasNext ()) {
                Map.Entry e = (Map.Entry)it.next ();
                int pos = ((Integer)e.getKey ()).intValue ();
                OutputListener listener = (OutputListener) e.getValue ();
                listener.outputLineCleared(new OutputEventImpl(tab, pos));
            }
            listeners.clear ();
            jumpLineSet = false;
            checkNextPrevActions();
        }
    }

    /** My implementation of OutputEvent. It has lazy initialized line text. */
    private class OutputEventImpl extends OutputEvent {
        private int index;

        static final long serialVersionUID =-437312909583471519L;
        public OutputEventImpl(InputOutput src, int index) {
            super(src);
        }

        /** Returns text on the line.
        * @return the text on the line
        */
        public String getLine () {
            return (String) model.getElementAt(index);
        }
    }

    private static final String EMPTY = " "; // NOI18N

    /** Writer, which can insert text into the output document.
    * @see java.io.Writer
    */
    class PaneWriter extends Writer implements ListModel {

        /** Array with all lines (String) except current line 
         * @associates String*/
        ArrayList lines;

        /** Store current line */
        private StringBuffer currentLine;
        private String currentLineStr;

        /** the buffer with text to add */
        private StringBuffer sb;

        /** true if a request for redraw has been send */
        private boolean sent = false;

        /** Last printed character was '\r' */
        private boolean lastR = false;

        /** only one listener for the model */
        private ListDataListener dataListener;

        public PaneWriter () {
            super();
            lines = new ArrayList();
            currentLine = new StringBuffer();
            currentLineStr = EMPTY;
            sb = new StringBuffer();
        }

        void cleaned () {
            sent = true;
            redraw ();
        }

        /** Draws text from buffer to editor.
        * Must be called from synchronized methods.
        */
        private synchronized void redraw() {
            final int origSize = getSize();

            String bufferStr = sb.toString();
            StringTokenizer tok = new StringTokenizer(bufferStr, "\n\r\t", true); // NOI18N

            String tab = null;

            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                if (token.equals("\n") || token.equals("\r")) { // NOI18N
                    boolean r = token.equals("\r"); // NOI18N
                    if (r || !lastR) {
                        String addLine = (currentLine.length() == 0) ? EMPTY : currentLine.toString();
                        lines.add(addLine);
                        currentLine.setLength(0);
                        currentLineStr = EMPTY;
                    }
                    lastR = r;
                }
                else {
                    if (token.equals("\t")) { // NOI18N
                        if (tab == null)
                            tab = getTab();
                        token = tab;
                    }
                    currentLine.append(token);
                    currentLineStr = null;
                }
            }

            sb.setLength(0);

            SwingUtilities.invokeLater(new Runnable () {
                                           public void run () {
                                               if (!sent)
                                                   return;
                                               sent = false;

                                               int currentSize = getSize();
                                               if (currentSize != origSize) {
                                                   fireIntervalAdded(origSize, currentSize);
                                               }
                                               else {
                                                   fireContentsChanged(currentSize, currentSize);
                                               }

                                               int selIndex = getSelectedIndex();
                                               ensureIndexIsVisible((selIndex == -1) ? currentSize - 1:  selIndex);
                                           }
                                       });
        }

        void fireChanges() {
        }


        public synchronized void write(char[] cbuf, int off, int len) throws IOException {
            if (tab != null && tab.isClosed()) {
                tab.rebindTab();
            }
            sb.append(new String(cbuf, off, len));

            // requests redraw
            sent = true;
            redraw ();
        }

        /** Prints the given string and register OutputListener on this line */
        public synchronized void println(String s, OutputListener l) throws IOException {
            if (tab != null && tab.isClosed()) {
                tab.rebindTab();
            }

            if (sb.length() > 0)
                sb.append("\n"); // NOI18N

            int indx = getSize();

            sb.append(s);
            sb.append("\n"); // NOI18N

            boolean empty = listeners.isEmpty();

            listeners.put(new Integer(indx), l);

            // flush the new line
            sent = true;
            redraw();

            if (empty) {
                // stop at this position
                setSelectedIndex(indx);
                // fires info about the change
                valueChanged (null);
            }

            checkNextPrevActions();
        }

        public synchronized void flush() throws IOException {
            if (tab != null && tab.isClosed())
                throw new IOException();
            sent = true;
            redraw();
        }

        public synchronized void close() throws IOException {
            if (tab != null && tab.isClosed())
                throw new IOException();

            // to signal that all requests for redraw has been sent
            sent = false;
            reset();
        }

        public synchronized void reset() {
            clearLineTable();
            int2Line.clear();

            int size = getSize();
            lines.clear();
            sb.setLength(0);
            currentLine.setLength(0);
            currentLineStr = EMPTY;
            if (size >= 0) {
                fireIntervalRemoved (0, size);
            }
        }

        //
        // ListDataListener methods
        //

        public Object getElementAt (int i) {
            if (i < lines.size())
                return lines.get(i);
            else if ((i == lines.size()) && (currentLine.length() > 0)) {
                if (currentLineStr == null)
                    currentLineStr = currentLine.toString();
                return currentLineStr;
            }
            else {
                return EMPTY;
            }
        }

        public int getSize () {
            return lines.size() + ((currentLine.length() > 0) ? 1 : 0);
        }

        /**
         * Add a listener to the list that's notified each time a change
         * to the data model occurs.
         * @param l the ListDataListener
         */
        public void addListDataListener(ListDataListener l) {
            if (dataListener != null) {
                // only one listener supported
                throw new InternalError ();
            }
            dataListener = l;
        }

        /**
         * Remove a listener from the list that's notified each time a
         * change to the data model occurs.
         * @param l the ListDataListener
         */
        public void removeListDataListener(ListDataListener l) {
            dataListener = null;
        }


        protected void fireContentsChanged(int index0, int index1) {
            if (dataListener != null) dataListener.contentsChanged(
                    new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index0, index1)
                );
        }


        protected void fireIntervalAdded(int index0, int index1) {
            if (dataListener != null) dataListener.intervalAdded (
                    new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index0, index1)
                );
        }


        protected void fireIntervalRemoved(int index0, int index1) {
            if (dataListener != null) dataListener.intervalRemoved (
                    new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index0, index1)
                );
        }
    }

    /** Will be returned by TopOutput (Output class implements it), as an instance of OutputWriter.
    */
    class OutputWriterImpl extends OutputWriter {
        PaneWriter writer;

        public OutputWriterImpl (PaneWriter writer) {
            super(writer);
            this.writer = writer;
        }

        public void println (String s, OutputListener l) throws IOException {
            writer.println(s, l);
        }

        public void reset() {
            writer.reset();
        }
    }


    private class JumpActionPerformer extends JLabel implements ActionPerformer, ListCellRenderer  {
        {
            // initializer
            this.setOpaque (true);
        }

        static final long serialVersionUID =4405590143900383138L;

        /** Performer for actions */
        public void performAction(SystemAction action) {
            int jump = -1;
            if (action instanceof NextOutJumpAction) {
                jump = nextJump();
            }
            if (action instanceof PreviousOutJumpAction) {
                jump = previousJump();
            }
            if (jump != -1) {
                setSelectedIndex (jump);
                if (action instanceof NextOutJumpAction) {
                    ensureIndexIsVisible (jump + 2);
                }
                ensureIndexIsVisible (jump);
                invokeJumpListener(jump);
            }
            checkNextPrevActions();
        }


        /** Component for rendering the cell.
        */
        public java.awt.Component getListCellRendererComponent (JList list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {

            String newVal = (value instanceof String) ? (String) value : ((StringBuffer)value).toString ();

            setText(newVal);

            boolean isJump = listeners.containsKey (new Integer (index)) ||
                             parseException(index, false);
            if (index != list.getSelectedIndex()) {
                setBackground (outputSettings.getBaseBackground ());
                setForeground (outputSettings.getBaseForeground ());
            } else {
                if (isJump) {
                    setBackground (outputSettings.getJumpCursorBackground ());
                    setForeground (outputSettings.getJumpCursorForeground ());
                } else {
                    setBackground (outputSettings.getCursorBackground ());
                    setForeground (outputSettings.getCursorForeground ());
                }
            }

            setFont(new java.awt.Font("monospaced", java.awt.Font.PLAIN, outputSettings.getFontSize ())); // NOI18N
            return this;
        }
    }


    /** @return true if there were an listner */
    private boolean invokeJumpListener(int index) {
        OutputListener listener = getListenerForPosition(index);
        if (listener != null) {
            listener.outputLineAction(new OutputEventImpl(tab, index));
            return true;
        } else {
            return false;
        }
    }

    // ListSelectionListener method

    /** previous selected index */
    private int previousIndex = -1;


    public void valueChanged (ListSelectionEvent ev) {
        int indx = getSelectedIndex ();

        if (indx == previousIndex) return;

        previousIndex = indx;

        OutputListener listener = getListenerForPosition(indx);
        if (listener != null) {
            listener.outputLineSelected(new OutputEventImpl(tab, indx));
        }

        checkNextPrevActions();
    }

    // mouse listener methods
    public void mouseClicked (MouseEvent evt) {
        if ((evt.getClickCount() == 2) && ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)) {
            int loc = locationToIndex(evt.getPoint());
            if (! invokeJumpListener(loc)) {
                parseException(loc, true);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if (isPopupTrigger(e)) {
            showPopup(e);
        }
    }


    /** @return true if this event is popup menu trigger... */
    private static boolean isPopupTrigger(MouseEvent evt) {
        return (evt.getModifiers() & (MouseEvent.BUTTON2_MASK | MouseEvent.BUTTON3_MASK)) != 0;
    }

    /** Shows "copy" popup menu */ // NOI18N
    private void showPopup(MouseEvent ev) {
        jPopup.show(this, ev.getX(), ev.getY());
    }

    // focus listener

    public void focusGained(FocusEvent ev) {
        checkCopyAction();
    }
    public void focusLost(FocusEvent ev) {
        // do not do copyAction.setActionPerformer(null);
        // doesn't work with main window
    }

    /** Parses exception or tries to find
    * @param openEditor <tt>true</tt> if an editor should be open
    * @return <tt>true</tt> if the line is recognized as part of exception dump
    */
    private boolean parseException(int loc, boolean openEditor) {

        if (loc < 0) {
            return false;
        }

        String s = ((String) model.getElementAt(loc)).trim();
        Integer locI = new Integer(loc);

        try {
            Object aLine = int2Line.get(locI);
            Line l;
            if (aLine == null) {
                Object[] o = getExceptionFormat().parse(s);

                String all = o[0].toString();
                String file = o[1].toString();
                String ext = o[2].toString();
                int line = Integer.parseInt(o[3].toString());

                int i = all.indexOf(file);
                String path = all.substring(0, i + file.length()).replace('.', '/') + ".java"; // NOI18N

                FileObject fo = TopManager.getDefault().getRepository().findResource(path);
                if (fo == null) {
                    int2Line.put(locI, int2Line);
                    return false;
                }

                DataObject data = DataObject.find(fo);
                EditorCookie cookie = (EditorCookie) data.getCookie(EditorCookie.class);
                if (cookie == null) {
                    int2Line.put(locI, int2Line);
                    return false;
                }
                l = cookie.getLineSet().getOriginal (line - 1);
            } else if (aLine == int2Line) { // already parsed - not soccess
                return false;
            } else {
                l = (Line) aLine;
            }

            if (openEditor) {
                l.show(Line.SHOW_GOTO, 0);
            }
            return true;
            // ignore all
        } catch (java.text.ParseException e) { // MsgForm.parse
        } catch (NumberFormatException e) { // Integer.parseInt
        } catch (DataObjectNotFoundException e) { // DO.find
        } catch (IndexOutOfBoundsException e) { // getLine
        }

        int2Line.put(locI, int2Line);
        return false;
    }

    /** Getter for a MessageFormat that describes a line of exception dump. */
    private static MessageFormat getExceptionFormat() {
        if (formatOfException == null) {
            formatOfException = new MessageFormat(OutputSettings.getString("MSG_Exception_Line")); //at {0}({1}.{2}:{3})
        }
        return formatOfException;
    }

    /** Performer for copy action */
    public void performAction(SystemAction action) {
        doCopy();
    }
    /** Performer for registerKeyboardAction */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource () == copyItem) doCopy();
        else doClear ();
    }

    /** Inserts selected indexes into the clipboard */
    private void doCopy() {
        final Object[] o = model.lines.toArray();

        StringBuffer buff = new StringBuffer(o.length * 25);

        if (o.length >= 1) {
            buff.append(o[0].toString());

            for (int i = 1; i < o.length; i++) {
                buff.append("\n").append(o[i].toString()); // NOI18N
            }
        }

        StringSelection ss = new StringSelection(buff.toString());
        TopManager.getDefault().getClipboard ().setContents (ss, ss);
    }

    private void doClear() {
        model.lines.clear ();
        model.cleaned ();
    }

    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    // key listener waits for enter

    public void keyTyped (KeyEvent e) {
    }

    public void keyPressed (KeyEvent e) {
        if (e.getKeyCode () == KeyEvent.VK_ENTER) {
            e.consume ();

            int indx = getSelectedIndex();

            if (indx < 0) {
                return;
            }

            if (! invokeJumpListener(indx)) {
                parseException(indx, true);
            }
        }
    }

    public void keyReleased (KeyEvent e) {
    }

    static String getTab() {
        int tabSize = outputSettings.getTabSize();
        StringBuffer buf = new StringBuffer(tabSize);
        for (int j = 0; j < tabSize; j++)
            buf.append(" "); // NOI18N
        return buf.toString();
    }

}

/*
 * Log
 *  45   Gandalf   1.44        3/16/00  Martin Ryzl     #5687
 *  44   Gandalf   1.43        3/11/00  Martin Ryzl     menufix [by E.Adams, 
 *       I.Formanek]
 *  43   Gandalf   1.42        3/9/00   Ales Novak      #5687
 *  42   Gandalf   1.41        2/7/00   Ales Novak      #5613
 *  41   Gandalf   1.40        1/18/00  Ales Novak      ALT-F7/F8 - open editor
 *  40   Gandalf   1.39        1/12/00  Ales Novak      i18n
 *  39   Gandalf   1.38        12/30/99 Jaroslav Tulach New dialog for 
 *       notification of exceptions.
 *  38   Gandalf   1.37        12/23/99 Radko Najman    bug 4022
 *  37   Gandalf   1.36        12/17/99 Ales Novak      #4135
 *  36   Gandalf   1.35        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  35   Gandalf   1.34        10/7/99  Ales Novak      next error action works 
 *       even from the java editor
 *  34   Gandalf   1.33        10/6/99  Ian Formanek    Fixed bug 3183 - Output 
 *       Window is not cleared immediatelly after clicking Clear Output.
 *  33   Gandalf   1.32        10/6/99  Jaroslav Tulach ProgressEvent.TASK_CLEANING
 *       
 *  32   Gandalf   1.31        9/28/99  Petr Hamernik   #3980 fixed
 *  31   Gandalf   1.30        9/21/99  Petr Hamernik   #3841 + caching 
 *       rewritten
 *  30   Gandalf   1.29        8/13/99  Ales Novak      a single character on a 
 *       line was not printed - bug
 *  29   Gandalf   1.28        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  28   Gandalf   1.27        8/3/99   Ales Novak      slow redrawing fixed
 *  27   Gandalf   1.26        8/2/99   Petr Hamernik   fixed bug #2599
 *  26   Gandalf   1.25        7/30/99  Ales Novak      race condition
 *  25   Gandalf   1.24        7/30/99  Jaroslav Tulach getOriginal & getCurrent
 *       in LineSet
 *  24   Gandalf   1.23        7/28/99  Ales Novak      bugfix #2826
 *  23   Gandalf   1.22        7/20/99  Ian Formanek    Popup menu on output 
 *       window
 *  22   Gandalf   1.21        7/15/99  Petr Hamernik   optimization
 *  21   Gandalf   1.20        6/22/99  Ales Novak      Copying changed
 *  20   Gandalf   1.19        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  19   Gandalf   1.18        5/20/99  Ales Novak      exception parsing + copy
 *       action
 *  18   Gandalf   1.17        5/6/99   Ales Novak      newlines fix
 *  17   Gandalf   1.16        5/5/99   Ales Novak      scrollbars fixed
 *  16   Gandalf   1.15        4/22/99  Ales Novak      fixed "red" lines from 
 *       compiler
 *  15   Gandalf   1.14        4/9/99   Ales Novak      fix for newlines
 *  14   Gandalf   1.13        4/8/99   Ales Novak      
 *  13   Gandalf   1.12        3/29/99  Jaroslav Tulach Displayes tabs.
 *  12   Gandalf   1.11        3/29/99  Ales Novak      
 *  11   Gandalf   1.10        3/21/99  Jaroslav Tulach Keys.
 *  10   Gandalf   1.9         3/19/99  Jaroslav Tulach 
 *  9    Gandalf   1.8         3/19/99  Jaroslav Tulach 
 *  8    Gandalf   1.7         3/19/99  Jaroslav Tulach 
 *  7    Gandalf   1.6         3/18/99  Jaroslav Tulach 
 *  6    Gandalf   1.5         3/18/99  Jaroslav Tulach println opens the tab
 *  5    Gandalf   1.4         3/17/99  Jaroslav Tulach Output Window fixing.
 *  4    Gandalf   1.3         3/11/99  Ales Novak      removed comments
 *  3    Gandalf   1.2         2/27/99  Jaroslav Tulach Shortcut changed to 
 *       Keymap
 *  2    Gandalf   1.1         2/18/99  Ales Novak      
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.17        --/--/98 Jan Jancura     Tab switching changed
 *  0    Tuborg    0.18        --/--/98 Jaroslav Tulach new syntax behaviour
 *  0    Tuborg    0.19        --/--/98 Petr Hamernik   bugfix
 *  0    Tuborg    0.20        --/--/98 Petr Hamernik   bugfix
 */
