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

package org.netbeans.modules.openfile;

import java.io.File;
import java.net.*;
import java.util.ResourceBundle;

/** Sends names of files through a datagram socket
* to the NetBeans IDE.
* The class {@link OpenFile} then acts as a server and tries to open the files in the
* Editor or Explorer as appropriate.
* <p>
* Arguments by default are filenames. May also pass the following options:
* <ol>
* <li> <code>-host</code> <em>hostname</em>
* <br> The host name defaults to the local host.
* <li> <code>-port</code> <em>port number</em>
* <br> The port number defaults to the <em>static setting</em> in {@link #DEFAULT_PORT};
* you may change the live port in the control panel but this does not affect
* the static default, so you must then set the port number explicitly.
* <li> <code>-canon</code> and <code>-nocanon</code>
* <br> The launcher default to canonicalizing the file names automatically,
* but you can disable this. This might be useful if you are trying to open
* a file from a remote host.
* <li> <code>-wait</code> and <code>-nowait</code>
* <br> You may ask that the launcher wait until the user has finished edits.
* This may be useful if the launcher is to be used as a callback-editor,
* as is used for example in version-control systems as an "external editor".
* As an example, think of the Emacs <code>emacsclient</code> tool.
* <li> <code>-line</code> <em>line number</em>
* Request that a file be opened at a specified line number (starting at 1).
* Applies only to the next file on the command line.
* </ol>
* @author Jaroslav Tulach, Jesse Glick
*/
public class Main extends Object {
    /** Default port number to use. */
    static final int DEFAULT_PORT = 7318;
    /** Time-out for answer. */
    private static final int TIME_OUT = 3500;

    /** Send the message.
    * @param args list of filenames and options
    * @throws Exception if anything goes wrong, like bad option syntax, hostname lookup, etc.
    */
    public static void main (String[] args) {
        try {
            int port = DEFAULT_PORT;
            int lineNumber = -1;
            InetAddress host = InetAddress.getLocalHost ();
            boolean canon = true;
            boolean wait = false;
            DatagramSocket s = new DatagramSocket ();
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals ("-host")) { // NOI18N
                    host = InetAddress.getByName (args[++i]);
                } else if (arg.equals ("-port")) { // NOI18N
                    port = Integer.parseInt (args[++i]);
                } else if (arg.equals ("-canon")) { // NOI18N
                    canon = true;
                } else if (arg.equals ("-nocanon")) { // NOI18N
                    canon = false;
                } else if (arg.equals ("-wait")) { // NOI18N
                    wait = true;
                } else if (arg.equals ("-nowait")) { // NOI18N
                    wait = false;
                } else if (arg.equals ("-help")) { // NOI18N
                    System.err.println (ResourceBundle.getBundle ("org.netbeans.modules.openfile.Bundle").getString ("TXT_launcherHelp"));
                } else if (arg.equals ("-line")) { // NOI18N
                    lineNumber = Integer.parseInt (args[++i]);
                } else {
                    // Absolute file name to send.
                    File f = new File (arg);
                    if (canon) f = f.getCanonicalFile ();
                    String toSend = (wait ? "Y" : "N") + f.toString (); // NOI18N
                    if (lineNumber != -1) {
                        toSend += "@" + lineNumber; // NOI18N
                        lineNumber = -1;
                    }
                    byte[] arr = toSend.getBytes ();
                    DatagramPacket p = new DatagramPacket (arr, 0, arr.length, host, port);
                    s.send (p);

                    // wait for reply
                    p.setLength (1);
                    if (! wait) s.setSoTimeout (TIME_OUT);
                    s.receive (p);

                    if (p.getData ()[0] != 0) {
                        System.exit (p.getData ()[0]);
                    }
                }
            }
        } catch (java.io.IOException ex) {
            // exit with error
            ex.printStackTrace();
            System.exit (2);
        }
        // ok
        System.exit (0);
    }
}

/*
 * Log
 *  11   Gandalf   1.10        1/12/00  Jesse Glick     I18N.
 *  10   Gandalf   1.9         1/7/00   Jesse Glick     -line option for line 
 *       numbers.
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         7/10/99  Jesse Glick     Open File module moved 
 *       to core.
 *  7    Gandalf   1.6         7/10/99  Jesse Glick     Sundry clean-ups (mostly
 *       bundle usage).
 *  6    Gandalf   1.5         5/25/99  Jesse Glick     Added -wait.
 *  5    Gandalf   1.4         5/25/99  Jaroslav Tulach Waits for notification 
 *       that the open command succeeded.
 *  4    Gandalf   1.3         5/22/99  Jesse Glick     Licenses.
 *  3    Gandalf   1.2         5/22/99  Jesse Glick     Various options, and 
 *       doc.
 *  2    Gandalf   1.1         5/22/99  Jesse Glick     
 *  1    Gandalf   1.0         5/19/99  Jesse Glick     
 * $
 */
