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

package  org.netbeans.modules.web.wizards.beanjsp.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;

import java.awt.*;
import java.awt.event.*;

import org.netbeans.modules.web.wizards.beanjsp.model.*;
import org.netbeans.modules.web.wizards.wizardfw.*;
import org.netbeans.modules.web.wizards.beanjsp.ide.netbeans.*;

import org.netbeans.modules.web.util.*;
import org.netbeans.modules.web.wizards.beanjsp.util.*;

import org.openide.util.*;

public class JSPSubmitAndErrorURLInfoPanel extends StandardWizardPanel {

    // ---------------------------------------------------------------------------------------
    // WizardPanel initialization

    /** Creates new BeanTypePanel */

    public JSPSubmitAndErrorURLInfoPanel() {
        this(JSPPage.IO_PAGE);
    }

    public JSPSubmitAndErrorURLInfoPanel(int pageType) {
        super();
        this.pageType = pageType;
        initComponents ();
    }

    /*public HelpCtx getHelp () {
      return new HelpCtx (JSPSubmitAndErrorURLInfoPanel.class);
}*/

    private void initComponents () {

        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);

        standardPageMsg = resBundle.getString("JBW_StandardPageInfoMsg");				 // NOI18N
        errorPageMsg = resBundle.getString("JBW_ErrorPageInfoMsg");					 // NOI18N


        this.setTopMessage(errorPageMsg);

        //// create components

        submitOptionMsgLabel = new MultiLineLabel(resBundle.getString("JBW_JSPSubmitOptionMsgLabel"));		 // NOI18N
        submitCheckB = new javax.swing.JCheckBox(resBundle.getString("JBW_JSPPageSubmitOptionLabel"));		 // NOI18N

        submitURLLabel = new JLabel(resBundle.getString("JBW_JSPSubmitURLLabel")); 			 // NOI18N
        submitURLTF = new JTextField(30);
        // submitURLTF.setEditable(false);
        // submitURLTF.setBackground (java.awt.Color.lightGray);
        submitURLBrowseB = new JButton(resBundle.getString("JBW_JSPSubmitURLBrowseBLabel"));		 // NOI18N
        // netbeans convention
        submitURLBrowseB.setFont (new java.awt.Font ("SansSerif", 0, 11));						 // NOI18N
        submitURLBrowseB.setMinimumSize (new java.awt.Dimension(85, 15));
        submitURLBrowseB.setMaximumSize (new java.awt.Dimension(85, 27));
        submitURLBrowseB.setPreferredSize (new java.awt.Dimension(85, 25));

        submitButtonNameLabel = new JLabel(resBundle.getString("JBW_JSPSubmitButtonNameLabel")); 		 // NOI18N
        submitButtonNameTF = new JTextField(30);


        errorPageURLMsgLabel = new MultiLineLabel(resBundle.getString("JBW_JSPErrorPageURLMsgLabel"));	 // NOI18N

        errorPageURLLabel = new JLabel(resBundle.getString("JBW_JSPErrorPageURLLabel"));	  				 // NOI18N
        errorPageURLTF = new JTextField(30);
        // errorPageURLTF.setEnabled(false);
        // errorPageURLTF.setEditable(false);
        // errorPageURLTF.setBackground (java.awt.Color.lightGray);
        errorPageURLBrowseB = new JButton(resBundle.getString("JBW_JSPErrorPageURLBrowseBLabel"));		 // NOI18N
        errorPageURLBrowseB.setFont (new java.awt.Font ("SansSerif", 0, 11));						 // NOI18N
        errorPageURLBrowseB.setMinimumSize (new java.awt.Dimension(85, 15));
        errorPageURLBrowseB.setMaximumSize (new java.awt.Dimension(85, 27));
        errorPageURLBrowseB.setPreferredSize (new java.awt.Dimension(85, 25));




        //// layout components
        arrangeComponents();

        //// setup models

        this.setDefaults();


        errorPageURLBrowseB.addActionListener(new ActionListener() {
                                                  public void actionPerformed(ActionEvent e) {
                                                      doErrorPageBrowse();
                                                  }
                                              });

        submitURLBrowseB.addActionListener(new ActionListener() {
                                               public void actionPerformed(ActionEvent e) {
                                                   doSubmitURLBrowse();
                                               }
                                           });

