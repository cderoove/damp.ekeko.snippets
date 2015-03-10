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

package org.netbeans.modules.apisupport;

import java.io.IOException;
import java.lang.reflect.*;

import org.openide.TopManager;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.util.io.FoldingIOException;
import org.openide.windows.InputOutput;

public class SystemActionTester extends Tester {

    private static final long serialVersionUID =-1835218579791857931L;
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.testers.SystemAction");
    }

    protected void checkClass (Class clazz) throws IOException {
        try {
            SystemAction action = (SystemAction) clazz.newInstance ();
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            throw new FoldingIOException (t);
        }
    }

    protected void executeClass (Class clazz, String[] args) {
        try {
            SystemAction action = (SystemAction) clazz.newInstance ();
            System.err.println ("Will run " + clazz.getName () + " ...");
            if (action instanceof NodeAction) {
                NodeAction na = (NodeAction) action;
                Class search;
                Method m1 = null;
                for (search = clazz; ! search.equals (CallableSystemAction.class); search = search.getSuperclass ()) {
                    try {
                        m1 = search.getDeclaredMethod ("enable", new Class[] { Node[].class });
                        break;
                    } catch (NoSuchMethodException nsme) {
                    }
                }
                m1.setAccessible (true);
                try {
                    Method m2 = null;
                    for (search = clazz; ! search.equals (CallableSystemAction.class); search = search.getSuperclass ()) {
                        try {
                            m2 = search.getDeclaredMethod ("performAction", new Class[] { Node[].class });
                            break;
                        } catch (NoSuchMethodException nsme) {
                        }
                    }
                    m2.setAccessible (true);
                    try {
                        final NodeAction na_ = na;
                        final Method m1_ = m1;
                        Node[] selection = TopManager.getDefault ().getNodeOperation ().select
                                           ("Select some nodes for " + clazz.getName () + " ...", "Desktop",
                                            TopManager.getDefault ().getPlaces ().nodes ().projectDesktop (),
                                            new NodeAcceptor () {
                                                public boolean acceptNodes (Node[] nodes) {
                                                    try {
                                                        return ((Boolean) m1_.invoke (na_, new Object[] { nodes })).booleanValue ();
                                                    } catch (Exception e) {
                                                        e.printStackTrace ();
                                                        return false;
                                                    }
                                                }
                                            });
                        m2.invoke (na, new Object[] { selection });
                    } finally {
                        m2.setAccessible (false);
                    }
                } finally {
                    m1.setAccessible (false);
                }
            } else if (action instanceof CallableSystemAction) {
                ((CallableSystemAction) action).performAction ();
            } else {
                action.actionPerformed (null);
            }
            System.err.println ("Action finished.");
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            t.printStackTrace ();
        }
    }

}

/*
 * Log
 *  10   Gandalf-post-FCS1.8.1.0     3/28/00  Jesse Glick     SVUIDs.
 *  9    Gandalf   1.8         1/26/00  Jesse Glick     Executor display names 
 *       can just be taken from bean descriptor.
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/13/99 Jesse Glick     Various fixes and 
 *       enhancements:    - added a Changes.txt    - fixed handling of 
 *       OpenAPIs.zip on install/uninstall (previously did not correctly unmount
 *       on uninstall, nor check for already-mounted on install)    - added a 
 *       CompilerTypeTester    - display name & icon updates from Tim    - 
 *       removed link to ToDo.txt from docs page    - various BeanInfo's, both 
 *       in templates and in the support itself, did not display superclass 
 *       BeanInfo correctly    - ExecutorTester now permits user to customize 
 *       new executor instance before running it
 *  6    Gandalf   1.5         10/7/99  Jesse Glick     Service name changes.
 *  5    Gandalf   1.4         10/6/99  Jesse Glick     Added table of contents,
 *       anchored context help.
 *  4    Gandalf   1.3         10/5/99  Jesse Glick     Sundry API changes 
 *       affecting me.
 *  3    Gandalf   1.2         9/30/99  Jesse Glick     Package rename and misc.
 *  2    Gandalf   1.1         9/20/99  Jesse Glick     Fixed output from 
 *       testers; now has correct classloader.
 *  1    Gandalf   1.0         9/12/99  Jesse Glick     
 * $
 */
