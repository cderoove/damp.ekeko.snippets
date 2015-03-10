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

import java.beans.*;

import org.openide.src.*;
import org.openide.explorer.propertysheet.editors.*;

/**
* Bean info containing common information for all MemberElements (e.g. Fields, Methods,...)
*
* @author Petr Hamernik
*/
public class MemberElementBeanInfo extends SimpleBeanInfo implements ElementProperties {

    private static PropertyDescriptor[] properties = new PropertyDescriptor[6];

    static {
        try {
            properties = new PropertyDescriptor[] {
                             new PropertyDescriptor(PROP_NAME, MemberElement.class, "getName", "setName"), // NOI18N
                             new PropertyDescriptor(PROP_MODIFIERS, MemberElement.class, "getModifiers", "setModifiers") // NOI18N
                         };
            properties[1].setPropertyEditorClass(ModifierEditor.class);
        }
        catch( IntrospectionException e) {
        }
    }

    /**
     * Gets the beans <code>PropertyDescriptor</code>s.
     * 
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return properties;
    }

    /**
     * A bean may have a "default" property that is the property that will
     * mostly commonly be initially chosen for update by human's who are 
     * customizing the bean.
     * @return  Index of default property in the PropertyDescriptor array
     * 		returned by getPropertyDescriptors.
     * <P>	Returns -1 if there is no default property.
     */
    public int getDefaultPropertyIndex() {
        return 0;
    }
}

/*
* Log
*  2    Gandalf   1.1         1/13/00  Jaroslav Tulach I18N
*  1    Gandalf   1.0         11/25/99 Petr Hamernik   
* $
*/
