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
import java.awt.Toolkit;
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.explorer.propertysheet.editors.ChoicePropertyEditor;
import org.openide.explorer.propertysheet.*;
import org.openide.util.NbBundle;

/** Description of <code>PropertySheetSettings</code>.
*
* @author Jan Jancura
* @version 0.11, May 16, 1998
*/
public class PropertySheetSettingsBeanInfo extends SimpleBeanInfo {

    // the bundle to use
    static ResourceBundle bundle = NbBundle.getBundle (PropertySheetSettingsBeanInfo.class);

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;
    private static Image image = Toolkit.getDefaultToolkit ().getImage (
                                     PropertySheetSettingsBeanInfo.class.getResource ("/org/openide/resources/propertysheet/propertySheetSettings.gif")); // NOI18N
    private static Image image32 = Toolkit.getDefaultToolkit ().getImage (
                                       PropertySheetSettingsBeanInfo.class.getResource ("/org/openide/resources/propertysheet/propertySheetSettings.gif")); // NOI18N

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor (PropertySheet.PROPERTY_PROPERTY_PAINTING_STYLE, PropertySheetSettings.class),
                       new PropertyDescriptor (PropertySheet.PROPERTY_SORTING_MODE, PropertySheetSettings.class),
                       new PropertyDescriptor (PropertySheet.PROPERTY_PLASTIC, PropertySheetSettings.class),
                       new PropertyDescriptor (PropertySheet.PROPERTY_VALUE_COLOR, PropertySheetSettings.class),
                       new PropertyDescriptor (PropertySheet.PROPERTY_DISABLED_PROPERTY_COLOR, PropertySheetSettings.class),
                       new PropertyDescriptor (PropertySheet.PROPERTY_DISPLAY_WRITABLE_ONLY, PropertySheetSettings.class)
                   };
            desc[0].setPropertyEditorClass (PaintingStyleChoice.class);
            desc[0].setDisplayName (bundle.getString("PROP_paintingStyle"));
            desc[0].setShortDescription (bundle.getString("HINT_paintingStyle"));
            desc[1].setPropertyEditorClass (SortingChoice.class);
            desc[1].setDisplayName (bundle.getString("PROP_sortingMode"));
            desc[1].setShortDescription (bundle.getString("HINT_sortingMode"));
            desc[2].setDisplayName (bundle.getString("PROP_plastic"));
            desc[2].setShortDescription (bundle.getString("HINT_plastic"));
            desc[3].setDisplayName (bundle.getString("PROP_valueColor"));
            desc[3].setShortDescription (bundle.getString("HINT_valueColor"));
            desc[4].setDisplayName (bundle.getString("PROP_disabledPropertyColor"));
            desc[4].setShortDescription (bundle.getString("HINT_disabledPropertyColor"));
            desc[5].setDisplayName (bundle.getString("PROP_displayWritableOnly"));
            desc[5].setShortDescription (bundle.getString("HINT_displayWritableOnly"));
        } catch (IntrospectionException ex) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace();
        }
    }


    /* Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return desc;
    }

    /* Returns the PropertySheetSettings' icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
                (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return image;
        else
            return image32;
    }

    public static class PaintingStyleChoice extends ChoicePropertyEditor {
        public PaintingStyleChoice () {
            super (
                new int [] {
                    PropertySheet.ALWAYS_AS_STRING,
                    PropertySheet.STRING_PREFERRED,
                    PropertySheet.PAINTING_PREFERRED
                },
                new String [] {
                    bundle.getString ("CTL_AlwaysAsString"),
                    bundle.getString ("CTL_StringPreferred"),
                    bundle.getString ("CTL_PaintingPreferred")
                }
            );
        }
    }

    public static class SortingChoice extends ChoicePropertyEditor {
        public SortingChoice () {
            super (
                new int [] {
                    PropertySheet.UNSORTED,
                    PropertySheet.SORTED_BY_NAMES,
                    PropertySheet.SORTED_BY_TYPES
                },
                new String [] {
                    bundle.getString ("CTL_Unsorted"),
                    bundle.getString ("CTL_SortByNames"),
                    bundle.getString ("CTL_SortByTypes")
                }
            );
        }
    }
}

/*
 * Log
 *  3    Gandalf   1.2         1/13/00  Jaroslav Tulach I18N
 *  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/15/99  Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Formanek    property display names changed
 */
