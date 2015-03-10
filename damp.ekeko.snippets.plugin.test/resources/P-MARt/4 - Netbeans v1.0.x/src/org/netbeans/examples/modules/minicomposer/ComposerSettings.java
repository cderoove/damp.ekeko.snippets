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

package org.netbeans.examples.modules.minicomposer;
import org.openide.ServiceType;
import org.openide.execution.Executor;
import org.openide.options.SystemOption;
import org.openide.util.*;
public class ComposerSettings extends SystemOption {
    public static final String PROP_player = "player";
    public static final String PROP_sampleRate = "sampleRate";
    private static final long serialVersionUID =-1247005365478408406L;
    protected void initialize () {
        if (Utilities.isWindows () || Utilities.OS_SOLARIS == Utilities.getOperatingSystem ()) {
            setPlayer (Executor.find (InternalPlayer.class));
        } else {
            setPlayer (Executor.find (ExternalPlayer.class));
        }
        setSampleRate (24000.0f);
    }
    public String displayName () {
        return NbBundle.getBundle (ComposerSettings.class).getString ("LBL_ComposerSettings");
    }
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.examples.modules.minicomposer");
    }
    static final ComposerSettings DEFAULT = (ComposerSettings) findObject (ComposerSettings.class, true);
    public Executor getPlayer () {
        return (Executor) ((ServiceType.Handle) getProperty (PROP_player)).getServiceType ();
    }
    public void setPlayer (Executor player) {
        putProperty (PROP_player, new ServiceType.Handle (player), true);
    }
    public float getSampleRate () {
        return ((Float) getProperty (PROP_sampleRate)).floatValue ();
    }
    public void setSampleRate (float sampleRate) {
        putProperty (PROP_sampleRate, new Float (sampleRate), true);
    }
}
