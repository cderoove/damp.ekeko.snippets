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

package org.netbeans.examples.modules.breakpointview;

import org.openide.nodes.*;

class BPViewChildren extends FilterNode.Children {
    BPViewChildren (Node orig) {
        super (orig);
        System.err.println ("Making children for " + orig);
        BPViewNode.depthcharge (7);
    }

    protected Node[] createNodes (Object key) {
        System.err.println ("Creating nodes from key " + key);
        BPViewNode.depthcharge (8);
        Node child = (Node) key;
        if (BPViewNode.isInteresting (child))
            return new Node[] { new BPViewNode (child) };
        else
            return new Node[] { };
    }
}
