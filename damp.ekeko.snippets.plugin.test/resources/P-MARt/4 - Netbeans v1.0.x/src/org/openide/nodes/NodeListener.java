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

package org.openide.nodes;

/** Listener to special changes in <code>Node</code>s. Is a property
* change listener so that all changes in properties in the {@link Node node} can be fired
* in the usual way.
*
* @author Jaroslav Tulach
* @version 0.11, Jan 15, 1998
*/
public interface NodeListener extends java.beans.PropertyChangeListener {

    /** Fired when a set of new children is added.
    * @param ev event describing the action
    */
    public void childrenAdded (NodeMemberEvent ev);

    /** Fired when a set of children is removed.
    * @param ev event describing the action
    */
    public void childrenRemoved (NodeMemberEvent ev);

    /** Fired when the order of children is changed.
    * @param ev event describing the change
    */
    public void childrenReordered(NodeReorderEvent ev);

    /** Fired when the node is deleted.
    * @param ev event describing the node
    */
    public void nodeDestroyed (NodeEvent ev);
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/17/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
