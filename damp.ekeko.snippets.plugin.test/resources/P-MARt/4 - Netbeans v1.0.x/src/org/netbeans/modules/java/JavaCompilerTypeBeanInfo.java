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

import org.openide.util.NbBundle;

/** BeanInfo for CompilerSettings.
*
* @author Ales Novak
*/
public class JavaCompilerTypeBeanInfo extends SimpleBeanInfo {
    /** Icons for compiler settings objects. */
    static Image icon;
    static Image icon32;

    /** localized optimize property */
    private static final String OPTIMIZEPROP = JavaCompilerType.getString("OPTIMIZE_PROP");
    /** localized optimize hint */
    private static final String OPTIMIZEHINT = JavaCompilerType.getString("OPTIMIZE_HINT");
    /** localized deprecation property */
    private static final String DEPRECATIONPROP = JavaCompilerType.getString("DEPRECATION_PROP");
    /** localized deprecation hint */
    private static final String DEPRECATIONHINT = JavaCompilerType.getString("DEPRECATION_HINT");
    /** localized dependencies property */
    private static final String DEPENDENCIESPROP = JavaCompilerType.getString("DEPENDENCIES_PROP");
    /** localized dependencies hint */
    private static final String DEPENDENCIESHINT = JavaCompilerType.getString("DEPENDENCIES_HINT");
    /** localized warning property */
    private static final String WARNINGSPROP = JavaCompilerType.getString("WARNINGS_PROP");
    /** localized warning hint */
    private static final String WARNINGSHINT = JavaCompilerType.getString("WARNINGS_HINT");
    /** localized debug property */
    private static final String DEBUGPROP = JavaCompilerType.getString("DEBUG_PROP");
    /** localized debug hint */
    private static final String DEBUGHINT = JavaCompilerType.getString("DEBUG_HINT");
    /** localized charEncoding property */
    private static final String ENCODINGPROP = JavaCompilerType.getString("ENCODING_PROP");
    /** localized charEncoding hint */
    private static final String ENCODINGHINT = JavaCompilerType.getString("ENCODING_HINT");

    /** err msg */
    private static final String MSGERRINIT = JavaCompilerType.getString("MSG_Error_in_settings");

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor ("optimize", JavaCompilerType.class, "getOptimize", "setOptimize"), // 0 // NOI18N
                       new PropertyDescriptor ("deprecation", JavaCompilerType.class,"getDeprecation","setDeprecation" ), // 1 // NOI18N
                       new PropertyDescriptor ("debug", JavaCompilerType.class, "getDebug", "setDebug"), // 2 // NOI18N
                       new PropertyDescriptor ("charEncoding", JavaCompilerType.class, "getCharEncoding", "setCharEncoding") // 3 // NOI18N
                   };

            desc[0].setDisplayName(OPTIMIZEPROP);
            desc[0].setShortDescription(OPTIMIZEHINT);
            desc[1].setDisplayName(DEPRECATIONPROP);
            desc[1].setShortDescription(DEPRECATIONHINT);
            desc[2].setDisplayName(DEBUGPROP);
            desc[2].setShortDescription(DEBUGHINT);
            desc[3].setDisplayName(ENCODINGPROP);
            desc[3].setShortDescription(ENCODINGHINT);
        } catch (IntrospectionException ex) {
            throw new InternalError(MSGERRINIT);
        }
    }

    /** Default constructor
    */
    public JavaCompilerTypeBeanInfo() {
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (org.openide.compiler.CompilerType.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    /** Returns the CompilerSettings' icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/java/settings/compilerSettings.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/java/settings/compilerSettings32.gif"); // NOI18N
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
 */
