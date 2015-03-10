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
import net.jini.lookup.*;
import net.jini.discovery.*;

import org.openide.nodes.*;
import org.openide.util.actions.*;

/**
 * Node representing unicast discovery. If deleted the discovery is stopped.
 * In cooperation with top level JiniNode stays persistent among IDE restarts.
 * 
 *
 * @author  Petr Kuzel
 * @version 
 */
public class LocatorNode extends DefaultNode implements DiscoveryListener {

    LookupLocator loc;
    BrowserModel browser;

    /** Creates new LocatorNode */
    public LocatorNode(BrowserModel browser, LookupLocator loc) {

        super(new LookupChildren(browser));

        this.browser = browser;
        this.loc = loc;
        browser.addDiscoveryListener(this);


        setName("Locator: " + loc.getHost() + ":" + loc.getPort());

        setIconBase(Util.getString("INVALID_REGISTRAR_ICON_BASE"));    //NOI18N

        systemActions = new SystemAction[] {
                            SystemAction.get(org.openide.actions.OpenLocalExplorerAction.class),
                            null,
                            SystemAction.get(org.openide.actions.DeleteAction.class),
                            null,
                            SystemAction.get(org.openide.actions.PropertiesAction.class)
                        };

    }

    /** Test equality by LookupLocator equality. */
    public boolean equals(Object obj) {
        if (obj instanceof LocatorNode)
            return loc.equals(((LocatorNode)obj).loc);
        return false;
    }

    /**
     */
    public boolean canDestroy() {
        return true;
    }


    public void destroy() throws IOException {
        browser.terminate();
        JiniNode.getNode().removeKey(loc);
        super.destroy();
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

            p = new PropertySupport.Reflection (obj, String.class, "getHost", null);
            p.setName("Host");
            p.setDisplayName(Util.getString("PROP_getHost"));
            p.setShortDescription(Util.getString("HINT_getHost"));
            ss.put(p);

            p = new PropertySupport.Reflection (obj, Integer.TYPE, "getPort", null);
            p.setName("Port");
            p.setDisplayName(Util.getString("PROP_getPort"));
            p.setShortDescription(Util.getString("HINT_getPort"));
            ss.put(p);

            p = new PropertySupport.Reflection (obj, String.class, "getGroups", null);
            p.setName("Groups");
            p.setDisplayName("Groups");
            //      p.setShortDescription(Util.getString("HINT_getPort"));
            ss.put(p);

        } catch (Exception ex) {
            throw new InternalError();
        }

        return s;
    }

    /** @return locator posr. */
    public int getPort() {
        return loc.getPort();
    }

    /** @return locator host. */
    public String getHost() {
        return loc.getHost();
    }

    /** @return groups as comma separated string. */
    public String getGroups() {
        return new Groups(browser.getDiscoveredGroups()).toString();
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
        if (browser.getDiscoveredLocators().length == 0) {
            setIconBase(Util.getString("INVALID_REGISTRAR_ICON_BASE"));    //NOI18N
        } else {
            setIconBase(Util.getString("VALID_REGISTRAR_ICON_BASE"));    //NOI18N
        }
    }
}


/*
* <<Log>>
*  2    Gandalf   1.1         2/3/00   Petr Kuzel      Be smart and documented
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

