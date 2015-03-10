/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jarpackager.options;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** Description of {@link JarPackagerOption}.
 *
 * @author Jesse Glick
 */
public class JarPackagerOptionBeanInfo extends SimpleBeanInfo {

    private static ResourceBundle bundle = null;
    private static ResourceBundle getBundle () {
        if (bundle == null) bundle = NbBundle.getBundle (JarPackagerOptionBeanInfo.class);
        return bundle;
    }
    static String getString (String key) {
        return getBundle ().getString (key);
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor addToRepository = new PropertyDescriptor ("addToRepository", JarPackagerOption.class); // NOI18N
            addToRepository.setDisplayName (getString ("PROP_addToRepository"));
            addToRepository.setShortDescription (getString ("HINT_addToRepository"));
            PropertyDescriptor compressed = new PropertyDescriptor ("compressed", JarPackagerOption.class); // NOI18N
            compressed.setDisplayName (getString ("PROP_compressed"));
            compressed.setShortDescription (getString ("HINT_compressed"));
            PropertyDescriptor compressionLevel = new PropertyDescriptor ("compressionLevel", JarPackagerOption.class); // NOI18N
            compressionLevel.setDisplayName (getString ("PROP_compressionLevel"));
            compressionLevel.setShortDescription (getString ("HINT_compressionLevel"));
            PropertyDescriptor confirmAutoCreation = new PropertyDescriptor ("confirmAutoCreation", JarPackagerOption.class); // NOI18N
            confirmAutoCreation.setDisplayName (getString ("PROP_confirmAutoCreation"));
            confirmAutoCreation.setShortDescription (getString ("HINT_confirmAutoCreation"));
            PropertyDescriptor contentExt = new PropertyDescriptor ("contentExt", JarPackagerOption.class); // NOI18N
            contentExt.setDisplayName (getString ("PROP_contentExt"));
            contentExt.setShortDescription (getString ("HINT_contentExt"));
            PropertyDescriptor historyDepth = new PropertyDescriptor ("historyDepth", JarPackagerOption.class); // NOI18N
            historyDepth.setDisplayName (getString ("PROP_historyDepth"));
            historyDepth.setShortDescription (getString ("HINT_historyDepth"));
            PropertyDescriptor mainAttributes = new PropertyDescriptor ("mainAttributes", JarPackagerOption.class); // NOI18N
            mainAttributes.setDisplayName (getString ("PROP_mainAttributes"));
            mainAttributes.setShortDescription (getString ("HINT_mainAttributes"));
            PropertyDescriptor manifestFileList = new PropertyDescriptor ("manifestFileList", JarPackagerOption.class); // NOI18N
            manifestFileList.setDisplayName (getString ("PROP_manifestFileList"));
            manifestFileList.setShortDescription (getString ("HINT_manifestFileList"));
            return new PropertyDescriptor[] { addToRepository, compressed, compressionLevel, confirmAutoCreation, contentExt, historyDepth, mainAttributes, manifestFileList };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    private static Image icon, icon32;
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("/org/netbeans/modules/jarpackager/resources/jarObject.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/jarpackager/resources/jarObject32.gif"); // NOI18N
            return icon32;
        }
    }

}

/*
* <<Log>>
*  2    Gandalf   1.1         1/16/00  David Simonek   i18n
*  1    Gandalf   1.0         11/6/99  Jesse Glick     
* $ 
*/ 
