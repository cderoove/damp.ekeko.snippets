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

package org.netbeans.modules.debugger.jpda;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.sun.jdi.Value;
import com.sun.jdi.Field;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.tools.example.debug.expr.ExpressionParser;
import com.sun.tools.example.debug.expr.ParseException;

import org.openide.debugger.Watch;
import org.openide.util.NbBundle;

import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.AbstractWatch;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.PrintAction;
import org.netbeans.modules.debugger.support.util.Validator;
import org.netbeans.modules.debugger.support.util.Protector;


/**
* Standart implementation of Watch interface.
* @see org.openide.debugger.Watch
*
* @author   Jan Jancura
* @version  0.18, Feb 23, 1998
*/
public class JPDAWatch extends AbstractWatch {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 3439367144447814302L;


    // private variables .....................................................

    private JPDAVariable                        var;

    protected transient PropertyChangeSupport   pcs;

    protected transient Validator              validator;

    protected transient boolean                 inScope = false;
    /** Name of watch like xxx.yyy [2]. */
    protected String                            displayName;


    // init .....................................................................

    /**
    * Non public constructor called from the JavaDebugger only.
    * User must create watch from Debugger.getNewWatch () method.
    */
    JPDAWatch (JPDADebugger debugger) {
        var = new JPDAVariable (debugger, false);
        validator = debugger.getValidator ();
        init ();
    }

    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject ();
        validator = var.getDebugger().getValidator ();
        init ();
    }

    protected void init () {
        if (validator != null) validator.add (this);
        pcs = new PropertyChangeSupport (this);
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
        var.getDebugger().removeWatch (this);
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
        Watch[] w = var.getDebugger().getWatches ();
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
        return (getErrorMessage () == null);
        // return inScope;
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
        //S ystem.out.println ("JPDAWatch.validate " + getVariableName () + " : " + // NOI18N
        //debugger + " : " + var.getDebugger().getState ()); // NOI18N
        if (var.getDebugger().getState () == var.getDebugger().DEBUGGER_NOT_RUNNING
           ) {
            setError (NbBundle.getBundle (JPDAWatch.class).getString ("EXC_No_session"));
            pcs.firePropertyChange (null, null, null);
        } else
            if (var.getDebugger().getState () != var.getDebugger().DEBUGGER_STOPPED) {
                setError (NbBundle.getBundle (JPDAWatch.class).getString ("CTL_No_context"));
                pcs.firePropertyChange (null, null, null);
            } else {
                final JPDAThread tt = (JPDAThread) var.getDebugger().getCurrentThread ();
                if (tt != null) {
                    refreshValue (tt);
                } else {
                    setError (NbBundle.getBundle (JPDAWatch.class).getString ("CTL_No_context"));
                    pcs.firePropertyChange (null, null, null);
                }
            }
    }

    /**
    * @return true if debugger is stopped.
    */
    public boolean canValidate () {
        int state = var.getDebugger().getState ();
        return (state == JPDADebugger.DEBUGGER_STOPPED) ||
               (state == JPDADebugger.DEBUGGER_NOT_RUNNING);
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
    void refreshValue (final JPDAThread tt) {
        ThreadReference rt = tt.getThreadReference ();
        final StackFrame sf;
        try {
            sf = rt.frame (0);
        } catch (Exception e) {
            setError (NbBundle.getBundle (JPDAWatch.class).getString ("CTL_No_context"));
            pcs.firePropertyChange (null, null, null);
            return;
        }
        var.setErrorMessage (null);
        Value v;
        try {                                   //S ystem.out.println ("EXPRESSION ENTER " + displayName); // NOI18N
            v = (Value) new Protector ("ExpressionParser") { // NOI18N
                    public Object protect () throws Exception {  //S ystem.out.println ("EXPRESSION ENTER1 " + displayName); // NOI18N
                        Value vv = ExpressionParser.evaluate (
                                       displayName,
                                       var.getDebugger().virtualMachine,
                                       new ExpressionParser.GetFrame () {
                                           public StackFrame get () {
                                               return sf;
                                           }
                                       }
                                   );                                //S ystem.out.println ("EXPRESSION EXIT1  " + displayName); // NOI18N
                        return vv;
                    }
                }.throwAndWait (null);                //S ystem.out.println ("EXPRESSION EXIT  " + displayName); // NOI18N
        } catch (ParseException e) {            //S ystem.out.println ("EXPRESSION EXIT  " + displayName); // NOI18N
            // a pacth for static variables *******************************************
            v = null;
            boolean found = false;
            try {
                ReferenceType refType = sf.thisObject ().referenceType ();
                v = refType.getValue (refType.fieldByName (displayName));
                found = true;
            }
            catch (ObjectCollectedException  ee) {}
            catch (InvalidStackFrameException ee) {}
            catch (ClassNotPreparedException  ee) {}
            catch (java.lang.IllegalArgumentException  ee) {}
            catch (NullPointerException ee) {}
            if (!found) {
                setError (e.getMessage ());
                pcs.firePropertyChange (null, null, null);
                return;
            }
            // end of patch ***********************************************************
        } catch (ThreadDeath e) {               //S ystem.out.println ("EXPRESSION EXIT  " + displayName); // NOI18N
            setError ("Deadlock detected while resolving expression."); // NOI18N
            pcs.firePropertyChange (null, null, null);
            return;
        } catch (Throwable e) {                 //S ystem.out.println ("EXPRESSION EXIT  " + displayName); // NOI18N
            setError (e.toString ());
            pcs.firePropertyChange (null, null, null);
            return;
        }
        if (v == null)
            setNull ();  // really null value?
        else
            update (
                displayName,
                v,
                v.type ().name ()
            );
        pcs.firePropertyChange (null, null, null);
    }

    public void refresh (AbstractThread t) {
        refreshValue ((JPDAThread)t);
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

    // Delegating methods for JPDAVariable instance ..................................

    // Methods from JPDAVariable

    public void setAsText (final String value) {
        var.setAsText (value);
    }

    public AbstractVariable[] getFields () {
        return var.getFields ();
    }

    boolean update (String name, ArrayReference array, int index, String type) {
        return var.update (name, array, index, type);
    }

    boolean update (Field field, ObjectReference parentObject) {
        return var.update (field, parentObject);
    }

    void update (String name, Value remoteValue, String type) {
        var.update (name, remoteValue, type);
    }

    void setNull () {
        var.setNull ();
    }

    void setValue (String v) {
        var.setValue (v);
    }

    protected void setError (String description) {
        var.setError_protected (description);
    }

    // methods from VariableImpl

    protected java.lang.Object clone () {
        return var.clone_protected ();
    }

    public String getAsText () {
        return var.getAsText ();
    }

    public String getType () {
        return var.getType ();
    }

    public String getInnerType () {
        return var.getInnerType ();
    }

    public boolean isObject () {
        return var.isObject ();
    }

    public boolean isArray () {
        return var.isArray ();
    }

    public String getModifiers () {
        return var.getModifiers ();
    }

    public String getErrorMessage () {
        return var.getErrorMessage ();
    }

    public String toString () {
        return var.toString ();
    }

}

/*
 * Log
 *  13   Gandalf-post-FCS1.11.4.0    3/28/00  Daniel Prusa    
 *  12   Gandalf   1.11        1/14/00  Daniel Prusa    NOI18N
 *  11   Gandalf   1.10        1/13/00  Daniel Prusa    NOI18N
 *  10   Gandalf   1.9         12/30/99 Daniel Prusa    Validator placed into 
 *       Watch
 *  9    Gandalf   1.8         12/21/99 Daniel Prusa    Interfaces Debugger, 
 *       Watch, Breakpoint changed to abstract classes.
 *  8    Gandalf   1.7         12/10/99 Jan Jancura     Deadlock protection for 
 *       JPDA
 *  7    Gandalf   1.6         11/8/99  Jan Jancura     Somma classes renamed
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/5/99  Jan Jancura     Serialization of 
 *       debugger.
 *  4    Gandalf   1.3         9/15/99  Jan Jancura     
 *  3    Gandalf   1.2         9/9/99   Jan Jancura     
 *  2    Gandalf   1.1         9/3/99   Jan Jancura     
 *  1    Gandalf   1.0         9/2/99   Jan Jancura     
 * $
 */
