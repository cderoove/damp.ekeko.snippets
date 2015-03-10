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


import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

import java.text.MessageFormat;

import org.netbeans.modules.web.wizards.beanjsp.model.*;
import org.netbeans.modules.web.wizards.wizardfw.*;
import org.netbeans.modules.web.wizards.beanjsp.ide.netbeans.*;

import org.netbeans.modules.web.util.*;
import org.netbeans.modules.web.wizards.beanjsp.util.*;

import org.openide.util.*;
import org.openide.*;

import org.openide.loaders.DataFolder;

public class JSPPageWizard  extends DefaultWizard {

    public static JSPPage simpleJSPPage;
    public static DataFolder beanPakFolder;
    public static DataFolder jspFolder;

    public static final int JSP_PAGETYPE_PANEL  = 0;
    public static final int JSP_BEAN_SELECTION_PANEL = 1;
    public static final int JSP_SUBMIT_PROCESS_PANEL = 2;
    public static final int JSP_INPUT_FILEDS_PANEL = 3;
    public static final int JSP_BIZ_METHODS_PANEL = 4;
    public static final int JSP_DISPLAY_FIELDS_PANEL = 5;
    public static final int JSP_SUBMIT_AND_ERROR_URL_INFO_PANEL  = 6;
    public static final int JSP_PAGE_NAME_AND_STYLE_INFO_PANEL = 7;
    public static final int JSP_GENERATION_PANEL = 8;


    public static final String i18nBundle = "org.netbeans.modules.web.wizards.beanjsp.resources.Bundle";			 // NOI18N

    // a simple way to turn on the Add button in the JSPBeansSelectionPanel
    private static JSPBeansSelectionPanel jspBeanSelPanel;
    private static JSPBizMethodsPanel 		jspBizMethodsPanel;
    private static JSPDisplayFieldsPanel	jspDisplayFieldsPanel;
    private static JSPInputFieldsPanel		jspInputFieldsPanel;

    public static boolean doNonVoidMethodWarning=false;

    public JSPPageWizard(WizardDescriptor.Panel[] panels) {
        super(panels,new Object());
    }

    public static void updateBeanSelectionPanel(){
        if(jspBeanSelPanel != null)
            jspBeanSelPanel.setButtonsEnabled();
    }

    public static void updateBizMethodsPanel(){
        if(jspBizMethodsPanel != null)
            jspBizMethodsPanel.setButtonsEnabled();
    }

    public static void updateDisplayFieldsPanel(){
        if(jspDisplayFieldsPanel != null)
            jspDisplayFieldsPanel.setButtonsEnabled();
    }

    public static void updateInputFieldsPanel(){
        if(jspInputFieldsPanel != null)
            jspInputFieldsPanel.setButtonsEnabled();
    }

    public static void initWizardData() {
        simpleJSPPage = new JSPPage();
        simpleJSPPage.beanManager = new BeanManager();
        beanPakFolder = null;
        jspFolder = null;
    }

    public static void releaseWizardData() {
        simpleJSPPage = null;
        simpleJSPPage.beanManager = null;
        beanPakFolder = null;
        jspFolder = null;
    }

    //// wizard navigation implemetation

    /* Moves to the next panel.
    * @exception NoSuchElementException if the panel does not exist
    */
    public synchronized void nextPanel () {
        if (index + 1 == panels.length) throw new java.util.NoSuchElementException ();

        if(index == JSP_SUBMIT_PROCESS_PANEL) {
            panels[index].storeSettings(new Object());
            if(!JSPPageWizard.simpleJSPPage.isSubmitProcessed()) {
                index += 2;
                return;
            }
        } else if ( index == JSP_PAGE_NAME_AND_STYLE_INFO_PANEL ){
            // Debug.println(" On NextPanel  Validating the Last Page");
            JSPPageNameAndStyleInfoPanel jspNameStyleInfoPanel = (JSPPageNameAndStyleInfoPanel) panels[index];
            if(!jspNameStyleInfoPanel.validateInput())
                return;
        } else if ( index == JSP_SUBMIT_AND_ERROR_URL_INFO_PANEL ) {
            JSPSubmitAndErrorURLInfoPanel jspSubmitAndErrorInfoPanel = (JSPSubmitAndErrorURLInfoPanel) panels[index];
            if(!jspSubmitAndErrorInfoPanel.validateInput())
                return;
        }

        index++;
    }

    /* Moves to previous panel.
    * @exception NoSuchElementException if the panel does not exist
    */
    public synchronized void previousPanel () {
        if (index == 0) throw new java.util.NoSuchElementException ();
        if(index == JSP_BIZ_METHODS_PANEL) {
            if(!JSPPageWizard.simpleJSPPage.isSubmitProcessed()) {
                index -= 2;
                return;
            }
        }
        index--;
    }

