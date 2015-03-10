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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.netbeans.editor.LocaleSupport;

/** Default dialog creation methods are located here.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class DefaultDialogCreator implements DialogCreator {

    protected static final String FIND_TITLE_LOCALE = "find-title"; // NOI18N
    protected static final String FIND_TITLE_DEFAULT = "Find"; // NOI18N

    protected static final String GOTO_TITLE_LOCALE = "goto-title"; // NOI18N
    protected static final String GOTO_TITLE_DEFAULT = "Goto Line"; // NOI18N


    public Dialog createFindDialog(JPanel findPanel, final JButton[] buttons,
                                   final int defaultButtonIndex, final int cancelButtonIndex,
                                   final ActionListener l) {
        JDialog d = new JDialog();
        d.setTitle(LocaleSupport.getString(FIND_TITLE_LOCALE, FIND_TITLE_DEFAULT));
        d.getContentPane().add(findPanel, BorderLayout.CENTER);
        d.getContentPane().add(createButtonPanel(buttons), BorderLayout.EAST);
        // add listener to buttons
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].addActionListener(l);
        }
        d.getRootPane().setDefaultButton(buttons[defaultButtonIndex]);
        d.getRootPane().registerKeyboardAction(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    l.actionPerformed(
                        new ActionEvent(buttons[cancelButtonIndex], 0, null));
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        d.pack();
        d.setLocation(100, 100);
        return d;
    }

    public Dialog createGotoDialog(JPanel gotoPanel, final JButton[] buttons,
                                   final int defaultButtonIndex, final int cancelButtonIndex,
                                   final ActionListener l) {
        JDialog d = new JDialog();
        d.setTitle(LocaleSupport.getString(GOTO_TITLE_LOCALE, GOTO_TITLE_DEFAULT));
        d.getContentPane().add(gotoPanel, BorderLayout.CENTER);
        d.getContentPane().add(createButtonPanel(buttons), BorderLayout.EAST);
        // add listener to buttons
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].addActionListener(l);
        }
        d.getRootPane().setDefaultButton(buttons[defaultButtonIndex]);
        d.getRootPane().registerKeyboardAction(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    l.actionPerformed(
                        new ActionEvent(buttons[cancelButtonIndex], 0, null));
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        d.pack();
        d.setLocation(100, 100);
        return d;
    }

    protected JPanel createButtonPanel(JButton[] buttons) {
        JPanel butPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        for (int i = 0; i < buttons.length; i++) {
            boolean last = (i == buttons.length - 1);
            // add OK button
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridheight = last ? GridBagConstraints.REMAINDER
                             : 1;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gbc.insets = new java.awt.Insets (5, 5, 2, 5);
            gbc.anchor = java.awt.GridBagConstraints.NORTH;
            gbc.weightx = 1.0;
            if (last) {
                gbc.weighty = 1.0;
            }
            butPanel.add(buttons[i], gbc);
        }
        return butPanel;
    }


}

/*
 * Log
 *  5    Gandalf   1.4         1/13/00  Miloslav Metelka Localization
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/29/99  Miloslav Metelka Scrolling and patches
 *  2    Gandalf   1.1         5/16/99  Miloslav Metelka 
 *  1    Gandalf   1.0         5/5/99   Miloslav Metelka 
 * $
 */

