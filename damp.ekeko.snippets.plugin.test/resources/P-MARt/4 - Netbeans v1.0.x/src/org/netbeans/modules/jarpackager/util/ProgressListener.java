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

package org.netbeans.modules.jarpackager.util;

import org.openide.filesystems.FileObject;

/** Interface for receiving the information about progress
* when packaging jar archive.
*
* @author Dafe Simonek
*/
public interface ProgressListener {

    /** Notifies about progress.
    * @param percent percentage of how much work is done.
    * @param description textual description of the action which is
    * currently being processed.
    */  
    public void progress (int percent, String description);

}

/*
* <<Log>>
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         10/4/99  David Simonek   
* $
*/