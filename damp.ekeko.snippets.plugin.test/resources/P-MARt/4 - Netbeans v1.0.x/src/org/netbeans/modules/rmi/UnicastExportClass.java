/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi;

import java.net.*;
import java.lang.reflect.*;
import java.util.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;

import org.openide.util.NbBundle;

/** Class for execution of RemoteObjects. Execution is done using main method.
* Arguments of main method are:
* -port port_for_rmi_export 
* -service url_for_registering
* class_to_be_exported
*
* At first, the instance for given class is created. Then it is exported
* using UnicastRemoteObject.exportObject and finally it is registered in
* RMI registry.
* 
* @author Martin Ryzl
*/
public class UnicastExportClass extends Object {

    /** Name of command line parameter for port.*/
    public static final String TAG_PORT = "port"; // NOI18N
    /** Name of command line parameter for service.*/
    public static final String TAG_SERVICE = "service"; // NOI18N
    /** Name of command line parameter for verbose mode.*/
    public static final String TAG_VERBOSE = "verbose"; // NOI18N
    /** Name of command line parameter for wait mode.*/
    public static final String TAG_WAIT = "wait"; // NOI18N

    /** Resource bundle. */
    private static ResourceBundle bundle = NbBundle.getBundle(UnicastExportClass.class);

    /** Verbose flag. */
    private static boolean verbose = false;

    /**
    */
    public UnicastExportClass() {
    }

