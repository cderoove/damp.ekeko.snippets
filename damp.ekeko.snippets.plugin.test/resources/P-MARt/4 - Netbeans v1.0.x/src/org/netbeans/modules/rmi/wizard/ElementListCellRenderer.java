/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi.wizard;

import java.awt.Component;
import javax.swing.*;

import org.openide.src.*;

/**
 *
 * @author  mryzl
 */

public class ElementListCellRenderer extends DefaultListCellRenderer {

    ElementFormat ef;

    static final long serialVersionUID =-5230605038321914769L;
    /** Creates new ElementListCellRenderer. */
    public ElementListCellRenderer(ElementFormat ef) {
        super();
        this.ef = ef;
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        if (value instanceof Icon) {
            setIcon((Icon)value);
        } else if (value instanceof Element) {
            setText(ef.format((Element)value));
        } else {
            setText((value == null) ? "" : value.toString());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

        return this;
    }
}

/*
* <<Log>>
*  3    Gandalf   1.2         11/27/99 Patrik Knakal   
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         7/28/99  Martin Ryzl     
* $ 
*/ 
