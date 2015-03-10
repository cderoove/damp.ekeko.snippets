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

package org.netbeans.modules.objectbrowser;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/**
* ShowBrowserAction action.
*
* @author Jan Jancura
*/
public class ShowBrowserAction extends CallableSystemAction {

    /** generated Serialized Version UID */
    static final long serialVersionUID = 1391479985940417455L;

    /** Link to the class browser. */
    static ObjectBrowser objectBrowser = null;

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle (ShowBrowserAction.class).getString ("CTL_Object_browser_name");
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/netbeans/modules/objectbrowser/resources/browser.gif"; // NOI18N
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ShowBrowserAction.class);
    }

    /** This method is called by one of the "invokers" as a result of
    * some user's action that should lead to actual "performing" of the action.
    * This default implementation calls the assigned actionPerformer if it
    * is not null otherwise the action is ignored.
    */
    public void performAction () {
        if (objectBrowser != null) objectBrowser.open ();
        else (objectBrowser = new ObjectBrowser ()).open ();
    }

    /** Returns instance of object browser.
    * @return instance of object browser.
    */
    public ObjectBrowser getObjectBrowser () {
        if (objectBrowser == null)
            objectBrowser = new ObjectBrowser ();
        return objectBrowser;
    }
}

/*
 * Log
 *  9    src-jtulach1.8         1/13/00  Radko Najman    I18N
 *  8    src-jtulach1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    src-jtulach1.6         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  6    src-jtulach1.5         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    src-jtulach1.4         5/26/99  Ian Formanek    Actions cleanup
 *  4    src-jtulach1.3         5/6/99   Jan Jancura     
 *  3    src-jtulach1.2         4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  2    src-jtulach1.1         4/2/99   Jan Jancura     
 *  1    src-jtulach1.0         3/23/99  Jan Jancura     
 * $
 */
