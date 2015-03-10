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

package org.netbeans.modules.vcs;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;

import org.netbeans.modules.vcs.cmdline.exec.*;
import org.netbeans.modules.vcs.util.*;
import org.openide.util.*;

/** Recursively refresh directories.
 *
 * @author Michal Fadljevic
 */
public class RetrievingDialog extends JDialog
    implements Runnable, DirReaderListener {

    private Debug E=new Debug("RetrievingDialog",true); // NOI18N
    private Debug D=E;

    private JLabel      retrievingLabel;
    private JScrollPane listScrollPane;
    private JButton     stopButton;
    private JList       list;
    private DefaultListModel listData;

    private VcsFileSystem fileSystem=null;

    /** Directory for which recursive refresh started,
        e.g. "" or "src/org/netbeans" 
    */
    private String rootPath=null;

    /** Queue of the directories to be processed.
     * @associates String
     */
    private Vector queue=new Vector(50);

    private boolean shouldStop=false;
    private boolean success = true;

    //-------------------------------------------
    static final long serialVersionUID =-6441709213287922213L;
    public RetrievingDialog(VcsFileSystem fileSystem, String rootPath,
                            Frame parent, boolean modal) {
        super (parent, modal);
        this.rootPath=rootPath;
        this.fileSystem=fileSystem;
        this.success = true;
        initComponents ();
        pack ();
        HelpCtx.setHelpIDString (getRootPane (), RetrievingDialog.class.getName ());
    }

    //-------------------------------------------
    private void initComponents () {
        setBackground (new Color (192, 192, 192));
        setTitle ( g("CTL_Retrieving") ); // NOI18N
        addWindowListener (new WindowAdapter () {
                               public void windowClosing (WindowEvent evt) {
                                   closeDialog();
                               }
                           }
                          );
        getContentPane ().setLayout (new GridBagLayout ());
        GridBagConstraints gridBagConstraints1;

        retrievingLabel = new JLabel ();
        retrievingLabel.setText (g("CTL_Retrieving_directories")); // NOI18N
        gridBagConstraints1 = new GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.insets = new Insets (5, 5, 0, 0);
        gridBagConstraints1.anchor = GridBagConstraints.WEST;
        gridBagConstraints1.weightx = 0.2;
        gridBagConstraints1.weighty = 0.05;
        getContentPane ().add (retrievingLabel, gridBagConstraints1);

        listScrollPane = new JScrollPane ();
        listScrollPane.setPreferredSize (new Dimension(400, 70));
        listScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        list = new JList ();
        list.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        listData=new DefaultListModel();
        list.setModel(listData);
        listScrollPane.add (list);

        listScrollPane.setViewportView (list);
        gridBagConstraints1 = new GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = 4;
        gridBagConstraints1.gridheight = 4;
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new Insets (5, 5, 5, 5);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 0.9;
        getContentPane ().add (listScrollPane, gridBagConstraints1);

        stopButton = new JButton ();
        stopButton.setText (g("CTL_StopButtonLabel")); // NOI18N
        //stopButton.setLabel (g("CTL_StopButtonLabel")); // NOI18N
        gridBagConstraints1 = new GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.insets = new Insets (0, 0, 5, 5);
        gridBagConstraints1.anchor = GridBagConstraints.EAST;
        gridBagConstraints1.weightx = 1.0;
        getContentPane ().add (stopButton, gridBagConstraints1);
        stopButton.addActionListener( new ActionListener(){
                                          public void actionPerformed(ActionEvent e) {
                                              stopButtonPressed(e);
                                          }
                                      }
                                    );
    }


    //-------------------------------------------
    private void stopButtonPressed(ActionEvent e){
        closeDialog();
    }


    //-------------------------------------------
    private void closeDialog() {
        shouldStop=true ;
        setVisible (false);
        dispose ();
    }


    //-------------------------------------------
    public void readDirFinished(VcsDir dir, Vector rawData, boolean success){
        String[] sub=dir.getSubdirs();
        for(int i=0;i<sub.length;i++){
            D.deb("readDirFinished for "+dir.getName());
            String d=dir.getPath ()+"/"+sub[i]; // NOI18N
            if( d.startsWith("/") ){ // NOI18N
                d=d.substring(1);
            }
            D.deb("adding '"+d+"' to queue");
            if (success) queue.addElement(d);
            else {
                queue.removeAllElements();
                this.success = false;
            }
        }
        if (!success) this.success = success;

        //D.deb("forwarding -> readDirFinished("+dir.name+",...)"); // NOI18N
        fileSystem.getCache().readDirFinished(dir,rawData, success);
    }

    public void readDirFinishedRecursive(VcsDir dir, VcsDirContainer rawData, boolean success) {
        // an empty method
    }


    //-------------------------------------------
    private void printMessage(String message){
        final String displayMessage=message;
        SwingUtilities.invokeLater( new Runnable() {
                                        public void run() {
                                            listData.addElement(displayMessage);
                                            int index=Math.max(0,listData.size()-1);
                                            list.setSelectedIndex(index);
                                            list.ensureIndexIsVisible(index);
                                            list.validate();
                                        }
                                    });
    }


    //-------------------------------------------
    public void run(){
        String message=null;

        queue.addElement(rootPath);
        show();
        boolean cancel = false;

        while( queue.isEmpty()==false ){
            String path=(String)queue.remove(0);

            D.deb("Retrieving recursively for path = "+path);
            fileSystem.debug(g("MSG_Recursively_retrieving_directory",path)); // NOI18N
            String rootFolderLabel=g("MSG_Root_folder"); // NOI18N
            message=(path.equals("")?rootFolderLabel:path); // NOI18N
            printMessage(message);

            VcsDirReader reader= fileSystem.getVcsFactory ().getVcsDirReader (this,path,fileSystem);
            if (reader == null) {
                fileSystem.debug(fileSystem.getBundleProperty("MSG_CommandCanceled")); // NOI18N
                queue.removeAllElements();
                cancel = true;
                continue;
            }
            Thread th = new Thread (reader);
            th.start();
            try{
                th.join();
            }catch (InterruptedException e){
                E.err(e,"reader.join() interrupted"); // NOI18N
                shouldStop=true ;
            }

            if(shouldStop){
                message=g("MSG_Recursive_retrieving_interrupted_by_the_user"); // NOI18N
                printMessage(message);
                fileSystem.debug(message);
                return;
            }

        }

        stopButton.setText(g("CTL_CloseButtonLabel")); // NOI18N
        if (cancel) message=g("MSG_Recursive_retrieving_canceled"); // NOI18N
        else {
            if (success) message=g("MSG_Subtree_successfully_retrieved"); // NOI18N
            else message=g("MSG_Recursive_retrieving_failed"); // NOI18N
        }
        printMessage(message);
        fileSystem.debug(message);
    }


    //-------------------------------------------
    String g(String s) {
        return NbBundle.getBundle
               ("org.netbeans.modules.vcs.cmdline.Bundle").getString (s);
    }
    String  g(String s, Object obj) {
        return MessageFormat.format (g(s), new Object[] { obj });
    }
    String g(String s, Object obj1, Object obj2) {
        return MessageFormat.format (g(s), new Object[] { obj1, obj2 });
    }
    String g(String s, Object obj1, Object obj2, Object obj3) {
        return MessageFormat.format (g(s), new Object[] { obj1, obj2, obj3 });
    }
    //-------------------------------------------

}

/*
 * Log
 *  12   Gandalf-post-FCS1.10.2.0    3/23/00  Martin Entlicher 
 *  11   Gandalf   1.10        2/8/00   Martin Entlicher Fix for recursive 
 *       retrieving.
 *  10   Gandalf   1.9         1/15/00  Ian Formanek    NOI18N
 *  9    Gandalf   1.8         1/11/00  Jesse Glick     Context help.
 *  8    Gandalf   1.7         1/6/00   Martin Entlicher 
 *  7    Gandalf   1.6         12/28/99 Martin Entlicher 
 *  6    Gandalf   1.5         12/21/99 Martin Entlicher Recursive retrieving is 
 *       canceled when the command fails.
 *  5    Gandalf   1.4         11/27/99 Patrik Knakal   
 *  4    Gandalf   1.3         10/25/99 Pavel Buzek     copyright and log
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/5/99  Pavel Buzek     VCS at least can be 
 *       mounted
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
