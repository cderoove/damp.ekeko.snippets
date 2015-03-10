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

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.DirectoryOnlyEditor;
import org.openide.filesystems.*;

import org.netbeans.core.ExLocalFileSystem;

/** Object that provides beaninfo for a {@link LocalFileSystem}.
*
* @author Ian Formanek
*/
public class ExLocalFileSystemBeanInfo extends SimpleBeanInfo {
    /** Icons for LocalFileSystem. */
    private static Image icon;
    private static Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[3];
            // Note: readOnly included here to make it writable, which it is not in FileSystem
            desc[0] = new PropertyDescriptor ("readOnly", LocalFileSystem.class, "isReadOnly", "setReadOnly"); // 0 // NOI18N
            desc[1] = new PropertyDescriptor ("rootDirectory", LocalFileSystem.class, "getRootDirectory", "setRootDirectory"); // 1 // NOI18N
            desc[2] = new PropertyDescriptor ("backupExtensions", ExLocalFileSystem.class, "getBackupExtensions", "setBackupExtensions"); // 2 // NOI18N

            desc[1].setPropertyEditorClass (RootEd.class);

            ResourceBundle bundle = NbBundle.getBundle(LocalFileSystemBeanInfo.class);
            desc[0].setDisplayName (bundle.getString("PROP_readOnly"));
            desc[0].setShortDescription (bundle.getString("HINT_readOnly"));
            desc[1].setDisplayName (bundle.getString("PROP_rootDirectory"));
            desc[1].setShortDescription (bundle.getString("HINT_rootDirectory"));
            desc[2].setDisplayName (bundle.getString("PROP_backupExtensions"));
            desc[2].setShortDescription (bundle.getString("HINT_backupExtensions"));

        } catch (IntrospectionException ex) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace();
        }
    }

    /* Provides the LocalFileSystem's icon */
    public Image getIcon(int type) {
        if (icon == null) {
            icon = loadImage("/org/openide/resources/localFS.gif"); // NOI18N
            icon32 = loadImage("/org/openide/resources/localFS32.gif"); // NOI18N
        }
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return icon;
        else
            return icon32;
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        BeanInfo[] beanInfos = new BeanInfo [1];
        beanInfos[0] = new FileSystemBeanInfo ();
        return beanInfos;
    }


    /* Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return desc;
    }

    public static class RootEd extends DirectoryOnlyEditor {

        protected HelpCtx getHelpCtx () {
            return new HelpCtx (RootEd.class);
        }

    }

}

/*
 * Log
 *  2    Gandalf   1.1         1/13/00  Jaroslav Tulach I18N
 *  1    Gandalf   1.0         11/25/99 Jaroslav Tulach 
 * $
 */
