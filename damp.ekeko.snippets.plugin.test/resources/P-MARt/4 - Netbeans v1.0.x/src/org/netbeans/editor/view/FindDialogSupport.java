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
import java.util.HashMap;
import java.util.Collections;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.Timer;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.FindSupport;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.GuardedException;
import org.netbeans.editor.Utilities;

/**
* Support for displaying find and replace dialogs
*
* @author Miloslav Metelka
* @version 1.00
*/

public class FindDialogSupport implements ActionListener {

    protected FindPanel findPanel;

    protected JButton[] findButtons;

    protected Dialog findDialog;

    protected Timer incSearchTimer;

    private static final String MNEMONIC_SUFFIX = "-mnemonic"; // NOI18N

    /** Whether the currently visible dialog is for replace */
    protected boolean replaceDialog;

    public FindDialogSupport() {
        int delay = SettingsUtil.getInteger(null, Settings.FIND_INC_SEARCH_DELAY, 200);
        incSearchTimer = new Timer(delay, this);
        incSearchTimer.setRepeats(false);
    }

    protected FindPanel getFindPanel() {
        if (findPanel == null) {
            findPanel = new FindPanel();
        }
        return findPanel;
    }

    protected JButton[] getFindButtons() {
        if (findButtons == null) {
            findButtons = new JButton[] {
                              new JButton(LocaleSupport.getString("find-button-find", "Find")), // NOI18N
                              new JButton(LocaleSupport.getString("find-button-replace", "Replace")), // NOI18N
                              new JButton(LocaleSupport.getString("find-button-replace-all", "Replace All")), // NOI18N
                              new JButton(LocaleSupport.getString("find-button-cancel", "Cancel")) // NOI18N
                          };
        }
        return findButtons;
    }

