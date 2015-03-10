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

package org.netbeans.core;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.BorderLayout;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JToolBar;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;

import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.awt.SplittedPanel;
import org.openide.awt.ToolbarToggleButton;
import org.openide.windows.*;
import org.openide.util.actions.SystemAction;
import org.openide.actions.*;
import org.openide.TopManager;

/** Default explorer which contains toolbar with cut/copy/paste,
* switchable property sheet and menu view actions in the toolbar.
*
* @author Dafe Simonek
*/
public final class NbExplorer extends ExplorerPanel implements ItemListener {
    /** Tree explorer view */
    private transient BeanTreeView treeView;
    /** Switchable property view panel */
    private transient PropertySheetView propertyView;
    /** Splitted panel containing tree view and property view */
    private transient SplittedPanel split;
    /** Explorer's toolbar */
    private transient JToolBar toolbar;
    /** Explorer's toolbar */
    private transient ToolbarToggleButton sheetSwitcher;
    /** Flag specifying if property sheet is visible */
    private boolean sheetVisible = false;
    /** the width of the property sheet pane */
    private int sheetWidth = 250;
    private int sheetHeight = 400;

    private transient java.beans.PropertyChangeListener closeListener;

    static final long serialVersionUID =4684705516892980682L;
    /** Default constructor
    */
    public NbExplorer () {
        split = new SplittedPanel();
        split.add(treeView = new BeanTreeView(), SplittedPanel.ADD_LEFT);
        split.setSplitType(SplittedPanel.HORIZONTAL);
        split.setSplitAbsolute(true);
        add(split, BorderLayout.CENTER);
        add(toolbar = createToolbar(), BorderLayout.NORTH);

        closeListener = new java.beans.PropertyChangeListener () {
                            public void propertyChange (java.beans.PropertyChangeEvent evt) {
                                if (org.openide.explorer.ExplorerManager.PROP_ROOT_CONTEXT.equals (evt.getPropertyName ())) {
                                    if (getExplorerManager ().getRootContext () == org.openide.nodes.Node.EMPTY) {
                                        getExplorerManager ().removePropertyChangeListener (closeListener);
                                        NbExplorer.this.close ();
                                    }
                                }
                            }
                        };
        getExplorerManager ().addPropertyChangeListener (closeListener);
    }

    /** Utility method, creates the explorer's toolbar */
    JToolBar createToolbar () {
        JToolBar result = SystemAction.createToolbarPresenter(
                              new SystemAction[] {
                                  SystemAction.get(CutAction.class),
                                  SystemAction.get(CopyAction.class),
                                  SystemAction.get(PasteAction.class),
                                  null,
                                  SystemAction.get(DeleteAction.class),
                                  null
                              }
                          );
        // property sheet switch action
        ImageIcon icon = new ImageIcon (getClass().getResource(
                                            "/org/netbeans/core/resources/actions/properties.gif")); // NOI18N
        sheetSwitcher = new ToolbarToggleButton (icon, sheetVisible);
        sheetSwitcher.setMargin (new java.awt.Insets (2, 0, 1, 0));
        sheetSwitcher.setToolTipText (org.openide.util.NbBundle.getBundle (NbExplorer.class).getString ("CTL_ToggleProperties"));
        sheetSwitcher.addItemListener (this);
        result.add (sheetSwitcher);
        result.setBorder(new EmptyBorder(2, 0, 2, 2));
        result.setFloatable (false);
        return result;
    }

    /** Implementation of the ItemListener interface */
    public void itemStateChanged (ItemEvent evt) {
        sheetVisible = sheetSwitcher.isSelected();
        java.awt.Dimension size = split.getSize ();
        java.awt.Dimension compSize = getSize ();
        // add enclosing mode insets
        Rectangle modeBounds =
            TopManager.getDefault().getWindowManager().getCurrentWorkspace().
            findMode(this).getBounds();
        compSize.width += modeBounds.width - compSize.width;
        compSize.height += modeBounds.height - compSize.height;
        // compute further...
        int splitType = split.getSplitType ();
        boolean swapped = split.getPanesSwapped();
        if (sheetVisible) { // showing property sheet pane
            if (propertyView == null)
                propertyView = new PropertySheetView ();
            int splitPos;
            if (splitType == SplittedPanel.HORIZONTAL) {
                splitPos = swapped ? sheetWidth : size.width;
                compSize.width += sheetWidth;
            } else {
                splitPos = swapped ? sheetHeight : size.height;
                compSize.height += sheetHeight;
            }
            setRequestedSize (compSize);
            split.setSplitPosition (splitPos);
            if (swapped) {
                split.setKeepFirstSame(true);
                split.add(propertyView, SplittedPanel.ADD_LEFT);
            } else {
                split.setKeepSecondSame(true);
                split.add(propertyView, SplittedPanel.ADD_RIGHT);
            }
        }
        else {              // hiding property sheet pane
            split.remove(propertyView);
            int splitPos = split.getSplitPosition ();
            if (splitType == SplittedPanel.HORIZONTAL) {
                sheetWidth = propertyView.getSize().width;
                compSize.width -= sheetWidth;
            } else {
                sheetHeight = propertyView.getSize().height;
                compSize.height -= sheetHeight;
            }
            setRequestedSize (compSize);
            //split.setSplitPosition (splitPos);
        }
    }

    private void setRequestedSize (Dimension dim) {
        Workspace ws = TopManager.getDefault().getWindowManager().
                       getCurrentWorkspace();
        if (ws != null) {
            Mode mode = ws.findMode(this);
            if (mode != null) {
                Rectangle bounds = mode.getBounds();
                Rectangle newBounds =
                    new Rectangle(bounds.x, bounds.y, dim.width, dim.height);
                mode.setBounds(newBounds);
            }
        }
    }

}

/*
* Log
*  12   src-jtulach1.11        1/13/00  Jaroslav Tulach I18N
*  11   src-jtulach1.10        11/30/99 David Simonek   neccessary changes needed
*       to change main explorer to new UI style  (tabs are full top components 
*       now, visual workspace added, layout of editing workspace chnaged a bit)
*  10   src-jtulach1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  9    src-jtulach1.8         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  8    src-jtulach1.7         7/16/99  Ian Formanek    Fixed bug 1696 - Explore 
*       from here window should be closed when its root node is removed.
*  7    src-jtulach1.6         7/16/99  Ian Formanek    Fixed bug #1800 - You can
*       drag off the explorer toolbar. 
*  6    src-jtulach1.5         7/11/99  David Simonek   window system change...
*  5    src-jtulach1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    src-jtulach1.3         3/17/99  David Simonek   slightly changed window 
*       system
*  3    src-jtulach1.2         3/13/99  Ian Formanek    add (constraints, 
*       component) changed to add (component, constraints)
*  2    src-jtulach1.1         3/9/99   Ian Formanek    Reflecting ExplorerPanel 
*       using BorderLayout by default
*  1    src-jtulach1.0         2/17/99  David Simonek   
* $
*/
