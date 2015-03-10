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
* @author Ian Formanek
*/
public class ExecutionSettingsBeanInfo extends SimpleBeanInfo {
    /** Icons for execution settings objects. */
    private static Image icon;
    private static Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(ExecutionSettingsBeanInfo.class);
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor (ExecutionSettings.PROP_REUSE, ExecutionSettings.class, "getReuse", "setReuse"), // 0 // NOI18N
                       new PropertyDescriptor (ExecutionSettings.PROP_CLEAR, ExecutionSettings.class,"getClear","setClear"), // 1 // NOI18N
                       new PropertyDescriptor (ExecutionSettings.PROP_WORKSPACE, ExecutionSettings.class,"getWorkspace","setWorkspace"), // 2 // NOI18N
                       new PropertyDescriptor (ExecutionSettings.PROP_RUN_COMPILATION, ExecutionSettings.class,"getRunCompilation","setRunCompilation") // 3 // NOI18N
                   };
            desc[0].setDisplayName(bundle.getString("PROP_Reuse"));
            desc[0].setShortDescription(bundle.getString("HINT_Reuse"));
            desc[1].setDisplayName(bundle.getString("PROP_Clear"));
            desc[1].setShortDescription(bundle.getString("HINT_Clear"));
            desc[2].setDisplayName(bundle.getString("PROP_Workspace"));
            desc[2].setShortDescription(bundle.getString("HINT_Workspace"));
            desc[2].setPropertyEditorClass (WorkspaceEditor.class);
            desc[3].setDisplayName(bundle.getString("PROP_RunCompilation"));
            desc[3].setShortDescription(bundle.getString("HINT_RunCompilation"));
        } catch (IntrospectionException ex) {
            throw new InternalError(ProcessNode.getBundle().getString("EXC_PropInit"));
        }
    }

    /** Returns the ExecutionSettings' icon */
    public Image getIcon(int type) {
        if (icon == null) {
            icon = loadImage("/org/netbeans/core/resources/executionSettings.gif"); // NOI18N
            icon32 = loadImage ("/org/netbeans/core/resources/executionSettings32.gif"); // NOI18N
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
 *  12   Gandalf   1.11        1/13/00  Jaroslav Tulach I18N
 *  11   Gandalf   1.10        1/12/00  Ales Novak      i18n
 *  10   Gandalf   1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         10/5/99  Ales Novak      Workspace property 
 *       editor moved into this package
 *  8    Gandalf   1.7         8/7/99   Ian Formanek    Cleaned loading of icons
 *  7    Gandalf   1.6         7/13/99  Ales Novak      workspace editor 
 *       uncommented
 *  6    Gandalf   1.5         7/11/99  Petr Hamernik   Usage of WorkspaceEditor
 *       temporary commented.
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         3/9/99   Jan Jancura     Bundles moved.
 *  3    Gandalf   1.2         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
