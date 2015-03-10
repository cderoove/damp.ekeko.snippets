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

package org.netbeans.modules.form;

import org.openide.actions.*;
import org.openide.cookies.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.form.compat2.layouts.support.DesignSupportLayout;

import java.util.ArrayList;

/**
*
* @author Ian Formanek
*/
public class RADChildren extends Children.Keys {
    /** Icon base for non visuals node. */
    private static final String NON_VISUAL_ICON_BASE = "org/netbeans/modules/form/resources/formNonVisual"; // NOI18N

    private ComponentContainer container;

    private final static Object KEY_NON_VISUAL = new Object ();
    private final static Object KEY_LAYOUT = new Object ();

    // FINALIZE DEBUG METHOD
    public void finalize () throws Throwable {
        super.finalize ();
        if (System.getProperty ("netbeans.debug.form.finalize") != null) {
            System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this); // NOI18N
        }
    } // FINALIZE DEBUG METHOD

    /** Creates new RADChildren for specified ComponentContainer
    * @param container The component container for which this children should be created
    */
    public RADChildren (ComponentContainer container) {
        super ();
        this.container = container;
        updateKeys ();
    }

    void updateKeys () {
        RADComponent[] subComps = container.getSubBeans ();
        ArrayList keys = new ArrayList (subComps.length + 2);
        if (container instanceof FormContainer) {
            keys.add (KEY_NON_VISUAL);
        }

        if ((container instanceof RADVisualContainer) && !(((RADVisualContainer)container).getDesignLayout () instanceof DesignSupportLayout)) {
            keys.add (KEY_LAYOUT);
        }

        for (int i = 0; i < subComps.length; i++) {
            keys.add (subComps[i]);
        }

        setKeys (keys);
    }

    /** Create nodes for a given key.
    * @param key the key
    * @return child nodes for this key
    */
    protected Node[] createNodes (Object key) {
        if (key == KEY_NON_VISUAL) {
            NonVisualNode node = new NonVisualNode (((RADVisualFormContainer)container).getFormManager ());
            return new Node[] { node };

        } else if (key == KEY_LAYOUT) {
            return new Node[] { new RADLayoutNode ((RADVisualContainer)container) };

        } else {
            Node newNode = new RADComponentNode ((RADComponent)key);
            newNode.getChildren ().getNodes (); // enforce subnodes creation
            return new Node[] { newNode };
        }
    }

    private final static class NonVisualNode extends AbstractNode {
        private FormManager2 manager;

        NonVisualNode (FormManager2 manager) {
            super (manager.getNonVisualChildren ());
            this.manager = manager;
            setIconBase (NON_VISUAL_ICON_BASE);
            setName (FormEditor.getFormBundle ().getString ("CTL_NonVisualComponents"));
            getCookieSet ().add (new NonVisualIndex (manager));
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (NonVisualNode.class);
        }

        /** Lazily initialize set of node's actions (overridable).
        * The default implementation returns <code>null</code>.
        * <p><em>Warning:</em> do not call {@link #getActions} within this method.
        * If necessary, call {@link NodeOp#getDefaultActions} to merge in.
        * @return array of actions for this node, or <code>null</code> to use the default node actions
        */
        protected SystemAction [] createActions () {
            return new SystemAction[] {
                       SystemAction.get(ReorderAction.class),
                       null,
                       SystemAction.get(ToolsAction.class),
                       SystemAction.get(PropertiesAction.class),
                   };
        }

        /** Get a cookie from the node.
        * Uses the cookie set as determined by {@link #getCookieSet}.
        *
        * @param type the representation class
        * @return the cookie or <code>null</code>
        */
        public Node.Cookie getCookie (Class type) {
            Node.Cookie inh = super.getCookie (type);
            if (inh == null) {
                if (CompilerCookie.class.isAssignableFrom (type) ||
                        SaveCookie.class.isAssignableFrom (type) ||
                        ExecCookie.class.isAssignableFrom (type) ||
                        DebuggerCookie.class.isAssignableFrom (type) ||
                        CloseCookie.class.isAssignableFrom (type) ||
                        ArgumentsCookie.class.isAssignableFrom (type) ||
                        PrintCookie.class.isAssignableFrom (type)) {
                    return manager.getFormObject ().getCookie (type);
                }
            }
            return inh;
        }

    }

    /** Index support for reordering of file system pool.
    */
    private final static class NonVisualIndex extends org.openide.nodes.Index.Support {
        private FormManager2 manager;

        NonVisualIndex (FormManager2 manager) {
            this.manager = manager;
        }

        /** Get the nodes; should be overridden if needed.
        * @return the nodes
        * @throws NotImplementedException always
        */
        public Node[] getNodes () {
            return manager.getNonVisualChildren ().getNodes ();
        }

        /** Get the node count. Subclasses must provide this.
        * @return the count
        */
        public int getNodesCount () {
            return getNodes ().length;
        }

        /** Reorder by permutation. Subclasses must provide this.
        * @param perm the permutation
        */
        public void reorder (int[] perm) {
            manager.reorderNonVisualComponents (perm);
            manager.getNonVisualChildren ().updateKeys ();
        }

    }
}

/*
 * Log
 *  15   Gandalf   1.14        1/15/00  Ian Formanek    I18N
 *  14   Gandalf   1.13        1/5/00   Ian Formanek    NOI18N
 *  13   Gandalf   1.12        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   Gandalf   1.11        7/25/99  Ian Formanek    Fixed bug with too many 
 *       tools actions (namely those on DataObject.class) being enabled on the 
 *       node
 *  11   Gandalf   1.10        7/20/99  Jesse Glick     Context help.
 *  10   Gandalf   1.9         7/3/99   Ian Formanek    Fixed bug with selecting
 *       components added to previously added container which node had not been 
 *       expanded in the inspector before
 *  9    Gandalf   1.8         6/27/99  Ian Formanek    Many form actions 
 *       (compile, save, ...) are now enabled on form and component inspector
 *  8    Gandalf   1.7         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         6/2/99   Ian Formanek    ToolsAction, Reorder
 *  6    Gandalf   1.5         5/24/99  Ian Formanek    Non-Visual components
 *  5    Gandalf   1.4         5/15/99  Ian Formanek    
 *  4    Gandalf   1.3         5/12/99  Ian Formanek    
 *  3    Gandalf   1.2         5/11/99  Ian Formanek    Build 318 version
 *  2    Gandalf   1.1         5/4/99   Ian Formanek    Package change
 *  1    Gandalf   1.0         4/29/99  Ian Formanek    
 * $
 */
