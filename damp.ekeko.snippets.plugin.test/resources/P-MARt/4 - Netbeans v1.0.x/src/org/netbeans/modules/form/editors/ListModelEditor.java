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

package org.netbeans.modules.form.editors;

import java.awt.*;
import java.beans.*;
import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import java.text.MessageFormat;

import org.netbeans.modules.form.FormEditor;
import org.openide.explorer.propertysheet.editors.StringArrayCustomizable;
import org.openide.explorer.propertysheet.editors.StringArrayCustomEditor;

/** A property editor for ListModel.
* @author  Ian Formanek
* @version 1.00, 17 Sep 1998
*/
public class ListModelEditor extends Object implements PropertyEditor, StringArrayCustomizable {

    /** Creates a new ListModelEditor */
    public ListModelEditor () {
        support = new PropertyChangeSupport (this);
    }

    public Object getValue () {
        return model;
    }

    public void setValue (Object value) {
        if (! (value instanceof ListModel)) return;
        model = (ListModel) value;
        support.firePropertyChange ("", null, null); // NOI18N
    }

    public void setAsText (String string) {
    }

    public String getAsText () {
        return (model == null) ? FormEditor.getFormBundle().getString("MSG_LM_NoData") : MessageFormat.format(FormEditor.getFormBundle().getString("FMT_MSG_ITEMS"), new Object [] { new Integer (model.getSize ())});
    }

    // -----------------------------------------------------------------------------
    // StringArrayCustomizable implementation

    /** Used to acquire the current value from the PropertyEditor
    * @return the current value of the property
    */
    public String[] getStringArray () {
        ListModel model = (ListModel) getValue ();
        if (model != null) {
            String[] array = new String [model.getSize ()];
            for (int i = 0; i < model.getSize (); i++)
                array[i] = model.getElementAt (i).toString ();
            return array;
        } else return new String [0];
    }

    /** Used to modify the current value in the PropertyEditor
    * @param value the new value of the property
    */
    public void setStringArray (String[] value) {
        setValue (new NbListModel (value));
    }

    public static class NbListModel extends AbstractListModel implements java.io.Serializable {
        static final long serialVersionUID =7587411890999439265L;
        public NbListModel (String[] data) {
            this.data = data;
        }

        public int getSize() { return data.length; }
        public Object getElementAt(int i) { return data[i]; }

        private String[] data;
    }

    // end of StringArrayCustomizable implementation

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
        return new StringArrayCustomEditor (this);
    }

    public String getJavaInitializationString () {
        /* This editor generates something like:
           new AbstractListModel() {
             public int getSize() { return 2; }
             public Object getElementAt(int i) { 
               switch (i) {
                 case 0: return "item at 0";
                 case 1: return "item at 1";
                 default: throw new IndexOutOfBoundsException ();
               }
             }
           }
        */
        if (model == null) return "null"; // NOI18N

        int size = model.getSize ();
        StringBuffer buf = new StringBuffer ("new javax.swing.AbstractListModel() {\n"); // NOI18N
        buf.append ("\t\tpublic int getSize() { return "); // NOI18N
        buf.append (size);
        buf.append ("; }\n"); // NOI18N
        buf.append ("\t\tpublic Object getElementAt(int i) {\n"); // NOI18N
        buf.append ("\t\t\tswitch (i) {\n"); // NOI18N
        for (int i = 0; i < size; i++) {
            String valueString = model.getElementAt (i).toString ();
            // replace all " with \" // NOI18N
            valueString = org.openide.util.Utilities.replaceString (valueString, "\"", "\\\""); // NOI18N

            buf.append ("\t\t\t\tcase "+i); // NOI18N
            buf.append (": return \""); // NOI18N
            buf.append (valueString);
            buf.append ("\";\n"); // NOI18N
        }

        buf.append ("\t\t\t\tdefault: throw new IndexOutOfBoundsException();\n"); // NOI18N
        buf.append ("\t\t\t}\n"); // end of switch // NOI18N
        buf.append ("\t\t}\n"); // end of method getElementAt () // NOI18N
        buf.append ("\t}\n"); // end of innerclass // NOI18N
        return buf.toString ();
    }

    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener (propertyChangeListener);
    }

    protected PropertyChangeSupport support;
    private ListModel model;
}

/*
 * Log
 *  9    Gandalf   1.8         1/13/00  Ian Formanek    NOI18N #2
 *  8    Gandalf   1.7         1/12/00  Pavel Buzek     I18N
 *  7    Gandalf   1.6         1/5/00   Ian Formanek    NOI18N
 *  6    Gandalf   1.5         11/27/99 Patrik Knakal   
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         6/30/99  Ian Formanek    
 *  3    Gandalf   1.2         6/27/99  Ian Formanek    Ignores non-ListModel 
 *       values
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */




