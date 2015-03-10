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

/** A BeanInfo for java.awt.ScrollBar.
*
* @author Ales Novak
* @version 0.10, August 04, 1998
*/
public class ScrollbarBeanInfo extends ComponentBeanInfo {

    /** icon */
    private static Image icon;
    /** icon32 */
    private static Image icon32;
    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;
    /** localized string const */
    private static String HORIZONTAL;
    /** string const */
    private static String VERTICAL;

    static {
        ResourceBundle rb = NbBundle.getBundle(ScrollbarBeanInfo.class);
        HORIZONTAL = rb.getString("HORIZONTAL");
        VERTICAL = rb.getString("VERTICAL");
    }

    /** no-arg */
    public ScrollbarBeanInfo() {
        if (icon == null) {
            icon = loadImage("/org/netbeans/beaninfo/awt/scrollbar.gif"); // NOI18N
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
            synchronized (ScrollbarBeanInfo.class) {
                if (desc == null) {
                    PropertyDescriptor[] inh = super.getPropertyDescriptors();
                    desc = new PropertyDescriptor[inh.length + 7];
                    System.arraycopy(inh, 0, desc, 0, inh.length);
                    try {
                        desc[inh.length] = new PropertyDescriptor("unitIncrement", java.awt.Scrollbar.class); // NOI18N
                        desc[inh.length + 1] = new PropertyDescriptor("minimum", java.awt.Scrollbar.class); // NOI18N
                        desc[inh.length + 2] = new PropertyDescriptor("maximum", java.awt.Scrollbar.class); // NOI18N
                        desc[inh.length + 3] = new PropertyDescriptor("value", java.awt.Scrollbar.class); // NOI18N
                        desc[inh.length + 4] = new PropertyDescriptor("blockIncrement", java.awt.Scrollbar.class); // NOI18N
                        desc[inh.length + 5] = new PropertyDescriptor("orientation", java.awt.Scrollbar.class); // NOI18N
                        desc[inh.length + 6] = new PropertyDescriptor("visibleAmount", java.awt.Scrollbar.class); // NOI18N

                        desc[inh.length + 5].setPropertyEditorClass(ScrollbarBeanInfo.OrientationPropertyEditor.class);
                    } catch (IntrospectionException ex) {
                        desc = super.getPropertyDescriptors();
                    }
                }
            }
        }
        return (PropertyDescriptor[]) desc.clone();
    }

    /** orientation PropertyEditor */
    public static class OrientationPropertyEditor extends PropertyEditorSupport {

        static String[] tags = {HORIZONTAL, VERTICAL};

        /** @return tags */
        public String[] getTags() {
            return tags;
        }

        public void setAsText(String s) {
            Integer i;
            if (s.equals(tags[0])) i = new Integer(java.awt.Scrollbar.HORIZONTAL);
            else i = new Integer(java.awt.Scrollbar.VERTICAL);
            setValue(i);
        }

        public String getAsText() {
            int i = ((Integer) getValue()).intValue();
            return i == java.awt.Scrollbar.VERTICAL ? VERTICAL : HORIZONTAL;
        }

        public String getJavaInitializationString() {
            int i = ((Integer) getValue()).intValue();
            return i == java.awt.Scrollbar.VERTICAL ? "java.awt.Scrollbar.VERTICAL" : "java.awt.Scrollbar.HORIZONTAL"; // NOI18N
        }
    }
}

/*
 * Log
 */
