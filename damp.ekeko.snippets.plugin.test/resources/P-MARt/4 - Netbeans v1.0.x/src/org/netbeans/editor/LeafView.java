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

import java.awt.Insets;
import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.Rectangle;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.Position;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;

/**
* Leaf view implementation. This corresponds and requires leaf element
* to be element for this view.
*
* The view has the following structure:
*  +---------------------------------------------------------+  
*  |                       insets.top area                   | A
*  |                                                         | | insets.top
*  |                                                         | V
*  |      +--------------------------------------------------+
*  |      |                                                  | A
*  |      |                                                  | |
*  |  i   |                                                  | |
*  |  n   |                                                  | |
*  |  s   |                                                  | |
*  |  e   |                                                  | |
*  |  t   |                                                  | |
*  |  s   |                                                  | |
*  |  .   |                                                  | |
*  |  l   |                                                  | |
*  |  e   |              Main area of this view              | | mainHeight
*  |  f   |                                                  | |
*  |  t   |                                                  | |
*  |      |                                                  | |
*  |  a   |                                                  | |
*  |  r   |                                                  | |
*  |  e   |                                                  | |
*  |  a   |                                                  | |
*  |      |                                                  | |
*  |      |                                                  | |
*  |      |                                                  | |
*  |      |                                                  | |
*  |      |                                                  | V
*  |      +--------------------------------------------------+
*  |                       insets.bottom area                | A
*  |                                                         | | insets.bottom
*  |                                                         | V
*  +---------------------------------------------------------+
*
* @author  Miloslav Metelka
* @version 1.00
*/

public class LeafView extends BaseView {

    /** Height of the area this view manages excluding areas
    * managed by its children and excluding insets.
    */
    protected int mainHeight;

    /** Draw graphics for converting position to coords */
    ModelToViewDG modelToViewDG = new ModelToViewDG();

    /** Draw graphics for converting coords to position */
    ViewToModelDG viewToModelDG = new ViewToModelDG();

    /** Construct new base view */
    public LeafView(Element elem) {
        super(elem);
    }

    /** Returns binary composition of paint areas */
    protected int getPaintAreas(Graphics g, int clipY, int clipHeight) {
        // invalid or empty height
        if (clipHeight <= 0) {
            return 0;
        }

        int clipEndY = clipY + clipHeight;
        int startY = getStartY();
        if (insets != null) { // valid insets
            int mainAreaY = startY + insets.top;
            if (clipEndY <= mainAreaY) {
                return INSETS_TOP;
            }
            int bottomInsetsY = mainAreaY + mainHeight;
            if (clipEndY <= bottomInsetsY) {
                if (clipY <= mainAreaY) {
                    return INSETS_TOP + MAIN_AREA;
                } else {
                    return MAIN_AREA;
                }
            }
            if (clipY <= mainAreaY) {
                return INSETS_TOP + MAIN_AREA + INSETS_BOTTOM;
            } else if (clipY <= bottomInsetsY) {
                return MAIN_AREA + INSETS_BOTTOM;
            } else if (clipY <= bottomInsetsY + insets.bottom) {
                return INSETS_BOTTOM;
            } else {
                return 0;
            }
        } else { // no insets
            if (clipEndY <= startY || clipY >= startY + getHeight()) {
                return 0;
            } else {
                return MAIN_AREA;
            }
        }
    }

