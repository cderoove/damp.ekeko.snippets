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

package org.netbeans.modules.vcs.cmdline;

import java.io.*;

public class VcsMkDir {
    String fname;

    public VcsMkDir(String fname) {
        File f = new File(fname);
        if (!f.exists())
            if (!f.mkdirs()) {
                System.out.println("Can not create directory"+fname); // NOI18N
                System.exit(1);
            }
    }

    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("Usage: VcsMkDir <directory_path>"); // NOI18N
            System.exit(1);
        }
        VcsMkDir mkd = new VcsMkDir(args[0]);
    }
}

/*
 * Log
 *  4    Gandalf   1.3         1/6/00   Martin Entlicher 
 *  3    Gandalf   1.2         10/25/99 Pavel Buzek     
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
