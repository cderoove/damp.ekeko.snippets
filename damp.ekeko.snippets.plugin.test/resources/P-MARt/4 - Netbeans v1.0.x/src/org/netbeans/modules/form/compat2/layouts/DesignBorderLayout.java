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
import java.util.Vector;
import java.lang.reflect.InvocationTargetException;

import org.netbeans.modules.form.util2.NbVersion;
import org.netbeans.modules.form.util2.NbVersionNotCompatibleException;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.netbeans.modules.form.*;

/** A design-time support for BorderLayout.
*
* @author   Ian Formanek
*/
final public class DesignBorderLayout extends DesignLayout {
    /** A JDK 1.1. serial version UID */
    static final long serialVersionUID = -5510213702084891937L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    /* Layout Properties */
    public static final String PROP_HGAP = "horizontalGap"; // NOI18N
    public static final String PROP_VGAP = "verticalGap"; // NOI18N

    /* Component Layout Properties */
    public static final String PROP_DIRECTION = FormEditor.LAYOUT_PREFIX + "direction"; // NOI18N

    /* Default layout property values */
    private static final int DEFAULT_VGAP = 0;
    private static final int DEFAULT_HGAP = 0;

    /** The width of the design component if no components were added into it */
    private static final int EMPTY_WIDTH = 120;
    /** The height of the design component if no components were added into it */
    private static final int EMPTY_HEIGHT = 70;

    private static final Color MARKED_COLOR = Color.blue;
    private static final Color BORDER_COLOR = Color.darkGray;

    private static final double HORIZONTAL_RATIO = 0.2;
    private static final double VERTICAL_RATIO = 0.2;

    /** bundle to obtain text information from */
    private static final java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle (DesignBorderLayout.class);

    /** icons for the Layout. */
    private static final Image icon = Toolkit.getDefaultToolkit ().getImage (
                                          DesignBorderLayout.class.getResource ("/org/netbeans/modules/form/resources/palette/borderLayout.gif")); // NOI18N
    private static final Image icon32 = Toolkit.getDefaultToolkit ().getImage (
                                            DesignBorderLayout.class.getResource ("/org/netbeans/modules/form/resources/palette/borderLayout32.gif")); // NOI18N

    /** The Sheet holding layout property sheet */
    private Sheet sheet;

