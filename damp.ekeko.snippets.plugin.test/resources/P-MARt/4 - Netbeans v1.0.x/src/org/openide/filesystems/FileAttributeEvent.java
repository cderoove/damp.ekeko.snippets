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

/** Event used to listen on file system attribute changes.
*
* @author Petr Hamernik
* @version 0.10, December 24, 1997
*/
public class FileAttributeEvent extends FileEvent {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8601944809928093922L;
    /** Name of attribute. */
    private String name;

    /** Old value of attribute */
    private Object oldValue;

    /** New value of attribute */
    private Object newValue;

    /** Creates new <code>FileAttributeEvent</code>. The <code>FileObject</code> where the action occurred
    * is assumed to be the same as the source object.
    * @param src source file which sent this event
    * @param name name of attribute
    * @param oldValue old value of attribute
    * @param newValue new value of attribute
    */
    public FileAttributeEvent(FileObject src, String name, Object oldValue, Object newValue) {
        this(src, src, name, oldValue, newValue);
    }

    /** Creates new <code>FileAttributeEvent</code>.
    * @param src source file which sent this event
    * @param file file object where the action occurred
    * @param name name of attribute
    * @param oldValue old value of attribute
    * @param newValue new value of attribute
    */
    public FileAttributeEvent(FileObject src, FileObject file,
                              String name, Object oldValue, Object newValue) {
        super(src, file);
        this.name = name;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /** Gets the name of the attribute.
    * @return Name of the attribute.
    */
    public String getName () {
        return name;
    }

    /** Gets the old value of the attribute.
    * @return Old value of the attribute.
    */
    public Object getOldValue () {
        return oldValue;
    }

    /** Gets the new value of the attribute.
    * @return New value of the attribute.
    */
    public Object getNewValue () {
        return newValue;
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         2/1/99   Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
