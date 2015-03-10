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

import org.openide.actions.ExecuteAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;

import org.netbeans.modules.jarpackager.util.JarUtils;

/** Inokes deploy mechanism for jar file.
*
* @author Dafe Simonek
*/
public class DeployJarAction extends ExecuteAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 546212346547575788L;

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(DeployJarAction.class).
               getString("CTL_DeployJarAction");
    }

    /** Enables this action only if jar content
    * information is available */
    protected boolean enable (Node[] activatedNodes) {
        // only for one node
        if (activatedNodes.length != 1) {
            return false;
        }
        // enable only when jarContent exists
        return JarUtils.jarContentFromNode(activatedNodes[0]) != null;
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (DeployJarAction.class); //PENDING
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/netbeans/modules/jarpackager/resources/deployJar.gif"; // NOI18N
    }

}

/*
* <<Log>>
*  5    Gandalf   1.4         1/16/00  David Simonek   i18n
*  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems copyright in file comment
*  3    Gandalf   1.2         10/13/99 David Simonek   jar content now primary 
*       file, other small changes
*  2    Gandalf   1.1         8/18/99  David Simonek   stupid bugs fixes
*  1    Gandalf   1.0         8/17/99  David Simonek   
* $
*/