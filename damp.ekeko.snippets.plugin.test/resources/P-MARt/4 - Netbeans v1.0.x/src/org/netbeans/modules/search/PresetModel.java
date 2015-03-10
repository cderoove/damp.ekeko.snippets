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

package org.netbeans.modules.search;

import java.beans.*;
import java.util.*;

import javax.swing.*;

import org.openide.*;

import org.openidex.search.*;

/**
 * Takes care for preset values.
 * delegates most functionality to parent.
 *
 * <p>Listens: PROP_STATE on parent CriterionModel
 *
 * @author  Petr Kuzel
 * @version 
 */
public class PresetModel implements PropertyChangeListener {

    private final CriterionModel parent;
    private final Class type;
    private String name;
    private DefaultComboBoxModel comboModel;

    /** Creates new PresetModel */
    public PresetModel(CriterionModel parent, Class type) {
        this.parent = parent;
        this.type = type;
        comboModel = new DefaultComboBoxModel(getNames());
        comboModel.setSelectedItem(parent.DO_NOT_APPLY);
        parent.addPropertyChangeListener(this);
    }

    /** @return proper combobox model.*/
    public ComboBoxModel getComboBoxModel() {
        return comboModel;
    }


    /** Move model to new state.
    */
    public void usePreset(String state) {
        parent.usePreset(state);
    }

    public boolean isInitialized() {
        return parent.isInitialized();
    }

    /**
    * Save as new System Option bean.
    */
    public void saveAs(String name) throws IllegalArgumentException {
        if (parent.saveAs(name))
            comboModel.addElement(name);
        comboModel.setSelectedItem(name);
    }

    /** Listen on customized event -> leave */
    public void propertyChange(final PropertyChangeEvent event) {

        if (CriterionModel.PROP_STATE.equals(event.getPropertyName())) {
            String state = event.getNewValue().toString();
            comboModel.setSelectedItem(state);
        }
    }

    /** @return true if restore can be used. */
    public boolean canRestore() {
        return ! (
                   parent.getState().equals(parent.APPLY) ||
                   parent.getState().equals(parent.DO_NOT_APPLY)
               ) ;
    }

    /** @return true if can be saved. */
    public boolean canSave() {
        return //(! parent.getState().equals(parent.DO_NOT_APPLY)) &&
            parent.isModified();
    }

    public void addPropertyChangeListener(PropertyChangeListener lis) {
        parent.addPropertyChangeListener(lis);
    }

    public void removePropertyChangeListener(PropertyChangeListener lis) {
        parent.removePropertyChangeListener(lis);
    }

    /** @return Vector of names of presets.
    */
    private Vector getNames() {

        TreeSet ret = new TreeSet();
        ret.add(parent.DO_NOT_APPLY);
        ret.add(parent.APPLY);

        // test all search types

        Enumeration en = TopManager.getDefault().getServices().services(type);

        while (en.hasMoreElements()) {

            SearchType next = (SearchType) en.nextElement();
            String name = next.getName();

            if (name != null)  ret.add(name);
        }

        return new Vector(ret);

    }

}


/*
* Log
*  6    Gandalf-post-FCS1.4.1.0     4/4/00   Petr Kuzel      Comments + output window 
*       fix
*  5    Gandalf   1.4         1/10/00  Petr Kuzel      Buttons enabling.
*  4    Gandalf   1.3         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  3    Gandalf   1.2         1/4/00   Petr Kuzel      Bug hunting.
*  2    Gandalf   1.1         12/23/99 Petr Kuzel      Architecture improved.
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 

