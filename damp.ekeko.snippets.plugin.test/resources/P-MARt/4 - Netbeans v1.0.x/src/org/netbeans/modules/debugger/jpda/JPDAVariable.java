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
import java.lang.reflect.Modifier;
import java.util.ResourceBundle;
import java.util.List;

import javax.swing.SwingUtilities;

import com.sun.jdi.Value;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.InvalidTypeException;
import com.sun.tools.example.debug.expr.ExpressionParser;
import com.sun.tools.example.debug.expr.ParseException;

import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.debugger.Watch;
import org.openide.debugger.DebuggerNotFoundException;

import org.netbeans.modules.debugger.support.VariableImpl;
import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.util.Validator;
import org.netbeans.modules.debugger.support.util.Protector;
import org.netbeans.modules.debugger.support.util.Utils;


/**
* JPDA implemetation of variable.
*
* @author   Jan Jancura
*/
public class JPDAVariable extends VariableImpl {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -4908841115435123749L;
    /** bundle to obtain text information from */
    private static ResourceBundle                 bundle = NbBundle.getBundle (JPDAVariable.class);

    //static int num = 0;
    //int myNum;

    // variables .........................................................................

    /** Variables parentObject & (name | index) identifies variable. */
    private transient ObjectReference             parentObject;
    /** Current value - used for getFields (). */
    protected transient Value                     remoteValue;
    /** Current field. */
    protected transient Field                     remoteField;
    /** Stack Frame for locales. */
    private transient StackFrame                  stackFrame = null;
    /** Cashing of children variables */
    private transient ObjectReference             oldObject;
    /** Cashing of children variables */
    private transient AbstractVariable[]          oldFields;
    protected transient JPDADebugger              debugger;


    // init ...............................................................................

    protected void finalize () {
        //S ystem.out.println("fuck out JPDAVariable " + getVariableName ());
    }

    /**
    * Non public constructor.
    */
    JPDAVariable (JPDADebugger debugger, boolean validate) {
        super (debugger, validate ? debugger.getValidator () : null);
        this.debugger = debugger;
    }

    /**
    * Creates variable.
    */
    private JPDAVariable (
        JPDADebugger     debugger,
        ObjectReference  parentObject,
        Field            field
    ) {
        this (debugger, true);
        update (field, parentObject);
        //S ystem.out.println("new JPDAVariable " + getVariableName ());
    }

    /**
    * Creates local variable. Called from thread, LineBreakpoint ...
    */
    JPDAVariable (
        JPDADebugger  debugger,
        String        name,
        Value         value,
        String        type
    ) {
        this (debugger, true);
        update (name, value, type);
        //S ystem.out.println("new JPDAVariable " + getVariableName ());
    }

    /**
    * Creates local variable. Called from thread.
    */
    JPDAVariable (
        JPDADebugger  debugger,
        String        name,
        Value         value,
        String        type,
        StackFrame    stackFrame
    ) {
        this (debugger, true);
        update (name, value, type, stackFrame);
        //S ystem.out.println("new JPDAVariable " + getVariableName ());
    }

    /**
    * Creates member varaible of array.
    */
    private JPDAVariable (
        JPDADebugger  debugger,
        ArrayReference array,
        String        parentName,
        int           index,
        String        type
    ) {
        this (debugger, true);
        update (parentName, array, index, type);
        //S ystem.out.println("new JPDAVariable " + getVariableName ());
    }

    private void readObject (java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject ();
        try {
            debugger = (JPDADebugger) TopManager.getDefault ().getDebugger ();
        } catch (DebuggerNotFoundException e) {
            throw new java.io.IOException ();
        }
    }


    // AbstractVariable implementation ...........................................

