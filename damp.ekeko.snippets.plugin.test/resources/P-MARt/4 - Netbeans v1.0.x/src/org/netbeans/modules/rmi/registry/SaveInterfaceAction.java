/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi.registry;

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
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Martin Ryzl
 */
public class SaveInterfaceAction extends CookieAction {

    /** Serial version UID. */
    static final long serialVersionUID = 9058662081474395978L;

    /** Resource bundle. */
    private static ResourceBundle bundle = NbBundle.getBundle(SaveInterfaceAction.class);

    /** Get the cookies that this action requires.
    * @return a list of cookies
    */
    protected Class[] cookieClasses() {
        return new Class[] { InterfaceNode.class };
    }

    /** Get the mode of the action, i.e. how strict it should be about cookie support.
    * @return the mode of the action. Possible values are disjunctions of the MODE_XXX constants.
    */
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    /** Action.
    */
    protected void performAction(final Node[] nodes) {
        if (nodes.length > 0) {
            InterfaceNode in = (InterfaceNode) nodes[0].getCookie(InterfaceNode.class);
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
                        is = cl.getResourceAsStream(classfile + ".class"); // NOI18N

                        if (is != null) {
                            // select filesystem to save interface in
                            FileObject fs = selectFileSystem();
                            if (fs != null) {
                                FileObject fp = getPackage(fs, classpackage);
                                FileObject fo = fp.createData(classfile, "class"); // NOI18N
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
                        TopManager.getDefault().notifyException(ex);
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

        StringTokenizer st = new StringTokenizer(pkg, "."); // NOI18N
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
            throw new IOException(bundle.getString("ERR_PackageCreation")); // NOI18N
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
                            bundle.getString("LAB_SelectFilesystem"),  // NOI18N
                            bundle.getString("LAB_LookIn"),  // NOI18N
                            nd);

            return ((DataNode)node).getDataObject().getPrimaryFile();
        } catch (UserCancelException ex) {
            return null;
        }
    } // select filesystem

    /** Get a human presentable name of the action. This may be presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return bundle.getString("PROP_SaveInterfaceActionName");  // NOI18N
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
 *  4    Gandalf-post-FCS1.2.1.0     3/20/00  Martin Ryzl     localization
 *  3    Gandalf   1.2         11/27/99 Patrik Knakal   
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         8/27/99  Martin Ryzl     
 * $
 */














