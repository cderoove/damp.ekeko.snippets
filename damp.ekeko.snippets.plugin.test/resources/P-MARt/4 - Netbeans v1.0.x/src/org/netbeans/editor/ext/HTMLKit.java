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

import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.BaseDocument;
import javax.swing.text.Caret;

/**
* Editor kit implementation for HTML content type
*
* @author Miloslav Metelka
* @version 0.01
*/

public class HTMLKit extends BaseKit {

    static final long serialVersionUID =-1381945567613910297L;

    /** Create new instance of syntax coloring parser */
    public Syntax createSyntax(BaseDocument doc) {
        return new HTMLSyntax();
    }

    public Caret createCaret() {
        return new ExtCaret();
    }

}

/*
 * Log
 *  8    Gandalf   1.7         12/28/99 Miloslav Metelka 
 *  7    Gandalf   1.6         11/10/99 Miloslav Metelka 
 *  6    Gandalf   1.5         11/9/99  Miloslav Metelka 
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/10/99  Miloslav Metelka 
 *  3    Gandalf   1.2         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  2    Gandalf   1.1         7/20/99  Miloslav Metelka 
 *  1    Gandalf   1.0         6/1/99   Miloslav Metelka 
 * $
 */

