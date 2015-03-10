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

package org.netbeans.examples.layoutmanager.twocolumn;

import java.awt.*;
import java.beans.*;

public class DesignTwoColumnLayoutBeanInfo extends SimpleBeanInfo
{
    private static Image _icon16;

    public Image getIcon(int iconKind) {
        if (iconKind == BeanInfo.ICON_COLOR_16x16) {
            if (_icon16 == null) {
                _icon16 = loadImage("TwoColumnLayout16.gif");
            }
            return _icon16;
        }
        else
            return null;
    }
}
