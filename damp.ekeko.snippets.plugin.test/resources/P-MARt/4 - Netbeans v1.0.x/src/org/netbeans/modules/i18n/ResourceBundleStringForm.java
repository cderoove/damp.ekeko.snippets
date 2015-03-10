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

import javax.swing.event.*;
import java.util.Vector;
import java.util.Enumeration;

import org.netbeans.modules.form.FormDesignValue;
import org.netbeans.modules.form.RADComponent;

import org.netbeans.modules.properties.ResourceBundleString;
import org.netbeans.modules.properties.BundleStructure;
import org.netbeans.modules.properties.PropertiesFileEntry;
import org.netbeans.modules.properties.PropertiesStructure;
import org.netbeans.modules.properties.PropertiesDataObject;
import org.netbeans.modules.properties.PropertyBundleEvent;
import org.netbeans.modules.properties.PropertyBundleListener;
import org.netbeans.modules.properties.WeakListenerPropertyBundle;

/**
 *
 * @author  pjiricka
 * @version 
 */
public class ResourceBundleStringForm extends ResourceBundleString implements FormDesignValue.Listener {

    static final long serialVersionUID =-5754505794855958684L;
    public ResourceBundleStringForm() {
        super();
    }

    public ResourceBundleStringForm(ResourceBundleString source) {
        super(source);
    }

    public Object getDesignValue (RADComponent radComponent) {
        String value = getPropertyValue();
        if (value == null)
            return IGNORED_VALUE;
        return value;
    }

    public void setResourceBundle(PropertiesDataObject resourceBundle) {
        // remove myself from the old listener
        super.setResourceBundle(resourceBundle);
        if (getResourceBundle() != null) {
            BundleStructure bs = getResourceBundle().getBundleStructure();
            if (bs != null) {
                bundList = new PropertyBundleListener() {
                               public void bundleChanged(PropertyBundleEvent evt) {
                                   switch (evt.getChangeType()) {
                                   case PropertyBundleEvent.CHANGE_STRUCT:
                                       fireChange();
                                       break;
                                   case PropertyBundleEvent.CHANGE_ALL:
                                       fireChange();
                                       break;
                                   case PropertyBundleEvent.CHANGE_FILE:
                                       if (fileMatches(evt.getEntryName()))
                                           fireChange();
                                       break;
                                   case PropertyBundleEvent.CHANGE_ITEM:
                                       if (fileMatches(evt.getEntryName()) && evt.getItemName().equals(getKey()))
                                           fireChange();
                                       break;
                                   }
                               }
                           };
                bs.addPropertyBundleListener(new WeakListenerPropertyBundle(bundList));
            }
        }
        fireChange();
    }

    private boolean fileMatches(String entryName) {
        // pending - locale sensitive ?
        return (getResourceBundle().getPrimaryFile().getName().equals(entryName));
    }


    public void setKey(String key) {
        super.setKey(key);
        fireChange();
    }

    protected void fireChange() {
        if (listvec != null) {
            Vector vecclone = (Vector)listvec.clone();
            Enumeration enum = vecclone.elements();
            ChangeEvent evt = new ChangeEvent(this);
            while(enum.hasMoreElements()) {
                ChangeListener elist = (ChangeListener)enum.nextElement();
                elist.stateChanged(evt);
            }
        }
    }


    public void addChangeListener (ChangeListener listener) {
        if (listvec == null) listvec = new Vector(1);
        listvec.add(listener);
    }

    public void removeChangeListener (ChangeListener listener) {
        if (listvec != null) listvec.remove(listener);
    }

    private transient PropertyBundleListener bundList;

    /**
     * @associates ChangeListener 
     */
    private transient Vector listvec;

}

/*
 * <<Log>>
 *  7    Gandalf   1.6         11/27/99 Patrik Knakal   
 *  6    Gandalf   1.5         10/25/99 Petr Jiricka    Various bugfixes
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/17/99  Petr Jiricka    Serialization
 *  3    Gandalf   1.2         8/2/99   Petr Jiricka    
 *  2    Gandalf   1.1         8/1/99   Petr Jiricka    
 *  1    Gandalf   1.0         7/29/99  Petr Jiricka    
 * $
 */