    public static void main(String[] args) throws Throwable {

        System.setSecurityManager(new RMISecurityManager());

        ParamParse pp = new ParamParse(args, new String[] { TAG_PORT, TAG_SERVICE, TAG_WAIT });
        String[] args2 = pp.getRest();
        String classname = args2[0];
        Map map =  pp.getParams();

        String service, sport, time;
        int port = 0;
        long time2wait = 0;

        if ((sport = (String)map.get(TAG_PORT)) != null) port = Integer.parseInt(sport);
        if ((service = (String)map.get(TAG_SERVICE)) == null) service = classname;
        if ((time = (String)map.get(TAG_WAIT)) != null) time2wait = Long.parseLong(time);
        if (map.containsKey(TAG_VERBOSE)) verbose = true;

        try {
        } catch (Exception ex) {
            usage();
            System.exit(1);
        }

        Class clazz = Class.forName(classname);
        Remote remote = export(clazz, port);
        if (remote != null) {
            System.out.println(bundle.getString("MSG_SuccessfullyExported")); // NOI18N
            // wait
            try {
                Thread.sleep(time2wait);
            } catch (InterruptedException ex) {
                // ignore
            }

            try {
                register(1099, service, remote);
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
            System.out.println(bundle.getString("MSG_SuccessfullyRegistered")); // NOI18N
            // drop the reference
            remote = null;
        } else {
            System.out.println(bundle.getString("MSG_ExportFailed")); // NOI18N
            System.exit(1);
        }
    }

    /** Export given class on the port.
    * @param clazz - class to create instance and export.
    * @param port - port
    * @return a remote reference
    */
    public static Remote export(Class clazz, int port) {
        Constructor cn;
        UnicastRemoteObject uro;
        boolean noConstructor = false;

        // UnicastRemoteObject with (int) constructor
        try {
            if (java.rmi.server.UnicastRemoteObject.class.isAssignableFrom(clazz)) {
                cn = clazz.getConstructor(new Class[] {int.class});
                uro = (UnicastRemoteObject) cn.newInstance( new Object[] { new Integer(port) } );
                return uro;
            }
        } catch (NoSuchMethodException ex) {
            noConstructor = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // UnicastRemoteObject with () constructor
        try {
            if (java.rmi.server.UnicastRemoteObject.class.isAssignableFrom(clazz)) {
                cn = clazz.getConstructor(new Class[] {});
                uro = (UnicastRemoteObject) cn.newInstance( new Object[] { } );
                return uro;
            }
        } catch (NoSuchMethodException ex) {
            noConstructor = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (java.rmi.Remote.class.isAssignableFrom(clazz)) {
                cn = clazz.getConstructor(new Class[] {});
                Remote remote = (Remote) cn.newInstance( new Object[] { } );
                Remote obj = UnicastRemoteObject.exportObject(remote, port);
                return remote;
            }
        } catch (NoSuchMethodException ex) {
            noConstructor = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.err.println(bundle.getString("MSG_Unicast_Export_Hint"));
        return null;
    }

    /** Register given remote object to the registry.
    * @param rport - port of the registry
    * @param service - name of the service
    * @param remote - remote reference
    */
    public static void register(int rport, String service, Remote remote) throws RemoteException, MalformedURLException {
        Naming.rebind(service, remote);
    }

    /** Print usage.
    */
    public static void grUsage() {
        String message = NbBundle.getBundle(UnicastExportClass.class).getString("MSG_Unicast_Export_Hint"); // NOI18N
        System.err.println(message);
    }

    /** Test whether the given method is main.
    * @param m a method
    */
    public static boolean isMain(Method m) {
        // must be public, static and return type must be void
        int mod = m.getModifiers();
        return (m.getReturnType().equals(void.class) && Modifier.isStatic(mod) && Modifier.isPublic(mod));
    }

    /**
    */
    public static void usage() {
        String message = NbBundle.getBundle(UnicastExportClass.class).getString("MSG_UnicastExportUsage"); // NOI18N
        System.err.println(message);
    }

    /**
    */
    public static String arrayString(Object[] obj) {
        StringBuffer sb = new StringBuffer(1024);
        if (obj.length > 0) sb.append(obj[0].toString());
        for(int i = 1; i < obj.length; i++) {
            sb.append(";"); // NOI18N
            sb.append(obj[i].toString());
        }
        return sb.toString();
    }

    // -- Inner classes. --

    /** Parser of the command line.
    * For given command line and set of switches, it parses cmdline.
    * Format of cmdline is     -switch1 value -switch2 -switch3 value.
    * Constructor parameters are cmdline, and switches for which a value should be
    * obtained.
    * After the cmdline were succesfully processed, it is possible to obtain 
    * the hashtable with values and the rest of cmdline.
    */
    static class ParamParse {

        Set one;

        /**
         * @associates String 
         */
        HashMap params;
        String[] args;
        int last;

        /**  Constructor.
        * @param args - arguments for parsing
        * @param oneval - swithces that are supposed to have one parameter
        */
        public ParamParse(String[] args, String[] oneval) throws IllegalArgumentException  {
            this.args = args;
            one = createSet(oneval);
            params = new HashMap();
            process();
        }

        /** Create a set for given array.
        * @param values - array
        * @return a set
        */
        protected Set createSet(String[] values) {
            Set set = new HashSet();
            if (values != null) {
                for(int i = 0; i < values.length; i++) {
                    set.add(values[i]);
                }
            }
            return set;
        }

        /** Proceed parsing.
        */
        protected void process() throws IllegalArgumentException {
            String token, sw, value;

            try {
                for(int i = 0; i < args.length; i++) {
                    token = args[i];
                    if ((sw = getSwitchName(token)) != null) {
                        if (one.contains(sw)) {
                            value = args[++i];
                            if (getSwitchName(value) != null) throw new IllegalArgumentException();
                        }
                        else {
                            value = null;
                        }
                        params.put(sw, value);
                    } else {
                        last = i;
                        break;
                    }
                }
            } catch (IndexOutOfBoundsException ex) {
                throw new IllegalArgumentException();
            }
        }

        /** Get recognized parameters.
        * @return map of the parameters
        */
        public Map getParams() {
            return params;
        }

        /** Get rest of the command line. Parsing is stopped when first non-switch is detected.
        * @return rest
        */
        public String[] getRest() {
            String[] rest = new String[args.length - last];
            System.arraycopy(args, last, rest, 0, rest.length);
            return rest;
        }

        /** Return switch name.
        * @return name of the switch or null
        */
        public static String getSwitchName(String sw) {
            if (sw.charAt(0) == '-') return sw.substring(1, sw.length());
            return null;
        }
    }
}

/*
* <<Log>>
*  10   Gandalf-post-FCS1.7.1.1     4/20/00  Martin Ryzl     fix of #4387, #4514, 
*       #4521, #4598, #4395
*  9    Gandalf-post-FCS1.7.1.0     3/20/00  Martin Ryzl     localization
*  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    Gandalf   1.6         8/27/99  Martin Ryzl     
*  6    Gandalf   1.5         8/18/99  Martin Ryzl     corrected localization
*  5    Gandalf   1.4         8/16/99  Martin Ryzl     method filter in RMI 
*       Encapsulation Wizard  service URL in RMIDataObject
*  4    Gandalf   1.3         8/12/99  Martin Ryzl     hints on executors and 
*       compiler, debug executors
*  3    Gandalf   1.2         7/14/99  Martin Ryzl     
*  2    Gandalf   1.1         7/13/99  Martin Ryzl     first working version of 
*       UnicastExportClass
*  1    Gandalf   1.0         7/12/99  Martin Ryzl     
* $
*/