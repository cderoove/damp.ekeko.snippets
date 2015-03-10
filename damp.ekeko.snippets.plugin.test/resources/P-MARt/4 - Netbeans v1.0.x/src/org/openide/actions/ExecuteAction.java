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

import java.util.*;

import org.openide.TopManager;
import org.openide.cookies.CompilerCookie;
import org.openide.cookies.ExecCookie;
import org.openide.compiler.CompilerTask;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.Compiler;

import org.openide.execution.Executor;
import org.openide.windows.Workspace;
import org.openide.windows.WindowManager;

import org.openide.util.enum.ArrayEnumeration;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;

/** Execute a class.
* Is enabled if the only selected node implements
* {@link ExecCookie}.
* @see org.openide.execution
*
* @author   Ian Formanek, Jaroslav Tulach, Jan Jancura
*/
public class ExecuteAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1861936644244723970L;
    // static ..........................................................................................

    private static String workspace = "Running"; // NOI18N

    /** should we run compilation before execution */
    private static boolean runCompilation = true;

    /** Switches to running workspace */
    static void changeWorkspace () {
        WindowManager dp = TopManager.getDefault ().getWindowManager ();
        Workspace d = dp.findWorkspace (workspace);
        if (d != null) d.activate ();
    }

    /**
    * Get the name of the workspace in which execution is performed.
    * By default, execution is performed in the "running" workspace.
    * @return the workspace name
    */
    public static String getWorkspace () {
        return workspace;
    }

    /**
    * Set the name of the workspace in which execution is performed.
    * @param workspace the workspace name
    */
    public static void setWorkspace (String workspace) {
        ExecuteAction.workspace = workspace;
    }

    /** Set whether files should be compiled before execution.
    * @param run <code>true</code> if they should
    */
    public static void setRunCompilation (boolean run) {
        runCompilation = run;
    }

    /** Test whether files will be compiled before execution.
    * By default they will.
    * @return <code>true</code> if they will be
    */
    public static boolean getRunCompilation () {
        return runCompilation;
    }

    // init ..........................................................................................

    /* Needs exec cookie.
    */
    protected Class[] cookieClasses () {
        return new Class[] { ExecCookie.class };
    }

    /* Checks the cookies and starts them.
    */
    protected void performAction (final Node[] activatedNodes) {
        // do the compilation in different thread
        new Thread () {
            public void run () {
                execute(activatedNodes, runCompilation, true);
            }

            /* JST: This check must be done in the ExecCookie.start method

                          if (!((ExecCookie)Cookies.getInstanceOf (cookie, ExecCookie.class)).isExecAllowed()) {
                            TopManager.getDefault().notify(
                              new NotifyDescriptor.Message(
                                  java.text.MessageFormat.format (
                                      topBundle.getString ("FMT_MSG_CannotExecute"),
                                      new Object[] {
                                        node.getDisplayName ()
                                      }
                                  ),
                                  NotifyDescriptor.WARNING_MESSAGE)
                            );
                            doRun = false;
                          }
            */
        }.start ();
    }

    /* Mode any is enough.
    */
    protected int mode () {
        return MODE_ANY;
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return ActionConstants.BUNDLE.getString("Execute");
    }

    /* Help context where to find more about the acion.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (ExecuteAction.class);
    }

    protected String iconResource () {
        return "/org/openide/resources/actions/execute.gif"; // NOI18N
    }

    // utility methods

    /** Execute a list of items by cookie.
    *
    * @param execCookies list of {@link ExecCookie}s (any may be <code>null</code>)
    */
    public static void execute(Iterator execCookies) {
        while (execCookies.hasNext()) {
            ExecCookie cookie = (ExecCookie) execCookies.next();
            if (cookie != null) {
                cookie.start();
            }
        }
    }

    /** Execute some data objects.
    *
    * @param dataObjects the data objects (should have {@link ExecCookie} on them if they are to be used)
    * @param compileBefore <code>true</code> to compile before executing
    * @return true if compilation succeeded or was not performed, false if compilation failed
    */
    public static boolean execute(DataObject[] dataObjects, boolean compileBefore) {
        // search all DataObjects with unique ExecCookies/StartCookies -
        // - it is possible, that multiple activated nodes have the same exec cookie and
        // we have to prevent running it multiple times
        HashSet compile = new HashSet ();
        HashSet execute = new HashSet ();

        for (int i = 0; i < dataObjects.length; i++) {
            ExecCookie exec = (ExecCookie) dataObjects[i].getCookie(ExecCookie.class);
            if (exec != null) {
                execute.add(exec);
                if (compileBefore) {
                    CompilerCookie comp = (CompilerCookie) dataObjects[i].getCookie(CompilerCookie.Compile.class);
                    if (comp != null) {
                        compile.add(comp);
                    }
                }
            }
        }
        // compile
        if (compileBefore) {
            if (! AbstractCompileAction.compile(Collections.enumeration(compile),
                                                AbstractCompileAction.findName(dataObjects))) {
                return false;
            }
        }

        // execute
        execute(execute.iterator());
        return true;
    }

    /** Execute some nodes.
    *
    * @param nodes the nodes (should have {@link ExecCookie} on them if they are to be used)
    * @param compileBefore <code>true</code> to compile before executing
    * @return true if compilation succeeded or was not performed, false if compilation failed
    */
    public static boolean execute(Node[] nodes, boolean compileBefore) {
        return execute(nodes, compileBefore, false);
    }

    /** Execute some nodes.
    *
    * @param nodes the nodes (should have {@link ExecCookie} on them if they are to be used)
    * @param compileBefore <code>true</code> to compile before executing
    * @param switchWorkspace <code>true</code> to switch workspace before executing
    * @return true if compilation succeeded or was not performed, false if compilation failed
    */
    private static boolean execute(Node[] nodes, boolean compileBefore, boolean switchWorkspace) {
        // find all activatedNodes with unique ExecCookies/StartCookies -
        // - it is possible, that multiple activated nodes have the same exec cookie and
        // we have to prevent running it multiple times
        HashSet compile = new HashSet ();
        HashSet execute = new HashSet ();

        for (int i = 0; i < nodes.length; i++) {
            ExecCookie exec = (ExecCookie) nodes[i].getCookie(ExecCookie.class);
            if (exec != null) {
                execute.add(exec);
                if (compileBefore) {
                    CompilerCookie comp = (CompilerCookie) nodes[i].getCookie(CompilerCookie.Compile.class);
                    if (comp != null) {
                        compile.add(comp);
                    }
                }
            }
        }

        // compile
        if (compileBefore) {
            if (! AbstractCompileAction.compile(Collections.enumeration(compile),
                                                AbstractCompileAction.findName(nodes))) {
                return false;
            }
        }

        if (switchWorkspace) {
            if (java.awt.EventQueue.isDispatchThread()) {
                ExecuteAction.changeWorkspace ();
            } else {
                Runnable run = new Runnable() {
                                   public void run() {
                                       ExecuteAction.changeWorkspace ();
                                   }
                               };
                try {
                    java.awt.EventQueue.invokeAndWait(run);
                } catch (InterruptedException e) {
                    throw new ThreadDeath();
                } catch (java.lang.reflect.InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    if (t instanceof Error) {
                        throw (Error) t;
                    } else if (t instanceof RuntimeException) {
                        throw (RuntimeException) t;
                    } else { // cannot happen
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                            t.printStackTrace();
                        }
                    }
                }
            }
        }
        // execute
        execute(execute.iterator());
        return true;
    }
}

