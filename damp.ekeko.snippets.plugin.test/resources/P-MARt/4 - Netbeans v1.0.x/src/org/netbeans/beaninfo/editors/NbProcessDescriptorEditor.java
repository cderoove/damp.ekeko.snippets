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
import org.openide.execution.NbProcessDescriptor;

/** A property editor for NbProcessDescriptor.
* @author  Ian Formanek
*/
public class NbProcessDescriptorEditor extends Object implements PropertyEditor {

    NbProcessDescriptor pd;
    private PropertyChangeSupport support;

    public NbProcessDescriptorEditor() {
        support = new PropertyChangeSupport (this);
    }

    public Object getValue () {
        return pd;
    }

    public void setValue (Object value) {
        pd = (NbProcessDescriptor) value;
        support.firePropertyChange ("", null, null); // NOI18N
    }

    public String getAsText () {
        return pd.getProcessName () + " " + pd.getArguments (); // NOI18N
    }

    public void setAsText (String string) {
        string = string.trim ();
        int indx = string.indexOf (' ');
        String prg;
        String args;
        if (indx == -1) {
            prg = string;
            args = ""; // NOI18N
        } else {
            prg = string.substring (0, indx);
            args = string.substring (indx + 1);
        }

        NbProcessDescriptor newPD = new NbProcessDescriptor (
                                        prg,
                                        args,
                                        pd.getInfo ()
                                    );
        setValue (newPD);
    }

    public String getJavaInitializationString () {
        return null; // no code generation
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
        return new NbProcessDescriptorCustomEditor (this);
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
 *  7    Gandalf   1.6         1/13/00  Petr Jiricka    i18n
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/7/99   Ian Formanek    Custom editor ...
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/1/99   Jaroslav Tulach Allows to edit the file 
 *       and also parameters.
 *  2    Gandalf   1.1         5/31/99  Jaroslav Tulach External Execution & 
 *       Compilation
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */




