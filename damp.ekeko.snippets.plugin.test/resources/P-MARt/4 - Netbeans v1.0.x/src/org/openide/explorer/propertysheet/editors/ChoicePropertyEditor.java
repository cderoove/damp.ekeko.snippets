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

package org.openide.explorer.propertysheet.editors;

import java.beans.*;
import java.awt.*;


/** A property editor for Color class.
* @author   Jan Jancura, Ian Formanek
* @version  0.10, 09 Mar 1998
*/
public class ChoicePropertyEditor implements PropertyEditor {


    // variables ..................................................................................

    private PropertyChangeSupport support;

    private int[] constants;
    private String[] names;
    private int index;
    private String name;


    // init .......................................................................................

    public ChoicePropertyEditor (int[] constants, String[] names) {
        support = new PropertyChangeSupport (this);
        this.constants = constants;
        this.names = names;
    }


    // main methods .......................................................................................

    public Object getValue () {
        return new Integer (constants [index]);
    }

    public void setValue (Object object) {
        if (!(object instanceof Integer)) throw new IllegalArgumentException ();
        int ii = ((Integer)object).intValue ();
        int i, k = constants.length;
        for (i = 0; i < k; i++) if (constants [i] == ii) break;
        if (i == k)  throw new IllegalArgumentException ();
        index = i;
        name = names [i];
        support.firePropertyChange (null, null, null);
    }

    public String getAsText () {
        return name;
    }

    public void setAsText (String string)
    throws IllegalArgumentException {
        int i, k = names.length;
        for (i = 0; i < k; i++) if (names [i].equals (string)) break;
        if (i == k)  throw new IllegalArgumentException ();
        index = i;
        name = names [i];
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
}

/*
 * Log
 *  4    Gandalf   1.3         1/12/00  Ian Formanek    NOI18N
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/30/99  Ian Formanek    Moved to package 
 *       org.openide.explorer.propertysheet.editors
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */




