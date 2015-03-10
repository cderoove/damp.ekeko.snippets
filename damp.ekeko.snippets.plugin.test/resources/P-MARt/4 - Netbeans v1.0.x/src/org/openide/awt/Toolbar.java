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

package org.openide.awt;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.Vector;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.cookies.InstanceCookie;
import org.openide.util.actions.Presenter;

/**
* Toolbar provides a component which is useful for displaying commonly used
* actions.  It can be dragged inside its <code>ToolbarPanel</code> to
* customize its location.
*
* @version 1.0
* @author  David Peroutka, Libor Kramolis
*/

/* * * * * * * * *
 * class Toolbar *
 * * * * * * * * */
public class Toolbar extends JToolBar implements MouseInputListener {
    public static int BASIC_HEIGHT = 34;
    static int HEIGHT_TOLERANCE = 5;
    static int TOP = 0;
    static int LEFT = 3;
    static int BOTTOM = 0;
    static int RIGHT = 3;

    private boolean floatable;
    private DnDListener listener;
    private boolean dragging;
    private Point startPoint;

    static final long serialVersionUID =5011742660516204764L;
    public Toolbar () {
        this (""); // NOI18N
    }

    public Toolbar (String name) {
        this (name, false);
    }

    /**
    * Creates a new <code>Toolbar</code>.
    * @param name a <code>String</code> containing the associated name
    */
    public Toolbar (String name, boolean f) {
        super();
        setName (name);
        setFloatable (false);
        setBorder (new CompoundBorder (new EtchedBorder(),
                                       new EmptyBorder (TOP, LEFT, BOTTOM, RIGHT)));
        floatable = f;
        dragging = false;
        setLayout (new InnerLayout (2, 2));
        addGrip();
    }

    public Component add (Component comp) {
        add ("Action", comp); // NOI18N
        return comp;
    }


    public void removeAll () {
        super.removeAll();
        addGrip();
    }

    /**
     * When Toolbar is floatable, ToolbarGrid is added as Grid as first toolbar component
     */
    void addGrip () {
        if (floatable) {
            ToolbarGrip grip = new ToolbarGrip();
            grip.addMouseListener (this);
            grip.addMouseMotionListener (this);
            add ("Grip", grip); // NOI18N
        }
    }


    static public int rowCount (int height) {
        int rows = 1;
        int max_height = (BASIC_HEIGHT + HEIGHT_TOLERANCE);
        while (height > max_height) {
            // 5 pixels is tolerance so toolbar can be high (BASIC_HEIGHT + HEIGHT_TOLERANCE)
            //   but it will be set to BASIC_HEIGHT high
            rows++;
            height -= max_height;
        }
        return rows;
    }


    public void setDnDListener (DnDListener l) {
        listener = l;
    }

    protected void fireDragToolbar (int dx, int dy, int type) {
        if (listener != null)
            listener.dragToolbar (new DnDEvent (this, getName(), dx, dy, type));
    }

    protected void fireDropToolbar (int dx, int dy, int type) {
        if (listener != null)
            listener.dropToolbar (new DnDEvent (this, getName(), dx, dy, type));
    }

    // MouseInputListener
    public void mouseClicked (MouseEvent e) {
    }

    public void mouseEntered (MouseEvent e) {
    }

    public void mouseExited (MouseEvent e) {
    }

    public void mousePressed (MouseEvent e) {
        startPoint = new Point (e.getX(), e.getY());
    }

    public void mouseReleased (MouseEvent e) {
        if (dragging) {
            fireDropToolbar (e.getX() - startPoint.x,
                             e.getY() - startPoint.y,
                             DnDEvent.DND_ONE);
            dragging = false;
        }
    }

    public void mouseDragged (MouseEvent e) {
        int m = e.getModifiers();
        int type = DnDEvent.DND_ONE;
        if (e.isControlDown())
            type = DnDEvent.DND_LINE;
        else if (((m & InputEvent.BUTTON2_MASK) != 0) || ((m & InputEvent.BUTTON3_MASK) != 0))
            type = DnDEvent.DND_END;
        if (startPoint == null)
            startPoint = new Point (e.getX(), e.getY());
        fireDragToolbar (e.getX() - startPoint.x,
                         (e.getY() - startPoint.y),
                         type);
        dragging = true;
    }

