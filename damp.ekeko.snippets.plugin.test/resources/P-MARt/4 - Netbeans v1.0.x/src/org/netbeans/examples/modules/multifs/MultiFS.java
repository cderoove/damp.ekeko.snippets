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

package org.netbeans.examples.modules.multifs;

import org.openide.TopManager;
import org.openide.filesystems.*;

public abstract /* XXX */ class MultiFS extends FileSystem {
    Config conf;
    static final long serialVersionUID =-2844466304134405189L;
    public MultiFS () {
        conf = new Config (new FileSystem[] { }, null, new Config.Sieve[] { });
    }
}
