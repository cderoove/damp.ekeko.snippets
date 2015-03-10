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
import java.util.Arrays;
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

public class ViewSettings implements Settings.Initializer {

    JTextComponent.KeyBinding[] keyBindings;

    protected JTextComponent.KeyBinding[] createKeyBindings() {
        return new JTextComponent.KeyBinding[] {
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK),
                       BaseKit.findAction
                   ),
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK),
                       BaseKit.replaceAction
                   ),
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK),
                       BaseKit.gotoAction
                   )
               };
    }

    public JTextComponent.KeyBinding[] getKeyBindings() {
        if (keyBindings == null) {
            keyBindings = createKeyBindings();
        }
        return keyBindings;
    }

    public static Action[] createActions() {
        return new Action[] {
                   new FindAction(),
                   new ReplaceAction(),
                   new GotoAction(),
                   new DefaultBuildPopupMenuAction(),
                   new DefaultBuildToolTipAction()
               };
    }

    public Map updateSettingsMap(Class kitClass, Map m) {
        if (kitClass == BaseKit.class && m != null) {

            // add key bindings
            JTextComponent.KeyBinding[] bnds = getKeyBindings();
            SettingsUtil.updateListSetting(m, Settings.KEY_BINDING_LIST, bnds);

            SettingsUtil.updateListSetting(m, Settings.POPUP_MENU_ACTION_NAME_LIST,
                                           new String[] {
                                               BaseKit.cutAction,
                                               BaseKit.copyAction,
                                               BaseKit.pasteAction,
                                               null,
                                               BaseKit.removeSelectionAction
                                           }
                                          );

        }
        return m;
    }

    public static class FindAction extends BaseAction {

        static final long serialVersionUID =-1004216157599217921L;

        public FindAction() {
            super(BaseKit.findAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                DialogSupport.getFindDialogSupport().showFindDialog();
            }
        }

    }

    public static class ReplaceAction extends BaseAction {

        static final long serialVersionUID =8613430313030895150L;

        public ReplaceAction() {
            super(BaseKit.replaceAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                DialogSupport.getFindDialogSupport().showReplaceDialog();
            }
        }

    }

    public static class GotoAction extends BaseAction {

        static final long serialVersionUID =-7955197520415687709L;

        public GotoAction() {
            super(BaseKit.gotoAction, ABBREV_RESET
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
 *  8    Gandalf   1.7         2/14/00  Miloslav Metelka tooltips fix
 *  7    Gandalf   1.6         1/13/00  Miloslav Metelka Localization
 *  6    Gandalf   1.5         12/28/99 Miloslav Metelka 
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/30/99  Miloslav Metelka 
 *  3    Gandalf   1.2         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  2    Gandalf   1.1         7/26/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */

