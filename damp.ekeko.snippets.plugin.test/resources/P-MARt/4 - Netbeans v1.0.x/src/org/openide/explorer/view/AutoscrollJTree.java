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

package org.openide.explorer.view;

import java.awt.Insets;
import java.awt.Point;
import java.awt.dnd.Autoscroll;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

/** Implementation of JTree with support for autoscrolling
* during DnD operations. 
*
* @author Dafe Simonek
*/
class AutoscrollJTree extends JTree implements Autoscroll {

    static final long serialVersionUID =-6425357101508588684L;

    /** Support for autoscrolling - we delegate all
    * real work to it. */
    AutoscrollSupport support;

    /** Creates new JTree with autoscroll support */
    AutoscrollJTree (javax.swing.tree.TreeModel m) {
        super(m);

        // clicks are handle by us (works only in JDK 1.3)
        toggleClickCount = 0;
    }

    /** notify the Component to autoscroll */
    public void autoscroll (Point cursorLoc) {
        getSupport().autoscroll(cursorLoc);
    }


    /** @return the Insets describing the autoscrolling
    * region or border relative to the geometry of the
    * implementing Component.
    */
    public Insets getAutoscrollInsets () {
        return getSupport().getAutoscrollInsets();
    }

    /** Safe getter for autoscroll support. */
    AutoscrollSupport getSupport() {
        if (support == null)
            support = new AutoscrollSupport(
                          this, new Insets(15, 10, 15, 10));
        return support;
    }

}

/*
* Log
*  8    Gandalf   1.7         1/7/00   Jaroslav Tulach #5160, but works 
*       correctly only on JDK1.3 on JDK1.2 does both, expands the node and also 
*       starts default action. alas.
*  7    Gandalf   1.6         12/9/99  Jaroslav Tulach Double-click does only 
*       one action (expand/invoke default).
*  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         8/27/99  Jaroslav Tulach New threading model & 
*       Children.
*  4    Gandalf   1.3         8/18/99  Ian Formanek    Generated serial version 
*       UID
*  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         4/27/99  David Simonek   autoscroll support and 
*       visual feedback in DnD operations added
*  1    Gandalf   1.0         4/21/99  David Simonek   
* $
*/