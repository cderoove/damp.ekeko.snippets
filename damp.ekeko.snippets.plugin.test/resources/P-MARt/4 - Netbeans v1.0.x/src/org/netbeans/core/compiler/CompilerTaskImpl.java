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

package org.netbeans.core.compiler;
/*
* Class.java -- synopsis.
*
*
* Date
* Revision
*
* SUN PROPRIETARY/CONFIDENTIAL:  INTERNAL USE ONLY.
*
* Copyright © 1997-1999 Sun Microsystems, Inc. All rights reserved.
* Use is subject to license terms.
*/

import org.openide.compiler.CompilerTask;
import org.openide.compiler.CompilerJob;

/**
*
* @author Ales Novak
* @version 1.0, November 12, 1998
*/
class CompilerTaskImpl extends CompilerTask {
    private static final Runnable NONE = new Runnable () {
                                             public void run () {
                                             }
                                         };

    boolean success;
    private CompilerJob job;
    private CompilationEngineImpl eng;
    Object[] ref;

    public CompilerTaskImpl(CompilerJob job, CompilationEngineImpl eng, Object[] ref) {
        super(NONE);
        this.job = job;
        this.eng = eng;
        this.ref = ref;
    }
    /** makes notifyFinished public */
    void done() {
        notifyFinished();
    }
    /** stops this task */
    public void stop() {
        eng.stopTask(this);
    }
    /** @return true if success */
    public boolean isSuccessful() {
        waitFinished();
        return success;
    }

    /** Returns display name of the compiler job */
    String getDisplayName () {
        return job.getDisplayName();
    }

}

/*
 * Log
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         7/30/99  Jaroslav Tulach getOriginal & getCurrent
 *       in LineSet
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         4/28/99  Ales Novak      fixed changes from Task
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
