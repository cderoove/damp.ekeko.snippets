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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This class implements a panel used by ScrollPalette.
 *
 * @version 1.1, Sep 3, 1998
 * @author David Peroutka
 */
public class ScrollPalettePanel extends JPanel implements Scrollable {
    static JFrame frame;

    static final long serialVersionUID =-5756697655004401780L;
    public ScrollPalettePanel() {
        // PENDING(david) ScrollPalettePanelUI
        setBorder(null);
        setLayout(new Layout());
    }

    public Dimension getPreferredSize() {
        return getLayout().preferredLayoutSize(this);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Returns the preferred size of the viewport for a view component.
     * 
     * @return The preferredSize of a JViewport whose view is this Scrollable.
     * @see JViewport#getPreferredSize
     */
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /**
     * Returns the "unit" increment for scrolling in the specified direction.
     * 
     * @param visibleRect The view area visible within the viewport
     * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction Less than zero to scroll up/left, greater than zero for down/right.
     * @return The "unit" increment for scrolling in the specified direction
     * @see JScrollBar#setUnitIncrement
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        switch (orientation) {
        case SwingConstants.HORIZONTAL:
            int ratio = getComponent(0).getPreferredSize().width;
            return ratio + (direction < 0 ? ratio + (visibleRect.x % ratio)
                            : (getPreferredSize().width - visibleRect.x + visibleRect.width) % ratio);
        case SwingConstants.VERTICAL:
            break;
        }
        return 0;
    }

    /**
     * Returns the "block" increment for scrolling in the specified direction.
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return getScrollableUnitIncrement(visibleRect, orientation, direction);
    }

    /**
     * Return true if a viewport should always force the width of this 
     * Scrollable to match the width of the viewport.
     * 
     * @return True if a viewport should force the Scrollables width to match its own.
     */
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    /**
     * Return true if a viewport should always force the height of this 
     * Scrollable to match the height of the viewport.
     *
     * @return True if a viewport should force the Scrollables height to match its own.
     */
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /**
     * This class implements layout manager for the ScrollPalettePanel.
     */
    final static private class Layout extends FlowLayout {
        static final long serialVersionUID =3756192927480423700L;
        /**
         * Creates a new flow layout manager.
         */
        public Layout() {
            super(FlowLayout.LEFT, 5, 0);
        }

        /**
         * Lays out the container so that the components are vertically centered.
         */
        public void layoutContainer(Container target) {
            // PENDING(david) simple layouting instead of super calls
            setVgap(0);
            super.layoutContainer(target);
            setVgap((target.getSize().height - preferredLayoutSize(target).height) / 2);
            super.layoutContainer(target);
            setVgap(0);
        }
    }
}

/*
 * Log
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         8/10/99  Ian Formanek    Generated Serial Version
 *       UID
 *  1    Gandalf   1.0         3/30/99  Ian Formanek    
 * $
 */
