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

import java.awt.Point;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.io.Reader;
import java.io.Writer;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.text.Document;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.BadLocationException;
import javax.swing.text.ViewFactory;
import javax.swing.text.Caret;
import javax.swing.text.TextAction;
import javax.swing.text.Keymap;
import javax.swing.text.JTextComponent;

/**
* Editor kit implementation for base document
*
* @author Miloslav Metelka
* @version 1.00
*/

public class BaseKit extends DefaultEditorKit {

    /** This action is searched and executed when the popup menu should
    * be displayed to build the popup menu.
    */
    public static final String buildPopupMenuAction = "build-popup-menu"; // NOI18N

    /** This action is searched and executed when the tool-tip should
    * be displayed by tool-tip support to build the tool-tip.
    */
    public static final String buildToolTipAction = "build-tool-tip"; // NOI18N

    /** Move one page up and make or extend selection */
    public static final String selectionPageUpAction = "selection-page-up"; // NOI18N

    /** Move one page down and make or extend selection */
    public static final String selectionPageDownAction = "selection-page-down"; // NOI18N

    /** Remove indentation */
    public static final String removeTabAction = "remove-tab"; // NOI18N

    /** Remove selected block or do nothing - useful for popup menu */
    public static final String removeSelectionAction = "remove-selection"; // NOI18N

    /** Toggle bookmark on the current line */
    public static final String toggleBookmarkAction = "bookmark-toggle"; // NOI18N

    /** Goto the next bookmark */
    public static final String gotoNextBookmarkAction = "bookmark-next"; // NOI18N

    /** Expand the abbreviation */
    public static final String abbrevExpandAction = "abbrev-expand"; // NOI18N

    /** Reset the abbreviation accounting string */
    public static final String abbrevResetAction = "abbrev-reset"; // NOI18N

    /** Remove to the begining of the word */
    public static final String removeWordAction = "remove-word"; // NOI18N

    /** Remove to the begining of the line */
    public static final String removeLineBeginAction = "remove-line-begin"; // NOI18N

    /** Remove line */
    public static final String removeLineAction = "remove-line"; // NOI18N

    /** Toggle the typing mode to overwrite mode or back to insert mode */
    public static final String toggleTypingModeAction = "toggle-typing-mode"; // NOI18N

    /** Change the selected text or current character to uppercase */
    public static final String toUpperCaseAction = "to-upper-case"; // NOI18N

    /** Change the selected text or current character to lowercase */
    public static final String toLowerCaseAction = "to-lower-case"; // NOI18N

    /** Switch the case of the selected text or current character */
    public static final String switchCaseAction = "switch-case"; // NOI18N

    /** Toggle the case for the first character of the word under caret */
    public static final String toggleCaseIdentifierBeginAction = "toggle-case-identifier-begin"; // NOI18N

    /** Open find dialog action - this action is defined in view package, but
    * its name is defined here for clarity
    */
    public static final String findAction = "find"; // NOI18N

    /** Open goto dialog action - this action is defined in view package, but
    * its name is defined here for clarity
    */
    public static final String gotoAction = "goto"; // NOI18N

    /** Open replace dialog action - this action is defined in view package, but
    * its name is defined here for clarity
    */
    public static final String replaceAction = "replace"; // NOI18N

    /** Find next occurence action */
    public static final String findNextAction = "find-next"; // NOI18N

    /** Find previous occurence action */
    public static final String findPreviousAction = "find-previous"; // NOI18N

    /** Toggle highlight search action */
    public static final String toggleHighlightSearchAction = "toggle-highlight-search"; // NOI18N

    /** Find current word */
    public static final String findSelectionAction = "find-selection"; // NOI18N

    /** Undo action */
    public static final String undoAction = "undo"; // NOI18N

    /** Redo action */
    public static final String redoAction = "redo"; // NOI18N

    /** Word match next */
    public static final String wordMatchNextAction = "word-match-next"; // NOI18N

    /** Word match prev */
    public static final String wordMatchPrevAction = "word-match-prev"; // NOI18N

    /** Match brace */
    public static final String bracketMatchAction = "bracket-match"; // NOI18N

    /** Select the text to the matching bracket */
    public static final String selectionBracketMatchAction = "selection-bracket-match"; // NOI18N

    /** Shift line right action */
    public static final String shiftLineRightAction = "shift-line-right"; // NOI18N

    /** Shift line left action */
    public static final String shiftLineLeftAction = "shift-line-left"; // NOI18N

    /** Action that scrolls the window so that caret is at the center of the window */
    public static final String adjustWindowCenterAction = "adjust-window-center"; // NOI18N

    /** Action that scrolls the window so that caret is at the top of the window */
    public static final String adjustWindowTopAction = "adjust-window-top"; // NOI18N

    /** Action that scrolls the window so that caret is at the bottom of the window */
    public static final String adjustWindowBottomAction = "adjust-window-bottom"; // NOI18N

    /** Action that moves the caret so that caret is at the center of the window */
    public static final String adjustCaretCenterAction = "adjust-caret-center"; // NOI18N

    /** Action that moves the caret so that caret is at the top of the window */
    public static final String adjustCaretTopAction = "adjust-caret-top"; // NOI18N

    /** Action that moves the caret so that caret is at the bottom of the window */
    public static final String adjustCaretBottomAction = "adjust-caret-bottom"; // NOI18N

    /** Format part of the document text using Indent */
    public static final String formatAction = "format"; // NOI18N

    /** First non-white character on the line */
    public static final String firstNonWhiteAction = "first-non-white"; // NOI18N

    /** Last non-white character on the line */
    public static final String lastNonWhiteAction = "last-non-white"; // NOI18N

    /** First non-white character on the line */
    public static final String selectionFirstNonWhiteAction = "selection-first-non-white"; // NOI18N

    /** Last non-white character on the line */
    public static final String selectionLastNonWhiteAction = "selection-last-non-white"; // NOI18N

