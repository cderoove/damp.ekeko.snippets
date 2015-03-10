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

package org.openide.text;

import java.awt.Image;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;

/** BeanInfo for PrintSettings.
*
* @author Ales Novak
*/
public class PrintSettingsBeanInfo extends SimpleBeanInfo {
    private static Image icon;
    private static Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor(PrintSettings.PROP_WRAP, PrintSettings.class), // 0
                       new PropertyDescriptor(PrintSettings.PROP_HEADER_FORMAT, PrintSettings.class), // 1
                       new PropertyDescriptor(PrintSettings.PROP_FOOTER_FORMAT, PrintSettings.class), // 2
                       new PropertyDescriptor(PrintSettings.PROP_HEADER_FONT, PrintSettings.class), // 3
                       new PropertyDescriptor(PrintSettings.PROP_FOOTER_FONT, PrintSettings.class), // 4
                       new PropertyDescriptor(PrintSettings.PROP_HEADER_ALIGNMENT, PrintSettings.class), // 5
                       new PropertyDescriptor(PrintSettings.PROP_FOOTER_ALIGNMENT, PrintSettings.class), // 6
                       //        new PropertyDescriptor(PrintSettings.PROP_PAGE_FORMAT, PrintSettings.class), // 7
                       new PropertyDescriptor(PrintSettings.PROP_LINE_ASCENT_CORRECTION, PrintSettings.class) // 8
                   };
            desc[0].setDisplayName(PrintSettings.getString("PROP_WRAP"));
            desc[0].setShortDescription(PrintSettings.getString("HINT_WRAP"));
            desc[1].setDisplayName(PrintSettings.getString("PROP_HEADER_FORMAT"));
            desc[1].setShortDescription(PrintSettings.getString("HINT_HEADER_FORMAT"));
            desc[2].setDisplayName(PrintSettings.getString("PROP_FOOTER_FORMAT"));
            desc[2].setShortDescription(PrintSettings.getString("HINT_FOOTER_FORMAT"));
            desc[3].setDisplayName(PrintSettings.getString("PROP_HEADER_FONT"));
            desc[3].setShortDescription(PrintSettings.getString("HINT_HEADER_FONT"));
            desc[4].setDisplayName(PrintSettings.getString("PROP_FOOTER_FONT"));
            desc[4].setShortDescription(PrintSettings.getString("HINT_FOOTER_FONT"));
            desc[5].setDisplayName(PrintSettings.getString("PROP_HEADER_ALIGNMENT"));
            desc[5].setShortDescription(PrintSettings.getString("HINT_HEADER_ALIGNMENT"));
            desc[5].setPropertyEditorClass(PrintSettings.AlignmentEditor.class);
            desc[6].setDisplayName(PrintSettings.getString("PROP_FOOTER_ALIGNMENT"));
            desc[6].setShortDescription(PrintSettings.getString("HINT_FOOTER_ALIGNMENT"));
            desc[6].setPropertyEditorClass(PrintSettings.AlignmentEditor.class);
            /*
            desc[7].setDisplayName(PrintSettings.getString("PROP_PAGE_FORMAT"));
            desc[7].setShortDescription(PrintSettings.getString("HINT_PAGE_FORMAT"));
            desc[7].setPropertyEditorClass(PrintSettings.PageFormatEditor.class);
            */
            desc[7].setDisplayName(PrintSettings.getString("PROP_LINE_ASCENT_CORRECTION"));
            desc[7].setShortDescription(PrintSettings.getString("HINT_LINE_ASCENT_CORRECTION"));
        } catch (IntrospectionException ex) {
            throw new InternalError ();
        }
    }

    /** Returns the PrintSettings' icon */
    public Image getIcon(int type) {
        if (icon == null) {
            icon = loadImage("/org/openide/resources/printSettings.gif"); // NOI18N
            icon32 = loadImage ("/org/openide/resources/printSettings32.gif"); // NOI18N
        }
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return icon;
        else
            return icon32;
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
 *  7    Gandalf   1.6         1/13/00  Ian Formanek    NOI18N
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/7/99   Ian Formanek    Cleaned loading of icons
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/11/99  Ales Novak      new option added
 *  1    Gandalf   1.0         4/30/99  Ales Novak      
 * $
 */
