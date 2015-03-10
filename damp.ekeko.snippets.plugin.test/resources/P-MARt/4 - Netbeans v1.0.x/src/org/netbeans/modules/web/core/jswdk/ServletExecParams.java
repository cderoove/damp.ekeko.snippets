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

import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.io.Serializable;
import javax.swing.table.DefaultTableModel;

import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/** Servlet execution parameters
* 
* @author  Petr Jiricka
* @version 1.00, Jun 03, 1999
*/
public class ServletExecParams implements Serializable {

    public static final int HTTP_GET  = 1;
    public static final int HTTP_POST = 2;
    public static final int HTTP_HEAD = 3;


    private Vector initParams = new Vector();

    private String name = ""; // NOI18N

    private String mapping = ""; // NOI18N

    private String queryString = ""; // NOI18N

    private int httpMethod = HTTP_GET;

    static final long serialVersionUID =-7133245634646898359L;
    public ServletExecParams(DataObject obj) {
        name = obj.getPrimaryFile().getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) this.name = name;
        else              this.name = ""; // NOI18N
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        if (mapping != null) this.mapping = mapping;
        else                 this.mapping = ""; // NOI18N
        if (mapping.length() > 0 && !mapping.startsWith("/") && !mapping.startsWith(".")) // NOI18N
            mapping = "/" + mapping; // NOI18N
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        if (queryString != null) this.queryString = queryString;
        else                     this.queryString = ""; // NOI18N
    }

    public Vector getInitParams() {
        return initParams;
    }

    public void setInitParams(Vector initParams) {
        if (initParams != null) this.initParams = initParams;
        else                    this.initParams = new Vector();
    }


}

/*
 * Log
 *  5    Gandalf   1.4         1/12/00  Petr Jiricka    i18n phase 1
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/8/99  Petr Jiricka    Ensure there are no null
 *       values
 *  1    Gandalf   1.0         10/7/99  Petr Jiricka    
 * $
 */
