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

package org.netbeans.editor.ext;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import javax.swing.KeyStroke;
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.undo.UndoManager;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.DefaultSettings;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.view.ViewSettings;

/**
* Extended settings provide the settings for the extended editor features
* supported by the various classes of this package.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ExtSettings implements Settings.Initializer {

    public static final Integer defaultJCAutoPopupDelay = new Integer(500);
    public static final Integer defaultJCRefreshDelay = new Integer(200);
    public static final Boolean defaultJCAutoPopup = Boolean.TRUE;
    public static final Boolean defaultFormatParenthesisAddSpace = Boolean.FALSE;
    public static final Boolean defaultFormatCompoundBracketAddNL = Boolean.FALSE;

    /** Highlight the row where the caret currently is. The ExtCaret must be used.
    * Values: java.lang.Boolean 
    */
    public static final String HIGHLIGHT_CARET_ROW = "highlight-caret-row"; // NOI18N

    /** Highlight the matching bracket (if the caret currently stands after the bracket).
    * The ExtCaret must be used.
    * Values: java.lang.Boolean 
    */
    public static final String HIGHLIGHT_MATCHING_BRACKET = "highlight-matching-bracket"; // NOI18N

    /** Whether the fast and simple matching should be used for higlighting
    * the matching bracket. Its disadvantage is that it doesn't ignore the comments
    * and string and character constants in the search.
    */
    public static final String SIMPLE_BRACKET_MATCHING = "simple-bracket-matching"; // NOI18N

    /**
    * Cell renderer to be used for the code completion list.
    */
    public static final String JCOMPLETION_CELL_RENDERER = "jcompletion-cell-renderer"; // NOI18N

    /** The delay after which the completion window is shown automatically.
    * Values: java.lang.Integer
    */
    public static final String JCOMPLETION_AUTO_POPUP_DELAY = "jcompletion-auto-popup-delay"; // NOI18N

    /** Whether the code completion window should popup automatically.
    * Values: java.lang.Boolean
    */
    public static final String JCOMPLETION_AUTO_POPUP = "jcompletion-auto-popup"; // NOI18N

    /** The delay after which the completion window is refreshed.
    * Values: java.lang.Integer
    */
    public static final String JCOMPLETION_REFRESH_DELAY = "jcompletion-refresh-delay"; // NOI18N

    /** Whether insert extra space before the parenthesis or not.
    * Values: java.lang.Boolean instances
    * Example: Settings.setValue(JavaKit.class, Settings.FORMAT_PARENTHESIS_ADD_SPACE,
    *              Boolean.TRUE);
    * Effect: c.setSize(200, 100)
    *           becomes
    *         c.setSize (200, 100)
    */
    public static final String FORMAT_PARENTHESIS_ADD_SPACE
    = "format-parenthesis-add-space"; // NOI18N

    /** Whether insert space after the comma inside the parameter list
    * Values: java.lang.Boolean instances
    */
    public static final String FORMAT_COMMA_ADD_SPACE = "format-comma-add-space"; // NOI18N

    /** Whether insert extra new-line before the compound bracket or not.
    * Values: java.lang.Boolean instances
    * Example: Settings.setValue(JavaKit.class, Settings.FORMAT_SHIFT_WIDTH,
    *              Boolean.TRUE);
    * Effect: if (test) {
    *           foo();
    *         }
    *           becomes
    *         if (test)
    *         {
    *           foo();
    *         }
    */
    public static final String FORMAT_COMPOUND_BRACKET_ADD_NL
    = "format-compound-bracket-add-nl"; // NOI18N

    /** Add star at the line begining of the multi-line comment
    * if it's not already there.
    * Values: java.lang.Boolean
    * Default: Boolean.FALSE
    * Example: Settings.setValue(JavaKit.class, Settings.FORMAT_COMPOUND_BRACKET_ADD_NL,
    *              Boolean.TRUE);
    * Effect: /* this is
    *          multiline comment
    *         *\/
    *            becomes
    *         /* this is
    *         *  multiline comment
    *         *\/
    */
    public static final String FORMAT_COMMENT_ADD_STAR
    = "format-comment-add-star"; // NOI18N

    /** Add one more space to the begining of each line
    * in the multi-line comment
    * if it's not already there.
    * Values: java.lang.Boolean
    * Default: Boolean.FALSE
    * Example: Settings.setValue(JavaKit.class, Settings.FORMAT_COMPOUND_BRACKET_ADD_NL,
    *              Boolean.TRUE);
    * Effect: /* this is
    *          multiline comment
    *         *\/
    *            becomes
    *         /* this is
    *          *  multiline comment
    *          *\/
    */
    public static final String FORMAT_COMMENT_ADD_SPACE
    = "format-comment-add-space"; // NOI18N

    /** Whether to perform syntax coloring of the javadoc block comments. It requires
    * using of the different syntax anlayzer.
    * Values: java.lang.Boolean
    */
    public static final String JAVADOC_SYNTAX_COLORING = "javadoc-syntax-coloring"; // NOI18N

    private static boolean inited;

    /**
     * @associates String 
     */
    private Map javaAbbrevActionMap;

    /**
     * @associates String 
     */
    private Map javaAbbrevMap;
    private JTextComponent.KeyBinding[] javaKeyBindings;

    /** Initialization */
    public static void init() {
        if (!inited) {
            Settings.addInitializer(new ExtSettings());
            inited = true;
        }
    }

    /** Construct default settings */
    ExtSettings() {
        // make sure default and view settings are added as initializer
        Settings.addInitializer(new DefaultSettings());
        Settings.addInitializer(new ViewSettings());
    }

    /** Create map filled with all the desired settings
    * @param kitClass kit class for which the settings are being created
    *   or null when global settings are created.
    * @return map containing the desired settings or null if no settings
    *   are defined for the given kit
    */
    public Map updateSettingsMap(Class kitClass, Map m) {

        if (kitClass == JavaKit.class) {
            // ------------------------ JavaKit Settings --------------------------------------

            if (m == null) {
                m = new HashMap();
            }

            m.put(HIGHLIGHT_CARET_ROW, Boolean.FALSE);
            m.put(HIGHLIGHT_MATCHING_BRACKET, Boolean.TRUE);
            m.put(SIMPLE_BRACKET_MATCHING, Boolean.FALSE);

            m.put(Settings.IDENTIFIER_ACCEPTOR, AcceptorFactory.JAVA_IDENTIFIER);
            m.put(Settings.ABBREV_ACTION_MAP, getJavaAbbrevActionMap());
            m.put(Settings.ABBREV_MAP, getJavaAbbrevMap());
            m.put(Settings.ABBREV_RESET_ACCEPTOR, AcceptorFactory.NON_JAVA_IDENTIFIER);
            m.put(Settings.FORMATTER, new JavaFormatter());
            m.put(Settings.INDENT_HOT_CHAR_ACCEPTOR,
                  new Acceptor() {
                      public boolean accept(char ch) {
                          return (ch == '}');
                      }
                  }
                 );
            m.put(Settings.WORD_MATCH_MATCH_CASE, Boolean.TRUE);
            m.put(Settings.WORD_MATCH_STATIC_WORDS,
                  "Exception IntrospectionException FileNotFoundException IOException" // NOI18N
                  + " ArrayIndexOutOfBoundsException ClassCastException ClassNotFoundException" // NOI18N
                  + " CloneNotSupportedException NullPointerException NumberFormatException" // NOI18N
                  + " SQLException"); // NOI18N

            SettingsUtil.updateListSetting(m, Settings.KEY_BINDING_LIST, getExtKeyBindings());
            SettingsUtil.updateListSetting(m, Settings.KEY_BINDING_LIST, getJavaKeyBindings());
            m.put(JCOMPLETION_CELL_RENDERER, new JCCellRenderer());
            m.put(JCOMPLETION_AUTO_POPUP, defaultJCAutoPopup);
            m.put(JCOMPLETION_AUTO_POPUP_DELAY, defaultJCAutoPopupDelay);
            m.put(JCOMPLETION_REFRESH_DELAY, defaultJCRefreshDelay);
            m.put(FORMAT_PARENTHESIS_ADD_SPACE, defaultFormatParenthesisAddSpace);
            m.put(FORMAT_COMPOUND_BRACKET_ADD_NL, defaultFormatCompoundBracketAddNL);

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

        } else if (kitClass == HTMLKit.class) {
            // ------------------------ HTMLKit Settings --------------------------------------

            if (m == null) {
                m = new HashMap();
            }

            SettingsUtil.setColoring(m, HTMLSyntax.TN_TEXT, new Coloring( null, null, null ) );
            SettingsUtil.setColoring(m, HTMLSyntax.TN_WS, new Coloring( null, null, null ) );
            SettingsUtil.setColoring(m, HTMLSyntax.TN_ERROR, new Coloring( null, Color.white, Color.red ) );
            SettingsUtil.setColoring(m, HTMLSyntax.TN_TAG, new Coloring( null, Color.blue, null ) );
            SettingsUtil.setColoring(m, HTMLSyntax.TN_ARGUMENT, new Coloring( null, Color.green.darker().darker(), null ) );
            SettingsUtil.setColoring(m, HTMLSyntax.TN_OPERATOR, new Coloring( null, Color.green, null ) );
            SettingsUtil.setColoring(m, HTMLSyntax.TN_VALUE, new Coloring( null, Color.magenta, null ) );
            SettingsUtil.setColoring(m, HTMLSyntax.TN_BLOCK_COMMENT, new Coloring( null, Color.gray, null ) );
            SettingsUtil.setColoring(m, HTMLSyntax.TN_SGML_COMMENT, new Coloring( null, Color.gray, null ) );
            SettingsUtil.setColoring(m, HTMLSyntax.TN_DECLARATION, new Coloring( null, Color.orange, null) );
            SettingsUtil.setColoring(m, HTMLSyntax.TN_CHARACTER, new Coloring( null, Color.red.darker(), null ) );
            SettingsUtil.updateListSetting(m, Settings.COLORING_NAME_LIST,
                                           new String[] {
                                               HTMLSyntax.TN_TEXT,
                                               HTMLSyntax.TN_WS,
                                               HTMLSyntax.TN_ERROR,
                                               HTMLSyntax.TN_TAG,
                                               HTMLSyntax.TN_ARGUMENT,
                                               HTMLSyntax.TN_OPERATOR,
                                               HTMLSyntax.TN_VALUE,
                                               HTMLSyntax.TN_BLOCK_COMMENT,
                                               HTMLSyntax.TN_SGML_COMMENT,
                                               HTMLSyntax.TN_DECLARATION,
                                               HTMLSyntax.TN_CHARACTER
                                           }
                                          );
        }

        return m; // Settings for other kits are not affected
    }

    public Map getJavaAbbrevActionMap() {
        if (javaAbbrevActionMap == null) {
            javaAbbrevActionMap = new TreeMap();
            javaAbbrevActionMap.put("soutd", JavaKit.abbrevDebugLineAction); // NOI18N
        }
        return javaAbbrevActionMap;
    }

    public Map getJavaAbbrevMap() {
        if (javaAbbrevMap == null) {
            javaAbbrevMap = new TreeMap();
            javaAbbrevMap.put("sout", "System.out.println(\""); // NOI18N
            javaAbbrevMap.put("serr", "System.err.println(\""); // NOI18N

            javaAbbrevMap.put("impa", "import java.awt."); // NOI18N
            javaAbbrevMap.put("impb", "import java.beans."); // NOI18N
            javaAbbrevMap.put("impd", "import org.netbeans."); // NOI18N
            javaAbbrevMap.put("impx", "import org.netbeans.developerx."); // NOI18N
            javaAbbrevMap.put("impj", "import java."); // NOI18N
            javaAbbrevMap.put("imps", "import javax.swing."); // NOI18N
            javaAbbrevMap.put("impS", "import com.sun.java.swing."); // NOI18N
            javaAbbrevMap.put("impq", "import javax.sql."); // NOI18N
            javaAbbrevMap.put("impi", "import org.openide."); // NOI18N

            javaAbbrevMap.put("psf", "private static final "); // NOI18N
            javaAbbrevMap.put("psfi", "private static final int "); // NOI18N
            javaAbbrevMap.put("psfs", "private static final String "); // NOI18N
            javaAbbrevMap.put("psfb", "private static final boolean "); // NOI18N
            javaAbbrevMap.put("Psf", "public static final "); // NOI18N
            javaAbbrevMap.put("Psfi", "public static final int "); // NOI18N
            javaAbbrevMap.put("Psfs", "public static final String "); // NOI18N
            javaAbbrevMap.put("Psfb", "public static final boolean "); // NOI18N

            javaAbbrevMap.put("ab", "abstract "); // NOI18N
            javaAbbrevMap.put("bo", "boolean "); // NOI18N
            javaAbbrevMap.put("br", "break"); // NOI18N
            javaAbbrevMap.put("ca", "catch ("); // NOI18N
            javaAbbrevMap.put("cl", "class "); // NOI18N
            javaAbbrevMap.put("cn", "continue"); // NOI18N
            javaAbbrevMap.put("df", "default:"); // NOI18N
            javaAbbrevMap.put("ex", "extends "); // NOI18N
            javaAbbrevMap.put("fa", "false"); // NOI18N
            javaAbbrevMap.put("fi", "final "); // NOI18N
            javaAbbrevMap.put("fl", "float "); // NOI18N
            javaAbbrevMap.put("fy", "finally "); // NOI18N
            javaAbbrevMap.put("im", "implements "); // NOI18N
            javaAbbrevMap.put("ir", "import "); // NOI18N
            javaAbbrevMap.put("iof", "instanceof "); // NOI18N
            javaAbbrevMap.put("ie", "interface "); // NOI18N
            javaAbbrevMap.put("pr", "private "); // NOI18N
            javaAbbrevMap.put("pe", "protected "); // NOI18N
            javaAbbrevMap.put("pu", "public "); // NOI18N
            javaAbbrevMap.put("re", "return"); // NOI18N
            javaAbbrevMap.put("sh", "short "); // NOI18N
            javaAbbrevMap.put("st", "static "); // NOI18N
            javaAbbrevMap.put("sw", "switch ("); // NOI18N
            javaAbbrevMap.put("sy", "synchronized "); // NOI18N
            javaAbbrevMap.put("tr", "transient "); // NOI18N
            javaAbbrevMap.put("th", "throws "); // NOI18N
            javaAbbrevMap.put("tw", "throw "); // NOI18N
            javaAbbrevMap.put("twn", "throw new "); // NOI18N
            javaAbbrevMap.put("twni", "throw new InternalError();"); // NOI18N
            javaAbbrevMap.put("twne", "throw new Error();"); // NOI18N
            javaAbbrevMap.put("wh", "while ("); // NOI18N

            javaAbbrevMap.put("En", "Enumeration"); // NOI18N
            javaAbbrevMap.put("Ex", "Exception"); // NOI18N
            javaAbbrevMap.put("Gr", "Graphics"); // NOI18N
            javaAbbrevMap.put("Ob", "Object"); // NOI18N
            javaAbbrevMap.put("Re", "Rectangle"); // NOI18N
            javaAbbrevMap.put("St", "String"); // NOI18N
            javaAbbrevMap.put("Ve", "Vector"); // NOI18N

            javaAbbrevMap.put("pst", "printStackTrace();"); // NOI18N
            javaAbbrevMap.put("tds", "Thread.dumpStack();"); // NOI18N
        }
        return javaAbbrevMap;
    }

    public JTextComponent.KeyBinding[] getJavaKeyBindings() {
        if (javaKeyBindings == null) {
            javaKeyBindings = new JTextComponent.KeyBinding[] {
                                  new MultiKeyBinding(
                                      KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK),
                                      JavaKit.jCompletionShowHelpAction
                                  ),
                                  new MultiKeyBinding(
                                      KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                      BaseKit.escapeAction
                                  ),
                                  new MultiKeyBinding(
                                      KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
                                      JavaKit.commentAction
                                  ),
                                  new MultiKeyBinding(
                                      KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
                                      JavaKit.uncommentAction
                                  ),
                                  new MultiKeyBinding(
                                      new KeyStroke[] {
                                          KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
                                          KeyStroke.getKeyStroke(KeyEvent.VK_G, 0)
                                      },
                                      JavaKit.makeGetterAction
                                  ),
                                  new MultiKeyBinding(
                                      new KeyStroke[] {
                                          KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
                                          KeyStroke.getKeyStroke(KeyEvent.VK_S, 0)
                                      },
                                      JavaKit.makeSetterAction
                                  ),
                                  new MultiKeyBinding(
                                      new KeyStroke[] {
                                          KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
                                          KeyStroke.getKeyStroke(KeyEvent.VK_I, 0)
                                      },
                                      JavaKit.makeIsAction
                                  )
                              };
        }
        return javaKeyBindings;
    }

    public JTextComponent.KeyBinding[] getExtKeyBindings() {
        return new JTextComponent.KeyBinding[] {
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.ALT_MASK),
                       ExtActionFactory.gotoDeclarationAction
                   )
               };
    }

}

