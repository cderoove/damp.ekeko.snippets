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

import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.Action;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.GuardedException;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Utilities;

/**
* Extended generic actions supported by the classes in this package.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ExtActionFactory {

    /** Goto declaration action */
    public static final String gotoDeclarationAction = "goto-declaration"; // NOI18N

    /** Action to go to the declaration of the variable under the caret.
    */
    public static class GotoDeclarationAction extends BaseAction {

        static final long serialVersionUID =-6440495023918097760L;

        public GotoDeclarationAction() {
            super(gotoDeclarationAction,
                  ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET
                  | SAVE_POSITION
                 );
        }

        public boolean gotoDeclaration(JTextComponent target) {
            try {
                Caret caret = target.getCaret();
                int dotPos = caret.getDot();
                BaseDocument doc = (BaseDocument)target.getDocument();
                int[] idBlk = Utilities.getIdentifierBlock(doc, dotPos);
                ExtSyntaxSupport extSup = (ExtSyntaxSupport)doc.getSyntaxSupport();
                if (idBlk != null) {
                    int decPos = extSup.findDeclarationPosition(doc.getText(idBlk), idBlk[1]);
                    if (decPos >= 0) {
                        caret.setDot(decPos);
                        return true;
                    }
                }
            } catch (BadLocationException e) {
            }
            return false;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                gotoDeclaration(target); // try to go to the declaration position
            }
        }
    }

}

/*
 * Log
 *  5    Gandalf   1.4         1/13/00  Miloslav Metelka Localization
 *  4    Gandalf   1.3         1/10/00  Miloslav Metelka 
 *  3    Gandalf   1.2         1/7/00   Miloslav Metelka 
 *  2    Gandalf   1.1         1/4/00   Miloslav Metelka 
 *  1    Gandalf   1.0         11/8/99  Miloslav Metelka 
 * $
 */

