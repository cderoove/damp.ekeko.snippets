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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.*;

/**
 * This class implements a specialized container that manages a scrollable
 * viewport.
 *
 * @version 1.1
 * @author David Peroutka
 */
public class ScrollPalette extends JScrollBar
{
    /**
     * @see #getUIClassID
     * @see #readObject
     */
    private static final String uiClassID = "ScrollPaletteUI"; // NOI18N

    protected JViewport viewport;

    static {
        try {
            // [IAN]
            // the next line is important for form module reloading to work, as the there is a cached Class under the BasicScrollPaletteUI class name key
            // in UIManager and theic Class will not be equal to Class loaded with the new module ClassLoader
            // [/IAN]
            UIManager.getDefaults ().remove("org.netbeans.modules.form.palette.BasicScrollPaletteUI"); // NOI18N
            UIManager.put(uiClassID, "org.netbeans.modules.form.palette.BasicScrollPaletteUI"); // NOI18N
        } catch (Exception e) {
            e.printStackTrace ();
        }
    }

    static final long serialVersionUID =6009601442100182208L;
    /**
     * Create a ScrollPalette that displays the contents of the specified
     * component, where both horizontal and vertical arrow buttons appear
     * whenever the component's contents are larger than the view.
     *
     * @param view the Component to display
     */
    public ScrollPalette(JComponent view)
    {
        // initialize model
        super(HORIZONTAL);
        // initialize viewport
        if (view == null)
            throw new IllegalArgumentException("Specified view must be non null"); // NOI18N
        setViewport(new JViewport());
        getViewport().setView(view);
    }

    Component getView () {
        return getViewport ().getView ();
    }

    /**
     * Sets the L&F object that renders this component.
     *
     * @param ui  the ScrollPaletteUI L&F object
     */
    public void setUI(ScrollPaletteUI ui) {
        super.setUI(ui);
    }

    /**
     * Notification from the UIManager that the L&F has changed.  Replaces the
     * current UI object with the latest version from the UIManager.
     */
    /*    public void updateUI() {

            Object o = UIManager.getUI (this);
            if (o instanceof ScrollPaletteUI) {
              setUI((ScrollPaletteUI)o);
            } else {
              System.out.println("Strange: UI not instance of ScrollPaletteUI: "+o.getClass ().getSuperclass ().getName ());
            }
        } */

    /**
     * Returns the name of the L&F class that renders this component.
     *
     * @see JComponent#getUIClassID
     * @see UIDefaults#getUI
     */
    public String getUIClassID() {
        return uiClassID;
    }

    /**
     * If the viewports view is a Scrollable then ask the view to compute
     * the unit increment.  Otherwise return super.getUnitIncrement().
     *
     * @see Scrollable#getScrollableUnitIncrement
     */
    public int getUnitIncrement(int direction) {
        JViewport vp = getViewport();
        if ((vp != null) && (vp.getView() instanceof Scrollable)) {
            Scrollable view = (Scrollable)(vp.getView());
            Rectangle vr = vp.getViewRect();
            return view.getScrollableUnitIncrement(vr, getOrientation(), direction);
        }
        else {
            return super.getUnitIncrement(direction);
        }
    }

    /**
     * If the viewports view is a Scrollable then ask the view to compute the
     * block increment.  Otherwise the blockIncrement equals the viewports
     * width or height.  If there's no viewport return
     * super.getBlockIncrement().
     *
     * @see Scrollable#getScrollableBlockIncrement
     */
    public int getBlockIncrement(int direction) {
        JViewport vp = getViewport();
        if (vp == null) {
            return super.getBlockIncrement(direction);
        }
        else if (vp.getView() instanceof Scrollable) {
            Scrollable view = (Scrollable)(vp.getView());
            Rectangle vr = vp.getViewRect();
            return view.getScrollableBlockIncrement(vr, getOrientation(), direction);
        }
        else if (getOrientation() == VERTICAL) {
            return vp.getExtentSize().width;
        }
        else {
            return vp.getExtentSize().height;
        }
    }

    /**
     * Returns the current viewport.
     * @return the JViewport currently in use
     */
    public JViewport getViewport() {
        return viewport;
    }

    /**
     * Sets the current viewport.
     */
    public void setViewport(JViewport newViewport) {
        JViewport oldViewport = viewport;
        viewport = newViewport;
        firePropertyChange("viewport", oldViewport, newViewport); // NOI18N
    }

    /**
     * Returns true if this component paints every pixel in its range. (In
     * other words, it does not have a transparent background or foreground.)
     *
     * @return The value of the opaque property
     * @see JComponent#isOpaque
     */
    public boolean isOpaque() {
        JViewport viewport;
        Component view;
        if (((viewport = getViewport()) != null) &&
                ((view = viewport.getView()) != null) &&
                ((view instanceof JComponent) && ((JComponent)view).isOpaque())) {
            if (((JComponent)view).getWidth()  >= viewport.getWidth() &&
                    ((JComponent)view).getHeight() >= viewport.getHeight())
                return true;
        }
        return false;
    }

    /**
     * Calls to revalidate() any descendant of this component, e.g.  the
     * viewports view, will cause a request to be queued that will validate
     * this component and all its descendants.
     *
     * @return true
     * @see revalidate
     * @see java.awt.Component#invalidate
     * @see java.awt.Container#validate
     */
    public boolean isValidateRoot() {
        return true;
    }
}

/*
 * Log
 *  7    Gandalf   1.6         1/5/00   Ian Formanek    NOI18N
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/10/99  Ian Formanek    Generated Serial Version
 *       UID
 *  4    Gandalf   1.3         7/15/99  Ian Formanek    Fixed problem 
 *       (NullPointerException in BasicScrollPaletteUI) which occured when form 
 *       editor module was reinstaled.
 *  3    Gandalf   1.2         5/10/99  Ian Formanek    
 *  2    Gandalf   1.1         4/4/99   Ian Formanek    added package-private 
 *       method getView ()
 *  1    Gandalf   1.0         3/30/99  Ian Formanek    
 * $
 */
