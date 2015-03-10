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

import java.util.TooManyListenersException;

import org.openide.nodes.Node;

/** Scanner above a SearchType or set of SearchTypes.
* Has to have default constructor because it is constructed
* dynamicly by the search machine for a set of SearchTypes that
* return the same class of Scanner.
* <P>
* After the instance is created the SearchTypes are registered
* to the Scanner by method add.
*
* @author Jaroslav Tulach
*/
public abstract class Scanner extends Object {
    /** listener attached to scanner or null */
    protected ScannerListener listener;

    /** All subclasses have to have default constructor.
    */
    public Scanner() {
    }

    /** Registers a SearchType to the Scanner. It is
    * guaranteed that the SearchType previously returned
    * this Scanner's class from its scannerClass () method.
    * <P>
    * This method allows a Scanner to obtain data
    * entered by user into SearchType.
    *
    * @param st the search type
    * @exception ClassCastException if the kriterium passed is
    *   not the right one (which should never happen)
    */
    public  abstract void add (SearchType st);


    /** Adds a listener to things that happen in the
    * scanner. This is a unisource listener.
    *
    * @param l the listener
    * @exception TooManyListenersException
    */
    public synchronized void addScannerListener (ScannerListener l)
    throws TooManyListenersException {
        if (listener != null) {
            throw new TooManyListenersException ();
        }
        listener = l;
    }

    /** Removes a listener.
    * 
    * @param l the listener
    */
    public synchronized void removeScannerListener (ScannerListener l) {
        if (listener == l) {
            listener = null;
        }
    }

    /** Notify listener about new found nodes.
    */
    protected void notifyFound(Node[] nodes) throws InterruptedException {
        ScannerListener lis;
        synchronized (this) {
            if (listener == null) return;
            lis = listener;
        }

        lis.scannerFound(new ScannerEvent(nodes));
    }

    /** Starts the scan on given array of nodes and notifies the
    * associated listener about found results.
    * 
    * @param nodes nodes to perform scan on
    */
    public  abstract void scan (Node[] nodes);
}

/*
* Log
*  4    Gandalf-post-FCS1.1.1.1     4/4/00   Petr Kuzel      unknown state
*  3    Gandalf-post-FCS1.1.1.0     4/4/00   Petr Kuzel      unknown status
*  2    Gandalf   1.1         12/15/99 Martin Balin    Fixed package statement
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 
