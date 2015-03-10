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

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.text.JTextComponent;

/**
* Configurable settings that editor uses. All the methods are static
* The editor is configurable mainly by using the following static
* method in Settings class:
* 
*   org.netbeans.editor.Settings.setValue(Class kitClass, String settingName, Object newValue);
* 
* kitClass - this is the class of the editor kit for which the setting is changed.
*   The current hierarchy of editor kits is as follows:
*     <tt>org.netbeans.editor.BaseKit</tt> - this is the base kit, the begining of the whole
*       kit hierarchy.
*     <tt>org.netbeans.editor.ext.PlainKit</tt> - this is the kit that extends the BaseKit
*       and it's used for plain text editing
*     <tt>org.netbeans.editor.ext.JavaKit</tt> - this kit also extends the BaseKit
*       and it's used for java files editing
* 
*   When the particular setting is not set for a given kit, then the superclass of
*   the given kit class is retrieved and the search for the setting value is performed.
*   Example: If the java document calls Settings.getValue() to retrieve the value
*   for TAB_SIZE setting and it passes JavaKit.class as the kitClass
*   parameter and the setting has no value on this level, then the super class
*   of the JavaKit is retrieved (by using Class.getSuperclass() call) which is BaseKit
*   in this case and the search for the value of TAB_SIZE setting
*   is performed again. It is finished by reaching the null value for the kitClass.
*   The null value can be also used as the kitClass parameter value.
*   In a more general look not only the kit-class hierarchy could be used
*   in <tt>Settings</tt>. Any class inheritance hierarchy could be used here
*   having the null as the common root.
*
*   This way the inheritance of the setting values is guaranteed. By changing
*   the setting value on the BaseKit level (or even on the null level),
*   all the kit classes that don't
*   override the particular setting are affected.
* 
* settingName - name of the setting to change. All the currently available
*   setting names are defined as public String constants in Settings class.
* 
* newValue - new value for the setting. It must be always an object even
*   if the setting is logicaly the basic datatype such as int (java.lang.Integer
*   would be used in this case). A particular class types that can be used for
*   the value of the settings are documented for each setting.
*
* WARNING! Please read carefully the description for each option you're
*   going to change as you can make the editor stop working if you'll
*   change the setting in a wrong way.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class Settings {

    /** Tab size for the document.
    * Values: java.lang.Integer instances
    */
    public static final String TAB_SIZE = "tab-size"; // NOI18N

    /** Whether do tab to spaces expansion. The number of spaces to substitute
    * per one tab is determined by SPACES_PER_TAB setting.
    * Values: java.lang.Boolean instances
    */
    public static final String EXPAND_TABS = "expand-tabs"; // NOI18N

    /** How many spaces substitute per one typed tab. This parameter has
    * effect only when EXPAND_TABS setting is set to true.
    * This parameter has no influence on how
    * the existing tabs are displayed.
    * Values: java.lang.Integer instances
    */
    public static final String SPACES_PER_TAB = "spaces-per-tab"; // NOI18N

    /** Acceptor that recognizes the identifier characters.
    * If set it's used instead of the default Syntax.isIdentifierPart() call.
    * Values: org.netbeans.editor.Acceptor instances
    */
    public static final String IDENTIFIER_ACCEPTOR = "identifier-acceptor"; // NOI18N

    /** Acceptor that recognizes the whitespace characters.
    * If set it's used instead of the default Syntax.isWhitespace() call.
    * Values: org.netbeans.editor.Acceptor instances
    */
    public static final String WHITESPACE_ACCEPTOR = "whitespace-acceptor"; // NOI18N

    /** Map of the string abbreviations. The second string (value) means
    * the full version of the first string (key).
    * Values: java.util.Map instances holding
    *      [abbrev-string, expanded-abbrev-string] pairs
    */
    public static final String ABBREV_MAP = "abbrev-map"; // NOI18N

    /** Map of the action abbreviations. The second string (value) is
    * the name of the action to execute. The action must be available
    * in the kit actions. It can be added through <tt>CUSTOM_ACTION_LIST</tt>.
    * The original abbreviation string
    * is first removed from the text before the action is executed.
    * If there is the same abbreviation in the <tt>ABBREV_MAP</tt> map
    * it has a precedence over the <tt>ABBREV_ACTION_MAP</tt>.
    * Values: java.util.Map instances holding
    *   [abbrev-string, name-of-the-action-to-execute] pairs
    */
    public static final String ABBREV_ACTION_MAP = "abbrev-action-map"; // NOI18N

    /** Acceptor checking whether abbreviation should be attempted
    * after the appropriate character was typed.
    * Values: org.netbeans.editor.Acceptor instances
    */
    public static final String ABBREV_EXPAND_ACCEPTOR = "abbrev-expand-acceptor"; // NOI18N

    /** Acceptor checking whether typed character that performed
    * abbreviation expansion should be added to the text or not.
    * Values: org.netbeans.editor.Acceptor instances
    */
    public static final String ABBREV_ADD_TYPED_CHAR_ACCEPTOR
    = "abbrev-add-typed-char-acceptor"; // NOI18N

    /** Acceptor checking whether typed character should reset
    * abbreviation accounting. By default all non-letterOrDigit chars
    * reset the abbreviation accounting.
    * Values: org.netbeans.editor.Acceptor instances
    */
    public static final String ABBREV_RESET_ACCEPTOR = "abbrev-reset-acceptor"; // NOI18N

    /** Type of caret for insert mode.
    * Values: java.lang.String instances
    *   Currently supported types are:
    *     org.netbeans.editor.BaseCaret.LINE_CARET - default 2point caret
    *     org.netbeans.editor.BaseCaret.THIN_LINE_CARET - swing like thin caret
    *     org.netbeans.editor.BaseCaret.BLOCK_CARET - block covering whole character
    */
    public static final String CARET_TYPE_INSERT_MODE = "caret-type-insert-mode"; // NOI18N

    /** Type of caret for over write mode.
    * Values: java.lang.String instances
    *   Currently supported types are:
    *     org.netbeans.editor.BaseCaret.LINE_CARET - default 2point caret
    *     org.netbeans.editor.BaseCaret.THIN_LINE_CARET - swing like thin caret
    *     org.netbeans.editor.BaseCaret.BLOCK_CARET - block covering whole character
    */
    public static final String CARET_TYPE_OVERWRITE_MODE = "caret-type-overwrite-mode"; // NOI18N

    /** Will the insert mode caret be italicized if the underlying font
    * is italic?
    * Values: java.lang.Boolean instances
    */
    public static final String CARET_ITALIC_INSERT_MODE = "caret-italic-insert-mode"; // NOI18N

    /** Will the overwrite mode caret be italicized if the underlying font
    * is italic?
    * Values: java.lang.Boolean instances
    */
    public static final String CARET_ITALIC_OVERWRITE_MODE = "caret-italic-overwrite-mode"; // NOI18N

    /** Caret color for insert mode.
    * Values: java.awt.Color instances
    */
    public static final String CARET_COLOR_INSERT_MODE = "caret-color-insert-mode"; // NOI18N

    /** Caret color for overwrite mode.
    * Values: java.awt.Color instances
    */
    public static final String CARET_COLOR_OVERWRITE_MODE = "caret-color-overwrite-mode"; // NOI18N

    /** Caret blink rate in milliseconds.
    * Values: java.lang.Integer
    */
    public static final String CARET_BLINK_RATE = "caret-blink-rate"; // NOI18N

    /** Whether to display line numbers on the left part of the screen.
    * Values: java.lang.Boolean instances
    */
    public static final String LINE_NUMBER_VISIBLE = "line-number-visible"; // NOI18N

    /** Whether to display line numbers when printing to the printer.
    * Values: java.lang.Boolean instances
    */
    public static final String PRINT_LINE_NUMBER_VISIBLE = "print-line-number-visible"; // NOI18N

    /** How much should the view jump when scrolling goes off the screen.
    * Insets are used so that it can be specified for each direction specifically.
    * Each inset value can be positive or negative. The positive value means
    * the number of lines for the top and the bottom and the number of characters
    * for the left and the right. The negative value means percentage of the editor
    * component height for the top and the bottom and percentage of the editor
    * component width for the left and the right.
    * Values: java.awt.Insets instances
    */
    public static final String SCROLL_JUMP_INSETS = "scroll-jump-insets"; // NOI18N

    /** How much space must be reserved in each direction for the find operation.
    * It's here to ensure the found information will be visible in some
    * context around it.
    * Insets are used so that it can be specified for each direction specifically.
    * Each inset value can be positive or negative. The positive value means
    * the number of lines for the top and the bottom and the number of characters
    * for the left and the right. The negative value means percentage of the editor
    * component height for the top and the bottom and percentage of the editor
    * component width for the left and the right.
    * Values: java.awt.Insets instances
    */
    public static final String SCROLL_FIND_INSETS = "scroll-find-insets"; // NOI18N

    /** How much space will be added additionaly when the component needs to be
    * resized.
    * Each dimension value can be positive or negative. The positive value means
    * the number of lines for the height and the number of characters
    * for the width. The negative value means percentage of the editor
    * component height for the height and percentage of the editor
    * component width for the width.
    * Values: java.awt.Dimension instances
    */
    public static final String COMPONENT_SIZE_INCREMENT = "component-size-increment"; // NOI18N

    /** Margin for the editor component
    * Values: java.awt.Insets instances
    */
    public static final String MARGIN = "margin"; // NOI18N

    /** Margin on the left and right side of the line number.
    * It's used only when line numbers are visible. The top and bottom values
    * are ignored.
    * Values: java.awt.Insets instances
    */
    public static final String LINE_NUMBER_MARGIN = "line-number-margin"; // NOI18N

    /** Rendering hints to be used for the painting.
    * Values: java.util.Map instances
    */
    public static final String RENDERING_HINTS = "rendering-hints"; // NOI18N

    /** Key binding list for particular kit.
    * Values: java.util.List instances holding
    *   javax.swing.JTextComponent.KeyBinding instances
    *   or org.netbeans.editor.MultiKeyBinding instances
    */
    public static final String KEY_BINDING_LIST = "key-bindings"; // NOI18N

    /** Whether the input-methods should be enabled.
    * Values: java.lang.Boolean
    */
    public static final String INPUT_METHODS_ENABLED = "input-methods-enabled"; // NOI18N

    /** Float constant by which the height of the character obtained from
    * the font is multiplied. It defaults to 1.
    * Values: java.lang.Float instances
    */
    public static final String LINE_HEIGHT_CORRECTION = "line-height-correction"; // NOI18N

    /* Find properties.
    * They are read by FindSupport when its instance is being initialized.
    * FIND_WHAT: java.lang.String - search expression
    * FIND_REPLACE_BY: java.lang.String - replace string
    * FIND_HIGHLIGHT_SEARCH: java.lang.Boolean - highlight matching strings in text
    * FIND_INC_SEARCH: java.lang.Boolean - show matching strings immediately
    * FIND_BACKWARD_SEARCH: java.lang.Boolean - search in backward direction
    * FIND_WRAP_SEARCH: java.lang.Boolean - if end of doc reached, start from begin
    * FIND_MATCH_CASE: java.lang.Boolean - match case of letters
    * FIND_SMART_CASE: java.lang.Boolean - case insensitive search if FIND_MATCH_CASE
    *   is false and all letters of FIND_WHAT are small, case sensitive otherwise
    * FIND_WHOLE_WORDS: java.lang.Boolean - match only whole words
    * FIND_REG_EXP: java.lang.Boolean - use regular expressions in search expr
    * FIND_HISTORY: java.util.List - History of search expressions
    * FIND_HISTORY_SIZE: java.lang.Integer - Maximum size of the history
    */
    public static final String FIND_WHAT = "find-what"; // NOI18N
    public static final String FIND_REPLACE_WITH = "find-replace-with"; // NOI18N
    public static final String FIND_HIGHLIGHT_SEARCH = "find-highlight-search"; // NOI18N
    public static final String FIND_INC_SEARCH = "find-inc-search"; // NOI18N
    public static final String FIND_INC_SEARCH_DELAY = "find-inc-search-delay"; // NOI18N
    public static final String FIND_BACKWARD_SEARCH = "find-backward-search"; // NOI18N
    public static final String FIND_WRAP_SEARCH = "find-wrap-search"; // NOI18N
    public static final String FIND_MATCH_CASE = "find-match-case"; // NOI18N
    public static final String FIND_SMART_CASE = "find-smart-case"; // NOI18N
    public static final String FIND_WHOLE_WORDS = "find-whole-words"; // NOI18N
    public static final String FIND_REG_EXP = "find-reg-exp"; // NOI18N
    public static final String FIND_HISTORY = "find-history"; // NOI18N
    public static final String FIND_HISTORY_SIZE = "find-history-size"; // NOI18N


    /** Number of characters that can be searched. If the value is larger
    * than the document size, the document is used but the next document
    * will not be used. The zero value disables the word match completely.
    * Specify Integer.MAX_VALUE to search all the documents regardless
    * of the size.
    * Values: java.lang.Integer instances
    */
    public static final String WORD_MATCH_SEARCH_LEN = "word-match-search-len"; // NOI18N

    /** Wrap the word match searching
    * on current document after it reaches the end/begining of
    * current document. All the other documents except the current (first) one
    * are searched from begining in forward direction.
    * Values: java.lang.Boolean instances
    */
    public static final String WORD_MATCH_WRAP_SEARCH = "word-match-wrap-search"; // NOI18N

    /** Word list that is searched as last resort in word matching.
    * It can contain the words that are used often by the user.
    * If this property is set, these words are searched regardless
    * of WORD_MATCH_SEARCH_LEN setting.
    * Values: java.lang.String instances
    */
    public static final String WORD_MATCH_STATIC_WORDS = "word-match-static-words"; // NOI18N

    /** Whether to use case sensitive search or not.
    * Values: java.lang.Boolean instances
    */
    public static final String WORD_MATCH_MATCH_CASE = "word-match-match-case"; // NOI18N

    /** Whether to use case insensitive search if all the letters are small
    * and case sensitive search if at least one letter is capital.
    * Values: java.lang.Boolean instances
    */
    public static final String WORD_MATCH_SMART_CASE = "word-match-smart-case"; // NOI18N

    /** Whether the word matching should return the match even if the matching
    * word has only one char. The WORD_MATCH_MATCH_CASE setting is ignored
    * in case this setting is on.
    * Values: java.lang.Boolean instances
    */
    public static final String WORD_MATCH_MATCH_ONE_CHAR = "word-match-match-one-char"; // NOI18N

    /** List of actions that will be added to the standard list of actions
    * for the particular kit. Using this mechanism, user can add actions
    * and possibly map them to the keys without overriding kit classes.
    * NOTICE!: This option has INCREMENTAL HANDLING, i.e. current kit list but also 
    * all the super kit lists are used. For example if there is a list of actions
    * both for JavaKit and BaseKit classes, both list of actions will be added
    * and BaseKit actions will be added first.
    * Values: java.util.List instances
    */
    public static final String CUSTOM_ACTION_LIST = "custom-action-list"; // NOI18N

    /** List of actions which is executed when
    * editor kit is installed into component. Actions are executed one by one
    * in the order they occur in the list.
    * At the time the kit is installed, the document is not yet assigned.
    * To perform some actions on document, use the DOC_INSTALL_ACTION_LIST.
    * NOTICE!: This option has INCREMENTAL HANDLING, i.e. current kit list but also 
    * all the super kit lists are used. For example if there is a list of actions
    * both for JavaKit and BaseKit classes, both list of actions will be executed
    * and JavaKit actions will be executed first.
    * Values: java.util.List instances
    */
    public static final String KIT_INSTALL_ACTION_NAME_LIST = "kit-install-action-name-list"; // NOI18N

    /** List of actions that are executed when
    * editor kit is being removed from component. Actions are executed one by one
    * in the order they occur in the list.
    * NOTICE!: This option has INCREMENTAL HANDLING, i.e. current kit list but also 
    * all the super kit lists are used. For example if there is a list of actions
    * both for JavaKit and BaseKit classes, both list of actions will be executed
    * and JavaKit actions will be executed first.
    * Values: java.util.List instances
    */
    public static final String KIT_DEINSTALL_ACTION_NAME_LIST = "kit-deinstall-action-name-list"; // NOI18N

    /** List of actions which is executed when
    * the new document is installed into component. Actions are executed one by one
    * in the order they occur in the list.
    * NOTICE!: This option has INCREMENTAL HANDLING, i.e. current kit list but also 
    * all the super kit lists are used. For example if there is a list of actions
    * both for JavaKit and BaseKit classes, both list of actions will be executed
    * and JavaKit actions will be executed first.
    * Values: java.util.List instances
    */
    public static final String DOC_INSTALL_ACTION_NAME_LIST = "doc-install-action-name-list"; // NOI18N

    /** List of the action names that should be shown in the popup menu.
    * Null name means separator.
    * Values: java.util.List containing java.lang.String instances
    */
    public static final String POPUP_MENU_ACTION_NAME_LIST = "popup-menu-action-name-list"; // NOI18N

    /** Whether status bar should be visible or not.
    * Values: java.lang.Boolean instances
    */
    public static final String STATUS_BAR_VISIBLE = "status-bar-visible"; // NOI18N

    /** Delay for updating information about caret in the status bar.
    * Values: java.lang.Integer instances
    */
    public static final String STATUS_BAR_CARET_DELAY = "status-bar-caret-delay"; // NOI18N

    /** Whether the line displaying the text limit should be displayed.
    * Values: java.lang.Boolean instances
    */
    public static final String TEXT_LIMIT_LINE_VISIBLE = "text-limit-line-visible"; // NOI18N

    /** Which color should be used for the line showing the text limit.
    * Values: java.awt.Color instances
    */
    public static final String TEXT_LIMIT_LINE_COLOR = "text-limit-line-color"; // NOI18N

    /** After how many characters the text limit line should be displayed.
    * Values: java.awt.Integer instances
    */
    public static final String TEXT_LIMIT_WIDTH = "text-limit-width"; // NOI18N

    /** Whether the home key should go to column 1 or first go to text start
    * on the given line and then to the column 1.
    * Values: java.lang.Boolean
    */
    public static final String HOME_KEY_COLUMN_ONE = "home-key-column-one"; // NOI18N

    /** Finder for finding the next word. If it's not set,
    * the <tt>FinderFactory.NextWordFwdFinder</tt> is used.
    * Values: org.netbeans.editor.Finder
    */
    public static final String NEXT_WORD_FINDER = "next-word-finder"; // NOI18N

    /** Finder for finding the previous word. If it's not set,
    * the <tt>FinderFactory.WordStartBwdFinder</tt> is used.
    * Values: org.netbeans.editor.Finder
    */
    public static final String PREVIOUS_WORD_FINDER = "previous-word-finder"; // NOI18N

    /** Whether the word move should stop on the '\n' character. This setting
    * affects both the 
    * Values: java.lang.Boolean
    */
    public static final String WORD_MOVE_NEW_LINE_STOP = "word-move-new-line-stop"; // NOI18N

    /** Indentation and text formatting. User can create its own formatter
    * by subclassing Formatter.
    * Values: org.netbeans.editor.Formatter instances
    */
    public static final String FORMATTER = "formatter"; // NOI18N

    /** Hot characters after which the line should be re-tested for indentation
    * such as '}' or ':'.
    * Values: org.netbeans.editor.Acceptor instances
    */
    public static final String INDENT_HOT_CHAR_ACCEPTOR = "indent-hot-char"; // NOI18N

    /** Shift-width says how many spaces should indentation use
    * to delimit next level of code. This setting is independent of TAB_SIZE.
    * Values: java.lang.Integer instances
    */
    public static final String INDENT_SHIFT_WIDTH = "indent-shift-width"; // NOI18N

    /** Whether to trim the white space characters (except '\n') from
    * the end of the line.
    * Values: java.lang.Boolean instances
    */
    //  public static final String TRIM_SPACES = "trim-spaces"; // NOI18N

    /** Buffer size for reading into the document from input stream or reader.
    * Values: java.lang.Integer
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String READ_BUFFER_SIZE = "read-buffer-size"; // NOI18N

    /** Buffer size for writing from the document to output stream or writer.
    * Values: java.lang.Integer instances
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String WRITE_BUFFER_SIZE = "write-buffer-size"; // NOI18N

    /** Read mark distance is used when performing initial read
    * of the document. It denotes the distance in chars of two adjacent
    * syntax marks inserted into the document.
    * Values: java.lang.Integer instances
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String READ_MARK_DISTANCE = "read-mark-distance"; // NOI18N

    /** Implicit mark distance for inserting to the document.
    * If the insert is made then the distance between nearest syntax
    * marks around insertion point is checked and if it's greater
    * than the max mark distance then another mark(s) are inserted
    * automatically with the distance given by this setting.
    * Values: java.lang.Integer instances instances
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String MARK_DISTANCE = "mark-distance"; // NOI18N

    /** Maximum mark distance. When there is an insertion done in document
    * and the distance between marks gets greater than this setting, another
    * mark will be inserted automatically.
    * Values: java.lang.Integer instances
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String MAX_MARK_DISTANCE = "max-mark-distance"; // NOI18N

    /** Minimum mark distance for removals. When there is a removal done
    * in document and it makes the marks to get closer than this value, then
    * the marks the additional marks that are closer than the distance
    * given by this setting are removed automatically.
    * Values: java.lang.Integer instances
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String MIN_MARK_DISTANCE = "min-mark-distance"; // NOI18N

    /** Size of one batch of characters loaded into syntax segment
    * when updating syntax marks. It prevents checking and loading
    * of syntax segment at every syntax mark. Instead it loads
    * at least the amount of characters given by this setting.
    * This whole process is done only in case the changes in syntax
    * extend the end of current line. If the syntax changes don't
    * extend to the next line, this setting has no effect.
    * Values: java.lang.Integer instances
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String SYNTAX_UPDATE_BATCH_SIZE = "syntax-update-batch-size"; // NOI18N

    /** How many lines should be processed at once in the various text
    * processing. This is used for example when processing the text
    * by syntax scanner.
    */
    public static final String LINE_BATCH_SIZE = "line-batch-size"; // NOI18N

    /** List of the names of the additional colorings (except the base ones).
    * The coloring names are without the suffix just like the predefined coloring names.
    * Values: java.util.List instances
    */
    public static final String COLORING_NAME_LIST = "coloring-name-list"; // NOI18N

    /** List of the coloring names that should be excluded from the font uniformity check.
    * At the begining the extended-UI needs to determine whether all the colorings
    * that will be used to color the component have the same fixed-size font. If so
    * the faster drawing method is used. This setting should contain the names of all
    * the colorings that are not used in the editor component for example the status bar
    * colorings.
    * Values: java.util.List instances
    */
    public static final String UNIFORM_FONT_EXCLUSION_LIST = "uniform-font-exclusion-list"; // NOI18N

    /** Suffix added to the coloring settings. The resulting name is used
    * as the name of the setting.
    */
    public static final String COLORING_NAME_SUFFIX = "-coloring"; // NOI18N

    /** Suffix added to the print coloring settings. The resulting name is used
    * as the name of the setting.
    */
    public static final String COLORING_NAME_PRINT_SUFFIX = "-print-coloring"; // NOI18N


    /** Default coloring for the drawing. */
    public static final String DEFAULT_COLORING = "default"; // NOI18N

    /** Coloring that will be used for line numbers displayed on the left
    * side on the screen.
    */
    public static final String LINE_NUMBER_COLORING = "line-number"; // NOI18N

    /** Coloring used for guarded blocks */
    public static final String GUARDED_COLORING = "guarded"; // NOI18N

    /** Coloring used for selection */
    public static final String SELECTION_COLORING = "selection"; // NOI18N

    /** Coloring used for highlight search */
    public static final String HIGHLIGHT_SEARCH_COLORING = "highlight-search"; // NOI18N

    /** Coloring used for incremental search */
    public static final String INC_SEARCH_COLORING = "inc-search"; // NOI18N

    /** Coloring used to highlight the row where the caret resides */
    public static final String HIGHLIGHT_ROW_COLORING = "highlight-row"; // NOI18N

    /** Coloring used to highlight the matching bracket */
    public static final String HIGHLIGHT_BRACKET_COLORING = "highlight-bracket"; // NOI18N

    /** Coloring used for bookmark lines */
    public static final String BOOKMARK_COLORING = "bookmark"; // NOI18N

    /** Coloring used for the status bar */
    public static final String STATUS_BAR_COLORING = "status-bar"; // NOI18N

    /** Coloring used to mark important text in the status bar */
    public static final String STATUS_BAR_BOLD_COLORING = "status-bar-bold"; // NOI18N



    /** List of Initializers 
     * @associates Initializer*/
    private static final List initializerList = new ArrayList();

    /** List of Filters 
     * @associates Filter*/
    private static final List filterList = new ArrayList();

    /** [kit-class, map-of-settings] pairs 
     * @associates Map*/
    private static final Map kit2Maps = new HashMap();

    /** Support for firing change events */
    private static final WeakEventListenerList listenerList = new WeakEventListenerList();

    /** Internal map instance signing that initializer returned null
    * map for particular kit. To sign this fact and not query initializer
    * again, this simple map is used.
    */
    private static final Map NULL_MAP = new HashMap(1);

    private static boolean firingEnabled = true;

    private Settings() {
        // no instances allowed
    }

    /** Add initializer instance to the list of current initializers.
    * If there are already existing editor components,
    * and you want to apply the settings provided by this new initializer
    * to these existing
    * components, you can call reset(). However all the changes
    * that were made explicitly by calling setValue() will be lost.
    *
    * @param i initializer to add to the current list of initializers
    */
    public static synchronized void addInitializer(Initializer i) {
        initializerList.add(i);
    }

    /** Add filter instance to the list of current filters.
    * If there are already existing editor components,
    * and you want to apply the changes that this filter makes
    * to these existing
    * components, you can call reset(). However all the changes
    * that were made explicitly by calling setValue() will be lost.
    *
    * @param f filter to add to the list of the filters
    */
    public static synchronized void addFilter(Filter f) {
        filterList.add(f);
    }

    /** Get the property by searching the given kit class settings and if not
    * found then the settings for super class and so on.
    * @param kitClass editor kit class for which the value of setting should
    *   be retrieved. The null can be used as the root of the whole hierarchy.
    *   (which is top of kit class hierarchy) before the processing begins.
    * @param settingName name of the setting for which the value should
    *   be retrieved
    * @return the value of the setting
    */
    public static synchronized Object getValue(Class kitClass, String settingName) {
        Object value = null;
        Class kc = kitClass;
        while (true) {
            Map map = getKitMap(kc, false);
            if (map != null) {
                value = map.get(settingName);
                if (value instanceof Substituter) {
                    value = ((Substituter)value).getValue(kitClass, settingName);
                }
                if (value != null) {
                    break;
                }
            }
            if (kc == null) {
                break;
            }
            kc = kc.getSuperclass();
        }

        // filter the value if necessary
        int cnt = filterList.size();
        for (int i = 0; i < cnt; i++) {
            value = ((Filter)filterList.get(i)).filterValue(kitClass, settingName, value);
        }

        return value;
    }

    /** Get array of KitAndValue objects sorted from the given kit class to its
    * deepest superclass and the last member can be filled whether there
    * is global setting (kit class of that member would be null).
    * This method is useful for objects like keymaps that
    * need to create all the parent keymaps to work properly.
    * @param kitClass editor kit class for which the value of setting should
    *   be retrieved. The null can be used as the root of the whole hierarchy.
    * @param settingName name of the setting for which the value should
    *   be retrieved
    * @return the array containing KitAndValue instances describing the particular
    *   setting's value on the specific kit level.
    */
    public static synchronized KitAndValue[] getKitAndValueArray(Class kitClass,
            String settingName) {
        ArrayList kavList = new ArrayList();
        Class kc = kitClass;
        while (true) {
            Map map = getKitMap(kc, false);
            if (map != null) {
                Object value = map.get(settingName);
                if (value instanceof Substituter) {
                    value = ((Substituter)value).getValue(kitClass, settingName);
                }
                if (value != null) {
                    kavList.add(new KitAndValue(kc, value));
                }
            }
            if (kc == null) {
                break;
            }
            kc = kc.getSuperclass();
        }
        KitAndValue[] kavArray = (KitAndValue[])kavList.toArray(
                                     new KitAndValue[kavList.size()]);

        // filter the value if necessary
        int cnt = filterList.size();
        for (int i = 0; i < cnt; i++) {
            kavArray = ((Filter)filterList.get(i)).filterKitAndValueArray(
                           kitClass, settingName, kavArray);
        }

        return kavArray;
    }

    /** Set the new value for property on kit level. The old and new values
    * are compared and if they are equal the setting is not changed and
    * nothing is fired.
    * 
    * @param kitClass editor kit class for which the value of setting should
    *   be set. The null can be used as the root of the whole hierarchy.
    * @param settingName the string used for searching the value
    * @param newValue new value to set for the property; the value can
    *   be null to clear the value for the specified kit
    */
    public static synchronized void setValue(Class kitClass, String settingName,
            Object newValue) {
        Map map = getKitMap(kitClass, true);
        Object oldValue = map.get(settingName);
        if (oldValue == null && newValue == null
                || (oldValue != null && oldValue.equals(newValue))
           ) {
            return; // no change
        }
        if (newValue != null) {
            map.put(settingName, newValue);
        } else {
            map.remove(settingName);
        }
        fireSettingsChange(kitClass, settingName, oldValue, newValue);
    }

    /** Don't change the value of the setting, but fire change
    * event. This is useful when there's internal change in the value object
    * of some setting.
    */
    public static synchronized void touchValue(Class kitClass, String settingName) {
        fireSettingsChange(kitClass, settingName, null, null); // kit class currently not used
    }

    /** Set the value for the current kit and propagate it to all
    * the children of the given kit by removing
    * the possible values for the setting from the children kit setting maps.
    * Note: This call only affects the settings for the kit classes for which
    * the kit setting map with the setting values currently exists, i.e. when
    * there was at least one getValue() or setValue() call performed for any
    * setting on that particular kit class level. Other kit classes maps
    * will be initialized by the particular initializer(s) as soon as
    * the first getValue() or setValue() will be performed for them.
    * However that future process will not be affected by the current
    * propagateValue() call.
    * This method is useful for the visual options that always set
    * the value on all the kit levels without regard whether it's necessary or not.
    * If the value is then changed for the base kit, it's not propagated
    * naturally as there's a special setting
    * This method enables 
    * 
    * The current implementation always fires the change regardless whether
    * there was real change in setting value or not.
    * @param kitClass editor kit class for which the value of setting should
    *   be set. When given null, it's automatically set to BaseKit.class
    *   (which is top of kit class hierarchy) before the processing begins.
    *   Therefore you get exactly the same results for kitClass == null and
    *   kitClass == BaseKit.class.
    * @param settingName the string used for searching the value
    * @param newValue new value to set for the property; the value can
    *   be null to clear the value for the specified kit
    */
    public static synchronized void propagateValue(Class kitClass,
            String settingName, Object newValue) {
        Map map = getKitMap(kitClass, true);
        if (newValue != null) {
            map.put(settingName, newValue);
        } else {
            map.remove(settingName);
        }
        // resolve kits
        Iterator it = kit2Maps.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry me = (Map.Entry)it.next();
            Class kc = (Class)me.getKey();
            if (kitClass != kc && (kitClass == null || kitClass.isAssignableFrom(kc))) {
                ((Map)me.getValue()).remove(settingName);
            }
        }
        fireSettingsChange(null, settingName, null, null);
    }

    /** Run the given runnable. All the changes in the settings are not fired until
    * the whole runnable completes. Nesting of <tt>update()</tt> call is allowed.
    * Only one firing is performed after the whole runnable completes
    * using the 'null triple'.
    */
    public static synchronized void update(Runnable r) {
        boolean turnedOff = firingEnabled;
        firingEnabled = false;
        try {
            r.run();
        } finally {
            if (turnedOff) {
                firingEnabled = true;
                fireSettingsChange(null, null, null, null);
            }
        }
    }

    /** Reset all the settings and fire the change of the settings
    * so that all the listeners will be notified and will reload
    * the settings.
    * The settings that were changed using setValue() and propagateValue()
    * are lost. Initializers will be asked for the settings values when
    * necessary.
    */
    public static void reset() {
        kit2Maps.clear();
        fireSettingsChange(null, null, null, null);
    }

    /** Add weak listener to listen to change of any property. The caller must
    * hold the listener object in some instance variable to prevent it
    * from being garbage collected.
    */
    public static void addSettingsChangeListener(SettingsChangeListener l) {
        listenerList.add(SettingsChangeListener.class, l);
    }

    /** Remove listener for changes in properties */
    public static void removeSettingsChangeListener(SettingsChangeListener l) {
        listenerList.remove(SettingsChangeListener.class, l);
    }

    private static void fireSettingsChange(Class kitClass, String settingName,
                                           Object oldValue, Object newValue) {
        if (firingEnabled) {
            SettingsChangeListener[] listeners = (SettingsChangeListener[])
                                                 listenerList.getListeners(SettingsChangeListener.class);
            SettingsChangeEvent evt = new SettingsChangeEvent(Settings.class,
                                      kitClass, settingName, oldValue, newValue);
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].settingsChange(evt);
            }
        }
    }

    /** Get (and possibly create) kit map for particular kit */
    private static Map getKitMap(Class kitClass, boolean forceCreation) {
        Map kitMap = (Map)kit2Maps.get(kitClass);
        if (kitMap == null) {
            Iterator iter = initializerList.iterator();
            while (iter.hasNext()) {
                Initializer i = (Initializer)iter.next();
                kitMap = i.updateSettingsMap(kitClass, kitMap);
            }
            if (kitMap == null) { // initializer refused creation
                kitMap = NULL_MAP; // initializer will not be called again
            }
            kit2Maps.put(kitClass, kitMap);
        }
        if (kitMap == NULL_MAP) {
            if (!forceCreation) {
                return null;
            } else {
                kitMap = new HashMap(20); // create empty map
                kit2Maps.put(kitClass, kitMap);
            }
        }
        return kitMap;
    }


    /** Kit class and value pair */
    public static class KitAndValue {

        public Class kitClass;

        public Object value;

        public KitAndValue(Class kitClass, Object value) {
            this.kitClass = kitClass;
            this.value = value;
        }

    }


    /** Initializer of the settings updates the map filled
    * with settings for the particular kit class when asked.
    * If the settings are being initialized all the initializers registered
    * by the <tt>Settings.addInitializer()</tt> are being asked to update'
    * the settings-map through calling their <tt>updateSettingsMap()</tt>.
    * If there is more initializers they are all called to update
    * the settings-map in the order they were added.
    */
    public static interface Initializer {

        /** Update map filled with the settings from the previous initializer
        * @param kitClass kit class for which the settings are being updated.
        *   It is always non-null value.
        * @param m map with settings to update. It can be null if all
        *   the previous initializers didn't need to create any settings
        *   for the given kit class.
        * @return map containing the desired settings or null if no settings
        *   are defined for the given kit
        */
        public Map updateSettingsMap(Class kitClass, Map m);

    }


    /** Substituter can be used in cases when value of some setting
    * depends on the value for other setting and it allows to compute
    * the value dynamically based on the other setting(s) value.
    * The <tt>Substituter</tt> instance can be used as the value
    * in the <tt>Settings.setValue()</tt> call. In that case the call
    * to the <tt>Settings.getValue()</tt> call will 'evaluate' the substituter
    * by calling its <tt>getValue()</tt>.
    */
    public static interface Substituter {

        /** Compute the particular setting's value.
        * @param kitClass kit class for which the setting is being retrieved.
        * @param settingName name of the setting to retrieve. Although the substituter
        *   are usually constructed only for the concrete setting, this parameter
        *   allows creation of the substituter for multiple settings.
        * @return the value for the requested setting. The substitution
        *   is not attempted again, so the return value cannot be another
        *   Substituter instance. If the returned value is null, the same
        *   action is taken as if there would no value set on the particular
        *   kit level.
        *   
        */
        public Object getValue(Class kitClass, String settingName);

    }


    /** Filter is applied on every value or KitAndValue pairs returned from getValue().
    * The filter can be registered by calling <tt>Settings.addFilter()</tt>.
    * Each call to <tt>Settings.getValue()</tt> will first retrieve the value and
    * then call the <tt>Filter.filterValue()</tt> to get the final value. Each call
    * to <tt>Settings.getKitAndValueArray()</tt> will first retrieve the kit-and-value
    * array and then call the <tt>Filter.filterKitAndValueArray()</tt>.
    * If more filters are registered they are all used in the order they were added.
    */
    public static interface Filter {

        /** Filter single value. The value can be substituted here.
        */
        public Object filterValue(Class kitClass, String settingName, Object value);

        /** Filter array of kit and value pairs. The pairs can be completely
        * substituted with an array with different length and different members.
        */
        public KitAndValue[] filterKitAndValueArray(Class kitClass, String settingName,
                KitAndValue[] kavArray);

    }


}

