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

package org.openide.explorer.propertysheet;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.event.*;
import java.beans.PropertyEditor;
import java.util.Vector;

/**
* This lightweight component encapsulates calling of propertyEditor.paintValue
* (Graphics g, Rectangle r) method in special Component.
*
* @author   Jan Jancura
* @version  0.15, May 13, 1997
*/
class PropertyShow extends javax.swing.JPanel {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8663903931982719530L;

    /** Preferred size of this button. */
    //  private Dimension preferredSize = null;

    /** Link to the property editor */
    private PropertyEditor propertyEditor;

    /** Standart helper variable. 
     * @associates SheetButtonListener*/
    private Vector listeners = new Vector (1,5);

    /**
    * Constructs new PropertyShow for specified PropertyEditor.
    *
    * @param PropertyEditor aPropertyEditor proper property editor
    */
    public PropertyShow (PropertyEditor aPropertyEditor) {
        propertyEditor = aPropertyEditor;
        setDoubleBuffered (false);
        setOpaque (true);
    }

    /**
    * Sets value of showen property.
    */
    public void setValue (Object newValue) {
        propertyEditor.setValue (newValue);
    }

    /**
    * Standart method for painting component.
    */
    public void paintComponent (Graphics g) {
        Dimension sz = getSize();
        Color color = g.getColor();
        g.setColor(getBackground());
        g.fillRect(0, 0, sz.width, sz.height);
        g.setColor(color);

        propertyEditor.paintValue (g, new Rectangle(sz));
    }

    /**
    * Standart helper method.
    */
    void fireSheetButtonClicked (ActionEvent e) {
        Vector l = (Vector)listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            ((SheetButtonListener)l.elementAt (i)).sheetButtonClicked (e);
    }

    public void addSheetButtonListener (SheetButtonListener sheetListener) {
        listeners.addElement (sheetListener);
    }

    public void removeSheetButtonListener (SheetButtonListener sheetListener) {
        listeners.removeElement (sheetListener);
    }
}


/*
 * Log
 *  5    Gandalf-post-FCS1.3.1.0     4/5/00   Tran Duc Trung  FIX: 
 *       PropertyShow.paintComponent() does not clear background, garbage from 
 *       previous draw is left over
 *  4    Gandalf   1.3         1/19/00  Jan Jancura     Cycling while repainting
 *       solved.
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.14        --/--/98 Jaroslav Tulach Changed not to be public
 */
