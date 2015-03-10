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

package org.netbeans.modules.sysprops;
import java.io.IOException;
import java.util.ResourceBundle;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
public class AllPropsNode extends AbstractNode {
    private static ResourceBundle bundle = NbBundle.getBundle (AllPropsNode.class);
    public AllPropsNode () {
        super (new AllPropsChildren ());
        setIconBase ("/org/netbeans/modules/sysprops/allPropsIcon");
        setName ("AllPropsNode");
        setDisplayName (bundle.getString ("LBL_AllPropsNode"));
        setShortDescription (bundle.getString ("HINT_AllPropsNode"));
    }
    protected SystemAction[] createActions () {
        return new SystemAction[] {
                   SystemAction.get (RefreshPropsAction.class),
                   null,
                   SystemAction.get (OpenLocalExplorerAction.class),
                   null,
                   SystemAction.get (NewAction.class),
                   null,
                   SystemAction.get (ToolsAction.class),
                   SystemAction.get (PropertiesAction.class),
               };
    }
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.sysprops");
    }
    public Node cloneNode () {
        return new AllPropsNode ();
    }
    public NewType[] getNewTypes () {
        return new NewType[] { new NewType () {
                                   public String getName () {
                                       return bundle.getString ("LBL_NewProp");
                                   }
                                   public HelpCtx getHelpCtx () {
                                       return new HelpCtx ("org.netbeans.modules.sysprops");
                                   }
                                   public void create () throws IOException {
                                       String title = bundle.getString ("LBL_NewProp_dialog");
                                       String msg = bundle.getString ("MSG_NewProp_dialog_key");
                                       NotifyDescriptor.InputLine desc = new NotifyDescriptor.InputLine (msg, title);
                                       TopManager.getDefault ().notify (desc);
                                       String key = desc.getInputText ();
                                       if ("".equals (key)) return;
                                       msg = bundle.getString ("MSG_NewProp_dialog_value");
                                       desc = new NotifyDescriptor.InputLine (msg, title);
                                       TopManager.getDefault ().notify (desc);
                                       String value = desc.getInputText ();
                                       System.setProperty (key, value);
                                       PropertiesNotifier.changed ();
                                   }
                               } };
    }
}
