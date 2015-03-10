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

package org.netbeans.modules.form.compat2.layouts.support;

import javax.swing.JLayeredPane;

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;
import org.openide.util.NbBundle;
import org.netbeans.modules.form.util2.NbVersion;
import org.netbeans.modules.form.util2.NbVersionNotCompatibleException;
import org.openide.nodes.*;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.compat2.layouts.DesignLayout;

/** A design-time support layout for JLayeredPane component.
*
* @author   Ian Formanek
*/
final public class JLayeredPaneSupportLayout extends PlainDesignSupportLayout {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -8907380680799986131L;

    public final static String PROP_LAYER = "layer"; // NOI18N
    public final static String PROP_POSITION = "position"; // NOI18N
    public final static String PROP_X = "x"; // NOI18N
    public final static String PROP_Y = "y"; // NOI18N
    public final static String PROP_WIDTH = "width"; // NOI18N
    public final static String PROP_HEIGHT = "height"; // NOI18N

    private final static java.util.ResourceBundle bundle = NbBundle.getBundle (JLayeredPaneSupportLayout.class);

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
    public DesignLayout.ConstraintsDescription getConstraintsDescription(java.awt.Point position) {
        return new JLayeredPaneConstraintsDescription (JLayeredPane.DEFAULT_LAYER.intValue (), -1, position.x, position.y, -1, -1);
    }

