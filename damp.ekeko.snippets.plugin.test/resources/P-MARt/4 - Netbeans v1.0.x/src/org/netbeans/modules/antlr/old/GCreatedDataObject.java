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

package org.netbeans.modules.antlr.old;

import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;


/**
 *
 * @author  jleppanen
 * @version 
 */
public class GCreatedDataObject extends MultiDataObject {

    /** Creates new GCreatedDataObject */
    public GCreatedDataObject(FileObject pf, MultiFileLoader l) throws DataObjectExistsException {
        super (pf, l);
    }

    protected org.openide.nodes.Node createNodeDelegate () {
        DataNode n = new DataNode (this, Children.LEAF);
        //n.setDefaultAction (SystemAction.get (ViewAction.class));
        return n;
    }
}