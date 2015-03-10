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

package org.netbeans.modules.debugger.debug;

import java.util.ResourceBundle;
import javax.swing.JComponent;

import org.openide.TopManager;
import org.openide.text.Line;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.debugger.Watch;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

import org.netbeans.modules.debugger.support.PrintAction;
import org.netbeans.modules.debugger.support.CoreBreakpoint;


/**
*
* @author   Jan Jancura
*/
public class ToolsPrintAction extends PrintAction {

    // init ...............................................................................................

    static final long serialVersionUID =-5788636383102639577L;
    /**
    * Creates the new Print action with given text.
    */
    public ToolsPrintAction (String s) {
        super (s);
    }


    // CoreBreakpoint.Action implementation ................................................................

    /**
    * Returns new initialized instance of Print action.
    */
    protected CoreBreakpoint.Action getNewInstance () {
        return new ToolsPrintAction (text);
    }


    // other methods ...................................................................................

    /**
    * Returns value of given variable as text. Can be changed by debugger
    * implementations. Default implementation uses hidden watch for getting this
    * value.
    */
    protected String getValue (String variable, CoreBreakpoint.Event event) {
        Watch w = event.getDebugger ().createWatch (variable, true);
        ((ToolsWatch) w).refreshValue ((ToolsThread) event.getThread ());
        return w.getAsText ();
    }
}

/*
* Log
*  4    Gandalf   1.3         11/8/99  Jan Jancura     Somma classes renamed
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  1    Gandalf   1.0         7/30/99  Jan Jancura     
* $
*/
