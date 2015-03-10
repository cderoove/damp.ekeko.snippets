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

package org.openidex.search;

import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;

/** Search engine handles following search operations:
* <UL>
* <LI>use of SearchType(s) to search some nodes
* <LI>canceling such search
* <LI>configuration of SearchTypes
* <LI>displaying of results
* </UL>
*
* @author Jaroslav Tulach
*/
public abstract class SearchEngine extends Object {

    private static SearchEngine def = null;

    /** Temporary getter for default instance of SearchEngine.
    * Should be replaced
    * by TopManager.getDefault ().getSearchEngine () when this API
    * moves to OpenAPI.
    *
    * @return the instance of SearchEngine used by the system, or <code>null</code> if none is registered
    */
    public static SearchEngine getDefault () {
        return def;
    }

    /** Install the default search engine implementation.
     * @param engine the new default
     * @throws SecurityException if there was one already
     */
    public static synchronized void setDefault (SearchEngine engine) throws SecurityException {
        if (def != null) throw new SecurityException ();
        def = engine;
    }

    /** Starts search on given array of nodes using given
    * array of SearchTypes. Returns a control object that 
    * allows the caller to check the progress of search.
    * <P>
    * The call to search is non-blocking and returns a special
    * subclass of Task to control the search.
    * Some interesting methods to perform on the task are
    * <PRE>
    *   SearchTask task = engine.search (nodes, types, acceptor);
    *   task.stop (); // stops the search
    *   task.isFinished (); // returns true if the task has been finished
    *   task.waitFinished (); // waits till the task is finished
    *   Node[] result = task.getResult (); // list of nodes found
    *     // during the search and accepted by the acceptor
    * </PRE>
    * 
    * @param nodes array of nodes to start search at
    * @param types array of search types to use during search
    * @param na acceptor to notify about found nodes or null if all
    *    found nodes should be present in the task's result
    * @return task to control the search process
    */
    public abstract SearchTask search (
        Node[] nodes, SearchType[] types, NodeAcceptor na
    );
}

/*
* Log
*  4    Gandalf-post-FCS1.2.1.0     4/4/00   Petr Kuzel      unknown state
*  3    Gandalf   1.2         1/16/00  Jesse Glick     No compile-time 
*       dependency on search module.
*  2    Gandalf   1.1         12/15/99 Martin Balin    Fixed package statement
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 
