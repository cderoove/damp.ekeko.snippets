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

import java.util.List;
import java.util.Iterator;
import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.ExtUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.Settings;

/**
* Creator of the popup menu
*
* @author Miloslav Metelka
* @version 1.00
*/

public class DefaultBuildPopupMenuAction extends BaseAction {

    static final long serialVersionUID =4257043398248915291L;

    public DefaultBuildPopupMenuAction() {
        super(BaseKit.buildPopupMenuAction, NO_RECORDING);
    }

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            JPopupMenu pm = buildPopupMenu(target);
            Utilities.getExtUI(target).putProperty(ExtUI.POPUP_MENU_PROPERTY, pm);
        }
    }

    protected JPopupMenu buildPopupMenu(JTextComponent target) {
        JPopupMenu pm = new JPopupMenu();
        List l = (List)Settings.getValue(Utilities.getKitClass(target),
                                         Settings.POPUP_MENU_ACTION_NAME_LIST);
        if (l != null) {
            Iterator i = l.iterator();
            while (i.hasNext()) {
                String an = (String)i.next();
                if (an != null) {
                    JMenuItem item = getItem(target, an);
                    if (item != null) {
                        pm.add(item);
                    }
                } else { // null name -> add separator
                    pm.addSeparator();
                }
            }
        }
        return pm;
    }

    protected JMenuItem getItem(JTextComponent target, String actionName) {
        JMenuItem item = null;
        Action a = Utilities.getKit(target).getActionByName(actionName);
        if (a != null) {
            if (a instanceof BaseAction) {
                item = ((BaseAction)a).getPopupMenuItem(target);
            }
            if (item == null) {
                String itemText = getItemText(target, actionName, a);
                if (itemText != null) {
                    item = new JMenuItem(itemText);
                    item.addActionListener(a);
                    // Try to get the accelerator
                    Keymap km = target.getKeymap();
                    if (km != null) {
                        KeyStroke[] keys = km.getKeyStrokesForAction(a);
                        if (keys != null && keys.length > 0) {
                            item.setAccelerator(keys[0]);
                        }
                    }
                    item.setEnabled(a.isEnabled());
                    Object helpID = a.getValue ("helpID");
                    if (helpID != null && (helpID instanceof String))
                        item.putClientProperty ("HelpID", helpID);
                }
            }
        }
        return item;
    }

    protected String getItemText(JTextComponent target, String actionName, Action a) {
        String itemText;
        if (a instanceof BaseAction) {
            itemText = ((BaseAction)a).getPopupMenuText(target);
        } else {
            itemText = actionName;
        }
        return itemText;
    }

}

/*
 * Log
 *  9    Gandalf-post-FCS1.7.1.0     3/8/00   Miloslav Metelka 
 *  8    Gandalf   1.7         1/19/00  Jesse Glick     Context help.
 *  7    Gandalf   1.6         1/10/00  Miloslav Metelka 
 *  6    Gandalf   1.5         1/7/00   Miloslav Metelka 
 *  5    Gandalf   1.4         1/4/00   Miloslav Metelka 
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/30/99  Miloslav Metelka 
 *  2    Gandalf   1.1         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */
