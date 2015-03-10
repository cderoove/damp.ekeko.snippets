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

import java.util.*;
import java.awt.Dimension;
import java.awt.Rectangle;

import org.openide.awt.Toolbar;

/** An object that encapsulates position and (optionally) size for
 * Absolute positioning of components.
 *
 * @see ToolbarLayout
 * @author Libor Kramolis
 */
public class ToolbarConstraints {//implements Comparable {
    static final long serialVersionUID =3065774641403311880L;

    static final int LEFT_ANCHOR  = -1;
    static final int NO_ANCHOR    =  0;

    private String    name;
    private int       position;
    private int       anchor;    // LEFT_ANCHOR | NO_ANCHOR
    private boolean   visible;

    /**
     * @associates ToolbarRow 
     */
    private Vector    ownRows;   // Vector of ToolbarRows


    /**
     * @associates ToolbarConstraints 
     */
    private Vector    prevBars;  // Vector of ToolbarConstraints


    /**
     * @associates ToolbarConstraints 
     */
    private Vector    nextBars;  // Vector of ToolbarConstraints
    private int       prevEnd;   // nejblizsi konec predchozich toolbaru
    private int       nextBeg;   // nejblizsi konec nasledujicich toolbaru
    private int       prevBeg;   // nejblizsi konec predchozich toolbaru
    private int       nextEnd;   // nejblizsi konec nasledujicich toolbaru

    private Dimension prefSize;
    private Rectangle bounds;
    private ToolbarConfiguration toolbarConfig;
    private int       rowCount;
    private int       prefLastWidth;

    ToolbarConstraints (ToolbarConfiguration conf, String nam, Integer pos, Boolean vis) {
        toolbarConfig = conf;
        name = nam;
        if (pos == null) {
            position = 0;
            anchor = LEFT_ANCHOR;
        } else {
            position = pos.intValue();
            anchor = NO_ANCHOR;
        }
        visible = vis.booleanValue();

        prefSize = new Dimension ();
        rowCount = 0;
        prefLastWidth = 0;
        bounds = new Rectangle ();

        initValues();
    }


    void initValues () {
        ownRows = new Vector();
        prevBars = new Vector();
        nextBars = new Vector();

        resetPrev();
        resetNext();
    }

    void checkNextPosition (Integer position, Boolean visible) {
        if (position == null) {
            this.position = 0;
            this.anchor = LEFT_ANCHOR;
        } else {
            if (anchor == NO_ANCHOR)
                this.position = (this.position + position.intValue()) / 2;
            else
                this.position = position.intValue();
            this.anchor = NO_ANCHOR;
        }
        this.visible = this.visible || visible.booleanValue();
    }

    String getName () {
        return name;
    }

    int getAnchor () {
        return anchor;
    }

    void setAnchor (int anch) {
        anchor = anch;
    }

    boolean isVisible () {
        return visible;
    }

    void setVisible (boolean v) {
        visible = v;
    }

    int getPosition () {
        return position;
    }

    void setPosition (int pos) {
        position = pos;
    }

    int getWidth () {
        return prefSize.width;
    }

    int getRowCount () {
        return rowCount;
    }

    Rectangle getBounds () {
        return new Rectangle (bounds);
    }

    boolean destroy () {
        Iterator it = ownRows.iterator();
        ToolbarRow row;
        boolean emptyRow = false;
        while (it.hasNext()) {
            row = (ToolbarRow)it.next();
            row.removeToolbar (this);
            emptyRow = emptyRow || row.isEmpty();
        }

        initValues();
        return emptyRow;
    }

    void addOwnRow (ToolbarRow row) {
        ownRows.add (row);
    }

    void addPrevBar (ToolbarConstraints prev) {
        if (prev == null)
            return;
        prevBars.add (prev);
    }

    void addNextBar (ToolbarConstraints next) {
        if (next == null)
            return;
        nextBars.add (next);
    }

    void removePrevBar (ToolbarConstraints prev) {
        if (prev == null)
            return;
        prevBars.remove (prev);
    }

