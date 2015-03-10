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

package org.netbeans.modules.editor;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.ListCellRenderer;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.CallableSystemAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.view.DefaultBuildPopupMenuAction;
import org.netbeans.editor.ext.ExtSettings;
import org.netbeans.editor.ext.JCCellRenderer;
import org.netbeans.modules.editor.java.NbJCCellRenderer;
import org.netbeans.modules.editor.java.NbEditorJavaKit;

/**
* Customized settings for NetBeans editor
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorSettings implements Settings.Initializer {

    private static boolean inited;

    public static void init() {
        if (!inited) {
            inited = true;
            Settings.addInitializer(new NbEditorSettings());
        }
    }

    public NbEditorSettings() {
    }

    public Map updateSettingsMap(Class kitClass, Map m) {
        if (m == null) {
            m = new HashMap();
        }

        if (kitClass == NbEditorJavaKit.class) {
            m.put(Settings.POPUP_MENU_ACTION_NAME_LIST,
                  new ArrayList(Arrays.asList(
                                    new String[] {
                                        NbEditorJavaKit.gotoHelpAction,
                                        null,
                                        NbEditorJavaKit.gotoSourceAction,
                                        NbEditorJavaKit.gotoDeclarationAction,
                                        null,
                                        BaseKit.formatAction,
                                        null,
                                        KitSupport.systemActionSave,
                                        null,
                                        KitSupport.systemActionCompile,
                                        null,
                                        KitSupport.systemActionExecute,
                                        null,
                                        NbEditorJavaKit.toggleBreakpointAction,
                                        NbEditorJavaKit.addWatchAction,
                                        null,
                                        BaseKit.cutAction,
                                        BaseKit.copyAction,
                                        BaseKit.pasteAction,
                                        null,
                                        BaseKit.removeSelectionAction,
                                        null,
                                        KitSupport.systemActionNew,
                                        null,
                                        KitSupport.systemActionTools,
                                        KitSupport.systemActionProperties
                                    }
                                ))
                 );

            SettingsUtil.updateListSetting(m, Settings.KEY_BINDING_LIST,
                                           new MultiKeyBinding[] {
                                               new MultiKeyBinding(
                                                   KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_MASK),
                                                   NbEditorJavaKit.gotoSourceAction
                                               ),
                                               new MultiKeyBinding(
                                                   KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.ALT_MASK),
                                                   NbEditorJavaKit.gotoHelpAction
                                               ),
                                           }
                                          );

            m.put(ExtSettings.JCOMPLETION_CELL_RENDERER, new NbJCCellRenderer());

        } else { // other kits

            m.put(Settings.POPUP_MENU_ACTION_NAME_LIST,
                  new ArrayList(Arrays.asList(
                                    new String[] {
                                        KitSupport.systemActionSave,
                                        null,
                                        BaseKit.cutAction,
                                        BaseKit.copyAction,
                                        BaseKit.pasteAction,
                                        null,
                                        BaseKit.removeSelectionAction
                                    }
                                ))
                 );

            SettingsUtil.updateListSetting(m, Settings.CUSTOM_ACTION_LIST,
                                           new Action[] {
                                               new NbBuildPopupMenuAction(),
                                           }
                                          );
        }

        return m;
    }

}

/*
 * Log
 *  12   Gandalf   1.11        1/10/00  Miloslav Metelka 
 *  11   Gandalf   1.10        1/4/00   Miloslav Metelka 
 *  10   Gandalf   1.9         11/14/99 Miloslav Metelka 
 *  9    Gandalf   1.8         11/8/99  Miloslav Metelka 
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         9/30/99  Miloslav Metelka 
 *  6    Gandalf   1.5         9/10/99  Miloslav Metelka 
 *  5    Gandalf   1.4         8/27/99  Miloslav Metelka 
 *  4    Gandalf   1.3         8/5/99   Jaroslav Tulach Tools & New action in 
 *       editor.
 *  3    Gandalf   1.2         7/26/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */

