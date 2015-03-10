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

import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Enumeration;
import java.awt.Color;
import java.awt.Font;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.Style;
import javax.swing.text.Element;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleContext;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
/**
* Extension to the guarded document that implements
* StyledDocument interface
*
* @author Miloslav Metelka
* @version 1.00
*/

public class GuardedDocument extends BaseDocument
    implements StyledDocument {

    /** Guarded attribute used for specifying that the inserted block
    * will be guarded.
    */
    public static final String GUARDED_ATTRIBUTE = "guarded"; // NOI18N

    /** AttributeSet with only guarded attribute */
    public static final SimpleAttributeSet guardedSet = new SimpleAttributeSet();

    /** AttributeSet with only break-guarded attribute */
    public static final SimpleAttributeSet unguardedSet = new SimpleAttributeSet();

    private static final boolean debugAtomic = Boolean.getBoolean("netbeans.debug.editor.atomic"); // NOI18N
    private static final boolean debugAtomicStack = Boolean.getBoolean("netbeans.debug.editor.atomic.stack"); // NOI18N

    // Add the attributes to sets
    static {
        guardedSet.addAttribute(GUARDED_ATTRIBUTE, Boolean.TRUE);
        unguardedSet.addAttribute(GUARDED_ATTRIBUTE, Boolean.FALSE);
    }

    public static final String FMT_GUARDED_INSERT_LOCALE = "FMT_guarded_insert"; // NOI18N
    public static final String FMT_GUARDED_INSERT_DEFAULT
    = "Attempt to insert into guarded block at position {0}."; // NOI18N

    public static final String FMT_GUARDED_REMOVE_LOCALE = "FMT_guarded_remove"; // NOI18N
    public static final String FMT_GUARDED_REMOVE_DEFAULT
    = "Attempt to remove from guarded block at position {0}."; // NOI18N

    MarkBlockChain guardedBlockChain;

    /** Break the guarded flag, so inserts/removals over guarded areas will work */
    boolean breakGuarded;

    boolean atomicAsUser;

    /** Style context to hold the styles */
    protected StyleContext styles;

    /** Style to layer name mapping 
     * @associates String*/
    protected Hashtable stylesToLayers;

    /** Name of the normal style. The normal style is used to reset the effect
    * of all styles applied to the line.
    */
    protected String normalStyleName;

    public GuardedDocument(Class kitClass) {
        this(kitClass, true, new StyleContext());
    }

    /** Create base document with a specified syntax and style context.
    * @param kitClass class used to initialize this document with proper settings
    *   category based on the editor kit for which this document is created
    * @param syntax syntax scanner to use with this document
    * @param styles style context to use
    */
    public GuardedDocument(Class kitClass, boolean addToRegistry, StyleContext styles) {
        super(kitClass, addToRegistry);
        this.styles = styles;
        stylesToLayers = new Hashtable(5);
        addLayer(new DrawLayerFactory.GuardedLayer());
        guardedBlockChain = new MarkBlockChain.LayerChain(this, DrawLayerFactory.GUARDED_LAYER_NAME);
    }

    /** Get the chain of the guarded blocks */
    public MarkBlockChain getGuardedBlockChain() {
        return guardedBlockChain;
    }

    public boolean isPosGuarded(int pos) {
        int rel = guardedBlockChain.compareBlock(pos, pos) & MarkBlock.IGNORE_EMPTY;
        return (rel == MarkBlock.INSIDE_BEGIN || rel == MarkBlock.INNER);
    }

    /** This method is called automatically before the document
    * is updated as result of removal. This function can throw
    * BadLocationException or its descendants to stop the ongoing
    * insert from being actually done.
    * @param evt document event containing the change including array
    *  of characters that will be inserted
    */
    protected void preInsertUpdate(int offset, String text, AttributeSet a)
    throws BadLocationException {
        super.preInsertUpdate(offset, text, a);

        int rel = guardedBlockChain.compareBlock(offset, offset) & MarkBlock.IGNORE_EMPTY;

        if (debugAtomic) {
            System.err.println("GuardedDocument.beforeInsertUpdate() atomicAsUser=" // NOI18N
                               + atomicAsUser + ", breakGuarded=" + breakGuarded // NOI18N
                               + ", inserting text='" + EditorDebug.debugString(text) // NOI18N
                               + "' at offset=" + Utilities.debugPosition(this, offset)); // NOI18N
            if (debugAtomicStack) {
                Thread.dumpStack();
            }
        }

        if (text.length() > 0
                && (rel & MarkBlock.OVERLAP) != 0
                && rel != MarkBlock.INSIDE_END // guarded blocks have insertAfter endMark
                && !(text.charAt(text.length() - 1) == '\n'
                     && rel == MarkBlock.INSIDE_BEGIN)
           ) {
            if (!breakGuarded || atomicAsUser) {
                throw new GuardedException(
                    MessageFormat.format(
                        LocaleSupport.getString(FMT_GUARDED_INSERT_LOCALE, FMT_GUARDED_INSERT_DEFAULT),
                        new Object [] {
                            new Integer(offset)
                        }
                    ),
                    offset
                );
            }
        }
    }

    /** This method is called automatically before the document
    * removal occurs and can be used to revoke the removal before it occurs
    * by throwing the <tt>BadLocationException</tt>.
    */
    protected void preRemoveUpdate(DefaultDocumentEvent evt)
    throws BadLocationException {
        int pos = evt.getOffset();
        int rel = guardedBlockChain.compareBlock(pos, pos + evt.getLength());

        if (debugAtomic) {
            System.err.println("GuardedDocument.beforeRemoveUpdate() atomicAsUser=" // NOI18N
                               + atomicAsUser + ", breakGuarded=" + breakGuarded // NOI18N
                               + ", removing text='" + EditorDebug.debugString(evt.getDocument().getText(pos, evt.getLength())) // NOI18N
                               + "'at pos=" + Utilities.debugPosition(this, pos)); // NOI18N
            if (debugAtomicStack) {
                Thread.dumpStack();
            }
        }

        if ((rel & MarkBlock.OVERLAP) != 0
                || (rel == MarkBlock.CONTINUE_BEGIN
                    && !(pos == 0 || ((BaseDocument)evt.getDocument()).getChars(pos - 1, 1)[0] == '\n'))
           ) {
            if (!breakGuarded || atomicAsUser) {
                // test whether the previous char before removed text is '\n'
                throw new GuardedException(
                    MessageFormat.format(
                        LocaleSupport.getString(FMT_GUARDED_REMOVE_LOCALE, FMT_GUARDED_REMOVE_DEFAULT),
                        new Object [] {
                            new Integer(pos)
                        }
                    ),
                    pos
                );
            }
        }
    }

    public void setCharacterAttributes(int offset, int length, AttributeSet s,
                                       boolean replace) {
        if (((Boolean)s.getAttribute(GUARDED_ATTRIBUTE)).booleanValue() == true) {
            guardedBlockChain.addBlock(offset, offset + length, false); // no concat
            fireChangedUpdate(createDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE));
        }
        if (((Boolean)s.getAttribute(GUARDED_ATTRIBUTE)).booleanValue() == false) {
            guardedBlockChain.removeBlock(offset, offset + length);
            fireChangedUpdate(createDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE));
        }
    }

    public void runAtomic(Runnable r) {
        if (debugAtomic) {
            System.out.println("GuardedDocument.runAtomic() called"); // NOI18N
            if (debugAtomicStack) {
                Thread.dumpStack();
            }
        }

        boolean origBreakGuarded = breakGuarded;
        try {
            breakGuarded = true;
            super.runAtomicAsUser(r);
        } finally {
            breakGuarded = origBreakGuarded;
            if (debugAtomic) {
                System.out.println("GuardedDocument.runAtomic() finished"); // NOI18N
            }
        }
    }

    public void runAtomicAsUser(Runnable r) {
        if (debugAtomic) {
            System.out.println("GuardedDocument.runAtomicAsUser() called"); // NOI18N
            if (debugAtomicStack) {
                Thread.dumpStack();
            }
        }

        boolean origAtomicAsUser = atomicAsUser;
        try {
            atomicAsUser = true;
            super.runAtomicAsUser(r);
        } finally {
            if (debugAtomic) {
                System.out.println("GuardedDocument.runAtomicAsUser() finished"); // NOI18N
            }
            atomicAsUser = origAtomicAsUser;
        }
    }

    protected BaseDocumentEvent createDocumentEvent(int offset, int length,
            DocumentEvent.EventType type) {
        return new GuardedDocumentEvent(this, offset, length, type);
    }

    /** Adds style to the document */
    public Style addStyle(String styleName, Style parent) {
        String layerName = (String)stylesToLayers.get(styleName);
        if (layerName == null) {
            layerName = styleName; // same layer name as style name
            addStyleToLayerMapping(styleName, layerName);
        }

        Style style =  styles.addStyle(styleName, parent);
        if (findLayer(layerName) == null) { // not created by default
            try {
                extWriteLock();
                DrawLayer layer = createStyledLayer(layerName, style);
                if (layer != null) {
                    addLayer(layer);
                }
            } finally {
                extWriteUnlock();
            }
        }
        return style;
    }

    public void addStyleToLayerMapping(String styleName, String layerName) {
        stylesToLayers.put(styleName, layerName);
    }

    /** Removes style from document */
    public void removeStyle(String styleName) {
        styles.removeStyle(styleName);
    }

    /** Fetches style previously added */
    public Style getStyle(String styleName) {
        return styles.getStyle(styleName);
    }

    /** Set the name for normal style. Normal style is used to reset the effect
    * of all aplied styles.
    */
    public void setNormalStyleName(String normalStyleName) {
        this.normalStyleName = normalStyleName;
    }

    /** Fetches the list of style names */
    public Enumeration getStyleNames() {
        return styles.getStyleNames();
    }

    /** Change attributes for part of the text.  */
    public void setParagraphAttributes(int offset, int length, AttributeSet s,
                                       boolean replace) {
        // !!! implement
    }

    /**
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
        try {
            extWriteLock();
            pos = op.getBOL(pos); // begining of line
            String layerName = (String)stylesToLayers.get(s.getName());
            // remove all applied styles
            DrawLayer[] layerArray = getDrawLayerList().currentLayers();
            for (int i = 0; i < layerArray.length; i++) {
                if (layerArray[i] instanceof DrawLayerFactory.StyleLayer) {
                    ((DrawLayerFactory.StyleLayer)layerArray[i]).markChain.removeMark(pos);
                }
            }
            // now set the requested style
            DrawLayerFactory.StyleLayer styleLayer
            = (DrawLayerFactory.StyleLayer)findLayer(layerName);
            if (styleLayer != null) {
                styleLayer.markChain.addMark(pos);
            }
        } catch (BadLocationException e) {
            // do nothing for invalid positions
        } finally {
            extWriteUnlock();
        }
        fireChangedUpdate(createDocumentEvent(pos, 0,
                                              DocumentEvent.EventType.CHANGE)); // enough to say length 0
    }

    /** Get logical style for position in paragraph */
    public Style getLogicalStyle(int pos) {
        try {
            pos = op.getBOL(pos); // begining of line
            DrawLayer[] layerArray = getDrawLayerList().currentLayers();
            for (int i = 0; i < layerArray.length; i++) {
                DrawLayer layer = layerArray[i];
                if (layer instanceof DrawLayerFactory.StyleLayer) {
                    if (((DrawLayerFactory.StyleLayer)layer).markChain.isMark(pos)) {
                        return ((DrawLayerFactory.StyleLayer)layer).style;
                    }
                }
            }
        } catch (BadLocationException e) {
            // do nothing for invalid positions
        }
        return getStyle(normalStyleName); // no style found
    }

    /**
     * Gets the element that represents the character that
     * is at the given offset within the document.
     *
     * @param pos the offset >= 0
     * @return the element
     */
    public Element getCharacterElement(int pos) {
        return getParagraphElement(pos);
    }


    /**
     * Takes a set of attributes and turn it into a foreground color
     * specification.  This might be used to specify things
     * like brighter, more hue, etc.
     *
     * @param attr the set of attributes
     * @return the color
     */
    public Color getForeground(AttributeSet attr) {
        return null; // !!!
    }

    /**
     * Takes a set of attributes and turn it into a background color
     * specification.  This might be used to specify things
     * like brighter, more hue, etc.
     *
     * @param attr the set of attributes
     * @return the color
     */
    public Color getBackground(AttributeSet attr) {
        return null; // !!!
    }

    /**
     * Takes a set of attributes and turn it into a font
     * specification.  This can be used to turn things like
     * family, style, size, etc into a font that is available
     * on the system the document is currently being used on.
     *
     * @param attr the set of attributes
     * @return the font
     */
    public Font getFont(AttributeSet attr) {
        return null; // !!!
    }

    protected DrawLayer createStyledLayer(String layerName, Style style) {
        if (layerName != null) {
            try {
                int indColon = layerName.indexOf(':');
                int layerVisibility = Integer.parseInt(layerName.substring(indColon + 1));
                return new DrawLayerFactory.StyleLayer(layerName, layerVisibility,
                                                       this, style);
            } catch (NumberFormatException e) {
                // wrong name, let it pass
            }
        }
        return null;
    }

    public String toString() {
        return "length=" + getLength() + ", " // NOI18N
               + getDrawLayerList()
               + ",\nGUARDED blocks:\n" + guardedBlockChain; // NOI18N
    }

}

/*
 * Log
 *  6    Gandalf-post-FCS1.5         4/13/00  Miloslav Metelka repaired wrong branching
 *  5    Gandalf-post-FCS1.4         4/13/00  Miloslav Metelka overriding postFCS patch
 *  4    Gandalf-post-FCS1.3         4/13/00  Miloslav Metelka fixing remove from 
 *       guarded blocks
 *  3    Gandalf-post-FCS1.2         4/4/00   Miloslav Metelka 
 *  2    Gandalf-post-FCS1.1         4/3/00   Miloslav Metelka undo update
 *  1    Gandalf-post-FCS1.0         3/8/00   Miloslav Metelka 
 * $
 */

