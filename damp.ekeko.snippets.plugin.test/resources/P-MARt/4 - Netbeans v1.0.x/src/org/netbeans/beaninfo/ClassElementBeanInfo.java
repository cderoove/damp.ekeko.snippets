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

package org.netbeans.beaninfo;

import java.awt.Image;
import java.beans.*;

import org.openide.src.*;
import org.openide.explorer.propertysheet.editors.*;

/**
* Bean info for classes.
*
* @author Petr Hamernik
*/
public class ClassElementBeanInfo extends MemberElementBeanInfo {
    /** icon */
    private static Image icon;
    /** icon32 */
    private static Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    /** no-arg */
    public ClassElementBeanInfo() {
        if (icon == null) {
            icon = loadImage("/org/openide/resources/src/class.gif"); // NOI18N
            icon32 = icon;
        }
    }

    /**
    * Claim there are no icons available.  You can override
    * this if you want to provide icons for your bean.
    */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return icon;
        else
            return icon32;
    }

    /** @return Propertydescriptors */
    public PropertyDescriptor[] getPropertyDescriptors() {
        if (desc == null) {
            synchronized (FieldElementBeanInfo.class) {
                if (desc == null) {
                    PropertyDescriptor[] inh = super.getPropertyDescriptors();
                    desc = new PropertyDescriptor[inh.length + 3];
                    System.arraycopy(inh, 0, desc, 0, inh.length);
                    try {
                        desc[inh.length] = new PropertyDescriptor(PROP_SUPERCLASS, ClassElement.class, "getSuperclass", "setSuperclass" ); // NOI18N
                        desc[inh.length + 1] = new PropertyDescriptor(PROP_CLASS_OR_INTERFACE, ClassElement.class, "isClassOrInterface", "setClassOrInterface" ); // NOI18N
                        desc[inh.length + 2] = new PropertyDescriptor(PROP_INTERFACES, ClassElement.class, "getInterfaces", "setInterfaces" ); // NOI18N
                        desc[inh.length + 2].setPropertyEditorClass(IdentifierArrayEditor.class);
                    } catch (IntrospectionException ex) {
                        desc = super.getPropertyDescriptors();
                    }
                }
            }
        }
        return (PropertyDescriptor[]) desc.clone();
    }
}

/*
* Log
*  2    Gandalf   1.1         1/13/00  Jaroslav Tulach I18N
*  1    Gandalf   1.0         11/25/99 Petr Hamernik   
* $
*/
