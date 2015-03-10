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

package org.netbeans.examples.lib.timerbean;

/** The TimerListener interface must be implemented by
* a class that wants to be notified about time events.
*
* @version  1.00, Jul 20, 1998
*/
public interface TimerListener extends java.util.EventListener {

    /** Called when a new timer event occurs */
    public void onTime (java.awt.event.ActionEvent event);

}


/*
 * Log
 *  5    Gandalf   1.3.2.0     10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Tuborg    1.3         12/29/98 Ian Formanek    Fixed end-of-line 
 *       characters. No semantic change.
 *  3    Tuborg    1.2         10/17/98 Ian Formanek    Modified comments to be 
 *       same as the sources in distribution
 *  2    Tuborg    1.1         7/22/98  Ian Formanek    
 *  1    Tuborg    1.0         6/17/98  Ian Formanek    
 * $
 */
