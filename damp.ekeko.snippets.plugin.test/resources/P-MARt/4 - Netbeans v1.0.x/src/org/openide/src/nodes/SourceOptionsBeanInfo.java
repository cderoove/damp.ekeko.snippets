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

package org.openide.src.nodes;

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** BeanInfo for source options.
*
* @author Petr Hamernik
*/
public final class SourceOptionsBeanInfo extends SimpleBeanInfo {
    /** Prefix of the icon location. */
    private static String ICON_PREFIX = "/org/openide/resources/src/sourceOptions"; // NOI18N

    /** Icons for compiler settings objects. */
    private static Image icon;
    private static Image icon32;

    /** Propertydescriptors */
    private static PropertyDescriptor[] descriptors;

    /** Default constructor.
    */
    public SourceOptionsBeanInfo() {
    }

    public BeanDescriptor getBeanDescriptor() {
        ResourceBundle bundle = NbBundle.getBundle(SourceOptionsBeanInfo.class);
        BeanDescriptor desc = new BeanDescriptor(SourceOptions.class);
        desc.setDisplayName(bundle.getString("MSG_sourceOptions"));
        /* for Post-FCS desc.setShortDescription(bundle.getString("HINT_sourceOptions")); */
        return desc;
    }

    /*
    * @return Returns an array of PropertyDescriptors
    * describing the editable properties supported by this bean.
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        if (descriptors == null) {
            ResourceBundle bundle = NbBundle.getBundle(SourceOptionsBeanInfo.class);
            try {
                descriptors = new PropertyDescriptor[6];
                for (int i = 0; i < 6; i++) {
                    descriptors[i] = new PropertyDescriptor(SourceOptions.PROP_NAMES[i], SourceOptions.class);
                    descriptors[i].setDisplayName(bundle.getString("PROP_"+SourceOptions.PROP_NAMES[i]));
                    descriptors[i].setShortDescription(bundle.getString("HINT_"+SourceOptions.PROP_NAMES[i]));
                }
                //        descriptors[6] = new PropertyDescriptor(SourceOptions.PROP_CATEGORIES_USAGE, SourceOptions.class);
                //        descriptors[6].setDisplayName(bundle.getString("PROP_"+SourceOptions.PROP_CATEGORIES_USAGE));
                //        descriptors[6].setShortDescription(bundle.getString("HINT_"+SourceOptions.PROP_CATEGORIES_USAGE));
            }
            catch (IntrospectionException e) {
                if (System.getProperty ("netbeans.debug.exceptions") != null) e.printStackTrace();
                descriptors = new PropertyDescriptor[0];
            }
        }
        return descriptors;
    }

    /* @param type Desired type of the icon
    * @return returns the Java loader's icon
    */
    public Image getIcon(final int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage(ICON_PREFIX + ".gif"); // NOI18N
            return icon;
        }
        else {
            if (icon32 == null)
                icon32 = loadImage(ICON_PREFIX + "32.gif"); // NOI18N
            return icon32;
        }
    }
}

/*
* Log
*  13   Gandalf-post-FCS1.10.1.1    3/16/00  Svatopluk Dedic 
*  12   Gandalf-post-FCS1.10.1.0    3/15/00  Svatopluk Dedic Fixed hints
*  11   src-jtulach1.10        1/12/00  Petr Hamernik   i18n using perl script 
*       (//NOI18N comments added)
*  10   src-jtulach1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  9    src-jtulach1.8         7/28/99  Petr Hamernik   hierarchy usage property 
*       removed
*  8    src-jtulach1.7         7/25/99  Ian Formanek    Exceptions printed to 
*       console only on "netbeans.debug.exceptions" flag
*  7    src-jtulach1.6         6/28/99  Petr Hamernik   new hierarchy under 
*       ClassChildren
*  6    src-jtulach1.5         6/9/99   Ian Formanek    Fixed resources for 
*       package change
*  5    src-jtulach1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    src-jtulach1.3         4/2/99   Jesse Glick     [JavaDoc]
*  3    src-jtulach1.2         3/15/99  Petr Hamernik   
*  2    src-jtulach1.1         3/12/99  Petr Hamernik   Icons moving
*  1    src-jtulach1.0         3/12/99  Petr Hamernik   
* $
*/
