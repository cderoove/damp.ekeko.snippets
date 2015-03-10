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

package org.netbeans.core.actions;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.netbeans.core.compiler.CompilationEngineImpl;

/** the action causes the compilation to stop
* @author Ales Novak
*/
public final class StopCompileAction extends CallableSystemAction {
    /** serialver */
    static final long serialVersionUID = -6929979383975920370L;
    /** engine */
    private static CompilationEngineImpl engine;

    /** performs action */
    public void performAction() {
        getEngine().stop();
    }

    /** Manages enable / disable logic of this action */
    public boolean isEnabled() {
        return getEngine().isCompiling();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (StopCompileAction.class);
    }

    public String getName() {
        return NbBundle.getBundle(StopCompileAction.class).getString("StopCompileAction");
    }

    /** Getter for resource string of icon of this action
    * @return Icon resource string
    */
    protected String iconResource () {
        return "/org/netbeans/core/resources/actions/stopCompilation.gif"; // NOI18N
    }

    /** Convenience method - returns compilation engine */
    static CompilationEngineImpl getEngine () {
        if (engine == null)
            engine = (CompilationEngineImpl)TopManager.getDefault().
                     getCompilationEngine();
        return engine;
    }

}

/*
 * Log
 *  10   Gandalf   1.9         1/12/00  Ales Novak      i18n
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  7    Gandalf   1.6         6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         5/17/99  Ales Novak      bugfix #1773
 *  4    Gandalf   1.3         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  3    Gandalf   1.2         3/3/99   David Simonek   
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
