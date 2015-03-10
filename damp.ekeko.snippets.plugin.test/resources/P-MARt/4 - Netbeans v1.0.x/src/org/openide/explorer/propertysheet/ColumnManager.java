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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * Column layout is used to layout components in a NamesPanel.
 *
 * @version    1.13, Apr 9, 1998
 * @author     Jan Jancura
 */
class ColumnManager implements LayoutManager, java.io.Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -5706896066699438744L;

    /** If size of this CM depends on the other CM size, there is link on it. */
    ColumnManager                                 columnManager;

    /** One components height. All the components has the same. */
    int                                           height;


    /**
     * Constructs a new ColumnManager.
     */
    public ColumnManager () {
        this (null);
    }

    /**
     * Constructs a new ColumnManager.
     */
    public ColumnManager (ColumnManager columnManager) {
        this.columnManager = columnManager;
    }

    /**
     * Adds the specified component to the layout.
     *
     * @param <CODE>String position</CODE> the name of the position of the component
     * @param <CODE>Component component</CODE> the the component to be added
     */
    public void addLayoutComponent (String position, Component component) {
    }

    /**
     * Removes the specified component from the layout.
     *
     * @param <CODE>Component component</CODE> the component to remove.
     */
    public void removeLayoutComponent (Component component) {
    }

    /**
     * Returns the preferred dimensions for this layout given the components
     * in the specified target container.
     *
     * @param <CODE>Container target</CODE> The container which needs to be laid out.
     * @see java.awt.Container
     * @see #minimumLayoutSize
     */
    public Dimension preferredLayoutSize (Container target) {
        int k = target.getComponentCount ();
        if (k < 1) return new Dimension (1, 1);
        if (columnManager != null) height = columnManager.getComponentHeight ();
        else height = target.getComponent (0).getPreferredSize ().height;
        return new Dimension (1, height * k);
    }

    /**
     * Returns component height.
     *
     * @return Component height.
     */
    public int getComponentHeight () {
        return height;
    }

    /**
     * Returns the minimum dimensions needed to layout the components
     * contained in the specified target container.
     *
     * @param </CODE>Container target</CODE> The container which needs to be laid out.
     * @see #preferredLayoutSize
     */
    public Dimension minimumLayoutSize (Container target) {
        return preferredLayoutSize (target);
    }

    /**
     * Lays out the container. This method will actually reshape the
     * components in the target in order to satisfy the constraints of
     * the BorderLayout object.
     *
     * @param <CODE>Component target</CODE> The specified container being laid out.
     * @see java.awt.Container
     */
    public void layoutContainer (Container target) {
        if (target.getComponentCount () < 1) return;
        Insets insets = target.getInsets ();
        int height,
        k = target.getComponentCount (),
            y = 0,
                width = target.getSize ().width - (insets.left + insets.right);

        if (columnManager != null) height = columnManager.getComponentHeight ();
        else height = target.getComponent (0).getPreferredSize ().height;

        for (int i = 0; i < k; i++) {
            target.getComponent (i).setBounds (0, y, width, height);
            y += height;
        }
    }
}

/*
 * Log
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
