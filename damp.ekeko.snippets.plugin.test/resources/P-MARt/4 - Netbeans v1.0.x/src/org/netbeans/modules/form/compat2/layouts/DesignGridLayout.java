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

import org.netbeans.modules.form.util2.NbVersion;
import org.netbeans.modules.form.util2.NbVersionNotCompatibleException;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.netbeans.modules.form.*;

/** A design-time support for GridLayout.
*
* @author   Ian Formanek
*/
final public class DesignGridLayout extends DesignLayout {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -2244090640619613486L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    /* Layout Properties */
    public final static String PROP_ROWS = "rows"; // NOI18N
    public final static String PROP_COLUMNS = "columns"; // NOI18N
    public final static String PROP_HGAP = "horizontalGap"; // NOI18N
    public final static String PROP_VGAP = "verticalGap"; // NOI18N

    /* Default layout property values */
    private static final int DEFAULT_ROWS = 2;
    private static final int DEFAULT_COLUMNS = 3;
    private static final int DEFAULT_ROWS_GRID = 1; // the GridLayout's default
    private static final int DEFAULT_COLUMNS_GRID = 0; // the GridLayout's default
    private static final int DEFAULT_VGAP = 0;
    private static final int DEFAULT_HGAP = 0;

    /** The width of the design component if no components were added into it */
    private static final int EMPTY_WIDTH = 120;
    /** The height of the design component if no components were added into it */
    private static final int EMPTY_HEIGHT = 70;

    private static final Color MARKED_COLOR = Color.blue;
    private static final Color BORDER_COLOR = Color.darkGray;

    /** bundle to obtain text information from */
    private static final java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle (DesignGridLayout.class);

    /* icons for the Layout. */
    private static final Image icon = Toolkit.getDefaultToolkit ().getImage (
                                          DesignGridLayout.class.getResource ("/org/netbeans/modules/form/resources/palette/gridLayout.gif")); // NOI18N
    private static final Image icon32 = Toolkit.getDefaultToolkit ().getImage (
                                            DesignGridLayout.class.getResource ("/org/netbeans/modules/form/resources/palette/gridLayout32.gif")); // NOI18N

    /** The Sheet holding layout property sheet */
    private Sheet sheet;

    private Node.Property rowsProperty;
    private Node.Property columnsProperty;
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

    /** Method which allows the design layout to provide list of properties to be saved with the form.
    * @return list of Node.Property objects
    */
    public java.util.List getChangedProperties () {
        getPropertySet (); // enforce creation of properties
        java.util.ArrayList list = new java.util.ArrayList (4);
        if (rows != DEFAULT_ROWS) list.add (rowsProperty);
        if (columns != DEFAULT_COLUMNS) list.add (columnsProperty);
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
        Integer rowsVal = (Integer)changedProperties.get (PROP_ROWS); if (rowsVal != null) rows = rowsVal.intValue ();
        Integer columnsVal = (Integer)changedProperties.get (PROP_COLUMNS); if (columnsVal != null) columns = columnsVal.intValue ();
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
            designComponent = new DesignGridComponent();
            realLayout = new GridLayout(rows, columns, hgap, vgap);
            if (getMode() == DESIGN_MODE) {
                getContainer().setLayout(new BorderLayout());
                getContainer().add(designComponent, "Center"); // NOI18N
            }
            else
                getContainer().setLayout(realLayout);
        }
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
            for (int i=0; i< children.length; i++)
                designComponent.add(getFormManager ().getVisualRepresentation (children[i]), children[i].getComponentIndex ());

