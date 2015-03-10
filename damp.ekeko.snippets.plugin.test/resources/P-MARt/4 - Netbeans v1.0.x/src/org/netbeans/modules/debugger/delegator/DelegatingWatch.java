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

package org.netbeans.modules.debugger.delegator;

import java.beans.*;

import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.debugger.Watch;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.debugger.DebuggerException;

import org.netbeans.modules.debugger.support.AbstractWatch;
import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.AbstractDebugger;


/**
* Core watch delegates all functionality on inner instance of watch.
*
* @author Jan Jancura
*/
public class DelegatingWatch extends AbstractWatch {

    static final long serialVersionUID = -8584610162523086115L;

    // variables ........................................................................................

    /** Delegating instance of watch. */
    private transient AbstractWatch           watch;
    /** Instance of debuger. */
    private transient DelegatingDebugger      debugger;
    private transient AbstractDebugger        oldDebugger;
    private transient PropertyChangeSupport   pcs;
    private String                            expression;
    private String                            errorMessage;


    // init ............................................................................................

    /** Creates new DelegatingWatch */
    public DelegatingWatch (DelegatingDebugger debugger) {
        this.debugger = debugger;
        init ();
    }

    private void readObject (java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject ();
        try {
            debugger = (DelegatingDebugger) TopManager.getDefault ().getDebugger ();
        } catch (DebuggerNotFoundException e) {
            throw new java.io.IOException ();
        }
        init ();
    }

    protected void init () {
        if (debugger.getValidator () != null) {
            debugger.getValidator ().add (this);
        }
        pcs = new PropertyChangeSupport (this);
    }


    // Watch implementation ...............................................................................

    public void validate () {
        AbstractDebugger newDebugger = debugger.getCurrentDebugger ();

        //S ystem.out.println ("DelegatingWatch.validate " + getVariableName () + " : " + // NOI18N
        //debugger + " : " + debugger.getState () + " : " + newDebugger); // NOI18N

        if (debugger.getState () == debugger.DEBUGGER_NOT_RUNNING
           ) {
            // debugger stopped
            watch = null;
            oldDebugger = null;
            setError (NbBundle.getBundle (DelegatingWatch.class).getString ("EXC_No_session"));
            pcs.firePropertyChange (null, null, null);
            return;
        } else
            if ((newDebugger == null) && (debugger.getState () != debugger.DEBUGGER_STOPPED)) {
                // debugger running & no current session
                setError (NbBundle.getBundle (DelegatingWatch.class).getString ("CTL_No_context"));
                pcs.firePropertyChange (null, null, null);
                return;
            }
        errorMessage = null;  //S ystem.out.println ("DelegatingWatch.validate3 " + oldDebugger + " : " + newDebugger); // NOI18N
        if ((oldDebugger == null) && (newDebugger == null)
           ) {
            watch = null;
            pcs.firePropertyChange (null, null, null);
            return;
        }
        if ((oldDebugger != null) && (newDebugger != null) &&
                oldDebugger.equals (newDebugger)
           ) {
            if (watch != null) watch.validate ();
            else watch = (AbstractWatch) newDebugger.createWatch (expression, true);
        } else {
            if (newDebugger == null)
                watch = null;
            else
                watch = (AbstractWatch) newDebugger.createWatch (expression, true);
        }
        oldDebugger = newDebugger;
        pcs.firePropertyChange (null, null, null);
    }

    /**
    * @return true if debugger is stopped.
    */
    public boolean canValidate () {
        return (debugger.getState () == AbstractDebugger.DEBUGGER_STOPPED) ||
               (debugger.getState () == AbstractDebugger.DEBUGGER_NOT_RUNNING);
    }

    /**
    * @return false, watch cannot be removed from validator when debugger is finished
    */
    public boolean canRemove () {
        return false;
    }

    /**
    * Returns the name of this variable.
    *
    * @return the name of this variable.
    */
    public String getVariableName () {
        return expression;
    }

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getType () {
        if (watch != null) return watch.getType ();
        return ""; // NOI18N
    }

    /**
    * Returns string representation of inner type of this variable.
    *
    * @return string representation of inner type of this variable.
    */
    public String getInnerType () {
        if (watch != null) return watch.getInnerType ();
        return ""; // NOI18N
    }

