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

import java.awt.dnd.Autoscroll;
import java.awt.Point;
import java.awt.Insets;

import javax.swing.JList;

/** Extended JList with the support for autoscrolling
* during DnD operations.
*
* @author Dafe Simonek
*/
class AutoscrollJList extends JList implements Autoscroll {

    /** Support for autoscrolling - we delegate all
    * real work to it. */
    AutoscrollSupport support;

    static final long serialVersionUID =-1504011744044499802L;
    /** Creates new JList with autoscroll support */
    AutoscrollJList () {
        super();
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
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         8/27/99  Jaroslav Tulach New threading model & 
*       Children.
*  3    Gandalf   1.2         8/18/99  Ian Formanek    Generated serial version 
*       UID
*  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         4/27/99  David Simonek   
* $
*/