/*
 * Log
 *  34   Gandalf-post-FCS1.32.1.0    3/31/00  Petr Nejedly    New HTML Syntax
 *  33   Gandalf   1.32        3/8/00   Miloslav Metelka "de" -> "df" abbrev
 *  32   Gandalf   1.31        2/15/00  Miloslav Metelka parenthesis instead of 
 *       curly braces
 *  31   Gandalf   1.30        2/14/00  Miloslav Metelka impi abbrev
 *  30   Gandalf   1.29        1/13/00  Miloslav Metelka Localization
 *  29   Gandalf   1.28        1/10/00  Miloslav Metelka 
 *  28   Gandalf   1.27        1/6/00   Miloslav Metelka 
 *  27   Gandalf   1.26        1/4/00   Miloslav Metelka 
 *  26   Gandalf   1.25        12/28/99 Miloslav Metelka 
 *  25   Gandalf   1.24        11/24/99 Miloslav Metelka 
 *  24   Gandalf   1.23        11/14/99 Miloslav Metelka 
 *  23   Gandalf   1.22        11/9/99  Miloslav Metelka 
 *  22   Gandalf   1.21        11/8/99  Miloslav Metelka 
 *  21   Gandalf   1.20        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  20   Gandalf   1.19        10/8/99  Miloslav Metelka Ctrl+U -> Alt+U
 *  19   Gandalf   1.18        10/7/99  Miloslav Metelka Added 
 *       make-getter,setter,is bindings
 *  18   Gandalf   1.17        10/1/99  Miloslav Metelka Proper java identifier 
 *       resolution
 *  17   Gandalf   1.16        9/15/99  Miloslav Metelka 
 *  16   Gandalf   1.15        9/13/99  Petr Jiricka    Registration of JSP and 
 *       properties syntax and coloring removed.
 *  15   Gandalf   1.14        9/2/99   Libor Kramolis  
 *  14   Gandalf   1.13        8/17/99  Miloslav Metelka 
 *  13   Gandalf   1.12        8/6/99   Petr Jiricka    Added JSP kit settings
 *  12   Gandalf   1.11        8/4/99   Petr Jiricka    Added settings for JSP 
 *       and properties
 *  11   Gandalf   1.10        7/29/99  Miloslav Metelka 
 *  10   Gandalf   1.9         7/28/99  Libor Kramolis  
 *  9    Gandalf   1.8         7/21/99  Miloslav Metelka 
 *  8    Gandalf   1.7         7/20/99  Miloslav Metelka 
 *  7    Gandalf   1.6         7/9/99   Miloslav Metelka 
 *  6    Gandalf   1.5         7/2/99   Miloslav Metelka 
 *  5    Gandalf   1.4         6/29/99  Miloslav Metelka Scrolling and patches
 *  4    Gandalf   1.3         6/22/99  Miloslav Metelka 
 *  3    Gandalf   1.2         6/10/99  Miloslav Metelka 
 *  2    Gandalf   1.1         6/8/99   Miloslav Metelka 
 *  1    Gandalf   1.0         6/1/99   Miloslav Metelka 
 * $
 */