/*
 * Log
 *  27   Gandalf-post-FCS1.25.1.0    3/15/00  David Simonek   japanese localization 
 *       now works correctly
 *  26   Gandalf   1.25        1/12/00  Ian Formanek    NOI18N
 *  25   Gandalf   1.24        12/15/99 Ales Novak      #4915 again
 *  24   Gandalf   1.23        12/14/99 Ales Novak      #4915
 *  23   Gandalf   1.22        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  22   Gandalf   1.21        7/26/99  Ian Formanek    finetuned behavior when 
 *       compilation is not requested before execution
 *  21   Gandalf   1.20        7/21/99  Ales Novak      deadlock
 *  20   Gandalf   1.19        7/11/99  David Simonek   window system change...
 *  19   Gandalf   1.18        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  18   Gandalf   1.17        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  17   Gandalf   1.16        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  16   Gandalf   1.15        6/8/99   Ales Novak      #1780
 *  15   Gandalf   1.14        5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  14   Gandalf   1.13        5/28/99  Ian Formanek    Fixed bug 1692 - When an
 *       out-of-date class is executed, the compilation is run and it fails, the
 *       workspace is switched to "Running" anyway.
 *  13   Gandalf   1.12        5/15/99  Jesse Glick     [JavaDoc]
 *  12   Gandalf   1.11        5/15/99  Ales Novak      prev revision changed to
 *       enumerations
 *  11   Gandalf   1.10        5/14/99  Ales Novak      bugfix for #1667 #1598 
 *       #1625
 *  10   Gandalf   1.9         5/2/99   Ian Formanek    Fixed last change
 *  9    Gandalf   1.8         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  8    Gandalf   1.7         4/2/99   Jaroslav Tulach Compiles before 
 *       execution.
 *  7    Gandalf   1.6         3/26/99  Jesse Glick     [JavaDoc]
 *  6    Gandalf   1.5         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  5    Gandalf   1.4         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  4    Gandalf   1.3         1/20/99  Ales Novak      
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    fixed resource names
 *  2    Gandalf   1.1         1/6/99   Ales Novak      
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
