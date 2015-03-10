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

package org.netbeans.modules.makefile;

import java.beans.*;
import java.awt.Image;

import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/** BeanInfo for Makefile loader.
*
* @author Libor Kramolis, Jesse Glick
*/
public final class MakefileDataLoaderBeanInfo extends SimpleBeanInfo {

    /** Get inherited bean infos.
     * @return the super info
     */
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (UniFileLoader.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    /** Get the loader properties.
     * @return one bean property
     */
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor specialNames = new PropertyDescriptor ("specialNames", MakefileDataLoader.class);
            specialNames.setDisplayName (NbBundle.getBundle (MakefileDataLoaderBeanInfo.class).getString ("PROP_specialNames"));
            specialNames.setShortDescription (NbBundle.getBundle (MakefileDataLoaderBeanInfo.class).getString ("HINT_specialNames"));
            return new PropertyDescriptor[] { specialNames };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    /** Icons for compiler settings objects. */
    private static Image icon;

    /** Get the bean icon for the loader.
     * @param type desired type of the icon
     * @return returns the Makefile loader's icon
     */
    public Image getIcon(final int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
                (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/makefile/makefileObject.gif");
            return icon;
        } else {
            return null;
        }
    }

}
