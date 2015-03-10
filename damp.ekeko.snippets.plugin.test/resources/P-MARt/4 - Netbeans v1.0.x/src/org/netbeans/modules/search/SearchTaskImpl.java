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

import org.openidex.search.*;
import org.openide.nodes.*;


/**
 * Task performing search.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class SearchTaskImpl extends SearchTask {

    // ScannerThread management fields


    /**
     * @associates Thread 
     */
    private Vector threads;
    private volatile boolean stop = false;
    private ThreadGroup group;

    // constructor fields
    private Node[] nodes;
    private SearchType[] types;
    private NodeAcceptor na;

    /** Holds all found nodes. */
    private Vector found = new Vector();

    private final boolean TRACE = false;

    /** Creates new SearchTaskImpl
    * @param nodes search starting points
    * @param types search criteria
    * @param na who could be notified
    */
    public SearchTaskImpl(Node[] nodes, SearchType[] types, NodeAcceptor na) {

        super(org.openide.util.Task.EMPTY);

        group = new ThreadGroup("Search group"); // NOI18N
        group.setDaemon(true);
        group.setMaxPriority(3);

        threads = new Vector();

        this.nodes = nodes;
        this.types = types;

        this.na = na;
    }

    /** Can be tested is finnished
    */
    public void run() {

        t("run()"); // NOI18N

        try {

            // set of scanner instances to be used
            // more searchtypes can share one scanner instance
            HashMap scannerInstances = new HashMap();

            if (types == null) return;

            // fill scanner instances

            for (int i = 0; i<types.length; i++) {

                try {
                    Class clzz = types[i].getScannerClass();

                    if ( ! scannerInstances.containsKey(clzz) ) {
                        scannerInstances.put(clzz, clzz.newInstance());
                    }

                    ((Scanner) scannerInstances.get(clzz)).add(types[i]);

                } catch (InstantiationException ex) {
                    // ignore such scanner
                } catch (IllegalAccessException ex) {
                    // ignore such scanner
                }
            }


            AcceptAdapter adapter = new AcceptAdapter(na);
            SearchResultIntersection intersector =
                new SearchResultIntersection(adapter , scannerInstances.size());

            Iterator it = scannerInstances.values().iterator();
            while (it.hasNext()) {
                Scanner next = (Scanner) it.next();

                t("scanner: " + next); // NOI18N
                ScannerThread thread = new ScannerThread(next, nodes);
                try {
                    next.addScannerListener(intersector);

                    Thread t = new Thread(group, thread, next.toString());
                    t.start();
                    threads.add(t);

                } catch (TooManyListenersException ex) {
                    // ignore such scanner
                }

            }


            //join all scanner threads

            Iterator tit = threads.iterator();
            while (tit.hasNext()) {
                Thread next = (Thread) tit.next();

                try {
                    next.join(stop ? 1:0);
                } catch (InterruptedException ex) {
                    // Ok the thread is interrupted
                }

                if (next.isAlive()) {
                    next.setPriority(1);
                }
            }

        } finally {
            notifyFinished();
            notifyStop();
            t("done."); // NOI18N
        }
    }

    /** Stop all running ScannerThreads
    */
    public void stop() {

        t("stop()"); // NOI18N

        try {
            group.interrupt();
            group.setMaxPriority(1);
            group.setDaemon(true);
        } catch (Exception ex) {
            // we are stopping threads
            // and no exception stop us from it
        } finally {
            notifyStop();
        }
    }

    /**
    * Block until the search is done then return all found nodes.
    * @return found nodes
    */
    public Node[] getResult() {
        waitStop();
        return (Node[]) found.toArray();
    }

    /** Block until search is finished. */
    private synchronized void waitStop() {
        try {
            while (!stop) wait();
        } catch (InterruptedException ex) {
            // proceed
        }
    }

    /** Notify that search has finished. */
    private synchronized void notifyStop() {
        stop = true;
        notifyAll();
    }

    /** One scanning thread.
    */
    private class ScannerThread implements Runnable {
        private Node[] nodes;
        private Scanner scanner;

        /**
        * @param scanner instance that knows what to search (add(SearchType))
        * @param nodes search starting pojnts
        */
        public ScannerThread(Scanner scanner, Node[] nodes) {
            this.scanner = scanner;
            this.nodes = nodes;
        }

        public void run() {
            scanner.scan(nodes);
        }
    }


    /** Translates search events to node acceptor calls
    */
    private class AcceptAdapter implements ScannerListener {

        private final NodeAcceptor acceptor;

        public AcceptAdapter(NodeAcceptor acceptor) {
            this.acceptor = acceptor;
        }

        /** Notification about new found nodes.
         * @throw InterruptedException to iterrupt scanner task
         */
        public void scannerFound(ScannerEvent event) throws InterruptedException {
            if (stop)
                throw new InterruptedException();

            //notify a interested acceptor
            if (acceptor != null) {
                acceptor.acceptNodes(event.getFound());
            }

            found.addAll(Arrays.asList(event.getFound()));
        }
    }

    private void t(String msg) {
        if (TRACE)
            System.err.println("SearchTaskI " + msg);
    }

}


/*
* Log
*  7    Gandalf-post-FCS1.5.1.0     3/23/00  Petr Kuzel      NullPointer bug fix.
*  6    Gandalf   1.5         1/13/00  Radko Najman    I18N
*  5    Gandalf   1.4         1/11/00  Petr Kuzel      Result details added.
*  4    Gandalf   1.3         1/10/00  Petr Kuzel      Buttons enabling.
*  3    Gandalf   1.2         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  2    Gandalf   1.1         12/14/99 Petr Kuzel      Minor enhancements
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 

