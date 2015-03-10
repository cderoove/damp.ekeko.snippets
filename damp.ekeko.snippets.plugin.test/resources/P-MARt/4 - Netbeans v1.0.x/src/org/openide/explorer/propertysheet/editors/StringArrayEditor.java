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

import java.awt.*;
import java.beans.*;

/** A property editor for array of Strings.
* @author  Ian Formanek
* @version 0.10, 17 Jun 1998
*/
public class StringArrayEditor extends Object implements PropertyEditor, StringArrayCustomizable {

    public StringArrayEditor() {
        support = new PropertyChangeSupport (this);
    }

    public Object getValue () {
        return strings;
    }

    public void setValue (Object value) {
        strings = (String[]) value;
        support.firePropertyChange ("", null, null); // NOI18N
    }

    // -----------------------------------------------------------------------------
    // StringArrayCustomizable implementation

    /** Used to acquire the current value from the PropertyEditor
    * @return the current value of the property
    */
    public String[] getStringArray () {
        return (String[])getValue ();
    }

    /** Used to modify the current value in the PropertyEditor
    * @param value the new value of the property
    */
    public void setStringArray (String[] value) {
        setValue (value);
    }

    // end of StringArrayCustomizable implementation

    private String getStrings () {
        StringBuffer buf = new StringBuffer ();
        for (int i = 0; i < strings.length; i++) {
            buf.append (strings[i]);
            if (i != strings.length - 1)
                buf.append (", "); // NOI18N
        }

        return buf.toString ();
    }

    public String getAsText () {
        return getStrings();
    }

    public void setAsText (String string) {
    }

    public String getJavaInitializationString () {
        // [PENDING - wrap strings ???]
        StringBuffer buf = new StringBuffer ("new String[] { "); // NOI18N
        buf.append (getStrings ());
        buf.append ("}"); // NOI18N
        return buf.toString ();
    }

    public String[] getTags () {
        return null;
    }

    public boolean isPaintable () {
        return false;
    }

    public void paintValue (Graphics g, Rectangle rectangle) {
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public Component getCustomEditor () {
        return new StringArrayCustomEditor (this);
    }

    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener (propertyChangeListener);
    }

    private String[] strings;
    private PropertyChangeSupport support;
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




