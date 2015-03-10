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

import org.openide.nodes.*;

import org.openidex.search.*;


/**
 * Singleton representing search engine.
 * It spawns search task threads.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class SearchEngineImpl extends SearchEngine {

    /** Creates new SearchEngineImpl */
    public SearchEngineImpl() {
    }

    /** Delegate it on search task and executes it in new thread.
    * @return SearchTask can be tested on end using addTaskListener()
    */
    public SearchTask search ( Node[] nodes, SearchType[] types, NodeAcceptor na ) {
        SearchTask task = new SearchTaskImpl(nodes, types, na);

        Thread t = new Thread(task, "SearchTaskImpl"); // NOI18N

        t.setDaemon(true);
        t.start();

        return task;
    }

}


/*
* Log
*  5    Gandalf   1.4         1/13/00  Radko Najman    I18N
*  4    Gandalf   1.3         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  3    Gandalf   1.2         12/23/99 Petr Kuzel      Architecture improved.
*  2    Gandalf   1.1         12/14/99 Petr Kuzel      Minor enhancements
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 

