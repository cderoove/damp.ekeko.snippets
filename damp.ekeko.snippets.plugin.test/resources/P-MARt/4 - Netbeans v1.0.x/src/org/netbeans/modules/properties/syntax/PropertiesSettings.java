/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties.syntax;

import java.awt.Font;
import java.awt.Color;
import java.util.*;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.DefaultSettings;
import org.netbeans.editor.Syntax;

public class PropertiesSettings implements org.netbeans.editor.Settings.Initializer {

    /** Create map filled with all the desired settings
     * @param kitClass kit class for which the settings are being created
     *   or null when global settings are created.
     * @return map containing the desired settings or null if no settings
     *   are defined for the given kit
     */
    public Map updateSettingsMap (Class kitClass, Map m) {
        if (kitClass == PropertiesKit.class) {
            if (m == null)
                m = new HashMap();
            m.put (org.netbeans.editor.Settings.ABBREV_MAP, getPropertiesAbbrevMap());
            Font boldFont = DefaultSettings.defaultFont.deriveFont(Font.BOLD);
            SettingsUtil.setColoring(m, PropertiesSyntax.TN_KEY, new Coloring(boldFont, Color.blue, null));
            SettingsUtil.setColoring(m, PropertiesSyntax.TN_EQ, new Coloring(null, null, null));
            SettingsUtil.setColoring(m, PropertiesSyntax.TN_VALUE, new Coloring(null, Color.magenta, null));
            SettingsUtil.updateListSetting(m, Settings.COLORING_NAME_LIST,
                                           new String[] {
                                               Syntax.TN_TEXT,
                                               Syntax.TN_LINE_COMMENT,
                                               PropertiesSyntax.TN_KEY,
                                               PropertiesSyntax.TN_EQ,
                                               PropertiesSyntax.TN_VALUE
                                           }
                                          );

        }
        return m; // Settings for other kits are not affected
    }

    Map getPropertiesAbbrevMap() {
        Map propertiesAbbrevMap = new HashMap ();
        return propertiesAbbrevMap;
    }

}

/*
 * <<Log>>
 *  3    Gandalf   1.2         12/28/99 Miloslav Metelka ColoringManager removed 
 *       and different Colorings handling
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/13/99  Petr Jiricka    
 * $
 */
