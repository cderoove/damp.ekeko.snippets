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

import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.netbeans.modules.form.util2.NbVersion;
import org.netbeans.modules.form.util2.NbVersionNotCompatibleException;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.netbeans.modules.form.*;

/** A design-time support for FlowLayout.
*
* @author   Ian Formanek
*/
final public class DesignFlowLayout extends DesignLayout implements java.io.Externalizable {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -8528054575046773692L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    /* Layout Properties */
    public static final String PROP_ALIGNMENT = "alignment"; // NOI18N
    public static final String PROP_HGAP = "horizontalGap"; // NOI18N
    public static final String PROP_VGAP = "verticalGap"; // NOI18N

    /* Default Property values */
    private static final int DEFAULT_VGAP = 5;
    private static final int DEFAULT_HGAP = 5;
    private static final int DEFAULT_ALIGNMENT = FlowLayout.CENTER;

    private Node.Property alignmentProperty;
    private Node.Property hgapProperty;
    private Node.Property vgapProperty;

    /** bundle to obtain text information from */
    private static final java.util.ResourceBundle bundle = NbBundle.getBundle (DesignFlowLayout.class);

    /** icons for the Layout. */
    private static final Image icon = Toolkit.getDefaultToolkit ().getImage (
                                          DesignFlowLayout.class.getResource ("/org/netbeans/modules/form/resources/palette/flowLayout.gif")); // NOI18N
    private static final Image icon32 = Toolkit.getDefaultToolkit ().getImage (
                                            DesignFlowLayout.class.getResource ("/org/netbeans/modules/form/resources/palette/flowLayout32.gif")); // NOI18N

    /** The Sheet holding layout property sheet */
    private Sheet sheet;

    /** Assigns this DesignLayout to the specified RADVisualContainer.
    * @param cont The RADVisualContainer that represents a container that will be
    *             managed by this layout or null as a notification that this layout
    *             is not a designLayout for its current container anymore
    */
    public void setRADContainer (RADVisualContainer cont) {
        super.setRADContainer(cont);
        if (cont != null) {
            getContainer().setLayout (realLayout = new FlowLayout());
            realLayout.setAlignment (alignment);
            realLayout.setHgap (hgap);
            realLayout.setVgap (vgap);
        }
    }

    /** Method which allows the design layout to provide list of properties to be saved with the form.
    * @return list of Node.Property objects
    */
    public java.util.List getChangedProperties () {
        getPropertySet (); // enforce creation of properties
        java.util.ArrayList list = new java.util.ArrayList (3);
        if (alignment != DEFAULT_ALIGNMENT) list.add (alignmentProperty);
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
        Integer alignmentVal = (Integer)changedProperties.get (PROP_ALIGNMENT); if (alignmentVal != null) alignment = alignmentVal.intValue ();
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

    /** @return an index that would be used to insert a new component to
     * specified position. The index is an first index after the existing component
     * under the posistion.
     */
    /*  private int findIndexForPosition (Point position) {
        Component[] components = getContainer ().getComponents ();
        if (components.length == 0) return 0;

        // find first component index with y-pos > position.y
        // this will become the boundary
        int biggerY = -1;
        for (int i = 0; i < components.length; i++)
          if (components[i].getLocation ().y > position.y) {
            biggerY = i;
            break;
          }
        // no component is below this position
        if (biggerY == -1) {
          // 1. check for adding below all components
          // find if the position is below lower edge of the last row of components
          int i;
          for (i = components.length -1; i >= 0; i--)
            if (components[i].getLocation ().y + components[i].getSize ().height > position.y) {
              break;
            }

          if (i == -1) // all components are above, so we are adding to the end
            return components.length;

          // 2. check for adding right of all components
          // to prevent the whole traversal, we check separately
          if (components [components.length - 1].getLocation ().x +
              components [components.length - 1].getSize ().width < position.x)
            return components.length;
          biggerY = components.length;
        }

        // find the component with the right x coordinates
        // we are searching from the last component with bigger y coordinates
        // to find the bottom-most component
        for (int i = biggerY - 1; i >= 0; i--) {
          Component comp = components [i];
          if (comp.getLocation ().x + comp.getSize ().width < position.x)
            return i + 1;
        }
        return components.length;
      } */

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
        return new FlowConstraintsDescription();
    }

