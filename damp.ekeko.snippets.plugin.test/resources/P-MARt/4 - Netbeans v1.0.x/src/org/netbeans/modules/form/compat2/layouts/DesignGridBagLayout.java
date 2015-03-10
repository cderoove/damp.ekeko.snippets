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
import java.beans.PropertyEditor;

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;
import org.netbeans.modules.form.util2.NbVersion;
import org.netbeans.modules.form.util2.NbVersionNotCompatibleException;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.netbeans.modules.form.*;

/** A design-time support for GridBagLayout.
*
* @author   Ian Formanek
*/
final public class DesignGridBagLayout extends DesignLayout {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -3363823952097286371L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    public static final int MAXGRIDSIZE = 512;

    // Component Layout Properties
    final static String PROP_GRIDX = FormEditor.LAYOUT_PREFIX + "gridX"; // NOI18N
    final static String PROP_GRIDY = FormEditor.LAYOUT_PREFIX + "gridY"; // NOI18N
    final static String PROP_GRIDWIDTH = FormEditor.LAYOUT_PREFIX + "gridWidth"; // NOI18N
    final static String PROP_GRIDHEIGHT = FormEditor.LAYOUT_PREFIX + "gridHeight"; // NOI18N
    final static String PROP_FILL = FormEditor.LAYOUT_PREFIX + "fill"; // NOI18N
    final static String PROP_IPADX = FormEditor.LAYOUT_PREFIX + "ipadX"; // NOI18N
    final static String PROP_IPADY = FormEditor.LAYOUT_PREFIX + "ipadY"; // NOI18N
    final static String PROP_INSETS = FormEditor.LAYOUT_PREFIX + "insets"; // NOI18N
    final static String PROP_ANCHOR = FormEditor.LAYOUT_PREFIX + "anchor"; // NOI18N
    final static String PROP_WEIGHTX = FormEditor.LAYOUT_PREFIX + "weightX"; // NOI18N
    final static String PROP_WEIGHTY = FormEditor.LAYOUT_PREFIX + "weightY"; // NOI18N

    // default values of GridBagConstraints values
    final static int DEFAULT_GRID_X = GridBagConstraints.RELATIVE;
    final static int DEFAULT_GRID_Y = GridBagConstraints.RELATIVE;
    final static int DEFAULT_GRID_WIDTH = 1;
    final static int DEFAULT_GRID_HEIGHT = 1;
    final static int DEFAULT_FILL = GridBagConstraints.NONE;
    final static int DEFAULT_IPAD_X = 0;
    final static int DEFAULT_IPAD_Y = 0;
    final static Insets DEFAULT_INSETS = new Insets (0, 0, 0, 0);
    final static int DEFAULT_ANCHOR = GridBagConstraints.CENTER;
    final static double DEFAULT_WEIGHT_X = 0;
    final static double DEFAULT_WEIGHT_Y = 0;

    /** bundle to obtain text information from */
    private static java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle (DesignGridBagLayout.class);

    /** icons for the Layout. */
    static protected Image icon;
    static protected Image icon32;

    static {
        icon = Toolkit.getDefaultToolkit ().getImage (
                   DesignGridBagLayout.class.getResource ("/org/netbeans/modules/form/resources/palette/gridBagLayout.gif")); // NOI18N
        icon32 = Toolkit.getDefaultToolkit ().getImage (
                     DesignGridBagLayout.class.getResource ("/org/netbeans/modules/form/resources/palette/gridBagLayout32.gif")); // NOI18N
    }

    /** A design layout may provide a customizer for advanced layout
    * customization (e.g. for GridBagLayout).
    * Might return null, if the customizer is not available.
    * @return customizer for this design layout or null if it has no customizer
    */
    public Class getCustomizerClass () {
        return GridBagCustomizer.class;
    }

