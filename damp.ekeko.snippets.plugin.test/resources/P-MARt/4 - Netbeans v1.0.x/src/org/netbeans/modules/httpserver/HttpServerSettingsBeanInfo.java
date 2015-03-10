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

package org.netbeans.modules.httpserver;

import java.awt.Image;
import java.beans.*;

import org.openide.util.NbBundle;

/** BeanInfo for http - custom editor for hosts property
*
* @author Ales Novak, Petr Jiricka
*/
public class HttpServerSettingsBeanInfo extends SimpleBeanInfo {

    /** Icons for compiler settings objects. */
    static Image icon;
    static Image icon32;

    static final java.util.ResourceBundle bundle =
        NbBundle.getBundle(HttpServerSettingsBeanInfo.class);

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor("host", HttpServerSettings.class), // 0 // NOI18N
                       new PropertyDescriptor("grantedAddresses", HttpServerSettings.class), // 1 // NOI18N
                       new PropertyDescriptor("port", HttpServerSettings.class), // 2 // NOI18N
                       new PropertyDescriptor("repositoryBaseURL", HttpServerSettings.class), // 3 // NOI18N
                       new PropertyDescriptor("classpathBaseURL", HttpServerSettings.class), // 4 // NOI18N
                       new PropertyDescriptor("running", HttpServerSettings.class), // 5 // NOI18N
                   };
            desc[0].setDisplayName(bundle.getString("PROP_Host"));
            desc[0].setShortDescription(bundle.getString("HINT_Host"));
            desc[0].setPropertyEditorClass (HostPropertyEditor.class);
            desc[1].setDisplayName(bundle.getString("PROP_Granted"));
            desc[1].setShortDescription(bundle.getString("HINT_Granted"));
            desc[2].setDisplayName(bundle.getString("PROP_Port"));
            desc[2].setShortDescription(bundle.getString("HINT_Port"));
            desc[3].setDisplayName(bundle.getString("PROP_RepositoryBase"));
            desc[3].setShortDescription(bundle.getString("HINT_RepositoryBase"));
            desc[3].setExpert(true);
            desc[4].setDisplayName(bundle.getString("PROP_ClasspathBase"));
            desc[4].setShortDescription(bundle.getString("HINT_ClasspathBase"));
            desc[4].setExpert(true);
            desc[5].setDisplayName(bundle.getString("PROP_Running"));
            desc[5].setShortDescription(bundle.getString("HINT_Running"));
        } catch (IntrospectionException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace ();
        }
    }

    /**
    * loads icons
    */
    public HttpServerSettingsBeanInfo () {
    }

    /** @return the ExternalCompilerSettings' icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/httpserver/httpServerSettings.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/httpserver/httpServerSettings32.gif"); // NOI18N
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
 *  9    Gandalf   1.8         1/13/00  Petr Jiricka    More i18n
 *  8    Gandalf   1.7         1/12/00  Petr Jiricka    i18n
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         6/23/99  Petr Jiricka    
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/17/99  Petr Jiricka    
 *  3    Gandalf   1.2         5/15/99  Ian Formanek    Fixed package name for 
 *       icons
 *  2    Gandalf   1.1         5/10/99  Petr Jiricka    
 *  1    Gandalf   1.0         5/7/99   Petr Jiricka    
 * $
 */
