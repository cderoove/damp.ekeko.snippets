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

import java.util.*;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.nodes.*;

import org.netbeans.modules.projects.settings.ProjectOption;

/**
 *
 * @author  mryzl
 */

public class ProjectFilterNode extends FilterNode {

    static boolean delete = Boolean.getBoolean("netbeans.project.delete"); // NOI18N

    /** Creates new ProjectFilterNode. */
    public ProjectFilterNode(Node node, Children children) {
        super(node, children);
    }

    public boolean canDestroy() {
        if (delete) return super.canDestroy();

        try {
            DataObject dobj = getMyDataObject();
            ProjectDataObject pdo = (ProjectDataObject) TopManager.getDefault().getPlaces().nodes().projectDesktop().getCookie(ProjectDataObject.class);
            DataFolder folder = pdo.getFileFolder();

            // System.err.println("myDO = " + ((dobj != null) ? dobj.getName(): "null") + ", " + dobj);

            // non data object
            if (dobj == null) return super.canDestroy();

            dobj = deShadow(dobj);

            // top-level object
            //      if (folder.equals(dobj.getFolder())) return super.canDestroy();
            Enumeration en = folder.children();
            while (en.hasMoreElements()) {
                DataObject obj = (DataObject)en.nextElement();
                // System.err.println("child = " + obj.getName() + ", " + obj);
                obj = deShadow(obj);
                // System.err.println("deshadow.child = " + obj.getName() + ", " + obj);
                if (obj.equals(dobj)) return super.canDestroy();
            }
        } catch (java.io.IOException ex) {
            // default
        }

        return false;
    }

    public void destroy() throws java.io.IOException {
        if (canDestroy()) super.destroy();
    }

    protected DataObject deShadow(DataObject dobj) {
        while (dobj instanceof DataShadow) dobj = ((DataShadow) dobj).getOriginal();
        return dobj;
    }

    protected DataObject getMyDataObject() {
        DataObject dobj = (DataObject) getCookie(DataObject.class);
        if (dobj == null) return null;

        for(Node node = getParentNode(); node != null; node = node.getParentNode()) {
            DataObject dobj2 = (DataObject) node.getCookie(DataObject.class);
            if (dobj2 != null) {
                return (dobj.equals(dobj2) ? null : dobj);
            }
        }
        return null;
    }
}

/*
* Log
*  1    Gandalf   1.0         3/20/00  Martin Ryzl     
* $ 
*/ 
