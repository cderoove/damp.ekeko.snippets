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

import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.debugger.Watch;
import org.openide.debugger.DebuggerNotFoundException;

import org.netbeans.modules.debugger.support.util.Validator;


/**
*  Default inplementation of variable.
*
* @author   Jan Jancura
* @version  0.17, Apr 29, 1998
*/
public abstract class VariableImpl implements AbstractVariable, java.io.Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -4908841115435797749L;
    /** bundle to obtain text information from */
    protected static ResourceBundle                 bundle = NbBundle.getBundle (VariableImpl.class);


    // variables .........................................................................

    protected transient AbstractDebugger            debugger;
    protected transient Validator                   validator;
    protected transient PropertyChangeSupport       pcs;
    private transient RequestProcessor.Task         task;

    /** When its true, the value isnt changed in the validate () method. */
    protected transient boolean                     isCloned = false;

    /** Name of field. */
    protected String                                name = null;
    /** Index in the array (parentObject is RemoteArray). */
    protected int                                   index = -1;

    /** Property variables. */
    protected transient String                      value = null;
    protected transient String                      type = null;
    protected transient String                      innerType = ""; // NOI18N
    protected transient String                      modifiers = null;
    protected transient String                      errorMessage = null;
    /** true if object or array */
    protected transient boolean                     isObject = false;
    /** true if array */
    protected transient boolean                     isArray = false;


    // init ...............................................................................

    /**
    * Non public constructor called from TheWatch only.
    */
    protected VariableImpl (AbstractDebugger debugger, Validator validator) {
        this.debugger = debugger;
        this.validator = validator;
        init ();
    }

    private void readObject (java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject ();
        try {
            validator = ((AbstractDebugger) TopManager.getDefault ().getDebugger ()).getValidator ();
        } catch (DebuggerNotFoundException e) {
            throw new java.io.IOException ();
        }
        init ();
    }

    protected void init () {
        if (validator != null) validator.add (this);
        pcs = new PropertyChangeSupport (this);
    }

    public java.lang.Object clone () {
        try {
            VariableImpl v = (VariableImpl) super.clone ();
            v.isCloned = true;
            return v;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }


    // interface Watch .........................................................

    /**
    * Returns the name of this variable.
    *
    * @return the name of this variable.
    */
    public String getVariableName () {
        return name;
    }

    /**
    * Getter for textual representation of the value. It converts
    * the value to a string representation. So if the watch represents
    * null reference, the returned string will be for example "null".
    * That is why null can be returned when the watch is not valid
    *
    * @return the value of this watch or null if the watch is not in the scope
    */
    public String getAsText () {
        return value;
    }

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getType () {
        return type;
    }

    /**
    * Adds listener on the property changing.
    */
    public synchronized void addPropertyChangeListener (PropertyChangeListener listener) {
        pcs.addPropertyChangeListener (listener);
    }

    /**
    * Removes listener on the property changing.
    */
    public synchronized void removePropertyChangeListener (PropertyChangeListener listener) {
        pcs.removePropertyChangeListener (listener);
    }

    protected void firePropertyChange (String s, Object o, Object n) {
        if (pcs != null) pcs.firePropertyChange (s, o, n);
    }


    // other methods ....................................................................

    /**
    * Returns string representation of inner type of this variable.
    *
    * @return string representation of inner type of this variable.
    */
    public String getInnerType () {
        return innerType;
    }

    /**
    * Returns true, if this variable do not represents primitive type.
    *
    * @return true, if this variable do not represents primitive type.
    */
    public boolean isObject () {
        return isObject;
    }

    /**
    * Returns true, if this variable do not represents primitive type.
    *
    * @return true, if this variable do not represents primitive type.
    */
    public boolean isArray () {
        return isArray;
    }

    /**
    * Returns modifiers of this variable.
    *
    * @return modifiers of this variable.
    */
    public String getModifiers () {
        return modifiers;
    }

    /**
    * Returns error message if watch cannot be resolved or null.
    *
    * @return AbstractVariable object for this class.
    */
    public String getErrorMessage () {
        return errorMessage;
    }

    /**
    * Sets error message for this watch.
    */
    protected void setError (String description) {
        modifiers = ""; // NOI18N
        type = ""; // NOI18N
        isObject = false;
        isArray = false;
        value = null;
        innerType = ""; // NOI18N
        errorMessage = description;
    }

    /**
    * remoteValue => isObject, isArray, value, innerType
    * + fire
    */
    public String toString () {
        return "VariableImpl " + name + " = (" + type + ") (" + innerType + ") " + value; // NOI18N
    }
}

/*
 * Log
 *  15   Gandalf-post-FCS1.13.3.0    3/28/00  Daniel Prusa    
 *  14   Gandalf   1.13        1/13/00  Daniel Prusa    NOI18N
 *  13   Gandalf   1.12        1/10/00  Jan Jancura     Refresh of locales 
 *       updated
 *  12   Gandalf   1.11        1/3/00   Daniel Prusa    bugfix for fixed watches
 *  11   Gandalf   1.10        11/8/99  Jan Jancura     Somma classes renamed
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         10/5/99  Jan Jancura     Serialization of 
 *       debugger.
 *  8    Gandalf   1.7         9/15/99  Jan Jancura     
 *  7    Gandalf   1.6         9/2/99   Jan Jancura     
 *  6    Gandalf   1.5         6/10/99  Jan Jancura     
 *  5    Gandalf   1.4         6/9/99   Jan Jancura     
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/4/99   Jan Jancura     
 *  2    Gandalf   1.1         6/4/99   Jan Jancura     
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 */
