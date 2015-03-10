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

package org.netbeans.core.actions;

import java.awt.*;
import java.util.ResourceBundle;
import java.io.ObjectInput;
import java.io.IOException;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.windows.Workspace;

import org.netbeans.core.execution.ProcessNode;
import org.netbeans.core.windows.WindowManagerImpl;

/** Execution view action.
*
* @author  Jan Jancura, Ian Formanek (checked [PENDING HelpCtx])
*/
public final class ExecutionViewAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -6906468860893774146L;

    /** The ExecutionView's icon */
    private static java.awt.Image executionIcon;

    public static final int DEFAULT_WINDOW_WIDTH = 270;
    public static final int DEFAULT_WINDOW_HEIGHT = 150;
    public static final int MIN_HEIGHT = 50;

    /** Link to the execution view. Singleton. */
    private static ExecutionView executionView = null;

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(ExecutionViewAction.class).getString("ExecutionView");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public org.openide.util.HelpCtx getHelpCtx() {
        return new HelpCtx (ExecutionViewAction.class);
    }

    /** Icon of this action.
    * @return name of the action icon
    */
    protected String iconResource () {
        return "/org/netbeans/core/resources/actions/executionView.gif"; // NOI18N
    }

    /** This method is called by one of the "invokers" as a result of
    * some user's action that should lead to actual "performing" of the action.
    * This default implementation calls the assigned actionPerformer if it
    * is not null otherwise the action is ignored.
    */
    public void performAction () {
        WindowManagerImpl wm = (WindowManagerImpl)TopManager.getDefault().
                               getWindowManager();
        if (!wm.reactivateComponent(ExecutionView.class)) {
            getExecutionView ().open ();
        }
    }

    /** @return an instance of ExecutionView */
    public static ExecutionView getExecutionView () {
        if (executionView == null)
            executionView = new ExecutionView ();
        return executionView;
    }

    /** Utility helper method - lazy init of icon */
    private static java.awt.Image getExecutionIcon () {
        if (executionIcon == null) {
            executionIcon = java.awt.Toolkit.getDefaultToolkit ().getImage (
                                ExecutionView.class.getResource (
                                    org.openide.util.Utilities.isLargeFrameIcons()
                                    ? "/org/netbeans/core/resources/frames/execution32.gif" // NOI18N
                                    : "/org/netbeans/core/resources/frames/execution.gif" // NOI18N
                                )
                            );
        }
        return executionIcon;
    }

    /** The top component which shows execution view */
    public static final class ExecutionView extends ExplorerPanel {
        // Attributes
        private boolean initialized = false;

        /** serial version UID */
        static final long serialVersionUID = 3712218929995126077L;

        /** Default constructor */
        ExecutionView () {
            super();
            ExplorerManager em = getExplorerManager ();
            em.setRootContext (ProcessNode.getExecutionNode());
            add (new org.openide.explorer.view.ListView(), BorderLayout.CENTER);
            setName (NbBundle.getBundle (ExecutionViewAction.class).
                     getString("CTL_Execution_view_title"));
            setIcon (getExecutionIcon());
        }

        public HelpCtx getHelpCtx () {
            return super.getHelpCtx (getExplorerManager ().getSelectedNodes (),
                                     new HelpCtx (ExecutionView.class));
        }

        public java.awt.Dimension getPreferredSize () {
            return new java.awt.Dimension (DEFAULT_WINDOW_WIDTH,
                                           DEFAULT_WINDOW_HEIGHT);
        }

        /** Does nothing - overriden to keep the title unchanged */
        protected void updateTitle () {
        }


        /** Writes a resolvable */
        protected Object writeReplace() {
            return new Resolvable();
        }
    }

    static class Resolvable implements java.io.Serializable {

        static final long serialVersionUID =8143238035030034549L;
        private Object readResolve() {
            return getExecutionView();
        }
    }
}

/*
 * Log
 *  24   src-jtulach1.23        1/12/00  Ales Novak      i18n
 *  23   src-jtulach1.22        12/8/99  Petr Hamernik   compilable by Javac V8 
 *       (jdk1.3)
 *  22   src-jtulach1.21        12/8/99  Jaroslav Tulach Write replace je 
 *       protected.
 *  21   src-jtulach1.20        11/26/99 Patrik Knakal   
 *  20   src-jtulach1.19        11/17/99 Ales Novak      #4438
 *  19   src-jtulach1.18        11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  18   src-jtulach1.17        11/4/99  Ales Novak      serialization
 *  17   src-jtulach1.16        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   src-jtulach1.15        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  15   src-jtulach1.14        7/28/99  David Simonek   serialization of window 
 *       system...first draft :-)
 *  14   src-jtulach1.13        7/12/99  Jesse Glick     Context help.
 *  13   src-jtulach1.12        7/11/99  David Simonek   window system change...
 *  12   src-jtulach1.11        6/17/99  David Simonek   various serialization 
 *       bugfixes
 *  11   src-jtulach1.10        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  10   src-jtulach1.9         5/26/99  Ian Formanek    Actions cleanup
 *  9    src-jtulach1.8         5/11/99  David Simonek   changes to made window 
 *       system correctly serializable
 *  8    src-jtulach1.7         4/16/99  Libor Martinek  
 *  7    src-jtulach1.6         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  6    src-jtulach1.5         3/25/99  David Simonek   changes in window 
 *       system, initial positions, bugfixes
 *  5    src-jtulach1.4         3/22/99  David Simonek   window system updated
 *  4    src-jtulach1.3         3/14/99  Ian Formanek    
 *  3    src-jtulach1.2         3/14/99  David Simonek   
 *  2    src-jtulach1.1         3/13/99  David Simonek   
 *  1    src-jtulach1.0         3/12/99  David Simonek   
 * $
 */
