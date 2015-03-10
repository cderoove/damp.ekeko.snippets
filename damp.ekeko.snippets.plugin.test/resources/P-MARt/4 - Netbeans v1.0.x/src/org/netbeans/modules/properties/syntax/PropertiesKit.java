/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties.syntax;

import java.awt.Font;
import java.awt.event.ActionEvent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseDocument;
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

/**
* Editor kit implementation for text/properties content type
*
* @author Miloslav Metelka, Karel Gardas
* @version 0.01
*/

public class PropertiesKit extends NbEditorBaseKit {

    static final long serialVersionUID =3229768447965508461L;
    /** Create new instance of syntax coloring parser */
    public Syntax createSyntax(BaseDocument doc) {
        return new PropertiesSyntax();
    }


    /*  public Document createDefaultDocument() {
        return new NbEditorDocument(this.getClass());
      }

      public void install(JEditorPane c) {
        super.install(c);
        KitSupport.updateActions(c); // update IDE find and goto action
      }*/

}

/*
 * <<Log>>
 *  5    Gandalf   1.4         1/12/00  Petr Jiricka    Syntax coloring API 
 *       fixes
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         11/12/99 Miloslav Metelka NbEditorBaseKit as 
 *       parent
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/13/99  Petr Jiricka    
 * $
 */

