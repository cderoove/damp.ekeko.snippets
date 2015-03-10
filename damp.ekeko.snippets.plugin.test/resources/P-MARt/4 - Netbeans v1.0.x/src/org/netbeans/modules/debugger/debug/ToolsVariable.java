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
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;

import sun.tools.debug.*;

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
*
*
* @author   Jan Jancura
* @version  0.17, Apr 29, 1998
*/
public class ToolsVariable extends VariableImpl {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -4908841115435797749L;
    /** bundle to obtain text information from */
    private static ResourceBundle                 bundle = NbBundle.getBundle (ToolsVariable.class);

    //static int num = 0;
    //int myNum;

    // variables .........................................................................

    /** Variables parentObject & (name | index) identifies variable. */
    private transient RemoteObject                parentObject;
    /** Current value - used for getFields (). */
    protected transient RemoteValue               remoteValue;
    /** Cashing of children variables */
    private transient RemoteObject                oldObject;
    /** Cashing of children variables */
    private transient AbstractVariable[]          oldFields;
    protected transient ToolsDebugger             debugger;


    // init ...............................................................................

    /**
    * Non public constructor.
    */
    ToolsVariable (ToolsDebugger debugger, boolean validate) {
        super (debugger, validate ? debugger.getValidator () : null);
        this.debugger = debugger;
    }

    /**
    * Non public constructor called from the ToolsVariable only.
    */
    ToolsVariable (
        ToolsDebugger debugger,
        RemoteObject  parentObject,
        RemoteField   remoteField
    ) {
        this (debugger, true);
        update (remoteField, parentObject);
    }

    /**
    * Non public constructor called from the TheThread only. Creates local variable.
    */
    ToolsVariable (
        ToolsDebugger debugger,
        String        name,
        RemoteValue   value,
        String        type
    ) {
        this (debugger, false);
        update (name, value, type);
    }

    /**
    * Non public constructor called from the AbstractVariable only.
    */
    ToolsVariable (
        ToolsDebugger debugger,
        RemoteArray   array,
        String        parentName,
        int           index,
        String        type
    ) {
        this (debugger, true);
        update (parentName, array, index, type);
    }

    private void readObject (java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject ();
        try {
            debugger = (ToolsDebugger) TopManager.getDefault ().getDebugger ();
        } catch (DebuggerNotFoundException e) {
            throw new java.io.IOException ();
        }
    }


    // AbstractVariable implementation ............................................................

