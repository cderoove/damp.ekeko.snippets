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

package org.netbeans.modules.projects;

import org.openide.nodes.*;

/**
 *
 * @author  mryzl
 */

public class ProjectFilterChildren extends FilterNode.Children {

    /** Creates new ProjectFilterChildren. */
    public ProjectFilterChildren(Node node) {
        super(node);
    }

    protected Node copyNode(Node node) {
        return new ProjectFilterNode(node, new ProjectFilterChildren(node));
    }
}

/*
* Log
*  1    Gandalf   1.0         3/20/00  Martin Ryzl     
* $ 
*/ 
