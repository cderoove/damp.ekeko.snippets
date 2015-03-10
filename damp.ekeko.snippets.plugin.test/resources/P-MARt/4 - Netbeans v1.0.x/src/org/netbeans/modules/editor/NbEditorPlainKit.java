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
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;
import javax.swing.JEditorPane;
import org.netbeans.editor.ext.PlainKit;
import org.openide.windows.TopComponent;
import org.openide.util.actions.SystemAction;
import org.openide.actions.UndoAction;
import org.openide.actions.RedoAction;

/**
* Editor kit with appropriate document
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorPlainKit extends PlainKit {

    static final long serialVersionUID =-2710881638877322083L;

    public Document createDefaultDocument() {
        return new NbEditorDocument(this.getClass());
    }

    public void install(JEditorPane c) {
        super.install(c);
        KitSupport.updateActions(c);
    }

    public Class getFocusableComponentClass(JEditorPane c) {
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
 *  7    Gandalf   1.6         12/28/99 Miloslav Metelka 
 *  6    Gandalf   1.5         11/11/99 Miloslav Metelka 
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/10/99  Miloslav Metelka 
 *  3    Gandalf   1.2         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  2    Gandalf   1.1         5/17/99  Miloslav Metelka 
 *  1    Gandalf   1.0         5/5/99   Miloslav Metelka 
 * $
 */

