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

package org.openide.execution;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ResourceBundle;
import java.io.IOException;

import org.openide.TopManager;
import org.openide.execution.*;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;
import org.openide.util.io.FoldingIOException;

/** Executes a class in a thread in the current VM.
*
* @author Ales Novak
*/
public class ThreadExecutor extends Executor {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7160546092135474445L;

    // the bundle to use
    static ResourceBundle bundle;

    /** Create a new thread executor. */
    public ThreadExecutor() {
    }

    /*
    * @param ctx @see ExecutionEngine.Context
    * @param info an ExecInfo instance describing executed class
    */
    public ExecutorTask execute(ExecInfo info) throws IOException {
        TERunnable run = new TERunnable(info);
        ExecutorTask ret;
        InputOutput inout = (needsIO() ? null : InputOutput.NULL);

        synchronized (run) {
            ret = TopManager.getDefault().getExecutionEngine().execute(info.getClassName(), run, inout);
            run.setInputOutput(ret.getInputOutput());
            try {
                run.wait();  // wait for arbitrary exceptions during executing run
                Throwable t = run.getException();
                if (t != null) {
                    if (! (t instanceof ThreadDeath)) {
                        if (t instanceof RuntimeException) {
                            throw (RuntimeException) t;
                        } else if (t instanceof Error) {
                            throw (Error) t;
                        } else if (t instanceof IOException) {
                            throw (IOException) t;
                        } else {
                            throw new FoldingIOException(t);
                        }
                    }
                }
            } catch (InterruptedException e) {
                throw new FoldingIOException(e);
            }
        }
        return ret;
    }

    /** notifies about exception
    * @param t is a Throwable
    */
    private static void notifyException(final Throwable t) {
        //javax.swing.SwingUtilities.invokeLater(new Runnable() {
        // public void run() {
        TopManager.getDefault().notifyException(t);
        //  }
        // });
    }

    /* @return name */
    protected String displayName() {
        return getString("InternalExecution");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ThreadExecutor.class);
    }

    /** Subclasses of the executor can override this method
    * to check loaded class before its main method is invoked.
    *
    * @param clazz
    * @exception IOException
    */
    protected void checkClass(Class clazz) throws IOException {
        // find main (String[])
        final java.lang.reflect.Method method;
        try {
            method = clazz.getDeclaredMethod("main", new Class[] { String[].class }); // NOI18N
        } catch (NoSuchMethodException e) {
            if (e.getLocalizedMessage() == null) {
                e = new NoSuchMethodException(getString("EXC_NoSuchMethodException"));
            }
            throw new FoldingIOException(e);
        }

        if (! java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("java.lang.reflect.Modifier.isStatic(" + method + ") == false"); // NOI18N
        }
        if (! java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
            throw new IllegalArgumentException("java.lang.reflect.Modifier.isPublic(" + method + ") == false"); // NOI18N
        }
        if (method.getReturnType() != Void.TYPE) {
            throw new IllegalArgumentException(method + ".getReturnType() != Void.TYPE"); // NOI18N
        }
    }

    /** Invokes main method of the class with given parameters.
    *
    * @param clazz
    * @param params
    */
    protected void executeClass(Class clazz, String[] params) {
        try {
            final java.lang.reflect.Method method = clazz.getDeclaredMethod("main", new Class[] { params.getClass () }); // NOI18N
            method.setAccessible(true); // needs a permission
            method.invoke (null, new Object[] { params });
        } catch (java.lang.reflect.InvocationTargetException ex) {
            if (! (ex.getTargetException() instanceof ThreadDeath))
                ex.getTargetException().printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();  // is redirected since executed under EE
        }
    }



    /** @return localized string */
    static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(ThreadExecutor.class);
        }
        return  bundle.getString(s);
    }

    /* ThreadExecutor runnable
    * Its run method loads needed class, notifies waiting thread and executes main method of the class.
    */
    private class TERunnable implements Runnable {

        private Throwable exception;
        private ExecInfo info;
        private InputOutput io;

        TERunnable(ExecInfo info) {
            this.info = info;
        }

        public synchronized void run() {
            String className = info.getClassName();
            final String[] params  = info.getArguments();
            Class clazz = null;
            try {
                NbClassLoader loader = new NbClassLoader(io);
                clazz = loader.loadClass(className);

                if (clazz == null) {
                    throw new IOException(); // [PENDING]
                }

                if (clazz.getClassLoader() != loader) {
                    TopManager.getDefault().setStatusText(clazz + getString("MSG_Inv_CLoader") + clazz.getClassLoader());
                }

                checkClass(clazz);

            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable e) {
                exception = e;
                return;
            } finally {
                notifyAll();
            }

            executeClass(clazz, params);
        }

        public Throwable getException() {
            return exception;
        }
        public void setInputOutput(InputOutput io) {
            this.io = io;
        }
    }
}

/*
 * Log
 *  18   Gandalf   1.17        1/17/00  Ales Novak      #5399
 *  17   Gandalf   1.16        1/12/00  Ian Formanek    NOI18N
 *  16   Gandalf   1.15        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   Gandalf   1.14        10/7/99  Jesse Glick     displayName should be 
 *       protected, not public.
 *  14   Gandalf   1.13        10/5/99  Ales Novak      exec is divided into two
 *       phases
 *  13   Gandalf   1.12        10/1/99  Ales Novak      major change of 
 *       execution
 *  12   Gandalf   1.11        8/6/99   Ales Novak      invalid classloader msg
 *  11   Gandalf   1.10        7/2/99   Jesse Glick     Help IDs for debugger & 
 *       executor types.
 *  10   Gandalf   1.9         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         4/8/99   Ales Novak      
 *  8    Gandalf   1.7         4/6/99   Ales Novak      
 *  7    Gandalf   1.6         3/31/99  Ales Novak      
 *  6    Gandalf   1.5         3/26/99  Jaroslav Tulach 
 *  5    Gandalf   1.4         3/23/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         3/23/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         3/4/99   Ales Novak      
 *  2    Gandalf   1.1         1/14/99  Ales Novak      
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
