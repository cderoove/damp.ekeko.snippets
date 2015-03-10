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

package org.openide.loaders;

import java.util.Enumeration;
import java.util.Hashtable;

import org.openide.filesystems.FileObject;

/** Property class that collects a modifiable list of file extensions
* and permits checking of whether a name or a file object has a given extension.
* It comes with a {@link ExtensionListEditor property editor} to allow the user to modify the extensions.
*
* @author Jaroslav Tulach
* @version 0.10 November 11, 1997
*/
public class ExtensionList extends Object
    implements Cloneable, java.io.Serializable {
    /** property list type is (String, String) 
     * @associates String*/
    private Hashtable list;

    static final long serialVersionUID =8868581349510386291L;
    /** Default constructor.
    */
    public ExtensionList () {
        list  = new Hashtable ();
    }

    /** Copy constructor.
    * @param h hashtable to use
    */
    private ExtensionList (Hashtable h) {
        list = h;
    }

    /** Clone new object.
    */
    public Object clone () {
        return new ExtensionList ((Hashtable)list.clone ());
    }

    /** Add a new extension.
    * @param ext the extension
    */
    public void addExtension (String ext) {
        list.put (ext, ext);
    }

    /** Remove an extension.
    * @param ext the extension
    */
    public void removeExtension (String ext) {
        list.remove (ext);
    }

    /** Test whether the name in the string is acceptable.
    * It should end with a dot and be one of the registered extenstions.
    * @param s the name
    * @return <CODE>true</CODE> if the name is acceptable
    */
    public boolean isRegistered (String s) {
        try {
            String ext = s.substring (s.lastIndexOf ('.') + 1);
            return list.get (ext) != null;
        } catch (StringIndexOutOfBoundsException ex) {
            return false;
        }
    }

    /** Tests whether the file object is acceptable.
    * Its extension should be registered.
    * @param fo the file object to test
    * @return <CODE>true</CODE> if the file object is acceptable
    */
    public boolean isRegistered (FileObject fo) {
        return list.get (fo.getExt ()) != null;
    }

    /** Get all extensions.
    * @return enumeration of <CODE>String</CODE>s
    */
    public Enumeration extensions () {
        return list.elements ();
    }
}

/*
 * Log
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/18/99  Ian Formanek    Generated serial version
 *       UID
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/9/99   Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
