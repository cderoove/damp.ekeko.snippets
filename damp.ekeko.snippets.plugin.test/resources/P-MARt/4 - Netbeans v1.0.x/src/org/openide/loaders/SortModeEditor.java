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

package org.openide.loaders;

import java.beans.*;
import java.util.ResourceBundle;
import java.util.Comparator;

import org.openide.util.NbBundle;

/** Editor for sorting mode
*
* @author Jaroslav Tulach
*/
class SortModeEditor extends PropertyEditorSupport {
    /** modes */
    private static final DataFolder.SortMode[] values = {
        DataFolder.SortMode.NONE,
        DataFolder.SortMode.NAMES,
        DataFolder.SortMode.CLASS,
        DataFolder.SortMode.FOLDER_NAMES
    };

    /** Names for modes. First is for displaying files */
    private static final String[] modes = {
        DataObject.getString ("VALUE_sort_none"),
        DataObject.getString ("VALUE_sort_names"),
        DataObject.getString ("VALUE_sort_class"),
        DataObject.getString ("VALUE_sort_folder_names")
    };

    /** @return names of the two possible modes */
    public String[] getTags () {
        return modes;
    }

    /** @return text for the current value (File or Element mode) */
    public String getAsText () {
        Object obj = getValue ();
        for (int i = 0; i < values.length; i++) {
            if (obj == values[i]) {
                return modes[i];
            }
        }
        return null;
    }

    /** Setter.
    * @param str string equal to one value from modes array
    */
    public void setAsText (String str) {
        for (int i = 0; i < modes.length; i++) {
            if (str.equals (modes[i])) {
                setValue (values[i]);
                return;
            }
        }
    }
}

/*
 * Log
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