    /**
    * Setter that allows to change value of the watched variable.
    *
    * @param value text representation of the value
    * @exception DebuggerException if the value cannot be changed or the
    *    string does not represent valid value
    */
    public void setAsText (final String value) {
        final String type = this.type;
        final String name = this.name;
        final PropertyChangeSupport pcs = this.pcs;
        new Protector ("ToolsVariable.setAsText") { // NOI18N
            public Object protect () throws Exception {
                if (parentObject == null)
                    throw Utils.localizeException (
                        new IllegalArgumentException (),
                        bundle.getString ("EXC_Static")
                    );
                if (type.equals ("int")) { // NOI18N
                    try {
                        parentObject.setField (name, Integer.parseInt (value));
                        setValue (value);
                    } catch (NumberFormatException e) {
                        throw Utils.localizeException (
                            new IllegalArgumentException (),
                            bundle.getString ("EXC_Wrong_format")
                        );
                    } catch (Exception e) {
                        throw Utils.localizeException (
                            new IllegalArgumentException (),
                            bundle.getString ("EXC_Value_cannt_be_set")
                        );
                    }
                } else
                    if (type.equals ("byte")) { // NOI18N
                        try {
                            parentObject.setField (name, Byte.parseByte (value));
                            setValue (value);
                        } catch (NumberFormatException e) {
                            throw Utils.localizeException (
                                new IllegalArgumentException (),
                                bundle.getString ("EXC_Wrong_format")
                            );
                        } catch (Exception e) {
                            throw Utils.localizeException (
                                new IllegalArgumentException (),
                                bundle.getString ("EXC_Value_cannt_be_set")
                            );
                        }
                    } else
                        if (type.equals ("short")) { // NOI18N
                            try {
                                parentObject.setField (name, Short.parseShort (value));
                                setValue (value);
                            } catch (NumberFormatException e) {
                                throw Utils.localizeException (
                                    new IllegalArgumentException (),
                                    bundle.getString ("EXC_Wrong_format")
                                );
                            } catch (Exception e) {
                                throw Utils.localizeException (
                                    new IllegalArgumentException (),
                                    bundle.getString ("EXC_Value_cannt_be_set")
                                );
                            }
                        } else
                            if (type.equals ("boolean")) { // NOI18N
                                Boolean b = Boolean.valueOf (value);
                                if (b == null)
                                    throw Utils.localizeException (
                                        new IllegalArgumentException (),
                                        bundle.getString ("EXC_Wrong_format")
                                    );
                                try {
                                    parentObject.setField (name, b.booleanValue ());
                                    setValue (value);
                                } catch (Exception e) {
                                    throw Utils.localizeException (
                                        new IllegalArgumentException (),
                                        bundle.getString ("EXC_Value_cannt_be_set")
                                    );
                                }
                            } else
                                if (type.equals ("char")) { // NOI18N
                                    if (value.length () < 1)
                                        throw Utils.localizeException (
                                            new IllegalArgumentException (),
                                            bundle.getString ("EXC_Wrong_format")
                                        );
                                    try {
                                        parentObject.setField (name, value.charAt (0));
                                        setValue (value);
                                    } catch (Exception e) {
                                        throw Utils.localizeException (
                                            new IllegalArgumentException (),
                                            bundle.getString ("EXC_Value_cannt_be_set")
                                        );
                                    }
                                } else
                                    if (type.equals ("double")) { // NOI18N
                                        try {
                                            parentObject.setField (name, new Double (value).doubleValue ());
                                            setValue (value);
                                        } catch (NumberFormatException e) {
                                            throw Utils.localizeException (
                                                new IllegalArgumentException (),
                                                bundle.getString ("EXC_Wrong_format")
                                            );
                                        } catch (Exception e) {
                                            throw Utils.localizeException (
                                                new IllegalArgumentException (),
                                                bundle.getString ("EXC_Value_cannt_be_set")
                                            );
                                        }
                                    } else
                                        if (type.equals ("float")) { // NOI18N
                                            try {
                                                parentObject.setField (name, new Float (value).floatValue ());
                                                setValue (value);
                                            } catch (NumberFormatException e) {
                                                throw Utils.localizeException (
                                                    new IllegalArgumentException (),
                                                    bundle.getString ("EXC_Wrong_format")
                                                );
                                            } catch (Exception e) {
                                                throw Utils.localizeException (
                                                    new IllegalArgumentException (),
                                                    bundle.getString ("EXC_Value_cannt_be_set")
                                                );
                                            }
                                        } else
                                            if (type.equals ("long")) { // NOI18N
                                                try {
                                                    parentObject.setField (name, Long.parseLong (value));
                                                    setValue (value);
                                                } catch (NumberFormatException e) {
                                                    throw Utils.localizeException (
                                                        new IllegalArgumentException (),
                                                        bundle.getString ("EXC_Wrong_format")
                                                    );
                                                } catch (Exception e) {
                                                    throw Utils.localizeException (
                                                        new IllegalArgumentException (),
                                                        bundle.getString ("EXC_Value_cannt_be_set")
                                                    );
                                                }
                                            } else
                                                throw Utils.localizeException (
                                                    new IllegalArgumentException (),
                                                    bundle.getString ("EXC_Unsupported_type")
                                                );
                return null;
            }
        }.wait (debugger.synchronizer, debugger.killer);
        // deadlock prevention
        if (pcs != null) pcs.firePropertyChange (Watch.PROP_AS_TEXT, null, null);
    }

