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

package org.netbeans.core.windows;

import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;

import org.openide.nodes.Node;
import org.openide.windows.Workspace;
import org.openide.windows.TopComponent;

/** Hash table with weak keys. Maps Workspace::Value
*
* @author Ales Novak
*/
final class WeakHash {

    final Entry[] entries;
    final ReferenceQueue refq;

    /** Creates new WeakHash */
    public WeakHash() {
        entries = new Entry[7];
        refq = new ReferenceQueue();
    }

    int computeHash(Object ob) {
        return (ob.hashCode() & 0x7fffffff) % entries.length;
    }

    public Value put(Workspace ws, Value nodes) {
        /*    System.out.println ("PUT FOR WS: " + ws.getName());
            System.out.println ("ARR: " + nodes);
            if (nodes != null) {
              System.out.println ("FIRST: " + nodes[0]);
              if (nodes[0] != null) {
                for (int i = 0; i < nodes[0].length; i++) {
                  System.out.println (nodes[0][i]);
                }
              }
              System.out.println ("FIRST: " + nodes[1]);
              if (nodes[0] != null) {
                for (int i = 0; i < nodes[1].length; i++) {
                  System.out.println (nodes[1][i]);
                }
              }
            }
        */
        checkq();
        int hash = computeHash(ws);
        Entry e = entries[hash];
        while ((e != null) &&
                !e.wkey.equals(ws)) {
            e = e.next;
        }
        if (e == null) {
            entries[hash] = new Entry(null, ws, nodes, refq, entries, hash);
            return null;
        } else {
            Value old = e.val;
            e.val = nodes;
            return old;
        }
    }

    public Value get(Workspace ws) {
        //    System.out.println ("GET FOR WS: " + ws.getName()); // NOI18N
        checkq();
        int hash = computeHash(ws);
        Entry e = entries[hash];
        while ((e != null) &&
                !e.wkey.equals(ws)) {
            e = e.next;
        }
        if (e == null) {
            //      System.out.println ("NULL"); // NOI18N
            return null;
        } else {
            /*      System.out.println ("ARR: " + e.val);
                  if (e.val != null) {
                    System.out.println ("FIRST: " + e.val[0]);
                    if (e.val[0] != null) {
                      for (int i = 0; i < e.val[0].length; i++) {
                        System.out.println (e.val[0][i]);
                      }
                    }
                    System.out.println ("FIRST: " + e.val[1]);
                    if (e.val[0] != null) {
                      for (int i = 0; i < e.val[1].length; i++) {
                        System.out.println (e.val[1][i]);
                      }
                    }
                  }
            */
            return e.val;
        }
    }
    private void checkq() {
        WeakKey wk = (WeakKey) refq.poll();
        if (wk != null) {
            wk.remove();
        }
    }

    static class Entry {
        Entry next;
        Entry prev;
        WeakKey wkey;
        Value val;
        int hash;
        Entry[] entries;

        Entry(Entry next, Workspace ws, Value val, ReferenceQueue rq, Entry[] es, int hash) {
            this.next = next;
            if (next != null) {
                next.prev = this;
            }
            wkey = new WeakKey(ws, rq, this);
            this.val = val;
            this.hash = hash;
            entries = es;
        }

        void remove() {
            if (prev == null && next == null) {
                return;
            } else if (prev == null) {
                entries[hash] = next;
                next.prev = null;
            } else {
                prev.next = next;
                if (next != null) {
                    next.prev = prev;
                }
            }
        }
    }

    static class WeakKey extends WeakReference {
        int hash;
        Entry my;

        WeakKey(Workspace ws, ReferenceQueue rq, Entry parent) {
            super(ws, rq);
            hash = ws.hashCode();
            my = parent;
        }

        void remove() {
            my.remove();
        }

        Workspace getWorkspace() {
            return (Workspace) this.get();
        }

        public int hashCode() {
            return hash;
        }

        public boolean equals(Object o) {
            if (! (o instanceof Workspace)) {
                return false;
            }
            Workspace me = getWorkspace();
            if (me == null) {
                return o == null;
            } else {
                return me.equals(o);
            }
        }
    }

    static class Value {
        Node[][] activatedNodes;
        TopComponent activatedTC;
    }

}

/*
* Log
*  4    Gandalf   1.3         1/13/00  David Simonek   i18n
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         7/16/99  Ales Novak      bugfix
*  1    Gandalf   1.0         7/15/99  Ales Novak      
* $
*/