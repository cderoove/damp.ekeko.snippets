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

package org.netbeans.modules.innertesters;

import java.awt.Image;
import java.beans.*;

import org.openide.compiler.CompilerType;
import org.openide.explorer.propertysheet.editors.DirectoryOnlyEditor;
import org.openide.util.NbBundle;

/** Description of the compiler type.
 *
 * @author Jesse Glick
 */
public class InnerCompilerTypeBeanInfo extends SimpleBeanInfo {

    /** Get the super bean info.
     * @return the super bean info
     */
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

    /** Get the bean descriptor.
     * Adds a localized display name and description.
     * @return the descriptor
     */
    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor desc = new BeanDescriptor (InnerCompilerType.class);
        desc.setDisplayName (NbBundle.getBundle (InnerCompilerTypeBeanInfo.class).getString ("LBL_inner_tester_compiler"));
        desc.setShortDescription (NbBundle.getBundle (InnerCompilerTypeBeanInfo.class).getString ("HINT_inner_tester_compiler"));
        return desc;
    }

    /** Get the properties of the compiler type.
     * @return the properties
     */
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor testDir = new PropertyDescriptor ("testDir", InnerCompilerType.class);
            testDir.setDisplayName (NbBundle.getBundle (InnerCompilerTypeBeanInfo.class).getString ("PROP_testDir"));
            testDir.setShortDescription (NbBundle.getBundle (InnerCompilerTypeBeanInfo.class).getString ("HINT_testDir"));
            testDir.setPropertyEditorClass (DirectoryOnlyEditor.class);
            PropertyDescriptor mainCompiler = new PropertyDescriptor ("mainCompiler", InnerCompilerType.class);
            mainCompiler.setDisplayName (NbBundle.getBundle (InnerCompilerTypeBeanInfo.class).getString ("PROP_mainCompiler"));
            mainCompiler.setShortDescription (NbBundle.getBundle (InnerCompilerTypeBeanInfo.class).getString ("HINT_mainCompiler"));
            PropertyDescriptor innerName = new PropertyDescriptor ("innerName", InnerCompilerType.class);
            innerName.setDisplayName (NbBundle.getBundle (InnerCompilerTypeBeanInfo.class).getString ("PROP_innerName"));
            innerName.setShortDescription (NbBundle.getBundle (InnerCompilerTypeBeanInfo.class).getString ("HINT_innerName"));
            return new PropertyDescriptor[] { testDir, mainCompiler, innerName };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    /** Cached icon.
     */
    private static Image icon;
    /** Get an icon to represent the compiler type.
     * @param type the type of icon
     * @return the desired icon or <CODE>null</CODE>
     */
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("InnerCompilerIcon.gif");
            return icon;
        } else {
            return null;
        }
    }

}
