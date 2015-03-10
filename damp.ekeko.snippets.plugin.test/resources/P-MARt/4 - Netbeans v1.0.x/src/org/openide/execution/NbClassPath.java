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

import java.io.File;
import java.util.StringTokenizer;
import java.util.LinkedList;
import java.util.Enumeration;

import org.openide.TopManager;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystemCapability;
import org.openide.filesystems.EnvironmentNotSupportedException;

/** Property that can hold informations about class path and
* that can be used to create string representation of the 
* class path.
*/
public final class NbClassPath extends Object implements java.io.Serializable {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -8458093409814321744L;

    /** Fuj: This is the most overloaded variable in this class.
    * It can hold Object[] with elements of String or Exception
    * or later Exception[] array.
    *
    * Also the array can hold File[] array.
    */
    private Object[] items;
    /** the prepared classpath */
    private String classpath;

    /** Create a new descriptor for the specified process, classpath switch, and classpath.
    * @param classpathItems  the classpath to be passed to the process 
    */
    public NbClassPath (String[] classpathItems) {
        this.items = classpathItems;
    }

    /** Create a new descriptor for the specified process, classpath switch, and classpath.
    * @param classpathItems  the classpath to be passed to the process 
    */
    public NbClassPath (File[] classpathItems) {
        this.items = classpathItems;
    }

    /** Private constructor
    * @param arr array of String and Exceptions
    */
    private NbClassPath (Object[] arr) {
        this.items = arr;
    }

    /** Constructor.
    * @param path separated by File.separatorChars
    */
    public NbClassPath (String path) {
        this.items = new Exception[0];
        this.classpath = path;
        if (path.indexOf(' ') >= 0) {
            if (path.startsWith("\"")) { // NOI18N
                return;
            } else {
                StringBuffer buff = new StringBuffer(path);
                buff.insert(0, '"');
                buff.append('"');
                classpath = buff.toString();
            }
        }
    }

    /** Method to obtain class path for the current state of the repository.
    * The classpath should be scanned for all occured exception caused
    * by file systems that cannot be converted to class path by a call to
    * method getExceptions().
    *
    *
    * @return class path for all reachable systems in the repository
    */
    public static NbClassPath createRepositoryPath () {
        return createRepositoryPath (FileSystemCapability.ALL);
    }

    /** Method to obtain class path for the current state of the repository.
    * The classpath should be scanned for all occured exception caused
    * by file systems that cannot be converted to class path by a call to
    * method getExceptions().
    *
    *
    * @param cap the capability that must be satisfied by the file system
    *    added to the class path
    * @return class path for all reachable systems in the repository
    */
    public static NbClassPath createRepositoryPath (FileSystemCapability cap) {
        final LinkedList res = new LinkedList ();


        final class Env extends FileSystem.Environment {
            /* method of interface Environment */
            public void addClassPath(String element) {
                res.add (element);
            }
        }


        Env env = new Env ();
        Enumeration en = cap.fileSystems ();
        while (en.hasMoreElements ()) {
            try {
                FileSystem fs = (FileSystem)en.nextElement ();
                fs.prepareEnvironment(env);
            } catch (EnvironmentNotSupportedException ex) {
                // store the exception
                res.add (ex);
            }
        }

        // return it
        return new NbClassPath (res.toArray ());
    }

    /** Creates class path describing additional libraries needed by the system.
    */
    public static NbClassPath createLibraryPath () {
        // modules & libs
        return TopManager.getDefault ().getExecutionEngine ().createLibraryPath ();
    }

    /** Creates class path of the system.
    */
    public static NbClassPath createClassPath () {
        return new NbClassPath (System.getProperty("java.class.path"));
    }

    /** Creates path describing boot class path of the system.
    * @return class path of system class including extensions
    */
    public static NbClassPath createBootClassPath () {
        // boot
        String boot = System.getProperty("sun.boot.class.path");
        StringBuffer sb = (boot != null ? new StringBuffer(boot) : new StringBuffer());
        if (boot != null) {
            sb.append(java.io.File.pathSeparatorChar);
        }

        // std extensions
        String extensions = System.getProperty("java.ext.dirs");
        if (extensions != null) {
            for (StringTokenizer st = new StringTokenizer(extensions, File.pathSeparator); st.hasMoreTokens();) {
                String dir = st.nextToken();
                File file = new File(dir);
                if (!dir.endsWith(File.separator)) dir += File.separator;
                if (file.isDirectory()) {
                    String[] files = file.list();
                    for (int i = 0; i < files.length; i++) {
                        String entry = files[i];
                        if (entry.endsWith(".jar")) // NOI18N
                            sb.append(java.io.File.pathSeparatorChar).append(dir).append(entry);
                    }
                }
            }
        }

        return new NbClassPath (sb.toString());
    }

