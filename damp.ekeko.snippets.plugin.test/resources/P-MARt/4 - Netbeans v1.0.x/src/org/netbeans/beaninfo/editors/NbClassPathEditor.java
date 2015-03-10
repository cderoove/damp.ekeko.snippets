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

package org.netbeans.beaninfo.editors;

import java.awt.*;
import java.beans.*;
import org.openide.execution.NbClassPath;

/** A property editor for NbClassPath.
* @author  Jaroslav Tulach
*/
public class NbClassPathEditor extends Object implements PropertyEditor {
    private NbClassPath pd;
    private PropertyChangeSupport support;

    public NbClassPathEditor () {
        support = new PropertyChangeSupport (this);
    }

    public Object getValue () {
        return pd;
    }

    public void setValue (Object value) {
        pd = (NbClassPath) value;
        support.firePropertyChange ("", null, null); // NOI18N
    }

    public String getAsText () {
        return pd.getClassPath ();
    }

    public void setAsText (String string) {
        setValue (new NbClassPath (string));
    }

    public String getJavaInitializationString () {
        return "new NbClassPath (" + getAsText () + ")"; // NOI18N
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
        return false;
    }

    public Component getCustomEditor () {
        // return new NbClassPathCustomEditor (this);
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
 *  5    Gandalf   1.4         1/13/00  Petr Jiricka    i18n
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         8/7/99   Ian Formanek    cleaned up comments
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         5/31/99  Jaroslav Tulach 
 * $
 */




