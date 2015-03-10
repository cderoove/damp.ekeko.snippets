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

package org.openide.actions;

import org.openide.loaders.DataObject;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;
import org.openide.cookies.SaveCookie;


/** Save an object.
* @see SaveCookie
*
* @author   Jan Jancura, Petr Hamernik, Ian Formanek, Dafe Simonek
*/
public class SaveAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 8726214103323017934L;

    protected Class[] cookieClasses () {
        return new Class[] { SaveCookie.class };
    }

    protected boolean enable (final Node[] activatedNodes) {
        if (!super.enable (activatedNodes)) return false;
        boolean result = activatedNodes[0].getCookie (SaveCookie.class) != null;
        //System.out.println ("Save enable: " + result); // NOI18N
        return result;
    }

    protected void performAction (final Node[] activatedNodes) {
        SaveCookie sc = (SaveCookie)activatedNodes[0].getCookie (SaveCookie.class);
        if (sc != null) {
            try {
                sc.save();
            }
            catch (java.io.IOException e) {
                TopManager.getDefault().notifyException(e);
            }
        }
    }

    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return ActionConstants.BUNDLE.getString("Save");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (SaveAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/save.gif"; // NOI18N
    }
}

/*
 * Log
 *  16   Gandalf   1.15        1/13/00  Ian Formanek    NOI18N
 *  15   Gandalf   1.14        1/12/00  Ian Formanek    NOI18N
 *  14   Gandalf   1.13        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  13   Gandalf   1.12        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  12   Gandalf   1.11        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  11   Gandalf   1.10        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  10   Gandalf   1.9         5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  9    Gandalf   1.8         5/2/99   Ian Formanek    Fixed last change
 *  8    Gandalf   1.7         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  7    Gandalf   1.6         4/9/99   Ian Formanek    Removed debug printlns
 *  6    Gandalf   1.5         3/26/99  Jesse Glick     [JavaDoc]
 *  5    Gandalf   1.4         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  4    Gandalf   1.3         1/13/99  David Simonek   
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    fixed resource names
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach 
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