    private Node.Property hgapProperty;
    private Node.Property vgapProperty;

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
            return;
        }

        super.setRADContainer(cont);

        if (cont != null) {
            designComponent = new DesignBorderComponent();
            realLayout = new BorderLayout(hgap, vgap);
            if (getMode() == DESIGN_MODE) {
                getContainer().setLayout(new BorderLayout());
                getContainer().add(designComponent, "Center"); // NOI18N
            }
            else
                getContainer().setLayout(realLayout);
        }
    }

    /** Method which allows the design layout to provide list of properties to be saved with the form.
    * @return list of Node.Property objects
    */
    public java.util.List getChangedProperties () {
        getPropertySet (); // enforce creation of properties
        java.util.ArrayList list = new java.util.ArrayList (2);
        if (hgap != DEFAULT_HGAP) list.add (hgapProperty);
        if (vgap != DEFAULT_VGAP) list.add (vgapProperty);
        return list;
    }

    /** Method which is called after the layout is loaded with the form to initialize its properties.
    * @param cahngedProperties map of <String, Object> pairs, where the String is a name of property and the Object its value
    */
    public void initChangedProperties (java.util.Map changedProperties) {
        Integer hgapVal = (Integer)changedProperties.get (PROP_HGAP); if (hgapVal != null) hgap = hgapVal.intValue ();
        Integer vgapVal = (Integer)changedProperties.get (PROP_VGAP); if (vgapVal != null) vgap = vgapVal.intValue ();
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
                Object constr = constraints.get(children[i]);
                Component visual = getFormManager ().getVisualRepresentation (children[i]);
                designComponent.add(visual, constr, 0);
            }
            getContainer ().validate ();
            getContainer ().repaint ();
        }
        else {
            RADVisualComponent[] children = getRADContainer().getSubComponents ();
            designComponent.removeAll();
            getContainer().remove(designComponent);
            getContainer().setLayout(realLayout);
            for (int i=0; i< children.length; i++) {
                Object constr = constraints.get(children[i]);
                Component visual = getFormManager ().getVisualRepresentation (children[i]);
                getContainer().add(visual, constr, 0);
            }
            getContainer ().validate ();
            getContainer ().repaint ();
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
        if (getMode() == REAL_MODE) {
            return findFreeDirection();
        }
        else {
            Dimension size = getContainer().getSize();
            Insets insets = getContainer().getInsets();
            size.width -= insets.left;
            size.width -= insets.right;
            size.height -= insets.top;
            size.height -= insets.bottom;
            int horizontalSize = (int) (DesignBorderLayout.HORIZONTAL_RATIO * size.width);
            int verticalSize = (int) (DesignBorderLayout.VERTICAL_RATIO * size.height);
            if (position.y < verticalSize) return north;
            else if (position.y > size.height-verticalSize) return south;
            else if (position.x < horizontalSize) return west;
            else if (position.x > size.width-horizontalSize) return east;
            else return center;
        }
    }

    /** A display name of the layout will be used for displaying the layout in
    * the components hierarchy during design-time.
    * @return layout's display name.
    */
    public String getDisplayName() {
        return "BorderLayout"; // NOI18N
    }

    /** Returns the global layout's properties (i.e. the properties
    * that are not different for different components in the layout
    * @return the global layout properties
    */
    public Node.PropertySet[] getPropertySet () {
        if (sheet == null) {
            sheet = new Sheet ();
            Sheet.Set set = Sheet.createPropertiesSet ();
            set.put (hgapProperty = new PropertySupport.ReadWrite (
                                        PROP_HGAP,
                                        Integer.TYPE,
                                        bundle.getString("PROP_border_hgap"),
                                        bundle.getString("HINT_border_hgap")
                                    ) {

                                        public Object getValue () {
                                            return new Integer(hgap);
                                        }

                                        public void setValue (Object val) throws IllegalArgumentException {
                                            if (val instanceof Integer) {
                                                int newValue = ((Integer)val).intValue();
                                                if (hgap == newValue)
                                                    return;
                                                int oldValue = hgap;
                                                hgap = newValue;
                                                realLayout.setHgap (hgap);
                                                firePropertyChange (null, PROP_HGAP, new Integer(oldValue), new Integer(hgap));
                                                getContainer().invalidate();
                                                getContainer().validate();
                                            }
                                            else throw new IllegalArgumentException();
                                        }

                                        public boolean supportsDefaultValue () {
                                            return true;
                                        }

                                        public void restoreDefaultValue () {
                                            try {
                                                setValue (new Integer (DEFAULT_HGAP));
                                            } catch (IllegalArgumentException e) { } // ignore failure
                                        }
                                    }
                    );

            set.put (vgapProperty = new PropertySupport.ReadWrite (
                                        PROP_VGAP,
                                        Integer.TYPE,
                                        bundle.getString("PROP_border_vgap"),
                                        bundle.getString("HINT_border_vgap")
                                    ) {

                                        public Object getValue () {
                                            return new Integer(vgap);
                                        }

                                        public void setValue (Object val) throws IllegalArgumentException {
                                            if (val instanceof Integer) {
                                                int newValue = ((Integer)val).intValue();
                                                if (vgap == newValue)
                                                    return;
                                                int oldValue = vgap;
                                                vgap = newValue;
                                                realLayout.setVgap (vgap);
                                                firePropertyChange (null, PROP_VGAP, new Integer(oldValue), new Integer(vgap));
                                                getContainer().invalidate();
                                                getContainer().validate();
                                            }
                                            else throw new IllegalArgumentException();
                                        }

                                        public boolean supportsDefaultValue () {
                                            return true;
                                        }

                                        public void restoreDefaultValue () {
                                            try {
                                                setValue (new Integer (DEFAULT_VGAP));
                                            } catch (IllegalArgumentException e) { } // ignore failure
                                        }
                                    }
                    );

            sheet.put (set);
        }

        return sheet.toArray ();
    }

    /** Returns the layout's properties for specified component.
    * @param comp the RADVisualComponent of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getComponentProperties(RADVisualComponent comp) {
        final RADVisualComponent component = comp;
        return new Node.Property[] {
                   new PropertySupport.ReadWrite (PROP_DIRECTION, String.class,
                                                  bundle.getString("PROP_bordercomp_direction"), bundle.getString("HINT_bordercomp_direction")) {

                       public Object getValue () {
                           BorderConstraintsDescription bcd = (BorderConstraintsDescription)component.getConstraints(DesignBorderLayout.class);
                           if (bcd == null) return null;
                           return bcd.getConstraintsString();
                       }

                       public void setValue (Object val) {
                           if (val instanceof String) {
                               Object oldValue = getValue ();
                               BorderConstraintsDescription bcd = null;
                               if ("Center".equals (val)) bcd = center; // NOI18N
                               else if ("North".equals (val)) bcd = north; // NOI18N
                               else if ("South".equals (val)) bcd = south; // NOI18N
                               else if ("West".equals (val)) bcd = west; // NOI18N
                               else if ("East".equals (val)) bcd = east; // NOI18N
                               if (component != null) {
                                   component.setConstraints(DesignBorderLayout.class, bcd);
                                   updateComponent(component);
                                   getContainer().validate();
                                   firePropertyChange (component, PROP_DIRECTION, oldValue, val);
                               } else {
                                   throw new IllegalArgumentException();
                               }
                           }
                           else throw new IllegalArgumentException();
                       }

                       /** Editor for directions. */
                       public java.beans.PropertyEditor getPropertyEditor () {
                           return new BorderDirectionEditor ();
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
        return BorderLayout.class;
    }

    // -----------------------------------------------------------------------------
    // Child components management

    /** @return a first free unused direction, or "Center" if no free is available */ // NOI18N
    private BorderConstraintsDescription findFreeDirection() {
        boolean isNorth = false, isSouth = false, isWest = false, isEast = false, isCenter = false;
        for (java.util.Enumeration e = constraints.elements(); e.hasMoreElements();) {
            String str = (String)e.nextElement();
            if (str.equals("North")) isNorth = true; // NOI18N
            else if (str.equals("South")) isSouth = true; // NOI18N
            else if (str.equals("West")) isWest = true; // NOI18N
            else if (str.equals("East")) isEast = true; // NOI18N
            else if (str.equals("Center")) isCenter = true; // NOI18N
        }
        if (!isNorth) return north;
        else if (!isWest) return west;
        else if (!isCenter) return center;
        else if (!isEast) return east;
        else if (!isSouth) return south;
        else return center;
    }

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
        ConstraintsDescription cd = comp.getConstraints(DesignBorderLayout.class);
        if (cd == null) {
            cd = findFreeDirection();
            comp.setConstraints (DesignBorderLayout.class, cd);
        }
        Object direction = cd.getConstraintsObject();
        Component visual = getFormManager ().getVisualRepresentation (comp);
        if (getMode() == DESIGN_MODE)
            designComponent.add(visual, direction, 0);
        else
            getContainer().add(visual, direction, 0);
        constraints.put(comp, direction);
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
            designComponent .validate ();
            designComponent .repaint ();
        } else {
            getContainer ().validate ();
            getContainer ().repaint ();
        }
    }

    /** Should be called when specified component's layout properties change.
    * @param comp The component to update
    */
    void updateComponent (RADVisualComponent comp) {
        Component visual = getFormManager ().getVisualRepresentation (comp);
        ConstraintsDescription cd = comp.getConstraints(DesignBorderLayout.class);
        if (cd == null) {
            cd = findFreeDirection();
            comp.setConstraints (DesignBorderLayout.class, cd);
        }

        Object direction = cd.getConstraintsObject();
        if (getMode() == DESIGN_MODE) {
            designComponent.remove(visual);
            designComponent.add(visual, direction, 0);
        } else {
            getContainer().remove(visual);
            getContainer().add(visual, direction, 0);
        }

        constraints.put(comp, direction);
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
        return getConstraintsDescription(new Point (deltaX + hotSpot.x, deltaY + hotSpot.y));
    }

    /** Called to inform the Layout that it should provide a design-time visual
    * feedback for a "move" action of the specified comp to the specified constraints.
    * @param comp The RADVisualComponent that is being dragged
    * @param desc The "drag to" constraints or null for ending the marking operation
    */
    public void markMoveTo (RADVisualComponent comp, ConstraintsDescription desc) {
        if (desc == null)
            designComponent.markDirection (null);
        else
            designComponent.markDirection ((String)desc.getConstraintsObject ());
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
        } catch (Exception e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
            // ok, no default
        }
        if (defaultLM != null) {
            if (defaultLM.getClass ().equals (BorderLayout.class)) {
                if ((((BorderLayout)defaultLM).getHgap () == hgap) && (((BorderLayout)defaultLM).getVgap () == vgap)) {
                    return null; // the default layout is the same as current settings => no need to generate layout
                }
            }
        }

        StringBuffer buf = new StringBuffer();
        buf.append(createContainerGenName(cont));
        if ((hgap != DEFAULT_HGAP) || (vgap != DEFAULT_VGAP)) {
            buf.append("setLayout (new java.awt.BorderLayout ("); // NOI18N
            buf.append(hgap);
            buf.append(", "); // NOI18N
            buf.append(vgap);
            buf.append("));\n"); // NOI18N
        }
        else {
            buf.append("setLayout (new java.awt.BorderLayout ());\n"); // NOI18N
        }
        return buf.toString();
    }

    /** Generates the code for adding specified component to this layout.
    * @param comp   The component to be added to this layout
    * @param cont   The container that is managed by this layout
    */
    public String generateComponentCode(RADVisualContainer cont, RADVisualComponent comp) {
        ConstraintsDescription cd = comp.getConstraints(DesignBorderLayout.class);
        String constr;
        if ((cd == null) || ((constr = (String)cd.getConstraintsObject()) == null))
            return "// "+FormEditor.getFormBundle ().getString ("MSG_ErrorGeneratingCode");

        StringBuffer buf = new StringBuffer();
        buf.append(createContainerGenName(cont));
        buf.append("add ("); // NOI18N
        buf.append(comp.getName());
        buf.append(", java.awt.BorderLayout."); // NOI18N
        buf.append(constr.toUpperCase ());
        buf.append(");\n"); // NOI18N

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

        oo.writeInt (vgap);
        oo.writeInt (hgap);
    }

    /** Reads the object from stream.
    * @param oi input stream to read from
    * @exception IOException Includes any I/O exceptions that may occur
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    public void readExternal (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        org.netbeans.modules.form.FormUtils.DEBUG(">> DesignBorderLayout: readExternal: START"); // NOI18N
        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

        vgap = oi.readInt ();
        hgap = oi.readInt ();
        org.netbeans.modules.form.FormUtils.DEBUG("<< DesignBorderLayout: readExternal: END"); // NOI18N
    }

    // -----------------------------------------------------------------------------
    // constraints innerclass

    /** The ConstraintsDescription class encapsulates constraints data and
    * operations on a constraints that will be used for adding components
    * to the layout.
    */
    final public static class BorderConstraintsDescription extends DesignLayout.ConstraintsDescription {
        /** A JDK 1.1. serial version UID */
        static final long serialVersionUID = -8892681661752848070L;

        /** Netbeans class version */
        public static final NbVersion nbClassVersion = new NbVersion (1, 0);

        /** For Externalization only */
        public BorderConstraintsDescription () {
        }

        public BorderConstraintsDescription(String dir) {
            direction = dir;
        }

        public String getConstraintsString() {
            return direction;
        }

        public Object getConstraintsObject() {
            return direction;
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

            oo.writeObject (direction);
        }

        /** Reads the object from stream.
        * @param oi input stream to read from
        * @exception IOException Includes any I/O exceptions that may occur
        * @exception ClassNotFoundException if the class of the read object is not found
        */
        public void readExternal (java.io.ObjectInput oi)
        throws java.io.IOException, ClassNotFoundException {
            org.netbeans.modules.form.FormUtils.DEBUG(">> BorderConstraintsDescription: readExternal: START"); // NOI18N
            // check the version
            NbVersion classVersion = (NbVersion) oi.readObject ();
            if (!nbClassVersion.isCompatible (classVersion))
                throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

            direction = (String) oi.readObject ();
            org.netbeans.modules.form.FormUtils.DEBUG("<< BorderConstraintsDescription: readExternal: END"); // NOI18N
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
            if (!XML_BORDER_CONSTRAINTS.equals (element.getNodeName ())) {
                throw new java.io.IOException ();
            }
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
            org.w3c.dom.Node dirNode = attributes.getNamedItem (ATTR_DIRECTION);
            if (dirNode != null) direction = dirNode.getNodeValue ();
        }

        /** Called to store current property value into XML subtree. The property value should be set using the
        * setValue method prior to calling this method.
        * @param doc The XML document to store the XML in - should be used for creating nodes only
        * @return the XML DOM element representing a subtree of XML from which the value should be loaded or null 
        *         if the value does not need to save any additional data and can be created using the default constructor
        */
        public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
            org.w3c.dom.Element el = doc.createElement (XML_BORDER_CONSTRAINTS);
            el.setAttribute (ATTR_DIRECTION, direction);
            return el;
        }

        public static final String XML_BORDER_CONSTRAINTS = "BorderConstraints"; // NOI18N
        public static final String ATTR_DIRECTION = "direction"; // NOI18N

        /** The direction represented by this BorderConstraintsDescription class */
        private String direction;
    }

    /** The design-mode version of border layout - the components span always only the
    * space for the direction they are in, so that the other directions are empty and
    * available for adding components.
    */
    final class XBorderLayout implements LayoutManager2 {
        public void addLayoutComponent (String name, Component comp) {
            if (BorderLayout.CENTER.equals(name)) {
                center.add (comp);
            } else if (BorderLayout.NORTH.equals(name)) {
                north.add (comp);
            } else if (BorderLayout.SOUTH.equals(name)) {
                south.add (comp);
            } else if (BorderLayout.EAST.equals(name)) {
                east.add (comp);
            } else if (BorderLayout.WEST.equals(name)) {
                west.add (comp);
            } else {
                throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + name); // NOI18N
            }
        }

        public void addLayoutComponent(Component comp, Object constraints) {
            if ((constraints == null) || (constraints instanceof String)) {
                addLayoutComponent((String)constraints, comp);
            } else {
                throw new IllegalArgumentException("cannot add to layout: constraint must be a string (or null)"); // NOI18N
            }
        }

        public void removeLayoutComponent (Component comp) {
            if (center.indexOf(comp)>-1) {
                center.remove (comp);
            } else if (north.indexOf(comp)>-1) {
                north.remove (comp);
            } else if (south.indexOf(comp)>-1) {
                south.remove (comp);
            } else if (east.indexOf(comp)>-1) {
                east.remove (comp);
            } else if (west.indexOf(comp)>-1) {
                west.remove (comp);
            }
        }

        public Dimension preferredLayoutSize (Container target) {
            Dimension dim = new Dimension(0, 0);
            Dimension d = new Dimension(0, 0);
            Dimension size = target.getSize();
            int horizontalSize = (int) (DesignBorderLayout.HORIZONTAL_RATIO * size.width);
            int verticalSize = (int) (DesignBorderLayout.VERTICAL_RATIO * size.height);
            if (east.size () > 0) {
                d = ((Component) east.get(east.size ()-1)).getPreferredSize();
                dim.width += d.width + hgap;
                dim.height = Math.max(d.height, dim.height);
            }
            if (west.size () > 0) {
                d = ((Component) west.get(west.size ()-1)).getPreferredSize();
                dim.width += d.width + hgap;
                dim.height = Math.max(d.height, dim.height);
            }
            if (center.size () > 0) {
                d = ((Component) center.get(center.size ()-1)).getPreferredSize();
                dim.width += d.width;
                dim.height = Math.max(d.height, dim.height);
            }
            if (north.size () > 0)
                d = ((Component) north.get(north.size ()-1)).getPreferredSize();
            else
                d = new Dimension(horizontalSize, verticalSize);
            dim.width = Math.max(d.width, dim.width);
            dim.height += d.height + vgap;
            if (south.size () > 0)
                d = ((Component) south.get (south.size ()-1)).getPreferredSize();
            else
                d = new Dimension(horizontalSize, verticalSize);
            dim.width = Math.max(d.width, dim.width);
            dim.height += d.height + vgap;

            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;

            return dim;
        }

        public Dimension minimumLayoutSize (Container target) {
            Dimension dim = new Dimension(0, 0);

            if ((east.size () > 0) && ((Component)east.get (east.size ()-1)).isVisible()) {
                Dimension d = ((Component) east.get(east.size ()-1)).getMinimumSize();
                dim.width += d.width + hgap;
                dim.height = Math.max(d.height, dim.height);
            }
            if ((west.size () > 0) && ((Component)west.get (west.size ()-1)).isVisible()) {
                Dimension d = ((Component) west.get(west.size ()-1)).getMinimumSize();
                dim.width += d.width + hgap;
                dim.height = Math.max(d.height, dim.height);
            }
            if ((center.size () > 0) && ((Component)center.get (center.size ()-1)).isVisible()) {
                Dimension d = ((Component) center.get(center.size ()-1)).getMinimumSize();
                dim.width += d.width;
                dim.height = Math.max(d.height, dim.height);
            }
            if ((north.size () > 0) && ((Component)north.get (north.size ()-1)).isVisible()) {
                Dimension d = ((Component) north.get(north.size ()-1)).getMinimumSize();
                dim.width = Math.max(d.width, dim.width);
                dim.height += d.height + vgap;
            }
            if ((south.size () > 0) && ((Component)south.get (south.size ()-1)).isVisible()) {
                Dimension d = ((Component) south.get (south.size ()-1)).getMinimumSize();
                dim.width = Math.max(d.width, dim.width);
                dim.height += d.height + vgap;
            }

            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;

            return dim;
        }

        public void layoutContainer (Container parent) {
            Dimension size = parent.getSize();
            int horizontalSize = (int) (DesignBorderLayout.HORIZONTAL_RATIO * size.width);
            int verticalSize = (int) (DesignBorderLayout.VERTICAL_RATIO * size.height);
            Component c;
            java.util.Iterator it;
            int i,n;
            for (i = 0; i<north.size (); i++){
                c = (Component) north.get (i);
                c.setLocation(1, 1);
                c.setSize(size.width - 2, verticalSize - 1);
            }
            for (i = 0; i<south.size (); i++){
                c = (Component) south.get (i);
                c.setLocation(1, size.height - verticalSize);
                c.setSize(size.width - 2, verticalSize - 1);
            }
            for (i = 0; i<west.size (); i++){
                c = (Component) west.get (i);
                c.setLocation(1, verticalSize + 1);
                c.setSize(horizontalSize - 1, size.height - 2*verticalSize - 2);
            }
            for (i = 0; i<east.size (); i++){
                c = (Component) east.get (i);
                c.setLocation(size.width - horizontalSize, verticalSize + 1);
                c.setSize(horizontalSize - 1, size.height - 2*verticalSize - 2);
            }
            for (i = center.size ()-1; i>-1; i--){
                c = (Component) center.get (i);
                c.setLocation(horizontalSize + 1, verticalSize + 1);
                c.setSize(size.width - 2*horizontalSize - 2, size.height - 2*verticalSize - 2);
            }
        }

        public Dimension maximumLayoutSize(Container target) {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        public float getLayoutAlignmentX(Container target) { return 0; }

        public float getLayoutAlignmentY(Container target) { return 0; }

        public void invalidateLayout(Container target) {}

        /**
         * @associates Component 
         */
        private Vector north = new Vector ();

        /**
         * @associates Component 
         */
        private Vector south = new Vector ();

        /**
         * @associates Component 
         */
        private Vector west = new Vector ();

        /**
         * @associates Component 
         */
        private Vector east = new Vector ();

        /**
         * @associates Component 
         */
        private Vector center = new Vector ();

    }

    // -----------------------------------------------------------------------------
    // design component innerclass

    /** The design component for BorderLayout - paints the N/S/W/E/C lines to mark
    * the directions.
    */
    final public class DesignBorderComponent extends Container {
        private String markedDir = null;

        static final long serialVersionUID =2803657361269012269L;
        /** Constructs a new DesignBorderComponent */
        public DesignBorderComponent() {
            setLayout (new DesignBorderLayout.XBorderLayout());
        }

        void markDirection (String dir) {
            markedDir = dir;
            repaint ();
        }

        public void paint(Graphics g) {
            Dimension size = getSize();
            int horizontalSize = (int) (DesignBorderLayout.HORIZONTAL_RATIO * size.width);
            int verticalSize = (int) (DesignBorderLayout.VERTICAL_RATIO * size.height);
            g.setColor(BORDER_COLOR);
            g.drawLine(0, verticalSize, size.width, verticalSize);
            g.drawLine(0, size.height - verticalSize - 1, size.width, size.height - verticalSize  - 1);
            g.drawLine(horizontalSize, verticalSize, horizontalSize, size.height - verticalSize  - 1);
            g.drawLine(size.width - horizontalSize - 1, verticalSize, size.width - horizontalSize - 1, size.height - verticalSize - 1);
            if (markedDir != null) {
                g.setColor (MARKED_COLOR);
                int posX, posY, posX2, posY2;
                if ("North".equals (markedDir)) { // NOI18N
                    posX = 0; posY = 0;
                    posX2 = size.width - 1; posY2 = verticalSize;
                } else if ("South".equals (markedDir)) { // NOI18N
                    posX = 0; posY = size.height - 1 - verticalSize;
                    posX2 = size.width - 1; posY2 = size.height - 1;
                } else if ("West".equals (markedDir)) { // NOI18N
                    posX = 0; posY = verticalSize;
                    posX2 = horizontalSize; posY2 = size.height - 1 - verticalSize;
                } else if ("East".equals (markedDir)) { // NOI18N
                    posX = size.width - 1 - horizontalSize; posY = verticalSize;
                    posX2 = size.width - 1; posY2 = size.height - 1 - verticalSize;
                } else {
                    posX = horizontalSize; posY = verticalSize;
                    posX2 = size.width - 1 - horizontalSize; posY2 = size.height - 1 - verticalSize;
                }
                g.drawLine(posX, posY, posX2, posY);
                g.drawLine(posX, posY, posX, posY2);
                g.drawLine(posX, posY2, posX2, posY2);
                g.drawLine(posX2, posY, posX2, posY2);
            }
            super.paint(g);
        }

        public Dimension getPreferredSize() {
            if (getComponentCount() == 0)
                return new Dimension (EMPTY_WIDTH, EMPTY_HEIGHT);
            Dimension dim = super.getPreferredSize();
            if (dim.width < EMPTY_WIDTH)
                dim.width = EMPTY_WIDTH;
            if (dim.height < EMPTY_HEIGHT)
                dim.height = EMPTY_HEIGHT;
            return dim;
        }

        public Dimension getMinimumSize() {
            Dimension dim = super.getMinimumSize();
            if (dim.width < EMPTY_WIDTH)
                dim.width = EMPTY_WIDTH;
            if (dim.height < EMPTY_HEIGHT)
                dim.height = EMPTY_HEIGHT;
            return dim;
        }
    }

    // -----------------------------------------------------------------------------
    // private area

    /** The real BorderLayout LayoutManager that works in real-layout mode*/
    transient private BorderLayout realLayout;

    /** The layout-design mode component */
    transient private DesignBorderComponent designComponent;

    // predefined constraints
    private BorderConstraintsDescription north = new BorderConstraintsDescription("North"); // NOI18N
    private BorderConstraintsDescription south = new BorderConstraintsDescription("South"); // NOI18N
    private BorderConstraintsDescription west = new BorderConstraintsDescription("West"); // NOI18N
    private BorderConstraintsDescription east = new BorderConstraintsDescription("East"); // NOI18N
    private BorderConstraintsDescription center = new BorderConstraintsDescription("Center"); // NOI18N

    /** Mapping <RADVisualComponent, String constraints> 
     * @associates Object*/
    private java.util.Hashtable constraints = new java.util.Hashtable();

    /** The vertical gap property */
    private int vgap = DEFAULT_VGAP;
    /** The horizontal gap property */
    private int hgap = DEFAULT_HGAP;
}

