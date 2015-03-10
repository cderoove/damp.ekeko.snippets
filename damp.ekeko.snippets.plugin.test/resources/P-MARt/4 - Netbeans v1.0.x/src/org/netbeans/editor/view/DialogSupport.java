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

package org.netbeans.editor.view;

/** Support for creation of all the known dialogs
*
* @author Miloslav Metelka
* @version 1.00
*/

public class DialogSupport {

    private static DialogCreator dialogCreator;

    private static FindDialogSupport findDialogSupport;

    private static GotoDialogSupport gotoDialogSupport;

    public static DialogCreator getDialogCreator() {
        if (dialogCreator == null) {
            dialogCreator = new DefaultDialogCreator();
        }
        return dialogCreator;
    }

    public static void setDialogCreator(DialogCreator newDialogCreator) {
        dialogCreator = newDialogCreator;
    }

    public static FindDialogSupport getFindDialogSupport() {
        if (findDialogSupport == null) {
            findDialogSupport = new FindDialogSupport();
        }
        return findDialogSupport;
    }

    public static void setFindDialogSupport(FindDialogSupport newFindDialogSupport) {
        findDialogSupport = newFindDialogSupport;
    }

    public static GotoDialogSupport getGotoDialogSupport() {
        if (gotoDialogSupport == null) {
            gotoDialogSupport = new GotoDialogSupport();
        }
        return gotoDialogSupport;
    }

    public static void setGotoDialogSupport(GotoDialogSupport newGotoDialogSupport) {
        gotoDialogSupport = newGotoDialogSupport;
    }

}

/*
 * Log
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         7/20/99  Miloslav Metelka 
 *  2    Gandalf   1.1         5/16/99  Miloslav Metelka 
 *  1    Gandalf   1.0         5/5/99   Miloslav Metelka 
 * $
 */