    /** Returns the layout's properties for specified component.
    * @param node the RADVisualComponent of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getComponentProperties(RADVisualComponent comp) {
        final RADVisualComponent componentNode = comp;
        return new Node.Property[] {
                   new PropertySupport.ReadWrite (PROP_LAYER, Integer.TYPE,
                                                  bundle.getString("PROP_jlayeredpanecomp_layer"), bundle.getString("HINT_jlayeredpanecomp_layer")) {

                       public Object getValue () {
                           JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)componentNode.getConstraints(JLayeredPaneSupportLayout.class);
                           return new Integer (cd.layer);
                       }

                       public void setValue (Object val) {
                           if (val instanceof Integer) {
                               Object oldValue = getValue ();
                               JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)componentNode.getConstraints(JLayeredPaneSupportLayout.class);
                               cd.layer = ((Integer)val).intValue ();
                               java.awt.Component visual = getRADContainer ().getFormManager ().getVisualRepresentation (componentNode);
                               ((JLayeredPane) getContainerHelper ()).setLayer (visual, cd.layer, cd.position);
                               firePropertyChange (componentNode, PROP_LAYER, oldValue, val);

                               updateContainer ();
                           }
                           else throw new IllegalArgumentException();
                       }

                       public java.beans.PropertyEditor getPropertyEditor () {
                           return new LayerEditor ();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_POSITION, Integer.TYPE,
                                                  bundle.getString("PROP_jlayeredpanecomp_position"), bundle.getString("HINT_jlayeredpanecomp_position")) {

                       public Object getValue () {
                           JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)componentNode.getConstraints(JLayeredPaneSupportLayout.class);
                           return new Integer (cd.position);
                       }

                       public void setValue (Object val) {
                           if (val instanceof Integer) {
                               Object oldValue = getValue ();
                               JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)componentNode.getConstraints(JLayeredPaneSupportLayout.class);
                               cd.position = ((Integer)val).intValue ();
                               java.awt.Component visual = getRADContainer ().getFormManager ().getVisualRepresentation (componentNode);
                               ((JLayeredPane) getContainerHelper ()).setLayer (visual, cd.layer, cd.position);
                               firePropertyChange (componentNode, PROP_POSITION, oldValue, val);

                               updateContainer ();
                           }
                           else throw new IllegalArgumentException();
                       }

                   },

                   new PropertySupport.ReadWrite (PROP_X, Integer.TYPE,
                                                  bundle.getString("PROP_jlayeredpanecomp_x"), bundle.getString("HINT_jlayeredpanecomp_x")) {
                       public Object getValue () {
                           JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)componentNode.getConstraints(JLayeredPaneSupportLayout.class);
                           return new Integer (cd.x);
                       }
                       public void setValue (Object val) {
                           if (val instanceof Integer) {
                               Object oldValue = getValue ();
                               JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)componentNode.getConstraints(JLayeredPaneSupportLayout.class);
                               cd.x = ((Integer)val).intValue ();
                               java.awt.Component visual = getRADContainer ().getFormManager ().getVisualRepresentation (componentNode);
                               visual.setBounds (cd.x, cd.y, cd.width, cd.height);
                               firePropertyChange (componentNode, PROP_X, oldValue, val);
                               updateContainer ();
                           }
                           else throw new IllegalArgumentException();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_Y, Integer.TYPE,
                                                  bundle.getString("PROP_jlayeredpanecomp_y"), bundle.getString("HINT_jlayeredpanecomp_y")) {
                       public Object getValue () {
                           JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)componentNode.getConstraints(JLayeredPaneSupportLayout.class);
                           return new Integer (cd.y);
                       }
                       public void setValue (Object val) {
                           if (val instanceof Integer) {
                               Object oldValue = getValue ();
                               JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)componentNode.getConstraints(JLayeredPaneSupportLayout.class);
                               cd.y = ((Integer)val).intValue ();
                               java.awt.Component visual = getRADContainer ().getFormManager ().getVisualRepresentation (componentNode);
                               visual.setBounds (cd.x, cd.y, cd.width, cd.height);
                               firePropertyChange (componentNode, PROP_X, oldValue, val);
                               updateContainer ();
                           }
                           else throw new IllegalArgumentException();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_WIDTH, Integer.TYPE,
                                                  bundle.getString("PROP_jlayeredpanecomp_width"), bundle.getString("HINT_jlayeredpanecomp_width")) {
                       public Object getValue () {
                           JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)componentNode.getConstraints(JLayeredPaneSupportLayout.class);
                           return new Integer (cd.width);
                       }
                       public void setValue (Object val) {
                           if (val instanceof Integer) {
                               Object oldValue = getValue ();
                               JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)componentNode.getConstraints(JLayeredPaneSupportLayout.class);
                               cd.width = ((Integer)val).intValue ();
                               java.awt.Component visual = getRADContainer ().getFormManager ().getVisualRepresentation (componentNode);
                               visual.setBounds (cd.x, cd.y, cd.width, cd.height);
                               firePropertyChange (componentNode, PROP_X, oldValue, val);
                               updateContainer ();
                           }
                           else throw new IllegalArgumentException();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_HEIGHT, Integer.TYPE,
                                                  bundle.getString("PROP_jlayeredpanecomp_height"), bundle.getString("HINT_jlayeredpanecomp_height")) {
                       public Object getValue () {
                           JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)componentNode.getConstraints(JLayeredPaneSupportLayout.class);
                           return new Integer (cd.height);
                       }
                       public void setValue (Object val) {
                           if (val instanceof Integer) {
                               Object oldValue = getValue ();
                               JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)componentNode.getConstraints(JLayeredPaneSupportLayout.class);
                               cd.height = ((Integer)val).intValue ();
                               java.awt.Component visual = getRADContainer ().getFormManager ().getVisualRepresentation (componentNode);
                               visual.setBounds (cd.x, cd.y, cd.width, cd.height);
                               firePropertyChange (componentNode, PROP_X, oldValue, val);
                               updateContainer ();
                           }
                           else throw new IllegalArgumentException();
                       }
                   },
               };
    }

    void updateContainer () {
        getContainerHelper ().invalidate ();
        getContainerHelper ().validate ();
        getContainerHelper ().repaint ();
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
        JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)comp.getConstraints(JLayeredPaneSupportLayout.class);
        if (cd == null) {
            cd = new JLayeredPaneConstraintsDescription ();
            comp.setConstraints (JLayeredPaneSupportLayout.class, cd);
        }

        java.awt.Component visual = getRADContainer ().getFormManager ().getVisualRepresentation (comp);
        if (cd.position == -1)
            ((JLayeredPane)getContainer()).add (visual, new Integer (cd.layer), comp.getComponentIndex ());
        else
            ((JLayeredPane)getContainer()).add (visual, new Integer (cd.layer), cd.position);

        visual.setBounds (cd.x, cd.y, cd.width, cd.height);

        getContainer().invalidate ();
        getContainer().validate ();
        getContainer().repaint ();
    }

    /** Generates the code for adding specified component to this layout.
    * @param comp   The component to be added to this layout
    * @param cont   The container that is managed by this layout
    */
    public String generateComponentCode(RADVisualContainer cont, RADVisualComponent comp) {
        JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)comp.getConstraints(JLayeredPaneSupportLayout.class);
        StringBuffer buf = new StringBuffer();
        buf.append(createContainerGenName(cont));
        buf.append("add ("); // NOI18N
        buf.append (comp.getName());
        buf.append (", "); // NOI18N

