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

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.ObjectInput;
import java.io.IOException;
import java.util.*;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.border.Border;

import org.openide.awt.SplittedPanel;
import org.openide.awt.MouseUtils;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.*;

import org.netbeans.core.windows.WellKnownModeNames;

/* TODO:
- setOutputVisible
- err editor init only if err separated
- ??? known bug: exception on setInputVisible(false)
*/

/** One Output window's tab. Can contain StdOut and ErrOut "editors" and StdIn input line.
* Initially, it contains just the StdOut "editor".
* This class is final only for performance reasons. Can be unfinaled
* happily.
*
* @author Petr Hamernik, Ian Formanek, Jan Jancura
*/
public final class OutputTab extends TopComponent
    implements InputOutput, java.io.Externalizable, PropertyChangeListener, WellKnownModeNames {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 9220615777783507203L;

    public static final int DEFAULT_WINDOW_HEIGHT = 180;
    public static final int TYPICAL_WINDOWS_TASKBAR_HEIGHT = 27;

    public static final String ICON_RESOURCE =
        "/org/netbeans/core/resources/frames/output.gif"; // NOI18N

    /** Mapping of string:OutputTab */
    static WeakHashMap ioCache = new WeakHashMap(7);

    private SplittedPanel splittedInside;
    private int splitTypeBackup = SplittedPanel.HORIZONTAL;

    /** If true, the error output is separated from the normal output */
    private boolean errSeparated;
    private boolean inVisible;
    private boolean errVisible;

    /** Should this InputOutput take the focus and bring the window to the front,
    * when anything is written into the stream. */
    private boolean focusTaken = false;

    /** The reader for standard input */
    private java.io.Reader inReader;
    private JTextField inputLine;
    private JLabel inputLineDesc;
    private JPanel inputPanel;

    /** Output pane */
    private OutPane output;
    /** Error pane */
    private OutPane error;

    /** am I activated */
    private boolean amISelected;

    /** piped writer */
    private PipedWriter inWriter = new PipedWriter();

    /** Creates an empty Output window with only the std output pane and with
    * reference to its pane.
    */
    public OutputTab()  {
        this(""); // NOI18N
    }

    /** Creates an empty Output window with only the std output pane */
    public OutputTab(String name)  {
        synchronized (OutputTab.class) {
            setName (name);
            setActivatedNodes (null);

            errSeparated = false;
            setLayout(new BorderLayout());
            splittedInside = new SplittedPanel();
            add (splittedInside, "Center"); // NOI18N

            output = new OutPane(this);
            error = new OutPane(this);

            JScrollPane opane = new JScrollPane(output);
            JScrollPane errpane =  new JScrollPane(error);

            splittedInside.add(opane, SplittedPanel.ADD_FIRST);
            splittedInside.add(errpane, SplittedPanel.ADD_SECOND);
            errVisible = false;
            splittedInside.setSplitType(SplittedPanel.NONE);

            try {
                inReader = new OutputPipedReader(inWriter);
            } catch (java.io.IOException ex) {
                TopManager.getDefault().notifyException(ex);
            }

            inputLine = new JTextField();
            inputLine.addActionListener(new ActionListener() {
                                            public void actionPerformed(ActionEvent e) {
                                                try {
                                                    inWriter.write(inputLine.getText()+"\n"); // NOI18N
                                                    inWriter.flush();
                                                } catch (java.io.IOException ex) {
                                                    // TopManager.getDefault().notifyException(ex);
                                                }
                                                inputLine.setText(""); // NOI18N
                                            }
                                        });

            inputLineDesc = new JLabel(OutputSettings.getString("CTL_ProgramInput"));
            inputLineDesc.setForeground(java.awt.Color.black);

            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new GridBagLayout());

            inputPanel = new JPanel();
            inputPanel.setLayout(new GridBagLayout());

            GridBagConstraints gd = new GridBagConstraints();
            gd.anchor = GridBagConstraints.WEST;
            gd.fill = GridBagConstraints.NONE;
            gd.gridwidth = 1;
            gd.gridheight = 1;
            gd.gridx = 0;
            gd.gridy = 0;
            gd.weightx = 0;
            gd.weighty = 1;
            inputPanel.add(labelPanel, gd);

            gd = new GridBagConstraints();
            gd.anchor = GridBagConstraints.WEST;
            gd.fill = GridBagConstraints.NONE;
            gd.gridwidth = 1;
            gd.gridheight = 1;
            gd.gridx = 0;
            gd.gridy = 0;
            gd.weightx = 0;
            gd.weighty = 1;
            gd.insets = new java.awt.Insets(0, 8, 0, 8);
            labelPanel.add(inputLineDesc, gd);

            gd = new GridBagConstraints();
            gd.anchor = GridBagConstraints.WEST;
            gd.fill = GridBagConstraints.HORIZONTAL;
            gd.gridwidth = 2;
            gd.gridheight = 1;
            gd.gridx = 1;
            gd.gridy = 0;
            gd.weightx = 1;
            gd.weighty = 1;
            inputPanel.add(inputLine, gd);

            inVisible = false;

            OutputSettings set = (OutputSettings)OutputSettings.findObject (OutputSettings.class, true);
            set.addPropertyChangeListener (this);

            ioCache.put(name, this);
        }
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (OutputTab.class);
    }

    /** Activates the component */
    protected void componentActivated () {
        amISelected = true;
        inputLine.requestFocus();
    }

    /** Deactivates the component */
    protected void componentDeactivated () {
        amISelected = false;
    }

    /** Listener for changes of the property.
    */
    public void propertyChange (PropertyChangeEvent ev) {
        textPropertiesChanged();
        invalidate ();
    }

    /** A class that works same as PipedReader and in advance shows the InputLine
    * on the read request
    */
    class OutputPipedReader extends PipedReader {
        OutputPipedReader(PipedWriter writer) throws java.io.IOException {
            super(writer);
        }

        public int read(char cbuf[], int off, int len) throws java.io.IOException {
            setInputVisible(true);
            return super.read(cbuf, off, len);
        }

        public int read() throws java.io.IOException {
            setInputVisible(true);
            return super.read();
        }

    }

    /** Method to acquire an OutputWriter for accessing an output pane of the tab.
    * @return an OutputWriter for accessing an output pane of the tab.
    */
    public OutputWriter getOut() {
        return output.writer;
    }

    /** Method to acquire an error output pane of the tab.
    * @return an error output pane of the tab.
    */
    public OutputWriter getErr() {
        if (errSeparated)
            return error.writer;
        else return output.writer;
    }

    /** Method to acquire a Reader for accessing an input line of the tab.
    * On a first try to read from the Reader, an input line should be
    * added to this tab in the OutputWindow if it does not exist yet.
    * @return a Reader for accessing an input line of the tab.
    */
    public java.io.Reader getIn() {
        return inReader;
    }

    /** Closes I/O
    */
    public void closeInputOutput () {
        Runnable run = new Runnable() {
                           public void run() {
                               close();
                           }
                       };
        java.awt.EventQueue.invokeLater(run);
        try {
            inWriter.flush();
            inWriter.close();
        } catch (java.io.IOException ex) {
        }
    }

    /** checks whether tab is closed or not */
    public boolean isClosed() {
        return true;
    }

    /** Shows or hides the std output pane.
    * @param value the new visibility state of the std output "editor"
    */
    public void setOutputVisible(boolean value) {
        // [PENDING]
    }

    /** Shows or hides the err output pane (regardless the error output
    * is separated or not).
    * @param value the new visibility state of the err output "editor"
    */
    public void setErrVisible(boolean value) {
        if ((errVisible == value) || (!errSeparated))
            return;

        errVisible = value;
        if (errVisible)
            splittedInside.setSplitType(splitTypeBackup);
        else {
            splitTypeBackup = splittedInside.getSplitType();
            if (splitTypeBackup == SplittedPanel.NONE)
                splitTypeBackup = SplittedPanel.HORIZONTAL;
            splittedInside.setSplitType(SplittedPanel.NONE);
        }

        invalidate ();
        Container parent = getParent ();
        if (parent != null) parent.validate ();
    }

    /** Shows or hides the input line
    * @param value the new visibility state of the input line
    */
    public void setInputVisible(boolean value) {
        if (inVisible == value)
            return;
        inVisible = value;
        if (inVisible) {
            add (inputPanel, "South"); // NOI18N
            if (isShowing () && amISelected) {
                // select focus
                inputLine.requestFocus ();
            }
            if (isClosed() && isFocusTaken()) {
                open();
            }
        } else {
            remove(inputPanel);
        }
        inputPanel.invalidate();
        inputLine.invalidate();
        inputLineDesc.invalidate();
        Container parent = getParent ();
        if (parent != null) parent.validate ();
    }

    public boolean isErrSeparated() {
        return errSeparated;
    }

    public void setErrSeparated(boolean value) {
        if (errSeparated == value)
            return;

        errSeparated = value;
        if (!errSeparated)
            setErrVisible(false);
    }

    /** @return true if output window takes focus, when anything is written. */
    public boolean isFocusTaken() {
        return focusTaken;
    }

    /** Set true, if you want to focus out window, when anything is written
    * into the stream */
    public void setFocusTaken(boolean value) {
        focusTaken = value;
    }

    /**
    * Sets this input / output visible. In the implementation where InputOutput
    * is representated by one tab in TabbedPane selects this pane for example.
    */
    public void select () {
        // [JST] hopefully there should be true and not focusTaken
        requestFocus ();
        inputLine.requestFocus();
    }

    /** Request focus for the output pane
    */
    public void requestFocus () {
        super.requestFocus ();
        output.requestFocus ();
    }

    /** always open this top component in output mode, if
    * no mode for this component is specified yet */
    public void open (Workspace workspace) {
        Workspace realWorkspace = (workspace == null)
                                  ? TopManager.getDefault().getWindowManager().getCurrentWorkspace()
                                  : workspace;
        // dock into outwin mode if not docked yet
        Mode mode = realWorkspace.findMode(OUTPUT);
        if (mode == null) {
            mode = realWorkspace.createMode(OUTPUT, getOutDisplayName(),
                                            OutputTab.class.getResource(ICON_RESOURCE));
        }
        Mode tcMode = realWorkspace.findMode(this);
        if (tcMode == null)
            mode.dockInto(this);
        // behave like superclass
        super.open(workspace);
    }

    /** @return true if the requestor is out (not err) and this tab is selected */
    boolean amISelected(OutPane requestor) {
        return (requestor == output) && amISelected;
    }

    /** Checks if the Next- and Prev-OutJumpActions should be enabled or disabled.
    */
    void checkNextPrevActions() {
        output.checkNextPrevActions();
    }

    /** Notification about the change in output colors or font */
    void textPropertiesChanged() {
        output.repaint ();
        error.repaint ();
    }

    /** rebinds this tab to ow */
    void rebindTab() {
        open ();
    }

    /** Creates new instance into inWriter because of pending data in the old one
    * @return flushed Reader
    */
    public final java.io.Reader flushReader() {
        inWriter = new PipedWriter();
        try {
            inReader = new OutputPipedReader(inWriter);
        } catch (java.io.IOException ex) {
            TopManager.getDefault().notifyException(ex);
            return null;
        }
        return inReader;
    }

    // OutputTabs are not serialized
    // null is returned during deserialization
    // only standard output tab is dseserialized to
    // default instance

    public synchronized Object writeReplace() throws java.io.ObjectStreamException {
        if (replace == null) {
            replace = new Replace(this.equals(panel));
        }
        return replace;
    }

    private Replace replace;

    /** This class is serializaed instead of Output tab */
    static class Replace implements java.io.Serializable {
        /** true if output tab was default one, false otherwise */
        boolean defaultInstance;

        static final long serialVersionUID =-7661588925874641544L;
        public Replace (boolean defaultInstance) {
            this.defaultInstance = defaultInstance;
        }

        /** Resolve as default singleton or null */
        public Object readResolve() throws java.io.ObjectStreamException {
            return defaultInstance ? getStdOutputTab() : null;
        }
    }

    //
    // Static stuff
    //

    /** sinngleton instance of the standard output tab */
    private static OutputTab panel;

    private static synchronized void initialize () {
        if (panel == null) {
            // create the tab for StdOut
            panel = new OutputTab(NbBundle.getBundle(OutputTab.class).getString("CTL_OutputWindow_OutputTab"));
        }
    }

    public static InputOutput getIO(String name, boolean newIO) {
        initialize ();
        if (newIO) {
            return new OutputTab(name);
        } else {
            InputOutput ino = (InputOutput) ioCache.get(name);
            if (ino == null) {
                ino = new OutputTab(name);
            }
            return ino;
        }
    }

    public static OutputWriter getStdOut() {
        initialize ();
        return panel.getOut();
    }

    /** Returns standard output top component */
    public static OutputTab getStdOutputTab() {
        initialize ();
        return panel;
    }

    static String getOutDisplayName() {
        return org.openide.util.NbBundle.getBundle("org.netbeans.core.windows.Bundle").getString("CTL_OutputWindow");
    }

}