    /* Current name of the panel */
    public String name () {
        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);

        Object[] panelNumArgs = {
            new Integer (index + 1),
            new Integer (panels.length)
        };

        MessageFormat panelNumMF = new MessageFormat (resBundle.getString ("JBW_WizardPanelNumber"));		 // NOI18N
        String panelNum = panelNumMF.format (panelNumArgs);

        String panelTitle = "";			 // NOI18N
        switch(index) {
        case JSP_PAGETYPE_PANEL:
            panelTitle = resBundle.getString("JBW_PageTypeOptionPanelTitle");				 // NOI18N
            break;
        case JSP_BEAN_SELECTION_PANEL :
            panelTitle = resBundle.getString("JBW_JSPBeansSelectionPanelTitle");			 // NOI18N
            break;
        case JSP_SUBMIT_PROCESS_PANEL:
            panelTitle = resBundle.getString("JBW_SubmitProcessOptionPanelTitle");		 // NOI18N
            break;
        case JSP_INPUT_FILEDS_PANEL:
            panelTitle = resBundle.getString("JBW_InputFieldsPanelTitle");				 // NOI18N
            break;
        case JSP_BIZ_METHODS_PANEL:
            panelTitle = resBundle.getString("JBW_BizMethodsPanelTitle");					 // NOI18N
            break;
        case JSP_DISPLAY_FIELDS_PANEL:
            panelTitle = resBundle.getString("JBW_DisplayFieldsPanelTitle");				 // NOI18N
            break;
        case JSP_SUBMIT_AND_ERROR_URL_INFO_PANEL:
            panelTitle = resBundle.getString("JBW_JSPPageSubmitAndErrorURLInfoPanelTitle");	 // NOI18N
            break;
        case JSP_PAGE_NAME_AND_STYLE_INFO_PANEL:
            panelTitle = resBundle.getString("JBW_JSPPageNameAndStyleInfoPanelTitle");	 // NOI18N
            break;
        case JSP_GENERATION_PANEL:
            panelTitle = resBundle.getString("JBW_JSPGenereationPanelTitle");				 // NOI18N
            break;
        }

        MessageFormat titleMF = new MessageFormat (resBundle.getString("JBW_JSPPageWizardTitle"));	 // NOI18N

        // Debug.println("Title MF "+titleMF.format(""));
        // Debug.println("Panel Num MF "+panelNumMF.format(""));

        Object[] titleMFArgs = {
            panelNum,
            panelTitle
        };

        return titleMF.format(titleMFArgs);

    }


    //// wizard methods

    public boolean onFinish() {

        try {
            //  Debug.println(" On Finish Validating the Last Page");
            // validate data for NameAndStyle Panel if it is the Last Panel ( Name And Style Panel)
            JSPPageNameAndStyleInfoPanel jspNameInfoPanel = (JSPPageNameAndStyleInfoPanel) panels[JSP_PAGE_NAME_AND_STYLE_INFO_PANEL];
            jspNameInfoPanel.storeSettings(new Object());
            if(!jspNameInfoPanel.validateInput())
                return false;
            simpleJSPPage.saveInRepository();

        }catch(Exception ex) {
            // Debug.print(ex);
            IDEHelper.showErrorMessageI18N("JBW_UnableToCreateJSPErr");		 // NOI18N
            return false;
        }

        // reset wizard data  to release resources
        releaseWizardData();
        return true;
    }

    public boolean onCancel() {
        // reset wizard data  to release resources
        releaseWizardData();
        return true;
    }

    public static void showWizard() {

        JSPPageWizard.initWizardData();


        WizardDescriptor.Panel[] wizardPanels = {
            new JSPPageTypeOptionPanel(),
            jspBeanSelPanel = new JSPBeansSelectionPanel(),
            new JSPSubmitProcessOptionPanel(),
            jspInputFieldsPanel = new JSPInputFieldsPanel(),
            jspBizMethodsPanel = new JSPBizMethodsPanel(),
            jspDisplayFieldsPanel = new JSPDisplayFieldsPanel(),
            new JSPSubmitAndErrorURLInfoPanel(),
            new JSPPageNameAndStyleInfoPanel()
            // new JSPPageGenerationPanel()
        };


        JSPPageWizard jspPageWizard = new JSPPageWizard(wizardPanels);
        jspPageWizard.executeWizard();
    }

    public static void main(String[]args) {

        // WizardManager.setDefault(new WizardManager());
        JSPPageWizard.showWizard();
    }

}


