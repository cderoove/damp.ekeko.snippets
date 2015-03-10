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


import javax.swing.JSplitPane;

import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.util2.NbVersion;
import org.netbeans.modules.form.util2.NbVersionNotCompatibleException;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.RADVisualContainer;
import org.netbeans.modules.form.RADVisualComponent;


/** A design-time support layout for JSplitPane component.
*
* @author   Ian Formanek
*/
final public class JSplitPaneSupportLayout extends PlainDesignSupportLayout {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -4531158783548926564L;

    /** bundle to obtain text information from */
    private static final java.util.ResourceBundle bundle = NbBundle.getBundle (JSplitPaneSupportLayout.class);

    public final static String PROP_SPLIT_POSITION = "splitPosition"; // NOI18N

    public static final int POS_TOP = 0;
    public static final int POS_BOTTOM = 1;
    public static final int POS_LEFT = 2;
    public static final int POS_RIGHT = 3;

    private static final JSplitPaneConstraintsDescription topCD = new JSplitPaneConstraintsDescription (POS_TOP);
    private static final JSplitPaneConstraintsDescription bottomCD = new JSplitPaneConstraintsDescription (POS_BOTTOM);
    private static final JSplitPaneConstraintsDescription leftCD = new JSplitPaneConstraintsDescription (POS_LEFT);
    private static final JSplitPaneConstraintsDescription rightCD = new JSplitPaneConstraintsDescription (POS_RIGHT);


    private EmptyComponent emptyLeft = new EmptyComponent ();
    private EmptyComponent emptyRight = new EmptyComponent ();

    /** Assigns this DesignLayout to the specified RADVisualContainer.
    * @param cont The RADVisualContainer that represents a container that will be
    *             managed by this layout or null as a notification that this layout
    *             is not a designLayout for its current container anymore
    */
    public void setRADContainer (RADVisualContainer cont) {
        super.setRADContainer (cont);
        if ((cont != null) && (cont.getFormManager () != null)) {
            JSplitPane splitPane = (JSplitPane)getContainer ();
            splitPane.setRightComponent (emptyLeft);
            splitPane.setLeftComponent (emptyRight);
            centerSplitPane ();
        }
    }

