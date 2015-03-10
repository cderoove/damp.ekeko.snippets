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

package org.netbeans.modules.vcs.advanced;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.text.*;

import org.openide.util.*;

import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.cmdline.*;

/** Edit single user command.
 * 
 * @author Michal Fadljevic
 */
//-------------------------------------------
class EditUserCommand extends JDialog {
    private Debug E=new Debug("EditUserCommand", false); // NOI18N
    private Debug D=E;

    private UserCommand command = null;

    private JTextField nameField = null;
    private JTextField labelField = null;
    private JTextField execField = null;
    private JTextField inputField = null;
    private JTextField timeoutField = null;
    private JTextField dataRegexField = null;
    private JTextField errorRegexField = null;

    private JTextField statusField = null;
    private JTextField lockerField = null;
    private JTextField attrField = null;
    private JTextField dateField = null;
    private JTextField sizeField = null;
    private JTextField fileNameField = null;
    private JTextField confirmMsgField = null;

    private JCheckBox displayOutputCheckBox = null;
    private JCheckBox doRefreshCheckBox = null;
    private JCheckBox onFileCheckBox = null;
    private JCheckBox onDirCheckBox = null;
    private JCheckBox onRootCheckBox = null;

    private JButton cancelButton = null;
    private JButton okButton = null;

    private boolean cancelled=true;

    //-------------------------------------------
    static final long serialVersionUID =-4222907664360427488L;
    public EditUserCommand(Frame owner, UserCommand command){
        super(owner,"",true); // NOI18N
        setTitle( g("CTL_Edit_command") ); // NOI18N
        this.command=command;
        initComponents();
        initListeners();
    }

    //-------------------------------------------
    private void createEntry(Container panel, GridBagLayout layout, int gridy,
                             Component c1, Component c2){
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=0;
        c.gridy=gridy;
        //    c.weightx=0.2;
        c.insets=new Insets(2,7,2,7);
        c.anchor=GridBagConstraints.NORTHWEST;
        c.fill=GridBagConstraints.NONE;
        layout.setConstraints(c1,c);
        panel.add(c1);

        c.fill=GridBagConstraints.HORIZONTAL;
        c.gridx=1;
        c.weightx=0.8;
        layout.setConstraints(c2,c);
        panel.add(c2);
    }


