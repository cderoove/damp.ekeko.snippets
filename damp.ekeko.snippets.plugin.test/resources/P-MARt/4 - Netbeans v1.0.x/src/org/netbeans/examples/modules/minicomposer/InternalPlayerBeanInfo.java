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

package org.netbeans.examples.modules.minicomposer;
import java.awt.Image;
import java.beans.*;
import org.openide.execution.Executor;
import org.openide.util.NbBundle;
public class InternalPlayerBeanInfo extends SimpleBeanInfo {
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (Executor.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }
    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor desc = new BeanDescriptor (InternalPlayer.class);
        desc.setDisplayName (NbBundle.getBundle (InternalPlayerBeanInfo.class).getString ("LBL_InternalPlayer"));
        desc.setShortDescription (NbBundle.getBundle (InternalPlayerBeanInfo.class).getString ("HINT_InternalPlayer"));
        return desc;
    }
    private static Image icon;
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("InternalPlayerIcon.gif");
            return icon;
        } else {
            return null;
        }
    }

}
