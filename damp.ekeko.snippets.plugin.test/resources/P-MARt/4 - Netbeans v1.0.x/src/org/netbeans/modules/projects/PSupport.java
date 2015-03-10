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

package org.netbeans.modules.projects;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.text.MessageFormat;

import org.openide.*;
import org.openide.cookies.*;
import org.openide.debugger.DebuggerException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;

import org.openidex.projects.*;
import org.openide.util.SharedClassObject;

import org.netbeans.modules.projects.content.*;

/**
 *
 * @author  mryzl
 */

public class PSupport extends ProjectSupport implements ExecCookie, DebuggerCookie {

    static final long serialVersionUID = -8907373672945248595L;

    static final String GLOBAL_METHOD_NAME = "isGlobal"; // NOI18N

    /** Relative pathname to file with list of beans. */
    static final String BEANS_LIB_FILE = File.separatorChar + "beans" + File.separatorChar + "libs.properties"; // NOI18N

    /** Values of following properties will be used as prefix for pathnames to files with list of beans.  */
    static final String[] BEANS_LIB_PROPERTIES = new String[] {
                "netbeans.users", // NOI18N
                "netbeans.home", // NOI18N
            };

    private static transient ProjectContent globalContent = null;
    private transient ProjectContent projectContent = null;

    /** Creates a new PSupport.
    */
    public PSupport(DataObject dobj) {
        super(dobj);
    }

    /** Get node representing this project.
    */
    public Node projectDesktop() {
        ProjectDataObject pdo = (ProjectDataObject) getDataObject();
        Children children = Children.LEAF;
        try {
            children = pdo.getFileFolder().createNodeChildren (DataFilter.ALL);
        } catch (java.io.IOException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
        }
        ProjectDataNode pdn = new ProjectDataNode(pdo, children, true);
        return new ProjectFilterNode(pdn, new ProjectFilterChildren(pdn));
    }

    /** Get current ProjectContent for this project.
    */
    protected ProjectContent getProjectContent() {
        if (projectContent == null) {
            projectContent = new XMLProjectContent(getDataObject().getPrimaryFile(), getGlobalProjectContent());
        }
        return projectContent;
    }

    /** Get global ProjectContent.
    * * @return ProjectContent common for all projects
    */
    static ProjectContent getGlobalProjectContent() {
        if (globalContent == null) {
            DataFolder folder = org.openide.TopManager.getDefault().getPlaces().folders().projects();
            globalContent = new XMLProjectContent(folder.getPrimaryFile(), null);
        }
        return globalContent;
    }

    /** Close the project. This method instructs the project that another project
     * is becoming the active project and that the project can drop allocated 
     * resources.
     *
     * @exception IOException if an error occurs during saving
     */
    public void projectClose () throws IOException {
        super.projectClose();

        // drop resources
        projectContent = null;
    }

