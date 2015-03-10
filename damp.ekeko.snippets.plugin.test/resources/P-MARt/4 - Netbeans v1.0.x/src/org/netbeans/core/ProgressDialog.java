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

package org.netbeans.core;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/** Dialog which can be used as an indicator of the progress long actions.
*
* @author Petr Hamernik
* @version
*/
public class ProgressDialog extends JDialog {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 6292164837837186498L;
    /** Label - the text info */
    JLabel label;

    /** Progress bar */
    JProgressBar bar;

    /** Creates new progress dialog */
    public ProgressDialog(String title, int min, int max) {
        setDefaultCloseOperation (javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
        setTitle(title);
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BorderLayout(5, 5));
        progressPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        label = new JLabel(""); // NOI18N
        progressPanel.add(label, "North"); // NOI18N
        bar = new JProgressBar();
        bar.setMinimum(min);
        bar.setMaximum(max);
        bar.setValue(0);
        progressPanel.add(bar, "South"); // NOI18N

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(progressPanel, "Center"); // NOI18N
        pack();
        center();
    }

    /** Sets the new label in the progress bar */
    public void setLabel(String text) {
        label.setText(text);
        label.invalidate();
        validate();
    }

    /** Sets new value of progress bar. */
    public void setValue(int newValue) {
        try {
            bar.setValue(newValue);
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
}

/*
 * Log
 *  3    Gandalf   1.2         1/13/00  Jaroslav Tulach I18N
 *  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
