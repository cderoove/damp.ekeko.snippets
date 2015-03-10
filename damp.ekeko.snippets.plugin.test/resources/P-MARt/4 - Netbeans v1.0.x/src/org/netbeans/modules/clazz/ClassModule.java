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

package org.netbeans.modules.clazz;

import org.openide.cookies.SourceCookie;
import org.openide.src.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.modules.ModuleInstall;
import org.openide.TopManager;

/**
* Module installation class for Clazz DO.
*
* @author Jaroslav Tulach, Petr Hamernik
*/
public class ClassModule extends ModuleInstall
    implements ClassElement.Finder {

    static final long serialVersionUID =-3725446838997905475L;


    /** Method to find a class element.
    */
    public ClassElement find (Class clazz) {
        return new ClassElement (new ClassElementImpl (clazz),
                                 new SourceElement(new SourceElementImpl(clazz)));
    }

    // JST:
    /** This method does nothing, because the current impl (jaga)
    * will call the find (Class) if this method returns null and no
    * other finder is interested in the class of this name.
    *
    * @param name class name to find
    * @return null
    */
    public ClassElement find (String name) {
        return null;
    }

    /** Module installed again. */
    public void restored() {
        ClassElement.register (this);
    }

    /** Module was uninstalled. */
    public void uninstalled() {
        ClassElement.unregister (this);
    }

}

/*
 * Log
 *  8    Gandalf   1.7         2/14/00  Jaroslav Tulach Added second 
 *       ClassElement.Finder.find method. It will be called by  some 
 *       modifications in Jaga view, so it has not impact on 1.0 code base.  
 *  7    Gandalf   1.6         11/27/99 Patrik Knakal   
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
 *        changed to class + some methods added
 *  4    Gandalf   1.3         7/25/99  Petr Hamernik   temporary solution - 
 *       must be improved
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/6/99   Petr Hamernik   find rewritten
 *  1    Gandalf   1.0         4/26/99  Jaroslav Tulach 
 * $
 */