    void removeNextBar (ToolbarConstraints next) {
        if (next == null)
            return;
        nextBars.remove (next);
    }

    void setPreferredSize (Dimension size) {
        prefSize = size;
        rowCount = Toolbar.rowCount (prefSize.height);

        if (ownRows.isEmpty())
            return;

        ToolbarRow row;

        if (visible) {
            boolean emptyRow = false;
            while (rowCount < ownRows.size()) {
                row = (ToolbarRow)ownRows.lastElement();
                row.removeToolbar (this);
                ownRows.remove (row);
                emptyRow = emptyRow || row.isEmpty();
            }
            if (emptyRow)
                toolbarConfig.checkToolbarRows();
            while (rowCount > ownRows.size()) {
                row = (ToolbarRow)ownRows.lastElement();
                ToolbarRow nR = row.getNextRow();
                if (nR == null)
                    nR = toolbarConfig.createLastRow();
                nR.addToolbar (this, position);
            }
        }
        updatePosition();
    }

    int rowIndex () {
        if (!visible)
            return toolbarConfig.getRowCount();
        return toolbarConfig.rowIndex (((ToolbarRow)ownRows.firstElement()));
    }

    boolean isAlone () {
        Iterator it = ownRows.iterator();
        ToolbarRow row;
        while (it.hasNext()) {
            row = (ToolbarRow)it.next();
            if (row.toolbarCount() != 1)
                return false;
        }
        return true;
    }

    void updatePreferredSize (Dimension size) {
        if (!prefSize.equals (size)) {
            setPreferredSize (size);
        }
    }

    void updateBounds () {
        int rI = rowIndex();
        int rC = getRowCount();
        bounds = new Rectangle (position,
                                ((Toolbar.BASIC_HEIGHT + ToolbarLayout.VGAP) * rI) + ToolbarLayout.VGAP,
                                nextBeg - position - ToolbarLayout.HGAP,
                                (Toolbar.BASIC_HEIGHT * rC) + ((rC - 1) * ToolbarLayout.VGAP));
    }

    void updatePosition () {
        updatePrev();
        if (anchor == NO_ANCHOR) {
            if (position < (prevEnd + ToolbarLayout.HGAP)) {
                position = prevEnd + ToolbarLayout.HGAP;
                anchor = LEFT_ANCHOR;
            }
        } else {
            position = prevEnd + ToolbarLayout.HGAP;
        }
        updatePrevBars();
        updateNextBars();
        updateBounds();
        updatePrefWidth();
    }

    void updatePrevPosition () {
        Iterator it = prevBars.iterator();
        ToolbarConstraints tc;
        while (it.hasNext()) {
            tc = (ToolbarConstraints)it.next();

            //        tc.updatePrev();
            //        tc.updatePrevBars();
            tc.updatePosition();
        }
    }

    void updatePrevBars () {
        Iterator it = prevBars.iterator();
        ToolbarConstraints tc;
        while (it.hasNext()) {
            tc = (ToolbarConstraints)it.next();
            tc.updateNext();
        }
    }

    void updateNextBars () {
        Iterator it = nextBars.iterator();
        ToolbarConstraints tc;
        if (!it.hasNext()) {
            resetNext();
            updatePrefWidth();
        }
        while (it.hasNext()) {
            tc = (ToolbarConstraints)it.next();
            tc.updatePosition();
        }
    }

    void updatePrefWidth () {
        if (nextBars.size() == 0) {
            prefLastWidth = getPosition() + getWidth() + ToolbarLayout.HGAP;
            toolbarConfig.updatePrefWidth();
        }
    }

    int getPrefWidth () {
        return prefLastWidth;
    }

    void updateNext () {
        resetNext();
        Iterator it = nextBars.iterator();
        ToolbarConstraints tc;
        int nextPos;
        while (it.hasNext()) {
            tc = (ToolbarConstraints)it.next();
            nextBeg = Math.min (nextBeg, nextPos = tc.getPosition());
            nextEnd = Math.min (nextEnd, nextPos + tc.getWidth());
        }
        updateBounds();
    }

