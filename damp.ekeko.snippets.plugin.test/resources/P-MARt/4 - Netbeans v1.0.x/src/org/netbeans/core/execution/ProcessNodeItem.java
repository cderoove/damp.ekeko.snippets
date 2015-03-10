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

package org.netbeans.core.execution;

import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.JPopupMenu;

import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;

/** A Node that represents one process in the tree
* Due to changes:
* 1) process ends - this is carried out in execution listener
* 2) process is killed - proc.terminate(); removeThis();
*
* @author Ales Novak
* @version 0.13, Jun 09, 1998
*/
class ProcessNodeItem extends AbstractNode implements Cookie {

    static private final Image icon = Toolkit.getDefaultToolkit ().getImage (
                                          ProcessNodeItem.class.getResource ("/org/netbeans/core/resources/process.gif")); // NOI18N

    /** process that we represents */
    DefaultSysProcess proc;

    /** menu of the node */
    JPopupMenu menu;

    /** flag for recognizing whether the proc is running or not */
    private boolean zombie;

    ProcessNodeItem(final DefaultSysProcess proc) {
        super(org.openide.nodes.Children.LEAF);
        this.proc = proc;
        zombie = false;
        setName(proc.getName());
        setShortDescription (ProcessNode.getBundle().getString("HINT_ProcessNodeItem"));

        ExecutionEngine.getExecutionEngine().
        addExecutionListener(new ExecutionListener() {
                                 public void startedExecution(ExecutionEvent ev) {
                                 }

                                 public void finishedExecution(ExecutionEvent ev) {
                                     if (ev.getProcess() == proc) {
                                         removeThis();
                                         zombie = true;
                                         replaceMenu();
                                         // proc.terminate();
                                         ExecutionEngine.getExecutionEngine().
                                         removeExecutionListener(this);
                                     }
                                 }
                             });
        getCookieSet().add(this);
    }

    /** Finds an icon for this node. This icon should represent the node
    * when it is opened (when it can have children).
    *
    * @see java.bean.BeanInfo
    * @param type constants from <CODE>java.bean.BeanInfo</CODE>
    * @return icon to use to represent the bean when opened
    */
    public Image getIcon (int type) {
        return icon;
    }

    /** Finds an icon for this node. This icon should represent the node
    * when it is opened (when it can have children).
    *
    * @see java.bean.BeanInfo
    * @param type constants from <CODE>java.bean.BeanInfo</CODE>
    * @return icon to use to represent the bean when opened
    */
    public Image getOpenedIcon (int type) {
        return getIcon (type);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ProcessNodeItem.class);
    }

    public int hashCode() {
        return proc.hashCode();
    }

    /** remove this node from its parent that is from ProcessNode */
    protected void removeThis() {
        try {
            destroy ();
        } catch (java.io.IOException e) {
            org.openide.TopManager.getDefault().notifyException(e);
        }
    }

    public boolean equals(Object o) {
        if (! (o instanceof ProcessNodeItem)) return false;
        if (o == this) return true;
        return proc.equals(o);
    }

    /**
    * @return TerminateProcessAction
    */
    public SystemAction[] getActions() {
        return new SystemAction[] {(SystemAction) TerminateProcessAction.findObject(TerminateProcessAction.class, true)};
    }

    /** called when process changed its state from running to zombie
    * and some context menu is displayed during this change
    */
    void replaceMenu() {
        if (menu == null) return;
        if (! menu.isVisible()) return; //itself displays right menu item
        menu.setVisible(false);
    }

    /** action that closes the process */
    public static class TerminateProcessAction extends NodeAction {

        static final long serialVersionUID =3143000725263158885L;
        public TerminateProcessAction() {
        }

        /**
        * Perform the action based on the currently activated nodes.
        * Note that if the source of the event triggering this action was itself
        * a node, that node will be the sole argument to this method, rather
        * than the activated nodes.
        *
        * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
        */
        protected void performAction (final Node[] activatedNodes) {
            Runnable run = new Runnable () {
                               public void run () {
                                   if (activatedNodes != null &&
                                           activatedNodes.length > 0
                                      ) {
                                       ProcessNodeItem thiz;
                                       for (int i = 0; i < activatedNodes.length; i++) {
                                           thiz = (ProcessNodeItem) activatedNodes[i].getCookie(ProcessNodeItem.class);
                                           thiz.proc.stop();
                                           thiz.removeThis();
                                       }
                                   }
                               }
                           };
            org.openide.util.RequestProcessor.postRequest (run);
        }

        /**
        * Test whether the action should be enabled based
        * on the currently activated nodes.
        *
        * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
        * @return <code>true</code> to be enabled, <code>false</code> to be disabled
        */
        protected boolean enable(Node[] activatedNodes) {
            return true; // [PENDING]
        }

        /** @return the action's icon */
        public String getName() {
            return ProcessNode.getBundle().getString("terminateProcess");
        }

        /** @return the action's help context */
        public HelpCtx getHelpCtx() {
            return new HelpCtx (TerminateProcessAction.class);
        }

        /** Icon resource.
        * @return name of resource for icon
        */
        protected String iconResource () {
            return "/org/netbeans/core/resources/actions/empty.gif"; // NOI18N
        }
    }

    public boolean canCopy() {
        return false;
    }
}

/*
 * Log
 *  21   Gandalf   1.20        1/12/00  Ales Novak      i18n
 *  20   Gandalf   1.19        11/11/99 Jesse Glick     Display miscellany.
 *  19   Gandalf   1.18        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  18   Gandalf   1.17        10/1/99  Ales Novak      major change of 
 *       execution
 *  17   Gandalf   1.16        9/3/99   Jaroslav Tulach Proper synch.
 *  16   Gandalf   1.15        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  15   Gandalf   1.14        7/28/99  Ales Novak      bugfix #2662
 *  14   Gandalf   1.13        7/21/99  Ales Novak      canCopy - false
 *  13   Gandalf   1.12        7/21/99  Ales Novak      #2177
 *  12   Gandalf   1.11        7/8/99   Jesse Glick     Context help.
 *  11   Gandalf   1.10        7/8/99   Ales Novak      deadlock avoidance
 *  10   Gandalf   1.9         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  9    Gandalf   1.8         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    Gandalf   1.7         5/26/99  Ian Formanek    Actions cleanup
 *  7    Gandalf   1.6         5/9/99   Ian Formanek    setDisplayName -> 
 *       setName as recommended for AbstractNode
 *  6    Gandalf   1.5         5/6/99   Ales Novak      displaying of 
 *       NoProcesses node
 *  5    Gandalf   1.4         4/8/99   Ian Formanek    Changed Object.class -> 
 *       getClass ()
 *  4    Gandalf   1.3         1/21/99  Ales Novak      
 *  3    Gandalf   1.2         1/8/99   Ian Formanek    Fixed to compile under 
 *       Gandalf (quick&dirty)
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Formanek    popup menu improved
 *  0    Tuborg    0.13        --/--/98 Jaroslav Tulach perform action is run in different thread (as a request)
 */
