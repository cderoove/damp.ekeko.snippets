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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

import org.openide.TopManager;
import org.openide.execution.*;
import org.openide.filesystems.*;
import org.openide.util.io.FoldingIOException;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/** For external compilation - runs external compiler through std
* execution interface.
*
* @author Ales Novak
*/
final class CompilerExecutor extends ProcessExecutor {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2611259508166125914L;

    /** files to compile (String) 
     * @associates String*/
    private List files;

    /** ExternalCompiler reference */
    private ExternalCompilerGroup eCompiler;
    /** description of external compiler - it is the "class" of this compiler*/ // NOI18N
    private NbProcessDescriptor nbDescriptor;
    /** ErrorExpression */
    private ExternalCompiler.ErrorExpression errorExpression;
    /** type of compiler */
    private Object compilerType;

    /**
    * @param ec is a reference to external compiler
    * @param filePath is compiled file
    */
    CompilerExecutor(
        ExternalCompilerGroup ecg,
        NbProcessDescriptor nbDescriptor,
        ExternalCompiler.ErrorExpression err,
        Object compilerType
    ) {
        files = new LinkedList ();
        this.eCompiler = ecg;
        this.nbDescriptor = nbDescriptor;
        this.errorExpression = err;
        this.compilerType = compilerType;
    }

    /** add a new file to files */
    public void addFile(String file) {
        files.add(file);
    }

    /** @return name */
    public String displayName() {
        return ExternalCompiler.getLocalizedString("LAB_ExternalExecution"); // NOI18N
    }

    /** Executes a compilation
    * @param ctx is ignored
    * @param info is ignored
    */
    public ExecutorTask execute(ExecInfo info) throws IOException {
        CERunnable run = new CERunnable(info, eCompiler, errorExpression, nbDescriptor, files, compilerType);
        synchronized (run) {
            ExecutorTask et = TopManager.getDefault().getExecutionEngine().execute(null, run, InputOutput.NULL);

            try {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                run.wait(); // let the runnable be executed
                Throwable e = run.getException();
                if (e != null) {
                    if (e instanceof Error) {
                        throw (Error) e;
                    } else if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else {
                        throw new FoldingIOException(e);
                    }
                }
                CompilerSysProcess csp = run.getCompilerSysProcess();
                csp.setExecutorTask(et);
                return csp;
            } catch (InterruptedException e) {
                return new CompilerSysProcess.InterruptedProcess();
            }
        }
    }

    /** Getter for error expresions.
    */
    ExternalCompiler.ErrorExpression getErrorExpression () {
        return errorExpression;
    }

    /** @return descriptor
    */
    NbProcessDescriptor getDescriptor () {
        return nbDescriptor;
    }

    /** Instance are executed in ExecutionEngine */
    static class CERunnable implements Runnable {

        private ExecInfo info;
        private ExternalCompilerGroup eeg;
        private ExternalCompiler.ErrorExpression errorExpression;
        private NbProcessDescriptor nbDescriptor;
        private Throwable t;
        private CompilerSysProcess csp;
        private List files;
        private Object compilerType;

        CERunnable(ExecInfo info, ExternalCompilerGroup eeg, ExternalCompiler.ErrorExpression errorExpression, NbProcessDescriptor nbDescriptor, List files, Object compilerType) {
            this.info = info;
            this.eeg = eeg;
            this.errorExpression = errorExpression;
            this.nbDescriptor = nbDescriptor;
            this.files = files;
            this.compilerType = compilerType;
        }

        public void run() {
            try {
                // Save all modified files
                TopManager.getDefault().saveAll();
                String msg = eeg.getStatusLineText();

                TopManager.getDefault().setStatusText(msg);

                csp = new CompilerSysProcess(
                          this,
                          eeg,
                          getClassPathEntries(),
                          eeg.createProcess(nbDescriptor,(String[]) files.toArray(new String[0]), compilerType),
                          errorExpression
                      );
            } catch (Throwable tt) {
                t = tt;
            } finally {
                synchronized (this) {
                    notifyAll();
                }
            }
        }

        /** @return items from filesystems which take part in compilation */
        static String getClassPathEntries() {
            String repPath = NbClassPath.createRepositoryPath(FileSystemCapability.COMPILE).getClassPath();
            if (repPath.charAt(0) == '"') {
                repPath = repPath.substring(1, repPath.length() - 1);
            }
            return repPath;
        }


        public Throwable getException() {
            return t;
        }
        public CompilerSysProcess getCompilerSysProcess() {
            return csp;
        }
    }
}


/*
 * Log
 *  23   src-jtulach1.22        2/4/00   Ales Novak      #5556
 *  22   src-jtulach1.21        1/14/00  Ales Novak      i18n
 *  21   src-jtulach1.20        1/12/00  Ian Formanek    NOI18N
 *  20   src-jtulach1.19        1/10/00  Ales Novak      stopCompile action
 *  19   src-jtulach1.18        12/15/99 Ales Novak      Comiling message added
 *  18   src-jtulach1.17        11/1/99  Ales Novak      external compilation is 
 *       not shown in exec window
 *  17   src-jtulach1.16        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   src-jtulach1.15        10/1/99  Ales Novak      major change of 
 *       execution
 *  15   src-jtulach1.14        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  14   src-jtulach1.13        6/7/99   Jaroslav Tulach FS capabilities.
 *  13   src-jtulach1.12        6/2/99   Jaroslav Tulach createProcess receives 
 *       also compiler type.
 *  12   src-jtulach1.11        6/2/99   Jaroslav Tulach ExternalCompiler has 
 *       method for specifying its type.
 *  11   src-jtulach1.10        5/31/99  Jaroslav Tulach External Execution & 
 *       Compilation
 *  10   src-jtulach1.9         5/17/99  Ales Novak      bugfix #1773
 *  9    src-jtulach1.8         5/15/99  Ales Novak      bugfix #1798
 *  8    src-jtulach1.7         5/12/99  Ales Novak      StringBuffer constructor
 *       changed
 *  7    src-jtulach1.6         5/7/99   Ales Novak      getAllLibraries moved to
 *       CompilationEngine
 *  6    src-jtulach1.5         4/22/99  Ales Novak      patches
 *  5    src-jtulach1.4         4/21/99  Ales Novak      system.out.println fixed
 *  4    src-jtulach1.3         4/21/99  Ales Novak      commandline parsing + 
 *       no_classpath
 *  3    src-jtulach1.2         4/2/99   Ales Novak      
 *  2    src-jtulach1.1         3/31/99  Ales Novak      
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 */



