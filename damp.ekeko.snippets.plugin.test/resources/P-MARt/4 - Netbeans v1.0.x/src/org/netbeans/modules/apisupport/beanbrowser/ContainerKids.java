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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Collections;

import org.openide.nodes.*;

/** Children list of an AWT Container. */
public class ContainerKids extends Children.Keys {

    private Container container;
    private ContainerListener containerListener = null;

    public ContainerKids (Container container) {
        this.container = container;
    }

    protected void addNotify () {
        updateKeys ();
        if (containerListener == null) {
            containerListener = new ContainerListener () {
                                    public void componentAdded (ContainerEvent ev) {
                                        updateKeys ();
                                    }
                                    public void componentRemoved (ContainerEvent ev) {
                                        updateKeys ();
                                    }
                                };
            container.addContainerListener (containerListener);
        }
    }
    private void cleanUp () {
        if (containerListener != null) {
            container.removeContainerListener (containerListener);
            containerListener = null;
        }
    }
    protected void removeNotify () {
        cleanUp ();
        setKeys (Collections.EMPTY_SET);
    }
    protected void finalize () {
        cleanUp ();
    }

    private void updateKeys () {
        setKeys (container.getComponents ());
    }

    protected Node[] createNodes (Object key) {
        return new Node[] { PropSetKids.makeObjectNode (key) };
    }

}

/*
 * Log
 *  7    Gandalf   1.6         2/4/00   Jesse Glick     Misc fixes.
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/7/99  Jesse Glick     Package change. Also 
 *       cloning in Wrapper.make, which may be necessary.
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/27/99  Jesse Glick     Clean-up: comments, 
 *       licenses, removed debugging code, a few minor code changes.
 *  2    Gandalf   1.1         5/24/99  Jesse Glick     Using RefinedBeanNode 
 *       for CustomizeBeanAction.
 *  1    Gandalf   1.0         5/21/99  Jesse Glick     
 * $
 */
