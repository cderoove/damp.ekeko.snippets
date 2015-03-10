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
public class JarFileSystemBeanInfo extends SimpleBeanInfo {
    /** Icon for image data objects. */
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
 *  14   Gandalf   1.13        1/13/00  Jaroslav Tulach I18N
 *  13   Gandalf   1.12        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   Gandalf   1.11        9/20/99  Jaroslav Tulach 3165
 *  11   Gandalf   1.10        8/7/99   Ian Formanek    Cleaned loading of icons
 *  10   Gandalf   1.9         7/24/99  Ian Formanek    Printing stack trace on 
 *       netbeans.debug.exceptions property only
 *  9    Gandalf   1.8         6/30/99  Ian Formanek    Reflecting package 
 *       change of FIleEditor
 *  8    Gandalf   1.7         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         3/26/99  Jaroslav Tulach 
 *  5    Gandalf   1.4         3/12/99  Jaroslav Tulach 
 *  4    Gandalf   1.3         3/1/99   Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