    /** An icon of the design-layout. This icon will be used on the ComponentPalette
    * for this layout's item.
    * @param  type the desired type of the icon (BeanInfo.ICON_???)
    * @return layout's icon.
    */
    public Image getIcon (int type) {
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
        DesignLayout previousLayout = null;
        if (cont != null) {
            previousLayout = cont.getPreviousDesignLayout ();
        } else {
            if (constraintsVariableName != null) {
                getRADContainer ().getFormManager ().getVariablesPool ().releaseNameSurviveReparse (constraintsVariableName);
            }
        }

        super.setRADContainer (cont);
        if (cont == null)
            return;

        realLayout = new GridBagLayout ();
        if (cont != null) {

            // if changing layout directly from AbsoluteLayout, convert AbsoluteConstraints to GridBagConstraints
            // ONLY performed during this direct change to prevent unwanted changes of stored GridBagConstraints
            if (previousLayout instanceof DesignAbsoluteLayout) {
                RADVisualComponent[] children = cont.getSubComponents ();
                Component[] components = new Component [children.length];
                Rectangle[] bounds = new Rectangle [children.length];
                for (int i = 0; i < children.length; i++) {
                    components [i] = children [i].getComponent ();
                    DesignAbsoluteLayout.AbsoluteConstraintsDescription acd =
                        (DesignAbsoluteLayout.AbsoluteConstraintsDescription) children[i].getConstraints (DesignAbsoluteLayout.class);
                    int x = components[i].getLocation ().x;
                    int y = components[i].getLocation ().y;
                    int w = components[i].getSize ().width;
                    int h = components[i].getSize ().height;
                    if (acd != null) {
                        x = acd.position.x;
                        y = acd.position.y;
                        w = acd.size.width;
                        h = acd.size.height;
                    }
                    if (w == -1) w = components[i].getPreferredSize ().width;
                    if (h == -1) h = components[i].getPreferredSize ().height;
                    bounds [i] = new Rectangle (x, y, w, h);
                }

                GridBagConstraints[] gbc = FormUtils.convertToConstraints (bounds, components);

                for (int i = 0; i < children.length; i++) {
                    DesignGridBagLayout.GridBagConstraintsDescription gbcd =
                        new DesignGridBagLayout.GridBagConstraintsDescription (gbc [i]);
                    children [i].setConstraints (DesignGridBagLayout.class, gbcd);
                    realLayout.setConstraints (components[i], gbc[i]); // not sure
                }
            }
            getContainer ().setLayout (realLayout);

        } else {
            constraintsVariableName = null; // release the variable name
        }
    }

