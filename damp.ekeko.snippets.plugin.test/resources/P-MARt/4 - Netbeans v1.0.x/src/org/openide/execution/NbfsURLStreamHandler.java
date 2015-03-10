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

package org.openide.execution;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/**This is a URLStreamHandlerClass for Nbfs URL.
*
* @author Ales Novak, Petr Hamernik
* @version 0.11, Apr 28, 1998
*/
class NbfsURLStreamHandler extends URLStreamHandler {

    // the bundle to use
    static ResourceBundle bundle = NbBundle.getBundle(NbfsURLStreamHandler.class);

    /**
    * @param u - URL to open connection to.
    * @return new URLConnection.
    */
    public URLConnection openConnection(URL u) throws IOException {
        if (u.getProtocol().equals(NbfsURLConnection.PROTOCOL)) {
            return new NbfsURLConnection(u);
        }
        else {
            throw new IOException(bundle.getString("EXC_UnrecognizedProtocol"));
        }
    }
}

/*
 * Log
 *  3    src-jtulach1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    src-jtulach1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    src-jtulach1.0         3/26/99  Jaroslav Tulach 
 * $
 */
