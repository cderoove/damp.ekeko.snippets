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

package org.netbeans.modules.jini;

import org.openide.nodes.*;
import org.openide.util.actions.*;

/**
 * Jini module default node behaviour.
 * 
 * @author  pkuzel
 * @version 
 */
public class DefaultNode extends AbstractNode {

    public DefaultNode (Children kids) {
        super(kids);
        systemActions = new SystemAction[] { };
    }

    public boolean canCut() { return false; }

    public boolean canCopy() { return false; }

}


/*
* <<Log>>
*  2    Gandalf   1.1         2/3/00   Petr Kuzel      Be smart and documented
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