    /**
    */
    public void projectOpen() throws java.io.IOException {

        TopManager tm = TopManager.getDefault();
        ResourceBundle nb = NbBundle.getBundle(PSupport.class);
        Object[] objs = new Object[] { getDataObject().getName() };

        try {
            tm.setStatusText(MessageFormat.format(nb.getString("MSG_OpenProject"), objs));

            if (ProjectsModule.defaultProjectCreated) {
                DiffSet services = getProjectContent().getServices(true);
                setServices(services);
                services.store();
            }

            super.projectOpen();

            if (ProjectsModule.numberOfStarts < 2) {
                // install JavaDoc
                JavaDocHack.installJavaDoc();
            }

            if (ProjectsModule.defaultProjectCreated) {
                ProjectsModule.defaultProjectCreated = false;

                if (ProjectsModule.numberOfStarts >= 2) {
                    // install JavaDoc
                    JavaDocHack.installJavaDoc();
                }

                // add beans
                addBeans();


                // add initial file system
                String canpath = NbBundle.getBundle (ProjectsModule.class).getString ("FILE_Development"); // NOI18N
                try {
                    LocalFileSystem localFS = MainClassHelper.createFileSystem();
                    MainClassHelper.setRelative(localFS, canpath);

                    Repository r = TopManager.getDefault ().getRepository ();
                    if (r.findFileSystem(localFS.getSystemName()) == null) {
                        r.addFileSystem (localFS);
                    }
                    DiffSet diffset = getProjectContent().getRepository(true);
                    setRepository(diffset);
                    diffset.store();

                } catch (IOException ex) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
                    Object[] arg = new Object[] {canpath};
                    System.out.println (new MessageFormat(NbBundle.getBundle(ProjectsModule.class).getString("CTL_Local_not_mounted")).format(arg));
                }
            }
            tm.setStatusText(MessageFormat.format(nb.getString("MSG_OpenProjectSuccessfull"), objs));

        } catch (IOException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
            tm.setStatusText(MessageFormat.format(nb.getString("MSG_OpenProjectUnsuccessfull"), objs));
            throw ex;
        }
    }

    /** Save the project.
    */
    public void projectSave() throws java.io.IOException {
        TopManager tm = TopManager.getDefault();

        tm.saveAll();

        ResourceBundle nb = NbBundle.getBundle(PSupport.class);
        Object[] objs = new Object[] { getDataObject().getName() };

        try {
            tm.setStatusText(MessageFormat.format(nb.getString("MSG_SaveProject"), objs));

            ProjectContent gpc = getGlobalProjectContent(), pc = getProjectContent();
            SharedClassObject[] scos = null;
            Enumeration en;
            DiffSet diffset;

            // update global project
            // update control panel
            scos = tm.getControlPanel().getSystemOptions();
            updateSettingsSet(gpc.getControlPanel(true), scos);

            // update loaders
            scos = tm.getLoaderPool().toArray();
            updateSettingsSet(gpc.getLoaderPool(true), scos);

            // update modules - not at all
            // update repository - not now
            // update services - not now
            // update window manager
            // if null, save current manager
            // update project
            // update control panel - only project specific options
            scos = tm.getControlPanel().getSystemOptions();
            scos = getProjectObjects(scos);
            updateSettingsSet(pc.getControlPanel(true), scos);

            /*
            System.err.println("PSupport: projectSave.controlPanel"); // NOI18N
            for(Iterator it = pc.getControlPanel().getObjects().iterator();
              it.hasNext(); System.err.println("\t" + it.next())); // NOI18N
            */

            // update loaders
            scos = tm.getLoaderPool().toArray();
            scos = getProjectObjects(scos);
            updateSettingsSet(pc.getLoaderPool(true), scos);

            // update modules - not at all

            // update repository
            setRepository(pc.getRepository(true));

            // update services
            setServices(pc.getServices(true));

            // update window manager
            // if null, set global window manager
            super.projectSave();
            tm.setStatusText(MessageFormat.format(nb.getString("MSG_SaveProjectSuccessfull"), objs));
        } catch (java.io.IOException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
            tm.setStatusText(MessageFormat.format(nb.getString("MSG_SaveProjectUnsuccessfull"), objs));
            throw ex;
        }
    }

    /** Set repository to current state.
    */
    private void setRepository(DiffSet diffset) {
        Enumeration en;
        TopManager tm = TopManager.getDefault();

        diffset.clear();
        en = tm.getRepository().getFileSystems();
        while (en.hasMoreElements()) {
            FileSystem fs = (FileSystem) en.nextElement();
            diffset.add(fs.getSystemName(), fs);
        }
    }

    /** Set services to current state.
    */
    private void setServices(DiffSet diffset) {
        Enumeration en;
        TopManager tm = TopManager.getDefault();

        diffset.clear();
        en = tm.getServices().services();
        while (en.hasMoreElements()) {
            ServiceType st = (ServiceType) en.nextElement();
            diffset.add(st.getName(), st);
        }
    }


    /** Update SettingsSet to new values.
    * @param set SettingsSet
    * @param scos new values
    */
    private void updateSettingsSet(SettingsSet set, SharedClassObject[] scos) {
        set.clear();
        for(int i = 0; i < scos.length; i++) {
            set.add(scos[i]);
        }
    }

    /** Called when an action is performed. Should be overriden in subclasses.
    * @param type type of the info
    * @param additional info
    */
    public void info(int type, int what) {
        TopManager tm = TopManager.getDefault();
        ResourceBundle nb = NbBundle.getBundle(PSupport.class);

        if (type == INFO_OPEN_START) {
            switch (what) {
            case INFO_CONTROL_PANEL:
                tm.setStatusText(nb.getString("MSG_OpenControlPanel"));
                break;
            case INFO_LOADERS:
                tm.setStatusText(nb.getString("MSG_OpenLoaders"));
                break;
            case INFO_MODULES:
                tm.setStatusText(nb.getString("MSG_OpenModules"));
                break;
            case INFO_REPOSITORY:
                tm.setStatusText(nb.getString("MSG_OpenRepository"));
                break;
            case INFO_SERVICES:
                tm.setStatusText(nb.getString("MSG_OpenServices"));
                break;
            case INFO_WINDOW_MANAGER:
                tm.setStatusText(nb.getString("MSG_OpenWindowManager"));
                break;
            }
        }

        if (type == INFO_SAVE_START) {
            switch (what) {
            case INFO_CONTROL_PANEL:
                tm.setStatusText(nb.getString("MSG_SaveControlPanel"));
                break;
            case INFO_LOADERS:
                tm.setStatusText(nb.getString("MSG_SaveLoaders"));
                break;
            case INFO_MODULES:
                tm.setStatusText(nb.getString("MSG_SaveModules"));
                break;
            case INFO_REPOSITORY:
                tm.setStatusText(nb.getString("MSG_SaveRepository"));
                break;
            case INFO_SERVICES:
                tm.setStatusText(nb.getString("MSG_SaveServices"));
                break;
            case INFO_WINDOW_MANAGER:
                tm.setStatusText(nb.getString("MSG_SaveWindowManager"));
                break;
            }
        }
    }

    /** Opens new project
    */
    public void openNew() {
        RequestProcessor.postRequest(new Runnable() {
                                         public void run() {
                                             boolean keepRepository = false;

                                             try {

                                                 try {
                                                     DiffSet services = getProjectContent().getServices(true);
                                                     setServices(services);
                                                     services.store();
                                                 } catch (IOException ex) {
                                                     // ignore it
                                                 }

                                                 if (!Import.isImport) {
                                                     ResourceBundle bundle = NbBundle.getBundle(PSupport.class);
                                                     String NEW = bundle.getString("CTL_New"), OLD = bundle.getString("CTL_Old");
                                                     NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                                                                               bundle.getString("MSG_ClearRepositoryQuestion"),
                                                                               bundle.getString("CTL_Question"),
                                                                               NotifyDescriptor.YES_NO_OPTION,
                                                                               NotifyDescriptor.QUESTION_MESSAGE
                                                                           );
                                                     nd.setOptions(new Object[] { NEW, OLD});
                                                     Object result = TopManager.getDefault().notify(nd);
                                                     if (result.equals(OLD)) {
                                                         // keep repository
                                                         keepRepository = true;
                                                         DiffSet diffset = getProjectContent().getRepository(true);
                                                         setRepository(diffset);
                                                     } else {
                                                         keepRepository = false;
                                                     }
                                                 }

                                                 org.openide.TopManager.getDefault().openProject(PSupport.this);

                                                 if (Import.isImport) {
                                                     // add beans
                                                     PSupport.addBeans();
                                                     // add java doc
                                                     JavaDocHack.installJavaDoc();
                                                     // import
                                                     ImportProjectAction ia = new ImportProjectAction();
                                                     ia.getDefaultImport().importAll();
                                                 } else {
                                                     if (!keepRepository) {
                                                         // add beans
                                                         PSupport.addBeans();
                                                         // add java doc
                                                         JavaDocHack.installJavaDoc();
                                                         // ask for new filesystem
                                                         MainClassHelper.addFileSystem();
                                                     }
                                                 }

                                                 // save repository
                                                 getProjectContent().getRepository(true).store();

                                             } catch (UserCancelException ex) {
                                                 // user canceled -> nothing to do
                                             } catch (IOException ex) {
                                                 TopManager.getDefault().notifyException(ex);
                                             } finally {
                                                 Import.isImport = false;
                                             }
                                         }
                                     });
    }

    /** Add Beans filesystems to the repository.
    * @param diffset repository
    */
    public static void addBeans(DiffSet diffset) {
        FileSystem[] fss = findBeans();
        for(int i = 0; i < fss.length; i++) {
            diffset.add(fss[i].getSystemName(), fss[i]);
        }
    }

    /** Add Beans as filesystems to the repository.
    */
    public static void addBeans() {
        FileSystem[] fss = findBeans();
        Repository rep = TopManager.getDefault().getRepository();
        for(int i = 0; i < fss.length; i++) {
            String fsname = fss[i].getSystemName();
            if (rep.findFileSystem(fsname) == null) {
                rep.addFileSystem(fss[i]);
            }
        }
    }

    /** Find Beans.
    * @return an array of filesystems representing jar files with beans
    */
    public static FileSystem[] findBeans() {
        List list = new ArrayList(32);

        for(int i = 0; i < BEANS_LIB_PROPERTIES.length; i++) {
            String pref = System.getProperty(BEANS_LIB_PROPERTIES[i]);
            String fname = null;
            if (pref != null) {
                fname = pref + BEANS_LIB_FILE;
            } else {
                continue;
            }
            // open the file and read all keys, then mount them to the repository
            InputStream is = null;
            Properties props = new Properties();
            try {
                is = new FileInputStream(fname);
                props.load(is);
            } catch (IOException ex) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception ex2) {
                        //
                    }
                }
            }
            Enumeration en = props.propertyNames();
            while (en.hasMoreElements()) {
                String fname2 = props.getProperty((String) en.nextElement());
                try {
                    File file = new File(fname2);
                    JarFileSystem jar = new JarFileSystem();
                    jar.setJarFile(file);
                    jar.setHidden(true);
                    list.add(jar);
                } catch (Exception ex) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
                    // ignore all
                }
            }
        }
        return (FileSystem[]) list.toArray(new FileSystem[list.size()]);
    }


    /** Filter out objects that are not in project.
    * @param scos set of all objects
    * @return filtered set
    */
    public static SharedClassObject[] getProjectObjects(SharedClassObject[] scos) {
        int i,j;
        for(i = 0, j = 0; i < scos.length; i++ ){
            if (isProjectObject(scos[i])) j++;
        }
        SharedClassObject[] objs = new SharedClassObject[j];
        for(i = 0, j = 0; i < scos.length; i++ ){
            if (isProjectObject(scos[i])) objs[j++] = scos[i];
        }
        return objs;
    }

    /** Testatic if the object is Project specific.
    * @param obj an object
    * @return true if the object is Project specific
    */
    public static boolean isProjectObject(SharedClassObject obj) {
        try {
            Class clazz = obj.getClass();
            Method m = clazz.getMethod(GLOBAL_METHOD_NAME, new Class[] {});
            m.setAccessible(true);
            Boolean b = (Boolean) m.invoke(obj, new Object[] {});
            return !b.booleanValue();
        } catch (Exception ex) {
            // default is false
        }
        return false;
    }

    /** Start execution of the project.
    */
    public void start() {
        ProjectDataObject df = (ProjectDataObject) getDataObject();
        DataObject dobj = MainClassHelper.getMainClass(df);
        if (dobj == null) {
            try {
                MainClassHelper.setMainClassDialog(df);
                dobj = MainClassHelper.getMainClass(df);
            } catch (java.io.IOException ex) {
                //
            }
        }
        if (dobj != null) {
            ExecCookie ec = (ExecCookie)dobj.getCookie(ExecCookie.class);
            if (ec != null) {
                ec.start();
            }
        }
    }

    // DebuggerCookie

    /** Start debugging of associated object.
    * @param stopOnMain if <code>true</code>, debugger stops on the first line of debugged code
    * @exception DebuggerException if the session cannot be started
    */
    public void debug(boolean stopOnMain) throws DebuggerException {
        ProjectDataObject df = (ProjectDataObject) getDataObject();
        DataObject dobj = MainClassHelper.getMainClass(df);
        if (dobj == null) {
            try {
                MainClassHelper.setMainClassDialog(df);
                dobj = MainClassHelper.getMainClass(df);
            } catch (java.io.IOException ex) {
                //
            }
        }
        if (dobj != null) {
            DebuggerCookie dc = (DebuggerCookie)dobj.getCookie(DebuggerCookie.class);
            if (dc != null) {
                dc.debug(stopOnMain);
            }
        }
    }


    CompilerCookie getCompileCookie(Class c) {
        return new CompileCookie(c);
    }

    protected ProjectDataObject getProjectDataObject() {
        return (ProjectDataObject) getDataObject();
    }

    // CompilerCookie

    private final class CompileCookie implements CompilerCookie {

        private CompilerCookie cookie;

        /** Get delegatee CompilerCookie.
        * @return CompilerCookie or null
        */
        public CompileCookie(Class c) {
            ProjectDataObject pdo = getProjectDataObject();
            try {
                cookie = (CompilerCookie) pdo.getFileFolder().getCookie(c);
            } catch (Exception ex) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
            }
        }

        public void addToJob(org.openide.compiler.CompilerJob job,
                             org.openide.compiler.Compiler.Depth depth) {

            if (cookie != null) {
                cookie.addToJob (job, org.openide.compiler.Compiler.DEPTH_INFINITE);
            }
        }

        public boolean isDepthSupported(org.openide.compiler.Compiler.Depth depth) {
            return true;
        }
    }

    public static void main(String[] args) {
        FileSystem[] fss = findBeans();
        for(int i = 0; i < fss.length; i++) {
            System.err.println("fss[" + i + "] = " + fss[i]);
        }
    }
}

