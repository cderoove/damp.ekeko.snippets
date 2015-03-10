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

package org.netbeans.modules.form;

import org.openide.cookies.InstanceCookie;

/** InstanceCookie support for RADCOmponent class.
*
* @author Ian Formanek
*/
public class RADComponentInstance implements InstanceCookie {
    private RADComponent component;

    RADComponentInstance (RADComponent component) {
        this.component = component;
    }

    /** The bean name for the instance.
    * @return the name
    */
    public String instanceName () {
        return component.getBeanClass ().getName ();
    }

    /** The representation type that may be created as instances.
    * Can be used to test whether the instance is of an appropriate
    * class without actually creating it.
    *
    * @return the representation class of the instance
    * @exception IOException if an I/O error occurred
    * @exception ClassNotFoundException if a class was not found
    */
    public Class instanceClass () throws java.io.IOException, ClassNotFoundException {
        return component.getBeanClass ();
    }

    /** Create an instance.
    * @return the instance of type {@link #instanceClass}
    * @exception IOException if an I/O error occured
    * @exception ClassNotFoundException if a class was not found
    */
    public Object instanceCreate () throws java.io.IOException, ClassNotFoundException {
        return component.getBeanInstance ();
    }

}

/*
 * Log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         7/28/99  Ian Formanek    
 * $
 */
