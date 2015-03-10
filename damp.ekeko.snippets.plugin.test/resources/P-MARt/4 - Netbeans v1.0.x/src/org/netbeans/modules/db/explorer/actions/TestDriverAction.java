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
import org.openide.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.*;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.dlg.*;
import org.netbeans.modules.db.explorer.infos.*;

public class TestDriverAction extends DatabaseAction
{
    static final long serialVersionUID =-7201887208073852066L;
    protected boolean enable(Node[] activatedNodes)
    {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
        else return false;

        ConnectionNodeInfo info = (ConnectionNodeInfo)node.getCookie(ConnectionNodeInfo.class);
        if (info != null) return (info.getConnection() != null);
        return true;
    }

    public void performAction (Node[] activatedNodes)
    {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
        else return;

        try {
            ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle");
            DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
            Connection conn = info.getConnection();
            String user = info.getUser();
            boolean testok = false;
            if (conn == null) {
                DatabaseConnection cinfo = new DatabaseConnection();
                String driver = (String)info.get(DatabaseNodeInfo.URL);
                cinfo.setDriver(driver);
                cinfo.setDatabase((String)info.get(DatabaseNodeInfo.PREFIX));
                TestDriverDialog cdlg = new TestDriverDialog(cinfo);
                if (cdlg.run()) {
                    conn = cdlg.getConnection();
                    if (conn != null) testok = performTest(info, conn, user, driver);
                    else throw new SQLException("unable to get the connection");
                    conn.close();
                } else return;

            } else {
                String driver = info.getDriver();
                testok = performTest(info, conn, user, driver);
            }

            String testmsg = bundle.getString(testok ? "TestDriverActionOK" : "TestDriverActionError");
            TopManager.getDefault().notify(new NotifyDescriptor.Message(testmsg));

        } catch(Exception e) {
            //      e.printStackTrace();
            TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to perform action, " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        }
    }

    private boolean performTest(DatabaseNodeInfo info, Connection dbcon, String user, String driver) throws SQLException
    {
        boolean ok = true;
        PrintWriter ow = TopManager.getDefault().getStdOut();
        if (ow == null)
            ow = new PrintWriter(System.out);

        int counter;
        DatabaseMetaData dmd = null;
        String tab = null, view = null, proc = null;
        DatabaseSpecification spec = info.getSpecification();
        DriverSpecification drvSpec = info.getDriverSpecification();
        String catalog = dbcon.getCatalog();

        try {
            if (spec == null) {
                SpecificationFactory fac = new SpecificationFactory();
                spec = fac.createSpecification(dbcon, "GenericDatabaseSystem");
                drvSpec = fac.createDriverSpecification(spec.getMetaData().getDriverName().trim());
            }
            dmd = spec.getMetaData();
        } catch (DDLException ex) {
            dmd = dbcon.getMetaData();
        } catch (DatabaseProductNotFoundException ex) {
            dmd = dbcon.getMetaData();
            //    } catch (DriverProductNotFoundException ex) {
            //      dmd = dbcon.getMetaData();
        }

        ow.println("\n\nTesting driver "+driver+"\n\n");

        // Tables
        String[] filter = new String[] {"TABLE"};
        drvSpec.getTables(catalog, dmd, null, filter);
        counter = 0;
        if (drvSpec.rs != null) {
            while (drvSpec.rs.next()) {
                if (tab == null)
                    tab = drvSpec.rs.getString("TABLE_NAME");
                counter++;
            }
            drvSpec.rs.close();
        }
        if (tab != null) {
            ow.println("Test getTables: found "+counter+" tables");

            // Columns
            drvSpec.getColumns(catalog, dmd, tab, null);
            counter = 0;
            if (drvSpec.rs != null) {
                while (drvSpec.rs.next())
                    counter++;
                if (counter > 0)
                    ow.println("Test getColumns: table " + tab + " contains " + counter + " columns");
                else {
                    ow.println("Warning: no columns were found in table " + tab + ", check your database.");
                    ok = false;
                }
                drvSpec.rs.close();
            }

            // Primary keys
            drvSpec.getPrimaryKeys(catalog, dmd, tab);
            counter = 0;
            if (drvSpec.rs != null) {
                while (drvSpec.rs.next())
                    counter++;
                if (counter > 0)
                    ow.println("Test getPrimaryKeys: table " + tab + " contains " + counter + " primary keys");
                else {
                    ow.println("Warning: no primary keys were found in table " + tab + ", check your database.");
                    ok = false;
                }
                drvSpec.rs.close();
            }

            /*
                  //ExportedKeys
                  drvSpec.getExportedKeys(null, dmd, tab);
                  counter = 0;
                  if (drvSpec.rs != null) {
                    while (drvSpec.rs.next()) counter++;
                    if (counter > 0)
                      ow.println("Test getExportedKeys: table " + tab + " contains " + counter + " exported keys");
                    else {
                      ow.println("Warning: no exported keys were found, check your database.");
                      ok = false;
                    }
                    drvSpec.rs.close();
                  }
            */

            // Indexes
            drvSpec.getIndexInfo(catalog, dmd, tab, true, false);
            counter = 0;
            if (drvSpec.rs != null) {
                while (drvSpec.rs.next())
                    counter++;
                if (counter > 0)
                    ow.println("Test getIndexInfo: table " + tab + " contains " + counter + " indexes");
                else {
                    ow.println("Warning: no indexes were found in table " + tab + ", check your database.");
                    ok = false;
                }
                drvSpec.rs.close();
            }
        } else {
            ow.println("Warning: no tables were found, check your database.");
            ok = false;
        }

        // Views
        filter = new String[] {"VIEW"};
        drvSpec.getTables(catalog, dmd, null, filter);
        counter = 0;
        if (drvSpec.rs != null) {
            while (drvSpec.rs.next()) {
                if (view == null) view = drvSpec.rs.getString("TABLE_NAME");
                counter++;
            }
            if (view != null)
                ow.println("Test getTables: found " + counter + " views");
            else {
                ow.println("Warning: no views were found, check your database.");
                ok = false;
            }
            drvSpec.rs.close();
        } else {
            ow.println("Warning: no views were found, check your database.");
            ok = false;
        }

        // Procedures
        drvSpec.getProcedures(catalog, dmd, null);
        counter = 0;
        if (drvSpec.rs != null) {
            while (drvSpec.rs.next()) {
                if (proc == null) proc = drvSpec.rs.getString("PROCEDURE_NAME");
                counter++;
            }
            drvSpec.rs.close();

            if (proc != null) {
                ow.println("Test getProcedures: found " + counter + " procedures");

                // Procedure columns
                drvSpec.getProcedureColumns(catalog, dmd, proc, null);
                counter = 0;
                if (drvSpec.rs != null) {
                    while (drvSpec.rs.next()) counter++;
                    if (counter > 0)
                        ow.println("Test getProcedureColumns: procedure " + proc + " contains " + counter + " columns");
                    else {
                        ow.println("Warning: no procedure columns were found in procedure " + proc + ", check your database.");
                        ok = false;
                    }
                    drvSpec.rs.close();
                }
            } else {
                ow.println("Warning: no procedures were found, check your database.");
                ok = false;
            }
        } else {
            ow.println("Warning: no procedures were found, check your database.");
            ok = false;
        }

        return ok;
    }
}
/*
 * <<Log>>
 *  14   Gandalf-post-FCS1.12.1.0    4/10/00  Radko Najman    
 *  13   Gandalf   1.12        3/3/00   Radko Najman    ExportedKeys - driver 
 *       adaptor
 *  12   Gandalf   1.11        1/25/00  Radko Najman    new driver adaptor 
 *       version
 *  11   Gandalf   1.10        12/15/99 Radko Najman    driver adaptor
 *  10   Gandalf   1.9         11/27/99 Patrik Knakal   
 *  9    Gandalf   1.8         11/15/99 Radko Najman    MS ACCESS
 *  8    Gandalf   1.7         11/3/99  Radko Najman    getUserName()
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/17/99  Slavek Psenicka 
 *  5    Gandalf   1.4         9/17/99  Slavek Psenicka Test driver on open 
 *       connexction 
 *  4    Gandalf   1.3         9/15/99  Slavek Psenicka 
 *  3    Gandalf   1.2         9/13/99  Slavek Psenicka 
 *  2    Gandalf   1.1         9/8/99   Slavek Psenicka adaptor changes
 *  1    Gandalf   1.0         9/2/99   Slavek Psenicka 
 * $
 */
