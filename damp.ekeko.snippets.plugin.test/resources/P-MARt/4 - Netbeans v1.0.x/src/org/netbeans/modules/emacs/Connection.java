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

package org.netbeans.modules.emacs;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

// XXX
import org.openide.util.Utilities;

public class Connection implements Runnable, Protocol {

    static boolean DEBUG = false;

    private static final String THREAD_MAGIC = "org.netbeans.modules.emacs.Connection.THREAD_MAGIC:";

    static final int KILL_PORT = 9166;

    private static final Object ERROR_RETURN = new Object () {
                public String toString () {
                    return "<Error>";
                }
            };

    // This section implements Connection as a server.
    /** List of waiting servers by port. These listen for connections from remote targets. 
     * @associates Integer*/
    private static Set servers; // Set<int port>
    /** Get a list of all server ports currently running. */
    public synchronized static Set getServerPorts () {
        if (servers == null)
            return Collections.EMPTY_SET;
        else
            return new HashSet (servers);
    }
    /** Start a new server on a port (if it was not started already). */
    public synchronized static void startServer (final int port) {
        if (servers == null) servers = Collections.synchronizedSet (new HashSet ());
        if (! servers.contains (new Integer (port))) {
            try {
                if (DEBUG) System.err.println ("Starting a Connection server on port " + port + " (I am " + InetAddress.getLocalHost () + ")");
            } catch (UnknownHostException uhe) {
                uhe.printStackTrace ();
            }
            Thread t = new Thread (new Runnable () {
                                       public void run () {
                                           ServerSocket ssock;
                                           try {
                                               try {
                                                   ssock = new ServerSocket (port);
                                               } catch (BindException be) {
                                                   if (DEBUG) {
                                                       System.err.println("BindException caught, will retry after 10 sec...");
                                                       be.printStackTrace ();
                                                   }
                                                   // First try to kill off another instance of this class if there is one:
                                                   InetAddress localhost = InetAddress.getLocalHost ();
                                                   try {
                                                       Socket s = new Socket (localhost, port, localhost, KILL_PORT);
                                                       s.close ();
                                                   } catch (IOException ioe) {
                                                       // Fine, ignore.
                                                   }
                                                   try {
                                                       Thread.sleep (10000);
                                                   } catch (InterruptedException ie) {
                                                       ie.printStackTrace ();
                                                   }
                                                   ssock = new ServerSocket (port);
                                               }
                                           } catch (IOException e1) {
                                               e1.printStackTrace ();
                                               if (DEBUG) System.err.println("Could not bind server, will stop it");
                                               servers.remove (new Integer (port));
                                               return;
                                           }
                                           while (servers.contains (new Integer (port))) {
                                               try {
                                                   if (DEBUG) System.err.println ("Listening for passive connections...");
                                                   Socket sock = ssock.accept ();
                                                   if (sock.getPort () == KILL_PORT) {
                                                       if (DEBUG) System.err.println("Got a KILL_PORT, will stop server");
                                                       servers.remove (new Integer (port));
                                                       return;
                                                   }
                                                   ConnectionInfo info = new ConnectionInfo (sock.getInetAddress (), 0);
                                                   synchronized (Connection.class) {
                                                       if (connections.get (info) != null) {
                                                           if (DEBUG) System.err.println ("Error! Duplicate server connection from " + info);
                                                       } else {
                                                           Connection nue = new Connection (info, sock);
                                                           connections.put (info, nue);
                                                           if (DEBUG) System.err.println ("Added passive connection: " + nue);
                                                           if (DEBUG) System.err.println ("Connections: " + connections.keySet ());
                                                           Connection.class.notifyAll ();
                                                       }
                                                   }
                                                   // Now continue listening for connections.
                                               } catch (IOException e2) {
                                                   e2.printStackTrace ();
                                               }
                                           }
                                           if (DEBUG) System.err.println ("Shutting down Connection server on port " + port);
                                       }
                                   });
            servers.add (new Integer (port));
            t.setDaemon (true);
            t.start ();
        }
    }
    /** Stop the server on a port (if it was running). */
    public synchronized static void stopServer (int port) {
        if (servers.contains (new Integer (port))) {
            if (DEBUG) System.err.println ("Will shut down Connection server on port " + port);
            try {
                InetAddress localhost = InetAddress.getLocalHost ();
                Socket s = new Socket (localhost, port, localhost, KILL_PORT);
                s.close ();
            } catch (IOException ioe) {
                ioe.printStackTrace ();
            }
        }
    }

