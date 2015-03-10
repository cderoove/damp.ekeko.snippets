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

package org.netbeans.modules.emacs;

import java.awt.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class EmacsCaret extends DefaultCaret implements Protocol {

    private transient EmacsProxier proxy = null;

    private transient EmacsListener elist = new EmacsListener () {
                                                public void callback (EmacsEvent ev) {
                                                    if (EVT_newDotAndMark.equals (ev.getType ())) {
                                                        if (Connection.DEBUG) System.err.println("EmacsCaret.elist");
                                                        Object[] args = ev.getArgs ();
                                                        if (args.length != 2) throw new RuntimeException ();
                                                        final int dot = ((Integer) args[0]).intValue ();
                                                        final int mark = ((Integer) args[1]).intValue ();
                                                        SwingUtilities.invokeLater (new Runnable () {
                                                                                        public void run () {
                                                                                            if (Connection.DEBUG) System.err.println("EmacsCaret.elist II");
                                                                                            setDotSuper (mark);
                                                                                            if (dot != mark)
                                                                                                moveDotSuper (dot);
                                                                                        }
                                                                                    });
                                                    }
                                                }
                                            };

    EmacsCaret () {
        if (Connection.DEBUG) System.err.println("Creating " + this);
    }
    protected void finalize () throws Throwable {
        if (Connection.DEBUG) System.err.println("Destroying " + this);
        if (proxy != null) {
            // XXX this might not be right, if there were >1 carets on same doc
            proxy.call (CMD_stopCaretListen);
            proxy = null;
        }
        super.finalize ();
    }
    public String toString () {
        return "EmacsCaret[" + proxy + "]";
    }

    /*
    private static long lastRefreshed = 0;
    private void refresh () {
      if (Connection.DEBUG) System.err.println("EmacsCaret.refresh");
      if (System.currentTimeMillis () - lastRefreshed < 500L) {
        if (Connection.DEBUG) System.err.println("skipping refresh, too soon");
        return;
      }
      final int mark = getMarkRemote ();
      final int dot = getDotRemote ();
      lastRefreshed = System.currentTimeMillis ();
      SwingUtilities.invokeLater (new Runnable () {
        public void run () {
          if (Connection.DEBUG) System.err.println("EmacsCaret.refresh II");
          setDotSuper (mark);
          if (dot != mark)
            moveDotSuper (dot);
        }
      });
}
    */
    // XXX compiler hax:
    private void setDotSuper (int dot) {
        super.setDot (dot);
    }
    private void moveDotSuper (int dot) {
        super.moveDot (dot);
    }

    private void refreshOtherWay () {
        if (Connection.DEBUG) System.err.println("EmacsCaret.refreshOtherWay");
        if (proxy == null) {
            if (Connection.DEBUG) System.err.println("skipping due to proxy==null");
            return;
        }
        proxy.call (CMD_setMark, new Object[] { new Integer (getMark ()) });
        proxy.call (CMD_setDot, new Object[] { new Integer (getDot ()) });
    }

    /*
    private int getDotRemote () {
      if (proxy == null) {
        if (Connection.DEBUG) System.err.println("skipping getDotRemote due to proxy==null");
        return 0;
      }
      Object[] result = proxy.function (FUN_getDot);
      if (result.length != 1) throw new RuntimeException ();
      return ((Integer) result[0]).intValue ();
}

    private int getMarkRemote () {
      if (proxy == null) {
        if (Connection.DEBUG) System.err.println("skipping getMarkRemote due to proxy==null");
        return 0;
      }
      Object[] result = proxy.function (FUN_getMark);
      if (result.length != 1) throw new RuntimeException ();
      return ((Integer) result[0]).intValue ();
}
    */

    // XXX should not be necessary, but the listener is disabled
    // since it has unpredictable interactions with the DefaultCaret DocumentListener
    public void setDot (int dot) {
        super.setDot (dot);
        refreshOtherWay ();
    }
    public void moveDot (int dot) {
        super.moveDot (dot);
        refreshOtherWay ();
    }

    public void install (JTextComponent c) {
        super.install (c);
        if (Connection.DEBUG) System.err.println("installing " + this + " into " + c);
        if (c instanceof JEditorPane) {
            EditorKit kit = ((JEditorPane) c).getEditorKit ();
            if (kit != null && kit instanceof EmacsKit) {
                proxy = ((EmacsKit) kit).getProxy ();
                if (proxy == null) {
                    if (Connection.DEBUG) System.err.println("Skipping since proxy == null");
                    return;
                }
                proxy.addEmacsListener (elist);
                proxy.call (CMD_startCaretListen);
                addChangeListener (new ChangeListener () {
                                       public void stateChanged (ChangeEvent ev) {
                                           if (Connection.DEBUG) System.err.println("EmacsCaret.stateChanged");
                                           SwingUtilities.invokeLater (new Runnable () {
                                                                           public void run () {
                                                                               // Compiler hax
                                                                               refreshOtherWay0 ();
                                                                           }
                                                                       });
                                       }
                                       private void refreshOtherWay0 () {
                                           // XXX currently disabled:
                                           //refreshOtherWay ();
                                       }
                                   });
            }
        }
    }

    public void deinstall (JTextComponent c) {
        if (Connection.DEBUG) System.err.println("uninstalling " + this + " from " + c);
        if (proxy != null) {
            // XXX should maybe count uses, like E.D. used to do?
            proxy.call (CMD_stopCaretListen);
            proxy.removeEmacsListener (elist);
            proxy = null;
        }
        super.deinstall (c);
    }

}
