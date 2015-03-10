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

package org.netbeans.modules.java.settings;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;

/** BeanInfo for class ExternalCompilerSettings. It describes three properties
* one with special editor.
*
* @author  Ales Novak, Ian Formanek
*/
public class ExternalCompilerSettingsBeanInfo extends SimpleBeanInfo {
    /** Icons for compiler settings objects. */
    Image icon;
    Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor("externalCompiler", ExternalCompilerSettings.class, "getExternalCompiler", "setExternalCompiler"), // 0 // NOI18N
                       new PropertyDescriptor("errorExpression", ExternalCompilerSettings.class, "getErrorDescriptions", "setErrorDescriptions"), // 1 // NOI18N
                   };

            desc[0].setDisplayName(CompilerSettings.bundle.getString("PROP_EXTERNAL_COMPILER"));
            desc[0].setShortDescription(CompilerSettings.bundle.getString("HINT_EXTERNAL_COMPILER"));

            desc[1].setPropertyEditorClass(ErrorDescriptionsPropertyEditor.class);
            desc[1].setDisplayName(CompilerSettings.bundle.getString("ERROREXPR_PROP"));
            desc[1].setShortDescription(CompilerSettings.bundle.getString("ERROREXPR_HINT"));

        } catch (IntrospectionException ex) {
            //throw new InternalError ();
            ex.printStackTrace ();
        }
    }


    /**
    * loads icons
    */
    public ExternalCompilerSettingsBeanInfo () {
    }

    /** Returns the ExternalCompilerSettings' icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/java/settings/externalCompilerSettings.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/java/settings/externalCompilerSettings32.gif"); // NOI18N
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
 *  4    src-jtulach1.3         1/13/00  Petr Hamernik   i18n -(2nd round) - 
 *       script bug fixed.
 *  3    src-jtulach1.2         1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  2    src-jtulach1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 */

