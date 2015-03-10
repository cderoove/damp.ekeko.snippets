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

import java.awt.*;
import java.beans.*;
import java.util.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.*;

import org.openidex.search.*;

import org.netbeans.modules.search.res.*;


/**
 * Represent one criterion during customization. The criterion
 * may be in different states: [un]modified. Also links the criterion
 * with its customizer and storage of own presets.
 *
 * <p>Listens: <every property on customized bean>
 * <p>Fires: PROP_CUSTOMIZED, PROP_STATE
 *
 * @author  Petr Kuzel
 * @version  
 */
public class CriterionModel implements PropertyChangeListener {

    /** ID of predefined system type. */
    public final static String DO_NOT_APPLY = Res.text("DO_NOT_APPLY"); // NOI18N
    public final static String APPLY = Res.text("APPLY"); // NOI18N

    /** Customized property name. */
    public static final String PROP_CUSTOMIZED = "customized";
    private boolean modified;

    /** Inner state property name. */
    public static final String PROP_STATE = "state";
    private String state;

    private static final String MODIFICATOR_SUFFIX = " *";

    private PresetModel presetModel;
    private SearchType criterion;
    private CriteriaModel parent;
    private Class type;

    // beaninfo of criterion and customizer obtained from it
    private BeanInfo beanInfo;
    private Customizer customizer;
    private Component customizerComponent;

    /** Utility field used by bound properties. */
    private PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport (this);

    /** Creates new PresetModel */
    public CriterionModel(CriteriaModel parent, Class type) {

        state = DO_NOT_APPLY;
        modified = false;

        this.parent = parent;
        this.type = type;

        criterion = getInstance(type, null);

        presetModel = new PresetModel(this, type);

        try{

            beanInfo = org.openide.util.Utilities.getBeanInfo(type);

            if (hasCustomizer()) {

                customizer = getCustomizer();
                customizerComponent = (Component) customizer;

            } else {
                // TODO use property sheet as it will implement Customizer
                // allow hiding tabs, ....
                System.err.println("No custonizer for " + criterion.getName() + ", skipping...");
            }

        } catch (java.beans.IntrospectionException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
        }

        customizer.setObject(criterion);
        criterion.addPropertyChangeListener(this);

    }

    public boolean hasCustomizer () {
        // true if we have already computed beanInfo and it has customizer class
        return beanInfo.getBeanDescriptor ().getCustomizerClass () != null;
    }

    /**
    * Get name of this criterion used as tab name, 
    * it is mangled if the criterion is modified.
    * @return my name. 
    */
    public String getName() {
        String name;
        if (criterion.getTabText() == null)
            name = criterion.getDisplayName();
        else
            name = criterion.getTabText();

        if (isModified())
            return  name + MODIFICATOR_SUFFIX; //NOI18N
        else
            return  name;
    }

    /** @return relaled customizer compoment. */
    public Component getComponent() {
        return customizerComponent;
    }

    /** */
    public Customizer getCustomizer() {

        if (customizer != null) return customizer;

        Class clazz = beanInfo.getBeanDescriptor ().getCustomizerClass ();
        if (clazz == null) return null;

        Object o;
        try {
            o = clazz.newInstance ();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }

        if (!(o instanceof java.awt.Component) ||
                !(o instanceof java.beans.Customizer)) return null;

        return (Customizer) o;

    }

    public CriteriaModel getParent() {
        return parent;
    }

    public PresetModel getPresetModel() {
        return presetModel;
    }

    public Class getCriterionClass() {
        return criterion.getClass();
    }

    /** Change customizer value according to preset value. */
    public void usePreset(String state) {

        boolean old = modified;
        String oldState = this.state;

        if (state.equals(DO_NOT_APPLY)) {
            modified = false;
        } else if (state.equals(APPLY)) {
            if (oldState.equals(DO_NOT_APPLY)) modified = true;
        } else {
            modified = true;
            criterion = getInstance(type, state);
            getCustomizer().setObject(criterion);
            criterion.addPropertyChangeListener(this);
        }

        //    t("State " + oldState + " -> " + state + " : " + modified); // NOI18N
        //    t("Value " + criterion ); // NOI18N

        this.state = state;

        propertyChangeSupport.firePropertyChange(
            PROP_CUSTOMIZED, old,  modified // NOI18N
        );

        propertyChangeSupport.firePropertyChange(
            PROP_STATE, oldState, state // NOI18N
        );

    }

