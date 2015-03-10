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

/** The listener is notified about state transitions of TopComponents.
* The primary use is for TopComponent.Registry implementation.
*
* @author Ales Novak
*/
public interface TopComponentListener extends java.util.EventListener {

    /** Called when a TopComponent is activated.
    *
    * @param ev TopComponentChangedEvent
    */
    public void topComponentActivated(TopComponentChangedEvent ev);
    /** Called when a TopComponent is opened.
    *
    * @param ev TopComponentChangedEvent
    */
    public void topComponentOpened(TopComponentChangedEvent ev);
    /** Called when a TopComponent is closed.
    *
    * @param ev TopComponentChangedEvent
    */
    public void topComponentClosed(TopComponentChangedEvent ev);
    /** Called when selected nodes change..
    *
    * @param ev TopComponentChangedEvent
    */
    public void selectedNodesChanged(SelectedNodesChangedEvent ev);
}

/*
* Log
*  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         7/11/99  David Simonek   
* $
*/