    /** Returns a description of the constraints for the
    * specified position. The constraints description object will be used
    * for adding, getting textual description of the constraints and
    * the popup menu.
    * The default implementation just returns the default constraints
    * description.
    * @param position The position within the container for which the
    *                 constraints description should be returned.
    */
    public ConstraintsDescription getConstraintsDescription(java.awt.Point position) {
        java.awt.Dimension size = getContainer ().getSize ();
        int orientation = ((JSplitPane) getContainer ()).getOrientation ();
        if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
            if (position.x < size.width / 2)
                return new JSplitPaneConstraintsDescription (POS_LEFT);
            else
                return new JSplitPaneConstraintsDescription (POS_RIGHT);
        } else {
            if (position.y < size.height / 2)
                return new JSplitPaneConstraintsDescription (POS_TOP);
            else
                return new JSplitPaneConstraintsDescription (POS_BOTTOM);
        }
    }

    final protected java.awt.Container getContainerHelper () {
        return getContainer ();
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
        JSplitPaneConstraintsDescription cd =
            (JSplitPaneConstraintsDescription)
            comp.getConstraints(JSplitPaneSupportLayout.class);

        if (cd == null) {
            cd = new JSplitPaneConstraintsDescription (POS_LEFT);
            comp.setConstraints (JSplitPaneSupportLayout.class, cd);
        }

        java.awt.Component visual = getFormManager().getVisualRepresentation(comp);
        JSplitPane splitPane = (JSplitPane) getContainer ();

        java.awt.Component c = splitPane.getLeftComponent();
        if (c != null) {
            if (c != emptyLeft && c != emptyRight) {
                if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT)
                    cd = new JSplitPaneConstraintsDescription (POS_RIGHT);
                else
                    cd = new JSplitPaneConstraintsDescription (POS_BOTTOM);
                comp.setConstraints (JSplitPaneSupportLayout.class, cd);
            }
        } else {
            c = splitPane.getRightComponent();
            if (c != null && c != emptyLeft && c != emptyRight) {
                if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT)
                    cd = new JSplitPaneConstraintsDescription (POS_LEFT);
                else
                    cd = new JSplitPaneConstraintsDescription (POS_TOP);
                comp.setConstraints (JSplitPaneSupportLayout.class, cd);
            }
        }

        if ((cd.splitPos == POS_RIGHT) || (cd.splitPos == POS_BOTTOM))
            splitPane.setRightComponent (visual);
        else
            splitPane.setLeftComponent (visual);

        centerSplitPane ();
        //    splitPane.addComponentListener
    }

    void centerSplitPane () {
        JSplitPane splitPane = (JSplitPane) getContainer ();
        java.awt.Dimension size = splitPane.getSize ();
        if (splitPane.getOrientation () == JSplitPane.HORIZONTAL_SPLIT)
            splitPane.setDividerLocation (size.width / 2);
        else
            splitPane.setDividerLocation (size.height / 2);
        //    splitPane.invalidate ();
        //    splitPane.validate ();
        //    splitPane.repaint ();
    }

    /** Removes specified component from this layout.
    * The default implementation removes the visual representation of the specified comp
    * from the container.
    * @param comp The component to remove
    * @see #addComponent
    */
    public void removeComponent (RADVisualComponent comp) {
        JSplitPaneConstraintsDescription cd = (JSplitPaneConstraintsDescription)comp.getConstraints(JSplitPaneSupportLayout.class);
        java.awt.Component visual = getFormManager ().getVisualRepresentation (comp);
        getContainer ().remove (visual);

        // find if there is some other component on the same side (which was previously not displayed due to the component
        // we are removing right now)
        RADVisualComponent comps[] = getRADContainer ().getSubComponents ();
        for (int i = 0; i < comps.length; i++) {
            if (comps [i].equals (comp))
                continue;
            JSplitPaneConstraintsDescription newCd = (JSplitPaneConstraintsDescription)comps[i].getConstraints(JSplitPaneSupportLayout.class);
            if ((cd.splitPos == POS_RIGHT) || (cd.splitPos == POS_BOTTOM)) {
                if ((newCd.splitPos == POS_RIGHT) || (newCd.splitPos == POS_BOTTOM)) {
                    java.awt.Component newVisual = getFormManager ().getVisualRepresentation (comps[i]);
                    ((JSplitPane)getContainer()).setRightComponent (newVisual);
                    centerSplitPane ();
                    return;
                }
            } else {
                if ((newCd.splitPos == POS_LEFT) || (newCd.splitPos == POS_BOTTOM)) {
                    java.awt.Component newVisual = getFormManager ().getVisualRepresentation (comps[i]);
                    ((JSplitPane)getContainer()).setLeftComponent (newVisual);
                    centerSplitPane ();
                    return;
                }
            }
        }
        // if no new component found ==>> set no component
        if ((cd.splitPos == POS_RIGHT) || (cd.splitPos == POS_BOTTOM)) {
            ((JSplitPane)getContainer()).setRightComponent (emptyRight);
        } else {
            ((JSplitPane)getContainer()).setLeftComponent (emptyLeft);
        }
        centerSplitPane ();

    }

    /** Generates the code for adding specified component to this layout.
    * @param comp   The component to be added to this layout
    * @param cont   The container that is managed by this layout
    */
    public String generateComponentCode(RADVisualContainer cont, RADVisualComponent comp) {
        JSplitPaneConstraintsDescription cd = (JSplitPaneConstraintsDescription)comp.getConstraints(JSplitPaneSupportLayout.class);
        StringBuffer buf = new StringBuffer();
        buf.append(createContainerGenName(cont));
        boolean horizontalOrientation = true;
        if (cont.getComponent () instanceof JSplitPane) {
            horizontalOrientation = (((JSplitPane)cont.getComponent ()).getOrientation () == JSplitPane.HORIZONTAL_SPLIT);
        }

        if (horizontalOrientation) {
            if ((cd.splitPos == POS_LEFT) || (cd.splitPos == POS_TOP)) {
                buf.append("setLeftComponent ("); // NOI18N
            } else {
                buf.append("setRightComponent ("); // NOI18N
            }
        } else {
            if ((cd.splitPos == POS_TOP) || (cd.splitPos == POS_LEFT)) {
                buf.append("setTopComponent ("); // NOI18N
            } else {
                buf.append("setBottomComponent ("); // NOI18N
            }
        }

        buf.append (comp.getName());
        buf.append (");\n"); // NOI18N

        return buf.toString();
    }

    /** Returns the layout's properties for specified component.
    * @param node the RADVisualComponent of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getComponentProperties(RADVisualComponent comp) {
        final RADVisualComponent componentNode = comp;
        return new Node.Property[] {
                   new PropertySupport.ReadWrite (PROP_SPLIT_POSITION, Integer.TYPE,
                                                  bundle.getString("PROP_jsplitpanecomp_splitPos"), bundle.getString("HINT_jsplitpanecomp_splitPos")) {

                       public Object getValue () {
                           JSplitPaneConstraintsDescription cd = (JSplitPaneConstraintsDescription)componentNode.getConstraints(JSplitPaneSupportLayout.class);
                           return new Integer (cd.splitPos);
                       }

                       public void setValue (Object val) {
                           if (val instanceof Integer) {
                               Object oldValue = getValue ();
                               JSplitPaneConstraintsDescription cd = (JSplitPaneConstraintsDescription)componentNode.getConstraints(JSplitPaneSupportLayout.class);
                               cd.splitPos = ((Integer)val).intValue ();
                               java.awt.Component visual = getRADContainer ().getFormManager ().getVisualRepresentation (componentNode);
                               if ((cd.splitPos == POS_LEFT) || (cd.splitPos == POS_TOP)) {
                                   ((JSplitPane) getContainerHelper ()).setLeftComponent (visual);
                               } else {
                                   ((JSplitPane) getContainerHelper ()).setRightComponent (visual);
                               }
                               firePropertyChange (componentNode, PROP_SPLIT_POSITION, oldValue, val);

                               updateContainer ();
                           }
                           else throw new IllegalArgumentException();
                       }

                       public java.beans.PropertyEditor getPropertyEditor () {
                           int orientation = ((JSplitPane) getContainerHelper ()).getOrientation ();
                           return new SplitPosEditor (orientation == JSplitPane.HORIZONTAL_SPLIT);
                       }
                   },

               };
    }

    void updateContainer () {
        getContainerHelper ().invalidate ();
        getContainerHelper ().validate ();
        getContainerHelper ().repaint ();
    }

    // -----------------------------------------------------------------------------
    // innerclasses

    public static class SplitPosEditor extends java.beans.PropertyEditorSupport {
        /** Display Names for alignment. */
        protected static final String[] tbNames = new String[] { "Top", "Bottom" }; // NOI18N
        protected static final String[] lrNames = new String[] { "Left", "Right" }; // NOI18N

        private boolean horizontal;

        public SplitPosEditor (boolean horizontal) {
            this.horizontal = horizontal;
        }

        /** @return names of the possible directions */
        public String[] getTags () {
            if (horizontal) return lrNames;
            else return tbNames;
        }

        /** @return text for the current value */
        public String getAsText () {
            int value = ((Integer)getValue ()).intValue ();
            switch (value) {
            case POS_TOP: if (!horizontal) return tbNames[0]; else return lrNames[0];
            case POS_BOTTOM: if (!horizontal) return tbNames[1]; else return lrNames[1];
            case POS_LEFT: if (horizontal) return lrNames[0]; else return tbNames[0];
            case POS_RIGHT: if (horizontal) return lrNames[1]; else return tbNames[1];
            }
            return null;
        }

        /** Setter.
        * @param str string equal to one value from directions array
        */
        public void setAsText (String str) {
            if (str.equals (tbNames[0])) setValue (new Integer (POS_TOP));
            else if (str.equals (tbNames[1])) setValue (new Integer (POS_BOTTOM));
            else if (str.equals (lrNames[0])) setValue (new Integer (POS_LEFT));
            else if (str.equals (lrNames[1])) setValue (new Integer (POS_RIGHT));
        }
    }

    final static class EmptyComponent extends javax.swing.JComponent {
        static final long serialVersionUID =4804600714927070059L;
        public void paint (java.awt.Graphics g) {
            java.awt.Dimension size = getSize ();
            g.setColor (java.awt.Color.black);
            g.drawString ("Empty", size.width / 2, size.height / 2); // NOI18N
            super.paint (g);
        }

        public java.awt.Dimension getPreferredSize () {
            return new java.awt.Dimension (100, 100);
        }
    }

    // -----------------------------------------------------------------------------
    // constraints innerclass

    /** The ConstraintsDescription class encapsulates constraints data and
    * operations on a constraints that will be used for adding components
    * to the layout.
    */
    final public static class JSplitPaneConstraintsDescription extends PlainDesignSupportLayout.PlainSupportConstraintsDescription {
        /** A JDK 1.1 serial version UID */
        static final long serialVersionUID = -9167777002792738110L;

        /** Netbeans class version */
        public static final NbVersion nbClassVersion = new NbVersion (1, 0); // saved from Beta3 (Build 129)

        public JSplitPaneConstraintsDescription () {
            this (POS_LEFT);
        }

        JSplitPaneConstraintsDescription (int pos) {
            splitPos = pos;
        }

        /** Returns a textual descriptions of constraints represented by this
        * class. E.g. for BorderLayout, it is a text "Center" or "North".
        * @return textual descriptions of the constraints
        */
        public String getConstraintsString() {
            switch (splitPos) {
            case POS_TOP: return bundle.getString ("MSG_ADD_jsplit_top");
            case POS_BOTTOM: return bundle.getString ("MSG_ADD_jsplit_bottom");
            case POS_RIGHT: return bundle.getString ("MSG_ADD_jsplit_right");
            default:
            case POS_LEFT: return bundle.getString ("MSG_ADD_jsplit_left");
            }
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

            oo.writeInt (splitPos);
        }

        /** Reads the object from stream.
        * @param oi input stream to read from
        * @exception IOException Includes any I/O exceptions that may occur
        * @exception ClassNotFoundException if the class of the read object is not found
        */
        public void readExternal (java.io.ObjectInput oi)
        throws java.io.IOException, ClassNotFoundException {
            org.netbeans.modules.form.FormUtils.DEBUG(">> JSplitPaneConstraintsDescription: readExternal: START"); // NOI18N
            // check the version
            NbVersion classVersion = (NbVersion) oi.readObject ();
            if (!nbClassVersion.isCompatible (classVersion))
                throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

            splitPos = oi.readInt ();
            org.netbeans.modules.form.FormUtils.DEBUG("<< JSplitPaneConstraintsDescription: readExternal: END"); // NOI18N
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
            if (!XML_SPLIT_PANE_CONSTRAINTS.equals (element.getNodeName ())) {
                throw new java.io.IOException ();
            }
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
            org.w3c.dom.Node node;
            node = attributes.getNamedItem (ATTR_POSITION);
            if (node != null) {
                String pos = node.getNodeValue ();
                if (VALUE_LEFT.equals (pos)) splitPos = POS_LEFT;
                else if (VALUE_RIGHT.equals (pos)) splitPos = POS_RIGHT;
                else if (VALUE_TOP.equals (pos)) splitPos = POS_TOP;
                else if (VALUE_BOTTOM.equals (pos)) splitPos = POS_BOTTOM;
                else splitPos = POS_LEFT; // not recognized => default
            }
        }

        /** Called to store current property value into XML subtree. The property value should be set using the
        * setValue method prior to calling this method.
        * @param doc The XML document to store the XML in - should be used for creating nodes only
        * @return the XML DOM element representing a subtree of XML from which the value should be loaded or null 
        *         if the value does not need to save any additional data and can be created using the default constructor
        */
        public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
            org.w3c.dom.Element el = doc.createElement (XML_SPLIT_PANE_CONSTRAINTS);
            switch (splitPos) {
            case POS_RIGHT:  el.setAttribute (ATTR_POSITION, VALUE_RIGHT); break;
            case POS_BOTTOM: el.setAttribute (ATTR_POSITION, VALUE_BOTTOM); break;
            case POS_TOP:    el.setAttribute (ATTR_POSITION, VALUE_TOP); break;
            case POS_LEFT:
            default:         el.setAttribute (ATTR_POSITION, VALUE_LEFT); break;
            }

            return el;
        }

        public static final String XML_SPLIT_PANE_CONSTRAINTS = "JSplitPaneConstraints"; // NOI18N
        public static final String ATTR_POSITION = "position"; // NOI18N
        public static final String VALUE_LEFT = "left"; // NOI18N
        public static final String VALUE_RIGHT = "right"; // NOI18N
        public static final String VALUE_TOP = "top"; // NOI18N
        public static final String VALUE_BOTTOM = "bottom"; // NOI18N

        /** The psoition on the JSplitPane */
        int splitPos;
    }
}

/*
 * Log
 *  17   Gandalf   1.16        2/16/00  Tran Duc Trung  FIX: layout constraints 
 *       need special treatments during pasting new components into JSplitPane
 *  16   Gandalf   1.15        2/15/00  Tran Duc Trung  FIX: pasting a component
 *       into JSplitPane causes NPE
 *  15   Gandalf   1.14        1/12/00  Ian Formanek    NOI18N
 *  14   Gandalf   1.13        11/27/99 Patrik Knakal   
 *  13   Gandalf   1.12        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   Gandalf   1.11        9/17/99  Ian Formanek    Fixed bug 3451 - Working
 *       with JSplitPane is not convenient
 *  11   Gandalf   1.10        9/12/99  Ian Formanek    Fixed bug 3573 - 
 *       Reloading JSplitPane project does not work correctly
 *  10   Gandalf   1.9         6/27/99  Ian Formanek    Removed indent parameter
 *       from code generation methods
 *  9    Gandalf   1.8         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    Gandalf   1.7         5/27/99  Ian Formanek    Fixed bug 1198 - 
 *       JSplitPane in HORIZONTAL mode sets the child components backwards 
 *       (duh!).
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
