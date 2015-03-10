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
import org.openide.util.actions.*;
import org.openide.windows.*;

import java.awt.*;
import java.beans.*;

/** Browse the selected window. */
public class BeanBrowseWindowAction extends CallableSystemAction {

    static final long serialVersionUID =363544761434854016L;
    public String getName () {
        return "Bean Browse Window";
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser");
    }

    public void performAction () {
        try {
            Component c = TopComponent.getRegistry ().getActivated ();
            if (c == null)
                c = TopManager.getDefault ().getWindowManager ().getMainWindow ();
            Node n = Wrapper.make (new RefinedBeanNode (c));
            TopManager.getDefault ().getNodeOperation ().explore (n);
        } catch (IntrospectionException e) {
            TopManager.getDefault ().notifyException (e);
        }
    }
    protected String iconResource () {
        return "/org/netbeans/modules/apisupport/resources/BeanBrowserIcon.gif";
    }
}

/*
 * Log
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
 *  5    Gandalf   1.4         7/22/99  Jesse Glick     Window system changes.
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/27/99  Jesse Glick     Clean-up: comments, 
 *       licenses, removed debugging code, a few minor code changes.
 *  2    Gandalf   1.1         5/24/99  Jesse Glick     Using RefinedBeanNode 
 *       for CustomizeBeanAction.
 *  1    Gandalf   1.0         5/21/99  Jesse Glick     
 * $
 */
