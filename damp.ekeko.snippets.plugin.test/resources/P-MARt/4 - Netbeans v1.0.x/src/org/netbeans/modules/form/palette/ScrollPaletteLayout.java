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

package org.netbeans.modules.form.palette;

import java.awt.LayoutManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.io.Serializable;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JViewport;
import javax.swing.JScrollBar;

/**
 * This class implements a layout manager used by ScrollPalette.
 *
 * @version 1.1, August 28, 1998
 * @author David Peroutka
 */
public class ScrollPaletteLayout extends BorderLayout implements LayoutManager, Serializable
{
    /** The JViewport that displays the palette's contents */
    protected JViewport viewport;
    /** The increasing button */
    protected AbstractButton incButton;
    /** The decreasing button */
    protected AbstractButton decButton;
    /** Layout's orientation (horizontal or vertical) */
    protected int orientation;

    static final long serialVersionUID =-7446287426509147851L;
    /**
     * Creates a palette layout with the specified orientation.
     * @param orientation layout's orientation
     */
    public ScrollPaletteLayout(int orientation) {
        super(4, 4);
        this.orientation = orientation;
    }

    /**
     * Adds the specified component with the specified name to the layout.
     * @param name the component name
     * @param comp the component to be added
     */
    public void addLayoutComponent(String name, Component comp)
    {
        super.addLayoutComponent(name, comp);
        // assign components according the known regions of the layout
        if ("East".equals(name) || "South".equals(name)) // NOI18N
            incButton = (AbstractButton)comp;
        else if ("West".equals(name) || "North".equals(name)) // NOI18N
            decButton = (AbstractButton)comp;
        else if ("Center".equals(name)) // NOI18N
            viewport = (JViewport)comp;
    }

    /**
     * Removes the specified component from the layout.
     * @param comp the component to remove
     */
    public void removeLayoutComponent(Component comp)
    {
        super.removeLayoutComponent(comp);
        // assign components according the known regions of the layout
        if (comp == incButton)
            incButton = null;
        else if (comp == decButton)
            decButton = null;
        else if (comp == viewport)
            viewport = null;
    }

    /**
     * Lays out the container argument using this border layout. 
     * @param   target   the container in which to do the layout.
     */
    public void layoutContainer(Container target) {
        if (viewport != null && incButton != null && decButton !=null)
        {
            Dimension extentSize = viewport.getExtentSize();// getPreferredSize();
            Dimension viewSize = viewport.getViewSize();
            Point viewPosition = viewport.getViewPosition();

            switch (orientation) {
            case JScrollBar.HORIZONTAL:
                boolean state;
                ButtonModel model;
                // increasing button
                model = decButton.getModel();
                state = viewPosition.x > 0 ? true : false;
                model.setEnabled(state);
                if (!model.isRollover())
                    decButton.setVisible(state);
                // decreasing button
                model = incButton.getModel();
                state = viewPosition.x + extentSize.width < viewSize.width ? true : false;
                model.setEnabled(state);
                if (!model.isRollover())
                    incButton.setVisible(state);
                break;
            case JScrollBar.VERTICAL:
                // PENDING(david)
                break;
            }
        }
        super.layoutContainer(target);
    }
}

/*
 * Log
 *  4    Gandalf   1.3         1/5/00   Ian Formanek    NOI18N
 *  3    Gandalf   1.2         11/27/99 Patrik Knakal   
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         3/30/99  Ian Formanek    
 * $
 */
