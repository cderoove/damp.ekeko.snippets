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
import java.util.*;
import javax.swing.event.*;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
public class AllPropsChildren extends Children.Keys {
    private ChangeListener listener;
    protected void addNotify () {
        refreshList ();
        PropertiesNotifier.addChangeListener (listener = new ChangeListener () {
                                                  public void stateChanged (ChangeEvent ev) {
                                                      refreshList ();
                                                  }
                                              });
    }
    protected void removeNotify () {
        if (listener != null) {
            PropertiesNotifier.removeChangeListener (listener);
            listener = null;
        }
        setKeys (Collections.EMPTY_SET);
    }
    protected Node[] createNodes (Object key) {
        return new Node[] { new OnePropNode ((String) key) };
    }
    private void refreshList () {
        List keys = new ArrayList ();
        Properties p = System.getProperties ();
        Enumeration e = p.propertyNames ();
        while (e.hasMoreElements ()) keys.add (e.nextElement ());
        Collections.sort (keys);
        setKeys (keys);
    }
}
