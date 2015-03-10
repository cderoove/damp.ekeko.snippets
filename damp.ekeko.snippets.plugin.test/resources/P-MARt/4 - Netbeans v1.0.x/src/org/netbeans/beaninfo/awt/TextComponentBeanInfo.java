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

/** A BeanInfo for java.awt.TextComponent.
*
* @author Ales Novak
* @version 0.10, August 04, 1998
*/
abstract class TextComponentBeanInfo extends ComponentBeanInfo {

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    /** no-arg */
    TextComponentBeanInfo() {
    }

    /** @return Propertydescriptors */
    public PropertyDescriptor[] getPropertyDescriptors() {
        if (desc == null) {
            synchronized (TextComponentBeanInfo.class) {
                if (desc == null) {
                    PropertyDescriptor[] inh = super.getPropertyDescriptors();
                    desc = new PropertyDescriptor[inh.length + 5];
                    System.arraycopy(inh, 0, desc, 0, inh.length);
                    try {
                        desc[inh.length] = new PropertyDescriptor("selectionStart", java.awt.TextComponent.class); // NOI18N
                        desc[inh.length + 1] = new PropertyDescriptor("text", java.awt.TextComponent.class); // NOI18N
                        desc[inh.length + 2] = new PropertyDescriptor("caretPosition", java.awt.TextComponent.class); // NOI18N
                        desc[inh.length + 3] = new PropertyDescriptor("selectionEnd", java.awt.TextComponent.class); // NOI18N
                        desc[inh.length + 4] = new PropertyDescriptor("editable", java.awt.TextComponent.class); // NOI18N
                    } catch (IntrospectionException ex) {
                        desc = super.getPropertyDescriptors();
                    }
                }
            }
        }
        return desc;
    }
}

/*
 * Log
 */
