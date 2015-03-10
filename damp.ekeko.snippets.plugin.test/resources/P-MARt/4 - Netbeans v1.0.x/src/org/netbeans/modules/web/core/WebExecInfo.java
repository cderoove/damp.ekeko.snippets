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

package org.netbeans.modules.web.core;

import org.openide.execution.ExecInfo;
import org.openide.loaders.DataObject;

/** ExecInfo for JSPs and HTML.
*
* @author Petr Jiricka
*/
public class WebExecInfo extends ExecInfo {

    protected DataObject theDataObject;

    public WebExecInfo(java.lang.String className, java.lang.String[] argv, DataObject theDataObject) {
        super(className, argv);
        this.theDataObject = theDataObject;
    }

    public WebExecInfo(java.lang.String className, DataObject theDataObject) {
        super(className);
        this.theDataObject = theDataObject;
    }

    public DataObject getDataObject() {
        return theDataObject;
    }

}

/*
 * Log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         10/8/99  Petr Jiricka    
 * $
 */
