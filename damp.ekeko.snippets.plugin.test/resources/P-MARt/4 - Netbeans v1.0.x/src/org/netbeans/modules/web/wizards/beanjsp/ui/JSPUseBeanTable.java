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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import org.netbeans.modules.web.wizards.beanjsp.model.*;
import org.netbeans.modules.web.wizards.wizardfw.*;
import org.netbeans.modules.web.wizards.beanjsp.ide.netbeans.*;

import org.netbeans.modules.web.util.*;
import org.netbeans.modules.web.wizards.beanjsp.util.*;

import org.openide.util.*;


public class JSPUseBeanTable extends JScrollPane {

    public JSPUseBeanTable() {
        super();
        initComponents ();
    }

    private void initComponents () {

        useBeansTable = new JTable();
        this.setViewportView(useBeansTable);

        setTableModel(new JSPBeanTableModel(new JSPVector()));

    }

    private javax.swing.JTable useBeansTable;
    private AbstractTableModel jspBeansTableModel;

    public void setTableModel(JSPBeanTableModel tableModel) {
        jspBeansTableModel = tableModel;
        useBeansTable.setModel(jspBeansTableModel);
        useBeansTable.setPreferredScrollableViewportSize(new Dimension(200,100));

        TableColumn scopeColumn = useBeansTable.getColumnModel().getColumn(JSPBeanTableModel.BEAN_SCOPE_COL);
        JComboBox scopeCombo = new JComboBox(JSPBean.getScopeList());
        scopeColumn.setCellEditor(new DefaultCellEditor(scopeCombo));

        //Set up tool tips for the sport cells.
        DefaultTableCellRenderer renderer =
            new DefaultTableCellRenderer();
        // renderer.setToolTipText("Click for combo box");
        scopeColumn.setCellRenderer(renderer);

    }


    //// delegation to JTable

    public JTable getTable() { return useBeansTable; }

    public static void main(String[] args) {
        if(Debug.TEST) {
            JFrame testFrame = new JFrame("This is Test Frame");   // NOI18N
            BeanManager beanManager = new BeanManager();
            Collection jspBeans = beanManager.getValidJSPBeans("d:\\dev\\taal\\classes","jspbeans");   // NOI18N

            JSPUseBeanTable jspUseBeanTable = new JSPUseBeanTable();
            jspUseBeanTable.setTableModel(new JSPBeanTableModel((JSPVector)jspBeans));

            testFrame.getContentPane().add(jspUseBeanTable,SwingConstants.CENTER);
            testFrame.setSize(500,300);
            testFrame.pack();
            testFrame.show();
        }
    }

}


