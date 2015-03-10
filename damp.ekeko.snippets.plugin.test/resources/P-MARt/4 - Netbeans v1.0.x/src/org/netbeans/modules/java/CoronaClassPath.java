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

package org.netbeans.modules.java;

import java.io.File;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Collections;
import java.util.HashMap;
import java.util.WeakHashMap;
import sun.tools.java.ClassFile;
import sun.tools.java.ClassPath;

import org.openide.TopManager;
import org.openide.execution.NbClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemCapability;

/** Bridges CLASSPATH and Repository for javac.
*
* @author Ales Novak
*/
public class CoronaClassPath extends ClassPath {
    /* * keeps .class ClassFiles from java/sun.com/....*/
    //private static ClassPath superPath = new ClassPath(getSystemEntriesFromClassPath());
    /** corona class environment this class path belongs to */
    private CoronaEnvironment env;

    /** Pool for asking files */
    private FileSystemCapability pool;

    /** cache for already found files; format is String:CoronaClassFile
    * or String:nullFile
    */
    private static WeakHashMap cache = new WeakHashMap(113);

    /**represents null in the cache */
    private static final Object nullFile = new Object();

    /** string with path separator */
    private static final String pathStr = new String(new char[] {File.pathSeparatorChar});

    /** empty enumeration */
    private static final Enumeration emptyEnum = org.openide.util.enum.EmptyEnumeration.EMPTY;

    private CoronaClassPath srcPath;

    /* * system paths * /
    private static final String[] systemEntries = {
      "classes.zip", "swingall.jar", "HotJavaBean.jar", "TextBean.jar"
};                  */

    /**
    * @param binary is it the classpath for binary files?
    * @param p is a Repository that is used instead of CLASSPATH variable
    */
    public CoronaClassPath (boolean binary, CoronaClassPath src) {
        super(binary ? getSystemEntriesFromClassPath() : ""); // NOI18N
        pool = FileSystemCapability.COMPILE;
        cache.clear();
        this.srcPath = src;
    }

    /**
    * @param binary is it the classpath for binary files?
    * @param p is a Repository that is used instead of CLASSPATH variable
    */
    public CoronaClassPath (boolean binary) {
        this (binary, null);
    }


    /** Attateches to corona env.
    */
    public void attachToEnvironment (CoronaEnvironment env) {
        this.env = env;
    }

    /**
    * @param dirName is a name of searched directory
    * @return requested dir or null
    */
    public ClassFile getDirectory(String dirName) {
        ClassFile c;
        c = getFile(dirName, true);
        return c;
    }

    /**
    * @param fileName is a name of searched directory
    * @return requested dir or null
    */
    public ClassFile getFile(String fileName) {
        ClassFile c;
        if ((srcPath != null) &&
                fileName.endsWith(".class")) { // NOI18N
            StringBuffer fname = new StringBuffer(fileName);
            fname.setLength(fileName.length() - 5);
            int iidex;
            if ((iidex = fname.toString().indexOf('$')) >= 0) {
                fname.setLength(iidex);
                fname.append('.');
            }
            fname.append("java"); // NOI18N
            c = srcPath.getFile(fname.toString());
            if (c != null) {
                return srcPath.getFile(fileName);
            }
        }
        c = getFile(fileName, false);
        return c;
    }

