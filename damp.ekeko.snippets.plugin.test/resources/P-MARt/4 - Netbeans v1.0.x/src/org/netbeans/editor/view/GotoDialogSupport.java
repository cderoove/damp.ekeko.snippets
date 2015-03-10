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

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.Map;
import java.util.Hashtable;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.FindSupport;
import org.netbeans.editor.LocaleSupport;

/**
* Support for displaying goto dialog
*
* @author Miloslav Metelka
* @version 1.00
*/

public class GotoDialogSupport implements ActionListener {

    private static final String MNEMONIC_SUFFIX = "-mnemonic"; // NOI18N

    protected static final String GOTO_LINE_LOCALE = "goto-line"; // NOI18N

    protected GotoPanel gotoPanel;

    protected JButton gotoButtons[];

    protected Dialog gotoDialog;

    protected GotoPanel getGotoPanel() {
        if (gotoPanel == null) {
            gotoPanel = new GotoPanel();
        }
        return gotoPanel;
    }

    protected JButton[] getGotoButtons() {
        if (gotoButtons == null) {
            gotoButtons = new JButton[] {
                              new JButton(LocaleSupport.getString("goto-button-goto", "Goto")), // NOI18N
                              new JButton(LocaleSupport.getString("goto-button-cancel", "Cancel")) // NOI18N
                          };
        }
        return gotoButtons;
    }

    protected Dialog buildGotoDialog() {
        JButton[] gb = getGotoButtons();
        Dialog d = DialogSupport.getDialogCreator().createGotoDialog(
                       getGotoPanel(), gb, 0, 1, this);

        String ls = LocaleSupport.getString("goto-button-goto" + MNEMONIC_SUFFIX); // NOI18N
        char mnemonic = (ls != null && ls.length() > 0) ? ls.charAt(0) : 'G';
        gb[0].setMnemonic(mnemonic);

        d.addWindowListener(
            new WindowAdapter() {
                public void windowActivated(WindowEvent evt) {
                    windowAct(evt, true);
                }

                public void windowDeactivated(WindowEvent evt) {
                    windowAct(evt, false);
                }
            }
        );
        return d;
    }

    public void showGotoDialog() {
        if (gotoDialog == null) {
            gotoDialog = buildGotoDialog();
        }
        gotoDialog.setVisible(true);
        gotoDialog.requestFocus();
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    gotoPanel.gotoCombo.requestFocus();
                    gotoPanel.gotoCombo.getEditor().getEditorComponent().requestFocus();
                    gotoPanel.gotoCombo.getEditor().selectAll();
                }
            }
        );
    }

    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();
        if (src == gotoButtons[0]) { // Find button
            if (performGoto()) {
                gotoDialog.setVisible(false);
            }
        } else if (src == gotoButtons[1]) { // Cancel button
            gotoDialog.setVisible(false);
        }
    }

    /** Perform the goto operation.
    * @return whether the dialog should be made invisible or not
    */
    protected boolean performGoto() {
        boolean OK = true;
        JTextComponent c = Utilities.getLastActiveComponent();
        if (c != null) {
            try {
                int line = Integer.parseInt(
                               (String)gotoPanel.gotoCombo.getEditor().getItem());
                int pos = Utilities.getRowStartFromLineOffset(
                              (BaseDocument)c.getDocument(), line - 1);
                if (pos != -1) {
                    c.getCaret().setDot(pos);
                } else {
                    c.getToolkit().beep();
                    OK = false;
                }
            } catch (NumberFormatException e) {
                c.getToolkit().beep();
                OK = false;
            }
        }
        return OK;
    }

    protected void windowAct(WindowEvent evt, boolean activated) {
        FindSupport fSup = FindSupport.getFindSupport();
        if (!activated) {
            Utilities.returnFocus();
        }
    }

    protected class GotoPanel extends GotoDialogPanel
        implements KeyListener {

        static final long serialVersionUID =2528341478406015876L;

        protected GotoPanel() {
            gotoCombo.getEditor().getEditorComponent().addKeyListener(this);

            String ls = LocaleSupport.getString(GOTO_LINE_LOCALE);
            if (ls != null) {
                gotoLabel.setText(ls);
            }
            ls = LocaleSupport.getString(GOTO_LINE_LOCALE + MNEMONIC_SUFFIX);
            char mnemonic = (ls != null && ls.length() > 0) ? ls.charAt(0) : 'l';
            gotoLabel.setLabelFor(gotoCombo);
            gotoLabel.setDisplayedMnemonic(mnemonic);
        }

        public void keyPressed(KeyEvent evt) {
        }

        public void keyReleased(KeyEvent evt) {
        }

        public void keyTyped(KeyEvent evt) {
            if (evt.getKeyChar() == '\n') {
                GotoDialogSupport.this.actionPerformed(
                    new ActionEvent(gotoButtons[0], 0, null));
            }
        }

    }

}

/*
 * Log
 *  10   Gandalf-post-FCS1.7.1.1     3/8/00   Miloslav Metelka 
 *  9    Gandalf-post-FCS1.7.1.0     3/1/00   Petr Nejedly    History combo fixup
 *  8    Gandalf   1.7         1/13/00  Miloslav Metelka Localization
 *  7    Gandalf   1.6         11/8/99  Miloslav Metelka 
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  4    Gandalf   1.3         7/22/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/20/99  Miloslav Metelka 
 *  2    Gandalf   1.1         6/1/99   Miloslav Metelka 
 *  1    Gandalf   1.0         5/16/99  Miloslav Metelka 
 * $
 */

