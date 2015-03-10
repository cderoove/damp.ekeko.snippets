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

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** Html data loader bean info.
*
* @author Petr Jiricka
*/
public class WebLoaderBeanInfo extends SimpleBeanInfo {

    /** Icons for image data loader. */
    private static Image icon;
    private static Image icon32;

    /** Propertydescriptors */
    private static PropertyDescriptor[] descriptors;

    /** Default constructor
    */
    public WebLoaderBeanInfo() {
    }

    /**
    * @return Returns an array of PropertyDescriptors
    * describing the editable properties supported by this bean.
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        if (descriptors == null) initializeDescriptors();
        return descriptors;
    }

    /** @param type Desired type of the icon
    * @return returns the Image loader's icon
    */
    public Image getIcon(final int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
                (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/web/core/resources/webLoader.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/web/core/resources/webLoader32.gif"); // NOI18N
            return icon32;
        }
    }

    private static void initializeDescriptors () {
        try {
            final ResourceBundle bundle =
                NbBundle.getBundle(WebLoaderBeanInfo.class);

            descriptors =  new PropertyDescriptor[] {
                               new PropertyDescriptor ("displayName", WebLoader.class, // NOI18N
                                                       "getDisplayName", null), // NOI18N
                               new PropertyDescriptor ("extensions", WebLoader.class, // NOI18N
                                                       "getExtensions", "setExtensions") // NOI18N
                           };
            descriptors[0].setDisplayName(bundle.getString("PROP_Name"));
            descriptors[0].setShortDescription(bundle.getString("HINT_Name"));
            descriptors[1].setDisplayName(bundle.getString("PROP_Extensions"));
            descriptors[1].setShortDescription(bundle.getString("HINT_Extensions"));
        } catch (IntrospectionException e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                e.printStackTrace ();
        }
    }

}

/*
* Log
*  4    Gandalf   1.3         1/12/00  Petr Jiricka    i18n phase 1
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         10/12/99 Petr Jiricka    Removed debug messages
*  1    Gandalf   1.0         6/30/99  Petr Jiricka    
* $
*/