    /**
     * @associates Connection 
     */
    private final static Map connections = new HashMap (); // Map<ConnectionInfo, Connection>

    private static final class ConnectionInfo {
        private final InetAddress host;
        private final int port;
        ConnectionInfo (InetAddress host, int port) {
            this.host = host;
            this.port = port;
        }
        public int hashCode () {
            return host.hashCode () ^ port;
        }
        public boolean equals (Object o) {
            if (o != null && o instanceof ConnectionInfo) {
                ConnectionInfo oo = (ConnectionInfo) o;
                return host.equals (oo.host) && port == oo.port;
            } else {
                return false;
            }
        }
        public String toString () {
            return host + ":" + port;
        }
    }

    private final ConnectionInfo info;
    private int users;
    private final BufferedReader rd;
    private final Writer wr;
    private final Socket sock;
    private boolean connected;
    private final boolean autoDisconnect;
    private String auth;

    private Connection (ConnectionInfo info, String auth) throws IOException {
        if (DEBUG) System.err.println ("Creating active connection...");
        this.info = info;
        this.auth = auth;
        sock = new Socket (info.host, info.port);
        rd = new BufferedReader (new InputStreamReader (sock.getInputStream ()));
        wr = new OutputStreamWriter (sock.getOutputStream ());
        users = 1;
        connected = true;
        autoDisconnect = true; // XXX make configurable in call to addClient ()
        wr.write (META_AUTH + " " + auth + "\n");
        wr.flush ();
        String response = rd.readLine ();
        if (response == null)
            throw new IOException ("Server closed connection w/o responding to auth");
        if (response.equals (META_ACCEPT)) {
            if (DEBUG) System.err.println ("Auth OK");
        } else if (response.equals (META_REJECT)) {
            throw new IOException ("Server rejected auth");
        } else {
            throw new IOException ("Auth response not understood: " + response);
        }
        Thread t = new Thread (this);
        t.setDaemon (true);
        t.start ();
    }
    private Connection (ConnectionInfo info, Socket sock) throws IOException {
        if (DEBUG) System.err.println ("Creating passive connection...");
        this.info = info;
        this.sock = sock;
        rd = new BufferedReader (new InputStreamReader (sock.getInputStream ()));
        wr = new OutputStreamWriter (sock.getOutputStream ());
        // Wait for them to send desired auth info.
        String hello = rd.readLine ();
        if (hello == null) throw new IOException ("Attached client closed connection w/o sending auth");
        if (! hello.startsWith (META_AUTH + " ")) throw new IOException ("Client sent invalid auth introduction: " + hello);
        auth = hello.substring ((META_AUTH + " ").length ());
        if (DEBUG) System.err.println ("Got passive auth " + auth);
        users = 0;
        connected = true;
        autoDisconnect = false;
        Thread t = new Thread (this);
        t.setDaemon (true);
        t.setName (THREAD_MAGIC + this);
        t.start ();
    }

    private synchronized void disconnect () throws IOException {
        synchronized (Connection.class) {
            if (connected) {
                if (DEBUG) System.err.println("Disconnecting " + this);
                connections.remove (info);
                connected = false;
                wr.write (META_DISCONNECT + "\n");
                wr.close ();
                rd.close ();
                sock.close ();
            }
        }
    }
    protected void finalize () throws Exception {
        disconnect ();
    }

    public static synchronized void disconnectAll () {
        if (DEBUG) System.err.println ("Connection.disconnectAll");
        Iterator it = new HashSet (connections.values ()).iterator ();
        while (it.hasNext ()) {
            try {
                ((Connection) it.next ()).disconnect ();
            } catch (IOException ioe) {
                ioe.printStackTrace ();
            }
        }
    }

