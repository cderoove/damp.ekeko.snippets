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

package org.netbeans.core.actions;

import java.io.File;
import java.awt.Dimension;
import javax.swing.JFileChooser;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.filesystems.Repository;
import org.openide.filesystems.LocalFileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.NbBundle;


/** The action that mount new file system.
*
* @author Petr Hamernik (checked [PENDING HelpCtx])
*/
public class AddFSAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2698652859441371126L;
    /** Remember last folder, which was explored in FileChooser of Mount-Dir action */
    private static File lastMountDirFolder = null;

    /** Icon of this action.
    * @return name of the action icon
    */
    public String iconResource() {
        return "/org/netbeans/core/resources/actions/addDirectory.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (AddFSAction.class);
    }

    public String getName() {
        return NbBundle.getBundle(AddFSAction.class).getString("AddFS");
    }

    /** Gets localized string. */
    private static final String getString(String s) {
        return NbBundle.getBundle(AddFSAction.class).getString(s);
    }

    /** Adds a directory. */
    public void performAction() {
        JFileChooser chooser = new FSChooser();
        HelpCtx.setHelpIDString (chooser, getHelpCtx ().getHelpID ());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(getString("CTL_Mount_Dialog_Title"));

        if (lastMountDirFolder != null) {
            chooser.setCurrentDirectory(lastMountDirFolder);
        }

        if (chooser.showDialog(TopManager.getDefault ().getWindowManager ().getMainWindow (),
                               getString("CTL_Mount_Approve_Button"))
                == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if ((f != null) && (f.isDirectory())) {
                LocalFileSystem localFS = new org.netbeans.core.ExLocalFileSystem();
                try {
                    lastMountDirFolder = chooser.getCurrentDirectory();

                    try {
                        f = f.getCanonicalFile ();
                    } catch (java.io.IOException ex) {
                        // ignore it is not needed
                    }

                    localFS.setRootDirectory (f);
                    Repository r = TopManager.getDefault ().getRepository ();
                    if (r.findFileSystem(localFS.getSystemName()) == null) {
                        r.addFileSystem (localFS);
                    }
                    else {
                        TopManager.getDefault().notify(
                            new NotifyDescriptor.Message(getString("MSG_LocalFSAlreadyMounted"),
                                                         NotifyDescriptor.ERROR_MESSAGE)
                        );
                    }
                } catch (java.io.IOException ex) {
                } catch (java.beans.PropertyVetoException ex) {
                }
            }
        }
    }


    /** Class used for the choosing of filesystem (local or jar) */
    static class FSChooser extends JFileChooser {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 4451076155975278278L;

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
 *  20   Gandalf   1.19        1/12/00  Ales Novak      i18n
 *  19   Gandalf   1.18        11/25/99 Jaroslav Tulach LocalFileSystem with 
 *       backup & JarFileSystem with filesystem.attributes.
 *  18   Gandalf   1.17        11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  17   Gandalf   1.16        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   Gandalf   1.15        10/6/99  Jaroslav Tulach #3948
 *  15   Gandalf   1.14        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  14   Gandalf   1.13        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  13   Gandalf   1.12        5/26/99  Ian Formanek    Actions cleanup
 *  12   Gandalf   1.11        5/26/99  Ian Formanek    Fixed obtaining bundle
 *  11   Gandalf   1.10        4/16/99  Libor Martinek  
 *  10   Gandalf   1.9         3/26/99  Jaroslav Tulach 
 *  9    Gandalf   1.8         3/19/99  Jaroslav Tulach TopManager.getDefault 
 *       ().getRegistry ()
 *  8    Gandalf   1.7         3/9/99   Jaroslav Tulach 
 *  7    Gandalf   1.6         3/5/99   Ales Novak      
 *  6    Gandalf   1.5         3/5/99   Ales Novak      
 *  5    Gandalf   1.4         2/11/99  Ian Formanek    Renamed FileSystemPool 
 *       -> Repository
 *  4    Gandalf   1.3         1/21/99  David Peroutka  
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    fixed resource names
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Fixed to compile under 
 *       JDK 1.2
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
