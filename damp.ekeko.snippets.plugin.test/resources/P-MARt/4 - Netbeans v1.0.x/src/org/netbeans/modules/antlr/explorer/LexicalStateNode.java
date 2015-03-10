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

import java.util.*;
import org.openide.nodes.*;

import parser.tokenizer.*;
import parser.tokenizer.tokens.*;

/**
 *
 * @author  jleppanen
 * @version 
 */
public class LexicalStateNode extends AbstractNode {
    /** Creates new TokenNode */
    public LexicalStateNode() {
        super(new Children.Array());
        setDisplayName("LexicalStateNode - default");
    }

    public LexicalStateNode(LexicalState s) {
        super(new Children.Array());
        setDisplayName("LexicalState: "+s.getName());

        Children lexStates = new Children.Array();
        Node lexStatesNode = new AbstractNode(lexStates);
        lexStatesNode.setName("sub-states");

        Children tokens = new Children.Array();
        Node tokensNode = new AbstractNode(tokens);
        tokensNode.setName("tokens");

        Children children = getChildren();
        children.add(new Node[] { lexStatesNode, tokensNode });

        {Iterator i = s.getTokens().iterator();
            while (i.hasNext()) {
                LexToken t = (LexToken)i.next();
                tokens.add( new Node[] { new TokenNode(t) }  );
            }}

        {Iterator i = s.getStates().iterator();
            while (i.hasNext()) {
                LexicalState t = (LexicalState)i.next();
                lexStates.add( new Node[] { new LexicalStateNode(t) }  );
            }}
    }
}