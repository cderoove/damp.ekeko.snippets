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

package org.netbeans.editor;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JComponent;

/**
* Immutable class that stores font and foreground and background colors.
* Generally the editor uses two sets of the colorings to colorize the text.
* They are called component-set and printing-set. The component-set is used
* for the editor component while the printing-set is used solely
* for colorizing the printed text.
* 
*
* @author Miloslav Metelka
* @version 1.00
*/

public class Coloring implements java.io.Serializable {

    /** Font */
    private Font font;

    /** Foreground color */
    private Color foreColor;

    /** Background color */
    private Color backColor;

    static final long serialVersionUID =-1382649127124476675L;

    /** Construct empty coloring */
    public Coloring() {
    }

    /** Construct new coloring */
    public Coloring(Font font, Color foreColor, Color backColor) {
        this.font = font;
        this.foreColor = foreColor;
        this.backColor = backColor;
    }

    /** Getter for font */
    public final Font getFont() {
        return font;
    }

    /** Getter for foreground color */
    public final Color getForeColor() {
        return foreColor;
    }

    /** Getter for background color */
    public final Color getBackColor() {
        return backColor;
    }

    /** Apply this coloring to draw context. */
    public void apply(DrawContext ctx) {
        if (font != null) {
            ctx.setFont(font);
        }
        if (foreColor != null) {
            ctx.setForeColor(foreColor);
        }
        if (backColor != null) {
            ctx.setBackColor(backColor);
        }
    }

    /** Apply this coloring to component colors/font. */
    public void apply(JComponent c) {
        if (font != null) {
            c.setFont(font);
        }
        if (foreColor != null) {
            c.setForeground(foreColor);
        }
        if (backColor != null) {
            c.setBackground(backColor);
        }
    }

    /** Apply this coloring to some other coloring c and return
    * the resulting coloring. All non-null properties from this coloring overwrite
    * corresponding (possibly non-null) properties in c.
    * If c is either null or equal to this
    * coloring or if the resulting coloring would be the same as this
    * coloring, the this coloring is returned.
    */
    public Coloring apply(Coloring c) {
        if (c == null || c == this
                || ((c.font == null || c.font.equals(font))
                    && (c.foreColor == null || c.foreColor.equals(foreColor))
                    && (c.backColor == null || c.backColor.equals(backColor)))
           ) {
            return this;
        }

        return new Coloring(
                   (font != null) ? font : c.font,
                   (foreColor != null) ? foreColor : c.foreColor,
                   (backColor != null) ? backColor : c.backColor
               );
    }

    /** All font, foreColor and backColor are the same. */
    public boolean equals(Object o) {
        if (o instanceof Coloring) {
            Coloring c = (Coloring)o;
            return ((font == null && c.font == null) || (font != null && font.equals(c.font)))
                   && ((foreColor == null && c.foreColor == null)
                       || (foreColor != null && foreColor.equals(c.foreColor)))
                   && ((backColor == null && c.backColor == null)
                       || (backColor != null && backColor.equals(c.backColor)));
        }
        return false;
    }

    public int hashCode() {
        return font.hashCode() ^ foreColor.hashCode() ^ backColor.hashCode();
    }

    public static Coloring changeFont(Coloring c, Font newFont) {
        if ((newFont == null && c.font == null)
                || (newFont != null && newFont.equals(c.font))
           ) {
            return c;
        }
        return new Coloring(newFont, c.foreColor, c.backColor);
    }

    public static Coloring changeFontName(Coloring c, String newFontName) {
        if (c.font == null || newFontName == null // null font name has no sense
                || (newFontName.equals(c.font.getName()))
           ) {
            return c;
        }
        return new Coloring(new Font(newFontName, c.font.getStyle(),
                                     c.font.getSize()), c.foreColor, c.backColor);
    }

    public static Coloring changeFontStyle(Coloring c, int newStyle) {
        if (c.font == null || c.font.getStyle() == newStyle) {
            return c;
        }
        return new Coloring(c.font.deriveFont(newStyle), c.foreColor, c.backColor);
    }

    public static Coloring changeFontSize(Coloring c, float newSize) {
        if (c.font == null || c.font.getSize() == newSize) {
            return c;
        }
        return new Coloring(c.font.deriveFont(newSize), c.foreColor, c.backColor);
    }

    public static Coloring changeFontNameAndSize(Coloring c, String newFontName,
            int newSize) {
        if (c.font == null || newFontName == null
                || (newFontName.equals(c.font.getName()) && (c.font.getSize() == newSize))
           ) {
            return c;
        }
        return new Coloring(new Font(newFontName, c.font.getStyle(),
                                     newSize), c.foreColor, c.backColor);
    }

    public static Coloring changeForeColor(Coloring c, Color newForeColor) {
        if ((newForeColor == null && c.foreColor == null)
                || (newForeColor != null && newForeColor.equals(c.foreColor))
           ) {
            return c;
        }
        return new Coloring(c.font, newForeColor, c.backColor);
    }

    public static Coloring changeBackColor(Coloring c, Color newBackColor) {
        if ((newBackColor == null && c.backColor == null)
                || (newBackColor != null && newBackColor.equals(c.backColor))
           ) {
            return c;
        }
        return new Coloring(c.font, c.foreColor, newBackColor);
    }

    /** Return true if the colorings have the same font name and size */
    public static boolean sameFontNameAndSize(Coloring c1, Coloring c2) {
        if (c1.font == null || c2.font == null) {
            return true; // cannot compare in this case
        }
        return c1.font.getName().equals(c2.font.getName());
    }

    public String toString() {
        return "font=" + font + ", foreColor=" + foreColor // NOI18N
               + ", backColor=" + backColor; // NOI18N
    }

}

/*
 * Log
 *  13   Gandalf   1.12        1/13/00  Miloslav Metelka 
 *  12   Gandalf   1.11        12/28/99 Miloslav Metelka 
 *  11   Gandalf   1.10        11/9/99  Miloslav Metelka 
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         8/17/99  Miloslav Metelka 
 *  8    Gandalf   1.7         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  7    Gandalf   1.6         7/20/99  Miloslav Metelka 
 *  6    Gandalf   1.5         7/9/99   Miloslav Metelka 
 *  5    Gandalf   1.4         5/15/99  Miloslav Metelka fixes
 *  4    Gandalf   1.3         5/13/99  Miloslav Metelka 
 *  3    Gandalf   1.2         5/5/99   Miloslav Metelka 
 *  2    Gandalf   1.1         3/18/99  Miloslav Metelka 
 *  1    Gandalf   1.0         2/3/99   Miloslav Metelka 
 * $
 */