    /**
    * Setter that allows to change value of the watched variable.
    *
    * @param value text representation of the value
    * @exception DebuggerException if the value cannot be changed or the
    *    string does not represent valid value
    */
    public void setAsText (final String value) {
        if (debugger.getState () != debugger.DEBUGGER_STOPPED) return;
        if (isCloned) return; // fixed watch
        final JPDAThread tt = (JPDAThread) debugger.getCurrentThread ();
        if (tt == null) return;
        String errorMsg = getErrorMessage ();
        if (errorMsg != null) {
            throw Utils.localizeException (
                new IllegalArgumentException (),
                bundle.getString ("EXC_Value_cannt_be_set_info") + " " + errorMsg // NOI18N
            );
        }
        ThreadReference rt = tt.getThreadReference ();
        final StackFrame sf;
        try {
            sf = rt.frame (0);
        } catch (Exception e) {
            return;
        }
        Value v = null;
        try {
            v = (Value) new Protector ("JPDAVariable.ExpressionParser") { // NOI18N
                    public Object protect () throws Exception {
                        Value vv = ExpressionParser.evaluate (
                                       value,
                                       debugger.virtualMachine,
                                       new ExpressionParser.GetFrame () {
                                           public StackFrame get () {
                                               return sf;
                                           }
                                       }
                                   );
                        return vv;
                    }
                }.throwAndWait (null);
        } catch (ParseException e) {
            // a pacth for static variables ******************************************
            try {
                ReferenceType refType = sf.thisObject ().referenceType ();
                v = refType.getValue (refType.fieldByName (value));
            }
            catch (Exception  ee) {
                throw Utils.localizeException (
                    new IllegalArgumentException (),
                    bundle.getString ("EXC_Value_cannt_be_set_info") + " " + e.getMessage() //NOI18N
                );
            }
            // end of patch ***********************************************************
        }
        catch (Throwable e) {
            throw Utils.localizeException (
                new IllegalArgumentException (),
                bundle.getString ("EXC_Value_cannt_be_set_info") + " " + e.getMessage() //NOI18N
            );
        }
        // find the variable
        Boolean result = new Boolean (false);
        final Value v2 = v;
        try {
            final int ind = index;
            result = (Boolean) new Protector ("JPDAVariable.ExpressionParser") { // NOI18N
                         public Object protect () throws Exception {
                             if (stackFrame != null) { // locales
                                 LocalVariable localVariable = stackFrame.visibleVariableByName (
                                                                   getVariableName ()
                                                               );
                                 if (localVariable != null)
                                     stackFrame.setValue (localVariable, v2);
                                 else // local variable variable cannot be found
                                     return new Boolean (false);
                             }
                             else {
                                 if (parentObject == null) { // watch
                                     LocalVariable localVariable = sf.visibleVariableByName (
                                                                       getVariableName ()
                                                                   );
                                     if (localVariable != null)
                                         sf.setValue (localVariable, v2);
                                     else {
                                         ObjectReference obj = sf.thisObject ();
                                         if (obj == null)
                                             return new Boolean (false);
                                         ReferenceType ref = obj.referenceType ();
                                         Field field = ref.fieldByName (getVariableName ());
                                         if (field != null)
                                             sf.thisObject ().setValue (field, v2);
                                         else // variable is an expression
                                             return new Boolean (false);
                                     }
                                 }
                                 else { // parentObject != null
                                     if (parentObject instanceof ArrayReference)
                                         ((ArrayReference) parentObject).setValue (ind, v2);
                                     else
                                         parentObject.setValue (remoteField, v2);
                                 }
                             }
                             return new Boolean (true);
                         }
                     }.throwAndWait (null);
        }
        catch (InvalidTypeException e) {
            throw Utils.localizeException (
                new IllegalArgumentException (),
                bundle.getString ("EXC_Wrong_type")
            );
        }
        catch (Exception e) {
            throw Utils.localizeException (
                new IllegalArgumentException (),
                bundle.getString ("EXC_Value_cannt_be_set")
            );
        }
        if (! result.booleanValue ()) {
            // variable is an expression
            throw Utils.localizeException (
                new IllegalArgumentException (),
                bundle.getString ("EXC_Cannot_assign_to_expression")
            );
        }
        if (v == null) {
            // setNull
            remoteValue = null;
            type = ""; // NOI18N
            update ();
            // end of setNull
        }
        else {
            // update
            remoteValue = v;
            type = v.type ().name ();
            update ();
            // end of update
        }
        if (pcs != null)
            pcs.firePropertyChange (null, null, null);
    }

