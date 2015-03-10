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

import org.openide.cookies.CompilerCookie.Clean;
import org.openide.compiler.Compiler;
import org.openide.util.HelpCtx;

/** Forcibly compiles selected nodes.
*
* @author Ales Novak
*/
public class CleanAction extends AbstractCompileAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1376549632146453810L;

    /* The depth the compiler compiles on
    * @return depth for the job that this compiler works on
    */
    protected Compiler.Depth depth () {
        return Compiler.DEPTH_ONE;
    }


    /* The cookie class we request.
    */
    protected final Class cookie () {
        return Clean.class;
    }

    /** Message to display when the action is looking for
    * object that should be processed.
    *
    * @return text to display at status line
    */
    protected String message () {
        return ActionConstants.BUNDLE.getString ("CTL_CleanStarted");
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return ActionConstants.BUNDLE.getString("Clean");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (CleanAction.class);
    }

    /* URL to this action.
    * @return URL to the action icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/clean.gif"; // NOI18N
    }
}

/*
 * Log
 *  5    Gandalf   1.4         1/12/00  Ian Formanek    NOI18N
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         10/5/99  Jaroslav Tulach Looking for objects to 
 *       compile/build/clean/etc.
 *  2    Gandalf   1.1         8/9/99   Ian Formanek    icons
 *  1    Gandalf   1.0         8/5/99   Ales Novak      
 * $
 */
