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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import java.beans.*;
import java.util.*;

import net.jini.discovery.LookupDiscovery;
import net.jini.core.discovery.*;

import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.nodes.CookieSet;

/**
* The root node of Jini Browser representation .
*
* @author Martin Ryzl, Petr Kuzel
*/
public class JiniNode extends DefaultNode implements Node.Cookie {

    /** Only one allowed instance of Jini node. */
    private static JiniNode jnode = null;

    /** Method for debugging.*/
    private static void debug(String msg) {
        System.err.println("JiniNode: " + msg);
    }

    /** Default constructor. Can be called only once.
    */
    public JiniNode() {
        super(
            (JiniModule.isEnabled()) ?
            new JiniChildren()
            : Children.LEAF
        );
        if (jnode != null) throw new InstantiationError("Only one instance is allowed.");
        init();
        jnode = this;
    }

    /** Initialization of the node.
    */
    private void init() {
        setDisplayName(Util.getString("PROP_Jini_Node_Name"));
        setName(Util.getString("PROP_Jini_Node_Name"));

        if (JiniModule.isEnabled()) {
            systemActions = new SystemAction[] {
                                SystemAction.get(org.openide.actions.NewAction.class),
                                null,
                                SystemAction.get(org.openide.actions.ToolsAction.class),
                                SystemAction.get(org.openide.actions.PropertiesAction.class),
                            };
            setIconBase(Util.getString("JINI_NODE_ICON_BASE"));

        } else { // not enabled
            setIconBase(Util.getString("NO_JINI_NODE_ICON_BASE"));
        }
    }

    /** Getter for node.
     * @return jini node
     */
    public static JiniNode getNode() {
        if (jnode == null) jnode = new JiniNode();
        return jnode;
    }

    /** Type for new action.
     * @return possible types
     */
    public NewType[] getNewTypes() {
        return new NewType[] { new LookupType(), new DiscoveryType() };
    }

    public void removeKey(Object key) {
        JiniChildren kids = (JiniChildren) getChildren();
        kids.removeKey(key);
    }

    /**
    * Represents top browser levels. 
    */
    static class JiniChildren extends Children.Keys implements PropertyChangeListener {

        org.netbeans.modules.jini.settings.JiniSettings data;

        public JiniChildren() {
            data = org.netbeans.modules.jini.settings.JiniSettings.DEFAULT;
            data.addPropertyChangeListener(this);
        }

        public Node[] createNodes(Object key) {
            BrowserModel browser;

            if (key instanceof LookupLocator) {

                LookupLocator loc = (LookupLocator) key;
                browser = new BrowserModel(loc);
                return new Node[] { new LocatorNode(browser, loc) };

            } else if (key instanceof Groups) {

                Groups groups = (Groups) key;
                browser = new BrowserModel(groups.getGroups());
                return new Node[] { new GroupNode(browser, groups)};

            }

            return null;
        }

        public void addLocator(LookupLocator loc) {
            data.addTarget(loc);
        }

        public void addGroups(String[] grps) {
            if (grps == LookupDiscovery.ALL_GROUPS) {
                data.addTarget(Groups.ALL);

            } else {
                for (int i = 0; i<grps.length; i++) {
                    addGroup(grps[i]);
                }
            }
        }

        public void addGroup(String grp) {
            data.addTarget(new Groups(grp));
        }

        public void removeKey(Object key) {
            data.removeTarget(key);
        }

        /** Syntactic fix. */
        public void _setKeys(Collection col) {
            super.setKeys(col);
        }

        public void propertyChange(final PropertyChangeEvent e) {

            // during deserialization process I can got null named property name
            if ("targets".equals(e.getPropertyName()) || e.getPropertyName() == null) {
                // asynchrony fix #5886
                org.openide.util.RequestProcessor.postRequest( new Runnable() {
                            public void run() {
                                HashSet keys = org.netbeans.modules.jini.settings.JiniSettings.DEFAULT.getTargets();
                                _setKeys(keys);
                            }
                        });
            }
        }
    }


    /** New registrar by lookup locator. */
    class LookupType extends NewType implements ActionListener {

        private Dialog myDialog = null;
        private RegistrarPanel rpanel = null;

        public LookupType() {
            super();
        }

        public void create() throws IOException {
            rpanel = new RegistrarPanel();
            DialogDescriptor dd = new DialogDescriptor(rpanel,
                                  Util.getString("PROP_New_Lookup"), false, this);
            myDialog = TopManager.getDefault().createDialog(dd);
            myDialog.show();
        }

        public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() == DialogDescriptor.OK_OPTION) {
                try {
                    int port = Integer.parseInt(rpanel.getPort());
                    String host = rpanel.getHost().trim();
                    if (host.length() > 0) {

                        // add new Locator node
                        LookupLocator loc = new LookupLocator(host, port);
                        ((JiniChildren)JiniNode.this.getChildren()).addLocator(loc);
                    } else {
                        // hostname must not be empty
                        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                                  Util.getString("MSG_Wrong_Host"), NotifyDescriptor.ERROR_MESSAGE);
                        TopManager.getDefault().notify(nd);
                    }
                } catch (NumberFormatException ex) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(
                                              Util.getString("MSG_Wrong_Port"), NotifyDescriptor.ERROR_MESSAGE);
                    TopManager.getDefault().notify(nd);
                }
            }
            if (myDialog != null) {
                myDialog.dispose();
                myDialog = null;
            }
        }

        public String getName() {
            return "Locator"; //Util.getString("Locator");
        }
    }

    /** New registrar by discovery. */
    class DiscoveryType extends NewType implements ActionListener {

        Dialog myDialog;
        SelectGroups gpanel;

        public void create() throws IOException {
            //      LookupDiscovery ld = JiniRegistrarPool.getPool().getDiscovery();
            gpanel = new SelectGroups();
            //      gpanel.setGroups(ld.getGroups());
            DialogDescriptor dd = new DialogDescriptor(gpanel,
                                  Util.getString("LAB_Group"), false, this
                                                      );
            myDialog = TopManager.getDefault().createDialog(dd);
            myDialog.show();
        }

        public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() == DialogDescriptor.OK_OPTION) {
                String[] groups = gpanel.getGroups();
                ((JiniChildren)JiniNode.this.getChildren()).addGroups(groups);
            }
            if (myDialog != null) {
                myDialog.dispose();
                myDialog = null;
            }
        }

        public String getName() {
            return "Group"; //Util.getString("Group");
        }
    }
}


/*
* <<Log>>
*  12   Gandalf-post-FCS1.9.1.1     3/7/00   Petr Kuzel      syntax fix
*  11   Gandalf-post-FCS1.9.1.0     3/1/00   Petr Kuzel      bug fix
*  10   Gandalf   1.9         2/2/00   Petr Kuzel      Jini module upon 1.1alpha
*  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  8    Gandalf   1.7         8/18/99  Martin Ryzl     localization corrected
*  7    Gandalf   1.6         8/3/99   Martin Ryzl     
*  6    Gandalf   1.5         7/30/99  Martin Ryzl     group selection dialog
*  5    Gandalf   1.4         6/11/99  Martin Ryzl     
*  4    Gandalf   1.3         6/9/99   Ian Formanek    ToolsAction
*  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         6/4/99   Martin Ryzl     jini v2
*  1    Gandalf   1.0         6/2/99   Martin Ryzl     
* $ 
*/ 