    /**
    * @param path - names of dirs or files are divided by File.separatorChar.
    * @param directory says whether we should search for dir.
    * @return requested ClassFile if it exists null otherwise.
    *
    * Using File.separatorChar and null on return is given by Sun's standard.
    */
    private ClassFile getFile(String path, boolean directory) {

        FileObject fo = null;
        CoronaClassFile classFile;
        Object o;
        o = cache.get(path);
        if (o != null) {
            if (o != nullFile) return (ClassFile) o;
            else return null;
        } /*else if (path.endsWith(".class")) {
          ClassFile cfile = null;
          if (path.startsWith("java") ||
            path.startsWith("sun") ||
            path.startsWith(comsun))  {
              cfile  = directory ? super.getDirectory(path) : super.getFile(path);
          }
          if (cfile != null) {
            cache.put(path, cfile);
            return cfile;
          }
    }   */
        String name = null, pack = null, ext = null;

        if (path == null) {
            return null;
        }

        if (File.separatorChar != '/')  {
            pack = path.replace(File.separatorChar, '/');
        } else {
            pack = path;
        }

        if (directory) {
            //first cut off last token in pack and set it as a name
            fo = pool.findResource(pack);

            if (fo == null)  {
                // get netbeans classes from sys classpath
                //if (path.startsWith(comnetb)) {
                ClassFile cfile = super.getDirectory(path);
                if (cfile != null) {
                    cache.put(path, cfile);
                    return cfile;
                }
                //}
                cache.put(path, nullFile);
                return null;
            }
        } else {

            fo = pool.findResource(pack); //pack, name, ext);
            if (fo == null) {
                // get netbeans classes from sys classpath
                //if (path.startsWith(comnetb)) {
                ClassFile cfile = super.getFile(path);
                if (cfile != null) {
                    cache.put(path, cfile);
                    return cfile;
                }
                //}
                cache.put(path, nullFile);
                return null;
            }
            if (! pack.equals(fo.getPackageNameExt('/', '.'))) {
                cache.put(path, nullFile);
                return null;
            }
        }
        classFile = env.getClassFile(fo);
        cache.put(path, classFile);
        return classFile;
    }

    /**
    * @param aPackage is a package we want to search in
    * @param anExt gives the extension we search for
    * @return an enumeration of all files in the given package with the given extension.
    */
    public Enumeration getFiles(String aPackage, String anExt) {


        String name = null;
        ClassFile classFile;
        HashMap files = new HashMap();

        Enumeration sup = super.getFiles(aPackage, anExt);

        while (sup.hasMoreElements()) {
            classFile = (ClassFile) sup.nextElement();
            name = classFile.getName();
            files.put(name, classFile);
        }

        //first cut off last token in pack and set it as a name
        String pack = aPackage.replace(File.separatorChar, '.');

        while (pack.endsWith(".")) pack = pack.substring(0, pack.length() - 1); // NOI18N
        while (anExt.startsWith(".")) anExt = anExt.substring(1, anExt.length()); // NOI18N

        Enumeration fss = pool.fileSystems();

        while (fss.hasMoreElements()) {
            FileSystem fsys = (FileSystem) fss.nextElement();
            FileObject dir = fsys.findResource(pack);

            //enumerate
            if (dir != null && dir.isFolder()) {
                FileObject[] list = dir.getChildren();

                for (int j = 0; j < list.length; j++) {
                    if (list[j].getExt().compareTo(anExt) == 0) {
                        classFile = env.getClassFile(list[j]);
                        files.put(classFile.getName(), classFile);
                    }
                }
            }
        }
        return Collections.enumeration(files.values());
    }

    //useless
    public void close()
    throws java.io.IOException {
    }

    /** @return string that contains system entries from classpath e.g. classes.zip */
    private static final String getSystemEntriesFromClassPath() {
        NbClassPath boot = NbClassPath.createBootClassPath ();
        NbClassPath system = NbClassPath.createClassPath ();
        NbClassPath library = NbClassPath.createLibraryPath ();


        return
            boot.getClassPath () + File.pathSeparatorChar +
            system.getClassPath () + File.pathSeparatorChar + library.getClassPath ();
    }
}

/*
 * Log
 *  14   src-jtulach1.13        1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  13   src-jtulach1.12        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   src-jtulach1.11        8/4/99   Ales Novak      old constructor readded
 *  11   src-jtulach1.10        8/4/99   Ales Novak      bugfix #1658
 *  10   src-jtulach1.9         6/10/99  Ales Novak      import of x.y.z fix
 *  9    src-jtulach1.8         6/9/99   Ian Formanek    Class made public so 
 *       that JavaDoc compiles
 *  8    src-jtulach1.7         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    src-jtulach1.6         6/8/99   Ales Novak      FSCapabilities deployed
 *  6    src-jtulach1.5         5/31/99  Jaroslav Tulach 
 *  5    src-jtulach1.4         5/7/99   Ales Novak      getAllLibraries moved 
 *  4    src-jtulach1.3         4/23/99  Petr Hrebejk    Classes temporay made 
 *       public
 *  3    src-jtulach1.2         4/23/99  Ales Novak      modules + lib added
 *  2    src-jtulach1.1         4/6/99   Ales Novak      
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 */