    public void mouseMoved (MouseEvent e) {
    }


    /* * * * * * * * * * * *
     * inner class  Folder *
     * * * * * * * * * * * */
    /**
     * This class can be used to produce a <code>Toolbar</code> instance from
     * the given <code>DataFolder</code>.
     */
    final static class Folder extends FolderInstance {

        /** the <code>Toolbar</code> to work with */
        private JToolBar toolbar;

        /**
         * Creates a new folder on the specified <code>DataFolder</code>.
         * @param folder a <code>DataFolder</code> to work with
         */
        public Folder (DataFolder folder) {
            super (folder);
            recreate ();
        }

        /**
         * Returns a <code>Toolbar</code> representee of this folder.
         * @return a <code>Toolbar</code> representee of this folder
         */
        public final JToolBar getToolbar() {
            if (toolbar == null) {
                toolbar = new Toolbar (folder.getNodeDelegate ().getDisplayName (), true);
            }
            return toolbar;
        }

        /**
         * Full name of the data folder's primary file separated by dots.
         * @return the name
         */
        public String instanceName () {
            return getToolbar().getClass().getName();
        }

        /**
         * Returns the root class of all objects.
         * @return Object.class
         */
        public Class instanceClass ()
        throws java.io.IOException, ClassNotFoundException {
            return getToolbar().getClass();
        }

        /** If no instance cookie, tries to create execution action on the
        * data object.
        */
        protected org.openide.cookies.InstanceCookie acceptDataObject (DataObject dob) {
            InstanceCookie ic = super.acceptDataObject (dob);
            if (
                ic == null &&
                !(dob instanceof DataFolder) &&
                dob.getCookie (InstanceCookie.class) == null
            ) {
                return new InstanceSupport.Instance (ExecBridge.createButton (dob));
            } else {
                return ic;
            }
        }

        private static Class separatorClass = javax.swing.JToolBar.Separator.class;

        /**
        * Accepts only cookies that can provide <code>Toolbar</code>.
        * @param cookie an <code>InstanceCookie</code> to test
        * @return true if the cookie can provide accepted instances
        */
        protected InstanceCookie acceptCookie (InstanceCookie cookie)
        throws java.io.IOException, ClassNotFoundException {
            Class c = cookie.instanceClass();
            if (Toolbar.class.isAssignableFrom(c)) {
                return cookie;
            }
            if (Presenter.Toolbar.class.isAssignableFrom(c)) {
                return cookie;
            }
            if (separatorClass.isAssignableFrom(c)) {
                return cookie;
            }
            return null;
        }

        /**
        * Returns a <code>Toolbar.Folder</code> cookie for the specified
        * <code>DataFolder</code>.
        * @param df a <code>DataFolder</code> to create the cookie for
        * @return a <code>Toolbar.Folder</code> for the specified folder
        */
        protected org.openide.cookies.InstanceCookie acceptFolder(DataFolder df) {
            return null; // PENDING new Toolbar.Folder(df);
        }

        /**
         * Updates the <code>Toolbar</code> represented by this folder.
         *
         * @param cookies array of instance cookies for the folder
         * @return the updated <code>ToolbarPool</code> representee
         */
        protected Object createInstance(InstanceCookie[] cookies)
        throws java.io.IOException, ClassNotFoundException {
            // refresh the toolbar's content
            getToolbar().removeAll();
            for (int i = 0; i < cookies.length; i++) {
                try {
                    Object obj = cookies[i].instanceCreate();
                    if (obj instanceof Presenter.Toolbar) {
                        obj = ((Presenter.Toolbar)obj).getToolbarPresenter();
                    }
                    if (obj instanceof Component) {
                        Component comp = (Component)obj;
                        getToolbar().add (comp);
                    }
                } catch (java.io.IOException ex) {
                } catch (ClassNotFoundException ex) {
                }
            }

            // invalidate the toolbar and its parent
            toolbar.invalidate ();
            java.awt.Container parent = toolbar.getParent ();
            if (parent != null) {
                parent.validate();
                parent.repaint();
            }
            return toolbar;
        }
    } // end of inner class Folder


