/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vcs.advanced;

import java.io.*;

public class VcsMkDir {
    String fname;

    public VcsMkDir(String fname) {
        File f = new File(fname);
        if (!f.exists())
            if (!f.mkdirs()) {
                System.out.println("Can not create directory"+fname);
                System.exit(1);
            }
    }

    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("Usage: VcsMkDir <directory_path>");
            System.exit(1);
        }
        VcsMkDir mkd = new VcsMkDir(args[0]);
    }
}

/*
 * <<Log>>
 *  1    Gandalf   1.0         12/16/99 Martin Entlicher 
 * $
 */