    private String getConstraintsVariableName () {
        if ((constraintsVariableName == null) && (getRADContainer () != null)) {
            constraintsVariableName = getRADContainer ().getFormManager ().getVariablesPool ().getNewName (GridBagConstraints.class);
            getRADContainer ().getFormManager ().getVariablesPool ().reserveNameSurviveReparse (constraintsVariableName);
        }

        return constraintsVariableName;
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
    public DesignLayout.ConstraintsDescription getConstraintsDescription (Point position) {
        return new GridBagConstraintsDescription ();
    }

    /** A display name of the layout will be used for displaying the layout in
    * the components hierarchy during design-time.
    * @return layout's display name.
    */
    public String getDisplayName () {
        return "GridBagLayout"; // NOI18N
    }

    /** Returns the layout's properties for specified component.
    * @param node the RADVisualComponent of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getComponentProperties (RADVisualComponent node) {
        final RADVisualComponent componentNode = node;
        return new Node.Property[] {
                   new PropertySupport.ReadWrite (PROP_GRIDX, Integer.TYPE,
                                                  bundle.getString ("PROP_gridbagcomp_gridx"), bundle.getString ("HINT_gridbagcomp_gridx")) {
                       public Object getValue () {
                           GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints (DesignGridBagLayout.class);
                           return new Integer (gbcd.getGridX ());
                       }

                       public void setValue (Object val) {
                           if (val instanceof Integer) {
                               if (((Integer)val).intValue () >= MAXGRIDSIZE) return;
                               GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                               int oldValue = gbcd.getGridX ();
                               gbcd.setGridX (((Integer)val).intValue ());
                               firePropertyChange (componentNode, PROP_GRIDX, new Integer(oldValue), val);
                               updateComponent (componentNode);
                           }
                           else throw new IllegalArgumentException();
                       }

                       public PropertyEditor getPropertyEditor () {
                           return new GridPosEditor ();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_GRIDY, Integer.TYPE,
                                                  bundle.getString("PROP_gridbagcomp_gridy"), bundle.getString("HINT_gridbagcomp_gridy")) {
                       public Object getValue () {
                           GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                           return new Integer (gbcd.getGridY ());
                       }

                       public void setValue (Object val) {
                           if (val instanceof Integer) {
                               if (((Integer)val).intValue () >= MAXGRIDSIZE) return;
                               GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                               int oldValue = gbcd.getGridY ();
                               gbcd.setGridY (((Integer)val).intValue ());
                               firePropertyChange (componentNode, PROP_GRIDY, new Integer(oldValue), val);
                               updateComponent (componentNode);
                           }
                           else throw new IllegalArgumentException();
                       }

                       public PropertyEditor getPropertyEditor () {
                           return new GridPosEditor ();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_GRIDWIDTH, Integer.TYPE,
                                                  bundle.getString("PROP_gridbagcomp_gridwidth"), bundle.getString("HINT_gridbagcomp_gridwidth")) {
                       public Object getValue () {
                           GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                           return new Integer (gbcd.getGridWidth ());
                       }

                       public void setValue (Object val) {
                           if (val instanceof Integer) {
                               if (((Integer)val).intValue () >= MAXGRIDSIZE) return;
                               GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                               int oldValue = gbcd.getGridWidth ();
                               gbcd.setGridWidth (((Integer)val).intValue ());
                               firePropertyChange (componentNode, PROP_GRIDWIDTH, new Integer(oldValue), val);
                               updateComponent (componentNode);
                           }
                           else throw new IllegalArgumentException();
                       }

                       public PropertyEditor getPropertyEditor () {
                           return new GridSizeEditor ();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_GRIDHEIGHT, Integer.TYPE,
                                                  bundle.getString("PROP_gridbagcomp_gridheight"), bundle.getString("HINT_gridbagcomp_gridheight")) {
                       public Object getValue () {
                           GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                           return new Integer (gbcd.getGridHeight ());
                       }

                       public void setValue (Object val) {
                           if (val instanceof Integer) {
                               if (((Integer)val).intValue () >= MAXGRIDSIZE) return;
                               GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                               int oldValue = gbcd.getGridHeight ();
                               gbcd.setGridHeight (((Integer)val).intValue ());
                               firePropertyChange (componentNode, PROP_GRIDHEIGHT, new Integer(oldValue), val);
                               updateComponent (componentNode);
                           }
                           else throw new IllegalArgumentException();
                       }

                       public PropertyEditor getPropertyEditor () {
                           return new GridSizeEditor ();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_FILL, Integer.TYPE,
                                                  bundle.getString("PROP_gridbagcomp_fill"), bundle.getString("HINT_gridbagcomp_fill")) {
                       public Object getValue () {
                           GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                           return new Integer (gbcd.getFill ());
                       }

                       public void setValue (Object val) {
                           if (val instanceof Integer) {
                               GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                               int oldValue = gbcd.getFill ();
                               gbcd.setFill (((Integer)val).intValue ());
                               firePropertyChange (componentNode, PROP_FILL, new Integer(oldValue), val);
                               updateComponent (componentNode);
                           }
                           else throw new IllegalArgumentException();
                       }

                       public PropertyEditor getPropertyEditor () {
                           return new FillEditor ();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_IPADX, Integer.TYPE,
                                                  bundle.getString("PROP_gridbagcomp_ipadx"), bundle.getString("HINT_gridbagcomp_ipadx")) {
                       public Object getValue () {
                           GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                           return new Integer (gbcd.getIpadX ());
                       }

                       public void setValue (Object val) {
                           if (val instanceof Integer) {
                               GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                               int oldValue = gbcd.getIpadX ();
                               gbcd.setIpadX (((Integer)val).intValue ());
                               firePropertyChange (componentNode, PROP_IPADX, new Integer(oldValue), val);
                               updateComponent (componentNode);
                           }
                           else throw new IllegalArgumentException();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_IPADY, Integer.TYPE,
                                                  bundle.getString("PROP_gridbagcomp_ipady"), bundle.getString("HINT_gridbagcomp_ipady")) {
                       public Object getValue () {
                           GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                           return new Integer (gbcd.getIpadY ());
                       }

                       public void setValue (Object val) {
                           if (val instanceof Integer) {
                               GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                               int oldValue = gbcd.getIpadY ();
                               gbcd.setIpadY (((Integer)val).intValue ());
                               firePropertyChange (componentNode, PROP_IPADY, new Integer(oldValue), val);
                               updateComponent (componentNode);
                           }
                           else throw new IllegalArgumentException();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_INSETS, Insets.class,
                                                  bundle.getString("PROP_gridbagcomp_insets"), bundle.getString("HINT_gridbagcomp_insets")) {
                       public Object getValue () {
                           GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                           return gbcd.getInsets ();
                       }

                       public void setValue (Object val) {
                           if (val instanceof Insets) {
                               GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                               Insets oldValue = gbcd.getInsets ();
                               gbcd.setInsets ((Insets)val);
                               firePropertyChange (componentNode, PROP_INSETS, oldValue, val);
                               updateComponent (componentNode);
                           }
                           else throw new IllegalArgumentException();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_ANCHOR, Integer.TYPE,
                                                  bundle.getString("PROP_gridbagcomp_anchor"), bundle.getString("HINT_gridbagcomp_anchor")) {
                       public Object getValue () {
                           GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                           return new Integer (gbcd.getAnchor ());
                       }

                       public void setValue (Object val) {
                           if (val instanceof Integer) {
                               GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                               int oldValue = gbcd.getAnchor ();
                               gbcd.setAnchor (((Integer)val).intValue ());
                               firePropertyChange (componentNode, PROP_ANCHOR, new Integer(oldValue), val);
                               updateComponent (componentNode);
                           }
                           else throw new IllegalArgumentException();
                       }

                       public PropertyEditor getPropertyEditor () {
                           return new AnchorEditor ();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_WEIGHTX, Double.TYPE,
                                                  bundle.getString("PROP_gridbagcomp_weightx"), bundle.getString("HINT_gridbagcomp_weightx")) {
                       public Object getValue () {
                           GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                           return new Double (gbcd.getWeightX ());
                       }

                       public void setValue (Object val) {
                           if (val instanceof Double) {
                               GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                               double oldValue = gbcd.getWeightX ();
                               gbcd.setWeightX (((Double)val).doubleValue ());
                               firePropertyChange (componentNode, PROP_WEIGHTX, new Double(oldValue), val);
                               updateComponent (componentNode);
                           }
                           else throw new IllegalArgumentException();
                       }
                   },

                   new PropertySupport.ReadWrite (PROP_WEIGHTY, Double.TYPE,
                                                  bundle.getString("PROP_gridbagcomp_weighty"), bundle.getString("HINT_gridbagcomp_weighty")) {
                       public Object getValue () {
                           GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                           return new Double (gbcd.getWeightY ());
                       }

                       public void setValue (Object val) {
                           if (val instanceof Double) {
                               GridBagConstraintsDescription gbcd = (GridBagConstraintsDescription)componentNode.getConstraints(DesignGridBagLayout.class);
                               double oldValue = gbcd.getWeightY ();
                               gbcd.setWeightY (((Double)val).doubleValue ());
                               firePropertyChange (componentNode, PROP_WEIGHTY, new Double(oldValue), val);
                               updateComponent (componentNode);
                           }
                           else throw new IllegalArgumentException();
                       }
                   },
               };
    }

    /** Returns a class of the layout that this DesignLayout represents (e.g.
    * returns FlowLayout.class from DesignFlowLayout).
    * @return a class of the layout represented by this DesignLayout or null if the 
    *         design layout does not represent a "real" layout (e.g. support layouts for JTabbedPane, ...)
    */
    public Class getLayoutClass() {
        return GridBagLayout.class;
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
    public void addComponent (RADVisualComponent node) {
        GridBagConstraintsDescription gcd = (GridBagConstraintsDescription)node.getConstraints(DesignGridBagLayout.class);
        if (gcd == null) {
            gcd = new GridBagConstraintsDescription();
            node.setConstraints(DesignGridBagLayout.class, gcd);
        }

        Component visual = getFormManager ().getVisualRepresentation (node);
        realLayout.setConstraints (visual, gcd.getGridBagConstraints ());
        getContainer().add(visual, node.getComponentIndex ());
    }

    /** Updates the component after its ConstraintsDescription changed - sets the constraints
    * on the real layout and validates the container to reflect the changes.
    * @param node The component to update
    */
    void updateComponent (RADVisualComponent node) {
        GridBagConstraintsDescription gcd = (GridBagConstraintsDescription)node.getConstraints(DesignGridBagLayout.class);
        if (gcd != null) {
            Component visual = getFormManager ().getVisualRepresentation (node);
            realLayout.setConstraints (visual, gcd.getGridBagConstraints ());
            getContainer().invalidate();
            getContainer().validate ();
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
        boolean doNotGen = false;
        if (defaultLM != null) {
            if (defaultLM.getClass ().equals (GridBagLayout.class)) {
                doNotGen = true; // the default layout is the same as current settings => no need to generate layout
            }
        }

        StringBuffer buf = new StringBuffer();
        if (!doNotGen) {
            buf.append(createContainerGenName(cont));
            buf.append("setLayout (new java.awt.GridBagLayout ());\n"); // NOI18N
        }

        // generate variable for constraints
        buf.append ("java.awt.GridBagConstraints "); // NOI18N
        buf.append (getConstraintsVariableName ());
        buf.append (";\n"); // NOI18N
        return buf.toString();
    }

    /** Generates the code for adding specified component to this layout.
    * @param comp   The component to be added to this layout
    * @param cont   The container that is managed by this layout
    */
    public String generateComponentCode(RADVisualContainer cont, RADVisualComponent comp) {
        GridBagConstraintsDescription gcd = (GridBagConstraintsDescription)comp.getConstraints(DesignGridBagLayout.class);
        if (gcd == null)
            return ""; // error - the node does not have GridBagConstraints associated // NOI18N

        StringBuffer buf = new StringBuffer();

        // create the constraints variable
        buf.append (getConstraintsVariableName ());
        buf.append (" = new java.awt.GridBagConstraints"); // NOI18N
        buf.append (" ();\n"); // NOI18N

        // set the constraints properties
        if (gcd.getGridX () != DEFAULT_GRID_X) {
            buf.append (getConstraintsVariableName ());
            buf.append (".gridx = "); buf.append (gcd.getGridX ()); buf.append (";\n"); // NOI18N
        }
        if (gcd.getGridY () != DEFAULT_GRID_Y) {
            buf.append (getConstraintsVariableName ());
            buf.append (".gridy = "); buf.append (gcd.getGridY ()); buf.append (";\n"); // NOI18N
        }
        if (gcd.getGridWidth () != DEFAULT_GRID_WIDTH) {
            buf.append (getConstraintsVariableName ());
            buf.append (".gridwidth = "); buf.append (gcd.getGridWidth ()); buf.append (";\n"); // NOI18N
        }
        if (gcd.getGridHeight () != DEFAULT_GRID_HEIGHT) {
            buf.append (getConstraintsVariableName ());
            buf.append (".gridheight = "); buf.append (gcd.getGridHeight ()); buf.append (";\n"); // NOI18N
        }
        if (gcd.getFill () != DEFAULT_FILL) {
            buf.append (getConstraintsVariableName ());
            buf.append (".fill = "); buf.append (FillEditor.getFillInitString (gcd.getFill ())); buf.append (";\n"); // NOI18N
        }
        if (gcd.getIpadX () != DEFAULT_IPAD_X) {
            buf.append (getConstraintsVariableName ());
            buf.append (".ipadx = "); buf.append (gcd.getIpadX ()); buf.append (";\n"); // NOI18N
        }
        if (gcd.getIpadY () != DEFAULT_IPAD_Y) {
            buf.append (getConstraintsVariableName ());
            buf.append (".ipady = "); buf.append (gcd.getIpadY ()); buf.append (";\n"); // NOI18N
        }
        if (!(gcd.getInsets ().equals (DEFAULT_INSETS))) {
            Insets insets = gcd.getInsets ();
            buf.append (getConstraintsVariableName ());
            buf.append (".insets = new java.awt.Insets ("); // NOI18N
            buf.append (insets.top); buf.append (", "); // NOI18N
            buf.append (insets.left); buf.append (", "); // NOI18N
            buf.append (insets.bottom); buf.append (", "); // NOI18N
            buf.append (insets.right); buf.append (");\n"); // NOI18N
        }
        if (gcd.getAnchor () != DEFAULT_ANCHOR) {
            buf.append (getConstraintsVariableName ());
            buf.append (".anchor = "); buf.append (AnchorEditor.getAnchorInitString (gcd.getAnchor ())); buf.append (";\n"); // NOI18N
        }
        if (gcd.getWeightX () != DEFAULT_WEIGHT_X) {
            buf.append (getConstraintsVariableName ());
            buf.append (".weightx = "); buf.append (gcd.getWeightX ()); buf.append (";\n"); // NOI18N
        }
        if (gcd.getWeightY () != DEFAULT_WEIGHT_Y) {
            buf.append (getConstraintsVariableName ());
            buf.append (".weighty = "); buf.append (gcd.getWeightY ()); buf.append (";\n"); // NOI18N
        }

        // generate the "add" code // NOI18N
        buf.append (createContainerGenName(cont));
        buf.append ("add ("); // NOI18N
        buf.append (comp.getName());
        buf.append (", "); // NOI18N
        buf.append (getConstraintsVariableName ());
        buf.append (");\n"); // NOI18N

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
        org.netbeans.modules.form.FormUtils.DEBUG(">> DesignGridBagLayout: readExternal: START"); // NOI18N
        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);
        org.netbeans.modules.form.FormUtils.DEBUG("<< DesignGridBagLayout: readExternal: END"); // NOI18N
    }

    // -----------------------------------------------------------------------------
    // constraints innerclass

    /** The ConstraintsDescription class encapsulates constraints data and
    * operations on a constraints that will be used for adding components
    * to the layout.
    */
    final public static class GridBagConstraintsDescription extends DesignLayout.ConstraintsDescription {
        /** A JDK 1.1. serial version UID */
        static final long serialVersionUID = -1938084789822712168L;

        /** Netbeans class version */
        public static final NbVersion nbClassVersion = new NbVersion (1, 0);

        public GridBagConstraintsDescription() {
            constraints = new GridBagConstraints ();
        }

        public GridBagConstraintsDescription(GridBagConstraints gbc) {
            constraints = gbc;
        }

        public String getConstraintsString() {
            return "GridBag"; // NOI18N
        }

        public Object getConstraintsObject() {
            return constraints;
        }

        public GridBagConstraints getGridBagConstraints() {
            return constraints;
        }

        int getGridX () { return constraints.gridx; }
        void setGridX (int value) { constraints.gridx = value; }
        int getGridY () { return constraints.gridy; }
        void setGridY (int value) { constraints.gridy = value; }
        int getGridWidth () { return constraints.gridwidth; }
        void setGridWidth (int value) { constraints.gridwidth = value; }
        int getGridHeight () { return constraints.gridheight; }
        void setGridHeight (int value) { constraints.gridheight = value; }
        int getFill () { return constraints.fill; }
        void setFill (int value) { constraints.fill = value; }
        int getIpadX () { return constraints.ipadx; }
        void setIpadX (int value) { constraints.ipadx = value; }
        int getIpadY () { return constraints.ipady; }
        void setIpadY (int value) { constraints.ipady = value; }
        Insets getInsets () { return constraints.insets; }
        void setInsets (Insets value) { constraints.insets = value; }
        int getAnchor () { return constraints.anchor; }
        void setAnchor (int value) { constraints.anchor = value; }
        double getWeightX () { return constraints.weightx; }
        void setWeightX (double value) { constraints.weightx = value; }
        double getWeightY () { return constraints.weighty; }
        void setWeightY (double value) { constraints.weighty = value; }

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

            oo.writeObject (constraints);
        }

        /** Reads the object from stream.
        * @param oi input stream to read from
        * @exception IOException Includes any I/O exceptions that may occur
        * @exception ClassNotFoundException if the class of the read object is not found
        */
        public void readExternal (java.io.ObjectInput oi)
        throws java.io.IOException, ClassNotFoundException {
            org.netbeans.modules.form.FormUtils.DEBUG(">> GridBagConstraintsDescription: readExternal: START"); // NOI18N
            // check the version
            NbVersion classVersion = (NbVersion) oi.readObject ();
            if (!nbClassVersion.isCompatible (classVersion))
                throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

            constraints = (GridBagConstraints) oi.readObject ();
            org.netbeans.modules.form.FormUtils.DEBUG("<< GridBagConstraintsDescription: readExternal: END"); // NOI18N
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
            if (!XML_GRIDBAG_CONSTRAINTS.equals (element.getNodeName ())) {
                throw new java.io.IOException ();
            }

            GridBagConstraints gbc = new GridBagConstraints ();
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
            org.w3c.dom.Node node;
            node = attributes.getNamedItem (ATTR_GRID_X); if (node != null) gbc.gridx = Integer.parseInt (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_GRID_Y); if (node != null) gbc.gridy = Integer.parseInt (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_GRID_W); if (node != null) gbc.gridwidth = Integer.parseInt (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_GRID_H); if (node != null) gbc.gridheight = Integer.parseInt (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_FILL); if (node != null) gbc.fill = Integer.parseInt (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_IPAD_X); if (node != null) gbc.ipadx = Integer.parseInt (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_IPAD_Y); if (node != null) gbc.ipady = Integer.parseInt (node.getNodeValue ());

            int top = 0, left = 0, bottom = 0, right = 0;
            node = attributes.getNamedItem (ATTR_INSETS_TOP); if (node != null) top = Integer.parseInt (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_INSETS_LEFT); if (node != null) left = Integer.parseInt (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_INSETS_BOTTOM); if (node != null) bottom = Integer.parseInt (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_INSETS_RIGHT); if (node != null) right = Integer.parseInt (node.getNodeValue ());
            gbc.insets = new Insets (top, left, bottom, right);

            node = attributes.getNamedItem (ATTR_ANCHOR); if (node != null) gbc.anchor = Integer.parseInt (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_WEIGHT_X); if (node != null) gbc.weightx = Double.parseDouble (node.getNodeValue ());
            node = attributes.getNamedItem (ATTR_WEIGHT_Y); if (node != null) gbc.weighty = Double.parseDouble (node.getNodeValue ());

            constraints = gbc;
        }

        /** Called to store current property value into XML subtree. The property value should be set using the
        * setValue method prior to calling this method.
        * @param doc The XML document to store the XML in - should be used for creating nodes only
        * @return the XML DOM element representing a subtree of XML from which the value should be loaded or null 
        *         if the value does not need to save any additional data and can be created using the default constructor
        */
        public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
            org.w3c.dom.Element el = doc.createElement (XML_GRIDBAG_CONSTRAINTS);
            el.setAttribute (ATTR_GRID_X, Integer.toString (constraints.gridx));
            el.setAttribute (ATTR_GRID_Y, Integer.toString (constraints.gridy));
            el.setAttribute (ATTR_GRID_W, Integer.toString (constraints.gridwidth));
            el.setAttribute (ATTR_GRID_H, Integer.toString (constraints.gridheight));
            el.setAttribute (ATTR_FILL, Integer.toString (constraints.fill));
            el.setAttribute (ATTR_IPAD_X, Integer.toString (constraints.ipadx));
            el.setAttribute (ATTR_IPAD_Y, Integer.toString (constraints.ipady));
            el.setAttribute (ATTR_INSETS_TOP, Integer.toString (constraints.insets.top));
            el.setAttribute (ATTR_INSETS_LEFT, Integer.toString (constraints.insets.left));
            el.setAttribute (ATTR_INSETS_BOTTOM, Integer.toString (constraints.insets.bottom));
            el.setAttribute (ATTR_INSETS_RIGHT, Integer.toString (constraints.insets.right));
            el.setAttribute (ATTR_ANCHOR, Integer.toString (constraints.anchor));
            el.setAttribute (ATTR_WEIGHT_X, Double.toString (constraints.weightx));
            el.setAttribute (ATTR_WEIGHT_Y, Double.toString (constraints.weighty));
            return el;
        }

        public static final String XML_GRIDBAG_CONSTRAINTS = "GridBagConstraints"; // NOI18N
        public static final String ATTR_GRID_X = "gridX"; // NOI18N
        public static final String ATTR_GRID_Y = "gridY"; // NOI18N
        public static final String ATTR_GRID_W = "gridWidth"; // NOI18N
        public static final String ATTR_GRID_H = "gridHeight"; // NOI18N
        public static final String ATTR_FILL = "fill"; // NOI18N
        public static final String ATTR_IPAD_X = "ipadX"; // NOI18N
        public static final String ATTR_IPAD_Y = "ipadY"; // NOI18N
        public static final String ATTR_INSETS_TOP = "insetsTop"; // NOI18N
        public static final String ATTR_INSETS_LEFT = "insetsLeft"; // NOI18N
        public static final String ATTR_INSETS_BOTTOM = "insetsBottom"; // NOI18N
        public static final String ATTR_INSETS_RIGHT = "insetsRight"; // NOI18N
        public static final String ATTR_ANCHOR = "anchor"; // NOI18N
        public static final String ATTR_WEIGHT_X = "weightX"; // NOI18N
        public static final String ATTR_WEIGHT_Y = "weightY"; // NOI18N

        private GridBagConstraints constraints;
    }

