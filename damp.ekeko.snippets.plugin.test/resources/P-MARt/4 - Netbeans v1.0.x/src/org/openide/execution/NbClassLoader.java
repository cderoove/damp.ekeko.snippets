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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Collections;
import java.security.PermissionCollection;
import java.security.CodeSource;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.openide.TopManager;
import org.openide.windows.InputOutput;

// ClassLoader constructs URL (NbfsURL) in this way:
// "protocol"://"fs_name"#"package"/"name.extension" // NOI18N
/** A class loader which is capable of loading classes from the Repository.
* Classes loaded from file systems in the repository are handled by {@link NbfsStreamHandlerFactory}.
*
* @author Ales Novak, Petr Hamernik, Jaroslav Tulach, Ian Formanek
*/
public class NbClassLoader extends URLClassLoader {

    /** I/O for classes defined by this classloader. May be <code>null</code>. */
    protected InputOutput inout;
    /** Cached PermissionCollections returned from ExecutionEngine. 
     * @associates PermissionCollection*/
    private HashMap permissionCollections;

    /** Create a new class loader retrieving classes from the core IDE as well as the Repository.
     * @see FileSystemCapability.EXECUTE.fileSystems
     * @see TopManager#systemClassLoader
    */
    public NbClassLoader () {
        super (
            createRootURLs (FileSystemCapability.EXECUTE.fileSystems ()),
            TopManager.getDefault ().systemClassLoader ()
        );
    }

    /** Create a new class loader retrieving classes from the core IDE as well as the Repository,
    * and redirecting system I/O.
     * @param io an I/O tab in the Output Window
     * @see org.openide.filesystems.Repository#getFileSystems
     * @see TopManager#systemClassLoader
     */
    public NbClassLoader(InputOutput io) {
        this();
        inout = io;
    }

    /** Create a new class loader retrieving classes from the core IDE as well as specified file systems.
     * @param fileSystems file systems to load classes from
     * @see TopManager#systemClassLoader
    */
    public NbClassLoader (FileSystem[] fileSystems) {
        this(fileSystems, TopManager.getDefault ().systemClassLoader ());
    }

    /** Create a new class loader.
     * @param fileSystems file systems to load classes from
     * @param parent fallback class loader
    */
    public NbClassLoader (FileSystem[] fileSystems, ClassLoader parent) {
        super (
            createRootURLs (Collections.enumeration (Arrays.asList (fileSystems))),
            parent
        );
    }

    /** Create a URL to a resource specified by name.
    * Same behavior as in the super method, but handles names beginning with a slash.
    * @param name resource name
    * @return URL to that resource or <code>null</code>
    */
    public URL getResource (String name) {
        return super.getResource (name.startsWith ("/") ? name.substring (1) : name); // NOI18N
    }

    /* @return a PermissionCollection for given CodeSource. */
    protected final synchronized PermissionCollection getPermissions(CodeSource cs) {

        if (permissionCollections != null) {
            PermissionCollection pc = (PermissionCollection) permissionCollections.get(cs);
            if (pc != null) {
                return pc;
            }
        }

        return (inout == null ? super.getPermissions(cs) : createPermissions(cs, inout));
    }

    /**
    * @param cs CodeSource
    * @param inout InputOutput passed to @seeExecutionEngine#createPermissions(java.security.CodeSource, org.openide.windows.InpuOutput).
    * @return a PermissionCollection for given CodeSource.
    */
    private PermissionCollection createPermissions(CodeSource cs, InputOutput inout) {
        ExecutionEngine engine = TopManager.getDefault().getExecutionEngine();
        PermissionCollection pc = engine.createPermissions(cs, inout);
        pc.add(new java.security.AllPermission());
        if (permissionCollections == null) {
            permissionCollections = new HashMap(7);
        }
        permissionCollections.put(cs, pc);
        return pc;
    }


    /** Creates urls for filesystems.
    * @param enumeration of FileSystems
    * @return array of urls
    */
    private static URL[] createRootURLs (Enumeration en) {
        ArrayList list = new ArrayList ();
        while (en.hasMoreElements ()) {
            FileSystem fs = (FileSystem)en.nextElement ();
            list.add (NbfsURLConnection.encodeFileObject (fs, fs.getRoot ()));
        }
        return (URL[])list.toArray (new URL[0]);
    }
}


/*
 * Log
 *  14   src-jtulach1.13        1/13/00  Ian Formanek    NOI18N
 *  13   src-jtulach1.12        1/12/00  Ian Formanek    NOI18N
 *  12   src-jtulach1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   src-jtulach1.10        10/1/99  Ales Novak      major change of 
 *       execution
 *  10   src-jtulach1.9         6/15/99  Ales Novak      removed cached instance 
 *       of exec. eng.
 *  9    src-jtulach1.8         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    src-jtulach1.7         6/7/99   Jaroslav Tulach FS capabilities.
 *  7    src-jtulach1.6         5/15/99  Jesse Glick     [JavaDoc]
 *  6    src-jtulach1.5         5/14/99  Jaroslav Tulach Pallete works again.
 *  5    src-jtulach1.4         4/8/99   Ales Novak      
 *  4    src-jtulach1.3         4/8/99   Jesse Glick     [JavaDoc]
 *  3    src-jtulach1.2         3/31/99  Jesse Glick     [JavaDoc]
 *  2    src-jtulach1.1         3/31/99  Ales Novak      
 *  1    src-jtulach1.0         3/26/99  Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jaroslav Tulach New constructor added, cache made instance variable not static,
 *  0    Tuborg    0.12        --/--/98 Jaroslav Tulach added getResourceAsStream method
 *  0    Tuborg    0.13        --/--/98 Ales Novak      All constructors removed, Pool removed.
 *  0    Tuborg    0.14        --/--/98 Jan Jancura     Bugfix.
 *  0    Tuborg    0.15        --/--/98 Petr Hamernik   constructors changed.
 *  0    Tuborg    0.16        --/--/98 Petr Hamernik   parent class loader added
 *  0    Tuborg    0.17        --/--/98 Ales Novak      com.sun classes found in classpath
 */
