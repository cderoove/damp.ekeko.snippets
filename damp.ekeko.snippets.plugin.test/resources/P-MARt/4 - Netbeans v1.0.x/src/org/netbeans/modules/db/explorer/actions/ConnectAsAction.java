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

package org.netbeans.modules.db.explorer.actions;

import java.io.*;
import java.beans.*;
import java.util.*;
import java.sql.*;
import org.netbeans.lib.ddl.impl.*;
import org.openide.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.dlg.*;

public class ConnectAsAction extends ConnectAction
{
    static final long serialVersionUID =-4698745426544151857L;
    public void performAction(Node[] activatedNodes)
    {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
        else return;
        try {
            DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
            ConnectionNodeInfo nfo = (ConnectionNodeInfo)info.getParent(DatabaseNode.CONNECTION);
            Connection connection = nfo.getConnection();
            if (connection != null) return;
            String dbsys = null;
            String drvurl = (String)nfo.get(DatabaseNodeInfo.DRIVER);
            String dburl = (String)nfo.get(DatabaseNodeInfo.DATABASE);
            String user = (String)nfo.getUser();
            String pwd = (String)nfo.getPassword();
            if (user == null || pwd == null || !nfo.containsKey("rememberspassword")) {
                Set sdbs = (Set)nfo.get(DatabaseNodeInfo.SUPPORTED_DBS);
                ConnectAsDialog dlg = new ConnectAsDialog(user, sdbs.toArray(new String[0]));
                if (dlg.run()) {
                    user = dlg.getUser();
                    nfo.setUser(user);
                    pwd = dlg.getPassword();
                    nfo.setPassword(pwd);
                    dbsys = dlg.getSelectedDatabaseProduct();
                } else return;
            }

            nfo.connect(dbsys);

        } catch (Exception e) {
            TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to connect, "+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        }
    }
}
/*
 * <<Log>>
 *  7    Gandalf   1.6         11/27/99 Patrik Knakal   
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/21/99  Slavek Psenicka new version
 *  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         4/23/99  Slavek Psenicka oprava activatedNode[0] 
 *       check
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
