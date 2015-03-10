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

package org.netbeans.editor;

import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;
import javax.swing.text.BadLocationException;
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;

/**
* This is the parent of majority of the actions. It implements
* the necessary resetting depending of what is required
* by constructor of target action.
* The other thing implemented here is 
*
* @author Miloslav Metelka
* @version 1.00
*/

public abstract class BaseAction extends TextAction {

    /** Text of the menu item in popup menu for this action */
    public static final String POPUP_MENU_TEXT = "PopupMenuText"; // NOI18N

    /** Prefix for the name of the key for description in locale support */
    public static final String LOCALE_DESC_PREFIX = "desc-"; // NOI18N

    /** Prefix for the name of the key for popup description in locale support */
    public static final String LOCALE_POPUP_PREFIX = "popup-"; // NOI18N

    /** Remove the selected text at the action begining */
    public static final int SELECTION_REMOVE = 1;

    /** Reset magic caret position */
    public static final int MAGIC_POSITION_RESET = 2;

    /** Reset abbreviation accounting to empty string */
    public static final int ABBREV_RESET = 4;

    /** Prevents adding the new undoable edit to the old one when the next
    * document change occurs.
    */
    public static final int UNDO_MERGE_RESET = 8;

    /** Reset word-match table */
    public static final int WORD_MATCH_RESET = 16;

    /** Clear status bar text */
    public static final int CLEAR_STATUS_TEXT = 32;

    /** The action will not be recorded if in macro recording */
    public static final int NO_RECORDING = 64;

    /** Save current position in the jump list */
    public static final int SAVE_POSITION = 128;


    /** Bit mask of what should be updated when the action is performed before
    * the action's real task is invoked.
    */
    protected int updateMask;

    static final long serialVersionUID =-4255521122272110786L;

    public BaseAction(String name) {
        this(name, 0);
    }

    public BaseAction(String name, int updateMask) {
        super(name);
        this.updateMask = updateMask;
        // Initialize short description
        String key = LOCALE_DESC_PREFIX + name;
        String desc = LocaleSupport.getString(key);
        if (desc == null) {
            desc = LocaleSupport.getString(name);
        }
        putValue(SHORT_DESCRIPTION, desc);
        // Initialize menu description
        key = LOCALE_POPUP_PREFIX + name;
        String popupMenuText = LocaleSupport.getString(key);
        if (popupMenuText == null) {
            popupMenuText = desc;
        }
        putValue(POPUP_MENU_TEXT, popupMenuText);
    }

    /** This method is called once after the action is constructed
    * and then each time the settings are changed.
    * @param evt event describing the changed setting name. It's null
    *   if it's called after the action construction.
    * @param kitClass class of the kit that created the actions
    */
    protected void settingsChange(SettingsChangeEvent evt, Class kitClass) {
    }

    /** This method is made final here as there's an important
    * processing that must be done before the real action
    * functionality is performed. It can include the following:
    * 1. Updating of the target component depending on the update
    *    mask given in action constructor.
    * 2. Possible macro recoding when the macro recording
    *    is turned on.
    * The real action functionality should be done in
    * the method actionPerformed(ActionEvent evt, JTextComponent target)
    * which must be redefined by the target action.
    */
    public final void actionPerformed(ActionEvent evt) {
        JTextComponent target = getTextComponent(evt);

        updateComponent(target);

        actionPerformed(evt, target);
    }

    /** The target method that performs the real action functionality.
    * @param evt action event describing the action that occured
    * @param target target component where the action occured. It's retrieved
    *   by the TextAction.getTextComponent(evt).
    */
    public abstract void actionPerformed(ActionEvent evt, JTextComponent target);

    public JMenuItem getPopupMenuItem(JTextComponent target) {
        return null;
    }

    public String getPopupMenuText(JTextComponent target) {
        String txt = (String)getValue(POPUP_MENU_TEXT);
        if (txt == null) {
            txt = (String)getValue(NAME);
        }
        return txt;
    }

