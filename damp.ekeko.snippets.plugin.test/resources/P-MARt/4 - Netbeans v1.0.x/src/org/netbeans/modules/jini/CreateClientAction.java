/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jini;

import java.awt.datatransfer.StringSelection;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.*;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.*;

/**
* Action that creates client binding code.
*
* @author Petr Kuzel
*/
public class CreateClientAction extends NodeAction {

    private static boolean DEBUG = false;
    private static ResourceBundle bundle = NbBundle.getBundle(CreateClientAction.class);

    //{0} fully qualified interface name
    //{1} justinterfacename

    static String FMT_CODE =
        "   \n" +
        "  /** \n" +
        "  * Obtain reference for specific djinn service in multicast domain. \n" +
        "  * @return service (blocks until arbitrary one is located)\n" +
        "  * @throw java.io.IOException on network error \n" +
        "  */ \n" +
        "  protected {0} lookup{1}() throws java.io.IOException '{' \n" +
        "     \n" +
        "    // prepare runtime environment \n" +
        "     \n" +
        "    if (System.getSecurityManager() == null) '{' \n" +
        "      System.setSecurityManager(new java.rmi.RMISecurityManager()); \n" +
        "    '}' \n" +
        "     \n" +
        "    // discovery stage \n" +
        "     \n" +
        "    String[] groups = net.jini.discovery.LookupDiscovery.ALL_GROUPS; \n" +
        "    net.jini.core.discovery.LookupLocator[] locators =  null; \n" +
        "    net.jini.discovery.DiscoveryManagement dmgr = \n" +
        "      new net.jini.discovery.LookupDiscoveryManager(groups, locators, null); \n" +
        "     \n" +
        "    // lookup stage \n" +
        "     \n" +
        "    net.jini.core.lookup.ServiceID sid = null; \n" +
        "    Class[] interfaces = new Class[] '{' {0}.class '}'; \n" +
        "    net.jini.core.entry.Entry[] entries = null; \n" +
        "    net.jini.core.lookup.ServiceTemplate template = \n" +
        "      new net.jini.core.lookup.ServiceTemplate(sid, interfaces, entries); \n" +
        "     \n" +
        "    net.jini.lookup.ClientLookupManager djinn = \n" +
        "      new net.jini.lookup.ClientLookupManager(dmgr, null); \n" +
        "     \n" +
        "    Object service = djinn.lookup(template, \n" +
        "        new net.jini.lookup.ServiceItemFilter() '{' \n" +
        "          public boolean check(net.jini.core.lookup.ServiceItem item) '{' \n" +
        "            if (item.service != null) return true; \n" +
        "            else return false; \n" +
        "          '}' \n" +
        "        '}' \n" +
        "      ); \n" +
        "     \n" +
        "    return ({0}) service; \n" +
        "     \n" +
        "  '}' \n";

    /**
    * @return true if one node selected because it is assumed that it is 
    * attached only to interface nodes.
    */
    public boolean enable(Node[] nodes) {
        return nodes.length == 1;
    }


    /** Action.
    */
    protected void performAction(final Node[] nodes) {
        if (nodes.length != 1) return;

        InterfaceNode in = (InterfaceNode) nodes[0];
        if (in != null) {
            Class cl = in.getInterface();
            if (cl != null) {

                String cln = cl.getName();
                int offset = cln.lastIndexOf(".");
                offset = offset < 0 ? 1 : offset + 1;
                String name = cl.getName().substring(offset);

                StringSelection ss = new StringSelection(MessageFormat.format(
                                         FMT_CODE,
                                         new Object[] {
                                             cl.getName(),
                                             name
                                         }
                                     ));
                TopManager.getDefault().getClipboard().setContents(ss, null);
                TopManager.getDefault().setStatusText("Lookup method code was placed into clipboard");

            }
        }

    }


    /** Get name of the action.
    */
    public String getName() {
        return "Copy lookup method";
    }

    /** Get help context for the action.
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("xx");
    }
}


/*
* <<Log>>
*  2    Gandalf   1.1         2/7/00   Petr Kuzel      More service details
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

