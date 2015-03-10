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

import org.openide.util.datatransfer.NewType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.JarFileSystem;
import org.openide.modules.ManifestSection;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.TopManager;
import org.openide.nodes.BeanNode;
import org.openide.*;


/** Allows to obtain set of new types to use for
* creation of new filesystems.
*
* @author Jaroslav Tulach
*/
class ModuleFSSection extends NewType {
    /** table with New objects (ManifestSection.FileSystemSection, New) 
     * @associates ModuleFSSection*/
    private static HashMap newTypes = new HashMap ();

    /** fs section description */
    private ManifestSection.FileSystemSection fs;

    /** Constructor */
    public ModuleFSSection (ManifestSection.FileSystemSection fs) {
        this.fs = fs;
    }

    /** Constructor */
    protected ModuleFSSection () {
    }

    /** Human presentable name of the paste type. This should be
    * presented as an item in a menu.
    *
    * @return the name of the action
    */
    public String getName() {
        return fs.getName ();
    }

    /** Help context where to find more about the paste type action.
    * @return the help context for this action
    */
    public org.openide.util.HelpCtx getHelpCtx() {
        return fs.getHelpCtx ();
    }

    /** Create file system.
    */
    protected FileSystem createFileSystem () throws InstantiationException {
        return this.fs.createFileSystem ();
    }

    /** Creates the object.
    */
    public void create () throws java.io.IOException {
        try {
            FileSystem fs = createFileSystem ();
            BeanNode bn=new BeanNode(fs);
            java.awt.Component c = (java.awt.Component) bn.getCustomizer ();
            if(c==null) {
                org.openide.explorer.propertysheet.PropertySheet ps = new org.openide.explorer.propertysheet.PropertySheet ();
                ps.setNodes(new BeanNode[] {bn});
                c = ps;
            }
            DialogDescriptor dd = new DialogDescriptor (c, org.openide.util.NbBundle.getBundle(ModuleFSSection.class).getString("LAB_FS_Configuration"));
            if (HelpCtx.findHelp (c).equals (HelpCtx.DEFAULT_HELP))
                dd.setHelpCtx (getHelpCtx ());
            TopManager.getDefault ().createDialog (dd).show ();
            if(dd.getValue ().equals (DialogDescriptor.OK_OPTION)) {
                NbTopManager.getDefaultRepository ().addFileSystem (fs);
            }
        } catch (Exception ex) {
            if (System.getProperty("netbeans.debug.exceptions") != null) ex.printStackTrace();
            throw new java.io.IOException (ex.getMessage ());
        }
    }

    /** Adds new filesystem into list of filesystem ones.
    */
    public synchronized static void install (ManifestSection.FileSystemSection fs) {
        newTypes.put (fs, new ModuleFSSection (fs));
    }

    public synchronized static void uninstall (ManifestSection.FileSystemSection fs) {
        newTypes.remove (fs);
    }

    /** Getter for all new filesystem types available
    * @param inc including default or not
    * @return array of new types that can create new filesystem
    */
    public synchronized static NewType[] listOfNewTypes (boolean inc) {
        Collection c;
        if (inc) {
            ArrayList al = new ArrayList ();
            al.add (new Local ());
            al.add (new Jar ());
            al.addAll (newTypes.values ());
            c = al;
        } else {
            c = newTypes.values ();
        }
        return (NewType[])c.toArray (new NewType[0]);
    }

    private static class Local extends ModuleFSSection {
        /** Human presentable name of the paste type. This should be
        * presented as an item in a menu.
        *
        * @return the name of the action
        */
        public String getName() {
            return NbBundle.getBundle (ModuleFSSection.class).getString ("CTL_Repository_Local");
        }

        /** Help context where to find more about the paste type action.
        * @return the help context for this action
        */
        public org.openide.util.HelpCtx getHelpCtx() {
            return new HelpCtx (Local.class);
        }

        /** Creates the object.
        */
        public FileSystem createFileSystem () {
            return new ExLocalFileSystem ();
        }
    }

    private static class Jar extends ModuleFSSection {
        /** Human presentable name of the paste type. This should be
        * presented as an item in a menu.
        *
        * @return the name of the action
        */
        public String getName() {
            return NbBundle.getBundle (ModuleFSSection.class).getString ("CTL_Repository_Jar");
        }

        /** Help context where to find more about the paste type action.
        * @return the help context for this action
        */
        public HelpCtx getHelpCtx() {
            return new HelpCtx (Jar.class);
        }

        /** Creates the object.
        */
        public FileSystem createFileSystem () {
            return new ExJarFileSystem ();
        }
    }

}

/*
* Log
*  3    src-jtulach1.2         12/08/98 Jaroslav Tulach Modules at startup.
*
*
*  2    src-jtulach1.1         11/18/98 David Simonek
*  1    src-jtulach1.0         10/19/98 Jaroslav Tulach
* $
*/