    /**
    * If this AbstractVariable object represents instance of some class or array this method
    * returns variables (static and non-static) of this object.
    *
    * @return variables (static and non-static) of this object.
    */
    public AbstractVariable[] getFields () {
        if (debugger.synchronizer == null)
            return new ToolsVariable [0];
        final String type;
        if (remoteValue instanceof RemoteArray)
            type = innerType.substring (0, innerType.length () - 2);
        else
            type = innerType;
        final String name = this.name;
        return (AbstractVariable[]) new Protector ("ToolsVariable.getFields") { // NOI18N
                   public Object protect () throws Exception {
                       //S ystem.out.println ("getFields " + remoteValue + " " + oldObject); // NOI18N
                       if ((remoteValue == null) ||
                               !(remoteValue instanceof RemoteObject)
                          ) {
                           //S ystem.out.println ("getFields prim."); // NOI18N
                           return new ToolsVariable [0];
                       }
                       if ((oldObject != null) && oldObject.equals (remoteValue)) {
                           //S ystem.out.println ("getFields return old " + oldFields + " " + oldFields.length); // NOI18N
                           //          int i, k = oldFields.length;
                           //          for (i = 0; i < k; i++) oldFields [i].validate ();
                           return oldFields;
                       }
                       try {
                           AbstractVariable[] variable;
                           RemoteObject remoteObject = (RemoteObject)remoteValue;
                           if (remoteValue instanceof RemoteArray) {
                               RemoteArray array = (RemoteArray) remoteValue;
                               RemoteValue[] values = array.getElements ();
                               int i, k = values.length;
                               variable = new AbstractVariable [k];
                               for (i = 0; i < k; i++)
                                   variable [i] = new ToolsVariable (
                                                      debugger,
                                                      array,
                                                      name,
                                                      i,
                                                      type
                                                  );
                               //S ystem.out.println ("getFields return array " + variable + " " + variable.length); // NOI18N
                           } else {
                               RemoteField[] remoteField = ((RemoteObject)remoteValue).getFields ();
                               RemoteClass remoteClass = remoteObject.getClazz ();
                               RemoteField[] sRemoteField = remoteClass.getFields ();
                               variable = new AbstractVariable [remoteField.length + sRemoteField.length];
                               int i, k = remoteField.length;
                               for (i = 0; i < k; i++)
                                   variable [i] = new ToolsVariable (
                                                      debugger,
                                                      remoteObject,
                                                      remoteField [i]
                                                  );
                               int l = sRemoteField.length;
                               for (i = 0; i < l; i++)
                                   variable [i + k] = new ToolsVariable (
                                                          debugger,
                                                          remoteClass,
                                                          sRemoteField [i]
                                                      );
                               //S ystem.out.println ("getFields return fields " + variable + " " + variable.length); // NOI18N
                           }
                           oldObject = remoteObject;
                           oldFields = variable;
                           return variable;
                       } catch (Exception e) {
                           return new ToolsVariable [0];
                       }
                   }
               }.wait (debugger.synchronizer, debugger.killer);
    }

    /**
    * Returns true if this variable hasn't any fields.
    *
    * @return True if this variable hasn't any fields.
    */
    public boolean isLeaf () {
        return (remoteValue == null) || !(remoteValue instanceof RemoteObject);
    }


    // other methods ....................................................................................

