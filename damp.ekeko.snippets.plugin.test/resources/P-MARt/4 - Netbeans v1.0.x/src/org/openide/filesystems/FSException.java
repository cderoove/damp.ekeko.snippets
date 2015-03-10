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

import java.io.IOException;

/** Localized IOException for filesystems.
*
* @author Jaroslav Tulach
*/
final class FSException extends IOException {
    /** name of resource to use for localized message */
    //  private String resource;
    /** arguments to pass to the resource */
    private Object[] args;

    /** Creates new FSException. */
    private FSException (String resource, Object[] args) {
        super (resource);
        this.args = args;
    }

    /** Message should be meaning full, but different from localized one.
    */
    public String getMessage () {
        return " " + getLocalizedMessage (); // NOI18N
    }

    /** Localized message.
    */
    public String getLocalizedMessage () {
        String res = super.getMessage ();
        String format = FileSystem.getString (res);

        if (args != null) {
            return java.text.MessageFormat.format (format, args);
        } else {
            return format;
        }
    }

    /** Creates the localized exception.
    * @param resource to take localization string from
    * @exception the exception
    */
    public static void io (String resource) throws IOException {
        throw new FSException (resource, null);
    }

    public static void io (String resource, Object[] args) throws IOException {
        throw new FSException (resource, args);
    }

    public static void io (String resource, Object arg1) throws IOException {
        throw new FSException (resource, new Object[] { arg1 });
    }

    public static void io (
        String resource, Object arg1, Object arg2
    ) throws IOException {
        throw new FSException (resource, new Object[] { arg1, arg2 });
    }

    public static void io (
        String resource, Object arg1, Object arg2, Object arg3
    ) throws IOException {
        throw new FSException (resource, new Object[] { arg1, arg2, arg3 });
    }

    public static void io (
        String resource, Object arg1, Object arg2, Object arg3, Object arg4
    ) throws IOException {
        throw new FSException (resource, new Object[] { arg1, arg2, arg3, arg4 });
    }

}

/*
* Log
*  3    Gandalf   1.2         1/16/00  Ian Formanek    NOI18N
*  2    Gandalf   1.1         1/15/00  Jaroslav Tulach Better localization
*  1    Gandalf   1.0         12/30/99 Jaroslav Tulach 
* $ 
*/ 
