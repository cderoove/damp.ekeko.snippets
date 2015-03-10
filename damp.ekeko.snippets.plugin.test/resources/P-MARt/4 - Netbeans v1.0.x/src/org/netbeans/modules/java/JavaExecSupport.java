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

import org.openide.loaders.ExecSupport;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.debugger.DebuggerType;
import org.openide.execution.Executor;
import org.netbeans.modules.java.settings.JavaSettings;

/** Support for execution of a class file. Looks for the class with
* the same base name as the primary file, locates a main method
* in it, and starts it.
*
*/
final class JavaExecSupport extends ExecSupport {

    /** settings */
    static JavaSettings settings;

    /** new JavaExecSupport */
    public JavaExecSupport(Entry entry) {
        super(entry);
    }

    /** This method allows subclasses to override the default
    * debugger type they want to use for debugging.
    *
    * @return current implementation returns DebuggerType.getDefault ()
    */
    protected DebuggerType defaultDebuggerType () {
        return getSettings().getDebugger();
    }

    /** This method allows subclasses to override the default
    * executor they want to use for debugging.
    *
    * @return current implementation returns Executor.getDefault ()
    */
    protected Executor defaultExecutor () {
        return getSettings().getExecutor();
    }

    /** @return JavaSettings */
    static JavaSettings getSettings() {
        if (settings == null) {
            settings = (JavaSettings) JavaSettings.findObject(JavaSettings.class, true);
        }
        return settings;
    }
}

/*
* Log
*  1    Gandalf   1.0         12/20/99 Ales Novak      
* $
*/
