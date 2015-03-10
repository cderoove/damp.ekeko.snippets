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

package org.netbeans.modules.web.core.jswdk;

import java.beans.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;

import javax.swing.JTextField;

import org.openide.util.NbBundle;

public abstract class PairPropertyEditor extends PropertyEditorSupport {

    public final boolean supportsCustomEditor() {
        return true;
    }
    public final String getAsText() {
        return null;
    }
    public final void setAsText(String s) {
    }

    protected abstract String[] getTexts();
    protected abstract void addInternal(String text);
    protected abstract void removeInternal(String text);
    protected abstract void changeInternal(int idx, String[] text);

    public java.awt.Component getCustomEditor() {
        MapPanel mapPanel = new MapPanel();
        mapPanel.setPropertyEditor(this);
        return mapPanel;
    }

    public static class MimeMapEditor extends PairPropertyEditor {

        protected String[] getTexts() {
            Map map = (Map) getValue();
            Iterator iter = map.entrySet().iterator();
            String[] texts = new String[map.size()];

            int i = 0;
            while (iter.hasNext()) {
                Entry entry = (Entry) iter.next();
                texts[i++] = entry.getKey() + " " + entry.getValue(); // NOI18N
            }

            return texts;
        }

        protected void addInternal(String text) {
            String[] entry = toEntry(text);
            Map map = (Map) getValue();
            map.put(entry[0], entry[1]);
            //      return new EntryActListener((Map) getValue());
        }

        protected void removeInternal(String text) {
            String[] entry = toEntry(text);
            Map map = (Map) getValue();
            map.remove(entry[0]);
        }

        protected void changeInternal(int i, String[] text) {
            Map map = (Map) getValue();
            map.put(text[0], text[1]);
        }

        static String[] toEntry(String s) {
            int firstSpace = s.indexOf(' ');
            if (firstSpace <= 0) {
                throw new IllegalArgumentException();
            }
            String[] entry = new String[2];
            entry[0] = s.substring(0, firstSpace).trim();
            entry[1] = s.substring(firstSpace).trim();
            return entry;
        }

        public java.awt.Component getCustomEditor() {
            MapPanel mapPanel = (MapPanel)super.getCustomEditor();
            mapPanel.setEditLabel(NbBundle.getBundle(PairPropertyEditor.class).getString("CTL_MIMETypesLabel"));
            mapPanel.setEditFirstString(NbBundle.getBundle(PairPropertyEditor.class).getString("CTL_MIMETypesKey"));
            mapPanel.setEditSecondString(NbBundle.getBundle(PairPropertyEditor.class).getString("CTL_MIMETypesValue"));
            return mapPanel;
        }
        /*static class EntryActListener implements java.awt.event.ActionListener {
          Map map;

          EntryActListener(Map map) {
            this.map = map;
          }
          
          public void actionPerformed(java.awt.event.ActionEvent ev) {
            JTextField my = (JTextField) ev.getSource();
            String[] entry = toEntry(my.getText());
            map.put(entry[0], entry[1]);
            my.repaint();
          }
    }*/

    }

}

/*
* Log
*  4    Gandalf   1.3         1/12/00  Petr Jiricka    Fully I18n-ed
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         10/7/99  Petr Jiricka    
*  1    Gandalf   1.0         10/7/99  Petr Jiricka    
* $
*/
