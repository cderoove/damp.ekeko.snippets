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

package org.netbeans.beaninfo.editors;

import java.awt.Image;
import java.beans.*;
import java.util.*;

import org.openide.execution.*;

/** Object that provides beaninfo for no-op service types.
*
* @author Jesse Glick
*/
public abstract class NoServiceTypeBeanInfo extends SimpleBeanInfo {
    /**
     * @associates Image 
     */
    private static Map icons = new HashMap (); // Map<String,Image>

    public PropertyDescriptor[] getPropertyDescriptors () {
        return new PropertyDescriptor[0];
    }

    /** Path to icon for this bean info. */
    protected abstract String iconResource ();

    public Image getIcon (int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            String res = iconResource ();
            Image img = (Image) icons.get (res);
            if (img == null)
                icons.put (res, img = loadImage (res));
            return img;
        } else {
            return null;
        }
    }

}

/*
 * Log
 *  1    Gandalf   1.0         10/29/99 Jesse Glick     
 * $
 */
