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

import java.util.Map;

/**
 *
 * @author  mryzl
 */

public class RMIExecInfo extends org.openide.execution.ExecInfo {



    protected int port = 0;
    protected String service = null;

    /** Creates new RMIExecInfo. */
    public RMIExecInfo(String className) {
        super(className);
    }

    public RMIExecInfo(String className, String[] argv) {
        super(className, argv);
    }

    public RMIExecInfo(String className, String[] argv, int port, String service) {
        super(className, argv);
        this.port = port;
        this.service = service;
    }

    public int getPort() {
        return port;
    }

    public String getService() {
        return service;
    }

    /** Add settings to the map.
    * @param map map
    * @return map with new settings (the same instance)
    */
    public Map addSettings(Map map) {
        map.put(RMIExecutorSettings.TAG_EXPORT_PORT, new Integer(getPort()).toString());
        map.put(RMIExecutorSettings.TAG_EXPORT_SERVICE, getService());
        return map;
    }
}

/*
* <<Log>>
*  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         8/17/99  Martin Ryzl     debugger support
*  2    Gandalf   1.1         8/16/99  Martin Ryzl     
*  1    Gandalf   1.0         7/12/99  Martin Ryzl     
* $ 
*/ 