    //-------------------------------------------
    private void initComponents(){
        Container content=getContentPane();

        JLabel nameLabel = new JLabel(g("CTL_Command_name"),      SwingConstants.RIGHT); // NOI18N
        JLabel labelLabel = new JLabel(g("CTL_Command_label"),    SwingConstants.RIGHT); // NOI18N
        JLabel execLabel = new JLabel(g("CTL_Execute"),           SwingConstants.RIGHT); // NOI18N
        JLabel inputLabel = new JLabel(g("CTL_Input"),            SwingConstants.RIGHT); // NOI18N
        JLabel timeoutLabel = new JLabel(g("CTL_Timeout"),        SwingConstants.RIGHT); // NOI18N
        JLabel dataRegexLabel = new JLabel(g("CTL_Data_regex"),   SwingConstants.RIGHT); // NOI18N
        JLabel errorRegexLabel = new JLabel(g("CTL_Error_regex"), SwingConstants.RIGHT); // NOI18N
        JLabel displayOuputLabel = new JLabel(g("CTL_Display_Output"), SwingConstants.RIGHT); // NOI18N
        JLabel doRefreshLabel = new JLabel(g("CTL_Do_Refresh"),   SwingConstants.RIGHT); // NOI18N
        JLabel onFileLabel = new JLabel(g("CTL_On_File"), SwingConstants.RIGHT); // NOI18N
        JLabel onDirLabel = new JLabel(g("CTL_On_Dir"), SwingConstants.RIGHT); // NOI18N
        JLabel onRootLabel = new JLabel(g("CTL_On_Root"), SwingConstants.RIGHT); // NOI18N
        JLabel confirmMsgLabel = new JLabel(g("CTL_Confirm_Msg"), SwingConstants.RIGHT); // NOI18N

        nameField = new JTextField(command.getName(),8);
        labelField = new JTextField(command.getLabel(),10);
        execField = new JTextField(command.getExec(),60);
        inputField = new JTextField(command.getInput(),60);
        timeoutField = new JTextField(""+command.getTimeout(),5); // NOI18N
        dataRegexField = new JTextField(command.getDataRegex(),60);
        errorRegexField = new JTextField(command.getErrorRegex(),60);
        confirmMsgField = new JTextField(command.getConfirmationMsg(),60);

        displayOutputCheckBox = new JCheckBox("", command.getDisplayOutput()); // NOI18N
        doRefreshCheckBox = new JCheckBox("", command.getDoRefresh()); // NOI18N
        onFileCheckBox = new JCheckBox("", command.getOnFile()); // NOI18N
        onDirCheckBox = new JCheckBox("", command.getOnDir()); // NOI18N
        onRootCheckBox = new JCheckBox("", command.getOnRoot()); // NOI18N

        GridBagLayout layout=new GridBagLayout();
        content.setLayout(layout);
        int y=0;
        createEntry(content, layout, y++, nameLabel,       nameField );
        createEntry(content, layout, y++, labelLabel,      labelField );
        createEntry(content, layout, y++, execLabel,       execField );
        createEntry(content, layout, y++, inputLabel,      inputField );
        createEntry(content, layout, y++, timeoutLabel,    timeoutField );
        createEntry(content, layout, y++, dataRegexLabel,  dataRegexField );
        createEntry(content, layout, y++, errorRegexLabel, errorRegexField );
        createEntry(content, layout, y++, confirmMsgLabel, confirmMsgField);
        createEntry(content, layout, y++, displayOuputLabel, displayOutputCheckBox );

        if (command.getName().equals("LIST") || command.getName().equals("LIST_SUB")) { // NOI18N
            JLabel statusLabel=new JLabel(g("CTL_List_Status_group"),      SwingConstants.RIGHT); // NOI18N
            JLabel lockerLabel=new JLabel(g("CTL_List_Locker_group"),      SwingConstants.RIGHT); // NOI18N
            JLabel attrLabel=new JLabel(g("CTL_List_Attribute_group"),     SwingConstants.RIGHT); // NOI18N
            JLabel dateLabel=new JLabel(g("CTL_List_Date_group"),          SwingConstants.RIGHT); // NOI18N
            JLabel sizeLabel=new JLabel(g("CTL_List_Size_group"),          SwingConstants.RIGHT); // NOI18N
            JLabel fileNameLabel=new JLabel(g("CTL_List_File_name_group"), SwingConstants.RIGHT); // NOI18N

            statusField=new JTextField(""+command.getStatus(),3); // NOI18N
            lockerField=new JTextField(""+command.getLocker(),3); // NOI18N
            attrField=new JTextField(""+command.getAttr(),3); // NOI18N
            dateField=new JTextField(""+command.getDate(),3); // NOI18N
            sizeField=new JTextField(""+command.getSize(),3); // NOI18N
            fileNameField=new JTextField(""+command.getFileName(),3); // NOI18N

            createEntry(content, layout, y++, statusLabel,    statusField );
            createEntry(content, layout, y++, lockerLabel,    lockerField );
            createEntry(content, layout, y++, attrLabel,      attrField );
            createEntry(content, layout, y++, dateLabel,      dateField );
            createEntry(content, layout, y++, sizeLabel,      sizeField );
            createEntry(content, layout, y++, fileNameLabel,  fileNameField );
        } else {
            createEntry(content, layout, y++, doRefreshLabel,  doRefreshCheckBox );
        }
        createEntry(content, layout, y++, onFileLabel,  onFileCheckBox );
        createEntry(content, layout, y++, onDirLabel,  onDirCheckBox );
        createEntry(content, layout, y++, onRootLabel,  onRootCheckBox );

        okButton=new JButton(g("CTL_OK")); // NOI18N
        cancelButton=new JButton(g("CTL_Cancel")); // NOI18N

        GridBagConstraints c=new GridBagConstraints();
        c.gridx=0;
        c.gridy=y;
        c.gridwidth=3;
        c.weightx=0.0;
        c.weightx=1.0;
        c.fill=GridBagConstraints.NONE;
        c.anchor=GridBagConstraints.EAST;
        JPanel p=new JPanel(new FlowLayout());
        p.add(okButton);
        p.add(cancelButton);
        content.add(p,c);

        nameField.selectAll();
        pack();
    }

