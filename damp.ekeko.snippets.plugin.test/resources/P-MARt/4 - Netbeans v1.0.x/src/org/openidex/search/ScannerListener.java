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

package org.openidex.search;

import java.util.EventListener;

/** Listener to events that are happening in the scanner.
*
* @author Jaroslav Tulach
*/
public interface ScannerListener extends EventListener {

    /** Notification about new found nodes.
    * @throw InterruptedException to iterrupt scanner task
    */
    public void scannerFound(ScannerEvent event) throws InterruptedException;

}

/*
* Log
*  3    Gandalf-post-FCS1.1.1.0     4/4/00   Petr Kuzel      unknown state
*  2    Gandalf   1.1         12/15/99 Martin Balin    Fixed package statement
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 
