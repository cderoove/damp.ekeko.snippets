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

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.ext.HTMLKit;
import org.openide.windows.TopComponent;
import org.openide.util.actions.SystemAction;
import org.openide.actions.UndoAction;
import org.openide.actions.RedoAction;

/**
* Java editor kit with appropriate document
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorHTMLKit extends HTMLKit {

    static final long serialVersionUID =5706493629185142101L;

    public Document createDefaultDocument() {
        return new NbEditorDocument(this.getClass());
    }

    public void install(JEditorPane c) {
        super.install(c);
        KitSupport.updateActions(c); // update IDE find and goto action
    }

    public Class getFocusableComponentClass(JTextComponent c) {
        return TopComponent.class;
    }

    protected Action[] createActions() {
        Action[] plainActions = new Action[] {
                                    new NbUndoAction(),
                                    new NbRedoAction(),
                                };
        return TextAction.augmentList(super.createActions(), plainActions);
    }

    public static class NbUndoAction extends org.netbeans.editor.ActionFactory.UndoAction {

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            UndoAction ua = (UndoAction)SystemAction.get(UndoAction.class);
            if (ua != null && ua.isEnabled()) {
                ua.actionPerformed(evt);
            }
        }

    }

    public static class NbRedoAction extends org.netbeans.editor.ActionFactory.RedoAction {

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            RedoAction ua = (RedoAction)SystemAction.get(RedoAction.class);
            if (ua != null && ua.isEnabled()) {
                ua.actionPerformed(evt);
            }
        }

    }

}

/*
 * Log
 *  8    Gandalf-post-FCS1.6.1.0     4/6/00   Miloslav Metelka undo action
 *  7    Gandalf   1.6         11/14/99 Miloslav Metelka 
 *  6    Gandalf   1.5         11/11/99 Miloslav Metelka 
 *  5    Gandalf   1.4         11/9/99  Miloslav Metelka 
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/10/99  Miloslav Metelka 
 *  2    Gandalf   1.1         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  1    Gandalf   1.0         6/8/99   Miloslav Metelka 
 * $
 */

