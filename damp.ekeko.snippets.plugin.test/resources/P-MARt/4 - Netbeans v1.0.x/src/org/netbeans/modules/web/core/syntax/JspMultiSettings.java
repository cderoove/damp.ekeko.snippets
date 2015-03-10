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

package org.netbeans.modules.web.core.syntax;

import java.awt.Color;
import java.awt.Font;
import java.util.*;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.DefaultSettings;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.ext.HTMLSyntax;
import org.netbeans.modules.editor.KitSupport;

public class JspMultiSettings implements org.netbeans.editor.Settings.Initializer {

    /** Create map filled with all the desired settings
     * @param kitClass kit class for which the settings are being created
     *   or null when global settings are created.
     * @return map containing the desired settings or null if no settings
     *   are defined for the given kit
     */
    public Map updateSettingsMap (Class kitClass, Map m) {
        if (kitClass == JSPKit.class) {
            if (m == null)
                m = new HashMap();

            m.put(Settings.POPUP_MENU_ACTION_NAME_LIST,
                  new ArrayList(Arrays.asList(
                                    new String[] {
                                        KitSupport.systemActionSave,
                                        null,
                                        KitSupport.systemActionCompile,
                                        null,
                                        KitSupport.systemActionExecute,
                                        null,
                                        BaseKit.cutAction,
                                        BaseKit.copyAction,
                                        BaseKit.pasteAction,
                                        null,
                                        BaseKit.removeSelectionAction,
                                        null,
                                        KitSupport.systemActionTools,
                                        KitSupport.systemActionProperties
                                    }
                                ))
                 );

            m.put (org.netbeans.editor.Settings.ABBREV_MAP, getJSPAbbrevMap());

            Font boldFont = DefaultSettings.defaultFont.deriveFont (Font.BOLD);
            Font italicFont = DefaultSettings.defaultFont.deriveFont (Font.ITALIC);
            Font italicBoldFont = italicFont.deriveFont (Font.BOLD);
            SettingsUtil.PrintColoringSubstituter lightGraySubstituter
            = new SettingsUtil.ForeColorPrintColoringSubstituter(Color.lightGray);

            SettingsUtil.setColoring(m, JspTagSyntax.TN_JSP_TAG, new Coloring(italicBoldFont, Color.blue, new Color(239, 240, 235)));
            SettingsUtil.setColoring(m, JspTagSyntax.TN_JSP_SYMBOL, new Coloring(boldFont, Color.black, new Color(239, 240, 235)));
            SettingsUtil.setColoring(m, JspTagSyntax.TN_JSP_ATTRIBUTE, new Coloring(null, Color.green.darker().darker(), new Color(239, 240, 235)));
            SettingsUtil.setColoring(m, JspTagSyntax.TN_JSP_ATTR_VALUE, new Coloring(null, Color.magenta, new Color(239, 240, 235)));
            SettingsUtil.setColoring(m, JspTagSyntax.TN_JSP_COMMENT, new Coloring(boldFont, Color.gray, null),
                                     lightGraySubstituter);
            SettingsUtil.setColoring(m, JspTagSyntax.TN_JSP_SYMBOL2, new Coloring(boldFont, Color.black, new Color(255, 249, 223))); // same as Java

            // Token colorings for Java
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_TEXT, new Coloring(null, null, new Color(255, 249, 223)));
            //SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_ERROR, new Coloring(null, Color.white, Color.red));
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_KEYWORD, new Coloring(boldFont, Color.blue, new Color(255, 249, 223)));
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_IDENTIFIER, new Coloring(null, null, new Color(255, 249, 223)));
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_FUNCTION, new Coloring(boldFont, null, new Color(255, 249, 223)),
                                     SettingsUtil.italicFontPrintColoringSubstituter);
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_OPERATOR, new Coloring(null, null, new Color(255, 249, 223)));
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_LINE_COMMENT, new Coloring(italicFont, Color.gray, new Color(255, 249, 223)),
                                     lightGraySubstituter);
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_BLOCK_COMMENT, new Coloring(italicFont, Color.gray, new Color(255, 249, 223)),
                                     lightGraySubstituter);
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_CHAR, new Coloring(null, Color.green.darker(), new Color(255, 249, 223)));
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_STRING, new Coloring(null, Color.magenta, new Color(255, 249, 223)));
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_INT, new Coloring(null, Color.red, new Color(255, 249, 223)));
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_HEX, new Coloring(null, Color.red, new Color(255, 249, 223)));
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_OCTAL, new Coloring(null, Color.red, new Color(255, 249, 223)));
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_LONG, new Coloring(null, Color.red, new Color(255, 249, 223)));
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_FLOAT, new Coloring(null, Color.red, new Color(255, 249, 223)));
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_DOUBLE, new Coloring(null, Color.red, new Color(255, 249, 223)));

            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_LINE_COMMENT, lightGraySubstituter, true);
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_BLOCK_COMMENT, lightGraySubstituter, true);
            SettingsUtil.setColoring(m, JspMultiSyntax.JAVA_PREFIX + Syntax.TN_FUNCTION,
                                     SettingsUtil.italicFontPrintColoringSubstituter, true);

            // Token colorings for HTML
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + HTMLSyntax.TN_TEXT, new Coloring( null, null, null ) );
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + HTMLSyntax.TN_WS, new Coloring( null, null, null ) );
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + HTMLSyntax.TN_ERROR, new Coloring( null, Color.white, Color.red ) );
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + HTMLSyntax.TN_TAG, new Coloring( null, Color.blue, null ) );
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + HTMLSyntax.TN_ARGUMENT, new Coloring( null, Color.green.darker().darker(), null ) );
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + HTMLSyntax.TN_OPERATOR, new Coloring( null, Color.green, null ) );
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + HTMLSyntax.TN_VALUE, new Coloring( null, Color.magenta, null ) );
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + HTMLSyntax.TN_BLOCK_COMMENT, new Coloring( null, Color.gray, null ) );
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + HTMLSyntax.TN_SGML_COMMENT, new Coloring( null, Color.gray, null ) );
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + HTMLSyntax.TN_DECLARATION, new Coloring( null, Color.orange, null) );
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + HTMLSyntax.TN_CHARACTER, new Coloring( null, Color.red.darker(), null ) );

            /*SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + HTMLSyntax.TN_TEXT, new Coloring(null, null, null));
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + HTMLSyntax.TN_WS, new Coloring(null, null, null));
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + HTMLSyntax.TN_TAG, new Coloring(null, Color.blue, null));
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + HTMLSyntax.TN_ARGUMENT, new Coloring(null, Color.green.darker().darker(), null));
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + Syntax.TN_OPERATOR, new Coloring(null, Color.blue.darker(), null));
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + Syntax.TN_VALUE, new Coloring(null, Color.magenta, null));
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + Syntax.TN_BLOCK_COMMENT, new Coloring(italicFont, Color.gray, null),
                lightGraySubstituter);
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + Syntax.TN_SGML_COMMENT, new Coloring(italicFont, Color.gray, null),
                lightGraySubstituter);
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + Syntax.TN_DECLARATION, new Coloring(null, Color.orange.darker(), null));
            SettingsUtil.setColoring(m, JspMultiSyntax.HTML_PREFIX + Syntax.TN_CHARACTER, new Coloring(null, Color.red.darker(), null));
            */

            Syntax multiJsp = new JspMultiSyntax();
            String tokens[] = new String[multiJsp.getHighestTokenID() + 1];
            for (int i = 0; i <= multiJsp.getHighestTokenID(); i++) {
                tokens[i] = multiJsp.getTokenName(i);
            }
            SettingsUtil.updateListSetting(m, Settings.COLORING_NAME_LIST, tokens);
            /*
            SettingsUtil.updateListSetting(m, Settings.COLORING_NAME_LIST,
              new String[] {
                Syntax.TN_TEXT,
                Syntax.TN_ERROR,
                JspTagSyntax.TN_JSP_TAG,
                JspTagSyntax.TN_JSP_SYMBOL,
                JspTagSyntax.TN_JSP_COMMENT,
                JspTagSyntax.TN_JSP_ATTRIBUTE,
                JspTagSyntax.TN_JSP_ATTR_VALUE,
              }
            );

            // java
            SettingsUtil.updateListSetting(m, Settings.COLORING_NAME_LIST,
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
                Syntax.TN_DOUBLE
              }
            );

            // html
            SettingsUtil.updateListSetting(m, Settings.COLORING_NAME_LIST,
              new String[] {
                Syntax.TN_TEXT,
                Syntax.TN_ERROR,
                HTMLSyntax.TN_TAG,
                Syntax.TN_OPERATOR,
                HTMLSyntax.TN_ARG,
                Syntax.TN_BLOCK_COMMENT,
                Syntax.TN_STRING,
                Syntax.TN_INT,
              }
            );*/

        }
        return m; // Settings for other kits are not affected
    }

    Map getJSPAbbrevMap() {
        Map jspAbbrevMap = new TreeMap ();
        // <jsp:something tags
        jspAbbrevMap.put ("jspu", "<jsp:useBean id=\"");
        jspAbbrevMap.put ("jspg", "<jsp:getProperty name=\"");
        jspAbbrevMap.put ("jg", "<jsp:getProperty name=\"");
        jspAbbrevMap.put ("jsps", "<jsp:setProperty name=\"");
        jspAbbrevMap.put ("jspi", "<jsp:include page=\"");
        jspAbbrevMap.put ("jspf", "<jsp:forward page=\"");
        jspAbbrevMap.put ("jspp", "<jsp:plugin type=\"");
        // taglib
        jspAbbrevMap.put ("tglb", "<%@ taglib uri=\"");
        // <%@ page tags
        jspAbbrevMap.put ("pg", "<%@ page ");
        jspAbbrevMap.put ("pgl", "<%@ page language=\"");
        jspAbbrevMap.put ("pgex", "<%@ page extends=\"");
        jspAbbrevMap.put ("pgim", "<%@ page import=\"");
        jspAbbrevMap.put ("pgs", "<%@ page session=\"");
        jspAbbrevMap.put ("pgb", "<%@ page buffer=\"");
        jspAbbrevMap.put ("pga", "<%@ page autoFlush=\"");
        jspAbbrevMap.put ("pgin", "<%@ page info=\"");
        jspAbbrevMap.put ("pgit", "<%@ page isThreadSafe=\"");
        jspAbbrevMap.put ("pgerr", "<%@ page errorPage=\"");
        jspAbbrevMap.put ("pgc", "<%@ page contentType=\"");
        jspAbbrevMap.put ("pgie", "<%@ page isErrorPage=\"");
        // common java abbrevs
        jspAbbrevMap.put ("rg", "request.getParameter(\"");
        jspAbbrevMap.put ("sg", "session.getValue(\"");
        jspAbbrevMap.put ("sp", "session.putValue(\"");
        jspAbbrevMap.put ("sr", "session.removeValue(\"");
        jspAbbrevMap.put ("pcg", "pageContext.getAttribute(\"");
        jspAbbrevMap.put ("pcgn", "pageContext.getAttributeNamesInScope(");
        jspAbbrevMap.put ("pcgs", "pageContext.getAttributesScope(\"");
        jspAbbrevMap.put ("pcr", "pageContext.removeAttribute(\"");
        jspAbbrevMap.put ("pcs", "pageContext.setAttribute(\"");
        jspAbbrevMap.put ("ag", "application.getValue(\"");
        jspAbbrevMap.put ("ap", "application.putValue(\"");
        jspAbbrevMap.put ("ar", "application.removeValue(\"");
        jspAbbrevMap.put ("oup", "out.print(\"");
        jspAbbrevMap.put ("oupl", "out.println(\"");
        jspAbbrevMap.put ("cfgi", "config.getInitParameter(\"");

        return jspAbbrevMap;
    }

}

/*
 * Log
 *  3    Gandalf-post-FCS1.1.1.0     3/31/00  Petr Jiricka    Using new HTMLSyntax
 *  2    Gandalf   1.1         2/11/00  Petr Jiricka    Numerous small fixes.
 *  1    Gandalf   1.0         2/10/00  Petr Jiricka    
 * $
 */

