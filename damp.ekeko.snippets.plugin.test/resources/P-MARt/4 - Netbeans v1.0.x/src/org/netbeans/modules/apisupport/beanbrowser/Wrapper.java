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

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

/** The basic class--a wrapper for a node. */
public class Wrapper extends FilterNode {

    private Wrapper (Node orig) {
        super (orig, new WrapperKids (orig));
    }

    /** Create a wrapper node from an original.
    * Specially prevents recursion (creating a wrapper of a wrapper).
    */
    public static Node make (Node orig) {
        if (orig instanceof Wrapper) {
            // FQN to avoid interpretation as FilterNode.Children:
            org.openide.nodes.Children kids = new Children.Array ();
            kids.add (new Node[] { orig.cloneNode () });
            AbstractNode toret = new AbstractNode (kids) {
                                     public HelpCtx getHelpCtx () {
                                         return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser");
                                     }
                                 };
            toret.setName ("Already a wrapper node...");
            toret.setIconBase ("/org/netbeans/modules/apisupport/resources/BeanBrowserIcon");
            return toret;
        } else {
            return new Wrapper (orig);
        }
    }

    public Node cloneNode () {
        return new Wrapper (getOriginal ());
    }

    // Override to include special node-exploration action.
    public SystemAction[] getActions () {
        SystemAction[] orig = super.getActions ();
        if (orig == null) orig = new SystemAction[0];
        boolean includeSep = orig.length > 0 && orig[0] != null;
        SystemAction[] nue = new SystemAction[orig.length + (includeSep ? 2 : 1)];
        nue[0] = SystemAction.get (NodeExploreAction.class);
        if (includeSep) nue[1] = null;
        for (int i = 0; i < orig.length; i++)
            nue[i + (includeSep ? 2 : 1)] = orig[i];
        return nue;
    }

    // For access by NodeExploreAction:
    public Node getOriginal () {
        return super.getOriginal ();
    }

}

/*
 * Log
 *  10   Gandalf   1.9         10/25/99 Jesse Glick     NodeExploreAction.
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/13/99 Jesse Glick     Various fixes and 
 *       enhancements:    - added a Changes.txt    - fixed handling of 
 *       OpenAPIs.zip on install/uninstall (previously did not correctly unmount
 *       on uninstall, nor check for already-mounted on install)    - added a 
 *       CompilerTypeTester    - display name & icon updates from Tim    - 
 *       removed link to ToDo.txt from docs page    - various BeanInfo's, both 
 *       in templates and in the support itself, did not display superclass 
 *       BeanInfo correctly    - ExecutorTester now permits user to customize 
 *       new executor instance before running it
 *  7    Gandalf   1.6         10/7/99  Jesse Glick     Package change. Also 
 *       cloning in Wrapper.make, which may be necessary.
 *  6    Gandalf   1.5         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         5/27/99  Jesse Glick     Clean-up: comments, 
 *       licenses, removed debugging code, a few minor code changes.
 *  4    Gandalf   1.3         5/25/99  Jesse Glick     Fully cleaned up name 
 *       handling, looks much nicer now. Much safer too.
 *  3    Gandalf   1.2         5/13/99  Jesse Glick     
 *  2    Gandalf   1.1         5/13/99  Jesse Glick     
 *  1    Gandalf   1.0         5/13/99  Jesse Glick     
 * $
 */
