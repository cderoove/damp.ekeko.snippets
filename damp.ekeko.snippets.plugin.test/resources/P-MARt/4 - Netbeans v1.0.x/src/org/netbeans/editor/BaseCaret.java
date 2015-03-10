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

import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Component;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.lang.ref.WeakReference;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.Action;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.EventListenerList;

/**
* Caret implementation
*
* @author Miloslav Metelka
* @version 1.00
*/

public class BaseCaret extends Rectangle implements Caret, FocusListener,
            MouseListener, MouseMotionListener, PropertyChangeListener,
    DocumentListener, ActionListener, SettingsChangeListener {

    /** Caret type representing block covering current character */
    public static final String BLOCK_CARET = "block-caret"; // NOI18N

    /** Default caret type */
    public static final String LINE_CARET = "line-caret"; // NOI18N

    /** One dot thin line compatible with Swing default caret */
    public static final String THIN_LINE_CARET = "thin-line-caret"; // NOI18N

    private static final boolean debugCaretFocus
    = Boolean.getBoolean("netbeans.debug.editor.caret.focus"); // NOI18N

    /** Component this caret is bound to */
    protected JTextComponent component;

    /** Position of the caret on the screen. This helps to compute
    * caret position on the next after jump.
    */
    Point magicCaretPosition;

    /** Draw mark designating the position of the caret.  */
    MarkFactory.DrawMark caretMark = new MarkFactory.CaretMark();

    /** Draw mark that supports caret mark in creating selection */
    MarkFactory.DrawMark selectionMark = new MarkFactory.DrawMark(
                                             DrawLayerFactory.CARET_LAYER_NAME, null);

    /** Is the caret visible */
    boolean visible;

    /** Caret is visible and the blink is visible. Both must be true
    * in order to show the caret.
    */
    boolean blinkVisible;

    /** Is the selection currently visible? */
    boolean selectionVisible;

    /** Listeners */
    protected EventListenerList listenerList = new EventListenerList();

    /** Timer used for blinking the caret */
    protected Timer flasher;

    /** Type of the caret */
    String type;

    /** Is the caret italic for italic fonts */
    boolean italic;

    private int xPoints[] = new int[4];
    private int yPoints[] = new int[4];
    private Action selectWordAction;
    private Action selectLineAction;

    /** Change event. Only one instance needed because it has only source property */
    protected ChangeEvent changeEvent;

    private static char emptyDotChar[] = { ' ' };

    /** Dot array of one character under caret */
    protected char dotChar[] = emptyDotChar;

    private boolean overwriteMode;

    /** Remembering document on which caret listens avoids
    * duplicate listener addition to SwingPropertyChangeSupport
    * due to the bug 4200280
    */
    private BaseDocument listenDoc;

    /** Caret draw graphics */
    CaretDG caretDG = new CaretDG();

    /** Font of the text underlying the caret. It can be used
    * in caret painting.
    */
    protected Font textFont;

    /** Font of the text right before the caret */
    protected Font previousFont;

    /** Foreground color of the text underlying the caret. It can be used
    * in caret painting.
    */
    protected Color textForeColor;

    /** Background color of the text underlying the caret. It can be used
    * in caret painting.
    */
    protected Color textBackColor;

    private PropertyChangeListener settingsListener;

    private transient FocusListener focusListener;

    static final long serialVersionUID =-9113841520331402768L;

    public BaseCaret() {
        Settings.addSettingsChangeListener(this);
    }

    /** Called when settings were changed. The method is called
    * also in constructor, so the code must count with the evt being null.
    */
    public void settingsChange(SettingsChangeEvent evt) {
        updateType();
    }

    void updateType() {
        JTextComponent c = component;
        if (c != null) {
            Class kitClass = Utilities.getKitClass(c);
            String newType;
            boolean newItalic;
            Color caretColor;
            if (overwriteMode) {
                newType = SettingsUtil.getString(kitClass,
                                                 Settings.CARET_TYPE_OVERWRITE_MODE, LINE_CARET);
                newItalic = SettingsUtil.getBoolean(kitClass,
                                                    Settings.CARET_ITALIC_OVERWRITE_MODE, false);
            } else { // insert mode
                newType = SettingsUtil.getString(kitClass,
                                                 Settings.CARET_TYPE_INSERT_MODE, LINE_CARET);
                newItalic = SettingsUtil.getBoolean(kitClass,
                                                    Settings.CARET_ITALIC_INSERT_MODE, false);
            }

            this.type = newType;
            this.italic = newItalic;

            dispatchUpdate();
        }
    }

    /** Called when UI is being installed into JTextComponent */
    public void install(JTextComponent c) {
        component = c;
        component.addPropertyChangeListener(this);
        focusListener = new FocusHandler(this);
        component.addFocusListener(focusListener);
        component.addMouseListener(this);
        component.addMouseMotionListener(this);

        ExtUI extUI = Utilities.getExtUI(component);
        extUI.addLayer(new DrawLayerFactory.CaretLayer());
        caretMark.setExtUI(extUI);
        selectionMark.setExtUI(extUI);

        BaseDocument doc = Utilities.getDocument(c);
        if (doc != null) {
            modelChanged(null, doc);
        }

        if (component.hasFocus()) {
            focusGained(null); // emulate focus gained
        }
    }

    /** Called when UI is being removed from JTextComponent */
    public void deinstall(JTextComponent c) {
        component = null; // invalidate

        if (flasher != null) {
            setBlinkRate(0);
        }

        Utilities.getExtUI(c).removeLayer(DrawLayerFactory.CARET_LAYER_NAME);

        c.removeMouseMotionListener(this);
        c.removeMouseListener(this);
        if (focusListener != null) {
            c.removeFocusListener(focusListener);
            focusListener = null;
        }
        c.removePropertyChangeListener(this);

        modelChanged(listenDoc, null);
    }

    protected void modelChanged(BaseDocument oldDoc, BaseDocument newDoc) {
        // [PENDING] !!! this body looks strange because of the bug 4200280
        if (oldDoc != null && listenDoc == oldDoc) {
            oldDoc.removeDocumentListener(this);

            try {
                caretMark.remove();
                selectionMark.remove();
            } catch (InvalidMarkException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            }

            listenDoc = null;
        }


        if (newDoc != null) {
            settingsChange(null); // update settings

            if (listenDoc != null) {
                // deinstall from the listenDoc first
                modelChanged(listenDoc, null);
            }

            newDoc.addDocumentListener(this);
            listenDoc = newDoc;

            try {
                Utilities.insertMark(newDoc, caretMark, 0);
                Utilities.insertMark(newDoc, selectionMark, 0);
            } catch (InvalidMarkException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            } catch (BadLocationException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            }

            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        updateType();
                    }
                }
            );

        }
    }

    /** Renders the caret */
    public void paint(Graphics g) {
        if (visible && blinkVisible) {
            paintCustomCaret(g);
        }
    }

    protected void paintCustomCaret(Graphics g) {
        JTextComponent c = component;
        if (c != null) { // && textFont != null) {
            ExtUI extUI = Utilities.getExtUI(c);
            if (THIN_LINE_CARET.equals(type)) { // thin line caret
                g.setColor(c.getCaretColor());
                int upperX = x;
                if (previousFont != null && previousFont.isItalic() && italic) {
                    upperX += Math.tan(previousFont.getItalicAngle()) * height;
                }
                g.drawLine((int)upperX, y, x, (y + height - 1));
            } else if (BLOCK_CARET.equals(type)) { // block caret
                g.setColor(c.getCaretColor());
                g.setFont(textFont);
                if (textFont.isItalic() && italic) {
                    int upperX = (int)(x + Math.tan(textFont.getItalicAngle()) * height);
                    xPoints[0] = upperX; yPoints[0] = y;
                    xPoints[1] = upperX + width; yPoints[1] = y;
                    xPoints[2] = x + width; yPoints[2] = y + height - 1;
                    xPoints[3] = x; yPoints[3] = y + height - 1;
                    g.fillPolygon(xPoints, yPoints, 4);
                } else {
                    g.fillRect(x, y, width, height);
                }
                g.setColor(Color.white);
                g.drawChars(dotChar, 0, 1, x, y + extUI.ascents[0]);
            } else { // two dot line caret
                g.setColor(c.getCaretColor());
                int blkWidth = 2;
                if (previousFont != null && previousFont.isItalic() && italic) {
                    int upperX = (int)(x + Math.tan(previousFont.getItalicAngle()) * height);
                    xPoints[0] = upperX; yPoints[0] = y;
                    xPoints[1] = upperX + blkWidth; yPoints[1] = y;
                    xPoints[2] = x + blkWidth; yPoints[2] = y + height - 1;
                    xPoints[3] = x; yPoints[3] = y + height - 1;
                    g.fillPolygon(xPoints, yPoints, 4);
                } else {
                    g.fillRect(x, y, blkWidth, height - 1);
                }
            }
        }
    }

    /** Update the caret's visual position */
    void dispatchUpdate() {
        dispatchUpdate(null, ExtUI.SCROLL_MOVE);
    }

    void dispatchUpdate(final Rectangle scrollRect, final int scrollPolicy) {
        Utilities.runInEventDispatchThread(
            new Runnable() {
                public void run() {
                    update(scrollRect, scrollPolicy);
                }
            }
        );
    }

    protected void update(Rectangle scrollRect, int scrollPolicy) {
        JTextComponent c = component;
        if (c != null) {
            BaseTextUI ui = (BaseTextUI)c.getUI();
            ExtUI extUI = ui.getExtUI();
            BaseDocument doc = Utilities.getDocument(c);
            if (doc != null) {
                if (scrollRect == null) {
                    scrollRect = this;
                }

                doc.readLock();
                try {
                    Rectangle oldCaretRect = new Rectangle(this);
                    int dot = getDot();
                    try {
                        ui.modelToViewDG(dot, caretDG);
                    } catch (BadLocationException e) {
                        // Sometimes thrown at document closing
                        // !!!          if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                        // !!!            e.printStackTrace();
                        // !!!          }
                    }
                    resetBlink();
                    if (!extUI.scrollRectToVisibleFragile(scrollRect, scrollPolicy)) {
                        oldCaretRect.add(this);
                        c.repaint(oldCaretRect);
                    }
                } finally {
                    doc.readUnlock();
                }
            }
        }
    }

    /** Redefine to Object.equals() to prevent defaulting to Rectangle.equals()
    * which would cause incorrect firing
    */
    public boolean equals(Object o) {
        return (this == o);
    }

    /** Adds listener to track when caret position was changed */
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    /** Removes listeners to caret position changes */
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    /** Notifies listeners that caret position has changed */
    protected void fireStateChanged() {
        Object listeners[] = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0 ; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }

    /** Is the caret currently visible */
    public final boolean isVisible() {
        return visible;
    }

    protected void setVisibleImpl(boolean v) {
        Timer t = flasher;
        if (t != null) {
            if (visible) {
                t.stop();
            }
            if (v) {
                t.start();
            } else {
                t.stop();
            }
        }
        visible = v;
        JTextComponent c = component;
        if (c != null) {
            c.repaint(this);
        }
    }

    void resetBlink() {
        Timer t = flasher;
        if (t != null) {
            t.stop();
            blinkVisible = true;
            t.start();
        }
    }

    /** Sets the caret visibility */
    public void setVisible(final boolean v) {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    setVisibleImpl(v);
                }
            }
        );
    }

    /** Is the selection visible? */
    public final boolean isSelectionVisible() {
        return selectionVisible;
    }

    /** Sets the selection visibility */
    public void setSelectionVisible(boolean v) {
        if (selectionVisible == v) {
            return;
        }
        JTextComponent c = component;
        if (c != null) {
            selectionVisible = v;
            if (selectionVisible) {
                int caretPos = getDot();
                int selPos = getMark();
                boolean selMarkFirst = (selPos < caretPos);
                selectionMark.activateLayer = selMarkFirst;
                caretMark.activateLayer = !selMarkFirst && !(selPos == caretPos);
            } else { // make selection invisible
                caretMark.activateLayer = false;
                selectionMark.activateLayer = false;
            }

            // repaint the block
            BaseTextUI ui = (BaseTextUI)c.getUI();
            try {
                ui.getExtUI().repaintBlock(caretMark.getOffset(), selectionMark.getOffset());
            } catch (BadLocationException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            } catch (InvalidMarkException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            }

        }
    }

    /** Saves the current caret position.  This is used when
    * caret up or down actions occur, moving between lines
    * that have uneven end positions.
    *
    * @param p  the Point to use for the saved position
    */
    public void setMagicCaretPosition(Point p) {
        magicCaretPosition = p;
    }

    /** Get position used to mark begining of the selected block */
    public final Point getMagicCaretPosition() {
        return magicCaretPosition;
    }

    /** Sets the caret blink rate.
    * @param rate blink rate in milliseconds, 0 means no blink
    */
    public synchronized void setBlinkRate(int rate) {
        if (flasher == null && rate > 0) {
            flasher = new Timer(rate, this);
        }
        if (flasher != null) {
            if (rate > 0) {
                if (flasher.getDelay() != rate) {
                    flasher.setDelay(rate);
                }
            } else { // zero rate - don't blink
                flasher.stop();
                flasher.removeActionListener(this);
                flasher = null;
            }
        }
    }

    /** Returns blink rate of the caret or 0 if caret doesn't blink */
    public synchronized int getBlinkRate() {
        return (flasher != null) ? flasher.getDelay() : 0;
    }

    /** Gets the current position of the caret */
    public int getDot() {
        if (component != null) {
            try {
                return caretMark.getOffset();
            } catch (InvalidMarkException e) {
            }
        }
        return 0;
    }

    /** Gets the current position of the selection mark.
    * If there's a selection this position will be different
    * from the caret position.
    */
    public int getMark() {
        if (component != null) {
            if (selectionVisible) {
                try {
                    return selectionMark.getOffset();
                } catch (InvalidMarkException e) {
                }
            } else { // selection not visible
                return getDot(); // must return same position as dot
            }
        }
        return 0;
    }

    public void setDot(int pos) {
        setDot(pos, null, ExtUI.SCROLL_DEFAULT);
    }

    /** Sets the caret position to some position.  This
    * causes removal of the active selection.
    */
    public void setDot(int pos, Rectangle scrollRect, int scrollPolicy) {
        JTextComponent c = component;
        if (c != null) {
            setSelectionVisible(false);
            BaseDocument doc = (BaseDocument)c.getDocument();
            if (doc != null) {
                try {
                    Utilities.moveMark(doc, caretMark, pos);
                } catch (BadLocationException e) {
                    // setting the caret to wrong position leaves it at current position
                } catch (InvalidMarkException e) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                        e.printStackTrace();
                    }
                }
            }
            fireStateChanged();
            dispatchUpdate(scrollRect, scrollPolicy);
        }
    }

    public void moveDot(int pos) {
        moveDot(pos, null, ExtUI.SCROLL_MOVE);
    }

    /** Makes selection by moving dot but leaving mark */
    public void moveDot(int pos, Rectangle scrollRect, int scrollPolicy) {
        JTextComponent c = component;
        if (c != null) {
            BaseDocument doc = (BaseDocument)c.getDocument();
            try {
                int oldCaretPos = getDot();
                if (pos == oldCaretPos) { // no change
                    return;
                }
                int selPos; // current position of selection mark

                if (selectionVisible) {
                    selPos = selectionMark.getOffset();
                } else {
                    Utilities.moveMark(doc, selectionMark, oldCaretPos);
                    selPos = oldCaretPos;
                }
                Utilities.moveMark(doc, caretMark, pos);
                if (selectionVisible) { // selection already visible
                    boolean selMarkFirst = (selPos < pos);
                    selectionMark.activateLayer = selMarkFirst;
                    caretMark.activateLayer = !selMarkFirst && !(selPos == pos);
                    Utilities.getExtUI(c).repaintBlock(oldCaretPos, pos);
                } else { // selection not yet visible
                    setSelectionVisible(true);
                }
            } catch (BadLocationException e) {
                // position is incorrect
            } catch (InvalidMarkException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            }
            fireStateChanged();
            dispatchUpdate(scrollRect, scrollPolicy);
        }
    }

    // DocumentListener methods
    public void insertUpdate(DocumentEvent evt) {
        JTextComponent c = component;
        if (c != null) {
            BaseDocument doc = (BaseDocument)component.getDocument();
            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
            if ((bevt.isInUndo() || bevt.isInRedo())
                    && component == Utilities.getLastActiveComponent()
               ) {

                // in undo mode and current component
                setDot(evt.getOffset() + evt.getLength());
            } else {
                fireStateChanged();
                if (evt.getLength() == 0) {
                    updateType();
                    setVisible(false);
                    setVisible(c.isEnabled());
                }
                dispatchUpdate();
            }
        }
    }

    public void removeUpdate(DocumentEvent evt) {
        JTextComponent c = component;
        if (c != null) {
            BaseDocument doc = (BaseDocument)c.getDocument();
            // make selection invisible if removal shrinked block to zero size
            if (selectionVisible && (getDot() == getMark())) {
                setSelectionVisible(false);
            }

            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
            if ((bevt.isInUndo() || bevt.isInRedo())
                    && c == Utilities.getLastActiveComponent()
               ) {
                // in undo mode and current component
                setDot(evt.getOffset());
            } else {
                fireStateChanged();
                dispatchUpdate();
            }
        }
    }

    public void changedUpdate(DocumentEvent evt) {
        dispatchUpdate();
    }

    // FocusListener methods
    public void focusGained(FocusEvent evt) {
        if (debugCaretFocus) {
            System.out.println("BaseCaret.focusGained() in doc="
                               + component.getDocument().getProperty(Document.TitleProperty));
        }

        JTextComponent c = component;
        if (c != null) {
            updateType();
            setVisible(c.isEnabled()); // invisible caret if disabled
        }
    }

    public void focusLost(FocusEvent evt) {
        if (debugCaretFocus) {
            System.out.println("BaseCaret.focusLost() in doc="
                               + component.getDocument().getProperty(Document.TitleProperty));
        }

        setVisible(false);
    }

    // MouseListener methods
    public void mouseClicked(MouseEvent evt) {
        JTextComponent c = component;
        if (c != null) {
            if (SwingUtilities.isLeftMouseButton(evt)) {
                if (evt.getClickCount() == 2) {
                    if (selectWordAction == null) {
                        BaseTextUI ui = (BaseTextUI)c.getUI();
                        selectWordAction = ((BaseKit)ui.getEditorKit(
                                                c)).getActionByName(BaseKit.selectWordAction);
                    }
                    selectWordAction.actionPerformed(null);
                } else if (evt.getClickCount() == 3) {
                    if (selectLineAction == null) {
                        BaseTextUI ui = (BaseTextUI)c.getUI();
                        selectLineAction = ((BaseKit)ui.getEditorKit(
                                                c)).getActionByName(BaseKit.selectLineAction);
                    }
                    selectLineAction.actionPerformed(null);
                }
            }
        }
    }

    public void mousePressed(MouseEvent evt) {
        JTextComponent c = component;
        if (c != null) {
            // Position the cursor at the appropriate place in the document
            if (SwingUtilities.isLeftMouseButton(evt) || !isSelectionVisible()) {
                int pos = ((BaseTextUI)c.getUI()).viewToModel(c,
                          evt.getX(), evt.getY());
                if (pos >= 0) {
                    if ((evt.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
                        moveDot(pos);
                    } else {
                        setDot(pos);
                    }
                    setMagicCaretPosition(null);
                }
                if (c.isEnabled()) {
                    c.requestFocus();
                }
            }

            // Show popup menu for right click
            if (SwingUtilities.isRightMouseButton(evt)) {
                Utilities.getExtUI(c).showPopupMenu(evt.getX(), evt.getY());
            }
        }
    }

    public void mouseReleased(MouseEvent evt) {
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }

    // MouseMotionListener methods
    public void mouseDragged(MouseEvent evt) {
        JTextComponent c = component;
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (c != null) {
                int pos = ((BaseTextUI)c.getUI()).viewToModel(c,
                          evt.getX(), evt.getY());
                moveDot(pos);
            }
        }
    }

    public void mouseMoved(MouseEvent evt) {
    }

    // PropertyChangeListener methods
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if ("document".equals(propName)) {
            BaseDocument oldDoc = (evt.getOldValue() instanceof BaseDocument)
                                  ? (BaseDocument)evt.getOldValue() : null;
            BaseDocument newDoc = (evt.getNewValue() instanceof BaseDocument)
                                  ? (BaseDocument)evt.getNewValue() : null;
            modelChanged(oldDoc, newDoc);

        } else if (ExtUI.OVERWRITE_MODE_PROPERTY.equals(propName)) {
            Boolean b = (Boolean)evt.getNewValue();
            overwriteMode = (b != null) ? b.booleanValue() : false;
            updateType();
        }
    }

    // ActionListener methods
    /** Fired when blink timer fires */
    public void actionPerformed(ActionEvent evt) {
        JTextComponent c = component;
        if (c != null) {
            blinkVisible = !blinkVisible;
            c.repaint(this);
        }
    }

    /** Caret draw graphics used to update the caret position
    * and the character the caret sits on.
    */
    final class CaretDG extends Drawer.AbstractDG {

        public boolean targetPosReached(int pos, char ch, int x, int y,
                                        int charWidth, DrawContext ctx, Font previousFont) {
            JTextComponent c = BaseCaret.this.component;
            if (c != null) {
                BaseCaret.this.x = x;
                BaseCaret.this.y = y;
                BaseCaret.this.width = charWidth;
                BaseCaret.this.height = Utilities.getExtUI(c).charHeight;
                BaseCaret.this.textFont = ctx.getFont();
                BaseCaret.this.previousFont = previousFont;
                BaseCaret.this.textForeColor = ctx.getForeColor();
                BaseCaret.this.textBackColor = ctx.getBackColor();
                BaseCaret.this.dotChar[0] = ch;
            }
            return false;
        }

    }

    private static class FocusHandler implements FocusListener {
        private transient FocusListener fl;

        FocusHandler(FocusListener fl) {
            this.fl = fl;
        }

        public void focusGained(FocusEvent e) {
            fl.focusGained(e);
        }

        public void focusLost(FocusEvent e) {
            fl.focusLost(e);
        }
    }

}

