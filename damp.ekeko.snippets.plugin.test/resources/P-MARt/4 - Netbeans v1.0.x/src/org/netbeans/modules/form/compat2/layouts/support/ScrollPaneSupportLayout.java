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

package org.netbeans.modules.form.compat2.layouts.support;

/** A design-time support layout.
*
* @author   Ian Formanek
*/
final public class ScrollPaneSupportLayout extends SingleDesignSupportLayout {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -1970766324386221399L;

    public ScrollPaneSupportLayout () {
        super (true);
    }

    // -----------------------------------------------------------------------------
    // constraints innerclass

    /** The ConstraintsDescription class encapsulates constraints data and
    * operations on a constraints that will be used for adding components
    * to the layout.
    */
    final public static class ScrollPaneConstraintsDescription extends SingleDesignSupportLayout.SingleSupportConstraintsDescription {
        /** A JDK 1.1 serial version UID */
        static final long serialVersionUID = 428503652545568382L;

    }

}

/*
 * Log
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         5/10/99  Ian Formanek    
 *  1    Gandalf   1.0         3/29/99  Ian Formanek    
 * $
 */
