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

import java.beans.PropertyEditor;

/**
* Represents value of some property. Value may be: cann't read, excecption or object value.
*
* @author Jan Jancura
* @version 0.20, Jan 28, 1998
*/
class PropertyValue extends Object {

    private boolean canRead = false;
    private boolean isArray;
    private Object value = null;
    private Throwable exception = null;

    /**
    * Scans current value of property from given propertyDetails.
    *
    * @param propertyDetails Value of property to read from.
    */
    PropertyValue (PropertyDetails propertyDetails) {

        //    if (isArray = propertyDetails.isArray ()) return;
        try {
            if (!(canRead = propertyDetails.canRead ())) return;
            value = propertyDetails.getPropertyValue ();
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            exception = e;
            canRead = false;
        }
    }

    /**
    * Returns true, if property can be readed.
    */
    public boolean canRead () {
        return canRead;
    }

    /**
    * Returns value of property.
    */
    public Object getValue () {
        return value;
    }

    /**
    * Returns exception which was throwen in getter method.
    */
    public Throwable getException () {
        return exception;
    }

    /**
    * Two property values are equal if these canRead (), getValue() and getException () methods returns
    * the same values.
    */
    public boolean equals (PropertyValue property) {
        return /*isArray ||*/
            ( (canRead  == property.canRead) &&
              ( (value == null) ? (property.value == null) :
                ((property.value != null) && value.equals (property.value))) &&
              ( (exception == null) ? (property.exception == null) :
                ((property.exception != null) && exception.equals (property.exception)))
            );
    }

    /**
    * Returs string representation of PropertyValue.
    */
    public String toString () {
        return canRead ?
               ( (exception == null) ? ("value: " + value) : // NOI18N
         ("exception: " + exception) ) : // NOI18N
               "cannot read"; // NOI18N
    }
}

/*
 * Log
 *  5    Gandalf   1.4         1/12/00  Ian Formanek    NOI18N
 *  4    Gandalf   1.3         1/12/00  Ian Formanek    NOI18N
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