    private boolean confirmAuth (String auth) {
        return this.auth.equals (auth);
    }

    public static synchronized Connection addClient (final String host, int port, final String auth) throws IOException {
        ConnectionInfo info = new ConnectionInfo (InetAddress.getByName (host), port);
        if (DEBUG) System.err.println("Adding Connection client for " + info);
        Connection existing = (Connection) connections.get (info);
        if (DEBUG) System.err.println ("Connections: " + connections.keySet ());
        if (existing != null) {
            if (! existing.confirmAuth (auth)) throw new IOException ("Existing connection " + existing + " did not accept auth " + auth);
            if (DEBUG) System.err.println("Existing client, " + existing.users + " users");
            existing.users++;
            return existing;
        } else {
            if (port == 0) {
                // Add passive client (wait for it)
                try {
                    // XXX should avoid showing if there already is one
                    // XXX should auto-close when a connection is made
                    SwingUtilities.invokeLater (new Runnable () {
                                                    public void run () {
                                                        if (DEBUG) System.err.println("addClient II (start)");
                                                        String localhost = "localhost";
                                                        try {
                                                            localhost = InetAddress.getLocalHost ().toString ();
                                                        } catch (IOException ioe) {
                                                            ioe.printStackTrace ();
                                                        }
                                                        JOptionPane.showMessageDialog (null, new String[] {
                                                                                           "Please connect from " + host + " to " + localhost + " on port(s) " + getServerPorts () + " with password " + auth,
                                                                                           "within the next sixty seconds, and then close this dialog.",
                                                                                           "To cancel, just close this dialog immediately."
                                                                                       });
                                                        if (DEBUG) System.err.println("addClient II (mid)");
                                                        synchronized (Connection.class) {
                                                            Connection.class.notifyAll ();
                                                        }
                                                        if (DEBUG) System.err.println("addClient II (end)");
                                                    }
                                                });
                    if (DEBUG) System.err.println("Will wait for a passive connection...");
                    Connection.class.wait (60000);
                    if (connections.get (info) != null)
                        return addClient (host, port, auth);
                    else
                        throw new IOException ("Timed out on adding passive connection (or refused to add one)");
                } catch (InterruptedException ie) {
                    ie.printStackTrace ();
                    throw new IOException (ie.toString ());
                }
            } else {
                // Add active client
                if (DEBUG) System.err.println("New client");
                Connection nue = new Connection (info, auth);
                connections.put (info, nue);
                return nue;
            }
        }
    }

    public synchronized void removeClient () throws IOException {
        if (DEBUG) System.err.println("Removing client " + this + ", " + users + " users");
        synchronized (Connection.class) {
            if (--users == 0 && autoDisconnect)
                disconnect ();
        }
    }

    // read from input stream
    public void run () {
        String line;
        //int padding = 0;
        try {
            while ((line = rd.readLine ()) != null) {
                /*
                if (line.equals ("")) {
                  padding++;
                  continue;
            } else if (padding > 0) {
                  if (DEBUG) System.err.println ("Proxier got padding: " + padding);
                  padding = 0;
            }
                */
                if (DEBUG) System.err.println("Proxier got input line `" + line + "'");
                int idx = line.indexOf (' ');
                if (idx == -1) {
                    maybeCallback (line, new Object[0]);
                } else {
                    String type = line.substring (0, idx);
                    List ls = new ArrayList ();
                    while (idx != line.length ()) {
                        if (line.charAt (idx) != ' ')
                            throw new RuntimeException ("Unexpected char at " + idx + " in `" + line + "': `" + line.charAt (idx) + "'");
                        idx++;
                        Object arg;
                        if (line.charAt (idx) == '"') {
                            // parse escaped string
                            StringBuffer text = new StringBuffer ();
                            idx++;          // skip first "
                            char c;
                            while ((c = line.charAt (idx)) != '"') {
                                if (c == '\\') {
                                    char nextChar = line.charAt (++idx);
                                    if (nextChar == 'n')
                                        text.append ('\n');
                                    else if (nextChar == 'r')
                                        text.append ('\r');
                                    else
                                        text.append (nextChar);
                                } else {
                                    text.append (c);
                                }
                                idx++;
                            }
                            idx++;          // skip last "
                            arg = text.toString ();
                        } else if (line.charAt (idx) == 'T') {
                            arg = Boolean.TRUE;
                            idx++;
                        } else if (line.charAt (idx) == 'F') {
                            arg = Boolean.FALSE;
                            idx++;
                        } else if (line.charAt (idx) == '!') {
                            arg = ERROR_RETURN;
                            idx++;
                        } else {
                            // parse nonnegative integer
                            StringBuffer digits = new StringBuffer ();
                            char c;
                            while (idx != line.length () && Character.isDigit (c = line.charAt (idx))) {
                                digits.append (c);
                                idx++;
                            }
                            int val;
                            try {
                                val = Integer.parseInt (digits.toString ());
                            } catch (NumberFormatException e) {
                                e.printStackTrace ();
                                val = 0;
                            }
                            arg = new Integer (val);
                        }
                        ls.add (arg);
                    }
                    maybeCallback (type, ls.toArray (new Object[ls.size ()]));
                }
            }
            if (connected) {
                if (DEBUG) System.err.println ("Connection closed by server");
                disconnect ();
            }
        } catch (IOException e) {
            if (connected || DEBUG)
                e.printStackTrace ();
            if (! connected && DEBUG)
                System.err.println("...but that is to be expected.");
        }
    }

