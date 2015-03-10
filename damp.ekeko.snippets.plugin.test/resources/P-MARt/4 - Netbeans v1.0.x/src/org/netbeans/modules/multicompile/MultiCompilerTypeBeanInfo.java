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

import org.openide.compiler.CompilerType;
import org.openide.util.NbBundle;

/** Description of the compiler type.
 *
 * @author jglick
 */
public class MultiCompilerTypeBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor bd = new BeanDescriptor (MultiCompilerType.class);
        bd.setDisplayName (NbBundle.getBundle (MultiCompilerTypeBeanInfo.class).getString ("LBL_multi_compiler_type_display_name"));
        return bd;
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] {
                       Introspector.getBeanInfo (CompilerType.class)
                   };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor firstCompiler = new PropertyDescriptor ("firstCompiler", MultiCompilerType.class);
            firstCompiler.setDisplayName (NbBundle.getBundle (MultiCompilerTypeBeanInfo.class).getString ("PROP_firstCompiler"));
            firstCompiler.setShortDescription (NbBundle.getBundle (MultiCompilerTypeBeanInfo.class).getString ("HINT_firstCompiler"));
            PropertyDescriptor secondCompiler = new PropertyDescriptor ("secondCompiler", MultiCompilerType.class);
            secondCompiler.setDisplayName (NbBundle.getBundle (MultiCompilerTypeBeanInfo.class).getString ("PROP_secondCompiler"));
            secondCompiler.setShortDescription (NbBundle.getBundle (MultiCompilerTypeBeanInfo.class).getString ("HINT_secondCompiler"));
            return new PropertyDescriptor[] { firstCompiler, secondCompiler };
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
                icon = loadImage ("MultiCompilerIcon.gif");
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("MultiCompilerIcon32.gif");
            return icon32;
        }
    }

}
