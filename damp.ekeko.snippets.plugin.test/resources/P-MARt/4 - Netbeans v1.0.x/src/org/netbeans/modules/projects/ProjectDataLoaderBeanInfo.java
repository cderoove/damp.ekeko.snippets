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

package org.netbeans.modules.projects;

import java.beans.*;
import java.awt.Image;

import org.openide.loaders.DataLoader;

/** Form data loader bean info.
*
* @author Ian Formanek
*/
public class ProjectDataLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            // Hide the actions property from users:
            PropertyDescriptor actions = new PropertyDescriptor ("actions", ProjectDataLoader.class); // NOI18N
            actions.setHidden (true);
            return new PropertyDescriptor[] { actions };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    /** Icons for url data loader. */
    private static Image icon;
    private static Image icon32;

    /** @param type Desired type of the icon
    * @return returns the Form loader's icon
    */
    public Image getIcon(final int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
                (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/projects/resources/projectObject.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/projects/resources/projectObject32.gif"); // NOI18N
            return icon32;
        }
    }

}

/*
* Log
*  1    Gandalf   1.0         1/13/00  Jesse Glick     
* $
*/
