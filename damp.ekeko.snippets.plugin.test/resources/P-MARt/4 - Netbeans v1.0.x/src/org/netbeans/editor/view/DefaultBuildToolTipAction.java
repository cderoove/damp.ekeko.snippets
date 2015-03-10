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

import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.ToolTipSupport;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.Utilities;

/**
* Customized settings for NetBeans editor
*
* @author Miloslav Metelka
* @version 1.00
*/

public class DefaultBuildToolTipAction extends BaseAction {

    static final long serialVersionUID =-2701131863705941250L;
    public DefaultBuildToolTipAction() {
        super(BaseKit.buildToolTipAction, NO_RECORDING);
    }

    protected String buildText(JTextComponent c) {
        return null;
    }

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            ToolTipSupport tts = Utilities.getExtUI(target).getToolTipSupport();
            if (tts != null) {
                tts.setToolTipText(buildText(target));
            }
        }
    }

}

/*
 * Log
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/30/99  Miloslav Metelka 
 *  2    Gandalf   1.1         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  1    Gandalf   1.0         7/26/99  Miloslav Metelka 
 * $
 */

