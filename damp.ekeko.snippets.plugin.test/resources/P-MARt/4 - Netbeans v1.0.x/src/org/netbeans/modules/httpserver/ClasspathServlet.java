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

package org.netbeans.modules.httpserver;

import javax.servlet.*;
import javax.servlet.http.*;

import org.openide.util.NbBundle;

/** Servlet for handling NetBeans system classpath requests
*
* @author Petr Jiricka
* @version 0.11 May 5, 1999
*/
public class ClasspathServlet extends NbBaseServlet {

    static final long serialVersionUID =426128385123489216L;
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {

        if (!checkAccess(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                               NbBundle.getBundle(NbBaseServlet.class).getString("MSG_HTTP_FORBIDDEN"));
            return;
        }

        if (handleClasspathRequest(request, response)) {
            return;
        }

        if (handleRepositoryRequest(request, response)) {
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }



    /**
    * Returns a short description of the servlet.
    */
    public String getServletInfo() {
        return NbBundle.getBundle(ClasspathServlet.class).getString("MSG_ClassPathServletDescr");
    }

}

/*
 * Log
 *  7    Gandalf   1.6         1/12/00  Petr Jiricka    i18n
 *  6    Gandalf   1.5         1/11/00  Petr Jiricka    Debug output removed
 *  5    Gandalf   1.4         1/3/00   Petr Jiricka    
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/7/99  Petr Jiricka    Removed debug println
 *  1    Gandalf   1.0         9/30/99  Petr Jiricka    
 * $
 */
