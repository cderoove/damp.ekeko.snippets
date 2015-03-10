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

import org.openide.execution.ExecutorTask;
import org.openide.windows.InputOutput;

/** SysProcess class for external compiler executor (CompilerExecutor).
*
* @author Ales Novak
*/
class CompilerSysProcess extends ExecutorTask {

    static final int INTERRUPTED = 97943;

    /** reference to external prcess */
    private Process process;

    /** reference to parsing thread - err */
    private ErrorsParsingThread thr1;

    /** reference to parsing thread - std */
    private ErrorsParsingThread thr2;

    /** Foreign ExecutorTask */
    private ExecutorTask him;

    /**
    * @param eCompiler
    * @param classPath
    * @param proc is a Process obtained from Runtime.exec call
    */
    CompilerSysProcess(Runnable run, ExternalCompilerGroup eCompile, String classPath, Process proc, ExternalCompiler.ErrorExpression err) {
        super(run);
        process = proc;
        thr1 = new ErrorsParsingThread(proc.getErrorStream(), eCompile, classPath, err, null);
        thr1.start();
        thr2 = new ErrorsParsingThread(proc.getInputStream(), eCompile, classPath, err, thr1);
        thr2.start();
        Thread.yield();
    }

    /** destroys the process */
    public void stop() {
        process.destroy();
        thr1.stopParsing(true);
        thr2.stopParsing(true);
        him.stop();
    }

    /** waits for the end of external compiler */
    public int result() {
        try {
            int ret = process.waitFor();
            return ret;
        } catch (InterruptedException e) {
            process.destroy();
            return INTERRUPTED;
        } finally {
            thr1.stopParsing(false);
            thr2.stopParsing(false);
            notifyFinished();
        }
    }

    public String getName() {
        return ""; // NOI18N
    }

    public InputOutput getInputOutput() {
        return him.getInputOutput();
    }

    public void setExecutorTask(ExecutorTask et) {
        him = et;
    }

    public void run() {
    }

    static class InterruptedProcess extends ExecutorTask {
        InterruptedProcess() {
            super(null);
        }

        public void stop() {
        }

        public int result() {
            return INTERRUPTED;
        }

        public InputOutput getInputOutput() {
            return InputOutput.NULL;
        }
    }
}

/*
 * Log
 *  7    src-jtulach1.6         1/12/00  Ian Formanek    NOI18N
 *  6    src-jtulach1.5         1/10/00  Ales Novak      stopCompile action
 *  5    src-jtulach1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    src-jtulach1.3         10/1/99  Ales Novak      major change of 
 *       execution
 *  3    src-jtulach1.2         9/14/99  Ales Novak      upgrade to 
 *       org.netbeans.lib.regexp.*
 *  2    src-jtulach1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 */
