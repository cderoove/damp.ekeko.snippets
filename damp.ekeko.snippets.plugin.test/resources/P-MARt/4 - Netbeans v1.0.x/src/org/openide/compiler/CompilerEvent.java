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

/** Event about a file being compiled.
*
* @author Ales Novak
* @version 0.11 12/4/97
*/
public class CompilerEvent extends java.util.EventObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -5493299146619631055L;

    /** the file that produced the result */
    private FileObject file;

    /** Create an event.
    * @param source the compiler group that produced the event
    * @param file the file being compiled
    */
    public CompilerEvent (CompilerGroup source, FileObject file) {
        super(source);
        this.file = file;
    }

    /** Get the source of the event.
    * @return the compiler group that produced the event
    */
    public CompilerGroup getCompilerGroup () {
        return (CompilerGroup)getSource ();
    }

    /** Get the file this event pertains to.
    * @return the file under compilation
    */
    public FileObject getFile() {
        return file;
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
