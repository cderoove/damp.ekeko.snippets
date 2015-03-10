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
import java.awt.event.*;
import java.beans.*;
import java.io.Serializable;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;

import org.openide.awt.*;

/**
 * Implementation of BasicScrollPalette for the Basic Look and Feel.
 *
 * @version 1.1, Sep 03, 1998
 * @author David Peroutka
 */
public class BasicScrollPaletteUI extends ScrollPaletteUI implements Serializable, SwingConstants {
    /*
     * ScrollPalette parts, listeners, and scrolling support.
     */

    protected ScrollPalette palette;

    protected JViewport viewport; // move it to a custom layout manager
    protected ViewportListener viewportListener;
    protected PropertyListener propertyListener;

    protected ArrowButton incrButton;
    protected ArrowButton decrButton;
    protected ArrowButtonListener buttonListener;

    protected ModelListener modelListener;

    protected ScrollListener scrollListener;
    protected Timer scrollTimer;

    static final long serialVersionUID =4930316897291352148L;
    /**
     * Creates a new instance of UI for a given ScrollPalette component.
     */
    public static ComponentUI createUI(JComponent c) {
        return new BasicScrollPaletteUI();
    }

    public void installUI(JComponent c) {
        if (!(c instanceof ScrollPalette)) {
            System.err.println( "Very strange: Component is not an instance of ScrollPalette: "+c); // NOI18N
            return;
        }
        palette = (ScrollPalette)c;
        palette.setLayout(new ScrollPaletteLayout(palette.getOrientation()));
        // create listeners
        modelListener = new ModelListener();
        buttonListener = new ArrowButtonListener();
        viewportListener = new ViewportListener();
        propertyListener = new PropertyListener();
        // install arrows
        switch (palette.getOrientation()) {
        case JScrollBar.HORIZONTAL:
            incrButton = new ArrowButton(EAST);
            decrButton = new ArrowButton(WEST);
            palette.add(incrButton, "East"); // NOI18N
            palette.add(decrButton, "West"); // NOI18N
            break;

        case JScrollBar.VERTICAL:
            incrButton = new ArrowButton(SOUTH);
            decrButton = new ArrowButton(NORTH);
            palette.add(incrButton, "South"); // NOI18N
            palette.add(decrButton, "North"); // NOI18N
            break;
        }
        incrButton.addMouseListener(buttonListener);
        decrButton.addMouseListener(buttonListener);

        // install viewport
        JViewport viewport = palette.getViewport();
        if (viewport != null) {
            palette.add(viewport, "Center"); // NOI18N
            viewport.addChangeListener(viewportListener);
        }
        palette.addPropertyChangeListener(propertyListener);

        // misc initialization
        palette.getModel().addChangeListener(modelListener);
        palette.setEnabled(palette.isEnabled());
        palette.setOpaque(true);

        // scrolling support
        scrollListener = new ScrollListener();
        scrollTimer = new Timer(100, scrollListener);
        scrollTimer.setInitialDelay(300);
    }

    public void uninstallUI(JComponent c)
    {
        scrollTimer.stop();
        scrollTimer = null;
        // remove components
        palette.removeAll();
        palette.setLayout(null);
        // remove listeners
        if (decrButton != null)
            decrButton.removeMouseListener(buttonListener);
        if (incrButton != null)
            incrButton.removeMouseListener(buttonListener);
        palette.getModel().removeChangeListener(modelListener);
        palette.removePropertyChangeListener(propertyListener);
        // clean-up
        incrButton = null;
        decrButton = null;
        palette = null;
    }

    public void paint(Graphics g, JComponent c) {
    }

    public Dimension getPreferredSize(JComponent c) {
        return getMinimumSize(c);
    }

    public Dimension getMinimumSize(JComponent c) {
        return palette.getLayout().preferredLayoutSize(c);
        //  return getPreferredSize(c);
    }

    public Dimension getMaximumSize(JComponent c) {
        switch (palette.getOrientation()) {
        case JScrollBar.HORIZONTAL:
            return new Dimension(Integer.MAX_VALUE,
                                 getMinimumSize(c).height);
        case JScrollBar.VERTICAL:
            return new Dimension(getMinimumSize(c).width,
                                 Integer.MAX_VALUE);
        }
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }


