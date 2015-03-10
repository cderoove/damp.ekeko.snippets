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

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;

/** A subclass of JMenu which provides workaround for pre-JDK 1.2.2 JMenu positioning problem.
* It assures, that the popup menu gets placed inside visible screen area.
*/
public class JMenuPlus extends JMenu {
    static final long serialVersionUID =-7700146216422707913L;
    private static JPopupMenu myPopupMenu;

    public JMenuPlus() {
        this (""); // NOI18N
    }

    public JMenuPlus(String label) {
        super(label);

        enableInputMethods(false);
    }

    /** Overriden to provide better strategy for placing the JMenu on the screen.
    * @param b a boolean value -- true to make the menu visible, false to hide it
    */
    public void setPopupMenuVisible(boolean b) {
        boolean isVisible = isPopupMenuVisible();
        if (b != isVisible) {
            // Set location of popupMenu (pulldown or pullright)
            //  Perhaps this should be dictated by L&F
            if ((b==true) && isShowing()) {
                Point p = getPopupMenuOrigin();
                getPopupMenu().show(this, p.x, p.y);
            } else {
                getPopupMenu().setVisible(false);
            }
        }
    }

    /** Overriden to provide better strategy for placing the JMenu on the screen.
    *
    * @returns a Point in the coordinate space of the menu instance
    * which should be used as the origin of the JMenu's popup menu.
    */
    protected Point getPopupMenuOrigin() {
        int x = 0;
        int y = 0;
        JPopupMenu pm = getPopupMenu();
        Rectangle screenRect = JPopupMenuUtils.getScreenRect();
        Dimension s = getSize();
        Dimension pmSize = pm.getSize();
        int screenRight = screenRect.x + screenRect.width;
        int screenBottom = screenRect.y + screenRect.height;
        // For the first time the menu is popped up,
        // the size has not yet been initiated
        if (pmSize.width==0) {
            pmSize = pm.getPreferredSize();
        }
        Point position = getLocationOnScreen();

        Container parent = getParent();
        if (parent instanceof JPopupMenu) {
            // We are a submenu (pull-right)

            // First determine x:
            if (position.x+s.width + pmSize.width < screenRight) {
                x = s.width;         // Prefer placement to the right
            } else {
                x = 0-pmSize.width;  // Otherwise place to the left
            }
            // Then the y:
            if (position.y+pmSize.height < screenBottom) {
                y = 0;                       // Prefer dropping down
            } else {
                y = s.height-pmSize.height;  // Otherwise drop 'up'
            }
        } else {
            // We are a toplevel menu (pull-down)

            // First determine the x:
            if (position.x+pmSize.width < screenRight) {
                x = 0;                     // Prefer extending to right
            } else {
                x = s.width-pmSize.width;  // Otherwise extend to left
            }
            // Then the y:
            if (position.y+s.height+pmSize.height < screenBottom) {
                y = s.height;          // Prefer dropping down
            } else {
                y = 0-pmSize.height;   // Otherwise drop 'up'
            }
        }
        if (y < -position.y) y = -position.y;
        if (x < -position.x) x = -position.x;
        return new Point(x,y);
    }
}

/*
* Log
*  7    Gandalf   1.6         3/16/00  Martin Ryzl     Correction of the pNull 
*       data problem.
*  6    Gandalf   1.5         3/11/00  Martin Ryzl     menufix [by E.Adams, 
*       I.Formanek]
*  5    Gandalf   1.4         2/17/00  Jaroslav Tulach Correction of the pNull 
*       data problem.
*  4    Gandalf   1.3         1/12/00  Ian Formanek    NOI18N
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         8/18/99  Ian Formanek    Generated serial version 
*       UID
*  1    Gandalf   1.0         6/28/99  Ian Formanek    
* $
*/