    /** Paint either top insets, main area, or bottom insets depending on paintAreas variable */
    protected void paintAreas(Graphics g, int clipY, int clipHeight, int paintAreas) {
        if ((paintAreas & MAIN_AREA) == MAIN_AREA) {
            ExtUI extUI = getExtUI();
            int paintY = Math.max(clipY, 0); // relative start of area to paint
            int startPos = getPosFromY(paintY);
            int endPos = getPosFromY(clipY + clipHeight + (extUI.charHeight - 1));
            int baseY;
            try {
                baseY = getYFromPos(startPos);
                Drawer.getDrawer().draw(new Drawer.GraphicsDG(g),
                                        extUI, startPos, endPos,
                                        getBaseX(baseY), baseY, Integer.MAX_VALUE);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    /** Get total height of this view */
    public int getHeight() {
        if (insets != null) {
            return insets.top + mainHeight + insets.bottom;
        } else {
            return mainHeight;
        }
    }

    /** Compute and update main area height */
    public void updateMainHeight() {
        LeafElement elem = (LeafElement)getElement(); // need leaf element
        try {
            int lineDiff = (elem.getEndMark().getLine() - elem.getStartMark().getLine()
                            + 1);
            mainHeight = lineDiff * getExtUI().charHeight;
        } catch (InvalidMarkException e) {
            mainHeight = 0;
        }
    }

    /** Get begin of line position from y-coord.
    * If the position is before main area begining
    * it returns start position. If it's beyond the end of view it returns
    * end position.
    * @param y y-coord to inspect
    *   always returns startOffset for y < start of main area
    * @param eol means to return end of specified line instead of begining
    * @return position in the document
    */
    protected int getPosFromY(int y) {
        // relative distance from begining of main area
        int relY = y - getStartY() - ((insets != null) ? insets.top : 0);
        if (relY < 0) { // before the view
            return getStartOffset();
        }
        if (relY >= mainHeight) { // beyond the view
            return getEndOffset();
        }

        int line = 0;
        // get the begining line of the element
        try {
            line = ((BaseElement)getElement()).getStartMark().getLine();
        } catch (InvalidMarkException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }
        // advance the line by by relative distance
        line += relY / getExtUI().charHeight;

        int startOffset = getStartOffset();
        int pos;
        pos = Utilities.getRowStartFromLineOffset(((BaseDocument)getDocument()), line);
        if (pos == -1) {
            pos = startOffset;
        }
        return Math.max(pos, startOffset);
    }

    public int getBaseX(int y) {
        return getExtUI().textMargin.left + ((insets != null) ? insets.left : 0);
    }

    /** Returns the number of child views in this view. */
    public final int getViewCount() {
        return 0;
    }

    /** Gets the n-th child view.  */
    public final View getView(int n) {
        return null;
    }

    /** !!! osetrit konec view -> jump na dalsi v branchview */
    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a,
                                         int direction, Position.Bias[] biasRet)
    throws BadLocationException {
        if (biasRet != null) {
            biasRet[0] = Position.Bias.Forward;
        }
        switch (direction) {
        case NORTH:
            {
                try {
                    BaseDocument doc = (BaseDocument)getDocument();
                    int visCol = doc.op.getVisColFromPos(pos);
                    pos = doc.op.getOffsetFromVisCol(visCol, doc.op.getBOLRelLine(pos, -1));
                } catch (BadLocationException e) {
                    // leave the original position
                }
                return pos;
            }
        case SOUTH:
            {
                try {
                    BaseDocument doc = (BaseDocument)getDocument();
                    int visCol = doc.op.getVisColFromPos(pos);
                    pos = doc.op.getOffsetFromVisCol(visCol, doc.op.getBOLRelLine(pos, 1));
                } catch (BadLocationException e) {
                    // leave the original position
                }
                return pos;
            }
        case WEST:
            return (pos == -1) ? getStartOffset() : (pos - 1);
        case EAST:
            return (pos == -1) ? getEndOffset() : (pos + 1);
        default:
            throw new IllegalArgumentException("Bad direction: " + direction); // NOI18N
        }
    }

    /** Get y coordinate from position.
    * The position can lay anywhere inside this view.
    */
    protected int getYFromPos(int pos) throws BadLocationException {
        int relLine = 0;
        try {
            relLine = ((BaseDocument)getDocument()).op.getLine(pos)
                      - ((BaseElement)getElement()).getStartMark().getLine();
        } catch (InvalidMarkException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }
        return getStartY() + ((insets != null) ? insets.top : 0)
               + relLine * getExtUI().charHeight;
    }

    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        ExtUI extUI = getExtUI();
        Rectangle r = new Rectangle(0, 0, extUI.spaceWidths[0], extUI.charHeight);
        BaseDocument doc = (BaseDocument)getDocument();
        r.y = getYFromPos(pos);
        if (extUI.superFixedFont) { // all chars in all styles have same width
            r.x = getBaseX(r.y) + doc.op.getVisColFromPos(pos) * extUI.spaceWidths[0];
            return r;
        } else { // not superfixed font
            try {
                synchronized (modelToViewDG) {
                    modelToViewDG.setRect(r);
                    int bolPos = doc.op.getBOL(pos);
                    int eolPos = doc.op.getEOL(pos);
                    Drawer.getDrawer().draw(modelToViewDG, extUI, bolPos, eolPos,
                                            getBaseX(r.y), 0, pos);
                }
            } catch (BadLocationException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            }
            return r;
        }
    }

    public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1,
                             Shape a) throws BadLocationException {
        Rectangle r0 = (Rectangle)modelToView(p0, a, b0);
        Rectangle r1 = (Rectangle)modelToView(p1, a, b1);
        if (r0.y != r1.y) {
            // If it spans lines, force it to be the width of the view.
            r0.x = getComponent().getX();
            r0.width = getComponent().getWidth();
        }
        r0.add(r1);
        return r0;
    }

