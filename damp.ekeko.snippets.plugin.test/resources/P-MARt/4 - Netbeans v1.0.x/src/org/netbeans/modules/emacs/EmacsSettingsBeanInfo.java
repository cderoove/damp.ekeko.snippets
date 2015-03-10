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

package org.netbeans.modules.emacs;

import java.awt.Image;
import java.beans.*;

public class EmacsSettingsBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor host = new PropertyDescriptor ("host", EmacsSettings.class);
            host.setDisplayName ("Host Name");
            host.setShortDescription ("Host where Emacs will be running.");
            PropertyDescriptor port = new PropertyDescriptor ("port", EmacsSettings.class);
            port.setDisplayName ("Port Number");
            port.setShortDescription ("Port number to connect to/from.");
            port.setExpert (true);
            PropertyDescriptor passive = new PropertyDescriptor ("passive", EmacsSettings.class);
            passive.setDisplayName ("Passive Mode");
            passive.setShortDescription ("THIS MUST BE LEFT ON FOR EMACS: if true, IDE waits for connections; if false, actively connects.");
            passive.setExpert (true);
            PropertyDescriptor password = new PropertyDescriptor ("password", EmacsSettings.class);
            password.setDisplayName ("Password");
            password.setShortDescription ("Password that Emacs must provide. Please change this to a new value!");
            PropertyDescriptor mimeTypes = new PropertyDescriptor ("mimeTypes", EmacsSettings.class);
            mimeTypes.setDisplayName ("MIME Types");
            mimeTypes.setShortDescription ("MIME types to use Emacs to edit.");
            mimeTypes.setExpert (true);
            PropertyDescriptor debug = new PropertyDescriptor ("debug", EmacsSettings.class);
            debug.setDisplayName ("Debug Mode");
            debug.setShortDescription ("Debugging mode for IDE side. For diagnostic purposes only.");
            debug.setExpert (true);
            return new PropertyDescriptor[] { host, port, passive, password, mimeTypes, debug };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    private static Image icon/*, icon32*/;
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("EmacsSettingsIcon.gif");
            return icon;
        } else {
            /*
            if (icon32 == null)
              icon32 = loadImage ("EmacsSettingsIcon32.gif");
            */
            return /*icon32*/null;
        }
    }

}
