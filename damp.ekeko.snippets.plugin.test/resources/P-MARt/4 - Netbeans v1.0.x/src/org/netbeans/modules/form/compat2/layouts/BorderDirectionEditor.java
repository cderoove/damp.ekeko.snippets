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

package org.netbeans.modules.form.compat2.layouts;

import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** Editor for BorderLayout's direction
*
* @author Ian Formanek
*/
class BorderDirectionEditor extends PropertyEditorSupport {
    /** direction values */
    public static final String CENTER = "Center"; // NOI18N
    public static final String WEST = "West"; // NOI18N
    public static final String EAST = "East"; // NOI18N
    public static final String NORTH = "North"; // NOI18N
    public static final String SOUTH = "South"; // NOI18N

    /** bundle */
    private static final ResourceBundle bundle = NbBundle.getBundle (BorderDirectionEditor.class);

    /** Values for directions. */
    private static final String[] values = {
        CENTER,
        WEST,
        EAST,
        NORTH,
        SOUTH
    };

    /** Display Names for directions. */
    private static final String[] directions = {
        bundle.getString ("VALUE_borderdirection_center"),
        bundle.getString ("VALUE_borderdirection_west"),
        bundle.getString ("VALUE_borderdirection_east"),
        bundle.getString ("VALUE_borderdirection_north"),
        bundle.getString ("VALUE_borderdirection_south")
    };

    /** @return names of the possible directions */
    public String[] getTags () {
        return directions;
    }

    /** @return text for the current value */
    public String getAsText () {
        return (String)getValue ();
    }

    /** Setter.
    * @param str string equal to one value from directions array
    */
    public void setAsText (String str) {
        for (int i = 0; i < directions.length; i++) {
            if (str.equals (directions[i])) {
                setValue (values [i]);
                return ;
            }
        }
    }
}

/*
 * Log
 *  5    Gandalf   1.4         1/12/00  Ian Formanek    NOI18N
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/15/99  Ian Formanek    
 *  1    Gandalf   1.0         5/11/99  Ian Formanek    
 * $
 */