    /**
     * Listener for arrow buttons.
     */
    protected class ArrowButtonListener extends MouseAdapter implements Serializable
    {
        static final long serialVersionUID =-678701750640807839L;
        public void mousePressed(MouseEvent e) {
            if (!palette.isEnabled())
                return;

            scrollTimer.stop();
            int direction = (e.getSource() == incrButton) ? 1 : -1;
            scrollListener.setDirection(direction);
            scrollTimer.start();
            // update model
            synchronized(palette) {
                palette.setValue(palette.getValue() +
                                 (direction > 0 ? +palette.getUnitIncrement(direction)
                                  : -palette.getUnitIncrement(direction)));
            }
        }

        public void mouseReleased(MouseEvent e) {
            scrollTimer.stop();
            palette.setValueIsAdjusting(false);
        }

        public void mouseEntered(MouseEvent e) {
            AbstractButton button = (AbstractButton)e.getSource();
            button.getModel().setRollover(true);
        }

        /**
         * Relayout palette when mouse exited disabled arrow button.
         */
        public void mouseExited(MouseEvent e) {
            AbstractButton button = (AbstractButton)e.getSource();
            ButtonModel model = button.getModel();
            model.setRollover(false);
            if (!model.isEnabled()) {
                //                button.setVisible(false);
                palette.getLayout().layoutContainer(palette);
            }
        }
    }

    /**
     * Listener for scrolling events intiated in the ScrollPalette.
     */
    protected class ScrollListener implements ActionListener, Serializable
    {
        int direction;

        static final long serialVersionUID =-4275746989151409808L;
        public ScrollListener()  {
            direction = +1;
        }

        public void setDirection(int direction) {
            this.direction = direction;
        }

        public void actionPerformed(ActionEvent e) {
            // update model
            synchronized(palette) {
                palette.setValue(palette.getValue() +
                                 (direction > 0 ? +palette.getUnitIncrement(direction)
                                  : -palette.getUnitIncrement(direction)));
            }
            // update scrolling support
            if ((direction > 0) && (palette.getValue() + palette.getVisibleAmount() >= palette.getMaximum()))
                ((Timer)e.getSource()).stop();
            else if ((direction < 0) && (palette.getValue() <= palette.getMinimum()))
                ((Timer)e.getSource()).stop();
        }
    }

    /**
     * Listener for model changes.
     */
    protected class ModelListener implements ChangeListener, Serializable {
        static final long serialVersionUID =6915809793036078938L;
        public void stateChanged(ChangeEvent e) {
            // set the view coordinates
            JViewport viewport = palette.getViewport();
            if (viewport != null) {
                BoundedRangeModel model = (BoundedRangeModel)(e.getSource());
                Point p = viewport.getViewPosition();
                switch (palette.getOrientation()) {
                case JScrollBar.HORIZONTAL:
                    p.x = model.getValue();
                    break;
                case JScrollBar.VERTICAL:
                    p.y = model.getValue();
                    break;
                }
                viewport.setViewPosition(p);
            }
        }
    }

    /**
     * Listener for viewport events.
     */
    protected class ViewportListener implements ChangeListener, Serializable
    {
        static final long serialVersionUID =3501368301128067126L;
        public void stateChanged(ChangeEvent e) {
            if (palette != null) {
                // lay out the container in the palette
                palette.getLayout().layoutContainer(palette);
                // synchronize palette with viewport
                JViewport viewport = palette.getViewport();
                if (viewport != null) {
                    Dimension viewSize = viewport.getViewSize();
                    Point viewPosition = viewport.getViewPosition();
                    Dimension extentSize = viewport.getExtentSize();
                    switch (palette.getOrientation()) {
                    case JScrollBar.HORIZONTAL:
                        viewPosition.x = Math.max(0, Math.min(viewPosition.x, viewSize.width - extentSize.width));
                        palette.setValues(viewPosition.x, extentSize.width, 0, viewSize.width);
                        break;
                    case JScrollBar.VERTICAL:
                        viewPosition.y = Math.max(0, Math.min(viewPosition.y, viewSize.height - extentSize.height));
                        palette.setValues(viewPosition.y, extentSize.height, 0, viewSize.height);
                        break;
                    }
                    viewport.setViewPosition(viewPosition);
                }
            }
        }
    }

