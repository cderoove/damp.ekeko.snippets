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

package  org.netbeans.modules.web.wizards.beanjsp.model;

import org.netbeans.modules.web.wizards.beanjsp.util.*;
import org.netbeans.modules.web.wizards.beanjsp.ide.netbeans.*;
import org.netbeans.modules.web.wizards.beanjsp.ui.*;
import org.netbeans.modules.web.util.*;

// import com.sun.jasper.webapp.ide.netbeans.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

import java.io.*;
import java.lang.reflect.*;
import java.beans.*;

public class JSPPage extends Object {

    public static final int DUMB_PAGE = 0;
    public static final int INPUT_PAGE = 1;
    public static final int RESULT_PAGE = 2;
    public static final int IO_PAGE = 3;
    public static final int ERROR_PAGE = 4;
    public static final int ERR_IN_PAGE = 5;
    public static final int ERR_OUT_PAGE = 6;
    public static final int ERR_IO_PAGE = 7;

    public static BeanManager beanManager = new BeanManager();

    private boolean valid;
    private String jspName;
    private int pageID = IO_PAGE;

    private JSPVector useBeans;
    private JSPVector inputFields;
    private JSPVector bizMethods;
    private JSPVector resultFields;

    private JSPPageTemplate pageTemplate;

    private String submitURL;
    private String errorURL;
    private String submitButtonLabel;

    private String pageTemplateName;
    private int pageFormStyleName;

    private JSPItemListModel beansModel;
    private JSPItemListModel methodsModel;
    private JSPItemListModel setterFieldsModel;
    private JSPItemListModel getterFieldsModel;

    private JSPBeanTableModel useBeansModel;
    private JSPItemListModel bizMethodsModel;

    private JSPInputFieldTableModel jspInputFieldsModel;
    private JSPDisplayFieldTableModel jspDisplayFieldsModel;

    boolean errorPage = false;
    boolean submitAllowed = true;
    boolean submitProcessed = true;

    boolean overwrite = false;

    public JSPPage() {
        this(DUMB_PAGE);
    }

    public JSPPage(int pageID) {
        valid = true;
        this.pageID = pageID;
        jspName = getDefaultPageName(pageID);
        useBeans = new JSPVector();
        bizMethods = new JSPVector();
        inputFields = new JSPVector();
        resultFields = new JSPVector();
        pageTemplate = new JSPPageTemplate();

        pageTemplateName = JSPPageTemplate.DEF_JSPPAGE_TLT;
        pageFormStyleName = HTMLForm.FS_COLUMN;

        submitURL = "";					 // NOI18N
        errorURL = "";					 // NOI18N
        submitButtonLabel="Submit";		 // NOI18N
        overwrite = false;

        initializeModels();
    }

    private String getDefaultPageName(int pageID) {
        switch (pageID) {
        case DUMB_PAGE : return "JSPPage";				 // NOI18N
        case INPUT_PAGE  : return "InputPage";			 // NOI18N
        case RESULT_PAGE  : return "ResultPage";		 // NOI18N
        case ERROR_PAGE  : return "ErrorPage";			 // NOI18N
        case IO_PAGE  : return "JSPPage";				 // NOI18N
        default : return "JSPPage";						 // NOI18N
        }
    }

    /** Returns JSP name */
    public String getJSPName() {
        return jspName;
    }

    /** Sets JSP name */
    public void setJSPName(String name) {
        this.jspName = name;
    }

    public int getPageID() { return pageID; }
    public void setPageID(int pageID) { this.pageID = pageID; }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getSubmitURL() { return submitURL;}
    public void setSubmitURL(String submitURL) { this.submitURL = submitURL;}

    public String getSubmitButtonLabel() { return submitButtonLabel;}
    public void setSubmitButtonLabel(String submitButtonLabel) { this.submitButtonLabel = submitButtonLabel;}

    public String getErrorURL() { return errorURL;}
    public void setErrorURL(String errorURL) { this.errorURL = errorURL;}

    public String getPageTemplateName() { return pageTemplateName; }
    public void setPageTemplateName(String pageTemplateName) { this.pageTemplateName = pageTemplateName; }

    public int getPageFormStyleName() { return pageFormStyleName; }
    public void setPageFormStyleName(int pageFormStyleName) { this.pageFormStyleName = pageFormStyleName; }


    //// new properties
    public boolean isErrorPage() { return errorPage; }
    public void setErrorPage(boolean errorPage) { this.errorPage = errorPage;}

