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

package org.netbeans.core;

import java.util.*;
import java.net.*;
import java.security.PermissionCollection;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.AllPermission;

/** Supporting classloader for loading of modules.
* Handles instaling/deinstalling and installing again the module.
*
* @author Jaroslav Tulach
*/
class ModuleClassLoader extends URLClassLoader {
    /** class loader for modules */
    private static ModuleClassLoader modulesClassLoader;
    /** PermissionCollection with an instance of AllPermission. */
    private static PermissionCollection modulePermissions;

    /** Getter for class loader */
    static ClassLoader systemClassLoader () {
        return modulesClassLoader;
    }

    /** Initializes the loader by given set of URLs.
    */
    public synchronized static void initialize (URL[] urls) {
        if (modulesClassLoader != null) throw new InternalError ();
        modulesClassLoader = new ModuleClassLoader (urls);
        ClassLoaderSupport.resetLoader ();
    }

    /** Installs new URL.
    */
    public static synchronized void add (URL url) {

        if ( url == null )
            return;

        ModuleClassLoader m = modulesClassLoader.installURL (url);
        if (m != null) {
            modulesClassLoader = m;
            ClassLoaderSupport.resetLoader ();
        }
    }

    /** Uninstalls an URL.
    */
    public static synchronized void remove (URL url) {

        if ( url == null )
            return;

        modulesClassLoader.uninstallURL (url);
    }

    /** set of URL bases to ignore */
    private HashSet ignore;
    /** classloader to delegate to */
    // private ClassLoader exludeLoader;

    /** Public constructor. Initializes the loader so all URL are accessible
    * and the ignore set is empty.
    * @param urls URLs to reader from
    */
    private ModuleClassLoader (URL[] urls) {
        super (urls);
        ignore = new HashSet ();
    }

    /** Private constructor. Delegates to newly provided URL and if the desired
    * class is not found, tries to load data from system class loader. If still
    * not found asks the exclude loader. But tests wheter the URL of the class
    * is not on the list of exlude ones.
    *
    * @param url the url to delegate to
    * @param ignore list of URL to ignore
    * @param el exclude loader to load classes in the last case
    */
    private ModuleClassLoader (URL url, HashSet ignore, ClassLoader el) {
        super (new URL[] { url });
        this.ignore = ignore;
        // this.exludeLoader = el;
    }

    /** Adds new URL
    * @return the new classloader to use or null if this one is still valid
    */
    private ModuleClassLoader installURL (URL url) {

        if (ignore.contains (url) )
            ignore.remove( url );
        else
            super.addURL (url);

        return null;

        /*
        if (ignore.contains (url)) {
          // installing again already removed URL => must create
          // new classloader
          HashSet newSet = new HashSet (ignore);
          newSet.remove (url);
          return new ModuleClassLoader (url, newSet, this);
    } else {
          // only add the URL into the set we can load from
          super.addURL (url);
          return null;
    }
        */
    }

    /** Uninstall an URL
    * @param url to not use anymore
    */
    private void uninstallURL (URL url) {
        // add the URL to the list of ignored ones

        if ( url == null )
            return;

        /*
        ignore.add (url);
        try {
          url.openConnection ().setUseCaches (false);
    } catch (java.io.IOException ex) {
    }
        */

    }

    /** Finds resource. First of all try to test the delegating
    * URLs and if not found, asks exludeLoader if any.
    */
    public URL findResource (String name) {

        return super.findResource( name );

        /*
        URL u = super.findResource (name);
        if (u == null && exludeLoader != null) {
          u = exludeLoader.getResource (name);
    }
        return u;
        */
    }

    /** Loads class. First of all from this loader and if not found,
    * tries to load from the exludeLoader.
    */
    protected Class findClass (String name) throws ClassNotFoundException {

        return super.findClass (name);

        /*
        if (exludeLoader == null) {
          // behaviour of the super class
          return super.findClass (name);
    } else {

          // special behaviour
          Class c = null;
          try {
            c = super.findClass (name);
          } catch (ClassNotFoundException ex) {
          }
          if (c == null) {
            // try the exludeLoader loader
            c = Class.forName (name, true, exludeLoader);
          }
          
          return c;
    }
        */
    }

    /** Inherited.
    * @param cs is ignored
    * @return PermissionCollection with an AllPermission instance
    */
    protected PermissionCollection getPermissions(CodeSource cs) {
        return getAllPermission();
    }

    /** @return initialized @see #modulePermission */
    private static PermissionCollection getAllPermission() {
        if (modulePermissions == null) {
            synchronized (ModuleClassLoader.class) {
                if (modulePermissions == null) {
                    modulePermissions = new Permissions();
                    modulePermissions.add(new AllPermission());
                    modulePermissions.setReadOnly();
                }
            }
        }
        return modulePermissions;
    }
}

/*
* Log
*  8    src-jtulach1.7         1/5/00   Petr Hrebejk    New module installer
*  7    src-jtulach1.6         10/27/99 Petr Hrebejk    Testing of modules added
*  6    src-jtulach1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    src-jtulach1.4         6/7/99   Jaroslav Tulach FS capabilities.
*  4    src-jtulach1.3         5/14/99  Jaroslav Tulach 
*  3    src-jtulach1.2         4/21/99  Ales Novak      modules have 
*       AllPermission now
*  2    src-jtulach1.1         4/19/99  Jaroslav Tulach Updating of modules  
*  1    src-jtulach1.0         1/12/99  Jaroslav Tulach 
* $
*/
