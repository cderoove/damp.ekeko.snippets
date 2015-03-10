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

package org.netbeans.core;

import java.io.*;
import org.openide.filesystems.*;

/** Enhanced version of jar file system that uses filesystem.attributes.
*
* @author  Jaroslav Tulach
*/
public final class ExJarFileSystem extends JarFileSystem {
    /** serial UID */
    static final long serialVersionUID = 8682224601177674646L;

    public ExJarFileSystem () {
        DefaultAttributes da = new DefaultAttributes (this, this, this);
        attr = da;
        list = da;
    }

}

/*
* Log
*  1    Gandalf   1.0         11/25/99 Jaroslav Tulach 
* $ 

*/ 





