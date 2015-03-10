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

package org.netbeans.modules.java;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.StringWriter;
import java.io.PrintWriter;

import org.openide.compiler.CompilerGroup;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.CompilerListener;
import org.openide.compiler.ProgressEvent;
import org.openide.compiler.ErrorEvent;
import org.openide.compiler.Compiler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;

/** CompilerGroup for CleanCompiler.
*
* @author Ales Novak
*/
public class CleanCompilerGroup extends CompilerGroup {

    /** a set of Compilers 
     * @associates Compiler*/
    protected final HashSet compilers;

    /** new CompilerGroup */
    public CleanCompilerGroup() {
        compilers = new HashSet(11);
    }


    /** inherited */
    public void add(Compiler compiler) {
        if (! (compiler instanceof CleanCompiler)) {
            throw new IllegalArgumentException();
        }
        synchronized (compilers) {
            compilers.add(compiler);
        }
    }

    /** inherited */
    public boolean start() { // scalabilty ensured by sharing CoronaEnvironment for all JavaCompilers
        synchronized (compilers) {
            final boolean[] status = new boolean[1];
            status[0] = true;
            final Iterator iter = compilers.iterator();

            //fill jc
            if (iter.hasNext()) {
                final CleanCompiler jco = ((CleanCompiler) iter.next());
                FileSystem fsys = getFS(jco.jdo);

                FileSystem.AtomicAction run = new FileSystem.AtomicAction() {
                                                  public void run() {
                                                      CleanCompiler jc = jco;
                                                      try {
                                                          for (;;) {
                                                              jc.group = CleanCompilerGroup.this;
                                                              status[0] &= jc.compile();
                                                              jc.group = null;

                                                              if (iter.hasNext() && status[0]) {
                                                                  jc = ((CleanCompiler) iter.next());
                                                              } else {
                                                                  break;
                                                              }
                                                          }
                                                          compilers.clear();
                                                      } catch (ThreadDeath err) {
                                                          throw err;
                                                      } catch (Error ee) {
                                                          if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                                              printThrowable(ee);
                                                          }
                                                          status[0] = false;
                                                      } catch (Exception ee) {
                                                          if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                                              printThrowable(ee);
                                                          }
                                                          status[0] = false;
                                                      }
                                                  } // run method
                                              }; // atomic action
                try {
                    fsys.runAtomicAction(run);
                } catch (java.io.IOException e) {
                    printThrowable(e);
                    return false;
                }
            }
            return status[0];
        }
    }

    /** @return FileSystem of the jdo */
    private FileSystem getFS(JavaDataObject jdo) {
        try {
            return jdo.getPrimaryFile().getFileSystem();
        } catch (FileStateInvalidException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
            return null;
        }
    }

    /** prints the Throwable into OutputWindow */
    protected final void printThrowable(Throwable t) {
        StringWriter swriter = new StringWriter();
        PrintWriter pw = new PrintWriter(swriter);
        t.printStackTrace(pw);
        pw.close();
        ErrorEvent ev = new ErrorEvent(this, null, -1, -1, swriter.toString(), null);
        fireErrorEvent(ev);
    }

    void ireProgressEvent(ProgressEvent ev) {
        fireProgressEvent(ev);
    }
    void ireErrorEvent(ErrorEvent ev) {
        fireErrorEvent(ev);
    }
}

/*
 * Log
 *  3    Gandalf   1.2         1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  2    Gandalf   1.1         1/10/00  Ales Novak      new compiler API 
 *       deployed
 *  1    Gandalf   1.0         11/30/99 Ales Novak      
 * $
 */
