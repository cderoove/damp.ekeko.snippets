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

package org.netbeans.modules.apisupport;

import java.awt.Image;
import java.beans.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class APISettingsBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor useNbBundle = new PropertyDescriptor ("useNbBundle", APISettings.class);
            useNbBundle.setDisplayName ("Use NbBundle");
            useNbBundle.setShortDescription ("Whether to use NbBundle for resource localizations, instead of a plain ResourceBundle.");
            return new PropertyDescriptor[] { useNbBundle };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    private static Image icon;
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("resources/ModuleDataIcon.gif");
            return icon;
        } else {
            return null;
        }
    }

}

/*
 * Log
 *  1    Gandalf   1.0         10/27/99 Jesse Glick     
 * $
 */
