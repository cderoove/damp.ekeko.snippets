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

package org.openide.windows;

/** Listener to actions taken on a line in the Output Window.
*
* @author Jaroslav Tulach
* @version 0.11 Dec 01, 1997
*/
public interface OutputListener extends java.util.EventListener {
    /** Called when a line is selected.
    * @param ev the event describing the line
    */
    public void outputLineSelected (OutputEvent ev);

    /** Called when some sort of action is performed on a line.
    * @param ev the event describing the line
    */
    public void outputLineAction (OutputEvent ev);

    /** Called when a line is cleared from the buffer of known lines.
    * @param ev the event describing the line
    */
    public void outputLineCleared (OutputEvent ev);
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/29/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 anonymous       moved to package org.openide.windows
 */