    void updatePrev () {
        resetPrev();
        Iterator it = prevBars.iterator();
        ToolbarConstraints tc;
        int prevPos;
        while (it.hasNext()) {
            tc = (ToolbarConstraints)it.next();
            prevBeg = Math.max (prevBeg, prevPos = tc.getPosition());
            prevEnd = Math.max (prevEnd, prevPos + tc.getWidth());
        }
    }

    void resetPrev () {
        prevBeg = 0;
        prevEnd = 0;
    }

    void resetNext () {
        nextBeg = Integer.MAX_VALUE;
        nextEnd = Integer.MAX_VALUE;
    }

    //    void testPrinting () {
    //      System.out.print (" ToolbarConstraints [" + super.toString() + "]"); // NOI18N
    //      System.out.print ("       + \"" + name + "\""); // NOI18N
    //      if (visible)
    //        System.out.print ("; is visible "); // NOI18N
    //      else
    //        System.out.print ("; is NOT visible "); // NOI18N
    //      System.out.println ("; position = " + position); // NOI18N
    //      System.out.println ("         Own Rows: size  = " + ownRows.size()); // NOI18N
    //      for (int i = 0; i < ownRows.size(); i++)
    //        System.out.println ("         $ ownRow [" + i + "]  = " + ownRows.elementAt (i).toString()); // NOI18N
    //      System.out.println ("         Prev Bars: size = " + prevBars.size()); // NOI18N
    //      for (int i = 0; i < prevBars.size(); i++)
    //        System.out.println ("         $ prevBar [" + i + "] = " + prevBars.elementAt (i).toString()); // NOI18N
    //      System.out.println ("         Next Bars: size = " + nextBars.size()); // NOI18N
    //      for (int i = 0; i < nextBars.size(); i++)
    //        System.out.println ("         $ nextBar [" + i + "] = " + nextBars.elementAt (i).toString()); // NOI18N
    //    }


    //    public String toString () {
    //      StringBuffer sb = new StringBuffer ();

    //      sb.append ("name=").append (name); // NOI18N
    //      sb.append (" - "); // NOI18N
    //      sb.append ("visible=").append (visible); // NOI18N
    //      sb.append (" - "); // NOI18N
    //      sb.append ("anchor=").append (anchor); // NOI18N
    //      sb.append (" - "); // NOI18N
    //      sb.append ("position=").append (position); // NOI18N
    //      sb.append (" - "); // NOI18N
    //      sb.append ("rows=").append (ownRows.size()); // NOI18N
    //      sb.append (" - "); // NOI18N
    //      sb.append ("prev=").append (prevBars.size()); // NOI18N
    //      sb.append (" - "); // NOI18N
    //      sb.append ("next=").append (nextBars.size()); // NOI18N
    //      sb.append (" - "); // NOI18N
    //      sb.append ("prevBeg=").append (prevBeg); // NOI18N
    //      sb.append (" - "); // NOI18N
    //      sb.append ("prevEnd=").append (prevEnd); // NOI18N
    //      sb.append (" - "); // NOI18N
    //      sb.append ("nextBeg=").append (nextBeg); // NOI18N
    //      sb.append (" - "); // NOI18N
    //      sb.append ("nextEnd=").append (nextEnd); // NOI18N
    //      sb.append (" - "); // NOI18N
    //      sb.append ("prefSize=").append (prefSize); // NOI18N
    //      sb.append (" - "); // NOI18N
    //      sb.append ("bounds=").append (bounds); // NOI18N

    //      return new String ("ToolbarConstraints: [ \n" + sb.toString() + " ]\n"); // NOI18N
    //    }

    // moves
    void moveLeft (int dx) {
        int wantX = position - dx;

        position = wantX;
        anchor = NO_ANCHOR;
        if (wantX > prevEnd) { // no problem to move left
            setAnchorTo (NO_ANCHOR, nextBars);
        } else {
            if (canSwitchLeft (getPosition(), getWidth(), prevBeg, prevEnd - prevBeg)) { // can switch left ?
                switchToolbarLeft ();
            }
        }
    }

