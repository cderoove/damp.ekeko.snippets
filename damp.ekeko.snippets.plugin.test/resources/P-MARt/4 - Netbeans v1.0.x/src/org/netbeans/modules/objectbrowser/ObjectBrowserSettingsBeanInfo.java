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

package org.netbeans.modules.objectbrowser;

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;


/** A BeanInfo for ObjectBrowserSettingsBeanInfo.
*
* @author Jan Jancura
* @version 0.11, May 16, 1998
*/
public class ObjectBrowserSettingsBeanInfo extends SimpleBeanInfo {

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    /** Icons */
    static Image icon;
    static Image icon32;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor ("packageFilter", ObjectBrowserSettings.class, // NOI18N
                                               "getPackageFilter", "setPackageFilter"), // 0 // NOI18N
                   };
            ResourceBundle bundle = NbBundle.getBundle (ObjectBrowserSettingsBeanInfo.class);
            desc[0].setDisplayName (bundle.getString ("PROP_PACKAGE_FILTER"));
            desc[0].setShortDescription (bundle.getString ("HINT_PACKAGE_FILTER"));
            desc[0].setPropertyEditorClass (PackagesFilterEditor.class);
        } catch (IntrospectionException ex) {
            //throw new InternalError ();
            ex.printStackTrace (); //[PENDINGbeta comment out]
        }
    }


    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return desc;
    }

    /** Returns the ObjectBrowserSettings' icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/objectbrowser/resources/browserSettings.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/objectbrowser/resources/browserSettings.gif"); // NOI18N
            return icon32;
        }
    }
}

/*
 * Log
 *  4    Gandalf   1.3         1/16/00  Ian Formanek    Tweaked comments
 *  3    Gandalf   1.2         1/13/00  Radko Najman    I18N
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         6/10/99  Jan Jancura     
 * $
 */
