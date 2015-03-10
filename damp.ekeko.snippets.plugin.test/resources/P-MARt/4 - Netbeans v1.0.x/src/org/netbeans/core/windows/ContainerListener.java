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

import java.util.EventObject;
import java.awt.event.ComponentEvent;

/** Allows implementors to listen to events on top component container.
* This listener is very similar like window listener plus bound change
* events.
*
* @author Dafe Simonek
*/
interface ContainerListener {

    /** When container is being deactivated */
    public void containerDeactivated (EventObject eo);

    /** When container was closed */
    public void containerClosed (EventObject eo);

    /** When container was brought bacjk from icon to normal state */
    public void containerDeiconified (EventObject eo);

    /** When container was opened - showed */
    public void containerOpened (EventObject eo);

    /** When container was iconified */
    public void containerIconified (EventObject eo);

    /** When user tried to invoke container closing */
    public void containerClosing (EventObject eo);

    /** When container was made active - receives focus */
    public void containerActivated (EventObject eo);

    /** When container was resized, its size was changed */
    public void containerResized (ComponentEvent ce);

    /** When container was moved, its position was changed */
    public void containerMoved (ComponentEvent ce);

    /** Called when container was shown. */
    public void containerShown (ComponentEvent ce);

    /** Called when container was hidden. */
    public void containerHidden (ComponentEvent ce);
}

/*
* Log
*  3    Gandalf   1.2         12/17/99 David Simonek   #1913, #2970
*  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         7/11/99  David Simonek   
* $
*/