    /**
    * I am member of field. 
    * modifiers, type, remoteValue, parentObject
    */
    boolean update (
        String        name,
        RemoteArray   array,
        int           index,
        String        type
    ) {
        this.name = name + " [" + index + "]"; // NOI18N
        parentObject = array;
        this.index = index;
        this.type = type;
        modifiers = ""; // NOI18N
        try {
            remoteValue = array.getElement (index);
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
        RemoteField   remoteField,
        RemoteObject  parentObject
    ) {
        if (remoteField == null) return false;
        this.name = remoteField.getName ();
        this.parentObject = parentObject;
        try {
            remoteValue = parentObject.getFieldValue (name);
            modifiers = remoteField.getModifiers ();
            type = remoteField.getType ().toString ();
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
        RemoteValue   remoteValue,
        String        type
    ) {
        this.name = name;
        this.remoteValue = remoteValue;
        this.parentObject = null;
        this.type = type;
        modifiers = ""; // NOI18N
        update ();
    }

    void setNull () {
        remoteValue = null;
        parentObject = null;
        modifiers = ""; // NOI18N
        type = ""; // NOI18N
        update ();
    }

    /**
    * Validate value of variable (validateIn) and fires changes.
    */
    public void validate () {
        if (debugger.synchronizer == null) return;
        new Protector ("ToolsVariable.validate") { // NOI18N
            public Object protect () {
                validateUnsafe ();
                return null;
            }
        }.wait (debugger.synchronizer, debugger.killer);
        // deadlock prevention
        final PropertyChangeSupport p = pcs;
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            firePropertyChange ();
                                        }
                                    });
    }

    /**
    * @return true if debugger is stopped.
    */
    public boolean canValidate () {
        return debugger.getState () == ToolsDebugger.DEBUGGER_STOPPED;
    }

    /**
    * @return true, variable can be removed from validator when debugger is finished
    */
    public boolean canRemove () {
        return true;
    }

    private void validateUnsafe() {
        if (isCloned) {
            if (isObject) {
                value = remoteValue.toString ();
                checkString ();
            }
            return;
        } else
            if (index != -1)
                if (parentObject instanceof RemoteArray)
                    try {
                        remoteValue = ((RemoteArray) parentObject).getElement (index);
                    } catch (Exception e) {
                    }
                else remoteValue = null;
            else
                if (parentObject != null)
                    try {
                        remoteValue = parentObject.getFieldValue (name);
                    } catch (Exception e) {
                    }
                else
                    remoteValue = null;
        update ();
    }

    /**
    * remoteValue => isObject, isArray, value, innerType
    */
    private void update () {
        errorMessage = null;
        if (remoteValue == null) {
            isObject = false;
            isArray = false;
            value = null;
            innerType = ""; // NOI18N
        } else
            try {
                isObject = remoteValue.isObject ();
                isArray = remoteValue instanceof RemoteArray;
                if (isArray) {
                    value = remoteValue.toString ();
                    checkString ();
                    innerType = obtainType (((RemoteArray)remoteValue).getClazz ().getName ());
                } else
                    if (isObject) {
                        value = remoteValue.toString ();
                        checkString ();
                        innerType = ((RemoteObject)remoteValue).getClazz ().getName ();
                    } else {
                        innerType = remoteValue.typeName ();
                        value = remoteValue.description ();
                        // value of byte or short is returned in hexadecimal format, decimal format is required
                        if (innerType.equals ("byte") || innerType.equals ("short")) { // NOI18N
                            try {
                                value = Integer.toString ((Integer.decode (value)).intValue ());
                            }
                            catch (NumberFormatException e) {
                            }
                        }
                    }
            } catch (Exception e) {
                // exception will be notified
                type = null;
                value = e.toString ();
            }
    }

    /**
    * PATCH: Extracts type name from a string returned by sun.tools.debug.RemoteClass.getName () for an array.
    */
    private String obtainType (String string) {
        try {
            if (!string.startsWith("[")) // NOI18N
                return string;
            int index = string.lastIndexOf ('[');
            String base = ""; // NOI18N
            switch (string.charAt (index + 1)) {
            case 'I': base = "int"; break; // NOI18N
            case 'Z': base = "boolean"; break; // NOI18N
            case 'J': base = "long"; break; // NOI18N
            case 'F': base = "float"; break; // NOI18N
            case 'D': base = "double"; break; // NOI18N
            case 'B': base = "byte"; break; // NOI18N
            case 'S': base = "short"; break; // NOI18N
            case 'C': base = "char"; break; // NOI18N
            case 'L': base = string.substring (index + 2, string.length () - 1); break; // NOI18N
            }
            for (int x = 0; x <= index; x++)
                base = base.concat ("[]"); // NOI18N
            return base;
        }
        catch (IndexOutOfBoundsException e) {
            return string;
        }
    }

    private void checkString () {
        try {
            if (type.equals ("java.lang.String")) { // NOI18N
                AbstractVariable [] var = getFields ();
                int k = 0;
                while (k < var.length) {
                    if (var[k].getVariableName().equals("count")) // NOI18N
                        break;
                    k++;
                }
                if ((k < var.length)&&(var[k].getAsText ().equals ("0"))) // NOI18N
                    value = new String (""); // NOI18N
                if (value != null)
                    value = "\"".concat (value.concat ("\"")); // NOI18N
            }
        }
    catch (Exception e) {}
    }

    void setValue (String v) {
        value = v;
    }

    // Helper methods enabling delegating in ToolsWatch

    ToolsDebugger getDebugger () {
        return debugger;
    }

    /*
    public String getErrorMessage () {
      return errorMessage;
}
    */

    void setErrorMessage (String errMessage) {
        errorMessage = errMessage;
    }

    RemoteValue getRemoteValue () {
        return remoteValue;
    }

    void setRemoteValue (RemoteValue val) {
        remoteValue = val;
    }

    java.lang.Object clone_protected () {
        return clone ();
    }

    void setError_protected (String description) {
        setError (description);
    }

    void firePropertyChange () {
        super.firePropertyChange (null, null, null);
    }
}

