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
import java.awt.Dimension;
import javax.swing.JFileChooser;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.filesystems.Repository;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.NbBundle;

/** The action that mount new file system.
*
* @author Petr Hamernik (checked [PENDING HelpCtx])
*/
public class AddJavaDocFSAction extends CallableSystemAction {

    /** Remember last folder, which was explored in FileChooser of Mount-Dir action */
    private static File lastMountDirFolder = null;

    static final long serialVersionUID =4104439471362090001L;
    /** Icon of this action.
    * @return name of the action icon
    */
    public String iconResource() {
        //return "/org/netbeans/core/resources/actions/addDirectory.gif"; // NOI18N
        return null;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (AddJavaDocFSAction.class);
    }

    public String getName() {
        return NbBundle.getBundle(AddJavaDocFSAction.class).getString("AddFS");
    }

    /** Gets localized string. */
    private static final String getString(String s) {
        return NbBundle.getBundle(AddJavaDocFSAction.class).getString(s);
    }

    /** Adds a directory. */
    public void performAction() {
        JFileChooser chooser = new FSChooser();
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
                String dirName = f.getAbsolutePath();
                FileSystemCapability.Bean cap = new FileSystemCapability.Bean();
                cap.setCompile( false );
                cap.setExecute( false );
                cap.setDebug( false );
                cap.setDoc( true );
                LocalFileSystem localFS = new LocalFileSystem( cap );
                localFS.setHidden( true );
                try {
                    lastMountDirFolder = chooser.getCurrentDirectory();
                    localFS.setRootDirectory (new File (dirName));
                    Repository r = TopManager.getDefault ().getRepository ();
                    if (r.findFileSystem(localFS.getSystemName()) == null) {

                        // Test if the file system contains searchable docs

                        if( DocFileSystem.getDocFileObject( localFS ) == null ) {
                            NotifyDescriptor.Confirmation nd = new NotifyDescriptor.Confirmation(
                                                                   getString("MSG_NoIndexFiles"),
                                                                   getString("CTL_NoIndexFiles"),
                                                                   NotifyDescriptor.YES_NO_OPTION,
                                                                   NotifyDescriptor.WARNING_MESSAGE);

                            TopManager.getDefault().notify( nd );

                            if ( nd.getValue().equals( NotifyDescriptor.YES_OPTION ) ) {
                                r.addFileSystem (localFS);
                            }
                        }
                        else {
                            r.addFileSystem (localFS);
                        }
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
 *  4    Gandalf   1.3         1/13/00  Petr Hrebejk    i18n mk3  
 *  3    Gandalf   1.2         11/27/99 Patrik Knakal   
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         8/13/99  Petr Hrebejk    
 * $
 */
