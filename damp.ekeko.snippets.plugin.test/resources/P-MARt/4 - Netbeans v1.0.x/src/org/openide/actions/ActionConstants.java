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

package org.openide.actions;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** Defines constant BUNDLE that is a bundle for all actions in this package.
*
* @author Ales Novak
*/
class ActionConstants {
    /** A defined bundle for the actions */
    static final ResourceBundle BUNDLE = NbBundle.getBundle(
                                             "org.openide.actions.Bundle" // NOI18N
                                         );

    /** Applies message format on given param and resource.
    * @param resName resource to use
    * @param param argument to the message format
    * @return formated text
    */
    public static String getString (String resName, Object param) {
        return MessageFormat.format (BUNDLE.getString (resName), new Object[] { param });
    }
}


/*
 * Log
 *  4    Gandalf   1.3         1/12/00  Ian Formanek    NOI18N
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
