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

package org.netbeans.modules.vcs;

/**
 *
 * @author  Pavel Buzek
 * @version 
 */

public interface VcsFactory {
    public VcsAdvancedCustomizer getVcsAdvancedCustomizer ();
    public VcsDirReader getVcsDirReader (DirReaderListener listener, String path, VcsFileSystem fileSystem);
    public VcsDirReader getVcsDirReaderRecursive (DirReaderListener listener, String path, VcsFileSystem fileSystem);
    public VcsAction getVcsAction (VcsFileSystem fs);
}

/*
 * Log
 *  4    Gandalf-post-FCS1.2.2.0     3/23/00  Martin Entlicher Recursive reader added.
 *  3    Gandalf   1.2         10/25/99 Pavel Buzek     copyright and log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
