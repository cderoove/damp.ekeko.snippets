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

package org.netbeans.core.execution;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.security.Permission;
import java.util.*;
import java.lang.reflect.Field;

import org.openide.execution.NbClassLoader;

import ice.iblite.BrowserClassLoader;

/** See java.lang.SecurityManager for more details.
*
* @author Ales Novak
*/
public class TopSecurityManager extends SecurityManager {

    /** thread group of executed classes */
    private ThreadGroup base;

    /**
    * constructs new TopSecurityManager
    */
    public TopSecurityManager () {
        base = ExecutionEngine.base;
    }

    public void checkExit(int status) throws SecurityException {
        ThreadGroup g = Thread.currentThread().getThreadGroup ();
        ThreadGroup old = null;
        while (g != null && g != base) {
            old = g;
            g = g.getParent ();
        }

        IOPermissionCollection iopc;
        iopc = AccController.getIOPermissionCollection();

        if (g != null) { // g == base
            if (old instanceof TaskThreadGroup) {
                if (iopc != null) {
                    ExecutionEngine.getTaskIOs().free(old, iopc.getIO()); // closes output
                } // else loaded by App classloader
                ExecutionEngine.closeGroup(old);
                stopTaskThreadGroup((TaskThreadGroup) old);
            }
        } else {
            if ((iopc != null) &&
                    (iopc.grp != null)) {
                ExecutionEngine.getTaskIOs().free(iopc.grp, iopc.getIO()); // closes output
                ExecutionEngine.closeGroup(iopc.grp);
                stopTaskThreadGroup(iopc.grp);
                throw new ExitSecurityException();
            }

            if (isNbClassLoader()) {
                throw new SecurityException();
            }
        }
        super.checkExit(status);
    }

    public boolean checkTopLevelWindow(Object window) {
        IOPermissionCollection iopc = AccController.getIOPermissionCollection();
        if (iopc != null && iopc.grp != null && (window instanceof java.awt.Window)) {
            ExecutionEngine.putWindow((java.awt.Window) window, iopc.grp);
        }
        return super.checkTopLevelWindow(window);
    }

    /** Hack against permissions of Launcher$AppLoader. */
    public void checkPackageAccess(String pckg) {
        if (pckg == null) return;
        if (pckg.startsWith("sun.")) { // NOI18N
            if (inClazz("sun.misc.Launcher") || inClazz("java.lang.Class")) { // NOI18N
                return;
            }
        }
        super.checkPackageAccess(pckg);
    }

    /* ----------------- private methods ------------- */

    private boolean inClazz(String s) {
        Class[] classes = getClassContext();
        int i = 0;
        for (; (i < classes.length) && (classes[i] == TopSecurityManager.class); i++);
        if (i == classes.length) {
            return false;
        }
        return classes[i].getName().startsWith(s);
    }

    private void stopTaskThreadGroup(TaskThreadGroup old) {
        synchronized(old) {
            int count = old.activeCount();
            int icurrent = -1;
            Thread current = Thread.currentThread();
            Thread[] thrs = new Thread[count];
            old.enumerate(thrs, true);
            for (int i = 0; i < thrs.length; i++) {
                if (thrs[i] == null) break;
                if (thrs[i] == current) icurrent = i;
                else thrs[i].stop();
            }
            if (icurrent != -1) thrs[icurrent].stop();
            //throw new ExitSecurityException();
        }
    }
    public void checkRead(String name) {
        if (isForeignClassLoader()) {
            super.checkRead(name);
        }
    }

    public void checkRead(String file, Object context) {
        if (isForeignClassLoader()) {
            super.checkRead(file, context);
        }
    }

    public void checkWrite(String file) {
        if (isForeignClassLoader()) {
            super.checkWrite(file);
        }
    }

    public void checkDelete(String file) {
        if (isForeignClassLoader()) {
            super.checkDelete(file);
        }
    }

    public void checkRead(FileDescriptor fd) {
    }

    public void checkWrite(FileDescriptor fd) {
    }

    public synchronized void checkConnect(String host, int port) {
        ClassLoader cloader = getForeignClassLoader();
        if (cloader != null) {
            Object ctx = BrowserClassLoader.getSecurityContext(cloader);
            if (ctx instanceof URL) {
                try {
                    check = false;
                    String fromHost = ((URL) ctx).getHost();
                    InetAddress ia2 = InetAddress.getByName(host);
                    InetAddress ia3 = InetAddress.getByName(fromHost);
                    if (ia2.equals(ia3)) {
                        return;
                    }
                } catch (UnknownHostException e) { // ignore
                    if (Boolean.getBoolean("netbeans.debug.security")) { // NOI18N
                        e.printStackTrace();
                    }
                } finally {
                    check = true;
                }
            }
            super.checkConnect(host, port);
        }
    }