/*
 * Log
 *  46   Gandalf-post-FCS1.44.1.0    3/8/00   Miloslav Metelka 
 *  45   Gandalf   1.44        1/13/00  Miloslav Metelka 
 *  44   Gandalf   1.43        1/11/00  Miloslav Metelka 
 *  43   Gandalf   1.42        1/6/00   Miloslav Metelka 
 *  42   Gandalf   1.41        1/4/00   Miloslav Metelka 
 *  41   Gandalf   1.40        12/28/99 Miloslav Metelka 
 *  40   Gandalf   1.39        11/24/99 Miloslav Metelka 
 *  39   Gandalf   1.38        11/14/99 Miloslav Metelka 
 *  38   Gandalf   1.37        11/10/99 Miloslav Metelka 
 *  37   Gandalf   1.36        11/8/99  Miloslav Metelka 
 *  36   Gandalf   1.35        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  35   Gandalf   1.34        10/6/99  Miloslav Metelka 
 *  34   Gandalf   1.33        9/30/99  Miloslav Metelka 
 *  33   Gandalf   1.32        9/10/99  Miloslav Metelka 
 *  32   Gandalf   1.31        8/27/99  Miloslav Metelka 
 *  31   Gandalf   1.30        8/19/99  Miloslav Metelka 
 *  30   Gandalf   1.29        8/18/99  Miloslav Metelka 
 *  29   Gandalf   1.28        8/17/99  Miloslav Metelka 
 *  28   Gandalf   1.27        7/29/99  Miloslav Metelka 
 *  27   Gandalf   1.26        7/26/99  Miloslav Metelka 
 *  26   Gandalf   1.25        7/22/99  Miloslav Metelka 
 *  25   Gandalf   1.24        7/21/99  Miloslav Metelka 
 *  24   Gandalf   1.23        7/20/99  Miloslav Metelka 
 *  23   Gandalf   1.22        7/9/99   Miloslav Metelka 
 *  22   Gandalf   1.21        7/2/99   Miloslav Metelka 
 *  21   Gandalf   1.20        6/29/99  Miloslav Metelka Scrolling and patches
 *  20   Gandalf   1.19        6/25/99  Miloslav Metelka from floats back to ints
 *  19   Gandalf   1.18        6/22/99  Miloslav Metelka 
 *  18   Gandalf   1.17        6/10/99  Miloslav Metelka 
 *  17   Gandalf   1.16        6/8/99   Miloslav Metelka 
 *  16   Gandalf   1.15        6/1/99   Miloslav Metelka 
 *  15   Gandalf   1.14        5/15/99  Miloslav Metelka fixes
 *  14   Gandalf   1.13        5/13/99  Miloslav Metelka 
 *  13   Gandalf   1.12        5/7/99   Miloslav Metelka line numbering and fixes
 *  12   Gandalf   1.11        5/5/99   Miloslav Metelka 
 *  11   Gandalf   1.10        4/23/99  Miloslav Metelka changes in settings
 *  10   Gandalf   1.9         4/23/99  Miloslav Metelka Undo added and internal 
 *       improvements
 *  9    Gandalf   1.8         4/13/99  Ian Formanek    Abbreviations are back 
 *       (it is not possible to compile it under Startup as it depends on editor
 *       module)
 *  8    Gandalf   1.7         4/11/99  Ian Formanek    Added static method 
 *       getAbbrevTable
 *  7    Gandalf   1.6         4/11/99  Ian Formanek    
 *  6    Gandalf   1.5         4/8/99   Ian Formanek    Abbreviations moved to 
 *       Startup/Abbrevs.java
 *  5    Gandalf   1.4         4/8/99   Miloslav Metelka 
 *  4    Gandalf   1.3         4/1/99   Miloslav Metelka 
 *  3    Gandalf   1.2         3/30/99  Miloslav Metelka 
 *  2    Gandalf   1.1         3/27/99  Miloslav Metelka 
 *  1    Gandalf   1.0         3/23/99  Miloslav Metelka 
 * $
 */

