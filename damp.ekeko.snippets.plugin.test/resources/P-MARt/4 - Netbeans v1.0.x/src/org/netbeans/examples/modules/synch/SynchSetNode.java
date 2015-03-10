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
import java.io.*;

final class SynchSetNode extends AbstractNode {
    private NodeSyncher.SynchSet set;
    private static int counter = 0;
    public SynchSetNode (NodeSyncher syncher) {
        super (new Children.Array ());
        set = syncher.new SynchSet ();
        setName ("<synch set #" + ++counter + ">");
    }
    public NodeSyncher.SynchSet getSynchSet () {
        return set;
    }
    public boolean canDestroy () { return true; }
    public void destroy () throws IOException {
        super.destroy ();
        set.getSyncher ().removeSynchSet (set);
    }
    public boolean canCut () { return false; }
    public SystemAction[] getActions () {
        return new SystemAction[] { SystemAction.get (NewAction.class) };
    }
    public NewType[] getNewTypes () {
        return new NewType[] {
                   new NewType () {
                       public String getName () { return "synched window"; }
                       public void create () {
                           getChildren ().add (new Node[] { new ElementNode (set) });
                       }
                   }
               };
    }
}
