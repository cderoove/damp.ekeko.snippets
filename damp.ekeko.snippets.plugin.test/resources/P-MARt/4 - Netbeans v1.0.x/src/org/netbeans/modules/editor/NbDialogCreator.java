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

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.view.DefaultDialogCreator;
import org.openide.TopManager;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;

/** NetBeans dialog creation methods
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbDialogCreator extends DefaultDialogCreator {

    public Dialog createFindDialog(JPanel findPanel, final JButton[] buttons,
                                   int defaultButtonIndex, final int cancelButtonIndex, final ActionListener l) {
        Dialog d = TopManager.getDefault().createDialog(
                       new DialogDescriptor(findPanel,
                                            LocaleSupport.getString(FIND_TITLE_LOCALE, FIND_TITLE_DEFAULT),
                                            false,
                                            buttons, buttons[defaultButtonIndex], DialogDescriptor.RIGHT_ALIGN,
                                            new HelpCtx (NbDialogCreator.class.getName () + ".findDialog"), l)); // NOI18N

        // Register Cancel key
        if (d instanceof JDialog) {
            ((JDialog)d).getRootPane().registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        l.actionPerformed(
                            new ActionEvent(buttons[cancelButtonIndex], 0, null));
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
                JComponent.WHEN_IN_FOCUSED_WINDOW
            );
        }

        return d;
    }

    public Dialog createGotoDialog(JPanel gotoPanel, final JButton[] buttons,
                                   int defaultButtonIndex, final int cancelButtonIndex, final ActionListener l) {
        Dialog d = TopManager.getDefault().createDialog(
                       new DialogDescriptor(gotoPanel,
                                            LocaleSupport.getString(GOTO_TITLE_LOCALE, GOTO_TITLE_DEFAULT),
                                            false,
                                            buttons, buttons[defaultButtonIndex], DialogDescriptor.RIGHT_ALIGN,
                                            new HelpCtx (NbDialogCreator.class.getName () + ".gotoDialog"), l)); // NOI18N

        // Register Cancel key
        if (d instanceof JDialog) {
            ((JDialog)d).getRootPane().registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        l.actionPerformed(
                            new ActionEvent(buttons[cancelButtonIndex], 0, null));
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
                JComponent.WHEN_IN_FOCUSED_WINDOW
            );
        }

        return d;
    }


}

/*
 * Log
 *  8    Gandalf   1.7         1/19/00  Miloslav Metelka Escape checking
 *  7    Gandalf   1.6         1/13/00  Miloslav Metelka Localization
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         7/8/99   Jesse Glick     Context help.
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/17/99  Miloslav Metelka 
 *  2    Gandalf   1.1         5/16/99  Miloslav Metelka Added goto dialog
 *  1    Gandalf   1.0         5/5/99   Miloslav Metelka 
 * $
 */

