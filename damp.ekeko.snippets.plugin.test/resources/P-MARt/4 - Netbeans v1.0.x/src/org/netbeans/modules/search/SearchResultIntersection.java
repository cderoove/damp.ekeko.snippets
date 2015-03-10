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
import org.openide.nodes.*;
import org.openidex.search.*;

/**
 * Intersect results from more scan tasks.
 * Functionality depends on equals() method of found nodes.
 *
 * @author  Petr kuzel
 * @version 1.0
 */
public class SearchResultIntersection implements ScannerListener {

    // checks for intersection


    /**
     * @associates Integer 
     */
    private HashMap cache;

    // number of scanners creating intersection result
    private int scanners;

    private ScannerListener target;

    private final boolean TRACE = false;

    /** Creates new Composer that notifies target. */
    public SearchResultIntersection(ScannerListener target, int scanTasks) {
        cache = new HashMap();
        scanners = scanTasks;
        this.target = target;

        t("task num:" + scanners); // NOI18N
    }


    /**
    * Is found by all other scanners?
    * depends on associated Integer that match number of matches
    * @return true if the node was found by all branch scanners
    */
    private synchronized boolean cacheHit(Node node) {

        int i = 1; // number of hits on this node

        if (scanners == 1) return true;

        if ( cache.containsKey(node) ) {
            i = ((Integer)cache.get(node)).intValue() + 1;
        }

        if (i < scanners) { // someone does not found it, yet

            cache.put(node, new Integer(i));
            return false;

        } else {

            if (i>1) cache.remove(node);
            return true;

        }
    }


    /** Intersect scanner results.
    * All scanners results are test on equality.
    */
    public void scannerFound (ScannerEvent se) throws InterruptedException {

        Vector ret = new Vector();
        boolean added  = false;

        Node[] nodes = se.getFound();

        t("event contains: " + nodes[0]); // NOI18N

        for (int i = 0; i<nodes.length; i++) {
            if ( cacheHit(nodes[i]) ) {
                ret.add(nodes[i]);
                added = true;
            }
        }
        if (added) {
            Node[] res = new Node[ret.size()];
            ret.copyInto(res);

            t("forwarding: " + target + " > " + res[0]); // NOI18N

            target.scannerFound(new ScannerEvent(res));
        }
    }

    private void t(String msg) {
        if (TRACE)
            System.err.println("Intercection: " + msg);
    }

}


/*
* Log
*  4    Gandalf   1.3         1/13/00  Radko Najman    I18N
*  3    Gandalf   1.2         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  2    Gandalf   1.1         12/14/99 Petr Kuzel      Minor enhancements
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 