        LayerEditor layerEditor = new LayerEditor ();
        layerEditor.setValue (new Integer (cd.layer));
        buf.append (layerEditor.getJavaInitializationString ());

        if (cd.position != -1) {
            buf.append (", "); // NOI18N
            buf.append ("" + cd.position); // NOI18N
        }

        buf.append (");\n"); // NOI18N

        buf.append (comp.getName ());
        buf.append (".setBounds ("); // NOI18N
        buf.append (cd.x);
        buf.append (", "); // NOI18N
        buf.append (cd.y);
        buf.append (", "); // NOI18N
        buf.append (cd.width);
        buf.append (", "); // NOI18N
        buf.append (cd.height);
        buf.append (");\n"); // NOI18N

        return buf.toString();
    }

    final protected java.awt.Container getContainerHelper () {
        return getContainer ();
    }

    // -----------------------------------------------------------------------------
    // Drag'n'drop support

    /** A design layout that supports moving components should redefine
    * this method and return true.
    * @return true if the design layout supports moving, false otherwise
    * @see #moveTo
    */
    public boolean canMove () {
        return true;
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
    public ConstraintsDescription moveTo (ConstraintsDescription desc, int deltaX, int deltaY, java.awt.Point hotSpot) {
        JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)desc;
        return new JLayeredPaneConstraintsDescription (cd.layer, cd.position, deltaX, deltaY, cd.width, cd.height);
    }

    /** A design layout that supports resizing components should redefine
    * this method and return true.
    * The resizing includes a initial drag-resize when adding a new component.
    * @return true if the design layout supports resizing, false otherwise
    * @see #resizeTo
    */
    public boolean canResize () {
        return true;
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
        JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)desc;
        return new JLayeredPaneConstraintsDescription (cd.layer, cd.position, cd.x, cd.y, width, height);
    }

    /** A design layout that supports *BOTH* resizing and moving components
    * should redefine this method to modify the constraints according to the supplied
    * new bounds.
    * @param desc the constraints to resize from
    * @param bounds the bounds to resize to
    * @see #canMove
    * @see #canResize
    */
    public ConstraintsDescription resizeToBounds (ConstraintsDescription desc, java.awt.Rectangle bounds) {
        JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription)desc;
        return new JLayeredPaneConstraintsDescription (cd.layer, cd.position, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /** Called to inform the Layout that it should provide a design-time visual
    * feedback for a "move" action of the specified comp to the specified constraints.
    * @param comp The RADVisualComponent that is being dragged
    * @param desc The "drag to" constraints or null for ending the marking operation
    */
    public void markMoveTo (RADVisualComponent comp, ConstraintsDescription desc) {
        /*    java.awt.Component visual = getRADContainer ().getFormManager ().getVisualRepresentation (comp);
            JLayeredPaneConstraintsDescription cd;
            if (desc == null) { // restore original bounds
              cd = (JLayeredPaneConstraintsDescription)comp.getConstraints(JLayeredPaneSupportLayout.class);
            } else {
              cd = (JLayeredPaneConstraintsDescription)desc;
            }
            visual.setBounds (cd.x, cd.y, cd.width, cd.height); */
    }


    /** Called to inform the Layout that it should provide a design-time visual
    * feedback for a "resize" action to the specified constraints.
    * @param desc The "resize to" constraints or null to notify about cancelling the resize operation
    */
    public void markResizeTo (ConstraintsDescription desc) {
        /*    java.awt.Component visual = getRADContainer ().getFormManager ().getVisualRepresentation (comp);
            JLayeredPaneConstraintsDescription cd;
            if (desc == null) { // restore original bounds
              cd = (JLayeredPaneConstraintsDescription)comp.getConstraints(JLayeredPaneSupportLayout.class);
            } else {
              cd = (JLayeredPaneConstraintsDescription)desc;
            }
            visual.setBounds (cd.x, cd.y, cd.width, cd.height); */
        // [PENDING]
    }

    // -----------------------------------------------------------------------------
    // Innerclasses

    public static class LayerEditor
                extends java.beans.PropertyEditorSupport
                implements EnhancedPropertyEditor
    {
        /** Display Names for alignment. */
        protected static final String[] names = new String[] {
                                                    "DEFAULT_LAYER", "PALETTE_LAYER", "MODAL_LAYER", "POPUP_LAYER", "DRAG_LAYER" }; // NOI18N

        protected static final int values[] = new int[] { 0, 100, 200, 300, 400 };

        /**
        * @return Returns custom property editor to be showen inside the property
        *         sheet.
        */
        public java.awt.Component getInPlaceCustomEditor () {
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
            return names;
        }

        /** @return text for the current value */
        public String getAsText () {
            int value = ((Integer)getValue ()).intValue ();
            for (int i = 0; i < values.length; i++)
                if (value == values [i])
                    return names [i];

            return "" +value; // NOI18N
        }

        /** Setter.
        * @param str string equal to one value from directions array
        */
        public void setAsText (String str) {
            for (int i = 0; i < names.length; i++)
                if (names[i].equals (str)) {
                    setValue (new Integer (values[i]));
                    return;
                }
            try {
                setValue (new Integer (Integer.parseInt (str)));
            } catch (NumberFormatException e) {
                // what can we do, ignore it...
            }
        }

        public String getJavaInitializationString () {

            int value = ((Integer)getValue ()).intValue ();
            for (int i = 0; i < values.length; i++)
                if (value == values [i])
                    return "javax.swing.JLayeredPane." + names [i]; // NOI18N

            return "new Integer (" +value + ")"; // NOI18N
        }
    }

    // -----------------------------------------------------------------------------
    // constraints innerclass

    /** The ConstraintsDescription class encapsulates constraints data and
    * operations on a constraints that will be used for adding components
    * to the layout.
    */
    final public static class JLayeredPaneConstraintsDescription extends PlainDesignSupportLayout.PlainSupportConstraintsDescription {
        /** A JDK 1.1 serial version UID */
        static final long serialVersionUID = 731223873106449595L;

        /** Netbeans class version */
        public static final NbVersion nbClassVersion = new NbVersion (1, 0); // saved from Beta3 (Build 129)

        public JLayeredPaneConstraintsDescription () {
        }

        public JLayeredPaneConstraintsDescription (int layer, int position, int x, int y, int width, int height) {
            this.layer = layer;
            this.position = position;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public boolean equals (Object o) {
            if (!(o instanceof JLayeredPaneConstraintsDescription)) return false;
            JLayeredPaneConstraintsDescription cd = (JLayeredPaneConstraintsDescription) o;
            return (layer == cd.layer) &&
                   (position == cd.position) &&
                   (x == cd.x) &&
                   (y == cd.y) &&
                   (width == cd.width) &&
                   (height == cd.height);
        }

        public int hashCode () {
            return layer*32 + position*16 + x*8 + y*4 + width*2 + height;
        }

        public String getConstraintsString() {
            if ((width == -1) && (height == -1))
                return "[layer="+layer+", position="+position+", x="+x+", y="+y+"]"; // NOI18N
            else
                return "[layer="+layer+", position="+position+", x="+x+", y="+y+", width="+width+", height="+height+"]"; // NOI18N
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

            oo.writeInt (layer);
            oo.writeInt (position);
            oo.writeInt (x);
            oo.writeInt (y);
            oo.writeInt (width);
            oo.writeInt (height);
        }

        /** Reads the object from stream.
        * @param oi input stream to read from
        * @exception IOException Includes any I/O exceptions that may occur
        * @exception ClassNotFoundException if the class of the read object is not found
        */
        public void readExternal (java.io.ObjectInput oi)
        throws java.io.IOException, ClassNotFoundException {
            org.netbeans.modules.form.FormUtils.DEBUG(">> JLayeredPaneConstraintsDescription: readExternal: START"); // NOI18N
            // check the version
            NbVersion classVersion = (NbVersion) oi.readObject ();
            if (!nbClassVersion.isCompatible (classVersion))
                throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

            layer = oi.readInt ();
            position = oi.readInt ();
            x = oi.readInt ();
            y = oi.readInt ();
            width = oi.readInt ();
            height = oi.readInt ();
            org.netbeans.modules.form.FormUtils.DEBUG("<< JLayeredPaneConstraintsDescription: readExternal: END"); // NOI18N
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
            if (!XML_LAYERED_PANE_CONSTRAINTS.equals (element.getNodeName ())) {
                throw new java.io.IOException ();
            }
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
            org.w3c.dom.Node node;
            node = attributes.getNamedItem (ATTR_X); if (node != null) x = Integer.parseInt (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_Y); if (node != null) y = Integer.parseInt (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_W); if (node != null) width = Integer.parseInt (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_H); if (node != null) height = Integer.parseInt (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_LAYER); if (node != null) layer = Integer.parseInt (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_POSITION); if (node != null) position = Integer.parseInt (node.getNodeValue ());
        }

        /** Called to store current property value into XML subtree. The property value should be set using the
        * setValue method prior to calling this method.
        * @param doc The XML document to store the XML in - should be used for creating nodes only
        * @return the XML DOM element representing a subtree of XML from which the value should be loaded or null 
        *         if the value does not need to save any additional data and can be created using the default constructor
        */
        public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
            org.w3c.dom.Element el = doc.createElement (XML_LAYERED_PANE_CONSTRAINTS);
            el.setAttribute (ATTR_X, Integer.toString (x));
            el.setAttribute (ATTR_Y, Integer.toString (y));
            el.setAttribute (ATTR_W, Integer.toString (width));
            el.setAttribute (ATTR_H, Integer.toString (height));
            el.setAttribute (ATTR_LAYER, Integer.toString (layer));
            el.setAttribute (ATTR_POSITION, Integer.toString (position));
            return el;
        }

        public static final String XML_LAYERED_PANE_CONSTRAINTS = "JLayeredPaneConstraints"; // NOI18N
        public static final String ATTR_X = "x"; // NOI18N
        public static final String ATTR_Y = "y"; // NOI18N
        public static final String ATTR_W = "width"; // NOI18N
        public static final String ATTR_H = "height"; // NOI18N
        public static final String ATTR_LAYER = "layer"; // NOI18N
        public static final String ATTR_POSITION = "position"; // NOI18N

        /** The layer represented by this JLayeredPaneConstraintsDescription class */
        int layer = javax.swing.JLayeredPane.DEFAULT_LAYER.intValue ();

        /** The psoition represented by this JLayeredPaneConstraintsDescription class */
        int position = -1;

        int x = 0;
        int y = 0;
        int width = -1;
        int height = -1;
    }

}

/*
 * Log
 *  14   Gandalf   1.13        1/12/00  Ian Formanek    NOI18N
 *  13   Gandalf   1.12        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   Gandalf   1.11        7/13/99  Ian Formanek    XML Persistence
 *  11   Gandalf   1.10        6/30/99  Ian Formanek    reflected change in 
 *       enhanced property editors
 *  10   Gandalf   1.9         6/27/99  Ian Formanek    Removed indent parameter
 *       from code generation methods
 *  9    Gandalf   1.8         6/10/99  Ian Formanek    Regeneration on layout 
 *       changes
 *  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         5/15/99  Ian Formanek    
 *  6    Gandalf   1.5         5/14/99  Ian Formanek    
 *  5    Gandalf   1.4         5/12/99  Ian Formanek    
 *  4    Gandalf   1.3         5/10/99  Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  2    Gandalf   1.1         3/29/99  Ian Formanek    Uses FormUtils.DEBUG to 
 *       print messages
 *  1    Gandalf   1.0         3/29/99  Ian Formanek    
 * $
 */
