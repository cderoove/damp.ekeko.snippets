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

/** Action that opens explorer view which displays global
* options of the IDE.
 *
 * @author Dafe Simonek
 */
public class OptionsAction extends CallableSystemAction {

    /** Creates new OptionsAction. */
    public OptionsAction() {
    }

    /** Shows options panel. */
    public void performAction () {
        OptionsPanel singleton = OptionsPanel.singleton();
        singleton.open();
        singleton.requestFocus();
    }

    /** URL to this action.
    * @return URL to the action icon
    */
    public String iconResource () {
        return "/org/netbeans/modules/projects/resources/sessionSettings.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(OptionsAction.class);
    }

    public String getName() {
        return NbBundle.getBundle(OptionsAction.class).getString("Options");
    }

    /** Options panel. Uses singleton pattern. */
    public static final class OptionsPanel extends SettingsTab {

        /** Singleton instance of options panel */
        private static OptionsPanel singleton;
        /** Formatted title of this view */
        private static MessageFormat formatTitle;

        public OptionsPanel () {
            super();
            Node original = TopManager.getDefault().getPlaces().nodes().session();
            setRootContext(new OptionsNode(original, new GlobalOptionsChildren()));
        }

        /** Accessor to the singleron instance */
        static OptionsPanel singleton () {
            if (singleton == null) {
                singleton = new OptionsPanel();
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
        static final long serialVersionUID =7928901119428333839L;

        public Node getNode() {
            Node original = TopManager.getDefault().getPlaces().nodes().session();
            return new OptionsNode(original, new GlobalOptionsChildren());
        }
    }

    private static class OptionsNode extends FilterNode {
        public OptionsNode(Node original, org.openide.nodes.Children children) {
            super(original, children);
        }

        public Node.Handle getHandle() {
            return new FilterHandle();
        }
    }
}

/*
* Log
*  5    Gandalf   1.4         1/14/00  Martin Ryzl     
*  4    Gandalf   1.3         1/13/00  Martin Ryzl     
*  3    Gandalf   1.2         1/13/00  Martin Ryzl     heavy localization
*  2    Gandalf   1.1         1/10/00  Martin Ryzl     
*  1    Gandalf   1.0         1/3/00   Martin Ryzl     
* $ 
*/ 