    public boolean isSubmitAllowed() { return submitAllowed; }
    public void setSubmitAllowed(boolean submitAllowed) { this.submitAllowed = submitAllowed;}

    public boolean isSubmitProcessed() { return submitProcessed; }
    public void setSubmitProcessed(boolean submitProcessed) { this.submitProcessed = submitProcessed;}

    public boolean isOverwrite() { return this.overwrite;}
    public void setOverwrite(boolean overwrite) { this.overwrite = overwrite;}

    //// beans managing

    public void addJSPBean(JSPBean jspBean) {
        this.useBeans.add(jspBean);
    }

    public void removeJSPBean(JSPBean jspBean) {
        this.useBeans.remove(jspBean);
    }

    public void addJSPBeanMethod(JSPBeanMethod jspBeanMethod) {
        this.bizMethods.add(jspBeanMethod);
    }

    public void removeJSPBeanMethod(JSPBeanMethod jspBeanMethod) {
        this.bizMethods.remove(jspBeanMethod);
    }

    public void addInputField(JSPBeanField jspBeanField) {
        this.inputFields.add(jspBeanField);
    }

    public void removeInputField(JSPBeanField jspBeanField) {
        this.inputFields.remove(jspBeanField);
    }

    public void addResultField(JSPBeanField jspBeanField) {
        this.resultFields.add(jspBeanField);
    }

    public void removeResultField(JSPBeanField jspBeanField) {
        this.resultFields.remove(jspBeanField);
    }

    //// model management

    public void initializeModels() {

        beansModel = new JSPItemListModel();
        methodsModel = new JSPItemListModel();
        setterFieldsModel = new JSPItemListModel();
        getterFieldsModel = new JSPItemListModel();

        useBeansModel = new JSPBeanTableModel(this.useBeans);
        bizMethodsModel = new JSPItemListModel(this.bizMethods);

        jspInputFieldsModel = new JSPInputFieldTableModel(this.inputFields);
        jspDisplayFieldsModel = new JSPDisplayFieldTableModel(this.resultFields);


        useBeansModel.addTableModelListener(new TableModelListener() {
                                                public void tableChanged(TableModelEvent evt) {
                                                    if(evt instanceof JSPBeanTableModel.JSPBeanTableModelEvent) {
                                                        if(evt.getType() == TableModelEvent.DELETE) {
                                                            JSPBean jspBean = ((JSPBeanTableModel.JSPBeanTableModelEvent)evt).getJSPBean();
                                                            removeFieldsAndMethods(jspBean);
                                                        }else if(evt.getType() == TableModelEvent.INSERT) {
                                                            JSPBean jspBean = ((JSPBeanTableModel.JSPBeanTableModelEvent)evt).getJSPBean();
                                                            addFieldsAndMethods(jspBean);
                                                        }
                                                    }
                                                }
                                            });
    }

    private void addFieldsAndMethods(JSPBean jspBean) {
        // remove fields and methods from usebean fields & methods list
        // and add it to available beans fields & methods list
        // Debug.println("adding the Fields and Methods for Bean : "+jspBean.getBeanName());

        JSPVector setterFields = (JSPVector)JSPPage.beanManager.getValidJSPBeanSetterFields(jspBean);
        JSPVector getterFields = (JSPVector)JSPPage.beanManager.getValidJSPBeanGetterFields(jspBean);
        JSPVector beanMethods = (JSPVector)JSPPage.beanManager.getValidJSPBeanMethods(jspBean);

        methodsModel.addItems(beanMethods);
        setterFieldsModel.addItems(setterFields);
        getterFieldsModel.addItems(getterFields);

    }


    private void removeFieldsAndMethods(JSPBean jspBean) {

        if(jspBean == null) {
            // Debug.println("Removing All Fields and Methods...");
            bizMethodsModel.removeAll();
            jspInputFieldsModel.removeAll();
            jspDisplayFieldsModel.removeAll();

            methodsModel.removeAll(jspBean);
            setterFieldsModel.removeAll();
            getterFieldsModel.removeAll();

        } else {
            // Debug.println("removing the Fields and Methods for Bean : "+jspBean.getBeanName());

            bizMethodsModel.removeAll(jspBean);
            jspInputFieldsModel.removeAll(jspBean);
            jspDisplayFieldsModel.removeAll(jspBean);

            methodsModel.removeAll(jspBean);
            setterFieldsModel.removeAll(jspBean);
            getterFieldsModel.removeAll(jspBean);
        }

    }

