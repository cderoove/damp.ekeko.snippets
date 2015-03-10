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

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.TopManager;

public class APISettings extends SystemOption {

    public static final String PROP_USENBBUNDLE = "useNbBundle";

    private static final String NBBUNDLE_INITSTRING = "org.openide.util.NbBundle.getBundle({3}.class).getString(\"{2}\")";
    private static final String DEFAULT_INITSTRING = "java.util.ResourceBundle.getBundle(\"{0}\").getString(\"{2}\")";

    private static final long serialVersionUID =-3312041815988660063L;
    public String displayName () {
        return "API Support";
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.utils");
    }

    public boolean isUseNbBundle () {
        try {
            Class cl = Class.forName ("org.netbeans.modules.properties.ResourceBundleStringEditor");
            Field fl = cl.getField ("javaStringFormat");
            return NBBUNDLE_INITSTRING.equals (fl.get (null));
        } catch (Exception e) {
            //TopManager.getDefault().notifyException(e);
            return false;
        }
    }

    public void setUseNbBundle (boolean useNbBundle) {
        try {
            Class cl = Class.forName ("org.netbeans.modules.properties.ResourceBundleStringEditor");
            Field fl = cl.getField ("javaStringFormat");
            fl.set (null, useNbBundle ? NBBUNDLE_INITSTRING : DEFAULT_INITSTRING);
            firePropertyChange (PROP_USENBBUNDLE, new Boolean (! useNbBundle), new Boolean (useNbBundle));
        } catch (Exception e) {
            //TopManager.getDefault().notifyException(e);
        }
    }

}

/*
 * Log
 *  3    Gandalf-post-FCS1.1.1.0     3/28/00  Jesse Glick     SVUIDs.
 *  2    Gandalf   1.1         11/10/99 Jesse Glick     Restart action removed; 
 *       NbBundle use off by default.
 *  1    Gandalf   1.0         10/27/99 Jesse Glick     
 * $
 */
