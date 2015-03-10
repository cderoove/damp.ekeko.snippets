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

package org.netbeans.modules.form.compat2.border;

import java.awt.*;

import javax.swing.border.Border;

/** An abstract superclass of description of
* @author   Petr Hamernik
*/
public class DesignBorder extends Object implements Border, java.io.Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5493077508687414472L;

    BorderInfo bInfo;

    public DesignBorder(BorderInfo bInfo) {
        this.bInfo = bInfo;
    }

    public BorderInfo getInfo() {
        return bInfo;
    }

    /**
     * Paints the border for the specified component with the specified 
     * position and size.
     * @param c the component for which this border is being painted
     * @param g the paint graphics
     * @param x the x position of the painted border
     * @param y the y position of the painted border
     * @param width the width of the painted border
     * @param height the height of the painted border
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        bInfo.getBorder().paintBorder(c, g, x, y, width, height);
    }

    /**
     * Returns the insets of the border.  
     * @param c the component for which this border insets value applies
     */
    public Insets getBorderInsets(Component c) {
        return bInfo.getBorder().getBorderInsets(c);
    }

    /**
     * Returns whether or not the border is opaque.  If the border
     * is opaque, it is responsible for filling in it's own
     * background when painting.
     */
    public boolean isBorderOpaque() {
        return bInfo.getBorder().isBorderOpaque();
    }
}

/*
 * Log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         4/2/99   Ian Formanek    
 * $
 */
