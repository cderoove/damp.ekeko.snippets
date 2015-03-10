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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.text.MessageFormat;
import java.util.*;

import javax.swing.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.cookies.*;
import org.openide.util.*;
import org.openide.util.enum.*;
import org.openide.windows.*;

import org.netbeans.modules.projects.settings.ProjectOption;

/**
 * Library class that supports setting/getting main class.
 * 
 * @author  Petr Kuzel
 * @version 
 */
public class MainClassHelper extends Object {

    public static final String MAINCLASS = "mainClass"; // NOI18N

    /** Forbid Creating new MainClassHelper */
    private MainClassHelper() {
    }

    /**
    * Set new main class for specified project.
    * 
    * @param project folder containing the project 
    */
    public static void setMainClassDialog(ProjectDataObject project) throws IOException {

        DataFolder files = getFiles(project);

        Children kids = files.createNodeChildren(new Filter());

        Node root = project.support.projectDesktop().cloneNode();

        // show UI and let user select
        DataObject obj = getMainClassDO(project);
        Node delegate = null;
        if (obj != null) {
            delegate = ExplorerView.getNodeFor(root, obj.getPrimaryFile());
        }
        Node selection = getSelection(root, delegate);

        if (selection == null) return; //cancel operation

        setMainClass(project, (DataObject) selection.getCookie(DataObject.class));

    }

    /**
    * Set new main class for specified project.
    * 
    * @param project folder containing the project 
    */
    public static void setMainClass(ProjectDataObject project, DataObject dobj) throws IOException {

        if (dobj != null) {
            // delete old one
            DataObject original = getMainClassDO(project);
            if (original != null) original.delete();

            // create new one
            DataShadow.create(project, MAINCLASS, dobj);
        }
    }

    /**
    * Get main class for specified project.
    * 
    * @param project folder containing the project 
    * @return ExecCookie or null if no main class is  specified.
    */  
    public static DataObject getMainClass(DataFolder project) {

        return getMainClassDO(project);
    }

    /**
    * @return DataObject of main class or null
    */
    private static DataObject getMainClassDO(DataFolder project) {

        Enumeration en = new ArrayEnumeration(project.getChildren());

        while (en.hasMoreElements()) {
            DataObject next = (DataObject) en.nextElement();

            if (next.getName().equals(MAINCLASS))
                return next;
        }

        return null;
    }

    /**
    * @return project's FILES datafolder
    * @throw IOException if not found
    */
    private static DataFolder getFiles(DataFolder project) throws IOException {

        Enumeration en = new ArrayEnumeration(project.getChildren());

        while (en.hasMoreElements()) {
            DataObject next = (DataObject) en.nextElement();

            if (next.getName().equals(ProjectDataObject.FILES_FOLDER)) {
                DataFolder cake = (DataFolder) next.getCookie(DataFolder.class);
                if (cake != null)
                    return cake;
            }
        }

        throw new IOException(MessageFormat.format(
                                  NbBundle.getBundle(MainClassHelper.class).getString("FMT_FolderNotFound"),
                                  new Object[] { ProjectDataObject.FILES_FOLDER }
                              ));
    }


    /**
    * Display UI and let user do selection.
    * @return user selection or null if canceled. 
    */
    private static Node getSelection(Node root, Node last) {

        ExplorerView view = new ExplorerView(root, (last != null) ? new Node[] {last}: null,
                                             NbBundle.getBundle(MainClassHelper.class).getString("CTL_SelectMainClass")
                                            );

        ExplorerView.DialogAcceptor acc = new ExplorerView.DialogAcceptor();

        DialogDescriptor desc = new DialogDescriptor (
                                    view, view.getName(), true,
                                    DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                                    DialogDescriptor.BOTTOM_ALIGN, getHelpCtx(), acc
                                );

        Dialog dlg = TopManager.getDefault().createDialog(desc);
        dlg.setModal(true);
        dlg.show();

        if (!acc.ok) return null;

        Node[] nodes = view.getSelected();
        if ((nodes.length > 0) &&
                ((nodes[0].getCookie(ExecCookie.class) != null) ||
                 (nodes[0].getCookie(DebuggerCookie.class) != null))) return nodes[0];
        return null;
    }

    /** HELP
    */
    private static HelpCtx getHelpCtx() {
        return new HelpCtx(MainClassHelper.class);
    }

    /**
    * Accepts just executable nodes and packages.
    */
    private static class Filter implements DataFilter {

        public boolean acceptDataObject(DataObject obj) {
            if (obj.getCookie(ProjectDataObject.class) != null) return false;
            if (obj.getCookie(DataFolder.class) != null) return true;
            if (obj.getCookie(ExecCookie.class) != null) return true;
            return false;
        }
    }

    /**
    */
    public static boolean canAddToProject(String name) {


        ProjectOption po = new ProjectOption();
        int what = po.getAddToProject();

        switch (what) {
        case ProjectOption.ADD_NEVER: return false;
        case ProjectOption.ADD_ALWAYS: return true;
        }

        NotifyDescriptor nd = new NotifyDescriptor.Confirmation (
                                  MessageFormat.format(
                                      NbBundle.getBundle(MainClassHelper.class).getString("FMT_AddToProjectQuestion"),
                                      new Object[] { name }
                                  ),
                                  NbBundle.getBundle(MainClassHelper.class).getString("CTL_Question"),
                                  NotifyDescriptor.YES_NO_CANCEL_OPTION
                              );

        String YES = NbBundle.getBundle(MainClassHelper.class).getString("CTL_YES");
        String NO = NbBundle.getBundle(MainClassHelper.class).getString("CTL_NO");
        String ALWAYS = NbBundle.getBundle(MainClassHelper.class).getString("CTL_ALWAYS");
        String NEVER = NbBundle.getBundle(MainClassHelper.class).getString("CTL_NEVER");

        nd.setOptions(new Object[] { YES, NO, ALWAYS, NEVER });

        Object obj = TopManager.getDefault().notify(nd);

        if (obj.equals(YES)) return true;

        if (obj.equals(ALWAYS)) {
            po.setAddToProject(po.ADD_ALWAYS);
            return true;
        }

        if (obj.equals(NEVER)) {
            po.setAddToProject(po.ADD_NEVER);
        }

        return false;
    }

