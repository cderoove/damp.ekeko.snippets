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
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

import javax.swing.table.*;

/** A property editor for array of Strings.
* @author  Ian Formanek
* @version 0.10, 17 Jun 1998
*/
public class TableModelEditor extends Object implements PropertyEditor {

    // the bundle to use
    static ResourceBundle bundle = NbBundle.getBundle (
                                       TableModelEditor.class);

    public TableModelEditor() {
        support = new PropertyChangeSupport (this);
    }

    public Object getValue () {
        return table;
    }

    public void setValue (Object value) {
        table = new NbTableModel ((TableModel) value);
        support.firePropertyChange ("", null, null); // NOI18N
    }

    public String getAsText () {
        return null;
    }

    public void setAsText (String string) {
    }

    public String getJavaInitializationString () {
        TableModel m = (TableModel) getValue ();
        StringBuffer titlesSB = new StringBuffer ();
        int i = m.getColumnCount ();
        int j = m.getRowCount ();
        titlesSB.append ("{\n\t\t"); // NOI18N
        if (i > 0) {
            String s = m.getColumnName (0);
            titlesSB.append ("\"").append (s != null ? s : "").append ('"'); // NOI18N
            for (int k = 1; k < i; k++) {
                String s1 = m.getColumnName (k);
                titlesSB.append (", \"").append (s1 != null ? s1 : "").append ('"'); // NOI18N
            }
        }
        titlesSB.append ("\n\t}"); // NOI18N

        boolean generateTypes = false;
        StringBuffer typesSB = new StringBuffer ();
        typesSB.append ("{\n\t\t"); // NOI18N
        if (i > 0) {
            typesSB.append (m.getColumnClass (0).getName ()).append (".class"); // NOI18N
            if (m.getColumnClass (0) != String.class) generateTypes = true;
            for (int k = 1; k < i; k++) {
                if (m.getColumnClass (k) != String.class) generateTypes = true;
                typesSB.append (", ").append (m.getColumnClass (k).getName ()).append (".class"); // NOI18N
            }
        }
        typesSB.append ("\n\t}"); // NOI18N

        boolean generateEditable = false;
        StringBuffer editableSB = new StringBuffer ();
        editableSB.append ("{\n\t\t"); // NOI18N
        if (i > 0) {
            editableSB.append (m.isCellEditable (0, 0));
            if (!m.isCellEditable (0, 0)) generateEditable = true;
            for (int k = 1; k < i; k++) {
                if (!m.isCellEditable (0, k)) generateEditable = true;
                editableSB.append (", ").append (m.isCellEditable (0, k)); // NOI18N
            }
        }
        editableSB.append ("\n\t}"); // NOI18N

        StringBuffer dataSB = new StringBuffer ();
        dataSB.append ("{\n\t\t"); // NOI18N
        if (j > 0) {
            for (int l = 0; l < j; l++) {
                if (l != 0)
                    dataSB.append (",\n\t\t"); // NOI18N
                if (i == 0) {
                    dataSB.append ("{}"); // NOI18N
                } else {
                    Object obj = m.getValueAt (l, 0);
                    dataSB.append ('{').append (getAsString (obj));
                    for (int i1 = 1; i1 < i; i1++) {
                        obj = m.getValueAt (l, i1);
                        dataSB.append (", ").append (getAsString (obj)); // NOI18N
                    }
                    dataSB.append ('}');
                }
            }
        }
        dataSB.append ("\n\t}"); // NOI18N
        if (generateEditable || generateTypes) {
            return
                "new javax.swing.table.DefaultTableModel (\n" + // NOI18N
                "\tnew Object [][] " + new String (dataSB) + ",\n" + // NOI18N
                "\tnew String [] " + new String (titlesSB) + "\n" + // NOI18N
                ") {\n" + // NOI18N
                (generateTypes ? (
                     "\tClass[] types = new Class [] " + new String (typesSB) + ";\n") : // NOI18N
                 "") + // NOI18N
                (generateEditable ? (
                     "\tboolean[] canEdit = new boolean [] " + new String (editableSB) + ";\n") : // NOI18N
                 "") + // NOI18N
                (generateTypes ? (
                     "\n" + // NOI18N
                     "\tpublic Class getColumnClass (int columnIndex) {\n" + // NOI18N
                     "\t\treturn types [columnIndex];\n" + // NOI18N
                     "\t}\n") : // NOI18N
                 "") + // NOI18N
                (generateEditable ? (
                     "\n" + // NOI18N
                     "\tpublic boolean isCellEditable (int rowIndex, int columnIndex) {\n" + // NOI18N
                     "\t\treturn canEdit [columnIndex];\n" + // NOI18N
                     "\t}\n") : // NOI18N
                 "") + // NOI18N
                "}"; // NOI18N
        } else {
            return
                "new javax.swing.table.DefaultTableModel (\n" + // NOI18N
                "\tnew Object [][] " + new String (dataSB) + ",\n" + // NOI18N
                "\tnew String [] " + new String (titlesSB) + "\n" + // NOI18N
                ")"; // NOI18N
        }
    }

