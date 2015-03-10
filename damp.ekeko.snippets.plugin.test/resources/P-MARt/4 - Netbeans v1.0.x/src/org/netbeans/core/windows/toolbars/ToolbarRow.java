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

import java.util.Vector;
import java.util.Iterator;

/**
 * Class ToolbarRow ...
 *
 * @author Libor Kramolis
 */
public class ToolbarRow {
    ToolbarConfiguration toolbarConfig;
    ToolbarRow prevRow;
    ToolbarRow nextRow;

    /**
     * @associates ToolbarConstraints 
     */
    private Vector toolbars;

    ToolbarRow (ToolbarConfiguration config) {
        toolbarConfig = config;
        toolbars = new Vector();
        prevRow = nextRow = null;
    }

    void addToolbar (ToolbarConstraints tc) {
        addToolbar2 (tc, toolbars.size());
    }

    void addToolbar (ToolbarConstraints newTC, int pos) {
        int index = 0;
        Iterator it = toolbars.iterator();
        ToolbarConstraints tc;
        while (it.hasNext()) {
            tc = (ToolbarConstraints)it.next();
            if (pos < tc.getPosition())
                break;
            index++;
        }
        addToolbar2 (newTC, index);
    }

    private void addToolbar2 (ToolbarConstraints tc, int index) {
        if (toolbars.contains (tc))
            return;

        ToolbarConstraints prev = null;
        ToolbarConstraints next = null;
        if (index != 0) {
            prev = (ToolbarConstraints)toolbars.elementAt (index - 1);
            prev.addNextBar (tc);
            tc.addPrevBar (prev);
        }
        if (index < toolbars.size()) {
            next = (ToolbarConstraints)toolbars.elementAt (index);
            tc.addNextBar (next);
            next.addPrevBar (tc);
        }
        if ((prev != null) && (next != null)) {
            prev.removeNextBar (next);
            next.removePrevBar (prev);
        }

        tc.addOwnRow (this);
        toolbars.insertElementAt (tc, index);

        tc.updatePosition();
    }

    void removeToolbar (ToolbarConstraints tc) {
        int index = toolbars.indexOf (tc);

        ToolbarConstraints prev = null;
        ToolbarConstraints next = null;
        try {
            prev = (ToolbarConstraints)toolbars.elementAt (index - 1);
            prev.removeNextBar (tc);
        } catch (ArrayIndexOutOfBoundsException e) { }
        try {
            next = (ToolbarConstraints)toolbars.elementAt (index + 1);
            next.removePrevBar (tc);
            next.setAnchor (ToolbarConstraints.NO_ANCHOR);
        } catch (ArrayIndexOutOfBoundsException e) { }
        if ((prev != null) && (next != null)) {
            prev.addNextBar (next);
            next.addPrevBar (prev);
        }

        toolbars.removeElement (tc);

        if (prev != null) {
            prev.updatePosition();
        } else {
            if (next != null) {
                next.updatePosition();
            }
        }
    }

    Iterator iterator () {
        return toolbars.iterator();
    }

    void setPrevRow (ToolbarRow prev) {
        prevRow = prev;
    }

    ToolbarRow getPrevRow () {
        return prevRow;
    }

    void setNextRow (ToolbarRow next) {
        nextRow = next;
    }

    ToolbarRow getNextRow () {
        return nextRow;
    }

    int getPrefWidth () {
        if (toolbars.isEmpty())
            return -1;
        return ((ToolbarConstraints)toolbars.lastElement()).getPrefWidth();
    }

    boolean isEmpty () {
        return toolbars.isEmpty();
    }

    int toolbarCount () {
        return toolbars.size();
    }

    void updateBounds () {
        Iterator it = toolbars.iterator();
        ToolbarConstraints tc;
        while (it.hasNext()) {
            tc = (ToolbarConstraints)it.next();
            tc.updateBounds();
        }
    }

    void switchBars (ToolbarConstraints left, ToolbarConstraints right) {
        int leftIndex = toolbars.indexOf (left);
        int rightIndex = toolbars.indexOf (right);
        ToolbarConstraints leftPrev = null;
        ToolbarConstraints rightNext = null;

        try {
            leftPrev = (ToolbarConstraints)toolbars.elementAt (leftIndex - 1);
        } catch (ArrayIndexOutOfBoundsException e) { }
        try {
            rightNext = (ToolbarConstraints)toolbars.elementAt (rightIndex + 1);
        } catch (ArrayIndexOutOfBoundsException e) { }

        if (leftPrev != null)
            leftPrev.removeNextBar (left);
        left.removePrevBar (leftPrev);
        left.removeNextBar (right);

        right.removePrevBar (left);
        right.removeNextBar (rightNext);
        if (rightNext != null)
            rightNext.removePrevBar (right);

        if (leftPrev != null)
            leftPrev.addNextBar (right);
        left.addPrevBar (right);
        left.addNextBar (rightNext);

        right.addPrevBar (leftPrev);
        right.addNextBar (left);
        if (rightNext != null)
            rightNext.addPrevBar (left);

        toolbars.setElementAt (left, rightIndex);
        toolbars.setElementAt (right, leftIndex);
    }

    void trySwitchLeft (ToolbarConstraints tc) {
        int index = toolbars.indexOf (tc);
        if (index == 0)
            return;

        try {
            ToolbarConstraints prev = (ToolbarConstraints)toolbars.elementAt (index - 1);
            if (ToolbarConstraints.canSwitchLeft (tc.getPosition(), tc.getWidth(), prev.getPosition(), prev.getWidth())) {
                switchBars (prev, tc);
            }
        } catch (ArrayIndexOutOfBoundsException e) { /* No left toolbar - it means tc is toolbar like Palette (:-)) */ }
    }

    void trySwitchRight (ToolbarConstraints tc) {
        int index = toolbars.indexOf (tc);

        try {
            ToolbarConstraints next = (ToolbarConstraints)toolbars.elementAt (index + 1);
            if (ToolbarConstraints.canSwitchRight (tc.getPosition(), tc.getWidth(), next.getPosition(), next.getWidth())) {
                switchBars (tc, next);
                next.setPosition (tc.getPosition() - next.getWidth() - ToolbarLayout.HGAP);
            }
        } catch (ArrayIndexOutOfBoundsException e) { /* No right toolbar - it means tc is toolbar like Palette (:-)) */ }
    }

    //    void testPrinting () {
    //      System.out.println (" ToolbarRow [" + super.toString() + "]"); // NOI18N
    //      System.out.println ("    Toolbars, size = " + toolbars.size()); // NOI18N
    //      for (int i = 0; i < toolbars.size(); i++) {
    //        System.out.print ("    * toolbar [" + i + "] = "); // NOI18N
    //        ((ToolbarConstraints)toolbars.elementAt (i)).testPrinting();
    //      }
    //    }


    // class WritableToolbarRow
    static class WritableToolbarRow {
        /**
         * @associates WritableToolbar 
         */
        Vector toolbars;

        public WritableToolbarRow () {
            toolbars = new Vector();
        }

        public WritableToolbarRow (ToolbarRow row) {
            this();
            initToolbars (row);
        }

        void initToolbars (ToolbarRow r) {
            Iterator it = r.toolbars.iterator();
            while (it.hasNext()) {
                toolbars.addElement (new ToolbarConstraints.WritableToolbar ((ToolbarConstraints)it.next()));
            }
        }

        void addToolbar (ToolbarConstraints newTC) {
            int index = 0;
            Iterator it = toolbars.iterator();
            ToolbarConstraints.WritableToolbar tc;
            while (it.hasNext()) {
                tc = (ToolbarConstraints.WritableToolbar)it.next();
                if (newTC.getPosition() < tc.position)
                    break;
                index++;
            }

            toolbars.insertElementAt (new ToolbarConstraints.WritableToolbar (newTC), index);
        }

        boolean isEmpty () {
            return toolbars.isEmpty();
        }

        public String toString () {
            StringBuffer sb = new StringBuffer();

            sb.append ("  <").append (ToolbarConfiguration.TAG_ROW).append (">\n"); // NOI18N
            Iterator it = toolbars.iterator();
            while (it.hasNext()) {
                sb.append (it.next().toString());
            }
            sb.append ("  </").append (ToolbarConfiguration.TAG_ROW).append (">\n"); // NOI18N

            return sb.toString();
        }
    } // end of class WritableToolbarRow
} // end of class ToolbarRow

/*
 * Log
 *  4    Gandalf   1.3         1/20/00  Libor Kramolis  
 *  3    Gandalf   1.2         1/19/00  Libor Kramolis  
 *  2    Gandalf   1.1         1/16/00  Libor Kramolis  
 *  1    Gandalf   1.0         1/16/00  Libor Kramolis  initial revision
 * $
 */
