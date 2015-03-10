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

package org.netbeans.modules.web.core;

import java.awt.Image;
import java.beans.*;

import org.openide.util.NbBundle;


/** BeanInfo for Servlet settings
*
* @author Petr Jiricka
*/
public class ServletSettingsBeanInfo extends SimpleBeanInfo {

    /** Icons for compiler settings objects. */
    static Image icon;
    static Image icon32;

    static final java.util.ResourceBundle bundle =
        NbBundle.getBundle(ServletSettingsBeanInfo.class);

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor("webBrowser", ServletSettings.class), // 0 // NOI18N
                       new PropertyDescriptor("externalBrowser", ServletSettings.class) // 1 // NOI18N
                   };
            desc[0].setDisplayName(bundle.getString("PROP_WebBrowser"));
            desc[0].setShortDescription(bundle.getString("HINT_WebBrowser"));
            desc[0].setPropertyEditorClass (BrowserPropertyEditor.class);
            desc[1].setDisplayName(bundle.getString("PROP_ExternalBrowser"));
            desc[1].setShortDescription(bundle.getString("HINT_ExternalBrowser"));
        } catch (IntrospectionException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace ();
        } catch (java.util.MissingResourceException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace ();
        }
    }

    /**
    * loads icons
    */
    public ServletSettingsBeanInfo () {
    }

    /** @return the ServletSettings icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/web/core/resources/servletSettings.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/web/core/resources/servletSettings32.gif"); // NOI18N
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
 *  6    Gandalf   1.5         1/13/00  Petr Jiricka    More i18n
 *  5    Gandalf   1.4         1/12/00  Petr Jiricka    i18n phase 1
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         10/12/99 Petr Jiricka    Removed debug messages
 *  2    Gandalf   1.1         10/10/99 Petr Jiricka    Fixed Javadoc
 *  1    Gandalf   1.0         8/3/99   Petr Jiricka    
 * $
 */