/*
 * Log
 *  49   Gandalf-post-FCS1.46.1.1    4/3/00   Miloslav Metelka undo update
 *  48   Gandalf-post-FCS1.46.1.0    3/8/00   Miloslav Metelka 
 *  47   Gandalf   1.46        1/18/00  Miloslav Metelka 
 *  46   Gandalf   1.45        1/16/00  Miloslav Metelka 
 *  45   Gandalf   1.44        1/13/00  Miloslav Metelka 
 *  44   Gandalf   1.43        1/10/00  Miloslav Metelka 
 *  43   Gandalf   1.42        1/7/00   Miloslav Metelka 
 *  42   Gandalf   1.41        1/4/00   Miloslav Metelka 
 *  41   Gandalf   1.40        12/28/99 Miloslav Metelka 
 *  40   Gandalf   1.39        11/27/99 Patrik Knakal   
 *  39   Gandalf   1.38        11/14/99 Miloslav Metelka 
 *  38   Gandalf   1.37        11/11/99 Miloslav Metelka 
 *  37   Gandalf   1.36        11/8/99  Miloslav Metelka 
 *  36   Gandalf   1.35        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  35   Gandalf   1.34        10/10/99 Miloslav Metelka 
 *  34   Gandalf   1.33        10/8/99  Miloslav Metelka Stability improvement
 *  33   Gandalf   1.32        10/4/99  Miloslav Metelka 
 *  32   Gandalf   1.31        9/15/99  Miloslav Metelka 
 *  31   Gandalf   1.30        8/17/99  Miloslav Metelka 
 *  30   Gandalf   1.29        8/9/99   Miloslav Metelka flasher resets synced
 *  29   Gandalf   1.28        7/29/99  Miloslav Metelka 
 *  28   Gandalf   1.27        7/22/99  Miloslav Metelka 
 *  27   Gandalf   1.26        7/20/99  Miloslav Metelka 
 *  26   Gandalf   1.25        7/9/99   Miloslav Metelka 
 *  25   Gandalf   1.24        7/2/99   Miloslav Metelka 
 *  24   Gandalf   1.23        6/29/99  Miloslav Metelka Scrolling and patches
 *  23   Gandalf   1.22        6/25/99  Miloslav Metelka from floats back to ints
 *  22   Gandalf   1.21        6/22/99  Miloslav Metelka 
 *  21   Gandalf   1.20        6/8/99   Miloslav Metelka 
 *  20   Gandalf   1.19        5/18/99  Miloslav Metelka getDot() fix
 *  19   Gandalf   1.18        5/16/99  Miloslav Metelka 
 *  18   Gandalf   1.17        5/15/99  Miloslav Metelka fixes
 *  17   Gandalf   1.16        5/13/99  Miloslav Metelka 
 *  16   Gandalf   1.15        5/7/99   Miloslav Metelka line numbering and fixes
 *  15   Gandalf   1.14        5/5/99   Miloslav Metelka 
 *  14   Gandalf   1.13        4/23/99  Miloslav Metelka Undo added and internal 
 *       improvements
 *  13   Gandalf   1.12        4/9/99   Miloslav Metelka 
 *  12   Gandalf   1.11        4/8/99   Miloslav Metelka 
 *  11   Gandalf   1.10        4/1/99   Miloslav Metelka 
 *  10   Gandalf   1.9         3/30/99  Miloslav Metelka 
 *  9    Gandalf   1.8         3/27/99  Miloslav Metelka 
 *  8    Gandalf   1.7         3/23/99  Miloslav Metelka 
 *  7    Gandalf   1.6         3/19/99  Miloslav Metelka 
 *  6    Gandalf   1.5         3/18/99  Miloslav Metelka 
 *  5    Gandalf   1.4         3/18/99  Miloslav Metelka 
 *  4    Gandalf   1.3         3/18/99  Miloslav Metelka 
 *  3    Gandalf   1.2         2/13/99  Miloslav Metelka 
 *  2    Gandalf   1.1         2/9/99   Miloslav Metelka 
 *  1    Gandalf   1.0         2/3/99   Miloslav Metelka 
 * $
 */

