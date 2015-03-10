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

import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;

/** The event is fired whenever a TopComponent changes its state.
*
* @author Ales Novak
*/
public class TopComponentChangedEvent extends java.util.EventObject {

    /** Mask for TopComponent opened event. */
    public static int OPENED = 0x0;
    /** Mask for TopComponent activated event. */
    public static int ACTIVATED = 0x1;
    /** Mask for TopComponent closed event. */
    public static int CLOSED = 0x2;

    /** type of the event */
    public final int type;
    public final TopComponent topComponent;
    public final Workspace workspace;

    static final long serialVersionUID =8746512617051866536L;
    public TopComponentChangedEvent(Object source, TopComponent topComponent,
                                    Workspace workspace, int type) {
        super (source);
        this.type = type;
        this.topComponent = topComponent;
        this.workspace = workspace;
    }

    /** @return type == OPENED */
    public boolean isOpened() {
        return type == OPENED;
    }
    /** @return type == ACTIVATED */
    public boolean isActivated() {
        return type == ACTIVATED;
    }
    /** @return type == CLOSED */
    public boolean isClosed() {
        return type == CLOSED;
    }
}

/*
* Log
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         8/18/99  Ian Formanek    Generated serial version 
*       UID
*  1    Gandalf   1.0         7/11/99  David Simonek   
* $
*/