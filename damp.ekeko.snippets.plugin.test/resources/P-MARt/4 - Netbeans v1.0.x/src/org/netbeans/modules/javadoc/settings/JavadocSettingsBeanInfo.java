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
import org.openide.explorer.propertysheet.editors.FileOnlyEditor;
import org.openide.explorer.propertysheet.editors.StringArrayCustomEditor;

/** BeanInfo for JavadocSettings - defines property editor
*
* @author Petr Hrebejk
*/
public class JavadocSettingsBeanInfo extends SimpleBeanInfo {
    /** Icons for compiler settings objects. */
    static Image icon;
    static Image icon32;

    static final ResourceBundle bundle = NbBundle.getBundle(JavadocSettingsBeanInfo.class);

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor("members", JavadocSettings.class),         //0 // NOI18N
                       new PropertyDescriptor("overview", JavadocSettings.class),        // 1 // NOI18N
                       //new PropertyDescriptor("bootclasspath", JavadocSettings.class),   // // NOI18N
                       new PropertyDescriptor("extdirs", JavadocSettings.class),         // 2 // NOI18N
                       new PropertyDescriptor("style1_1", JavadocSettings.class),        // 3 // NOI18N
                       new PropertyDescriptor("verbose", JavadocSettings.class),         // 4 // NOI18N
                       new PropertyDescriptor("encoding", JavadocSettings.class),        // 5 // NOI18N
                       new PropertyDescriptor("locale", JavadocSettings.class),          // 6 // NOI18N
                   };

            desc[0].setDisplayName(bundle.getString("PROP_Members"));
            desc[0].setShortDescription(bundle.getString("HINT_Members"));
            desc[0].setPropertyEditorClass(MembersPropertyEditor.class);

            desc[1].setDisplayName(bundle.getString("PROP_Overview"));
            desc[1].setShortDescription(bundle.getString("HINT_Overview"));
            desc[1].setPropertyEditorClass(FileOnlyEditor.class);

            //desc[1].setDisplayName(bundle.getString("PROP_Bootclasspath"));
            //desc[1].setShortDescription(bundle.getString("HINT_Bootclasspath"));

            desc[2].setDisplayName(bundle.getString("PROP_Extdirs"));
            desc[2].setShortDescription(bundle.getString("HINT_Extdirs"));

            desc[3].setDisplayName(bundle.getString("PROP_Style1_1"));
            desc[3].setShortDescription(bundle.getString("HINT_Style1_1"));

            desc[4].setDisplayName(bundle.getString("PROP_Verbose"));
            desc[4].setShortDescription(bundle.getString("HINT_Verbose"));

            desc[5].setDisplayName(bundle.getString("PROP_Encoding"));
            desc[5].setShortDescription(bundle.getString("HINT_Encoding"));

            desc[6].setDisplayName(bundle.getString("PROP_Locale"));
            desc[6].setShortDescription(bundle.getString("HINT_Locale"));


        } catch (IntrospectionException ex) {
            //throw new InternalError ();
            ex.printStackTrace ();
        }
    }

    /**
    * loads icons
    */
    public JavadocSettingsBeanInfo () {
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
 *  6    Gandalf   1.5         1/13/00  Petr Hrebejk    i18n mk3  
 *  5    Gandalf   1.4         1/12/00  Petr Hrebejk    i18n
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/30/99  Ian Formanek    Reflecting package 
 *       changes of some property editors
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $
 */
