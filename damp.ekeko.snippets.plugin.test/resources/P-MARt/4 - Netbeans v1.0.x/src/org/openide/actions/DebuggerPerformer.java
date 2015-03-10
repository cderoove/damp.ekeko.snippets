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

package org.openide.actions;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Collections;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.*;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.cookies.DebuggerCookie;
import org.openide.cookies.CompilerCookie;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.CompilerTask;
import org.openide.debugger.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.ActionPerformer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.Workspace;
import org.openide.windows.WindowManager;


/**
* Performer for debugger actions.
*
* @author Jan Jancura
*/
class DebuggerPerformer extends Object implements PropertyChangeListener {


    // static ..........................................................................

    private static DebuggerPerformer  defaultPerformer;

    /** Debugger exception notification system */
    static void notifyDebuggerException (DebuggerException e) {
        TopManager.getDefault().notify(
            new NotifyDescriptor.Exception(e.getTargetException() == null ? e : e.getTargetException(),
                                           java.text.MessageFormat.format (NbBundle.getBundle(TraceIntoAction.class).getString("FMT_EXC_Debugger"),
                                                                           new Object[] { e.getMessage() })
                                          )
        );
    }

    static DebuggerPerformer getDefault () {
        if (defaultPerformer == null) new DebuggerPerformer ();
        return defaultPerformer;
    }

    private boolean installed = false;

    // init ............................................................................

    DebuggerPerformer () {
        if (defaultPerformer == null) defaultPerformer = this;
        TopManager.getDefault ().addPropertyChangeListener (this);
        update ();
    }


    // main methods ....................................................................

    public void propertyChange (PropertyChangeEvent e) {
        if (e.getPropertyName ().equals (TopManager.PROP_DEBUGGER)) {
            update ();
            return;
        }
        if (!e.getPropertyName ().equals (Debugger.PROP_STATE)) return;
        try {
            switch (TopManager.getDefault ().getDebugger ().getState ()) {
            case Debugger.DEBUGGER_NOT_RUNNING:
                ((TraceOverAction) SystemAction.get (TraceOverAction.class)).setActionPerformer (null);
                ((StepOutAction) SystemAction.get (StepOutAction.class)).setActionPerformer (null);
                ((StartDebuggerAction) SystemAction.get (StartDebuggerAction.class)).changeEnabled (true);
                ((GoAction) SystemAction.get (GoAction.class)).changeEnabled (false);
                ((TraceIntoAction) SystemAction.get (TraceIntoAction.class)).changeEnabled (true);
                ((FinishDebuggerAction) SystemAction.get (FinishDebuggerAction.class)).setActionPerformer (null);
                ((GoToCursorAction) SystemAction.get (GoToCursorAction.class)).changeEnabled (true);
                break;
            case Debugger.DEBUGGER_STARTING:
                break;
            case Debugger.DEBUGGER_RUNNING:
                ((TraceOverAction) SystemAction.get (TraceOverAction.class)).setActionPerformer (null);
                ((StepOutAction) SystemAction.get (StepOutAction.class)).setActionPerformer (null);
                ((GoAction) SystemAction.get (GoAction.class)).changeEnabled (false);
                ((TraceIntoAction) SystemAction.get (TraceIntoAction.class)).changeEnabled (false);
                ((FinishDebuggerAction) SystemAction.get (FinishDebuggerAction.class)).setActionPerformer (new FinishDebuggerPerformer ());
                ((GoToCursorAction) SystemAction.get (GoToCursorAction.class)).changeEnabled (false);
                break;
            case Debugger.DEBUGGER_STOPPED:
                ((TraceOverAction) SystemAction.get (TraceOverAction.class)).setActionPerformer (new TraceOverPerformer ());
                ((StepOutAction) SystemAction.get (StepOutAction.class)).setActionPerformer (new StepOutPerformer ());
                ((GoAction) SystemAction.get (GoAction.class)).changeEnabled (true);
                ((TraceIntoAction) SystemAction.get (TraceIntoAction.class)).changeEnabled (true);
                ((GoToCursorAction) SystemAction.get (GoToCursorAction.class)).changeEnabled (true);
                break;
            }
        } catch (DebuggerNotFoundException ex) {
        }
    }

    void update () {
        try {
            Debugger debugger = TopManager.getDefault ().getDebugger ();
            if (installed) return;
            installed = true;
            debugger.addPropertyChangeListener (this);
            ((AddWatchAction) SystemAction.get (AddWatchAction.class)).setActionPerformer (new AddWatchPerformer ());
            return;
        } catch (DebuggerNotFoundException ex) {
        }
        if (!installed) return;
        installed = false;
        ((AddWatchAction) SystemAction.get (AddWatchAction.class)).setActionPerformer (null);
    }

