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

package org.netbeans.modules.form;

import java.awt.*;
import java.awt.event.*;

/** A container that holds selected lightweight components.
* It paints a border around it to mark them "selected"
*
* @author   Ian Formanek
*/
final public class SelectionLayer extends Panel implements Selection {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -5620835638396311593L;
    private int borderSize = FormEditor.getFormSettings ().getSelectionBorderSize ();
    private Color borderColor = FormEditor.getFormSettings ().getSelectionBorderColor ();

    private static final int MIN_DRAG_DIST = 3;

    public static final int NO_BOX = -1;
    public static final int UPLEFT_BOX = 0;
    public static final int MIDLEFT_BOX = 1;
    public static final int LOLEFT_BOX = 2;
    public static final int UPRIGHT_BOX = 3;
    public static final int MIDRIGHT_BOX = 4;
    public static final int LORIGHT_BOX = 5;
    public static final int UPMID_BOX = 6;
    public static final int LOMID_BOX = 7;
    public static final int MOVE_BOX = 8;

    private boolean resizable = false;
    private boolean movable = false;

    private RADVisualComponent radVisualComponent;
    private Selection.ResizeListener rListener;
    private Selection.MoveListener mListener;

    // Resize drag context
    private int mode = NO_BOX;
    private boolean keepCursor = false;
    private boolean moveRequested = false;
    private boolean resizeRequested = false;
    private int requestedBox = NO_BOX;
    private Rectangle dragRect = new Rectangle ();
    private Point dragLoRight, dragUpLeft;
    private Point originalUpLeft;
    private Point originalLoRight;
    private Point originalPoint;


    /** Creates a new SelectionLayer. */
    public SelectionLayer (RADVisualComponent radComp, Component comp, Selection.ResizeListener rListener, Selection.MoveListener mListener) {
        radVisualComponent = radComp;
        this.rListener = rListener;
        this.mListener = mListener;
        selectionComponent = comp;
        selected = false;
        setLayout (new SelectionLayout());
        add (comp);
        addMouseListener (new SelMouseListener ());
        addMouseMotionListener (new SelMouseMotionListener ());
    }

    class SelMouseListener extends MouseAdapter {
        public void mousePressed (MouseEvent evt) {
            if (! (selected && (resizable || movable))) return;
            int pressedBox = getBox (evt.getPoint ());
            if (pressedBox == MOVE_BOX) {    // moving
                if (!movable) return;
                originalPoint = evt.getPoint ();
                keepCursor = true;
                moveRequested = true;
                Rectangle originalRect = getBounds ();
                dragUpLeft = new Point (originalRect.x + borderSize, originalRect.y + borderSize);
                originalUpLeft = new Point (dragUpLeft.x, dragUpLeft.y);
            }
            else if (pressedBox != NO_BOX) { // resizing
                if (!resizable) return;
                originalPoint = evt.getPoint ();
                keepCursor = true;
                resizeRequested = true;
                requestedBox = pressedBox;
                Rectangle originalRect = getBounds ();
                dragUpLeft = new Point (originalRect.x + borderSize, originalRect.y + borderSize);
                originalUpLeft = new Point (dragUpLeft.x, dragUpLeft.y);
                dragLoRight = new Point (
                                  originalRect.x + originalRect.width,
                                  originalRect.y + originalRect.height);
                originalLoRight = new Point (dragLoRight.x, dragLoRight.y);
            }
        }

        public void mouseReleased (MouseEvent evt) {
            if (! (selected && (resizable || movable))) return;
            if (mode == MOVE_BOX) {    // moving
                mListener.moveFinished ();
                mode = NO_BOX;
            }
            else if (mode != NO_BOX) {
                rListener.resizeFinished ();
                mode = NO_BOX;
            }
            if (keepCursor)
                keepCursor = false;
            moveRequested = false;
            resizeRequested = false;
        }

        public void mouseExited (MouseEvent evt) {
            if ((!keepCursor) && (mode == NO_BOX))
                setCursor (Cursor.getDefaultCursor ());
        }
    }