    //-------------------------------------------
    private void initListeners(){

        addWindowListener(new WindowAdapter() {
                              public void windowActivated(WindowEvent e){
                                  //D.deb("windowActivated e="+e); // NOI18N
                                  nameField.requestFocus();
                              }
                              public void windowOpened(WindowEvent e){
                                  //D.deb("windowOpened e="+e); // NOI18N
                                  nameField.requestFocus();
                              }
                          });

        cancelButton.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e){
                                               //D.deb("Cancel pressed"); // NOI18N
                                               cancel();
                                           }
                                       });

        okButton.addActionListener(new ActionListener() {
                                       public void actionPerformed(ActionEvent e){
                                           //D.deb("OK pressed"); // NOI18N
                                           ok();
                                       }
                                   });

        getRootPane().registerKeyboardAction(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    cancel();
                }
            },
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0, true),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        /*
            getRootPane().registerKeyboardAction(
              new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
        	  ok ();
                }
              },
              KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0, true),
              JComponent.WHEN_IN_FOCUSED_WINDOW
            );      
        */
    }

    //-------------------------------------------
    private void cancel(){
        // D.deb("cancel()"); // NOI18N
        cancelled=true;
        hide();
    }

    //-------------------------------------------
    private int safeStringToInt(String s, int def){
        int result=def;
        s=s.trim();
        try{
            result=Integer.parseInt(s);
        }
        catch(NumberFormatException e){
            E.err(e,"safeStringToInt("+s+")");
        }
        return result;
    }

    //-------------------------------------------
    private int positionToInt(String s){
        int result=safeStringToInt(s,-1);
        result = ( result < -1 ? -1 : result );
        return result;
    }

    //-------------------------------------------
    private int timeoutToInt(String s){
        int result=safeStringToInt(s,-1);
        result = ( result < 5000 ? 5000 : result );
        return result;
    }

    //-------------------------------------------
    private void ok(){
        command.setName(       nameField.getText()  );
        command.setLabel(      labelField.getText() );
        command.setExec(       execField.getText()  );
        command.setInput(      inputField.getText()  );
        command.setTimeout(    timeoutToInt(timeoutField.getText()) );
        command.setDataRegex(  dataRegexField.getText()  );
        command.setErrorRegex( errorRegexField.getText() );
        command.setDisplayOutput(displayOutputCheckBox.isSelected());
        command.setConfirmationMsg(confirmMsgField.getText());

        if(command.getName().equals("LIST") || command.getName().equals("LIST_SUB")) { // NOI18N
            command.setStatus(   positionToInt( statusField.getText()   ));
            command.setLocker(   positionToInt( lockerField.getText()   ));
            command.setAttr(     positionToInt( attrField.getText()     ));
            command.setDate(     positionToInt( dateField.getText()     ));
            command.setSize(     positionToInt( sizeField.getText()     ));
            command.setFileName( positionToInt( fileNameField.getText() ));
        } else {
            command.setDoRefresh(doRefreshCheckBox.isSelected());
        }
        command.setOnFile(onFileCheckBox.isSelected());
        command.setOnDir(onDirCheckBox.isSelected());
        command.setOnRoot(onRootCheckBox.isSelected());
        cancelled=false;
        hide();
    }

    //-------------------------------------------
    public boolean wasCancelled(){
        return cancelled;
    }


    //-------------------------------------------
    String g(String s) {
        return NbBundle.getBundle
               ("org.netbeans.modules.vcs.advanced.Bundle").getString (s);
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
 * <<Log>>
 *  21   Gandalf-post-FCS1.19.2.0    3/23/00  Martin Entlicher Confirmation message 
 *       added, property onRoot added, and LIST_SUB has the same properties as 
 *       LIST.
 *  20   Gandalf   1.19        2/10/00  Martin Entlicher onFile and onDir 
 *       properties
 *  19   Gandalf   1.18        1/27/00  Martin Entlicher NOI18N
 *  18   Gandalf   1.17        11/27/99 Patrik Knakal   
 *  17   Gandalf   1.16        11/24/99 Martin Entlicher Added displayOutput  and
 *       doRefresh property.
 *  16   Gandalf   1.15        10/25/99 Pavel Buzek     copyright
 *  15   Gandalf   1.14        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   Gandalf   1.13        9/30/99  Pavel Buzek     
 *  13   Gandalf   1.12        9/8/99   Pavel Buzek     class model changed, 
 *       customization improved, several bugs fixed
 *  12   Gandalf   1.11        8/31/99  Pavel Buzek     
 *  11   Gandalf   1.10        8/31/99  Pavel Buzek     
 *  10   Gandalf   1.9         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         5/27/99  Michal Fadljevic 
 *  8    Gandalf   1.7         5/24/99  Michal Fadljevic 
 *  7    Gandalf   1.6         5/14/99  Michal Fadljevic 
 *  6    Gandalf   1.5         5/4/99   Michal Fadljevic 
 *  5    Gandalf   1.4         5/4/99   Michal Fadljevic 
 *  4    Gandalf   1.3         4/26/99  Michal Fadljevic 
 *  3    Gandalf   1.2         4/22/99  Michal Fadljevic 
 *  2    Gandalf   1.1         4/22/99  Michal Fadljevic 
 *  1    Gandalf   1.0         4/21/99  Michal Fadljevic 
 * $
 */
