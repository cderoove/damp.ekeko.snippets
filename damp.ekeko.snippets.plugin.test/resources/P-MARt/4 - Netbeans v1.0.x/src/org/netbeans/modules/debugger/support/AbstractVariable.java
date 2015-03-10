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

package org.netbeans.modules.debugger.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.debugger.Watch;
import org.openide.debugger.DebuggerException;
import org.netbeans.modules.debugger.support.util.Validator;


/**
*
*
* @author   Jan Jancura
* @version  0.17, Apr 29, 1998
*/
public interface AbstractVariable extends java.io.Serializable, Cloneable, Validator.Object {


    // Watch methods duplicated ..............................................................

    /**
    * Returns the name of this variable.
    *
    * @return the name of this variable.
    */
    public String getVariableName ();

    /**
    * Getter for textual representation of the value. It converts
    * the value to a string representation. So if the watch represents
    * null reference, the returned string will be for example "null".
    * That is why null can be returned when the watch is not valid
    *
    * @return the value of this watch or null if the watch is not in the scope
    */
    public String getAsText ();

    /**
    * Setter that allows to change value of the watched variable.
    *
    * @param value text representation of the value
    * @exception DebuggerException if the value cannot be changed or the
    *    string does not represent valid value
    */
    public void setAsText (String value) throws DebuggerException;

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getType ();

    /**
    * Adds listener on the property changing.
    */
    public void addPropertyChangeListener (PropertyChangeListener listener);

    /**
    * Removes listener on the property changing.
    */
    public void removePropertyChangeListener (PropertyChangeListener listener);


    // other methods ....................................................................

    /**
    * Returns string representation of inner type of this variable.
    *
    * @return string representation of inner type of this variable.
    */
    public String getInnerType ();

    /**
    * Returns true, if this variable do not represents primitive type.
    *
    * @return true, if this variable do not represents primitive type.
    */
    public boolean isObject ();

    /**
    * Returns true, if this variable do not represents primitive type.
    *
    * @return true, if this variable do not represents primitive type.
    */
    public boolean isArray ();

    /**
    * Returns true if it has no subelements.
    *
    * @return true if it has no subelements.
    */
    public boolean isLeaf ();

    /**
    * Returns modifiers of this variable.
    *
    * @return modifiers of this variable.
    */
    public String getModifiers ();

    /**
    * If this AbstractVariable object represents instance of some class or array this method
    * returns variables (static and non-static) of this object.
    *
    * @return variables (static and non-static) of this object.
    */
    public AbstractVariable[] getFields ();
}

/*
 * Log
 *  2    Gandalf   1.1         1/17/00  Daniel Prusa    setAsText method throws 
 *       DebuggerException
 *  1    Gandalf   1.0         11/8/99  Jan Jancura     
 * $
 */