    /** Select the nearest identifier around caret */
    public static final String selectIdentifierAction = "select-identifier"; // NOI18N

    /** Select the next parameter (after the comma) in the given context */
    public static final String selectNextParameterAction = "select-next-parameter"; // NOI18N

    /** Go to the previous position stored in the jump-list */
    public static final String jumpListNextAction = "jump-list-next"; // NOI18N

    /** Go to the next position stored in the jump-list */
    public static final String jumpListPrevAction = "jump-list-prev"; // NOI18N

    /** Go to the last position in the previous component stored in the jump-list */
    public static final String jumpListNextComponentAction = "jump-list-next-component"; // NOI18N

    /** Go to the next position in the previous component stored in the jump-list */
    public static final String jumpListPrevComponentAction = "jump-list-prev-component"; // NOI18N

    /** Scroll window one line up */
    public static final String scrollUpAction = "scroll-up"; // NOI18N

    /** Scroll window one line down */
    public static final String scrollDownAction = "scroll-down"; // NOI18N

    /** Select the code depending on the braces around the caret */
    public static final String braceCodeSelectAction = "brace-code-select"; // NOI18N

    /** Action used when escape is pressed. By default it hides popup-menu */
    public static final String escapeAction = "escape"; // NOI18N

    // Self test actions
    /** Dump of the mark planes */
    public static final String dumpPlanesAction = "selftest-dump-planes"; // NOI18N

    /** Dump of syntax marks */
    public static final String dumpSyntaxMarksAction = "selftest-dump-syntax-marks"; // NOI18N

    /** Perform several internal tests */
    public static final String selfTestAction = "selftest-run-tests"; // NOI18N


    private static final int KIT_CNT_PREALLOC = 57;

    /** [kit-class, kit-instance] pairs are stored here 
     * @associates BaseKit*/
    static Map kits = new HashMap(KIT_CNT_PREALLOC);

    /** [kit-class, keymap] pairs 
     * @associates MultiKeymap*/
    static Map kitKeymaps = new HashMap(KIT_CNT_PREALLOC);

    /** [kit, action[]] pairs 
     * @associates Action*/
    static Map kitActions = new HashMap(KIT_CNT_PREALLOC);

    /** [kit, action-map] pairs 
     * @associates Map*/
    static Map kitActionMaps = new HashMap(KIT_CNT_PREALLOC);

    static SettingsChangeListener settingsListener = new SettingsChangeListener() {
                public void settingsChange(SettingsChangeEvent evt) {
                    synchronized (BaseKit.class) {
                        String settingName = (evt != null) ? evt.getSettingName() : null;

                        if (Settings.KEY_BINDING_LIST.equals(settingName)) {
                            kitKeymaps.clear();
                        }

                        if (Settings.CUSTOM_ACTION_LIST.equals(settingName)) {
                            kitActions.clear();
                            kitActionMaps.clear();
                        } else { // only refresh action settings
                            Iterator i = kitActions.entrySet().iterator();
                            while (i.hasNext()) {
                                Map.Entry me = (Map.Entry)i.next();
                                updateActionSettings((Action[])me.getValue(), evt, (Class)me.getKey());
                            }
                        }
                    }
                }
            };

    static {
        Settings.addSettingsChangeListener(settingsListener);
    }

    private static void updateActionSettings(Action[] actions,
            SettingsChangeEvent evt, Class kitClass) {
        for (int i = 0; i < actions.length; i++) {
            if (actions[i] instanceof BaseAction) {
                ((BaseAction)actions[i]).settingsChange(evt, kitClass);
            }
        }
    }

    public static synchronized BaseKit getKit(Class kitClass) {
        if (kitClass == null) {
            kitClass = BaseKit.class;
        }
        BaseKit kit = (BaseKit)kits.get(kitClass);
        if (kit == null) {
            try {
                kit = (BaseKit)kitClass.newInstance();
            } catch (IllegalAccessException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            } catch (InstantiationException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            }
            kits.put(kitClass, kit);
        }
        return kit;
    }

    static final long serialVersionUID =-8570495408376659348L;

    public BaseKit() {
        // possibly register
        synchronized (BaseKit.class) {
            if (kits.get(this.getClass()) == null) {
                kits.put(this.getClass(), this); // register itself
            }
        }
    }

    /** Clone this editor kit */
    public Object clone() {
        return this; // no need to create another instance
    }

    /** Fetches a factory that is suitable for producing
     * views of any models that are produced by this
     * kit.  The default is to have the UI produce the
     * factory, so this method has no implementation.
     *
     * @return the view factory
     */
    public ViewFactory getViewFactory() {
        return null;
    }

    /** Create caret to navigate through document */
    public Caret createCaret() {
        return new BaseCaret();
    }

    /** Create empty document */
    public Document createDefaultDocument() {
        return new BaseDocument(this.getClass(), true);
    }

    /** Create new instance of syntax coloring scanner
    * @param doc document to operate on. It can be null in the cases the syntax
    *   creation is not related to the particular document
    */
    public Syntax createSyntax(BaseDocument doc) {
        return new Syntax();
    }