    protected Dialog buildFindDialog() {
        JButton[] fb = getFindButtons();
        Dialog d = DialogSupport.getDialogCreator().createFindDialog(
                       getFindPanel(), fb, 0, fb.length - 1, this);

        String ls = LocaleSupport.getString("find-button-find" + MNEMONIC_SUFFIX); // NOI18N
        char mnemonic = (ls != null && ls.length() > 0) ? ls.charAt(0) : 'F';
        fb[0].setMnemonic(mnemonic);
        ls = LocaleSupport.getString("find-button-replace" + MNEMONIC_SUFFIX); // NOI18N
        mnemonic = (ls != null && ls.length() > 0) ? ls.charAt(0) : 'R';
        fb[1].setMnemonic(mnemonic);
        ls = LocaleSupport.getString("find-button-replace-all" + MNEMONIC_SUFFIX); // NOI18N
        mnemonic = (ls != null && ls.length() > 0) ? ls.charAt(0) : 'A';
        fb[2].setMnemonic(mnemonic);

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

    protected Dialog getFindDialogImpl(boolean isReplace) {
        replaceDialog = isReplace;
        if (findDialog == null) {
            findDialog = buildFindDialog();
        }

        if (isReplace) {
            findPanel.updateReplace();
        } else {
            findPanel.updateFind();
        }

        return findDialog;
    }

    public Dialog getFindDialog() {
        return getFindDialogImpl(false);
    }

    public Dialog getReplaceDialog() {
        return getFindDialogImpl(true);
    }

    protected void showFindDialogImpl(boolean isReplace) {
        getFindDialogImpl(isReplace); // make sure it's built

        JTextComponent c = Utilities.getLastActiveComponent();
        if (c != null) {
            String selText = c.getSelectedText();
            if (selText != null) {
                findPanel.updateFindWhat(selText);
            }
        }

        findDialog.setVisible(true);
        findDialog.requestFocus();
        findPanel.updateFocus();
    }

    public void showFindDialog() {
        showFindDialogImpl(false);
    }

    public void showReplaceDialog() {
        showFindDialogImpl(true);
    }

    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();
        FindSupport fSup = FindSupport.getFindSupport();
        if (src == findButtons[0]) { // Find button
            findPanel.updateFindHistory();
            findPanel.save();
            if (fSup.find(null, false)) { // found
            }
            if (!replaceDialog) {
                findDialog.setVisible(false);
            }
        } else if (src == findButtons[1]) { // Replace button
            findPanel.updateReplaceHistory();
            try {
                findPanel.save();
                if (fSup.replace(null, false)) { // replaced
                    fSup.find(null, false);
                }
            } catch (GuardedException e) {
                // replace in guarded block
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        } else if (src == findButtons[2]) { // Replace All button
            findPanel.updateReplaceHistory();
            findPanel.save();
            fSup.replaceAll(null);
        } else if (src == findButtons[3]) { // Cancel button
            findDialog.setVisible(false);
            /* !!!      JTextComponent c = Utilities.getLastActiveComponent();
                  if (c != null) {
                    ((BaseCaret)c.getCaret()).dispatchUpdate();
                  }
            */
        } else if (src == incSearchTimer) {
            fSup.incSearch(findPanel.getFindProps());
        }
    }

    protected void windowAct(WindowEvent evt, boolean activated) {
        FindSupport fSup = FindSupport.getFindSupport();
        if (activated) {
            incSearchTimer.start();
            //      fSup.incSearch(findPanel.findProps);
        } else {
            incSearchTimer.stop();
            fSup.incSearchReset();
            Utilities.returnFocus();
        }
    }

    /** Panel that holds the find logic */
    public class FindPanel extends FindDialogPanel
        implements ItemListener, KeyListener, ActionListener {
        /**
         * @associates Object 
         */
        Map findProps = Collections.synchronizedMap(new HashMap(20));

        /**
         * @associates String 
         */
        Map objToProps = Collections.synchronizedMap(new HashMap(20));

        FindSupport findSupport = FindSupport.getFindSupport();

        static final long serialVersionUID =917425125419841466L;

        FindPanel() {
            objToProps.put(findWhat, Settings.FIND_WHAT);
            objToProps.put(replaceWith, Settings.FIND_REPLACE_WITH);
            objToProps.put(highlightSearch, Settings.FIND_HIGHLIGHT_SEARCH);
            objToProps.put(incSearch, Settings.FIND_INC_SEARCH);
            objToProps.put(matchCase, Settings.FIND_MATCH_CASE);
            objToProps.put(smartCase, Settings.FIND_SMART_CASE);
            objToProps.put(wholeWords, Settings.FIND_WHOLE_WORDS);
            objToProps.put(regExp, Settings.FIND_REG_EXP);
            objToProps.put(bwdSearch, Settings.FIND_BACKWARD_SEARCH);
            objToProps.put(wrapSearch, Settings.FIND_WRAP_SEARCH);

            regExp.setVisible(false); // !!! remove when regexp search is fine

            String ls = LocaleSupport.getString(Settings.FIND_WHAT);
            if (ls != null) {
                findWhatLabel.setText(ls);
            }
            ls = LocaleSupport.getString(Settings.FIND_WHAT + MNEMONIC_SUFFIX);
            char mnemonic = (ls != null && ls.length() > 0) ? ls.charAt(0) : 'n';
            findWhatLabel.setLabelFor(findWhat);
            findWhatLabel.setDisplayedMnemonic(mnemonic);

            ls = LocaleSupport.getString(Settings.FIND_REPLACE_WITH);
            if (ls != null) {
                replaceWithLabel.setText(ls);
            }
            ls = LocaleSupport.getString(Settings.FIND_WHAT + MNEMONIC_SUFFIX);
            mnemonic = (ls != null && ls.length() > 0) ? ls.charAt(0) : 'l';
            replaceWithLabel.setLabelFor(replaceWith);
            replaceWithLabel.setDisplayedMnemonic(mnemonic);

            ls = LocaleSupport.getString(Settings.FIND_HIGHLIGHT_SEARCH + MNEMONIC_SUFFIX);
            mnemonic = (ls != null && ls.length() > 0) ? ls.charAt(0) : 'H';
            highlightSearch.setMnemonic(mnemonic);

            ls = LocaleSupport.getString(Settings.FIND_INC_SEARCH + MNEMONIC_SUFFIX);
            mnemonic = (ls != null && ls.length() > 0) ? ls.charAt(0) : 'I';
            incSearch.setMnemonic(mnemonic);

            ls = LocaleSupport.getString(Settings.FIND_MATCH_CASE + MNEMONIC_SUFFIX);
            mnemonic = (ls != null && ls.length() > 0) ? ls.charAt(0) : 'C';
            matchCase.setMnemonic(mnemonic);

            ls = LocaleSupport.getString(Settings.FIND_SMART_CASE + MNEMONIC_SUFFIX);
            mnemonic = (ls != null && ls.length() > 0) ? ls.charAt(0) : 'S';
            smartCase.setMnemonic(mnemonic);

            ls = LocaleSupport.getString(Settings.FIND_WHOLE_WORDS + MNEMONIC_SUFFIX);
            mnemonic = (ls != null && ls.length() > 0) ? ls.charAt(0) : 'W';
            wholeWords.setMnemonic(mnemonic);

            ls = LocaleSupport.getString(Settings.FIND_REG_EXP + MNEMONIC_SUFFIX);
            mnemonic = (ls != null && ls.length() > 0) ? ls.charAt(0) : 'E';
            regExp.setMnemonic(mnemonic);

            ls = LocaleSupport.getString(Settings.FIND_BACKWARD_SEARCH + MNEMONIC_SUFFIX);
            mnemonic = (ls != null && ls.length() > 0) ? ls.charAt(0) : 'B';
            bwdSearch.setMnemonic(mnemonic);

            ls = LocaleSupport.getString(Settings.FIND_WRAP_SEARCH + MNEMONIC_SUFFIX);
            mnemonic = (ls != null && ls.length() > 0) ? ls.charAt(0) : 'p';
            wrapSearch.setMnemonic(mnemonic);


            load();
            findWhat.getEditor().getEditorComponent().addKeyListener(this);
            findWhat.addActionListener(this);
            replaceWith.getEditor().getEditorComponent().addKeyListener(this);
            replaceWith.addActionListener(this);
            highlightSearch.addItemListener(this);
            incSearch.addItemListener(this);
            matchCase.addItemListener(this);
            smartCase.addItemListener(this);
            wholeWords.addItemListener(this);
            regExp.addItemListener(this);
            bwdSearch.addItemListener(this);
            wrapSearch.addItemListener(this);
        }

        protected Map getFindProps() {
            return findProps;
        }

        void putProperty(Object component, Object value) {
            String prop = (String)objToProps.get(component);
            if (prop != null) {
                findProps.put(prop, value);
                incSearchTimer.restart();
                //        findSupport.incSearch(findProps);
            }
        }

        Object getProperty(Object component) {
            String prop = (String)objToProps.get(component);
            return (prop != null) ? findProps.get(prop) : null;
        }

        boolean getBooleanProperty(Object component) {
            Object prop = getProperty(component);
            return (prop != null) ? ((Boolean)prop).booleanValue() : false;
        }

        private void changeVisibility(boolean v) {
            replaceWith.setVisible(v);
            replaceWithLabel.setVisible(v);
            getFindButtons()[1].setVisible(v);
            getFindButtons()[2].setVisible(v);
        }

        protected void updateFocus() {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        findWhat.getEditor().getEditorComponent().requestFocus();
                        findWhat.requestFocus();
                        findWhat.getEditor().selectAll();
                    }
                }
            );
        }

        protected void updateFind() {
            changeVisibility(false);
        }

        protected void updateReplace() {
            changeVisibility(true);
        }

        private void updateHistory(JComboBox c) {
            Object item = c.getEditor().getItem();
            javax.swing.DefaultComboBoxModel m = new javax.swing.DefaultComboBoxModel();
            if (item != null) {
                m.addElement(item);
            }
            int cnt = c.getItemCount();
            for (int i = 0; i < cnt; i++) {
                Object o = c.getItemAt(i);
                if (o != null && !o.equals(item)) {
                    m.addElement(o);
                }
            }
            c.setModel(m);
        }

        protected void updateFindHistory() {
            updateHistory(findWhat);
        }

        protected void updateReplaceHistory() {
            updateHistory(replaceWith);
        }

        protected void updateFindWhat(String selectedText) {
            findWhat.getEditor().setItem(selectedText);
        }


        /** Load the current find properties from those in FindSupport */
        void load() {
            findProps = findSupport.getDefaultFindProperties();

            findWhat.getEditor().setItem(getProperty(findWhat));
            replaceWith.getEditor().setItem(getProperty(replaceWith));
            highlightSearch.setSelected(getBooleanProperty(highlightSearch));
            incSearch.setSelected(getBooleanProperty(incSearch));
            matchCase.setSelected(getBooleanProperty(matchCase));
            smartCase.setSelected(getBooleanProperty(smartCase));
            wholeWords.setSelected(getBooleanProperty(wholeWords));
            regExp.setSelected(getBooleanProperty(regExp));
            bwdSearch.setSelected(getBooleanProperty(bwdSearch));
            wrapSearch.setSelected(getBooleanProperty(wrapSearch));
        }

        /** Save the current find properties into those in FindSupport */
        void save() {
            findSupport.putFindProperties(findProps);
        }

        void changeFindWhat() {
            Object old = getProperty(findWhat);
            Object cur = findWhat.getEditor().getItem();
            if (old == null || !old.equals(cur)) {
                putProperty(findWhat, cur);
            }
        }

        void changeReplaceWith() {
            Object old = getProperty(replaceWith);
            Object cur = replaceWith.getEditor().getItem();
            if (old == null || !old.equals(cur)) {
                putProperty(replaceWith, cur);
            }
        }

        private void postChangeCombos() {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        changeFindWhat();
                        changeReplaceWith();
                        if (regExp.isSelected()) {

                        }
                    }
                }
            );
        }

        public void keyPressed(KeyEvent evt) {
            postChangeCombos();
        }

        public void keyReleased(KeyEvent evt) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        changeFindWhat();
                        changeReplaceWith();
                    }
                }
            );
        }

        public void keyTyped(KeyEvent evt) {
            if (evt.getKeyChar() == '\n') {
                FindDialogSupport.this.actionPerformed(
                    new ActionEvent(getFindButtons()[0], 0, null));
            }
        }

        public void itemStateChanged(ItemEvent evt)  {
            Boolean val = (evt.getStateChange() == ItemEvent.SELECTED) ? Boolean.TRUE
                          : Boolean.FALSE;
            putProperty(evt.getSource(), val);
        }

        public void actionPerformed(ActionEvent evt) {
            postChangeCombos();
        }

    }

}

