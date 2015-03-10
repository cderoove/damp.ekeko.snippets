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

package org.netbeans.modules.form.compat2.border;

import java.awt.*;
import java.beans.*;
import java.util.*;
import javax.swing.border.Border;

import org.openide.nodes.*;
import org.netbeans.modules.form.FormPropertyEditorManager;
import org.netbeans.modules.form.FormUtils;

/** An abstract superclass of description of
*
* @author   Petr Hamernik
*/
public abstract class BorderInfoSupport extends BorderInfo {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 8572675385766227289L;

    public BorderInfoSupport() {
    }

    /** Generates the code into the StringBuffer.
    * @param buf where to generate
    */
    public void generateCode(StringBuffer buf) {
        int pos = buf.length();
        try {
            buf.append("new "); // NOI18N
            buf.append(border.getClass().getName());
            buf.append("("); // NOI18N
            generateConstrParams(buf);
            buf.append(")"); // NOI18N
        }
        catch (IllegalStateException e) {
            buf.setLength(pos);
            buf.append("null"); // NOI18N
        }
    }

    /** @return array of constructors described like indexes into array of props.
    * e.g.
    * props are: int x, int y, int w, int h.
    * available constructors are BInfo(), BInfo(int x, int y), BInfo(int x, int y, int w, int h)
    * => getConstructors() returns: { { }, { 0, 1 }, {0,1,2,3} }
    */
    protected abstract int[][] getConstructors();

    /** Generates params depending on modified props and
    * which constructor is most suitable for use in this case.
    */
    private void generateConstrParams(StringBuffer buf) {
        Node.Property[] props = getProperties();
        BitSet bit = new BitSet(props.length);
        for (int i = 0; i < props.length; i++) {
            if (!isDefault(props[i])) {
                bit.set(i);
            }
        }
        int[][] constr = getConstructors();
        for (int i = 0; i < constr.length; i++) {
            int size = constr[i].length;
            BitSet tmp = (BitSet) bit.clone();
            for (int j = 0; j < size; j++) {
                tmp.clear(constr[i][j]);
            }

            boolean ok = true;
            for (int j = 0; j < tmp.size(); j++) {
                if (tmp.get(j)) {
                    ok = false;
                    break;
                }
            }

            if (ok) {
                int currentLen = 0;
                final String tab = getTab();

                for (int j = 0; j < size; j++) {
                    Node.Property prop = props[constr[i][j]];
                    PropertyEditor ed = prop.getPropertyEditor();
                    if (ed == null) {
                        throw new IllegalStateException();
                    }
                    else {
                        try {
                            Object value = prop.getValue();

                            if (value instanceof Border) {
                                buf.append(tab);
                                currentLen = 0;
                            }

                            ed.setValue(value);
                            String addingStr = ed.getJavaInitializationString();
                            if (addingStr == null) {
                                addingStr = "null"; // we need to generate the "null" as it is needed for CompoundBorder // NOI18N
                            }

                            int breakLine = addingStr.lastIndexOf('\n');
                            if (breakLine == -1) {
                                if ((currentLen > 0) && (currentLen + addingStr.length() > 80)) {
                                    buf.append(tab);
                                    currentLen = addingStr.length();
                                }
                                else {
                                    currentLen += addingStr.length();
                                }
                            }
                            else {
                                currentLen = addingStr.length() - breakLine;
                            }

                            buf.append(addingStr);
                        }
                        catch (java.lang.reflect.InvocationTargetException e) {
                        }
                        catch (IllegalAccessException e) {
                        }
                    }

                    if (j < size - 1) {
                        buf.append(", "); // NOI18N
                    }
                }
                return;
            }
        }
        throw new IllegalStateException();
    }

    private static final String getTab() {
        StringBuffer buf = new StringBuffer("\n"); // NOI18N
        int count = 2; // (new org.netbeans.modules.editor.EditorSettingsJava()).getTabSize();
        for (int i = 0; i < count; i++, buf.append(" ")); // NOI18N
        return buf.toString();
    }

    protected boolean isDefault(Node.Property prop) {
        return (prop instanceof BorderProp) ? ((BorderProp) prop).isDefault() : false;
    }

    /** A ReadWrite property (just a simple descendant of PropertySupport
    * which passes to the super constructor appropriate canR and canW)
    * This class is intended to be overwritten with implementation of
    * getValue and setValue methods.
    */
    public static abstract class BorderProp extends PropertySupport.ReadWrite {
        PropertyChangeListener l;

        /** Constructs a new ReadWrite property with specified parameters. The
        * name, displayName, shortDescription and expert are set up for the
        * property in this constructor.
        * @param name        The name of the property
        * @param type        The class type of the property
        * @param displayName The displayName of the property
        */
        public BorderProp(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        /** @return true if the property is default */
        abstract boolean isDefault();

        protected void firePropChange() {
            if (l != null)
                l.propertyChange(new PropertyChangeEvent(this, null, null, null));
        }

        public void setPropertyChangeListener(PropertyChangeListener l) {
            this.l = l;
        }

        /** Get a property editor for this property.
        * The default implementation tries to use {@link java.beans.PropertyEditorManager}.
        * @return the property editor, or <CODE>null</CODE> if there is no editor
        */
        public PropertyEditor getPropertyEditor () {
            if (getValueType () == null) return null;
            return FormPropertyEditorManager.findEditor(getValueType ());
        }

    }
}

/*
 * Log
 *  7    Gandalf   1.6         1/13/00  Ian Formanek    NOI18N #2
 *  6    Gandalf   1.5         1/12/00  Ian Formanek    NOI18N
 *  5    Gandalf   1.4         12/9/99  Pavel Buzek     
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/30/99  Ian Formanek    Fix for finding property
 *       editors
 *  1    Gandalf   1.0         4/2/99   Ian Formanek    
 * $
 */
