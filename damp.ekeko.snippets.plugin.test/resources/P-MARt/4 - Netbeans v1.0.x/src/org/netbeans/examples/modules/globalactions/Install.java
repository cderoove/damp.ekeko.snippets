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

package org.netbeans.examples.modules.globalactions;

import java.beans.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

import org.openide.*;
import org.openide.actions.ExecuteAction;
import org.openide.cookies.InstanceCookie;
import org.openide.execution.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;

/** Install {@link TestAction} into the main window's menu and toolbar.
*
* <p>Feel free to copy-and-paste pieces of this implementation for your own
* install code, if you are doing similar things.
*/
public class Install extends ModuleInstall {

    // Arbitrary names. They ensure that the file name is unique.
    // (More important for the separator since its class will be common.)
    // Generally, a more specific name would be a good idea to avoid
    // conflicts.
    // null could be used if the instance class is obviously from your
    // module, but using a specific name is more descriptive.
    // Note that this name does NOT affect the display name of the action!
    private static final String ACTION_NAME = "MyTestAction";
    private static final String SEP_NAME = "MyTestActionSeparator";

    /** Install actions.
    * Adds the {@link TestAction} to the top of the Tools menu (with an intervening separator);
    * and to the end of the Data toolbar (with a toolbar separator).
    *
    * <p>Note that the {@link TestServiceAction} is completely handled by the manifest.
    * For {@link TestAction}, placing it in the manifest would add it as a service action,
    * which is permissible but unnecessary since it is installed specially here.
    *
    * <p>Also installs startup file for keybindings.
    *
    * <p>[PENDING] Should also install the actions into the actions pool for full user customizability.
    */
    public void installed () {
        try {
            installInstance (getMenuFolder (), SEP_NAME, JSeparator.class, true);
            installInstance (getMenuFolder (), ACTION_NAME, TestAction.class, true);
            installInstance (getToolbarFolder (), SEP_NAME, JToolBar.Separator.class, false);
            installInstance (getToolbarFolder (), ACTION_NAME, TestAction.class, false);
            installInstance (getPoolFolder (), ACTION_NAME, TestAction.class, false);
            // Startup file.
            InputStream is = null;
            try {
                // First retrieve the desired contents from the module resource.
                // It is only for convenience that this is stored as a separate file in the module.
                is = Install.class.getClassLoader ().getResourceAsStream
                     ("org/netbeans/examples/modules/globalactions/GlobalActionsKeys.java_");
                if (is == null) System.err.println ("Resource not found");
                FileObject startup = TopManager.getDefault ().getPlaces ().folders ().startup ().getPrimaryFile ();
                // Never try to overwrite an existing startup file.
                if (startup.getFileObject ("GlobalActionsKeys", "java") == null) {
                    FileObject gakFile = startup.createData ("GlobalActionsKeys", "java");
                    // Write the proper contents to it.
                    FileLock lock = null;
                    try {
                        lock = gakFile.lock ();
                        OutputStream os = null;
                        try {
                            os = gakFile.getOutputStream (lock);
                            byte[] buf = new byte[1024];
                            int count;
                            while ((count = is.read (buf)) != -1)
                                os.write (buf, 0, count);
                        } finally {
                            if (os != null) os.close ();
                        }
                    } finally {
                        if (lock != null) lock.releaseLock ();
                    }
                    try {
                        final DataObject gak = DataObject.find (gakFile);
                        if (gak instanceof MultiDataObject) {
                            // Set internal execution. This property will be stored on disk.
                            ExecSupport.setExecutor (((MultiDataObject) gak).getPrimaryEntry (), Executor.find (ThreadExecutor.class));
                            // (Compile and) execute it now in a separate thread.
                            // This could take a couple of seconds, so do not block during module install,
                            // doing so might cause a deadlock.
                            RequestProcessor.postRequest (new Runnable () {
                                                              public void run () {
                                                                  // This runs it appropriately, compiling first.
                                                                  ExecuteAction.execute (new DataObject[] { gak }, true);
                                                              }
                                                          });
                        } else {
                            // Will not happen with standard Java module, but to be safe:
                            System.err.println ("Not a MultiDataObject");
                            gak.delete ();
                        }
                    } catch (DataObjectNotFoundException donfe) {
                        donfe.printStackTrace ();
                    }
                }
            } finally {
                if (is != null) is.close ();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace ();
        }
    }

    /** Uninstall the same actions we previously installed.
    * Conditionally removes keybinding startup file as well.
    */
    public void uninstalled () {
        try {
            uninstallInstance (getMenuFolder (), SEP_NAME, JSeparator.class);
            uninstallInstance (getMenuFolder (), ACTION_NAME, TestAction.class);
            uninstallInstance (getToolbarFolder (), SEP_NAME, JToolBar.Separator.class);
            uninstallInstance (getToolbarFolder (), ACTION_NAME, TestAction.class);
            uninstallInstance (getPoolFolder (), ACTION_NAME, TestAction.class);
            // Startup file.
            FileObject gakFile = TopManager.getDefault ().getPlaces ().folders ().startup ().
                                 getPrimaryFile ().getFileObject ("GlobalActionsKeys", "java");
            // Only delete if still exists and user permits it.
            // User might have made valuable modifications they would wish to
            // save elsewhere.
            if (gakFile != null &&
                    NotifyDescriptor.OK_OPTION ==
                    TopManager.getDefault ().notify (new NotifyDescriptor.Confirmation
                                                     ("Do you want to delete file Startup/GlobalActionsKeys.java?",
                                                      "Uninstalling Global Actions"))) {
                try {
                    // Delete the whole data object, not just the file;
                    // otherwise the .class will remain.
                    DataObject gak = DataObject.find (gakFile);
                    gak.delete ();
                } catch (DataObjectNotFoundException donfe) {
                    donfe.printStackTrace ();
                }
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    /** Get the folder to install menu items in.
    * @return the folder
    */
    private static DataFolder getMenuFolder () throws IOException {
        return DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().menus (), "Tools");
    }
    /** Get the folder to install toolbar items in.
    * @return the folder
    */
    private static DataFolder getToolbarFolder () throws IOException {
        return DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().toolbars (), "Data");
    }
    /** Get the folder for the actions pool.
    * @return the folder
    */
    private static DataFolder getPoolFolder () throws IOException {
        return DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "GlobalActions");
    }

    /** Install an instance into a folder.
    * @param where the folder to add to
    * @param name the (data-object) name of the instance
    * @param clazz the instance class that will be instantiated in the system object
    * @param atFront <code>true</code> to place at the beginning of the folder; <code>false</code> to place at the end
    */
    private void installInstance (final DataFolder where, String name, Class clazz, boolean atFront) throws IOException {
        if (InstanceDataObject.find (where, name, clazz) != null) return;
        final InstanceDataObject ido = InstanceDataObject.create (where, name, clazz);
        List newkids = new ArrayList ();
        if (atFront) newkids.add (ido);
        DataObject[] oldkids = where.getChildren ();
        int seen = 0;
        for (int i = 0; i < oldkids.length; i++) {
            DataObject kid = oldkids[i];
            // DataObject==DataObject should work, but just to be safe:
            if (kid.equals (ido))
                seen++;
            else
                newkids.add (kid);
        }
        if (! atFront) newkids.add (ido);
        if (seen != 1) System.err.println ("Saw instance data object " + name + " " + seen + " (!= 1) times!");
        where.setOrder ((DataObject[]) newkids.toArray (new DataObject[newkids.size ()]));
    }
    /** Uninstall an instance from a folder.
    * Also removes the folder if there was nothing else in it.
    * @param where folder to uninstall from
    * @param name instance name
    * @param clazz instance class
    */
    private void uninstallInstance (DataFolder where, String name, Class clazz) throws IOException {
        if (! InstanceDataObject.remove (where, name, clazz))
            throw new IOException ("Could not remove instance " + name + " (reason unknown)");
        if (where.getChildren ().length == 0)
            where.delete ();
    }

    /** Restore the module.
    * In this case does nothing--the instances are already on disk.
    */
    public void restored () {
    }
    /** Permit the IDE to be closed.
    * @return <code>true</code> to permit it
    */
    public boolean closing () {
        return true;
    }

    /** Test main method. */
    public static void main (String[] args) {
        if (args.length != 1) throw new RuntimeException ();
        if (args[0].equals ("install"))
            new Install ().installed ();
        else if (args[0].equals ("uninstall"))
            new Install ().uninstalled ();
        else
            throw new RuntimeException ();
    }
}
