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

import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

/** Clipboard operation providing one kind of paste action.
* @see org.openide.nodes.Node#getPasteTypes
* @author Petr Hamernik
* @version 0.11, Jan 16, 1998
*/
public abstract class PasteType extends Object {
    /** Display name for the paste action. This should be
    * presented as an item in a menu.
    *
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle (PasteType.class).getString ("Paste");
    }

    /** Help content for the action.
    * @return the help context
    */
    public org.openide.util.HelpCtx getHelpCtx() {
        return org.openide.util.HelpCtx.DEFAULT_HELP;
    }

    /** Perform the paste action.
    * @return transferable which should be inserted into the clipboard after the
    *         paste action. It can be <code>null</code>, meaning that the clipboard content
    *         is not affected. Use e.g. {@link ExTransferable#EMPTY} to clear it.
    * @throws IOException if something fails
    */
    public abstract Transferable paste() throws IOException;

    /* JST: Originally designed for dnd and it now uses getDropType () of a node.
    *
    * Perform the paste action at an index.
    * @see NewType#createAt(int)
    * @param indx index to insert into, can be ignored if not supported
    * @return new transferable to be inserted into the clipboard
    *  public Transferable pasteAt (int indx) throws IOException {
      return paste ();
}
    */
}


/*
 * Log
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         7/29/99  Jaroslav Tulach no pasteAt
 *  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  5    Gandalf   1.4         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  4    Gandalf   1.3         3/17/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         3/10/99  Jesse Glick     [JavaDoc]
 *  2    Gandalf   1.1         2/25/99  Jaroslav Tulach 
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach added pasteAt
 */
