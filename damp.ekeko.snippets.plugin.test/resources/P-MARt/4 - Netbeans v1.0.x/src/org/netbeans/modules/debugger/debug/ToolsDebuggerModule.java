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

import org.openide.TopManager;

import org.netbeans.modules.debugger.delegator.DelegatingDebugger;


/**
* Module installation class for HtmlModule
*
* @author Jan Jancura
*/
public class ToolsDebuggerModule extends org.openide.modules.ModuleInstall {

    static final long serialVersionUID =8693835612618896479L;
    /** Module installed again. */
    public void restored () {
        //S ystem.out.println ("installdebugger"); // NOI18N
        try {
            DelegatingDebugger.registerDebugger (
                new ToolsDebugger (
                    ((DelegatingDebugger) TopManager.getDefault ().getDebugger ()).
                    isMultiSession (),
                    ((DelegatingDebugger) TopManager.getDefault ().getDebugger ()).
                    getValidator ()
                )
            );
        } catch (Exception e) {
            //e.printStackTrace ();
        }
    }

    /** Module was uninstalled. */
    public void uninstalled () {
        try {
            DelegatingDebugger.unregisterDebugger (
                ToolsDebugger.class
            );
        } catch (Exception e) {
            //e.printStackTrace ();
        }
    }

}

/*
* Log
*  10   Gandalf-post-FCS1.8.3.0     3/28/00  Daniel Prusa    
*  9    Gandalf   1.8         1/14/00  Daniel Prusa    NOI18N
*  8    Gandalf   1.7         11/27/99 Patrik Knakal   
*  7    Gandalf   1.6         11/8/99  Jan Jancura     Somma classes renamed
*  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
*        changed to class + some methods added
*  4    Gandalf   1.3         9/2/99   Jan Jancura     
*  3    Gandalf   1.2         7/21/99  Jan Jancura     
*  2    Gandalf   1.1         7/13/99  Jan Jancura     
*  1    Gandalf   1.0         7/13/99  Jan Jancura     
* $
*/
