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

package org.netbeans.modules.web.core.jswdk;

import java.io.IOException;

import org.openide.util.NbBundle;
import org.openide.execution.ExecInfo;
import org.openide.execution.ExecutorTask;

/** Executes a class externally (in a separate process). Provides
* basic implementation that allows to specify the process to 
* execute, its parameters and also to substitute the content of repositorypath,
* classpath, bootclasspath and librarypath. This is done by inner class Format.
* <P>
* The behaviour described here can be overriden by subclasses to use different
* format (extend the set of recognized tags), execute the 
* process with additional environment properties, etc.
*
* @author Petr Jiricka
*/
public class JspExecutor extends ServletJspExecutor {

    /** serialVersionUID */
    private static final long serialVersionUID=6320088611750274178L;

    public JspExecutor() {
        super();
    }

    /* Default human-presentable name of the executor.
    * In the default implementation, just the class name.
    * @return initial value of the human-presentable name
    */
    public String displayName() {
        return NbBundle.getBundle(JspExecutor.class).getString("CTL_JspExec_Name");
    }

    public ExecutorTask execute(ExecInfo info) throws IOException {
        if (EditServletParamsAction.isJsp(info))
            return super.execute(info);

        throw new IOException(NbBundle.getBundle(JspExecutor.class).getString("EXC_NeedsJSP"));
    }


}

/*
 * Log
 *  2    Gandalf   1.1         1/16/00  Petr Jiricka    Added check that we are 
 *       executing a JSP page
 *  1    Gandalf   1.0         12/21/99 Petr Jiricka    
 * $
 */
