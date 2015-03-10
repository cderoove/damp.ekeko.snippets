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

package org.netbeans.modules.form.compat2;

import java.util.Iterator;
import java.util.Vector;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.form.util2.NbVersion;
import org.netbeans.modules.form.util2.NbVersionNotCompatibleException;
import org.netbeans.modules.form.FormEditor;

/** The RADNode is a Node that represents one component placed on the Form.
*
* @author Ian Formanek, Petr Hamernik
*/
public class RADNode implements java.io.Externalizable {
    /** A JDK 1.1. serial version UID */
    static final long serialVersionUID = 275515950684458282L;

    // -----------------------------------------------------------------------------
    // Versions

    /** Current Netbeans class version
    * version 1.2 saves Hashtable of event handlers instead of String[], 
    *             because of problems with different order under JDK 1.1 and JDK 1.2, which caused bug #1137
    */ 
    public static final NbVersion nbClassVersion = new NbVersion (1, 2);

    /** The version which has not saved the hidden state beans */
    public static final NbVersion nbNoHiddenVersion = new NbVersion (1, 0);
    /** The version which saved event handlers as String[] instead of Hashtable */
    public static final NbVersion nbStringArrayEventsVersion = new NbVersion (1, 1);

    // -----------------------------------------------------------------------------
    // Serialized fields

    public boolean invalidClass = false;

    public Map propertiesMap;

    public Hashtable handlersTable;

    /** The Vector of Object values of properties that were deserialized and
    * should be set in the init () method 
    * @associates Object*/
    transient public HashMap changedValues;

    public boolean hasHiddenState;

    /** The Hashtable of all properties that have been changed (i.e. a setter code for them
    * should be generated) */
    public java.util.Hashtable changedProperties;

    public Hashtable eventHandlers;

    /** The class of the JavaBean represented by this RADNode */
    public Class beanClass;

    /** The name of the component (= the name of the variable for it) */
    public String componentName = ""; // NOI18N

    /** The flag whether to generate local or global variable for the component.
    * True, if global variable should be generated, false for local
    */
    public boolean globalVariable = true;

    /** The FormManager that manages the form that contains this RADNode */
    public FormManager formManager;

    // -----------------------------------------------------------------------------
    // FINALIZE DEBUG METHOD

