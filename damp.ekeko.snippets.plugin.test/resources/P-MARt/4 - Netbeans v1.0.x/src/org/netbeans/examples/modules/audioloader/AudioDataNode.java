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

package org.netbeans.examples.modules.audioloader;
import org.openide.actions.ViewAction;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
public class AudioDataNode extends DataNode {
    public AudioDataNode (AudioDataObject obj) {
        super (obj, Children.LEAF);
        setIconBase ("/org/netbeans/examples/modules/audioloader/audioIcon");
    }
    /*
    protected Sheet createSheet () {
      Sheet sheet = super.createSheet ();
      Sheet.Set set = sheet.get (ExecSupport.PROP_EXECUTION);
      if (set == null) {
        set = new Sheet.Set ();
        set.setName (ExecSupport.PROP_EXECUTION);
        set.setDisplayName (NbBundle.getBundle (AudioDataNode.class).getString ("displayNameForAudioDataNodeExecSheet"));
        set.setShortDescription (NbBundle.getBundle (AudioDataNode.class).getString ("hintForAudioDataNodeExecSheet"));
      }
      ((ExecSupport) getCookie (ExecSupport.class)).addProperties (set);
      sheet.put (set);
      return sheet;
}
    */
    public SystemAction getDefaultAction () {
        return SystemAction.get (ViewAction.class);
    }
}
