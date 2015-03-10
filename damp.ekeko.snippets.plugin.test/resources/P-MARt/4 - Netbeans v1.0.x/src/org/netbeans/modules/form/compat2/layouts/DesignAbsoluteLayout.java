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

package org.netbeans.modules.form.compat2.layouts;

import java.awt.*;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;

import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;
import org.netbeans.modules.form.util2.NbVersion;
import org.netbeans.modules.form.util2.NbVersionNotCompatibleException;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.util.*;

/** A design-time support for AbsoluteLayout.
*
* @author   Ian Formanek
*/
final public class DesignAbsoluteLayout extends DesignLayout {
    /** A JDK 1.1. serial version UID */
    static final long serialVersionUID = -1148154120636340460L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    /** Layout Properties */
    final static String PROP_X = FormEditor.LAYOUT_PREFIX + "xPosition"; // NOI18N
    final static String PROP_Y = FormEditor.LAYOUT_PREFIX + "yPosition"; // NOI18N
    final static String PROP_WIDTH = FormEditor.LAYOUT_PREFIX + "widthSize"; // NOI18N
    final static String PROP_HEIGHT = FormEditor.LAYOUT_PREFIX + "heightSize"; // NOI18N

    /** bundle to obtain text information from */
    private static final java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle (DesignAbsoluteLayout.class);

    /** icons for the Layout. */
    static protected Image icon;
    static protected Image icon32;

    static {
        icon = Toolkit.getDefaultToolkit ().getImage (
                   DesignAbsoluteLayout.class.getResource ("/org/netbeans/modules/form/resources/palette/absoluteLayout.gif")); // NOI18N
        icon32 = Toolkit.getDefaultToolkit ().getImage (
                     DesignAbsoluteLayout.class.getResource ("/org/netbeans/modules/form/resources/palette/absoluteLayout32.gif")); // NOI18N
    }


