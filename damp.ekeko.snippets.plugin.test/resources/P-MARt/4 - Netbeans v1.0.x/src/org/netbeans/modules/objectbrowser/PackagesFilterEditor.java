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

package org.netbeans.modules.objectbrowser;

import java.beans.*;

/** A property editor for PackagesFilter class.
*
* @author Jan Jancura
*/
public class PackagesFilterEditor extends PropertyEditorSupport {

    /**
    * @return The property value as a human editable string.
    * <p>   Returns null if the value can't be expressed as an editable string.
    * <p>   If a non-null value is returned, then the PropertyEditor should
    *       be prepared to parse that string back in setAsText().
    */
    public String getAsText () {
        return null;
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public java.awt.Component getCustomEditor () {
        PackageFilterPanel c = new PackageFilterPanel ();
        c.setPackagesFilter ((PackagesFilter) getValue ());
        return c;
    }
}

/*
 * Log
 *  3    Gandalf   1.2         12/15/99 Jan Jancura     Bug 3039 + Bug 4917
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         6/10/99  Jan Jancura     
 * $
 */