    // -----------------------------------------------------------------------------
    // Innerclasses

    abstract static class GridEditor
                extends java.beans.PropertyEditorSupport
                implements EnhancedPropertyEditor
    {
        /** Display Names for alignment. */
        protected static final String NAME_RELATIVE = bundle.getString ("VALUE_gridbag_relative");
        protected static final String NAME_REMAINDER = bundle.getString ("VALUE_gridbag_remainder");

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
    }

    public static class GridPosEditor extends GridEditor {
        /** @return names of the possible directions */
        public String[] getTags () {
            return new String[] { NAME_RELATIVE };
        }

        /** @return text for the current value */
        public String getAsText () {
            int value = ((Integer)getValue ()).intValue ();

            if (value == GridBagConstraints.RELATIVE) return NAME_RELATIVE;
            else return "" +value; // NOI18N
        }

        /** Setter.
        * @param str string equal to one value from directions array
        */
        public void setAsText (String str) {
            if (str.equals (NAME_RELATIVE)) {
                setValue (new Integer (GridBagConstraints.RELATIVE));
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
            if (value == GridBagConstraints.RELATIVE) {
                return "java.awt.GridBagConstraints.RELATIVE"; // NOI18N
            }
            else {
                return "" + value; // NOI18N
            }
        }
    }

