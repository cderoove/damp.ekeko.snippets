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

package org.netbeans.modules.web.core.jsploader;

import java.beans.*;
import java.awt.Image;

import org.openide.loaders.UniFileLoader;

/** JSP loader bean info.
*
* @author Petr Jiricka
*/
public class JspLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (UniFileLoader.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    /** Icons for image data loader. */
    private static Image icon;
    private static Image icon32;

    /** @param type Desired type of the icon
    * @return returns the Image loader's icon
    */
    public Image getIcon(final int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
                (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/web/core/resources/jspObject.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/web/core/resources/jspObject32.gif"); // NOI18N
            return icon32;
        }
    }

}

/*
* Log
*  8    Gandalf   1.7         1/13/00  Petr Jiricka    More i18n
*  7    Gandalf   1.6         1/13/00  Jesse Glick     
*  6    Gandalf   1.5         1/13/00  Jesse Glick     Bean info fixes (but did 
*       not know how to correct display name of JspServletDataLoaderBeanInfo).
*  5    Gandalf   1.4         1/12/00  Petr Jiricka    i18n phase 1
*  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         10/12/99 Petr Jiricka    Removed debug messages
*  2    Gandalf   1.1         10/1/99  Petr Jiricka    Changed icon to 
*       resources/jspLoader
*  1    Gandalf   1.0         9/22/99  Petr Jiricka    
* $
*/
