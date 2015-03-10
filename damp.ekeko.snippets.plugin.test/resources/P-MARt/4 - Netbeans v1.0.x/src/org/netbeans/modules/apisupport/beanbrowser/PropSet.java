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

import org.openide.nodes.*;
import org.openide.util.HelpCtx;

/** A node representing a property set of the parent node.
* E.g. might represent regular or expert properties.
*/
public class PropSet extends AbstractNode {

    private Node original;
    private Node.PropertySet ps;

    public PropSet (Node original, Node.PropertySet ps) {
        super (new PropSetKids (original, ps));
        setName ("Properties (" + ps.getName () + ")");
        setIconBase ("/org/netbeans/modules/apisupport/resources/BeanBrowserIcon");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser");
    }

}

/*
 * Log
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/13/99 Jesse Glick     Various fixes and 
 *       enhancements:    - added a Changes.txt    - fixed handling of 
 *       OpenAPIs.zip on install/uninstall (previously did not correctly unmount
 *       on uninstall, nor check for already-mounted on install)    - added a 
 *       CompilerTypeTester    - display name & icon updates from Tim    - 
 *       removed link to ToDo.txt from docs page    - various BeanInfo's, both 
 *       in templates and in the support itself, did not display superclass 
 *       BeanInfo correctly    - ExecutorTester now permits user to customize 
 *       new executor instance before running it
 *  6    Gandalf   1.5         10/7/99  Jesse Glick     Context help.
 *  5    Gandalf   1.4         10/7/99  Jesse Glick     Package change. Also 
 *       cloning in Wrapper.make, which may be necessary.
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/27/99  Jesse Glick     Clean-up: comments, 
 *       licenses, removed debugging code, a few minor code changes.
 *  2    Gandalf   1.1         5/25/99  Jesse Glick     Fully cleaned up name 
 *       handling, looks much nicer now. Much safer too.
 *  1    Gandalf   1.0         5/18/99  Jesse Glick     
 * $
 */
