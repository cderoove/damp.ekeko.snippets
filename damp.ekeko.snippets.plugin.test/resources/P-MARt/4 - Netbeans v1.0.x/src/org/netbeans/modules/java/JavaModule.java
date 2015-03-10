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

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.MissingResourceException;
import java.util.jar.JarInputStream;
import java.util.jar.JarEntry;

import org.openide.TopManager;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.ExternalCompiler;
import org.openide.modules.ModuleInstall;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

import org.openidex.util.Utilities2;

import org.netbeans.modules.java.settings.JavaSettings;

/**
* Module installation class for JDO.
*
* @author Ales Novak
*/
public class JavaModule extends ModuleInstall implements java.beans.PropertyChangeListener {

    static final long serialVersionUID =9005197261261486367L;

    /** Module installed for the first time. */
    public void installed() {
        // -----------------------------------------------------------------------------
        // 1. copy Java templates
        copyTemplates ();

        try {
            Utilities2.createAction (SynchronizeAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "Tools")); // NOI18N
        } catch (IOException ioe) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ioe.printStackTrace ();
        }

        restored();
    }

    public void restored() {
        JavaSettings set = (JavaSettings) JavaSettings.findObject(JavaSettings.class, false);
        set.setCompiler(true);
        TopManager.getDefault().addPropertyChangeListener(this);
    }
    
    public void uninstalled () {
        try {
            Utilities2.removeAction (SynchronizeAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "Tools")); // NOI18N
        } catch (IOException ioe) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ioe.printStackTrace ();
        }
        TopManager.getDefault().removePropertyChangeListener(this);
    }

    /** Invoked on update */
    public void updated(int release, String specVersion) {
        copyFastJavaC();
        restored();
        afterUpdate = true;
    }
    
    // -----------------------------------------------------------------------------
    // Private methods


    private void copyTemplates () {
        try {
            org.openide.filesystems.FileUtil.extractJar (
                org.openide.TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
                NbBundle.getLocalizedFile ("org.netbeans.modules.java.resources.templates", "jar").openStream () // NOI18N
            );
        } catch (java.io.IOException e) {
            org.openide.TopManager.getDefault ().notifyException (e);
        }
    }

    private static void copyFastJavaC() {
        try {
            InputStream is = NbBundle.getLocalizedFile("org.netbeans.modules.java.resources.fastjavac", "jar").openStream(); // NOI18N
            JarInputStream jaris = new JarInputStream(is);
            String wiredName = FastJavacCompilerType.getWiredName();
            String nbdir = System.getProperty("netbeans.home"); // NOI18N
            File home = new File(nbdir);
            File bin = new File(home, "bin"); // NOI18N

            // wiredName - e.g. fastjavac.sun
            extract(jaris, bin, wiredName, org.openide.util.Utilities.isUnix());
            jaris.close();

            is = NbBundle.getLocalizedFile("org.netbeans.modules.java.resources.fastjavacmsgs", "jar").openStream(); // NOI18N
            jaris = new JarInputStream(is);
            
            extract(jaris, bin, null, false);
            jaris.close();
            
        } catch (java.io.IOException e) {
            org.openide.TopManager.getDefault().notifyException (e);
        } catch (java.util.MissingResourceException mre) {
            // ignore
        }
    }
    
    private static void extract(JarInputStream jaris, File destFolder, String wiredname, boolean executable) throws IOException {
        JarEntry entry;
        
        while ((entry = jaris.getNextJarEntry()) != null) {
            String name = entry.getName();
            if (name.toLowerCase().startsWith("meta-inf/")) { // NOI18N
                continue;
            }

            if (entry.isDirectory ()) {
                File dir = new File(destFolder, name);
                if (! dir.exists()) {
                    dir.mkdirs();
                }
                continue;
            }

            if (wiredname != null) {
                if (! name.endsWith(wiredname)) {
                    continue;
                }
                name = name2Target(name);
            }
            
            // copy the file
            File destination = createData(destFolder, name);
            OutputStream os = new FileOutputStream(destination);
            
            final byte[] BUFFER = new byte[0x10000];
            int len;

            for (;;) {
                len = jaris.read(BUFFER);
                if (len == -1) break;
                os.write(BUFFER, 0, len);
            }
            os.close();
            
            // set x flag
            if (executable) {
                Runtime.getRuntime().exec(new String[] {
                    "chmod",
                    "+x",
                    destination.getCanonicalPath()
                });
            }
        }
    }
    
    private static String name2Target(String name) {
        if ((org.openide.util.Utilities.getOperatingSystem() & org.openide.util.Utilities.OS_WINDOWS_MASK) != 0) {
            return name;
        } else {
            int idx = name.indexOf('.');
            return name.substring(0, idx);
        }
    }
    
    private static File createData(File destFolder, String name) throws IOException {
        String foldername, dataname, fname, ext;
        int index = name.lastIndexOf('/');
        File data;

        // names with '/' on the end are not valid
        if (index >= name.length()) throw new IOException();

        // if name contains '/', create necessary folder first
        if (index != -1) {
            foldername = name.substring(0, index);
            dataname = name.substring(index + 1);
            destFolder = createFolder(destFolder, foldername);
        } else {
            dataname = name;
        }

        return new File(destFolder, dataname);
    }
    
    private static File createFolder(File destFolder, String name) throws IOException {
        StringTokenizer st = new StringTokenizer(name, "/"); // NOI18N
        while (st.hasMoreElements()) {
            name = st.nextToken();
            if (name.length() > 0) {
                destFolder = new File(destFolder, name);
            }
        }
        destFolder.mkdirs();
        return destFolder;
    }
    
    /** Old project node. */
    private transient org.openide.nodes.Node oldProjectNode;
    transient boolean afterUpdate = false;
    
    /** Listens on project change.
    */
    public void propertyChange(final java.beans.PropertyChangeEvent p1) {
        if (p1.getPropertyName().equals(TopManager.PROP_PLACES)) {
            org.openide.nodes.Node projectNode = TopManager.getDefault().getPlaces().nodes().projectDesktop();
            if (!projectNode.equals(oldProjectNode)) {
                JavaSettings.setCompiler(afterUpdate);
                afterUpdate = false;
                oldProjectNode = projectNode;
            }
        }
    }
}

/*
 * Log
 *  14   src-jtulach1.13        1/16/00  Jesse Glick     Actions pool, localized 
 *       jars.
 *  13   src-jtulach1.12        1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  12   src-jtulach1.11        1/4/00   Ales Novak      FastJavac - default 
 *       compiler
 *  11   src-jtulach1.10        12/22/99 Petr Hamernik   Update V8 Parser - old 
 *       one is still used..
 *  10   src-jtulach1.9         12/8/99  Petr Hamernik   update
 *  9    src-jtulach1.8         11/27/99 Patrik Knakal   
 *  8    src-jtulach1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    src-jtulach1.6         10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
 *        changed to class + some methods added
 *  6    src-jtulach1.5         9/10/99  Jaroslav Tulach Changes to services.
 *  5    src-jtulach1.4         6/10/99  Ian Formanek    Copies templates on 
 *       install
 *  4    src-jtulach1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    src-jtulach1.2         4/15/99  Martin Ryzl     JavaDataObject.createCompiler
 *        added 
 *  2    src-jtulach1.1         4/2/99   Ales Novak      
 *  1    src-jtulach1.0         3/31/99  Ales Novak      
 * $
 */
