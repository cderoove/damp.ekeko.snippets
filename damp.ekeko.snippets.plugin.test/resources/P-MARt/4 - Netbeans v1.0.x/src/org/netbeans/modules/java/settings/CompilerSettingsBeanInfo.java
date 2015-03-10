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

import org.openide.util.NbBundle;

/** BeanInfo for CompilerSettings.
*
* @author Ales Novak
*/
public class CompilerSettingsBeanInfo extends SimpleBeanInfo {
    /** Icons for compiler settings objects. */
    static Image icon;
    static Image icon32;

    /** localized optimize property */
    private static final String OPTIMIZEPROP = CompilerSettings.bundle.getString("OPTIMIZE_PROP");
    /** localized optimize hint */
    private static final String OPTIMIZEHINT = CompilerSettings.bundle.getString("OPTIMIZE_HINT");
    /** localized deprecation property */
    private static final String DEPRECATIONPROP = CompilerSettings.bundle.getString("DEPRECATION_PROP");
    /** localized deprecation hint */
    private static final String DEPRECATIONHINT = CompilerSettings.bundle.getString("DEPRECATION_HINT");
    /** localized dependencies property */
    private static final String DEPENDENCIESPROP = CompilerSettings.bundle.getString("DEPENDENCIES_PROP");
    /** localized dependencies hint */
    private static final String DEPENDENCIESHINT = CompilerSettings.bundle.getString("DEPENDENCIES_HINT");
    /** localized warning property */
    private static final String WARNINGSPROP = CompilerSettings.bundle.getString("WARNINGS_PROP");
    /** localized warning hint */
    private static final String WARNINGSHINT = CompilerSettings.bundle.getString("WARNINGS_HINT");
    /** localized debug property */
    private static final String DEBUGPROP = CompilerSettings.bundle.getString("DEBUG_PROP");
    /** localized debug hint */
    private static final String DEBUGHINT = CompilerSettings.bundle.getString("DEBUG_HINT");
    /** localized charEncoding property */
    private static final String ENCODINGPROP = CompilerSettings.bundle.getString("ENCODING_PROP");
    /** localized charEncoding hint */
    private static final String ENCODINGHINT = CompilerSettings.bundle.getString("ENCODING_HINT");

    /** err msg */
    private static final String MSGERRINIT = CompilerSettings.bundle.getString("MSG_Error_in_settings");

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor ("optimize", CompilerSettings.class, "getOptimize", "setOptimize"), // 0 // NOI18N
                       new PropertyDescriptor ("deprecation", CompilerSettings.class,"getDeprecation","setDeprecation" ), // 1 // NOI18N
                       new PropertyDescriptor ("dependencies", CompilerSettings.class, "getDependencies", "setDependencies"), // 2 // NOI18N
                       new PropertyDescriptor ("warnings", CompilerSettings.class, "getWarnings", "setWarnings"), // 3 // NOI18N
                       new PropertyDescriptor ("debug", CompilerSettings.class, "getDebug", "setDebug"), // 4 // NOI18N
                       new PropertyDescriptor ("charEncoding", CompilerSettings.class, "getCharEncoding", "setCharEncoding") // 5 // NOI18N
                   };

            desc[0].setDisplayName(OPTIMIZEPROP);
            desc[0].setShortDescription(OPTIMIZEHINT);
            desc[1].setDisplayName(DEPRECATIONPROP);
            desc[1].setShortDescription(DEPRECATIONHINT);
            desc[2].setDisplayName(DEPENDENCIESPROP);
            desc[2].setShortDescription(DEPENDENCIESHINT);
            desc[3].setDisplayName(WARNINGSPROP);
            desc[3].setShortDescription(WARNINGSHINT);
            desc[4].setDisplayName(DEBUGPROP);
            desc[4].setShortDescription(DEBUGHINT);
            desc[5].setDisplayName(ENCODINGPROP);
            desc[5].setShortDescription(ENCODINGHINT);
        } catch (IntrospectionException ex) {
            throw new InternalError(MSGERRINIT);
        }
    }

    /** Default constructor
    */
    public CompilerSettingsBeanInfo () {
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
 *  5    src-jtulach1.4         1/13/00  Petr Hamernik   i18n -(2nd round) - 
 *       script bug fixed.
 *  4    src-jtulach1.3         1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  3    src-jtulach1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    src-jtulach1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Formanek    icons tweak
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    icons tweak #2
 */
