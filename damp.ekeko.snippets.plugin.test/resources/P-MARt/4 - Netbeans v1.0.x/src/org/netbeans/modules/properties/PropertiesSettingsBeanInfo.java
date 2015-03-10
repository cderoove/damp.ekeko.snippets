/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties;

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
/**
*
* @author Petr Jiricka
*/
public class PropertiesSettingsBeanInfo extends SimpleBeanInfo {

    /** icon */
    private static Image icon;
    /** icon32 */
    private static Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor (PropertiesSettings.PROP_AUTO_PARSING_DELAY, PropertiesSettings.class)
                   };
            desc[0].setDisplayName (PropertiesSettings.getString("PROP_AUTO_PARSING_DELAY"));
            desc[0].setShortDescription (PropertiesSettings.getString("HINT_AUTO_PARSING_DELAY"));
        } catch (IntrospectionException ex) {
            throw new InternalError ();
        }
    }


    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return desc;
    }

    /**
    * Claim there are no icons available.  You can override
    * this if you want to provide icons for your bean.
    */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null) {
                icon   = Toolkit.getDefaultToolkit().getImage(getClass ().getResource("/org/netbeans/modules/properties/propertiesObject.gif"));
            }
            return icon;
        } else { // 32
            if (icon32 == null) {
                icon32 = Toolkit.getDefaultToolkit().getImage(getClass ().getResource("/org/netbeans/modules/properties/propertiesObject32.gif"));
            }
            return icon32;
        }
    }
}

/*
 * <<Log>>
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         5/12/99  Petr Jiricka    
 * $
 */
