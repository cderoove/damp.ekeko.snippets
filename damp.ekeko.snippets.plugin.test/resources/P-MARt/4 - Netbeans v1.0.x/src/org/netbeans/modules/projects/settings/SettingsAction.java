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

package org.netbeans.modules.projects.settings;

import java.io.ObjectStreamException;
import java.text.MessageFormat;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallableSystemAction;
import org.openide.TopManager;
import org.openide.nodes.*;
import org.openide.explorer.ExplorerManager;

/** Action that opens explorer view which displays settings of the IDE
* which can have various values in multiple projects.
*
* @author Dafe Simonek
*/
public class SettingsAction extends CallableSystemAction {

    /** Shows settings panel. */
    public void performAction () {
        SettingsPanel singleton = SettingsPanel.singleton();
        singleton.open();
        singleton.requestFocus();
    }

    /** URL to this action.
    * @return URL to the action icon
    */
    public String iconResource () {
        return "/org/netbeans/modules/projects/resources/projectSettings.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(SettingsAction.class);
    }

    public String getName() {
        return NbBundle.getBundle(SettingsAction.class).getString("Settings");
    }

    /** Options panel. Uses singleton pattern. */
    public static final class SettingsPanel extends SettingsTab {

        /** Singleton instance of settings panel */
        private static SettingsPanel singleton;
        /** Formatted title of this view */
        private static MessageFormat formatTitle;

        public SettingsPanel () {
            super();
            Node original = TopManager.getDefault().getPlaces().nodes().project();
            setRootContext(new SettingsNode(original, new ProjectSettingsChildren(original)));
        }

        /** Accessor to the singleron instance */
        static SettingsPanel singleton () {
            if (singleton == null) {
                singleton = new SettingsPanel();
            }
            return singleton;
        }

        /** Resolves to the singleton instance of options panel. */
        public Object readResolve ()
        throws ObjectStreamException {
            if (singleton == null) {
                singleton = this;
            }
            return singleton;
        }

    } // end of inner class OptionsPanel


    private static class FilterHandle implements Node.Handle {
        static final long serialVersionUID =7928908222428333839L;
        public Node getNode() {
            Node original = TopManager.getDefault().getPlaces().nodes().project();
            return new SettingsNode(original, new ProjectSettingsChildren(original));
        }
    }

    private static class SettingsNode extends FilterNode {

        public SettingsNode(Node original, Children children) {
            super(original, children);
        }

        public Node.Handle getHandle() {
            Node.Handle handle = new FilterHandle();
            return handle;
        }
    }
}