    /** A display name of the layout will be used for displaying the layout in
    * the components hierarchy during design-time.
    * @return layout's display name.
    */
    public String getDisplayName() {
        return "FlowLayout"; // NOI18N
    }

    /** Returns the global layout's properties (i.e. the properties
    * that are not different for different components in the layout
    * @return the global layout properties
    */
    public Node.PropertySet[] getPropertySet() {
        if (sheet == null) {
            sheet = new Sheet ();
            Sheet.Set set = Sheet.createPropertiesSet ();
            set.put (alignmentProperty = new PropertySupport.ReadWrite (
                                             PROP_ALIGNMENT,
                                             Integer.TYPE,
                                             bundle.getString("PROP_flow_alignment"),
                                             bundle.getString("HINT_flow_alignment")
                                         ) {

                                             public Object getValue () {
                                                 return new Integer(alignment);
                                             }

                                             public void setValue (Object val) throws IllegalArgumentException {
                                                 if (val instanceof Integer) {
                                                     int newValue = ((Integer)val).intValue();
                                                     if (newValue == alignment) return;
                                                     int oldValue = alignment;
                                                     alignment = newValue;
                                                     realLayout.setAlignment (alignment);
                                                     getContainer().invalidate();
                                                     getContainer().validate();
                                                     firePropertyChange (null, PROP_ALIGNMENT, new Integer(oldValue), new Integer(alignment));
                                                 }
                                                 else throw new IllegalArgumentException();
                                             }

                                             /** Editor for alignment */
                                             public java.beans.PropertyEditor getPropertyEditor () {
                                                 return new FlowAlignmentEditor ();
                                             }

                                             public boolean supportsDefaultValue () {
                                                 return true;
                                             }

                                             public void restoreDefaultValue () {
                                                 try {
                                                     setValue (new Integer (DEFAULT_ALIGNMENT));
                                                 } catch (IllegalArgumentException e) { } // ignore failure
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
                                                getContainer().invalidate();
                                                getContainer().validate();
                                                firePropertyChange (null, PROP_HGAP, new Integer(oldValue), new Integer(hgap));
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
                                                getContainer().invalidate();
                                                getContainer().validate();
                                                firePropertyChange (null, PROP_VGAP, new Integer(oldValue), new Integer(vgap));
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

    /** Returns a class of the layout that this DesignLayout represents (e.g.
    * returns FlowLayout.class from DesignFlowLayout).
    * @return a class of the layout represented by this DesignLayout or null if the 
    *         design layout does not represent a "real" layout (e.g. support layouts for JTabbedPane, ...)
    */
    public Class getLayoutClass() {
        return FlowLayout.class;
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
            if (defaultLM.getClass ().equals (FlowLayout.class)) {
                if ((((FlowLayout)defaultLM).getHgap () == hgap) && (((FlowLayout)defaultLM).getVgap () == vgap) && (((FlowLayout)defaultLM).getAlignment () == alignment)) {
                    return null; // the default layout is the same as current settings => no need to generate layout
                }
            }
        }

        StringBuffer buf = new StringBuffer();
        String containerName = createContainerGenName(cont);
        buf.append(containerName);
        if (complexConstructor ()) {
            buf.append("setLayout (new java.awt.FlowLayout ("); // NOI18N
            buf.append (alignment);
            buf.append (", "); // NOI18N
            buf.append (hgap);
            buf.append (", "); // NOI18N
            buf.append (vgap);
            buf.append ("));\n"); // NOI18N
        } else {
            buf.append("setLayout (new java.awt.FlowLayout ());\n"); // NOI18N
        }

        return buf.toString();
    }

    private boolean complexConstructor () {
        return
            (alignment != DEFAULT_ALIGNMENT) ||
            (vgap != DEFAULT_VGAP) ||
            (hgap != DEFAULT_HGAP);
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

        oo.writeInt (alignment);
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
        org.netbeans.modules.form.FormUtils.DEBUG(">> DesignFlowLayout: readExternal: START"); // NOI18N
        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

        alignment = oi.readInt ();
        vgap = oi.readInt ();
        hgap = oi.readInt ();
        org.netbeans.modules.form.FormUtils.DEBUG("<< DesignFlowLayout: readExternal: END"); // NOI18N
    }

    // -----------------------------------------------------------------------------
    // constraints innerclass

    /** The ConstraintsDescription class encapsulates constraints data and
    * operations on a constraints that will be used for adding components
    * to the layout.
    */
    final public static class FlowConstraintsDescription extends DesignLayout.ConstraintsDescription implements java.io.Externalizable {
        /** A JDK 1.1. serial version UID */
        static final long serialVersionUID = 7307795910510106204L;

        /** Netbeans class version */
        public static final NbVersion nbClassVersion = new NbVersion (1, 0);

        /** Returns a textual descriptions of constraints represented by this
        * class. E.g. for BorderLayout, it is a text "Center" or "North".
        * @return textual descriptions of the constraints
        */
        public String getConstraintsString() {
            return bundle.getString ("MSG_flow_add");
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
            org.netbeans.modules.form.FormUtils.DEBUG(">> FlowConstraintsDescription: readExternal: START"); // NOI18N
            // check the version
            NbVersion classVersion = (NbVersion) oi.readObject ();
            if (!nbClassVersion.isCompatible (classVersion))
                throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);
            org.netbeans.modules.form.FormUtils.DEBUG("<< FlowConstraintsDescription: readExternal: END"); // NOI18N
        }
    }


    final public static class FlowAlignmentEditor extends java.beans.PropertyEditorSupport {
        /** Display Names for alignment. */
        private static final String[] names = {
            bundle.getString ("VALUE_flowalignment_center"),
            bundle.getString ("VALUE_flowalignment_left"),
            bundle.getString ("VALUE_flowalignment_right"),
        };

        /** @return names of the possible directions */
        public String[] getTags () {
            return names;
        }

        /** @return text for the current value */
        public String getAsText () {
            int value = ((Integer)getValue ()).intValue ();

            if (value == FlowLayout.CENTER) return names[0];
            else if (value == FlowLayout.LEFT) return names[1];
            else if (value == FlowLayout.RIGHT) return names[2];
            else return null;
        }

        /** Setter.
        * @param str string equal to one value from directions array
        */
        public void setAsText (String str) {
            if (names[0].equals (str))
                setValue (new Integer (FlowLayout.CENTER));
            else if (names[1].equals (str))
                setValue (new Integer (FlowLayout.LEFT));
            else if (names[2].equals (str))
                setValue (new Integer (FlowLayout.RIGHT));
        }

        public String getJavaInitializationString () {
            int value = ((Integer)getValue ()).intValue ();
            switch (value) {
            case FlowLayout.LEFT : return "java.awt.FlowLayout.LEFT"; // NOI18N
            case FlowLayout.RIGHT: return "java.awt.FlowLayout.RIGHT"; // NOI18N
            default: return "java.awt.FlowLayout.CENTER"; // NOI18N
            }
        }
    }

    // -----------------------------------------------------------------------------
    // private area

    /** The real FlowLayout LayoutManager that works in real-layout mode*/
    transient private FlowLayout realLayout;

    /** The alignment property */
    private int alignment = DEFAULT_ALIGNMENT;
    /** The vertical gap property */
    private int vgap = DEFAULT_VGAP;
    /** The horizontal gap property */
    private int hgap = DEFAULT_HGAP;
}

/*
 * Log
 *  18   Gandalf   1.17        1/12/00  Ian Formanek    NOI18N
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
 *  8    Gandalf   1.7         5/15/99  Ian Formanek    
 *  7    Gandalf   1.6         5/14/99  Ian Formanek    
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
