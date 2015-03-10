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

package org.netbeans.core.output;

import java.awt.Color;
import java.beans.BeanInfo;
import java.util.ResourceBundle;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Settings for output window.
*
* @author Petr Hamernik
* @version 0.12 Feb 28, 1998
*/
public class OutputSettings extends SystemOption {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5773055866277884154L;

    /** bundle to obtain text information from */
    private static ResourceBundle bundle;

    /** Property name of the fontSize property */
    public static final String PROP_FONT_SIZE = "fontSize"; // NOI18N
    /** Property name of the tabSize property */
    public static final String PROP_TAB_SIZE = "tabSize"; // NOI18N
    /** Property name of the foreground property */
    public static final String PROP_FOREGROUND = "foreground"; // NOI18N
    /** Property name of the cursorForeground property */
    public static final String PROP_CURSOR_FOREGROUND = "cursorForeground"; // NOI18N
    /** Property name of the jumpCursorForeground property */
    public static final String PROP_JUMP_CURSOR_FOREGROUND = "jumpCursorForeground"; // NOI18N
    /** Property name of the background property */
    public static final String PROP_BACKGROUND = "background"; // NOI18N
    /** Property name of the cursorBackground property */
    public static final String PROP_CURSOR_BACKGROUND = "cursorBackground"; // NOI18N
    /** Property name of the jumpCursorBackground property */
    public static final String PROP_JUMP_CURSOR_BACKGROUND = "jumpCursorBackground"; // NOI18N

    private static int fontSize = 12;
    private static int tabSize = 8;

    private static Color fBase = Color.black;
    private static Color fSelect = Color.white;
    private static Color fJumpSelect = Color.white;
    private static Color bBase = (java.awt.Color) javax.swing.UIManager.getDefaults ().get ("Label.background"); // NOI18N

    private static Color bSelect = Color.blue;
    private static Color bJumpSelect = Color.red;

    public OutputSettings () {
    }

    public String displayName () {
        return getString("CTL_Output_settings");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (OutputSettings.class);
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        if (this.fontSize != fontSize) {
            this.fontSize = fontSize;
            change();
        }
    }

    /** Tab size getter */
    public int getTabSize() {
        return tabSize;
    }

    /** Tab size setter */
    public void setTabSize(int tabSize) {
        if (this.tabSize != tabSize) {
            this.tabSize = tabSize;
            change();
        }
    }

    public Color getBaseForeground() { return fBase; }
    public Color getCursorForeground() { return fSelect; }
    public Color getJumpCursorForeground() { return fJumpSelect; }
    public Color getBaseBackground() { return bBase; }
    public Color getCursorBackground() { return bSelect; }
    public Color getJumpCursorBackground() { return bJumpSelect; }

    public void setBaseForeground(Color c) { fBase = c; change(); }
    public void setCursorForeground(Color c) { fSelect = c; change(); }
    public void setJumpCursorForeground(Color c) { fJumpSelect = c; change(); }
    public void setBaseBackground(Color c) { bBase = c; change(); }
    public void setCursorBackground(Color c) { bSelect = c; change(); }
    public void setJumpCursorBackground(Color c) { bJumpSelect = c; change(); }

    private void change() {
        firePropertyChange (null, null, null);
    }

    /** @return localized string */
    static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(OutputSettings.class);
        }
        return bundle.getString(s);
    }
}

/*
 * Log
 *  10   Gandalf   1.9         1/16/00  Ian Formanek    NOI18N
 *  9    Gandalf   1.8         1/16/00  Ian Formanek    OutputWindow has 
 *       consistent background with the rest of IDE
 *  8    Gandalf   1.7         1/12/00  Ales Novak      i18n
 *  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         7/2/99   Jesse Glick     More help IDs.
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/20/99  Ales Novak      exception parsing + copy
 *       action
 *  3    Gandalf   1.2         3/8/99   Petr Hamernik   localization
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach 
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
