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

/** Event fired when something happens to a line in the Output Window.
*
* @author Jaroslav Tulach, Petr Hamernik
* @version 0.11 Dec 01, 1997
*/
public abstract class OutputEvent extends java.util.EventObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4809584286971828815L;
    /** Create an event.
    * @param src the tab in question
    */
    public OutputEvent (InputOutput src) {
        super (src);
    }

    /** Get the text on the line.
    * @return the text
    */
    public abstract String getLine ();

    /** Get the Output Window tab in question.
    * @return the tab
    */
    public InputOutput getInputOutput() {
        return (InputOutput) getSource();
    }
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
 *  0    Tuborg    0.12        --/--/98 Petr Hamernik   redesigned
 */
