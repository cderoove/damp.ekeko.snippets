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

package org.netbeans.modules.web.core;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.MalformedURLException;

/** Utility class
* @author  Petr Jiricka
* @version 1.00, Jun 03, 1999
*/
public class Util {

    public static Util getUtil() {
        return new Util();
    }

    private boolean connectionEnd;

    /** Waits for startup of a server, waits until the connection has been established. */
    public boolean waitForURLConnection(URL url, int timeout, int retryTime) {
        connectionEnd = false;
        javax.swing.Timer timer = new javax.swing.Timer(timeout,
                                  new java.awt.event.ActionListener() {
                                      public void actionPerformed(java.awt.event.ActionEvent e) {
                                          connectionEnd = true;
                                      }
                                  });
        timer.setRepeats(false);
        timer.start();
        BufferedReader in = null;
        URLConnection connection;
        while (!connectionEnd) {
            try {
                /*        servletConnection = null;
                        // wait for the server to start - check by connecting to the port
                        servletConnection = url.openConnection();  
                        servletConnection.setDoInput(true);
                        servletConnection.setDoOutput(false);	        
                        servletConnection.setUseCaches(false);
                        servletConnection.setDefaultUseCaches(false);
                //        servletConnection.setAllowUserInteraction(false);
                        servletConnection.setRequestProperty ("Content-Type", "application/octet-stream");
                        inputFromServlet = servletConnection.getInputStream();
                        int sym;
                        while((sym = inputFromServlet.read()) != -1);*/

                in = new BufferedReader(new InputStreamReader(
                                            url.openStream()));
                String inputLine;
                //while ((inputLine = in.readLine()) != null)
                ;
                connectionEnd = true;
                return true;
            }
            catch (java.io.FileNotFoundException e) {
                // the server returned code > 400 (not found)
                connectionEnd = true;
                return true;
            }
            catch (Exception e) { /* that's ok, just retry */ }
            finally {
                if (in != null)
                    try {
                        in.close();
                    }
                    catch(IOException e) {
                        //e.printStackTrace();
                    }
                try {
                    Thread.currentThread().sleep(retryTime);
                }
                catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        }
        return false;
    }


}

/*
 * Log
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/12/99 Petr Jiricka    Removed debug messages
 *  3    Gandalf   1.2         8/9/99   Petr Jiricka    Removed debug prints
 *  2    Gandalf   1.1         7/27/99  Petr Jiricka    
 *  1    Gandalf   1.0         7/24/99  Petr Jiricka    
 * $
 */
