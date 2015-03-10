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

package org.netbeans.modules.antlr.editor;

import javax.swing.text.Document;
import javax.swing.JEditorPane;
import org.netbeans.modules.editor.KitSupport;
import org.netbeans.modules.incrlexer.examples.IScannerNbBaseKit;
import org.netbeans.modules.incrlexer.ITokenStream;

public class NbEditorGKit extends IScannerNbBaseKit {
    static final long serialVersionUID =5706493629185142101L;

    public ITokenStream createITokenStream() {
        return new ITokenStream.OnLexer(
                   //org.netbeans.modules.antlr.editor.g.ANTLRLexer.class
                   org.netbeans.modules.incrlexer.examples.antlr.g1.SimpleLexer.class
               );
    }


    public Document createDefaultDocument() {
        debugPrint("NbEditorGKit: "+this+" createDefaultDocument");
        return super.createDefaultDocument();
    }

    public void install(JEditorPane c) {
        super.install(c);
        KitSupport.updateActions(c); // update IDE find and goto action
    }

    private static final boolean isDebug = true;
    private static final void debugPrint(String s) {
        if (isDebug) {
            System.err.println(s);
        }
    }
}