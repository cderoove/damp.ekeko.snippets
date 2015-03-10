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
import java.util.*;


/**
 * 
 * @author Pavel Buzek
 */
//-------------------------------------------
public interface DirReaderListener {

    public void readDirFinished(VcsDir dir, Vector rawData, boolean success);
    public void readDirFinishedRecursive(VcsDir dir, VcsDirContainer rawData, boolean success);

}

/*
 * Log
 *  5    Gandalf-post-FCS1.3.2.0     3/23/00  Martin Entlicher added recursive refresh.
 *  4    Gandalf   1.3         12/21/99 Martin Entlicher 
 *  3    Gandalf   1.2         10/25/99 Pavel Buzek     copyright and log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