/*
 * Log
 *  23   Gandalf   1.22        1/12/00  Ian Formanek    NOI18N
 *  22   Gandalf   1.21        1/7/00   Pavel Buzek     display more than one 
 *       coponent in one direction
 *  21   Gandalf   1.20        12/14/99 Pavel Buzek     
 *  20   Gandalf   1.19        11/27/99 Patrik Knakal   
 *  19   Gandalf   1.18        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  18   Gandalf   1.17        9/24/99  Ian Formanek    Smarter code generation 
 *       - fixes bug 4016 - The setLayout code should not be generated if the 
 *       layout is already set on the container to prevent loosing components 
 *       already on the panel.
 *  17   Gandalf   1.16        9/24/99  Ian Formanek    generateInitCode method 
 *       clarified
 *  16   Gandalf   1.15        9/22/99  Ian Formanek    Fixed part of bug 3974 -
 *       Form Editor does not generate constants for properties, instead uses 
 *       hard-coded values.
 *  15   Gandalf   1.14        7/31/99  Ian Formanek    Cleaned up comments
 *  14   Gandalf   1.13        7/23/99  Ian Formanek    Fixed bug 2679 - An 
 *       exception in the compiler, after compiling ClockFrame, part2 or 
 *       MemoryView
 *  13   Gandalf   1.12        7/13/99  Ian Formanek    XML Persistence
 *  12   Gandalf   1.11        7/13/99  Ian Formanek    LayoutProperties support
 *       restoring default value, added changedProperties to support XML 
 *       Serialization
 *  11   Gandalf   1.10        6/27/99  Ian Formanek    Removed indent parameter
 *       from code generation methods
 *  10   Gandalf   1.9         6/10/99  Ian Formanek    Regeneration on layout 
 *       changes
 *  9    Gandalf   1.8         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    Gandalf   1.7         5/31/99  Ian Formanek    Design/Test Mode
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
