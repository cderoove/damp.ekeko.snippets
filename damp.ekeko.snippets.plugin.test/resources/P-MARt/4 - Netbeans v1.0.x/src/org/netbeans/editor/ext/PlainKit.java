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

import javax.swing.text.Caret;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Syntax;

/**
* Editor kit used to edit the plain text. This is currently
* only the 'signature class' so that it's possible to have
* special settings for plain text files without affecting
* BaseKit settings.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class PlainKit extends BaseKit {

    static final long serialVersionUID =8833386258767117977L;

    public Caret createCaret() {
        return new ExtCaret();
    }

    public Syntax createSyntax(BaseDocument doc) {
        return new PlainSyntax();
    }

}

/*
 * Log
 *  7    Gandalf   1.6         1/13/00  Miloslav Metelka Localization
 *  6    Gandalf   1.5         12/28/99 Miloslav Metelka 
 *  5    Gandalf   1.4         11/10/99 Miloslav Metelka 
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         5/5/99   Miloslav Metelka 
 * $
 */

