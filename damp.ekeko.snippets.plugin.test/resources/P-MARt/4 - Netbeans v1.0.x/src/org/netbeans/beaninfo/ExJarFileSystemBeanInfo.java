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

package org.netbeans.beaninfo;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.FileOnlyEditor;
import org.openide.filesystems.*;

/** Object that provides beaninfo for a {@link JarFileSystem}.
*
* @author Ian Formanek
*/
public class ExJarFileSystemBeanInfo extends SimpleBeanInfo {
    /** Icons for LocalFileSystem. */
    private static Image icon;
    private static Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;
    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[1];
            desc [0] = new PropertyDescriptor ("archiveFile", JarFileSystem.class, "getJarFile", "setJarFile"); // NOI18N
            desc [0].setPropertyEditorClass (JarFileEditor.class);
            ResourceBundle bundle = NbBundle.getBundle(JarFileSystemBeanInfo.class);
            desc[0].setDisplayName (bundle.getString("PROP_archiveFile"));
            desc[0].setShortDescription (bundle.getString("HINT_archiveFile"));
        } catch (IntrospectionException ex) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace();
        }
    }

    /* Provides the JarFileSystem's icon */
    public Image getIcon(int type) {
        if (icon == null) {
            icon = loadImage("/org/openide/resources/jarFS.gif"); // NOI18N
            icon32 = loadImage("/org/openide/resources/jarFS32.gif"); // NOI18N
        }

        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return icon;
        else
            return icon32;
    }

    /* Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return desc;
    }

    /* gets FileSystemBeanInfo
    * @return FileSystemBeanInfo
    */
    public final BeanInfo[] getAdditionalBeanInfo() {
        return new BeanInfo[] {new FileSystemBeanInfo()};
    }
}

/*
 * Log
 *  2    Gandalf   1.1         1/13/00  Jaroslav Tulach I18N
 *  1    Gandalf   1.0         11/25/99 Jaroslav Tulach 
 * $
 */
