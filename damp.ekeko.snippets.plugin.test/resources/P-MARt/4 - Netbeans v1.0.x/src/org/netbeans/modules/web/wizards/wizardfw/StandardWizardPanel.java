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

package  org.netbeans.modules.web.wizards.wizardfw;

import org.netbeans.modules.web.util.*;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import javax.swing.event.*;

//NB import com.sun.jasper.wizardfw.netbeans.util.HelpCtx;
//NB import com.sun.jasper.wizardfw.netbeans.api.WizardDescriptor;

import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;


public class StandardWizardPanel extends DefaultWizardPanel {

    // javax.swing.JLabel topMsgLabel;
    //javax.swing.JTextArea topMsgLabel;
    MultiLineLabel topMsgLabel;

    javax.swing.JLabel bottomMsgLabel;
    //NNB WizardPicturePanel navPanel;
    JPanel topPane;
    public JPanel contentPane;

    public StandardWizardPanel() {
        layoutComponents();
    }

    public void layoutComponents() {
        this.setLayout(new BorderLayout(5,5));

        topPane = new JPanel(new BorderLayout(5,5));
        topPane.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));

        //NNB navPanel = new WizardPicturePanel();

        // topMsgLabel = new javax.swing.JLabel();

        //topMsgLabel = new javax.swing.JTextArea ();
        //topMsgLabel.setLineWrap (true);
        //topMsgLabel.setBackground ((java.awt.Color) javax.swing.UIManager.getDefaults ().get ("Label.background"));
        //topMsgLabel.setText ("This Wizard creates JSP Page");
        //topMsgLabel.setEditable (false);
        //topMsgLabel.setFont (new java.awt.Font ("SansSerif", 0, 11));

        topMsgLabel = new MultiLineLabel();

        bottomMsgLabel = new javax.swing.JLabel();

        contentPane = new JPanel();

        this.topPane.add(topMsgLabel,BorderLayout.NORTH);
        this.topPane.add(contentPane,BorderLayout.CENTER);
        this.topPane.add(bottomMsgLabel,BorderLayout.SOUTH);

        //NNB this.add(navPanel,BorderLayout.WEST);
        this.add(topPane,BorderLayout.CENTER);

        this.setTopMessage("");			 // NOI18N
        this.setNavigationImage("/com/sun/forte4j/web/wizards/jsppage/resources/images/surfing-duke.gif");		 // NOI18N
        this.setNavigationText("");		 // NOI18N
    }

    public void setTopMessage(String topMsg) {
        topMsgLabel.setText(topMsg);
    }

    public void setBottomMessage(String bottomMsg) {
        bottomMsgLabel.setText(bottomMsg);
    }

    public void setNavigationImage(String imageFile) {
        //NNB navPanel.setImage(imageFile);
    }

    public void setNavigationText(String navigationMsg) {
        //NNB navPanel.setText(navigationMsg);
    }
}