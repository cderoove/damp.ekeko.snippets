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

import java.io.*;
import java.util.*;

import net.jini.core.discovery.*;
import net.jini.core.lookup.*;
import net.jini.discovery.*;
import net.jini.lookup.*;

import org.openide.nodes.*;
import org.openide.util.actions.*;

/**
 * Node representing multicast discovery. If deleted the discovery is stopped.
 * In cooperation with top level JiniNode stays persistent among IDE restarts.
 * 
 *
 * @author  Petr Kuzel
 * @version 
 */
public class GroupNode extends DefaultNode implements DiscoveryListener {

    private Groups grps;
    private BrowserModel browser;

    /** Creates new LocatorNode */
    public GroupNode(BrowserModel browser, Groups grps) {
        super(new LookupChildren(browser));

        this.grps = grps;
        this.browser = browser;
        browser.addDiscoveryListener(this);

        setName("Group: " + grps);

        updateIcon();

        systemActions = new SystemAction[] {
                            SystemAction.get(org.openide.actions.OpenLocalExplorerAction.class),
                            null,
                            SystemAction.get(org.openide.actions.DeleteAction.class),
                            null,
                            SystemAction.get(org.openide.actions.PropertiesAction.class)
                        };

    }

    /** Test equality by String[] equality. */
    public boolean equals(Object obj) {
        if (obj instanceof GroupNode) {
            GroupNode peer = (GroupNode) obj;
            return grps.equals(peer.grps);
        } else
            return false;

    }

    /** Initializes sheet of properties.
     *
     * @return sheet
     */
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        if (ss == null) {
            ss = s.createPropertiesSet();
            s.put(ss);
        }
        Node.Property p;

        try {

            Object obj = this;

            p = new PropertySupport.Reflection (obj, String.class, "getGroups", null);
            p.setName("Groups");
            p.setDisplayName("Groups");
            //      p.setShortDescription(Util.getString("HINT_getPort"));
            ss.put(p);

            p = new PropertySupport.Reflection (obj, String.class, "getLocators", null);
            p.setName("Locators");
            p.setDisplayName("Locators");
            //      p.setShortDescription(Util.getString("HINT_getPort"));
            ss.put(p);

        } catch (Exception ex) {
            throw new InternalError();
        }

        return s;
    }

    /** @return groups as comma separated string. */
    public String getGroups() {
        return grps.toString();
    }

    /** @return locators as comma separated string. */
    public String getLocators() {
        String prefix = "";

        LookupLocator[] locs = browser.getDiscoveredLocators();

        if (locs.length == 0)
            return "<none found>";
        else {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i< locs.length; i++) {
                buf.append(prefix + locs[i].getHost() + ":" + locs[i].getPort());
                prefix = ", ";
            }

            return buf.toString();
        }
    }

    /**
     */
    public boolean canDestroy() {
        return true;
    }

    public void destroy() throws IOException {
        browser.terminate();
        JiniNode.getNode().removeKey(grps);
        super.destroy();
    }

    /** Determine browser status. */
    public void discovered(DiscoveryEvent e) {
        updateStatus();
    }

    /** Determine browser status. */
    public void discarded(DiscoveryEvent e) {
        updateStatus();
    }

    private void updateStatus() {
        updateIcon();
    }

    private void updateIcon() {
        if (browser.getDiscoveredLocators().length == 0) {
            setIconBase(Util.getString("INVALID_GROUP_ICON_BASE"));    //NOI18N
        } else {
            if (grps.equals(Groups.ALL))
                setIconBase(Util.getString("GROUPS_ICON_BASE"));    //NOI18N
            else
                setIconBase(Util.getString("GROUP_ICON_BASE"));    //NOI18N
        }
    }

}


/*
* <<Log>>
*  2    Gandalf   1.1         2/3/00   Petr Kuzel      Be smart and documented
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

