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

package org.netbeans.beaninfo.swing;

import java.awt.Image;

/** BeanInfo for JLayeredPane - defines only the icons for now..
*
* @author  Ian Formanek
*/
public class JLayeredPaneBeanInfo extends java.beans.SimpleBeanInfo {
    /** Icons */
    private static Image icon;
    private static Image icon32;

    /** Returns the ExternalCompilerSettings' icon */
    public Image getIcon (int type) {
        if (icon == null) {
            icon = loadImage("/org/netbeans/beaninfo/swing/JLayeredPane.gif"); // NOI18N
            icon32 = loadImage ("/org/netbeans/beaninfo/swing/JLayeredPane32.gif"); // NOI18N
        }
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return icon;
        else
            return icon32;
    }

}

/*
 * Log
 *  6    Gandalf   1.5         1/12/00  Ales Novak      i18n
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/7/99   Ian Formanek    Cleaned loading of icons
 *  3    Gandalf   1.2         4/8/99   Ian Formanek    
 *  2    Gandalf   1.1         3/22/99  Ian Formanek    Icons moved from 
 *       modules/resources to this package
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