/*
 * Log
 *  35   Gandalf   1.34        2/17/00  Ian Formanek    Minor UI improvement - 
 *       got rid of BevelBorder for "Program Input" label in the output window
 *  34   Gandalf   1.33        2/15/00  David Simonek   open method updated
 *  33   Gandalf   1.32        1/12/00  Ales Novak      i18n
 *  32   Gandalf   1.31        1/12/00  Ales Novak      open() discarded
 *  31   Gandalf   1.30        1/3/00   Radko Najman    New property value 
 *       applied immediately
 *  30   Gandalf   1.29        12/15/99 Ales Novak      program input made 
 *       visible according to Kartik Mithail
 *  29   Gandalf   1.28        12/3/99  Jaroslav Tulach no selected nodes.
 *  28   Gandalf   1.27        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  27   Gandalf   1.26        8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  26   Gandalf   1.25        7/29/99  David Simonek   further ws serialization
 *       changes
 *  25   Gandalf   1.24        7/22/99  Ales Novak      caching of InputOutputs
 *  24   Gandalf   1.23        7/19/99  Jesse Glick     Context help.
 *  23   Gandalf   1.22        7/14/99  Ales Novak      sync bug
 *  22   Gandalf   1.21        7/14/99  Ales Novak      new win sys
 *  21   Gandalf   1.20        7/12/99  Jesse Glick     Context help.
 *  20   Gandalf   1.19        7/11/99  David Simonek   window system change...
 *  19   Gandalf   1.18        6/18/99  Ales Novak      IOException catched and 
 *       quietly discarded
 *  18   Gandalf   1.17        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  17   Gandalf   1.16        5/13/99  Ales Novak      VERTICAL_SCROLLBAR_ALWAYS
 *        removed
 *  16   Gandalf   1.15        5/12/99  Ales Novak      serialization
 *  15   Gandalf   1.14        5/11/99  David Simonek   changes to made window 
 *       system correctly serializable
 *  14   Gandalf   1.13        5/5/99   Ales Novak      scrollbars fixed
 *  13   Gandalf   1.12        5/2/99   Ian Formanek    Default output window 
 *       height raised from 150 to 180.
 *  12   Gandalf   1.11        4/9/99   Ales Novak      output was not shown 
 *       even if reading
 *  11   Gandalf   1.10        4/9/99   Ales Novak      
 *  10   Gandalf   1.9         3/25/99  David Simonek   changes in window 
 *       system, initial positions, bugfixes
 *  9    Gandalf   1.8         3/19/99  Jaroslav Tulach Deleted OutWindow
 *  8    Gandalf   1.7         3/19/99  Ian Formanek    Added default bounds
 *  7    Gandalf   1.6         3/19/99  David Simonek   
 *  6    Gandalf   1.5         3/17/99  David Simonek   slightly changed window 
 *       system
 *  5    Gandalf   1.4         3/12/99  David Simonek   default mode set to 
 *       OUTPUT
 *  4    Gandalf   1.3         3/12/99  Jan Jancura     
 *  3    Gandalf   1.2         1/6/99   Jaroslav Tulach 
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.20        --/--/98 Petr Hamernik   Completly redesigned whole package (switch to new editor)
 *  0    Tuborg    0.21        --/--/98 Petr Hamernik   Stealing of focus is optional
 *  0    Tuborg    0.22        --/--/98 Petr Hamernik   colors
 *  0    Tuborg    0.24        --/--/98 Jan Jancura     Tab switching changed
 *  0    Tuborg    0.25        --/--/98 Ales Novak      bugfix
 *  0    Tuborg    0.26        --/--/98 Jaroslav Tulach select calls setOut (this, true)
 *  0    Tuborg    0.27        --/--/98 Jaroslav Tulach requestFocus
 */
