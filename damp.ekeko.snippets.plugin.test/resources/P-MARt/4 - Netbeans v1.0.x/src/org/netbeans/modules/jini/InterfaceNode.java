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
import org.openide.src.*;
import org.openide.src.nodes.*;
import org.openide.util.actions.*;

/**
 * Any interface in Jini browser can be saved or a lookup method for
 * its implementors created.
 *
 * @author  Petr Kuzel
 * @version 
 */

public class InterfaceNode extends ClassElementNode implements Node.Cookie {
    Class cl;

    public InterfaceNode(Class cl, ClassElement ce) {
        super(ce, new ClassChildren(ce), false);
        this.cl = cl;

        systemActions = new SystemAction[] {
                            SystemAction.get(SaveInterfaceAction.class),
                            SystemAction.get(CreateClientAction.class)
                        };

        getCookieSet().add(this);

        setDisplayName(cl.getName());
    }

    public Class getInterface() {
        return cl;
    }
}


/*
* <<Log>>
*  2    Gandalf   1.1         2/3/00   Petr Kuzel      Be smart and documented
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

