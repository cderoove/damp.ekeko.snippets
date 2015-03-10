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

package org.netbeans.modules.javadoc.settings;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.StringArrayEditor;
import org.openide.explorer.propertysheet.editors.StringArrayCustomEditor;

/** BeanInfo for general documentation settings
*
* @author Petr Hrebejk
*/
public class DocumentationSettingsBeanInfo extends SimpleBeanInfo {

    /** Icons for compiler settings objects. */
    private static Image icon;
    private static Image icon32;

    private static final ResourceBundle bundle = NbBundle.getBundle(DocumentationSettingsBeanInfo.class);

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors

    static {
        try {

            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor("autocommentModifierMask", DocumentationSettings.class), // 0 // NOI18N
                       new PropertyDescriptor("autocommentPackage", DocumentationSettings.class),      // 1 // NOI18N
                       new PropertyDescriptor("autocommentErrorMask", DocumentationSettings.class),    // 2 // NOI18N
                       new PropertyDescriptor("idxSearchSort", DocumentationSettings.class),           // 3 // NOI18N
                       new PropertyDescriptor("idxSearchNoHtml", DocumentationSettings.class),         // 4 // NOI18N
                       new PropertyDescriptor("idxSearchSplit", DocumentationSettings.class),          // 5 // NOI18N
                   };

            desc[0].setDisplayName("autocommentModifierMask"); //NOI18N
            desc[0].setHidden( true );
            desc[1].setDisplayName("autocommentPackage"); //NOI18N
            desc[1].setHidden( true );
            desc[2].setDisplayName("autocommentErrorMask"); //NOI18N
            desc[2].setHidden( true );

            desc[3].setDisplayName("idxSearchSort"); //NOI18N
            desc[3].setHidden( true );
            desc[4].setDisplayName("idxSearchNoHtml"); //NOI18N
            desc[4].setHidden( true );
            desc[5].setDisplayName("idxSearchSplit"); //NOI18N
            desc[5].setHidden( true );

        } catch (IntrospectionException ex) {
            desc = new PropertyDescriptor[] {};

            //throw new InternalError ();
            ex.printStackTrace ();
        }

    }

    /** Returns the ExternalCompilerSettings' icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/javadoc/resources/JavadocSettings.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/javadoc/resources/JavadocSettings32.gif"); // NOI18N
            return icon32;
        }
    }

    /** Descriptor of valid properties
    * @return array of properties
    */

    public PropertyDescriptor[] getPropertyDescriptors () {
        return desc;
    }

}

/*
 * Log
 *  10   Gandalf   1.9         1/13/00  Petr Hrebejk    i18n mk3  
 *  9    Gandalf   1.8         1/12/00  Petr Hrebejk    i18n
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         8/17/99  Petr Hrebejk    IndexSearch window 
 *       serialization
 *  6    Gandalf   1.5         8/13/99  Petr Hrebejk    Serialization of 
 *       autocomment window added  
 *  5    Gandalf   1.4         6/30/99  Ian Formanek    Reflecting package 
 *       changes of some property editors
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/17/99  Petr Hrebejk    
 *  2    Gandalf   1.1         5/14/99  Petr Hrebejk    
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $
 */
