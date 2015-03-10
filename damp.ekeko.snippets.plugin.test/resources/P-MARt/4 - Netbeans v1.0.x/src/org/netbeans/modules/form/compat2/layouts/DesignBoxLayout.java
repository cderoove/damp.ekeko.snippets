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
import javax.swing.BoxLayout;

import org.openide.util.NbBundle;
import org.netbeans.modules.form.util2.NbVersion;
import org.netbeans.modules.form.util2.NbVersionNotCompatibleException;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.netbeans.modules.form.*;

/** A design-time support for BoxLayout.
*
* @author   Ian Formanek
*/
final public class DesignBoxLayout extends DesignLayout {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -7988984798744164032L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    /* Layout Properties */
    public static final String PROP_AXIS = "axis"; // NOI18N

    /* Default Property values */
    private static final int DEFAULT_AXIS = BoxLayout.X_AXIS;

    /** bundle to obtain text information from */
    private static final java.util.ResourceBundle bundle = NbBundle.getBundle (DesignBoxLayout.class);

    /** icons for the Layout. */
    private static final Image icon = Toolkit.getDefaultToolkit ().getImage (
                                          DesignBoxLayout.class.getResource ("/org/netbeans/modules/form/resources/palette/boxLayout.gif")); // NOI18N
    private static final Image icon32 = Toolkit.getDefaultToolkit ().getImage (
                                            DesignBoxLayout.class.getResource ("/org/netbeans/modules/form/resources/palette/boxLayout32.gif")); // NOI18N

    /** The Sheet holding layout property sheet */
    private Sheet sheet;

    private Node.Property axisProperty;

    /** Assigns this DesignLayout to the specified RADVisualContainer.
    * @param cont The RADVisualContainer that represents a container that will be
    *             managed by this layout or null as a notification that this layout
    *             is not a designLayout for its current container anymore
    */
    public void setRADContainer (RADVisualContainer cont) {
        super.setRADContainer(cont);
        if (cont != null)
            getContainer().setLayout (realLayout = new BoxLayout(getContainer (), axis));
    }

    /** Method which allows the design layout to provide list of properties to be saved with the form.
    * @return list of Node.Property objects
    */
    public java.util.List getChangedProperties () {
        getPropertySet (); // enforce creation of properties
        java.util.ArrayList list = new java.util.ArrayList (1);
        if (axis != DEFAULT_AXIS) list.add (axisProperty);
        return list;
    }

    /** Method which is called after the layout is loaded with the form to initialize its properties.
    * @param cahngedProperties map of <String, Object> pairs, where the String is a name of property and the Object its value
    */
    public void initChangedProperties (java.util.Map changedProperties) {
        Integer axisVal = (Integer)changedProperties.get (PROP_AXIS); if (axisVal != null) axis = axisVal.intValue ();
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
        return new BoxConstraintsDescription();
    }

    /** A display name of the layout will be used for displaying the layout in
    * the components hierarchy during design-time.
    * @return layout's display name.
    */
    public String getDisplayName() {
        return "BoxLayout"; // NOI18N
    }

