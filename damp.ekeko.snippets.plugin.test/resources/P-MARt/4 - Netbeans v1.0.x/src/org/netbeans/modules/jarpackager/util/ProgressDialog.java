/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jarpackager.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/** Dialog which can be used as an indicator of the progress long actions.
*
* @author Petr Hamernik
*/
public class ProgressDialog extends JDialog {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 6292164837837186498L;
    /** Label - the text info */
    JLabel label;
    /** Progress bar */
    JProgressBar bar;
    /** Panel containing bar and label */
    JPanel progressPanel;
    /** Dimensions of the panel */
    Dimension panelSize;

    /** Creates new progress dialog with specified owner */
    public ProgressDialog (Frame owner, String title, int min, int max) {
        super(owner);
        initialize(title, min, max);
    }

    /** Creates new progress dialog */
    public ProgressDialog (String title, int min, int max) {
        super();
        initialize(title, min, max);
    }

    /** Initialization, called from constructors */
    private void initialize (String title, int min, int max) {
        setDefaultCloseOperation (javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
        setTitle(title);
        progressPanel = new JPanel();
        progressPanel.setLayout(new BorderLayout(5, 5));
        progressPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        label = new JLabel("                           "); // NOI18N
        progressPanel.add(label, BorderLayout.NORTH);
        bar = new JProgressBar();
        bar.setMinimum(min);
        bar.setMaximum(max);
        bar.setValue(0);
        progressPanel.add(bar, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(progressPanel, BorderLayout.CENTER);
        pack();
        center();
    }

    /** Sets the new label in the progress bar */
    public void setLabel(String text) {
        label.setText(text);
        if (panelSize == null)
            panelSize = progressPanel.getSize();
        label.paintImmediately(0, 0, panelSize.width, panelSize.height);
    }

    /** Sets new value of progress bar. */
    public void setValue(final int newValue) {
        try {
            bar.setValue(newValue);
            bar.paintImmediately(0, 0, bar.getSize().width,
                                 bar.getSize().height);
        }
        catch (IllegalArgumentException e) {
        }
    }

    /** Places dialog into the center of the screen
    */
    public void center() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dialogSize = getSize();
        setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);
    }

    /** Increments value by 1 */
    public void inc() {
        setValue(bar.getValue() + 1);
    }

    /** @return preferred size of the dialog */
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = Math.max(d.width, 300);
        d.height = Math.max(d.height, 100);
        return d;
    }

    public void setVisible (boolean visible) {
        super.setVisible(visible);
        panelSize = progressPanel.getSize();
        progressPanel.paintImmediately(0, 0, panelSize.width, panelSize.height);
    }
}

/*
 * <<Log>>
 *  5    Gandalf   1.4         1/25/00  David Simonek   Various bugfixes and 
 *       i18n
 *  4    Gandalf   1.3         1/16/00  David Simonek   i18n
 *  3    Gandalf   1.2         12/7/99  David Simonek   
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         10/4/99  David Simonek   
 * $
 */
