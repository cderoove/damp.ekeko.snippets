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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/** A print writer with a few hooks for the Output Window.
*
* @author Ales Novak
* @version 0.13 Feb 24, 1997
*/
public abstract class OutputWriter extends PrintWriter {
    /** Make an output writer.
    * @param w the underlying writer
    */
    protected OutputWriter (Writer w) {
        super(w);
    }

    /** Print a line.
    * @param s a string to print to the tab
    * @param l a listener that will receive events about this line
    * @throws IOException if the string could not be printed
    */
    public abstract void println(String s, OutputListener l) throws IOException;

    /** Clear the output pane.
    * @throws IOException if there is a problem
    */
    public abstract void reset() throws IOException;
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
 *  0    Tuborg    0.12        --/--/98 anonymous       small change
 */