    void moveRight (int dx) {
        int wantX = position + dx;
        int wantXpWidth = wantX + getWidth(); // wantX plus width

        if (wantXpWidth < nextBeg) { // no problem to move right
            anchor = NO_ANCHOR;
            position = wantX;
        } else {
            if (canSwitchRight (wantX, getWidth(), nextBeg, nextEnd - nextBeg)) { // can switch right ?
                position = wantX;
                anchor = NO_ANCHOR;
                switchToolbarRight ();
            } else {
                position = nextBeg - getWidth() - ToolbarLayout.HGAP;
                anchor = NO_ANCHOR;
            }
        }

        updatePrevPosition();
    }

    void moveLeft2End (int dx) {
        int wantX = position - dx;

        anchor = NO_ANCHOR;
        if (wantX < (prevEnd + ToolbarLayout.HGAP)) {
            wantX = prevEnd + ToolbarLayout.HGAP;
        }
        move2End (wantX - position);
    }

    void move2End (int dx) {
        position += dx;
        Iterator it = nextBars.iterator();
        ToolbarConstraints tc;
        int nextPos;
        while (it.hasNext()) {
            tc = (ToolbarConstraints)it.next();
            tc.move2End (dx);
        }
    }

    void moveRight2End (int dx) {
        move2End (dx);
    }

    void setAnchorTo (int anch, Vector bars) {
        Iterator it = bars.iterator();
        ToolbarConstraints tc;
        while (it.hasNext()) {
            tc = (ToolbarConstraints)it.next();
            tc.setAnchor (anch);
        }
    }

    void switchToolbarLeft () {
        Iterator it = ownRows.iterator();
        ToolbarRow row;
        while (it.hasNext()) {
            row = (ToolbarRow)it.next();
            row.trySwitchLeft (this);
        }
    }

    void switchToolbarRight () {
        Iterator it = ownRows.iterator();
        ToolbarRow row;
        while (it.hasNext()) {
            row = (ToolbarRow)it.next();
            row.trySwitchRight (this);
        }
    }

    static boolean canSwitchLeft (int p1, int w1, int p2, int w2) {
        return (p1 < (p2));
    }

    static boolean canSwitchRight (int p1, int w1, int p2, int w2) {
        return (p1 > (p2));
    }


    // class WritableToolbar
    static class WritableToolbar {
        String name;
        int position;
        int anchor;
        boolean visible;

        public WritableToolbar (ToolbarConstraints tc) {
            name = tc.getName();
            position = tc.getPosition();
            anchor = tc.getAnchor();
            visible = tc.isVisible();
        }

        public String toString () {
            StringBuffer sb = new StringBuffer();

            sb.append ("    <").append (ToolbarConfiguration.TAG_TOOLBAR); // NOI18N
            sb.append (" ").append (ToolbarConfiguration.ATT_TOOLBAR_NAME).append ("=\"").append (name).append ("\""); // NOI18N
            if ((anchor == ToolbarConstraints.NO_ANCHOR) || !visible)
                sb.append (" ").append (ToolbarConfiguration.ATT_TOOLBAR_POSITION).append ("=\"").append (position).append ("\""); // NOI18N
            if (!visible)
                sb.append (" ").append (ToolbarConfiguration.ATT_TOOLBAR_VISIBLE).append ("=\"").append (visible).append ("\""); // NOI18N
            sb.append (" />\n"); // NOI18N

            return sb.toString();
        }
    } // end of class WritableToolbar
}

/*
 * Log
 *  9    Gandalf   1.8         1/20/00  Libor Kramolis  
 *  8    Gandalf   1.7         1/20/00  Libor Kramolis  
 *  7    Gandalf   1.6         1/19/00  Libor Kramolis  
 *  6    Gandalf   1.5         1/16/00  Libor Kramolis  
 *  5    Gandalf   1.4         1/16/00  Libor Kramolis  
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/9/99   Ian Formanek    Does not depend on 
 *       AbsoluteConstraints (which were removed from open API)
 *  2    Gandalf   1.1         8/18/99  Ian Formanek    Generated serial version
 *       UID
 *  1    Gandalf   1.0         7/11/99  David Simonek   
 * $
 */
