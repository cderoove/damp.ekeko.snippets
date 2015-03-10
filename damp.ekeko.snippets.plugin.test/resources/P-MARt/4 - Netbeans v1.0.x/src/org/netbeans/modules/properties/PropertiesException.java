/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties;

/** General exception for the properties loader. */
public class PropertiesException extends Exception {
    static final long serialVersionUID =6450913392902826829L;
    /** Create an exception. */
    public PropertiesException() {
        this("");
    }
    /** Create an exception with a detail message.
    * @param msg the message
    */
    public PropertiesException(String msg) {
        super(msg);
    }
}


/*
 * <<Log>>
 *  3    Gandalf   1.2         11/27/99 Patrik Knakal   
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         5/12/99  Petr Jiricka    
 * $
 */
