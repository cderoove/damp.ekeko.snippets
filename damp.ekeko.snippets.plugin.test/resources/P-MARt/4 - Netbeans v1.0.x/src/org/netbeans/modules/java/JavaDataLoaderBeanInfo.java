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

package org.netbeans.modules.java;

import java.beans.*;
import java.awt.Image;

import org.openide.loaders.MultiFileLoader;

/** BeanInfo for java source loader.
*
* @author Petr Hamernik, Dafe Simonek
*/
public final class JavaDataLoaderBeanInfo extends SimpleBeanInfo {
    /** Prefix of the icon location. */
    private static String ICON_PREFIX = "/org/netbeans/modules/java/resources/class"; // NOI18N

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (MultiFileLoader.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    /** Icons for compiler settings objects. */
    private static Image icon;
    private static Image icon32;

    /** @param type Desired type of the icon
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
*  8    src-jtulach1.7         1/13/00  Ian Formanek    NOI18N
*  7    src-jtulach1.6         1/13/00  Jesse Glick     BeanInfo fixes.
*  6    src-jtulach1.5         1/12/00  Petr Hamernik   i18n: perl script used ( 
*       //NOI18N comments added )
*  5    src-jtulach1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    src-jtulach1.3         3/22/99  Ian Formanek    Icons moved from 
*       modules/resources to this package
*  3    src-jtulach1.2         3/12/99  Petr Hamernik   
*  2    src-jtulach1.1         3/12/99  Petr Hamernik   
*  1    src-jtulach1.0         2/16/99  David Simonek   
* $
*/
