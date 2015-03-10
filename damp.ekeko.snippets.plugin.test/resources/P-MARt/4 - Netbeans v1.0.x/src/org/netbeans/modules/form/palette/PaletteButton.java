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

package org.netbeans.modules.form.palette;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.awt.ToolbarToggleButton;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceSupport;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;

import org.netbeans.modules.form.*;

/** Button that displays on node in component palette.
*
* @author Jaroslav Tulach
*/
final class PaletteButton extends ToolbarToggleButton
    implements InstanceCookie, NodeListener, Runnable {
    /** node */
    private Node itemNode;
    /** item */
    private PaletteItem palItem;
    static final long serialVersionUID =-8524018463476326691L;
    /**
    /** Creates new PaletteButton */
    public PaletteButton (Node itemNode, PaletteItem palItem) {
        super (
            new ImageIcon (itemNode.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16)),
            false
        );

        this.itemNode = itemNode;
        this.palItem = palItem;


        setMargin (new Insets (1, 1, 0, 1));
        setToolTipText (palItem.getDisplayName ());

        // listening to change of icon
        itemNode.addNodeListener (org.openide.util.WeakListener.node (this, itemNode));

        org.openide.util.RequestProcessor.postRequest (this);
    }

    /** Later initialization of the help.
    */
    public void run () {
        // JST: Found too deadlocking ;-) => moved to request processor

        // help
        // The following may be commented out if found to be slow (should not be):
        HelpCtx hc = InstanceSupport.findHelp (this);

        String help = null;
        if (hc != null) {
            help = hc.getHelpID ();
        }
        if (help == null) {
            help = ComponentPalette.class.getName () + ".paletteItem"; // NOI18N
        }

        HelpCtx.setHelpIDString (this, help);
    }

    public Class instanceClass () {
        return palItem.getItemClass ();
    }

    public String instanceName () {
        // probably never called
        return "dummy"; // NOI18N
    }

    public Object instanceCreate () {
        return null;
    }

    /** Fired when a set of new children is added.
     * @param ev event describing the action
     */
    public void childrenAdded(NodeMemberEvent ev) {
    }
    /** Fired when a set of children is removed.
     * @param ev event describing the action
     */
    public void childrenRemoved(NodeMemberEvent ev) {
    }
    /** Fired when the order of children is changed.
     * @param ev event describing the change
     */
    public void childrenReordered(NodeReorderEvent ev) {
    }

    /** Fired when the node is deleted.
     * @param ev event describing the node
     */
    public void nodeDestroyed(NodeEvent ev) {
    }


    public void propertyChange(final java.beans.PropertyChangeEvent ev) {
        if (Node.PROP_ICON.equals (ev.getPropertyName ())) {
            this.setIcon (
                new ImageIcon (itemNode.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16))
            );
        }
    }
}

/*
* Log
*  5    Gandalf   1.4         1/5/00   Ian Formanek    NOI18N
*  4    Gandalf   1.3         1/4/00   Ian Formanek    
*  3    Gandalf   1.2         12/9/99  Jaroslav Tulach #4927
*  2    Gandalf   1.1         11/27/99 Patrik Knakal   
*  1    Gandalf   1.0         11/4/99  Jaroslav Tulach 
* $
*/