    class SelMouseMotionListener extends MouseMotionAdapter {
        public void mouseMoved (MouseEvent evt) {
            if (! (selected && (resizable || movable))) return;
            int box = getBox (evt.getPoint ());
            if (box == MOVE_BOX) {
                if (movable)
                    setCursor (Cursor.getPredefinedCursor (Cursor.MOVE_CURSOR));
            } else if (box == NO_BOX) {
                setCursor (Cursor.getDefaultCursor ());
            } else {
                if (!resizable) return;
                switch (box) {
                case UPLEFT_BOX: setCursor (Cursor.getPredefinedCursor (Cursor.NW_RESIZE_CURSOR)); break;
                case MIDLEFT_BOX: setCursor (Cursor.getPredefinedCursor (Cursor.W_RESIZE_CURSOR)); break;
                case LOLEFT_BOX: setCursor (Cursor.getPredefinedCursor (Cursor.SW_RESIZE_CURSOR)); break;
                case UPRIGHT_BOX: setCursor (Cursor.getPredefinedCursor (Cursor.NE_RESIZE_CURSOR)); break;
                case MIDRIGHT_BOX: setCursor (Cursor.getPredefinedCursor (Cursor.E_RESIZE_CURSOR)); break;
                case LORIGHT_BOX: setCursor (Cursor.getPredefinedCursor (Cursor.SE_RESIZE_CURSOR)); break;
                case UPMID_BOX: setCursor (Cursor.getPredefinedCursor (Cursor.N_RESIZE_CURSOR)); break;
                case LOMID_BOX: setCursor (Cursor.getPredefinedCursor (Cursor.S_RESIZE_CURSOR)); break;
                }
            }
        }

        public void mouseDragged (MouseEvent evt) {
            if (! (selected && (resizable || movable))) return;
            Point pos = evt.getPoint ();
            if (moveRequested) {
                if ((Math.abs (pos.x - originalPoint.x) > MIN_DRAG_DIST) ||
                        (Math.abs (pos.y - originalPoint.y) > MIN_DRAG_DIST)) {
                    mListener.moveStarted (radVisualComponent);
                    mode = MOVE_BOX;
                }
            }
            else if (resizeRequested) {
                if ((Math.abs (pos.x - originalPoint.x) > MIN_DRAG_DIST) ||
                        (Math.abs (pos.y - originalPoint.y) > MIN_DRAG_DIST)) {
                    rListener.resizeStarted (radVisualComponent);
                    mode = requestedBox;
                }
            }

            if (mode == MOVE_BOX) {
                dragUpLeft.x = originalUpLeft.x + (pos.x - originalPoint.x);
                dragUpLeft.y = originalUpLeft.y + (pos.y - originalPoint.y);
                mListener.moveTo (dragUpLeft, originalPoint);
            } else if (mode != NO_BOX) {
                switch (mode) {
                case UPLEFT_BOX:
                    dragUpLeft.x = originalUpLeft.x + (pos.x - originalPoint.x);
                    dragUpLeft.y = originalUpLeft.y + (pos.y - originalPoint.y);
                    break;
                case MIDLEFT_BOX:
                    dragUpLeft.x = originalUpLeft.x + (pos.x - originalPoint.x);
                    break;
                case LOLEFT_BOX:
                    dragUpLeft.x = originalUpLeft.x + (pos.x - originalPoint.x);
                    dragLoRight.y = originalLoRight.y + (pos.y - originalPoint.y);
                    break;
                case UPRIGHT_BOX:
                    dragLoRight.x = originalLoRight.x + (pos.x - originalPoint.x);
                    dragUpLeft.y = originalUpLeft.y + (pos.y - originalPoint.y);
                    break;
                case MIDRIGHT_BOX:
                    dragLoRight.x = originalLoRight.x + (pos.x - originalPoint.x);
                    break;
                case LORIGHT_BOX:
                    dragLoRight.x = originalLoRight.x + (pos.x - originalPoint.x);
                    dragLoRight.y = originalLoRight.y + (pos.y - originalPoint.y);
                    break;
                case LOMID_BOX:
                    dragLoRight.y = originalLoRight.y + (pos.y - originalPoint.y);
                    break;
                case UPMID_BOX:
                    dragUpLeft.y = originalUpLeft.y + (pos.y - originalPoint.y);
                    break;
                }

                dragRect.x = dragUpLeft.x;
                dragRect.y = dragUpLeft.y;
                dragRect.width = dragLoRight.x - dragUpLeft.x;
                dragRect.height = dragLoRight.y - dragUpLeft.y;

                rListener.resizeTo (dragRect);
            }
        }
    }