    public void finalize () throws Throwable {
        super.finalize ();
        if (System.getProperty ("netbeans.debug.form.finalize") != null) {
            System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this); // NOI18N
        }
    } // FINALIZE DEBUG METHOD

    // -----------------------------------------------------------------------------
    // Constructor

    /** For externalization only. */
    public RADNode () {
    }

    // -----------------------------------------------------------------------------
    // Serialization

    /** Fields to restore before calling this method:
    *
    *     beanClass [Class]
    *     changedProperties [Hashtable]
    *     handlersTable [Hashtable]
    *     componentName [String]
    *     hasHiddenState [boolean]
    *     propertiesMap [Map]
    *
    */
    protected void writeExternalImpl (java.io.ObjectOutput oo)
    throws java.io.IOException {
        // store the version
        oo.writeObject (nbClassVersion);

        // beanClass is null for the form itself
        oo.writeObject (beanClass == null? null: beanClass.getName ());
        oo.writeObject (changedProperties);
        oo.writeObject (handlersTable);
        oo.writeObject (componentName);
        oo.writeObject (new Boolean (true)); // globalVariable is always true in Tuborg

        oo.writeObject (new Boolean (hasHiddenState)); // saved from version 1.1

        if (!hasHiddenState) {
            oo.writeInt (propertiesMap.size ());
            for (Iterator it = propertiesMap.keySet ().iterator (); it.hasNext ();) {
                Object name = it.next ();
                oo.writeObject (name);
                oo.writeObject (propertiesMap.get (name));
            }
        } // end of saving beans without hidden state
        else {
            /*      String serName = FormEditor.getSerializedBeanName (this);
                  try {
                    File
                  } 
                  // [PENDING] */
        }

    }

    /** Reads the object from stream.
    * @param ois input stream to read from
    * @exception IOException on error
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    protected void readExternalImpl (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        org.netbeans.modules.form.FormUtils.DEBUG(">> RADNode: readExternal: START"); // NOI18N
        org.netbeans.modules.form.FormUtils.DEBUG("?? RADNode: readExternal: expecting NbVersion"); // NOI18N
        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);
        org.netbeans.modules.form.FormUtils.DEBUG("** RADNode: readExternal: loaded: "+classVersion); // NOI18N

        org.netbeans.modules.form.FormUtils.DEBUG("?? RADNode: readExternal: expecting String (class name)"); // NOI18N
        Object o = oi.readObject ();

        String beanClassName = (String) o;
        if (beanClassName != null) {
            try {
                if (beanClassName.startsWith ("com.sun.java.swing")) { // NOI18N
                    beanClassName = org.openide.util.Utilities.replaceString (beanClassName, "com.sun.java.swing", "javax.swing"); // NOI18N
                }
                beanClass = org.openide.TopManager.getDefault ().currentClassLoader ().loadClass (beanClassName);
            } catch (ClassNotFoundException e) {
                invalidClass = true;
                FormEditor.fileError (java.text.MessageFormat.format (
                                          FormEditor.getFormBundle ().getString ("FMT_ERR_ClassNotFound"),
                                          new Object [] {
                                              e.getMessage (),
                                              e.getClass ().getName (),
                                          }
                                      ), e);
            }
        }

        org.netbeans.modules.form.FormUtils.DEBUG("** RADNode: readExternal: loaded: "+beanClassName); // NOI18N
        org.netbeans.modules.form.FormUtils.DEBUG("?? RADNode: readExternal: expecting Hashtable (changedProperties)"); // NOI18N
        changedProperties = (java.util.Hashtable) oi.readObject ();
        org.netbeans.modules.form.FormUtils.DEBUG("** RADNode: readExternal: loaded: "+changedProperties); // NOI18N

        if (classVersion.compareTo (nbStringArrayEventsVersion) == NbVersion.VERSION_NEWER_COMPATIBLE) {
            org.netbeans.modules.form.FormUtils.DEBUG("?? RADNode: readExternal: expecting Hashtable[] (eventHandlers)"); // NOI18N
            eventHandlers = (Hashtable) oi.readObject ();
            org.netbeans.modules.form.FormUtils.DEBUG("** RADNode: readExternal: loaded: "+eventHandlers); // NOI18N
        } else { // older version serialized event handlers as String[]
            org.netbeans.modules.form.FormUtils.DEBUG("?? RADNode: readExternal: expecting String[] (eventHandlers)"); // NOI18N
            String[] deserializedOldEventHandlers = (String[]) oi.readObject ();
            /*      for (int i = 0; i < deserializedOldEventHandlers.length; i++)
                    if (deserializedOldEventHandlers[i] != null) {
                      FormEditor.setEventsWarningFlag ();
                      break;
                    } */
            eventHandlers = new Hashtable (20);
            org.netbeans.modules.form.FormUtils.DEBUG("** RADNode: readExternal: loaded: "+deserializedOldEventHandlers); // NOI18N
        }
        org.netbeans.modules.form.FormUtils.DEBUG("?? RADNode: readExternal: expecting String (componentName)"); // NOI18N
        componentName = (String) oi.readObject ();
        org.netbeans.modules.form.FormUtils.DEBUG("** RADNode: readExternal: loaded: "+componentName); // NOI18N
        org.netbeans.modules.form.FormUtils.DEBUG("?? RADNode: readExternal: expecting Boolean (globalVariable)"); // NOI18N
        globalVariable = ((Boolean) oi.readObject ()).booleanValue ();
        org.netbeans.modules.form.FormUtils.DEBUG("** RADNode: readExternal: loaded: "+globalVariable); // NOI18N

        boolean readProperties = true;

        if (!classVersion.equals (nbNoHiddenVersion)) { // from version 1.1 a hiddenState boolean value is saved
            org.netbeans.modules.form.FormUtils.DEBUG("?? RADNode: readExternal: expecting Boolean (hasHiddenState)"); // NOI18N
            hasHiddenState = ((Boolean) oi.readObject ()).booleanValue ();
            org.netbeans.modules.form.FormUtils.DEBUG("** RADNode: readExternal: loaded: "+hasHiddenState); // NOI18N
            if (hasHiddenState)
                readProperties = false;
        }

        if (readProperties) {
            // read changed properties
            changedValues = new HashMap ();

            org.netbeans.modules.form.FormUtils.DEBUG("?? RADNode: readExternal: expecting int (length of changed properties)"); // NOI18N
            int count = oi.readInt ();
            org.netbeans.modules.form.FormUtils.DEBUG("** RADNode: readExternal: loaded: "+count); // NOI18N
            for (int i = 0; i < count; i++) {
                org.netbeans.modules.form.FormUtils.DEBUG("?? RADNode: readExternal: [] expecting Object (property name)"); // NOI18N
                Object iname = oi.readObject ();
                org.netbeans.modules.form.FormUtils.DEBUG("** RADNode: readExternal: loaded: "+iname); // NOI18N
                org.netbeans.modules.form.FormUtils.DEBUG("?? RADNode: readExternal: [] expecting Object (property value)"); // NOI18N
                Object ivalue = oi.readObject ();
                org.netbeans.modules.form.FormUtils.DEBUG("** RADNode: readExternal: loaded: "+ivalue); // NOI18N
                changedValues.put (iname, ivalue);
            }
        }
        org.netbeans.modules.form.FormUtils.DEBUG("<< RADNode: readExternal: END"); // NOI18N
    }

    /** Writes the object to the stream.
    * @param oo output stream to write to
    * @exception IOException Includes any I/O exceptions that may occur
    */
    final public void writeExternal (java.io.ObjectOutput oo)
    throws java.io.IOException {
        writeExternalImpl (oo);
    }

    /** Reads the object from stream.
    * @param oi input stream to read from
    * @exception IOException Includes any I/O exceptions that may occur
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    final public void readExternal (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        readExternalImpl (oi);
    }

}

/*
 * Log
 */
