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
import org.openide.explorer.propertysheet.editors.DirectoryOnlyEditor;


/** BeanInfo for standard doclet settings
*
* @author Petr Hrebejk
*/
public class StdDocletSettingsBeanInfo extends SimpleBeanInfo {
    /** Icons for compiler settings objects. */
    static Image icon;
    static Image icon32;

    static final ResourceBundle bundle = NbBundle.getBundle(StdDocletSettingsBeanInfo.class);

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor("directory", StdDocletSettings.class),       // 0 // NOI18N
                       new PropertyDescriptor("use", StdDocletSettings.class),             // 1 // NOI18N
                       new PropertyDescriptor("version", StdDocletSettings.class),         // 2 // NOI18N
                       new PropertyDescriptor("author", StdDocletSettings.class),          // 3 // NOI18N
                       new PropertyDescriptor("splitindex", StdDocletSettings.class),      // 4 // NOI18N
                       new PropertyDescriptor("windowtitle", StdDocletSettings.class),     // 5 // NOI18N
                       new PropertyDescriptor("doctitle", StdDocletSettings.class),        // 6 // NOI18N
                       new PropertyDescriptor("header", StdDocletSettings.class),          // 7 // NOI18N
                       new PropertyDescriptor("footer", StdDocletSettings.class),          // 8 // NOI18N
                       new PropertyDescriptor("bottom", StdDocletSettings.class),          // 9 // NOI18N
                       new PropertyDescriptor("link", StdDocletSettings.class),            // 10 // NOI18N
                       new PropertyDescriptor("group", StdDocletSettings.class),           // 11 // NOI18N
                       new PropertyDescriptor("nodeprecated", StdDocletSettings.class),    // 12 // NOI18N
                       new PropertyDescriptor("nodeprecatedlist", StdDocletSettings.class),// 13 // NOI18N
                       new PropertyDescriptor("notree", StdDocletSettings.class),          // 14 // NOI18N
                       new PropertyDescriptor("noindex", StdDocletSettings.class),         // 15 // NOI18N
                       new PropertyDescriptor("nohelp", StdDocletSettings.class),          // 16 // NOI18N
                       new PropertyDescriptor("nonavbar", StdDocletSettings.class),        // 17 // NOI18N
                       new PropertyDescriptor("helpfile", StdDocletSettings.class),        // 18 // NOI18N
                       new PropertyDescriptor("stylesheetfile", StdDocletSettings.class),  // 19 // NOI18N
                       new PropertyDescriptor("charset", StdDocletSettings.class),         // 20 // NOI18N
                   };

            desc[0].setDisplayName(bundle.getString("PROP_Directory"));
            desc[0].setShortDescription(bundle.getString("HINT_Directory"));
            desc[0].setPropertyEditorClass( DirectoryOnlyEditor.class );
            desc[1].setDisplayName(bundle.getString("PROP_Use"));
            desc[1].setShortDescription(bundle.getString("HINT_Use"));
            desc[2].setDisplayName(bundle.getString("PROP_Version"));
            desc[2].setShortDescription(bundle.getString("HINT_Version"));
            desc[3].setDisplayName(bundle.getString("PROP_Author"));
            desc[3].setShortDescription(bundle.getString("HINT_Author"));
            desc[4].setDisplayName(bundle.getString("PROP_Splitindex"));
            desc[4].setShortDescription(bundle.getString("HINT_Splitindex"));
            desc[5].setDisplayName(bundle.getString("PROP_Windowtitle"));
            desc[5].setShortDescription(bundle.getString("HINT_Windowtitle"));
            desc[6].setDisplayName(bundle.getString("PROP_Doctitle"));
            desc[6].setShortDescription(bundle.getString("HINT_Doctitle"));
            desc[7].setDisplayName(bundle.getString("PROP_Header"));
            desc[7].setShortDescription(bundle.getString("HINT_Header"));
            desc[8].setDisplayName(bundle.getString("PROP_Footer"));
            desc[8].setShortDescription(bundle.getString("HINT_Footer"));
            desc[9].setDisplayName(bundle.getString("PROP_Bottom"));
            desc[9].setShortDescription(bundle.getString("HINT_Bottom"));
            desc[10].setDisplayName(bundle.getString("PROP_Link"));
            desc[10].setShortDescription(bundle.getString("HINT_Link"));
            desc[11].setDisplayName(bundle.getString("PROP_Group"));
            desc[11].setShortDescription(bundle.getString("HINT_Group"));
            //desc[11].setPropertyEditorClass(StdDocletSettings.GroupEditor.class);
            desc[12].setDisplayName(bundle.getString("PROP_Nodeprecated"));
            desc[12].setShortDescription(bundle.getString("HINT_Nodeprecated"));
            desc[13].setDisplayName(bundle.getString("PROP_Nodeprecatedlist"));
            desc[13].setShortDescription(bundle.getString("HINT_Nodeprecatedlist"));
            desc[14].setDisplayName(bundle.getString("PROP_Notree"));
            desc[14].setShortDescription(bundle.getString("HINT_Notree"));
            desc[15].setDisplayName(bundle.getString("PROP_Noindex"));
            desc[15].setShortDescription(bundle.getString("HINT_Noindex"));
            desc[16].setDisplayName(bundle.getString("PROP_Nohelp"));
            desc[16].setShortDescription(bundle.getString("HINT_Nohelp"));
            desc[17].setDisplayName(bundle.getString("PROP_Nonavbar"));
            desc[17].setShortDescription(bundle.getString("HINT_Nonavbar"));
            desc[18].setDisplayName(bundle.getString("PROP_Helpfile"));
            desc[18].setShortDescription(bundle.getString("HINT_Helpfile"));
            desc[18].setPropertyEditorClass( DirectoryOnlyEditor.class );
            desc[19].setDisplayName(bundle.getString("PROP_Stylesheetfile"));
            desc[19].setShortDescription(bundle.getString("HINT_Stylesheetfile"));
            desc[19].setPropertyEditorClass( DirectoryOnlyEditor.class );
            desc[20].setDisplayName(bundle.getString("PROP_Charset"));
            desc[20].setShortDescription(bundle.getString("HINT_Charset"));

            //desc[0].setPropertyEditorClass(FileOnlyEditor.class);
            //desc[0].setPropertyEditorClass(StringArrayCustomEditor.class);

        } catch (IntrospectionException ex) {
            //throw new InternalError ();
            ex.printStackTrace ();
        }
    }

    /**
    * loads icons
    */
    public StdDocletSettingsBeanInfo () {
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
 *  8    Gandalf   1.7         1/13/00  Petr Hrebejk    i18n mk3  
 *  7    Gandalf   1.6         1/12/00  Petr Hrebejk    i18n
 *  6    Gandalf   1.5         1/3/00   Petr Hrebejk    Bugfix 4747
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/15/99  Petr Hrebejk    Option -docencoding 
 *       changed to -charset
 *  3    Gandalf   1.2         6/30/99  Ian Formanek    Reflecting package 
 *       changes of some property editors
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $
 */