    public void modelToViewDG(int pos, Drawer.DrawGraphics dg)
    throws BadLocationException {
        ExtUI extUI = getExtUI();
        BaseDocument doc = (BaseDocument)getDocument();
        int y = getYFromPos(pos);
        Drawer.getDrawer().draw(dg, extUI, doc.op.getBOL(pos),
                                doc.op.getEOL(pos), getBaseX(y), y, pos);
    }

    /** Get position from location on screen.
    * @param x the X coordinate >= 0
    * @param y the Y coordinate >= 0
    * @param a the allocated region to render into
    * @return the location within the model that best represents the
    *  given point in the view >= 0
    */
    public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
        int intX = (int)x;
        int intY = (int)y;
        if (biasReturn != null) {
            biasReturn[0] = Position.Bias.Forward;
        }
        int begMainY = getStartY() + ((insets != null) ? insets.top : 0);
        if (intY < begMainY) { // top insets or before this view
            return -1; // getStartOffset();
        } else if (intY > begMainY + mainHeight) { // bottom insets or beyond
            return getEndOffset();
        } else { // inside the view
            int pos = getPosFromY(intY); // first get BOL of target line
            ExtUI extUI = getExtUI();
            try {
                if (extUI.superFixedFont) {
                    intX -= extUI.textMargin.left; // substract possible line numbering width
                    if (intX > 0) {
                        pos = ((BaseDocument)getDocument()).op.getOffsetFromVisCol(
                                  intX / getExtUI().spaceWidths[0], pos);
                    }
                } else { // not superfixed font
                    int eolPos = ((BaseDocument)getDocument()).op.getEOL(pos);
                    synchronized (viewToModelDG) {
                        viewToModelDG.setX(intX);
                        viewToModelDG.pos = eolPos;
                        Drawer.getDrawer().draw(viewToModelDG, extUI, pos, eolPos,
                                                getBaseX(intY), 0, -1);
                        pos = viewToModelDG.getOffset();
                    }
                }
            } catch (BadLocationException e) {
                // return begining of line in this case
            }
            return pos;
        }
    }

    /** Gives notification that something was inserted into the document
    * in a location that this view is responsible for.
    *
    * @param e the change information from the associated document
    * @param a the current allocation of the view
    * @param f the factory to use to rebuild if the view has children
    */
    public void insertUpdate(DocumentEvent evt, Shape a, ViewFactory f) {
        if (evt.getLength() == 0) { // initial read was performed
            updateMainHeight();

        } else { // regular insertion (or undone removal)

            try {
                BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
                ExtUI extUI = getExtUI();
                int y = getYFromPos(evt.getOffset());
                int lineHeight = extUI.charHeight;
                if (bevt.getLFCount() > 0) { // one or more lines inserted
                    int addHeight = bevt.getLFCount() * lineHeight;
                    mainHeight += addHeight;
                    extUI.repaint(y);
                }

                int syntaxY = getYFromPos(bevt.getSyntaxUpdateOffset());
                // !!! patch for case when DocMarksOp.eolMark is at the end of document
                if (bevt.getSyntaxUpdateOffset() == evt.getDocument().getLength()) {
                    syntaxY += lineHeight;
                }

                if (getComponent().isShowing()) {
                    extUI.repaint(y, Math.max(lineHeight, syntaxY - y));
                }

            } catch (BadLocationException ex) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    ex.printStackTrace();
                }
            }

        }

    }

    /** Gives notification from the document that attributes were removed
    * in a location that this view is responsible for.
    *
    * @param e the change information from the associated document
    * @param a the current allocation of the view
    * @param f the factory to use to rebuild if the view has children
    */
    public void removeUpdate(DocumentEvent evt, Shape a, ViewFactory f) {
        try {
            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
            ExtUI extUI = getExtUI();
            int y = getYFromPos(evt.getOffset());
            int lineHeight = extUI.charHeight;
            if (bevt.getLFCount() > 0) { // one or more lines removed
                int removeHeight = bevt.getLFCount() * lineHeight;
                mainHeight -= removeHeight;
                extUI.repaint(y);
            }
            int syntaxY = getYFromPos(bevt.getSyntaxUpdateOffset());
            // !!! patch for case when DocMarksOp.eolMark is at the end of document
            if (bevt.getSyntaxUpdateOffset() == evt.getDocument().getLength()) {
                syntaxY += lineHeight;
            }

            if (getComponent().isShowing()) {
                extUI.repaint(y, Math.max(lineHeight, syntaxY - y));
            }

        } catch (BadLocationException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace();
            }
        }
    }

    /** Attributes were changed in the are this view is responsible for.
    * @param e the change information from the associated document
    * @param a the current allocation of the view
    * @param f the factory to use to rebuild if the view has children
    */
    public void changedUpdate(DocumentEvent evt, Shape a, ViewFactory f) {
        try {
            if (getComponent().isShowing()) {
                getExtUI().repaintBlock(evt.getOffset(), evt.getOffset() + evt.getLength());
            }
        } catch (BadLocationException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace();
            }
        }
    }

    /** Update view after changes were made to the model. */
    protected void update(DocumentEvent e, Shape a, ViewFactory f) {
        JTextComponent comp = getComponent();
        boolean isShowing =  comp.isShowing(); // !!! repaint based on this flag
        BaseDocumentEvent evt = (BaseDocumentEvent)e;
        int pos = evt.getOffset();
        int docLen = comp.getDocument().getLength();
        ExtUI extUI = getExtUI();
        int charHeight = extUI.charHeight;
        try {
            int y = getYFromPos(pos);
            if (evt.getType() == DocumentEvent.EventType.INSERT) {
                if (evt.getLFCount() > 0) { // one or more lines inserted
                    int addHeight = evt.getLFCount() * charHeight;
                    mainHeight += addHeight;
                    extUI.repaint(y);
                }
                int syntaxY = getYFromPos(evt.getSyntaxUpdateOffset());
                if (evt.getSyntaxUpdateOffset() == docLen) { // !!! patch for situation when DocMarksOp.eolMark is at the end of document
                    syntaxY += charHeight;
                }

                extUI.repaint(y, Math.max(charHeight, syntaxY - y));
            } else if (evt.getType() == DocumentEvent.EventType.REMOVE) {
                if (evt.getLFCount() > 0) { // one or more lines removed
                    int removeHeight = evt.getLFCount() * charHeight;
                    mainHeight -= removeHeight;
                    extUI.repaint(y);
                }
                int syntaxY = getYFromPos(evt.getSyntaxUpdateOffset());
                if (evt.getSyntaxUpdateOffset() == docLen) { // !!! patch for situation when DocMarksOp.eolMark is at the end of document
                    syntaxY += charHeight;
                }
                extUI.repaint(y, Math.max(charHeight, syntaxY - y));
            } else { // changed update
                extUI.repaintBlock(pos, pos + evt.getLength()); // repaint that area
            }
        } catch (BadLocationException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace();
            }
        }
    }

    /** Get child view's y base value. Invalid in this case. */
    protected int getViewStartY(BaseView view, int helperInd) {
        return 0; // invalid in this case
    }

    static final class ModelToViewDG extends Drawer.AbstractDG {

        Rectangle curRect;

        void setRect(Rectangle curRect) {
            this.curRect = curRect;
        }

        public boolean targetPosReached(int pos, char ch, int x, int y,
                                        int charWidth, DrawContext ctx, Font previousFont) {
            curRect.x = x;
            return false;
        }

    }

    static final class ViewToModelDG extends Drawer.AbstractDG {

        int targetX;

        int pos;

        void setX(int targetX) {
            this.targetX = targetX;
        }

        int getOffset() {
            return pos;
        }

        public boolean targetPosReached(int pos, char ch, int x, int y,
                                        int charWidth, DrawContext ctx, Font previousFont) {
            if (x + charWidth < targetX) {
                this.pos = pos;
                return true;

            } else { // x + charWidth >= targetX
                this.pos = pos;
                if (ch != '\n' && targetX >= x + charWidth / 2) {
                    Document doc = ctx.getExtUI().getDocument();
                    if (doc != null && pos + 1 <= doc.getLength()) {
                        this.pos = pos + 1;
                    }
                }
                return false;
            }
        }

    }

}

