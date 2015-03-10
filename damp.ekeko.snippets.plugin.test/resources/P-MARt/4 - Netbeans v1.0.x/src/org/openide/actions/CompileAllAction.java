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

import org.openide.cookies.CompilerCookie;
import org.openide.compiler.Compiler;
import org.openide.util.HelpCtx;

/** Compiles selected nodes, descending recursively into directories.
*
* @author   Petr Hamernik
* @version  0.10, Apr 16, 1998
*/
public class CompileAllAction extends AbstractCompileAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -4304121565527189258L;

    /* The depth the compiler compiles on
    * @return depth for the job that this compiler works on
    */
    protected Compiler.Depth depth () {
        return Compiler.DEPTH_INFINITE;
    }

    /* The cookie class we request.
    */
    protected final Class cookie () {
        return CompilerCookie.Compile.class;
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return ActionConstants.BUNDLE.getString("CompileAll");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (CompileAllAction.class);
    }

    /* URL to this action.
    * @return URL to the action icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/compileAll.gif"; // NOI18N
    }
}

/*
 * Log
 *  10   Gandalf   1.9         1/12/00  Ian Formanek    NOI18N
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  7    Gandalf   1.6         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  4    Gandalf   1.3         3/26/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
