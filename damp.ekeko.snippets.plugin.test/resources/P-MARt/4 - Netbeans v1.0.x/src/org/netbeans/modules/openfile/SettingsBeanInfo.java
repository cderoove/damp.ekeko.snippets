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

package org.netbeans.modules.openfile;

import java.awt.Image;
import java.beans.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** Description of Open File settings.
* @author Jesse Glick
*/
public class SettingsBeanInfo extends SimpleBeanInfo {

    private static ResourceBundle bundle;
    private static ResourceBundle getBundle () {
        if (bundle == null) bundle = NbBundle.getBundle (SettingsBeanInfo.class);
        return bundle;
    }
    static String getString (String key) {
        return getBundle ().getString (key);
    }
    static String getString (String key, Object o1) {
        return MessageFormat.format (getString (key), new Object[] { o1 });
    }
    static String getString (String key, Object o1, Object o2) {
        return MessageFormat.format (getString (key), new Object[] { o1, o2 });
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor running = new PropertyDescriptor ("running", Settings.class); // NOI18N
            running.setDisplayName (getString ("LBL_running"));
            running.setShortDescription (getString ("HINT_running"));
            PropertyDescriptor port = new PropertyDescriptor ("port", Settings.class); // NOI18N
            port.setDisplayName (getString ("LBL_port"));
            port.setShortDescription (getString ("HINT_port"));
            PropertyDescriptor access = new PropertyDescriptor ("access", Settings.class); // NOI18N
            access.setDisplayName (getString ("LBL_access"));
            access.setShortDescription (getString ("HINT_access"));
            access.setPropertyEditorClass (AccessEd.class);
            PropertyDescriptor actualRunning = new PropertyDescriptor ("actualRunning", Settings.class, "isActualRunning", null); // NOI18N
            actualRunning.setDisplayName (getString ("LBL_actualRunning"));
            actualRunning.setShortDescription (getString ("HINT_actualRunning"));
            actualRunning.setHidden (true);
            PropertyDescriptor actualPort = new PropertyDescriptor ("actualPort", Settings.class, "getActualPort", null); // NOI18N
            actualPort.setDisplayName (getString ("LBL_actualPort"));
            actualPort.setShortDescription (getString ("HINT_actualPort"));
            actualPort.setHidden (true);
            return new PropertyDescriptor[] { running, port, access, actualRunning, actualPort };
        } catch (IntrospectionException e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                e.printStackTrace ();
            return null;
        }
    }

    private static Image icon, icon32;
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("/org/netbeans/modules/openfile/openFile.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/openfile/openFile32.gif"); // NOI18N
            return icon32;
        }
    }

    /** Simple property editor for two-item pulldown. */
    public static class AccessEd extends PropertyEditorSupport {

        private static final String[] tags = { getString ("LBL_localHost"), getString ("LBL_anyHost") };

        public String[] getTags () {
            return tags;
        }

        public String getAsText () {
            return tags[((Integer) getValue ()).intValue ()];
        }

        public void setAsText (String text) throws IllegalArgumentException {
            for (int i = 0; i < tags.length; i++) {
                if (tags[i].equals (text)) {
                    setValue (new Integer (i));
                    return;
                }
            }
            throw new IllegalArgumentException ();
        }

    }

}

/*
 * Log
 *  13   Gandalf   1.12        2/4/00   Jesse Glick     Hiding Actually Running 
 *       and Actual Port.
 *  12   Gandalf   1.11        1/12/00  Jesse Glick     I18N.
 *  11   Gandalf   1.10        1/4/00   Jesse Glick     Friendlier mount 
 *       dialogs.
 *  10   Gandalf   1.9         11/2/99  Jesse Glick     Overhauled socket 
 *       handling.
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         9/10/99  Jesse Glick     #3647 - 
 *       SocketException's on Linux + native threads.
 *  7    Gandalf   1.6         7/16/99  Jesse Glick     Open File using 
 *       "pending" icon to make sure one is created.
 *  6    Gandalf   1.5         7/10/99  Jesse Glick     Open File module moved 
 *       to core.
 *  5    Gandalf   1.4         7/10/99  Jesse Glick     Tweaks.
 *  4    Gandalf   1.3         7/10/99  Jesse Glick     Sundry clean-ups (mostly
 *       bundle usage).
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/22/99  Jesse Glick     Licenses.
 *  1    Gandalf   1.0         5/22/99  Jesse Glick     
 * $
 */
