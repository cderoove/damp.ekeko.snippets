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

import java.awt.Font;
import java.awt.Color;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.PrintContainer;
import org.netbeans.editor.ExtUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.Settings;
import org.netbeans.editor.DefaultSettings;
import org.openide.text.AttributedCharacters;

/**
* Support for printing the document to the printer
*
* @author Miloslav Metelka
* @version 1.00
*/

public class PrintSupport {

    protected static final char[] ONE_SPACE = new char[] { ' ' };

    BaseDocument doc;

    public PrintSupport(BaseDocument doc) {
        this.doc = doc;
    }

    public AttributedCharacterIterator[] createPrintIterators() {
        NbPrintContainer npc = new NbPrintContainer();
        doc.print(npc);
        return npc.getIterators();
    }

    class NbPrintContainer extends AttributedCharacters implements PrintContainer {

        ArrayList acl = new ArrayList();

        AttributedCharacters a;

        NbPrintContainer() {
            a = new AttributedCharacters();
        }

        public void add(char[] chars, Font font, Color foreColor, Color backColor) {
            a.append(chars, font, foreColor);
        }

        public void eol() {
            acl.add(a);
            a = new AttributedCharacters();
        }

        public boolean initEmptyLines() {
            return true;
        }

        public AttributedCharacterIterator[] getIterators() {
            int cnt = acl.size();
            AttributedCharacterIterator[] acis = new AttributedCharacterIterator[cnt];
            for (int i = 0; i < cnt; i++) {
                AttributedCharacters ac = (AttributedCharacters)acl.get(i);
                acis[i] = ac.iterator();
            }
            return acis;
        }

    }


}

/*
 * Log
 *  6    Gandalf   1.5         11/14/99 Miloslav Metelka 
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         6/25/99  Miloslav Metelka PrintContainer modified
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/13/99  Miloslav Metelka line init changed
 *  1    Gandalf   1.0         5/5/99   Miloslav Metelka 
 * $
 */

