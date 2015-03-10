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
import java.lang.reflect.Method;

import org.openide.*;
import org.openide.cookies.*;
import org.openide.execution.*;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.loaders.DataObject;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.UserCancelException;
import org.openide.util.io.FoldingIOException;
import org.openide.windows.InputOutput;

public class ExecutorTester extends Tester {

    private String testableClass = null;
    private String[] testableArgs = new String[] { };

    private static final long serialVersionUID =7386740499134987565L;
    public String getTestableClass () {
        return testableClass;
    }

    public synchronized void setTestableClass (String nue) {
        if (nue != null && nue.equals ("")) nue = null;
        String old = testableClass;
        testableClass = nue;
        firePropertyChange ("testableClass", old, nue);
    }

    public String[] getTestableArgs () {
        return testableArgs;
    }

    public synchronized void setTestableArgs (String[] nue) {
        String[] old = testableArgs;
        testableArgs = nue;
        firePropertyChange ("testableArgs", old, nue);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.testers.Executor");
    }

    protected void checkClass (Class clazz) throws IOException {
        try {
            Executor exec = (Executor) clazz.newInstance ();
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            throw new FoldingIOException (t);
        }
    }

    protected void executeClass (Class clazz, String[] args) {
        try {
            Executor exec = (Executor) clazz.newInstance ();
            PropertySheet sheet = new PropertySheet ();
            sheet.setNodes (new Node[] { new BeanNode (exec) });
            TopManager.getDefault ().notify (new NotifyDescriptor.Message (sheet, NotifyDescriptor.PLAIN_MESSAGE));
            ExecInfo testInfo;
            if (testableClass == null) {
                DataObject toTest = (DataObject) TopManager.getDefault ().getNodeOperation ().select
                                    ("Select a class to test " + clazz.getName () + " with...",
                                     "Repository", TopManager.getDefault ().getPlaces ().nodes ().repository (),
                                     new NodeAcceptor () {
                                         public boolean acceptNodes (Node[] nodes) {
                                             return nodes.length == 1 &&
                                                    nodes[0].getCookie (DataObject.class) != null &&
                                                    nodes[0].getCookie (ExecCookie.class) != null &&
                                                    nodes[0].getCookie (ArgumentsCookie.class) != null;
                                         }
                                     })[0].getCookie (DataObject.class);
                testInfo = new ExecInfo (toTest.getPrimaryFile ().getPackageName ('.'),
                                         ((ArgumentsCookie) toTest.getCookie (ArgumentsCookie.class)).getArguments ());
            } else {
                testInfo = new ExecInfo (testableClass, testableArgs);
            }
            Class search;
            Method m = null;
            for (search = clazz; ! search.equals (ServiceType.class); search = search.getSuperclass ()) {
                try {
                    m = search.getDeclaredMethod ("execute", new Class[] { ExecInfo.class });
                    break;
                } catch (NoSuchMethodException nsme) {
                }
            }
            m.setAccessible (true);
            try {
                int result = ((ExecutorTask) m.invoke (exec, new Object[] { testInfo })).result ();
                // [PENDING] for some reason, this appears on console, not on O.W.
                System.err.println ("Exit status: " + result);
            } finally {
                m.setAccessible (false);
            }
        } catch (ThreadDeath td) {
            throw td;
        } catch (UserCancelException uce) {
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
 *  2    Gandalf   1.1         9/24/99  Jesse Glick     setName() public.
 *  1    Gandalf   1.0         9/20/99  Jesse Glick     
 * $
 */
