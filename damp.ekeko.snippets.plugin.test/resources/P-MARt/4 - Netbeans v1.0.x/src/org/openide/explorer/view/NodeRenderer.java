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

package org.openide.explorer.view;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Component;
import java.awt.Graphics;
import java.beans.BeanInfo;
import java.util.*;
import java.lang.ref.*;

import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.tree.*;
import javax.swing.ListCellRenderer;
import javax.swing.JLabel;

import org.openide.awt.ListPane;
import org.openide.nodes.Node;

/** Default renderer for nodes. Can be paint either Nodes directly or
* can be used to paint object produces by NodeTreeModel, etc.
*
* @author Jaroslav Tulach
*/
public class NodeRenderer extends Object
    implements TreeCellRenderer, ListCellRenderer {
    /** shared instance */
    private static NodeRenderer sharedInstance;

    /** big icons */
    private boolean bigIcons;

    /** Getter for one shared instance.
    */
    public static NodeRenderer sharedInstance () {
        if (sharedInstance == null) {
            sharedInstance = new NodeRenderer ();
        }
        return sharedInstance;
    }

    /** Creates default renderer.
    */
    public NodeRenderer () {
    }


    /** Creates renderer.
    * @param bigIcons use big icons if possible?
    */
    public NodeRenderer (boolean bigIcons) {
        this.bigIcons = bigIcons;
    }

    //
    // Rendering methods
    //

    /** Finds the component that is capable of drawing the cell in a tree.
    * @param value value can be either Node or a value produced by models (like
    *   NodeTreeModel, etc.)
    * @return component to draw the value
    */
    public Component getTreeCellRendererComponent(
        JTree tree, Object value,
        boolean sel, boolean expanded,
        boolean leaf, int row, boolean hasFocus
    ) {
        // accepting either Node or Visualizers
        VisualizerNode vis = (value instanceof Node) ?
                             VisualizerNode.getVisualizer (null, (Node)value)
                             :
                             (VisualizerNode)value;


        return vis.getTree ().getTreeCellRendererComponent (
                   tree, value, sel, expanded, leaf, row, hasFocus
               );
    }


    /* This is the only method defined by ListCellRenderer.  We just
    * reconfigure the Jlabel each time we're called.
    */
    public java.awt.Component getListCellRendererComponent (
        JList list,
        Object value,            // value to display
        int index,               // cell index
        boolean isSelected,      // is the cell selected
        boolean cellHasFocus     // the list and the cell have the focus
    ) {
        // accepting either Node or Visualizers
        VisualizerNode vis = (value instanceof Node) ?
                             VisualizerNode.getVisualizer (null, (Node)value)
                             :
                             (VisualizerNode)value;

        if (vis == null) {
            vis = VisualizerNode.EMPTY;
        }

        ListCellRenderer r = bigIcons ?
                             (ListCellRenderer)vis.getPane () :
                             (ListCellRenderer)vis.getList ();

        return r.getListCellRendererComponent (
                   list, vis, index, isSelected, cellHasFocus
               );
    }

    // ********************
    // Support for dragging
    // ********************

    /** Value of the cell with "drag under" visual feedback */ // NOI18N
    private static VisualizerNode draggedOver;


    /** DnD operation enters. Update look and feel to the
    * "drag under" state.
    * @param value the value of cell which should have 
    * "drag under" visual feedback
    */
    static void dragEnter (Object dragged) {
        draggedOver = (VisualizerNode)dragged;
    }

    /** DnD operation exits. Revert to the normal look and feel. */
    static void dragExit () {
        draggedOver = null;
    }


    // ********************
    // Cache for ImageIcons
    // ********************

    /** default icon to use when none is present */
    private static final String DEFAULT_ICON = "/org/openide/resources/defaultNode.gif"; // NOI18N

    /** loaded default icon */
    private static ImageIcon defaultIcon;

    /** of icons used (Image, IconImage)*/
    private static final WeakHashMap map = new WeakHashMap ();

    /** Loades default icon if not loaded.
    */
    static ImageIcon getDefaultIcon () {
        if (defaultIcon == null) {
            defaultIcon = new ImageIcon (DEFAULT_ICON);
        }

        return defaultIcon;
    }

    /** Finds imager for given resource.
    * @param image image to get
    * @return icon for the image
    */
    static ImageIcon getIcon (Image image) {
        Reference ref = (Reference)map.get (image);

        ImageIcon icon = ref == null ? null : (ImageIcon)ref.get ();
        if (icon != null) {
            return icon;
        }

        icon = new ImageIcon (image);
        map.put (image, new WeakReference (icon));

        return icon;
    }

    //
    // Renderers
    //

    final static class Tree extends DefaultTreeCellRenderer {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -183570483117501696L;


        // Attributes


        /** The borders for visual feedback during DnD operation.
        * Borders are initialized only when DnD operation becomes active */
        static Border activeBorder;
        static Border emptyBorder;

        private static Component oldRenderer = null;

        private boolean displaysOpenedIcon = false;

        private boolean nbHasFocus;
        private boolean nbDrawsFocusBorderAroundIcon;

        {
            Object value = UIManager.get("Tree.drawsFocusBorderAroundIcon"); // NOI18N
            nbDrawsFocusBorderAroundIcon = (value != null && ((Boolean)value).
                                            booleanValue());
        }


        /** @return Rendered cell component */
        public Component getTreeCellRendererComponent(
            JTree tree, Object value,
            boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus
        ) {
            // accepts only VisualizerNode
            VisualizerNode vis = (VisualizerNode)value;
            if (expanded != displaysOpenedIcon) {
                displaysOpenedIcon = expanded;
                update (vis);
            }

            // provide "drag under" feedback if DnD operation is active // NOI18N
            if ((draggedOver != null) && vis == draggedOver)
                setBorder(getActiveBorder());
            else if (emptyBorder != null)
                setBorder(emptyBorder);

            setEnabled(tree.isEnabled());
            selected = sel;
            if(sel)
                setForeground(getTextSelectionColor());
            else
                setForeground(getTextNonSelectionColor());

            nbHasFocus = hasFocus;


            if ( oldRenderer != null && oldRenderer.getParent() != null ) {
                oldRenderer.getParent().remove( oldRenderer );
            }
            oldRenderer = this;
            return this;
        }

        public void update (VisualizerNode vis) {
            if (displaysOpenedIcon) {
                setIcon(NodeRenderer.getIcon(vis.node.getOpenedIcon(BeanInfo.ICON_COLOR_16x16)));
            } else {
                setIcon(NodeRenderer.getIcon(vis.node.getIcon(BeanInfo.ICON_COLOR_16x16)));
            }

            String displayName = vis.displayName;
            String tooltip = vis.shortDescription;
            setText(displayName);

            if ((tooltip != null) && !tooltip.equals(displayName)) {
                setToolTipText(tooltip);
            }
            else {
                setToolTipText(null);
            }
        }

        /** Safe getter for active border. Initializes empty border too */
        Border getActiveBorder () {
            if (activeBorder == null) {
                activeBorder =
                    new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
                emptyBorder = new EmptyBorder(1, 1, 1, 1);
            }
            return activeBorder;
        }

        /**
        * Paints the value.  The background is filled based on selected.
        */
        public void paint(Graphics g) {

            // [IAN]
            // this piece of code is incredibly ugly, because it is not possible to set the private
            // hasFocus field of DefaultTreeCellRenderer. This code relies on the DefaultTreeCellRenderer's
            // paint method not painting the rectangle around the cell when it is not focused.
            // If there will be any problems with painting in next versions of swing, this place is a good
            // candidate for checking

            super.paint(g);
            if (nbHasFocus) {
                int imageOffset = -1;
                if (nbDrawsFocusBorderAroundIcon) {
                    imageOffset = 0;
                }
                else if (imageOffset == -1) {
                    imageOffset = getLabelStart();
                }
                g.setColor(getBorderSelectionColor());
                g.drawRect(imageOffset, 0, getWidth() - 1 - imageOffset,
                           getHeight() - 1);
            }
        }

        private int getLabelStart() {
            javax.swing.Icon currentI = super.getIcon();
            if(currentI != null && getText() != null) {
                return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
            }
            return 0;
        }


    }

    /** Implements a
    * <code>ListCellRenderer</code> for rendering items of a <code>List</code> containing <code>Node</code>s.
    * It displays the node's 16x16 icon and its display name.
    *
    * @author   Ian Formanek
    */
    static final class List extends JLabel implements ListCellRenderer {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -8387317362588264203L;

        protected static Border hasFocusBorder;
        protected static Border noFocusBorder;

        static {
            hasFocusBorder = new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        }

        /* This is the only method defined by ListCellRenderer.  We just
        * reconfigure the Jlabel each time we're called.
        */
        public java.awt.Component getListCellRendererComponent (
            JList list,
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // the list and the cell have the focus
        {
            VisualizerNode vis = (VisualizerNode)value;

            if (isSelected) {
                setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                setForeground(UIManager.getColor("List.selectionForeground")); // NOI18N
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            int delta = NodeListModel.findVisualizerDepth (list.getModel (), vis);

            Border b = (cellHasFocus || value == draggedOver) ? hasFocusBorder : noFocusBorder;
            if (delta > 0) {
                javax.swing.Icon icon = this.getIcon ();
                b = new javax.swing.border.CompoundBorder (
                        new EmptyBorder (0, icon.getIconWidth () * delta, 0, 0), b
                    );
            }
            setBorder(b);


            return this;
        }

        public void update (VisualizerNode vis) {
            setOpaque(true);
            setBorder(noFocusBorder);
            setIcon(NodeRenderer.getIcon(vis.node.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16)));
            setText(vis.displayName);
        }
    }


    /** Icon renderer
    */
    final static class Pane extends JLabel implements ListCellRenderer {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -5100925551665387243L;

        protected static Border hasFocusBorder;
        protected static Border noFocusBorder;

        static {
            hasFocusBorder = new LineBorder(java.awt.Color.black);
            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        }

        /** Creates a new NetbeansListCellRenderer */
        public Pane () {
            setOpaque(true);
            setBorder(noFocusBorder);
            setVerticalTextPosition(JLabel.BOTTOM);
            setHorizontalAlignment(JLabel.CENTER);
            setHorizontalTextPosition(JLabel.CENTER);
        }

        /** This is the only method defined by ListCellRenderer.  We just
        * reconfigure the Jlabel each time we're called.
        * @param list The ListPane.
        * @param value The value returned by list.getModel().getElementAt(index).
        * @param index The cells index.
        * @param isSelected True if the specified cell was selected.
        * @param cellHasFocus True if the specified cell has the focus.
        * @return A component whose paint() method will render the specified value.
        */
        public Component getListCellRendererComponent (
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus
        ) {
            if (isSelected){
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setBorder(cellHasFocus ? hasFocusBorder : noFocusBorder);

            return this;
        }

        public void update (VisualizerNode vis) {
            ImageIcon icon = NodeRenderer.getIcon(vis.node.getIcon(BeanInfo.ICON_COLOR_32x32));
            setIcon(icon);

            setText(vis.displayName);
        }

    }



}

/*
* Log
*  6    Gandalf   1.5         1/13/00  Ian Formanek    NOI18N
*  5    Gandalf   1.4         1/12/00  Ian Formanek    NOI18N
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         9/22/99  Petr Hrebejk    Caching of Renderers in 
*       CellRendererPane disabled 
*  2    Gandalf   1.1         8/27/99  Jaroslav Tulach List model can display 
*       more levels at once.
*  1    Gandalf   1.0         8/27/99  Jaroslav Tulach 
* $
*/