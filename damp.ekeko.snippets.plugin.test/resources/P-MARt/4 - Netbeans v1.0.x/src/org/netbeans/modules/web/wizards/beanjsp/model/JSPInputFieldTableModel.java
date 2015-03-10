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

public class JSPInputFieldTableModel extends AbstractJSPFieldTableModel {

    public static final int BEAN_CLASS_COL = 0;
    public static final int BEAN_FIELD_COL = 1;
    public static final int QUERY_PARAM_COL = 2;

    public static final int NUM_COLUMNS = 3;

    private String beanClassColName;
    private String beanFieldColName;
    private String queryParamColName;


    public JSPInputFieldTableModel(JSPVector jspBeanFields) {
        super(jspBeanFields);

        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);			 // NOI18N
        beanClassColName = resBundle.getString("JBW_IFTC_BeanClass");								 // NOI18N
        beanFieldColName = resBundle.getString("JBW_IFTC_Field");									 // NOI18N
        queryParamColName = resBundle.getString("JBW_IFTC_QueryParam");								 // NOI18N

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
        case QUERY_PARAM_COL:
            return queryParamColName;
        default:
            return "";		 // NOI18N
        }
    }

    public Class getColumnClass(int column) {

        switch (column) {
        case BEAN_CLASS_COL:
            return String.class;
        case BEAN_FIELD_COL:
            return String.class;
        case QUERY_PARAM_COL:
            return String.class;
        default:
            return Object.class;
        }
    }


    public boolean isCellEditable(int rowIndex, int columnIdx) {

        switch (columnIdx) {
        case QUERY_PARAM_COL:
            // return true;  //NB change it to editable in next ver
            return false;
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
        case QUERY_PARAM_COL:
            return jspBeanField.getQueryParam();
        default:
            return "";			 // NOI18N
        }
    }

    public void setValueAt(Object aValue, int row, int column) {
        JSPBeanField jspBeanField = (JSPBeanField) jspBeanFields.get(row);
        switch (column) {
        case QUERY_PARAM_COL:
            jspBeanField.setQueryParam((String)aValue);
            break;
        }

        //// todo: notify the change to all model listeners
    }
}



