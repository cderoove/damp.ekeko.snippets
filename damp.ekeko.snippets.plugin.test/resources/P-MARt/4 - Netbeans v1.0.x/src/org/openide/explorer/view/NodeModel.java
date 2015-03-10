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

/** Base class for all models that work over nodes and are swing thread safe.
*
* @author Jaroslav Tulach
*/
interface NodeModel extends java.util.EventListener {
    /** Notification of children addded event. Modifies the list of nodes
    * and fires info to all listeners.
    */
    abstract void added (VisualizerEvent.Added ev);

    /** Notification that children has been removed. Modifies the list of nodes
    * and fires info to all listeners.
    */
    abstract void removed (VisualizerEvent.Removed ev);

    /** Notification that children has been reordered. Modifies the list of nodes
    * and fires info to all listeners.
    */
    abstract void reordered (VisualizerEvent.Reordered ev);

    /** Update a visualizer (change of name, icon, description, etc.)
    */
    abstract void update (VisualizerNode v);
}

/*
* Log
*  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         8/27/99  Jaroslav Tulach 
* $
*/