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

import java.io.*;

import org.openide.util.*;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.*;


public class JSPPageNameAndStyleInfoPanel extends StandardWizardPanel {

    // ---------------------------------------------------------------------------------------
    // WizardPanel initialization

    /** Creates new BeanTypePanel */

    public JSPPageNameAndStyleInfoPanel() {
        this(JSPPage.IO_PAGE);
    }

    public JSPPageNameAndStyleInfoPanel(int pageType) {
        super();
        this.pageType = pageType;
        initComponents ();
    }

    /*public HelpCtx getHelp () {
      return new HelpCtx (JSPPageNameAndStyleInfoPanel.class);
}*/

    private void initComponents () {

        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);

        this.setTopMessage(resBundle.getString("JBW_JSPPageNameAndStyleInfoMsg"));				 // NOI18N

        //// create components
        pageFormStyleLabel = new JLabel(resBundle.getString("JBW_JSPFormStyleLabel"));			 // NOI18N
        pageFormStyleCombo = new JComboBox();

        pageNameLabel = new JLabel(resBundle.getString("JBW_JSPPageNameLabel"));					 // NOI18N
        pageNameTF = new JTextField(30);

        jspFolderLabel = new JLabel(resBundle.getString("JBW_JSPFolderLabel"));					 // NOI18N
        jspFolderTF = new javax.swing.JTextField (30);
        // jspFolderTF.setEnabled(false);
        jspFolderTF.setEditable(false);
        jspFolderTF.setBackground (java.awt.Color.lightGray);
        jspFolderBrowseB = new JButton (resBundle.getString("JBW_JSPFolderBrowseBLabel"));		 // NOI18N
        // netbeans convention
        jspFolderBrowseB.setFont (new java.awt.Font ("SansSerif", 0, 11));						 // NOI18N
        jspFolderBrowseB.setMinimumSize (new java.awt.Dimension(85, 15));
        jspFolderBrowseB.setMaximumSize (new java.awt.Dimension(85, 25));
        jspFolderBrowseB.setPreferredSize (new java.awt.Dimension(85, 25));


        //// layout components
        arrangeComponents();

        //// setup models

        pageFormStyleCombo.setModel(new DefaultComboBoxModel(HTMLForm.getFormStyles()));

        this.setDefaults();


        pageFormStyleCombo.addActionListener(new ActionListener() {
                                                 public void actionPerformed(ActionEvent e) {
                                                     JComboBox cb = (JComboBox)e.getSource();
                                                     formStyle = cb.getSelectedIndex();
                                                 }
                                             });

        jspFolderBrowseB.addActionListener( new ActionListener() {
                                                public void actionPerformed(ActionEvent evt) {
                                                    browseForJSPFolder();
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

        int gridy = 0;

        Component topGlue = Box.createGlue();
        Component styleGlue = Box.createVerticalStrut(10);

        Component bottomGlue = Box.createGlue();

        addGridBagComponent(this.contentPane,topGlue,
                            0,gridy,2,1,
                            100,100,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,2,2,2),5,5	);

        addGridBagComponent(this.contentPane,pageFormStyleLabel,
                            0,++gridy,1,1,
                            0,0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,2,2,2),5,5	);

        addGridBagComponent(this.contentPane,pageFormStyleCombo,
                            1,gridy,1,1,
                            100,0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,2,2,2),5,5	);

