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
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
* This is continer which manages Components in one column. All components have the same size
* which is setted by the first component's preferred size or by setter method
* setItemHeight (int aHeight).
*
* @author   Jan Jancura, Jaroslav Tulach
*/
class NamesPanel extends JPanel {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 1620670226589808833L;

    /** Index of selected item.*/
    private int selectedItem = -1;

    /** Component overlaped with input component is stored here. */
    private Component hiddenComponent;

    /** Vector of inner components. 
     * @associates Component*/
    private Vector item = new Vector (20,20);

    /** Column manager. */
    ColumnManager manager;

    /**
    * Construct NamesPanel.
    */
    public NamesPanel () {
        manager = new ColumnManager ();
        setLayout (manager);
    }

    /**
    * Construct NamesPanel which size depends on the other NamesPanel size..
    */
    public NamesPanel (NamesPanel namesPanel) {
        manager = new ColumnManager (namesPanel.getColumnManager ());
        setLayout (manager);
    }

    /**
    * Returns Column manager.
    */
    public ColumnManager getColumnManager () {
        return manager;
    }

    /**
    * Returns component with this index.
    *
    * @param int index Index of component which I can get.
    * @return Component with index "index".
    */
    public Component getItem (int index) {
        return (Component) item.elementAt (index);
    }

    /**
    * Adds new component at the end. After this operation must be validate () called.
    *
    * @ Component component This component will be added.
    */
    public Component add (Component component) {
        super.add (component);
        item.addElement (component);
        return component;
    }

    /**
    * Replaces component with index == itemIndex by component.
    *
    * @ int itemIndex Index of component to replace.
    * @ Component component This component will be added.
    */
    public void setInputComponent (int itemIndex, Component component) {
        if (selectedItem == itemIndex) {
            removeInputComponent ();
            return;
        }
        if (selectedItem != -1)
            removeInputComponent ();
        hiddenComponent = getComponent (itemIndex);
        remove (itemIndex);
        add (component, itemIndex);
        repaint ();
        validate ();
        selectedItem = itemIndex;
    }

    /**
    * Removes input component from this NamePanel, and adds original one.
    */
    public void removeInputComponent () {
        if (selectedItem == -1) return;
        remove (selectedItem);
        add (hiddenComponent, selectedItem);
        hiddenComponent.repaint ();
        selectedItem = -1;
    }

    /**
    * Removes all components from this NamePanel.
    */
    public void removeAll () {
        selectedItem = -1;
        super.removeAll ();
        item.removeAllElements ();
    }
}


/*
 * Log
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jaroslav Tulach Changed not to be public
 */
