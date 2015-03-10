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

package org.openide.filesystems;

/** Event for listening on file system changes.
* <P>
* By calling {@link #getFile} the original file where the action occurred
* can be obtained.
*
* @author Jaroslav Tulach, Petr Hamernik
* @version 0.26, December 14, 1997
*/
public class FileEvent extends java.util.EventObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 1028087432345400108L;
    /** Original file object where the action took place. */
    private FileObject file;
    /** time when this event has been fired */
    private long time;
    /** is expected? */
    private boolean expected;

    /** Creates new <code>FileEvent</code>. The <code>FileObject</code> where the action occurred
    * is assumed to be the same as the source object.
    * @param src source file which sent this event
    */
    public FileEvent(FileObject src) {
        this(src, src);
    }

    /** Creates new <code>FileEvent</code>, specifying the action object.
    * <p>
    * Note that the two arguments of this method need not be identical
    * in cases where it is reasonable that a different file object from
    * the one affected would be listened to by other components. E.g.,
    * in the case of a file creation event, the event source (which
    * listeners are attached to) would be the containing folder, while
    * the action object would be the newly created file object.
    * @param src source file which sent this event
    * @param file <code>FileObject</code> where the action occurred */
    public FileEvent(FileObject src, FileObject file) {
        super(src);
        this.file = file;
        this.time = System.currentTimeMillis ();
    }

    /** @return the original file where action occurred
    */
    public final FileObject getFile() {
        return file;
    }

    /** The time when this event has been created.
    * @return the milliseconds
    */
    public final long getTime () {
        return time;
    }

    /** Setter whether the change has been expected or not.
    */
    final void setExpected (boolean expected) {
        this.expected = expected;
    }

    /** Getter to test whether the change has been expected or not.
    */
    public final boolean isExpected () {
        return expected;
    }
}

/*
* Log
*  7    Gandalf   1.6         11/24/99 Jaroslav Tulach FileEvent can be expected
*       + fired by AbstractFileSystem
*  6    Gandalf   1.5         10/29/99 Jaroslav Tulach MultiFileSystem + 
*       FileStatusEvent
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  3    Gandalf   1.2         2/4/99   Jesse Glick     [JavaDoc]
*  2    Gandalf   1.1         2/1/99   Jesse Glick     [JavaDoc]
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/