    /**
     * Listener for viewport events.
     */
    protected class PropertyListener implements PropertyChangeListener, Serializable
    {
        static final long serialVersionUID =4115733089884221626L;
        /**
         * This method gets called when a bound property is changed.
         * @param evt describes the property that has changed.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt.getPropertyName().equals("viewport"))
            {
                JViewport newViewport = (JViewport)evt.getNewValue();
                if (newViewport != viewport)
                {
                    // remove old viewport
                    if (viewport != null) {
                        viewport.removeChangeListener(viewportListener);
                        palette.remove(viewport);
                    }
                    // initialize new viewport
                    viewport = newViewport;
                    if (viewport != null) {
                        palette.add(viewport, "Center"); // NOI18N
                        viewport.addChangeListener(viewportListener);
                    }
                }
            }
        }
    }


    /**
     * JButton object that draws an arrow in one of the cardinal directions.
     */
    protected static class ArrowButton extends ToolbarButton implements SwingConstants
    {
        static final long serialVersionUID =355082894431640570L;
        /**
         * Creates a new arrow button in the specified direction.
         * @param direction one of the cardinal directions
         */
        public ArrowButton(int direction) {
            super(new ArrowIcon(direction));
        }


        /**
         * Implements arrow icon that point to the specified direction.
         */
        private static class ArrowIcon implements Icon, UIResource, Serializable {

            protected int direction;

            static final long serialVersionUID =-3870974857610363041L;
            /**
             * Creates a new instance of 
             */
            ArrowIcon(int direction) {
                this.direction = direction;
            }

            public void paintIcon(Component c, Graphics g, int x, int y) {
                // define appropriate polygon
                Polygon p = new Polygon();
                switch (direction)
                {
                case EAST:
                    p.addPoint(x, y);
                    p.addPoint(x, y + getIconHeight());
                    p.addPoint(x + getIconWidth(), y + getIconHeight() / 2);
                    break;
                case WEST:
                    p.addPoint(x + getIconWidth(), y);
                    p.addPoint(x + getIconWidth(), y + getIconHeight());
                    p.addPoint(x, y + getIconHeight() / 2);
                    break;
                case NORTH:
                    p.addPoint(x, y + getIconHeight());
                    p.addPoint(x + getIconWidth(), y + getIconHeight());
                    p.addPoint(x + getIconWidth() / 2, y);
                    break;
                case SOUTH:
                    p.addPoint(x, y);
                    p.addPoint(x + getIconWidth(), y);
                    p.addPoint(x + getIconWidth() / 2, y + getIconHeight());
                    break;
                }
                // draw polygon according to model
                AbstractButton button = (AbstractButton)c;
                ButtonModel model = button.getModel();
                Color color = UIManager.getColor("controlDkShadow"); // NOI18N
                if (!model.isEnabled()) {
                    p.translate(1, 1);
                    g.setColor(UIManager.getColor("controlHighlight")); // NOI18N
                    g.fillPolygon(p);
                    p.translate(-1, -1);
                    color = UIManager.getColor("controlShadow"); // NOI18N
                }
                g.setColor(color);
                g.fillPolygon(p);
            }
            public int getIconWidth() { return 4; }
            public int getIconHeight() { return 8; }
        }
    }
}

/*
 * Log
 *  8    Gandalf   1.7         1/20/00  Ian Formanek    System.out changed to 
 *       System.err as this was an error message
 *  7    Gandalf   1.6         1/5/00   Ian Formanek    NOI18N
 *  6    Gandalf   1.5         11/27/99 Patrik Knakal   
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/10/99  Ian Formanek    Generated Serial Version
 *       UID
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/10/99  Ian Formanek    
 *  1    Gandalf   1.0         3/30/99  Ian Formanek    
 * $
 */
