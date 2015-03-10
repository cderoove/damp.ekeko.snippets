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

public class ManifestDataLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (org.openide.loaders.UniFileLoader.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor prefixes = new PropertyDescriptor ("prefixes", ManifestDataLoader.class);
            prefixes.setDisplayName ("File Prefixes");
            prefixes.setShortDescription ("List of file name prefixes which will also be treated as potential manifests.");
            return new PropertyDescriptor[] { prefixes };
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
                icon = loadImage ("resources/ManifestDataIcon.gif");
            return icon;
        } else {
            return null;
        }
    }

}

/*
 * Log
 *  2    Gandalf   1.1         1/26/00  Jesse Glick     Configurable prefixes.
 *  1    Gandalf   1.0         1/22/00  Jesse Glick     
 * $
 */
