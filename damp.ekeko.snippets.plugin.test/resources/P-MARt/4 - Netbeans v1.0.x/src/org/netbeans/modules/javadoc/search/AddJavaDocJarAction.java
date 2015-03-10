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

package org.netbeans.modules.javadoc.search;

import java.io.File;
import java.beans.PropertyVetoException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.filesystems.Repository;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.NbBundle;

/** The action that mounts new file system.
*
* @author Petr Hamernik
*/
public class AddJavaDocJarAction extends CallableSystemAction {
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
        return new org.openide.util.HelpCtx (AddJavaDocJarAction.class);
    }

    public String getName() {
        return getString("AddJar");
    }

    /** Gets localized string. */
    private static final String getString(String s) {
        return NbBundle.getBundle(AddJavaDocJarAction.class).getString(s);
    }

    /** Adds new JarFS. */
    public void performAction() {
        JFileChooser chooser = new AddJavaDocFSAction.FSChooser();

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
                    FileSystemCapability.Bean cap = new FileSystemCapability.Bean();
                    cap.setCompile( false );
                    cap.setExecute( false );
                    cap.setDebug( false );
                    cap.setDoc( true );
                    JarFileSystem jar = new JarFileSystem( cap );
                    jar.setHidden( true );
                    jar.setJarFile(f);
                    Repository r = TopManager.getDefault ().getRepository ();
                    if (r.findFileSystem(jar.getSystemName()) == null) {

                        // Test if the file system contains searchable docs
                        if( DocFileSystem.getDocFileObject(jar) == null ) {
                            NotifyDescriptor.Confirmation nd = new NotifyDescriptor.Confirmation(
                                                                   getString("MSG_NoIndexFiles"),
                                                                   getString("CTL_NoIndexFiles"),
                                                                   NotifyDescriptor.YES_NO_OPTION,
                                                                   NotifyDescriptor.WARNING_MESSAGE);

                            TopManager.getDefault().notify( nd );

                            if ( nd.getValue().equals( NotifyDescriptor.YES_OPTION ) ) {
                                r.addFileSystem (jar);
                            }
                        }
                        else {
                            r.addFileSystem (jar);
                        }
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
 *  3    Gandalf   1.2         1/12/00  Petr Hrebejk    i18n
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         8/13/99  Petr Hrebejk    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Formanek    icon
 */
