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

/** Event describing progress made compiling a file.
*
* @author Jaroslav Tulach
*/
public class ProgressEvent extends CompilerEvent {
    /** Task for general compiler overhead, unspecified. */
    public static final int TASK_UNKNOWN = 0x00;
    /** Task for parsing source code in preparation. */
    public static final int TASK_PARSING = 0x01;
    /** Task for generating the output code. */
    public static final int TASK_GENERATING = 0x02;
    /** Task for writing the output code. */
    public static final int TASK_WRITING = 0x03;
    /** Task for cleaning result of (previous) compilation. */
    public static final int TASK_CLEANING = 0x04;

    /** task */
    private int task;

    static final long serialVersionUID =9049676636384587798L;
    /** Create a progress event of unspecified type.
    *
    * @param source the compiler group that produced the event
    * @param file the file being compiled
    */
    public ProgressEvent(CompilerGroup source, FileObject file) {
        super (source, file);
        task = TASK_UNKNOWN;
    }

    /** Create a progress event.
    *
    * @param source the compiler group that produced the event
    * @param file the file being compiled
    * @param task one of {@link #TASK_UNKNOWN}, {@link #TASK_PARSING}, {@link #TASK_GENERATING}, {@link #TASK_WRITING}
    */
    public ProgressEvent(CompilerGroup source, FileObject file, int task) {
        super (source, file);
        this.task = task;
    }

    /** Get the task type.
    * @return the task
    */
    public int getTask () {
        return task;
    }
}

/*
 * Log
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/6/99  Jaroslav Tulach ProgressEvent.TASK_CLEANING
 *       
 *  4    Gandalf   1.3         8/18/99  Ian Formanek    Generated serial version
 *       UID
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/24/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
