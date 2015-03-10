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

package org.netbeans.examples.modules.microed;

import org.openide.TopManager;
import org.openide.util.NbBundle;
import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;

/** Describes the system option.
* @author Jesse Glick
* @version Date
*/
public class SettingsBeanInfo extends SimpleBeanInfo {
    private static final ResourceBundle bundle = NbBundle.getBundle (SettingsBeanInfo.class);

    public SettingsBeanInfo () {}

    // Set up display information for all of the system option
    // properties.
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor kitClass = new PropertyDescriptor ("kitClass", Settings.class);
            kitClass.setBound (true);
            kitClass.setDisplayName (bundle.getString ("PROP_kitClass"));
            kitClass.setShortDescription (bundle.getString ("HINT_kitClass"));
            // Put it on a separate tab, since it is not likely to be
            // edited:
            kitClass.setExpert (true);
            PropertyDescriptor font = new PropertyDescriptor ("font", Settings.class);
            font.setBound (true);
            font.setDisplayName (bundle.getString ("PROP_font"));
            font.setShortDescription (bundle.getString ("HINT_font"));
            // Note that there is a default property editor for Font which
            // works fine.
            PropertyDescriptor mimeTypes = new PropertyDescriptor ("mimeTypes", Settings.class);
            mimeTypes.setBound (true);
            mimeTypes.setDisplayName (bundle.getString ("PROP_mimeTypes"));
            mimeTypes.setShortDescription (bundle.getString ("HINT_mimeTypes"));
            mimeTypes.setExpert (true);
            // There is also a usable String[] property editor.
            PropertyDescriptor saved = new PropertyDescriptor ("saved", Settings.class);
            // No public display of this! It is only in the settings so that
            // it will be saved along with the module. It should not be
            // exposed to the user.
            saved.setHidden (true);
            saved.setBound (false);
            PropertyDescriptor debug = new PropertyDescriptor ("debug", Settings.class);
            debug.setExpert (true);
            debug.setBound (false);
            debug.setDisplayName (bundle.getString ("PROP_debug"));
            debug.setShortDescription (bundle.getString ("HINT_debug"));
            return new PropertyDescriptor[] { kitClass, font, mimeTypes, saved, debug };
        } catch (IntrospectionException e) {
            TopManager.getDefault ().notifyException (e);
            return null;
        }
    }

    // Provide an icon.
    private static Image icon, icon32;
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("/org/netbeans/examples/modules/microed/microEd.gif");
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/examples/modules/microed/microEd32.gif");
            return icon32;
        }
    }

}
