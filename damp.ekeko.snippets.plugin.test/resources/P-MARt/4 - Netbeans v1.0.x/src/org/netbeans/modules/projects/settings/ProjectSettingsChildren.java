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

package org.netbeans.modules.projects.settings;

import org.openide.cookies.InstanceCookie;
import org.openide.nodes.*;
import org.openide.options.SystemOption;
import org.openide.util.SharedClassObject;

import org.netbeans.modules.projects.*;

/**
 *
 * @author  mryzl
 */

public class ProjectSettingsChildren extends FilterNode.Children {

    /** Creates new ProjectSettingsNode. */
    public ProjectSettingsChildren(Node node) {
        super(node);
    }

    protected Node[] createNodes(Object key) {
        if (isProjectNode((Node)key)) {
            return new Node[] { ((Node) key).cloneNode() };
        }
        return new Node[] {};
    }

    static boolean isProjectNode(Node node) {
        InstanceCookie ic;

        // if node doesn't have an InstanceCookie ...
        if ((ic = (InstanceCookie)node.getCookie(InstanceCookie.class)) == null) return true;

        // or if it has isGlobal() method and it returns false
        try {
            Class clazz = ic.instanceClass();

            // or if it is the repository
            if (org.openide.filesystems.Repository.class.isAssignableFrom(clazz)) return true;

            if (SharedClassObject.class.isAssignableFrom(clazz)) {
                SharedClassObject sco = SharedClassObject.findObject(clazz, true);
                return PSupport.isProjectObject(sco);
            }
        } catch (Exception ex) {
            // it is global
        }
        return false;
    }
}

/*
* Log
*  3    Gandalf   1.2         1/7/00   Martin Ryzl     
*  2    Gandalf   1.1         1/3/00   Martin Ryzl     
*  1    Gandalf   1.0         1/3/00   Martin Ryzl     
* $ 
*/ 
