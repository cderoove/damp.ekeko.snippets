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

package org.netbeans.modules.debugger.debug;

import sun.tools.debug.*;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.util.ResourceBundle;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.debugger.Debugger;
import org.openide.debugger.DebuggerException;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.text.Line;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.DebuggerSettings;
import org.netbeans.modules.debugger.support.ActionTIPanel;
import org.netbeans.modules.debugger.support.util.*;

/**
* Debugger callback class.
*
* @author   Jan Jancura
*/
class ToolsCallback implements DebuggerCallback {

    private boolean                   printLn = false;
    private boolean                   internalErrorReported = false;
    private ToolsDebugger             debugger;

    private boolean                   stopOnMainReached = false;

    /**
    * Create the new callback for given debugger instance.
    */
    ToolsCallback (ToolsDebugger debugger) {
        this.debugger= debugger;
    }

    /**
    * Implementation of dprint event.
    */
    public void printToConsole (final String text) {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            if (text.startsWith ("[Internal debug-agent exception")) { // NOI18N
                                                internalErrorReported = true;
                                                return;
                                            }
                                            if (internalErrorReported && (text.equals ("\r\n")||text.equals ("\n"))) { // NOI18N
                                                internalErrorReported = false;
                                                return;
                                            }
                                            internalErrorReported = false;
                                            debugger.print (text, debugger.STD_OUT);
                                        } // run ()
                                    }); // invokeLater
    }

    /**
    * Implementation of breakpoint event.
    * Tests if on the current line is some breakpoint and calls CoreBreakpoint.Event.perform.
    * If not, writes message to output, calls TheThread.setCurrent (true) and 
    * updates state of threads, breakpoints...
    */
    public void breakpointEvent (final RemoteThread t) {
        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              final ResourceBundle bundle = org.openide.util.NbBundle.getBundle (ToolsCallback.class);
                                              Object[] v = null;
                                              // unsafe questions first...
                                              try {
                                                  v = (Object[]) new Protector ("ToolsCallback.breakpointEvent") { // NOI18N
                                                          public Object protect () throws Exception {
                                                              Object[] r = new Object [6];
                                                              RemoteStackFrame fr = t.getCurrentFrame ();
                                                              r [0] = fr;
                                                              r [1] = fr.getRemoteClass ().getName ();
                                                              r [2] = new Integer (fr.getLineNumber ());
                                                              r [3] = fr.getMethodName ();
                                                              r [4] = t.getName ();
                                                              r [5] = new Integer (t.dumpStack ().length);
                                                              return r;
                                                          }
                                                      }.throwAndWait (debugger.synchronizer, debugger.killer);

                                              } catch (Throwable e) {
                                                  if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                                                  debugger.println (bundle.getString ("EXC_Debugger") + ": " + e, debugger.ERR_OUT);
                                                  return;
                                              }

                                              // read answers
                                              final RemoteStackFrame fr = (RemoteStackFrame) v [0];
                                              String className = (String) v [1];
                                              final int line = ((Integer) v [2]).intValue ();
                                              String methodName = (String) v [3];
                                              final int stackDepth = ((Integer) v [5]).intValue ();

                                              // get lastStackDepth & lastAction
                                              final int lastStackDepth;
                                              final int lastAction;
                                              final ToolsThread ttt = ((ToolsThreadGroup) debugger.getThreadGroupRoot ()).getThread (t);
                                              if (ttt != null) {
                                                  lastStackDepth = ttt.getLastStackDepth ();
                                                  lastAction = ttt.getLastAction ();
                                              }
                                              else {
                                                  lastStackDepth = stackDepth + 1;
                                                  if (!stopOnMainReached && debugger.stopOnMainFlag)
                                                      lastAction = debugger.ACTION_TRACE_INTO;
                                                  else
                                                      lastAction = debugger.ACTION_START;
                                                  stopOnMainReached = true;
                                              }

                                              // the folloving condition tests if a 'step action' has been finished just now
                                              // its PATCH => we can to ignore breakpoints in this case
                                              if (!( (lastAction == debugger.ACTION_TRACE_INTO) ||
                                                      ( (lastAction == debugger.ACTION_TRACE_OVER) &&
                                                        (stackDepth <= lastStackDepth)
                                                      ) ||
                                                      ( (lastAction == debugger.ACTION_STEP_OUT) &&
                                                        (stackDepth < lastStackDepth)
                                                      )
                                                   )
                                                 ) {
                                                  // Last action is GO || somma TRACE is not finished right now
                                                  // is it breakpoint?!?
                                                  if (resolveAsBreakpoint (
                                                              t,
                                                              fr,
                                                              className,
                                                              methodName,
                                                              line,
                                                              lastAction,
                                                              stackDepth,
                                                              lastStackDepth
                                                          )) return;
                                              }

                                              // no breakpoint
                                              // test if Trace over or Step out has not been interrupted by breakpoint with suspend=false
                                              if ( ( (lastAction == debugger.ACTION_TRACE_OVER) &&
                                                      (lastStackDepth < stackDepth)
                                                   ) ||
                                                      ( (lastAction == debugger.ACTION_STEP_OUT) &&
                                                        (stackDepth > 1) &&
                                                        (lastStackDepth <= stackDepth)
                                                      )
                                                 ) {
                                                  new Protector ("ToolsCallback.stepOut") { // NOI18N
                                                      public Object protect () throws Exception {
                                                          t.stepOut ();
                                                          return null;
                                                      }
                                                  }.go (debugger.synchronizer, debugger.killer);
                                                  return;
                                              }

                                              // not breakpoint -> stop
                                              debugger.setDebuggerState (debugger.DEBUGGER_STOPPED);

                                              final String all = className + "." + v [3]; // NOI18N
                                              final String threadName = (String) v [4];

                                              SwingUtilities.invokeLater (new Runnable () {
                                                                              public void run () {
                                                                                  // show message
                                                                                  if (debugger.isFollowedByEditor ()) {
                                                                                      Line l = debugger.getLine (fr);
                                                                                      if (l != null) {
                                                                                          if (resolveCanBeCurrent (
                                                                                                      t,
                                                                                                      false,
                                                                                                      l
                                                                                                  )) return;
                                                                                          debugger.println (
                                                                                              bundle.getString ("CTL_Thread") + " " + threadName +
                                                                                              " " + bundle.getString ("CTL_stopped_at") + " " + all + " " +
                                                                                              bundle.getString ("CTL_line") + " " + line + ".",
                                                                                              debugger.ERR_OUT + debugger.STL_OUT
                                                                                          );
                                                                                      } else {
                                                                                          DebuggerSettings debuggerSettings = (DebuggerSettings) DebuggerSettings.
                                                                                                                              findObject (DebuggerSettings.class);
                                                                                          if (!debuggerSettings.isActionOnTraceIntoSet ()) {
                                                                                              // ask user, what to do ...
                                                                                              ActionTIPanel ap = new ActionTIPanel ();
                                                                                              NotifyDescriptor.Confirmation mess = new NotifyDescriptor.Confirmation (
                                                                                                                                       ap,
                                                                                                                                       NotifyDescriptor.OK_CANCEL_OPTION
                                                                                                                                   );
                                                                                              Object o = TopManager.getDefault ().notify (mess);
                                                                                              if (o.equals (NotifyDescriptor.OK_OPTION))
                                                                                                  ap.updateSettings ();
                                                                                          }
                                                                                          if (debuggerSettings.getActionOnTraceInto () !=
                                                                                                  DebuggerSettings.ACTION_ON_TI_STOP
                                                                                             ) {
                                                                                              try {
                                                                                                  debugger.stepOut ();
                                                                                              } catch (DebuggerException e) {
                                                                                              }
                                                                                              return;
                                                                                          }
                                                                                          debugger.println (
                                                                                              bundle.getString ("CTL_Thread") + " " + threadName +
                                                                                              " " + bundle.getString ("CTL_stopped_at") + " " + all + " " +
                                                                                              bundle.getString ("CTL_line") + " " + line +
                                                                                              " - " + bundle.getString ("CTL_unavailable_source_file") + ".",
                                                                                              debugger.ERR_OUT + debugger.STL_OUT
                                                                                          );
                                                                                      }
                                                                                  } else
                                                                                      debugger.println (
                                                                                          bundle.getString ("CTL_Thread") + " " + threadName +
                                                                                          " " + bundle.getString ("CTL_stopped_at") + " " + all + " " +
                                                                                          bundle.getString ("CTL_line") + " " + line + ".",
                                                                                          debugger.ERR_OUT + debugger.STL_OUT
                                                                                      );

                                                                                  // refresh all
                                                                                  try {
                                                                                      debugger.threadGroup.threadChanged ();
                                                                                  } catch (Error e) {
                                                                                      if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                                                                                      debugger.println (bundle.getString ("EXC_Debugger") + ": " + e, debugger.ERR_OUT);
                                                                                      return;
                                                                                  }
                                                                                  ToolsThread tt = ((ToolsThreadGroup) debugger.getThreadGroupRoot ()).getThread (t);
                                                                                  if (tt != null)
                                                                                      tt.setCurrent (true);
                                                                                  else
                                                                                      debugger.lastCurrentThread = t;
                                                                                  debugger.updateWatches ();
                                                                              }
                                                                          });
                                          }
                                      }, 200);
    }

    /**
    * Implementation of exception event.
    */

    public void exceptionEvent (final RemoteThread t, final String errorText) {
        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              final ResourceBundle bundle = org.openide.util.NbBundle.getBundle (ToolsCallback.class);
                                              Object[] v = null;
                                              // unsafe questions first...
                                              try {
                                                  v = (Object[]) new Protector ("ToolsCallback.exceptionEvent") { // NOI18N
                                                          public Object protect () throws Exception {
                                                              Object[] r = new Object [6];
                                                              RemoteStackFrame fr = t.getCurrentFrame ();
                                                              r [0] = fr;
                                                              r [1] = fr.getRemoteClass ().getName ();
                                                              r [2] = new Integer (fr.getLineNumber ());
                                                              r [3] = fr.getMethodName ();
                                                              r [4] = t.getName ();
                                                              r [5] = new Integer (t.dumpStack ().length);
                                                              return r;
                                                          }
                                                      }.throwAndWait (debugger.synchronizer, debugger.killer);
                                              } catch (Throwable e) {
                                                  if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                                                  debugger.println (bundle.getString ("EXC_Debugger") + ": " + e, debugger.ERR_OUT);
                                                  return;
                                              }

                                              // read answers
                                              final RemoteStackFrame fr = (RemoteStackFrame) v [0];
                                              String className = (String) v [1];
                                              final int line = ((Integer) v [2]).intValue ();
                                              final String all = className + "." + v [3]; // NOI18N
                                              final String threadName = (String) v [4];
                                              final int stackDepth = ((Integer) v [5]).intValue ();

                                              final int lastStackDepth;
                                              final int lastAction;
                                              final ToolsThread ttt = ((ToolsThreadGroup) debugger.getThreadGroupRoot ()).getThread (t);
                                              if (ttt != null) {
                                                  lastStackDepth = ttt.getLastStackDepth ();
                                                  lastAction = ttt.getLastAction ();
                                              }
                                              else {
                                                  lastStackDepth = stackDepth + 1;
                                                  lastAction = debugger.getLastAction ();
                                              }

                                              // is there a breakpoint setted on thrown exception ?
                                              final String exClassName = Utils.getExceptionName (errorText);
                                              if (exClassName != null) {
                                                  try {
                                                      AbstractDebugger deb = (AbstractDebugger) TopManager.getDefault ().getDebugger ();
                                                      CoreBreakpoint [] b = deb.findBreakpoints (exClassName);
                                                      ExceptionBreakpoint br = null;
                                                      CoreBreakpoint.Event ev;
                                                      int i, k = b.length;
                                                      for (i = 0; i < k; i++) {
                                                          ev = b[i].getEvent (debugger);
                                                          if (ev instanceof ExceptionBreakpoint) {
                                                              br = (ExceptionBreakpoint) ev;
                                                              break;
                                                          }
                                                      }
                                                      if (br!=null) {
                                                          br.exceptionName = exClassName;
                                                          br.perform (t);
                                                          return;
                                                      }
                                                  }
                                                  catch (DebuggerNotFoundException e) {}
                                              }

                                              // breakpoint not found, default handling will be performed
                                              debugger.setDebuggerState (debugger.DEBUGGER_STOPPED);
                                              SwingUtilities.invokeLater (new Runnable () {
                                                                              public void run () {
                                                                                  // show message
                                                                                  String mes = new String (bundle.getString ("CTL_An_exception") +
                                                                                                           " " + bundle.getString ("CTL_reached_at") + " " + all + " " +
                                                                                                           bundle.getString ("CTL_line") + " " + line);
                                                                                  debugger.print (mes + ". " + errorText, debugger.ERR_OUT); // NOI18N
                                                                                  if (debugger.isFollowedByEditor ()) {
                                                                                      Line l = debugger.getLine (fr);
                                                                                      if (l != null) {
                                                                                          debugger.println (mes + ".", debugger.STL_OUT); // NOI18N
                                                                                      } else {
                                                                                          debugger.println (mes + " - " + bundle.getString ("CTL_unavailable_source_file") + ".", debugger.STL_OUT);
                                                                                      }
                                                                                  } else
                                                                                      debugger.println (mes + ".", debugger.STL_OUT); // NOI18N
                                                                                  // refresh all
                                                                                  try {
                                                                                      debugger.threadGroup.threadChanged ();
                                                                                  } catch (Error e) {
                                                                                      if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                                                                                      debugger.println (bundle.getString ("EXC_Debugger") + ": " + e, debugger.ERR_OUT);
                                                                                      return;
                                                                                  }
                                                                                  final ToolsThread tt = ((ToolsThreadGroup) debugger.getThreadGroupRoot ()).getThread (t);
                                                                                  if (tt != null)
                                                                                      tt.setCurrent (true);
                                                                                  else
                                                                                      debugger.lastCurrentThread = t;
                                                                                  debugger.updateWatches ();
                                                                              }
                                                                          });

                                          }
                                      }, 200);
    }

    /**
    * Implementation of thread death event.
    */
    public void threadDeathEvent (final RemoteThread t) {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            try {
                                                ResourceBundle bundle = org.openide.util.NbBundle.getBundle (ToolsCallback.class);
                                                debugger.println (
                                                    bundle.getString ("CTL_Thread_death_event") + ": " +
                                                    t.getName (),
                                                    debugger.ERR_OUT
                                                );
                                            } catch (Throwable e) {
                                                if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                                            }
                                        } // run ()
                                    }); // invokeLater
    }

    /**
    * Implementation of debugger finish event.
    */
    public void quitEvent () {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            try {
                                                ResourceBundle bundle = org.openide.util.NbBundle.getBundle (ToolsCallback.class);
                                                debugger.println (bundle.getString ("CTL_Process_death_event"), debugger.ERR_OUT);
                                                debugger.finishDebugger ();
                                            } catch (Throwable e) {
                                                if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                                            }
                                        } // run ()
                                    }); // invokeLater
    }


    // helper methods ............................................................

    /**
    * Tries to resolve as breakpoint.
    *
    * @return true if resolved (is on breakpoint or can not mark 
    * this line current).
    */
    private boolean resolveAsBreakpoint (
        RemoteThread t,
        RemoteStackFrame fr,
        String className,
        String methodName,
        int line,
        int lastAction,
        int stackDepth,
        int lastStackDepth
    ) {
        try {
            AbstractDebugger deb = (AbstractDebugger) TopManager.getDefault ().
                                   getDebugger ();
            CoreBreakpoint b = deb.findBreakpoint (className, line);
            if (b != null) {
                CoreBreakpoint.Event ev = b.getEvent (debugger);
                if (ev instanceof LineBreakpoint) {
                    Line l = debugger.getLine (fr);
                    if (resolveCanBeCurrent (
                                t,
                                true,
                                l
                            )) return true;
                    ((LineBreakpoint) ev).perform (t);
                    return true;
                }
            }
            if ( (lastAction != debugger.ACTION_TRACE_OVER) ||
                    (lastStackDepth != stackDepth)
               ) {
                b = deb.findBreakpoint (className, methodName);
                if (b != null) {
                    CoreBreakpoint.Event ev = b.getEvent (debugger);
                    if (ev instanceof MethodBreakpoint) {
                        Line l = debugger.getLine (fr);
                        if (resolveCanBeCurrent (
                                    t,
                                    true,
                                    l
                                )) return true;
                        ((MethodBreakpoint) ev).perform (t);
                        return true;
                    }
                }
            }
        } catch (DebuggerNotFoundException e) {
        }
        return false;
    }

    /**
    * Asks Line if it can be current. If it can not => stepOver.
    *
    * @returns true if is is resolved => if it can not be current 
    * => stepOver.
    */
    private boolean resolveCanBeCurrent (
        final RemoteThread t,
        boolean isBreakpoint,
        Line l
    ) {
        if (l == null) return false;
        if (!debugger.canBeCurrent (l, false)) {
            // this line cannot be marked as current (non-java
            // languages support)
            new Protector ("ToolsCallback.next") { // NOI18N
                public Object protect () throws Exception {
                    t.next ();
                    return null;
                }
            }.go (debugger.synchronizer, debugger.killer);
            return true;
        }
        return false;
    }
}