    void setDebuggerRunning (boolean b) {
        ((StartDebuggerAction) SystemAction.get (StartDebuggerAction.class)).changeEnabled (!b);
        ((GoAction) SystemAction.get (GoAction.class)).changeEnabled (!b);
        ((GoToCursorAction) SystemAction.get (GoToCursorAction.class)).changeEnabled (!b);
        ((TraceIntoAction) SystemAction.get (TraceIntoAction.class)).changeEnabled (!b);
        ((TraceOverAction) SystemAction.get (TraceOverAction.class)).setActionPerformer (null);
    }

    static void init () {
        final Debugger debugger;
        try {
            debugger = TopManager.getDefault ().getDebugger ();
        } catch (DebuggerException ex) {
            notifyDebuggerException(ex);
            return;
        }
    }


    // innerclasses ................................................................

    /**
    * Performer for AddWatch action.
    * This class is final only for performance reasons,
    * can be happily unfinaled if desired.
    */
    static final class AddWatchPerformer implements ActionPerformer {
        private static String watchHistory = ""; // NOI18N
        public void performAction(final org.openide.util.actions.SystemAction p1) {
            ResourceBundle bundle = NbBundle.getBundle (DebuggerPerformer.class);
            NotifyDescriptor.InputLine il =
                new NotifyDescriptor.InputLine (bundle.getString ("CTL_Watch_Name"),
                                                bundle.getString ("CTL_Watch_Title"));
            il.setInputText (watchHistory);
            Object r = TopManager.getDefault ().notify (il);
            if (r != NotifyDescriptor.OK_OPTION) return;
            String watch = il.getInputText();
            if (watch == null) return;
            try {
                Watch w = TopManager.getDefault ().getDebugger ().createWatch ();
                w.setVariableName (watch);
                watchHistory = watch;
            } catch (DebuggerException ex) {
                DebuggerPerformer.notifyDebuggerException(ex);
                return;
            }
        }
    } // AddWatchPerformer

    /**
    * Performer for FinishDebugger action.
    * This class is final only for performance reasons,
    * can be happily unfinaled if desired.
    */
    static final class FinishDebuggerPerformer implements ActionPerformer {

        public void performAction (SystemAction action) {
            DebuggerPerformer.getDefault ().setDebuggerRunning (true);
            try {
                TopManager.getDefault().getDebugger().finishDebugger();
            } catch (DebuggerException e) {
                DebuggerPerformer.notifyDebuggerException (e);
            }
        }
    } // FinishDebuggerPerformer

    /** Performer for StepOut action.
    * This class is final only for performance reasons,
    * can be happily unfinaled if desired.
    */
    static final class StepOutPerformer implements ActionPerformer {

        public void performAction(final org.openide.util.actions.SystemAction p1) {
            DebuggerPerformer.getDefault ().setDebuggerRunning (true);
            try {
                TopManager.getDefault().getDebugger().stepOut();
            } catch (DebuggerException e) {
                DebuggerPerformer.notifyDebuggerException (e);
            }
        }
    } // StepOutPerformer

    /**
    * Performer for TraceOver action.
    * This class is final only for performance reasons,
    * can be happily unfinaled if desired.
    */
    static final class TraceOverPerformer implements ActionPerformer {
        public void performAction(final org.openide.util.actions.SystemAction p1) {
            DebuggerPerformer.getDefault ().setDebuggerRunning (true);
            try {
                TopManager.getDefault().getDebugger().traceOver ();
            } catch (DebuggerException e) {
                DebuggerPerformer.notifyDebuggerException (e);
            }
        }
    } // TraceOverPerformer