    /**
    * If this AbstractVariable object represents instance of some class or array this method
    * returns variables (static and non-static) of this object.
    *
    * @return variables (static and non-static) of this object.
    */
    public AbstractVariable[] getFields () {
        final String name = this.name;
        //S ystem.out.println ("getFields " + remoteValue + " " + oldObject); // NOI18N
        if ((remoteValue == null) ||
                !(remoteValue instanceof ObjectReference)
           ) {
            //S ystem.out.println ("getFields prim."); // NOI18N
            return new JPDAVariable [0];
        }
        if ((oldObject != null) && oldObject.equals (remoteValue)) {
            // the same set of children => validate only
            //S ystem.out.println ("getFields return old " + oldFields + " " + oldFields.length); // NOI18N
            //      int i, k = oldFields.length;
            //      for (i = 0; i < k; i++) oldFields [i].validate ();
            return oldFields;
        }
        try {
            AbstractVariable[] variable;
            ObjectReference remoteObject = (ObjectReference)remoteValue;
            if (remoteValue instanceof ArrayReference) {
                final String type = innerType.substring (0, innerType.length () - 2);
                ArrayReference array = (ArrayReference) remoteValue;
                int i, k = array.getValues ().size ();
                variable = new AbstractVariable [k];
                for (i = 0; i < k; i++)
                    variable [i] = new JPDAVariable (
                                       debugger,
                                       array,
                                       name,
                                       i,
                                       type
                                   );
                //S ystem.out.println ("getFields return array " + variable + " " + variable.length); // NOI18N
            } else {
                List fields = ((ObjectReference) remoteValue).referenceType ().allFields ();
                int i, k = fields.size ();
                variable = new AbstractVariable [k];
                for (i = 0; i < k; i++)
                    variable [i] = new JPDAVariable (
                                       debugger,
                                       remoteObject,
                                       (Field) fields.get (i)
                                   );
                //S ystem.out.println ("getFields return fields " + variable + " " + variable.length); // NOI18N
            }
            oldObject = remoteObject;
            oldFields = variable;
            return variable;
        } catch (Exception e) {
            //e.printStackTrace ();
            return new JPDAVariable [0];
        }
    }

    /**
    * Returns true if this variable hasn't any fields.
    *
    * @return True if this variable hasn't any fields.
    */
    public boolean isLeaf () {
        return (remoteValue == null) || !(remoteValue instanceof ObjectReference);
    }


    // other methods ....................................................................................

    /**
    * I am member of an array. 
    * Update modifiers, type, remoteValue, parentObject.
    */
    boolean update (
        String         name,
        ArrayReference array,
        int            index,
        String         type
    ) {
        this.name = name + " [" + index + "]"; // NOI18N
        parentObject = array;
        this.index = index;
        this.type = type;
        modifiers = ""; // NOI18N
        try {
            remoteValue = array.getValue (index);
        } catch (Exception e) {
            return false;
        }
        update ();
        return true;
    }

    /**
    * Init for remoteObject
    * modifiers, type, remoteValue, parentObject
    */
    boolean update (
        Field            field,
        ObjectReference  parentObject
    ) {
        this.name = field.name ();
        this.parentObject = parentObject;
        try {
            remoteField = field;
            if (remoteField == null) return false;
            remoteValue = parentObject.getValue (remoteField);
            modifiers = Modifier.toString (remoteField.modifiers ());
            type = remoteField.typeName ();
        } catch (Exception e) {
            return false;
        }
        update ();
        return true;
    }

