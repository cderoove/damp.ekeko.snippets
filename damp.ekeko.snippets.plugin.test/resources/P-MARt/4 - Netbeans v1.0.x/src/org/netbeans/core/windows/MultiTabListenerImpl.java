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

package org.netbeans.core.windows;

import java.awt.event.*;

/** Implementation of the window and component listener for MultiTabContainer.
* It simply serves as a bridge and receives events from WindowListener
* and ComponentListener of MultiTabContainer and forwards it further
* to the asociated container listener as needed.
*
* @author Dafe Simonek
*/
class MultiTabListenerImpl implements WindowListener, ComponentListener {

    /** Asociation with the container listener */
    ContainerListener cl;

    /** Creates new MultiTabListenerImpl */
    public MultiTabListenerImpl (ContainerListener cl) {
        this.cl = cl;
    }

    public void windowDeactivated (WindowEvent we) {
        cl.containerDeactivated(we);
    }

    public void windowClosed (WindowEvent we) {
        cl.containerClosed(we);
    }

    public void windowDeiconified (WindowEvent we) {
        cl.containerDeiconified(we);
    }

    public void windowOpened(WindowEvent we) {
        cl.containerOpened(we);
    }

    public void windowIconified(WindowEvent we) {
        cl.containerIconified(we);
    }

    public void windowClosing(WindowEvent we) {
        cl.containerClosing(we);
    }

    public void windowActivated(WindowEvent we) {
        cl.containerActivated(we);
    }

    public void componentResized(ComponentEvent ce) {
        cl.containerResized(ce);
    }

    public void componentMoved(ComponentEvent ce) {
        cl.containerMoved(ce);
    }

    public void componentShown(ComponentEvent ce) {
        cl.containerShown(ce);
    }

    public void componentHidden(ComponentEvent ce) {
        cl.containerHidden(ce);
    }

}

/*
* Log
*  3    Gandalf   1.2         12/17/99 David Simonek   #1913, #2970
*  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         7/11/99  David Simonek   
* $
*/