    /**
     * @associates Object 
     */
    private Map holds = Collections.synchronizedMap (new HashMap ());

    private void maybeCallback (final String type, final Object[] args) {
        try {
            int seq = Integer.parseInt (type);
            Integer seqI = new Integer (seq);
            Object hold = holds.get (seqI);
            if (hold == null) {
                System.err.println ("dead sequence: " + seq); // XXX
            } else {
                //if (DEBUG) System.err.println ("holds.put real result for seqnum " + seq);
                holds.put (seqI, args);
                synchronized (hold) {
                    // XXX does notify() suffice??
                    hold.notifyAll ();
                }
            }
        } catch (NumberFormatException ign) {
            // OK, normal string callback
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                callback (type, args);
                                            }
                                        });
        }
    }

    /**
     * @associates EmacsListener 
     */
    private Set listeners = new HashSet (); // Set<EmacsListener>
    public synchronized void addEmacsListener (EmacsListener l) {
        listeners.add (l);
    }
    public synchronized void removeEmacsListener (EmacsListener l) {
        listeners.remove (l);
    }

    private synchronized void callback (String type, Object[] args) {
        int idx = type.indexOf ('=');
        if (idx == -1) {
            System.err.println ("Bad callback string: " + type);
            return;
        }
        String realType;
        int seq;
        try {
            realType = type.substring (0, idx);
            seq = Integer.parseInt (type.substring (idx + 1));
        } catch (NumberFormatException nfe) {
            System.err.println ("Bad callback string: " + type);
            return;
        }
        boolean serial = false;
        for (int i = 0; i < SERIAL_EVENTS.length; i++) {
            if (realType.indexOf (SERIAL_EVENTS[i]) != -1) {
                serial = true;
                break;
            }
        }
        boolean oos = serial && seq < serialSeqNum;
        if (oos && DEBUG) System.err.println("OOS because: seq=" + seq + " serialSeqNum=" + serialSeqNum);
        EmacsEvent ev = new EmacsEvent (this, realType, args, oos);
        if (DEBUG) System.err.println ("Connection.callback: " + ev);
        Iterator it = listeners.iterator ();
        while (it.hasNext ())
            ((EmacsListener) it.next ()).callback (ev);
    }

    private static int seqNum = 1;
    private int thisSeqNum = 0;
    private int serialSeqNum = 0;
    // Please only call within a synch (this) block:
    private int getNextSeqNum () {
        synchronized (Connection.class) {
            return thisSeqNum = seqNum++;
        }
    }

    public Object[] function (final String type, Object[] args) throws EmacsException {
        final int mySeq;
        Integer mySeqI;
        Object hold;
        synchronized (this) {
            mySeq = getNextSeqNum ();
            if (DEBUG) {
                System.err.print("Calling Connection.function(" + type + "/" + mySeq);
                for (int i=0; i<args.length; i++) System.err.print("," + args[i]);
                System.err.println(")");
            }
            if (Thread.currentThread ().getName ().startsWith (THREAD_MAGIC))
                throw new IllegalStateException ("Attempt to call a function within dynamic scope of connection server!");
            hold = new Object () {
                       public String toString () { return "Connection-holding[" + type + "#" + mySeq + "]"; }
                   };
            mySeqI = new Integer (mySeq);
            //if (DEBUG) System.err.println ("holds.put for wait object " + hold);
            holds.put (mySeqI, hold);
            callWithSeq (type + '/' + mySeq, args, true);
        }
        Object result;
        int failcount = 0;
        while ((result = holds.get (mySeqI)) == hold) {
            if (failcount > 0) System.err.println("failed once...");
            if (failcount++ == 3) throw new EmacsException ("TIMEOUT (30 sec)!");
            try {
                synchronized (hold) {
                    hold.wait (10000L);
                }
            } catch (InterruptedException ign) {
                ign.printStackTrace ();
            }
        }
        //if (DEBUG) System.err.println ("holds.remove for whatever " + mySeqI);
        holds.remove (mySeqI);
        Object[] realResult = (Object[]) result;
        if (DEBUG) {
            System.err.print ("Returning from Connection.function #" + mySeq + " with [");
            for (int i=0; i<realResult.length; i++) {
                if (i > 0) System.err.print(",");
                System.err.print(realResult[i]);
            }
            System.err.println("]");
        }
        if (realResult.length > 0 && realResult[0] == ERROR_RETURN) {
            if (realResult.length == 2 && realResult[1] instanceof String)
                throw new EmacsException ((String) realResult[1]);
            else
                throw new IllegalArgumentException ("Bad format for error indication!");
        } else {
            return realResult;
        }
    }

    public synchronized void call (String type, Object[] args) throws EmacsException {
        callWithSeq (type + "!" + getNextSeqNum (), args, false);
    }

    // Please only call within a synch (this) block:
    private void callWithSeq (String type, Object[] args, boolean isFunction) throws EmacsException {
        // Check whether it is serial or not:
        boolean serial = false;
        String[] types = isFunction ? SERIAL_FUNCTIONS : SERIAL_COMMANDS;
        for (int i = 0; i < types.length; i++) {
            // XXX this is very ugly...should find these a nicer way
            if (type.indexOf (types[i]) != -1) {
                serial = true;
                break;
            }
        }
        if (DEBUG) {
            System.err.print("Connection.call(" + type);
            for (int i = 0; i < args.length; i++) System.err.print ("," + args[i]);
            System.err.println(",serial=" + serial + ")");
        }
        if (serial) serialSeqNum = thisSeqNum;
        try {
            wr.write (type);
            for (int i = 0; i < args.length; i++) {
                wr.write (' ');
                Object o = args[i];
                if (o instanceof Integer) {
                    if (((Integer) o).intValue () < 0)
                        throw new IllegalArgumentException ("Cannot send negative numbers: " + o);
                    wr.write (o.toString ());
                } else if (o instanceof String) {
                    String s = (String) o;
                    // XXX remove ref to Utilities class, unnecessary
                    s = Utilities.replaceString (s, "\\", "\\\\");
                    s = Utilities.replaceString (s, "\n", "\\n");
                    s = Utilities.replaceString (s, "\"", "\\\"");
                    s = Utilities.replaceString (s, "\r", "\\r");
                    wr.write ('"');
                    wr.write (s);
                    wr.write ('"');
                } else if (o instanceof Boolean) {
                    wr.write (((Boolean) o).booleanValue () ? 'T' : 'F');
                } else {
                    throw new IllegalArgumentException (o.toString ());
                }
            }
            wr.write ('\n');
            wr.flush ();
        } catch (IOException e) {
            e.printStackTrace ();
            throw new EmacsException ((Exception) e.fillInStackTrace ());
        }
    }

    public String toString () {
        return "Connection[" + info + "]@" + thisSeqNum;
    }

}