    /** That takes one file object and tries to convert it into local file.
    * The conversion can succeed only if the file object's file system
    * supports work with FileSystem.Environment. 
    *
    * @param fo file object to convert
    * @return java.io.File for that file object
    */
    public static java.io.File toFile (FileObject fo) {
        final String pne = fo.getPackageNameExt(File.separatorChar, '.');

        final class Env extends FileSystem.Environment {
            /** the file found or null */
            public File found;
            /** the file suggested or null */
            public File suggest;

            /* method of interface Environment */
            public void addClassPath(String element) {
                if (found != null) {
                    // file found, ignore the rest
                    return;
                }

                String p = element;
                if (!p.endsWith (File.separator)) {
                    p = p + File.separatorChar + pne;
                } else {
                    p = p + pne;
                }
                File f = new File (p);

                if (suggest == null) {
                    suggest = f;
                }

                if (f.exists ()) {
                    found = f;
                }
            }
        }

        Env env = new Env ();
        try {
            fo.getFileSystem ().prepareEnvironment(env);
            return env.found == null ? env.suggest : env.found;
        } catch (java.io.IOException ex) {
            return null;
        }
    }

    /** If there were some problems during creation of the class path, they can be identified
    * by asking the method. So this method can be called to test whether it is correct to
    * use the path or there can be some errors.
    * <P>
    * This can happen especially when creating NbClassPath for filesystems in repository and
    * they are not stored on locally accessible disks.
    *
    * @return array of exceptions thrown during creation of the path
    */ 
    public Exception[] getExceptions () {
        try {
            return (Exception[])items;
        } catch (ClassCastException ex) {
            // we have to convert the array first
        }

        synchronized (this) {
            // creates class path
            getClassPath ();

            int first = 0;
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null) {
                    // should be exception
                    items[first++] = items[i];
                }
            }

            Exception[] list = new Exception[first];
            System.arraycopy (items, 0, list, 0, first);
            items = list;
            return list;
        }
    }



    /** Create class path representation.
    * @return string representing the classpath items separated by File.separatorChar.
    */
    public String getClassPath () {
        if (classpath != null) return classpath;
        synchronized (this) {
            if (classpath != null) return classpath;

            if (items.length == 0) {
                return classpath = ""; // NOI18N
            } else {
                // not so nice but at least one need not create
                // synthetic method
                final Class stringClass = "".getClass (); // NOI18N

                StringBuffer sb = new StringBuffer ();
                sb.append (items[0]);

                for (int i = 1; i < items.length; i++) {
                    Object o = items[i];
                    if (o == null || o.getClass () != stringClass) {
                        // we accept only strings
                        continue;
                    }

                    sb.append (File.pathSeparatorChar);
                    sb.append (o.toString ());
                    items[i] = null;
                }
                String clsPth;
                if ((clsPth = sb.toString()).indexOf(' ') >= 0) {
                    sb.insert(0, '"');
                    sb.append('"');
                    classpath = sb.toString();
                } else {
                    classpath = clsPth;
                }
                return classpath;
            }
        }
    }

    /* equals */
    public boolean equals(Object o) {
        if (! (o instanceof NbClassPath)) return false;
        NbClassPath him = (NbClassPath) o;
        return getClassPath ().equals (him.getClassPath ());
    }
}


/*
 * Log
 *  9    Gandalf   1.8         1/12/00  Ian Formanek    NOI18N
 *  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/1/99  Jaroslav Tulach FileObject.move & 
 *       FileObject.copy
 *  6    Gandalf   1.5         9/6/99   Jaroslav Tulach 
 *  5    Gandalf   1.4         8/10/99  Ales Novak      external execution + 
 *       spaces in file names work together
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/7/99   Jaroslav Tulach FS capabilities.
 *  2    Gandalf   1.1         6/4/99   Jaroslav Tulach 
 *  1    Gandalf   1.0         5/31/99  Jaroslav Tulach 
 * $
 */



