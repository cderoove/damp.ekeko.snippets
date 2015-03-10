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

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

/** Menu element that can contain other menu items. These items
* are then displayed "inline". The JInlineMenu can be used to
* componse more menu items into one that can be added/removed
* at once.
*
* @author Jan Jancura
*/
public class JInlineMenu extends JMenuItem {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2310488127953523571L;

    /** north separator */
    private JSeparator north = new JSeparator ();
    /** south separator */
    private JSeparator south = new JSeparator ();
    /** Stores inner MenuItems added to outer menu. */
    private JMenuItem[] items;

    /**
    * Creates new JInlineMenu.
    */
    public JInlineMenu () {
        setEnabled (false);
    }

    /**
    * Setter for array of items to display. Can be called only from
    * event queue thread.
    *
    * @param items array of menu items to display
    */
    public void setMenuItems (final JMenuItem[] items) {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            removeItems ();
                                            JInlineMenu.this.items = items;
                                            addItems ();
                                        }
                                    });
    }

    /** Finds the index of a component in array of components.
    * @return index or -1
    */
    private static int findIndex (Object of, Object[] arr) {
        int menuLength = arr.length;
        for (int i = 0; i < menuLength; i++) {
            if (of == arr[i]) {
                return i;
            }
        }
        return -1;
    }

    /** Remove all current items.
    */
    private void removeItems () {
        JComponent m = (JComponent) getParent ();
        if (m == null) return;

        if (items != null) {

            for (int i = 0; i < items.length; i++) {
                m.remove (items[i]);
            }
        }

        m.remove (north);
        m.remove (south);
    }

    private void addItems () {
        JComponent m = (JComponent) getParent ();
        if (m == null) return;

        boolean usedToBeContained = false;
        if (m instanceof JPopupMenu) {
            usedToBeContained = JPopupMenuUtils.isPopupContained ((JPopupMenu) m);
        }

        // Find me please!
        Component[] array = m.getComponents ();
        int menuPos = findIndex (this, array);
        if (menuPos == -1) return; // not found

        if (
            menuPos > 0 &&
            array.length > 0 &&
            !(array[menuPos - 1] instanceof JPopupMenu.Separator) &&
            !(array[menuPos - 1] instanceof JSeparator) &&
            !(array[menuPos - 1] instanceof JInlineMenu)
        ) {
            // not first and not after separator or another inline menu ==>> add separator before
            m.add (north, menuPos++);
            array = m.getComponents ();
        }

        if (menuPos < array.length - 1) {
            // not last
            if (
                items.length > 0 &&
                !(array[menuPos + 1] instanceof JPopupMenu.Separator) &&
                !(array[menuPos + 1] instanceof JSeparator)
            ) {
                // adding non-zero items and not before separator
                m.add (south, menuPos + 1);
            } else if (
                items.length == 0 &&
                array[menuPos + 1] instanceof JPopupMenu.Separator
            ) {
                // adding zero items and there is an extra separator after the JInlineMenu item ==>> remove it
                m.remove (menuPos + 1);
            }
        }

        // Add components to outer menu.
        if (menuPos > array.length) {
            int menuLength = items.length;
            for (int i = 0; i < menuLength; i++) {
                m.add (items[i]);
            }
        } else {
            int menuLength = items.length;
            for (int i = 0; i < menuLength; i++) {
                m.add (items[i], ++menuPos);
            }
        }

        if (m instanceof JPopupMenu) {
            JPopupMenu p = (JPopupMenu)m;
            p.pack ();
            p.invalidate ();
            Component c = p.getParent ();
            if (c != null) {
                c.validate ();
            }
            JPopupMenuUtils.dynamicChange(p, usedToBeContained);
        }
    }

    /**
    * Not visible.
    */
    public java.awt.Dimension getPreferredSize () {
        if (isVisible ()) {
            return new java.awt.Dimension (0,0);
        } else {
            return Toolkit.getDefaultToolkit ().getScreenSize ();
        }
    }

    /*
      public static void main (String[] args) throws Exception {
        
        JFrame jf = new JFrame ();
        JMenuBar mb = new JMenuBar ();
        JMenu m = new JMenu ("Kukuc");
        
        JInlineMenu im = new JInlineMenu ();
        JInlineMenu im2 = new JInlineMenu ();

        m.add (new JMenuItem ("First item"));
        m.add (im);
        m.add (im2);
        m.add (new JMenuItem ("Last item"));
        
        mb.add (m);

        jf.setJMenuBar (mb);
        jf.pack ();
        jf.show ();
        
        BufferedReader r = new BufferedReader (new InputStreamReader (System.in));
        
        for (;;) {
          String s = r.readLine ();
          int i = Integer.valueOf (s).intValue ();
          im.setMenuItems (array (i, " cislo"));
          im2.setMenuItems (array (i, " ucho"));
          jf.pack ();
        }
        
      }
      
      private static JMenuItem[] array (int i, String  n) {
        JMenuItem[] arr = new JMenuItem[i];
        while (i-- > 0) {
          arr[i] = new JMenuItem (n + " : " + i);
        }
        return arr;
      }
    */  
}

/*
 * Log
 *  14   Gandalf   1.13        3/11/00  Martin Ryzl     menufix [by E.Adams, 
 *       I.Formanek]
 *  13   Gandalf   1.12        1/13/00  Jaroslav Tulach When the items are empty
 *       separator is still added into the menu.
 *  12   Gandalf   1.11        12/3/99  Jan Jancura     No separator before 
 *       empty InLine menu.
 *  11   Gandalf   1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         9/27/99  Jaroslav Tulach 
 *  9    Gandalf   1.8         9/25/99  Jaroslav Tulach 
 *  8    Gandalf   1.7         9/25/99  Jaroslav Tulach #3727
 *  7    Gandalf   1.6         8/18/99  Petr Hrebejk    Fix of lightweight menu 
 *       on nodes with tools action
 *  6    Gandalf   1.5         7/19/99  Ian Formanek    Fixed problem with 
 *       multiple JInlineMenus and doubling separators
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         3/22/99  Jaroslav Tulach Fixed creation from 
 *       template
 *  3    Gandalf   1.2         1/13/99  David Simonek   
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
