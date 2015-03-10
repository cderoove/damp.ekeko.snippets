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

package org.netbeans.core.execution;

import java.awt.Image;
import java.beans.*;

/** BeanInfo for ExecutionSettings.
*
* @author Ales Novak, Ian Formanek
*/
public final class ExternalExecutionSettingsBeanInfo extends SimpleBeanInfo {
    /** Icons for execution settings objects. */
    private static Image icon;
    private static Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    static {
        try {
            java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle (
                                                  ExternalExecutionSettingsBeanInfo.class);
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor ("externalExecutor", ExternalExecutionSettings.class, "getExternalExecutor", "setExternalExecutor"), // 0 // NOI18N
                   };
            desc[0].setDisplayName(bundle.getString ("PROP_External_Execution"));
            desc[0].setShortDescription(bundle.getString ("HINT_External_Execution"));
        } catch (IntrospectionException ex) {
            throw new InternalError(ProcessNode.getBundle().getString("EXC_PropInit"));
        }
    }

    /** Returns the ExecutionSettings' icon */
    public Image getIcon(int type) {
        if (icon == null) {
            icon = loadImage("/org/netbeans/core/resources/externalExecutionSettings.gif"); // NOI18N
            icon32 = loadImage ("/org/netbeans/core/resources/externalExecutionSettings32.gif"); // NOI18N
        }
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return icon;
        else
            return icon32;
    }

    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return desc;
    }
}


/*
 * Log
 *  8    Gandalf   1.7         1/13/00  Jaroslav Tulach I18N
 *  7    Gandalf   1.6         1/12/00  Ales Novak      i18n
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/7/99   Ian Formanek    Cleaned loading of icons
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         3/9/99   Jan Jancura     Bundles moved.
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */



