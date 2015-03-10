/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.corba.idl.editor.settings;

import java.util.Map;
import java.util.HashMap;

import java.awt.Color;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Syntax;
import org.netbeans.modules.corba.idl.editor.coloring.IDLKit;
import org.netbeans.modules.corba.idl.editor.coloring.IDLSyntax;

public class IDLEditorSettings implements org.netbeans.editor.Settings.Initializer {

    public Map updateSettingsMap (Class kitClass, Map map) {
        if (kitClass == IDLKit.class) {
            if (map == null) {
                map = new HashMap();
            }
            map.put (Settings.ABBREV_MAP, getIDLAbbrevMap());
            SettingsUtil.setColoring
            (map, IDLSyntax.TN_DIRECTIVE,
             new Coloring(null, Color.green.darker().darker(), null));
            SettingsUtil.updateListSetting(map, Settings.COLORING_NAME_LIST,
                                           new String[] {
                                               Syntax.TN_TEXT,
                                               Syntax.TN_ERROR,
                                               Syntax.TN_KEYWORD,
                                               Syntax.TN_IDENTIFIER,
                                               Syntax.TN_FUNCTION,
                                               Syntax.TN_OPERATOR,
                                               Syntax.TN_LINE_COMMENT,
                                               Syntax.TN_BLOCK_COMMENT,
                                               Syntax.TN_CHAR,
                                               Syntax.TN_STRING,
                                               Syntax.TN_INT,
                                               Syntax.TN_HEX,
                                               Syntax.TN_OCTAL,
                                               Syntax.TN_LONG,
                                               Syntax.TN_FLOAT,
                                               IDLSyntax.TN_DIRECTIVE
                                           }
                                          );

        }
        return map;
    }

    Map getIDLAbbrevMap() {
        Map idlAbbrevMap = new HashMap();
        idlAbbrevMap.put("#d", "#define ");
        idlAbbrevMap.put("#i", "#include ");
        idlAbbrevMap.put("#if", "#ifdef ");
        idlAbbrevMap.put("#ifn", "#ifndef ");
        idlAbbrevMap.put("#e", "#endif");
        idlAbbrevMap.put("#p", "#pragma ");

        idlAbbrevMap.put("at", "attribute ");
        idlAbbrevMap.put("bo", "boolean ");
        idlAbbrevMap.put("ca", "case ");
        idlAbbrevMap.put("co", "const ");
        idlAbbrevMap.put("de", "default");
        idlAbbrevMap.put("do", "double ");
        idlAbbrevMap.put("en", "enum ");
        idlAbbrevMap.put("ex", "exception ");
        idlAbbrevMap.put("FA", "FALSE");
        idlAbbrevMap.put("fa", "FALSE");
        idlAbbrevMap.put("fi", "fixed");
        idlAbbrevMap.put("fl", "float ");
        idlAbbrevMap.put("int", "interface ");
        idlAbbrevMap.put("lo", "long ");
        idlAbbrevMap.put("mo", "module ");
        idlAbbrevMap.put("Ob", "Object");
        idlAbbrevMap.put("ob", "Object");
        idlAbbrevMap.put("oc", "octet ");
        idlAbbrevMap.put("on", "oneway ");
        idlAbbrevMap.put("ra", "raises (");
        idlAbbrevMap.put("re", "readonly ");
        idlAbbrevMap.put("se", "sequence ");
        idlAbbrevMap.put("sh", "short ");
        idlAbbrevMap.put("stu", "struct ");
        idlAbbrevMap.put("str", "string ");
        idlAbbrevMap.put("sw", "switch ");
        idlAbbrevMap.put("TR", "TRUE");
        idlAbbrevMap.put("tr", "TRUE");
        idlAbbrevMap.put("ty", "typedef ");
        idlAbbrevMap.put("uns", "unsigned ");
        idlAbbrevMap.put("uni", "union ");
        idlAbbrevMap.put("wc", "wchar ");
        idlAbbrevMap.put("ws", "wstring ");

        return idlAbbrevMap;
    }
}

/*
 * <<Log>>
 *  4    Gandalf   1.3         2/8/00   Karel Gardas    
 *  3    Gandalf   1.2         12/28/99 Miloslav Metelka ColoringManager removed 
 *       and different Colorings handling
 *  2    Gandalf   1.1         11/12/99 Miloslav Metelka always returning map
 *  1    Gandalf   1.0         11/9/99  Karel Gardas    
 * $
 */
