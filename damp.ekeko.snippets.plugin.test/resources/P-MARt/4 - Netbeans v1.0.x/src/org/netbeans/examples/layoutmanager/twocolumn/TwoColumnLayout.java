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

package org.netbeans.examples.layoutmanager.twocolumn;

import java.awt.*;
import java.util.*;

// [PENDING] reordering components in the Form Editor does not immediately
// refresh the design form, though source is updated, and with FlowLayout
// both are refreshed immediately

/**
 * Components are laid out in this manner
 * <pre>
 *     xxxx xxxxxx
 *       xx xxxxxx
 *      xxx xxxxxx
 * </pre>
 * or
 * <pre>
 *     xxxx xxxx
 *       xx xxxxxx
 *      xxx xxxxx
 * </pre>
 */

public class TwoColumnLayout implements LayoutManager
{
    public static final String LEFT = "Left";
    public static final String RIGHT = "Right";
    public static final String CENTER = "Center";
    public static final String FILL = "Fill";

    private int _hgap, _vgap;

    /**
     * @associates String 
     */
    private Hashtable _constraints = new Hashtable();

    public TwoColumnLayout() {
        this(0, 0);
    }

    public TwoColumnLayout(int hgap, int vgap) {
        _hgap = hgap;
        _vgap = vgap;
    }

    public void setHgap(int hgap) {
        _hgap = hgap;
    }

    public int getHgap() {
        return _hgap;
    }

    public void setVgap(int vgap) {
        _vgap = vgap;
    }

    public int getVgap() {
        return _vgap;
    }

    public void addLayoutComponent(String name, Component comp) {
        if (name == null) {
            name = LEFT;
        }
        _constraints.put(comp, name);
    }

    public void removeLayoutComponent(Component comp) {
        _constraints.remove(comp);
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension size = new Dimension();

        synchronized(parent.getTreeLock()) {
            Component[] comps = parent.getComponents();
            Insets insets = parent.getInsets();
            int maxleft = 0, maxright = 0;
            Dimension empty = new Dimension();

            int count = comps.length;
            for (int i = 0; i < count; i += 2) {
                Dimension d = comps[i].getPreferredSize();
                Dimension d2 = (i >= count - 1)
                               ? empty : comps[i+1].getPreferredSize();

                size.height += Math.max(d.height, d2.height);

                maxleft = Math.max(maxleft, d.width);
                maxright = Math.max(maxright, d2.width);

                // if not the last line
                if (i < count - 2)
                    size.height += _vgap;
            }
            size.width += insets.left + insets.right + _hgap + maxleft + maxright;
            size.height += insets.top + insets.bottom;
        }
        return size;
    }

    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    public void layoutContainer(Container parent) {
        synchronized(parent.getTreeLock()) {
            Component[] comps = parent.getComponents();
            int count = comps.length;
            Insets insets = parent.getInsets();
            Dimension size = parent.getSize();
            Dimension empty = new Dimension();

            int xalign = 0;
            for (int i = 0; i < count; i += 2)
                xalign = Math.max(xalign, comps[i].getPreferredSize().width);

            xalign += insets.left;
            int y = insets.top;

            for (int i = 0; i < count; i += 2) {
                Component c, c2;
                Dimension d, d2;

                c = comps[i];
                d = c.getPreferredSize();

                if (i < comps.length - 1) {
                    c2 = comps[i+1];
                    d2 = c2.getPreferredSize();
                } else {
                    c2 = null;
                    d2 = empty;
                }

                int h = Math.max(d.height, d2.height);
                setComponentBounds(c, insets.left, y, xalign - insets.left, h);

                if (c2 != null) {
                    setComponentBounds(c2,
                                       xalign + _hgap,
                                       y,
                                       size.width - (xalign + _hgap + insets.right),
                                       h);
                }
                y += h + _vgap;
            }
        }
    }

    private void setComponentBounds(Component c, int x, int y, int w, int h) {
        String constraint = (String) _constraints.get(c);
        Dimension sz = new Dimension(c.getPreferredSize());
        sz.width = Math.min(sz.width, w);
        sz.height = Math.min(sz.height, h);

        if (LEFT.equals(constraint)) {
            w = sz.width;
        } else if (RIGHT.equals(constraint)) {
            x = w - sz.width;
            w = sz.width;
        } else if (CENTER.equals(constraint)) {
            x = x + (w - sz.width) / 2;
            w = sz.width;
        } else { // if (FILL.equals(constraint)) {
            // no-op
        }
        c.setBounds(x, y + (h - sz.height) / 2, w, sz.height);
    }
}
