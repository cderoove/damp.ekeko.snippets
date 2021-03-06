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

package org.openide.text;

import javax.swing.text.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;

// Document implementation
/** Document that delegates all functionality to a given {@link StyledDocument}.
* Useful if a subclass wants to modify the behaviour of a document.
* <p>Note that unlike {@link FilterDocument}, no methods are faked here, since a real styled document is available.
*
* @author Jaroslav Tulach
*/
public class FilterStyledDocument extends FilterDocument {
    /** Create new document instance.
     * @param original the delegated-to styled document
    */
    public FilterStyledDocument(StyledDocument original) {
        super (original);
    }

    //
    // StyledDocument methods
    //


    /*
    * Adds a new style into the logical style hierarchy.  Style attributes
    * resolve from bottom up so an attribute specified in a child
    * will override an attribute specified in the parent.
    *
    * @param nm   the name of the style (must be unique within the
    *   collection of named styles).  The name may be null if the style
    *   is unnamed, but the caller is responsible
    *   for managing the reference returned as an unnamed style can't
    *   be fetched by name.  An unnamed style may be useful for things
    *   like character attribute overrides such as found in a style
    *   run.
    * @param parent the parent style.  This may be null if unspecified
    *   attributes need not be resolved in some other style.
    * @return the style
    */
    public Style addStyle(String nm, Style parent) {
        return ((StyledDocument)original).addStyle (nm, parent);
    }

    /*
    * Removes a named style previously added to the document.
    *
    * @param nm  the name of the style to remove
    */
    public void removeStyle(String nm) {
        ((StyledDocument)original).removeStyle (nm);
    }

    /*
    * Fetches a named style previously added.
    *
    * @param nm  the name of the style
    * @return the style
    */
    public Style getStyle(String nm) {
        return ((StyledDocument)original).getStyle (nm);
    }

    /*
    * Changes the content element attributes used for the given range of
    * existing content in the document.  All of the attributes
    * defined in the given Attributes argument are applied to the
    * given range.  This method can be used to completely remove
    * all content level attributes for the given range by
    * giving an Attributes argument that has no attributes defined
    * and setting replace to true.
    *
    * @param offset the start of the change >= 0
    * @param length the length of the change >= 0
    * @param s    the non-null attributes to change to.  Any attributes
    *  defined will be applied to the text for the given range.
    * @param replace indicates whether or not the previous
    *  attributes should be cleared before the new attributes
    *  as set.  If true, the operation will replace the
    *  previous attributes entirely.  If false, the new
    *  attributes will be merged with the previous attributes.
    */
    public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
        ((StyledDocument)original).setCharacterAttributes (offset, length, s, replace);
    }

    /*
    * Sets paragraph attributes.
    *
    * @param offset the start of the change >= 0
    * @param length the length of the change >= 0
    * @param s    the non-null attributes to change to.  Any attributes
    *  defined will be applied to the text for the given range.
    * @param replace indicates whether or not the previous
    *  attributes should be cleared before the new attributes
    *  are set.  If true, the operation will replace the
    *  previous attributes entirely.  If false, the new
    *  attributes will be merged with the previous attributes.
    */
    public void setParagraphAttributes(int offset, int length, AttributeSet s, boolean replace) {
        ((StyledDocument)original).setParagraphAttributes (offset, length, s, replace);
    }

    /*
    * Sets the logical style to use for the paragraph at the
    * given position.  If attributes aren't explicitly set
    * for character and paragraph attributes they will resolve
    * through the logical style assigned to the paragraph, which
    * in turn may resolve through some hierarchy completely
    * independent of the element hierarchy in the document.
    *
    * @param pos the starting position >= 0
    * @param s the style to set
    */
    public void setLogicalStyle(int pos, Style s) {
        ((StyledDocument)original).setLogicalStyle (pos, s);
    }

    /*
    * Gets a logical style for a given position in a paragraph.
    *
    * @param p the position >= 0
    * @return the style
    */
    public Style getLogicalStyle(int p) {
        return ((StyledDocument)original).getLogicalStyle (p);
    }

    /*
    * Gets the element that represents the paragraph that
    * encloses the given offset within the document.
    *
    * @param pos the offset >= 0
    * @return the element
    */
    public Element getParagraphElement (int pos) {
        return ((StyledDocument)original).getParagraphElement (pos);
    }

    /*
    * Gets the element that represents the character that
    * is at the given offset within the document.
    *
    * @param pos the offset >= 0
    * @return the element
    */
    public Element getCharacterElement(int pos) {
        return ((StyledDocument)original).getCharacterElement (pos);
    }


    /*
    * Takes a set of attributes and turn it into a foreground color
    * specification.  This might be used to specify things
    * like brighter, more hue, etc.
    *
    * @param attr the set of attributes
    * @return the color
    */
    public java.awt.Color getForeground(AttributeSet attr) {
        return ((StyledDocument)original).getForeground (attr);
    }

    /*
    * Takes a set of attributes and turn it into a background color
    * specification.  This might be used to specify things
    * like brighter, more hue, etc.
    *
    * @param attr the set of attributes
    * @return the color
    */
    public java.awt.Color getBackground(AttributeSet attr) {
        return ((StyledDocument)original).getBackground (attr);
    }

    /*
    * Takes a set of attributes and turn it into a font
    * specification.  This can be used to turn things like
    * family, style, size, etc into a font that is available
    * on the system the document is currently being used on.
    *
    * @param attr the set of attributes
    * @return the font
    */
    public java.awt.Font getFont(AttributeSet attr) {
        return ((StyledDocument)original).getFont (attr);
    }
}


/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         2/10/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
