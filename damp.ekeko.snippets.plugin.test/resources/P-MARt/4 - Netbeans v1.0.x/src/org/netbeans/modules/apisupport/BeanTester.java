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

import java.lang.reflect.*;
import java.io.IOException;

import org.openide.TopManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.io.FoldingIOException;

import org.netbeans.modules.apisupport.beanbrowser.PropSetKids;

public class BeanTester extends Tester {

    private static final long serialVersionUID =-4004964495171713724L;
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.testers.Bean");
    }

    protected void checkClass (Class clazz) throws IOException {
        if (! Modifier.isPublic (clazz.getModifiers ()))
            throw new IOException ("Class is not public");
        try {
            Constructor c = clazz.getConstructor (new Class[] { });
            if (! Modifier.isPublic (c.getModifiers ()))
                throw new IOException ("Constructor is not public");
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            throw new FoldingIOException (t);
        }
    }

    protected void executeClass (Class clazz, String[] args) {
        try {
            Object o = clazz.newInstance ();
            Node n = PropSetKids.makeObjectNode (o);
            TopManager.getDefault ().getNodeOperation ().explore (n);
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            t.printStackTrace ();
        }
    }

}

/*
 * Log
 *  5    Gandalf-post-FCS1.3.1.0     3/28/00  Jesse Glick     SVUIDs.
 *  4    Gandalf   1.3         1/26/00  Jesse Glick     Executor display names 
 *       can just be taken from bean descriptor.
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/13/99 Jesse Glick     Various fixes and 
 *       enhancements:    - added a Changes.txt    - fixed handling of 
 *       OpenAPIs.zip on install/uninstall (previously did not correctly unmount
 *       on uninstall, nor check for already-mounted on install)    - added a 
 *       CompilerTypeTester    - display name & icon updates from Tim    - 
 *       removed link to ToDo.txt from docs page    - various BeanInfo's, both 
 *       in templates and in the support itself, did not display superclass 
 *       BeanInfo correctly    - ExecutorTester now permits user to customize 
 *       new executor instance before running it
 *  1    Gandalf   1.0         10/7/99  Jesse Glick     
 * $
 */
