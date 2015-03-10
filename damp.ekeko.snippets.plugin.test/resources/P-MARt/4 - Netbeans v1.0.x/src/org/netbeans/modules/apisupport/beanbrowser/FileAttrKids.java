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

package org.netbeans.modules.apisupport.beanbrowser;

import java.util.*;

import org.openide.filesystems.FileObject;
import org.openide.nodes.*;

class FileAttrKids extends Children.Keys {

    private FileObject fo;

    public FileAttrKids (FileObject fo) {
        this.fo = fo;
    }

    protected void addNotify () {
        List l = new LinkedList ();
        Enumeration e = fo.getAttributes ();
        while (e.hasMoreElements ()) l.add (e.nextElement ());
        setKeys (l);
    }

    protected void removeNotify () {
        setKeys (Collections.EMPTY_SET);
    }

    protected Node[] createNodes (Object k) {
        String key = (String) k;
        Node n = PropSetKids.makeObjectNode (fo.getAttribute (key));
        n.setDisplayName (key + " = " + n.getDisplayName ());
        return new Node[] { n };
    }

}

/*
 * Log
 *  2    Gandalf   1.1         1/19/00  Jesse Glick     File attribute list 
 *       improvements.
 *  1    Gandalf   1.0         12/23/99 Jesse Glick     
 * $
 */
