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

package org.netbeans.modules.apisupport;

import java.io.*;
import java.util.*;

import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.modules.ModuleInstall;
import org.openide.util.SharedClassObject;

import org.openidex.util.Utilities2;

import org.netbeans.modules.apisupport.beanbrowser.*;

public class APIModule extends ModuleInstall {

    private static final Class[] actions = new Class[] {
                                               ShowAPIJavadocAction.class,
                                               BeanBrowseAction.class,
                                               BeanBrowseWindowAction.class,
                                               BeanBrowseMasterAction.class,
                                               BeanBrowseGroupAction.class,
                                               NodeExploreAction.class,
                                               TemplateWizardHelperAction.class,
                                               LocalDocsLinkAction.class,
                                           };

    private static final long serialVersionUID = -8817465769852478093L;

    private static final String PROP_INSTALL_COUNT = "installCount";

    public void installed () {
        try {
            FileUtil.extractJar
            // Note: the templates will not really work unless in the package 'Templates',
            // due to their package names, and the links used in the group files.
            (TopManager.getDefault ().getPlaces ().folders ().templates ().getPrimaryFile (),
             getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/apisupport/resources/templates.jar"));
            // Actions pool:
            DataFolder folder = DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "API_Support");
            for (int i = 0; i < actions.length; i++)
                Utilities2.createAction (actions[i], folder);
            folder = DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().menus (), "Help");
            Utilities2.createAction
            (LocalDocsLinkAction.class, folder,
             "org-netbeans-core-actions-WebLinkAction$NetbeansOpenApiWebLink",
             true, false, false, false);
        } catch (IOException e) {
            TopManager.getDefault ().notifyException (e);
        }
        restored ();
    }

    public void restored () {
        System.setProperty ("netbeans.module.test", "true");
        // Mount docs, or remount if project was discarded:
        Integer count = (Integer) getProperty (PROP_INSTALL_COUNT);
        int icount = count == null ? 1 : count.intValue () + 1;
        putProperty (PROP_INSTALL_COUNT, new Integer (icount));
        // 1: first install (project is discarded anyway)
        // 2: first restore as actual user
        // 3: next restore (project settings incl. Repository loaded)
        if (icount <= 2) {
            File f = findAPIDocs ();
            if (f != null) {
                try {
                    // Mount docs in Documentation Repository:
                    JarFileSystem fs = new JarFileSystem ();
                    fs.setJarFile (f);
                    fs.setHidden (true);
                    FileSystemCapability capab = fs.getCapability ();
                    if (capab instanceof FileSystemCapability.Bean) {
                        FileSystemCapability.Bean bean = (FileSystemCapability.Bean) capab;
                        bean.setCompile (false);
                        bean.setExecute (false);
                        bean.setDebug (false);
                        bean.setDoc (true);
                    } else {
                        System.err.println ("Warning: JarFileSystem had strange capability: " + capab);
                    }
                    Repository repo = TopManager.getDefault ().getRepository ();
                    if (repo.findFileSystem (fs.getSystemName ()) == null)
                        repo.addFileSystem (fs);
                    else
                        System.err.println ("Note: OpenAPIs.zip was already present in Repository.");
                } catch (Exception e) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                        e.printStackTrace ();
                }
            } else {
                System.err.println("Note: OpenAPIs.zip not found to add to Javadoc, ignoring...");
            }
        }
    }

    public void uninstalled () {
        try {
            // [PENDING] Later on, would be nice to remove templates too.
            // Unmount docs (AutoUpdate should handle actually removing the file):
            File fo = findAPIDocs ();
            if (fo != null) {
                Repository repo = TopManager.getDefault ().getRepository ();
                Enumeration e = repo.fileSystems ();
                while (e.hasMoreElements ()) {
                    Object o = e.nextElement ();
                    //System.err.println("repo elt: " + o);
                    if (o instanceof JarFileSystem) {
                        JarFileSystem jfs = (JarFileSystem) o;
                        //System.err.println("fo.canonicalPath: " + fo.getCanonicalPath ());
                        //System.err.println("jfs.jarFile.canonicalPath: " + jfs.getJarFile ().getCanonicalPath ());
                        if (fo.getCanonicalPath ().equals (jfs.getJarFile ().getCanonicalPath ())) {
                            repo.removeFileSystem (jfs);
                            //System.err.println("removed");
                            break;
                        }
                    }
                }
            } else {
                System.err.println("Note: OpenAPIs.zip not found to remove from Javadoc, ignoring...");
            }
            // Actions pool:
            DataFolder folder = DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "API_Support");
            for (int i = 0; i < actions.length; i++)
                Utilities2.removeAction (actions[i], folder);
            if (folder.getChildren ().length == 0) folder.delete ();
            folder = DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().menus (), "Help");
            Utilities2.removeAction (LocalDocsLinkAction.class, folder);
        } catch (IOException ioe) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ioe.printStackTrace ();
        }
    }

    public void updated (int release, String specVersion) {
        try {
            // RestartAction:
            DataFolder folder = DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().menus (), "File");
            InstanceDataObject.remove (folder, "RestartAction", "org.netbeans.modules.apisupport.RestartAction");
            folder = DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "System");
            InstanceDataObject.remove (folder, "RestartAction", "org.netbeans.modules.apisupport.RestartAction");
        } catch (IOException ioe) {
            // Ignore
        }
    }

    static File findAPIDocs () {
        try {
            String suffix = "docs" + File.separator + "OpenAPIs.zip";
            String user = System.getProperty ("netbeans.user");
            if (user != null) {
                File f = new File (user, suffix);
                if (f.exists ()) return f.getCanonicalFile ();
            }
            String home = System.getProperty ("netbeans.home");
            if (home != null) {
                File f = new File (home, suffix);
                if (f.exists ()) return f.getCanonicalFile ();
            }
        } catch (IOException ioe) {
            TopManager.getDefault ().notifyException (ioe);
        }
        return null;
    }

    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal (in);
        putProperty (PROP_INSTALL_COUNT, in.readObject ());
    }

    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal (out);
        out.writeObject (getProperty (PROP_INSTALL_COUNT));
    }

}

