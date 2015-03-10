/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jini;

import java.io.*;
import java.awt.Dialog;
import java.awt.event.*;
import java.net.*;
import java.rmi.server.RMIClassLoader;
import java.text.MessageFormat;
import java.util.*;

import org.openide.*;

import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.*;

/**
 * Saves selectsd interface (including inner classes) into reposirory. 
 * Clone of rmi.registry.SaveInterfaceAction
 *
 * @author Martin Ryzl, Petr Kuzel
 */
public class SaveInterfaceAction extends NodeAction {

    private static boolean DEBUG = false;


    /**
    * @return true because it is assumed that it is 
    * attached only to interface nodes.
    */
    public boolean enable(Node[] nodes) {
        return true;
    }

    /** Action.
    */
    protected void performAction(final Node[] nodes) {

        if (nodes.length == 0) return;

        FileObject fs = selectFileSystem();

        for (int x = 0; x < nodes.length; x++) {
            InterfaceNode in = (InterfaceNode) nodes[x];
            if (in != null) {
                Class cl = in.getInterface();
                if (cl != null) {

                    // class and package names
                    String classname = cl.getName();
                    int index = classname.lastIndexOf('.');
                    String classfile, classpackage;
                    if (index != -1) {
                        classfile = classname.substring(index + 1);
                        classpackage = classname.substring(0, index);
                    } else {
                        classfile = classname;
                        classpackage = null;
                    }

                    // lock for writing
                    FileLock lock = null;

                    // streams
                    InputStream is = null;
                    OutputStream os = null;

                    try {
                        // load class as a resource
                        is = cl.getResourceAsStream(classfile + ".class");

                        if (is != null) {
                            // select filesystem to save interface in
                            if (fs != null) {
                                FileObject fp = getPackage(fs, classpackage);
                                FileObject fo = fp.createData(classfile, "class");
                                lock = fo.lock();
                                os = fo.getOutputStream(lock);

                                // load bytecode
                                final byte[] buffer = new byte[4096];
                                int i, n = 0;

                                while ((i = is.read(buffer)) > -1) {
                                    n += i;
                                    os.write(buffer, 0, i);
                                }
                            }
                        }
                    } catch (IOException ex) {
                        // already exist
                        //	    TopManager.getDefault().notifyException(ex);
                    } catch (Exception ex) {
                        TopManager.getDefault().notifyException(ex);
                    } finally {
                        // release lock
                        if (lock != null) lock.releaseLock();

                        // close streams
                    try { if (is != null) is.close(); } catch (IOException ex2) {}
                        try { if (os != null) os.close(); } catch (IOException ex2) {}
                    }
                }
            } else {
                // [TODO] Notification of failure.
            }

        }
    }


    /** Get (create) package.
     * @param fs filesystem (package root)
     * @param pkg package name
     * @return file object representing the package
     */
    protected static FileObject getPackage(FileObject fs, String pkg) throws IOException {
        if (pkg == null) return fs;

        StringTokenizer st = new StringTokenizer(pkg, ".");
        String token;
        FileObject fo = fs;

        while (st.hasMoreElements()) {
            token = st.nextToken();
            // must be a folder
            if (fs.isFolder()) {
                fo = fs.getFileObject(token);
                if (fo == null) {
                    // create folder
                    fo = fs.createFolder(token);
                }
            } else {
                break;
            }
            fs = fo;
        }

        if (!fs.isFolder()) {
            throw new IOException("Package " + pkg + " cannot be created.");
        }

        return fo;
    } // getPackage


    /** Lets the user to select a file system where the interface will be saved.
     * @return FileObject for the filesystem.
     */
    protected static FileObject selectFileSystem() {


        // data filter for filesystem selection
        DataFilter df = new DataFilter() {
                            public boolean acceptDataObject(DataObject obj) {
                                return false;
                            }
                        };

        try {
            Node nd = TopManager.getDefault().getPlaces().nodes().repository(df);

            // select file system
            Node node = TopManager.getDefault().getNodeOperation().select(
                            Util.getString("LAB_SelectFilesystem"), // "Select filesystem",
                            Util.getString("LAB_LookIn"), // "Look in",
                            nd);
            if (DEBUG) System.err.println("SaveInterface: I've got node " + node);

            return ((DataNode)node).getDataObject().getPrimaryFile();
        } catch (UserCancelException ex) {
            return null;
        }
    } // select filesystem

    /** Get a human presentable name of the action. This may be presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return Util.getString("PROP_SaveInterfaceActionName"); // new String("Save interface");
    }

    /** Get a help context for the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(SaveInterfaceAction.class);
    }
}


/*
* <<Log>>
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

