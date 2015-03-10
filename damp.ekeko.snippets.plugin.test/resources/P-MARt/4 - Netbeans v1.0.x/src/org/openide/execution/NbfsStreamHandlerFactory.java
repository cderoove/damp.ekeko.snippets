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

import java.net.URLStreamHandlerFactory;
import java.net.URLStreamHandler;
import java.io.IOException;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** Handle custom URL protocol for accessing internal resources.
*
* @author Ales Novak, Petr Hamernik
*/
public class NbfsStreamHandlerFactory extends Object implements URLStreamHandlerFactory {
    /** Create a new URL stream handler for accessing IDE-internal resources.
    * @param protocol the URL protocol. This should always be {@link NbfsURLConnection#PROTOCOL fixed}.
    * @return a stream handler if the proper protocol was specified, <code>null</code> otherwise
    */
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals(NbfsURLConnection.PROTOCOL)){
            return new NbfsURLStreamHandler();
        } else {
            return null;
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
