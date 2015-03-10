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

package org.netbeans.core.windows.toolbars;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import org.openide.awt.*;

/**
 * ToolbarLayout is a LayoutManager2 that should be used on a toolbar panel to
 * allow placement of components in absolute positions.
 *
 * @version 1.0
 * @author  Libor Kramolis
 */
public class ToolbarLayout implements LayoutManager2, java.io.Serializable {
    public static final int HGAP = 1;
    public static final int VGAP = 1;

    static final long serialVersionUID =7489472539255790677L;

    ToolbarConfiguration toolbarConfig;

    /**
     * @associates Object 
     */
    HashMap componentMap;


    /**
     * Creates a new ToolbarLayout.
     */
    public ToolbarLayout (ToolbarConfiguration conf) {
        toolbarConfig = conf;
        componentMap = new HashMap();
    }

    /** Adds the specified component with the specified name to
     * the layout. Everytime throws IllegalArgumentException.
     * @param name the component name
     * @param comp the component to be added
     */
    public void addLayoutComponent (String name, Component comp) {
        throw new IllegalArgumentException();
    }

    /**
     * Adds the specified component to the layout, using the specified
     * constraint object.
     * @param comp the component to be added
     * @param constraints  the where/how the component is added to the layout.
     * @exception <code>ClassCastException</code> if the argument is not a
     *		  <code>ToolbarConstraints</code>.
     */
    public void addLayoutComponent (Component comp, Object constr) {
        if (!(constr instanceof ToolbarConstraints))
            throw new IllegalArgumentException (ToolbarConfiguration.bundle.getString ("EXC_wrongConstraints"));

        componentMap.put (comp, constr);
        ToolbarConstraints tc = (ToolbarConstraints)constr;
        tc.setPreferredSize (comp.getPreferredSize());
        comp.setVisible (tc.isVisible());
    }

    /**
     * Removes the specified component from this layout.
     * @param comp the component to be removed
     */
    public void removeLayoutComponent (Component comp) {
        componentMap.remove (comp);
    }

    /**
     * Calculates the preferred dimension for the specified panel given the
     * components in the specified parent container.
     * @param parent the component to be laid out
     *
     * @see #minimumLayoutSize
     */
    public Dimension preferredLayoutSize (Container parent) {
        Insets insets = parent.getInsets();
        Dimension prefSize = new Dimension (insets.left + toolbarConfig.getPrefWidth() + insets.right,
                                            insets.top + toolbarConfig.getPrefHeight() + insets.bottom);
        return prefSize;
    }

    /**
     * Calculates the minimum dimension for the specified
     * panel given the components in the specified parent container.
     * @param parent the component to be laid out
     * @see #preferredLayoutSize
     */
    public Dimension minimumLayoutSize (Container parent) {
        return preferredLayoutSize (parent);
    }

    /**
     * Returns the maximum size of this component.
     * @see java.awt.Component#getMinimumSize()
     * @see java.awt.Component#getPreferredSize()
     * @see LayoutManager
     */
    public Dimension maximumLayoutSize (Container parent) {
        return new Dimension (Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Returns the alignment along the x axis.  This specifies how the
     * component would like to be aligned relative to other components.  The
     * value should be a number between 0 and 1 where 0 represents alignment
     * along the origin, 1 is aligned the furthest away from the origin, 0.5
     * is centered, etc.
     */
    public float getLayoutAlignmentX (Container parent) {
        return 0;
    }

    /**
     * Returns the alignment along the y axis.  This specifies how the
     * component would like to be aligned relative to other components.  The
     * value should be a number between 0 and 1 where 0 represents alignment
     * along the origin, 1 is aligned the furthest away from the origin, 0.5
     * is centered, etc.
     */
    public float getLayoutAlignmentY (Container parent) {
        return 0;
    }

    /**
     * Invalidates the layout, indicating that if the layout manager has
     * cached information it should be discarded.
     */
    public void invalidateLayout (Container parent) {
    }

    /**
     * Lays out the container in the specified panel.
     * @param parent the component which needs to be laid out
     */
    public void layoutContainer (Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int maxPosition = parent.getWidth() - (insets.left + insets.right) - HGAP;

            Iterator it;
            Component comp;
            ToolbarConstraints constr;
            Rectangle bounds;

            it = componentMap.keySet().iterator();
            while (it.hasNext()) {
                comp = (Component)it.next();
                constr = (ToolbarConstraints)componentMap.get (comp);
                constr.updatePreferredSize (comp.getPreferredSize());
            }

            it = componentMap.keySet().iterator();
            while (it.hasNext()) {
                comp = (Component)it.next();
                constr = (ToolbarConstraints)componentMap.get (comp);

                bounds = constr.getBounds();

                if ((bounds.x < maxPosition) &&
                        (bounds.x + bounds.width > maxPosition)) {
                    bounds.width = maxPosition - bounds.x;
                } else {
                    if (bounds.x > maxPosition + HGAP) {
                        constr.setPosition (maxPosition + HGAP);
                        constr.updatePosition();
                    }
                }

                comp.setBounds (bounds);
            }
        }
    }
}

/*
 * Log
 *  9    Gandalf   1.8         1/20/00  Libor Kramolis  
 *  8    Gandalf   1.7         1/20/00  Libor Kramolis  
 *  7    Gandalf   1.6         1/19/00  Libor Kramolis  
 *  6    Gandalf   1.5         1/16/00  Libor Kramolis  
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/18/99  Ian Formanek    Generated serial version
 *       UID
 *  3    Gandalf   1.2         8/3/99   Libor Kramolis  
 *  2    Gandalf   1.1         7/30/99  Libor Kramolis  
 *  1    Gandalf   1.0         7/11/99  David Simonek   
 * $
 */
