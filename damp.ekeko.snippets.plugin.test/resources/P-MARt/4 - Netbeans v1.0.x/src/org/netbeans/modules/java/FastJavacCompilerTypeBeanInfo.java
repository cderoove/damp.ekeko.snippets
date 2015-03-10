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

package org.netbeans.modules.java;

import java.beans.BeanDescriptor;

/** BeanInfo for class FastJavacCompilerType. It describes three properties
* one with special editor.
*
*/
public class FastJavacCompilerTypeBeanInfo extends JavaExternalCompilerTypeBeanInfo {

    /** @return BeanDescriptor with localized name */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(FastJavacCompilerType.class);
        bd.setDisplayName(JavaCompilerType.getString("CTL_FastCompilerType"));
        return bd;
    }
}

/*
 * Log
 *  1    Gandalf-post-FCS1.0         3/24/00  Ales Novak      
 * $
 */

