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
import org.openide.src.Identifier;
import org.openide.util.Utilities;

/** Property editors for array of org.openide.src.Identifier
*
* @author Petr Hamernik
*/
public class IdentifierArrayEditor extends PropertyEditorSupport {

    /** Custom property editor Component. */
    IdentifierArrayPanel panel;

    /** Flag for prevention of cycle in firing
    * of the properties changes.
    */
    boolean ignoreEditor = false;

    /** Flag for prevention of cycle in firing
    * of the properties changes.
    */
    boolean ignorePanel = false;

    /** @return text representation of the value */
    public String getAsText() {
        Identifier[] id = (Identifier []) getValue();
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < id.length; i++) {
            if (i > 0)
                buf.append(", "); // NOI18N
            buf.append(id[i].getSourceName());
        }

        return buf.toString();
    }

    /** Sets the value as the text */
    public void setAsText(String text) throws IllegalArgumentException {
        StringTokenizer tukac = new StringTokenizer(text, ", ", false); // NOI18N
        ArrayList list = new ArrayList();

        while (tukac.hasMoreTokens()) {
            list.add(Identifier.create(tukac.nextToken()));
        }

        Identifier[] ret = new Identifier[list.size()];
        list.toArray(ret);
        setValue(ret);
    }

    /** Set new value */
    public void setValue(Object o) {
        ignoreEditor = true;
        super.setValue(o);
        if ((panel != null) & !ignorePanel) {
            panel.setIdentifiers((Identifier[])o);
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
            panel = new IdentifierArrayPanel();

            panel.setIdentifiers((Identifier[])getValue());

            panel.addPropertyChangeListener(new PropertyChangeListener() {
                                                public void propertyChange(PropertyChangeEvent evt) {
                                                    if (!ignoreEditor && IdentifierArrayPanel.PROP_IDENTIFIERS.equals(evt.getPropertyName())) {
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
    static class IdentifierArrayPanel extends ObjectArrayPanel {

        /** Name of the 'identifiers' property. */
        public static final String PROP_IDENTIFIERS = "identifiers"; // NOI18N

        /** Previous value */
        Identifier[] prevValue;

        static final long serialVersionUID =-8655189809250688928L;
        /** Constructor */
        public IdentifierArrayPanel() {
            prevValue = new Identifier[0];

            this.getListComponent().setCellRenderer(new DefaultListCellRenderer() {
                                                        public java.awt.Component getListCellRendererComponent(JList list,
                                                                Object value, int index, boolean isSelected, boolean cellHasFocus) {
                                                            java.awt.Component comp = super.getListCellRendererComponent(list,
                                                                                      value, index, isSelected, cellHasFocus);
                                                            if (comp == this) {
                                                                setText(((Identifier)value).getFullName());
                                                            }
                                                            return comp;
                                                        }
                                                    });
        }

        /** @return the current value */
        public Identifier[] getIdentifiers() {
            Identifier[] ret = new Identifier[model.size()];
            model.copyInto(ret);
            return ret;
        }

        /** Set new value.
        */
        public void setIdentifiers(Identifier[] data) {
            model = new DefaultListModel();
            if (data != null) {
                for (int i = 0; i < data.length; i++)
                    model.addElement(data[i]);
            }
            this.getListComponent().setModel(model);
            modelChanged();
        }

        /** Fire the 'identifiers' property change. */
        protected void modelChanged() {
            Identifier[] newValue = getIdentifiers();
            firePropertyChange(PROP_IDENTIFIERS, prevValue, newValue);
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
            return openInputDialog((Identifier) oldValue);
        }

        /** Show dialog and allow user to enter new name.
        * @param defName Default value which is predefined.
        * @param titleKey the key to resource bundle for the title of input dialog
        * @return New valid name or <CODE>null</CODE> if user cancel the operation.
        */
        protected Identifier openInputDialog(Identifier origValue) {
            NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine(
                                                   bundle.getString("LAB_NewName"),
                                                   bundle.getString("LAB_NewIdentifier")
                                               );
            if (origValue != null)
                input.setInputText(origValue.getSourceName());

            for (;;) {
                Object ret = TopManager.getDefault().notify(input);
                if (ret == NotifyDescriptor.OK_OPTION) {
                    String retValue = input.getInputText();
                    if (retValue != null) {
                        if (!retValue.startsWith(".") && !retValue.endsWith(".") && // NOI18N
                                (retValue.indexOf("..") == -1)) { // NOI18N
                            boolean ok = true;
                            StringTokenizer tokenizer = new StringTokenizer(retValue, ".", false); // NOI18N
                            while (tokenizer.hasMoreTokens()) {
                                String token = tokenizer.nextToken();
                                if (!Utilities.isJavaIdentifier(token)) {
                                    ok = false;
                                    break;
                                }
                            }
                            if (ok)
                                return Identifier.create(retValue);
                        }
                    }
                    TopManager.getDefault().notify(new NotifyDescriptor.Message(bundle.getString("MSG_NotValidID")));
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
*  8    Gandalf   1.7         12/2/99  Petr Hamernik   fixed bug #4849
*  7    Gandalf   1.6         11/29/99 Petr Hamernik   NullPointerException 
*       bugfix
*  6    Gandalf   1.5         11/26/99 Patrik Knakal   
*  5    Gandalf   1.4         11/26/99 Petr Hamernik   Custom editor added - 
*       based on ObjectArrayPanel
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         6/30/99  Ian Formanek    Moved to package 
*       org.openide.explorer.propertysheet.editors
*  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         4/30/99  Petr Hamernik   
* $ 
*/
