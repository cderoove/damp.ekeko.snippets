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

package org.netbeans.beaninfo.awt;

import java.awt.Image;
import java.beans.*;

/** A BeanInfo for java.awt.MenuComponent.
*
* @author Ales Novak
* @version 0.10, August 04, 1998
*/
abstract class MenuComponentBeanInfo extends SimpleBeanInfo {
    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor("name", java.awt.MenuComponent.class), // 0 // NOI18N
                       new PropertyDescriptor("font", java.awt.MenuComponent.class) // NOI18N
                   };
        } catch (IntrospectionException ex) {
            throw new InternalError(LabelBeanInfo.getString("EXC_PropInit"));
        }
    }

    /** no-arg */
    MenuComponentBeanInfo() {
    }

    /** @return Propertydescriptors */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return desc;
    }
}

/*
 * Log
 */
