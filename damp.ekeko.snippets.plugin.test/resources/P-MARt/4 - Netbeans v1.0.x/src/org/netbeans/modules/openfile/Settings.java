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

import java.io.IOException;
import java.io.ObjectInput;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

/** Settings for the Open File utility.
* @author Jesse Glick
*/
public class Settings extends SystemOption {

    public static final String PROP_RUNNING = "running"; // NOI18N
    public static final String PROP_PORT = "port"; // NOI18N
    public static final String PROP_ACCESS = "access"; // NOI18N
    public static final int ACCESS_LOCAL = 0;
    public static final int ACCESS_ANY = 1;
    public static final String PROP_ACTUAL_RUNNING = "actualRunning"; // NOI18N
    public static final String PROP_ACTUAL_PORT = "actualPort"; // NOI18N

    private static boolean running, actualRunning;
    private static int port, actualPort;

    // Weird nonsense half-copied from HttpServerSettings.
    private static boolean inited, pendingRunning;

    // default settings object, avoids calling the constructor repeatedly
    public static final Settings DEFAULT = (Settings) findObject (Settings.class, true);

    private static final long serialVersionUID = 7655861665922160177L;

    private boolean isGlobal () {
        return true;
    }

    public String displayName () {
        return SettingsBeanInfo.getString ("LBL_openFileSettings");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (Settings.class);
    }

    protected void initialize () {
        //System.err.println("Settings.initialize");
        // Not what it looks like:
        running = false;
        inited = false;
        port = Main.DEFAULT_PORT;
        putProperty (PROP_ACCESS, new Integer (ACCESS_LOCAL), false);
        actualRunning = false;
        actualPort = 0;
    }

    // The nasty part:
    public boolean isRunning () {
        synchronized (DEFAULT) {
            //System.err.println("Settings.isRunning: inited=" + inited + " running=" + running + " writeExternal=" + isWriteExternal ());
            if (! inited) {
                //System.err.println("Settings.isRunning: will automatically call setRunning(true)");
                inited = true;
                // Only defaults to running on Windoze:
                setRunning (Utilities.isWindows ());
            }
            return running;
        }
    }
    public void setRunning (boolean r) {
        synchronized (DEFAULT) {
            if (isReadExternal ()) {
                //System.err.println("Settings.setRunning (readExternal): r=" + r);
                pendingRunning = r;
            } else {
                //System.err.println("Settings.setRunning: " + running + " -> " + r);
                boolean old = running;
                running = r;
                firePropertyChange (PROP_RUNNING, new Boolean (old), new Boolean (r));
                if (old && ! r)
                    Server.shutdown ();
                else if (! old && r)
                    Server.startup ();
            }
        }
    }
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        //System.err.println("Settings.readExternal");
        super.readExternal (in);
        inited = true;
        //System.err.println("Settings.readExternal: pendingRunning=" + pendingRunning);
        setRunning (pendingRunning);
    }

    public int getPort () {
        return port;
    }
    public void setPort (int p) {
        synchronized (DEFAULT) {
            //System.err.println("Settings.port: " + port + " -> " + p);
            boolean restart = (p != port && isActualRunning ());
            if (restart) Server.shutdown ();
            int old = port;
            port = p;
            firePropertyChange (PROP_PORT, new Integer (old), new Integer (p));
            if (restart && running) Server.startup ();
        }
    }

    public int getAccess () {
        return ((Integer) getProperty (PROP_ACCESS)).intValue ();
    }
    public void setAccess (int a) {
        putProperty (PROP_ACCESS, new Integer (a), true);
    }

    public boolean isActualRunning () {
        return actualRunning;
    }
    void setActualRunning0 (boolean r) {
        synchronized (DEFAULT) {
            //System.err.println("Settings.actualRunning: " + actualRunning + " -> " + r);
            boolean old = actualRunning;
            actualRunning = r;
            firePropertyChange (PROP_ACTUAL_RUNNING, new Boolean (old), new Boolean (r));
        }
    }

    public int getActualPort () {
        return actualPort;
    }
    void setActualPort0 (int p) {
        synchronized (DEFAULT) {
            //System.err.println("Settings.actualPort: " + actualPort + " -> " + p);
            int old = actualPort;
            actualPort = p;
            firePropertyChange (PROP_ACTUAL_PORT, new Integer (old), new Integer (p));
        }
    }

}

/*
 * Log
 *  15   Gandalf-post-FCS1.13.1.0    2/28/00  Jesse Glick     Server only runs by 
 *       default on Windows.
 *  14   Gandalf   1.13        1/12/00  Jesse Glick     I18N.
 *  13   Gandalf   1.12        1/10/00  Jesse Glick     Server was off by 
 *       default in new projects unless module installed().
 *  12   Gandalf   1.11        1/7/00   Jesse Glick     Fixed option storage to 
 *       not use putProperty for things that require setters to be called.
 *  11   Gandalf   1.10        1/4/00   Jesse Glick     Explicitly requesting to
 *       be global (if anyone is listening).
 *  10   Gandalf   1.9         1/4/00   Jesse Glick     Accessibility for 
 *       installation from Utils module.
 *  9    Gandalf   1.8         11/2/99  Jesse Glick     Overhauled socket 
 *       handling.
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  7    Gandalf   1.6         9/10/99  Jesse Glick     #3647 - 
 *       SocketException's on Linux + native threads.
 *  6    Gandalf   1.5         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  5    Gandalf   1.4         7/10/99  Jesse Glick     Open File module moved 
 *       to core.
 *  4    Gandalf   1.3         7/10/99  Jesse Glick     Sundry clean-ups (mostly
 *       bundle usage).
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/22/99  Jesse Glick     Licenses.
 *  1    Gandalf   1.0         5/22/99  Jesse Glick     
 * $
 */
