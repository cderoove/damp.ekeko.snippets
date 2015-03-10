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

package org.openide.debugger;

import java.beans.PropertyChangeListener;

/** Represents one debugger watch.
* It contains only basic information
* that can be used by the core of the IDE.
* For example, the editor can use this object
* to display tooltips above variable names in the edited text.
* <P>
* It is likely that the real implementation of the watch can offer more
* functionality to the user--in which case it should expose properties
* as a JavaBean.
*
* @author   Jan Jancura, Jaroslav Tulach
*/
public abstract class Watch implements java.io.Serializable {
    /** Name of the property for the watched variable name. */
    public static final String PROP_VARIABLE_NAME = "variableName"; // NOI18N
    /** Name of the property for the value of the watched expression as a string. */
    public static final String PROP_AS_TEXT = "asText"; // NOI18N
    /** Name of the property for the type of the watch value. */
    public static final String PROP_TYPE = "type"; // NOI18N


    static final long serialVersionUID =2309154226451869624L;
    /** Remove the watch from the list of all watches in the system.
    */
    public abstract void remove ();

    /** Get the name of the variable to watch.
    *
    * @return the variable name
    */
    public abstract String getVariableName ();

    /** Set the variable name to watch.
    *
    * @param name string name of the variable to watch
    */
    public abstract void setVariableName (String name);

    /** Get a textual representation of the value.
    * The watch should convert
    * the real value to a string representation. So if the watch represents
    * a <code>null</code> reference, the returned string will be for example <code>"null"</code>.
    *
    * @return the value of this watch, or <code>null</code> if the watch is not in scope
    */
    public abstract String getAsText ();

    /** Set the value of the watched variable (as text).
    *
    * @param value text representation of the new value
    * @exception DebuggerException if the value cannot be changed, or the
    *    string does not represent valid value, or the value type cannot reasonably be set as text
    */
    public abstract void setAsText (String value) throws DebuggerException;

    /** Get the string representation of the type of the variable.
    *
    * @return type string (i.e. the class name, or for a primitive e.g. <code>"int"</code>)
    */
    public abstract String getType ();

    /** Test whether the watch is hidden.
    * If so, it
    * is not presented in the list of all watches. Such a watch can be used
    * for the IDE's (or some module's) private use, not displaying anything to the user.
    * @return <code>true</code> if the watch is hidden
    * @see Debugger#createWatch(String, boolean)
    */
    public abstract boolean isHidden ();

    /**
    * Add a property change listener.
    * Change events should be fired for the properties {@link #PROP_VARIABLE_NAME}, {@link #PROP_AS_TEXT}, and {@link #PROP_TYPE}.
    *
    * @param l the listener to add
    */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /**
    * Remove a property change listener.
    *
    * @param l the listener to remove
    */
    public abstract void removePropertyChangeListener (PropertyChangeListener l);

}

/*
* Log
*  8    Gandalf   1.7         1/12/00  Ian Formanek    NOI18N
*  7    Gandalf   1.6         12/21/99 Daniel Prusa    Interfaces changed to 
*       abstract classes.
*  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  3    Gandalf   1.2         3/22/99  Jesse Glick     [JavaDoc]
*  2    Gandalf   1.1         2/26/99  Jaroslav Tulach Open API
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