/*
 * Log
 *  29   Gandalf-post-FCS1.23.1.4    4/18/00  Miloslav Metelka cursor move fix
 *  28   Gandalf-post-FCS1.23.1.3    4/17/00  Miloslav Metelka fixed ghost tabs
 *  27   Gandalf-post-FCS1.23.1.2    4/17/00  Miloslav Metelka unselecting empty line
 *  26   Gandalf-post-FCS1.23.1.1    4/3/00   Miloslav Metelka undo update
 *  25   Gandalf-post-FCS1.23.1.0    3/8/00   Miloslav Metelka 
 *  24   Gandalf   1.23        1/13/00  Miloslav Metelka 
 *  23   Gandalf   1.22        1/10/00  Miloslav Metelka 
 *  22   Gandalf   1.21        12/28/99 Miloslav Metelka 
 *  21   Gandalf   1.20        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  20   Gandalf   1.19        10/10/99 Miloslav Metelka 
 *  19   Gandalf   1.18        10/8/99  Miloslav Metelka stability improvements
 *  18   Gandalf   1.17        9/10/99  Miloslav Metelka 
 *  17   Gandalf   1.16        8/27/99  Miloslav Metelka 
 *  16   Gandalf   1.15        7/2/99   Miloslav Metelka 
 *  15   Gandalf   1.14        6/29/99  Miloslav Metelka Scrolling and patches
 *  14   Gandalf   1.13        6/25/99  Miloslav Metelka from floats back to ints
 *  13   Gandalf   1.12        6/1/99   Miloslav Metelka 
 *  12   Gandalf   1.11        5/15/99  Miloslav Metelka fixes
 *  11   Gandalf   1.10        5/13/99  Miloslav Metelka 
 *  10   Gandalf   1.9         5/7/99   Miloslav Metelka line numbering and fixes
 *  9    Gandalf   1.8         5/5/99   Miloslav Metelka 
 *  8    Gandalf   1.7         4/23/99  Miloslav Metelka Undo added and internal 
 *       improvements
 *  7    Gandalf   1.6         4/8/99   Miloslav Metelka 
 *  6    Gandalf   1.5         3/30/99  Miloslav Metelka 
 *  5    Gandalf   1.4         3/27/99  Miloslav Metelka 
 *  4    Gandalf   1.3         3/18/99  Miloslav Metelka 
 *  3    Gandalf   1.2         2/13/99  Miloslav Metelka 
 *  2    Gandalf   1.1         2/9/99   Miloslav Metelka 
 *  1    Gandalf   1.0         2/3/99   Miloslav Metelka 
 * $
 */

