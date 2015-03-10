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

package parser.tokenizer.explorer;

import java.awt.BorderLayout;

import org.openide.TopManager;
import org.openide.awt.SplittedPanel;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.view.ListView;
import org.openide.explorer.view.ContextTreeView;

import parser.tokenizer.*;
import parser.tokenizer.tokens.*;
import org.openide.nodes.*;


/**
 *
 * @author  jleppanen
 * @version 
 */
public class TokenExplorer extends ExplorerPanel {

    /** Creates new JavaTokenExplorer */
    public TokenExplorer() {
        // create a new TreeView component which does not display leafs (ContextTreeView)
        ContextTreeView tree = new ContextTreeView ();
        // create a new ListView component
        ListView list = new ListView ();

        // create a splitted panel with horizontal split and add the explorer views into it
        SplittedPanel panel = new SplittedPanel ();
        panel.setSplitType (SplittedPanel.HORIZONTAL);
        panel.add (tree, SplittedPanel.ADD_FIRST);
        panel.add (list, SplittedPanel.ADD_SECOND);
        setLayout (new BorderLayout ());
        add (panel, BorderLayout.CENTER);

        // set the root context of the explorer panel to the Repository root node
        // root context is the node that is used as the root of the hierarchy displayed
        // by explorer views inside this ExplorerPanel

    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) throws Exception {
        TokenExplorer explorer = new TokenExplorer();

        LexerGrammar g = new LexerGrammar();
        g.type("TOKEN").actionStateSwitch("STATE").add();
        LexicalState s = g.getLexState();//new LexicalState("DEFAULT");

        Node root = new LexicalStateNode(s);
        explorer.getExplorerManager ().setRootContext ( root );
        explorer.open ();
    }

}