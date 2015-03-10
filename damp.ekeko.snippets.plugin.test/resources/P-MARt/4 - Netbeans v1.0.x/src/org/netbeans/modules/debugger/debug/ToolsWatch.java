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

package org.netbeans.modules.debugger.debug;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import sun.tools.debug.RemoteClass;
import sun.tools.debug.RemoteThread;
import sun.tools.debug.RemoteField;
import sun.tools.debug.RemoteObject;
import sun.tools.debug.RemoteStackVariable;
import sun.tools.debug.RemoteValue;
import sun.tools.debug.RemoteStackFrame;
import sun.tools.debug.RemoteArray;

import org.openide.debugger.Watch;
import org.openide.util.NbBundle;

import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.AbstractWatch;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.util.Validator;
import org.netbeans.modules.debugger.support.util.Protector;


/**
* Standart implementation of Watch interface.
* @see org.openide.debugger.Watch
*
* @author   Jan Jancura
* @version  0.18, Feb 23, 1998
*/
public class ToolsWatch extends AbstractWatch {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 3439367144447814302L;


    // private variables .....................................................

    private ToolsVariable                       toolsVariable;

    protected transient boolean                 inScope = false;

    protected transient PropertyChangeSupport   pcs;

    protected transient Validator               validator;

    /** Name of watch like xxx.yyy [2]. */
    protected String                            displayName;


    // init .....................................................................

