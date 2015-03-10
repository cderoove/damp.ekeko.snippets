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

package org.netbeans.modules.web.core.jsploader;


import org.openide.loaders.DataObject;
import org.openide.debugger.DebuggerException;
import org.openide.cookies.ExecCookie;
import org.openide.cookies.DebuggerCookie;

/** Support for execution and debugging of a servlet generated from a JSP file.
* Does not execute/debug this servlet,
* rather, executes the original JSP page (if it is found).
*
* @author Petr Jiricka
*/
public class JspServletExecSupport extends Object
    implements ExecCookie {

    /** source JSP page be associated with */
    protected DataObject sourceJspPage;

    /** Create new support for given entry. The file is taken from the
    * entry and is updated if the entry moves or renames itself.
    * @param entry entry to create instance from
    */
    public JspServletExecSupport (DataObject sourceJspPage) {
        this.sourceJspPage = sourceJspPage;
    }

    /* Starts the class.
    */
    public void start () {
        ExecCookie ec = (ExecCookie)sourceJspPage.getCookie(ExecCookie.class);
        if (ec != null)
            ec.start();
        else {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                new Exception(org.openide.util.NbBundle.getBundle(JspServletExecSupport.class).
                              getString("CTL_NotExecutable")).printStackTrace();
        }
    }

    /* Start debugging of associated object.
    * @param stopOnMain if <code>true</code>, debugger stops on the first line of debugged code
    * @exception DebuggerException if the session cannot be started
    */
    public void debug (final boolean stopOnMain) throws DebuggerException {
        DebuggerCookie dc = (DebuggerCookie)sourceJspPage.getCookie(DebuggerCookie.class);
        if (dc != null)
            dc.debug(stopOnMain);
    }

}

/*
* Log
*  4    Gandalf   1.3         1/16/00  Petr Jiricka    DebuggerCookie removed
*  3    Gandalf   1.2         1/12/00  Petr Jiricka    Fully I18n-ed
*  2    Gandalf   1.1         1/12/00  Petr Jiricka    i18n phase 1
*  1    Gandalf   1.0         12/29/99 Petr Jiricka    
* $
*/
