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

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

import org.openide.nodes.Node;
import org.openide.nodes.NodeListener;


/**
* A helper class that manages woking with a set of beans. It usess BeanDetails for each
* of them, and creates PropertyDetails for each common property of all beans.
*
* @author   Jan Jancura
* @version  0.26, Apr 6, 1998
*/
class BeansDetails extends Object {

    /** There are the beans stored. */
    private Node[] node;

    /** There are the BeanDetails object for each bean stored. */
    private PropertyDetails[] propertyDetailsArray = null;

    private String[] names;

    private String[] displayNames;

    private String[] hints;

    private PropertyDetails[][] propertyDetails = null;


    // CONSTRUCTORS ...................................................................................

    /** Default constructor that creates description for null set of beans
    */
    public BeansDetails () {
        node = new Node [0];
    }

    /**
     * Constructs a new BeanaDetails class for given array of Java Beans.
     * There are BeanDetails created for each bean and PropertyChangeListeners are added
     * to the beans.
     *
     * @param Object[] aBeans the Java Beans for which we are constructing the BeansDetails.
     */
    public BeansDetails (Node[] aNode) throws IntrospectionException {
        if ((aNode == null) || (aNode.length < 1)) {
            node = new Node [0];
            return;
        }
        node = aNode;
    }// BeansDetails ()


    // MAIN METHODS .....................................................................................

    /**
    * Returns Names of property cathegories common for all the nodes.
    */
    public String[] getPropertySetNames () {
        if (names == null) parsePropertySets ();
        return names;
    }

    /**
    * Returns Display names of property cathegories common for all the nodes.
    */
    public String[] getPropertySetDisplayNames () {
        if (names == null) parsePropertySets ();
        return displayNames;
    }

    /**
    * Returns Hints of property cathegories common for all the nodes.
    */
    public String[] getPropertySetHints () {
        if (names == null) parsePropertySets ();
        return hints;
    }

    /**
    * Returns the poperties from one cathegory. There are all common properties
    * for all nodes in this cathegory.
    *
    * @param propertySetNameIndex Index of this cathegory.
    */
    public PropertyDetails[] getPropertyDetails (int propertySetNameIndex) {
        if (names == null) parsePropertySets ();
        return (PropertyDetails[]) propertyDetails [propertySetNameIndex].clone ();
    }

    /**
    * Returns true if this BeansDetails represents one bean which has customizer.
    *
    * @return true if this BeansDetails represents one bean which has customizer.
    */
    public boolean hasCustomizer () {
        if (node.length != 1) return false;
        return node [0].hasCustomizer ();
    }

    /**
    * Helper method that creates a customizer component for this JavaBean.
    *
    * @return The customizer component for this JavBean or null if the customizer does not exist or
    *         is not a subclass of java.awt.Component
    */
    public void customize () {
        if (node.length != 1) return;
        org.openide.TopManager.getDefault ().getNodeOperation ().customize (node [0]);
    }

    /**
    * Refresh informations about property set.
    */
    public void refresh () {
        names = null;
    }

    /**
    * Standart helper method.
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        int i, k = node.length;
        for (i = 0; i < k; i++)
            node [i].addPropertyChangeListener (l);
    }

    /**
    * Standart helper method.
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        int i, k = node.length;
        for (i = 0; i < k; i++)
            node [i].removePropertyChangeListener (l);
    }

    /**
    * Add Node listener to all nodes.
    */
    public void addNodeListener (NodeListener l) {
        int i, k = node.length;
        for (i = 0; i < k; i++)
            node [i].addNodeListener (l);
    }

    /**
    * Remove Node listener to all nodes.
    */
    public void removeNodeListener (NodeListener l) {
        int i, k = node.length;
        for (i = 0; i < k; i++)
            node [i].removeNodeListener (l);
    }


    // HELPER METHODS .....................................................................................

    /**
    * Parse property sets, inspect all comon property sets for common properties.
    *
    * Input: node[]
    * Output: name[], propertyDetails []
    */
    private void parsePropertySets () {
        if (node.length < 1) {
            names = new String [0];
            displayNames = new String [0];
            hints = new String [0];
            propertyDetails = new PropertyDetails [0][];
            return;
        }
        Node.PropertySet[] set = node [0].getPropertySets ();
        int i, k = set.length;
        Vector setNames = new Vector (10, 10);
        Vector allNames = new Vector (10, 10);
        Vector setProperties = new Vector (10, 10);
        Vector v;
        for (i = 0; i < k; i++) {
            allNames.addElement (set [i].getName ());
            setNames.addElement (set [i].getName ());
            setProperties.addElement (v = new Vector (10, 10));
            v.addElement (set [i].getProperties ());
        }
        int j, l = node.length;
        for (j = 1; j < l; j ++) {
            Vector newSetNames = new Vector (10, 10);
            Vector newSetProperties = new Vector (10, 10);
            set = node [j].getPropertySets ();
            k = set.length;
            for (i = 0; i < k; i++) {
                String s = set [i].getName ();
                int index;
                if ((index = setNames.indexOf (s)) < 0) continue;
                v = (Vector) setProperties.elementAt (index);
                v.addElement (set [i].getProperties ());
                newSetNames.addElement (s);
                newSetProperties.addElement (v);
            }//for
            setNames = newSetNames;
            setProperties = newSetProperties;
        }
        k = setNames.size ();

        names = new String [k];
        displayNames = new String [k];
        hints = new String [k];
        propertyDetails = new PropertyDetails[k] [];

        setNames.copyInto (names);
        set = node [0].getPropertySets ();
        for (i = 0; i < k; i++) {
            int index = allNames.indexOf (names [i]);
            displayNames [i] = set [index].getDisplayName ();
            hints [i] = set [index].getShortDescription ();
            propertyDetails [i] = parseProperties ((Vector) setProperties.elementAt (i));
        }
    }

    /**
    * Finds all common propertise from the vector of property arrays.
    *
    * @param v Vector of property arrays.
    */
    private PropertyDetails[] parseProperties (Vector v) {
        Node.Property[] properties = (Node.Property[]) v.elementAt (0);
        int i, k = properties.length;
        Vector propertyDetails = new Vector (k);
        for (i = 0; i < k; i++)
            if (properties [i] != null)
                propertyDetails.addElement (
                    new PropertyDetails (node, properties [i]));
        int j, l;
        k = node.length;
        for (i = 1; i < k; i++) {//node
            properties = (Node.Property[]) v.elementAt (i);;
            for (j = propertyDetails.size () - 1; j >= 0; j --) {//propertyDetails
                PropertyDetails pObject = (PropertyDetails)propertyDetails.elementAt (j);
                for (l = properties.length - 1; l >= 0; l --)
                    if (pObject.addProperty (properties [l])) break;
                if (l < 0) propertyDetails.removeElementAt (j);
            }
        }
        PropertyDetails[] propertyDetailsArray = new PropertyDetails [propertyDetails.size ()];
        propertyDetails.copyInto (propertyDetailsArray);
        return propertyDetailsArray;
    }
}


/*
 * Log
 *  6    Gandalf   1.5         1/4/00   Jan Jancura     Refresh PS when Property
 *       set is changed.
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/3/99   Jaroslav Tulach NodePropertyEditor & 
 *       NodeCustomizer
 *  2    Gandalf   1.1         3/8/99   Ian Formanek    Removed unused imports
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.16        --/--/98 Jaroslav Tulach Added default constructor, class is no longer public
 */
