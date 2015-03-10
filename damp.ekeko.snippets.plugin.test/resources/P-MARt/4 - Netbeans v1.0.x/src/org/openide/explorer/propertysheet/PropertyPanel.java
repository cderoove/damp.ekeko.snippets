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

package org.openide.explorer.propertysheet;

import java.awt.Component;
import java.awt.BorderLayout;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;

import org.openide.nodes.Node;
import org.openide.util.NbBundle;

// TO DO:
//  1. add PREF_XXX constants
//  2. implement PREF_READ_ONLY
//  3. improve updateComponent method
//  4. i18n

/** Visual Java Bean for editing of properties. It takes the model
* and represents the property editor for it.
*
* @author Jaroslav Tulach, Petr Hamernik, Jan Jancura
*/
public class PropertyPanel extends JComponent {

    /**
    * Constant defining preferences in displaying of value.
    * Value should be displayed in read-only mode.
    */
    public static final int PREF_READ_ONLY = 0x0001;

    /**
    * Constant defining preferences in displaying of value.
    * Value should be displayed in custom editor.
    */
    public static final int PREF_CUSTOM_EDITOR = 0x0002;

    /**
    * Constant defining preferences in displaying of value.
    * Value should be displayed in editor only.
    */
    public static final int PREF_INPUT_STATE = 0x0004;

    /** Name of the 'preferences' property */
    public static final String PROP_PREFERENCES = "preferences"; // NOI18N

    /** Name of the 'model' property */
    public static final String PROP_MODEL = "model"; // NOI18N

    /** Name of the read-only property 'propertyEditor' */
    public static final String PROP_PROPERTY_EDITOR = "propertyEditor"; // NOI18N

    /** Static instance of empty PropertyModel. */
    private static PropertyModel EMPTY_MODEL = new EmptyModel();

    /** Holds value of property preferences. */
    private int preferences = 0;

    /** Holds value of property model. */
    private PropertyModel model;

    /** Listener for the model and prop.editor properties changes. */
    private PropertyChangeListener listener;

    /** Current property editor */
    private PropertyEditor editor;

    /** Status flag - prevention of cycle in fires properties. */
    private boolean ignoreModelEvent = false;

    /** Status flag - prevention of cycle in fires properties. */
    private boolean ignoreEditorEvent = false;

    /** Creates new PropertyPanel with the empty DefaultPropertyModel
    */
    public PropertyPanel () {
        this (EMPTY_MODEL, 0);
    }

    /** Creates new PropertyPanel with DefaultPropertyModel
    * @param bean The instance of bean
    * @param propertyName The name of the property to be displayed
    */
    public PropertyPanel (
        Object bean,
        String propertyName,
        int preferences
    ) {
        this (
            new DefaultPropertyModel (bean, propertyName),
            preferences
        );
    }

    /** Creates new PropertyPanel
    * @param model The model for displaying
    */
    public PropertyPanel (
        PropertyModel model,
        int preferences
    ) {
        this.model = model;
        this.preferences = preferences;
        listener = new PropertyL ();
        setLayout (new BorderLayout ());

        model.addPropertyChangeListener (listener);
        updateEditor ();
        updateComponent ();
    }

    /** Getter for property preferences.
    * @return Value of property preferences.
    */
    public int getPreferences () {
        return preferences;
    }

    /* Setter for visual preferences in displaying
    * of the value of the property.
    * @param pref PREF_XXXX constants
    */
    public void setPreferences (int preferences) {
        int oldPreferences = this.preferences;
        this.preferences = preferences;
        updateComponent ();
        firePropertyChange(
            PROP_PREFERENCES,
            new Integer (oldPreferences),
            new Integer (preferences)
        );
    }

    /** Getter for property model.
    * @return Value of property model.
    */
    public PropertyModel getModel() {
        return model;
    }

    /** Setter for property model.
     *@param model New value of property model.
     */
    public void setModel(PropertyModel model) {
        PropertyModel oldModel = this.model;
        this.model = model;
        oldModel.removePropertyChangeListener(listener);
        model.addPropertyChangeListener(listener);
        updateEditor();
        updateComponent();
        firePropertyChange (PROP_MODEL, oldModel, model);
    }

    /** Update the current property editor depending on the model.
    */
    private void updateEditor() {
        PropertyEditor oldEditor = editor;

        // find new editor
        editor = null;
        Class editorClass = model.getPropertyEditorClass();
        if (editorClass != null) {
            try {
                editor = (PropertyEditor) editorClass.newInstance();
            }
            catch (Exception e) {
                if (Boolean.getBoolean("org.netbeans.exceptions")) // NOI18N
                    e.printStackTrace();
            }
        }
        if (editor == null) {
            Class propertyTypeClass = model.getPropertyType();
            if (propertyTypeClass != null)
                editor = PropertyEditorManager.findEditor(propertyTypeClass);
        } else {
            try {
                editor.setValue(model.getValue());
            }
            catch (InvocationTargetException e) {
                if (Boolean.getBoolean("org.netbeans.exceptions")) // NOI18N
                    e.printStackTrace();
            }
        }

        // listener add and remove
        if (oldEditor != null)
            oldEditor.removePropertyChangeListener(listener);
        if (editor != null)
            editor.addPropertyChangeListener(listener);

        // fire the change
        firePropertyChange(PROP_PROPERTY_EDITOR, oldEditor, editor);
    }