    /**
    * For local variables. (locales or watch)
    */
    void update (
        String        name,
        Value         remoteValue,
        String        type
    ) {
        this.name = name;
        this.remoteValue = remoteValue;
        this.parentObject = null;
        this.type = type;
        modifiers = ""; // NOI18N
        update ();
    }

    /**
    * For local variables, called from thread.
    */
    void update (
        String        name,
        Value         remoteValue,
        String        type,
        StackFrame    stackFrame
    ) {
        this.stackFrame = stackFrame;
        update (name, remoteValue, type);
    }

    void setNull () {
        remoteValue = null;
        parentObject = null;
        modifiers = ""; // NOI18N
        type = ""; // NOI18N
        update ();
    }

    protected void setError (String description) {
        remoteValue = null;
        parentObject = null;
        super.setError (description);
    }

    /**
    * 
    */
    public void validate () {
        if (isCloned) {
            if (isObject) {
                value = remoteValue.toString ();
            }
        } else {
            if (index != -1)
                if (parentObject instanceof ArrayReference)
                    try {
                        remoteValue = ((ArrayReference) parentObject).getValue (index);
                    } catch (Exception e) {
                    }
                else remoteValue = null;
            else
                try {
                    remoteValue = parentObject.getValue (remoteField);
                } catch (Exception e) {
                }
            update ();
        }

        if (pcs != null) pcs.firePropertyChange (null, null, null);
    }

    /**
    * @return true if debugger is stopped.
    */
    public boolean canValidate () {
        return debugger.getState () == JPDADebugger.DEBUGGER_STOPPED;
    }

    /**
    * @return true, variable can be removed from validator when debugger is finished
    */
    public boolean canRemove () {
        return true;
    }

    /**
    * remoteValue => isObject, isArray, value, innerType
    * + fire
    */
    private void update () {
        if (remoteValue == null) {
            isObject = false;
            isArray = false;
            value = null;
            innerType = ""; // NOI18N
        } else
            try {
                isObject = remoteValue instanceof ObjectReference;
                isArray = remoteValue instanceof ArrayReference;
                if (isArray) {
                    value = remoteValue.toString ();
                    innerType = ((ObjectReference)remoteValue).referenceType ().name ();
                } else
                    if (isObject) {
                        value = remoteValue.toString ();
                        innerType = ((ObjectReference)remoteValue).referenceType ().name ();
                    } else {
                        innerType = remoteValue.type ().name ();
                        value = remoteValue.toString ();
                    }
            } catch (Exception e) {
                // exception will be notified
                type = null;
                value = e.toString ();
            }
    }

    void setValue (String v) {
        value = v;
    }

    void firePropertyChange () {
        super.firePropertyChange (null, null, null);
    }

    // Helper methods enabling delegating in JPDAWatch

    JPDADebugger getDebugger () {
        return debugger;
    }

    void setErrorMessage (String errMessage) {
        errorMessage = errMessage;
    }

    Value getRemoteValue () {
        return remoteValue;
    }

    void setRemoteValue (Value val) {
        remoteValue = val;
    }

    void setError_protected (String description) {
        setError (description);
    }

    java.lang.Object clone_protected () {
        return clone ();
    }

}

/*
 * Log
 *  10   Gandalf-post-FCS1.8.4.0     3/28/00  Daniel Prusa    
 *  9    Gandalf   1.8         1/14/00  Daniel Prusa    NOI18N
 *  8    Gandalf   1.7         1/13/00  Daniel Prusa    NOI18N
 *  7    Gandalf   1.6         1/3/00   Daniel Prusa    
 *  6    Gandalf   1.5         12/30/99 Daniel Prusa    Validator placed into 
 *       Watch
 *  5    Gandalf   1.4         12/21/99 Daniel Prusa    Interfaces Debugger, 
 *       Watch, Breakpoint changed to abstract classes.
 *  4    Gandalf   1.3         11/8/99  Jan Jancura     Somma classes renamed
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/5/99  Jan Jancura     Serialization of 
 *       debugger.
 *  1    Gandalf   1.0         9/2/99   Jan Jancura     
 * $
 */
