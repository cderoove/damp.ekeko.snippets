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

import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import org.openide.TopManager;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.netbeans.editor.view.DefaultBuildPopupMenuAction;

/**
* Customized settings for NetBeans editor
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbBuildPopupMenuAction extends DefaultBuildPopupMenuAction {

    static final long serialVersionUID =-8623762627678464181L;

    protected JMenuItem getItem(JTextComponent target, String actionName) {
        JMenuItem item = null;
        SystemAction sa = KitSupport.getNbAction(actionName);
        if (sa instanceof Presenter.Popup) {
            item = ((Presenter.Popup)sa).getPopupPresenter();
            if (item != null && !(item instanceof JMenu)) {
                KeyStroke[] keys = TopManager.getDefault().getGlobalKeymap().getKeyStrokesForAction(sa);
                if (keys != null && keys.length > 0) {
                    item.setAccelerator(keys[0]);
                }
            }
        } else { // editor action
            item = super.getItem(target, actionName);
        }

        return item;
    }

}

/*
 * Log
 *  9    Gandalf   1.8         1/12/00  Miloslav Metelka 
 *  8    Gandalf   1.7         1/11/00  Miloslav Metelka 
 *  7    Gandalf   1.6         1/10/00  Miloslav Metelka 
 *  6    Gandalf   1.5         1/4/00   Miloslav Metelka 
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/10/99  Miloslav Metelka 
 *  3    Gandalf   1.2         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  2    Gandalf   1.1         8/5/99   Jaroslav Tulach Presents in popup menu.
 *  1    Gandalf   1.0         7/21/99  Miloslav Metelka 
 * $
 */

