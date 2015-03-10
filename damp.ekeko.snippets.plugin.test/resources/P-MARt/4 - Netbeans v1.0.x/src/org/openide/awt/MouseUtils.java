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

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.InputEvent;

/** A class that contains a set of utility classes and methods
* around mouse events and processing.
*
* @author   Ian Formanek
*/
public class MouseUtils extends Object {
    private static int DOUBLE_CLICK_DELTA = 300;

    /** variable for double click */
    private static int tempx = 0;
    private static int tempy = 0;
    private static long temph = 0;
    private static int tempm = 0;

    /** Determines if the event is originated from the right mouse button
    * @param e the MouseEvent
    * @returns true if the event is originated from the right mouse button, false otherwise
    */
    public static boolean isRightMouseButton (MouseEvent e) {
        int m = e.getModifiers();
        if ((m == InputEvent.BUTTON2_MASK) || (m == InputEvent.BUTTON3_MASK))
            return true;
        return false;
    }

    /** Determines if the event is originated from a left mouse button
    * @param e the MouseEvent
    * @returns true if the event is originated from the left mouse button, false otherwise
    */
    public static boolean isLeftMouseButton (MouseEvent e) {
        return javax.swing.SwingUtilities.isLeftMouseButton(e);
    }

    /** Returns true if parametr is a 'doubleclick event'
    * @param e MouseEvent
    * @returns true if the event is a doubleclick
    */
    public static boolean isDoubleClick(MouseEvent e) {
        // even number of clicks is considered like doubleclick
        // it works as well as 'normal testing against 2'
        // but on solaris finaly works and on Win32 works better
        //System.out.println ("Click COunt: "+e.getClickCount ()); // NOI18N
        return (e.getClickCount () % 2 == 0) || isDoubleClickImpl(e);
    }

    /** Tests the positions.
    */
    private static boolean isDoubleClickImpl (MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        long h = e.getWhen();
        int m = e.getModifiers();
        //System.out.println ("When:: "+h); // NOI18N
        // same position at short time
        if (tempx == x && tempy == y && h - temph < DOUBLE_CLICK_DELTA &&
                m == tempm) {
            // OK forget all
            tempx = 0;
            tempy = 0;
            temph = 0;
            tempm = 0;
            return true;
        } else {
            // remember
            tempx = x;
            tempy = y;
            temph = h;
            tempm = m;
            return false;
        }
    }


    // ---------------------------------------------------------------------------
    // Inner classes

    /** The PopupMouseAdapter implements a better popup menu invocation
    * mechanism. It should be used instead of invoking the popup in
    * mouseClicked because the mouseClicked does not work as "often" as
    * it should (i.e. sometimes it is not called).
    * The threshold that can be passed into the constructor
    * is the "sensitivity" of the mouse movement - if the mouse
    * move delta position (between mouse press and release) extends the
    * threshold, the menu should not be shown.
    * The default value is 5 which seems as a reasonable value for
    * the context menu invocation to be comfort for the user.
    */
    public static abstract class PopupMouseAdapter extends MouseAdapter {
        /** The default threshold value */
        public static final int DEFAULT_THRESHOLD = 5;

        /** The mouse position threshold */
        int threshold;
        /** The stored position from mousePressed which is used to calculate
        * the delta position in mouseReleased */
        Point pressedPos;

        /** Creates a new PopupMouseAdapter with default threshold */
        public PopupMouseAdapter () {
            this (DEFAULT_THRESHOLD);
        }

        /** Creates a new PopupMouseAdapter with specified threshold
        * @param threshold The threshold to be used
        */
        public PopupMouseAdapter (int threshold) {
            this.threshold = threshold;
        }

        public void mousePressed (MouseEvent e) {
            // the case when a second button is pressed while holding the
            // popup trigger cancels the popup invocation process
            if (pressedPos != null) {
                pressedPos = null;
                return;
            }

            int m = e.getModifiers();
            if (((m & InputEvent.BUTTON2_MASK) != 0) || ((m & InputEvent.BUTTON3_MASK) != 0))
                pressedPos = e.getPoint ();
        }

        public void mouseReleased (MouseEvent e) {
            int m = e.getModifiers();
            if (((m & InputEvent.BUTTON2_MASK) != 0) || ((m & InputEvent.BUTTON3_MASK) != 0))
                if (pressedPos != null) {
                    Point pos = e.getPoint ();
                    if ((Math.abs (pressedPos.x - pos.x) < threshold) &&
                            (Math.abs (pressedPos.y - pos.y) < threshold)) {
                        showPopup (e);
                    }
                    pressedPos = null;
                }
        }

        /** Called when the sequnce of mouse events should lead to actual
        * showing of the popup menu.
        * Should be redefined to show the menu.
        * param evt The mouse release event - should be used to obtain the
        *           position of the popup menu
        */
        abstract protected void showPopup (MouseEvent evt);
    }

}

/*
 * Log
 *  3    Tuborg    1.2         06/18/98 Ian Formanek    Added methods for
 *                                                      determining left/right
 *                                                      mouse button for MouseEvent
 *
 *  2    Tuborg    1.1         06/15/98 Ian Formanek
 *  1    Tuborg    1.0         06/11/98 David Peroutka
 * $
 */
