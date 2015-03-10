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

package org.netbeans.modules.icebrowser;

import java.beans.*;
import java.awt.*;


/**
* A property editor allowing to select right value (String, int, ...)
* from list (choice) of predefined string values.
*
* @author   Jan Jancura, Ian Formanek
* @version  0.10, 09 Mar 1998
*/
public class ChoicePropertyEditor implements PropertyEditor {


    // variables ..................................................................................

    private PropertyChangeSupport support;

    private Object[] constants;
    private String[] names;
    private int index;
    private String name;
    private Object value;
    private boolean canEdit;


    // init .......................................................................................

    public ChoicePropertyEditor (
        Object[] constants,
        String[] names,
        boolean canEdit
    ) {
        support = new PropertyChangeSupport (this);
        this.constants = constants;
        this.names = names;
        this.canEdit = canEdit;
    }


    // main methods .......................................................................................

    public Object getValue () {
        return value;
    }

    public void setValue (Object object) {
        if (object == null) {
            if (!canEdit)
                throw new IllegalArgumentException ();
            index = -1;
            name = "null"; // NOI18N
            value = null;
        }
        int i, k = constants.length;
        for (i = 0; i < k; i++)
            if (
                (constants [i] == object) ||
                (constants [i].equals (object))
            ) break;
        if (i == k) {
            if (!canEdit)
                throw new IllegalArgumentException ();
            index = -1;
            name = object.toString ();
            value = object;
        } else {
            index = i;
            name = names [i];
            value = constants [i];
        }
        support.firePropertyChange (null, null, null);
    }

    public String getAsText () {
        return name;
    }

    public void setAsText (String string)
    throws IllegalArgumentException {
        int i, k = names.length;
        for (i = 0; i < k; i++) if (names [i].equals (string)) break;
        if (i == k) {
            if (!canEdit)
                throw new IllegalArgumentException ();
            value = string;
            name = string;
            index = -1;
        } else {
            index = i;
            name = names [i];
            value = constants [i];
        }
        return;
    }

    public String getJavaInitializationString () {
        return "" + index; // NOI18N
    }

    public String[] getTags () {
        return names;
    }

    public boolean isPaintable () {
        return false;
    }

    public void paintValue (Graphics g, Rectangle rectangle) {
    }

    public boolean supportsCustomEditor () {
        return false;
    }

    public Component getCustomEditor () {
        return null;
    }

    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener (propertyChangeListener);
    }

    /** Get an in-place editor.
    * @return a custom property editor to be shown inside the property
    *         sheet
    */
    public Component getInPlaceCustomEditor () {
        return null;
    }

    /** Test for support of in-place custom editors.
    * @return <code>true</code> if supported
    */
    public boolean hasInPlaceCustomEditor () {
        return false;
    }

    /** Test for support of editing of tagged values.
    * Must also accept custom strings, otherwise you may may specify a standard property editor accepting only tagged values.
    * @return <code>true</code> if supported
    */
    public boolean supportsEditingTaggedValues () {
        return canEdit;
    }
}

/*
 * Log
 *  2    Gandalf-post-FCS1.1         4/5/00   Jan Jancura     null value support
 *  1    Gandalf-post-FCS1.0         4/5/00   Jan Jancura     
 * $
 */




