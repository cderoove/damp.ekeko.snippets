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

package org.openide.awt;

import javax.swing.*;

/**
 * An implementation of a toolbar button.
 */
public class ToolbarButton extends JButton {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 6564434578524381134L;

    /** Creates a button with an icon.
    *
    * @param icon  the Icon image to display on the button
    */
    public ToolbarButton(Icon icon) {
        super(null, icon);
        setModel(new EnabledButtonModel());
        setMargin(new java.awt.Insets(2, 1, 0, 1));
    }

    /** Notification from the UIFactory that the L&F has changed.
    *
    * @see JComponent#updateUI
    */
    public void updateUI() {
        setUI(UIManager.getLookAndFeel().getName().equals("Windows") ? // NOI18N
              ToolbarButtonUI.createUI(this) : UIManager.getUI(this));
    }

    /** Identifies whether or not this component can receive the focus.
    * A disabled button, for example, would return false.
    * @return true if this component can receive the focus
    */
    public boolean isFocusTraversable() {
        return false;
    }
}

/*
 * Log
 *  7    Gandalf   1.6         1/12/00  Ian Formanek    NOI18N
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         4/16/99  Jan Jancura     Object Browser support
 *  3    Gandalf   1.2         3/10/99  Jaroslav Tulach 
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
