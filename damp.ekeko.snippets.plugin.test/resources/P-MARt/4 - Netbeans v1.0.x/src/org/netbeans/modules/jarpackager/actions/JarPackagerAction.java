/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jarpackager.actions;

import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.jarpackager.PackagingView;

/** The action that opens managing panel of jar packager.
*
* @author Dafe Simonek
*/
public class JarPackagerAction extends CallableSystemAction {

    /** Creates new JarPackagerAction */
    public JarPackagerAction () {
        //System.out.println("Creating Jar packager action..."); // NOI18N
    }

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle (JarPackagerAction.class).
               getString ("CTL_PackagerView");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(JarPackagerAction.class);
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/netbeans/modules/jarpackager/resources/jarObject.gif"; // NOI18N
    }

    /** Opens packaging view. */
    public void performAction () {
        PackagingView pv = PackagingView.getPackagingView();
        pv.open();
        pv.requestFocus();
    }

}

/*
* <<Log>>
*  11   Gandalf   1.10        1/26/00  David Simonek   Minor changes concerning 
*       correct action installation / removal
*  10   Gandalf   1.9         1/25/00  David Simonek   Various bugfixes and i18n
*  9    Gandalf   1.8         1/16/00  David Simonek   i18n
*  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    Gandalf   1.6         8/17/99  David Simonek   installations of actions,
*       icon changing
*  6    Gandalf   1.5         7/11/99  David Simonek   
*  5    Gandalf   1.4         6/10/99  David Simonek   progress indocator + 
*       minor bugfixes....
*  4    Gandalf   1.3         6/9/99   Ian Formanek    Fixed resources for 
*       package change
*  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         6/8/99   David Simonek   
*  1    Gandalf   1.0         6/3/99   David Simonek   
* $
*/