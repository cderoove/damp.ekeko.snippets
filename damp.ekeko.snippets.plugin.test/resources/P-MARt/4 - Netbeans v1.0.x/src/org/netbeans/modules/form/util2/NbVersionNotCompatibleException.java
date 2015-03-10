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

package org.netbeans.modules.form.util2;

import java.text.MessageFormat;

/** Exception that is thrown when a version of some class does not
* conform to the required version.
*
* @author Ian Formanek
* @version 0.11, Jul 14, 1998
* @deprecated Please use Java Versioning Specification.
*/
public class NbVersionNotCompatibleException extends ClassNotFoundException {

    static final long serialVersionUID =8387009413680222279L;
    /** COnstructs a new NbVersionNotCompatibleException with specified pair of versions that do not match.
    * @param actualVersion The current version of the class
    * @param requiredVersion The required (expected) version of the class
    */
    public NbVersionNotCompatibleException (NbVersion actualVersion, NbVersion requiredVersion) {
        //    super(formatMessage (actualVersion, requiredVersion));
        this.requiredVersion = requiredVersion;
        this.actualVersion = actualVersion;
    }

    /** Formats message.
    */
    private static String formatMessage (NbVersion actualVersion, NbVersion requiredVersion) {
        return MessageFormat.format(
                   org.openide.util.NbBundle.getBundle (org.netbeans.modules.form.util2.NbVersion.class).getString("EXC_NBVersion"),
                   new Object[] { requiredVersion, actualVersion }
               );
    }

    /** @return the actual version of the class (whose loading caused this exception) */
    public NbVersion getActualVersion () {
        return actualVersion;
    }

    /** @return the required version of the class (whose loading caused this exception) */
    public NbVersion getRequiredVersion () {
        return requiredVersion;
    }

    private NbVersion requiredVersion;
    private NbVersion actualVersion;
}

/*
 * Log
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         5/15/99  Ian Formanek    
 * $
 */