    /**
    * Non public constructor called from the JavaDebugger only.
    * User must create watch from Debugger.getNewWatch () method.
    */
    ToolsWatch (ToolsDebugger debugger) {
        toolsVariable = new ToolsVariable (debugger, false);
        validator = debugger.getValidator ();
        init ();
    }

    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject ();
        validator = toolsVariable.getDebugger().getValidator ();
        init ();
    }


    // AbstractWatch implementation .........................................................

    /**
    * Returns the name of this watch.
    *
    * @return the name of this watch.
    */
    public String getVariableName () {
        return displayName;
    }

    /**
    * Destroys the watch. Removes it from the list of all watches in the system.
    */
    public void remove () {
        toolsVariable.getDebugger().removeWatch (this);
    }

    /**
    * Sets variable name of this watch.
    *
    * @param name The name of variable of this watch.
    */
    public void setVariableName (String displayName) {
        String old = this.displayName;
        this.displayName = displayName;
        validate ();
    }

    /** Getter to test if the property is "hidden". If a property is hidden it
    * is not presented in the list of all properties. Such a property can be used
    * for private usage of the IDE, not displaying anything to user.
    * <P>
    * To create hidden watch, use <CODE>Debugger.createWatch ("name", true)</CODE> method.
    *
    * @return true if the watch is hidden, false otherwise
    */
    public boolean isHidden () {
        Watch[] w = toolsVariable.getDebugger().getWatches ();
        int i, k = w.length;
        for (i = 0; i < k; i++)
            if (w [i] == this) return false;
        return true;
    }


    // other methods ......................................................................

    /**
    * Returns true if this variable is in scope.
    *
    * @return true if this variable is in scope.
    */
    public boolean isInScope () {
        return inScope;
    }

    /**
    * Create AbstractVariable object for this Watch. Can return null, if this Watch currently not
    * represents valide variable.
    *
    * @return AbstractVariable object for this class.
    */
    public AbstractVariable getVariable () {
        return (AbstractVariable) clone ();
    }

    /**
    * Returns true if this variable hasn't any fields.
    *
    * @return True if this variable hasn't any fields.
    */
    public boolean isLeaf () {
        return false;
    }

    public boolean equals (java.lang.Object o) {
        return hashCode () == o.hashCode ();
    }

    public int hashCode () {
        return System.identityHashCode (this);
    }

    /**
    * Checks value of this watch and if is valide, and sets it.
    */
    public void validate () {
        if ((toolsVariable.getDebugger ().synchronizer == null) ||
                ( toolsVariable.getDebugger ().getState () ==
                  toolsVariable.getDebugger ().DEBUGGER_NOT_RUNNING
                )
           ) {
            setError (NbBundle.getBundle (ToolsWatch.class).getString ("EXC_No_session"));
            firePropertyChange (null, null, null);
        } else
            if (toolsVariable.getDebugger().getState () != toolsVariable.getDebugger().DEBUGGER_STOPPED) {
                setError (NbBundle.getBundle (ToolsWatch.class).getString ("CTL_No_context"));
                firePropertyChange (null, null, null);
            } else {
                ToolsThread tt = (ToolsThread) toolsVariable.getDebugger().getCurrentThread ();
                if (tt != null)
                    refreshValue (tt);
                else {
                    setError (NbBundle.getBundle (ToolsWatch.class).getString ("CTL_No_context"));
                    firePropertyChange (null, null, null);
                }
            }
    }

    /**
    * @return true if debugger is stopped.
    */
    public boolean canValidate () {
        int state = toolsVariable.getDebugger().getState ();
        return (state == ToolsDebugger.DEBUGGER_STOPPED) || (state == ToolsDebugger.DEBUGGER_NOT_RUNNING);
    }

    /**
    * @return false, watch cannot be removed from validator when debugger is finished
    */
    public boolean canRemove () {
        return false;
    }

    /**
    * Refresh value of watch in given context.
    */
    void refreshValue (final ToolsThread tt) {
        toolsVariable.setErrorMessage (null);
        new Protector ("ToolsWatch.refreshValue") { // NOI18N
            public Object protect () throws Exception {
                RemoteValue oldValue = toolsVariable.getRemoteValue ();
                RemoteThread rt = tt.getRemoteThread ();
                try {
                    int i = displayName.lastIndexOf ('.');
                    if (i >= 0) {
                        RemoteClass clazz = getRemoteClass (displayName.substring (0, i));
                        if (clazz != null) {
                            readStaticVariable (clazz, displayName.substring (i + 1, displayName.length ()));
                        }
                    } else
                        if (!readLocalVariable (rt))
                            if (!readObjectVariable (rt))
                                if (!readStaticVariable (rt))
                                    setErrorHelper (NbBundle.getBundle (ToolsWatch.class).getString ("CTL_Name_unknown"));
                } catch (Throwable e) {
                    if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                    setErrorHelper (e.toString ());
                    //          toolsVariable.setNull ();
                    //value = e.toString ();
                }
                return null;
            }
        }.wait (toolsVariable.getDebugger().synchronizer, toolsVariable.getDebugger().killer);
        // deadlock prevention
        firePropertyChange (null, null, null);
    }

    public void refresh (AbstractThread t) {
        refreshValue ((ToolsThread)t);
    }

    private boolean readLocalVariable (RemoteThread rt) throws Exception {
        RemoteStackVariable variable = getRemoteStackVariable (rt, displayName);
        if (variable == null) return false;                 // unknown variable
        inScope = variable.inScope ();
        toolsVariable.update (
            displayName,
            variable.getValue (),
            variable.getType ().toString ()
        );
        return true;
    }

    private boolean readObjectVariable (RemoteThread rt) throws Exception {
        RemoteStackVariable variable = getRemoteStackVariable (rt, "this"); // NOI18N
        if (variable == null) return false;                 // I'am in static method
        RemoteValue thisValue = variable.getValue ();
        if ((thisValue == null) || (!thisValue.isObject ())) return false;  //Illegal state
        return toolsVariable.update (
                   ((RemoteObject) thisValue).getField (displayName),
                   (RemoteObject) thisValue
               );
    }

    private boolean readStaticVariable (RemoteThread rt) throws Exception {
        RemoteStackFrame frame = rt.getCurrentFrame ();
        if (frame == null) return false;
        RemoteClass clazz = frame.getRemoteClass ();
        if (clazz == null) return false;                  //???
        return readStaticVariable (clazz, displayName);
    }

    private boolean readStaticVariable (RemoteClass clazz, String displayName) throws Exception {
        RemoteField remoteField;
        RemoteObject parentObject = null;
        try {
            remoteField = clazz.getField (displayName);
            if (remoteField == null) return false;
            toolsVariable.setRemoteValue (clazz.getFieldValue (displayName));
        } catch (NoSuchFieldException e) {                // to have transparent behaviour
            return false;                                   // unknown variable
        }
        toolsVariable.update (remoteField, clazz);
        return true;
    }

    /**
    * Returns RemoteStackVariable for the name specified or null.
    *
    * @return RemoteStackVariable for the name specified or null.
    */
    /*  RemoteStackVariable getRemoteStackVariable (final String variableName) throws Exception {
        ToolsThread tt = (ToolsThread) debugger.getCurrentThread ();
        if (tt == null) return null;
        return getRemoteStackVariable (tt, variableName);
      }*/

    /**
    * Returns RemoteStackVariable for the name specified or null.
    *
    * @return RemoteStackVariable for the name specified or null.
    */
    private RemoteStackVariable getRemoteStackVariable (
        RemoteThread rt,
        String variableName
    ) throws Exception {
        RemoteStackFrame frame = rt.getCurrentFrame ();
        if (frame == null) return null;
        return frame.getLocalVariable (variableName);
    }

    /**
    * Returns currently debugged RemoteClass or null.
    *
    * @return currently debugged RemoteClass or null.
    */
    private RemoteClass getRemoteClass (final String className) throws Exception {
        if (toolsVariable.getDebugger().remoteDebugger == null) return null;
        return toolsVariable.getDebugger().remoteDebugger.findClass (className);
    }

    /**
    * Sets error message for this watch.
    */
    void setErrorHelper (String description) {
        setError (description);
    }

    protected void init () {
        if (validator != null) validator.add (this);
        pcs = new PropertyChangeSupport (this);
    }

    // Delegating methods for ToolsVariable instance ..................................

    // Methods from ToolsVariable

    /**
    * Delegating method for ToolsVariable instance
    */
    public void setAsText (final String value) {
        toolsVariable.setAsText (value);
    }

    /**
    * Delegating method for ToolsVariable instance
    */
    public AbstractVariable[] getFields () {
        return toolsVariable.getFields ();
    }

    void setValue (String v) {
        toolsVariable.setValue (v);
    }

    // methods from VariableImpl

    protected java.lang.Object clone () {
        return toolsVariable.clone_protected ();
    }

    public String getAsText () {
        return toolsVariable.getAsText ();
    }

    public String getType () {
        return toolsVariable.getType ();
    }

    public synchronized void addPropertyChangeListener (PropertyChangeListener listener) {
        pcs.addPropertyChangeListener (listener);
    }

    public synchronized void removePropertyChangeListener (PropertyChangeListener listener) {
        pcs.removePropertyChangeListener (listener);
    }

    protected void firePropertyChange (String s, Object o, Object n) {
        pcs.firePropertyChange (s, o, n);
    }

    public String getInnerType () {
        return toolsVariable.getInnerType ();
    }

    public boolean isObject () {
        return toolsVariable.isObject ();
    }

    public boolean isArray () {
        return toolsVariable.isArray ();
    }

    public String getModifiers () {
        return toolsVariable.getModifiers ();
    }

    public String getErrorMessage () {
        return toolsVariable.getErrorMessage ();
    }

    protected void setError (String description) {
        toolsVariable.setError_protected (description);
    }

    public String toString () {
        return toolsVariable.toString ();
    }

}

/*
 * Log
 *  15   Gandalf-post-FCS1.13.4.0    3/28/00  Daniel Prusa    
 *  14   Gandalf   1.13        1/13/00  Daniel Prusa    NOI18N
 *  13   Gandalf   1.12        1/10/00  Jan Jancura     Refresh of locales 
 *       updated
 *  12   Gandalf   1.11        12/30/99 Daniel Prusa    Validator placed into 
 *       Watch
 *  11   Gandalf   1.10        12/21/99 Daniel Prusa    Interfaces Debugger, 
 *       Watch, Breakpoint changed to abstract classes.
 *  10   Gandalf   1.9         11/8/99  Jan Jancura     Somma classes renamed
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         9/15/99  Jan Jancura     
 *  7    Gandalf   1.6         7/30/99  Jan Jancura     
 *  6    Gandalf   1.5         7/21/99  Jan Jancura     
 *  5    Gandalf   1.4         6/10/99  Jan Jancura     
 *  4    Gandalf   1.3         6/9/99   Jan Jancura     
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/4/99   Jan Jancura     
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 */
