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

public class JSPBeanTableModel extends javax.swing.table.AbstractTableModel {

    JSPVector useBeans;

    /**
     * @associates JSPBean 
     */
    HashMap useBeansMap;

    public static final int BEAN_CLASS_COL = 0;
    public static final int BEAN_VARIABLE_COL = 1;
    public static final int BEAN_SCOPE_COL = 2;
    public static final int NUM_COLUMNS = 3;

    private String beanClassColName;
    private String beanVarColName;
    private String beanScopeColName;

    public JSPBeanTableModel(JSPVector useBeans) {
        this.useBeans = useBeans;
        this.useBeansMap = new HashMap();

        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);
        beanClassColName = resBundle.getString("JBW_BTC_BeanClass");						 // NOI18N
        beanVarColName = resBundle.getString("JBW_BTC_Variable");							 // NOI18N
        beanScopeColName = resBundle.getString("JBW_BTC_Scope");							 // NOI18N

    }

    //// table model handling

    public int getColumnCount() { return NUM_COLUMNS; }

    public int getRowCount() { return useBeans.size(); }

    public String getColumnName(int column) {
        switch (column) {
        case BEAN_CLASS_COL:
            return beanClassColName;
        case BEAN_VARIABLE_COL:
            return beanVarColName;
        case BEAN_SCOPE_COL:
            return beanScopeColName;
        default:
            return "";			 // NOI18N
        }
    }

    public Class getColumnClass(int column) {
        switch (column) {
        case BEAN_CLASS_COL:
            return String.class;
        case BEAN_VARIABLE_COL:
            return String.class;
        case BEAN_SCOPE_COL:
            return JSPBean.class;
        default:
            return Object.class;
        }
    }


    public boolean isCellEditable(int rowIndex, int columnIdx) {
        switch (columnIdx) {
        case BEAN_CLASS_COL:
            return false;
        case BEAN_VARIABLE_COL:
            return true;
        case BEAN_SCOPE_COL:
            return true;
        default:
            return false;
        }

    }

    public Object getValueAt(int row, int column) {
        JSPBean jspBean = (JSPBean) useBeans.get(row);
        switch (column) {
        case BEAN_CLASS_COL:
            return jspBean.getBeanInfo().getBeanDescriptor().getBeanClass().getName();
        case BEAN_VARIABLE_COL:
            return jspBean.getBeanVariableName();
        case BEAN_SCOPE_COL:
            return jspBean.toScopeString(jspBean.getBeanScope());
        default:
            return "";			 // NOI18N
        }
    }

    public void setValueAt(Object aValue, int row, int column) {
        JSPBean jspBean = (JSPBean) useBeans.get(row);
        switch (column) {
        case BEAN_VARIABLE_COL:
            // jspBean.setBeanVariableName((String)aValue);
            setUseBeanVariableName(jspBean, (String)aValue);
            break;
        case BEAN_SCOPE_COL:
            jspBean.setBeanScope( JSPBean.toScopeValue((String)aValue));
            break;
        }

        //// todo: notify the change to all model listeners
    }

    //// key management methods

    void setUseBeanVariableName(JSPBean jspBean, String newVariableName) {
        modifyUseBeanMapKey(jspBean,newVariableName);
    }

    void modifyUseBeanMapKey(JSPBean jspBean, String newKey) {
        if(useBeansMap.get(newKey) == null) {
            useBeansMap.remove(jspBean.getBeanVariableName());
            jspBean.setBeanVariableName(newKey);
            useBeansMap.put(jspBean.getBeanVariableName(),jspBean);
        }
    }

    void setUseBeanMapKey(JSPBean jspBean) {
        try {
            int MAX_KEYS = 100;
            String rootKey = jspBean.getBeanVariableName();
            String useBeanMapKey = rootKey;
            for(int i=0; i < MAX_KEYS; ++i) {
                JSPBean foundJSPBean = (JSPBean) useBeansMap.get(useBeanMapKey);
                if(foundJSPBean == null) {
                    jspBean.setBeanVariableName(useBeanMapKey);
                    useBeansMap.put(jspBean.getBeanVariableName(),jspBean);
                    return;
                }
                useBeanMapKey = rootKey+i;
            }
            //NB must through exception
            // throw new Exception("Can not add more than 100 variables of the same type")

        }catch (Exception ex) {Debug.print(ex);}
    }

    void addToKeyMap(JSPBean jspBean) {
        try {
            setUseBeanMapKey(jspBean);
            useBeansMap.put(jspBean.getBeanVariableName(),jspBean);
        }catch(Exception ex) { Debug.print(ex);}
    }

    void removeFromKeyMap(JSPBean jspBean) {
        try {
            useBeansMap.remove(jspBean.getBeanVariableName());
        }catch(Exception ex){Debug.print(ex);}
    }

    void removeAllFromKeyMap() {
        useBeansMap.clear();
    }

    void removeAllFromKeyMap(Object jspItemKey) {
        Map removeMap = new HashMap();
        Iterator mapIterator = useBeansMap.values().iterator();
        for(;mapIterator.hasNext();) {
            JSPBean jspBean = (JSPBean)mapIterator.next();
            if(jspBean.getKey().equals(jspItemKey))
                removeMap.put(jspBean.getBeanVariableName(),jspBean);
        }


        mapIterator = removeMap.values().iterator();
        for(;mapIterator.hasNext();) {
            JSPBean jspBean = (JSPBean)mapIterator.next();
            useBeansMap.remove(jspBean.getBeanVariableName());
        }
    }

    //// USE BEAN MODEL methos

    public void add(JSPBean jspBean) {
        addToKeyMap(jspBean);
        useBeans.add(jspBean);
        int idx = useBeans.size()-1;
        this.fireTableRowsInserted(idx,idx,jspBean);
    }

    public JSPBean remove(int idx) {
        JSPBean jspBean = (JSPBean) useBeans.remove(idx);
        removeFromKeyMap(jspBean);
        this.fireTableRowsDeleted(idx,idx,jspBean);
        return jspBean;
    }

    public int moveUp(int idx) {
        int newIdx = useBeans.moveUp(idx);
        this.fireTableDataChanged();
        return newIdx;
    }

    public int moveDown(int idx) {
        int newIdx = useBeans.moveDown(idx);
        this.fireTableDataChanged();
        return newIdx;
    }

    public void removeAll() {
        int idx = useBeans.size()-1;
        useBeans.removeAllElements();
        removeAllFromKeyMap();
        if(idx >= 0 )
            this.fireTableRowsDeleted(0,idx,null);
    }

    public boolean removeAll(Object key) {
        boolean itemsRemoved = useBeans.removeAllByKey(key);
        removeAllFromKeyMap(key);

        if(itemsRemoved) {
            this.fireTableDataChanged();
        }

        return itemsRemoved;
    }


    public JSPVector getJSPBeans() { return useBeans; }

    public void fireTableRowsDeleted(int firstRow, int lastRow, JSPBean jspBean) {
        JSPBeanTableModelEvent evt = new JSPBeanTableModelEvent(this, firstRow, lastRow,
                                     TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
        evt.setJSPBean(jspBean);        super.fireTableChanged(evt);

    }

    public void fireTableRowsInserted(int firstRow, int lastRow, JSPBean jspBean) {
        JSPBeanTableModelEvent evt = new JSPBeanTableModelEvent(this, firstRow, lastRow,
                                     TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
        evt.setJSPBean(jspBean);        super.fireTableChanged(evt);
    }

    public class JSPBeanTableModelEvent extends TableModelEvent {
        JSPBean jspBean = null;
        public JSPBeanTableModelEvent(TableModel source, int firstRow,
                                      int lastRow,
                                      int column,
                                      int type){
            super(source,firstRow,lastRow,column,type);
        }
        public JSPBean getJSPBean() { return jspBean; }
        public void setJSPBean(JSPBean jspBean) { this.jspBean = jspBean;}
    }

}