    /**
    * Getter for current property editor depending on the model.
    * It could be <CODE>null</CODE> if there is not possible 
    * to obtain property editor for the current model.
    *
    * @return the property editor or <CODE>null</CODE>
    */
    public PropertyEditor getPropertyEditor() {
        return editor;
    }

    /** Update the content of this Panel depending on model.
    */
    private void updateComponent() {
        removeAll();

        if (editor == null) {
            add (new JLabel (NbBundle.getBundle (PropertyPanel.class).
                             getString ("CTL_No_property_editor")), BorderLayout.CENTER
                );
            return;
        }

        Component c = null;
        if ((getPreferences () & PREF_CUSTOM_EDITOR) != 0)
            c = editor.getCustomEditor();
        if (c == null) {
            c = new PropertyDisplayer ();
            ((PropertyDisplayer) c).setValueAsProperty (new Property (model.getPropertyType ()));
            if ((getPreferences () & PREF_INPUT_STATE) == 0) {
                ((PropertyDisplayer) c).setInputState (false);
                ((PropertyDisplayer) c).setSwitchAutomatically (true);
            } else {
                ((PropertyDisplayer) c).setSwitchAutomatically (false);
            }
        }
        if (c == null)
            c = new JLabel (NbBundle.getBundle (PropertyPanel.class).
                            getString ("CTL_No_custom_editor")
                           );

        add (c, BorderLayout.CENTER);

        try {
            editor.setValue(model.getValue());
        }
        catch (InvocationTargetException e) {
            if (Boolean.getBoolean("org.netbeans.exceptions")) // NOI18N
                e.printStackTrace();
        }
    }


    // innerclasses ..............................................................

    /** Property change listener for the editor and the model.
    */
    private class PropertyL implements PropertyChangeListener {
        /** Property was changed. */
        public void propertyChange(PropertyChangeEvent evt) {

            // MODEL changes
            if (!ignoreModelEvent && (evt.getSource() == model)) {
                if (PropertyModel.PROP_VALUE.equals(evt.getPropertyName())) {
                    if (editor != null) {
                        try {
                            ignoreEditorEvent = true;
                            editor.setValue(model.getValue());
                        }
                        catch (InvocationTargetException e) {
                            if (Boolean.getBoolean("org.netbeans.exceptions")) // NOI18N
                                e.printStackTrace();
                        }
                        finally {
                            ignoreEditorEvent = false;
                        }
                    }
                }
            }
            // EDITOR changes
            else if ((!ignoreEditorEvent) && (editor != null) && (evt.getSource() == editor)) {
                try {
                    ignoreModelEvent = true;
                    model.setValue(editor.getValue());
                }
                catch (InvocationTargetException e) {
                    if (Boolean.getBoolean("org.netbeans.exceptions")) // NOI18N
                        e.printStackTrace();
                }
                finally {
                    ignoreModelEvent = false;
                }
            }
        }
    }

    /** Empty implementation of the PropertyModel interface.
    */
    private static class EmptyModel implements PropertyModel {
        /** @return <CODE>null</CODE> */
        public Object getValue() throws InvocationTargetException {
            return null;
        }

        /** Does nothing. */
        public void setValue(Object v) throws InvocationTargetException {
        }

        /** @return <CODE>Object.class</CODE> */
        public Class getPropertyType() {
            return Object.class;
        }

        /** @return <CODE>null</CODE> */
        public Class getPropertyEditorClass() {
            return null;
        }

        /** Does nothing. */
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        /** Does nothing. */
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
    }

    private class Property extends Node.Property {
        /** Creates property. */
        Property (Class clazz) {
            super (clazz);
        }
        /** Gets value from model. */
        public Object getValue () throws InvocationTargetException {
            return model.getValue ();
        }
        /** Sets value for model. */
        public void setValue (Object val) throws InvocationTargetException {
            model.setValue (val);
        }
        /** Returns PropertyEditor. */
        public PropertyEditor getPropertyEditor () {
            return editor;
        }
        /** @return <CODE>true</CODE>. */
        public boolean canRead () {
            return true;
        }
        /** @return PREF_READ_ONLY value. */
        public boolean canWrite () {
            return (getPreferences () & PREF_READ_ONLY) == 0;
        }
    }
}


/*
* Log
*  5    Gandalf   1.4         1/12/00  Ian Formanek    NOI18N
*  4    Gandalf   1.3         1/9/00   Jan Jancura     [Petr H.] Used PropCh. 
*       supp. form superclass
*  3    Gandalf   1.2         12/9/99  Jan Jancura     PropertyPanel 
*       implementation + Bug 3961
*  2    Gandalf   1.1         12/2/99  Petr Nejedly    setValue for editor 
*       earlier, enable forwarding property chages in finally{}
*  1    Gandalf   1.0         11/25/99 Petr Hamernik   
* $
*/
