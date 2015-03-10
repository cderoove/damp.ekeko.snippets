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

import java.util.*;

import org.openide.*;
import org.openide.util.actions.*;
import org.openide.actions.*;
import org.openide.windows.*;
import org.openide.nodes.*;


import org.openidex.search.*;

/**
 * FindAction performer providing search dialog.
 * This classes is hooked on search action.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */

public class SearchPerformer implements ActionPerformer {

    private final boolean TRACE = false;

    /** Creates new SearchPresenter */
    public SearchPerformer() {
    }


    /**
    * @param nodes currently selected nodes
    * @return set of SearchType Classes that are enabled upon currently
    * selected nodes.
    */
    private Set getTypes(Node[] nodes) {

        HashSet set = new HashSet();

        if (nodes == null) return set;
        if (nodes.length == 0) return set;

        t("Testing ... "); // NOI18N

        // test all search types

        Enumeration en = TopManager.getDefault().getServices().services(SearchType.class);
        while (en.hasMoreElements()) {
            SearchType next = (SearchType) en.nextElement();

            t("Testing " + next ); // NOI18N

            if (next.enabled(nodes))
                set.add(next.getClass());
        }

        return set;

    }

    /** Test if presenter is enabled.
    * @param nodes currently selected nodes
    * @return true if some search type can be applyed
    */
    public boolean enabled(Node[] nodes) {

        int size = getTypes(nodes).size();

        t("test on enabled permitted by " + size); // NOI18N

        return size > 0;

    }


    /** Displays CriteriaView with predefined search types set.
    * @param nodes currently selected nodes
    */
    private void performAction(Node[] nodes) {

        Set criteria = getTypes(nodes);

        if (criteria.size() == 0) return;

        CriteriaModel model = new CriteriaModel(nodes, criteria);

        CriteriaView dialog = new CriteriaView(model);

        dialog.show();

        // if ok (==search) open ResultView

        if (dialog.getReturnStatus() == dialog.RET_OK) {

            ResultModel result = performSearch(nodes, model);

            t("Opening search results."); // NOI18N
            ResultViewTopComponent view = new ResultViewTopComponent(result);
            view.open();
        }
    }

    /**
    */
    private ResultModel performSearch(Node[] nodes, CriteriaModel criteria) {

        SearchEngine engine = new SearchEngineImpl();
        ResultModel target = new ResultModel(criteria);

        SearchTask task = engine.search(nodes, criteria.getCustomizedCriteria(), target);

        target.setTask(task);

        return target;
    }

    /** Make search support moving. Asks SearchEngine for search task. Run it.
    * Collects search results to ResultModel and display it.
    * <p>An entry point.
    */
    public void performAction(final SystemAction action) {

        if ( action instanceof RepositorySearchAction ) {
            RepositorySearchAction raction = (RepositorySearchAction) action;
            performAction( raction.getNodes() );

        } else if (action instanceof FindAction) {
            performAction( TopComponent.getRegistry ().getCurrentNodes () );

        } else {
            // unexpected action
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                throw new RuntimeException("Should not occur."); // NOI18N
        }

    }

    /** TRACE */
    private void t(String msg) {
        if (TRACE)
            System.err.println("SearchPresenter: " + msg);
    }

}


/*
* Log
*  5    Gandalf   1.4         1/13/00  Radko Najman    I18N
*  4    Gandalf   1.3         1/10/00  Petr Kuzel      Buttons enabling.
*  3    Gandalf   1.2         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  2    Gandalf   1.1         12/23/99 Petr Kuzel      Architecture improved.
*  1    Gandalf   1.0         12/17/99 Petr Kuzel      
* $ 
*/ 

