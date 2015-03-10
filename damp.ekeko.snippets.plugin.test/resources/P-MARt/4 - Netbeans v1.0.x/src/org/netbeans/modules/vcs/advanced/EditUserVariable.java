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

import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.*;
import org.openide.util.*;

/** Edit single user variable.
 * 
 * @author Michal Fadljevic
 */
//-------------------------------------------
class EditUserVariable extends JDialog {
    private Debug E=new Debug("EditUserVariable", false); // NOI18N
    private Debug D=E;

    private VcsConfigVariable var = null;

    private JTextField nameField = null;
    private JTextField labelField = null;
    private JTextField valueField = null;
    private JTextField selectorField = null;
    private JCheckBox basicCheckBox = null;
    private JCheckBox localFileCheckBox = null;
    private JCheckBox localDirCheckBox = null;
    private JButton cancelButton = null;
    private JButton okButton = null;

    private boolean cancelled = true;


    //-------------------------------------------
    static final long serialVersionUID =-8779036597145328094L;
    public EditUserVariable(Frame owner, VcsConfigVariable var){
        super(owner,"",true); // NOI18N
        setTitle(g("CTL_Edit_variable")); // NOI18N
        this.var=var;
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
        Container content = getContentPane();

        JLabel nameLabel = new JLabel(g("CTL_Variable_name"),      SwingConstants.RIGHT); // NOI18N
        JLabel labelLabel = new JLabel(g("CTL_Variable_label"),    SwingConstants.RIGHT); // NOI18N
        JLabel basicLabel = new JLabel(g("CTL_Variable_basic"),    SwingConstants.RIGHT); // NOI18N
        JLabel valueLabel = new JLabel(g("CTL_Variable_value"),    SwingConstants.RIGHT); // NOI18N
        JLabel localFileLabel = new JLabel(g("CTL_Variable_localFile"), SwingConstants.RIGHT); // NOI18N
        JLabel localDirLabel = new JLabel(g("CTL_Variable_localDir"),   SwingConstants.RIGHT); // NOI18N
        JLabel selectorLabel = new JLabel(g("CTL_Variable_selector"),   SwingConstants.RIGHT); // NOI18N

        nameField = new JTextField(var.getName(),8);
        labelField = new JTextField(var.getLabel(),10);
        valueField = new JTextField(var.getValue(),40);
        selectorField = new JTextField(var.getCustomSelector(), 40);
        basicCheckBox = new JCheckBox("", var.isBasic ()); // NOI18N
        localFileCheckBox = new JCheckBox("", var.isLocalFile()); // NOI18N
        localDirCheckBox = new JCheckBox("", var.isLocalDir()); // NOI18N

        GridBagLayout layout=new GridBagLayout();
        content.setLayout(layout);
        int y=0;
        createEntry(content, layout, y++, nameLabel,       nameField);
        createEntry(content, layout, y++, labelLabel,      labelField);
        createEntry(content, layout, y++, basicLabel,      basicCheckBox);
        createEntry(content, layout, y++, valueLabel,      valueField);
        createEntry(content, layout, y++, localFileLabel,  localFileCheckBox);
        createEntry(content, layout, y++, localDirLabel,   localDirCheckBox);
        createEntry(content, layout, y++, selectorLabel,   selectorField);

        okButton = new JButton(g("CTL_OK")); // NOI18N
        cancelButton = new JButton(g("CTL_Cancel")); // NOI18N

        GridBagConstraints c = new GridBagConstraints();
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

        nameField.addActionListener(new ActionListener() {
                                        public void actionPerformed(ActionEvent e){
                                            ok();
                                        }
                                    });

        valueField.addActionListener(new ActionListener() {
                                         public void actionPerformed(ActionEvent e){
                                             ok();
                                         }
                                     });

        selectorField.addActionListener(new ActionListener() {
                                            public void actionPerformed(ActionEvent e){
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

    }


    //-------------------------------------------
    private void cancel(){
        //D.deb("cancel()"); // NOI18N
        cancelled=true;
        hide();
    }

    //-------------------------------------------
    private void ok(){
        //D.deb("ok() nameField="+nameField.getText()+", valueField="+valueField.getText()); // NOI18N
        var.setName (nameField.getText());
        var.setValue (valueField.getText());
        var.setLabel (labelField.getText ());
        var.setBasic (basicCheckBox.isSelected ());
        var.setLocalFile (localFileCheckBox.isSelected());
        var.setLocalDir (localDirCheckBox.isSelected());
        var.setCustomSelector(selectorField.getText());
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
 *  4    Gandalf-post-FCS1.2.2.0     3/23/00  Martin Entlicher Selector Field added.
 *  3    Gandalf   1.2         1/27/00  Martin Entlicher NOI18N
 *  2    Gandalf   1.1         11/27/99 Patrik Knakal   
 *  1    Gandalf   1.0         11/24/99 Martin Entlicher 
 * $
 */