    public String[] getTags () {
        return null;
    }

    public boolean isPaintable () {
        return false;
    }

    public void paintValue (Graphics g, Rectangle rectangle) {
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public Component getCustomEditor () {
        return new CustomTableModelEditor (this);
    }

    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener (propertyChangeListener);
    }

    static String getAsString (Object o) {
        if (o == null) return "null"; // NOI18N

        if (o instanceof String)
            if (o == null) return "\"\""; // NOI18N
            else return "\"" + o + "\""; // NOI18N

        String s = o.getClass ().getName ();
        int g = s.lastIndexOf ('.');
        if (g >= 0) s = s.substring (g + 1, s.length ());

        return "new " + s + " (" + o + ")"; // NOI18N
    }

    static Object getDefaultValue (Class c) {
        return null;
    }

    // -----------------------------------------------------------------------------
    // NbTableModel

    public static class NbTableModel extends AbstractTableModel implements java.io.Externalizable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -6843008677521167210L;

        Object data[][];
        String titles[];
        Class types[];
        boolean editable[];
        transient boolean alwaysEditable = false;

        /** For externalization only */
        public NbTableModel () {
        }

        public NbTableModel (String[] titles, Class[] types, boolean[] editable) {
            this.titles = titles;
            this.types = types;
            this.editable = editable;
            data = new Object [titles.length][4];
        }

        public NbTableModel (TableModel createFrom) {
            if (createFrom == null) {
                data = new Object[4][4];
                titles = new String[] {
                             bundle.getString ("CTL_Title") + " 1",
                             bundle.getString ("CTL_Title") + " 2",
                             bundle.getString ("CTL_Title") + " 3",
                             bundle.getString ("CTL_Title") + " 4",
                         };
                types = new Class[] {
                            String.class, String.class, String.class, String.class
                        };
                editable = new boolean [] {
                               true, true, true, true
                           };
            } else {
                int columns = createFrom.getColumnCount ();
                int rows = createFrom.getRowCount ();

                data = new Object [rows][columns];
                titles = new String [columns];
                types = new Class [columns];
                editable = new boolean [columns];

                for (int i = 0; i < columns; i++) {
                    titles [i] = createFrom.getColumnName (i);
                    types [i] = createFrom.getColumnClass (i);
                    if (createFrom instanceof NbTableModel)
                        editable [i] = ((NbTableModel)createFrom).editable[i];
                    else
                        editable [i] = true;
                    for (int j = 0; j < rows; j++) {
                        data[j][i] = createFrom.getValueAt (j, i);
                    }
                }
            }
        }

        public Class getColumnClass (int i) {
            return types [i];
        }

        public String getColumnName (int i) {
            return titles[i];
        }

        public int getRowCount () {
            return data.length;
        }

        public int getColumnCount () {
            return titles.length;
        }

        public boolean isCellEditable (int i, int j) {
            return alwaysEditable || editable [j];
        }

