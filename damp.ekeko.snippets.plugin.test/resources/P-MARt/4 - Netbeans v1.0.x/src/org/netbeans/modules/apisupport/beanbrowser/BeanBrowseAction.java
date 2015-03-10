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

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.NodeAction;

/** Browse the current node selection. */
public class BeanBrowseAction extends NodeAction {
    static final long serialVersionUID =3942849820352945054L;
    public boolean enable (Node[] nodes) {
        return nodes.length == 1;
    }
    public void performAction (Node[] nodes) {
        if (enable (nodes)) {
            TopManager.getDefault ().getNodeOperation ().explore (Wrapper.make (nodes[0]));
        }
    }
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser");
    }
    public String getName () {
        return "Bean Browse Node";
    }
    protected String iconResource () {
        return "/org/netbeans/modules/apisupport/resources/BeanBrowserIcon.gif";
    }
}

/*
 * Log
 *  12   Gandalf   1.11        12/23/99 Jesse Glick     Making BB actions in a 
 *       popup.
 *  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         10/13/99 Jesse Glick     Various fixes and 
 *       enhancements:    - added a Changes.txt    - fixed handling of 
 *       OpenAPIs.zip on install/uninstall (previously did not correctly unmount
 *       on uninstall, nor check for already-mounted on install)    - added a 
 *       CompilerTypeTester    - display name & icon updates from Tim    - 
 *       removed link to ToDo.txt from docs page    - various BeanInfo's, both 
 *       in templates and in the support itself, did not display superclass 
 *       BeanInfo correctly    - ExecutorTester now permits user to customize 
 *       new executor instance before running it
 *  9    Gandalf   1.8         10/7/99  Jesse Glick     Context help.
 *  8    Gandalf   1.7         10/7/99  Jesse Glick     Removed obsoleted 
 *       testing main's.
 *  7    Gandalf   1.6         10/7/99  Jesse Glick     Package change. Also 
 *       cloning in Wrapper.make, which may be necessary.
 *  6    Gandalf   1.5         8/20/99  Jesse Glick     Serial version UIDs.
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/27/99  Jesse Glick     Clean-up: comments, 
 *       licenses, removed debugging code, a few minor code changes.
 *  3    Gandalf   1.2         5/21/99  Jesse Glick     Main method.
 *  2    Gandalf   1.1         5/21/99  Jesse Glick     
 *  1    Gandalf   1.0         5/18/99  Jesse Glick     
 * $
 */