    /** ResetWindowManager. */
    public static void resetWindowManager() {
        WindowManager wm = TopManager.getDefault().getWindowManager();
        try {
            Method m = wm.getClass().getMethod("createFromScratch", new Class[] {}); // NOI18N

            // remove Workspaces
            Workspace[] ws = wm.getWorkspaces();
            for(int i = 0; i < ws.length; i++) {
                ws[i].remove();
            }
            m.invoke(null, new Object[] {});
            // reset
        } catch (Exception ex) {
            // problems, don't be surprised ...
        }
    }

    /** Create a filesystem. */
    public static LocalFileSystem createFileSystem(File file) throws IOException, PropertyVetoException {
        try {
            file = file.getCanonicalFile ();
        } catch (java.io.IOException ex) {
            // ignore it is not needed
        }
        LocalFileSystem localFS = createFileSystem();
        localFS.setRootDirectory (file);
        return localFS;
    }

    /** Create a filesystem. */
    public static LocalFileSystem createFileSystem(String path)  throws IOException, PropertyVetoException {
        return createFileSystem(new File(path));
    }

    /** Create a filesystem.
    * [NOTE] use of impl.ExLocalFileSystem recommended by JTulach.
    */
    public static LocalFileSystem createFileSystem() {
        LocalFileSystem localFS;
        try {
            Class clazz = Class.forName("org.netbeans.core.ExLocalFileSystem"); // NOI18N
            localFS = (LocalFileSystem) clazz.newInstance();
        } catch (Exception ex) {
            localFS = new LocalFileSystem();
        }
        return localFS;
    }

    /** */
    public static void setRelative(LocalFileSystem fs, String path) throws IOException {
        try {
            //      System.err.println("MCH: fs.class = " + fs.getClass());
            Method m = fs.getClass().getMethod("setRelativeDirectory", new Class[] { String.class }); // NOI18N
            m.invoke(fs, new Object[] { path });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IOException("Unable to set relative."); // NOI18N
        }
    }

    /** Remember last folder, which was explored in FileChooser of Mount-Dir action */
    private static File lastMountDirFolder = null;

    /** Displays a dialog for adding new FileSystems.
    */
    public static void addFileSystem() {
        JFileChooser chooser = new FSChooser();
        HelpCtx.setHelpIDString (chooser, getHelpCtx ().getHelpID ());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(ProjectDataObject.getLocalizedString ("CTL_Mount_Dialog_Title")); // NOI18N

        if (lastMountDirFolder != null) {
            chooser.setCurrentDirectory(lastMountDirFolder);
        }

        if (chooser.showDialog(TopManager.getDefault ().getWindowManager ().getMainWindow (),
                               ProjectDataObject.getLocalizedString ("CTL_Mount_Approve_Button")) // NOI18N
                == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if ((f != null) && (f.isDirectory())) {
                try {
                    lastMountDirFolder = chooser.getCurrentDirectory();

                    LocalFileSystem localFS = createFileSystem(f);

                    Repository r = TopManager.getDefault ().getRepository ();
                    if (r.findFileSystem(localFS.getSystemName()) == null) {
                        r.addFileSystem (localFS);
                    }
                    else {
                        TopManager.getDefault().notify(
                            new NotifyDescriptor.Message(ProjectDataObject.getLocalizedString ("MSG_LocalFSAlreadyMounted"), // NOI18N
                                                         NotifyDescriptor.ERROR_MESSAGE)
                        );
                    }
                } catch (java.io.IOException ex) {
                    //
                } catch (java.beans.PropertyVetoException ex) {
                    //
                }
            }
        }
    }

    // -- Inner classes. --

    /** Class used for the choosing of filesystem (local or jar) */
    static class FSChooser extends JFileChooser {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 4451076153335278278L;

        public FSChooser() {
            setBorder(new javax.swing.border.EmptyBorder(0, 8, 0, 8));
        }

        public Dimension getPreferredSize() {
            Dimension pref = super.getPreferredSize ();
            return new Dimension(Math.max (425, pref.width), Math.max (250, pref.height));
        }
    }

}


/*
* Log
*  13   Gandalf   1.12        2/10/00  Martin Ryzl     DebugProjectAction added
*  12   Gandalf   1.11        1/19/00  Martin Ryzl     localization
*  11   Gandalf   1.10        1/18/00  Ian Formanek    removed debug println
*  10   Gandalf   1.9         1/18/00  Martin Ryzl     
*  9    Gandalf   1.8         1/17/00  Martin Ryzl     
*  8    Gandalf   1.7         1/16/00  Martin Ryzl     
*  7    Gandalf   1.6         1/14/00  Martin Ryzl     
*  6    Gandalf   1.5         1/13/00  Martin Ryzl     heavy localization
*  5    Gandalf   1.4         1/12/00  Martin Ryzl     
*  4    Gandalf   1.3         1/10/00  Martin Ryzl     
*  3    Gandalf   1.2         1/9/00   Martin Ryzl     
*  2    Gandalf   1.1         1/8/00   Martin Ryzl     
*  1    Gandalf   1.0         1/8/00   Petr Kuzel      
* $ 
*/ 

