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

import sun.tools.debug.RemoteClass;
import sun.tools.debug.RemoteField;
import sun.tools.debug.RemoteObject;
import sun.tools.debug.RemoteStackVariable;
import sun.tools.debug.RemoteValue;

import org.openide.debugger.Watch;
import org.netbeans.modules.debugger.support.util.Validator;


/**
* Standart implementation of Watch interface.
* @see org.openide.debugger.Watch
*
* @author   Jan Jancura
* @version  0.18, Feb 23, 1998
*/
public abstract class AbstractWatch extends Watch implements AbstractVariable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 3439367157517814302L;

    /** Property name constant */
    public static final String PROP_IN_SCOPE = "inScope"; // NOI18N


    /**
    * Returns true if this variable is in scope.
    *
    * @return true if this variable is in scope.
    */
    public abstract boolean isInScope ();

    /**
    * Create AbstractVariable object for this Watch. Can return null, if this Watch currently not
    * represents valide variable.
    *
    * @return AbstractVariable object for this class.
    */
    public abstract AbstractVariable getVariable ();

    /**
    * Returns error message if watch cannot be resolved or null.
    *
    * @return AbstractVariable object for this class.
    */
    public abstract String getErrorMessage ();

    public abstract void refresh (AbstractThread t);

}

/*
 * Log
 *  4    Gandalf-post-FCS1.2.4.0     3/28/00  Daniel Prusa    
 *  3    Gandalf   1.2         1/13/00  Daniel Prusa    NOI18N
 *  2    Gandalf   1.1         12/21/99 Daniel Prusa    Interfaces Debugger, 
 *       Watch, Breakpoint changed to abstract classes.
 *  1    Gandalf   1.0         11/8/99  Jan Jancura     
 * $
 */
