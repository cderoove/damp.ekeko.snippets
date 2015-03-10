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

import org.openide.execution.ProcessExecutor;
import org.openide.explorer.propertysheet.editors.DirectoryOnlyEditor;
import org.openide.util.NbBundle;

/** Description of the executor.
 *
 * @author Jesse Glick
 */
public class InnerExecutorBeanInfo extends SimpleBeanInfo {

    /** Get general bean info for external executors.
     * @return the super bean info
     */
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] {
                       Introspector.getBeanInfo (ProcessExecutor.class)
                   };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    /** Get a bean descriptor.
     * Adds localized name, etc.
     * @return the descriptor
     */
    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor desc = new BeanDescriptor (InnerExecutor.class);
        desc.setDisplayName (NbBundle.getBundle (InnerExecutorBeanInfo.class).getString ("LBL_inner_tester_executor"));
        desc.setShortDescription (NbBundle.getBundle (InnerExecutorBeanInfo.class).getString ("HINT_inner_tester_executor"));
        return desc;
    }

    /** Get properties for the executor.
     * These will be displayed to the user in addition to those
     * gotten from the super bean info.
     * @return a list of properties
     */
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor testDir = new PropertyDescriptor ("testDir", InnerExecutor.class);
            testDir.setDisplayName (NbBundle.getBundle (InnerExecutorBeanInfo.class).getString ("PROP_testDir"));
            testDir.setShortDescription (NbBundle.getBundle (InnerExecutorBeanInfo.class).getString ("HINT_testDir_4_exec"));
            testDir.setPropertyEditorClass (DirectoryOnlyEditor.class);
            PropertyDescriptor innerName = new PropertyDescriptor ("innerName", InnerExecutor.class);
            innerName.setDisplayName (NbBundle.getBundle (InnerExecutorBeanInfo.class).getString ("PROP_innerName"));
            innerName.setShortDescription (NbBundle.getBundle (InnerExecutorBeanInfo.class).getString ("HINT_innerName"));
            return new PropertyDescriptor[] { testDir, innerName };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    /** Icon for the executor.
     */
    private static Image icon;
    /** Get the executor's icon.
     * @param type the type of icon
     * @return an icon
     */
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("InnerExecutorIcon.gif");
            return icon;
        } else {
            return null;
        }
    }

}
