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

package org.netbeans.modules.debugger.support;

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;


/** A BeanInfor for DebuggerSettings.
*
* @author Jan Jancura
* @version 0.11, May 16, 1998
*/
public class DebuggerSettingsBeanInfo extends SimpleBeanInfo {

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;
    private static BeanDescriptor descr;

    /** Icons */
    static Image icon;
    static Image icon32;

    // initialization of the array of descriptors
    static {
        descr = new BeanDescriptor (DebuggerSettings.class);
        ResourceBundle bundle = NbBundle.getBundle (DebuggerSettingsBeanInfo.class);
        descr.setDisplayName (bundle.getString ("CTL_Debugger_option"));
        descr.setShortDescription (bundle.getString ("HINT_Debugger_option"));

        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor (DebuggerSettings.PROP_DESKTOP, DebuggerSettings.class,
                                               "getDesktop", "setDesktop"), // 0 // NOI18N
                       new PropertyDescriptor ("debugger", DebuggerSettings.class, // NOI18N
                                               "getRemoteDebugger", "setRemoteDebugger"), // 1 // NOI18N
                       new PropertyDescriptor (DebuggerSettings.PROP_RUN_COMPILATION, DebuggerSettings.class,
                                               "getRunCompilation","setRunCompilation"), // 2 // NOI18N
                       new PropertyDescriptor (DebuggerSettings.PROP_ACTION_ON_TRACE_INTO, DebuggerSettings.class,
                                               "getActionOnTraceInto", "setActionOnTraceInto"), // 3 // NOI18N
                       new PropertyDescriptor (DebuggerSettings.PROP_ACTION_ON_TRACE_INTO_SET, DebuggerSettings.class,
                                               "isActionOnTraceIntoSet", "setActionOnTraceIntoSet"), // 4 // NOI18N
                   };
            desc[0].setDisplayName (bundle.getString ("PROP_WORKSPACE"));
            desc[0].setShortDescription (bundle.getString ("HINT_WORKSPACE"));
            desc[0].setPropertyEditorClass (WorkspaceEditor.class);
            desc[1].setHidden (true);
            desc[2].setDisplayName (bundle.getString ("PROP_RunCompilation"));
            desc[2].setShortDescription (bundle.getString ("HINT_RunCompilation"));
            desc[3].setDisplayName (bundle.getString ("PROP_ActionOnTraceInto"));
            desc[3].setShortDescription (bundle.getString ("HINT_ActionOnTraceInto"));
            desc[3].setPropertyEditorClass (ActionTIEditor.class);
            desc[4].setHidden (true);
        } catch (IntrospectionException ex) {
            //throw new InternalError ();
            ex.printStackTrace (); //[PENDINGbeta comment out]
        }
    }

    /**
    * Returns BeanDescriptor.
    */
    public BeanDescriptor getBeanDescriptor () {
        return descr;
    }

    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return desc;
    }

    /** Returns the DebuggerSettings' icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/core/resources/debuggerSettings.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/core/resources/debuggerSettings32.gif"); // NOI18N
            return icon32;
        }
    }
}

/*
 * Log
 *  13   Gandalf   1.12        1/18/00  Jan Jancura     Bad property getter 
 *       name.
 *  12   Gandalf   1.11        1/17/00  Jan Jancura     Some propertie removed 
 *       form DebugerSettings
 *  11   Gandalf   1.10        1/14/00  Daniel Prusa    NOI18N
 *  10   Gandalf   1.9         1/13/00  Daniel Prusa    NOI18N
 *  9    Gandalf   1.8         1/6/00   Daniel Prusa    Followed by Editor 
 *       property removed
 *  8    Gandalf   1.7         1/4/00   Daniel Prusa    Show Messages property 
 *       removed
 *  7    Gandalf   1.6         12/10/99 Daniel Prusa    
 *  6    Gandalf   1.5         12/7/99  Daniel Prusa    WorkspaceEditor.class in
 *       setPropertyClassEditor replaced by its full name
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/6/99  Jan Jancura     DefaultDT=> ToolsDT +  
 *       WorkspaceEditor moved
 *  3    Gandalf   1.2         8/9/99   Jan Jancura     Move process settings 
 *       from DebuggerSettings to ProcesDebuggerType
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Formanek    property display names changed
 */
