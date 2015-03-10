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

package org.openide.compiler;

import java.util.*;

import org.openide.util.enum.*;

/** Graph of objects that is build over one Compilable object.
*
* @author Jaroslav Tulach
*/
final class Graph extends Object {
    /** vertexes indexed by Compilables (Compilable, Vertex) 
     * @associates Vertex*/
    private HashMap vertex = new HashMap (31);
    /** list with all levels */
    private List levels;

    /** Creates new Graph.
    * Stores all dependencies into Vertexes so later changes to
    * deps are ignored. This constructor should be invoked under
    * Compilable.MUTEX.writeAccess so nobody can change the
    * dependencies when the constructor is executing.
    */
    public Graph (final Compilable c) {
        Enumeration en = compilables (c);

        while (en.hasMoreElements ()) {
            Compilable com = (Compilable)en.nextElement ();

            Vertex v = (Vertex)vertex.get (com);
            if (v == null) {
                v = new Vertex ();
                vertex.put (com, v);
            }
            v.add (com);
        }

        addDeps ();
    }

    /** Test whether the graph is up-to-date
    * @return true if it is
    */
    public boolean isUpToDate () {
        Iterator it = vertex.values ().iterator ();
        while (it.hasNext ()) {
            Vertex v = (Vertex)it.next ();

            if (!v.isUpToDate ()) {
                return false;
            }
        }
        return true;
    }

    /** Getter for levels in the graph.
    * @return List[Set[Compiler]]
    * @exception CyclicDependencyException if cycle has been detected
    */
    public synchronized List getLevels () throws DependencyException {
        if (levels != null) {
            return levels;
        }

        List cycle = depth ();

        if (cycle == null) {
            // ok, levels computed
            return levels;
        }

        ListIterator it = cycle.listIterator ();
        while (it.hasNext ()) {
            Vertex v = (Vertex)it.next ();

            // first object
            it.set (v.compilables.iterator ().next ());
        }

        Compilable[] arr = new Compilable[cycle.size ()];
        cycle.toArray (arr);
        throw new DependencyException (arr);
    }



    /** Adds dependencies for all vertexes
    */
    private void addDeps () {
        Iterator it = vertex.values ().iterator ();
        while (it.hasNext ()) {
            Vertex v = (Vertex)it.next ();
            addDeps (v);
        }
    }

    /** Adds dependencies for given vertex.
    * @param v vertex
    */
    private void addDeps (Vertex v) {
        Iterator it = v.compilables.iterator ();

        while (it.hasNext ()) {
            Compilable c = (Compilable)it.next ();
            Iterator di = c.dependsOn ().iterator ();
            while (di.hasNext ()) {
                Compilable dc = (Compilable)di.next ();
                Vertex dv = (Vertex)vertex.get (dc);
                // also all compilers in compilable v depends on dv
                addCompilerDep (null, c.compilers (), dv);
            }

            addCompilerDep (v, c.compilers (), null);
        }
    }

    /** Adds dependency of compilers to a vertex.
    * @param compOwner vertex that should depend on all of the compilers 
    * @param col collection of compilers
    * @param v vertex to depend on
    */
    private void addCompilerDep (Vertex compOwner, Collection col, Vertex v) {
        Iterator it = col.iterator ();
        while (it.hasNext ()) {
            Compiler c = (Compiler)it.next ();

            Vertex cv = (Vertex)vertex.get (c);
            if (v != null) {
                cv.addDep (v);
            }

            if (compOwner != null && compOwner != cv) {
                // compOwner depends on each compiler except
                // special case when the owner and compiler are the
                // same objects
                compOwner.addDep (cv);
            }
        }
    }

    /** Assignes depth to vertexes in the graph.
    * @return null if it succeeds or List of Vertex objects that
    *   form a cycle
    */
    private List depth () {
        Iterator it = vertex.values ().iterator ();

        int max = -1;
        while (it.hasNext ()) {
            Vertex v = (Vertex)it.next ();
            if (v.depth > 0) {
                continue;
            }

            List cycle = computeDepth (v);
            if (cycle != null) {
                return cycle;
            }
            if (v.depth > max) {
                max = v.depth;
            }
        }

        // put into levels

        Set[] arr = new Set [max];
        it = vertex.values ().iterator ();
        while (it.hasNext ()) {
            Vertex v = (Vertex)it.next ();
            int indx = v.depth - 1;
            if (arr[indx] == null) {
                arr[indx] = new HashSet (17);
            }
            addCompilersFrom (arr[indx], v.compilables);
        }
        levels = Arrays.asList (arr);


        return null;
    }

    /** Takes array of Compilables takes all Compilers from it
    * and add them into the first collection
    * @param compilers to add to
    * @param compilables to take from 
    */
    private static void addCompilersFrom (
        Collection compilers, Collection compilables
    ) {
        Iterator it = compilables.iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof Compiler) {
                compilers.add (o);
            }
        }
    }

    /** Goes thru all of the vertexes and indexes them.
    * @param v the vertex to start at
    * @return the list of Vertex in cycle or null
    */
    private static LinkedList computeDepth (Vertex v) {
        if (v.dependsOn == null) {
            v.depth = 1;
            return null;
        }

        if (v.depth == -1) {
            // cycle
            LinkedList ll = new LinkedList ();
            ll.add (v);
            return ll;
        }

        // to signal that we began processing
        v.depth = -1;

        int max = -1;
        Iterator it = v.dependsOn.iterator ();
        while (it.hasNext ()) {
            Vertex child = (Vertex)it.next ();
            LinkedList ll = computeDepth (child);
            if (ll != null) {
                if (ll.size () == 1 || ll.getFirst () != ll.getLast ()) {
                    // we are still in the cycle
                    // otherwise only get from the recursion
                    ll.add (v);
                }

                return ll;
            }

            if (child.depth > max) {
                max = child.depth;
            }
        }

        if (max == -1 || v.anyCompiler () != null) {
            v.depth = max + 1;
        } else {
            v.depth = max;
        }
        return null;
    }

    /** Enumeration of all compilables accessible from the one.
    * @param c the compilable
    * @return enum of Compilable
    */
    private static Enumeration compilables (Compilable c) {
        QueueEnumeration en = new QueueEnumeration () {
                                  private IdSet set = new IdSet ();

                                  public void process (Object o) {
                                      Compilable comp = (Compilable)o;
                                      set.add (comp);

                                      LinkedList ll = new LinkedList (comp.compilers ());
                                      ll.addAll (comp.dependsOn ());

                                      ll.removeAll (set);

                                      put (ll.toArray ());
                                  }
                              };
        en.put (c);
        return en;
    }


    /** One vertex in the graph. Contains list of all compilers that
    * are equal and collection of other vertexes it depends on.
    */
    private final static class Vertex extends Object {
        /** all compilers that are equal 
         * @associates Compilable*/
        public Collection compilables = new IdSet ();
        /** vertexes this depend on 
         * @associates Vertex*/
        public Collection dependsOn;
        /** depth in the graph */
        public int depth;

        private static int count;
        private int cnt = ++count;

        /** debug */
        public String toString () {
            StringBuffer sb = new StringBuffer ();
            sb.append ("Vertex "); // NOI18N
            sb.append (cnt);
            sb.append (" ("); // NOI18N
            sb.append (compilables);
            sb.append (", "); // NOI18N
            if (dependsOn == null) {
                sb.append ("null, "); // NOI18N
            } else {
                sb.append ("{"); // NOI18N
                Iterator it = dependsOn.iterator();
                Vertex v = (Vertex)it.next ();
                sb.append (v.cnt);
                while (it.hasNext()) {
                    sb.append (", "); // NOI18N
                    Vertex vx = (Vertex)it.next ();
                    sb.append (vx.cnt);
                }
                sb.append ("}, "); // NOI18N
            }
            sb.append (depth);
            sb.append (")"); // NOI18N
            return sb.toString ();
        }

        public void add (Compilable c) {
            compilables.add (c);
        }

        public void addDep (Vertex v) {
            if (dependsOn == null) {
                dependsOn = new IdSet ();
            }
            dependsOn.add (v);
        }

        public Compiler anyCompiler () {
            Iterator it = compilables.iterator ();
            while (it.hasNext ()) {
                Object o = it.next ();
                if (o instanceof Compiler) {
                    return (Compiler)o;
                }
            }
            return null;
        }

        public boolean isUpToDate () {
            Iterator it = compilables.iterator ();
            while (it.hasNext ()) {
                Object o = it.next ();
                if (o instanceof Compiler && !((Compiler)o).isUpToDate ()) {
                    return false;
                }
            }
            return true;
        }

    }
}

/*
* Log
*  3    Gandalf   1.2         1/13/00  Ian Formanek    NOI18N
*  2    Gandalf   1.1         1/13/00  Ales Novak      System.out.println 
*       removed
*  1    Gandalf   1.0         12/23/99 Jaroslav Tulach 
* $ 
*/ 
