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



/** Object that provides beaninfo for {@link ToolsDebuggerType}.
*
* @author Jan Jancura
*/
public class ToolsDebuggerTypeBeanInfo extends SimpleBeanInfo {

    /** icon */
    private static Image icon;
    /** icon32 */
    private static Image icon32;


    private static BeanDescriptor descr;
    private static PropertyDescriptor[] prop;
    static {
        descr = new BeanDescriptor (ToolsDebuggerType.class);
        ResourceBundle bundle = NbBundle.getBundle (ToolsDebuggerTypeBeanInfo.class);

        descr.setName (bundle.getString ("CTL_ToolsDebuggerTypeName"));
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


/*
 * Log
 *  8    Gandalf   1.7         1/13/00  Daniel Prusa    NOI18N
 *  7    Gandalf   1.6         11/29/99 Jan Jancura     
 *  6    Gandalf   1.5         11/8/99  Jan Jancura     Somma classes renamed
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/11/99 Jan Jancura     Icons changed
 *  3    Gandalf   1.2         10/11/99 Ales Novak      new icons
 *  2    Gandalf   1.1         10/8/99  Ales Novak      
 *  1    Gandalf   1.0         10/6/99  Jan Jancura     
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach added hidden property
 */
