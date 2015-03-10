/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi;

import java.io.IOException;

import org.openide.loaders.*;
import org.openide.execution.*;
import org.openide.util.Mutex;
import org.openide.debugger.DebuggerType;

/**
 *
 * @author  mryzl
 */

public class RMIExecSupport extends org.openide.loaders.ExecSupport {

    /** Creates new RMIExecSupport. */
    public RMIExecSupport(MultiDataObject.Entry entry) {
        super(entry);
    }

    public void start() {
        final MultiDataObject.Entry entry = this.entry;
        new Thread() {
            public void run() {
                Executor exec = getExecutor (entry);
                if (exec == null) {
                    exec = defaultExecutor ();
                }
                String[] params = getArguments ();

                String classname = entry.getFile ().getPackageName ('.');
                String service = getService();
                if (service == null) service = classname;
                RMIExecInfo info = new RMIExecInfo(classname, params, getPort(), service);

                try {
                    //    exec.execute (entry.getFile ().getPackageName ('.'), params);
                    exec.execute(info);
                } catch (final java.io.IOException ex) {
                    Mutex.EVENT.readAccess (new Runnable () {
                                                public void run () {
                                                    if (startFailed (ex)) {
                                                        // restart
                                                        RMIExecSupport.this.start ();
                                                    }
                                                }
                                            });
                }
            }
        }.start();
    }

    /** Get port.
    * @return port number
    */
    public int getPort() {
        try {
            Integer port = (Integer)entry.getFile ().getAttribute (RMIDataObject.EA_PORT);
            if (port != null) {
                return port.intValue();
            }
        } catch (Exception ex) {
            // null pointer or IOException
        }
        return 0;
    }

    /** Return service name.
    */
    public String getService() {
        try {
            String service = (String)entry.getFile ().getAttribute (RMIDataObject.EA_SERVICE);
            if (service != null) {
                return service;
            }
        } catch (Exception ex) {
            // null pointer or IOException
        }
        return null;
    }

    /** Called when invocation of the executor fails. Allows to do some
    * modifications to the type of execution and try it again.
    *
    * @param ex exeception that occured during execution
    * @return true if the execution should be restarted
    */
    protected boolean startFailed (IOException ex) {
        return super.startFailed(ex);
    }

    /** This method allows subclasses to override the default
    * executor they want to use for debugging.
    *
    * @return current implementation returns Executor.getDefault ()
    */
    protected Executor defaultExecutor () {
        return Executor.find(RMIExecutor.class);
    }

    /** This method allows subclasses to override the default
     * debugger type they want to use for debugging.
     *
     * @return current implementation returns DebuggerType.getDefault ()
     */
    protected DebuggerType defaultDebuggerType () {
        return DebuggerType.find(RMIDebugType.class);
    }
}

/*
* <<Log>>
*  5    Gandalf   1.4         11/10/99 Martin Ryzl     some bugfixes
*  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         9/10/99  Jaroslav Tulach Changes to services.
*  2    Gandalf   1.1         8/16/99  Martin Ryzl     method filter in RMI 
*       Encapsulation Wizard  service URL in RMIDataObject
*  1    Gandalf   1.0         7/12/99  Martin Ryzl     
* $ 
*/ 
