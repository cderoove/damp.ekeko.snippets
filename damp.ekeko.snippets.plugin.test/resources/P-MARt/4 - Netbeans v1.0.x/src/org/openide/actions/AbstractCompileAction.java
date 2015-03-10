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

package org.openide.actions;

import java.util.Enumeration;
import java.util.Iterator;
import java.text.MessageFormat;

import org.openide.loaders.DataObject;
import org.openide.TopManager;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerTask;
import org.openide.cookies.CompilerCookie;
import org.openide.util.enum.AlterEnumeration;
import org.openide.util.enum.ArrayEnumeration;
import org.openide.util.enum.FilterEnumeration;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.nodes.Node;

/** Compilation action--compiles all selected nodes.
* Concrete subclasses must specify what type of
* compilation is needed (e.g. compile vs. build) and whether to operate
* recursively.
* @see org.openide.compiler
*
* @author   Jaroslav Tulach, Petr Hamernik
*/
public abstract class AbstractCompileAction extends NodeAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5089785814030008824L;

    /* Compiles the nodes */
    protected void performAction (Node[] activatedNodes) {
        compileNodes (activatedNodes);
    }

    /** Checks whether the depth is supported.
    */
    protected boolean enable (Node[] arr) {
        if (arr.length == 0) {
            return false;
        }

        Class cookie = cookie ();
        Compiler.Depth depth = depth ();
        for (int i = 0; i < arr.length; i++) {
            CompilerCookie cc = (CompilerCookie)arr[i].getCookie (cookie);
            if (cc == null || !cc.isDepthSupported (depth)) {
                return false;
            }
        }
        return true;
    }

    /** Get the depth the compiler compiles on.
    * @return depth for the job that this compiler works on
    */
    protected abstract Compiler.Depth depth ();


    /** Get the requested cookie class.
    * @return the class, e.g. {@link CompilerCookie.Compile}
    */
    protected abstract Class cookie ();

    /** Message to display when the action is looking for
    * object that should be processed.
    *
    * @return text to display at status line
    */
    protected String message () {
        return ActionConstants.BUNDLE.getString ("CTL_CompilationStarted");
    }

    /** Compiles a set of nodes.
    * @param nodes the nodes
    */
    void compileNodes (final Node[] nodes) {
        Thread t = new Thread ("Find nodes to compile") { // NOI18N
                       public void run () {
                           final Class cookie = cookie ();
                           final Compiler.Depth depth = depth ();

                           TopManager.getDefault ().setStatusText (message ());

                           ArrayEnumeration aen = new ArrayEnumeration (nodes);
                           AlterEnumeration alt = new AlterEnumeration (aen) {
                                                      public Object alter (Object node) {
                                                          return ((Node)node).getCookie (cookie);
                                                      }
                                                  };
                           FilterEnumeration fen = new FilterEnumeration (alt) {
                                                       public boolean accept (Object c) {
                                                           return c != null;
                                                       }
                                                   };
                           CompilerJob job = createJob (fen, depth);

                           DataObject[] objects = new DataObject[nodes.length];
                           boolean useNodes = false;
                           for (int i = 0; i < nodes.length; i++) {
                               objects[i] = (DataObject) nodes[i].getCookie(DataObject.class);
                               if (objects[i] == null) {
                                   useNodes = true;
                                   break;
                               }
                           }

                           job.setDisplayName (useNodes ? findName (nodes) : findName(objects));
                           job.start ();
                       }
                   };
        t.setPriority (Thread.NORM_PRIORITY - 1);
        t.setDaemon (true);
        t.start ();
    }

    /** Finds the right name for the compilation of the nodes
    * @param nodes the set of nodes
    * @return the name
    */
    static String findName (Node[] nodes) {
        Object[] args = new Object[] {
                            new Integer (nodes.length),
                            nodes.length > 0 ? nodes[0].getDisplayName () : "" // NOI18N
                        };
        return MessageFormat.format (ActionConstants.BUNDLE.getString ("FMT_Compile"), args);
    }

    /** Finds the right name for the compilation of the DataObjects
    * @param dataObjects the set of DataObjects
    * @return the name
    */
    static String findName (DataObject[] dataObjects) {
        Object[] args = new Object[] {
                            new Integer (dataObjects.length),
                            dataObjects.length > 0 ? dataObjects[0].getNodeDelegate().getDisplayName() : "" // NOI18N
                        };
        return MessageFormat.format (ActionConstants.BUNDLE.getString ("FMT_Compile"), args);
    }

    /** Create a job for compilation over a set of cookies.
    * @param en enumeration of {@link CompilerCookie}
    * @param depth the requested depth
    * @return the compiler job
    */
    public static CompilerJob createJob (Enumeration en, Compiler.Depth depth) {
        CompilerJob job = new CompilerJob (depth);
        while (en.hasMoreElements ()) {
            CompilerCookie cc = (CompilerCookie)en.nextElement ();
            cc.addToJob (job, depth);
        }
        return job;
    }

    /** Compile a number of files.
    * Should actually be files, not directories (i.e. a zero depth will be used).
    * @param compileCookies enumeration of {@link CompilerCookie}
    * @param name name of the job to use
    * @return <code>true</code> if compilation was successful, <code>false</code> if there was some sort of error
    */
    public static boolean compile(Enumeration compileCookies, String name) {
        CompilerJob job = createJob(compileCookies, Compiler.DEPTH_ZERO);
        job.setDisplayName(name);
        if (! job.isUpToDate()) {
            CompilerTask task = job.start();
            return task.isSuccessful();
        } else {
            return true;
        }
    }

}

/*
 * Log
 *  19   Gandalf   1.18        1/12/00  Ian Formanek    NOI18N
 *  18   Gandalf   1.17        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  17   Gandalf   1.16        10/5/99  Jaroslav Tulach Looking for objects to 
 *       compile/build/clean/etc.
 *  16   Gandalf   1.15        8/11/99  Jaroslav Tulach Disabled when no nodes 
 *       selected.
 *  15   Gandalf   1.14        8/5/99   Jaroslav Tulach Searches for the objects
 *       in separate thread.
 *  14   Gandalf   1.13        8/2/99   Petr Hamernik   fixed bug #3001
 *  13   Gandalf   1.12        7/29/99  Jaroslav Tulach Checks supported depth.
 *  12   Gandalf   1.11        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  11   Gandalf   1.10        5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  10   Gandalf   1.9         5/15/99  Jesse Glick     [JavaDoc]
 *  9    Gandalf   1.8         5/15/99  Ales Novak      prev revision changed to
 *       enumerations
 *  8    Gandalf   1.7         5/14/99  Ales Novak      bugfix for #1667 #1598 
 *       #1625
 *  7    Gandalf   1.6         4/2/99   Jaroslav Tulach Compiles before 
 *       execution.
 *  6    Gandalf   1.5         3/27/99  Jaroslav Tulach 
 *  5    Gandalf   1.4         3/26/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         1/6/99   Jaroslav Tulach Change of package of 
 *       DataObject
 *  3    Gandalf   1.2         1/6/99   Ales Novak      
 *  2    Gandalf   1.1         1/6/99   Ales Novak      
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