        public Object getValueAt (int row, int column) {
            return data [row][column];
        }

        public void setValueAt (Object obj, int row, int column) {
            data[row][column] = obj;
            fireTableCellUpdated (row, column);
        }

        void setRowCount (int newRowCount) {
            int col = getColumnCount ();
            int row = getRowCount ();
            if (newRowCount > row) {
                Object[][] newData = new Object [newRowCount][col];
                System.arraycopy (data, 0, newData, 0, row);
                int i;
                for (i = row; i < newRowCount; i++)
                    newData [i] = new Object [col];

                data = newData;
                fireTableRowsInserted (row, newRowCount - 1);
                return;
            }
            if (newRowCount > 0) {
                Object[][] newData = new Object [newRowCount][col];
                System.arraycopy (data, 0, newData, 0, newRowCount);
                int i1 = data.length;
                data = newData;
                fireTableRowsDeleted (newRowCount, i1 - 1);
            }
        }

        void setColumnCount (int newColumnCount)
        {
            int col = getColumnCount ();
            int row = getRowCount ();
            int min = Math.min (newColumnCount, col);
            for (int i = 0; i < row; i++) {
                Object[] line = new Object [newColumnCount];
                System.arraycopy (data [i], 0, line, 0, min);
                data [i] = line;
            }

            String[] newTitles = new String [newColumnCount];
            System.arraycopy (titles, 0, newTitles, 0, min);
            for (int i = min; i < newColumnCount; i ++)
                newTitles [i] = bundle.getString ("CTL_Title") + " " + (i + 1);
            titles = newTitles;

            Class[] newTypes = new Class [newColumnCount];
            System.arraycopy (types, 0, newTypes, 0, min);
            for (int i = min; i < newColumnCount; i ++)
                newTypes [i] = Object.class;
            types = newTypes;

            boolean[] newEditable = new boolean [newColumnCount];
            System.arraycopy (editable, 0, newEditable, 0, min);
            for (int i = min; i < newColumnCount; i ++)
                newEditable [i] = true;
            editable = newEditable;

            fireTableStructureChanged ();
        }

        void fireTableChanged () {
            fireTableStructureChanged ();
        }

        public void writeExternal (java.io.ObjectOutput oo) throws java.io.IOException {
            int rowCount = data.length;
            int columnCount = types.length;

            // row count
            oo.writeInt (rowCount);
            // column count
            oo.writeInt (columnCount);

            oo.writeObject (titles);
            oo.writeObject (editable);
            for (int i = 0; i < columnCount; i++) {
                oo.writeObject (types[i].getName ());
            }

            for (int row = 0; row < rowCount; row++) {
                for (int column = 0; column < columnCount; column++) {
                    if (data[row][column] instanceof java.io.Serializable)
                        oo.writeObject (data[row][column]);
                    else
                        oo.writeObject (null);
                }
            }
        }

        public void readExternal (java.io.ObjectInput oi) throws java.io.IOException, ClassNotFoundException {
            int rowCount = oi.readInt ();
            int columnCount = oi.readInt ();

            titles = (String[]) oi.readObject ();
            editable = (boolean[]) oi.readObject ();

            types = new Class [columnCount];
            for (int i = 0; i < columnCount; i++) {
                try {
                    types[i] = Class.forName ((String)oi.readObject ());
                } catch (ClassNotFoundException e) {
                    types[i] = Object.class;
                }
            }

            data = new Object[rowCount][columnCount];
            for (int row = 0; row < rowCount; row++) {
                for (int column = 0; column < columnCount; column++) {
                    data[row][column] = oi.readObject ();
                }
            }
        }

    } // Class NbTableModel

    // -----------------------------------------------------------------------------
    // private variables

    private NbTableModel table;
    private PropertyChangeSupport support;
}

/*
 * Log
 *  6    Gandalf   1.5         1/13/00  Petr Jiricka    i18n
 *  5    Gandalf   1.4         12/17/99 Pavel Buzek     #4196
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/4/99   Jan Jancura     bundle moved
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */




