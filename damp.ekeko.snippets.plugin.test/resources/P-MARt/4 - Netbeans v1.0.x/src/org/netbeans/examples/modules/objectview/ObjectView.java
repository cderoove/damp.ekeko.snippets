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

package org.netbeans.examples.modules.objectview;

import java.awt.BorderLayout;
import java.awt.Component;

import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.awt.SplittedPanel;
import org.openide.nodes.Node;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.view.ListView;
import org.openide.explorer.view.ContextTreeView;
import org.openide.explorer.view.BeanTreeView;


import org.openide.src.nodes.SourceElementFilter;
import org.openide.src.nodes.ClassElementFilter;

/**
* Opens a new Explorer with a context tree view and list view.
* Stolen from the Open API examples by Jaroslav Tulach.
*
* @author  Jaroslav Tulach
*/
public class ObjectView extends ExplorerPanel {

    static final long serialVersionUID =-920038335773815357L;
    /** Creates new ObjectView. */
    public ObjectView() {
        // create a new TreeView component which does not display leaves (ContextTreeView)
        Component tree = new ContextTreeView ();
        //    BeanTreeView tree = new BeanTreeView ();
        // create a new ListView component
        Component list = new ListView ();

        // create a splitted panel with horizontal split and add the Explorer views into it
        SplittedPanel panel = new SplittedPanel ();
        panel.setSplitType (SplittedPanel.HORIZONTAL);
        panel.add (tree, SplittedPanel.ADD_FIRST);
        panel.add (list, SplittedPanel.ADD_SECOND);
        setLayout (new BorderLayout ());
        add (panel, BorderLayout.CENTER);
    }

    /** Explore the folder elements.
    */  
    public static void explore (DataFolder df) {
        // This pays attention to ElementCookie's on some data objects, e.g. Java source d.o.'s:
        DataObjectFilter filter = new DataObjectFilter (df);

        //    SourceElementFilter sef = new SourceElementFilter();
        //    sef.setOrder (new int[] {f});
        //    sef.setModifiers (m);
        //    sef.setAllClasses (true);
        //    filter.putFilter (SourceElementFilter.class, sef);

        // Create a view of the folder, Object-Browser style:
        Node node = df.new FolderNode (filter);

        // Ask child components which are Explorer views to display this node:
        ExplorerPanel view = new ObjectView ();
        view.getExplorerManager ().setRootContext (node);

        view.open ();
    }

    /** Open the explorer on given folder.
    * @param arr should hold one package name
    */
    public static void main (String[] arr) {
        if (arr.length == 0) {
            System.err.println("Usage: " + ObjectView.class.getName () + " org.netbeans.examples");
            return;
        }

        Repository rep = TopManager.getDefault ().getRepository ();
        FileObject res = rep.find (arr[0], null, null);
        if (res == null) {
            System.err.println("Resource not found: " + arr[0]);
            return;
        }

        DataFolder df = DataFolder.findFolder (res);
        explore (df);
    }
}
