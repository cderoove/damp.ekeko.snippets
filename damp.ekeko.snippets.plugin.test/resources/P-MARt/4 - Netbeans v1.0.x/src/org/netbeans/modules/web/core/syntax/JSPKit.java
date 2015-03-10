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

package org.netbeans.modules.web.core.syntax;

import java.awt.Font;
import java.awt.event.ActionEvent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.KitSupport;
import org.netbeans.modules.editor.NbEditorBaseKit;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.Caret;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.JEditorPane;

import org.netbeans.editor.ext.ExtCaret;
import org.netbeans.editor.ext.JavaSyntaxSupport;

/**
* Editor kit implementation for JSP content type
*
* @author Miloslav Metelka, Petr Jiricka
* @version 0.01
*/

public class JSPKit extends /*BaseKit*/NbEditorBaseKit {

    /** serialVersionUID */
    private static final long serialVersionUID = 8933974837050367142L;

    public JSPKit() {
        super();
    }

    /** Create new instance of syntax coloring parser */
    public Syntax createSyntax(BaseDocument doc) {
        return new JspMultiSyntax();
        //return new JSPSyntax();
    }

    /*  public Caret createCaret() {
        return new ExtCaret();
      }*/

    /*  public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new JavaSyntaxSupport(doc);
      }*/

    /*  public void install(JEditorPane c) {
        super.install(c);
        KitSupport.updateActions(c); // update IDE find and goto action
      }*/

}

/*
 * Log
 *  6    Gandalf   1.5         2/10/00  Petr Jiricka    Delegating to the new 
 *       syntax implmentation.
 *  5    Gandalf   1.4         1/3/00   Petr Jiricka    Inherits from 
 *       NbEditorBaseKit
 *  4    Gandalf   1.3         12/30/99 Petr Jiricka    Fix syntax coloring 
 *       after Mila's changes
 *  3    Gandalf   1.2         11/12/99 Miloslav Metelka NbEditorBaseKit as 
 *       parent
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/13/99  Petr Jiricka    
 * $
 */

