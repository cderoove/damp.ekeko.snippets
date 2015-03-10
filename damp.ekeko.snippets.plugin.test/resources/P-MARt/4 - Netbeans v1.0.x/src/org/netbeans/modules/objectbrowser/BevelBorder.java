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

package org.netbeans.modules.objectbrowser;

import java.awt.*;

/**
 * Single line bevel border.
 */
public class BevelBorder extends javax.swing.border.AbstractBorder {
    /** Raised bevel type. */
    public static final int RAISED  = 0;
    /** Lowered bevel type. */
    public static final int LOWERED = 1;

    protected int bevelType;

    static final long serialVersionUID =-7306181339013614421L;
    /**
    * Creates a bevel border.
    */
    public BevelBorder (int bevelType) {
        this.bevelType = bevelType;
    }

    /**
    * Paints the border.
    */
    public void paintBorder (Component c, Graphics g, int x, int y, int w, int h) {
        if (bevelType == RAISED)
            paintR (c, g, x, y, w, h);
        else
            if (bevelType == LOWERED)
                paintL (c, g, x, y, w, h);
    }

    /**
    * Returns the insets of the border.
    */
    public Insets getBorderInsets (Component c) {
        return new Insets (1, 1, 1, 1);
    }

    /**
    * Returns true.
    */
    public boolean isBorderOpaque () {
        return true;
    }

    protected void paintR (
        Component c,
        Graphics g,
        int x,
        int y,
        int w,
        int h
    ) {
        Color oldColor = g.getColor ();
        g.translate (x, y);

        g.setColor (c.getBackground ().brighter ());
        g.drawLine (0, 0, 0, h-1);
        g.drawLine (1, 0, w-1, 0);

        g.setColor (c.getBackground ().darker ());
        g.drawLine (1, h-1, w-1, h-1);
        g.drawLine (w-1, 1, w-1, h-2);

        g.translate (-x, -y);
        g.setColor (oldColor);
    }

    protected void paintL (
        Component c,
        Graphics g,
        int x,
        int y,
        int w,
        int h
    ) {
        Color oldColor = g.getColor ();
        g.translate (x, y);

        g.setColor (c.getBackground ().darker ());
        g.drawLine (0, 0, 0, h-1);
        g.drawLine (1, 0, w-1, 0);
        g.setColor (c.getBackground ().brighter ());
        g.drawLine (1, h-1, w-1, h-1);
        g.drawLine (w-1, 1, w-1, h-2);

        g.translate (-x, -y);
        g.setColor (oldColor);
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         5/6/99   Jan Jancura     
 * $
 */