    public JSPItemListModel getAvailableBeansModel() {
        return this.beansModel;
    }

    public JSPItemListModel getAvailableBeanMethodsModel() {
        return this.methodsModel;
    }

    public JSPItemListModel getAvailableSetterFieldsModel() {
        return this.setterFieldsModel;
    }

    public JSPItemListModel getAvailableGetterFieldsModel() {
        return this.getterFieldsModel;
    }

    public JSPBeanTableModel getJSPBeansModel() {
        return this.useBeansModel;
    }

    public JSPDisplayFieldTableModel getJSPDisplayFieldsModel() {
        return this.jspDisplayFieldsModel;
    }

    public JSPInputFieldTableModel getJSPInputFieldsModel() {
        return this.jspInputFieldsModel;
    }


    public JSPItemListModel getBizMethodsModel() {
        return this.bizMethodsModel;
    }

    public void loadBeansList(JSPVector availableBeans) {

        useBeansModel.removeAll();
        bizMethodsModel.removeAll();
        jspInputFieldsModel.removeAll();
        jspDisplayFieldsModel.removeAll();

        beansModel.removeAll();
        methodsModel.removeAll();
        setterFieldsModel.removeAll();
        getterFieldsModel.removeAll();

        Iterator beansIterator = availableBeans.iterator();

        for(;beansIterator.hasNext();) {
            JSPBean jspBean = (JSPBean)((JSPBean)beansIterator.next()).clone();
            beansModel.add(jspBean);
        }
    }

    public void updatePageType() {

        int type = this.getPageID();

        if(this.isErrorPage()) {
            if(this.isSubmitAllowed() && this.isSubmitProcessed()){
                this.setPageID(this.ERR_IO_PAGE);
            } else if(this.isSubmitAllowed()&& !this.isSubmitProcessed() ) {
                this.setPageID(this.ERR_IN_PAGE);
            } else if(!this.isSubmitAllowed() && this.isSubmitProcessed() ) {
                this.setPageID(this.ERR_OUT_PAGE);
            } else {
                this.setPageID(this.ERROR_PAGE);
            }
        } else {
            if(this.isSubmitAllowed() && this.isSubmitProcessed()){
                this.setPageID(this.IO_PAGE);
            } else if(this.isSubmitAllowed()&& !this.isSubmitProcessed() ) {
                this.setPageID(this.INPUT_PAGE);
            } else if(!this.isSubmitAllowed()&& this.isSubmitProcessed() ) {
                this.setPageID(this.RESULT_PAGE);
            } else {
                this.setPageID(this.DUMB_PAGE);
            }
        }

    }

    //// add init info to use bean

    public void updateUseBeanInitProperties() {
        Iterator iterator = this.resultFields.iterator();
        for(; iterator.hasNext(); ) {
            JSPBeanField initField = (JSPBeanField)iterator.next();
            //// make sure no duplicates for this
            initField.selfPopulateToUseBeanInitProperties();
        }
    }

    //// Code generation Methods

    public void writePageDirective(PrintWriter jspWriter) {
        switch(this.getPageID()) {
        case ERROR_PAGE :
        case ERR_IN_PAGE:
        case ERR_OUT_PAGE:
        case ERR_IO_PAGE:
            jspWriter.println("<%@ page isErrorPage=\"true\" %> \n");						 // NOI18N
            break;
        default:
            String errorURL = this.getErrorURL();
            if(errorURL != null && errorURL.trim().length() > 0 ) {
                jspWriter.println("<%@ page errorPage=\""+this.getErrorURL()+"\" %> \n");		 // NOI18N
            }
            break;

        }
    }

    public void writeInstantiateJSPBeans(PrintWriter jspWriter) {

        ////todo: make sure that you are not calling this multiple times
        updateUseBeanInitProperties();

        jspWriter.println();
        Iterator beansIterator = useBeans.iterator();
        for(; beansIterator.hasNext(); ) {
            JSPBean jspBean = (JSPBean)beansIterator.next();
            jspWriter.println(jspBean.toJSPCode());
        }
        jspWriter.println();
    }

    public void writeProcessInputData(PrintWriter jspWriter){

        jspWriter.println();
        Iterator iterator = this.inputFields.iterator();
        for(; iterator.hasNext(); ) {
            JSPBeanField jspBeanField = (JSPBeanField)iterator.next();
            jspWriter.println(jspBeanField.toSubmitProcessJSPCode());
        }
        jspWriter.println();

    }