    public void checkConnect(String s, int port, Object context) {
        checkConnect(s, port);
    }

    public void checkPermission(Permission perm) {
        if (isForeignClassLoader ()) {
            super.checkPermission (perm);
        }
    }

    public void checkPermission(Permission perm, Object context) {
        if (isForeignClassLoader ()) {
            super.checkPermission (perm, context);
        }
    }

    public void checkMemberAccess(Class clazz, int which) {
        if (which == java.lang.reflect.Member.PUBLIC) {
            return;
        } else {
            super.checkMemberAccess(clazz, which);
        }
    }

    /** @return true iff an instance of the NbClassLoader class is on the stack
    */
    protected boolean isNbClassLoader() {
        Class[] ctx = getClassContext();
        ClassLoader cloader;

        for (int i = ctx.length; --i >= 0; ) {
            if (ctx[i].getClassLoader() instanceof NbClassLoader) {
                return true;
            }
        }
        return false;
    }

    protected synchronized boolean isForeignClassLoader() {
        return (getForeignClassLoader() != null);
    }

    private ClassLoader getForeignClassLoader() {
        if (! check) {
            return null;
        }

        try {
            check = false;

            Class[] secureArray = getSecure ();
            int secureArrayLength = secureArray.length;

            Class[] ctx = getClassContext();
            ClassLoader cloader;

LOOP: for (int i = 0; i < ctx.length; i++) {

                if (ClassLoader.class.isAssignableFrom(ctx[i])) {
                    //System.out.println("TRYING A CLASSLOADER: " + ctx[i]); // NOI18N
                }

                if (ctx[i] == getAccessControllerClass()) {
                    // privileged action is on the stack before an untrusted class loader
                    // #3950
                    return null;
                } else if ((cloader = ctx[i].getClassLoader()) != null) {
                    final Class hisClass = cloader.getClass();

                    for (int j = 0; j < secureArrayLength; j++) {
                        if (secureArray[j] == hisClass) {
                            if (ClassLoader.class.isAssignableFrom(ctx[i])) {  // foreign classloader does work
                                return null;
                            } else {
                                continue LOOP;
                            }
                        }
                    }

                    if (isSecureClass(ctx[i])) {
                        if (ClassLoader.class.isAssignableFrom(ctx[i])) {
                            return null;
                        } else {
                            continue LOOP;
                        }
                    }

                    // is foreign
                    if (System.getProperty ("netbeans.debug.security") != null) {
                        System.err.println("Not secure class loader: " + hisClass);
                    }

                    return cloader;
                } else if (ClassLoader.class.isAssignableFrom(ctx[i])) { // cloader == null
                    return null; // foreign classloader wants to do work...
                }
            }
        } finally {
            check = true;
        }

        return null;
    }

    /** Checks if the class is loaded through the nbfs URL */
    static boolean isSecureClass(final Class clazz) {
        java.security.PrivilegedAction run = new java.security.PrivilegedAction() {
                                                 public Object run() {
                                                     URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
                                                     if (url != null) {
                                                         return isSecureProtocol(url.getProtocol());
                                                     } else {
                                                         // [PENDING] remove this else if iceblit is not used
                                                         String s = getClassURLProtocol(clazz);
                                                         if (s != null) {
                                                             return isSecureProtocol(s);
                                                         } else {
                                                             return Boolean.FALSE; // none CodeSource -> be conservative
                                                         }
                                                     }
                                                 }
                                             };
        return ((Boolean) java.security.AccessController.doPrivileged(run)).booleanValue();
    }

