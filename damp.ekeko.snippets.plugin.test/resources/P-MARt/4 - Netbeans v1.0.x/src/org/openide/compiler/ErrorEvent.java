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

package org.openide.compiler;

import org.openide.filesystems.FileObject;

/** Used to report errors and warnings.
*
* @author Ales Novak
*/
public class ErrorEvent extends CompilerEvent {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1694485217662143181L;

    /** line with the error */
    private int line;
    /** message - i.e incompatible type for constructor */
    private String message;
    /** column with the error */
    private int column;
    /** text line with the error */
    private String ref;

    /** Create an error event.
    * @param source the compiler group producing the event
    * @param errorFile the file in error
    * @param line the line number of the error
    * @param column the column number of the error
    * @param message a description of the error
    * @param ref a text line showing the erroneous text
    */
    public ErrorEvent (
        CompilerGroup source, FileObject errorFile,
        int line, int column, String message, String ref
    ) {
        super(source, errorFile);
        this.line = line;
        this.message = message;
        this.column = column;
        this.ref = ref;
    }

    /** Get the line number where the error occurred.
    * @return the line
    */
    public int getLine() {
        return line;
    }

    /** Get a descriptive message explaining the error.
    * @return a description
    */
    public String getMessage() {
        return message;
    }

    /** Get the column number where the error occurred.
    * @return the column
    */
    public int getColumn() {
        return column;
    }

    /** Get a sample line of text containing the error.
    * @return the erroneous line
    */
    public String getReferenceText () {
        return ref;
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/24/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