    public void writeExecuteBusinessLogicMethods(PrintWriter jspWriter){

        jspWriter.println();
        Iterator iterator = this.bizMethods.iterator();
        for(; iterator.hasNext(); ) {
            JSPBeanMethod jspBeanMethod = (JSPBeanMethod)iterator.next();
            jspWriter.println(jspBeanMethod.toJSPCode());
        }
        jspWriter.println();

    }

    public void writeDynamicForm(PrintWriter jspWriter){
        jspWriter.println();

        String submitLink = this.getSubmitURL();
        String submitValue = this.getSubmitButtonLabel();

        switch(this.getPageID()){
        case INPUT_PAGE:
        case IO_PAGE:
        case ERR_IN_PAGE:
        case ERR_IO_PAGE:
            jspWriter.println("<form name=\""+this.getJSPName()+"Form\""+ " method=post "+			 // NOI18N
                              "action=\""+submitLink+"\" >");								 // NOI18N
            break;
        default :
            jspWriter.println("<form name=\""+this.getJSPName()+"Form\"" + " >");				 // NOI18N
            break;

        }

        ////todo: PLEASE implement this layout junk in HTMLForm
        // Debug.println(" Form Layout : "+this.pageFormStyleName);
        if(this.pageFormStyleName == HTMLForm.FS_COLUMN) {
            jspWriter.println("<table border=0 cols=2 width=\"100%\" >");				 // NOI18N
        }else if(this.pageFormStyleName == HTMLForm.FS_GRID_2) {
            jspWriter.println("<table border=0 cols=4 width=\"100%\" >");				 // NOI18N
        } else {
            // NO LAYOUT
            // jspWriter.println("<TABLE BORDER=0 COLS=2 WIDTH=\"100%\" >");
        }

        Iterator iterator = this.resultFields.iterator();

        if(this.pageFormStyleName == HTMLForm.FS_COLUMN) {
            for(; iterator.hasNext(); ) {
                jspWriter.println("<tr>");												 // NOI18N
                JSPBeanField jspBeanField = (JSPBeanField)iterator.next();
                jspWriter.println(jspBeanField.toFormJSPCode(true));
                jspWriter.println("</tr>");												 // NOI18N
            }
        } else if(this.pageFormStyleName == HTMLForm.FS_GRID_2) {
            int cols = 0;
            for(; iterator.hasNext(); ) {
                if(cols == 0) {
                    jspWriter.println("<tr>");											 // NOI18N
                }
                JSPBeanField jspBeanField = (JSPBeanField)iterator.next();
                jspWriter.println(jspBeanField.toFormJSPCode(true));
                ++cols;
                if(cols == 2) {
                    jspWriter.println("</tr>");											 // NOI18N
                    cols = 0;
                }
            }
        } else {
            for(; iterator.hasNext(); ) {
                JSPBeanField jspBeanField = (JSPBeanField)iterator.next();
                jspWriter.println(jspBeanField.toFormJSPCode(false));
            }
        }

        switch(this.getPageID()){
        case INPUT_PAGE:
        case IO_PAGE:
        case ERR_IN_PAGE:
        case ERR_IO_PAGE:

            if(this.pageFormStyleName == HTMLForm.FS_COLUMN ) {
                jspWriter.println("<tr><td>&nbsp;</td><td><input type=submit value=\""+submitValue+"\" ></td></tr>");			 // NOI18N
            } else if(this.pageFormStyleName == HTMLForm.FS_GRID_2) {
                jspWriter.println("<tr><td>&nbsp;</td><td><input type=submit value=\""+submitValue+"\" ></td></tr>");			 // NOI18N
            }else {
                // NO Layout
                jspWriter.println("&nbsp;<input type=submit value=\""+submitValue+"\" >");									 // NOI18N
            }

            break;
        default :
            break;
        }

        if(this.pageFormStyleName != HTMLForm.FS_NOLAYOUT) {
            jspWriter.println("</table>");												 // NOI18N
        }

        jspWriter.println("</form>");													 // NOI18N
        jspWriter.println();
    }

