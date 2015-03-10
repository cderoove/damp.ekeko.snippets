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

/** Event indicating a file rename.
*
* @author Petr Hamernik
* @version 0.10, December 14, 1997
*/
public class FileRenameEvent extends FileEvent {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -3947658371806653711L;
    /** Original name of the file. */
    private String name;

    /** Original extension of the file. */
    private String ext;

    /** Creates new <code>FileRenameEvent</code>. The <code>FileObject</code> where the action took place
    * is assumed to be the same as the source object.
    * @param src source file which sent this event
    * @param name name of the attribute
    * @param oldValue old value of the attribute
    * @param newValue new value of the attribute
    */
    public FileRenameEvent(FileObject src, String name, String ext) {
        this(src, src, name, ext);
    }

    /** Creates new <code>FileRenameEvent</code>, specifying an event location.
    * @param src source file which sent this event
    * @param file file object where the action took place
    * @param name name of the attribute
    * @param oldValue old value of the attribute
    * @param newValue new value of the attribute
    */
    public FileRenameEvent(FileObject src, FileObject file, String name, String ext) {
        super(src, file);
        this.name = name;
        this.ext = ext;
    }

    /** Get original name of the file.
    * @return Name of the file.
    */
    public String getName() {
        return name;
    }

    /** Get original extension of the file.
    * @return Extension of the file.
    */
    public String getExt() {
        return ext;
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         2/4/99   Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
