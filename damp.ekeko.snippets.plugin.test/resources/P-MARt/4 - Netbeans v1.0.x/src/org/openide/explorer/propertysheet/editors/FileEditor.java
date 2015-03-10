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

package org.openide.explorer.propertysheet.editors;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import javax.swing.JFileChooser;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**  Property editor for java.io.File. It can be configured to look for file or directories
* or both. Default constructor scans for both.
*
* @author Jaroslav Tulach
* @version 0.10
*/
public class FileEditor extends PropertyEditorSupport {
    /** mode to look for */
    private int mode = JFileChooser.FILES_AND_DIRECTORIES;

    /** The resource bundle for explorer */
    private static java.util.ResourceBundle bundle =
        NbBundle.getBundle(FileEditor.class);

    /** Editor that accepts files or directories */
    public FileEditor() {
    }

    /** Editor in special mode. It can be either JFileChooser.FILES_ONLY,
    * JFileChooser.DIRECTORIES_ONLY or JFileChooser.FILES_AND_DIRECTORIES.
    */
    public FileEditor(int mode) {
        this.mode = mode;
    }

    /** sets new value */
    public void setAsText(String s) {
        setValue(new File (s));
    }

    /** gets string value */
    public String getAsText () {
        Object obj = getValue ();
        if (obj instanceof File) return obj.toString ();
        return null;
    }

    /**
    * @return always true we support the editor
    */
    public boolean supportsCustomEditor () {
        return true;
    }

    /** @return file chooser in dialog
    */
    public Component getCustomEditor() {
        final JFileChooser chooser = createFileChooser ();

        final javax.swing.JDialog dialog = new javax.swing.JDialog (
                                               TopManager.getDefault ().getWindowManager ().getMainWindow (), chooser.getDialogTitle (), true
                                           );

        // attach cancel also to Escape key
        dialog.getRootPane().registerKeyboardAction(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    dialog.setVisible (false);
                    dialog.dispose ();
                }
            },
            javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0, true),
            javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        dialog.addKeyListener (new java.awt.event.KeyAdapter () {
                                   public void keyPressed (java.awt.event.KeyEvent evt) {
                                       if (evt.getKeyCode () == java.awt.event.KeyEvent.VK_ESCAPE) {
                                           dialog.setVisible (false);
                                           dialog.dispose ();
                                       }
                                   }
                               }
                              );

        dialog.getContentPane ().setLayout (new java.awt.BorderLayout ());
        dialog.getContentPane ().add (chooser, java.awt.BorderLayout.CENTER);
        chooser.addActionListener (new ActionListener () {
                                       public void actionPerformed (ActionEvent evt) {
                                           if (JFileChooser.APPROVE_SELECTION.equals (evt.getActionCommand ())) {
                                               File f = chooser.getSelectedFile ();
                                               setValue (f);
                                               dialog.setVisible (false);
                                               dialog.dispose ();
                                           } else if (JFileChooser.CANCEL_SELECTION.equals (evt.getActionCommand ())) {
                                               dialog.setVisible (false);
                                               dialog.dispose ();
                                           }
                                       }
                                   }
                                  );

        HelpCtx.setHelpIDString (dialog.getRootPane (), getHelpCtx ().getHelpID ());
        return dialog;
    }

    /** Java initialization string.
    */
    public String getJavaInitializationString () {
        Object value = getValue ();
        if (value == null) {
            return "null"; // NOI18N
        } else {
            return "new File (" + getValue () + ")"; // NOI18N
        }
    }

    /** Allows subclasses to modify the chooser to suit their needs.
    */
    protected JFileChooser createFileChooser () {
        File originalFile = (File)getValue ();

        final JFileChooser chooser = new JFileChooser ();
        chooser.setFileSelectionMode(mode);
        if (originalFile != null && originalFile.getParent () != null)
            chooser.setCurrentDirectory (new File (originalFile.getParent ()));
        chooser.setSelectedFile (originalFile);
        chooser.setApproveButtonText (bundle.getString ("CTL_ApproveSelect"));
        chooser.setApproveButtonToolTipText (bundle.getString ("CTL_ApproveSelectToolTip"));
        switch (mode) {
        case JFileChooser.FILES_AND_DIRECTORIES:
            chooser.setDialogTitle (bundle.getString ("CTL_DialogTitleFilesAndDirs"));
            break;
        case JFileChooser.FILES_ONLY:
            chooser.setDialogTitle (bundle.getString ("CTL_DialogTitleFiles"));
            break;
        case JFileChooser.DIRECTORIES_ONLY:
            chooser.setDialogTitle (bundle.getString ("CTL_DialogTitleDirs"));
            break;
        }

        return chooser;
    }

    /** Permits subclasses to associate a help context with the whole chooser.
    * @return suitable context help
    */
    protected HelpCtx getHelpCtx () {
        return new HelpCtx (FileEditor.class);
    }

}

/*
* Log
*  10   Gandalf   1.9         1/12/00  Ian Formanek    NOI18N
*  9    Gandalf   1.8         11/5/99  Jesse Glick     Configurable context 
*       help, and setting dialog title correctly.
*  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    Gandalf   1.6         9/20/99  Jaroslav Tulach 3165
*  6    Gandalf   1.5         6/30/99  Ian Formanek    Moved to package 
*       org.openide.explorer.propertysheet.editors
*  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    Gandalf   1.3         3/22/99  Jaroslav Tulach JAR FS
*  3    Gandalf   1.2         3/4/99   Jan Jancura     bundle moved
*  2    Gandalf   1.1         1/6/99   Jaroslav Tulach 
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