    public static class GridSizeEditor extends GridEditor {
        /** @return names of the possible directions */
        public String[] getTags () {
            return new String[] { NAME_REMAINDER, NAME_RELATIVE };
        }

        /** @return text for the current value */
        public String getAsText () {
            int value = ((Integer)getValue ()).intValue ();

            if (value == GridBagConstraints.RELATIVE) return NAME_RELATIVE;
            else if (value == GridBagConstraints.REMAINDER) return NAME_REMAINDER;
            else return "" + value; // NOI18N
        }

        /** Setter.
        * @param str string equal to one value from directions array
        */
        public void setAsText (String str) {
            if (str.equals (NAME_RELATIVE)) {
                setValue (new Integer (GridBagConstraints.RELATIVE));
            }
            else if (str.equals (NAME_REMAINDER)) {
                setValue (new Integer (GridBagConstraints.REMAINDER));
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
            if (value == GridBagConstraints.RELATIVE) {
                return "java.awt.GridBagConstraints.RELATIVE"; // NOI18N
            }
            else if (value == GridBagConstraints.REMAINDER) {
                return "java.awt.GridBagConstraints.REMAINDER"; // NOI18N
            }
            else {
                return "" + value; // NOI18N
            }
        }
    }

    final public static class FillEditor extends java.beans.PropertyEditorSupport {
        /** Display Names for alignment. */
        private static final String[] names = {
            bundle.getString ("VALUE_gridbagfill_none"),
            bundle.getString ("VALUE_gridbagfill_horizontal"),
            bundle.getString ("VALUE_gridbagfill_vertical"),
            bundle.getString ("VALUE_gridbagfill_both"),
        };

