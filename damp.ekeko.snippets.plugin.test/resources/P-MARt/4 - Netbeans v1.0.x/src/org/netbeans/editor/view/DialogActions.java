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

package org.netbeans.editor.view;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.DefaultSettings;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.MultiKeyBinding;

/** All actions related to functionality in this package
* are located here.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class DialogActions implements Settings.Initializer {

    /** Open find dialog action */
    public static final String findAction = "find"; // NOI18N

    /** Open goto dialog action */
    public static final String gotoAction = "goto"; // NOI18N

    /** Replace action */
    public static final String replaceAction = "replace"; // NOI18N

    protected Action[] actions;

    JTextComponent.KeyBinding[] keyBindings;

    protected Action[] createActions() {
        return new Action[] {
                   new FindAction(),
                   new ReplaceAction(),
                   new GotoAction()
               };
    }

    public Action[] getActions() {
        if (actions == null) {
            actions = createActions();
        }
        return actions;
    }

    protected JTextComponent.KeyBinding[] createKeyBindings() {
        return new JTextComponent.KeyBinding[] {
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK),
                       findAction
                   ),
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK),
                       replaceAction
                   ),
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK),
                       gotoAction
                   )
               };
    }

    public JTextComponent.KeyBinding[] getKeyBindings() {
        if (keyBindings == null) {
            keyBindings = createKeyBindings();
        }
        return keyBindings;
    }

    public Map updateSettingsMap(Class kitClass, Map m) {
        if (kitClass == BaseKit.class && m != null) {
            // add actions
            Action[] acts = getActions();
            SettingsUtil.updateListSetting(m, Settings.CUSTOM_ACTION_LIST, acts);

            // add key bindings
            JTextComponent.KeyBinding[] bnds = getKeyBindings();
            SettingsUtil.updateListSetting(m, Settings.KEY_BINDING_LIST, bnds);
        }
        return m;
    }

    public static class FindAction extends BaseAction {

        static final long serialVersionUID =719554648887497427L;
        public FindAction() {
            super(findAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | NO_RECORDING);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                DialogSupport.getFindDialogSupport().showFindDialog();
            }
        }

    }

    public static class ReplaceAction extends BaseAction {

        static final long serialVersionUID =1828017436079834384L;
        public ReplaceAction() {
            super(replaceAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | NO_RECORDING);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                DialogSupport.getFindDialogSupport().showReplaceDialog();
            }
        }

    }

    public static class GotoAction extends BaseAction {

        static final long serialVersionUID =8425585413146373256L;
        public GotoAction() {
            super(gotoAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                DialogSupport.getGotoDialogSupport().showGotoDialog();
            }
        }

    }

}

/*
 * Log
 *  10   Gandalf   1.9         1/13/00  Miloslav Metelka Localization
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         9/30/99  Miloslav Metelka 
 *  7    Gandalf   1.6         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  6    Gandalf   1.5         7/29/99  Miloslav Metelka 
 *  5    Gandalf   1.4         7/20/99  Miloslav Metelka 
 *  4    Gandalf   1.3         7/2/99   Miloslav Metelka 
 *  3    Gandalf   1.2         6/22/99  Miloslav Metelka 
 *  2    Gandalf   1.1         5/16/99  Miloslav Metelka 
 *  1    Gandalf   1.0         5/5/99   Miloslav Metelka 
 * $
 */

