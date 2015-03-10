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

package org.netbeans.beaninfo.editors;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.ResourceBundle;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/** A custom property editor for TableModel.
* @author  Jan Jancura, Ian Formanek
* @version 1.00, 06 Oct 1998
*/
public class CustomTableModelEditor extends JPanel implements EnhancedCustomPropertyEditor {

    // the bundle to use
    static ResourceBundle bundle = NbBundle.getBundle (
                                       CustomTableModelEditor.class);

    private JTable defaultValuesTable;
    private JTextField rowsField;
    private JTextField columnsField;
    private TableModelEditor.NbTableModel model;
    private TableModelEditor.NbTableModel titleModel;
    private boolean isCreated = false;
    private boolean isChangingTableModel = false;

    static final long serialVersionUID =8002510111948803668L;
    public CustomTableModelEditor (TableModelEditor editor) {
        model = new TableModelEditor.NbTableModel ((TableModel)editor.getValue ());

        setLayout (new BorderLayout ());
        setBorder (new EmptyBorder (6, 6, 6, 6));

        JTabbedPane tabbedPane = new JTabbedPane ();
        JPanel tab = new JPanel ();
        tab.setLayout (new BorderLayout (6, 6));
        tab.setBorder (new EmptyBorder (6, 0, 0, 6));
        tab.add ("North", new JLabel ( // NOI18N
                     bundle.getString ("CTL_Title1")
                 ));

        JTable settingsTable = new JTable ();
        settingsTable.addKeyListener (new KeyAdapter () {
                                          public void keyPressed (KeyEvent e) {
                                              if (e.getKeyChar () == KeyEvent.VK_ENTER) {
                                                  e.consume ();
                                              }
                                          }
                                      });
        titleModel = new TableModelEditor.NbTableModel (
                         new String[] {
                             bundle.getString ("CTL_Column"),
                             bundle.getString ("CTL_Title"),
                             bundle.getString ("CTL_Type"),
                             bundle.getString ("CTL_Editable")
                         },
                         new Class[] {
                             String.class, String.class, String.class, Boolean.class
                         },
                         new boolean[] {
                             false, true, true, true
                         }
                     );
        settingsTable.setModel (titleModel);

        JComboBox comboBox = new JComboBox();
        comboBox.addItem ("String"); // NOI18N
        comboBox.addItem ("Boolean"); // NOI18N
        comboBox.addItem ("Integer"); // NOI18N
        comboBox.addItem ("Byte"); // NOI18N
        comboBox.addItem ("Short"); // NOI18N
        comboBox.addItem ("Long"); // NOI18N
        comboBox.addItem ("Float"); // NOI18N
        comboBox.addItem ("Double"); // NOI18N
        comboBox.addItem ("Character"); // NOI18N
        comboBox.setSelectedIndex (0);

        TableColumn typeColumn = settingsTable.getColumn (bundle.getString ("CTL_Type"));
        typeColumn.setCellEditor (new DefaultCellEditor (comboBox));

        JScrollPane jscrollpane = new JScrollPane (settingsTable);
        settingsTable.setSelectionMode (0);
        settingsTable.setCellSelectionEnabled (true);
        settingsTable.setRowSelectionAllowed (true);
        settingsTable.setColumnSelectionAllowed (true);
        settingsTable.setPreferredScrollableViewportSize (new Dimension (450, 300));
        tab.add ("Center", jscrollpane); // NOI18N

        JPanel sizePanel = new JPanel ();
        sizePanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;

        JLabel rowsLabel = new JLabel ();
        rowsLabel.setText (bundle.getString ("CTL_Rows"));
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.insets = new java.awt.Insets (0, 0, 0, 5);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        sizePanel.add (rowsLabel, gridBagConstraints1);

        rowsField = new JTextField ();
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (0, 0, 5, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.ipadx = 20;
        sizePanel.add (rowsField, gridBagConstraints1);

        JLabel columnsLabel = new JLabel ();
        columnsLabel.setText (bundle.getString ("CTL_Columns"));
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.insets = new java.awt.Insets (0, 0, 0, 5);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        sizePanel.add (columnsLabel, gridBagConstraints1);

        columnsField = new JTextField ();
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.ipadx = 20;
        sizePanel.add (columnsField, gridBagConstraints1);

        JPanel padding = new JPanel ();
        padding.setLayout (new java.awt.FlowLayout ());

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        sizePanel.add (padding, gridBagConstraints1);

        tab.add  ("East", sizePanel); // NOI18N

        tabbedPane.addTab (bundle.getString ("CTL_Title2"), tab);

        tab = new JPanel ();
        tab.setLayout (new BorderLayout (6, 6));
        tab.setBorder (new EmptyBorder (6, 0, 0, 0));

        tab.add ("North", new JLabel (bundle.getString ("CTL_DefaultTableValues")));

        defaultValuesTable = new JTable ();
        defaultValuesTable.setModel (model);
        model.alwaysEditable = true;
        jscrollpane = new JScrollPane (defaultValuesTable);
        defaultValuesTable.setSelectionMode (0);
        defaultValuesTable.setCellSelectionEnabled (true);
        defaultValuesTable.setRowSelectionAllowed (true);
        defaultValuesTable.setColumnSelectionAllowed (true);
        defaultValuesTable.setPreferredScrollableViewportSize (new Dimension (450, 80));
        defaultValuesTable.addKeyListener (new KeyAdapter () {
                                               public void keyPressed (KeyEvent evt) {
                                                   if (evt.getKeyChar () == KeyEvent.VK_ENTER) {
                                                       evt.consume ();
                                                   }
                                               }
                                           });
        tab.add ("Center", jscrollpane); // NOI18N

        tabbedPane.addTab (bundle.getString ("CTL_DefaultValues"), tab);

        add ("Center", tabbedPane); // NOI18N

        rowsField.setText (String.valueOf (model.getRowCount ()));
        columnsField.setText (String.valueOf (model.getColumnCount ()));
        rowsField.addFocusListener (new FocusAdapter () {
                                        public void focusLost (FocusEvent evt) {
                                            updateRows (rowsField.getText ());
                                        }
                                    }
                                   );
        rowsField.addActionListener (new ActionListener () {
                                         public void actionPerformed (ActionEvent evt) {
                                             updateRows (rowsField.getText ());
                                         }
                                     }
                                    );
        columnsField.addFocusListener (new FocusAdapter () {
                                           public void focusLost (FocusEvent evt) {
                                               updateColumns (columnsField.getText ());
                                           }
                                       }
                                      );
        columnsField.addActionListener (new ActionListener () {
                                            public void actionPerformed (ActionEvent evt) {
                                                updateColumns (columnsField.getText ());
                                            }
                                        }
                                       );
        updateTitleModel ();
        isCreated = true;

        HelpCtx.setHelpIDString (this, CustomTableModelEditor.class.getName ());

        titleModel.addTableModelListener (new TableModelListener () {
                                              public void tableChanged (TableModelEvent evt) {
                                                  updateDefaultTable ();
                                              }
                                          }
                                         );
    }

    private void updateRows (String text) {
        int i = 0;
        try {
            i = Integer.parseInt (text);
        } catch (NumberFormatException e) {
            return;
        }
        if (i < 1) return;
        model.setRowCount (i);
    }

    private void updateColumns (String text) {
        int i = 0;
        try {
            i = Integer.parseInt (text);
        } catch (NumberFormatException e) {
            return;
        }
        if (i < 1) return;
        model.setColumnCount (i);
        updateTitleModel ();
    }

    void updateDefaultTable () {
        if (model==null || isChangingTableModel) return; //at component creation
        int cols = model.getColumnCount ();
        int rows = model.getRowCount ();
        int i;
        boolean typeChanged = false;
        for (i = 0; i < cols; i++) {
            model.titles [i] = (String) titleModel.data [i] [1];
            String t = (String) titleModel.data [i] [2];
            Class newType;
            try {
                newType = Class.forName ("java.lang." + t); // NOI18N
            } catch (Exception e) {
                newType = String.class;
            }
            if (!newType.equals (model.types [i])) {
                typeChanged = true;
                model.types [i] = newType;
                int j;
                for (j = 0; j < rows; j++)
                    model.data [j] [i] = null; // getDefaultValue (newType); [PENDING]
            }
            model.editable [i] = ((Boolean)titleModel.data [i] [3]).booleanValue ();
        }
        if (typeChanged) defaultValuesTable.createDefaultColumnsFromModel ();
        model.fireTableChanged ();
    }

    /**
    * @return Returns the property value that is result of the CustomPropertyEditor.
    * @exception InvalidStateException when the custom property editor does not represent valid property value
    *            (and thus it should not be set)
    */
    public Object getPropertyValue () throws IllegalStateException {
        updateDefaultTable ();
        return new TableModelEditor.NbTableModel (model);
    }

    void updateTitleModel () {
        isChangingTableModel=true;
        int cols = model.getColumnCount ();
        if (cols != titleModel.getRowCount ()) {
            isCreated = false;
            titleModel.setRowCount (cols);
            isCreated = true;
        }
        int i;
        for (i = 0; i < cols; i++) {
            titleModel.data [i][0] = "" + (i + 1); // NOI18N
            titleModel.data [i][1] = model.titles [i];
            String s = model.types [i].getName ();
            int g = s.lastIndexOf ('.');
            if (g >= 0) s = s.substring (g + 1, s.length ());
            titleModel.data [i][2] = s;
            titleModel.data [i][3] = new Boolean (model.editable [i]);
        }
        isChangingTableModel = false;
        updateDefaultTable ();
    }
} // class CustomTableModelEditor

/*
 * Log
 *  10   Gandalf   1.9         1/13/00  Petr Jiricka    i18n
 *  9    Gandalf   1.8         12/17/99 Pavel Buzek     #4196
 *  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  6    Gandalf   1.5         8/2/99   Ian Formanek    Fixed bug 1826 - The 
 *       table model property sheet is unusable, Plus improved focus 
 *  5    Gandalf   1.4         7/8/99   Jesse Glick     Context help.
 *  4    Gandalf   1.3         6/30/99  Ian Formanek    Reflecting changes in 
 *       editors packages and enhanced property editor interfaces
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/4/99   Jan Jancura     bundle moved
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */




