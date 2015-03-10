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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import org.netbeans.modules.web.wizards.beanjsp.util.*;
import org.netbeans.modules.web.wizards.beanjsp.ui.*;
import org.netbeans.modules.web.util.*;

import org.openide.util.*;

public class JSPDisplayFieldTableModel extends AbstractJSPFieldTableModel {

    public static final int BEAN_CLASS_COL = 100;  // not in use

    public static final int BEAN_FIELD_COL = 0;
    public static final int FIELD_LABEL_COL = 1;
    public static final int FIELD_ELEMENT_COL = 2;
    public static final int FIELD_INIT_COL = 3;


    public static final int NUM_COLUMNS = 4;

    private String beanClassColName;
    private String beanFieldColName;
    private String beanFieldLabelColName;
    private String htmlElementColName;
    private String initValueColName;

    private String initReadOnly;


    public JSPDisplayFieldTableModel(JSPVector jspBeanFields) {
        super(jspBeanFields);

        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);
        beanClassColName = resBundle.getString("JBW_DFTC_BeanClass");			 // NOI18N
        beanFieldColName = resBundle.getString("JBW_DFTC_Field");				 // NOI18N
        beanFieldLabelColName = resBundle.getString("JBW_DFTC_Label");			 // NOI18N
        htmlElementColName = resBundle.getString("JBW_DFTC_HTMLElement");		 // NOI18N
        initValueColName = resBundle.getString("JBW_DFTC_InitalValue");			 // NOI18N
        initReadOnly = resBundle.getString("JBW_DFTC_ReadOnly");				 // NOI18N

    }

    //// table model handling

    public int getColumnCount() { return NUM_COLUMNS; }

    public int getRowCount() { return jspBeanFields.size(); }

    public String getColumnName(int column) {
        switch (column) {
        case BEAN_CLASS_COL:
            return beanClassColName;
        case BEAN_FIELD_COL:
            return beanFieldColName;
        case FIELD_LABEL_COL:
            return beanFieldLabelColName;
        case FIELD_ELEMENT_COL:
            return htmlElementColName;
        case FIELD_INIT_COL:
            return initValueColName;
        default:
            return "";														 // NOI18N
        }
    }

    public Class getColumnClass(int column) {

        switch (column) {
        case BEAN_CLASS_COL:
            return String.class;
        case BEAN_FIELD_COL:
            return String.class;
        case FIELD_LABEL_COL:
            return String.class;
        case FIELD_ELEMENT_COL:
            return JSPBeanField.class;
        case FIELD_INIT_COL:
            return String.class;
        default:
            return Object.class;
        }
    }


    public boolean isCellEditable(int rowIndex, int columnIdx) {
        JSPBeanField jspBeanField = (JSPBeanField) jspBeanFields.get(rowIndex);
        switch (columnIdx) {
        case FIELD_LABEL_COL:
            return true;
        case FIELD_ELEMENT_COL:
            return true;
        case FIELD_INIT_COL:
            return true;
            // return !jspBeanField.isReadOnly();
        default:
            return false;
        }
    }

    public Object getValueAt(int row, int column) {
        JSPBeanField jspBeanField = (JSPBeanField) jspBeanFields.get(row);
        switch (column) {
        case BEAN_CLASS_COL:
            return jspBeanField.jspBean.getBeanInfo().getBeanDescriptor().getBeanClass().getName();
        case BEAN_FIELD_COL:
            return jspBeanField.toString();
        case FIELD_LABEL_COL:
            return jspBeanField.getDisplayLabel();
        case FIELD_ELEMENT_COL:
            return HTMLElement.getHTMLElement(jspBeanField.getHTMLElementType());
        case FIELD_INIT_COL:
            //Debug.println("getting init value "+jspBeanField.getInitValue());
            return jspBeanField.getDisplayInitValue();
            // if(jspBeanField.isReadOnly()) {
            //	  return this.initReadOnly;
            // } else {
            //	  return jspBeanField.getInitValue();
            // }
        default:
            return "";			 // NOI18N
        }
    }

    public void setValueAt(Object aValue, int row, int column) {
        JSPBeanField jspBeanField = (JSPBeanField) jspBeanFields.get(row);
        switch (column) {
        case FIELD_LABEL_COL:
            jspBeanField.setDisplayLabel((String)aValue);
            break;
        case FIELD_ELEMENT_COL:
            jspBeanField.setHTMLElementType(((HTMLElement)aValue).getHTMLTypeID());
            break;
        case FIELD_INIT_COL:
            //Debug.println("Setting init value "+aValue);
            jspBeanField.setDisplayInitValue((String)aValue);
            // if(!jspBeanField.isReadOnly()) {
            //	jspBeanField.setInitValue((String)aValue);
            // }
            break;

        }

        //// todo: notify the change to all model listeners
    }
}



