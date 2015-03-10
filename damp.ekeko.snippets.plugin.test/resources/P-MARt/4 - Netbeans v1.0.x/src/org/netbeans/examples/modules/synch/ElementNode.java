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
import org.openide.windows.*;
import java.awt.*;
import java.beans.*;
import java.io.*;
import java.util.*;

final class ElementNode extends AbstractNode {
    private NodeSyncher.SynchSet set;
    private Workspace.Element elt;
    public ElementNode (NodeSyncher.SynchSet set) {
        super (Children.LEAF);
        this.set = set;
        elt = Workspace.getRegistry ().getSelectedElement ();
        set.addElement (elt);
        setName (getNameForElt (elt));
    }
    private static String getNameForElt (Workspace.Element e) {
        Window win = e.getWindow ();
        if (win instanceof Frame) {
            return ((Frame) win).getTitle ();
        } else {
            return win.getName ();
        }
    }
    public boolean canDestroy () { return true; }
    public void destroy () throws IOException {
        super.destroy ();
        set.removeElement (elt);
    }
    public boolean canCut () { return false; }
    private Workspace.Element getElt () {
        System.err.println ("Getting elt: " + getNameForElt (elt));
        return elt;
    }
    private void setElt (Workspace.Element elt) {
        System.err.println ("Setting elt from " + getNameForElt (this.elt) + " to " + getNameForElt (elt));
        set.removeElement (this.elt);
        set.addElement (elt);
        this.elt = elt;
    }
    protected Sheet createSheet () {
        Sheet sheet = super.createSheet ();
        class MyProp extends PropertySupport.ReadWrite {
            public MyProp () {
                super ("workspaceElement", Workspace.Element.class, "Workspace Element",
                       "Select the window you wish to use.");
            }
            public Object getValue () { return getElt (); }
            public void setValue (Object newValue) {
                /* throw new IllegalArgumentException (); */
                /* setElt ((Workspace.Element) newValue); */
            }
            public PropertyEditor getPropertyEditor () {
                return new PropertyEditorSupport () {
                           public String[] getTags () {
                               Set all = Workspace.getRegistry ().getAllElements ();
                               Iterator it = all.iterator ();
                               java.util.List ls = new ArrayList ();
                               while (it.hasNext ()) {
                                   Workspace.Element e = (Workspace.Element) it.next ();
                                   ls.add (getNameForElt (e));
                               }
                               return (String[]) ls.toArray (new String[] {});
                           }
                           public String getAsText () {
                               return getNameForElt (getElt ());
                           }
                           public void setAsText (String name) {
                               Set all = Workspace.getRegistry ().getAllElements ();
                               Iterator it = all.iterator ();
                               while (it.hasNext ()) {
                                   Workspace.Element e = (Workspace.Element) it.next ();
                                   if (getNameForElt (e).equals (name)) {
                                       System.err.println ("Setting elt to " + name);
                                       setElt (e);
                                       return;
                                   }
                               }
                               System.err.println ("No match found for " + name);
                           }
                       };
            }
        }
        Sheet.Set sheetset = sheet.get (Sheet.PROPERTIES);
        if (sheetset == null) {
            sheetset = new Sheet.Set ();
            sheetset.setName (Sheet.PROPERTIES);
        }
        sheetset.put (new MyProp ());
        sheet.put (sheetset);
        return sheet;
    }
}
