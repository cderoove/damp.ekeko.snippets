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

package org.netbeans.core.output;

import java.awt.Image;
import java.beans.*;

/** BeanInfo for OutputSettings.
*
* @author Ian Formanek
*/
public class OutputSettingsBeanInfo extends SimpleBeanInfo {
    /** Icons for output settings objects. */
    private static Image icon;
    private static Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor (OutputSettings.PROP_FONT_SIZE, OutputSettings.class, "getFontSize", "setFontSize"), // 0 // NOI18N
                       new PropertyDescriptor (OutputSettings.PROP_TAB_SIZE, OutputSettings.class, "getTabSize", "setTabSize"), // 1 // NOI18N
                       new PropertyDescriptor (OutputSettings.PROP_FOREGROUND, OutputSettings.class, "getBaseForeground", "setBaseForeground"), // 2 // NOI18N
                       new PropertyDescriptor (OutputSettings.PROP_CURSOR_FOREGROUND, OutputSettings.class, "getCursorForeground", "setCursorForeground"), // 3 // NOI18N
                       new PropertyDescriptor (OutputSettings.PROP_JUMP_CURSOR_FOREGROUND, OutputSettings.class, "getJumpCursorForeground", "setJumpCursorForeground"), // 4 // NOI18N
                       new PropertyDescriptor (OutputSettings.PROP_BACKGROUND, OutputSettings.class, "getBaseBackground", "setBaseBackground"), // 5 // NOI18N
                       new PropertyDescriptor (OutputSettings.PROP_CURSOR_BACKGROUND, OutputSettings.class, "getCursorBackground", "setCursorBackground"), // 6 // NOI18N
                       new PropertyDescriptor (OutputSettings.PROP_JUMP_CURSOR_BACKGROUND, OutputSettings.class, "getJumpCursorBackground", "setJumpCursorBackground"), // 7 // NOI18N
                   };
            desc[0].setDisplayName (OutputSettings.getString ("PROP_FONT_SIZE"));
            desc[0].setShortDescription (OutputSettings.getString ("HINT_FONT_SIZE"));
            desc[1].setDisplayName (OutputSettings.getString ("PROP_TAB_SIZE"));
            desc[1].setShortDescription (OutputSettings.getString ("HINT_TAB_SIZE"));
            desc[2].setDisplayName (OutputSettings.getString ("PROP_FOREGROUND"));
            desc[2].setShortDescription (OutputSettings.getString ("HINT_FOREGROUND"));
            desc[3].setDisplayName (OutputSettings.getString ("PROP_CURSOR_FOREGROUND"));
            desc[3].setShortDescription (OutputSettings.getString ("HINT_CURSOR_FOREGROUND"));
            desc[4].setDisplayName (OutputSettings.getString ("PROP_JUMP_CURSOR_FOREGROUND"));
            desc[4].setShortDescription (OutputSettings.getString ("HINT_JUMP_CURSOR_FOREGROUND"));
            desc[5].setDisplayName (OutputSettings.getString ("PROP_BACKGROUND"));
            desc[5].setShortDescription (OutputSettings.getString ("HINT_BACKGROUND"));
            desc[6].setDisplayName (OutputSettings.getString ("PROP_CURSOR_BACKGROUND"));
            desc[6].setShortDescription (OutputSettings.getString ("HINT_CURSOR_BACKGROUND"));
            desc[7].setDisplayName (OutputSettings.getString ("PROP_JUMP_CURSOR_BACKGROUND"));
            desc[7].setShortDescription (OutputSettings.getString ("HINT_JUMP_CURSOR_BACKGROUND"));
        } catch (IntrospectionException ex) {
            throw new InternalError(OutputSettings.getString("EXC_PropInit"));
        }
    }

    /** Returns the OutputSettings' icon */
    public Image getIcon(int type) {
        if (icon == null) {
            icon = loadImage("/org/netbeans/core/resources/outputSettings.gif"); // NOI18N
            icon32 = loadImage ("/org/netbeans/core/resources/outputSettings32.gif"); // NOI18N
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
 *  7    Gandalf   1.6         1/13/00  Jaroslav Tulach I18N
 *  6    Gandalf   1.5         1/12/00  Ales Novak      i18n
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/7/99   Ian Formanek    Cleaned loading of icons
 *  3    Gandalf   1.2         5/20/99  Ales Novak      exception parsing + copy
 *       action
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
