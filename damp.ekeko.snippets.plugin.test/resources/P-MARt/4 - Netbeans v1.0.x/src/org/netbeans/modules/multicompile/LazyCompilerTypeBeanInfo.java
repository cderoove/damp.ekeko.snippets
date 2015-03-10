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

package org.netbeans.modules.multicompile;

import java.awt.Image;
import java.beans.*;

import org.openide.compiler.ExternalCompilerType;
import org.openide.util.NbBundle;

/** Description of the compiler type.
 *
 * @author jglick
 */
public class LazyCompilerTypeBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor bd = new BeanDescriptor (LazyCompilerType.class);
        bd.setDisplayName (NbBundle.getBundle (LazyCompilerTypeBeanInfo.class).getString ("LBL_lazy_compiler_type_display_name"));
        return bd;
    }

    // Inherit properties and so on from ExternalCompilerType.
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] {
                       Introspector.getBeanInfo (ExternalCompilerType.class)
                   };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor inExt = new PropertyDescriptor ("inExt", LazyCompilerType.class);
            inExt.setDisplayName (NbBundle.getBundle (LazyCompilerTypeBeanInfo.class).getString ("PROP_inExt"));
            inExt.setShortDescription (NbBundle.getBundle (LazyCompilerTypeBeanInfo.class).getString ("HINT_inExt"));
            PropertyDescriptor outExt = new PropertyDescriptor ("outExt", LazyCompilerType.class);
            outExt.setDisplayName (NbBundle.getBundle (LazyCompilerTypeBeanInfo.class).getString ("PROP_outExt"));
            outExt.setShortDescription (NbBundle.getBundle (LazyCompilerTypeBeanInfo.class).getString ("HINT_outExt"));
            return new PropertyDescriptor[] { inExt, outExt };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    private static Image icon, icon32;
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("LazyCompilerIcon.gif");
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("LazyCompilerIcon32.gif");
            return icon32;
        }
    }

}
