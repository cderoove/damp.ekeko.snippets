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

package org.netbeans.modules.java;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;

/** BeanInfo for class JavaExternalCompilerType. It describes three properties
* one with special editor.
*
* @author  Ales Novak, Ian Formanek
*/
public class JavaExternalCompilerTypeBeanInfo extends SimpleBeanInfo {
    /** Icons for compiler settings objects. */
    Image icon;
    Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor("externalCompiler", JavaExternalCompilerType.class), // 0 // NOI18N
                       new PropertyDescriptor("errorExpression", JavaExternalCompilerType.class), // 1 // NOI18N
                       new PropertyDescriptor("optimizeReplace", JavaExternalCompilerType.class), // 2 // NOI18N
                       new PropertyDescriptor("debuginfoReplace", JavaExternalCompilerType.class), // 3 // NOI18N
                       new PropertyDescriptor("deprecationReplace", JavaExternalCompilerType.class) // 4 // NOI18N
                   };

            desc[0].setDisplayName(JavaCompilerType.getString("PROP_EXTERNAL_COMPILER"));
            desc[0].setShortDescription(JavaCompilerType.getString("HINT_EXTERNAL_COMPILER"));

            desc[1].setPropertyEditorClass(org.openide.explorer.propertysheet.editors.ExternalCompiler.ErrorExpressionEditor.class);
            desc[1].setDisplayName(JavaCompilerType.getString("ERROREXPR_PROP"));
            desc[1].setShortDescription(JavaCompilerType.getString("ERROREXPR_HINT"));

            desc[2].setExpert(true);
            desc[2].setDisplayName(JavaCompilerType.getString("PROP_OPTIMIZE_REPLACE"));
            desc[2].setShortDescription(JavaCompilerType.getString("HINT_OPTIMIZE_REPLACE"));

            desc[3].setExpert(true);
            desc[3].setDisplayName(JavaCompilerType.getString("PROP_DEBUGINFO_REPLACE"));
            desc[3].setShortDescription(JavaCompilerType.getString("HINT_DEBUGINFO_REPLACE"));

            desc[4].setExpert(true);
            desc[4].setDisplayName(JavaCompilerType.getString("PROP_DEPRECATION_REPLACE"));
            desc[4].setShortDescription(JavaCompilerType.getString("HINT_DEPRECATION_REPLACE"));
        } catch (IntrospectionException ex) {
            //throw new InternalError ();
            ex.printStackTrace ();
        }
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

    // Please do not use constructors of super's bean info!
    // This does *not* correctly inherit properties.
    public BeanInfo[] getAdditionalBeanInfo() {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (org.netbeans.modules.java.JavaCompilerType.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(JavaExternalCompilerType.class);
        bd.setName(JavaCompilerType.getString("CTL_ExternalCompilerType"));
        return bd;
    }

}

/*
 * Log
 *  8    Gandalf   1.7         1/18/00  Jesse Glick     Various BeanInfo and 
 *       localization fixes for Java compiler types.
 *  7    Gandalf   1.6         1/14/00  Ales Novak      
 *  6    Gandalf   1.5         1/13/00  Petr Hamernik   i18n -(2nd round) - 
 *       script bug fixed.
 *  5    Gandalf   1.4         1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  4    Gandalf   1.3         10/26/99 Ales Novak      #4491
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  2    Gandalf   1.1         10/5/99  Ales Novak      changed names
 *  1    Gandalf   1.0         9/29/99  Ales Novak      
 * $
 */

