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

package org.openide.explorer.propertysheet.editors;

import java.beans.*;
import java.util.*;

import javax.swing.*;

import org.openide.*;
import org.openide.src.MethodParameter;
import org.openide.src.Type;
import org.openide.util.Utilities;

/** Property editor for array of org.openide.src.MethodParameter classes
*
* @author Petr Hamernik
*/
public class MethodParameterArrayEditor extends PropertyEditorSupport {

    /** Custom property editor Component. */
    MethodParameterArrayPanel panel;

    /** Flag for prevention of cycle in firing
    * of the properties changes.
    */
    boolean ignoreEditor = false;

    /** Flag for prevention of cycle in firing
    * of the properties changes.
    */
    boolean ignorePanel = false;

    /**
    * @return The property value as a human editable string.
    * <p>   Returns null if the value can't be expressed as an editable string.
    * <p>   If a non-null value is returned, then the PropertyEditor should
    *       be prepared to parse that string back in setAsText().
    */
    public String getAsText() {
        MethodParameter[] params = (MethodParameter[]) getValue();
        StringBuffer buf = new StringBuffer();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (i > 0)
                    buf.append(", "); // NOI18N
                buf.append(params[i].getSourceString());
            }
        }
        return buf.toString();
    }

    /** Set the property value by parsing a given String.  May raise
    * java.lang.IllegalArgumentException if either the String is
    * badly formatted or if this kind of property can't be expressed
    * as text.
    * @param text  The string to be parsed.
    */
    public void setAsText(String text) throws IllegalArgumentException {
        StringTokenizer tok = new StringTokenizer(text, ",", false); // NOI18N
        ArrayList list = new ArrayList();
        while (tok.hasMoreTokens()) {
            list.add(MethodParameter.parse(tok.nextToken()));
        }
        MethodParameter[] params = new MethodParameter[list.size()];
        list.toArray(params);
        setValue(params);
    }

    /** Sets the value */
    public void setValue(Object o) {
        ignoreEditor = true;
        super.setValue(o);
        if ((panel != null) & !ignorePanel) {
            panel.setMethodParameters((MethodParameter[])o);
        }
        ignoreEditor = false;
    }

    /** @return <CODE>true</CODE> */
    public boolean supportsCustomEditor () {
        return true;
    }

    /** Create new panel for this property editor.
    * @return the visual component for editing the property
    */
    public java.awt.Component getCustomEditor () {
        if (panel == null) {
            panel = new MethodParameterArrayPanel();

            panel.setMethodParameters((MethodParameter[]) getValue());

            panel.addPropertyChangeListener(new PropertyChangeListener() {
                                                public void propertyChange(PropertyChangeEvent evt) {
                                                    if (!ignoreEditor && MethodParameterArrayPanel.PROP_METHOD_PARAMETERS.equals(evt.getPropertyName())) {
                                                        ignorePanel = true;
                                                        setValue(evt.getNewValue());
                                                        ignorePanel = false;
                                                    }
                                                }
                                            });

        }
        return panel;
    }

    /** Implementation of the abstract ObjectArrayPanel class.
    * It is used for editing of arrays of Identifier objects.
    */
    static class MethodParameterArrayPanel extends ObjectArrayPanel {
        /** Name of the 'methodParameters' property */
        public static final String PROP_METHOD_PARAMETERS = "methodParameters"; // NOI18N

        /** Previous value */
        MethodParameter[] prevValue;

        /** Constructor */
        public MethodParameterArrayPanel() {
            prevValue = new MethodParameter[0];

            this.getListComponent().setCellRenderer(new DefaultListCellRenderer() {
                                                        public java.awt.Component getListCellRendererComponent(JList list,
                                                                Object value, int index, boolean isSelected, boolean cellHasFocus) {
                                                            java.awt.Component comp = super.getListCellRendererComponent(list,
                                                                                      value, index, isSelected, cellHasFocus);
                                                            if (comp == this) {
                                                                setText(((MethodParameter)value).toString());
                                                            }
                                                            return comp;
                                                        }
                                                    });
        }

        /** @return the current value
        */
        public MethodParameter[] getMethodParameters() {
            MethodParameter[] ret = new MethodParameter[model.size()];
            model.copyInto(ret);
            return ret;
        }

        /** Set new value
        */
        public void setMethodParameters(MethodParameter[] data) {
            model = new DefaultListModel();
            if (data != null) {
                for (int i = 0; i < data.length; i++)
                    model.addElement(data[i]);
            }
            this.getListComponent().setModel(model);
            modelChanged();
        }

        /** Fire the 'methodParameters' property change. */
        protected void modelChanged() {
            super.modelChanged();
            MethodParameter[] newValue = getMethodParameters();
            firePropertyChange(PROP_METHOD_PARAMETERS, prevValue, newValue);
            prevValue = newValue;
        }

        /** Ask user for new value.
        * @return new value or <CODE>null</CODE> when 
        *    operation was canceled.
        */
        protected Object insertNewValue() {
            return openInputDialog(null);
        }

        /** Ask user for edit value.
        * @param oldValue The previous value to be edited
        * @return new value or <CODE>null</CODE> when 
        *    operation was canceled.
        */
        protected Object editValue(Object oldValue) {
            return openInputDialog((MethodParameter) oldValue);
        }

        /** Show dialog and allow user to enter new method parameter.
        * @param defName Default value which is predefined.
        * @param titleKey the key to resource bundle for the title of input dialog
        * @return New valid name or <CODE>null</CODE> if user cancel the operation.
        */
        protected MethodParameter openInputDialog(MethodParameter origValue) {
            MethodParameterPanel panel = new MethodParameterPanel();

            NotifyDescriptor desriptor = new NotifyDescriptor(
                                             panel,
                                             bundle.getString("LAB_EnterParameter"),
                                             NotifyDescriptor.OK_CANCEL_OPTION,
                                             NotifyDescriptor.PLAIN_MESSAGE, null, null);

            if (origValue != null) {
                panel.nameTextField.setText(origValue.getName().toString());
                panel.typeCombo.setSelectedItem(origValue.getType().toString());
                panel.finalCheckBox.setSelected(origValue.isFinal());
            }

            for (;;) {
                Object ret = TopManager.getDefault().notify(desriptor);
                if (ret == NotifyDescriptor.OK_OPTION) {
                    String errMsg = null;
                    String name = panel.nameTextField.getText();
                    if (!Utilities.isJavaIdentifier(name))
                        errMsg = "MSG_NotValidID"; // NOI18N
                    else {
                        try {
                            Type type = Type.parse(panel.typeCombo.getSelectedItem().toString());
                            boolean isFinal = panel.finalCheckBox.isSelected();
                            return new MethodParameter(name, type, isFinal);
                        }
                        catch (IllegalArgumentException e) {
                            errMsg = "MSG_NotValidType"; // NOI18N
                        }
                    }
                    TopManager.getDefault().notify(new NotifyDescriptor.Message(bundle.getString(errMsg)));
                }
                else {
                    return null;
                }
            }
        }
    }
}

/*
* Log
*  10   Gandalf-post-FCS1.8.1.0     4/3/00   Svatopluk Dedic Displays source names 
*       instead of full names
*  9    Gandalf   1.8         1/12/00  Ian Formanek    NOI18N
*  8    Gandalf   1.7         1/5/00   Jaroslav Tulach Deleted all 
*       NotifyDescriptior constructors that take Icon as argument.
*  7    Gandalf   1.6         12/2/99  Petr Hamernik   fixed bug #4849
*  6    Gandalf   1.5         11/29/99 Petr Hamernik   NullPointerException 
*       bugfix
*  5    Gandalf   1.4         11/26/99 Petr Hamernik   custom editor added - 
*       using ObjectArrayEditor
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         6/30/99  Ian Formanek    Moved to package 
*       org.openide.explorer.propertysheet.editors
*  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         4/30/99  Petr Hamernik   
* $
*/