        static String getFillInitString (int value) {
            FillEditor fe = new FillEditor ();
            fe.setValue (new Integer (value));
            return fe.getJavaInitializationString ();
        }

        /** @return names of the possible directions */
        public String[] getTags () {
            return names;
        }

        /** @return text for the current value */
        public String getAsText () {
            int value = ((Integer)getValue ()).intValue ();

            if (value == GridBagConstraints.NONE) return names[0];
            else if (value == GridBagConstraints.HORIZONTAL) return names[1];
            else if (value == GridBagConstraints.VERTICAL) return names[2];
            else if (value == GridBagConstraints.BOTH) return names[3];
            else return null;
        }

        /** Setter.
        * @param str string equal to one value from directions array
        */
        public void setAsText (String str) {
            if (names[0].equals (str))
                setValue (new Integer (GridBagConstraints.NONE));
            else if (names[1].equals (str))
                setValue (new Integer (GridBagConstraints.HORIZONTAL));
            else if (names[2].equals (str))
                setValue (new Integer (GridBagConstraints.VERTICAL));
            else if (names[3].equals (str))
                setValue (new Integer (GridBagConstraints.BOTH));
        }

        public String getJavaInitializationString () {
            int value = ((Integer)getValue ()).intValue ();
            switch (value) {
            case GridBagConstraints.HORIZONTAL : return "java.awt.GridBagConstraints.HORIZONTAL"; // NOI18N
            case GridBagConstraints.VERTICAL : return "java.awt.GridBagConstraints.VERTICAL"; // NOI18N
            case GridBagConstraints.BOTH : return "java.awt.GridBagConstraints.BOTH"; // NOI18N
            default: return "java.awt.GridBagConstraints.NONE"; // NOI18N
            }
        }
    }

