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
import javax.swing.event.*;

import org.openide.*;
import org.openide.util.*;
import org.openide.nodes.*;

import org.openidex.search.*;

import org.netbeans.modules.search.res.*;

/**
 * Holds data of customized criteria. Collects
 * CriterionModels. Provides context help switching.
 *
 * <p>Presumtion: View is organized as TabbedPane so help
 * context switching is supported because criteria are
 * determined at runtime.
 *
 * <p>Listens: at each criterion model for PROP_CUSTOMIZED
 * <p>Fires: PROP_CUSTOMIZED if any criterion fired it
 * 
 * @author  Petr Kuzel
 * @version 
 */
public class CriteriaModel implements PropertyChangeListener, ChangeListener {


    // constructor parameters

    /** Set of seatch type classes. */
    private  Set criteriaClasses;

    /** The starting point of this criteria. */
    private Node[] nodes;

    /** Current map SearchType.class -> current CriterionModel.
     * @associates CriterionModel
    */
    private HashMap criteria;

    /** int -> CriterionModel
     * @associates CriterionModel
    */
    private Vector orderedCriteria;

    //combo box hack in PresetView
    private boolean initialized = true;

    private boolean TRACE = false;

    /** Utility field used by bound properties. */
    private PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport(this);

    private SingleSelectionModel tabModel;

    /** Customized property name. */
    public static final String PROP_CUSTOMIZED = "customized";
    private boolean customized = false;

    /** Creates new CriteriaModel
    * @param nodes starting point.
    * @param classes search types available.
    */
    public CriteriaModel(Node[] nodes, Set classes) {

        this.nodes = nodes;
        criteriaClasses = classes;
        criteria = new HashMap();
        orderedCriteria = new Vector();
        tabModel = new DefaultSingleSelectionModel();
        tabModel.addChangeListener(this);

        // default values of criteria

        Iterator it = classes.iterator();

        while (it.hasNext()) {

            Class next = (Class) it.next();

            t("Constructing: " + next); // NOI18N

            CriterionModel cust = new CriterionModel(this, next);
            cust.addPropertyChangeListener(this);
            criteria.put(next, cust);
        }

        initOrderedCriteria();
    }

    /** Initialized the vector. Includes only supported search types in
    * order defined by search services order.
    */
    private void initOrderedCriteria() {

        Enumeration en = TopManager.getDefault().getServices().services(SearchType.class);

        while (en.hasMoreElements()) {

            Class next = en.nextElement().getClass();

            if (criteriaClasses.contains(next)) {
                CriterionModel assoc = (CriterionModel) criteria.get(next);
                if ( ! orderedCriteria.contains(assoc) ) {
                    orderedCriteria.add(assoc);
                }
            }
        }
    }

    /** Query models handling customization.
    * @return properly ordered CriterionModel-s. 
    */
    public Iterator getCustomizers() {
        return orderedCriteria.iterator();
    }


    /** Query modified criteria.
    * @return current state of customized criteria.
    */
    public SearchType[] getCustomizedCriteria() {
        // convert entered criteria to array

        int size = 0;

        Iterator it = criteria.values().iterator();
        while(it.hasNext()) {
            CriterionModel next = (CriterionModel) it.next();
            if (next.isModified()) size++;
        }

        t("modified criteria count: " + size); // NOI18N

        SearchType[] criteriaArray = new SearchType[size];

        int index = 0;

        Iterator it2 = criteria.values().iterator();
        while(it2.hasNext()) {
            CriterionModel next = (CriterionModel) it2.next();
            if (next.isModified()) {
                criteriaArray[index++] = (SearchType) next.getCriterion();
                t(">" + next.getCriterion()); // NOI18N
            }
        }

        return criteriaArray;

    }

    /** @return node[] representing starting nodes. */
    public Node[] getNodes() {
        return nodes;
    }

    /** @return true if some criterion customized. */
    public boolean isCustomized() {
        return customized;
    }

    /** @return name of criterion at index is modified. */
    public String getTabText(int index) {
        try {
            return ((CriterionModel) orderedCriteria.get(index)).getName();
        } catch (ArrayIndexOutOfBoundsException ex) {
            return null;
        }
    }

    /** Add a PropertyChangeListener to the listener list.
     * @param l listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener (l);
    }

    /** Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener (l);
    }

    /** Listens for "customized" from all included CriterionModels. */ // NOI18N
    public void propertyChange(final java.beans.PropertyChangeEvent event) {

        t("Some criterion customized?"); // NOI18N

        if (CriterionModel.PROP_CUSTOMIZED.equals(event.getPropertyName())) {

            boolean old = customized;
            customized = getCustomizedCriteria().length != 0;

            propertyChangeSupport.firePropertyChange (
                PROP_CUSTOMIZED, old, customized // NOI18N
            );

        }
    }

    /** View support method - it provides milked SingleSelectionModel.
    * Context help is assigned according it.
    * @return SingleSelectionModel that is monitored by this. 
    */
    public SingleSelectionModel getTabModel() {
        return tabModel;
    }

    /** @return this as string.
    */
    public String toString() {
        String ret;
        ret = "CriteriaModel: \n"; // NOI18N
        Iterator it = criteria.values().iterator();
        while(it.hasNext()) {
            ret = ret + it.next().toString() + "\n"; // NOI18N
        }
        ret = ret + "/CriteriaModel"; // NOI18N

        return ret;
    }

    private void t(String msg) {
        if (TRACE)
            System.err.println("CriteriaM: " + msg);
    }

    /** Hacking code.
    */
    public boolean isInitialized() {
        return initialized;
    }

    /** Hacking code.
    */
    public void setInitialized(boolean value) {
        initialized = value;
    }

    /** The context help must be changed.
    */
    public void stateChanged(ChangeEvent e) {
        propertyChangeSupport.firePropertyChange(
            "help", null, getHelpCtx() // NOI18N
        );
    }

    /** Return current HelpCtx. */
    public HelpCtx getHelpCtx() {
        int index = tabModel.getSelectedIndex();
        CriterionModel model = (CriterionModel) orderedCriteria.get(index);
        return model.getHelpCtx();
    }

}


/*
* Log
*  10   Gandalf-post-FCS1.8.1.0     4/4/00   Petr Kuzel      Comments + output window 
*       improvements.
*  9    Gandalf   1.8         1/13/00  Radko Najman    I18N
*  8    Gandalf   1.7         1/10/00  Petr Kuzel      Buttons enabling.
*  7    Gandalf   1.6         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  6    Gandalf   1.5         1/4/00   Petr Kuzel      Bug hunting.
*  5    Gandalf   1.4         12/23/99 Petr Kuzel      Architecture improved.
*  4    Gandalf   1.3         12/20/99 Petr Kuzel      L&F fixes.
*  3    Gandalf   1.2         12/17/99 Petr Kuzel      Bundling.
*  2    Gandalf   1.1         12/14/99 Petr Kuzel      Minor enhancements
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 