    /* * * * * * * * * * * * * *
     * inner class ToolbarGrip *
     * * * * * * * * * * * * * */
    private class ToolbarGrip extends JPanel {
        static final int HGAP = 1;
        static final int VGAP = 1;
        static final int STEP = 1;
        static final int WIDTH = 2;

        int columns;
        Dimension dim;

        static final long serialVersionUID =-8819972936203315276L;
        public ToolbarGrip () {
            this (2);
        }

        public ToolbarGrip (int col) {
            super ();
            columns = col;
            int width = (col-1)*STEP + col*WIDTH + 2*HGAP;
            dim = new Dimension (width, width);
            setBorder (new EmptyBorder (VGAP, HGAP, VGAP, HGAP));
            setToolTipText (Toolbar.this.getName());
        }

        public void paint (Graphics g) {
            Dimension size = getSize();
            int top = VGAP;
            int bottom = size.height-1-VGAP;
            int height = bottom - top;
            g.setColor( getBackground() );
            for (int i = 0, x = HGAP; i < columns; i++, x += WIDTH+STEP) {
                g.draw3DRect (x, top, WIDTH, height, true);
            }
        }

        public Dimension getMinimumSize () {
            return dim;
        }

        public Dimension getPreferredSize () {
            return getMinimumSize();
        }
    } // end of inner class ToolbarGrid


    private class InnerLayout implements LayoutManager {
        /** Grip component */
        private Component grip;

        /** Vector of Components 
         * @associates Component*/
        private Vector actions;

        /** horizontal gap */
        private int hgap;

        /** vertical gap */
        private int vgap;

        /**
         * Constructs a new InnerLayout.
         */
        public InnerLayout () {
            this (5, 5);
        }

        /**
         * Constructs a new InnerLayout with the specified gap values.
         * @param hgap the horizontal gap variable
         * @param vgap the vertical gap variable
         */
        public InnerLayout (int hgap, int vgap) {
            this.hgap = hgap;
            this.vgap = vgap;
            actions = new Vector();
        }

        /**
         * Adds the specified component with the specified name to the layout.
         */
        public void addLayoutComponent (String name, Component comp) {
            synchronized (comp.getTreeLock()) {
                if ("Grip".equals (name)) { // NOI18N
                    grip = comp;
                } else
                    if ("Action".equals (name)) { // NOI18N
                        actions.addElement (comp);
                    } else
                        throw new IllegalArgumentException
                        ("cannot add to layout: unknown constraint: " + name); // NOI18N
            }
        }

        /**
         * Adds the specified component to the layout, using the specified
         * constraint object.
         */
        public void addLayoutComponent (Component comp, Object constraints) {
            throw new IllegalArgumentException();
        }

        /**
         * Removes the specified component from the layout.
         */
        public void removeLayoutComponent (Component comp) {
            synchronized (comp.getTreeLock()) {
                if (grip == comp)
                    grip = null;
                else
                    actions.removeElement (comp);
            }
        }

        /**
         * Calculates the preferred size dimensions for the specified panal given
         * the components in the specified parent container.
         */
        public Dimension preferredLayoutSize (Container target) {
            synchronized (target.getTreeLock()) {
                Dimension dim = new Dimension (0, 0);
                int size = actions.size();
                if ((grip != null) && grip.isVisible()) {
                    Dimension d = grip.getPreferredSize();
                    dim.width += d.width;
                    dim.width += hgap;
                }
                for (int i = 0; i < size; i++) {
                    Component comp = (Component)actions.elementAt (i);
                    if (comp.isVisible()) {
                        Dimension d = comp.getPreferredSize();
                        dim.width += d.width;
                        dim.height = Math.max (dim.height, d.height);
                        dim.width += hgap;
                    }
                }
                Insets insets = target.getInsets();
                dim.width += insets.left + insets.right;
                dim.height += insets.top + insets.bottom;

                return dim;
            }
        }

        /**
        * Calculates the minimum size dimensions for the specified panal given
        * the components in the specified parent container.
        */
        public Dimension minimumLayoutSize (Container target) {
            return preferredLayoutSize (target);
        }

