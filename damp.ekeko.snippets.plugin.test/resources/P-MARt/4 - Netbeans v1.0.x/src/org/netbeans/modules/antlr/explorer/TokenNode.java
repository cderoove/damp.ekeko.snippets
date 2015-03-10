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

import org.openide.nodes.*;
import parser.tokenizer.tokens.*;

/**
 *
 * @author  jleppanen
 * @version 
 */
public class TokenNode extends AbstractNode {
    /** Creates new TokenNode */
    public TokenNode() {
        super(new Children.Array());
        setDisplayName("TokenNode - default");
    }

    public TokenNode(LexToken t) {
        super(new Children.Array());
        setDisplayName(t.name);
    }
}