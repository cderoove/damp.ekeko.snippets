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

/** Object that provides beaninfo for a {@link LocalFileSystem}.
*
* @author Ian Formanek
*/
public class LocalFileSystemBeanInfo extends SimpleBeanInfo {
    /** Icons for LocalFileSystem. */
    private static Image icon;
    private static Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[2];
            // Note: readOnly included here to make it writable, which it is not in FileSystem
            desc[0] = new PropertyDescriptor ("readOnly", LocalFileSystem.class, "isReadOnly", "setReadOnly"); // 0 // NOI18N
            desc[1] = new PropertyDescriptor ("rootDirectory", LocalFileSystem.class, "getRootDirectory", "setRootDirectory"); // 1 // NOI18N
            desc[1].setPropertyEditorClass (RootEd.class);
            ResourceBundle bundle = NbBundle.getBundle(LocalFileSystemBeanInfo.class);
            desc[0].setDisplayName (bundle.getString("PROP_readOnly"));
            desc[0].setShortDescription (bundle.getString("HINT_readOnly"));
            desc[1].setDisplayName (bundle.getString("PROP_rootDirectory"));
            desc[1].setShortDescription (bundle.getString("HINT_rootDirectory"));
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
 *  18   Gandalf   1.17        1/13/00  Jaroslav Tulach I18N
 *  17   Gandalf   1.16        11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  16   Gandalf   1.15        11/3/99  Jesse Glick     [No semantic change]
 *  15   Gandalf   1.14        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   Gandalf   1.13        8/7/99   Ian Formanek    Cleaned loading of icons
 *  13   Gandalf   1.12        7/24/99  Ian Formanek    Printing stack trace on 
 *       netbeans.debug.exceptions property only
 *  12   Gandalf   1.11        6/30/99  Ian Formanek    Reflecting package 
 *       change of FIleEditor
 *  11   Gandalf   1.10        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  10   Gandalf   1.9         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         4/7/99   Jesse Glick     Possible bugs, added 
 *       comments.
 *  8    Gandalf   1.7         3/26/99  Jaroslav Tulach 
 *  7    Gandalf   1.6         3/12/99  Jaroslav Tulach 
 *  6    Gandalf   1.5         3/4/99   Petr Hamernik   
 *  5    Gandalf   1.4         3/4/99   Petr Hamernik   
 *  4    Gandalf   1.3         3/1/99   Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
