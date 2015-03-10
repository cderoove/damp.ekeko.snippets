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

package org.netbeans.modules.group;

import java.beans.*;
import java.awt.Image;

import org.openide.loaders.DataLoader;
import org.openide.util.NbBundle;

/**
 *
 * @author  mryzl
 */

public class GroupShadowLoaderBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor extensions = new PropertyDescriptor ("extensions", GroupShadowLoader.class); // NOI18N
            extensions.setDisplayName (NbBundle.getBundle (GroupShadowLoaderBeanInfo.class).getString ("PROP_Extensions"));
            extensions.setShortDescription (NbBundle.getBundle (GroupShadowLoaderBeanInfo.class).getString ("HINT_Extensions"));
            return new PropertyDescriptor[] { extensions };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    /** Icons for image data loader. */
    private static Image icon;
    private static Image icon32;

    public Image getIcon(final int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
                (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/group/resources/groupShadow.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/group/resources/groupShadow32.gif"); // NOI18N
            return icon32;
        }
    }
}

/*
* Log
*  4    Gandalf   1.3         1/16/00  Jesse Glick     
*  3    Gandalf   1.2         1/14/00  Ian Formanek    NOI18N
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         8/17/99  Martin Ryzl     
* $ 
*/ 