    /** Switches to running workspace */
    static void changeWorkspace () {
        WindowManager dp = TopManager.getDefault ().getWindowManager ();
        final Workspace d = dp.findWorkspace (StartDebuggerAction.getWorkspace());
        if (d != null)
            SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            d.activate ();
                                        }
                                    });
    }


    /** This class performs debugger starting */
    public class StartDebugThread extends Thread {
        /** Currently activated nodes array */
        private Node[] activatedNodes;
        private boolean stopOnMain;

        private PropertyChangeListener goToCursor_pcl = null;
        private Breakpoint goToCursor_breakpoint = null;

        public StartDebugThread (final Node[] activatedNodes, boolean stopOnMain) {
            super();
            this.activatedNodes = activatedNodes;
            this.stopOnMain = stopOnMain;
        }

        void storeGoToCursorInfo (PropertyChangeListener pcl, Breakpoint b) {
            goToCursor_pcl = pcl;
            goToCursor_breakpoint = b;
        }

        public void run () {


            if (StartDebuggerAction.getRunCompilation()) {
                // compile cookies
                HashSet compile = new HashSet ();
                for (int i = 0; i < activatedNodes.length; i++) {
                    CompilerCookie comp = (CompilerCookie) activatedNodes[i].getCookie(CompilerCookie.Compile.class);
                    if (comp != null) {
                        compile.add(comp);
                    }
                }
                // do compile
                if (! AbstractCompileAction.compile(Collections.enumeration(compile),
                                                    AbstractCompileAction.findName(activatedNodes))) {
                    setDebuggerRunning (false);
                    try {
                        if (goToCursor_pcl != null) {
                            TopManager.getDefault ().getDebugger ().removePropertyChangeListener (goToCursor_pcl);
                            goToCursor_pcl = null;
                            goToCursor_breakpoint.remove ();
                        }
                        return;
                    }
                    catch (org.openide.debugger.DebuggerException e) {
                        notifyDebuggerException (e);
                        return;
                    }
                }
            }

            DebuggerCookie cookie = (DebuggerCookie) activatedNodes[0].getCookie (DebuggerCookie.class);
            if (cookie == null) {
                TopManager.getDefault().notify(
                    new NotifyDescriptor.Message(
                        java.text.MessageFormat.format (
                            NbBundle.getBundle(TraceIntoAction.class).getString ("FMT_MSG_CannotDebug"),
                            new Object[] {
                                activatedNodes[0].getDisplayName ()
                            }
                        ),
                        NotifyDescriptor.WARNING_MESSAGE)
                );
                return;
            }

            changeWorkspace();

            try {
                cookie.debug (stopOnMain);
                if (TopManager.getDefault ().getDebugger ().getState () == Debugger.DEBUGGER_NOT_RUNNING) {
                    setDebuggerRunning (false);
                    if (goToCursor_pcl != null) {
                        TopManager.getDefault ().getDebugger ().removePropertyChangeListener (goToCursor_pcl);
                        goToCursor_pcl = null;
                        goToCursor_breakpoint.remove ();
                    }
                }
                else
                    goToCursor_pcl = null;
            } catch (org.openide.debugger.DebuggerException e) {
                notifyDebuggerException (e);
            }
        }

    } // end of StartDebugThread
}

/*
 * Log
 *  29   Gandalf   1.28        1/20/00  Daniel Prusa    a small correction
 *  28   Gandalf   1.27        1/20/00  Daniel Prusa    bugfix for Goto Cursor
 *  27   Gandalf   1.26        1/18/00  Daniel Prusa    StartDebugger action
 *  26   Gandalf   1.25        1/13/00  Ian Formanek    I18N
 *  25   Gandalf   1.24        1/12/00  Ian Formanek    NOI18N
 *  24   Gandalf   1.23        1/3/00   Daniel Prusa    small correction of 
 *       previous Check in
 *  23   Gandalf   1.22        12/30/99 Daniel Prusa    GoToCursorAction 
 *       implemented
 *  22   Gandalf   1.21        12/23/99 Daniel Prusa    Bugfixes for #4863, 
 *       #3918
 *  21   Gandalf   1.20        11/30/99 Ales Novak      check whether compile or
 *       not
 *  20   Gandalf   1.19        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  19   Gandalf   1.18        9/22/99  Jan Jancura     Switch desktopsin the 
 *       AWT thread
 *  18   Gandalf   1.17        8/2/99   Jan Jancura     Switch desktop after 
 *       compiling
 *  17   Gandalf   1.16        7/24/99  Jan Jancura     Bug in sospending 
 *       threads.
 *  16   Gandalf   1.15        7/11/99  David Simonek   window system change...
 *  15   Gandalf   1.14        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  14   Gandalf   1.13        5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  13   Gandalf   1.12        5/14/99  Ales Novak      bugfix for #1667 #1598 
 *       #1625
 *  12   Gandalf   1.11        3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  11   Gandalf   1.10        3/25/99  Ales Novak      
 *  10   Gandalf   1.9         3/16/99  Jan Jancura     
 *  9    Gandalf   1.8         3/11/99  Jan Jancura     
 *  8    Gandalf   1.7         3/10/99  Jan Jancura     
 *  7    Gandalf   1.6         3/9/99   Jan Jancura     Debugger actions updated
 *  6    Gandalf   1.5         3/4/99   Jan Jancura     Localization moved
 *  5    Gandalf   1.4         2/26/99  Jaroslav Tulach To compile after Open 
 *       API changes in debugger
 *  4    Gandalf   1.3         1/20/99  Jaroslav Tulach 
 *  3    Gandalf   1.2         1/20/99  Jaroslav Tulach DebuggerPerform not 
 *       public  
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
