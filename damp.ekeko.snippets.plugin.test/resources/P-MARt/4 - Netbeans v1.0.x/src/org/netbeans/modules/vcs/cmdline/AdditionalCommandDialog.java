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

package org.netbeans.modules.vcs.cmdline;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;

import org.netbeans.modules.vcs.cmdline.exec.*;
import org.netbeans.modules.vcs.cmdline.*;
import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.VcsFileSystem;
import org.openide.util.*;

/** Execute additional user defined command.
 *
 * @author Michal Fadljevic, Pavel Buzek
 */
public class AdditionalCommandDialog extends JDialog
    implements Runnable, RegexListener {

    private Debug E=new Debug("AdditionalCommandDialog",true); // NOI18N
    private Debug D=E;

    private JLabel      label;
    private JScrollPane listScrollPane;
    private JButton     stopButton;
    private JTextArea   textArea;

    private VcsFileSystem fileSystem=null;
    private UserCommand uc=null;
    private Hashtable vars=null;

    private boolean shouldStop=false;
    private Thread listUpdator = null;


    //-------------------------------------------
    static final long serialVersionUID =7828168693077944573L;
    public AdditionalCommandDialog(VcsFileSystem fileSystem,
                                   UserCommand uc,
                                   Hashtable vars,
                                   Frame parent, boolean modal) {
        super (parent, modal);
        this.fileSystem=fileSystem;
        this.uc=uc;
        this.vars=vars;
        initComponents ();
        pack ();
        HelpCtx.setHelpIDString (getRootPane (), AdditionalCommandDialog.class.getName ());
    }


    //-------------------------------------------
    private void initComponents () {
        setBackground (new Color (192, 192, 192));
        setTitle( g("CTL_Command_output")); // NOI18N
        addWindowListener (new WindowAdapter () {
                               public void windowClosing (WindowEvent evt) {
                                   closeDialog();
                               }
                           }
                          );
        getContentPane ().setLayout (new GridBagLayout ());
        GridBagConstraints gridBagConstraints1;

        label = new JLabel ();
        label.setText( g("CTL_Output_of_the_command",uc.getLabel()) ); // NOI18N
        gridBagConstraints1 = new GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.insets = new Insets (5, 5, 0, 0);
        gridBagConstraints1.anchor = GridBagConstraints.WEST;
        gridBagConstraints1.weightx = 0.2;
        gridBagConstraints1.weighty = 0.05;
        getContentPane ().add (label, gridBagConstraints1);

        listScrollPane = new JScrollPane ();
        listScrollPane.setPreferredSize (new Dimension(600, 400));
        //listScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        textArea = new JTextArea ();
        //list.setFont (new java.awt.Font ("Courier New", 0, 11)); // NOI18N
        textArea.setEditable(false);
        listScrollPane.add (textArea);

        listScrollPane.setViewportView (textArea);
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
        //stopButton.setText ("Stop"); // NOI18N
        stopButton.setText( g("CTL_StopButtonLabel") ); // NOI18N
        gridBagConstraints1 = new GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.insets = new Insets (0, 0, 5, 5);
        gridBagConstraints1.anchor = GridBagConstraints.EAST;
        gridBagConstraints1.weightx = 1.0;
        getContentPane ().add (stopButton, gridBagConstraints1);
        stopButton.addActionListener( new ActionListener(){
                                          public void actionPerformed(ActionEvent e){
                                              stopButtonPressed(e);
                                          };
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

    /**
     * Makes the last line visible.
     */
    private void scrollDown() {
        int height = textArea.getSize().height - listScrollPane.getSize().height;
        D.deb("scrollDown(): height = "+textArea.getSize().height+" - "+listScrollPane.getSize().height+" = "+height);
        if (height < 0) height = 0;
        listScrollPane.getViewport().setViewPosition(new Point(0, height));
    }

    //-------------------------------------------
    private void printMessage(String message){
        final String spaces [] = {" ", "  ", "   ", "    ", "     ", "      ", "       ", "        "}; // NOI18N
        String start;
        for (int i = message.indexOf('\t', 0); (i = message.indexOf('\t', i))>0; ) {
            start = message.substring(0, i);
            int index = 7 - (i-start.lastIndexOf('\n')-1)%8;
            message = start + spaces [index] + message.substring(i+1);
        }
        final String displayMessage=message;
        /*
        SwingUtilities.invokeLater( new Runnable() {
          public void run() {
        */
        textArea.append(displayMessage+"\n");
        if (listUpdator == null || !listUpdator.isAlive()) {
            listUpdator = new ListUpdator(textArea);
            listUpdator.start();
            listUpdator.yield();
        }
        /*
             }
           });
        */
    }

    class ListUpdator extends Thread {

        private JTextArea textArea;

        ListUpdator(JTextArea textArea) {
            this.textArea = textArea;
        }

        public void run () {
            try {
                SwingUtilities.invokeAndWait( new Runnable() {
                                                  public void run() {
                                                      scrollDown();
                                                      //textArea.validate();
                                                  }
                                              });
            } catch (InterruptedException e) {
                E.deb("List Updator Interrupted"); // NOI18N
            } catch (java.lang.reflect.InvocationTargetException e) {
                E.deb("List Updator throwed InvocationTargetException "+e.getMessage()); // NOI18N
            }
        }
    }


    //-------------------------------------------
    public void match(String[] elements){
        printMessage(MiscStuff.arrayToSpaceSeparatedString(elements) );
    }


    //-------------------------------------------
    public void run(){
        show();

        String message=g("MSG_Executing_additional_command", uc.getLabel()); // NOI18N
        fileSystem.debug(uc.getName()+": "+message); // NOI18N

        ExecuteCommand ec=new ExecuteCommand(fileSystem,uc,vars);
        ec.setOutputListener(this);
        ec.setErrorListener(this);
        ec.start();
        try{
            ec.join();
        }catch (InterruptedException e){
            E.err(e,"ec.join() interrupted"); // NOI18N
            shouldStop=true;
        }

        if(shouldStop){
            message=g("MSG_User_interrupt"); // NOI18N
            printMessage(">"+message); // NOI18N
            fileSystem.debug(uc.getName()+": "+message); // NOI18N
            return;
        }

        if( ec.getExitStatus()==0 ){
            message=g("MSG_Command_finished"); // NOI18N
        }
        else{
            message=g("MSG_Command_failed"); // NOI18N
        }
        stopButton.setText(g("CTL_CloseButtonLabel")); // NOI18N
        printMessage(">"+message); // NOI18N
        //fileSystem.debug(uc.getName()+": "+message); // NOI18N
        new ListUpdator(textArea).start();
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
 *  19   Gandalf-post-FCS1.16.2.1    3/29/00  Martin Entlicher Scrolling fixed.
 *  18   Gandalf-post-FCS1.16.2.0    3/23/00  Martin Entlicher JTextArea is used 
 *       instead of JList for good internationalization and possibilyty to copy 
 *       the text.
 *  17   Gandalf   1.16        2/10/00  Martin Entlicher 
 *  16   Gandalf   1.15        1/15/00  Ian Formanek    NOI18N
 *  15   Gandalf   1.14        1/11/00  Jesse Glick     Context help.
 *  14   Gandalf   1.13        1/6/00   Martin Entlicher 
 *  13   Gandalf   1.12        11/27/99 Patrik Knakal   
 *  12   Gandalf   1.11        11/24/99 Martin Entlicher Changed to use 
 *       VcsFileSystem instead of CvsFileSystem
 *  11   Gandalf   1.10        10/25/99 Pavel Buzek     
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         10/13/99 Pavel Buzek     
 *  8    Gandalf   1.7         9/30/99  Pavel Buzek     
 *  7    Gandalf   1.6         9/28/99  Martin Entlicher Improved listing of 
 *       command output to be much faster now
 *  6    Gandalf   1.5         9/8/99   Pavel Buzek     class model changed, 
 *       customization improved, several bugs fixed
 *  5    Gandalf   1.4         8/31/99  Pavel Buzek     
 *  4    Gandalf   1.3         8/31/99  Pavel Buzek     
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/1/99   Michal Fadljevic 
 *  1    Gandalf   1.0         5/27/99  Michal Fadljevic 
 * $
 */