            getContainer ().validate ();
            getContainer ().repaint ();
        }
        else {
            RADVisualComponent[] children = getRADContainer().getSubComponents ();
            designComponent.removeAll();
            getContainer().remove(designComponent);
            getContainer().setLayout(realLayout);
            for (int i=0; i< children.length; i++)
                getContainer().add(getFormManager ().getVisualRepresentation (children[i]), children[i].getComponentIndex ());

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
        return new GridConstraintsDescription();
    }

    /** A display name of the layout will be used for displaying the layout in
    * the components hierarchy during design-time.
    * @return layout's display name.
    */
    public String getDisplayName() {
        return "GridLayout"; // NOI18N
    }

    /** Returns the global layout's properties (i.e. the properties
    * that are not different for different components in the layout
    * @return the global layout properties
    */
    public Node.PropertySet[] getPropertySet() {
        if (sheet == null) {
            sheet = new Sheet ();
            Sheet.Set set = Sheet.createPropertiesSet ();
            set.put (rowsProperty = new PropertySupport.ReadWrite (
                                        PROP_ROWS,
                                        Integer.TYPE,
                                        bundle.getString("PROP_grid_rows"),
                                        bundle.getString("HINT_grid_rows")
                                    ) {

                                        public Object getValue () {
                                            return new Integer(rows);
                                        }

                                        public void setValue (Object val) throws IllegalArgumentException {
                                            if (val instanceof Integer) {
                                                int newValue = ((Integer)val).intValue();
                                                if (newValue == rows) return;
                                                int oldValue = rows;
                                                rows = newValue;
                                                realLayout.setRows (rows);
                                                firePropertyChange (null, PROP_ROWS, new Integer(oldValue), new Integer(rows));
                                                if (getMode() == DESIGN_MODE) {
                                                    designComponent.invalidate();
                                                    designComponent.validate();
                                                    designComponent.repaint ();
                                                } else {
                                                    getContainer().invalidate();
                                                    getContainer().validate();
                                                }
                                            }
                                            else throw new IllegalArgumentException();
                                        }

                                        public boolean supportsDefaultValue () {
                                            return true;
                                        }

                                        public void restoreDefaultValue () {
                                            try {
                                                setValue (new Integer (DEFAULT_ROWS_GRID));
                                            } catch (IllegalArgumentException e) { } // what to do, ignore...
                                        }

                                    }
                    );

            set.put (columnsProperty = new PropertySupport.ReadWrite (
                                           PROP_COLUMNS,
                                           Integer.TYPE,
                                           bundle.getString("PROP_grid_columns"),
                                           bundle.getString("HINT_grid_columns")
                                       ) {

                                           public Object getValue () {
                                               return new Integer(columns);
                                           }

                                           public void setValue (Object val) throws IllegalArgumentException {
                                               if (val instanceof Integer) {
                                                   int newValue = ((Integer)val).intValue();
                                                   if (newValue == columns) return;
                                                   int oldValue = columns;
                                                   columns = newValue;
                                                   realLayout.setColumns (columns);
                                                   firePropertyChange (null, PROP_COLUMNS, new Integer(oldValue), new Integer(columns));
                                                   if (getMode() == DESIGN_MODE) {
                                                       designComponent.invalidate();
                                                       designComponent.validate();
                                                       designComponent.repaint ();
                                                   } else {
                                                       getContainer().invalidate();
                                                       getContainer().validate();
                                                   }
                                               }
                                               else throw new IllegalArgumentException();
                                           }

                                           public boolean supportsDefaultValue () {
                                               return true;
                                           }

                                           public void restoreDefaultValue () {
                                               try {
                                                   setValue (new Integer (DEFAULT_COLUMNS_GRID));
                                               } catch (IllegalArgumentException e) { } // what to do, ignore...
                                           }

                                       }
                    );

            set.put (hgapProperty = new PropertySupport.ReadWrite (
                                        PROP_HGAP,
                                        Integer.TYPE,
                                        bundle.getString("PROP_flow_hgap"),
                                        bundle.getString("HINT_flow_hgap")
                                    ) {

                                        public Object getValue () {
                                            return new Integer(hgap);
                                        }

                                        public void setValue (Object val) throws IllegalArgumentException {
                                            if (val instanceof Integer) {
                                                int newValue = ((Integer)val).intValue();
                                                if (newValue == hgap) return;
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
                                            } catch (IllegalArgumentException e) { } // what to do, ignore...
                                        }

                                    }
                    );

            set.put (vgapProperty = new PropertySupport.ReadWrite (
                                        PROP_VGAP,
                                        Integer.TYPE,
                                        bundle.getString("PROP_flow_vgap"),
                                        bundle.getString("HINT_flow_vgap")
                                    ) {

                                        public Object getValue () {
                                            return new Integer(vgap);
                                        }

                                        public void setValue (Object val) throws IllegalArgumentException {
                                            if (val instanceof Integer) {
                                                int newValue = ((Integer)val).intValue();
                                                if (newValue == vgap) return;
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
                                            } catch (IllegalArgumentException e) { } // what to do, ignore...
                                        }

                                    }
                    );

            sheet.put (set);
        }

        return sheet.toArray ();
    }

    /** Returns a class of the layout that this DesignLayout represents (e.g.
    * returns FlowLayout.class from DesignFlowLayout).
    * @return a class of the layout represented by this DesignLayout or null if the 
    *         design layout does not represent a "real" layout (e.g. support layouts for JTabbedPane, ...)
    */
    public Class getLayoutClass() {
        return GridLayout.class;
    }

    // -----------------------------------------------------------------------------
    // Child components management

    /** Adds specified component to this layout. The constraints for the component
    * are acquired from the component by method getConstraints().
    * If the getConstraints() returns null, a new constraints should be
    * synthesized, and set in the component via setConstraints().
    * @param node The component to add
    * @see RADVisualComponent#getConstraints
    * @see RADVisualComponent#setConstraints
    * @see #removeComponent
    */
    public void addComponent (RADVisualComponent comp) {
        if (getMode() == DESIGN_MODE) {
            designComponent.add(getFormManager ().getVisualRepresentation (comp), comp.getComponentIndex ());
        } else
            getContainer().add(getFormManager ().getVisualRepresentation (comp), comp.getComponentIndex ());
    }

    /** Removes specified component from this layout.
    * @param comp The component to remove
    * @see #addComponent
    */
    public void removeComponent (RADVisualComponent comp) {
        if (getMode() == DESIGN_MODE)
            designComponent.remove (getFormManager ().getVisualRepresentation (comp));
        else
            getContainer().remove (getFormManager ().getVisualRepresentation (comp));
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

    // -----------------------------------------------------------------------------
    // Drag'n'drop support

    // none for now

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
            if (defaultLM.getClass ().equals (GridLayout.class)) {
                if ((((GridLayout)defaultLM).getHgap () == hgap) && (((GridLayout)defaultLM).getVgap () == vgap) &&
                        (((GridLayout)defaultLM).getRows () == rows) && (((GridLayout)defaultLM).getColumns () == columns)) {
                    return null; // the default layout is the same as current settings => no need to generate layout
                }
            }
        }

        StringBuffer buf = new StringBuffer();
        buf.append(createContainerGenName(cont));
        if ((hgap != DEFAULT_HGAP) || (vgap != DEFAULT_VGAP)) {
            buf.append("setLayout (new java.awt.GridLayout ("); // NOI18N
            buf.append(rows);
            buf.append(", "); // NOI18N
            buf.append(columns);
            buf.append(", "); // NOI18N
            buf.append(hgap);
            buf.append(", "); // NOI18N
            buf.append(vgap);
            buf.append("));\n"); // NOI18N
        } else if ((rows != DEFAULT_ROWS_GRID) || (columns != DEFAULT_COLUMNS_GRID)) {
            buf.append("setLayout (new java.awt.GridLayout ("); // NOI18N
            buf.append(rows);
            buf.append(", "); // NOI18N
            buf.append(columns);
            buf.append("));\n"); // NOI18N
        }
        else {
            buf.append("setLayout (new java.awt.GridLayout ());\n"); // NOI18N
        }
        return buf.toString();
    }

    /** Generates the code for adding specified component to this layout.
    * @param comp   The component to be added to this layout
    * @param cont   The container that is managed by this layout
    */
    public String generateComponentCode(RADVisualContainer cont, RADVisualComponent comp) {
        StringBuffer buf = new StringBuffer();
        buf.append(createContainerGenName(cont));
        buf.append("add ("); // NOI18N
        buf.append(comp.getName());
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
        oo.writeInt (rows);
        oo.writeInt (columns);
    }

    /** Reads the object from stream.
    * @param oi input stream to read from
    * @exception IOException Includes any I/O exceptions that may occur
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    public void readExternal (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        org.netbeans.modules.form.FormUtils.DEBUG(">> DesignGridLayout: readExternal: START"); // NOI18N
        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

        vgap = oi.readInt ();
        hgap = oi.readInt ();
        rows = oi.readInt ();
        columns = oi.readInt ();
        org.netbeans.modules.form.FormUtils.DEBUG("<< DesignGridLayout: readExternal: END"); // NOI18N
    }

    // -----------------------------------------------------------------------------
    // constraints innerclass

    /** The ConstraintsDescription class encapsulates constraints data and
    * operations on a constraints that will be used for adding components
    * to the layout.
    */
    final public static class GridConstraintsDescription extends DesignLayout.ConstraintsDescription {
        /** A JDK 1.1. serial version UID */
        static final long serialVersionUID = -861824372886710699L;

        /** Netbeans class version */
        public static final NbVersion nbClassVersion = new NbVersion (1, 0);

        public String getConstraintsString() {
            return bundle.getString ("MSG_grid_add");
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
            org.netbeans.modules.form.FormUtils.DEBUG(">> GridConstraintsDescription: readExternal: START"); // NOI18N
            // check the version
            NbVersion classVersion = (NbVersion) oi.readObject ();
            if (!nbClassVersion.isCompatible (classVersion))
                throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);
            org.netbeans.modules.form.FormUtils.DEBUG("<< GridConstraintsDescription: readExternal: END"); // NOI18N
        }

    }

    // -----------------------------------------------------------------------------
    // design component & layout

    /** The design-mode version of border layout - the components span always only the
    * space for the direction they are in, so that the other directions are empty and
    * available for adding components.
    */
    final class XGridLayout implements LayoutManager2 {
        public void addLayoutComponent (String name, Component comp) {
        }

        public void addLayoutComponent(Component comp, Object constraints) {
        }

        public void removeLayoutComponent (Component comp) {
        }

        public Dimension preferredLayoutSize (Container parent) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int nrows = getRows (parent);
            int ncols = getColumns (parent);
            if (nrows > 0)
                ncols = (ncomponents + nrows - 1) / nrows;
            else
                nrows = (ncomponents + ncols - 1) / ncols;

            int w = 0;
            int h = 0;
            for (int i = 0 ; i < ncomponents ; i++) {
                Component comp = parent.getComponent(i);
                Dimension d = comp.getPreferredSize();
                if (w < d.width)
                    w = d.width;

                if (h < d.height)
                    h = d.height;
            }
            return new Dimension(insets.left + insets.right + ncols*w + (ncols-1)*hgap,
                                 insets.top + insets.bottom + nrows*h + (nrows-1)*vgap);
        }

        public Dimension minimumLayoutSize (Container parent) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int nrows = getRows (parent);
            int ncols = getColumns (parent);
            if (nrows > 0)
                ncols = (ncomponents + nrows - 1) / nrows;
            else
                nrows = (ncomponents + ncols - 1) / ncols;

            int w = 0;
            int h = 0;
            for (int i = 0 ; i < ncomponents ; i++) {
                Component comp = parent.getComponent(i);
                Dimension d = comp.getMinimumSize();
                if (w < d.width)
                    w = d.width;

                if (h < d.height)
                    h = d.height;
            }
            return new Dimension(insets.left + insets.right + ncols*w + (ncols-1)*hgap,
                                 insets.top + insets.bottom + nrows*h + (nrows-1)*vgap);
        }

        public void layoutContainer (Container parent) {
            Insets insets = parent.getInsets();
            int r = getRows (parent);
            int c = getColumns (parent);
            if (r == 0) r = 1;
            if (c == 0) c = 1;
            Component[] children = parent.getComponents ();
            Dimension size = parent.getSize();
            size.width -= (insets.left + insets.right);
            size.height -= (insets.top + insets.bottom);
            int horizontalSize = (int) (size.width / c);
            int verticalSize = (int) (size.height / r);

            int rowCount = 0;
            int colCount = 0;
            for (int i = 0; i < children.length; i++) {
                children[i].setBounds (insets.left + colCount*horizontalSize + 1, insets.top + rowCount*verticalSize + 1,
                                       horizontalSize-2, verticalSize-2);
                colCount++;
                if (colCount == c) {
                    colCount = 0;
                    rowCount++;
                }
            }
        }

        public Dimension maximumLayoutSize(Container target) {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        public float getLayoutAlignmentX(Container target) { return 0; }

        public float getLayoutAlignmentY(Container target) { return 0; }

        public void invalidateLayout(Container target) {}

        int getColumns (Container cont) {
            int ncomponents = cont.getComponentCount ();
            if (ncomponents > rows * columns)
                if (rows > 0)
                    return (ncomponents + rows - 1) / rows;
                else
                    return (ncomponents + columns - 1) / columns;
            else
                return columns;
        }

        int getRows (Container cont) {
            return rows;
        }

    }

    /** The design component for GridLayout - paints the grid lines to
    * mark the grid.
    */
    final public class DesignGridComponent extends Container {
        DesignGridLayout.XGridLayout layout;

        static final long serialVersionUID =-308586264121301723L;
        /** Constructs a new DesignGridComponent */
        public DesignGridComponent() {
            setLayout (layout = new DesignGridLayout.XGridLayout());
        }

        public void paint(Graphics g) {
            Dimension size = getSize();
            int rows = layout.getRows (this);
            int columns = layout.getColumns (this);
            if (rows == 0) rows = 1;
            if (columns == 0) columns = 1;
            int horizontalSize = (int) (size.width / columns);
            int verticalSize = (int) (size.height / rows);
            g.setColor(BORDER_COLOR);
            for (int i = 0; i < columns; i++)
                g.drawLine(i*horizontalSize, 0, i*horizontalSize, size.height-1);
            for (int i = 0; i < rows; i++)
                g.drawLine(0, i*verticalSize, size.width-1, i*verticalSize);

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

    /** The real GridLayout LayoutManager that works in real-layout mode*/
    transient private GridLayout realLayout;

    /** The layout-design mode component */
    transient private DesignGridComponent designComponent;

    /** The rows property */
    private int rows = DEFAULT_ROWS;
    /** The columns property */
    private int columns = DEFAULT_COLUMNS;
    /** The rows property */
    private int vgap = DEFAULT_VGAP;
    /** The columns property */
    private int hgap = DEFAULT_HGAP;
}

/*
 * Log
 *  21   Gandalf   1.20        1/12/00  Ian Formanek    NOI18N
 *  20   Gandalf   1.19        12/13/99 Pavel Buzek     copy/paste operations 
 *       handled properly (esp. painting)
 *  19   Gandalf   1.18        12/8/99  Pavel Buzek     
 *  18   Gandalf   1.17        11/27/99 Patrik Knakal   
 *  17   Gandalf   1.16        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   Gandalf   1.15        9/24/99  Ian Formanek    Smarter code generation 
 *       - fixes bug 4016 - The setLayout code should not be generated if the 
 *       layout is already set on the container to prevent loosing components 
 *       already on the panel.
 *  15   Gandalf   1.14        9/24/99  Ian Formanek    generateInitCode method 
 *       clarified
 *  14   Gandalf   1.13        7/31/99  Ian Formanek    Cleaned up comments
 *  13   Gandalf   1.12        7/23/99  Ian Formanek    Fixed bug 2679 - An 
 *       exception in the compiler, after compiling ClockFrame, part2 or 
 *       MemoryView
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
