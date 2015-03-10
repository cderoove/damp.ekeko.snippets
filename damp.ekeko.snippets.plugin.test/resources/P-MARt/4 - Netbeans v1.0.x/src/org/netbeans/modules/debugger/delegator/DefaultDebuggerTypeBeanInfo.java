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

package org.netbeans.modules.debugger.delegator;

import java.beans.*;
import java.util.ResourceBundle;
import java.awt.Image;

import org.openide.util.NbBundle;
import org.openide.execution.Executor;
import org.openide.debugger.DebuggerType;
import org.openide.explorer.propertysheet.editors.FileOnlyEditor;

import org.netbeans.modules.debugger.support.ProcessDebuggerTypeBeanInfo;


/** Object that provides beaninfo for {@link DefaultDebuggerType}.
*
* @author Daniel Prusa
*/
public class DefaultDebuggerTypeBeanInfo extends SimpleBeanInfo {

    /** icon */
    private static Image icon;
    /** icon32 */
    private static Image icon32;

    private static BeanDescriptor descr;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    static {
        ResourceBundle bundle = NbBundle.getBundle (DefaultDebuggerTypeBeanInfo.class);
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor (DefaultDebuggerType.PROP_DEBUGGER_TYPE, DefaultDebuggerType.class,
                                               "getDebuggerType", "setDebuggerType"), // 0 // NOI18N
                   };
            desc[0].setDisplayName (bundle.getString ("PROP_DEBUGGER_TYPE"));
            desc[0].setShortDescription (bundle.getString ("HINT_DEBUGGER_TYPE"));
            desc[0].setPropertyEditorClass (DebuggerTypeEditor.class);
        } catch (IntrospectionException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ex.printStackTrace ();
        }

        descr = new BeanDescriptor (DefaultDebuggerType.class);
        descr.setName (bundle.getString ("CTL_DefaultDebuggerTypeName"));
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
        return desc;
    }

    /**
    * Claim there are no icons available.  You can override
    * this if you want to provide icons for your bean.
    */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null) {
                icon = loadImage("/org/netbeans/modules/debugger/resources/jpdaDebugging.gif"); // NOI18N
            }
            return icon;
        } else { // 32
            if (icon32 == null) {
                icon32 = loadImage("/org/netbeans/modules/debugger/resources/jpdaDebugging32.gif"); // NOI18N
            }
            return icon32;
        }
    }
}