/*
 * Log
 *  26   Gandalf-post-FCS1.21.3.3    3/30/00  Daniel Prusa    
 *  25   Gandalf-post-FCS1.21.3.2    3/30/00  Daniel Prusa    
 *  24   Gandalf-post-FCS1.21.3.1    3/29/00  Daniel Prusa    
 *  23   Gandalf-post-FCS1.21.3.0    3/28/00  Daniel Prusa    
 *  22   Gandalf   1.21        1/14/00  Daniel Prusa    NOI18N
 *  21   Gandalf   1.20        1/14/00  Daniel Prusa    missing 'else' in 
 *       validateIn added
 *  20   Gandalf   1.19        1/13/00  Daniel Prusa    NOI18N
 *  19   Gandalf   1.18        1/10/00  Jan Jancura     Refresh of locales 
 *       updated
 *  18   Gandalf   1.17        1/5/00   Jan Jancura     Bug 4276
 *  17   Gandalf   1.16        1/4/00   Daniel Prusa    
 *  16   Gandalf   1.15        1/3/00   Daniel Prusa    
 *  15   Gandalf   1.14        12/30/99 Daniel Prusa    Validator placed into 
 *       Watch
 *  14   Gandalf   1.13        12/28/99 Daniel Prusa    Bugfix for CR 1888
 *  13   Gandalf   1.12        12/21/99 Daniel Prusa    Interfaces Debugger, 
 *       Watch, Breakpoint changed to abstract classes.
 *  12   Gandalf   1.11        11/8/99  Jan Jancura     Somma classes renamed
 *  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         10/5/99  Jan Jancura     Serialization of 
 *       debugger.
 *  9    Gandalf   1.8         9/15/99  Jan Jancura     
 *  8    Gandalf   1.7         9/2/99   Jan Jancura     
 *  7    Gandalf   1.6         7/21/99  Jan Jancura     
 *  6    Gandalf   1.5         6/10/99  Jan Jancura     
 *  5    Gandalf   1.4         6/9/99   Jan Jancura     
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/4/99   Jan Jancura     
 *  2    Gandalf   1.1         6/4/99   Jan Jancura     
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 */
