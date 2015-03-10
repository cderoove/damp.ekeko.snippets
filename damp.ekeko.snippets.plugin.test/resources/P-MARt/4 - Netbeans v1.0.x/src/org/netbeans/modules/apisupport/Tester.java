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

import java.beans.Introspector;
import java.io.IOException;

import org.openide.TopManager;
import org.openide.execution.ThreadExecutor;

/** Could currently be removed with no ill effects. Just a placeholder. */
public abstract class Tester extends ThreadExecutor {

    /** serialVersionUID */
    private static final long serialVersionUID = 6209720862927398377L;

    abstract protected void checkClass (Class clazz) throws IOException;
    abstract protected void executeClass (Class clazz, String[] params);

    // Overridden in ThreadExecutor, so restoring ServiceType impl:
    protected String displayName () {
        try {
            return Introspector.getBeanInfo (getClass ()).getBeanDescriptor ().getDisplayName ();
        } catch (Exception e) {
            // Catching IntrospectionException, but also maybe NullPointerException...?
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                e.printStackTrace ();
            return getClass ().getName ();
        }
    }

}

/*
 * Log
 *  9    Gandalf   1.8         1/26/00  Jesse Glick     Executor display names 
 *       can just be taken from bean descriptor.
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/6/99  Jesse Glick     Added table of contents,
 *       anchored context help.
 *  6    Gandalf   1.5         10/5/99  Jesse Glick     Sundry API changes 
 *       affecting me.
 *  5    Gandalf   1.4         9/30/99  Jesse Glick     Package rename and misc.
 *  4    Gandalf   1.3         9/20/99  Jesse Glick     Couple of minor fixes.
 *  3    Gandalf   1.2         9/20/99  Jesse Glick     Fixed output from 
 *       testers; now has correct classloader.
 *  2    Gandalf   1.1         9/14/99  Jesse Glick     Context help.
 *  1    Gandalf   1.0         9/12/99  Jesse Glick     
 * $
 */
