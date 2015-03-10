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

package org.openidex.projects;

import java.io.IOException;
import java.util.*;

import org.openide.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.src.ClassElement;
import org.openide.util.*;
import org.openide.util.io.NbMarshalledObject;
import org.openide.windows.WindowManager;

import org.openidex.projects.*;

/**
 *
 * @author  mryzl
 */

public abstract class ProjectSupport implements java.io.Serializable,
            ProjectCookie, OpenCookie, SaveCookie
{

    /** Extended attribute dexcribing what should be stored in this project. */
    private static final String EA_SAVE = "Project-Save"; // NOI18N

    public static final int SAVE_CONTROL_PANEL = 1;
    public static final int SAVE_LOADERS = 2;
    public static final int SAVE_MODULES = 4;
    public static final int SAVE_REPOSITORY = 8;
    public static final int SAVE_SERVICES = 16;
    public static final int SAVE_WINDOW_MANAGER = 32;
    public static final int SAVE_ALL = SAVE_CONTROL_PANEL |
                                       SAVE_LOADERS |
                                       SAVE_MODULES |
                                       SAVE_REPOSITORY |
                                       SAVE_SERVICES |
                                       SAVE_WINDOW_MANAGER;


    /** Info type. Indicates that open of something has started. */
    public static final int INFO_OPEN_START = 1;
    /** Info type. Indicates that open of something has finished. */
    public static final int INFO_OPEN_DONE = 2;
    /** Info type. Indicates that open of something has finished. */
    public static final int INFO_OPEN_ERR = 3;
    /** Info type. Indicates that open of something has started. */
    public static final int INFO_SAVE_START = 4;
    /** Info type. Indicates that open of something has finished. */
    public static final int INFO_SAVE_DONE = 5;
    /** Info type. Indicates that open of something has finished. */
    public static final int INFO_SAVE_ERR = 6;

    /** Information concerning Control Panel. */
    public static final int INFO_CONTROL_PANEL = 1;
    /** Information concerning Loaders. */
    public static final int INFO_LOADERS = 2;
    /** Information concerning Modules. */
    public static final int INFO_MODULES = 4;
    /** Information concerning Repository. */
    public static final int INFO_REPOSITORY = 8;
    /** Information concerning Services. */
    public static final int INFO_SERVICES = 16;
    /** Information concerning Window Manager. */
    public static final int INFO_WINDOW_MANAGER = 32;



    private DataObject dobj;

    /** Creates new ProjectSupport. */
    public ProjectSupport(DataObject dobj) {
        this.dobj = dobj;
    }

    /** Get asociated entry.
    */
    protected DataObject getDataObject() {
        return dobj;
    }


    /** Error notifier.
    * @param msg a message
    */
    protected void notifyError(String message) {
        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                  message,
                                  NotifyDescriptor.ERROR_MESSAGE
                              );
        TopManager.getDefault().notify(nd);
    }

    /** Close the project. This method instructs the project that another project
    * is becoming the active project and that the project can drop allocated 
    * resources.
    *
    * @exception IOException if an error occurs during saving
    */
    public void projectClose () throws IOException {
    }

    /** Save the project.
    */
    public void projectSave() throws java.io.IOException {

        int save = getSave();

        ProjectContent project = getProjectContent();
        boolean err = false;

        // store project
        project.store();

        // store workspaces
        try {
            if ((save & SAVE_WINDOW_MANAGER) != 0) {
                info(INFO_SAVE_START, INFO_WINDOW_MANAGER);
                project.storeWindowManager();
                info(INFO_SAVE_DONE, INFO_WINDOW_MANAGER);
            }
        } catch (IOException ex) {
            notifyError(getLocalizedString("ERR_WindowManagerWrite")); // NOI18N
        }

        // for all settings set
        // store diff sets
        try {
            if ((save & SAVE_REPOSITORY) != 0) {
                info(INFO_SAVE_START, INFO_REPOSITORY);
                project.getRepository(true).store();
                info(INFO_SAVE_DONE, INFO_REPOSITORY);
            }
        } catch (IOException ex) {
            notifyError(getLocalizedString("ERR_RepositoryWrite")); // NOI18N
        }

        try {
            if ((save & SAVE_SERVICES) != 0) {
                info(INFO_SAVE_START, INFO_SERVICES);
                project.getServices(true).store();
                info(INFO_SAVE_DONE, INFO_SERVICES);
            }
        } catch (IOException ex) {
            notifyError(getLocalizedString("ERR_ServicesWrite")); // NOI18N
        }

        // create OptionProcessor for all SettingsSet and all options
        OptionProcessor.Set cpop = new OptionProcessor.Set(), lpop = new OptionProcessor.Set(),
                                   moop = new OptionProcessor.Set();

        info(INFO_SAVE_START, INFO_CONTROL_PANEL);
        if ((save & SAVE_CONTROL_PANEL) != 0) {
            err = false;
            // for current project content and all parent
            for(ProjectContent pc = project; pc != null; pc = pc.getSuperProject()) {
                // save SettingsSets
                // update OptionProcessor
                try {
                    SettingsSet cp = pc.getControlPanel(true);
                    cp.write(cpop);
                    cpop.addAll(cp.getObjects());
                } catch (IOException ex) {
                    err = true;
                }
            }
            if (err) {
                notifyError(getLocalizedString("ERR_ControlPanelWrite")); // NOI18N
            }
        }
        info(INFO_SAVE_DONE, INFO_CONTROL_PANEL);

        info(INFO_SAVE_START, INFO_LOADERS);
        if ((save & SAVE_LOADERS) != 0) {
            err = false;
            // for current project content and all parent
            for(ProjectContent pc = project; pc != null; pc = pc.getSuperProject()) {
                try {
                    SettingsSet lp = pc.getLoaderPool(true);
                    lp.write(lpop);
                    lpop.addAll(lp.getObjects());
                } catch (IOException ex) {
                    err = true;
                }
            }
            if (err) {
                notifyError(getLocalizedString("ERR_LoadersWrite")); // NOI18N
            }
        }
        info(INFO_SAVE_DONE, INFO_LOADERS);
    }

    /** Open the project.
    */
    public void projectOpen() throws java.io.IOException {

        ProjectContent project = getProjectContent();
        boolean err = false;

        // for all settings set
        // create OptionProcessor for all SettingsSet and all options
        OptionProcessor.Set cpop = new OptionProcessor.Set(), lpop = new OptionProcessor.Set(),
                                   moop = new OptionProcessor.Set();

        // read repository
        try {
            info(INFO_OPEN_START, INFO_REPOSITORY);
            updateRepository();
            info(INFO_OPEN_DONE, INFO_REPOSITORY);
        } catch (IOException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
            notifyError(getLocalizedString("ERR_RepositoryRead")); // NOI18N
        }

        // read loaders
        info(INFO_OPEN_START, INFO_LOADERS);
        err = false;
        // for current project content and all parent
        for(ProjectContent pc = project; pc != null; pc = pc.getSuperProject()) {
            try {
                SettingsSet lp;
                try {
                    lp = pc.getLoaderPool(false);
                } catch (IOException ex) {
                    err = true;
                    lp = pc.getLoaderPool(true);
                }
                lp.read(lpop);
                lpop.addAll(lp.getObjects());
            } catch (IOException ex) {
                err = true;
            }
        }
        if (err) {
            notifyError(getLocalizedString("ERR_LoadersRead")); // NOI18N
        }
        info(INFO_OPEN_DONE, INFO_LOADERS);

        // read control panel
        info(INFO_OPEN_START, INFO_CONTROL_PANEL);
        err = false;
        // for current project content and all parent
        for(ProjectContent pc = project; pc != null; pc = pc.getSuperProject()) {
            // read SettingsSets
            // update OptionProcessor
            try {
                SettingsSet cp;
                try {
                    cp = pc.getControlPanel(false);
                } catch (IOException ex) {
                    err = true;
                    cp = pc.getControlPanel(true);
                }
                cp.read(cpop);
                cpop.addAll(cp.getObjects());
            } catch (IOException ex) {
                err = true;
            }
        }
        if (err) {
            notifyError(getLocalizedString("ERR_ControlPanelRead")); // NOI18N
        }
        info(INFO_OPEN_DONE, INFO_CONTROL_PANEL);

        // read services
        try {
            info(INFO_OPEN_START, INFO_SERVICES);
            updateServices();
            info(INFO_OPEN_DONE, INFO_SERVICES);
        } catch (Exception ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
            notifyError(getLocalizedString("ERR_ServicesRead")); // NOI18N
        }

        // and the best ... - window manager
        try {
            info(INFO_OPEN_START, INFO_WINDOW_MANAGER);
            NbMarshalledObject nbo = null;

            for(ProjectContent pc = project; pc != null; pc = pc.getSuperProject()) {
                if ((nbo = pc.getWindowManager()) != null) break;
            }
            updateWindowManager(nbo);
            info(INFO_OPEN_DONE, INFO_WINDOW_MANAGER);
        } catch (Exception ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
            notifyError(getLocalizedString("ERR_WindowManagerRead")); // NOI18N
        }
    }

    /**
    */
    protected void updateWindowManager(NbMarshalledObject nbo) throws IOException, ClassNotFoundException {
        if (nbo != null) nbo.get();
    }

    /** Apply diffset on repository.
    */
    protected void updateRepository() throws IOException {

        Stack stack = new Stack();
        List list = new LinkedList();   // list of names of filesystems
        Map map = new HashMap();  // names and filesystems
        DiffSet diffset = null;
        Enumeration en;
        Iterator it;
        FileSystem fs;
        boolean err = false;

        for(ProjectContent pc = getProjectContent(); pc != null; pc = pc.getSuperProject()) {
            try {
                diffset = pc.getRepository(false);
            } catch (IOException ex) {
                err = true;
                diffset = pc.getRepository(true);
            }
            stack.push(diffset);
            if (diffset.isClear()) break;
        }

        Repository repository = org.openide.TopManager.getDefault().getRepository();

        if (diffset != null) {
            if (!diffset.isClear()) {
                // get filesystems from repository
                en = repository.fileSystems();
                while (en.hasMoreElements()) {
                    fs = (FileSystem) en.nextElement();
                    if (!fs.isDefault()) {
                        String fsname = fs.getSystemName();
                        list.add(fsname);
                        map.put(fsname, fs);
                    }
                }
            }

            while (!stack.empty()) {
                diffset = (DiffSet) stack.pop();

                // remove items
                it = diffset.removedItems().iterator();
                while (it.hasNext()) {
                    Object obj = it.next();
                    list.remove(obj);
                    map.remove(obj);
                }

                // add items
                it = diffset.addedItems().iterator();
                while (it.hasNext()) {
                    Object obj = it.next();
                    if (!list.contains(obj)) {
                        list.add(obj);
                        map.put(obj, diffset.addedItem((String) obj));
                    }
                }
            }

            // clear repository
            en = repository.fileSystems();
            while (en.hasMoreElements()) {
                fs = (FileSystem) en.nextElement();
                if (!fs.isDefault()) repository.removeFileSystem(fs);
            }

            // add new content
            it = list.iterator();
            while (it.hasNext()) {
                String fsname = (String) it.next();
                if (repository.findFileSystem( fsname ) == null) {
                    fs = (FileSystem) map.get(fsname);
                    if (fs != null) repository.addFileSystem(fs);
                }
            }
        }

        if (err) throw new IOException("repository update"); // NOI18N
        // [PENDING - change order]
    }

    /** Apply diffset on services.
    */
    protected void updateServices() throws IOException {
        /** [PENDIND]
        * Current implementation only clears all services and add freshly loaded 
        * ones.
        */

        Stack stack = new Stack();
        List list = new LinkedList();   // list of names of filesystems
        Map map = new HashMap();
        DiffSet diffset = null;
        Enumeration en;
        Iterator it;
        FileSystem fs;
        boolean err = false;

        for(ProjectContent pc = getProjectContent(); pc != null; pc = pc.getSuperProject()) {
            try {
                diffset = pc.getServices(false);
            } catch (IOException ex) {
                err = true;
                diffset = pc.getServices(true);
            }
            stack.push(diffset);
            if (diffset.isClear()) break;
        }

        ServiceType.Registry registry = TopManager.getDefault().getServices();

        if (diffset != null) {
            if (!diffset.isClear()) {
                // get current services
                en = registry.services();
                while (en.hasMoreElements()) {
                    ServiceType st = (ServiceType) en.nextElement();
                    String name = st.getName();
                    list.add(name);
                    map.put(name, st);
                }
            }

            while (!stack.empty()) {
                diffset = (DiffSet) stack.pop();

                // remove items
                it = diffset.removedItems().iterator();
                while (it.hasNext()) {
                    String name = (String) it.next();
                    list.remove(name);
                    map.remove(name);
                }

                // add items
                it = diffset.addedItems().iterator();
                while (it.hasNext()) {
                    String name = (String) it.next();
                    if (!list.contains(name)) {
                        list.add(name);
                        map.put(name, diffset.addedItem(name));
                    }
                }
            }

            List services = new ArrayList(list.size());
            it = list.iterator();
            while (it.hasNext()) {
                Object service = map.get(it.next());
                services.add(service);
            }

            registry.setServiceTypes(services);
        }
        if (err) throw new IOException("service update"); // NOI18N
    }

    /** Get project content.
    */
    abstract protected ProjectContent getProjectContent() throws java.io.IOException;

    /** Called when an action is performed. Should be overriden in subclasses.
    * For example, before opening the ControlPanel, 
    * info(INFO_OPEN_START, INFO_CONTROL_PANEL) is called.
    *
    * @param type type of the info
    * @param additional info
    */
    public void info(int type, int what) {
    }

    /** Getter for property save.
     *@return Value of property save.
     */
    public int getSave() {
        Integer save = (Integer) dobj.getPrimaryFile().getAttribute(EA_SAVE);
        return (save != null) ? save.intValue(): SAVE_ALL;
    }

    /** Setter for property save.
     *@param save New value of property save.
     */
    public void setSave(int save) throws java.io.IOException {
        dobj.getPrimaryFile().setAttribute(EA_SAVE, new Integer(save));
    }

    /**
    */
    private String getLocalizedString(String key) {
        return NbBundle.getBundle(ProjectSupport.class).getString(key);
    }

    // OpenCookie implementation

    /** Opens project
    */
    public void open() {
        RequestProcessor.postRequest(new Runnable() {
                                         public void run() {
                                             try {
                                                 org.openide.TopManager.getDefault().openProject(ProjectSupport.this);
                                             } catch (UserCancelException ex) {
                                                 // user canceled -> nothing to do
                                             } catch (IOException ex) {
                                                 TopManager.getDefault().notifyException(ex);
                                             }

                                         }
                                     });
    }

    // SaveCookie implementation

    /** Save project.
    */
    public void save() throws java.io.IOException {
        final IOException[] ex1 = { null };

        RequestProcessor.postRequest(new Runnable() {
                                         public void run() {
                                             try {
                                                 projectSave();
                                             } catch (IOException ex) {
                                                 ex1[0] = ex;
                                             }
                                         }
                                     });
        if (ex1[0] != null) {
            TopManager.getDefault().notifyException(ex1[0]);
            throw ex1[0];
        }
    }
}

