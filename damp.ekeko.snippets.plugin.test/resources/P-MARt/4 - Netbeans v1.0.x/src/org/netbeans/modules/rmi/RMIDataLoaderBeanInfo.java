/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi;

import java.beans.*;
import java.awt.Image;

import org.openide.util.NbBundle;

/** BeanInfo for RMIDataLoader.
*
* @author Martin Ryzl
*/
public final class RMIDataLoaderBeanInfo extends SimpleBeanInfo {
    /** Prefix of the icon location. */
    private static String ICON_PREFIX = "/org/netbeans/modules/rmi/resources/rmi"; // NOI18N

    /** Icons for compiler settings objects. */
    private static Image icon;
    private static Image icon32;

    /** Propertydescriptors */
    private static PropertyDescriptor[] descriptors;

    /** Default constructor.
    */
    public RMIDataLoaderBeanInfo() {
    }

    /**
    * @return Returns an array of PropertyDescriptors
    * describing the editable properties supported by this bean.
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        if (descriptors == null)
            initializeDescriptors();
        return descriptors;
    }

    /** @param type Desired type of the icon
    * @return returns the RMI loader's icon
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

    private static void initializeDescriptors () {
        try {
            descriptors =  new PropertyDescriptor[] {
                               createDescriptor("displayName", "getDisplayName", null, "PROP_Name", "HINT_Name"), // NOI18N
                           };
        }
        catch (IntrospectionException e) {
            e.printStackTrace ();
        }
    }

    /** Creates the descriptor.
    *
    * @param propertyName The programmatic name of the property.
    * @param getterName The name of the method used for reading the property value.
    * @param setterName The name of the method used for writing the property value.
    * @param displayNameKey The key to resource bundle for displayName
    * @param descriptionKey The key to resource bundle for the description
    */
    private static PropertyDescriptor createDescriptor(String propertyName,
            String getterName,
            String setterName,
            String displayNameKey,
            String descriptionKey) throws IntrospectionException {
        PropertyDescriptor desc = new PropertyDescriptor(propertyName, RMIDataLoader.class,
                                  getterName, setterName);
        desc.setDisplayName(getString(displayNameKey));
        desc.setShortDescription(getString(descriptionKey));
        return desc;
    }

    protected static String  getString(String name) {
        return NbBundle.getBundle(RMIDataLoader.class).getString(name);
    }
}

/*
* <<Log>>
*  4    Gandalf-post-FCS1.2.1.0     3/20/00  Martin Ryzl     localization
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         6/2/99   Martin Ryzl     
* $
*/


