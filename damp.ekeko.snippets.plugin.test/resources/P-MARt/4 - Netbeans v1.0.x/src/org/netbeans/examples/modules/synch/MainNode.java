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

package org.netbeans.examples.modules.synch;
import org.openide.nodes.*;
import org.openide.util.datatransfer.*;
import org.openide.actions.*;
import org.openide.util.actions.*;

public final class MainNode extends AbstractNode {
    private NodeSyncher syncher = new NodeSyncher ();
    public MainNode () {
        super (new Children.Array ());
        setName ("Node Synchronization Sets");
    }
    public boolean canDestroy () { return false; }
    public boolean canCut () { return false; }
    public SystemAction[] getActions () {
        return new SystemAction[] { SystemAction.get (NewAction.class) };
    }
    public NewType[] getNewTypes () {
        return new NewType[] {
                   new NewType () {
                       public String getName () { return "synch set"; }
                       public void create () {
                           getChildren ().add (new Node[] { new SynchSetNode (syncher) });
                       }
                   }
               };
    }
}