/*
* Log
*  20   Gandalf-post-FCS1.18.4.0    3/28/00  Daniel Prusa    
*  19   Gandalf   1.18        1/18/00  Jan Jancura     More buttons...
*  18   Gandalf   1.17        1/18/00  Jan Jancura     Dialog for action on TI 
*       settings.
*  17   Gandalf   1.16        1/17/00  Jan Jancura     Some propertie removed 
*       form DebugerSettings
*  16   Gandalf   1.15        1/13/00  Daniel Prusa    NOI18N
*  15   Gandalf   1.14        1/11/00  Daniel Prusa    bugfix for #5256 + 
*       additional filtering of [Internal debug-agent exception] message
*  14   Gandalf   1.13        12/9/99  Daniel Prusa    ExceptionBreakpoint
*  13   Gandalf   1.12        12/7/99  Daniel Prusa    added handling of 
*       breakpoint on method event
*  12   Gandalf   1.11        11/29/99 Jan Jancura     Bug 3341 - bad \n in 
*       output of debugger  Bug 3372 - do not print internal debugger 
*       messages...
*  11   Gandalf   1.10        11/8/99  Jan Jancura     Somma classes renamed
*  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  9    Gandalf   1.8         10/1/99  Jan Jancura     Current thread & bug 4108
*  8    Gandalf   1.7         7/30/99  Jan Jancura     
*  7    Gandalf   1.6         7/24/99  Jan Jancura     Bug in Suspend / resume 
*       thread in enterprise deb.
*  6    Gandalf   1.5         6/9/99   Jan Jancura     
*  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    Gandalf   1.3         6/4/99   Jan Jancura     
*  3    Gandalf   1.2         6/4/99   Jan Jancura     
*  2    Gandalf   1.1         6/4/99   Jan Jancura     
*  1    Gandalf   1.0         6/1/99   Jan Jancura     
* $
*/