    /** Returns the global layout's properties (i.e. the properties
    * that are not different for different components in the layout
    * @return the global layout properties
    */
    public Node.PropertySet[] getPropertySet() {
        if (sheet == null) {
            sheet = new Sheet ();
            Sheet.Set set = Sheet.createPropertiesSet ();
            set.put (axisProperty = new PropertySupport.ReadWrite (
                                        PROP_AXIS,
                                        Integer.TYPE,
                                        bundle.getString("PROP_box_axis"),
                                        bundle.getString("HINT_box_axis")
                                    ) {

                                        public Object getValue () {
                                            return new Integer(axis);
                                        }

                                        public void setValue (Object val) throws IllegalArgumentException {
                                            if (val instanceof Integer) {
                                                int newValue = ((Integer)val).intValue();
                                                if (newValue == axis) return;
                                                int oldValue = axis;
                                                axis = newValue;
                                                realLayout = new BoxLayout(getContainerHelper (), axis);
                                                getContainerHelper().setLayout (realLayout);
                                                firePropertyChange (null, PROP_AXIS, new Integer(oldValue), new Integer(axis));
                                                getContainer().invalidate();
                                                getContainer().validate();
                                            }
                                            else throw new IllegalArgumentException();
                                        }

                                        /** Editor for axis */
                                        public java.beans.PropertyEditor getPropertyEditor () {
                                            return new BoxAxisEditor ();
                                        }

                                        public boolean supportsDefaultValue () {
                                            return true;
                                        }

                                        public void restoreDefaultValue () {
                                            try {
                                                setValue (new Integer (DEFAULT_AXIS));
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
        return BoxLayout.class;
    }

    Container getContainerHelper () {
        return super.getContainer ();
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
            if (defaultLM.getClass ().equals (BoxLayout.class)) {
                // [PENDING] PROBLEM: should check whether the axis is the same as on the default layout, as it might be necessary
                //                    to generate the setLayout in the other case, *but* there is no way how to find out the axis
                //                    in existing BoxLayout
                return null; // the default layout is the same as current settings => no need to generate layout
            }
        }

        StringBuffer buf = new StringBuffer();
        String containerName = createContainerGenName(cont);
        buf.append(containerName);
        buf.append("setLayout (new javax.swing.BoxLayout ("); // NOI18N

        if (cont instanceof RADVisualFormContainer) {
            String setOn = ((RADVisualFormContainer)cont).getFormInfo ().getContainerGenName ();
            if (setOn.endsWith (".")) { // NOI18N
                setOn = setOn.substring (0, setOn.length () - 1);
            }
            if ("".equals (setOn)) { // NOI18N
                setOn = "this"; // NOI18N
            }
            buf.append (setOn);
        }
        else {
            buf.append (cont.getName ());
        }
        buf.append (", "); // NOI18N
        buf.append (axis);
        buf.append ("));\n"); // NOI18N

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

        oo.writeInt (axis);
    }

    /** Reads the object from stream.
    * @param oi input stream to read from
    * @exception IOException Includes any I/O exceptions that may occur
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    public void readExternal (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        org.netbeans.modules.form.FormUtils.DEBUG(">> DesignBoxLayout: readExternal: START"); // NOI18N
        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

        axis = oi.readInt ();
        org.netbeans.modules.form.FormUtils.DEBUG("<< DesignBoxLayout: readExternal: END"); // NOI18N
    }

    // -----------------------------------------------------------------------------
    // constraints innerclass

    /** The ConstraintsDescription class encapsulates constraints data and
    * operations on a constraints that will be used for adding components
    * to the layout.
    */
    final public static class BoxConstraintsDescription extends DesignLayout.ConstraintsDescription {
        /** A JDK 1.1. serial version UID */
        static final long serialVersionUID = -4377441459692564533L;

        /** Netbeans class version */
        public static final NbVersion nbClassVersion = new NbVersion (1, 0);

        /** Returns a textual descriptions of constraints represented by this
        * class. E.g. for BorderLayout, it is a text "Center" or "North".
        * @return textual descriptions of the constraints
        */
        public String getConstraintsString() {
            return bundle.getString ("MSG_box_add");
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
            org.netbeans.modules.form.FormUtils.DEBUG(">> BoxConstraintsDescription: readExternal: START"); // NOI18N
            // check the version
            NbVersion classVersion = (NbVersion) oi.readObject ();
            if (!nbClassVersion.isCompatible (classVersion))
                throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);
            org.netbeans.modules.form.FormUtils.DEBUG("<< BoxConstraintsDescription: readExternal: END"); // NOI18N
        }
    }

    final public static class BoxAxisEditor extends java.beans.PropertyEditorSupport {
        /** Display Names for axis. */
        private static final String[] names = {
            bundle.getString ("VALUE_boxaxis_x"),
            bundle.getString ("VALUE_boxaxis_y"),
        };

        /** @return names of the possible directions */
        public String[] getTags () {
            return names;
        }

        /** @return text for the current value */
        public String getAsText () {
            int value = ((Integer)getValue ()).intValue ();

            if (value == BoxLayout.X_AXIS) return names[0];
            else if (value == BoxLayout.Y_AXIS) return names[1];
            else return null;
        }

        /** Setter.
        * @param str string equal to one value from directions array
        */
        public void setAsText (String str) {
            if (names[0].equals (str))
                setValue (new Integer (BoxLayout.X_AXIS));
            else if (names[1].equals (str))
                setValue (new Integer (BoxLayout.Y_AXIS));
        }
    }

    // -----------------------------------------------------------------------------
    // private area

    /** The real BoxLayout LayoutManager that works in real-layout mode*/
    transient private BoxLayout realLayout;

    /** The axis property */
    private int axis = DEFAULT_AXIS;
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
 *  12   Gandalf   1.11        7/15/99  Ian Formanek    Fixed bug 2238 - Setting
 *       layout to BoxLayout generates incorrect code
 *  11   Gandalf   1.10        7/13/99  Ian Formanek    LayoutProperties support
 *       restoring default value, added changedProperties to support XML 
 *       Serialization
 *  10   Gandalf   1.9         6/27/99  Ian Formanek    Removed indent parameter
 *       from code generation methods
 *  9    Gandalf   1.8         6/10/99  Ian Formanek    Regeneration on layout 
 *       changes
 *  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
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
