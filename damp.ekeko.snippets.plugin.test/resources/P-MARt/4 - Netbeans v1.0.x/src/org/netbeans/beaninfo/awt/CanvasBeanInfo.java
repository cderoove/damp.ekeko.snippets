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

/** A BeanInfo for java.awt.Canvas.
*
* @author Ian Formanek
*/
public class CanvasBeanInfo extends ComponentBeanInfo {

    /** icon */
    private static Image icon;

    /**
    * Claim there are no icons available.  You can override
    * this if you want to provide icons for your bean.
    */
    public Image getIcon(int type) {
        if (icon == null) {
            icon = loadImage("/org/netbeans/beaninfo/awt/canvas.gif"); // NOI18N
        }
        return icon;
    }

}

/*
 * Log
 */