    /**
    * Returns true, if this variable do not represents primitive type.
    *
    * @return true, if this variable do not represents primitive type.
    */
    public boolean isObject () {
        if (watch != null) return watch.isObject ();
        return false;
    }

    /**
    * Returns true, if this variable do not represents primitive type.
    *
    * @return true, if this variable do not represents primitive type.
    */
    public boolean isArray () {
        if (watch != null) return watch.isArray ();
        return false;
    }

    /**
    * Returns modifiers of this variable.
    *
    * @return modifiers of this variable.
    */
    public String getModifiers () {
        if (watch != null) return watch.getModifiers ();
        return ""; // NOI18N
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
        if (watch != null) return watch.getAsText ();
        return ""; // NOI18N
    }

    /**
    * Setter that allows to change value of the watched variable.
    *
    * @param value text representation of the value
    * @exception DebuggerException if the value cannot be changed or the
    *    string does not represent valid value
    */
    public void setAsText (String value) throws DebuggerException {
        if (watch != null) watch.setAsText (value);
        pcs.firePropertyChange (null, null, null);
    }

    /**
    * If this AbstractVariable object represents instance of some class or array this method
    * returns variables (static and non-static) of this object.
    *
    * @return variables (static and non-static) of this object.
    */
    public AbstractVariable[] getFields () {
        if (watch != null) return watch.getFields ();
        return new AbstractVariable [0];
    }

    /**
    * Returns true if this variable hasn't any fields.
    *
    * @return True if this variable hasn't any fields.
    */
    public boolean isLeaf() {
        return false;
    }

    /**
    * Standart helper method.
    */
    public String toString () {
        return "DelegatingWatch " + getVariableName () + " = (" + getType () + ") (" + getInnerType () + ") " + getAsText (); // NOI18N
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

    /**  Returns true if this variable is in scope.
     *
     * @return true if this variable is in scope.
     */
    public boolean isInScope () {
        if (watch != null) return watch.isInScope ();
        return false;
    }

    /**  Create AbstractVariable object for this Watch. Can return null, if this Watch currently not
     * represents valide variable.
     *
     * @return AbstractVariable object for this class.
     */
    public AbstractVariable getVariable () {
        if (watch != null) return watch.getVariable ();
        return null;
    }

    /**
    * Returns error message if watch cannot be resolved or null.
    *
    * @return AbstractVariable object for this class.
    */
    public String getErrorMessage () {
        if (watch != null) return watch.getErrorMessage ();
        return errorMessage;
    }

    /** Remove the watch from the list of all watches in the system.
     */
    public void remove () {
        debugger.removeWatch (this);
    }

    /** Set the variable name to watch.
     *
     * @param name string name of the variable to watch
     */
    public void setVariableName (String name) {
        expression = name;
        if (watch != null) watch.setVariableName (name);
        validate ();
    }

    /** Test whether the watch is hidden.
     * If so, it
     * is not presented in the list of all watches. Such a watch can be used
     * for the IDE's (or some module's) private use, not displaying anything to the user.
     * @return <code>true</code> if the watch is hidden
     * @see Debugger#createWatch(String, boolean)
     */
    public boolean isHidden () {
        Watch[] w = debugger.getWatches ();
        int i, k = w.length;
        for (i = 0; i < k; i++)
            if (w [i] == this) return false;
        return true;
    }

    /**
    * Sets error message for this watch.
    */
    protected void setError (String description) {
        errorMessage = description;
    }

    public void refresh (AbstractThread t) {
        return; // ? watch.refresh ();
    }

}

/*
* Log
*  8    Gandalf-post-FCS1.6.4.0     3/28/00  Daniel Prusa    
*  7    Gandalf   1.6         1/17/00  Daniel Prusa    setAsText method throws 
*       DebuggerException
*  6    Gandalf   1.5         1/14/00  Daniel Prusa    NOI18N
*  5    Gandalf   1.4         1/13/00  Daniel Prusa    NOI18N
*  4    Gandalf   1.3         1/13/00  Daniel Prusa    rollback of previous 
*       Check In
*  3    Gandalf   1.2         1/12/00  Daniel Prusa    throws DebuggerException 
*       added for setAsText (String) method
*  2    Gandalf   1.1         12/21/99 Daniel Prusa    Interfaces Debugger, 
*       Watch, Breakpoint changed to abstract classes.
*  1    Gandalf   1.0         11/9/99  Jan Jancura     
* $
*/
