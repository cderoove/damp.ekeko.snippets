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
import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.execution.*;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.loaders.DataObject;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.UserCancelException;
import org.openide.util.io.FoldingIOException;
import org.openide.windows.InputOutput;

public class CompilerTypeTester extends Tester {

    static Compiler.Depth[] depths = new Compiler.Depth[] {
                                         Compiler.DEPTH_ZERO,
                                         Compiler.DEPTH_ONE,
                                         Compiler.DEPTH_INFINITE
                                     };

    private Class cookie = CompilerCookie.Compile.class;
    private static final long serialVersionUID =-4792677176257850023L;
    public Class getCookie () {
        return cookie;
    }
    public synchronized void setCookie (Class nue) {
        Class old = cookie;
        cookie = nue;
        firePropertyChange ("cookie", old, nue);
    }

    private int depth = 1; // Compiler.Depth is not serializable
    public int getDepth () {
        return depth;
    }
    public synchronized void setDepth (int nue) {
        int old = depth;
        depth = nue;
        firePropertyChange ("depth", new Integer (old), new Integer (nue));
    }

    // [PENDING] property for the data object found in Repo

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.testers.CompilerType");
    }

    protected void checkClass (Class clazz) throws IOException {
        try {
            CompilerType ct = (CompilerType) clazz.newInstance ();
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            throw new FoldingIOException (t);
        }
    }

    protected void executeClass (Class clazz, String[] args) {
        try {
            CompilerType ct = (CompilerType) clazz.newInstance ();
            PropertySheet sheet = new PropertySheet ();
            sheet.setNodes (new Node[] { new BeanNode (ct) });
            TopManager.getDefault ().notify (new NotifyDescriptor.Message (sheet, NotifyDescriptor.PLAIN_MESSAGE));
            // [PENDING] would be good to provide a data filter here
            DataObject toTest = (DataObject) TopManager.getDefault ().getNodeOperation ().select
                                ("Select a class to test " + clazz.getName () + " with...",
                                 "Repository", TopManager.getDefault ().getPlaces ().nodes ().repository (),
                                 new NodeAcceptor () {
                                     public boolean acceptNodes (Node[] nodes) {
                                         return nodes.length == 1 &&
                                                nodes[0].getCookie (DataObject.class) != null &&
                                                nodes[0].getCookie (CompilerCookie.class) != null;
                                     }
                                 })[0].getCookie (DataObject.class);
            Class search;
            Method m = null;
            for (search = clazz; ! search.equals (ServiceType.class); search = search.getSuperclass ()) {
                try {
                    m = search.getDeclaredMethod ("prepareJob", new Class[] { CompilerJob.class, Class.class, DataObject.class });
                    break;
                } catch (NoSuchMethodException nsme) {
                }
            }
            CompilerJob job = new CompilerJob (depths[getDepth ()]);
            m.setAccessible (true);
            try {
                m.invoke (ct, new Object[] { job, getCookie (), toTest });
            } finally {
                m.setAccessible (false);
            }
            if (job.isUpToDate ()) {
                System.err.println ("Job was already up to date.");
            } else {
                System.err.println ("Starting job...");
                if (job.start ().isSuccessful ())
                    System.err.println ("Successful.");
                else
                    System.err.println ("Unsuccessful.");
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
 *  4    Gandalf-post-FCS1.2.1.0     3/28/00  Jesse Glick     SVUIDs.
 *  3    Gandalf   1.2         1/26/00  Jesse Glick     Executor display names 
 *       can just be taken from bean descriptor.
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         10/13/99 Jesse Glick     
 * $
 */