    /** Create syntax support */
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new SyntaxSupport(doc);
    }


    /** Create text UI */
    protected BaseTextUI createUI() {
        return new BaseTextUI();
    }

    /** Create extended UI */
    protected ExtUI createExtUI() {
        return new ExtUI();
    }

    /** Create extended UI for printing a document. */
    protected ExtUI createPrintExtUI(BaseDocument doc) {
        return new ExtUI(doc);
    }

    /** This methods gives the class of the first component in the component
    * hierarchy that should be called when requesting focus for the other
    * component.
    * @return class of the component that will be searched in the parents
    *   in the hierarchy or null to do no search.
    */
    public Class getFocusableComponentClass(JTextComponent c) {
        return null;
    }

    public MultiKeymap getKeymap() {
        synchronized (BaseKit.class) {
            MultiKeymap km = (MultiKeymap)kitKeymaps.get(this.getClass());
            if (km == null) { // keymap not yet constructed
                // construct new keymap
                km = new MultiKeymap("Keymap for " + this.getClass()); // NOI18N
                // retrieve key bindings for this kit and super kits
                Settings.KitAndValue kv[] = Settings.getKitAndValueArray(
                                                this.getClass(), Settings.KEY_BINDING_LIST);
                // go through all levels and collect key bindings
                for (int i = kv.length - 1; i >= 0; i--) {
                    List keyList = (List)kv[i].value;
                    JTextComponent.KeyBinding[] keys = new JTextComponent.KeyBinding[keyList.size()];
                    keyList.toArray(keys);
                    km.load(keys, getActionMap());
                }

                kitKeymaps.put(this.getClass(), km);
            }
            return km;
        }
    }

    /** Inserts content from the given stream. */
    public void read(Reader in, Document doc, int pos)
    throws IOException, BadLocationException {
        if (doc instanceof BaseDocument) {
            ((BaseDocument)doc).read(in, pos); // delegate it to document
        } else {
            super.read(in, doc, pos);
        }
    }

    /** Writes content from a document to the given stream */
    public void write(Writer out, Document doc, int pos, int len)
    throws IOException, BadLocationException {
        if (doc instanceof BaseDocument) {
            ((BaseDocument)doc).write(out, pos, len);
        } else {
            super.write(out, doc, pos, len);
        }
    }

    /** Creates map with [name, action] pairs from the given
    * array of actions.
    */
    public static Map actionsToMap(Action[] actions) {
        Map map = new HashMap();
        for (int i = 0; i < actions.length; i++) {
            Action a = actions[i];
            String name = (String)a.getValue(Action.NAME);
            map.put(((name != null) ? name : ""), a); // NOI18N
        }
        return map;
    }

    /** Converts map with [name, action] back
    * to array of actions.
    */
    public static Action[] mapToActions(Map map) {
        Action[] actions = new Action[map.size()];
        int i = 0;
        for (Iterator iter = map.values().iterator() ; iter.hasNext() ;) {
            actions[i++] = (Action)iter.next();
        }
        return actions;
    }

    /** Called after the kit is installed into JEditorPane */
    public void install(JEditorPane c) {
        BaseTextUI ui = createUI();
        c.setUI(ui);

        String propName = "netbeans.editor.noinputmethods";
        Object noInputMethods = System.getProperty(propName);
        boolean enableIM;
        if (noInputMethods != null) {
            enableIM = !Boolean.getBoolean(propName);
        } else {
            enableIM = SettingsUtil.getBoolean(this.getClass(),
                                               Settings.INPUT_METHODS_ENABLED, true);
        }

        c.enableInputMethods(enableIM);
        executeInstallActions(c);
    }

    protected void executeInstallActions(JEditorPane c) {
        Settings.KitAndValue[] kv = Settings.getKitAndValueArray(this.getClass(),
                                    Settings.KIT_INSTALL_ACTION_NAME_LIST);
        for (int i = kv.length - 1; i >= 0; i--) {
            List actList = (List)kv[i].value;
            actList = translateActionNameList(actList); // translate names to actions
            if (actList != null) {
                for (Iterator iter = actList.iterator(); iter.hasNext();) {
                    Action a = (Action)iter.next();
                    a.actionPerformed(new ActionEvent(c, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                }
            }
        }
    }

    public void deinstall(JEditorPane c) {
        executeDeinstallActions(c);
    }

    protected void executeDeinstallActions(JEditorPane c) {
        Settings.KitAndValue[] kv = Settings.getKitAndValueArray(this.getClass(),
                                    Settings.KIT_DEINSTALL_ACTION_NAME_LIST);
        for (int i = kv.length - 1; i >= 0; i--) {
            List actList = (List)kv[i].value;
            actList = translateActionNameList(actList); // translate names to actions
            if (actList != null) {
                for (Iterator iter = actList.iterator(); iter.hasNext();) {
                    Action a = (Action)iter.next();
                    a.actionPerformed(new ActionEvent(c, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                }
            }
        }
    }

    /** Create actions that this kit supports. To use the actions of the parent kit
    * it's better instead of using super.createActions() to use
    * getKit(super.getClass()).getActions() because it can reuse existing
    * parent actions.
    */
    protected Action[] createActions() {
        Action[] baseActions = new Action[] {
                                   new DefaultKeyTypedAction(),
                                   new InsertContentAction(),
                                   new InsertBreakAction(),
                                   new InsertTabAction(),
                                   new DeleteCharAction(deletePrevCharAction, false),
                                   new DeleteCharAction(deleteNextCharAction, true),
                                   new ReadOnlyAction(),
                                   new WritableAction(),
                                   new CutAction(),
                                   new CopyAction(),
                                   new PasteAction(),
                                   new BeepAction(),
                                   new UpAction(upAction, false),
                                   new UpAction(selectionUpAction, true),
                                   new PageUpAction(pageUpAction, false),
                                   new PageUpAction(selectionPageUpAction, true),
                                   new DownAction(downAction, false),
                                   new DownAction(selectionDownAction, true),
                                   new PageDownAction(selectionPageDownAction, true),
                                   new PageDownAction(pageDownAction, false),
                                   new ForwardAction(forwardAction, false),
                                   new ForwardAction(selectionForwardAction, true),
                                   new BackwardAction(backwardAction, false),
                                   new BackwardAction(selectionBackwardAction, true),
                                   new BeginLineAction(beginLineAction, false),
                                   new BeginLineAction(selectionBeginLineAction, true),
                                   new EndLineAction(endLineAction, false),
                                   new EndLineAction(selectionEndLineAction, true),
                                   new BeginAction(beginAction, false),
                                   new BeginAction(selectionBeginAction, true),
                                   new EndAction(endAction, false),
                                   new EndAction(selectionEndAction, true),
                                   new NextWordAction(nextWordAction, false),
                                   new NextWordAction(selectionNextWordAction, true),
                                   new PreviousWordAction(previousWordAction, false),
                                   new PreviousWordAction(selectionPreviousWordAction, true),
                                   new BeginWordAction(beginWordAction, false),
                                   new BeginWordAction(selectionBeginWordAction, true),
                                   new EndWordAction(endWordAction, false),
                                   new EndWordAction(selectionEndWordAction, true),
                                   new SelectWordAction(),
                                   new SelectLineAction(),
                                   new SelectAllAction(),
                                   new ActionFactory.RemoveTabAction(),
                                   new ActionFactory.RemoveWordAction(),
                                   new ActionFactory.RemoveLineBeginAction(),
                                   new ActionFactory.RemoveLineAction(),
                                   new ActionFactory.RemoveSelectionAction(),
                                   new ActionFactory.ToggleTypingModeAction(),
                                   new ActionFactory.ToggleBookmarkAction(),
                                   new ActionFactory.GotoNextBookmarkAction(gotoNextBookmarkAction, false),
                                   new ActionFactory.AbbrevExpandAction(),
                                   new ActionFactory.AbbrevResetAction(),
                                   new ActionFactory.ChangeCaseAction(toUpperCaseAction, Utilities.CASE_UPPER),
                                   new ActionFactory.ChangeCaseAction(toLowerCaseAction, Utilities.CASE_LOWER),
                                   new ActionFactory.ChangeCaseAction(switchCaseAction, Utilities.CASE_SWITCH),
                                   new ActionFactory.ToggleCaseIdentifierBeginAction(),
                                   new ActionFactory.FindNextAction(),
                                   new ActionFactory.FindPreviousAction(),
                                   new ActionFactory.FindSelectionAction(),
                                   new ActionFactory.ToggleHighlightSearchAction(),
                                   new ActionFactory.UndoAction(),
                                   new ActionFactory.RedoAction(),
                                   new ActionFactory.WordMatchAction(wordMatchNextAction, true),
                                   new ActionFactory.WordMatchAction(wordMatchPrevAction, false),
                                   new ActionFactory.BracketMatchAction(bracketMatchAction, false),
                                   new ActionFactory.BracketMatchAction(selectionBracketMatchAction, true),
                                   new ActionFactory.ShiftLineAction(shiftLineLeftAction, false),
                                   new ActionFactory.ShiftLineAction(shiftLineRightAction, true),
                                   new ActionFactory.AdjustWindowAction(adjustWindowTopAction, 0),
                                   new ActionFactory.AdjustWindowAction(adjustWindowCenterAction, 50),
                                   new ActionFactory.AdjustWindowAction(adjustWindowBottomAction, 100),
                                   new ActionFactory.AdjustCaretAction(adjustCaretTopAction, 0),
                                   new ActionFactory.AdjustCaretAction(adjustCaretCenterAction, 50),
                                   new ActionFactory.AdjustCaretAction(adjustCaretBottomAction, 100),
                                   new ActionFactory.FormatAction(),
                                   new ActionFactory.FirstNonWhiteAction(firstNonWhiteAction, false),
                                   new ActionFactory.FirstNonWhiteAction(selectionFirstNonWhiteAction, true),
                                   new ActionFactory.LastNonWhiteAction(lastNonWhiteAction, false),
                                   new ActionFactory.LastNonWhiteAction(selectionLastNonWhiteAction, true),
                                   new ActionFactory.SelectIdentifierAction(),
                                   new ActionFactory.SelectNextParameterAction(),
                                   new ActionFactory.JumpListPrevAction(),
                                   new ActionFactory.JumpListNextAction(),
                                   new ActionFactory.JumpListPrevComponentAction(),
                                   new ActionFactory.JumpListNextComponentAction(),
                                   new ActionFactory.ScrollUpAction(),
                                   new ActionFactory.ScrollDownAction(),
                                   new ActionFactory.BraceCodeSelectAction(),
                                   new ActionFactory.EscapeAction(),

                                   // Self test actions
                                   //      new EditorDebug.SelfTestAction(),
                                   //      new EditorDebug.DumpPlanesAction(),
                                   //      new EditorDebug.DumpSyntaxMarksAction()
                               };
        return TextAction.augmentList(org.netbeans.editor.view.ViewSettings.createActions(), baseActions);
    }

    protected Action[] getCustomActions() {
        Settings.KitAndValue kv[] = Settings.getKitAndValueArray(
                                        this.getClass(), Settings.CUSTOM_ACTION_LIST);
        if (kv.length == 0) {
            return null;
        }
        if (kv.length == 1) {
            List l = (List)kv[0].value;
            return (Action[])l.toArray(new Action[l.size()]);
        }
        // more than one list of actions
        List l = new ArrayList();
        for (int i = kv.length - 1; i >= 0; i--) { // from BaseKit down
            l.addAll((List)kv[i].value);
        }
        return (Action[])l.toArray(new Action[l.size()]);
    }

    /** Get actions associated with this kit. createActions() is called
    * to get basic list and then customActions are added.
    */
    public final Action[] getActions() {
        synchronized (BaseKit.class) { // possibly long running code follows
            Class thisClass = this.getClass();
            Action[] actions = (Action[])kitActions.get(thisClass);
            if (actions == null) {
                // create map of actions
                Action[] createdActions = createActions();
                updateActionSettings(createdActions, null, thisClass);
                Map actionMap = actionsToMap(createdActions);
                // add custom actions
                Action[] customActions = getCustomActions();
                if (customActions != null) {
                    updateActionSettings(customActions, null, thisClass);
                    actionMap.putAll(actionsToMap(customActions));
                }
                // store for later use
                kitActionMaps.put(thisClass, actionMap);
                // create action array and store for later use
                actions = mapToActions(actionMap);
                kitActions.put(thisClass, actions);
            }
            return actions;
        }
    }

    Map getActionMap() {
        Map actionMap = (Map)kitActionMaps.get(this.getClass());
        if (actionMap == null) {
            getActions(); // init action map
            actionMap = (Map)kitActionMaps.get(this.getClass());
        }
        return actionMap;
    }

    /** Get action from its name. */
    public Action getActionByName(String name) {
        return (name != null) ? (Action)getActionMap().get(name) : null;
    }

    public List translateActionNameList(List actionNameList) {
        List ret = new ArrayList();
        if (actionNameList != null) {
            Iterator i = actionNameList.iterator();
            while (i.hasNext()) {
                Action a = getActionByName((String)i.next());
                if (a != null) {
                    ret.add(a);
                }
            }
        }
        return ret;
    }



    /** Default typed action */
    public static class DefaultKeyTypedAction extends BaseAction {

        static final long serialVersionUID =3069164318144463899L;

        public DefaultKeyTypedAction() {
            super(defaultKeyTypedAction, MAGIC_POSITION_RESET | SAVE_POSITION);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if ((target != null) && (evt != null)) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                int mod = evt.getModifiers();

                if ((mod & ActionEvent.ALT_MASK) == 0 || (mod & ActionEvent.CTRL_MASK) != 0) {
                    actionPerformed(evt, target, false);
                }
            }
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target,
                                    boolean inputMethodsCall) {
            Caret caret = target.getCaret();
            BaseDocument doc = (BaseDocument)target.getDocument();
            ExtUI extUI = Utilities.getExtUI(target);
            Formatter f = Utilities.getFormatter(doc);
            // determine if typed char is valid
            String cmd = evt.getActionCommand();
            if ((cmd != null) && (cmd.length() == 1)) {
                //          Utilities.clearStatusText(target);

                char ch = cmd.charAt(0);
                if ((ch >= 0x20) && (ch != 0x7F)) { // valid character
                    extUI.getWordMatch().clear(); // reset word matching

                    // check for abbreviation
                    try {
                        boolean doInsert = extUI.getAbbrev().checkAndExpand(ch, evt);
                        if (doInsert) {
                            if (caret.isSelectionVisible()) { // valid selection
                                target.replaceSelection(cmd);
                            } else { // no selection
                                int dotPos = caret.getDot();
                                Boolean overwriteMode = (Boolean)extUI.getProperty(
                                                            ExtUI.OVERWRITE_MODE_PROPERTY);
                                if (overwriteMode != null && overwriteMode.booleanValue()
                                        && dotPos < doc.getLength() && doc.getChars(dotPos, 1)[0] != '\n'
                                   ) { // overwrite current char
                                    doc.remove(dotPos, 1);
                                    doc.insertString(dotPos, cmd, null);
                                } else { // insert mode
                                    doc.insertString(dotPos, cmd, null);
                                }
                            }
                        }
                        if (f.isHotChar(cmd.charAt(0))) {
                            f.updateRowIndent(doc, caret.getDot());
                        }
                    } catch (BadLocationException e) {
                        target.getToolkit().beep();
                    }
                }
            }
        }

    }

    public static class InsertBreakAction extends BaseAction {

        static final long serialVersionUID =7966576342334158659L;

        public InsertBreakAction() {
            super(insertBreakAction, MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                Caret caret = target.getCaret();
                int dotPos = caret.getDot();
                BaseDocument doc = (BaseDocument)target.getDocument();
                Utilities.getFormatter(doc).indentNewLine(doc, dotPos);
            }
        }

    }

    public static class InsertTabAction extends BaseAction {

        static final long serialVersionUID =-3379768531715989243L;

        public InsertTabAction() {
            super(insertTabAction, MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                Caret caret = target.getCaret();
                BaseDocument doc = (BaseDocument)target.getDocument();
                if (caret.isSelectionVisible()) { // block selected
                    try {
                        Utilities.getFormatter(doc).changeBlockIndent(doc,
                                target.getSelectionStart(), target.getSelectionEnd(), +1);
                    } catch (GuardedException e) {
                        target.getToolkit().beep();
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                } else { // no selected text
                    int dotPos = caret.getDot();
                    int caretCol;
                    // find caret column
                    try {
                        caretCol = doc.op.getVisColFromPos(dotPos);
                    } catch (BadLocationException e) {
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                            e.printStackTrace();
                        }
                        caretCol = 0;
                    }

                    try {
                        // find indent of the first previous non-white row
                        int upperCol = Utilities.getRowIndent(doc, dotPos, false);
                        if (upperCol == -1) { // no prev line with  indent
                            upperCol = 0;
                        }
                        // is there any char on this line before cursor?
                        int indent = Utilities.getRowIndent(doc, dotPos);
                        // test whether we should indent
                        if (indent == -1) {
                            if (upperCol > caretCol) { // upper indent is greater
                                indent = upperCol;
                            } else { // simulate insert tab by changing indent
                                indent = Utilities.getNextTabColumn(doc, dotPos);
                            }
                            Utilities.getFormatter(doc).changeRowIndent(doc, dotPos, indent);
                        } else { // already chars on the line
                            doc.insertString(dotPos, Utilities.getTabInsertString(doc, dotPos),
                                             null);
                        }
                    } catch (BadLocationException e) {
                        // use the same pos
                    }
                }
            }

        }

    }

    /** Compound action that encapsulates several actions */
    public static class CompoundAction extends BaseAction {

        Action[] actions;

        static final long serialVersionUID =1649688300969753758L;

        public CompoundAction(String nm, Action[] actions) {
            this(nm, 0, actions);
        }

        public CompoundAction(String nm, int resetMask, Action[] actions) {
            super(nm, resetMask);
            this.actions = actions;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                for (int i = 0; i < actions.length; i++) {
                    Action a = actions[i];
                    if (a instanceof BaseAction) {
                        ((BaseAction)a).actionPerformed(evt, target);
                    } else {
                        a.actionPerformed(evt);
                    }
                }
            }
        }
    }

    /** Compound action that gets and executes its actions
    * depending on the kit of the component.
    * The other advantage is that it doesn't create additional
    * instances of compound actions.
    */
    public static class KitCompoundAction extends BaseAction {

        private String[] actionNames;

        static final long serialVersionUID =8415246475764264835L;

        public KitCompoundAction(String nm, String[] actionNames) {
            this(nm, 0, actionNames);
        }

        public KitCompoundAction(String nm, int resetMask, String[] actionNames) {
            super(nm, resetMask);
            this.actionNames = actionNames;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                BaseKit kit = Utilities.getKit(target);
                if (kit != null) {
                    for (int i = 0; i < actionNames.length; i++) {
                        Action a = kit.getActionByName(actionNames[i]);
                        if (a != null) {
                            if (a instanceof BaseAction) {
                                ((BaseAction)a).actionPerformed(evt, target);
                            } else {
                                a.actionPerformed(evt);
                            }
                        }
                    }
                }
            }
        }
    }

    public static class InsertContentAction extends BaseAction {

        static final long serialVersionUID =5647751370952797218L;

        public InsertContentAction() {
            super(insertContentAction, MAGIC_POSITION_RESET | ABBREV_RESET
                  | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if ((target != null) && (evt != null)) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                String content = evt.getActionCommand();
                if (content != null) {
                    target.replaceSelection(content);
                } else {
                    target.getToolkit().beep();
                }
            }
        }
    }

    /** Insert text specified in constructor */
    public static class InsertStringAction extends BaseAction {

        String text;

        static final long serialVersionUID =-2755852016584693328L;

        public InsertStringAction(String nm, String text) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
            this.text = text;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                target.replaceSelection(text);
            }
        }
    }

    /** Remove previous or next character */
    public static class DeleteCharAction extends BaseAction {

        boolean nextChar;

        static final long serialVersionUID =-4321971925753148556L;

        DeleteCharAction(String nm, boolean nextChar) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
            this.nextChar = nextChar;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                try {
                    Document doc = target.getDocument();
                    Caret caret = target.getCaret();
                    int dot = caret.getDot();
                    int mark = caret.getMark();
                    if (dot != mark) { // remove selection
                        doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                    } else {
                        if (nextChar) { // remove next char
                            doc.remove(dot, 1);
                        } else { // remove previous char
                            doc.remove(dot - 1, 1);
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class ReadOnlyAction extends BaseAction {

        static final long serialVersionUID =9204335480208463193L;

        ReadOnlyAction() {
            super(readOnlyAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                target.setEditable(false);
            }
        }
    }

    public static class WritableAction extends BaseAction {

        static final long serialVersionUID =-5982547952800937954L;

        WritableAction() {
            super(writableAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                target.setEditable(true);
            }
        }
    }

    public static class CutAction extends BaseAction {

        static final long serialVersionUID =6377157040901778853L;

        public CutAction() {
            super(cutAction, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            setEnabled(false);
            putValue ("helpID", CutAction.class.getName ());
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                target.cut();
            }
        }
    }

    public static class CopyAction extends BaseAction {

        static final long serialVersionUID =-5119779005431986964L;

        public CopyAction() {
            super(copyAction, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            setEnabled(false);
            putValue ("helpID", CopyAction.class.getName ());
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                target.copy();
            }
        }
    }

    public static class PasteAction extends BaseAction {

        static final long serialVersionUID =5839791453996432149L;

        public PasteAction() {
            super(pasteAction, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            putValue ("helpID", PasteAction.class.getName ());
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                target.paste();
            }
        }
    }

    public static class BeepAction extends BaseAction {

        static final long serialVersionUID =-4474054576633223968L;

        public BeepAction() {
            super(beepAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                target.getToolkit().beep();
            }
        }
    }


    public static class UpAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =4621760742646981563L;

        public UpAction(String nm, boolean select) {
            super(nm, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                try {
                    Caret caret = target.getCaret();
                    int dot = caret.getDot();
                    Point p = caret.getMagicCaretPosition();
                    if (p == null) {
                        Rectangle r = target.modelToView(dot);
                        p = new Point(r.x, r.y);
                        caret.setMagicCaretPosition(p);
                    }
                    try {
                        dot = Utilities.getPositionAbove(target, dot, p.x);
                        if (select) {
                            caret.moveDot(dot);
                        } else {
                            Utilities.getExtUI(target).caretSetDot(dot, null, ExtUI.SCROLL_MOVE);
                        }
                    } catch (BadLocationException e) {
                        // the position stays the same
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class DownAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =-5635702355125266822L;

        public DownAction(String nm, boolean select) {
            super(nm, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                try {
                    Caret caret = target.getCaret();
                    int dot = caret.getDot();
                    Point p = caret.getMagicCaretPosition();
                    if (p == null) {
                        Rectangle r = target.modelToView(dot);
                        p = new Point(r.x, r.y);
                        caret.setMagicCaretPosition(p);
                    }
                    try {
                        dot = Utilities.getPositionBelow(target, dot, p.x);
                        if (select) {
                            caret.moveDot(dot);
                        } else {
                            Utilities.getExtUI(target).caretSetDot(dot, null, ExtUI.SCROLL_MOVE);
                        }
                    } catch (BadLocationException e) {
                        // position stays the same
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    /** Go one page up */
    public static class PageUpAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =-3107382148581661079L;

        public PageUpAction(String nm, boolean select) {
            super(nm, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                try {
                    Caret caret = target.getCaret();
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    int dot = caret.getDot();
                    Rectangle tgtRect = ((BaseTextUI)target.getUI()).modelToView(target, dot);
                    Point p = caret.getMagicCaretPosition();
                    if (p == null) {
                        p = new Point((int)tgtRect.x, (int)tgtRect.y);
                        caret.setMagicCaretPosition(p);
                    } else {
                        p.y = (int)tgtRect.y;
                    }
                    ExtUI extUI = ((BaseTextUI)target.getUI()).getExtUI();
                    Rectangle bounds = extUI.getExtentBounds();
                    int baseY = (bounds.y + tgtRect.height -1)
                                / tgtRect.height * tgtRect.height;
                    int lines = (int)(bounds.height / tgtRect.height);
                    int baseHeight =  lines * tgtRect.height;
                    tgtRect.y = Math.max(baseY - baseHeight, 0);
                    tgtRect.height = bounds.height;
                    p.y = (int)Math.max(p.y - baseHeight, 0);
                    int newDot = target.viewToModel(p);
                    extUI.scrollRectToVisible(tgtRect, ExtUI.SCROLL_DEFAULT);
                    if (select) {
                        caret.moveDot(newDot);
                    } else {
                        caret.setDot(newDot);
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class ForwardAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =8007293230193334414L;

        public ForwardAction(String nm, boolean select) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int dot = target.getUI().getNextVisualPositionFrom(target,
                              caret.getDot(), null, SwingConstants.EAST, null);
                    if (select) {
                        caret.moveDot(dot);
                    } else {
                        caret.setDot(dot);
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    /** Go one page down */
    public static class PageDownAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =8942534850985048862L;

        public PageDownAction(String nm, boolean select) {
            super(nm, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                try {
                    Caret caret = target.getCaret();
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    int dot = caret.getDot();
                    Rectangle tgtRect = ((BaseTextUI)target.getUI()).modelToView(target, dot);
                    Point p = caret.getMagicCaretPosition();
                    if (p == null) {
                        p = new Point(tgtRect.x, tgtRect.y);
                        caret.setMagicCaretPosition(p);
                    } else {
                        p.y = tgtRect.y;
                    }
                    ExtUI extUI = ((BaseTextUI)target.getUI()).getExtUI();
                    Rectangle bounds = extUI.getExtentBounds();
                    int baseY = bounds.y / tgtRect.height * tgtRect.height;
                    int lines = bounds.height / tgtRect.height;
                    int baseHeight =  lines * tgtRect.height;
                    tgtRect.y = Math.max(baseY + baseHeight, 0);
                    tgtRect.height = bounds.height;
                    p.y = Math.max(p.y + baseHeight, 0);
                    int newDot = target.viewToModel(p);
                    extUI.scrollRectToVisible(tgtRect, ExtUI.SCROLL_DEFAULT);
                    if (select) {
                        caret.moveDot(newDot);
                    } else {
                        extUI.caretSetDot(newDot, null, ExtUI.SCROLL_MOVE);
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class BackwardAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =-3048379822817847356L;

        public BackwardAction(String nm, boolean select) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int dot = target.getUI().getNextVisualPositionFrom(target,
                              caret.getDot(), null, SwingConstants.WEST, null);
                    if (select) {
                        caret.moveDot(dot);
                    } else {
                        Utilities.getExtUI(target).caretSetDot(dot, null, ExtUI.SCROLL_MOVE);
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class BeginLineAction extends BaseAction {

        boolean select;

        boolean homeKeyColumnOne;

        static final long serialVersionUID =3269462923524077779L;

        public BeginLineAction(String nm, boolean select) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET);
            this.select = select;
        }

        protected void settingsChange(SettingsChangeEvent evt, Class kitClass) {
            homeKeyColumnOne = SettingsUtil.getBoolean(kitClass,
                               Settings.HOME_KEY_COLUMN_ONE, false);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                BaseDocument doc = (BaseDocument)target.getDocument();
                try {
                    int dot = caret.getDot();
                    int lineStartPos = Utilities.getRowStart(doc, dot, 0);
                    if (homeKeyColumnOne) { // to first column
                        dot = lineStartPos;
                    } else { // either to line start or text start
                        int textStartPos = Utilities.getRowFirstNonWhite(doc, lineStartPos);
                        if (textStartPos < 0) { // no text on the line
                            textStartPos = Utilities.getRowEnd(doc, lineStartPos);
                        }
                        if (dot == lineStartPos) { // go to the text start pos
                            dot = textStartPos;
                        } else if (dot <= textStartPos) {
                            dot = lineStartPos;
                        } else {
                            dot = textStartPos;
                        }
                    }
                    if (select) {
                        caret.moveDot(dot);
                    } else {
                        caret.setDot(dot);
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class EndLineAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =5216077634055190170L;

        public EndLineAction(String nm, boolean select) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int dot = Utilities.getRowEnd(target, caret.getDot());
                    if (select) {
                        caret.moveDot(dot);
                    } else {
                        caret.setDot(dot);
                    }
                    // now move the magic caret position far to the right
                    Rectangle r = target.modelToView(dot);
                    Point p = new Point(Integer.MAX_VALUE / 2, r.y);
                    caret.setMagicCaretPosition(p);
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class BeginAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =3463563396210234361L;

        public BeginAction(String nm, boolean select) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET | SAVE_POSITION);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                int dot = 0; // begin of document
                if (select) {
                    caret.moveDot(dot);
                } else {
                    caret.setDot(dot);
                }
            }
        }
    }

    public static class EndAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =8547506353130203657L;

        public EndAction(String nm, boolean select) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET | SAVE_POSITION);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                int dot = target.getDocument().getLength(); // end of document
                if (select) {
                    caret.moveDot(dot);
                } else {
                    caret.setDot(dot);
                }
            }
        }
    }

    public static class NextWordAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =-5909906947175434032L;

        public NextWordAction(String nm, boolean select) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int dotPos = caret.getDot();
                    dotPos = Utilities.getNextWord(target, dotPos);
                    if (select) {
                        caret.moveDot(dotPos);
                    } else {
                        caret.setDot(dotPos);
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class PreviousWordAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =-5465143382669785799L;

        public PreviousWordAction(String nm, boolean select) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int dot = Utilities.getPreviousWord(target, caret.getDot());
                    if (select) {
                        caret.moveDot(dot);
                    } else {
                        caret.setDot(dot);
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class BeginWordAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =3991338381212491110L;

        BeginWordAction(String nm, boolean select) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int dot = Utilities.getWordStart(target, caret.getDot());
                    if (select) {
                        caret.moveDot(dot);
                    } else {
                        caret.setDot(dot);
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class EndWordAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =3812523676620144633L;

        EndWordAction(String nm, boolean select) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int dot = Utilities.getWordEnd(target, caret.getDot());
                    if (select) {
                        caret.moveDot(dot);
                    } else {
                        caret.setDot(dot);
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    /** Select word around caret */
    public static class SelectWordAction extends KitCompoundAction {

        static final long serialVersionUID =7678848538073016357L;

        SelectWordAction() {
            super(selectWordAction,
                  new String[] {
                      beginWordAction,
                      selectionEndWordAction
                  }
                 );
        }

    }

    /** Select line around caret */
    public static class SelectLineAction extends KitCompoundAction {

        static final long serialVersionUID =-7407681863035740281L;

        SelectLineAction() {
            super(selectLineAction,
                  new String[] {
                      beginLineAction,
                      selectionEndLineAction
                  }
                 );
        }

    }

    /** Select text of whole document */
    public static class SelectAllAction extends KitCompoundAction {

        static final long serialVersionUID =-3502499718130556524L;

        SelectAllAction() {
            super(selectAllAction,
                  new String[] {
                      beginAction,
                      selectionEndAction
                  }
                 );
        }

    }

}

/*
 * Log
 *  47   Gandalf-post-FCS1.43.1.2    4/3/00   Miloslav Metelka undo update
 *  46   Gandalf-post-FCS1.43.1.1    3/9/00   Miloslav Metelka missed actions fix
 *  45   Gandalf-post-FCS1.43.1.0    3/8/00   Miloslav Metelka 
 *  44   Gandalf   1.43        2/14/00  Miloslav Metelka tooltips fix
 *  43   Gandalf   1.42        1/19/00  Jesse Glick     Context help.
 *  42   Gandalf   1.41        1/14/00  Miloslav Metelka 
 *  41   Gandalf   1.40        1/14/00  Miloslav Metelka -Dnetbeans.editor.noinputmethods
 *        added
 *  40   Gandalf   1.39        1/13/00  Miloslav Metelka 
 *  39   Gandalf   1.38        1/11/00  Miloslav Metelka 
 *  38   Gandalf   1.37        1/10/00  Miloslav Metelka 
 *  37   Gandalf   1.36        1/7/00   Miloslav Metelka 
 *  36   Gandalf   1.35        1/6/00   Miloslav Metelka 
 *  35   Gandalf   1.34        1/6/00   Miloslav Metelka 
 *  34   Gandalf   1.33        1/4/00   Miloslav Metelka 
 *  33   Gandalf   1.32        12/28/99 Miloslav Metelka 
 *  32   Gandalf   1.31        11/24/99 Miloslav Metelka 
 *  31   Gandalf   1.30        11/14/99 Miloslav Metelka 
 *  30   Gandalf   1.29        11/10/99 Miloslav Metelka 
 *  29   Gandalf   1.28        11/9/99  Miloslav Metelka 
 *  28   Gandalf   1.27        11/8/99  Miloslav Metelka 
 *  27   Gandalf   1.26        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  26   Gandalf   1.25        10/10/99 Miloslav Metelka 
 *  25   Gandalf   1.24        10/4/99  Miloslav Metelka 
 *  24   Gandalf   1.23        9/30/99  Miloslav Metelka 
 *  23   Gandalf   1.22        9/15/99  Miloslav Metelka 
 *  22   Gandalf   1.21        9/10/99  Miloslav Metelka 
 *  21   Gandalf   1.20        8/27/99  Miloslav Metelka 
 *  20   Gandalf   1.19        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  19   Gandalf   1.18        7/26/99  Miloslav Metelka 
 *  18   Gandalf   1.17        7/22/99  Miloslav Metelka 
 *  17   Gandalf   1.16        7/21/99  Miloslav Metelka 
 *  16   Gandalf   1.15        7/20/99  Miloslav Metelka 
 *  15   Gandalf   1.14        7/9/99   Miloslav Metelka 
 *  14   Gandalf   1.13        7/2/99   Miloslav Metelka 
 *  13   Gandalf   1.12        6/29/99  Miloslav Metelka Scrolling and patches
 *  12   Gandalf   1.11        6/25/99  Miloslav Metelka from floats back to ints
 *  11   Gandalf   1.10        6/22/99  Miloslav Metelka 
 *  10   Gandalf   1.9         6/11/99  Miloslav Metelka 
 *  9    Gandalf   1.8         6/10/99  Miloslav Metelka 
 *  8    Gandalf   1.7         6/8/99   Miloslav Metelka 
 *  7    Gandalf   1.6         6/1/99   Miloslav Metelka 
 *  6    Gandalf   1.5         5/24/99  Miloslav Metelka 
 *  5    Gandalf   1.4         5/21/99  Miloslav Metelka 
 *  4    Gandalf   1.3         5/15/99  Miloslav Metelka fixes
 *  3    Gandalf   1.2         5/13/99  Miloslav Metelka 
 *  2    Gandalf   1.1         5/7/99   Miloslav Metelka line numbering and fixes
 *  1    Gandalf   1.0         5/5/99   Miloslav Metelka 
 * $
 */