    /** @return a protocol through which was the class loaded (file://...) or null
    * [PENDING] remove this method if iceblit is not used
    */
    static String getClassURLProtocol(Class clazz) {
        if (clazz.getClassLoader().getClass().
                getName().indexOf("ice.iblite.BrowserClassLoader") >= 0) { // NOI18N
            try {
                Field fld = getUrlField(clazz.getClassLoader().getClass());
                if (fld == null) {
                    return null;
                } else {
                    URL url = (URL) fld.get(clazz.getClassLoader());
                    if (url != null) {
                        return url.getProtocol();
                    }
                }
            } catch (Exception e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    static Field getUrlField(Class clazz) {
        if (urlField == null) {
            try {
                Field[] fds = clazz.getDeclaredFields();
                for (int i = 0; i < fds.length; i++) {
                    if (fds[i].getType() == java.net.URL.class) {
                        fds[i].setAccessible(true);
                        urlField = fds[i];
                    }
                }
            } catch (Exception e) {
                if (Boolean.getBoolean("netbeans.debug.security")) { // NOI18N
                    e.printStackTrace();
                }
            }
        }
        return urlField;
    }

    private static Field urlField;

    /** @return Boolean.TRUE iff the string is a safe protocol (file, nbfs, ...) */
    static Boolean isSecureProtocol(String protocol) {
        if (protocol.equals("http") || // NOI18N
                protocol.equals("rmi")) { // NOI18N
            return Boolean.FALSE;
        }
        if (protocol.equals("nbfs") || // NOI18N
                protocol.equals("file") || // NOI18N
                protocol.equals("systemresource") || // NOI18N
                protocol.equals("jar:file")) { // NOI18N
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    private static Class[] secureArray;
    private static Class accessControllerClass;

    /** Array of secure class loaders.
    */
    private static Class[] getSecure () {
        if (secureArray != null) {
            return secureArray;
        }

        if (secureArray != null) {
            return secureArray;
        }

        secureArray = new Class[] {
                          forName ("sun.misc.Launcher$AppClassLoader"), // NOI18N
                          forName ("sun.misc.Launcher$ExtClassLoader"), // NOI18N
                          forName ("org.netbeans.core.ModuleClassLoader"), // NOI18N
                          forName ("org.netbeans.core.ClassLoaderSupport"), // NOI18N
                          org.openide.execution.NbClassLoader.class,
                          forName ("sun.rmi.server.LoaderHandler$Loader"), // NOI18N
                          forName("COM.jbms._104._735") // NOI18N
                      };

        return secureArray;
    }

    /** @return java.security.AccessController.class */
    static Class getAccessControllerClass() {
        if (accessControllerClass == null) {
            accessControllerClass = forName("java.security.AccessController"); // NOI18N
        }
        return accessControllerClass;
    }

    private static Class forName (String fn) {
        try {
            return Class.forName (fn);
        } catch (ClassNotFoundException ex) {
            if (Boolean.getBoolean("netbeans.debug.security")) { // NOI18N
                ex.printStackTrace();
            }
            return null;
        }
    }

    private static boolean check;
    static {
        if (Boolean.getBoolean("netbeans.security.nocheck")) { // NOI18N
            check = false;
        } else {
            check = true;
        }
    }


}

/*
 * Log
 *  27   src-jtulach1.26        1/20/00  Petr Hamernik   rolled back
 *  26   src-jtulach1.25        1/19/00  Petr Nejedly    Commented out debug 
 *       messages
 *  25   src-jtulach1.24        1/13/00  Jaroslav Tulach I18N
 *  24   src-jtulach1.23        1/12/00  Ales Novak      i18n
 *  23   src-jtulach1.22        12/20/99 Ales Novak      checkRead/Write/Delete 
 *       is back - FilePermissions are skipped
 *  22   src-jtulach1.21        12/14/99 Ales Novak      simplified security
 *  21   src-jtulach1.20        11/5/99  Ales Novak      secure protocols
 *  20   src-jtulach1.19        11/1/99  Ales Novak      cloudscape classloader 
 *       added into a list of trusted classloaders
 *  19   src-jtulach1.18        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  18   src-jtulach1.17        10/8/99  Ales Novak      security checks
 *  17   src-jtulach1.16        10/6/99  Ales Novak      dumpStack removed
 *  16   src-jtulach1.15        10/5/99  Ales Novak      #4166
 *  15   src-jtulach1.14        10/1/99  Martin Ryzl     "RMIClassLoader" added 
 *       to secure classloaders
 *  14   src-jtulach1.13        9/10/99  Jaroslav Tulach Does not use any weak 
 *       maps.
 *  13   src-jtulach1.12        9/2/99   Jaroslav Tulach ClassLoaderSupport is 
 *       trusted too.
 *  12   src-jtulach1.11        9/1/99   Jaroslav Tulach Treats jars in extension
 *       directory as trusted.
 *  11   src-jtulach1.10        8/31/99  Jaroslav Tulach Works not only for 
 *       Launcher$AppLoader, but also for Launcher$ExtLoader
 *  10   src-jtulach1.9         8/31/99  Jaroslav Tulach checkPermisson only for 
 *       foreign loaders
 *  9    src-jtulach1.8         8/30/99  Ales Novak      applet security
 *  8    src-jtulach1.7         8/6/99   Ales Novak      NullPointerExc
 *  7    src-jtulach1.6         7/28/99  Ales Novak      new window system/#1409
 *  6    src-jtulach1.5         7/28/99  Ales Novak      bugfix #1409
 *  5    src-jtulach1.4         7/9/99   Ales Novak      #2413 fix
 *  4    src-jtulach1.3         6/3/99   Ales Novak      security checks disabled
 *  3    src-jtulach1.2         4/10/99  Ales Novak      
 *  2    src-jtulach1.1         4/8/99   Ales Novak      
 *  1    src-jtulach1.0         3/31/99  Ales Novak      
 * $
 */
