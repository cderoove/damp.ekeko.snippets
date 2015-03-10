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

package org.netbeans.modules.jarpackager.filesystem;

import org.openide.filesystems.*;


/** Writeable jar file system. This is in fact multi file system
* consisting of writeable local file system and read only jar file
* system.
*
* @author Dafe Simonek
*/
public class WriteableJarFileSystem extends MultiFileSystem {

    static final long serialVersionUID =-2000857862088437012L;
    /** Creates new WriteableJarFileSystem */
    public WriteableJarFileSystem () {
        super(new FileSystem[] { new LocalFileSystem(),
                                 new JarFileSystem() } );
    }



}

/*
* <<Log>>
*  3    Gandalf   1.2         11/27/99 Patrik Knakal   
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         9/8/99   David Simonek   
* $
*/