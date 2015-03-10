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

import org.netbeans.modules.web.wizards.beanjsp.util.JSPVector;
import org.netbeans.modules.web.util.*;

public abstract class AbstractJSPFieldTableModel extends javax.swing.table.AbstractTableModel {

    protected JSPVector jspBeanFields;

    public AbstractJSPFieldTableModel(JSPVector jspBeanFields) {
        this.jspBeanFields = jspBeanFields;
    }

    //// USE BEAN MODEL methos

    public void add(JSPBeanField jspBeanField) {
        jspBeanFields.add(jspBeanField);
        int idx = jspBeanFields.size()-1;
        this.fireTableRowsInserted(idx,idx);
        // this.fireTableDataChanged();
    }

    public JSPBeanField remove(int idx) {
        JSPBeanField jspBeanField = (JSPBeanField) jspBeanFields.remove(idx);
        this.fireTableRowsDeleted(idx,idx);
        return jspBeanField;
    }

    public int moveUp(int idx) {
        int newIdx = jspBeanFields.moveUp(idx);
        this.fireTableDataChanged();
        return newIdx;
    }

    public int moveDown(int idx) {
        int newIdx = jspBeanFields.moveDown(idx);
        this.fireTableDataChanged();
        return newIdx;
    }

    public void removeAll() {
        int idx = jspBeanFields.size()-1;
        jspBeanFields.removeAllElements();
        if(idx >= 0 )
            this.fireTableRowsDeleted(0,idx);
    }

    public boolean removeAll(Object key) {
        boolean itemsRemoved = jspBeanFields.removeAllByKey(key);

        if(itemsRemoved) {
            this.fireTableDataChanged();
        }

        return itemsRemoved;
    }

    public void addItems(JSPVector newItems) {
        if(newItems.size() <= 0)
            return;
        int idx = jspBeanFields.size()-1;
        jspBeanFields.addAll(newItems);
        if(idx < 0 )
            idx = 0;
        this.fireTableRowsInserted(idx, idx+newItems.size());
    }



public JSPVector getJSPBeanFields() { return jspBeanFields; }


    public Vector getHTMLElementTypes(int row, int column) {
        JSPBeanField jspBeanField = (JSPBeanField) jspBeanFields.get(row);
        return jspBeanField.getHTMLElementChoices();
    }

}



