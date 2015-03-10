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

import java.beans.*;
import java.net.*;
import java.io.*;
import java.util.*;

import org.openide.*;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/** Acts as UDP server for requests from the Main class (for example).
* @author Jaroslav Tulach, Jesse Glick
*/
public class Server extends Object implements Runnable {

    /** max length of transferred data */
    private static final int LENGTH = 512;
    /** true if we should stop due to uninstallation */
    private static boolean stop;

    static void startup () {
        //System.err.println("Server.startup");
        stop = false;
        if (! Settings.DEFAULT.isActualRunning ()) {
            //System.err.println("Server.startup: starting thread");
            new Thread (new Server (), "OpenFile Server").start (); // NOI18N
        }
    }

    public static void shutdown () {
        //System.err.println("Server.shutdown");
        stop = true;
        if (Settings.DEFAULT.isActualRunning ()) {
            //System.err.println("Server.shutdown: will actually send shutdown packet");
            try {
                int port = Settings.DEFAULT.getActualPort ();
                if (port == 0) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                        System.err.println ("Port should not have been zero during shutdown!");
                    return;
                }
                DatagramPacket p = new DatagramPacket (new byte[] { (byte) 'X' }, 1, InetAddress.getLocalHost (), port);
                DatagramSocket s = new DatagramSocket ();
                try {
                    s.send (p);
                } finally {
                    s.close ();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace ();
            }
        }
    }

    /** the socket to use */
    private static DatagramSocket s;
    /** set up the socket */
    private static void initSocket () {
        //System.err.println("Server.initSocket");
        if (s != null) s.close ();
        int port = Settings.DEFAULT.getPort ();
        try {
            s = new DatagramSocket (port);
            Settings.DEFAULT.setActualPort0 (port);
        } catch (SocketException e) {
            TopManager.getDefault ().notify (new NotifyDescriptor.Message (SettingsBeanInfo.getString ("MSG_cannotBind", new Integer (port))));
            Settings.DEFAULT.setRunning (false);
            Settings.DEFAULT.setActualPort0 (0);
        }
    }
    /** Waits on the connection.
    */
    public void run () {
        //System.err.println("Server.run");
        initSocket ();
        DatagramPacket p = new DatagramPacket (new byte[LENGTH], LENGTH);
        try {
REQUESTS: while (! stop && Settings.DEFAULT.isRunning ()) {
                //System.err.println("Server.run @REQUESTS");
                if (Settings.DEFAULT.getPort () != Settings.DEFAULT.getActualPort ())
                    initSocket ();
                if (! Settings.DEFAULT.isActualRunning ())
                    Settings.DEFAULT.setActualRunning0 (true);
                p.setLength (LENGTH);
                //System.err.println("Server.run: will receive");
                s.receive (p);
                //System.err.println("Server.run: received");
                // Check access:
                if (Settings.DEFAULT.getAccess () == Settings.ACCESS_LOCAL) {
                    if (! p.getAddress ().equals (InetAddress.getLocalHost ())) {
                        TopManager.getDefault ().notify (new NotifyDescriptor.Message (SettingsBeanInfo.getString ("MSG_rejectHost", p.getAddress ())));
                        continue;
                    }
                }
                // Try to open the requested file:
                String fileName = new String (p.getData (), p.getOffset () + 1, p.getLength () - 1);
                int lineNumber;
                int index = fileName.indexOf ('@');
                if (index == -1) {
                    lineNumber = -1;
                } else {
                    try {
                        lineNumber = Integer.parseInt (fileName.substring (index + 1)) - 1;
                        fileName = fileName.substring (0, index);
                    } catch (NumberFormatException nfe) {
                        TopManager.getDefault ().notifyException (nfe);
                        lineNumber = -1;
                    }
                }
                //System.err.println("Server.run: to open " + fileName + " at " + lineNumber);
                final boolean wait;
                switch ((char) (p.getData ()[p.getOffset ()])) {
                case 'Y':
                    wait = true;
                    break;
                case 'N':
                    wait = false;
                    break;
                case 'X':
                    if (stop)
                        break REQUESTS;
                    else
                        continue REQUESTS;
                default:
                    throw new IOException (NbBundle.getBundle (Server.class).getString ("EXC_bad_lead_char"));
                }

                byte res;
                final File f = new File (fileName);
                if (f.exists () && f.isFile ()) {
                    res = 0;
                    final InetAddress addr = p.getAddress ();
                    final int port = p.getPort ();
                    // Opening may be slow, incl. prompting for mount point. Should not time out launcher while waiting for user.
                    final int _lineNumber = lineNumber;
                    new Thread (new Runnable () {
                                    public void run () {
                                        //System.err.println("Server.run: to call open on f=" + f + " wait=" + wait + " addr=" + addr + " port=" + port + " lineNumber=" + _lineNumber);
                                        OpenFile.open (f, wait, addr, port, _lineNumber);
                                    }
                                }).start ();
                } else {
                    // Do not time out launcher while waiting for user to press OK.
                    // XXX SwingUtilities.invokeLater?
                    final String _fileName = fileName;
                    new Thread (new Runnable () {
                                    public void run () {
                                        TopManager.getDefault ().notify (new NotifyDescriptor.Message (SettingsBeanInfo.getString ("MSG_fileNotFound", _fileName)));
                                    }
                                }).start ();
                    res = 1;
                }

                // send reply (unless we are waiting for file to be saved)
                if (!wait || res != 0) {
                    //System.err.println("Server.run: will send response res=" + res);
                    p.getData ()[0] = res;
                    p.setLength (1);
                    s.send (p);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace ();
            Settings.DEFAULT.setActualRunning0 (false);
            Settings.DEFAULT.setRunning (false);
        } finally {
            //System.err.println("Server.run: done");
            if (s != null) s.close ();
            s = null;
            Settings.DEFAULT.setActualRunning0 (false);
            Settings.DEFAULT.setActualPort0 (0);
        }
    }

    /** Addresses of waiting launchers, based on the DO they wait on. 
     * @associates InetAddress*/
    private static final Map addresses = new HashMap (); // Map<DataObject, InetAddress>
    /** Ports of waiting launchers. 
     * @associates Integer*/
    private static final Map ports = new HashMap (); // Map<DataObject, Integer>
    /** Listener on all waiting DOs that notices when they are saved (or deleted). */
    private static final PropertyChangeListener waitingListener = new PropertyChangeListener () {
                public void propertyChange (PropertyChangeEvent ev) {
                    DataObject obj = (DataObject) ev.getSource ();
                    if (DataObject.PROP_VALID.equals (ev.getPropertyName ())) {
                        // If destroyed, report an error.
                        if (! obj.isValid ()) {
                            unWait (obj, (byte) 1);
                        }
                    } else if (DataObject.PROP_MODIFIED.equals (ev.getPropertyName ())) {
                        // Don't do anything when it *becomes* modified, only when unmodified.
                        if (! obj.isModified ()) {
                            unWait (obj, (byte) 0);
                        }
                    }
                }
                /** Notify the launcher that it is done waiting on a DO. */
                private void unWait (DataObject obj, byte status) {
                    obj.removePropertyChangeListener (this);
                    if (s != null) {
                        InetAddress addr = (InetAddress) addresses.remove (obj);
                        Integer port = (Integer) ports.remove (obj);
                        DatagramPacket p = new DatagramPacket (new byte[] { status }, 1, addr, port.intValue ());
                        try {
                            s.send (p);
                        } catch (IOException e) {
                            TopManager.getDefault ().notifyException (e);
                        }
                    } else {
                        TopManager.getDefault ().notify (new NotifyDescriptor.Message (SettingsBeanInfo.getString ("MSG_serverNotRunningWhenSaved", obj.getName ())));
                    }
                }
            };
    /** Register a callback so that the launcher will be notified when the file is modified & saved.
    * @param obj the object to wait for
    * @param addr the address to send a message back to
    * @param port the port to send a message back to
    */
    static void waitFor (DataObject obj, InetAddress addr, int port) {
        addresses.put (obj, addr);
        ports.put (obj, new Integer (port));
        obj.addPropertyChangeListener (waitingListener);
    }

    /** Test run. */
    /*
    public static void main (String[] args) {
      startup ();
}
    */

}

/*
 * Log
 *  12   Gandalf   1.11        1/12/00  Jesse Glick     I18N.
 *  11   Gandalf   1.10        1/12/00  Ales Novak      can be compiled under 
 *       1.3
 *  10   Gandalf   1.9         1/7/00   Jesse Glick     -line option for line 
 *       numbers.
 *  9    Gandalf   1.8         1/4/00   Jesse Glick     Accessibility for 
 *       installation from Utils module.
 *  8    Gandalf   1.7         11/2/99  Jesse Glick     Overhauled socket 
 *       handling.
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/10/99  Jesse Glick     #3647 - 
 *       SocketException's on Linux + native threads.
 *  5    Gandalf   1.4         8/17/99  Jesse Glick     Changed handling of 
 *       return status code to be more immediate and simplified. Fixes #2420 and
 *       #3297.
 *  4    Gandalf   1.3         7/29/99  Jesse Glick     Abortive attempt at 
 *       fixing #2966.
 *  3    Gandalf   1.2         7/29/99  Jesse Glick     Bugfix #2755.
 *  2    Gandalf   1.1         7/10/99  Jesse Glick     Open File module moved 
 *       to core.
 *  1    Gandalf   1.0         7/10/99  Jesse Glick     
 * $
 */
