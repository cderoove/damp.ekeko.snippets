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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;

/** Abbreviation support allowing to expand defined character sequences
* into the expanded strings or call the arbitrary action.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class Abbrev implements SettingsChangeListener, PropertyChangeListener {

    /** Abbreviation accounting string. Here the characters forming
    * abbreviation are stored.
    */
    private StringBuffer abbrevSB = new StringBuffer();

    /** Check whether the document text matches the abbreviation accounting
    * string.
    */
    private boolean checkDocText;

    /** Additional check whether the character right before the abbreviation
    * string in the text is not accepted by the <tt>addTypedAcceptor</tt>.
    * This test is only performed if <tt>checkDocText</tt> is true.
    */
    private boolean checkTextDelimiter;

    /** Extended UI to which this abbreviation is associated to */
    protected ExtUI extUI;

    /** Chars on which to expand acceptor */
    private Acceptor doExpandAcceptor;

    /** Whether add the typed char */
    private Acceptor addTypedAcceptor;

    /** Which chars reset abbreviation accounting */
    private Acceptor resetAcceptor;

    /** Abbreviation map */
    private HashMap abbrevMap;

    public Abbrev(ExtUI extUI, boolean checkDocText, boolean checkTextDelimiter) {
        this.extUI = extUI;
        this.checkDocText = checkDocText;
        this.checkTextDelimiter = checkTextDelimiter;

        Settings.addSettingsChangeListener(this);
        extUI.addPropertyChangeListener(this);
        settingsChange(null);
    }

    /** Called when settings were changed. The method is called
    * by extUI when settings were changed and from constructor.
    */
    public void settingsChange(SettingsChangeEvent evt) {
        Class kitClass = Utilities.getKitClass(extUI.getComponent());

        if (kitClass != null) {
            String settingName = (evt != null) ? evt.getSettingName() : null;

            if (settingName == null || Settings.ABBREV_ACTION_MAP.equals(settingName)) {
                abbrevMap = new HashMap();
                // Inspect action abbrevs
                Map m = (Map)Settings.getValue(kitClass, Settings.ABBREV_ACTION_MAP);
                if (m != null) {
                    BaseKit kit = Utilities.getKit(extUI.getComponent());
                    Iterator iter = m.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry me = (Map.Entry)iter.next();
                        Object value = me.getValue();
                        Action a = null;
                        if (value instanceof String) {
                            a = kit.getActionByName((String)value);
                        } else if (value instanceof Action) {
                            a = (Action)value;
                        }

                        if (a != null) {
                            abbrevMap.put(me.getKey(), a);
                        }
                    }
                }
            }

            if (settingName == null || Settings.ABBREV_MAP.equals(settingName)) {
                // Inspect string abbrevs
                Map m = (Map)Settings.getValue(kitClass, Settings.ABBREV_MAP);
                if (m != null) {
                    Iterator iter = m.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry me = (Map.Entry)iter.next();
                        Object value = me.getValue();
                        if (value != null) {
                            abbrevMap.put(me.getKey(), value);
                        }
                    }
                }
            }

            if (settingName == null || Settings.ABBREV_EXPAND_ACCEPTOR.equals(settingName)) {
                doExpandAcceptor = SettingsUtil.getAcceptor(kitClass, Settings.ABBREV_EXPAND_ACCEPTOR, AcceptorFactory.FALSE);
            }
            if (settingName == null || Settings.ABBREV_ADD_TYPED_CHAR_ACCEPTOR.equals(settingName)) {
                addTypedAcceptor = SettingsUtil.getAcceptor(kitClass, Settings.ABBREV_ADD_TYPED_CHAR_ACCEPTOR, AcceptorFactory.FALSE);
            }
            if (settingName == null || Settings.ABBREV_RESET_ACCEPTOR.equals(settingName)) {
                resetAcceptor = SettingsUtil.getAcceptor(kitClass, Settings.ABBREV_RESET_ACCEPTOR, AcceptorFactory.TRUE);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if (ExtUI.COMPONENT_PROPERTY.equals(propName)) {
            if (evt.getNewValue() != null) { // just installed
                JTextComponent c = extUI.getComponent();

                settingsChange(null);

            } else { // just deinstalled
                JTextComponent c = (JTextComponent)evt.getOldValue();

            }

        }
    }

    /** Reset abbreviation accounting. */
    public void reset() {
        abbrevSB.setLength(0);
    }

    /** Add typed character to abbreviation accounting string. */
    public void addChar(char ch) {
        abbrevSB.append(ch);
    }

    /** Get current abbreviation string */
    public String getAbbrevString() {
        return abbrevSB.toString();
    }

    /** Get mapping table [abbrev, expanded-abbrev] */
    public Map getAbbrevMap() {
        return abbrevMap;
    }

    /** Translate string using abbreviation table
    * @param abbrev string to translate. Pass null to translate current abbreviation
    *    string
    * @return expanded abbreviation
    */
    public Object translateAbbrev(String abbrev) {
        String abbStr = (abbrev != null) ? abbrev : abbrevSB.toString();
        return getAbbrevMap().get(abbStr);
    }

    /** Checks whether there's valid string to expand and if so it returns it.
    */
    public String getExpandString(char typedChar) {
        return (doExpandAcceptor.accept(typedChar)) ? getExpandString() : null;
    }

    public String getExpandString() {
        BaseDocument doc = (BaseDocument)extUI.getDocument();
        String abbrevStr = getAbbrevString();
        int abbrevStrLen = abbrevStr.length();
        Object expansion = translateAbbrev(abbrevStr);
        Caret caret = extUI.getComponent().getCaret();
        int dotPos = caret.getDot();
        if (abbrevStr != null && expansion != null
                && dotPos >= abbrevStrLen
           ) {
            if (checkDocText) {
                try {
                    String prevChars = doc.getText(dotPos - abbrevStrLen, abbrevStrLen);
                    if (prevChars.equals(abbrevStr)) { // abbrev chars really match text
                        if (!checkTextDelimiter || dotPos == abbrevStrLen
                                || resetAcceptor.accept(
                                    doc.getChars(dotPos - abbrevStrLen - 1, 1)[0])
                           ) {
                            return abbrevStr;
                        }
                    }
                } catch (BadLocationException e) {
                }
            }
        }
        return null;
    }

    protected boolean doExpansion(int dotPos, String expandStr, ActionEvent evt)
    throws BadLocationException {
        Object expansion = translateAbbrev(expandStr);
        boolean expanded = false;
        if (expansion instanceof String) { // expand to string
            BaseDocument doc = extUI.getDocument();
            doc.insertString(dotPos, (String)expansion, null);
            expanded = true;
        } else if (expansion instanceof Action) {
            ((Action)expansion).actionPerformed(evt);
            expanded = true;
        }
        return expanded;
    }

    public boolean expandString(char typedChar, String expandStr, ActionEvent evt)
    throws BadLocationException {
        if (expandString(expandStr, evt)) {
            if (addTypedAcceptor.accept(typedChar)) {
                int dotPos = extUI.getComponent().getCaret().getDot();
                extUI.getDocument().insertString(dotPos, String.valueOf(typedChar), null);
            }
            return true;
        }
        return false;
    }

    /** Expand abbreviation on current caret position.
    * Remove characters back to the word start and insert expanded abbreviation.
    * @return whether the typed character should be added to the abbreviation or not
    */
    public boolean expandString(String expandStr, ActionEvent evt)
    throws BadLocationException {
        boolean expanded = false;
        BaseDocument doc = extUI.getDocument();
        doc.atomicLock();
        try {
            Caret caret = extUI.getComponent().getCaret();
            int pos = caret.getDot() - expandStr.length();
            if (expandStr != null) {
                doc.remove(pos, expandStr.length());
                expanded = doExpansion(pos, expandStr, evt);
            }
        } finally {
            if (expanded) {
                reset();
            } else {
                doc.breakAtomicLock();
            }
            doc.atomicUnlock();
        }
        return expanded;
    }

    public boolean checkReset(char typedChar) {
        if (resetAcceptor.accept(typedChar)) {
            reset();
            return true;
        }
        return false;
    }

    public boolean checkAndExpand(char typedChar, ActionEvent evt)
    throws BadLocationException {
        boolean doInsert = true;
        String expandStr = getExpandString(typedChar);
        if (expandStr != null) { // should expand
            doInsert = false;
            expandString(typedChar, expandStr, evt);
        } else {
            addChar(typedChar);
        }
        checkReset(typedChar);
        return doInsert;
    }

    public void checkAndExpand(ActionEvent evt)
    throws BadLocationException {
        String expandStr = getExpandString();
        if (expandStr != null) {
            expandString(expandStr, evt);
        }
    }

}

/*
 * Log
 *  14   Gandalf-post-FCS1.12.1.0    3/8/00   Miloslav Metelka 
 *  13   Gandalf   1.12        1/13/00  Miloslav Metelka 
 *  12   Gandalf   1.11        1/4/00   Miloslav Metelka 
 *  11   Gandalf   1.10        11/9/99  Miloslav Metelka 
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         9/16/99  Miloslav Metelka 
 *  8    Gandalf   1.7         8/17/99  Miloslav Metelka 
 *  7    Gandalf   1.6         7/20/99  Miloslav Metelka 
 *  6    Gandalf   1.5         6/22/99  Miloslav Metelka 
 *  5    Gandalf   1.4         6/10/99  Miloslav Metelka 
 *  4    Gandalf   1.3         6/1/99   Miloslav Metelka 
 *  3    Gandalf   1.2         5/15/99  Miloslav Metelka fixes
 *  2    Gandalf   1.1         5/5/99   Miloslav Metelka 
 *  1    Gandalf   1.0         4/23/99  Miloslav Metelka 
 * $
 */