/*
 * Log
 *  24   Gandalf-post-FCS1.20.1.2    4/14/00  Miloslav Metelka same replace string
 *  23   Gandalf-post-FCS1.20.1.1    3/8/00   Miloslav Metelka regexp checkbox 
 *       invisible
 *  22   Gandalf-post-FCS1.20.1.0    3/8/00   Miloslav Metelka 
 *  21   Gandalf   1.20        1/13/00  Miloslav Metelka Localization
 *  20   Gandalf   1.19        1/7/00   Miloslav Metelka 
 *  19   Gandalf   1.18        1/6/00   Miloslav Metelka 
 *  18   Gandalf   1.17        11/14/99 Miloslav Metelka 
 *  17   Gandalf   1.16        11/8/99  Miloslav Metelka 
 *  16   Gandalf   1.15        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   Gandalf   1.14        10/6/99  Miloslav Metelka 
 *  14   Gandalf   1.13        10/4/99  Miloslav Metelka 
 *  13   Gandalf   1.12        8/27/99  Miloslav Metelka 
 *  12   Gandalf   1.11        8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  11   Gandalf   1.10        8/17/99  Miloslav Metelka 
 *  10   Gandalf   1.9         7/29/99  Miloslav Metelka 
 *  9    Gandalf   1.8         7/26/99  Miloslav Metelka 
 *  8    Gandalf   1.7         7/22/99  Miloslav Metelka 
 *  7    Gandalf   1.6         7/20/99  Miloslav Metelka 
 *  6    Gandalf   1.5         7/2/99   Miloslav Metelka 
 *  5    Gandalf   1.4         6/29/99  Miloslav Metelka Scrolling and patches
 *  4    Gandalf   1.3         6/1/99   Miloslav Metelka 
 *  3    Gandalf   1.2         5/16/99  Miloslav Metelka 
 *  2    Gandalf   1.1         5/13/99  Miloslav Metelka 
 *  1    Gandalf   1.0         5/5/99   Miloslav Metelka 
 * $
 */