/*
 * Log
 *  22   Gandalf-post-FCS1.18.1.2    3/30/00  Jesse Glick     
 *  21   Gandalf-post-FCS1.18.1.1    3/30/00  Jesse Glick     Added local docs link.
 *  20   Gandalf-post-FCS1.18.1.0    3/28/00  Jesse Glick     
 *  19   Gandalf   1.18        1/26/00  Jesse Glick     Live manifest parsing.
 *  18   Gandalf   1.17        1/22/00  Jesse Glick     Less annoying error 
 *       messages; and deleting actions folder on uninstall.
 *  17   Gandalf   1.16        1/11/00  Jesse Glick     
 *  16   Gandalf   1.15        1/10/00  Jesse Glick     Actions pool.
 *  15   Gandalf   1.14        1/10/00  Jesse Glick     Hopefully OpenAPIs.zip 
 *       should now be installed automatically in internal builds.
 *  14   Gandalf   1.13        1/5/00   Jesse Glick     Automatically turning on
 *       netbeans.module.test property.
 *  13   Gandalf   1.12        11/10/99 Jesse Glick     Restart action removed; 
 *       NbBundle use off by default.
 *  12   Gandalf   1.11        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   Gandalf   1.10        10/13/99 Jesse Glick     Various fixes and 
 *       enhancements:    - added a Changes.txt    - fixed handling of 
 *       OpenAPIs.zip on install/uninstall (previously did not correctly unmount
 *       on uninstall, nor check for already-mounted on install)    - added a 
 *       CompilerTypeTester    - display name & icon updates from Tim    - 
 *       removed link to ToDo.txt from docs page    - various BeanInfo's, both 
 *       in templates and in the support itself, did not display superclass 
 *       BeanInfo correctly    - ExecutorTester now permits user to customize 
 *       new executor instance before running it
 *  10   Gandalf   1.9         10/7/99  Jesse Glick     
 *  9    Gandalf   1.8         10/5/99  Jesse Glick     Will have API docs in an
 *       NBM.
 *  8    Gandalf   1.7         10/5/99  Jesse Glick     ModuleInstall changes.
 *  7    Gandalf   1.6         9/30/99  Jesse Glick     Package rename and misc.
 *  6    Gandalf   1.5         9/23/99  Jesse Glick     
 *  5    Gandalf   1.4         9/20/99  Jesse Glick     Install also to actions 
 *       pool.
 *  4    Gandalf   1.3         9/20/99  Jesse Glick     New resources package.
 *  3    Gandalf   1.2         9/20/99  Jesse Glick     Added RestartAction.
 *  2    Gandalf   1.1         9/16/99  Jesse Glick     Handling DOUBLE_QUOTE 
 *       the hard way...
 *  1    Gandalf   1.0         9/12/99  Jesse Glick     
 * $
 */
