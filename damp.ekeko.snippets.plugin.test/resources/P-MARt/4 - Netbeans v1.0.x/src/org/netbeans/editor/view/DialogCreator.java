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

import java.awt.Dialog;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;

/** Interface describing methods for creation
* of all dialogs related to editor functionality
*
* @author Miloslav Metelka
* @version 1.00
*/

public interface DialogCreator {

    public Dialog createFindDialog(JPanel findPanel, JButton[] buttons,
                                   int defaultButtonIndex, int cancelButtonIndex, ActionListener l);

    public Dialog createGotoDialog(JPanel gotoPanel, JButton[] buttons,
                                   int defaultButtonIndex, int cancelButtonIndex, ActionListener l);

}

/*
 * Log
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         5/16/99  Miloslav Metelka 
 *  1    Gandalf   1.0         5/5/99   Miloslav Metelka 
 * $
 */

