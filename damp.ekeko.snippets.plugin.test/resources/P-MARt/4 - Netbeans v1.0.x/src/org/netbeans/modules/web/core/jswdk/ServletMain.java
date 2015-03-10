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

package org.netbeans.modules.web.core.jswdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import com.sun.web.server.HttpServer;

/** A program that is executed externally to start a servlet server.
* Parameters: port, document root URL, workDir
*
* @author Petr Jiricka
*/
public final class ServletMain {

    /**
    * @param args startup parameters, see this class's JavaDoc
    */
    public static void main (String[] arg) throws MalformedURLException, IOException, Exception {

        int port = Integer.parseInt(arg[0]);
        String docRootURL = arg[1];
        String workDir = arg[2];

        HttpServer server = new HttpServer(port, null, null);

        try {
            server.setDocumentBase(new URL(docRootURL));
        }
        catch (MalformedURLException e) {
            throw new InternalError();
        }

        server.setWorkDir(workDir);
        server.setWorkDirPersistent(true);

        server.start();

    }

}

/*
 * Log
 *  9    Gandalf   1.8         1/17/00  Petr Jiricka    Debug outputs removed
 *  8    Gandalf   1.7         1/13/00  Petr Jiricka    More i18n
 *  7    Gandalf   1.6         1/12/00  Petr Jiricka    i18n phase 1
 *  6    Gandalf   1.5         12/29/99 Petr Jiricka    Various execution fixes
 *  5    Gandalf   1.4         12/20/99 Petr Jiricka    Checking in changes made
 *       in the U.S.
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         10/12/99 Petr Jiricka    Removed debug messages
 *  2    Gandalf   1.1         10/7/99  Petr Jiricka    
 *  1    Gandalf   1.0         10/7/99  Petr Jiricka    
 * $
 */
