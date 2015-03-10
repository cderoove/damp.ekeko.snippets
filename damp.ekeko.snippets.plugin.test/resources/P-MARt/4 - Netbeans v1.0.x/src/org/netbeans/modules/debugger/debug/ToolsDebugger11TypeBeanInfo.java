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

package org.netbeans.modules.debugger.debug;

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.execution.Executor;
import org.openide.debugger.DebuggerType;
import org.openide.explorer.propertysheet.editors.FileOnlyEditor;

import org.netbeans.modules.debugger.support.ProcessDebuggerTypeBeanInfo;

/** Object that provides beaninfo for {@link ToolsDebugger11Type}.
*
* @author Jan Jancura
*/
public class ToolsDebugger11TypeBeanInfo extends SimpleBeanInfo {

    /** icon */
    private static Image icon;
    /** icon32 */
    private static Image icon32;


    private static BeanDescriptor descr;
    private static PropertyDescriptor[] prop;
    static {
        descr = new BeanDescriptor (ToolsDebugger11Type.class);
        ResourceBundle bundle = NbBundle.getBundle (ToolsDebugger11TypeBeanInfo.class);

        descr.setName (bundle.getString ("CTL_ToolsDebugger11TypeName"));

        try {
            prop = new PropertyDescriptor[] {
                       new PropertyDescriptor (ToolsDebugger11Type.PROP_JAVA_HOME, ToolsDebugger11Type.class,
                                               "getJavaHome", "setJavaHome"), // 0 // NOI18N
                   };
            prop[0].setDisplayName (bundle.getString ("PROP_JAVA_HOME_11"));
            prop[0].setShortDescription (bundle.getString ("HINT_JAVA_HOME_11"));
        } catch (IntrospectionException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ex.printStackTrace ();
        }
    }

    /* gets FileSystemBeanInfo
    * @return FileSystemBeanInfo
    */
    public final BeanInfo[] getAdditionalBeanInfo () {
        return new BeanInfo[] {new ProcessDebuggerTypeBeanInfo ()};
    }

    public BeanDescriptor getBeanDescriptor () {
        return descr;
    }

    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return prop;
    }

    /**
    * Claim there are no icons available.  You can override
    * this if you want to provide icons for your bean.
    */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null) {
                icon = loadImage("/org/netbeans/modules/debugger/resources/toolsDebugging.gif"); // NOI18N
            }
            return icon;
        } else { // 32
            if (icon32 == null) {
                icon32 = loadImage("/org/netbeans/modules/debugger/resources/toolsDebugging32.gif"); // NOI18N
            }
            return icon32;
        }
    }

}