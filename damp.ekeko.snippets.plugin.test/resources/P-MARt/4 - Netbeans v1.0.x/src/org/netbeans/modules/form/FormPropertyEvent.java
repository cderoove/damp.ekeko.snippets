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

package org.netbeans.modules.form;

import org.openide.nodes.*;

/**
 *
 * @author Ian Formanek
 */
public class FormPropertyEvent extends FormEvent {
    private String propertyName;
    private Object oldValue;
    private Object newValue;

    static final long serialVersionUID =5439870368198141296L;
    public FormPropertyEvent (RADComponent source, String propertyName, Object oldValue, Object newValue) {
        super (source);
        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getPropertyName () {
        return propertyName;
    }

    public Object getOldValue () {
        return oldValue;
    }

    public Object getNewValue () {
        return newValue;
    }

}

/*
 * Log
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         5/5/99   Ian Formanek    
 * $
 */
