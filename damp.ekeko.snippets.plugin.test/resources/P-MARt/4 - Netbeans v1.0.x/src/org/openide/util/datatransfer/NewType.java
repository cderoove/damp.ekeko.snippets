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

package org.openide.util.datatransfer;

import java.util.ResourceBundle;
import java.io.IOException;

import org.openide.util.NbBundle;

/** Describes a type that can be created anew.
* @see org.openide.nodes.Node#getNewTypes
* @author Jaroslav Tulach
* @version 0.10, Mar 19, 1998
*/
public abstract class NewType extends Object {
    /** Display name for the creation action. This should be
    * presented as an item in a menu.
    *
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle (NewType.class).getString ("Create");
    }

    /** Help context for the creation action.
    * @return the help context
    */
    public org.openide.util.HelpCtx getHelpCtx() {
        return org.openide.util.HelpCtx.DEFAULT_HELP;
    }

    /** Create the object.
    * @exception IOException if something fails
    */
    public abstract void create () throws IOException;

    /* JST: Originally designed for dnd and it now uses getDropType () of a node.
    *
    * Create the object at a specific position.
    * The default implementation simply calls {@link #create()}.
    * Subclasses may
    * allow pastes to a specific index in their
    * children list (if the object has children indexed by integer).
    *
    * @param indx index to insert into, can be ignored if not supported
    * @throws IOException if something fails
    *
    public void createAt (int indx) throws IOException {
      create ();
}
    */
}

/*
 * Log
 *  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         7/29/99  Jaroslav Tulach no createAt
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  4    Gandalf   1.3         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  3    Gandalf   1.2         3/10/99  Jesse Glick     [JavaDoc]
 *  2    Gandalf   1.1         2/19/99  Jaroslav Tulach Deleted 
 *       CreateOperationException
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
