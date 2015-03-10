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

import javax.swing.JMenuBar;

/** The JMenuBarContainer is an interface that is implemented by DesignForms
* that can contain a JMenuBar and want to provide a design-time support for menus.
*
* @author  Ian Formanek
*/
public interface JMenuBarContainer {

    /** Can be used to set the JMenuBar of this form.
    * @param menuBar The JMenuBar component or null to set no menu.
    */
    public void setJMenuBar (JMenuBar menuBar);

    /** Can be used to obtain the JMenuBar of this form.
    * @return The JMenuBar component or null if no menu is set.
    */
    public JMenuBar getJMenuBar ();

}

/*
* Log
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         5/4/99   Ian Formanek    Package change
*  1    Gandalf   1.0         3/24/99  Ian Formanek    
* $
*/
