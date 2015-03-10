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
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import org.openide.TopManager;
import org.openide.loaders.*;
import org.openide.text.EditorSupport;
import org.openide.util.*;
import org.openide.windows.*;
public class ScorePanel extends CloneableTopComponent implements PropertyChangeListener {
    private MultiDataObject.Entry entry;
    private ScoreCookie cookie;
    private ScoreOpenSupport support;
    private Score score;
    private static final long serialVersionUID =7204432764586558961L;
    /** For externalization only! */
    public ScorePanel () {
    }
    public ScorePanel (MultiDataObject.Entry entry) {
        super (entry.getDataObject ());
        this.entry = entry;
        init ();
    }
    protected CloneableTopComponent createClonedObject () {
        return new ScorePanel (entry);
    }
    /**
     * @serialData Super, then the MultiDataObject.Entry to represent. */
    public void writeExternal (ObjectOutput oo) throws IOException {
        super.writeExternal (oo);
        oo.writeObject (entry);
    }
    /**
     * @serialData #see writeExternal */
    public void readExternal (ObjectInput oi) throws IOException, ClassNotFoundException {
        super.readExternal (oi);
        entry = (MultiDataObject.Entry) oi.readObject ();
        init ();
    }
    public void open (Workspace ws) {
        if (ws == null) ws = TopManager.getDefault ().getWindowManager ().getCurrentWorkspace ();
        Mode mode = ws.findMode (EditorSupport.EDITOR_MODE);
        if (mode != null) mode.dockInto (this);
        super.open (ws);
    }
    protected boolean closeLast () {
        if (support == null) {
            System.err.println("WARNING: no ScoreOpenSupport, will just close");
            return true;
        }
        boolean ok = support.canClose ();
        return ok;
    }
    public void propertyChange (PropertyChangeEvent ev) {
        if (DataObject.PROP_MODIFIED.equals (ev.getPropertyName ())) {
            updateName ();
        }
    }
    protected void updateName () {
        DataObject obj = entry.getDataObject ();
        String displayName = obj.getNodeDelegate ().getDisplayName ();
        if (obj.isModified ()) {
            setName (MessageFormat.format (NbBundle.getBundle (ScorePanel.class).getString ("LBL_modified_name"),
                                           new Object[] { displayName }));
        } else {
            setName (displayName);
        }
    }
    private void init () {
        if (! SwingUtilities.isEventDispatchThread ()) {
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                init ();
                                            }
                                        });
            return;
        }
        updateName ();
        entry.getDataObject ().addPropertyChangeListener (WeakListener.propertyChange (this, entry.getDataObject ()));
        setLayout (new BorderLayout ());
        cookie = (ScoreCookie) entry.getDataObject ().getCookie (ScoreCookie.class);
        if (cookie == null) {
            JLabel label = new JLabel (NbBundle.getBundle (ScorePanel.class).getString ("LBL_cannot_load"));
            label.setHorizontalAlignment (SwingConstants.CENTER);
            add (label, BorderLayout.SOUTH);
            return;
        }
        Task t = cookie.prepare ();
        t.addTaskListener (new TaskListener () {
                               public void taskFinished (Task t2) {
                                   SwingUtilities.invokeLater (new Runnable () {
                                                                   public void run () {
                                                                       init2 ();
                                                                   }
                                                               });
                               }
                           });
        JLabel label = new JLabel (NbBundle.getBundle (ScorePanel.class).getString ("LBL_loading"));
        label.setHorizontalAlignment (SwingConstants.CENTER);
        add (label, BorderLayout.SOUTH);
        // Just for safety:
        score = new Score (Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        support = (ScoreOpenSupport) entry.getDataObject ().getCookie (ScoreOpenSupport.class);
    }
    private void init2 () {
        try {
            score = cookie.getScore ();
        } catch (IOException ioe) {
            TopManager.getDefault ().notifyException (ioe);
            return;
        }
        TableColumnModel columns = new DefaultTableColumnModel ();
        TableColumn column = new TableColumn (0, 150, new PulldownRenderer (Score.TONES_LONG), new PulldownEditor (Score.TONES_LONG));
        column.setHeaderValue (NbBundle.getBundle (ScorePanel.class).getString ("LBL_header_tone"));
        columns.addColumn (column);
        column = new TableColumn (1, 150, new PulldownRenderer (Score.OCTAVES_LONG), new PulldownEditor (Score.OCTAVES_LONG));
        column.setHeaderValue (NbBundle.getBundle (ScorePanel.class).getString ("LBL_header_octave"));
        columns.addColumn (column);
        column = new TableColumn (2, 150, new PulldownRenderer (Score.DURATIONS_LONG), new PulldownEditor (Score.DURATIONS_LONG));
        column.setHeaderValue (NbBundle.getBundle (ScorePanel.class).getString ("LBL_header_duration"));
        columns.addColumn (column);
        final JTable table = new JTable (new Model (), columns);
        removeAll ();
        add (new JScrollPane (table), BorderLayout.CENTER);
        JButton add = new JButton (NbBundle.getBundle (ScorePanel.class).getString ("LBL_add_row"));
        add.addActionListener (new ActionListener () {
                                   public void actionPerformed (ActionEvent ev) {
                                       addRow ();
                                   }
                               });
        final JButton del = new JButton (NbBundle.getBundle (ScorePanel.class).getString ("LBL_remove_row"));
        del.setEnabled (false);
        del.addActionListener (new ActionListener () {
                                   public void actionPerformed (ActionEvent ev) {
                                       delRows (table.getSelectedRows ());
                                   }
                               });
        table.getSelectionModel ().addListSelectionListener (new ListSelectionListener () {
                    public void valueChanged (ListSelectionEvent ev) {
                        del.setEnabled (table.getSelectedRows ().length > 0);
                    }
                });
        JPanel buttons = new JPanel ();
        FlowLayout layout = new FlowLayout ();
        layout.setAlignment (FlowLayout.CENTER);
        layout.setHgap (15);
        buttons.setLayout (layout);
        buttons.add (add);
        buttons.add (del);
        add (buttons, BorderLayout.SOUTH);
        revalidate ();
    }
    private void addRow () {
        int size = score.getSize ();
        List tones = new ArrayList (size + 1);
        List octaves = new ArrayList (size + 1);
        List durations = new ArrayList (size + 1);
        for (int i = 0; i < size; i++) {
            tones.add (new Integer (score.getTone (i)));
            octaves.add (new Integer (score.getOctave (i)));
            durations.add (new Integer (score.getDuration (i)));
        }
        tones.add (new Integer (Score.DEFAULT_TONE));
        octaves.add (new Integer (Score.DEFAULT_OCTAVE));
        durations.add (new Integer (Score.DEFAULT_DURATION));
        try {
            cookie.setScore (new Score (tones, octaves, durations));
        } catch (IOException ioe) {
            TopManager.getDefault ().notifyException (ioe);
        }
    }
    private void delRows (int[] rows) {
        int size = score.getSize ();
        int size2 = size - rows.length;
        List tones = new ArrayList (size2);
        List octaves = new ArrayList (size2);
        List durations = new ArrayList (size2);
KEEPROW:
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < rows.length; j++)
                if (rows[j] == i)
                    continue KEEPROW;
            tones.add (new Integer (score.getTone (i)));
            octaves.add (new Integer (score.getOctave (i)));
            durations.add (new Integer (score.getDuration (i)));
        }
        try {
            cookie.setScore (new Score (tones, octaves, durations));
        } catch (IOException ioe) {
            TopManager.getDefault ().notifyException (ioe);
        }
    }
    private class Model extends AbstractTableModel implements ChangeListener {
        public Model () {
            cookie.addChangeListener (WeakListener.change (this, cookie));
        }
        public void stateChanged (ChangeEvent ev) {
            try {
                score = cookie.getScore ();
                fireTableDataChanged ();
            } catch (IOException ioe) {
                // keep previous Score object instead
                TopManager.getDefault ().notifyException (ioe);
            }
        }
        public boolean isCellEditable (int row, int col) {
            return true;
        }
        public int getRowCount () {
            return score.getSize ();
        }
        public int getColumnCount () {
            return 3;
        }
        public Object getValueAt (int row, int column) {
            if (column == 0)
                return new Integer (score.getTone (row));
            else if (column == 1)
                return new Integer (score.getOctave (row));
            else if (column == 2)
                return new Integer (score.getDuration (row));
            else
                throw new ArrayIndexOutOfBoundsException ();
        }
        public void setValueAt (Object val, int row, int col) {
            if (val.equals (getValueAt (row, col))) {
                return;
            }
            int size = score.getSize ();
            List tones = new ArrayList (size);
            List octaves = new ArrayList (size);
            List durations = new ArrayList (size);
            for (int i = 0; i < size; i++) {
                Object tone;
                if (i == row && col == 0)
                    tone = val;
                else
                    tone = new Integer (score.getTone (i));
                tones.add (tone);
                Object octave;
                if (i == row && col == 1)
                    octave = val;
                else
                    octave = new Integer (score.getOctave (i));
                octaves.add (octave);
                Object duration;
                if (i == row && col == 2)
                    duration = val;
                else
                    duration = new Integer (score.getDuration (i));
                durations.add (duration);
            }
            Score nue = new Score (tones, octaves, durations);
            try {
                cookie.setScore (nue);
            } catch (IOException ioe) {
                TopManager.getDefault ().notifyException (ioe);
            }
        }
    }
    private static class PulldownRenderer extends DefaultTableCellRenderer {
        private String[] tokens;
        public PulldownRenderer (String[] tokens) {
            this.tokens = tokens;
        }
        protected void setValue (Object o) {
            int index = ((Integer) o).intValue ();
            setText (tokens[index]);
        }
    }
    private static class PulldownEditor extends DefaultCellEditor {
        public PulldownEditor (String[] tokens) {
            super (makeComboBox (tokens));
        }
        private static JComboBox makeComboBox (final String[] tokens) {
            Object[] list = new Object[tokens.length];
            for (int i = 0; i < list.length; i++)
                list[i] = new Integer (i);
            JComboBox combo = new JComboBox (list);
            combo.setRenderer (new DefaultListCellRenderer () {
                                   public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                                       int index2 = ((Integer) value).intValue ();
                                       return super.getListCellRendererComponent (list, tokens[index2], index, isSelected, cellHasFocus);
                                   }
                               });
            return combo;
        }
    }
}
