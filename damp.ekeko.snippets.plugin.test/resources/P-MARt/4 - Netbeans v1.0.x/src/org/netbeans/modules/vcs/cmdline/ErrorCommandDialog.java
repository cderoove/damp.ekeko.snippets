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
import org.netbeans.modules.vcs.util.*;
import org.openide.util.*;

/** Print the command error output.
 *
 * @author Martin Entlicher
 */

public class ErrorCommandDialog extends JDialog { //implements NoRegexListener {

    private Debug E=new Debug("ErrorCommandDialog",true); // NOI18N
    private Debug D=E;

    private JLabel      label;
    private JScrollPane listScrollPane;
    private JButton     closeButton;
    private JTextArea   textArea;
    //private DefaultListModel listData;

    private UserCommand uc=null;
    //private volatile Vector messages = null;
    private static String separator = "\n===========================================================\n"; // NOI18N

    //-------------------------------------------
    static final long serialVersionUID =2465240053029127192L;
    public ErrorCommandDialog(UserCommand uc,
                              Frame parent, boolean modal) {
        super (parent, modal);
        this.uc=uc;
        //messages = new Vector();
        initComponents ();
        //pack ();
        HelpCtx.setHelpIDString (getRootPane (), ErrorCommandDialog.class.getName ());
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
        if (uc == null) label.setText(g("CTL_Output_of_commands")); // NOI18N
        else label.setText( g("CTL_Output_of_the_command",uc.getLabel()) ); // NOI18N
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
        //list.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        //listData=new DefaultListModel();
        //list.setModel(listData);
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

        closeButton = new JButton ();
        closeButton.setText (g("CTL_CloseButtonLabel")); // NOI18N
        //closeButton.setLabel(g("CTL_CloseButtonLabel")); // NOI18N
        gridBagConstraints1 = new GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.insets = new Insets (0, 0, 5, 5);
        gridBagConstraints1.anchor = GridBagConstraints.EAST;
        gridBagConstraints1.weightx = 1.0;
        getContentPane ().add (closeButton, gridBagConstraints1);
        closeButton.addActionListener( new ActionListener(){
                                           public void actionPerformed(ActionEvent e){
                                               closeButtonPressed(e);
                                           };
                                       }
                                     );
    }


    //-------------------------------------------
    private void closeButtonPressed(ActionEvent e){
        closeDialog();
    }


    //-------------------------------------------
    private void closeDialog() {
        setVisible (false);
        //dispose ();
    }

    /**
     * Makes the last line visible.
     */
    private void scrollDown() {
        int height = textArea.getSize().height;
        listScrollPane.getViewport().setViewPosition(new Point(0, height));
    }

    //-------------------------------------------
    private void printMessage(String message){
        final String displayMessage=message;
        SwingUtilities.invokeLater( new Runnable() {
                                        public void run() {
                                            textArea.append(displayMessage+"\n");
                                            scrollDown();
                                            //textArea.validate();
                                            //int index=Math.max(0,listData.size()-1);
                                            //list.setSelectedIndex(index);
                                            //list.ensureIndexIsVisible(index);
                                            //list.validate();
                                        }
                                    });
    }

    public synchronized void putCommandOut(OutputContainer container) {
        if (container == null) return;
        final Vector messages = container.getMessages();
        SwingUtilities.invokeLater( new Runnable() {
                                        public void run() {
                                            Enumeration enum = messages.elements();
                                            while(enum.hasMoreElements()) {
                                                textArea.append(((String) enum.nextElement())+"\n");
                                            }
                                            textArea.append(separator+"\n");
                                            //int index=Math.max(0,listData.size()-1);
                                            //list.setSelectedIndex(index);
                                            //list.ensureIndexIsVisible(index);
                                            scrollDown();
                                            //textArea.validate();
                                            messages.removeAllElements();
                                        }
                                    });
    }

    //  public synchronized void removeCommandOut() {
    //    messages.removeAllElements();
    //  }

    //-------------------------------------------
    public void showDialog() {
        //D.deb("showDialog(): Message.size() = "+messages.size()); // NOI18N
        javax.swing.SwingUtilities.invokeLater(new Runnable () {
                                                   public void run () {
                                                       //if (messages.size() == 0) closeDialog();
                                                       //else {
                                                       pack();
                                                       show();
                                                       /*
                                                       Enumeration enum = messages.elements();
                                                       while(enum.hasMoreElements()) {
                                                       printMessage((String) enum.nextElement());
                                                   }
                                                   }
                                                       */
                                                   }
                                               });
    }

    //-------------------------------------------
    public void cancelDialog() {
        javax.swing.SwingUtilities.invokeLater(new Runnable () {
                                                   public void run () {
                                                       closeDialog();
                                                   }
                                               });
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
 *  10   Gandalf-post-FCS1.8.2.0     3/23/00  Martin Entlicher Change to user JTextArea
 *       instead of JList (=> it is possible to copy text and characters with 
 *       different encodings should display right).
 *  9    Gandalf   1.8         1/15/00  Ian Formanek    NOI18N
 *  8    Gandalf   1.7         1/11/00  Jesse Glick     Context help.
 *  7    Gandalf   1.6         1/6/00   Martin Entlicher 
 *  6    Gandalf   1.5         12/28/99 Martin Entlicher One error dialog for the
 *       whole session.  
 *  5    Gandalf   1.4         11/27/99 Patrik Knakal   
 *  4    Gandalf   1.3         10/25/99 Pavel Buzek     
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/5/99  Pavel Buzek     VCS at least can be 
 *       mounted
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