        addGridBagComponent(this.contentPane,styleGlue,
                            0,++gridy,3,1,
                            100,0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,2,2,2),5,5	);

        addGridBagComponent(this.contentPane,pageNameLabel,
                            0,++gridy,1,1,
                            0,0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,2,2,2),5,5	);

        addGridBagComponent(this.contentPane,pageNameTF,
                            1,gridy,1,1,
                            100,0,
                            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                            new Insets(2,2,2,2),5,5	);


        addGridBagComponent(this.contentPane,jspFolderLabel,
                            0,++gridy,1,1,
                            0,0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,2,2,2),5,5	);

        addGridBagComponent(this.contentPane,jspFolderTF,
                            1,gridy,1,1,
                            100,0,
                            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                            new Insets(2,2,2,2),5,5	);

        addGridBagComponent(this.contentPane,jspFolderBrowseB,
                            2,gridy,1,1,
                            0,0,
                            GridBagConstraints.WEST, GridBagConstraints.NONE,
                            new Insets(2,2,2,2),5,5	);


        addGridBagComponent(this.contentPane,bottomGlue,
                            0,++gridy,3,1,
                            100,100,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,2,2,2),5,5	);

    }



    // Variables declaration - do not modify//GEN-BEGIN:variables

    private javax.swing.JLabel pageNameLabel;
    private javax.swing.JTextField pageNameTF;


    private javax.swing.JLabel jspFolderLabel;
    private javax.swing.JTextField jspFolderTF;
    private javax.swing.JButton jspFolderBrowseB;

    private javax.swing.JLabel pageFormStyleLabel;
    private javax.swing.JComboBox pageFormStyleCombo;

    private javax.swing.JLabel pageInfoMsgLabel;

    private int pageType = JSPPage.IO_PAGE;
    private String templateName = JSPPageTemplate.DEF_JSPPAGE_TLT;
    private int formStyle = HTMLForm.FS_COLUMN;


    public void rearrangeComponents() {
        this.contentPane.removeAll();
        arrangeComponents();
    }

    public String getPageName() { return pageNameTF.getText(); }
    public void setPageName(String pageName) {pageNameTF.setText(pageName);}

    public String getPageTemplate() { return templateName; }
    public void setPageTemplate(String templateName) {
        this.templateName = templateName;
    }

    public int getPageFormStyle() { return pageFormStyleCombo.getSelectedIndex(); }
    public void setPageFormStyle(int formStyle) {pageFormStyleCombo.setSelectedIndex(formStyle);}

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

        switch(pageType) {
        case JSPPage.INPUT_PAGE:
            pageNameTF.setText("InputPage");								 // NOI18N
            pageFormStyleCombo.setSelectedIndex(HTMLForm.FS_COLUMN);
            templateName = JSPPageTemplate.DEF_JSPPAGE_TLT;
            break;
        case JSPPage.RESULT_PAGE:
            pageNameTF.setText("ResultPage");								 // NOI18N
            pageFormStyleCombo.setSelectedIndex(HTMLForm.FS_COLUMN);
            templateName = JSPPageTemplate.DEF_JSPPAGE_TLT;
            break;
        case JSPPage.ERROR_PAGE:
            pageNameTF.setText("ErrorPage");								 // NOI18N
            pageFormStyleCombo.setSelectedIndex(HTMLForm.FS_COLUMN);
            templateName = JSPPageTemplate.DEF_ERROR_TLT;
            break;
        }
    }


    public void readPageInfo() {
        JSPPage jspPage = JSPPageWizard.simpleJSPPage;
        jspPage.updatePageType();
        this.setPageType(jspPage.getPageID());
        //this.rearrangeComponents();

        pageNameTF.setText(jspPage.getJSPName());
        pageFormStyleCombo.setSelectedIndex(jspPage.getPageFormStyleName());
    }

    public void storePageInfo() {
        ////NB validate here and show message box if info missing.

        JSPPage jspPage = JSPPageWizard.simpleJSPPage;

        // jspPage.setJSPName(pageNameTF.getText());
        jspPage.setJSPName(this.getJSPPageName());
        //jspPage.setPageTemplateName(this.getPageTemplate());
        jspPage.setPageFormStyleName(this.getPageFormStyle());
    }

    // JSP Page Name without extension

    private void setJSPPageName(String pageName) throws Exception {
        pageName = pageName.trim();
        int idx = pageName.trim().lastIndexOf(".");								 // NOI18N
        if(idx >= 0) {
            String name = pageName.substring(0,idx);
            String ext = pageName.substring(idx,pageName.length());
            if( name.length() <= 0 || !ext.equalsIgnoreCase(".jsp")) {			 // NOI18N
                throw new Exception("Not a Valid JSP Name");						 // NOI18N
            }

        }else {
            pageName = pageName.concat(".jsp");									 // NOI18N
        }
        pageNameTF.setText(pageName);
    }

    private String getJSPPageName() {
        String pageName = pageNameTF.getText().trim();
        int idx = pageName.lastIndexOf(".");										 // NOI18N
        if(idx >= 0 ) {
            pageName = pageName.substring(0,idx);
        }
        return pageName;
    }
    // check for name and extensions and check for file in the repository.
    // if exists, ask user for overwrite permision , set overwrite permision
    // on JSP page.
    public boolean isValidJSPPageName() {
        String jspPageName = this.getJSPPageName();
        try {
            this.setJSPPageName(jspPageName);
        }catch(Exception ex) {
            IDEHelper.showErrorMessageI18N("JBW_NotValidJSPFileNameErr");		 // NOI18N
            return false;
        }

        if(IDEHelper.fileExists(JSPPageWizard.jspFolder,jspPageName,"jsp")) {			 // NOI18N

            if(jspPageName.equals(JSPPageWizard.simpleJSPPage.getJSPName()) &&
                    JSPPageWizard.simpleJSPPage.isOverwrite())
                return true;

            if(!IDEHelper.askConfirmationI18N("JBW_OverWriteJSPFileConfirmMsg")) {			 // NOI18N
                JSPPageWizard.simpleJSPPage.setOverwrite(false);
                return false;
            }	else {
                JSPPageWizard.simpleJSPPage.setOverwrite(true);
            }
        }
        return true;
    }

    private void browseForJSPFolder() {
        DataFolder jspFolder = IDEHelper.browseForJSPFolder();
        String rootName = "";										 // NOI18N
        String jspFolderName = "";									 // NOI18N
        if(jspFolder != null) {
            JSPPageWizard.jspFolder = jspFolder;
            jspFolderName = jspFolder.getPrimaryFile().getPackageName('/');
            try {
                rootName = jspFolder.getPrimaryFile().getFileSystem().getDisplayName();
            }catch(Exception ex) {
                rootName = "";										 // NOI18N
            }
            jspFolderTF.setText(rootName+"/"+jspFolderName);		 // NOI18N
        }

    }



    public boolean validateInput() {
        if(JSPPageWizard.jspFolder == null) {
            IDEHelper.showErrorMessageI18N("JBW_NullJSPFolderErr");		 // NOI18N
            return false;
        }
        return isValidJSPPageName();
    }

    // WizardDescriptor.Panel implementations

    // public boolean isValid() {
    //  return isValidJSPPageName();
    // }

    public void readSettings(Object setting) {
        readPageInfo();
    }

    public void storeSettings(Object setting) {
        storePageInfo();
    }



    // ---------------------------------------------------------------------------------------
    // WizardDescriptor.Panel implementation

    public static void main(String[] args) {
        if(Debug.TEST) {
            JFrame testFrame = new JFrame("This is Test Frame");			 // NOI18N
            JSPPageNameAndStyleInfoPanel jspPageInfoPanel1 = new JSPPageNameAndStyleInfoPanel(JSPPage.INPUT_PAGE);

            testFrame.getContentPane().setLayout(new GridLayout(0,1));
            testFrame.getContentPane().add(jspPageInfoPanel1);
            testFrame.setSize(500,300);
            testFrame.pack();
            testFrame.show();
        }


    }

}