    /** @return current state.
    */
    public String getState() {
        return state;
    }

    private String defaultState() {

        return APPLY;
    }

    /**
    * @return true if new value was created
    */
    public boolean saveAs(String name) throws IllegalArgumentException {

        boolean flag = true;

        // protect build in settings
        if (name.equals(DO_NOT_APPLY) || name.equals(APPLY))
            throw new IllegalArgumentException();

        criterion.setName(name);


        // overwrite existing
        if (Registry.exist(criterion)) {
            Registry.remove(criterion);
            flag = false;
        }

        Registry.append(criterion);
        return flag;
    }

    public void propertyChange(PropertyChangeEvent event) {

        if (event.getSource() == criterion) {

            boolean old = modified;

            // if Type fires valid property change listens for
            // its invalidity -> mark itself as unmodified
            if (SearchType.PROP_VALID.equals(event.getPropertyName()) ) {
                if (event.getNewValue().equals(new Boolean(false))) {
                    modified = false;
                    propertyChangeSupport.firePropertyChange (
                        PROP_CUSTOMIZED, old, modified // NOI18N
                    );
                }

            } else { //any modification -> modified = true

                modified = true;

                propertyChangeSupport.firePropertyChange (
                    PROP_CUSTOMIZED, old, modified // NOI18N
                );

                if (state.equals(DO_NOT_APPLY))
                    propertyChangeSupport.firePropertyChange (
                        PROP_STATE, state, defaultState() // NOI18N
                    );
            }
        }
    }


    /** This fire no property cange use parent. */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /** Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener (l);
    }

    public boolean isModified () {
        return modified;
    }


    /** Return currently hold bean. */
    public SearchType getCriterion () {
        return criterion;
    }


    /** Class equality
    * @return this.bean.getClass().equals(bean.getClass());
    */
    public boolean equals(Object obj) {
        try {
            return criterion.getClass().equals(((CriterionModel)obj).getCriterionClass());
        } catch (ClassCastException ex) {
            return false;
        }
    }


    /**
    * @param type class os SearchType which instance is required
    * @param name of instance (from control panel)
    * @return preset instance of search type or default instance if not found
    */
    private SearchType getInstance(Class type, String name) {

        // use named instance

        if ( type != null ) {

            Enumeration en = TopManager.getDefault().getServices().services(type);

            while (en.hasMoreElements()) {

                SearchType next = (SearchType) en.nextElement();

                if (next.getName().equals(name)) {
                    return (SearchType) next.clone();
                }
            }
        }

        // otherwise use default instance

        Object ret = null;

        try {
            ret = type.newInstance();

        } catch (InstantiationException ex) {
            // ignore such search type
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();

        } catch (IllegalAccessException ex) {
            // ignore such search type
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
        }

        return (SearchType) ret;

    }

    public boolean isInitialized() {
        return parent.isInitialized();
    }

    public String toString() {
        return "CriterionModel: " + type + " state:" + state + criterion; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return criterion.getHelpCtx();
    }


}


/*
* Log
*  9    Gandalf-post-FCS1.5.2.2     4/4/00   Petr Kuzel      Comments + output window 
*       fix
*  8    Gandalf-post-FCS1.5.2.1     3/9/00   Petr Kuzel      I18N
*  7    Gandalf-post-FCS1.5.2.0     2/24/00  Ian Formanek    Post FCS changes
*  6    Gandalf   1.5         1/13/00  Radko Najman    I18N
*  5    Gandalf   1.4         1/12/00  Petr Kuzel      Html tag removed.
*  4    Gandalf   1.3         1/10/00  Petr Kuzel      Buttons enabling.
*  3    Gandalf   1.2         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  2    Gandalf   1.1         1/4/00   Petr Kuzel      Bug hunting.
*  1    Gandalf   1.0         12/23/99 Petr Kuzel      
* $ 
*/ 

