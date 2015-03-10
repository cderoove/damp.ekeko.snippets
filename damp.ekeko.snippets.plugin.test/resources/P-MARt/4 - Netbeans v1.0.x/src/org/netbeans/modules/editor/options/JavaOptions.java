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

package org.netbeans.modules.editor.options;

import org.netbeans.editor.ext.ExtSettings;
import org.netbeans.editor.ext.JavaKit;

/**
* Options for the java editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class JavaOptions extends BaseOptions {

    public static final String JAVA = "java"; // NOI18N

    public static final String COMPLETION_AUTO_POPUP_PROP = "completionAutoPopup"; // NOI18N

    public static final String COMPLETION_AUTO_POPUP_DELAY_PROP = "completionAutoPopupDelay"; // NOI18N

    public static final String FORMAT_PARENTHESIS_ADD_SPACE_PROP = "formatParenthesisAddSpace"; // NOI18N

    public static final String FORMAT_COMPOUND_BRACKET_ADD_NL_PROP = "formatCompoundBracketAddNL"; // NOI18N

    static final String[] JAVA_PROP_NAMES = new String[] {
                                                COMPLETION_AUTO_POPUP_PROP,
                                                COMPLETION_AUTO_POPUP_DELAY_PROP,
                                                FORMAT_PARENTHESIS_ADD_SPACE_PROP,
                                                FORMAT_COMPOUND_BRACKET_ADD_NL_PROP
                                            };

    static final long serialVersionUID =-7951549840240159575L;

    public JavaOptions() {
        this(JavaKit.class, JAVA);
    }

    public JavaOptions(Class kitClass, String typeName) {
        super(kitClass, typeName);
    }

    public boolean getFormatParenthesisAddSpace() {
        return ((Boolean)getSettingValue(ExtSettings.FORMAT_PARENTHESIS_ADD_SPACE)).booleanValue();
    }
    public void setFormatParenthesisAddSpace(boolean v) {
        setSettingValue(ExtSettings.FORMAT_PARENTHESIS_ADD_SPACE, v ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean getFormatCompoundBracketAddNL() {
        return ((Boolean)getSettingValue(ExtSettings.FORMAT_COMPOUND_BRACKET_ADD_NL)).booleanValue();
    }
    public void setFormatCompoundBracketAddNL(boolean v) {
        setSettingValue(ExtSettings.FORMAT_COMPOUND_BRACKET_ADD_NL, v ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean getCompletionAutoPopup() {
        return ((Boolean)getSettingValue(ExtSettings.JCOMPLETION_AUTO_POPUP)).booleanValue();
    }
    public void setCompletionAutoPopup(boolean v) {
        setSettingValue(ExtSettings.JCOMPLETION_AUTO_POPUP, v ? Boolean.TRUE : Boolean.FALSE);
    }

    public int getCompletionAutoPopupDelay() {
        return ((Integer)getSettingValue(ExtSettings.JCOMPLETION_AUTO_POPUP_DELAY)).intValue();
    }
    public void setCompletionAutoPopupDelay(int delay) {
        setSettingValue(ExtSettings.JCOMPLETION_AUTO_POPUP_DELAY, new Integer(delay));
    }


}

/*
 * Log
 *  10   Gandalf   1.9         2/15/00  Miloslav Metelka Parenthesis instead of 
 *       curly braces
 *  9    Gandalf   1.8         1/13/00  Miloslav Metelka Localization
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  6    Gandalf   1.5         7/26/99  Miloslav Metelka 
 *  5    Gandalf   1.4         7/21/99  Miloslav Metelka 
 *  4    Gandalf   1.3         7/21/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/20/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/3/99   Ian Formanek    Changed package 
 *       statement to make it compilable
 *  1    Gandalf   1.0         6/30/99  Ales Novak      
 * $
 */