    final public static class AnchorEditor extends java.beans.PropertyEditorSupport {
        /** Display Names for alignment. */
        private static final String[] names = {
            bundle.getString ("VALUE_gridbaganchor_center"),
            bundle.getString ("VALUE_gridbaganchor_north"),
            bundle.getString ("VALUE_gridbaganchor_northeast"),
            bundle.getString ("VALUE_gridbaganchor_east"),
            bundle.getString ("VALUE_gridbaganchor_southeast"),
            bundle.getString ("VALUE_gridbaganchor_south"),
            bundle.getString ("VALUE_gridbaganchor_southwest"),
            bundle.getString ("VALUE_gridbaganchor_west"),
            bundle.getString ("VALUE_gridbaganchor_northwest"),
        };

        static String getAnchorInitString (int value) {
            AnchorEditor ae = new AnchorEditor ();
            ae.setValue (new Integer (value));
            return ae.getJavaInitializationString ();
        }

        /** @return names of the possible directions */
        public String[] getTags () {
            return names;
        }

        /** @return text for the current value */
        public String getAsText () {
            int value = ((Integer)getValue ()).intValue ();

            if (value == GridBagConstraints.CENTER) return names[0];
            else if (value == GridBagConstraints.NORTH) return names[1];
            else if (value == GridBagConstraints.NORTHEAST) return names[2];
            else if (value == GridBagConstraints.EAST) return names[3];
            else if (value == GridBagConstraints.SOUTHEAST) return names[4];
            else if (value == GridBagConstraints.SOUTH) return names[5];
            else if (value == GridBagConstraints.SOUTHWEST) return names[6];
            else if (value == GridBagConstraints.WEST) return names[7];
            else if (value == GridBagConstraints.NORTHWEST) return names[8];
            else return null;
        }

