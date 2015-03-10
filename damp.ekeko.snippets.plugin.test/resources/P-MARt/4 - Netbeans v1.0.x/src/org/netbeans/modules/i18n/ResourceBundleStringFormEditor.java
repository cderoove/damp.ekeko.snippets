/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.i18n;

import java.text.MessageFormat;

import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.FormAwareEditor;
import org.netbeans.modules.form.FormDesignValue;
import org.netbeans.modules.form.NamedPropertyEditor;

import org.netbeans.modules.properties.ResourceBundleStringEditor;
import org.netbeans.modules.properties.ResourceBundlePanel;
import org.netbeans.modules.properties.ResourceBundleString;
import org.netbeans.modules.properties.PropertiesModule;
import org.netbeans.modules.properties.Util;

/**
 *
 * @author  pjiricka
 * @version 
 */
public class ResourceBundleStringFormEditor extends ResourceBundleStringEditor
    implements FormAwareEditor, NamedPropertyEditor {

    protected RADComponent component;
    protected String propertyName;


    /** Creates new ResourceBundleStringEditorForm */
    public ResourceBundleStringFormEditor() {
    }

    public java.awt.Component getCustomEditor () {

        ResourceBundlePanel pan = new ResourceBundlePanel ();
        ResourceBundleStringForm newValue = new ResourceBundleStringForm((ResourceBundleStringForm)getValue());
        pan.setValue(newValue);
        return pan;
    }


    public Object getValue() {
        return getValueInternal(super.getValue());
    }

    protected String doFormatting() {
        return MessageFormat.format(javaStringFormat, new Object[] {
                                        currentValue.getResourceBundle().getPrimaryFile().getPackageName('/'),
                                        currentValue.getResourceBundle().getPrimaryFile().getPackageName('.'),
                                        currentValue.getKey(),
                                        component.getFormManager().getFormObject().getName()
                                    });
    }

    private ResourceBundleStringForm getValueInternal(Object oldValue) {
        ResourceBundleStringForm newValue;
        if ((oldValue == null) || !(oldValue instanceof ResourceBundleStringForm)) {

            newValue = new ResourceBundleStringForm();
            if ((oldValue != null) && (oldValue instanceof ResourceBundleString)) {
                newValue = new ResourceBundleStringForm((ResourceBundleString)oldValue);
            }
            else {
                newValue = new ResourceBundleStringForm();
            }
        }
        else
            newValue = (ResourceBundleStringForm)oldValue;
        if (newValue.getResourceBundle() == null)
            newValue.setResourceBundle(PropertiesModule.getLastBundleUsed());
        makeDefaultValue(newValue, oldValue);
        return newValue;
    }

    private void makeDefaultValue(ResourceBundleStringForm myValue, Object originalValue) {
        // find out the default value
        String defValue = null;
        if (originalValue != null)
            if (myValue.getPropertyValue() == null) {
                if (originalValue instanceof String)
                    defValue = (String)originalValue;
                else
                    if (originalValue instanceof ResourceBundleString) {
                        defValue = ((ResourceBundleString)originalValue).getDefaultValue();
                    }
                    else
                        if (originalValue instanceof FormDesignValue) {
                            Object desValue = ((FormDesignValue)originalValue).getDesignValue(component);
                            if (desValue instanceof String)
                                defValue = (String)desValue;
                        }
            }
        // set the key - find a key which is not being used
        if (myValue.getKey() == null) {
            if (propertyName == null) {
                // no property
                String curKey = component.getFormManager().getFormObject().getName() + "." + component.getName();
                int index;
                if ((defValue != null) && (defValue.length() > 0)) {
                    curKey += "." + Util.stringToKey(defValue);
                    myValue.setKey(curKey);
                    index = 0;
                }
                else  {
                    myValue.setKey(curKey + ".1");
                    index = 1;
                }
                while (myValue.getPropertyValue() != null) {
                    index ++;
                    myValue.setKey(curKey + "." + index);
                }
            }
            else {
                // there is a property
                String curKey = component.getFormManager().getFormObject().getName() + "." + component.getName() +
                                "." + propertyName;
                int index = 0;
                myValue.setKey(curKey);
                while (myValue.getPropertyValue() != null) {
                    index ++;
                    myValue.setKey(curKey + "." + index);
                }
            }
        }
        // set the default value
        if (myValue.getDefaultValue() == null)
            myValue.setDefaultValue(defValue);
    }

    public void setValue(Object value) {
        currentValue = getValueInternal(value);
    }

    /** If a property editor or customizer implements the FormAwareEditor
    * interface, this method is called immediately after the PropertyEditor
    * instance is created or the Customizer is obtained from getCustomizer ().
    * @param component The RADComponent representing the JavaBean being edited by this 
    *                  property editor or customizer
    * @param property  The RADProperty being edited by this property editor or null 
    *                  if this interface is implemented by a customizer
    */
    public void setRADComponent (RADComponent component, RADComponent.RADProperty property) {
        this.component = component;
        setPropertyName(property.getName ());
    }

    /** Sets the property name. If called, must be after setRADComponent. */
    public void setPropertyName (String propertyName) {
        this.propertyName = propertyName;
    }

    /** @return display name of the property editor */
    public String getDisplayName () {
        return org.openide.util.NbBundle.getBundle(ResourceBundleStringFormEditor.class).getString("CTL_PropertyEditorName");
    }

}

/*
 * <<Log>>
 *  13   Gandalf   1.12        10/25/99 Petr Jiricka    Various bugfixes
 *  12   Gandalf   1.11        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   Gandalf   1.10        9/12/99  Ian Formanek    FormAwareEditor.setRADComponent
 *        changes
 *  10   Gandalf   1.9         9/8/99   Petr Jiricka    
 *  9    Gandalf   1.8         9/2/99   Petr Jiricka    
 *  8    Gandalf   1.7         8/19/99  Petr Jiricka    Format string allows 
 *       class name
 *  7    Gandalf   1.6         8/17/99  Petr Jiricka    Implements namededitor
 *  6    Gandalf   1.5         8/17/99  Petr Jiricka    Changes with default 
 *       value
 *  5    Gandalf   1.4         8/4/99   Petr Jiricka    Fixed compilation 
 *       problem
 *  4    Gandalf   1.3         8/3/99   Petr Jiricka    Now clones the value 
 *       before passing it to CustomEditor
 *  3    Gandalf   1.2         8/2/99   Petr Jiricka    
 *  2    Gandalf   1.1         8/1/99   Petr Jiricka    
 *  1    Gandalf   1.0         7/29/99  Petr Jiricka    
 * $
 */
