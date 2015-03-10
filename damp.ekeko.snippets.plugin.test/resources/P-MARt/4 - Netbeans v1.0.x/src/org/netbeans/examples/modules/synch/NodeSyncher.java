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
import org.openide.windows.*;
import java.util.*;

final class NodeSyncher {
    /**
     * @associates SynchSet 
     */
    private List synchSets = new ArrayList ();
    public NodeSyncher () {
    }
    public void addSynchSet (SynchSet set) {
        synchSets.add (set);        // XXX check for prior existence
    }
    public void removeSynchSet (SynchSet set) {
        synchSets.remove (set);
    }
    public SynchSet[] getSynchSets () {
        return (SynchSet[]) synchSets.toArray (new SynchSet[] {});
    }

    final class SynchSet {
        private Set elements = new HashSet ();
        public SynchSet () {
        }
        public void addElement (Workspace.Element elt) {
            elements.add (elt);       // XXX check other synch sets first
        }
        public void removeElement (Workspace.Element elt) {
            elements.remove (elt);
        }
        public boolean containsElement (Workspace.Element elt) {
            return elements.contains (elt);
        }
        public Workspace.Element[] getElements () {
            return (Workspace.Element[]) elements.toArray (new Workspace.Element[] {});
        }
        public NodeSyncher getSyncher () {
            return NodeSyncher.this;
        }
    }
}
