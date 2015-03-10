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

package org.netbeans.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import javax.swing.KeyStroke;
import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.undo.UndoManager;

/**
* Default settings for the editor. This class contains only createSettingsMap()
* method which is called by Settings to initialize either global
* settings map or kit specific map of settings. User can extend this class
* and use Settings.setInitializer() to override the different editor settings.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class DefaultSettings implements Settings.Initializer {

    public static final Integer INTEGER_MAX_VALUE = new Integer(Integer.MAX_VALUE);


    // Caret color
    public static final Color defaultCaretColor = Color.black;

    // Default coloring
    public static final Font defaultFont = new Font("Monospaced", Font.PLAIN, 12); // NOI18N
    public static final Color defaultForeColor = Color.black;
    public static final Color defaultBackColor = Color.white;
    public static final Coloring defaultColoring
    = new Coloring(defaultFont, defaultForeColor, defaultBackColor);
    // line number coloring
    public static final Color defaultLineNumberForeColor = new Color(128, 64, 64);
    public static final Color defaultLineNumberBackColor = new Color(224, 224, 224);
    // caret selection coloring
    public static final Color defaultSelectionForeColor = Color.white;
    public static final Color defaultSelectionBackColor = Color.lightGray;
    // Highlight search coloring
    public static final Color defaultHighlightSearchForeColor = Color.black;
    public static final Color defaultHighlightSearchBackColor = Color.yellow;
    // Incremental search coloring
    public static final Color defaultIncSearchForeColor = Color.black;
    public static final Color defaultIncSearchBackColor = Color.orange;
    // Highlight row with caret coloring
    public static final Color defaultHighlightRowBackColor = new Color(255, 255, 220);
    // Highlight matching bracket coloring
    public static final Color defaultHighlightBracketForeColor = Color.white;
    public static final Color defaultHighlightBracketBackColor = new Color(255, 50, 210);
    // Bookmark coloring
    public static final Color defaultBookmarkForeColor = Color.black;
    public static final Color defaultBookmarkBackColor = new Color(100, 200, 200);
    // Guarded blocks coloring
    public static final Color defaultGuardedForeColor = null;
    public static final Color defaultGuardedBackColor = new Color(225, 236, 247);

    public static final Integer defaultCaretBlinkRate = new Integer(300);
    public static final Integer defaultTabSize = new Integer(8);
    public static final Integer defaultSpacesPerTab = new Integer(4);
    public static final Integer defaultShiftWidth = new Integer(4); // usually
    // not used as there's a substituter for shift width

    public static final Integer defaultStatusBarCaretDelay = new Integer(200);
    public static final Color defaultStatusBarForeColor = null;
    public static final Color defaultStatusBarBackColor
    = UIManager.getColor("ScrollPane.background"); // NOI18N
    public static final Color defaultStatusBarBoldForeColor = Color.white;
    public static final Color defaultStatusBarBoldBackColor = Color.red;

    public static final Color defaultTextLimitLineColor = new Color(255, 235, 235);
    public static final Integer defaultTextLimitWidth = new Integer(80);

    public static final Acceptor defaultIdentifierAcceptor = AcceptorFactory.LETTER_DIGIT;
    public static final Acceptor defaultWhitespaceAcceptor = AcceptorFactory.WHITESPACE;

    public static final Float defaultLineHeightCorrection = new Float(1.0);
    public static final Integer defaultFindHistorySize = new Integer(30);

    public static final Insets defaultMargin = new Insets(0, 2, 0, 0);
    public static final Insets defaultLineNumberMargin = new Insets(0, 3, 0, 5);
    public static final Insets defaultScrollJumpInsets = new Insets(-5, -10, -5, -30);
    public static final Insets defaultScrollFindInsets = new Insets(0, -0, -10, -0);
    public static final Dimension defaultComponentSizeIncrement = new Dimension(-5, -30);

    public static final Integer defaultReadBufferSize = new Integer(16384);
    public static final Integer defaultWriteBufferSize = new Integer(16384);
    public static final Integer defaultReadMarkDistance = new Integer(180);
    public static final Integer defaultMarkDistance = new Integer(100);
    public static final Integer defaultMaxMarkDistance = new Integer(150);
    public static final Integer defaultMinMarkDistance = new Integer(50);
    public static final Integer defaultSyntaxUpdateBatchSize
    = new Integer(defaultMarkDistance.intValue() * 7);
    public static final Integer defaultLineBatchSize = new Integer(2);


    private List defaultKeyBindingList;

    /** Construct default settings */
    public DefaultSettings() {
    }

    /** Create map filled with all the desired settings
    * @param kitClass kit class for which the settings are being created
    *   or null when global settings are created.
    * @return map containing the desired settings or null if no settings
    *   are defined for the given kit
    */
    public Map updateSettingsMap(Class kitClass, Map m) {
        // ------------------------ BaseKit Settings --------------------------------------
        if (kitClass == BaseKit.class) {
            if (m == null) {
                m = new HashMap();
            }
            m.put(Settings.TAB_SIZE, defaultTabSize);
            m.put(Settings.EXPAND_TABS, Boolean.TRUE);
            m.put(Settings.SPACES_PER_TAB, defaultSpacesPerTab);
            m.put(Settings.CARET_TYPE_INSERT_MODE, BaseCaret.LINE_CARET);
            m.put(Settings.CARET_TYPE_OVERWRITE_MODE, BaseCaret.BLOCK_CARET);
            m.put(Settings.CARET_ITALIC_INSERT_MODE, Boolean.FALSE);
            m.put(Settings.CARET_ITALIC_OVERWRITE_MODE, Boolean.FALSE);
            m.put(Settings.CARET_COLOR_INSERT_MODE, Color.black);
            m.put(Settings.CARET_COLOR_OVERWRITE_MODE, Color.black);
            m.put(Settings.CARET_BLINK_RATE, defaultCaretBlinkRate);
            m.put(Settings.ABBREV_MAP, new HashMap(1));
            m.put(Settings.ABBREV_EXPAND_ACCEPTOR, AcceptorFactory.SPACE_NL);
            m.put(Settings.ABBREV_ADD_TYPED_CHAR_ACCEPTOR, AcceptorFactory.NL);
            m.put(Settings.ABBREV_RESET_ACCEPTOR, AcceptorFactory.NON_JAVA_IDENTIFIER);
            m.put(Settings.LINE_NUMBER_VISIBLE, Boolean.TRUE);
            m.put(Settings.PRINT_LINE_NUMBER_VISIBLE, Boolean.FALSE);
            m.put(Settings.LINE_HEIGHT_CORRECTION, defaultLineHeightCorrection);
            m.put(Settings.MARGIN, defaultMargin);
            m.put(Settings.LINE_NUMBER_MARGIN, defaultLineNumberMargin);
            m.put(Settings.SCROLL_JUMP_INSETS, defaultScrollJumpInsets);
            m.put(Settings.SCROLL_FIND_INSETS, defaultScrollFindInsets);
            m.put(Settings.COMPONENT_SIZE_INCREMENT, defaultComponentSizeIncrement);
            m.put(Settings.STATUS_BAR_VISIBLE, Boolean.TRUE);
            m.put(Settings.STATUS_BAR_CARET_DELAY, defaultStatusBarCaretDelay);
            m.put(Settings.TEXT_LIMIT_LINE_VISIBLE, Boolean.TRUE);
            m.put(Settings.TEXT_LIMIT_LINE_COLOR, defaultTextLimitLineColor);
            m.put(Settings.TEXT_LIMIT_WIDTH, defaultTextLimitWidth);
            m.put(Settings.HOME_KEY_COLUMN_ONE, Boolean.FALSE);
            m.put(Settings.WORD_MOVE_NEW_LINE_STOP, Boolean.TRUE);
            m.put(Settings.KEY_BINDING_LIST, getDefaultKeyBindingList());
            m.put(Settings.INPUT_METHODS_ENABLED, Boolean.TRUE);
            m.put(Settings.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
            m.put(Settings.FIND_INC_SEARCH, Boolean.TRUE);
            m.put(Settings.FIND_BACKWARD_SEARCH, Boolean.FALSE);
            m.put(Settings.FIND_WRAP_SEARCH, Boolean.TRUE);
            m.put(Settings.FIND_MATCH_CASE, Boolean.FALSE);
            m.put(Settings.FIND_WHOLE_WORDS, Boolean.FALSE);
            m.put(Settings.FIND_REG_EXP, Boolean.FALSE);
            m.put(Settings.FIND_HISTORY_SIZE, defaultFindHistorySize);
            m.put(Settings.WORD_MATCH_SEARCH_LEN, INTEGER_MAX_VALUE); // !!! implement
            m.put(Settings.WORD_MATCH_WRAP_SEARCH, Boolean.TRUE);
            m.put(Settings.WORD_MATCH_MATCH_ONE_CHAR, Boolean.TRUE);
            m.put(Settings.WORD_MATCH_MATCH_CASE, Boolean.FALSE);
            m.put(Settings.WORD_MATCH_SMART_CASE, Boolean.FALSE);
            m.put(Settings.IDENTIFIER_ACCEPTOR, defaultIdentifierAcceptor);
            m.put(Settings.WHITESPACE_ACCEPTOR, defaultWhitespaceAcceptor);

            // Base colorings
            SettingsUtil.setColoring(m, Settings.DEFAULT_COLORING, defaultColoring);
            SettingsUtil.setColoring(m, Settings.LINE_NUMBER_COLORING,
                                     new Coloring(null, defaultLineNumberForeColor, defaultLineNumberBackColor));
            SettingsUtil.setColoring(m, Settings.BOOKMARK_COLORING,
                                     new Coloring(null, defaultBookmarkForeColor, defaultBookmarkBackColor));
            SettingsUtil.setColoring(m, Settings.GUARDED_COLORING,
                                     new Coloring(null, defaultGuardedForeColor, defaultGuardedBackColor));
            SettingsUtil.setColoring(m, Settings.SELECTION_COLORING,
                                     new Coloring(null, defaultSelectionForeColor, defaultSelectionBackColor));
            SettingsUtil.setColoring(m, Settings.HIGHLIGHT_SEARCH_COLORING,
                                     new Coloring(null, defaultHighlightSearchForeColor, defaultHighlightSearchBackColor));
            SettingsUtil.setColoring(m, Settings.INC_SEARCH_COLORING,
                                     new Coloring(null, defaultIncSearchForeColor, defaultIncSearchBackColor));
            SettingsUtil.setColoring(m, Settings.HIGHLIGHT_ROW_COLORING,
                                     new Coloring(null, null, defaultHighlightRowBackColor));
            SettingsUtil.setColoring(m, Settings.HIGHLIGHT_BRACKET_COLORING,
                                     new Coloring(null, defaultHighlightBracketForeColor, defaultHighlightBracketBackColor));
            SettingsUtil.setColoring(m, Settings.STATUS_BAR_COLORING,
                                     new Coloring(null, defaultStatusBarForeColor, defaultStatusBarBackColor));
            SettingsUtil.setColoring(m, Settings.STATUS_BAR_BOLD_COLORING,
                                     new Coloring(null, defaultStatusBarBoldForeColor, defaultStatusBarBoldBackColor));

            // Common token colorings
            Font boldFont = defaultFont.deriveFont(Font.BOLD);
            Font italicFont = defaultFont.deriveFont(Font.ITALIC);
            SettingsUtil.PrintColoringSubstituter lightGraySubstituter
            = new SettingsUtil.ForeColorPrintColoringSubstituter(Color.lightGray);
            SettingsUtil.setColoring(m, Syntax.TN_TEXT, new Coloring(null, null, null));
            SettingsUtil.setColoring(m, Syntax.TN_ERROR, new Coloring(null, Color.white, Color.red));
            SettingsUtil.setColoring(m, Syntax.TN_KEYWORD, new Coloring(boldFont, Color.blue, null));
            SettingsUtil.setColoring(m, Syntax.TN_IDENTIFIER, new Coloring(null, null, null));
            SettingsUtil.setColoring(m, Syntax.TN_FUNCTION, new Coloring(boldFont, null, null),
                                     SettingsUtil.italicFontPrintColoringSubstituter);
            SettingsUtil.setColoring(m, Syntax.TN_OPERATOR, new Coloring(null, null, null));
            SettingsUtil.setColoring(m, Syntax.TN_LINE_COMMENT, new Coloring(italicFont, Color.gray, null),
                                     lightGraySubstituter);
            SettingsUtil.setColoring(m, Syntax.TN_BLOCK_COMMENT, new Coloring(italicFont, Color.gray, null),
                                     lightGraySubstituter);
            SettingsUtil.setColoring(m, Syntax.TN_CHAR, new Coloring(null, Color.green.darker(), null));
            SettingsUtil.setColoring(m, Syntax.TN_STRING, new Coloring(null, Color.magenta, null));
            SettingsUtil.setColoring(m, Syntax.TN_INT, new Coloring(null, Color.red, null));
            SettingsUtil.setColoring(m, Syntax.TN_HEX, new Coloring(null, Color.red, null));
            SettingsUtil.setColoring(m, Syntax.TN_OCTAL, new Coloring(null, Color.red, null));
            SettingsUtil.setColoring(m, Syntax.TN_LONG, new Coloring(null, Color.red, null));
            SettingsUtil.setColoring(m, Syntax.TN_FLOAT, new Coloring(null, Color.red, null));
            SettingsUtil.setColoring(m, Syntax.TN_DOUBLE, new Coloring(null, Color.red, null));

            SettingsUtil.setColoring(m, Syntax.TN_LINE_COMMENT, lightGraySubstituter, true);
            SettingsUtil.setColoring(m, Syntax.TN_BLOCK_COMMENT, lightGraySubstituter, true);
            SettingsUtil.setColoring(m, Syntax.TN_FUNCTION,
                                     SettingsUtil.italicFontPrintColoringSubstituter, true);

            // List of the colorings for all the kits
            SettingsUtil.updateListSetting(m, Settings.COLORING_NAME_LIST,
                                           new String[] {
                                               Settings.DEFAULT_COLORING,
                                               Settings.LINE_NUMBER_COLORING,
                                               Settings.GUARDED_COLORING,
                                               Settings.SELECTION_COLORING,
                                               Settings.HIGHLIGHT_SEARCH_COLORING,
                                               Settings.INC_SEARCH_COLORING,
                                               Settings.HIGHLIGHT_ROW_COLORING,
                                               Settings.HIGHLIGHT_BRACKET_COLORING,
                                               Settings.BOOKMARK_COLORING,
                                               Settings.STATUS_BAR_COLORING,
                                               Settings.STATUS_BAR_BOLD_COLORING
                                           }
                                          );


            // Uniform font exclusions
            List uniFontExclusionList = new ArrayList();
            uniFontExclusionList.add(Settings.STATUS_BAR_COLORING);
            uniFontExclusionList.add(Settings.STATUS_BAR_BOLD_COLORING);
            m.put(Settings.UNIFORM_FONT_EXCLUSION_LIST, uniFontExclusionList);

            m.put(Settings.INDENT_SHIFT_WIDTH, // substitute
                  new Settings.Substituter() {
                      public Object getValue(Class kitClass2, String settingName) {
                          Object ret;
                          if (SettingsUtil.getBoolean(kitClass2, Settings.EXPAND_TABS, false)) {
                              ret = Settings.getValue(kitClass2, Settings.SPACES_PER_TAB);
                          } else { // don't expand tabs
                              ret = Settings.getValue(kitClass2, Settings.TAB_SIZE);
                          }
                          return ret;
                      }
                  }
                 );
            m.put(Settings.INDENT_HOT_CHAR_ACCEPTOR, AcceptorFactory.FALSE);

            /* WARNING!
            * Change the following settings with care as there are some dependencies
            * among the values of these settings. If the values are wrong
            * the editor may work in a wrong way in some circumstances.
            * If changing these values, run EditorDebug.checkSettings(kitClass)
            * in some DOC_INSTALL_ACTION_LIST action to ensure that
            * the changed values are correct.
            */
            m.put(Settings.READ_BUFFER_SIZE, defaultReadBufferSize);
            m.put(Settings.WRITE_BUFFER_SIZE, defaultWriteBufferSize);
            m.put(Settings.READ_MARK_DISTANCE, defaultReadMarkDistance);
            m.put(Settings.MARK_DISTANCE, defaultMarkDistance);
            m.put(Settings.MAX_MARK_DISTANCE, defaultMaxMarkDistance);
            m.put(Settings.MIN_MARK_DISTANCE, defaultMinMarkDistance);
            m.put(Settings.SYNTAX_UPDATE_BATCH_SIZE, defaultSyntaxUpdateBatchSize);
            m.put(Settings.LINE_BATCH_SIZE, defaultLineBatchSize);

        }

        return m; // Settings for other kits are not affected
    }

    /** Helper method to create default key bindings */
    public List getDefaultKeyBindingList() {
        if (defaultKeyBindingList == null) {
            JTextComponent.KeyBinding kb[] = new JTextComponent.KeyBinding[] {
                                                 new MultiKeyBinding(
                                                     (KeyStroke)null, // this assigns the default action to keymap
                                                     BaseKit.defaultKeyTypedAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                                                     BaseKit.insertBreakAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
                                                     BaseKit.insertTabAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK),
                                                     BaseKit.removeTabAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
                                                     BaseKit.deletePrevCharAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK),
                                                     BaseKit.deletePrevCharAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                                                     BaseKit.deleteNextCharAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
                                                     BaseKit.forwardAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK),
                                                     BaseKit.selectionForwardAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK),
                                                     BaseKit.nextWordAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK),
                                                     BaseKit.selectionNextWordAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
                                                     BaseKit.backwardAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK),
                                                     BaseKit.selectionBackwardAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK),
                                                     BaseKit.previousWordAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK),
                                                     BaseKit.selectionPreviousWordAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                                                     BaseKit.downAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_MASK),
                                                     BaseKit.selectionDownAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK),
                                                     BaseKit.scrollUpAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
                                                     BaseKit.upAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_MASK),
                                                     BaseKit.selectionUpAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK),
                                                     BaseKit.scrollDownAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
                                                     BaseKit.pageDownAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.SHIFT_MASK),
                                                     BaseKit.selectionPageDownAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
                                                     BaseKit.pageUpAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.SHIFT_MASK),
                                                     BaseKit.selectionPageUpAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
                                                     BaseKit.beginLineAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.SHIFT_MASK),
                                                     BaseKit.selectionBeginLineAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_MASK),
                                                     BaseKit.beginAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK),
                                                     BaseKit.selectionBeginAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
                                                     BaseKit.endLineAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.SHIFT_MASK),
                                                     BaseKit.selectionEndLineAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_MASK),
                                                     BaseKit.endAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK),
                                                     BaseKit.selectionEndAction
                                                 ),

                                                 // clipboard bindings
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK),
                                                     BaseKit.copyAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK),
                                                     BaseKit.cutAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK),
                                                     BaseKit.pasteAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_MASK),
                                                     BaseKit.copyAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_MASK),
                                                     BaseKit.cutAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_MASK),
                                                     BaseKit.pasteAction
                                                 ),

                                                 // undo and redo bindings
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK),
                                                     BaseKit.undoAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK),
                                                     BaseKit.redoAction
                                                 ),

                                                 // other bindings
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK),
                                                     BaseKit.selectAllAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_MASK),
                                                     BaseKit.endWordAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK),
                                                     BaseKit.removeWordAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK),
                                                     BaseKit.removeLineBeginAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK),
                                                     BaseKit.removeLineAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0),
                                                     BaseKit.toggleTypingModeAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.CTRL_MASK),
                                                     BaseKit.toggleBookmarkAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                                                     BaseKit.gotoNextBookmarkAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
                                                     BaseKit.findNextAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_MASK),
                                                     BaseKit.findPreviousAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.CTRL_MASK),
                                                     BaseKit.findSelectionAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK),
                                                     BaseKit.toggleHighlightSearchAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK),
                                                     BaseKit.wordMatchNextAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK),
                                                     BaseKit.wordMatchPrevAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK),
                                                     BaseKit.bracketMatchAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
                                                     BaseKit.selectionBracketMatchAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK),
                                                     BaseKit.shiftLineRightAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK),
                                                     BaseKit.shiftLineLeftAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.SHIFT_MASK),
                                                     BaseKit.abbrevResetAction
                                                 ),

                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.ALT_MASK),
                                                     BaseKit.adjustWindowTopAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_MASK),
                                                     BaseKit.adjustWindowCenterAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.ALT_MASK),
                                                     BaseKit.adjustWindowBottomAction
                                                 ),

                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK),
                                                     BaseKit.adjustCaretTopAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK),
                                                     BaseKit.adjustCaretCenterAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK),
                                                     BaseKit.adjustCaretBottomAction
                                                 ),

                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.ALT_MASK),
                                                     BaseKit.formatAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.ALT_MASK),
                                                     BaseKit.selectIdentifierAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_MASK),
                                                     BaseKit.jumpListPrevAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_MASK),
                                                     BaseKit.jumpListNextAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK),
                                                     BaseKit.jumpListPrevComponentAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK),
                                                     BaseKit.jumpListNextComponentAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     new KeyStroke[] {
                                                         KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
                                                         KeyStroke.getKeyStroke(KeyEvent.VK_U, 0),
                                                     },
                                                     BaseKit.toUpperCaseAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     new KeyStroke[] {
                                                         KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
                                                         KeyStroke.getKeyStroke(KeyEvent.VK_L, 0),
                                                     },
                                                     BaseKit.toLowerCaseAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     new KeyStroke[] {
                                                         KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
                                                         KeyStroke.getKeyStroke(KeyEvent.VK_R, 0),
                                                     },
                                                     BaseKit.switchCaseAction
                                                 ),
                                                 new MultiKeyBinding(
                                                     new KeyStroke[] {
                                                         KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
                                                         KeyStroke.getKeyStroke(KeyEvent.VK_F, 0),
                                                     },
                                                     BaseKit.toggleCaseIdentifierBeginAction
                                                 ),
                                                 /*      new MultiKeyBinding(
                                                           KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK),
                                                 //          KeyStroke.getKeyStroke(KeyEvent.VK_BRACELEFT, InputEvent.CTRL_MASK),
                                                           BaseKit.braceCodeSelectAction
                                                       ),
                                                 */

                                                 new MultiKeyBinding(
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK),
                                                     BaseKit.selectNextParameterAction
                                                 ),

                                                 /*      new MultiKeyBinding(
                                                           new KeyStroke[] {
                                                             KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK)
                                                           },
                                                           BaseKit.toUpperCaseAction
                                                       ),
                                                 */


                                                 // self test debugging key bindings
                                                 /*      new MultiKeyBinding(
                                                           KeyStroke.getKeyStroke(KeyEvent.VK_F11, InputEvent.CTRL_MASK),
                                                           BaseKit.dumpPlanesAction
                                                       ),
                                                       new MultiKeyBinding(
                                                           KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.CTRL_MASK),
                                                           BaseKit.dumpSyntaxMarksAction
                                                       ),
                                                       new MultiKeyBinding(
                                                           KeyStroke.getKeyStroke(KeyEvent.VK_F12, InputEvent.CTRL_MASK),
                                                           BaseKit.selfTestAction
                                                       )
                                                 */

                                             };
            defaultKeyBindingList = new ArrayList(Arrays.asList(kb));
        }
        return defaultKeyBindingList;
    }

}