/*
* Log
*  16   Gandalf   1.15        2/11/00  Martin Ryzl     changed the order of 
*       loading of settings  bugfix of #5447
*  15   Gandalf   1.14        2/4/00   Martin Ryzl     fixed
*  14   Gandalf   1.13        1/17/00  Martin Ryzl     
*  13   Gandalf   1.12        1/17/00  Martin Ryzl     
*  12   Gandalf   1.11        1/14/00  Martin Ryzl     
*  11   Gandalf   1.10        1/14/00  Martin Ryzl     projectClose() added
*  10   Gandalf   1.9         1/13/00  Martin Ryzl     
*  9    Gandalf   1.8         1/12/00  Martin Ryzl     
*  8    Gandalf   1.7         1/11/00  Martin Ryzl     clear() added
*  7    Gandalf   1.6         1/10/00  Martin Ryzl     
*  6    Gandalf   1.5         1/8/00   Martin Ryzl     
*  5    Gandalf   1.4         1/7/00   Martin Ryzl     some bugfixes  
*  4    Gandalf   1.3         1/4/00   Martin Ryzl     
*  3    Gandalf   1.2         1/3/00   Martin Ryzl     
*  2    Gandalf   1.1         12/28/99 Martin Ryzl     
*  1    Gandalf   1.0         12/22/99 Martin Ryzl     
* $ 
*/ 
