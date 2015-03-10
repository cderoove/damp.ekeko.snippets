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

import java.util.Iterator;
import java.util.ArrayList;

import org.openide.TopManager;
import org.openide.debugger.DebuggerNotFoundException;

import org.netbeans.modules.debugger.support.AbstractDebugger;

/** A property editor for available debugger types.
* @author   Jan Jancura
* @version  0.10, May 22, 1998
*/

public class DebuggerTypeEditor extends java.beans.PropertyEditorSupport {

    /*
    * @return The property value as a human editable string.
    * <p>   Returns null if the value can't be expressed as an editable string.
    * <p>   If a non-null value is returned, then the PropertyEditor should
    *       be prepared to parse that string back in setAsText().
    */
    public String getAsText() {
        return (String) getValue();
    }

    /* Set the property value by parsing a given String.  May raise
    * java.lang.IllegalArgumentException if either the String is
    * badly formatted or if this kind of property can't be expressed
    * as text.
    * @param text  The string to be parsed.
    */
    public void setAsText (String text) throws java.lang.IllegalArgumentException {
        setValue(text);
    }

    /*
    * If the property value must be one of a set of known tagged values,
    * then this method should return an array of the tag values.  This can
    * be used to represent (for example) enum values.  If a PropertyEditor
    * supports tags, then it should support the use of setAsText with
    * a tag value as a way of setting the value.
    *
    * @return The tag values for this property.  May be null if this
    *   property cannot be represented as a tagged value.
    *
    */
    public String[] getTags() {
        try {
            DelegatingDebugger debugger = (DelegatingDebugger) TopManager.getDefault ().getDebugger ();
            Iterator list = debugger.getRegisteredDebuggers ().iterator ();
            ArrayList types = new ArrayList ();
            while (list.hasNext ()) {
                AbstractDebugger deb = debugger.createDebugger ((Class) list.next ());
                types.add (deb.getVersion ());
            }
            String [] result = new String [types.size()];
            Object [] res = types.toArray ();
            for (int x = 0; x < types.size(); x++)
                result [x] = (String) res [x];
            return result;
        }
        catch (DebuggerNotFoundException e) {
        }
        return null;
    }

}