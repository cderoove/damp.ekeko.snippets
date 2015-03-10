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

package org.openide.loaders;

/** Listener that watches progress of recognizing objects
* in a folder.

* @author Jaroslav Tulach
*/
interface FolderListListener {
    /** Another object has been recognized.
    * @param obj the object recognized
    * @param arr array where the implementation should add the 
    *    object
    */
    public void process (DataObject obj, java.util.List arr);

    /** All objects has been recognized.
    * @param arr list of DataObjects
    */
    public void finished (java.util.List arr);
}

/*
* Log
*  1    Gandalf   1.0         12/2/99  Jaroslav Tulach 
* $ 
*/ 
