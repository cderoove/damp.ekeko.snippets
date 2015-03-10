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

package org.netbeans.modules.form;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
*
* @author   Ian Formanek
*/
public class LightweightLayer extends Container {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 2097628703916129522L;
    /** Creates a new LightweightLayer for specified component.
    * @param comp The component to be placed to this LightweightLayer.
    */
    public LightweightLayer (Component comp) {
        component = comp;
        setLayout(new LayerLayout());
        layer = new TransparentLayer();
        add (layer);
        add (component);
        layer.addMouseListener(new MouseListener() {
                                   public void mouseClicked(MouseEvent e) {
                                       if (mouseListener != null)
                                           mouseListener.mouseClicked(processEvent(e));
                                   }
                                   public void mousePressed(MouseEvent e) {
                                       if (mouseListener != null)
                                           mouseListener.mousePressed(processEvent(e));
                                   }
                                   public void mouseReleased(MouseEvent e) {
                                       if (mouseListener != null)
                                           mouseListener.mouseReleased(processEvent(e));
                                   }
                                   public void mouseEntered(MouseEvent e) {
                                       if (mouseListener != null)
                                           mouseListener.mouseEntered(processEvent(e));
                                   }
                                   public void mouseExited(MouseEvent e) {
                                       if (mouseListener != null)
                                           mouseListener.mouseExited(processEvent(e));
                                   }

                               }
                              );

        layer.addMouseMotionListener(new MouseMotionListener() {
                                         public void mouseDragged(MouseEvent e) {
                                             if (mouseMotionListener != null)
                                                 mouseMotionListener.mouseDragged(processEvent(e));
                                         }

                                         public void mouseMoved(MouseEvent e) {
                                             if (mouseMotionListener != null)
                                                 mouseMotionListener.mouseMoved(processEvent(e));
                                         }
                                     }
                                    );
    }

    void setConsumeMouse (boolean value) {
        if (consumeMouse == value) return;
        consumeMouse = value;
        invalidate ();
    }

    boolean getConsumeMouse () {
        return consumeMouse;
    }

    /** Processes the MouseEvent from the TransparentLayer, so that it
    * looks like it originated from the component.
    * and passes the event
    * to the listeners registered on the LightweightLayer.
    * @param e The MouseEvent to process
    * @return the changed MouseEvent
    */
    private MouseEvent processEvent (MouseEvent evt) {
        return new MouseEvent(
                   component,
                   evt.getID(),
                   evt.getWhen(),
                   evt.getModifiers(),
                   evt.getX(),
                   evt.getY(),
                   evt.getClickCount(),
                   evt.isPopupTrigger()
               );
    }

    /** Redirects the mouse listener to the transparent layer */
    public final void addMouseListener(MouseListener listener) {
        mouseListener = AWTEventMulticaster.add(mouseListener, listener);
    }

    /** Redirects the mouse listener to the transparent layer */
    public final void removeMouseListener(MouseListener listener) {
        mouseListener = AWTEventMulticaster.remove(mouseListener, listener);
    }

    /** Redirects the mouse listener to the transparent layer */
    public final void addMouseMotionListener(MouseMotionListener listener) {
        mouseMotionListener = AWTEventMulticaster.add(mouseMotionListener, listener);
    }

    /** Redirects the mouse listener to the transparent layer */
    public final void removeMouseListener(MouseMotionListener listener) {
        mouseMotionListener = AWTEventMulticaster.remove(mouseMotionListener, listener);
    }

    /** The Transparent Component that is placed on top of the lightweight
    * component to prevent it from processing the mouse events */
    private class TransparentLayer extends Component {
        static final long serialVersionUID =3739582600368383532L;
        TransparentLayer() {
            enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        }
    }

    /** The design-mode version of border layout - the components span always only the
    * space for the direction they are in, so that the other directions are empty and
    * available for adding components.
    */
    class LayerLayout implements LayoutManager {
        public void addLayoutComponent (String name, Component comp) {
        }

        public void addLayoutComponent(Component comp, Object constraints) {
        }

        public void removeLayoutComponent (Component comp) {
        }

        public Dimension preferredLayoutSize (Container target) {
            return component.getPreferredSize();
        }

        public Dimension minimumLayoutSize (Container target) {
            return component.getMinimumSize();
        }

        public void layoutContainer (Container parent) {
            Dimension size = parent.getSize();
            component.setBounds(0, 0, size.width, size.height);
            if (consumeMouse) {
                layer.setBounds(0, 0, size.width, size.height);
            }
            else {
                layer.setBounds(0, 0, 0, 0);
            }
        }
    }

    Component getComponent() {
        return component;
    }

    /** The component managed by this LightweightLayer */
    private Component component;

    /** The Transparent Component placed on top of the actual component
    * to prevent it from processing mouse events */
    private Component layer;

    private boolean consumeMouse = true;

    private MouseListener mouseListener;
    private MouseMotionListener mouseMotionListener;
}

/*
 * Log
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         5/14/99  Ian Formanek    
 *  1    Gandalf   1.0         5/11/99  Ian Formanek    
 * $
 */
