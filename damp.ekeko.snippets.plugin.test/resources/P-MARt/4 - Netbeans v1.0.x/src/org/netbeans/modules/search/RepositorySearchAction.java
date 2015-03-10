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

package org.netbeans.modules.search;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.actions.*;
import org.openide.util.*;


import org.netbeans.modules.search.res.*;

/**
 *
 * @author  Petr Kuzel
 * @version 
 */
public class RepositorySearchAction extends CallableSystemAction {

    public static final long serialVersionUID = 1;

    /** Creates new RepositorySearchAction */
    public RepositorySearchAction() {
        setIcon(Res.icon("SEARCH")); // NOI18N
    }

    /** @return human readable name of action */
    public String getName() {
        return Res.text("ACTION_REPOSITORY_SEARCH"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (RepositorySearchAction.class);
    }

    /** Where to search */
    public Node[] getNodes() {
        return new Node[] {TopManager.getDefault().getPlaces().nodes().repository()};
    }

    /** Perform this action. */
    public void performAction() {
        new SearchPerformer().performAction(this);
    }

}


/*
* Log
*  5    Gandalf   1.4         1/18/00  Jesse Glick     Context help.
*  4    Gandalf   1.3         1/13/00  Radko Najman    I18N
*  3    Gandalf   1.2         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  2    Gandalf   1.1         12/17/99 Petr Kuzel      Bundling.
*  1    Gandalf   1.0         12/16/99 Petr Kuzel      
* $ 
*/ 