    /** An icon of the design-layout. This icon will be used on the ComponentPalette
    * for this layout's item.
    * @param  type the desired type of the icon (BeanInfo.ICON_???)
    * @return layout's icon.
    */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return icon;
        else
            return icon32;
    }

    /** Assigns this DesignLayout to the specified RADVisualContainer.
    * @param cont The RADVisualContainer that represents a container that will be
    *             managed by this layout or null as a notification that this layout
    *             is not a designLayout for its current container anymore
    */
    public void setRADContainer (RADVisualContainer cont) {
        if (cont == null) { // deattaching the layout
            if ((designComponent != null) && (getMode() == DESIGN_MODE)) {
                getContainer().remove(designComponent);
            }
            if (formListener != null) {
                FormEditor.getFormSettings ().removePropertyChangeListener (formListener);
                formListener = null;
            }
            return;
        }

        super.setRADContainer(cont);
        if (cont != null) {
            if (formListener == null) {
                formListener = new PropertyChangeListener () {
                                   public void propertyChange (PropertyChangeEvent evt) {
                                       if ((evt.getPropertyName () == null) ||
                                               (evt.getPropertyName ().equals (FormLoaderSettings.PROP_SHOW_GRID)) ||
                                               (FormLoaderSettings.PROP_GRID_X.equals (evt.getPropertyName ())) ||
                                               (FormLoaderSettings.PROP_GRID_Y.equals (evt.getPropertyName ()))) {
                                           getContainerHelper ().validate();
                                           getContainerHelper ().repaint ();
                                       }
                                   }
                               };
                FormEditor.getFormSettings ().addPropertyChangeListener (formListener);
            }

            designComponent = new DesignAbsoluteComponent();
            realLayout = new EnhancedAbsoluteLayout();
            if (getMode() == DESIGN_MODE) {
                getContainer().setLayout(new BorderLayout());
                getContainer().add(designComponent, "Center"); // NOI18N
            } else {
                getContainer().setLayout(realLayout);
            }
        }
    }

    protected Container getContainerHelper () {
        return getContainer ();
    }

    /** Sets the current mode of this layout.
    * Descendants must override this method to provide any additional
    * functionality needed when layout mode is switched.
    * @see #DESIGN_MODE
    * @see #REAL_MODE
    * @see #getMode
    */
    public void setMode(int mode) {
        if (mode == getMode()) return;
        super.setMode(mode);
        if (mode == DESIGN_MODE) {
            getContainer().removeAll();
            RADVisualComponent[] children = getRADContainer().getSubComponents ();
            getContainer().setLayout(new BorderLayout());
            getContainer().add(designComponent, "Center"); // NOI18N
            for (int i=0; i< children.length; i++) {
                ConstraintsDescription cd = (ConstraintsDescription)children[i].getConstraints(DesignAbsoluteLayout.class);
                designComponent.add(getFormManager ().getVisualRepresentation (children[i]), cd.getConstraintsObject ());
            }
            getContainer().validate();
            getContainer().repaint ();
        }
        else {
            RADVisualComponent[] children = getRADContainer().getSubComponents ();
            designComponent.removeAll();
            getContainer().remove(designComponent);
            getContainer().setLayout(realLayout);
            for (int i=0; i< children.length; i++) {
                ConstraintsDescription cd = (ConstraintsDescription)children[i].getConstraints(DesignAbsoluteLayout.class);
                getContainer().add(getFormManager ().getVisualRepresentation (children[i]), cd.getConstraintsObject ());
            }
            getContainer().validate();
            getContainer().repaint ();
        }
    }
    /** Returns a constraint to be used for adding a component to the
    * specified position (e.g. in BorderLayout, the "North", "South", ...
    * will be determined by the position in the container).
    * The default implementation just returns the default constraints.
    * A special constraints object will be returned for layouts that
    * wish to set the position&size of the components rather than add
    * with constraints.
    * @param position The position within the container for which the
    *                 constraints should be returned.
    */
    public DesignLayout.ConstraintsDescription getConstraintsDescription(Point position) {
        if (FormEditor.getFormSettings ().getShowGrid () &&
                FormEditor.getFormSettings ().getApplyGridToPosition ())
        {
            int gridX = FormEditor.getFormSettings ().getGridX ();
            int gridY = FormEditor.getFormSettings ().getGridY ();
            int posX = position.x - (position.x % gridX);
            int posY = position.y - (position.y % gridY);

            return new AbsoluteConstraintsDescription (new Point (posX, posY));
        }
        else
            return new AbsoluteConstraintsDescription(position);
    }

    /** A display name of the layout will be used for displaying the layout in
    * the components hierarchy during design-time.
    * @return layout's display name.
    */
    public String getDisplayName() {
        return "AbsoluteLayout"; // NOI18N
    }

    /** Returns the layout's properties for specified component.
    * @param comp the RADVisualComponent of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getComponentProperties(RADVisualComponent comp) {

        ConstraintsDescription cd = comp.getConstraints(DesignAbsoluteLayout.class);
        if (cd == null) return new Node.Property[0];
        AbsoluteConstraints ac = (AbsoluteConstraints)cd.getConstraintsObject();
        if (ac == null) return new Node.Property[0];

        final RADVisualComponent component = comp;
        return new Node.Property[] {
                   new PropertySupport.ReadWrite (PROP_X, Integer.TYPE,
                                                  bundle.getString("PROP_absolute_x"), bundle.getString("HINT_absolute_x")) {
                       public Object getValue () throws IllegalAccessException,
                           IllegalArgumentException, InvocationTargetException {
                           ConstraintsDescription acd = component.getConstraints(DesignAbsoluteLayout.class);
                           return new Integer(((AbsoluteConstraints)acd.getConstraintsObject()).x);
                       }

                       public void setValue (Object val) throws IllegalAccessException,
                           IllegalArgumentException, InvocationTargetException {
                           if (val instanceof Integer) {
                               Object oldValue = getValue ();
                               AbsoluteConstraintsDescription acd =
                                   (AbsoluteConstraintsDescription)component.getConstraints(DesignAbsoluteLayout.class);
                               acd.position.x = ((Integer)val).intValue();
                               Component visual = getFormManager ().getVisualRepresentation (component);
                               designComponent.remove(visual);
                               designComponent.add(visual, acd.getConstraintsObject());
                               designComponent.validate ();
                               firePropertyChange (component, PROP_X, oldValue, val);
                           }
                           else throw new IllegalArgumentException();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_Y, Integer.TYPE,
                                                  bundle.getString("PROP_absolute_y"), bundle.getString("HINT_absolute_y")) {

                       public Object getValue () throws IllegalAccessException,
                           IllegalArgumentException, InvocationTargetException {
                           ConstraintsDescription acd = component.getConstraints(DesignAbsoluteLayout.class);
                           return new Integer(((AbsoluteConstraints)acd.getConstraintsObject()).y);
                       }

                       public void setValue (Object val) throws IllegalAccessException,
                           IllegalArgumentException, InvocationTargetException {
                           if (val instanceof Integer) {
                               Object oldValue = getValue ();
                               AbsoluteConstraintsDescription acd =
                                   (AbsoluteConstraintsDescription)component.getConstraints(DesignAbsoluteLayout.class);
                               acd.position.y = ((Integer)val).intValue();
                               Component visual = getFormManager ().getVisualRepresentation (component);
                               designComponent.remove(visual);
                               designComponent.add(visual, acd.getConstraintsObject());
                               designComponent.validate ();
                               firePropertyChange (component, PROP_Y, oldValue, val);
                           }
                           else throw new IllegalArgumentException();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_WIDTH, Integer.TYPE,
                                                  bundle.getString("PROP_absolute_width"), bundle.getString("HINT_absolute_width")) {

                       public Object getValue () throws IllegalAccessException,
                           IllegalArgumentException, InvocationTargetException {
                           ConstraintsDescription acd = component.getConstraints(DesignAbsoluteLayout.class);
                           return new Integer(((AbsoluteConstraints)acd.getConstraintsObject()).width);
                       }

                       public void setValue (Object val) throws IllegalAccessException,
                           IllegalArgumentException, InvocationTargetException {
                           if (val instanceof Integer) {
                               Object oldValue = getValue ();
                               AbsoluteConstraintsDescription acd =
                                   (AbsoluteConstraintsDescription)component.getConstraints(DesignAbsoluteLayout.class);
                               acd.size.width = ((Integer)val).intValue();
                               Component visual = getFormManager ().getVisualRepresentation (component);
                               designComponent.remove(visual);
                               designComponent.add(visual, acd.getConstraintsObject());
                               designComponent.validate ();
                               firePropertyChange (component, PROP_WIDTH, oldValue, val);
                           }
                           else throw new IllegalArgumentException();
                       }

                       public PropertyEditor getPropertyEditor () {
                           return new SizeEditor ();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_HEIGHT, Integer.TYPE,
                                                  bundle.getString("PROP_absolute_height"), bundle.getString("HINT_absolute_height")) {

                       public Object getValue () throws IllegalAccessException,
                           IllegalArgumentException, InvocationTargetException {
                           ConstraintsDescription acd = component.getConstraints(DesignAbsoluteLayout.class);
                           return new Integer(((AbsoluteConstraints)acd.getConstraintsObject()).height);
                       }

                       public void setValue (Object val) throws IllegalAccessException,
                           IllegalArgumentException, InvocationTargetException {
                           if (val instanceof Integer) {
                               Object oldValue = getValue ();
                               AbsoluteConstraintsDescription acd =
                                   (AbsoluteConstraintsDescription)component.getConstraints(DesignAbsoluteLayout.class);
                               acd.size.height = ((Integer)val).intValue();
                               Component visual = getFormManager ().getVisualRepresentation (component);
                               designComponent.remove(visual);
                               designComponent.add(visual, acd.getConstraintsObject());
                               designComponent.validate ();
                               firePropertyChange (component, PROP_HEIGHT, oldValue, val);
                           }
                           else throw new IllegalArgumentException();
                       }

                       public PropertyEditor getPropertyEditor () {
                           return new SizeEditor ();
                       }
                   }
               };
    }

    /** Returns a class of the layout that this DesignLayout represents (e.g.
    * returns FlowLayout.class from DesignFlowLayout).
    * @return a class of the layout represented by this DesignLayout or null if the 
    *         design layout does not represent a "real" layout (e.g. support layouts for JTabbedPane, ...)
    */
    public Class getLayoutClass() {
        return AbsoluteLayout.class;
    }

    // -----------------------------------------------------------------------------
    // Child components management

    /** Adds specified component to this layout. The constraints for the component
    * are acquired from the component by method getConstraints().
    * If the getConstraints() returns null, a new constraints should be
    * synthesized, and set in the component via setConstraints().
    * @param comp The component to add
    * @see RADVisualComponent#getConstraints
    * @see RADVisualComponent#setConstraints
    * @see #removeComponent
    */
    public void addComponent (RADVisualComponent comp) {
        ConstraintsDescription cd = comp.getConstraints(DesignAbsoluteLayout.class);
        if (cd == null) {
            cd = new AbsoluteConstraintsDescription (new Point(0, 0));
            comp.setConstraints(DesignAbsoluteLayout.class, cd);
        }
        AbsoluteConstraints constr = (AbsoluteConstraints)cd.getConstraintsObject();
        Component visual = getFormManager ().getVisualRepresentation (comp);
        if (getMode() == DESIGN_MODE) {
            designComponent.add(visual, constr);
        } else {
            getContainer().add(visual, constr);
        }
    }

    /** Removes specified component from this layout.
    * @param comp The component to remove
    * @see #addComponent
    */
    public void removeComponent (RADVisualComponent comp) {
        Component visual = getFormManager ().getVisualRepresentation (comp);
        if (getMode() == DESIGN_MODE)
            designComponent.remove(visual);
        else
            getContainer().remove(visual);

    }

    /** Updates the layout - readds all the visual components to reflect any global or
    * ordering changes.
    */
    public void updateLayout () {
        if (getMode() == DESIGN_MODE)
            designComponent.removeAll ();
        else
            getContainer().removeAll ();

        RADVisualComponent[] children = getRADContainer ().getSubComponents ();
        for (int i=0; i < children.length; i++)
            addComponent (children[i]);
        if (getMode() == DESIGN_MODE) {
            designComponent.validate ();
            designComponent.repaint ();
        } else {
            getContainer ().validate ();
            getContainer ().repaint ();
        }

    }

    // -----------------------------------------------------------------------------
    // Drag'n'drop support

    /** A design layout that supports moving components should redefine
    * this method and return true.
    * @return true if the design layout supports moving, false otherwise
    * @see #moveTo
    */
    public boolean canMove () {
        return (getMode () == DESIGN_MODE);
    }

    /** A design layout that supports moving components should redefine
    * this method to modify the constraints according to the supplied
    * delta positions (difference between the old and the new position).
    * @param desc    the constraints to move from
    * @param deltaX  the delat move in x axis
    * @param deltaY  the delta move in y axis
    * @param hotSpot The hotspot on the component (the point where the user started
    *                the drag on the component)
    * @return        the new modified ConstraintsDescription
    * @see #canMove
    */
    public ConstraintsDescription moveTo (ConstraintsDescription desc, int deltaX, int deltaY, Point hotSpot) {
        Dimension size = ((AbsoluteConstraintsDescription)desc).size;
        if (FormEditor.getFormSettings ().getShowGrid () &&
                FormEditor.getFormSettings ().getApplyGridToPosition ())
        {
            int gridX = FormEditor.getFormSettings ().getGridX ();
            int gridY = FormEditor.getFormSettings ().getGridY ();

            int posX = deltaX + gridX/2 - ((deltaX + gridX/2) % gridX);
            int posY = deltaY + gridY/2 - ((deltaY + gridY/2) % gridY);

            return new AbsoluteConstraintsDescription (new Point (posX, posY),
                    new Dimension (size.width, size.height));
        }
        else
            return new AbsoluteConstraintsDescription (new Point (deltaX, deltaY),
                    new Dimension (size.width, size.height));
    }

    /** A design layout that supports resizing components should redefine
    * this method and return true.
    * The resizing includes a initial drag-resize when adding a new component.
    * @return true if the design layout supports resizing, false otherwise
    * @see #resizeTo
    */
    public boolean canResize () {
        return (getMode () == DESIGN_MODE);
    }

    /** A design layout that supports resizing components should redefine
    * this method to modify the constraints according to the supplied
    * new size.
    * @param desc    the constraints to resize from
    * @param width   the width to resize to
    * @param height  the height to resize to
    * @return        the new modified ConstraintsDescription
    * @see #canResize
    */
    public ConstraintsDescription resizeTo (ConstraintsDescription desc, int width, int height) {
        Point position = ((AbsoluteConstraintsDescription)desc).position;
        if (FormEditor.getFormSettings ().getShowGrid () &&
                FormEditor.getFormSettings ().getApplyGridToSize ())
        {
            int gridX = FormEditor.getFormSettings ().getGridX ();
            int gridY = FormEditor.getFormSettings ().getGridY ();
            int newWidth = width - (width % gridX);
            int newHeight = height - (height % gridY);

            return new AbsoluteConstraintsDescription (new Point (position.x, position.y),
                    new Dimension (newWidth, newHeight));
        }
        else
            return new AbsoluteConstraintsDescription (new Point (position.x, position.y),
                    new Dimension (width, height));
    }

    /** A design layout that supports *BOTH* resizing and moving components
    * should redefine this method to modify the constraints according to the supplied
    * new bounds.
    * @param desc the constraints to resize from
    * @param bounds the bounds to resize to
    * @see #canMove
    * @see #canResize
    */
    public ConstraintsDescription resizeToBounds (ConstraintsDescription desc, Rectangle bounds) {
        if (FormEditor.getFormSettings ().getShowGrid () &&
                FormEditor.getFormSettings ().getApplyGridToSize ())
        {
            int gridX = FormEditor.getFormSettings ().getGridX ();
            int gridY = FormEditor.getFormSettings ().getGridY ();
            int posX = bounds.x - (bounds.x % gridX);
            int posY = bounds.y - (bounds.y % gridY);

            int posW = bounds.x + bounds.width;
            int posH = bounds.y + bounds.height;
            posW = posW - (posW % gridX);
            posH = posH - (posH % gridY);

            int newWidth = posW - posX;
            int newHeight = posH - posY;

            return new AbsoluteConstraintsDescription (new Point (posX, posY),
                    new Dimension (newWidth, newHeight));
        }
        else
            return new AbsoluteConstraintsDescription (new Point (bounds.x, bounds.y),
                    new Dimension (bounds.width, bounds.height));
    }

    /** Called to inform the Layout that it should provide a design-time visual
    * feedback for a "move" action of the specified comp to the specified constraints.
    * @param comp The RADVisualComponent that is being dragged
    * @param desc The "drag to" constraints or null for ending the marking operation
    */
    public void markMoveTo (RADVisualComponent comp, ConstraintsDescription desc) {
        if (getMode() == DESIGN_MODE) {
            if (desc == null) // clearing the mark
                designComponent.setMarkRect (null);
            else {
                AbsoluteConstraints ac = (AbsoluteConstraints) desc.getConstraintsObject ();
                int width, height;

                Component visual = comp.getComponent ();
                Dimension size = visual.getSize ();
                width = ac.width;
                if (width == -1)
                    width = size.width;
                height = ac.height;
                if (height == -1)
                    height = size.height;

                designComponent.setMarkRect (new Rectangle (ac.x, ac.y, width, height));
            }
        }
    }


    /** Called to inform the Layout that it should provide a design-time visual
    * feedback for a "resize" action to the specified constraints.
    * @param desc The "resize to" constraints or null to notify about cancelling the resize operation
    */
    public void markResizeTo (ConstraintsDescription desc) {
        if (getMode() == DESIGN_MODE) {
            if (desc == null) // clearing the mark
                designComponent.setMarkRect (null);
            else {
                AbsoluteConstraints ac = (AbsoluteConstraints) desc.getConstraintsObject ();
                if ((ac.width != -1) && (ac.height != -1))
                    designComponent.setMarkRect (new Rectangle (ac.x, ac.y, ac.width, ac.height));
            }
        }
    }

    // -----------------------------------------------------------------------------
    // Code generation

    /** Generates the code for initialization of this layout, e.g. panel1.setLayout (new BorderLayout ());.
    * @param cont   The container that is managed by this layout
    * @return the init code for the layout or null if it should not be generated
    */
    public String generateInitCode(RADVisualContainer cont) {
        LayoutManager defaultLM = null;
        try {
            Container defaultCont = (Container)BeanSupport.getDefaultInstance (cont.getBeanClass ());
            if (defaultCont != null) defaultLM = defaultCont.getLayout ();

            if ((defaultLM == null) && FormEditor.getFormSettings ().isNullLayout ()) {
                return null; // the default layout is the same as current settings => no need to generate layout
            }
        } catch (Exception e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
            // ok, no default
        }
        if (defaultLM != null) {
            if (defaultLM.getClass ().equals (AbsoluteLayout.class)) {
                if (!FormEditor.getFormSettings ().isNullLayout ()) {
                    return null; // the default layout is the same as current settings => no need to generate layout
                }
            }
        }

        StringBuffer buf = new StringBuffer();
        buf.append(createContainerGenName(cont));
        if (FormEditor.getFormSettings ().isNullLayout ()) { // [PENDING - layout's generateNullLayout property instead ???]
            buf.append("setLayout (null);\n"); // NOI18N
        } else {
            buf.append("setLayout (new org.netbeans.lib.awtextra.AbsoluteLayout ());\n"); // NOI18N
        }

        return buf.toString();
    }

    /** Generates the code for adding specified component to this layout.
    * @param comp   The component to be added to this layout
    * @param cont   The container that is managed by this layout
    */
    public String generateComponentCode(RADVisualContainer cont, RADVisualComponent comp) {
        ConstraintsDescription cd = comp.getConstraints(DesignAbsoluteLayout.class);
        if (cd == null) return "// "+bundle.getString ("MSG_ErrorGeneratingCode");
        AbsoluteConstraints ac = (AbsoluteConstraints)cd.getConstraintsObject();
        if (ac == null) return "// "+bundle.getString ("MSG_ErrorGeneratingCode");

        StringBuffer buf = new StringBuffer();
        if (FormEditor.getFormSettings ().isNullLayout ()) { // [PENDING - layout's generateNullLayout property instead ???]
            buf.append(createContainerGenName(cont));
            buf.append("add ("); // NOI18N
            buf.append(comp.getName());
            buf.append(");\n"); // NOI18N
            buf.append(comp.getName());
            if ((ac.getWidth () == -1) && (ac.getHeight () == -1)) { // preferred size used  ==>> generate setLocations and setSize
                buf.append(".setLocation ("); // NOI18N
                buf.append(ac.x);
                buf.append(", "); // NOI18N
                buf.append(ac.y);
                buf.append(");\n"); // NOI18N
                buf.append(comp.getName());
                buf.append(".setSize ("); // NOI18N
                buf.append(comp.getName());
                buf.append(".getPreferredSize ());\n"); // NOI18N
            } else { // not complete preferred size, either both width and height or one of them is specified ==>> generate setBounds
                buf.append(".setBounds ("); // NOI18N
                buf.append(ac.x);
                buf.append(", "); // NOI18N
                buf.append(ac.y);
                buf.append(", "); // NOI18N
                if (ac.width != -1) {
                    buf.append(ac.width);
                } else {
                    buf.append(comp.getName());
                    buf.append(".getPreferredSize ().width"); // NOI18N
                }
                buf.append(", "); // NOI18N
                if (ac.height != -1) {
                    buf.append(ac.height);
                } else {
                    buf.append(comp.getName());
                    buf.append(".getPreferredSize ().height"); // NOI18N
                }
                buf.append(");\n"); // NOI18N
            }
        } else {
            buf.append(createContainerGenName(cont));
            buf.append("add ("); // NOI18N
            buf.append(comp.getName());
            buf.append(", new org.netbeans.lib.awtextra.AbsoluteConstraints ("); // NOI18N
            buf.append(ac.x);
            buf.append(", "); // NOI18N
            buf.append(ac.y);
            buf.append(", "); // NOI18N
            buf.append(ac.width);
            buf.append(", "); // NOI18N
            buf.append(ac.height);
            buf.append("));\n"); // NOI18N
        }

        return buf.toString();
    }

    // -----------------------------------------------------------------------------
    // Serialization

    /** Writes the object to the stream.
    * @param oo output stream to write to
    * @exception IOException Includes any I/O exceptions that may occur
    */
    public void writeExternal (java.io.ObjectOutput oo)
    throws java.io.IOException {
        // store version
        oo.writeObject (nbClassVersion);
    }

    /** Reads the object from stream.
    * @param oi input stream to read from
    * @exception IOException Includes any I/O exceptions that may occur
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    public void readExternal (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        org.netbeans.modules.form.FormUtils.DEBUG(">> DesignAbsoluteLayout: readExternal: START"); // NOI18N
        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);
        org.netbeans.modules.form.FormUtils.DEBUG("<< DesignAbsoluteLayout: readExternal: END"); // NOI18N
    }

    // -----------------------------------------------------------------------------
    // constraints innerclass

    /** The ConstraintsDescription class encapsulates constraints data and
    * operations on a constraints that will be used for adding components
    * to the layout.
    */
    final public static class AbsoluteConstraintsDescription extends DesignLayout.ConstraintsDescription {
        /** A JDK 1.1. serial version UID */
        static final long serialVersionUID = 1265127948437909789L;

        /** Netbeans class version */
        public static final NbVersion nbClassVersion = new NbVersion (1, 0);

        /** For externalization only */
        public AbsoluteConstraintsDescription() {
        }

        public AbsoluteConstraintsDescription(Point pos) {
            this (pos, new Dimension (-1, -1));
        }

        public AbsoluteConstraintsDescription(Point pos, Dimension dim) {
            position = pos;
            size = dim;
        }

        public String getConstraintsString() {
            return "[x="+position.x+", y="+position.y+", width="+size.width+", height="+size.height+"]"; // NOI18N
        }

        public Object getConstraintsObject() {
            return new AbsoluteConstraints (position, size);
        }

        // -----------------------------------------------------------------------------
        // Serialization

        /** Writes the object to the stream.
        * @param oo output stream to write to
        * @exception IOException Includes any I/O exceptions that may occur
        */
        public void writeExternal (java.io.ObjectOutput oo)
        throws java.io.IOException {
            // store version
            oo.writeObject (nbClassVersion);

            oo.writeObject (size);
            oo.writeObject (position);
        }

        /** Reads the object from stream.
        * @param oi input stream to read from
        * @exception IOException Includes any I/O exceptions that may occur
        * @exception ClassNotFoundException if the class of the read object is not found
        */
        public void readExternal (java.io.ObjectInput oi)
        throws java.io.IOException, ClassNotFoundException {
            org.netbeans.modules.form.FormUtils.DEBUG(">> AbsoluteConstraintsDescription: readExternal: START"); // NOI18N
            // check the version
            NbVersion classVersion = (NbVersion) oi.readObject ();
            if (!nbClassVersion.isCompatible (classVersion))
                throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

            size = (Dimension) oi.readObject ();
            position = (Point) oi.readObject ();
            org.netbeans.modules.form.FormUtils.DEBUG("<< AbsoluteConstraintsDescription: readExternal: END"); // NOI18N
        }

        // -----------------------------------------------------------------------------
        // XML Persistence

        /** Called to load property value from specified XML subtree. If succesfully loaded,
        * the value should be available via the getValue method.
        * An IOException should be thrown when the value cannot be restored from the specified XML element
        * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
        * @exception IOException thrown when the value cannot be restored from the specified XML element
        */
        public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
            if (!XML_ABSOLUTE_CONSTRAINTS.equals (element.getNodeName ())) {
                throw new java.io.IOException ();
            }
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
            org.w3c.dom.Node xNode = attributes.getNamedItem (ATTR_X);
            org.w3c.dom.Node yNode = attributes.getNamedItem (ATTR_Y);
            org.w3c.dom.Node wNode = attributes.getNamedItem (ATTR_W);
            org.w3c.dom.Node hNode = attributes.getNamedItem (ATTR_H);
            if ((xNode != null) && (yNode != null)) position = new Point (Integer.parseInt (xNode.getNodeValue ()), Integer.parseInt (yNode.getNodeValue ()));
            if ((wNode != null) && (hNode != null)) size = new Dimension (Integer.parseInt (wNode.getNodeValue ()), Integer.parseInt (hNode.getNodeValue ()));
        }

        /** Called to store current property value into XML subtree. The property value should be set using the
        * setValue method prior to calling this method.
        * @param doc The XML document to store the XML in - should be used for creating nodes only
        * @return the XML DOM element representing a subtree of XML from which the value should be loaded or null 
        *         if the value does not need to save any additional data and can be created using the default constructor
        */
        public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
            org.w3c.dom.Element el = doc.createElement (XML_ABSOLUTE_CONSTRAINTS);
            el.setAttribute (ATTR_X, Integer.toString (position.x));
            el.setAttribute (ATTR_Y, Integer.toString (position.y));
            el.setAttribute (ATTR_W, Integer.toString (size.width));
            el.setAttribute (ATTR_H, Integer.toString (size.height));
            return el;
        }

        public static final String XML_ABSOLUTE_CONSTRAINTS = "AbsoluteConstraints"; // NOI18N
        public static final String ATTR_X = "x"; // NOI18N
        public static final String ATTR_Y = "y"; // NOI18N
        public static final String ATTR_W = "width"; // NOI18N
        public static final String ATTR_H = "height"; // NOI18N

        Dimension size;
        Point position;
    }

    // -----------------------------------------
    // inner classes

    /** The design component for AbsolutLayout */
    final public static class DesignAbsoluteComponent extends Container {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -6053097200817565615L;

        private Rectangle markRect = null;
        private Component markComponent = null;

        FormLoaderSettings formSettings = FormEditor.getFormSettings ();
        GridInfo grid;

        /** Constructs a new DesignAbsoluteComponent */
        public DesignAbsoluteComponent () {
            setLayout (new EnhancedAbsoluteLayout ());
            grid = new GridInfo (formSettings.getGridX (), formSettings.getGridY ());
        }

        static void drawDots (Graphics g, int x, int y, int width, int height) {
            // draw upper horizontal lines
            for (int i = x; i <= x + width; i+=2)
                g.drawLine (i, y, i, y);
            // draw left vertical lines
            for (int i = y; i <= y + height; i+=2)
                g.drawLine (x, i, x, i);

            int startX = x;
            if (height % 2 != 0) startX += 1;
            int startY = y;
            if (width % 2 != 0) startY += 1;

            // draw lower horizontal lines
            for (int i = startX; i <= x + width; i+=2)
                g.drawLine (i, y + height, i, y + height);
            // draw right vertical lines
            for (int i = startY; i <= y + height; i+=2)
                g.drawLine (x + width, i, x + width, i);
        }

        void setMarkRect (Rectangle rect) {
            if ((markRect == null) && (rect == null)) return;
            final int borderSize = FormEditor.getFormSettings ().getSelectionBorderSize ();
            if (markRect == null) {
                add (markComponent = new Component () {
                                         public void paint (Graphics g) {
                                             Dimension size = getSize ();
                                             g.setColor (FormEditor.getFormSettings ().getDragBorderColor ());
                                             for (int i = borderSize / 2; i < borderSize; i++)
                                                 drawDots (g, i, i, size.width - 2*i - 1, size.height - 2*i - 1);
                                         }

                                     },
                     new AbsoluteConstraints (rect.x - borderSize, rect.y - borderSize,
                                              rect.width + 2*borderSize, rect.height + 2*borderSize));
            }
            markRect = rect;
            if (markRect == null) {
                remove (markComponent);
            } else {
                remove (markComponent);
                add (markComponent,
                     new AbsoluteConstraints (rect.x - borderSize, rect.y - borderSize,
                                              rect.width + 2*borderSize, rect.height + 2*borderSize));
            }
            validate ();
            if (markRect == null) {
                repaint ();
            }

        }

        public void paint(Graphics g) {
            // paint grid
            if (formSettings.getShowGrid ()) {
                Dimension size = getSize ();
                if ((formSettings.getGridX () != grid.getGridX ()) ||
                        (formSettings.getGridY () != grid.getGridY ()))
                    grid = new GridInfo (formSettings.getGridX (), formSettings.getGridY ());

                FormUtils.paintGrid (this, g, grid, 0, 0, size.width, size.height);
            }

            super.paint(g);
        }

    }

    /** This enhancement recognizes "selected" components and layouts them bigger, so that
    * the selection border does not consume the inside area of the component
    */
    static class EnhancedAbsoluteLayout extends AbsoluteLayout {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 6256206967775931128L;

        /** Lays out the container in the specified panel.
        * @param parent the component which needs to be laid out
        */
        public void layoutContainer(Container parent) {
            int borderSize = FormEditor.getFormSettings ().getSelectionBorderSize ();
            for (java.util.Enumeration e = constraints.keys(); e.hasMoreElements();) {
                Component comp = (Component)e.nextElement();
                AbsoluteConstraints ac = (AbsoluteConstraints)constraints.get(comp);
                Dimension size = comp.getPreferredSize();
                int x = ac.x;
                int y = ac.y;
                int width = ac.getWidth ();
                if (width == -1) width = size.width;
                int height = ac.getHeight ();
                if (height == -1) height = size.height;
                /*        if (comp instanceof Selection) {
                          if (((Selection)comp).isSelected ()) {
                            if (ac.getWidth () != -1)
                              width += 2 * borderSize;
                            if (ac.getHeight () != -1)
                              height += 2 * borderSize;
                            x -= borderSize;
                            y -= borderSize;
                          }
                        }*/
                comp.setBounds(x, y, width, height);
            }
        }
    }

    // -----------------------------------------------------------------------------
    // Innerclasses

    public static class SizeEditor
                extends java.beans.PropertyEditorSupport
                implements EnhancedPropertyEditor
    {
        /** Display Names for alignment. */
        protected static final String NAME_PREFERRED = bundle.getString ("VALUE_absolute_preferred");

        /**
        * @return Returns custom property editor to be showen inside the property
        *         sheet.
        */
        public Component getInPlaceCustomEditor () {
            return null;
        }

        /**
        * @return true if this PropertyEditor provides an enhanced in-place custom
        *              property editor, false otherwise
        */
        public boolean hasInPlaceCustomEditor () {
            return false;
        }

        /**
        * @return true if this property editor provides tagged values and
        * a custom strings in the choice should be accepted too, false otherwise
        */
        public boolean supportsEditingTaggedValues () {
            return true;
        }

        /** @return names of the possible directions */
        public String[] getTags () {
            return new String[] { NAME_PREFERRED };
        }

        /** @return text for the current value */
        public String getAsText () {
            int value = ((Integer)getValue ()).intValue ();

            if (value == -1) return NAME_PREFERRED;
            else return "" +value; // NOI18N
        }

        /** Setter.
        * @param str string equal to one value from directions array
        */
        public void setAsText (String str) {
            if (str.equals (NAME_PREFERRED)) {
                setValue (new Integer (-1));
            }
            else {
                try {
                    setValue (new Integer (Integer.parseInt (str)));
                } catch (NumberFormatException e) {
                    // what can we do, ignore it...
                }
            }
        }

        public String getJavaInitializationString () {
            int value = ((Integer)getValue ()).intValue ();
            return "" + value; // NOI18N
        }
    }

    // -----------------------------------------
    // private area

    /** The real BorderLayout LayoutManager thaw works in real-layout mode*/
    transient private AbsoluteLayout realLayout;

    /** The layout-design mode component */
    transient private DesignAbsoluteComponent designComponent;

    /** The listener on FormSettings - for showGrid property changes */
    transient private PropertyChangeListener formListener;

    transient private boolean generateNullLayout = true;
}

