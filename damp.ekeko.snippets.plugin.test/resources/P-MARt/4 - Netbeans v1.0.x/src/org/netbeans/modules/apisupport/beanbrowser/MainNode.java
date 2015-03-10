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
import org.openide.util.HelpCtx;

import java.beans.IntrospectionException;

/** Node to browse the desktop, other roots, and the TopManager. */
public class MainNode extends AbstractNode {

    public MainNode () {
        super (new Children.Array ());
        Places.Nodes pn = TopManager.getDefault ().getPlaces ().nodes ();
        insert (pn.projectDesktop ());
        Node[] roots = pn.roots ();
        for (int i = 0; i < roots.length; i++)
            insert (roots[i]);
        try {
            Node n = new RefinedBeanNode (TopManager.getDefault ());
            n.setDisplayName ("TopManager");
            insert (n);
        } catch (IntrospectionException e) {
            e.printStackTrace ();
        }
        setName (getClass ().getName ()); // something to placate node handles
        setDisplayName ("Bean Browser");
        setIconBase ("/org/netbeans/modules/apisupport/resources/BeanBrowserIcon");
    }

    /** Add a wrapper for a node to this node's children.
    * @param orig the original node
    */
    protected void insert (Node orig) {
        getChildren ().add (new Node[] { Wrapper.make (orig) });
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser");
    }

}

/*
 * Log
 *  14   Gandalf   1.13        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  13   Gandalf   1.12        10/13/99 Jesse Glick     Various fixes and 
 *       enhancements:    - added a Changes.txt    - fixed handling of 
 *       OpenAPIs.zip on install/uninstall (previously did not correctly unmount
 *       on uninstall, nor check for already-mounted on install)    - added a 
 *       CompilerTypeTester    - display name & icon updates from Tim    - 
 *       removed link to ToDo.txt from docs page    - various BeanInfo's, both 
 *       in templates and in the support itself, did not display superclass 
 *       BeanInfo correctly    - ExecutorTester now permits user to customize 
 *       new executor instance before running it
 *  12   Gandalf   1.11        10/7/99  Jesse Glick     Context help.
 *  11   Gandalf   1.10        10/7/99  Jesse Glick     Package change. Also 
 *       cloning in Wrapper.make, which may be necessary.
 *  10   Gandalf   1.9         9/15/99  Jesse Glick     Fix for Explorer handles
 *       during restart.
 *  9    Gandalf   1.8         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    Gandalf   1.7         5/27/99  Jesse Glick     Clean-up: comments, 
 *       licenses, removed debugging code, a few minor code changes.
 *  7    Gandalf   1.6         5/25/99  Jesse Glick     Fully cleaned up name 
 *       handling, looks much nicer now. Much safer too.
 *  6    Gandalf   1.5         5/24/99  Jesse Glick     Using RefinedBeanNode 
 *       for CustomizeBeanAction.
 *  5    Gandalf   1.4         5/21/99  Jesse Glick     Main method moved to 
 *       action.
 *  4    Gandalf   1.3         5/18/99  Jesse Glick     Added main method for 
 *       testing.
 *  3    Gandalf   1.2         5/14/99  Jesse Glick     
 *  2    Gandalf   1.1         5/13/99  Jesse Glick     
 *  1    Gandalf   1.0         5/13/99  Jesse Glick     
 * $
 */
