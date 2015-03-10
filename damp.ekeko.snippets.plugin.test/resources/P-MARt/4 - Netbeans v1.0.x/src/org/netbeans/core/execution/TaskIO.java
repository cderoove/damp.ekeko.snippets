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

package org.netbeans.core.execution;

import java.io.InputStream;
import java.io.PrintStream;

import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/** simply contains all ins n' outs for running task
* There is one instance for every running task.
*
* @author Ales Novak
* @version 0.11 April 24, 1998
*/
class TaskIO {

    /** No name */
    static final String VOID = "noname"; // NOI18N

    /** stdout for task */
    PrintStream out;
    /** stderr */
    PrintStream err;
    /** stdin */
    InputStream in;

    /** 'theme' for this task */
    InputOutput inout;

    /** name for the TaskIO */
    private String name;

    /** Should not be this TaskIO processed by IOTable? */
    boolean foreign;

    /** Null constant */
    public static final TaskIO Null = new TaskIO();

    static {
        Null.in = new org.openide.util.io.NullInputStream();
        Null.out = Null.err = new java.io.PrintStream(new org.openide.util.io.NullOutputStream());
    }

    TaskIO () {
        name = VOID;
    }

    /**
    * @param inout is an InputOutput
    * @param name is a name
    */
    TaskIO (InputOutput inout) {
        this(inout, VOID);
    }

    /**
    * @param inout is an InputOutput
    * @param name is a name
    */
    TaskIO (InputOutput inout, String name) {
        this.inout = inout;
        this.name = name;
    }

    /**
    * @param inout is an InputOutput
    * @param name is a name
    * @param foreign if true then IOTable never cares about this TaskIO
    */
    TaskIO (InputOutput inout, String name, boolean foreign) {
        this.inout = inout;
        this.name = name;
        this.foreign = foreign;
    }

    /** inits out */
    void initOut () {
        if (out == null)
            out = new SysPrintStream (inout.getOut ());
    }

    /** inits err */
    void initErr () {
        if (err == null)
            err = new SysPrintStream (inout.getErr ());
    }

    /** inits in */
    void initIn () {
        if (in == null)
            in = new SysInStream (inout.getIn ());
    }

    /**
    * @return name
    */
    String getName() {
        return name;
    }

    /**
    * @return InputOutput for this TaskIO
    */
    InputOutput getInout() {
        return inout;
    }

    /** Null InputOutput */
    static final class NullIO implements InputOutput {
        public OutputWriter getOut() {
            throw new UnsupportedOperationException();
        }
        public java.io.Reader getIn() {
            throw new UnsupportedOperationException();
        }
        public OutputWriter getErr() {
            throw new UnsupportedOperationException();
        }
        public void closeInputOutput() {
            throw new UnsupportedOperationException();
        }
        public boolean isClosed() {
            throw new UnsupportedOperationException();
        }
        public void setOutputVisible(boolean value) {
            throw new UnsupportedOperationException();
        }
        public void setErrVisible(boolean value) {
            throw new UnsupportedOperationException();
        }
        public void setInputVisible(boolean value) {
            throw new UnsupportedOperationException();
        }
        public void select () {
            throw new UnsupportedOperationException();
        }
        public boolean isErrSeparated() {
            throw new UnsupportedOperationException();
        }
        public void setErrSeparated(boolean value) {
            throw new UnsupportedOperationException();
        }
        public boolean isFocusTaken() {
            throw new UnsupportedOperationException();
        }
        public void setFocusTaken(boolean value) {
            throw new UnsupportedOperationException();
        }
        public void addMenu(javax.swing.JPopupMenu menu) {
            throw new UnsupportedOperationException();
        }
        public java.io.Reader flushReader() {
            throw new UnsupportedOperationException();
        }
    }

}

/*
 * Log
 *  8    Gandalf   1.7         1/12/00  Ales Novak      i18n
 *  7    Gandalf   1.6         1/11/00  Ales Novak      provided InputOutput is 
 *       not handled by execution system
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/8/99  Ales Novak      improved redirection of 
 *       IO operations
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         3/31/99  Ales Novak      
 *  2    Gandalf   1.1         3/26/99  Ales Novak      
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