        /** Setter.
        * @param str string equal to one value from directions array
        */
        public void setAsText (String str) {
            if (names[0].equals (str))
                setValue (new Integer (GridBagConstraints.CENTER));
            else if (names[1].equals (str))
                setValue (new Integer (GridBagConstraints.NORTH));
            else if (names[2].equals (str))
                setValue (new Integer (GridBagConstraints.NORTHEAST));
            else if (names[3].equals (str))
                setValue (new Integer (GridBagConstraints.EAST));
            else if (names[4].equals (str))
                setValue (new Integer (GridBagConstraints.SOUTHEAST));
            else if (names[5].equals (str))
                setValue (new Integer (GridBagConstraints.SOUTH));
            else if (names[6].equals (str))
                setValue (new Integer (GridBagConstraints.SOUTHWEST));
            else if (names[7].equals (str))
                setValue (new Integer (GridBagConstraints.WEST));
            else if (names[8].equals (str))
                setValue (new Integer (GridBagConstraints.NORTHWEST));
        }

        public String getJavaInitializationString () {
            int value = ((Integer)getValue ()).intValue ();
            switch (value) {
            case GridBagConstraints.NORTH : return "java.awt.GridBagConstraints.NORTH"; // NOI18N
            case GridBagConstraints.NORTHEAST : return "java.awt.GridBagConstraints.NORTHEAST"; // NOI18N
            case GridBagConstraints.EAST : return "java.awt.GridBagConstraints.EAST"; // NOI18N
            case GridBagConstraints.SOUTHEAST : return "java.awt.GridBagConstraints.SOUTHEAST"; // NOI18N
            case GridBagConstraints.SOUTH : return "java.awt.GridBagConstraints.SOUTH"; // NOI18N
            case GridBagConstraints.SOUTHWEST : return "java.awt.GridBagConstraints.SOUTHWEST"; // NOI18N
            case GridBagConstraints.WEST : return "java.awt.GridBagConstraints.WEST"; // NOI18N
            case GridBagConstraints.NORTHWEST : return "java.awt.GridBagConstraints.NORTHWEST"; // NOI18N
            default : return "java.awt.GridBagConstraints.HORIZONTAL"; // NOI18N
            }
        }
    }
    // -----------------------------------------------------------------------------
    // private area

    private String constraintsVariableName;

    transient private GridBagLayout realLayout;
}

/*
 * Log
 *  22   Gandalf   1.21        1/13/00  Ian Formanek    NOI18N #2
 *  21   Gandalf   1.20        1/12/00  Ian Formanek    NOI18N
 *  20   Gandalf   1.19        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  19   Gandalf   1.18        9/24/99  Ian Formanek    Smarter code generation 
 *       - fixes bug 4016 - The setLayout code should not be generated if the 
 *       layout is already set on the container to prevent loosing components 
 *       already on the panel.
 *  18   Gandalf   1.17        9/24/99  Ian Formanek    generateInitCode method 
 *       clarified
 *  17   Gandalf   1.16        7/31/99  Ian Formanek    Cleaned up comments
 *  16   Gandalf   1.15        7/14/99  Ian Formanek    Fixed last change
 *  15   Gandalf   1.14        7/13/99  Ian Formanek    XML Persistence
 *  14   Gandalf   1.13        7/2/99   Petr Hrebejk    
 *  13   Gandalf   1.12        6/30/99  Ian Formanek    reflected change in 
 *       enhanced property editors
 *  12   Gandalf   1.11        6/27/99  Ian Formanek    Removed indent parameter
 *       from code generation methods
 *  11   Gandalf   1.10        6/22/99  Ian Formanek    Modified customizers
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
