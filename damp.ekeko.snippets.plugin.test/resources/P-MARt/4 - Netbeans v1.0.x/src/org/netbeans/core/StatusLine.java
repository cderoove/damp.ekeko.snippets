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

import java.awt.Component;
import java.awt.Dimension;
import java.beans.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;

import org.openide.util.WeakListener;

/** The status line component of the main window. A text can be put into it.
*
* @author Jaroslav Tulach
*/
public class StatusLine extends JLabel implements PropertyChangeListener {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -5644391883356409841L;
    /** all registered components */
    private static PropertyChangeSupport supp = new PropertyChangeSupport (Object.class);
    /** the current value */
    private static String value = " "; // to get reasonable preferred size // NOI18N

    /** Creates a new StatusLine with specified workspace switcher. */
    public StatusLine () {
        super (value);
        supp.addPropertyChangeListener (WeakListener.propertyChange (this, supp));
    }

    /** Listens to changes.
    */
    public void propertyChange (PropertyChangeEvent ev) {
        setText(value);
    }

    public static JComponent createLabel () {
        JComponent statusPanel = new JPanel();
        StatusLine statusText = new StatusLine ();
        statusPanel.setBorder(statusText.new StatusBorder(BevelBorder.LOWERED));
        statusPanel.setLayout(new java.awt.BorderLayout());
        statusPanel.add(statusText, "Center"); // NOI18N
        return statusPanel;
    }


    /** Displays specified text in the status line
    * @param text The text to be displayed
    */
    public static void setStatusText (String text) {
        if ((text == null) || "".equals (text)) // NOI18N
            text = " "; // to get reasonable preferred size // NOI18N
        value = text;
        supp.firePropertyChange (null, null, null);
    }

    /** Prefered size.
    */
    public Dimension getPreferredSize () {
        Dimension d = super.getPreferredSize ();
        d.width = 1024;
        return d;
    }


    private class StatusBorder extends BevelBorder {
        static final long serialVersionUID =607114083584589974L;
        /** Constructs a new StstusBorder with specified type (from BevelBorder) */
        StatusBorder(int type) {
            super(type);
        }

        /** Returns the insets of the border.
        * @param c the component for which this border insets value applies
        */
        public java.awt.Insets getBorderInsets(java.awt.Component c) {
            return new java.awt.Insets(2, 8, 2, 2);
        }

        /** Returns the outer highlight color of the bevel border. */
        public java.awt.Color getHighlightOuterColor(java.awt.Component c) {
            return c.getBackground();
        }

        /** Returns the outer shadow color of the bevel border. */
        public java.awt.Color getShadowOuterColor(java.awt.Component c) {
            return c.getBackground();
        }
    }
}

/*
 * Log
 *  13   Gandalf   1.12        1/13/00  Jaroslav Tulach I18N
 *  12   Gandalf   1.11        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  11   Gandalf   1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  9    Gandalf   1.8         8/1/99   Ian Formanek    Improved StatusLine 
 *       border
 *  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         3/27/99  Jaroslav Tulach 
 *  6    Gandalf   1.5         3/19/99  Jaroslav Tulach 
 *  5    Gandalf   1.4         3/9/99   Ian Formanek    
 *  4    Gandalf   1.3         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  3    Gandalf   1.2         2/11/99  Jaroslav Tulach StatusLine separated 
 *       from DesktopSwitcher
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    JLabel temporarily replaced by Label to improve performance
 *  0    Tuborg    0.13        --/--/98 Jan Formanek    JLabel is back
 *  0    Tuborg    0.15        --/--/98 Jan Formanek    border around statusLine
 *  0    Tuborg    0.16        --/--/98 Jan Formanek    setting status text now invokes immediate repaint
 */