/*
* Log
*  27   Gandalf-post-FCS1.23.1.2    3/24/00  Martin Ryzl     change of order of 
*       initialization when a new project is to be opened
*  26   Gandalf-post-FCS1.23.1.1    3/23/00  Martin Ryzl     fix of previous checkin
*  25   Gandalf-post-FCS1.23.1.0    3/23/00  Martin Ryzl     when a new project is 
*       created, services are stored  this change is necessary because behaviour
*       of ServiceType.Registry changed - setServiceTypes(_empty_list_) now 
*       clears the registry
*  24   Gandalf   1.23        3/20/00  Martin Ryzl     hotfix - delete non-links
*       from project prohibited
*  23   Gandalf   1.22        2/11/00  Martin Ryzl     filesystem keep/clear 
*       dialog
*  22   Gandalf   1.21        2/10/00  Martin Ryzl     DebugProjectAction added
*  21   Gandalf   1.20        2/8/00   Martin Ryzl     javadoc hack
*  20   Gandalf   1.19        2/4/00   Martin Ryzl     
*  19   Gandalf   1.18        1/24/00  Martin Ryzl     call to protected method 
*       from inner class fixed
*  18   Gandalf   1.17        1/24/00  Martin Ryzl     fixed #5520, build 
*       project
*  17   Gandalf   1.16        1/19/00  Martin Ryzl     javadoc hack
*  16   Gandalf   1.15        1/18/00  Martin Ryzl     uses relative filesystem 
*       in default project
*  15   Gandalf   1.14        1/18/00  Jesse Glick     Localization.
*  14   Gandalf   1.13        1/17/00  Martin Ryzl     
*  13   Gandalf   1.12        1/16/00  Martin Ryzl     comment changed  
*  12   Gandalf   1.11        1/16/00  Martin Ryzl     
*  11   Gandalf   1.10        1/14/00  Martin Ryzl     
*  10   Gandalf   1.9         1/13/00  Martin Ryzl     
*  9    Gandalf   1.8         1/13/00  Martin Ryzl     heavy localization
*  8    Gandalf   1.7         1/12/00  Martin Ryzl     
*  7    Gandalf   1.6         1/11/00  Martin Ryzl     
*  6    Gandalf   1.5         1/10/00  Martin Ryzl     
*  5    Gandalf   1.4         1/9/00   Martin Ryzl     
*  4    Gandalf   1.3         1/8/00   Martin Ryzl     
*  3    Gandalf   1.2         1/7/00   Martin Ryzl     
*  2    Gandalf   1.1         1/4/00   Martin Ryzl     
*  1    Gandalf   1.0         1/3/00   Martin Ryzl     
* $ 
*/ 
