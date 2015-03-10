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

package org.netbeans.modules.autoupdate;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.FileOnlyEditor;
import org.openide.explorer.propertysheet.editors.StringArrayCustomEditor;

/** BeanInfo for Autoupdate settings
*
* @author Petr Hrebejk
*/
public class SettingsBeanInfo extends SimpleBeanInfo {
    /** Icons for compiler settings objects. */
    static Image icon;
    static Image icon32;

    static final ResourceBundle bundle = NbBundle.getBundle(SettingsBeanInfo.class);

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor("period", Settings.class),              // 0 // NOI18N
                       new PropertyDescriptor("askBefore", Settings.class),           // 1 // NOI18N
                       new PropertyDescriptor("negativeResults", Settings.class),     // 2 // NOI18N
                       new PropertyDescriptor("lastCheck", Settings.class ),          // 3 // NOI18N
                       new PropertyDescriptor("lastStamp", Settings.class ),          // 4 // NOI18N
                       new PropertyDescriptor("registrationNumber", Settings.class )  // 5 // NOI18N
                   };

            desc[0].setDisplayName(bundle.getString("PROP_Period"));
            desc[0].setShortDescription(bundle.getString("HINT_Period"));
            desc[0].setPropertyEditorClass(Settings.PeriodPropertyEditor.class);

            desc[1].setDisplayName(bundle.getString("PROP_AskBefore"));
            desc[1].setShortDescription(bundle.getString("HINT_AskBefore"));

            desc[2].setDisplayName(bundle.getString("PROP_NegativeResuts"));
            desc[2].setShortDescription(bundle.getString("HINT_NegativeResults"));

            desc[3].setDisplayName(bundle.getString("PROP_LastCheck"));
            desc[3].setShortDescription(bundle.getString("HINT_LastCheck"));
            desc[3].setPropertyEditorClass(Settings.LastCheckPropertyEditor.class);
            desc[3].setWriteMethod( null );

            desc[4].setDisplayName(bundle.getString("PROP_LastStamp"));
            desc[4].setShortDescription(bundle.getString("HINT_LastStamp"));
            desc[4].setPropertyEditorClass(Settings.LastCheckPropertyEditor.class);
            desc[4].setHidden( true );

            desc[5].setDisplayName(bundle.getString("PROP_RegNum"));
            desc[5].setShortDescription(bundle.getString("HINT_RegNum"));

        } catch (IntrospectionException ex) {
            //throw new InternalError ();
            ex.printStackTrace ();
        }
    }

    /**
    * loads icons
    */
    public SettingsBeanInfo() {
    }

    /** Returns the ExternalCompilerSettings' icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/autoupdate/resources/updateAction.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/autoupdate/resources/updateAction32.gif"); // NOI18N
            return icon32;
        }
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
 *  6    Gandalf   1.5         1/13/00  Petr Hrebejk    i18 mk3
 *  5    Gandalf   1.4         1/12/00  Petr Hrebejk    i18n
 *  4    Gandalf   1.3         1/9/00   Petr Hrebejk    Proxy Config and 
 *       Registration number added
 *  3    Gandalf   1.2         12/20/99 Petr Hrebejk    Autocheck & security 
 *       finished
 *  2    Gandalf   1.1         12/16/99 Petr Hrebejk    Sign checking added
 *  1    Gandalf   1.0         12/1/99  Petr Hrebejk    
 * $
 */
