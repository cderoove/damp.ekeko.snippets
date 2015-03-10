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

package org.netbeans.beaninfo.awt;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** A BeanInfo for java.awt.Label.
*
* @author Ales Novak
* @version 0.11, Aug 18, 1998
*/
public class LabelBeanInfo extends ComponentBeanInfo {

    /** icon */
    private static Image icon;
    /** icon32 */
    private static Image icon32;
    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;
    /** localized string const */
    private static String LEFT;
    /** localized string const */
    private static String CENTER;
    /** localized string const */
    private static String RIGHT;

    /** Bundle - i18n */
    static ResourceBundle bundle;

    static {
        LEFT = getString("LEFT");
        CENTER = getString("CENTER");
        RIGHT = getString("RIGHT");
    }


    /** no-arg */
    public LabelBeanInfo() {
        if (icon == null) {
            icon = loadImage("/org/netbeans/beaninfo/awt/label.gif"); // NOI18N
            icon32 = icon;
        }
    }

    /**
    * Claim there are no icons available.  You can override
    * this if you want to provide icons for your bean.
    */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return icon;
        else
            return icon32;
    }

    /** @return Propertydescriptors */
    public PropertyDescriptor[] getPropertyDescriptors() {
        if (desc == null) {
            synchronized (LabelBeanInfo.class) {
                if (desc == null) {
                    PropertyDescriptor[] inh = super.getPropertyDescriptors();
                    desc = new PropertyDescriptor[inh.length + 2];
                    System.arraycopy(inh, 0, desc, 0, inh.length);
                    try {
                        desc[inh.length] = new PropertyDescriptor("alignment", java.awt.Label.class); // NOI18N
                        desc[inh.length + 1] = new PropertyDescriptor("text", java.awt.Label.class); // NOI18N

                        desc[inh.length].setPropertyEditorClass(LabelBeanInfo.AlignmentPropertyEditor.class);
                    }  catch (IntrospectionException ex) {
                        desc = super.getPropertyDescriptors();
                    }
                }
            }
        }
        return (PropertyDescriptor[]) desc.clone();
    }

    public static class AlignmentPropertyEditor extends PropertyEditorSupport {
        static String[] tags = {CENTER, LEFT, RIGHT};

        /** @return tags */
        public String[] getTags() {
            return tags;
        }

        public void setAsText(String s) {
            Integer i;
            if (s.equals(tags[0])) i = new Integer(java.awt.Label.CENTER);
            else if (s.equals(tags[1])) i = new Integer(java.awt.Label.LEFT);
            else i = new Integer(java.awt.Label.RIGHT);
            setValue(i);
        }

        public String getAsText() {
            int i = ((Integer) getValue()).intValue();
            return i == java.awt.Label.CENTER ? CENTER : (i == java.awt.Label.LEFT ? LEFT : RIGHT);
        }

        public String getJavaInitializationString () {
            int i = ((Integer) getValue()).intValue();
            switch (i) {
            case java.awt.Label.RIGHT :  return "java.awt.Label.RIGHT"; // NOI18N
            case java.awt.Label.LEFT :   return "java.awt.Label.LEFT"; // NOI18N
            default:
            case java.awt.Label.CENTER : return "java.awt.Label.CENTER"; // NOI18N
            }
        }
    }

    /** i18n */
    static String getString(String x) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(LabelBeanInfo.class);
        }
        return bundle.getString(x);
    }
}

/*
 * Log
 */
