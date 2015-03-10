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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.JList;
import javax.swing.Timer;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.Caret;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.ActionFactory;

/**
* Editor kit implementation for Java content type
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaKit extends BaseKit {

    private static final String[] COMPLETION_SPACE_POPUP_STRINGS = new String[] {
                "new ", ", " // NOI18N
            };

    /** Switch first letter of word to capital and insert 'set'
    * at word begining.
    */
    public static final String makeSetterAction = "make-setter"; // NOI18N

    /** Switch first letter of word to capital and insert 'get'
    * at word begining.
    */
    public static final String makeGetterAction = "make-getter"; // NOI18N

    /** Switch first letter of word to capital and insert 'is'
    * at word begining.
    */
    public static final String makeIsAction = "make-is"; // NOI18N

    /** Show java-completion listbox and find the information
    * depending on context
    */
    public static final String jCompletionShowHelpAction = "jcompletion-show-help"; // NOI18N

    /** Hide java-completion listbox */
    public static final String jCompletionHideAction = "jcompletion-hide"; // NOI18N

    /** Comment out the selected block */
    public static final String commentAction = "comment"; // NOI18N

    /** Uncomment the selected block */
    public static final String uncommentAction = "uncomment"; // NOI18N

    /** Debug source and line number */
    public static final String abbrevDebugLineAction = "abbrev-debug-line"; // NOI18N

    static final long serialVersionUID =2294040484136122149L;

    /** Create caret to navigate through document */
    public Caret createCaret() {
        return new ExtCaret();
        //    return new javax.swing.text.DefaultCaret();
    }

    /** Create new instance of syntax coloring parser */
    public Syntax createSyntax(BaseDocument doc) {
        boolean doJavadocColoring = SettingsUtil.getBoolean(
                                        this.getClass(), ExtSettings.JAVADOC_SYNTAX_COLORING, false);
        if (doJavadocColoring) {
            return new ExtJavaSyntax();
        } else {
            return new JavaSyntax();
        }
    }

    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new JavaSyntaxSupport(doc);
    }

    protected Action[] createActions() {
        Action[] javaActions = new Action[] {
                                   new ExtActionFactory.GotoDeclarationAction(),
                                   new JavaDefaultKeyTypedAction(),
                                   new JavaInsertTabAction(),
                                   new JavaInsertBreakAction(),
                                   new JavaUpAction(upAction, false),
                                   new JavaPageUpAction(pageUpAction, false),
                                   new JavaDownAction(downAction, false),
                                   new JavaPageDownAction(pageDownAction, false),
                                   new JavaForwardAction(forwardAction, false),
                                   new JavaBackwardAction(backwardAction, false),
                                   new JavaNextWordAction(nextWordAction, false),
                                   new JavaPreviousWordAction(previousWordAction, false),
                                   new JavaBeginLineAction(beginLineAction, false),
                                   new JavaEndLineAction(endLineAction, false),
                                   new JavaRemoveWordAction(),
                                   new PrefixMakerAction(makeSetterAction, "set"), // NOI18N
                                   new PrefixMakerAction(makeGetterAction, "get"), // NOI18N
                                   new PrefixMakerAction(makeIsAction, "is"), // NOI18N
                                   new CommentAction(),
                                   new UncommentAction(),
                                   new JCompletionShowHelpAction(),
                                   new JCompletionHideAction(),
                                   new AbbrevDebugLineAction(),
                                   new JavaEscapeAction(),
                                   new JavaWordMatchAction(wordMatchNextAction, true),
                                   new JavaWordMatchAction(wordMatchPrevAction, false),
                               };
        return TextAction.augmentList(super.createActions(), javaActions);
    }

    // Java Completion customized actions
    public static class JavaDefaultKeyTypedAction extends DefaultKeyTypedAction {

        boolean jcAutoPopup;

        Timer jcPopupTimer;

        private boolean inited;

        private int maxSPSLen = -1;

        static final long serialVersionUID =5273032708909044812L;

        public JavaDefaultKeyTypedAction() {
            jcPopupTimer  = new Timer(0,
                                      new ActionListener() {
                                          public void actionPerformed(ActionEvent evt) {
                                              JTextComponent target = Utilities.getLastActiveComponent();
                                              if (target != null) {
                                                  BaseKit kit = Utilities.getKit(target);
                                                  BaseAction a = (BaseAction)kit.getActionByName(jCompletionShowHelpAction);
                                                  if (a != null) {
                                                      a.actionPerformed(new ActionEvent(target, 0, ""), target); // NOI18N
                                                  }
                                              }
                                          }
                                      }
                                     );
            jcPopupTimer.setRepeats(false);
        }

        protected String[] getCompletionSpacePopupStrings() {
            return COMPLETION_SPACE_POPUP_STRINGS;
        }

        private int getMaxSPSLength() {
            if (maxSPSLen < 0) {
                maxSPSLen = 0;
                String[] sps = getCompletionSpacePopupStrings();
                if (sps != null) {
                    for (int i = sps.length - 1; i >= 0; i--) {
                        maxSPSLen = Math.max(maxSPSLen, sps[i].length());
                    }
                }
            }
            return maxSPSLen;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            String cmd = evt.getActionCommand();
            int mod = evt.getModifiers();

            // Dirty fix for JCompletion
            if (cmd != null && cmd.equals(" ") && ((mod & ActionEvent.CTRL_MASK) != 0)) { // NOI18N
                // Ctrl + SPACE
            } else {
                super.actionPerformed(evt, target);
            }

            if ((target != null) && (evt != null)) {
                if (!inited) { // init settings
                    inited = true;
                    Class kitClass = Utilities.getKitClass(target);
                    jcAutoPopup = SettingsUtil.getBoolean(kitClass,
                                                          ExtSettings.JCOMPLETION_AUTO_POPUP,
                                                          ExtSettings.defaultJCAutoPopup);

                    int delay = SettingsUtil.getInteger(kitClass,
                                                        ExtSettings.JCOMPLETION_AUTO_POPUP_DELAY,
                                                        ExtSettings.defaultJCAutoPopupDelay);

                    jcPopupTimer.setInitialDelay(delay);
                    jcPopupTimer.setDelay(delay);
                }

                if ((cmd != null) && (cmd.length() == 1) &&
                        ((mod & ActionEvent.ALT_MASK) == 0
                         && (mod & ActionEvent.CTRL_MASK) == 0)
                   ) {
                    char ch = cmd.charAt(0);

                    if (jcAutoPopup) {
                        switch (ch) {
                        case ' ':
                            int dotPos = target.getCaret().getDot();
                            boolean pop = false;
                            int maxLen = Math.min(getMaxSPSLength(), dotPos);
                            if (maxLen > 0) {
                                BaseDocument doc = (BaseDocument)target.getDocument();
                                try {
                                    String txt = doc.getText(dotPos - maxLen, maxLen);
                                    String[] sps = getCompletionSpacePopupStrings();
                                    for (int i = sps.length - 1; i >= 0; i--) {
                                        if (txt.endsWith(sps[i])) {
                                            pop = true;
                                        }
                                    }
                                } catch (BadLocationException e) {
                                }
                            }

                            if (pop) {
                                jcPopupTimer.restart();
                            } else {
                                jcPopupTimer.stop();
                            }
                            break;

                        case '.':
                        case ',':
                            jcPopupTimer.restart();
                            break;

                        default:
                            jcPopupTimer.stop();
                            break;
                        }
                    }

                    if (JCView.isViewVisible(target)) {
                        switch (cmd.charAt(0)) {
                        case '=':
                        case '{':
                        case ';':
                            JCView.setViewVisible(target, false);
                            break;

                        default:
                            JCView.refreshView(target, true);
                            break;
                        }
                    }
                }
            }
        }

    }

    public static class JavaInsertBreakAction extends InsertBreakAction {

        static final long serialVersionUID =4004043376345356060L;

        public JavaInsertBreakAction() {
            super();
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (JCView.isViewVisible(target)) {
                    JCView view = JCView.getView(target);
                    if (view.updateText()) { // text successfully updated
                        view.setVisible(false);
                    } else { // the query result was null
                        view.showHelp(false); // show fresh help immediately
                    }
                } else {
                    super.actionPerformed(evt, target);
                }
            }
        }

    }

    public static class JavaInsertTabAction extends InsertTabAction {

        static final long serialVersionUID =2711045528538714986L;

        public JavaInsertTabAction() {
            super();
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (JCView.isViewVisible(target)) {
                    JCView view = JCView.getView(target);
                    JCView.refreshView(target, false); // update so that the query result is up to date
                    view.updateCommonText();
                    JCView.refreshView(target, false);
                } else {
                    super.actionPerformed(evt, target);
                }
            }
        }

    }

    public static class JavaUpAction extends UpAction {

        static final long serialVersionUID =9039435547323841544L;

        JavaUpAction(String nm, boolean select) {
            super(nm, select);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (JCView.isViewVisible(target)) {
                    JCView.getView(target).moveUp();
                } else {
                    super.actionPerformed(evt, target);
                }
            }
        }
    }

    public static class JavaDownAction extends DownAction {

        static final long serialVersionUID =5728488498924841815L;

        JavaDownAction(String nm, boolean select) {
            super(nm, select);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (JCView.isViewVisible(target)) {
                    JCView.getView(target).moveDown();
                } else {
                    super.actionPerformed(evt, target);
                }
            }
        }
    }

    public static class JavaPageUpAction extends PageUpAction {

        static final long serialVersionUID =4336328849128594564L;

        JavaPageUpAction(String nm, boolean select) {
            super(nm, select);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (JCView.isViewVisible(target)) {
                    JCView.getView(target).movePageUp();
                } else {
                    super.actionPerformed(evt, target);
                }
            }
        }
    }

    public static class JavaForwardAction extends ForwardAction {

        static final long serialVersionUID =4144886454849169492L;

        JavaForwardAction(String nm, boolean select) {
            super(nm, select);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            super.actionPerformed(evt, target);
            JCView.refreshView(target, true);
        }
    }

    public static class JavaPageDownAction extends PageDownAction {

        static final long serialVersionUID =-2373608546477823838L;

        JavaPageDownAction(String nm, boolean select) {
            super(nm, select);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (JCView.isViewVisible(target)) {
                    JCView.getView(target).movePageDown();
                } else {
                    super.actionPerformed(evt, target);
                }
            }
        }
    }

    public static class JavaBackwardAction extends BackwardAction {

        static final long serialVersionUID =628388161377998033L;

        JavaBackwardAction(String nm, boolean select) {
            super(nm, select);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            super.actionPerformed(evt, target);
            JCView.refreshView(target, true);
        }
    }

    public static class JavaNextWordAction extends NextWordAction {

        JavaNextWordAction(String nm, boolean select) {
            super(nm, select);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            super.actionPerformed(evt, target);
            JCView.refreshView(target, true);
        }
    }

    public static class JavaPreviousWordAction extends PreviousWordAction {

        JavaPreviousWordAction(String nm, boolean select) {
            super(nm, select);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            super.actionPerformed(evt, target);
            JCView.refreshView(target, true);
        }
    }

    public static class JavaBeginLineAction extends BeginLineAction {

        static final long serialVersionUID =1226924439847693361L;

        public JavaBeginLineAction(String nm, boolean select) {
            super(nm, select);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (JCView.isViewVisible(target)) {
                    JCView.getView(target).moveBegin();
                } else {
                    super.actionPerformed(evt, target);
                }
            }
        }
    }

    public static class JavaEndLineAction extends EndLineAction {

        static final long serialVersionUID =-3703912765497771472L;

        public JavaEndLineAction(String nm, boolean select) {
            super(nm, select);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (JCView.isViewVisible(target)) {
                    JCView.getView(target).moveEnd();
                } else {
                    super.actionPerformed(evt, target);
                }
            }
        }
    }

    public static class JavaRemoveWordAction extends ActionFactory.RemoveWordAction {

        static final long serialVersionUID =-27616055927982902L;
        public JavaRemoveWordAction() {
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            super.actionPerformed(evt, target);
            JCView.refreshView(target, true);
        }

    }

    public static class JCompletionShowHelpAction extends BaseAction {

        static final long serialVersionUID =1050644925893851146L;

        public JCompletionShowHelpAction() {
            super(jCompletionShowHelpAction);
        }

        public JCompletionShowHelpAction(String name) { // !!! patch for 1.3
            super(name);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                JCView.setViewVisible(target, true);
            }
        }

    }

    public static class JCompletionHideAction extends BaseAction {

        static final long serialVersionUID =-9162014350666711948L;

        public JCompletionHideAction() {
            super(jCompletionHideAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                JCView.setViewVisible(target, false);
            }
        }

    }

    public static class JavaEscapeAction extends ActionFactory.EscapeAction {

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            super.actionPerformed(evt, target);
            JCView.setViewVisible(target, false);
        }

    }

    public static class JavaWordMatchAction extends ActionFactory.WordMatchAction {

        public JavaWordMatchAction(String name, boolean direction) {
            super(name, direction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            super.actionPerformed(evt, target);
            JCView.refreshView(target, false);
        }

    }

    public static class PrefixMakerAction extends BaseAction {

        static final long serialVersionUID =-2305157963664484920L;

        static final String[] prefixes = { "get", "set", "is" }; // NOI18N
        private String prefix;

        PrefixMakerAction(String name, String prefix) {
            super(name);
            this.prefix = prefix;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                BaseDocument doc = (BaseDocument)target.getDocument();
                int dotPos = target.getCaret().getDot();
                try {
                    // look for identifier around caret
                    int[] block = org.netbeans.editor.Utilities.getIdentifierBlock(doc, dotPos);

                    // If there is no identifier around, warn user
                    if (block == null) {
                        target.getToolkit().beep();
                        return;
                    }

                    // Get the identifier to operate on
                    String identifier = doc.getText(block[0], block[1] - block[0]);

                    // Handle the case we already have the work done - e.g. if we got called over 'getValue'
                    if (identifier.startsWith(prefix) && Character.isUpperCase(identifier.charAt(prefix.length()))) return;

                    // Handle the case we have other type of known xEr: eg isRunning -> getRunning
                    for (int i=0; i<prefixes.length; i++) {
                        String actPref = prefixes[i];
                        if (identifier.startsWith(actPref)
                                && identifier.length() > actPref.length()
                                && Character.isUpperCase(identifier.charAt(actPref.length()))
                           ) {
                            doc.remove(block[0], actPref.length());
                            doc.insertString(block[0], prefix, null);
                            return;
                        }
                    }

                    // Upcase the first letter
                    Utilities.changeCase(doc, block[0], 1, Utilities.CASE_UPPER);
                    // Prepend the prefix before it
                    doc.insertString(block[0], prefix, null);
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class CommentAction extends BaseAction {

        static final long serialVersionUID =-1422954906554289179L;
        public CommentAction() {
            super(commentAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                Caret caret = target.getCaret();
                BaseDocument doc = (BaseDocument)target.getDocument();
                try {
                    if (caret.isSelectionVisible()) {
                        int startPos = Utilities.getRowStart(doc, target.getSelectionStart());
                        int endPos = target.getSelectionEnd();
                        doc.atomicLock();
                        try {

                            if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                                endPos--;
                            }

                            int pos = startPos;
                            for (int lineCnt = Utilities.getRowCount(doc, startPos, endPos);
                                    lineCnt > 0; lineCnt--
                                ) {
                                doc.insertString(pos, "//", null); // NOI18N
                                pos = Utilities.getRowStart(doc, pos, +1);
                            }

                        } finally {
                            doc.atomicUnlock();
                        }
                    } else { // selection not visible
                        doc.insertString(Utilities.getRowStart(doc, target.getSelectionStart()),
                                         "//", null); // NOI18N
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }

    }

    public static class UncommentAction extends BaseAction {

        static final long serialVersionUID =-7005758666529862034L;
        public UncommentAction() {
            super(uncommentAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                Caret caret = target.getCaret();
                BaseDocument doc = (BaseDocument)target.getDocument();
                try {
                    if (caret.isSelectionVisible()) {
                        int startPos = Utilities.getRowStart(doc, target.getSelectionStart());
                        int endPos = target.getSelectionEnd();
                        doc.atomicLock();
                        try {

                            if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                                endPos--;
                            }

                            int pos = startPos;
                            for (int lineCnt = Utilities.getRowCount(doc, startPos, endPos);
                                    lineCnt > 0; lineCnt--
                                ) {
                                if (Utilities.getRowEnd(doc, pos) - pos >= 2
                                        && doc.getText(pos, 2).equals("//") // NOI18N
                                   ) {
                                    doc.remove(pos, 2);
                                }
                                pos = Utilities.getRowStart(doc, pos, +1);
                            }

                        } finally {
                            doc.atomicUnlock();
                        }
                    } else { // selection not visible
                        int pos = Utilities.getRowStart(doc, caret.getDot());
                        if (Utilities.getRowEnd(doc, pos) - pos >= 2
                                && doc.getText(pos, 2).equals("//") // NOI18N
                           ) {
                            doc.remove(pos, 2);
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }

    }

    public static class AbbrevDebugLineAction extends BaseAction {

        public AbbrevDebugLineAction() {
            super(abbrevDebugLineAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                BaseDocument doc = (BaseDocument)target.getDocument();
                StringBuffer sb = new StringBuffer("System.out.println(\""); // NOI18N
                String title = (String)doc.getProperty(Document.TitleProperty);
                if (title != null) {
                    sb.append(title);
                    sb.append(':');
                }
                try {
                    sb.append(Utilities.getLineOffset(doc, target.getCaret().getDot()) + 1);
                } catch (BadLocationException e) {
                }
                sb.append(' ');

                BaseKit kit = Utilities.getKit(target);
                Action a = kit.getActionByName(BaseKit.insertContentAction);
                if (a != null) {
                    Utilities.performAction(
                        a,
                        new ActionEvent(target, ActionEvent.ACTION_PERFORMED, sb.toString()),
                        target
                    );
                }
            }
        }

    }

}

/*
 * Log
 *  37   Gandalf-post-FCS1.35.1.0    3/8/00   Miloslav Metelka 
 *  36   Gandalf   1.35        1/18/00  Miloslav Metelka 
 *  35   Gandalf   1.34        1/15/00  Miloslav Metelka #5270
 *  34   Gandalf   1.33        1/13/00  Miloslav Metelka Localization
 *  33   Gandalf   1.32        1/10/00  Miloslav Metelka 
 *  32   Gandalf   1.31        1/7/00   Miloslav Metelka 
 *  31   Gandalf   1.30        1/4/00   Miloslav Metelka 
 *  30   Gandalf   1.29        12/28/99 Miloslav Metelka 
 *  29   Gandalf   1.28        11/27/99 Patrik Knakal   
 *  28   Gandalf   1.27        11/24/99 Miloslav Metelka 
 *  27   Gandalf   1.26        11/15/99 Miloslav Metelka 
 *  26   Gandalf   1.25        11/14/99 Miloslav Metelka 
 *  25   Gandalf   1.24        11/10/99 Miloslav Metelka 
 *  24   Gandalf   1.23        11/9/99  Miloslav Metelka 
 *  23   Gandalf   1.22        11/8/99  Miloslav Metelka 
 *  22   Gandalf   1.21        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  21   Gandalf   1.20        10/10/99 Miloslav Metelka 
 *  20   Gandalf   1.19        10/7/99  Petr Nejedly    Added PrefixMaker action
 *  19   Gandalf   1.18        10/4/99  Miloslav Metelka 
 *  18   Gandalf   1.17        10/4/99  Miloslav Metelka fixed End key
 *  17   Gandalf   1.16        9/30/99  Miloslav Metelka 
 *  16   Gandalf   1.15        9/15/99  Miloslav Metelka 
 *  15   Gandalf   1.14        9/10/99  Miloslav Metelka 
 *  14   Gandalf   1.13        8/18/99  Miloslav Metelka 
 *  13   Gandalf   1.12        8/17/99  Miloslav Metelka 
 *  12   Gandalf   1.11        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  11   Gandalf   1.10        7/30/99  Miloslav Metelka 
 *  10   Gandalf   1.9         7/26/99  Miloslav Metelka 
 *  9    Gandalf   1.8         7/21/99  Miloslav Metelka 
 *  8    Gandalf   1.7         7/21/99  Miloslav Metelka 
 *  7    Gandalf   1.6         7/20/99  Miloslav Metelka 
 *  6    Gandalf   1.5         6/10/99  Miloslav Metelka 
 *  5    Gandalf   1.4         6/10/99  Miloslav Metelka 
 *  4    Gandalf   1.3         6/8/99   Miloslav Metelka 
 *  3    Gandalf   1.2         6/1/99   Miloslav Metelka 
 *  2    Gandalf   1.1         5/7/99   Miloslav Metelka line numbering and fixes
 *  1    Gandalf   1.0         5/5/99   Miloslav Metelka 
 * $
 */