        submitCheckB.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e) {
                                               toggleSubmitOptions();
                                           }
                                       });


    }

    //// layout components in
    private void arrangeComponents() {
        arrangeCompsWithGridBag();
    }

    private void addGridBagComponent(Container parent, Component comp,
                                     int gridx, int gridy, int gridwidth, int gridheight,
                                     double weightx, double weighty,
                                     int anchor, int fill,
                                     Insets insets, int ipadx, int ipady
                                    ) {
        GridBagConstraints cons = new GridBagConstraints();
        cons.gridx = gridx;
        cons.gridy = gridy;
        cons.gridwidth = gridwidth;
        cons.gridheight = gridheight;
        cons.weightx = weightx;
        cons.weighty = weighty;
        cons.anchor = anchor;
        cons.fill = fill;
        cons.insets = insets;
        cons.ipadx = ipadx;
        cons.ipady = ipady;
        parent.add(comp,cons);
    }

    private void arrangeCompsWithGridBag() {

        this.contentPane.setLayout(new GridBagLayout());

        this.setTopMessage(errorPageMsg);

        int gridy = 0;

        Component topGlue = Box.createGlue();

        Component submitGlue = Box.createVerticalStrut(2);

        Component bottomGlue = Box.createGlue();

        addGridBagComponent(this.contentPane,topGlue,
                            0,gridy,3,1,
                            100,100,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,2,2,2),5,5	);
        addGridBagComponent(this.contentPane,submitOptionMsgLabel,
                            0,++gridy,3,1,
                            0,0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,2,2,2),5,5	);

        addGridBagComponent(this.contentPane,submitCheckB,
                            0,++gridy,3,1,
                            0,0,
                            GridBagConstraints.WEST, GridBagConstraints.BOTH,
                            new Insets(2,2,2,2),5,5	);

        addGridBagComponent(this.contentPane,submitURLLabel,
                            0,++gridy,1,1,
                            0,0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,8,2,2),5,5	);

        addGridBagComponent(this.contentPane,submitURLTF,
                            1,gridy,1,1,
                            100,0,
                            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                            new Insets(2,2,2,2),5,5	);

        addGridBagComponent(this.contentPane,submitURLBrowseB,
                            2,gridy,1,1,
                            0,0,
                            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                            new Insets(2,2,2,2),5,5	);

        addGridBagComponent(this.contentPane,submitButtonNameLabel,
                            0,++gridy,1,1,
                            0,0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,8,2,2),5,5	);

        addGridBagComponent(this.contentPane,submitButtonNameTF,
                            1,gridy,1,1,
                            100,0,
                            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                            new Insets(2,2,2,2),5,5	);

        addGridBagComponent(this.contentPane,submitGlue,
                            0,++gridy,3,1,
                            100,100,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,2,2,2),5,5	);


        if(this.pageType == JSPPage.DUMB_PAGE ||
                this.pageType == JSPPage.INPUT_PAGE ||
                this.pageType == JSPPage.RESULT_PAGE ||
                this.pageType == JSPPage.IO_PAGE
          ) {

            this.setTopMessage(standardPageMsg);

            addGridBagComponent(this.contentPane,errorPageURLMsgLabel,
                                0,++gridy,3,1,
                                0,0,
                                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(2,2,2,2),5,5	);

            addGridBagComponent(this.contentPane,errorPageURLLabel,
                                0,++gridy,1,1,
                                0,0,
                                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(2,2,2,2),5,5	);

            addGridBagComponent(this.contentPane,errorPageURLTF,
                                1,gridy,1,1,
                                100,0,
                                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                new Insets(2,2,2,2),5,5	);

            addGridBagComponent(this.contentPane,errorPageURLBrowseB,
                                2,gridy,1,1,
                                0,0,
                                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                new Insets(2,2,2,2),5,5	);
        }

        addGridBagComponent(this.contentPane,bottomGlue,
                            0,gridy,3,1,
                            100,100,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,2,2,2),5,5	);

    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox submitCheckB;

    private MultiLineLabel submitOptionMsgLabel;
    private javax.swing.JLabel submitURLLabel;
    private javax.swing.JTextField submitURLTF;
    private javax.swing.JButton submitURLBrowseB;

    private javax.swing.JLabel submitButtonNameLabel;
    private javax.swing.JTextField submitButtonNameTF;

    private MultiLineLabel errorPageURLMsgLabel;
    private javax.swing.JLabel errorPageURLLabel;
    private javax.swing.JTextField errorPageURLTF;
    private javax.swing.JButton errorPageURLBrowseB;


    private String standardPageMsg = "";				 // NOI18N
    private String errorPageMsg = "";					 // NOI18N
    private int pageType = JSPPage.IO_PAGE;


    public void rearrangeComponents() {
        this.contentPane.removeAll();
        arrangeComponents();
    }


    public int getPageType(){ return pageType;}

    public void setPageType(int pageType) {
        this.pageType = pageType;
        switch(pageType) {
        case JSPPage.INPUT_PAGE:
            break;
        case JSPPage.RESULT_PAGE:
            break;
        case JSPPage.ERROR_PAGE:
            break;
        }
    }


    public void setDefaults() {

    }

    public void doErrorPageBrowse() {
        // Debug.println("Browsing for Error Page");		 // NOI18N
        String jspFile =IDEHelper.browseForJSPPage();
        if (!jspFile.equals(""))						 // NOI18N
            errorPageURLTF.setText("/"+jspFile);			 // NOI18N

    }

    public void doSubmitURLBrowse() {
        // Debug.println("Browsing for Submit URL Page");  // NOI18N
        String jspFile =IDEHelper.browseForJSPPage();
        if (!jspFile.equals(""))						 // NOI18N
            submitURLTF.setText("/"+jspFile);				 // NOI18N
    }

    public void toggleSubmitOptions() {
        boolean yes = submitCheckB.isSelected();
        submitURLLabel.setEnabled(yes);
        submitURLTF.setEnabled(yes);
        submitURLBrowseB.setEnabled(yes);

        submitButtonNameLabel.setEnabled(yes);
        submitButtonNameTF.setEnabled(yes);
    }

    public void updateSubmitOptions() {
        boolean yes = JSPPageWizard.simpleJSPPage.isSubmitAllowed();
        submitCheckB.setSelected(yes);
        toggleSubmitOptions();
    }

    public void readSubmitAndErrorURLOptions() {
        JSPPage jspPage = JSPPageWizard.simpleJSPPage;

        jspPage.updatePageType();
        this.setPageType(jspPage.getPageID());
        this.rearrangeComponents();

        updateSubmitOptions();

        submitURLTF.setText(jspPage.getSubmitURL());
        submitButtonNameTF.setText(jspPage.getSubmitButtonLabel());
        errorPageURLTF.setText(jspPage.getErrorURL());
    }

    public void storeSubmitAndErrorURLOptions() {
        ////NB validate here and show message box if info missing.

        JSPPage jspPage = JSPPageWizard.simpleJSPPage;

        JSPPageWizard.simpleJSPPage.setSubmitAllowed(submitCheckB.isSelected());
        jspPage.setSubmitURL((String)submitURLTF.getText());
        jspPage.setSubmitButtonLabel((String)submitButtonNameTF.getText());

        jspPage.setErrorURL((String)errorPageURLTF.getText());

        jspPage.updatePageType();
    }

    public boolean validateURLsInfo() {

        if(submitCheckB.isSelected()) {
            // check submit url
            String submitURL = submitURLTF.getText();
            if(submitURL == null || submitURL.trim().length() <= 0) {
                if(!IDEHelper.askConfirmationI18N("JBW_NoSubmitURLConfirmMsg")) {			 // NOI18N
                    return false;
                }
            }

            String submitButtonLabel = submitButtonNameTF.getText();
            if(submitButtonLabel == null || submitButtonLabel.trim().length() <= 0) {
                IDEHelper.showErrorMessageI18N("JBW_NoSubmitLabelErr");	  	   // NOI18N
                return false;
            }
            // check submit button
        }
        // check error url
        // just warn user and ignore error handling url

        JSPPage jspPage = JSPPageWizard.simpleJSPPage;
        if(!jspPage.isErrorPage()) {
            String errorURL = errorPageURLTF.getText();
            if(errorURL == null || errorURL.trim().length() <= 0) {
                if(!IDEHelper.askConfirmationI18N("JBW_NoErrorURLConfirmMsg")) {			 // NOI18N
                    return false;
                }
            }
        }

        return true;
    }

    public boolean validateInput() {
        // return isValidJSPPageName();
        return validateURLsInfo();
    }

    // WizardDescriptor.Panel implementations


    public void readSettings(Object setting) {
        readSubmitAndErrorURLOptions();
    }

    public void storeSettings(Object setting) {
        storeSubmitAndErrorURLOptions();
    }



    // ---------------------------------------------------------------------------------------
    // WizardDescriptor.Panel implementation

    public static void main(String[] args) {

    }

}