    public void writeDynamicContent() {

        try {

            // Debug.println("Preparing Dynamic Contnet...");
            // Debug.println("Writing JSP with Type: "+this.getPageID());

            pageTemplate = new JSPPageTemplate();

            //NB String templateFileName = pageTemplate.getPageTemplateFileName(this.getPageTemplateName());
            //NB pageTemplate.loadTemplateData(pageTemplate.getTemplateReader(templateFileName));

            switch( this.getPageID()){
            case ERROR_PAGE:
            case ERR_IN_PAGE:
            case ERR_OUT_PAGE:
            case ERR_IO_PAGE:
                pageTemplate.loadTemplateData(pageTemplate.getDefaultErrorPageTemplate());
                break;
            default:
                pageTemplate.loadTemplateData(pageTemplate.getDefaultTemplate());
                break;
            }



            writePageDirective(pageTemplate.getPageDirectiveWriter());

            writeInstantiateJSPBeans(pageTemplate.getUseBeanWriter());

            switch( this.getPageID()){
            case RESULT_PAGE:
            case IO_PAGE:
            case ERR_OUT_PAGE:
            case ERR_IO_PAGE:
                writeProcessInputData(pageTemplate.getInputDataWriter());
                break;
            default:
                break;
            }

            writeExecuteBusinessLogicMethods(pageTemplate.getBizMethodsWriter());

            writeDynamicForm(pageTemplate.getDynamicFormWriter());

            // pageTemplate.saveAsInRepository(this.getJSPName(),"jsp",this.isOverwrite());

        }catch(Exception ex) {Debug.print(ex);}

    }

    public void saveInRepository() {
        try {

            writeDynamicContent();

            if(JSPPageWizard.doNonVoidMethodWarning) {
                IDEHelper.showWarningMessageI18N("JBW_NonVoidParamsInMethodsWarning");		 // NOI18N
                JSPPageWizard.doNonVoidMethodWarning = false;
            }
            // Debug.println("Writing JSP File to the Repository...");
            pageTemplate.saveAsInRepository(this.getJSPName(),"jsp",this.isOverwrite());    // NOI18N

        }catch(Exception ex) {Debug.print(ex);}
    }


    public void saveToFile() {
        try {

            writeDynamicContent();

            File jspFile = new File(this.getJSPName()+".jsp");					 // NOI18N
            // Debug.println("Writing JSP File to File System...");
            pageTemplate.saveAs(jspFile);

        }catch(Exception ex) {Debug.print(ex);}
    }

    public static void main(String[] args) {

        if(Debug.TEST) {
            try {

                String beanClassName = "jspbeans.SampleBean";		 // NOI18N
                if(args.length > 0 )
                    beanClassName = args[0];


                JSPBean jspBean = (JSPBean)JSPPage.beanManager.createJSBBean(Class.forName(beanClassName));

                JSPVector jspMethods = (JSPVector)JSPPage.beanManager.getValidJSPBeanMethods(jspBean);

                JSPVector jspDisplayFields = (JSPVector)JSPPage.beanManager.getValidJSPBeanGetterFields(jspBean);

                JSPVector jspInpitFields = (JSPVector)JSPPage.beanManager.getValidJSPBeanSetterFields(jspBean);


                JSPPage errorPage = new JSPPage(JSPPage.ERROR_PAGE);
                errorPage.setErrorPage(true);
                errorPage.setOverwrite(true);
                errorPage.saveToFile();

                JSPPage ioPage = new JSPPage();

                ioPage.addJSPBean(jspBean);

                for(int i=0; i < jspMethods.size(); ++i) {
                    ioPage.addJSPBeanMethod((JSPBeanMethod)jspMethods.elementAt(i));
                }

                for(int i=0; i < jspDisplayFields.size(); ++i) {
                    ioPage.addResultField((JSPBeanField)jspDisplayFields.elementAt(i));
                }

                for(int i=0; i < jspInpitFields.size(); ++i) {
                    ioPage.addInputField((JSPBeanField)jspInpitFields.elementAt(i));
                }


                ioPage.setSubmitAllowed(true);

                ioPage.setSubmitProcessed(true);

                ioPage.setErrorPage(false);

                ioPage.updatePageType();

                ioPage.setJSPName("TestJSPPage");							 // NOI18N

                ioPage.setSubmitURL(ioPage.getJSPName()+".jsp");			 // NOI18N

                ioPage.setErrorURL(errorPage.getJSPName()+".jsp");			 // NOI18N

                ioPage.setSubmitButtonLabel("Test Submit");					 // NOI18N



                ioPage.setOverwrite(true);
                ioPage.saveToFile();

            }catch(Exception ex) { Debug.print(ex);}

        }

    }

}