        /**
        * Calculates the maximum size dimensions for the specified panal given
        * the components in the specified parent container.
        */
        public Dimension maximumLayoutSize (Container target) {
            synchronized (target.getTreeLock()) {
                Dimension dim = new Dimension (0, 0);
                int size = actions.size();
                if ((grip != null) && grip.isVisible()) {
                    Dimension d = grip.getPreferredSize();
                    dim.width += d.width;
                    dim.width += hgap;
                }
                Component last = null;
                for (int i = 0; i < size; i++) {
                    Component comp = (Component)actions.elementAt (i);
                    if (comp.isVisible()) {
                        Dimension d = comp.getPreferredSize();
                        dim.width += d.width;
                        dim.height = Math.max (dim.height, d.height);
                        dim.width += hgap;
                        last = comp;
                    }
                }
                if (last != null) {
                    Dimension prefSize = last.getPreferredSize();
                    Dimension maxSize = last.getMaximumSize();
                    if (prefSize != maxSize) {
                        dim.width = dim.width - prefSize.width + maxSize.width;
                        dim.height = Math.max (dim.height, maxSize.height);
                    }
                }
                Insets insets = target.getInsets();
                dim.width += insets.left + insets.right;
                dim.height += insets.top + insets.bottom;

                return dim;
            }
        }

        /**
        * Lays out the container in the specified panel.
        */
        public void layoutContainer (Container target) {
            synchronized (target.getTreeLock()) {
                Insets insets = target.getInsets();
                Dimension dim = target.getSize();

                int fullHeight = dim.height;
                int bottom = dim.height - insets.bottom - 1;
                int top = 0 + insets.top;
                int left = insets.left;
                int maxPosition = dim.width - (insets.left + insets.right) - hgap;
                int right = dim.width - insets.right;
                maxPosition = right;
                int height = bottom - top;
                int size = actions.size();
                int w, h;
                if ((grip != null) && grip.isVisible()) {
                    Dimension d = grip.getPreferredSize();
                    grip.setBounds (left, top + vgap, d.width, bottom - top - 2*vgap);
                    left += d.width;
                    left += hgap;
                }
                for (int i = 0; i < size; i++) {
                    left += hgap;
                    Component comp = (Component)actions.elementAt (i);
                    Dimension d;
                    Dimension minSize = comp.getMinimumSize();

                    if (i == size - 1)
                        d = comp.getMaximumSize();
                    else
                        d = comp.getPreferredSize();
                    w = d.width;
                    h = Math.min (height, d.height);
                    if ((left < maxPosition) &&
                            (left + w > maxPosition)) {
                        if (maxPosition - left >= minSize.width)
                            w = maxPosition - left;
                    }

                    comp.setBounds (left, (fullHeight - d.height)/2, w, h);

                    //  	  if (i == size - 1)
                    //  	    d = comp.getMaximumSize();
                    //  	  else
                    //  	    d = comp.getPreferredSize();
                    //  	  if ((left + d.width) > right)
                    //  	    w = right - left;
                    //  	  else
                    //  	    w = d.width;
                    //  	  h = Math.min (height, d.height);
                    //  	  comp.setBounds (left, (fullHeight - d.height)/2, w, h);

                    left += d.width;
                }
            }
        }

        /**
        * Returns the alignment along the x axis.
        */
        public float getLayoutAlignmentX (Container target) {
            return 0;
        }

        /**
        * Returns the alignment along the y axis.
        */
        public float getLayoutAlignmentY (Container target) {
            return (float)0.5;
        }

        /**
        * Invalidates the layout, indicating that if the layout manager has cached
        * information it should ne discarded.
        */
        public void invalidateLayout (Container target) {
            // check target components with local vars (grip, actions)
        }
    } // end of class InnerLayout


    public abstract interface DnDListener extends java.util.EventListener {
        public void dragToolbar (DnDEvent e);
        public void dropToolbar (DnDEvent e);
    } // end of interface DnDListener


    public static class DnDEvent extends EventObject {
        public static final int DND_ONE  = 1;
        public static final int DND_END  = 2;
        public static final int DND_LINE = 3;

