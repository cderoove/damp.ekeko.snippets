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

package org.openide.awt;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import com.sun.java.swing.plaf.windows.*;

/**
 * This class implements UI for toolbar buttons.
 * @author David Peroutka
 * @version 0.2 06/05/1998
 */
class ToolbarButtonUI extends WindowsButtonUI {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5306796614639723529L;

    /* this icon is used to simulate crosshatch brush */
    private static ImageIcon brush;

    /** Getter for the brush.
    */
    private ImageIcon getBrush () {
        if (brush != null) return brush;
        brush = new ImageIcon (
                    getClass ().getResource("/org/netbeans/core/resources/plaf/crosshatch.gif") // NOI18N
                );
        return brush;
    }

    /* saved state of previously installed UI */
    Icon oldDisabledIcon;
    boolean oldFocusPainted, oldBorderPainted, oldRolloverEnabled;
    /* simple workaround until the better toolbar border will be used */
    protected final static Insets defaultMargin = new Insets(2, 1, 0, 1);
    private final static ToolbarButtonUI toolbarButtonUI = new ToolbarButtonUI();
    public static ComponentUI createUI(JComponent c)
    {
        return toolbarButtonUI;
    }

    public void installUI(JComponent c)
    {
        super.installUI(c);

        AbstractButton button = (AbstractButton)c;
        // save for uninstall
        oldFocusPainted  = button.isFocusPainted();
        oldBorderPainted = button.isBorderPainted();
        oldRolloverEnabled = button.isRolloverEnabled();
        // modify component
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setRolloverEnabled(true);
        // activate unavailable appearance effect
        ImageIcon icon = (ImageIcon)button.getDisabledIcon();
        if (icon instanceof DisabledIcon)
            ((DisabledIcon)icon).setBorderPainted(true);
        else if (icon instanceof ImageIcon)
            button.setDisabledIcon(new DisabledIcon(icon.getImage()));
    }

    public void uninstallUI(JComponent c)
    {
        super.uninstallUI(c);

        AbstractButton button = (AbstractButton)c;
        // restore saved status
        button.setFocusPainted(oldFocusPainted);
        button.setBorderPainted(oldBorderPainted);
        button.setRolloverEnabled(oldRolloverEnabled);
        // deactivate unavailable appearance effect
        ImageIcon icon = (ImageIcon)button.getDisabledIcon();
        if (icon instanceof DisabledIcon)
            ((DisabledIcon)icon).setBorderPainted(false);
    }

    public void paint(Graphics g, JComponent c)
    {
        AbstractButton button = (AbstractButton)c;
        ButtonModel model = button.getModel();
        // draw selection background
        if (model.isSelected() && !model.isRollover())
        {
            // ugly workaround until the "border" model rolls in // NOI18N
            g.setClip(2, 2, button.getWidth() - 4, button.getHeight() - 4);
            getBrush ().paintIcon(c, g, 0, 0);
            g.setClip(0, 0, button.getWidth(), button.getHeight());
        }
        // draw icon (default or disabled)
        super.paint(g, c);
        // draw button borders (TODO: ToolbarButtonBorderUI is better)
        if (model.isEnabled())
        {
            // set-up colors according to button status
            if (model.isSelected())
            {
                draw3DRect(g, 1, 1, button.getWidth() - 2, button.getHeight() - 2,
                           UIManager.getDefaults ().getColor ("controlShadow"), // NOI18N
                           UIManager.getDefaults ().getColor ("controlLtHighlight")); // NOI18N
            } else if (model.isRollover())
            {
                Color color1 = UIManager.getDefaults ().getColor ("controlLtHighlight"); // NOI18N
                Color color2 = UIManager.getDefaults ().getColor ("controlShadow"); // NOI18N
                if (model.isArmed() && model.isPressed())
                {
                    color1 = UIManager.getDefaults ().getColor ("controlShadow"); // NOI18N
                    color2 = UIManager.getDefaults ().getColor ("controlLtHighlight"); // NOI18N
                }
                draw3DRect(g, 1, 1, button.getWidth() - 2, button.getHeight() - 2, color1, color2);
            }
        }
    }

    /**
    * Draws a 3-D highlighted outline of the specified rectangle.  The
    * rectangle will be drawn with the top and left sides in the color
    * specified by <code>topColor</code> and the bottom and right sides in
    * the color specified by <code>bottomColor</code>.<p>
    *
    * @param   left the <i>x</i> coordinate of the rectangle to be drawn.
    * @param   top the <i>y</i> coordinate of the rectangle to be drawn.
    * @param   width the width of the rectangle to be drawn.
    * @param   height the height of the rectangle to be drawn.
    * @param   topColor Specifies the color of the top and left sides.
    * @param   bottomColor Specifies the color of the bottom and right sides.
    *
    * TODO: move this method to plaf.GraphicsUtils
    */
    private void draw3DRect(Graphics g, int left, int top, int width, int height,
                            Color topColor, Color bottomColor
                           ) {
        int right = left + width - 1;
        int bottom = top + height - 1;
        // draw rectangle
        g.setColor(topColor);
        g.drawLine(right - 1, top, left, top);
        g.drawLine(left, top + 1, left, bottom - 1);
        g.setColor(bottomColor);
        g.drawLine(left, bottom, right, bottom);
        g.drawLine(right, bottom - 1, right, top);
    }

    /**
    * Fetches the default margin space for the text ui.
    *
    * @return the margins
    */
    public Insets getDefaultMargin(AbstractButton button)
    {
        return defaultMargin;
    }
}


/*
* Log
*  6    src-jtulach1.5         1/13/00  Ian Formanek    NOI18N
*  5    src-jtulach1.4         1/12/00  Ian Formanek    NOI18N
*  4    src-jtulach1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    src-jtulach1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    src-jtulach1.1         3/10/99  Jaroslav Tulach 
*  1    src-jtulach1.0         3/10/99  Jaroslav Tulach 
* $
* Beta Change History:
*/