    /** Update the component according to the update mask specified
    * in the constructor of the action.
    * @param target target component to be updated.
    */
    public void updateComponent(JTextComponent target) {
        updateComponent(target, this.updateMask);
    }

    /** Update the component according to the given update mask
    * @param target target component to be updated.
    * @param updateMask mask that specifies what will be updated
    */
    public void updateComponent(JTextComponent target, int updateMask) {
        if (target != null && target.getDocument() instanceof BaseDocument) {
            BaseDocument doc = (BaseDocument)target.getDocument();
            boolean writeLocked = false;

            try {
                // remove selected text
                if ((updateMask & SELECTION_REMOVE) != 0) {
                    writeLocked = true;
                    doc.extWriteLock();
                    Caret caret = target.getCaret();
                    if (caret != null && caret.isSelectionVisible()) {
                        int dot = caret.getDot();
                        int markPos = caret.getMark();
                        if (dot < markPos) { // swap positions
                            int tmpPos = dot;
                            dot = markPos;
                            markPos = tmpPos;
                        }
                        try {
                            target.getDocument().remove(markPos, dot - markPos);
                        } catch (BadLocationException e) {
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // reset magic caret position
                if ((updateMask & MAGIC_POSITION_RESET) != 0) {
                    target.getCaret().setMagicCaretPosition(null);
                }

                // reset abbreviation accounting
                if ((updateMask & ABBREV_RESET) != 0) {
                    ((BaseTextUI)target.getUI()).getExtUI().getAbbrev().reset();
                }

                // reset merging of undoable edits
                if ((updateMask & UNDO_MERGE_RESET) != 0) {
                    doc.undoMergeReset = true;
                }

                // reset word matching
                if ((updateMask & WORD_MATCH_RESET) != 0) {
                    ((BaseTextUI)target.getUI()).getExtUI().getWordMatch().clear();
                }

                // Clear status bar text
                if ((updateMask & CLEAR_STATUS_TEXT) != 0) {
                    Utilities.clearStatusText(target);
                }

                // Possibly record in macro
                if ((updateMask & NO_RECORDING) == 0) {
                    // if (macro-recording-on) { record ... }
                }

                // Save current caret position in the jump-list
                if ((updateMask & SAVE_POSITION) != 0) {
                    JumpList.checkAddEntry(target);
                }

            } finally {
                if (writeLocked) {
                    doc.extWriteUnlock();
                }
            }
        }
    }

}

/*
 * Log
 *  18   Gandalf-post-FCS1.16.1.0    3/8/00   Miloslav Metelka 
 *  17   Gandalf   1.16        1/13/00  Miloslav Metelka 
 *  16   Gandalf   1.15        1/10/00  Miloslav Metelka 
 *  15   Gandalf   1.14        1/4/00   Miloslav Metelka 
 *  14   Gandalf   1.13        11/8/99  Miloslav Metelka 
 *  13   Gandalf   1.12        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   Gandalf   1.11        10/10/99 Miloslav Metelka 
 *  11   Gandalf   1.10        10/4/99  Miloslav Metelka 
 *  10   Gandalf   1.9         9/30/99  Miloslav Metelka 
 *  9    Gandalf   1.8         9/15/99  Miloslav Metelka 
 *  8    Gandalf   1.7         9/10/99  Miloslav Metelka 
 *  7    Gandalf   1.6         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  6    Gandalf   1.5         7/20/99  Miloslav Metelka 
 *  5    Gandalf   1.4         7/9/99   Miloslav Metelka 
 *  4    Gandalf   1.3         5/5/99   Miloslav Metelka 
 *  3    Gandalf   1.2         4/23/99  Miloslav Metelka Undo added and internal 
 *       improvements
 *  2    Gandalf   1.1         3/27/99  Miloslav Metelka 
 *  1    Gandalf   1.0         3/18/99  Miloslav Metelka 
 * $
 */