/*
 * Log
 *  24   Gandalf   1.23        1/12/00  Ian Formanek    NOI18N
 *  23   Gandalf   1.22        12/14/99 Pavel Buzek     
 *  22   Gandalf   1.21        12/13/99 Pavel Buzek     copy/paste operations 
 *       handled properly (esp. painting)
 *  21   Gandalf   1.20        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  20   Gandalf   1.19        9/24/99  Ian Formanek    Smarter code generation 
 *       - fixes bug 4016 - The setLayout code should not be generated if the 
 *       layout is already set on the container to prevent loosing components 
 *       already on the panel.
 *  19   Gandalf   1.18        9/24/99  Ian Formanek    generateInitCode method 
 *       clarified
 *  18   Gandalf   1.17        9/15/99  Ian Formanek    Fixed bug 3858 - Form 
 *       with NullPointer layout still has the getContentPane ().setLayout (new 
 *       om.netbeans.developer.awt.AbsoluteLayout ()); line.
 *  17   Gandalf   1.16        9/12/99  Ian Formanek    Fixed bug 3530 - We do 
 *       not support having a null layout.
 *  16   Gandalf   1.15        9/9/99   Ian Formanek    AbsoluteLayout changes
 *  15   Gandalf   1.14        7/31/99  Ian Formanek    Cleaned up comments
 *  14   Gandalf   1.13        7/13/99  Ian Formanek    XML Persistence
 *  13   Gandalf   1.12        6/30/99  Ian Formanek    reflected change in 
 *       enhanced property editors
 *  12   Gandalf   1.11        6/27/99  Ian Formanek    Removed indent parameter
 *       from code generation methods
 *  11   Gandalf   1.10        6/10/99  Ian Formanek    Regeneration on layout 
 *       changes
 *  10   Gandalf   1.9         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         5/31/99  Ian Formanek    Design/Test Mode
 *  8    Gandalf   1.7         5/17/99  Ian Formanek    Generates code for old 
 *       AbsoluteLayout location (org.netbeans.lib.awtextra    instead of 
 *       org.netbeans.ide.awt) which is kinda ugly, but necessary because of 
 *       backward compatibility 
 *  7    Gandalf   1.6         5/15/99  Ian Formanek    
 *  6    Gandalf   1.5         5/12/99  Ian Formanek    
 *  5    Gandalf   1.4         5/11/99  Ian Formanek    Build 318 version
 *  4    Gandalf   1.3         5/10/99  Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  2    Gandalf   1.1         3/29/99  Ian Formanek    Uses FormUtils.DEBUG to 
 *       print messages
 *  1    Gandalf   1.0         3/28/99  Ian Formanek    
 * $
 */
