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

import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/** The event being fired when new selection of nodes appears.
*
* @author Ales Novak
*/
public class SelectedNodesChangedEvent extends java.util.EventObject {

    /** Selected nodes */
    private final Node[] nodes;
    /** TopComponent containing the nodes. */
    public final TopComponent topComponent;

    static final long serialVersionUID =-4409636337342412958L;
    public SelectedNodesChangedEvent(Object source, TopComponent topComponent, Node[] nodes) {
        super (source);
        this.nodes = nodes;
        this.topComponent = topComponent;
    }

    /** @return an array of selected nodes */
    public Node[] getSelectedNodes() {
        return nodes;
    }
}

/*
* Log
*  4    Gandalf   1.3         12/3/99  Jaroslav Tulach Activated/Current works 
*       better.
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         8/18/99  Ian Formanek    Generated serial version 
*       UID
*  1    Gandalf   1.0         7/11/99  David Simonek   
* $
*/