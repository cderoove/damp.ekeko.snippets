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

package org.netbeans.modules.rmi.registry;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.awt.Dialog;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.rmi.registry.LocateRegistry;
import java.util.ResourceBundle;

import org.openide.*;
import org.openide.loaders.DataNode;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.src.*;
import org.openide.src.nodes.SourceChildren;
import org.openide.util.datatransfer.NewType;

import org.netbeans.modules.rmi.RMIModule;
import org.netbeans.modules.rmi.settings.RMIRegistrySettings;

/** The node representation of RMIDataObject for Java sources.
*
* @author Martin Ryzl
*/
public class RMIRegistryNode extends AbstractNode
    implements Node.Cookie, RefreshCookie {

    /** Icon base. */
    static final String REGISTRY_ICON_BASE = "org/netbeans/modules/rmi/resources/rmiRegistry"; // NOI18N

    /** Instance of registry node. */
    private static RMIRegistryNode rrnode;

    /** Bundle. */
    private static ResourceBundle bundle = NbBundle.getBundle(RMIRegistryNode.class);

    public RMIRegistryNode() {
        super(RMIRegistryPool.getDefault().getChildren());
        init();
        rrnode = this;
    }

    private void init() {
        setDisplayName(bundle.getString("PROP_RMIRegistry_Name")); // NOI18N
        setName(bundle.getString("PROP_RMIRegistry_Name")); // NOI18N
        systemActions = new SystemAction[] {
                            SystemAction.get(org.netbeans.modules.rmi.registry.RMIRegistryRefreshAction.class),
                            null,
                            SystemAction.get(org.openide.actions.NewAction.class),
                            null,
                            SystemAction.get(org.openide.actions.ToolsAction.class),
                            SystemAction.get(org.openide.actions.PropertiesAction.class)
                        };
        setIconBase(REGISTRY_ICON_BASE);
        CookieSet cookies = getCookieSet();
        cookies.add(this);
    }

    /** Causes refresh of the node
     *
     */
    public void refresh() {
        RMIRegistryPool.RegistryChildren children = RMIRegistryPool.getDefault().getChildren();
        children.refreshIt();
    }

    /** Get default instance of the node.
    */
    public static RMIRegistryNode getNode() {
        return rrnode;
    }

    /** Get possible types for new action.
    */
    public NewType[] getNewTypes() {
        return new NewType[] { new NewRegistryType() };
    }

    /** NewType for RMIRegistry.
    */
    class NewRegistryType extends NewType implements ActionListener {
        private Dialog myDialog;
        private RegistryPanel rpanel;

        public String getName() {
            return bundle.getString("PROP_New_RMI_Registry"); // NOI18N
        }

        public void create() {
            rpanel = new RegistryPanel();
            myDialog = TopManager.getDefault().createDialog(
                           new DialogDescriptor(
                               rpanel,
                               bundle.getString("LAB_New_RMI_Registry"),  // NOI18N
                               false,
                               this
                           )
                       );
            myDialog.show();
        }

        public void actionPerformed(final ActionEvent ae) {

            RMIModule.getRP().postRequest(new Runnable() {

                                              public void run() {
                                                  RegistryItem ri;
                                                  if (ae.getSource() == DialogDescriptor.OK_OPTION) {
                                                      try {
                                                          int port = Integer.parseInt(rpanel.getPort());
                                                          if (rpanel.isCreateRequired()) {
                                                              // create a local registry
                                                              RMIRegistrySettings settings = (RMIRegistrySettings) RMIRegistrySettings.findObject(RMIRegistrySettings.class, true);
                                                              try {
                                                                  // start registry
                                                                  settings.startRegistry(port);
                                                                  // check security manager
                                                                  if (System.getSecurityManager() == null) {
                                                                      TopManager.getDefault().notify(new NotifyDescriptor.Message(
                                                                                                         NbBundle.getBundle(RMIRegistryNode.class).getString("MSG_SecurityManager"), // NOI18N
                                                                                                         NotifyDescriptor.ERROR_MESSAGE
                                                                                                     ));
                                                                  }
                                                              } catch (Exception ex) {
                                                                  TopManager.getDefault().notify(new NotifyDescriptor.Message(
                                                                                                     NbBundle.getBundle(RMIRegistryNode.class).getString("ERR_CreateRegistry"), // NOI18N
                                                                                                     NotifyDescriptor.ERROR_MESSAGE
                                                                                                 ));
                                                              }
                                                          }

                                                          ri = new RegistryItem(rpanel.getHost(), port);
                                                          RMIRegistryPool.getDefault().add(ri);

                                                      } catch (java.net.UnknownHostException e) {
                                                          TopManager.getDefault().notify(new NotifyDescriptor.Message(
                                                                                             NbBundle.getBundle(RMIRegistryNode.class).getString("ERR_UnknownHost"), // NOI18N
                                                                                             NotifyDescriptor.ERROR_MESSAGE
                                                                                         ));
                                                      } catch (java.rmi.UnknownHostException e) {
                                                          TopManager.getDefault().notify(new NotifyDescriptor.Message(
                                                                                             NbBundle.getBundle(RMIRegistryNode.class).getString("ERR_UnknownHost"), // NOI18N
                                                                                             NotifyDescriptor.ERROR_MESSAGE
                                                                                         ));
                                                      } catch (Exception e) {
                                                          TopManager.getDefault().notifyException(e);
                                                      }
                                                  }
                                                  myDialog.dispose();
                                              }
                                          });
        }
    }
}

/*
 * <<Log>>
 *  5    Gandalf-post-FCS1.2.1.1     4/20/00  Martin Ryzl     fix of #4387, #4514, 
 *       #4521, #4598, #4395
 *  4    Gandalf-post-FCS1.2.1.0     3/2/00   Martin Ryzl     local registry control 
 *       added
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         8/31/99  Martin Ryzl     
 *  1    Gandalf   1.0         8/27/99  Martin Ryzl     
 * $
 */