/*
 * Log
 *  38   Gandalf-post-FCS1.35.1.1    4/6/00   Miloslav Metelka undo action
 *  37   Gandalf-post-FCS1.35.1.0    4/3/00   Miloslav Metelka undo update
 *  36   Gandalf   1.35        1/14/00  Miloslav Metelka Ctrl+Up/Down swapped
 *  35   Gandalf   1.34        1/13/00  Miloslav Metelka 
 *  34   Gandalf   1.33        1/11/00  Miloslav Metelka 
 *  33   Gandalf   1.32        1/10/00  Miloslav Metelka 
 *  32   Gandalf   1.31        1/7/00   Miloslav Metelka 
 *  31   Gandalf   1.30        1/4/00   Miloslav Metelka 
 *  30   Gandalf   1.29        12/28/99 Miloslav Metelka 
 *  29   Gandalf   1.28        11/14/99 Miloslav Metelka 
 *  28   Gandalf   1.27        11/10/99 Miloslav Metelka 
 *  27   Gandalf   1.26        11/8/99  Miloslav Metelka 
 *  26   Gandalf   1.25        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  25   Gandalf   1.24        10/10/99 Miloslav Metelka 
 *  24   Gandalf   1.23        10/7/99  Miloslav Metelka removed Ctrl+U binding
 *  23   Gandalf   1.22        9/30/99  Miloslav Metelka 
 *  22   Gandalf   1.21        9/15/99  Miloslav Metelka 
 *  21   Gandalf   1.20        9/10/99  Miloslav Metelka 
 *  20   Gandalf   1.19        8/27/99  Miloslav Metelka 
 *  19   Gandalf   1.18        8/19/99  Miloslav Metelka 
 *  18   Gandalf   1.17        8/17/99  Miloslav Metelka 
 *  17   Gandalf   1.16        7/29/99  Miloslav Metelka 
 *  16   Gandalf   1.15        7/26/99  Miloslav Metelka 
 *  15   Gandalf   1.14        7/21/99  Miloslav Metelka 
 *  14   Gandalf   1.13        7/20/99  Miloslav Metelka 
 *  13   Gandalf   1.12        7/9/99   Miloslav Metelka 
 *  12   Gandalf   1.11        7/2/99   Miloslav Metelka 
 *  11   Gandalf   1.10        6/29/99  Miloslav Metelka Scrolling and patches
 *  10   Gandalf   1.9         6/22/99  Miloslav Metelka 
 *  9    Gandalf   1.8         6/10/99  Miloslav Metelka 
 *  8    Gandalf   1.7         6/8/99   Miloslav Metelka 
 *  7    Gandalf   1.6         6/1/99   Miloslav Metelka 
 *  6    Gandalf   1.5         5/24/99  Miloslav Metelka 
 *  5    Gandalf   1.4         5/15/99  Miloslav Metelka fixes
 *  4    Gandalf   1.3         5/13/99  Miloslav Metelka 
 *  3    Gandalf   1.2         5/7/99   Miloslav Metelka line numbering and fixes
 *  2    Gandalf   1.1         5/5/99   Miloslav Metelka 
 *  1    Gandalf   1.0         4/23/99  Miloslav Metelka 
 * $
 */

