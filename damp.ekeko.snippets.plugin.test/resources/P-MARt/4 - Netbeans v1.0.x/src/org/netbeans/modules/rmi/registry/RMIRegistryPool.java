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

import java.beans.*;
import java.util.*;
import java.rmi.*;
import java.rmi.registry.Registry;

import org.openide.util.HelpCtx;
import org.openide.nodes.*;
import org.openide.options.*;
import org.openide.util.*;

import org.netbeans.modules.rmi.*;
import org.netbeans.modules.rmi.settings.*;

/** Class representing set of all used registries.
*
* @author Martin Ryzl
*/
public class RMIRegistryPool {

    private static boolean DEBUG = false;

    /** Only one instance of RMIREgistryPool. */
    protected static RMIRegistryPool pool = null;

    /** Children. */
    protected RegistryChildren children;

    /** Creates new RMIRegistryPool with empty list.
    */
    protected RMIRegistryPool() {
    }

    /** Returns default registry pool.
     * @return default registry pool
     */
    public static RMIRegistryPool getDefault() {
        try {
            if (pool == null) {
                // create pool with default items
                pool = new RMIRegistryPool();
            }
            return pool;
        } catch (NullPointerException ex) {
            if (DEBUG) ex.printStackTrace();
            throw ex;
        }
    }

    /** Add registry item.
     * @param item registry
     */
    public synchronized void add(RegistryItem item) {
        getItems().add(item);
        updateItem(item);
        children.setKeys();
    }

    /** Remove registry item.
     * @param item registry
     */
    public synchronized void remove(RegistryItem item) {
        if (getItems().remove(item)) {
            children.setKeys();
            //      item.setServices(null);
        }
    }

    /** Test if the registry is present.
    */
    public boolean contains(RegistryItem item) {
        return getItems().contains(item);
    }

    /** Get registry items.
    * @return list of items
    */
    public Collection getItems() {
        return Collections.synchronizedCollection(
                   ((RMIRegistrySettings)RMIRegistrySettings.findObject(RMIRegistrySettings.class, true)).getRegs()
               );
    }

    /** Create children.
    */
    public RegistryChildren getChildren() {
        try {
            if (children == null) children = new RegistryChildren();
            return children;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /** Update status of an item.
    */
    public static void updateItem(final RegistryItem item) {
        // perform update in separate thread
        RMIModule.getRP().postRequest(
            new Runnable() {
                public void run() {
                    item.updateServices();
                }
            },
            0,
            Thread.MIN_PRIORITY + 1
        );
    }

    // -- Inner classes. --

    /** Children for registries.
     */
    public class RegistryChildren extends Children.Keys implements PropertyChangeListener {

        /** Refresh task. */
        RequestProcessor.Task task = null;

        /** Timeout. */
        int timeout = 0; // [PENDING] change it

        /** A copy of keys. */
        Collection keys = Collections.EMPTY_SET;

        public RegistryChildren() {
            updateTimeout();
            new RMIRegistrySettings().addPropertyChangeListener(this);
        }

        /** Schedule refresh task.
        */
        protected void scheduleRefreshTask(int millis) {
            if (task == null) {
                task = RMIModule.getRP().create(new RefreshTask());
                task.setPriority(Thread.MIN_PRIORITY);
            }
            task.schedule(millis);
            if (DEBUG) System.err.println("RCH: planned to " + timeout + " ms.");
        }

        /** Called to notify that the children has been asked for
        * children after and that they should set its keys.
        */
        protected void addNotify() {
            refreshIt();
        }

        /** Called to notify that the children has lost all of its references to its nodes
        * associated to keys and that the keys could be cleared without affecting any 
        * nodes (because nobody listens to that nodes).
        */
        protected void removeNotify() {
            setKeys2(Collections.EMPTY_SET);
        }

        /** Set keys.
        * @returns Collection of new keys. (Used by refresh thread).
        */
        public void setKeys() {
            setKeys2(getDefault().getItems());
        }

        /** Proxy to setKeys. It is necessary to call setKeys2 because the copy of
        * keys should be stored.
        */
        protected void setKeys2(Collection keysSet) {
            keys = keysSet;
            super.setKeys(keys);
        }

        /** Refresh keys.
        */
        public void refreshIt() {
            // start refresh task immediately
            scheduleRefreshTask(0);
        }

        /** Proxy to refeshKey.
        */
        protected void refreshKey2(Object key) {
            refreshKey(key);
        }

        /** Refresh one key.
         * @param key - key to update.
         */
        public void refresh(final Object key) {
            refreshKey2(key);
        }

        protected Node[] createNodes(Object key) {
            Node node;
            RegistryItem item = (RegistryItem) key;
            Collection services;
            if ((services = item.getServices()) != null) {
                node = new RegistryItemNode.ValidNode(item, new RegistryItemChildren(item));
            } else {
                node = new RegistryItemNode.InvalidNode(item);
            }
            return new Node[] {node};
        }

        /** Proxy to keys. */
        protected Collection getKeys() {
            return keys;
        }

        /** Refresh all keys on background. */
        private class RefreshTask implements Runnable {
            public void run() {
                if (DEBUG) System.err.println("RCH: Refresh Task invoked. ");
                setKeys();
                Iterator it = keys.iterator();
                while (it.hasNext()) {
                    Object item = it.next();
                    ((RegistryItem)item).updateServices();
                }
                if (timeout > 0) {
                    task.schedule(timeout);
                    if (DEBUG) System.err.println("RCH: replanned to " + timeout + " ms.");
                }
                if (DEBUG) System.err.println("RCH: RefreshTask finished.");
            }
        }

        protected void updateTimeout() {
            timeout = new RMIRegistrySettings().getRefreshTime();
        }

        /** PropertyChangeListener. It listens on RMIRegistrySettings for change of the refresh timeout.
        */
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();

            if (source instanceof RMIRegistrySettings) {
                if (RMIRegistrySettings.PROP_INTERNAL_REGISTRY_PORT.equals(evt.getPropertyName())) {
                    refreshIt();
                }

                if (RMIRegistrySettings.PROP_REFRESH_TIME.equals(evt.getPropertyName())) {
                    updateTimeout();
                    if (timeout != 0) scheduleRefreshTask(timeout);
                }
            }
        }
    }
}

/*
 * <<Log>>
 *  10   Gandalf-post-FCS1.8.1.0     3/2/00   Martin Ryzl     local registry control 
 *       added
 *  9    Gandalf   1.8         10/26/99 Martin Ryzl     debug info off
 *  8    Gandalf   1.7         10/25/99 Martin Ryzl     refresh timeout property
 *       added
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         10/12/99 Martin Ryzl     Beta5 bugfixes
 *  5    Gandalf   1.4         9/13/99  Martin Ryzl     varioous bug corrected  
 *  4    Gandalf   1.3         8/31/99  Martin Ryzl     
 *  3    Gandalf   1.2         8/31/99  Martin Ryzl     
 *  2    Gandalf   1.1         8/30/99  Martin Ryzl     saving corrected
 *  1    Gandalf   1.0         8/27/99  Martin Ryzl     
 * $
 */














