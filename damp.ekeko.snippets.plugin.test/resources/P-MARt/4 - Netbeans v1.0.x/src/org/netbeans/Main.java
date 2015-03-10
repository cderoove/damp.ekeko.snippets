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

package org.netbeans;

/** The Corona's main class. Starts up the IDE.
*
* @author   Ian Formanek
*/
public class Main extends Object {
    /** Starts the IDE.
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        org.netbeans.core.Main.main (args);
    }
}

/*
 * Log
 *  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         4/8/99   Ian Formanek    Initialization of 
 *       IceBrowser's security manager moved to IceBrowserModule
 *  5    Gandalf   1.4         3/27/99  Ian Formanek    
 *  4    Gandalf   1.3         3/27/99  Ian Formanek    Removed HotJava 
 *       initialization
 *  3    Gandalf   1.2         3/22/99  Jan Jancura     Do not install ice. 
 *       security manager
 *  2    Gandalf   1.1         1/8/99   Ales Novak      
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
