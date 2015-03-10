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

import org.openide.text.Line;
import org.openide.src.ConstructorElement;

/**
* Represents one breakpoint.
* Breakpoints are created and returned
* by the {@link Debugger} implementation. A breakpoint may be assigned either to a source line
* of a class, or to a method (or constructor) of a class.
* The current set of breakpoints is stored
* in the implementation of {@link Debugger}.
* <P>
* The abstract class contains only the necessary manipulation methods
* used by the IDE, like enabling/disabling the breakpoint and obtaining its
* position. A good implementation can offer the user much better ways to
* deal with the breakpoint (conditional breakpoints, etc.). Such information should
* be presented as properties--so it is assumed that the
* breakpoint itself is a serializable JavaBean.
*
*
* @author   Jan Jancura, Jaroslav Tulach
*/
public abstract class Breakpoint implements java.io.Serializable {
    /** Property name for validity of the breakpoint. */
    public static final String PROP_VALID = "valid"; // NOI18N
    /** Property name for enabled status of the breakpoint. */
    public static final String PROP_ENABLED = "enabled"; // NOI18N

    static final long serialVersionUID =4899621215676971003L;
    /** Destroy this breakpoint.
    * Removes it from the list of all breakpoints in the system.
    */
    public abstract void remove ();

    /** Get the line assigned to the breakpoint.
    *
    * @return the source line or <code>null</code> if no line is specified (it is assigned to a method)
    */
    public abstract Line getLine ();

    /** Get the name of the class the breakpoint is in.
    *
    * @return the class name
    */
    public abstract String getClassName ();

    /** Get the method the breakpoint is in.
    *
    * @return the method (or constructor) element or <code>null</code> if it assigned to a line
    */
    public abstract ConstructorElement getMethod ();

    /**
    * Test whether the breakpoint is enabled.
    *
    * @return <code>true</code> if so
    */
    public abstract boolean isEnabled ();

    /**
    * Set whether the breakpoint is enabled.
    *
    * @param state <code>true</code> to enable, <code>false</code> to disable
    */
    public abstract void setEnabled (boolean enabled);

    /**
    * Test whether the breakpoint is valid.
    * Invalidity might be caused by an inability to find the class it is supposed to be in, for example.
    * @return <code>true</code> if it is valid
    */
    public abstract boolean isValid ();

    /** Test whether the breakpoint is hidden.
    * If so, it
    * is not presented in the list of all breakpoints. Such a breakpoint can be used
    * for the IDE's (or some module's) private use, not displaying anything to the user.
    * @return <code>true</code> if the breakpoint is hidden
    */
    public abstract boolean isHidden ();

    /**
    * Add a property change listener.
    * @param listener the listener to add
    */
    public abstract void addPropertyChangeListener (PropertyChangeListener listener);

    /**
    * Remove a property change listener.
    * @param listener the listener to remove
    */
    public abstract void removePropertyChangeListener (PropertyChangeListener listener);
}

/*
 * Log
 *  13   Gandalf   1.12        1/12/00  Ian Formanek    NOI18N
 *  12   Gandalf   1.11        12/21/99 Daniel Prusa    Interfaces changed to 
 *       abstract classes.
 *  11   Gandalf   1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  9    Gandalf   1.8         7/13/99  Jesse Glick     Breakpoints should 
 *       handle constructors as well as regular methods.
 *  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         3/22/99  Jesse Glick     [JavaDoc]
 *  6    Gandalf   1.5         3/11/99  Jan Jancura     
 *  5    Gandalf   1.4         3/4/99   Jan Jancura     
 *  4    Gandalf   1.3         2/26/99  Jaroslav Tulach Open API
 *  3    Gandalf   1.2         2/1/99   Jaroslav Tulach 
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach 
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