        public String name;
        public int dx;
        public int dy;
        public int type;

        static final long serialVersionUID =4389530973297716699L;
        public DnDEvent (Toolbar toolbar, String name, int dx, int dy, int type) {
            super (toolbar);

            this.name = name;
            this.dx = dx;
            this.dy = dy;
            this.type = type;
        }
    } // end of class DnDEvent
} // end of class Toolbar


/*

* Log
*  37   dperoutka-src-gandalf1.36        1/20/00  Libor Kramolis  
*  36   dperoutka-src-gandalf1.35        1/18/00  Libor Kramolis  
*  35   dperoutka-src-gandalf1.34        1/13/00  Jaroslav Tulach File names can be 
*       localized.  
*  34   dperoutka-src-gandalf1.33        1/12/00  Ian Formanek    NOI18N
*  33   dperoutka-src-gandalf1.32        1/12/00  Ian Formanek    NOI18N
*  32   dperoutka-src-gandalf1.31        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  31   dperoutka-src-gandalf1.30        8/16/99  Ian Formanek    Toolbars are smaller in 
*       height
*  30   dperoutka-src-gandalf1.29        8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  29   dperoutka-src-gandalf1.28        8/1/99   Petr Hrebejk    Toolbar.Grip apperance 
*       fixed
*  28   dperoutka-src-gandalf1.27        7/30/99  Libor Kramolis  
*  27   dperoutka-src-gandalf1.26        6/25/99  Ian Formanek    Fixed painting of toolbar
*       grips
*  26   dperoutka-src-gandalf1.25        6/10/99  Michal Fadljevic SystemColor changed to 
*       UIManager.getColor(...)  
*  25   dperoutka-src-gandalf1.24        6/10/99  Michal Fadljevic setForeground() in 
*       paint() added
*  24   dperoutka-src-gandalf1.23        6/10/99  Libor Kramolis  
*  23   dperoutka-src-gandalf1.22        6/10/99  Libor Kramolis  
*  22   dperoutka-src-gandalf1.21        6/9/99   Jaroslav Tulach Executables can be in 
*       menu & toolbars.
*  21   dperoutka-src-gandalf1.20        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  20   dperoutka-src-gandalf1.19        6/4/99   Ian Formanek    Toolbar Grips made 
*       slighly more decent
*  19   dperoutka-src-gandalf1.18        6/4/99   Libor Kramolis  
*  18   dperoutka-src-gandalf1.17        5/15/99  Libor Kramolis  
*  17   dperoutka-src-gandalf1.16        5/13/99  Libor Kramolis  
*  16   dperoutka-src-gandalf1.15        5/11/99  Jaroslav Tulach ToolbarPool changed to 
*       look better in Open API
*  15   dperoutka-src-gandalf1.14        5/7/99   Libor Kramolis  
*  14   dperoutka-src-gandalf1.13        4/8/99   Libor Kramolis  
*  13   dperoutka-src-gandalf1.12        4/7/99   Libor Kramolis  
*  12   dperoutka-src-gandalf1.11        4/5/99   Ian Formanek    
*  11   dperoutka-src-gandalf1.10        4/4/99   Ian Formanek    Latest Libor's version
*  10   dperoutka-src-gandalf1.9         3/30/99  Ian Formanek    FolderInstance creation 
*       in single thread
*  9    dperoutka-src-gandalf1.8         3/26/99  Libor Kramolis  
*  8    dperoutka-src-gandalf1.7         3/24/99  Libor Kramolis  
*  7    dperoutka-src-gandalf1.6         3/24/99  Libor Kramolis  
*  6    dperoutka-src-gandalf1.5         3/24/99  Libor Kramolis  
*  5    dperoutka-src-gandalf1.4         3/24/99  Libor Kramolis  
*  4    dperoutka-src-gandalf1.3         3/24/99  Libor Kramolis  
*  3    dperoutka-src-gandalf1.2         2/11/99  Jaroslav Tulach SystemAction is 
*       javax.swing.Action
*  2    dperoutka-src-gandalf1.1         1/25/99  David Peroutka  
*  1    dperoutka-src-gandalf1.0         1/25/99  David Peroutka  
* $

*/

