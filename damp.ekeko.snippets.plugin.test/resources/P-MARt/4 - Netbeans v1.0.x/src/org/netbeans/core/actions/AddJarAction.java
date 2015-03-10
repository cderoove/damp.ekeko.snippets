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
import java.beans.PropertyVetoException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.filesystems.Repository;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.NbBundle;

import org.netbeans.core.actions.AddFSAction;
import org.netbeans.core.ExJarFileSystem;

/** The action that mounts new file system.
*
* @author Petr Hamernik
*/
public class AddJarAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -3249779028704471940L;

    /** Remember last folder, which was explored in FileChooser of Mount-Jar action */
    private static File lastMountJarFolder = null;

    /** URL to this action.
    * @return URL to the action icon
    */
    public String iconResource() {
        return "/org/netbeans/core/resources/actions/addJarArchive.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx (AddJarAction.class);
    }

    public String getName() {
        return getString("AddJar");
    }

    /** Gets localized string. */
    private static final String getString(String s) {
        return NbBundle.getBundle(AddJarAction.class).getString(s);
    }

    /** Adds new JarFS. */
    public void performAction() {
        JFileChooser chooser = new AddFSAction.FSChooser();
        HelpCtx.setHelpIDString (chooser, getHelpCtx ().getHelpID ());

        chooser.setFileFilter(new FileFilter() {
                                  public boolean accept(File f) {
                                      return (f.isDirectory() || f.getName().endsWith(".jar") || f.getName().endsWith(".zip")); // NOI18N
                                  }
                                  public String getDescription() {
                                      return getString("CTL_JarArchivesMask");
                                  }
                              });

        if (lastMountJarFolder != null) {
            chooser.setCurrentDirectory(lastMountJarFolder);
        }

        chooser.setDialogTitle(getString("CTL_MountJar_Dialog_Title"));
        if (chooser.showDialog(TopManager.getDefault ().getWindowManager ().getMainWindow (),
                               getString("CTL_Mount_Approve_Button"))
                == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if ((f != null) && (f.isFile())) {
                try {
                    lastMountJarFolder = chooser.getCurrentDirectory();
                    JarFileSystem jar = new ExJarFileSystem();
                    jar.setJarFile(f);
                    Repository r = TopManager.getDefault ().getRepository ();
                    if (r.findFileSystem(jar.getSystemName()) == null) {
                        r.addFileSystem (jar);
                    }
                    else {
                        TopManager.getDefault().notify(
                            new NotifyDescriptor.Message(getString("MSG_JarFSAlreadyMounted"),
                                                         NotifyDescriptor.ERROR_MESSAGE)
                        );
                    }
                } catch (java.io.IOException e) {
                } catch (PropertyVetoException ex) {
                }
            }
        }
    }
}

/*
 * Log
 *  14   Gandalf   1.13        1/12/00  Ales Novak      i18n
 *  13   Gandalf   1.12        11/25/99 Jaroslav Tulach LocalFileSystem with 
 *       backup & JarFileSystem with filesystem.attributes.
 *  12   Gandalf   1.11        11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  11   Gandalf   1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  9    Gandalf   1.8         6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         3/26/99  Jaroslav Tulach 
 *  6    Gandalf   1.5         3/19/99  Jaroslav Tulach TopManager.getDefault 
 *       ().getRegistry ()
 *  5    Gandalf   1.4         3/5/99   Ales Novak      
 *  4    Gandalf   1.3         2/11/99  Ian Formanek    Renamed FileSystemPool 
 *       -> Repository
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    fixed resource names
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Fixed to compile under 
 *       JDK 1.2
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Formanek    icon
 */