    private int getBox (Point pos) {
        Dimension size = getSize ();
        int midHor = (size.width - borderSize ) / 2;
        int midVer = (size.height - borderSize) / 2;
        if ((pos.x < 0) || (pos.y < 0) || (pos.x >= size.width) || (pos.y >= size.height))
            return NO_BOX;
        if (pos.x < borderSize) { // left edge
            if (pos.y < borderSize)
                return UPLEFT_BOX;
            else if ((pos.y >= midVer) && (pos.y < midVer + borderSize))
                return MIDLEFT_BOX;
            else if (pos.y >= size.height - borderSize)
                return LOLEFT_BOX;
            else return MOVE_BOX;
        }
        else if (pos.x >= size.width - borderSize) { // right edge
            if (pos.y < borderSize)
                return UPRIGHT_BOX;
            else if ((pos.y >= midVer) && (pos.y < midVer + borderSize))
                return MIDRIGHT_BOX;
            else if (pos.y >= size.height - borderSize)
                return LORIGHT_BOX;
            else return MOVE_BOX;
        } else if ((pos.x >= midHor) && (pos.x < midHor + borderSize)) { // mid
            if (pos.y < borderSize)
                return UPMID_BOX;
            else if (pos.y >= size.height - borderSize)
                return LOMID_BOX;
        } else if ((pos.y < borderSize) || (pos.y >= size.height - borderSize))
            return MOVE_BOX;

        return NO_BOX;
    }

    // -----------------------------------------------------------------------------
    // Selection implementation

    /** Called to change the selection state of this selection wrapper.
    * @param sel the new selection state
    * @param conn true for connection mode, false for plain selection
    */
    public void setSelected (boolean sel, boolean conn) {
        selected = sel;
        if (getParent () != null) {
            if (selected) { // update the settings
                borderSize = FormEditor.getFormSettings ().getSelectionBorderSize ();
                if (conn) {
                    borderColor = FormEditor.getFormSettings ().getConnectionBorderColor ();
                } else {
                    borderColor = FormEditor.getFormSettings ().getSelectionBorderColor ();
                }
            }
            invalidate ();
            getParent ().validate ();
            repaint ();
        }
    }

    /** @return the selection state of this selection wrapper -
                true if selected, false otherwise
    */
    public boolean isSelected () {
        return selected;
    }

    public void setResizable (boolean resizable) {
        if (this.resizable == resizable) return;
        this.resizable = resizable;
        repaint ();
    }

    public boolean isResizable () {
        return resizable;
    }

    public void setMovable (boolean movable) {
        if (this.movable == movable) return;
        this.movable = movable;
        repaint ();
    }

    public boolean isMovable () {
        return movable;
    }

    // -----------------------------------------------------------------------------
    // Border painting

    public void paint (Graphics g) {
        if (isSelected ()) {
            Dimension size = getSize();
            int midHor = (size.width - borderSize ) / 2;
            int midVer = (size.height - borderSize) / 2;
            g.setColor (borderColor);
            g.fillRect (0, 0, borderSize, borderSize); // UpLeft
            g.fillRect (size.width-borderSize, 0, borderSize, borderSize); // UpRight
            g.fillRect (0, size.height-borderSize, borderSize, borderSize); // LoLeft
            g.fillRect (size.width-borderSize, size.height-borderSize, borderSize, borderSize); // LoRight

            if (resizable) {
                g.fillRect (midHor, 0, borderSize, borderSize); // UpMid
                g.fillRect (0, midVer, borderSize, borderSize); // LeftMid
                g.fillRect (size.width-borderSize, midVer, borderSize, borderSize); // RightMid
                g.fillRect (midHor, size.height-borderSize, borderSize, borderSize); // LoMid
            }
        }
        super.paint(g);
    }

    final class SelectionLayout implements LayoutManager {
        public void addLayoutComponent(String name, Component comp) {
        }

        public void removeLayoutComponent(Component comp) {
        }

        public Dimension preferredLayoutSize(Container parent) {
            if (isSelected ()) {
                Dimension compPref = selectionComponent.getPreferredSize();
                return new Dimension (compPref.width + 2*borderSize, compPref.height + 2*borderSize);
            } else
                return selectionComponent.getPreferredSize ();
        }

        public Dimension minimumLayoutSize(Container parent) {
            if (isSelected ()) {
                Dimension compMin = selectionComponent.getMinimumSize();
                return new Dimension (compMin.width + 2*borderSize, compMin.height + 2*borderSize);
            } else
                return selectionComponent.getMinimumSize ();
        }

        public void layoutContainer(Container parent) {
            Dimension size = parent.getSize();
            if (isSelected ())
                selectionComponent.setBounds (borderSize, borderSize, size.width - 2*borderSize, size.height - 2*borderSize);
            else
                selectionComponent.setBounds (0, 0, size.width, size.height);

        }
    }

    private Component selectionComponent;
    private boolean selected;
}

/*
 * Log
 *  3    Gandalf   1.2         12/13/99 Pavel Buzek     
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         5/14/99  Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Formanek    bugfix
 */
