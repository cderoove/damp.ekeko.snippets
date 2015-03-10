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

package org.netbeans.modules.multicompile;

import java.awt.Image;
import java.beans.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** Description of {@link GenericDataLoader}.
 *
 * @author jglick
 */
public class GenericDataLoaderBeanInfo extends SimpleBeanInfo {

    private static ResourceBundle bundle = null;
    private static ResourceBundle getBundle () {
        if (bundle == null) bundle = NbBundle.getBundle (GenericDataLoaderBeanInfo.class);
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
    static String getString (String key, Object o1, Object o2, Object o3) {
        return MessageFormat.format (getString (key), new Object[] { o1, o2, o3 });
    }
    static String getString (String key, Object o1, Object o2, Object o3, Object o4) {
        return MessageFormat.format (getString (key), new Object[] { o1, o2, o3, o4 });
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (GenericDataLoader.class.getSuperclass ()) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor secondaryExtensions = new PropertyDescriptor ("secondaryExtensions", GenericDataLoader.class);
            secondaryExtensions.setDisplayName (getString ("PROP_secondaryExtensions"));
            secondaryExtensions.setShortDescription (getString ("HINT_secondaryExtensions"));
            PropertyDescriptor innerClasses = new PropertyDescriptor ("innerClasses", GenericDataLoader.class);
            innerClasses.setDisplayName (getString ("PROP_innerClasses"));
            innerClasses.setShortDescription (getString ("HINT_innerClasses"));
            PropertyDescriptor mimeType = new PropertyDescriptor ("mimeType", GenericDataLoader.class);
            mimeType.setDisplayName (getString ("PROP_mimeType"));
            mimeType.setShortDescription (getString ("HINT_mimeType"));
            // [PENDING] prop ed with pulldown list, possibility to set to <None> (null) for no editing support
            return new PropertyDescriptor[] { secondaryExtensions, innerClasses, mimeType };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    private static Image icon, icon32;
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("GenericDataIcon.gif");
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("GenericDataIcon32.gif");
            return icon32;
        }
    }

}