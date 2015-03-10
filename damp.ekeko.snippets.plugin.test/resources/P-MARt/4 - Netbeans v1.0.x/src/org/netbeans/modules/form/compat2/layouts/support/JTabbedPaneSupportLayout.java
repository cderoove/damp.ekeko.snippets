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

import javax.swing.JTabbedPane;
import javax.swing.Icon;

import org.openide.explorer.propertysheet.editors.IconEditor;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.netbeans.modules.form.util2.NbVersion;
import org.netbeans.modules.form.util2.NbVersionNotCompatibleException;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.compat2.layouts.DesignLayout;


/** A design-time support layout.
*
* @author   Ian Formanek
*/
final public class JTabbedPaneSupportLayout extends PlainDesignSupportLayout {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = 5027333499529564152L;

    public final static String PROP_TABNAME = "tabName"; // NOI18N
    public final static String PROP_TABICON = "tabIcon"; // NOI18N
    public final static String PROP_TABTOOLTIP = "tabToolTip"; // NOI18N

    private final static java.util.ResourceBundle bundle = NbBundle.getBundle (JTabbedPaneSupportLayout.class);

    private IconEditor iconEditor = new IconEditor ();

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
        return new JTabbedPaneConstraintsDescription ();
    }

    /** Returns the layout's properties for specified component.
    * @param comp the RADVisualComponent of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getComponentProperties(RADVisualComponent comp) {
        final RADVisualComponent component = comp;
        return new Node.Property[] {
                   new PropertySupport.ReadWrite (PROP_TABNAME, String.class,
                                                  bundle.getString("PROP_jtabbedpanecomp_tabname"), bundle.getString("HINT_jtabbedpanecomp_tabname")) {

                       public Object getValue () {
                           JTabbedPaneConstraintsDescription cd = (JTabbedPaneConstraintsDescription)component.getConstraints(JTabbedPaneSupportLayout.class);
                           return cd.tabName;
                       }

                       public void setValue (Object val) {
                           if (val instanceof String) {
                               Object oldValue = getValue ();
                               JTabbedPaneConstraintsDescription cd = (JTabbedPaneConstraintsDescription)component.getConstraints(JTabbedPaneSupportLayout.class);
                               cd.tabName = (String)val;
                               ((JTabbedPane) getContainerHelper ()).setTitleAt (component.getComponentIndex (), (String)val);
                               getContainerHelper ().invalidate ();
                               getContainerHelper ().validate ();
                               getContainerHelper ().repaint ();
                               firePropertyChange (component, PROP_TABNAME, oldValue, val);
                           }
                           else throw new IllegalArgumentException();
                       }

                   },
                   new PropertySupport.ReadWrite (PROP_TABICON, IconEditor.NbImageIcon.class,
                                                  bundle.getString("PROP_jtabbedpanecomp_tabicon"), bundle.getString("HINT_jtabbedpanecomp_tabicon")) {

                       public Object getValue () {
                           JTabbedPaneConstraintsDescription cd = (JTabbedPaneConstraintsDescription)component.getConstraints(JTabbedPaneSupportLayout.class);
                           return cd.image;
                       }

                       public void setValue (Object val) {
                           if (val instanceof IconEditor.NbImageIcon) {
                               Object oldValue = getValue ();
                               JTabbedPaneConstraintsDescription cd = (JTabbedPaneConstraintsDescription)component.getConstraints(JTabbedPaneSupportLayout.class);
                               cd.image = (IconEditor.NbImageIcon)val;
                               ((JTabbedPane) getContainerHelper ()).setIconAt (component.getComponentIndex (), (Icon)val);
                               getContainerHelper ().invalidate ();
                               getContainerHelper ().validate ();
                               getContainerHelper ().repaint ();
                               firePropertyChange (component, PROP_TABICON, oldValue, val);
                           }
                           else throw new IllegalArgumentException();
                       }

                       public java.beans.PropertyEditor getPropertyEditor () {
                           return iconEditor;
                       }
                   },
                   new PropertySupport.ReadWrite (PROP_TABTOOLTIP, String.class,
                                                  bundle.getString("PROP_jtabbedpanecomp_tabtooltip"), bundle.getString("HINT_jtabbedpanecomp_tabtooltip")) {

                       public Object getValue () {
                           JTabbedPaneConstraintsDescription cd = (JTabbedPaneConstraintsDescription)component.getConstraints(JTabbedPaneSupportLayout.class);
                           return cd.tip;
                       }

                       public void setValue (Object val) {
                           if (val instanceof String) {
                               Object oldValue = getValue ();
                               JTabbedPaneConstraintsDescription cd = (JTabbedPaneConstraintsDescription)component.getConstraints(JTabbedPaneSupportLayout.class);
                               cd.tip = (String)val;
                               firePropertyChange (component, PROP_TABTOOLTIP, oldValue, val);
                           }
                           else throw new IllegalArgumentException();
                       }

                   },
               };
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
        DesignLayout.ConstraintsDescription ocd = (DesignLayout.ConstraintsDescription) comp.getConstraints (JTabbedPaneSupportLayout.class);
        JTabbedPaneConstraintsDescription cd = null;
        if (ocd instanceof JTabbedPaneConstraintsDescription)
            cd = (JTabbedPaneConstraintsDescription) ocd;
        if (cd == null) {
            cd = new JTabbedPaneConstraintsDescription ();
            cd.tabName = comp.getName (); // use variable name as the name of the tab
            comp.setConstraints (JTabbedPaneSupportLayout.class, cd);
        } else if (cd.tabName == null) {
            cd.tabName = comp.getName ();
        }

        java.awt.Component visual = getFormManager ().getVisualRepresentation (comp);
        try {
            ((JTabbedPane)getContainer()).insertTab (cd.tabName, cd.image, visual, "", comp.getComponentIndex ()); // NOI18N
        } catch (ArrayIndexOutOfBoundsException e) {
            // [IAN] patch for loading JTabbedPane with modified selectedTab property
            // Fixes bug 1267 - When a form contains a JTabbedPane with modified selectedTab property,
            // it fails to load with ArrayIndexOutOfBoundsException
        }
        getContainer().invalidate ();
        getContainer().validate ();
        getContainer().repaint ();
    }

    /** Generates the code for adding specified component to this layout.
    * @param comp   The component to be added to this layout
    * @param cont   The container that is managed by this layout
    */
    public String generateComponentCode(RADVisualContainer cont, RADVisualComponent comp) {
        JTabbedPaneConstraintsDescription cd = (JTabbedPaneConstraintsDescription)comp.getConstraints(JTabbedPaneSupportLayout.class);
        StringBuffer buf = new StringBuffer();
        buf.append(createContainerGenName(cont));
        buf.append("addTab ("); // NOI18N
        buf.append ("\""); // NOI18N
        buf.append (cd.tabName);
        buf.append ("\""); // NOI18N
        buf.append (", "); // NOI18N

        // if we have icon or tip, we must generate longer version
        if ((cd.image != null) || ((cd.tip != null) && (!"".equals (cd.tip)))) { // NOI18N
            if (cd.image == null) {
                buf.append ("null, "); // NOI18N
            }
            else {
                iconEditor.setValue (cd.image);
                buf.append (iconEditor.getJavaInitializationString ());
                buf.append (", "); // NOI18N
            }
            buf.append (comp.getName());

            if ((cd.tip != null) && (!"".equals (cd.tip))) { // NOI18N
                buf.append (", "); // NOI18N
                buf.append ("\""); // NOI18N
                buf.append (cd.tip);
                buf.append ("\""); // NOI18N
            }
        } else {
            buf.append (comp.getName());
        }

        buf.append (");\n"); // NOI18N

        return buf.toString();
    }

    protected java.awt.Container getContainerHelper () {
        return getContainer ();
    }

    // -----------------------------------------------------------------------------
    // constraints innerclass

    /** The ConstraintsDescription class encapsulates constraints data and
    * operations on a constraints that will be used for adding components
    * to the layout.
    */
    final public static class JTabbedPaneConstraintsDescription extends PlainDesignSupportLayout.PlainSupportConstraintsDescription {
        /** A JDK 1.1 serial version UID */
        static final long serialVersionUID = -867579720914538248L;

        /** Netbeans class version */
        public static final NbVersion nbClassVersion = new NbVersion (1, 0); // saved from Beta3 (Build 129)

        public String getTabName() {
            return tabName;
        }

        public String getToolTip() {
            return tip;
        }

        public javax.swing.ImageIcon getImage() {
            return image;
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

            oo.writeObject (tabName);
            oo.writeObject (image);
            oo.writeObject (tip);
        }

        /** Reads the object from stream.
        * @param oi input stream to read from
        * @exception IOException Includes any I/O exceptions that may occur
        * @exception ClassNotFoundException if the class of the read object is not found
        */
        public void readExternal (java.io.ObjectInput oi)
        throws java.io.IOException, ClassNotFoundException {
            org.netbeans.modules.form.FormUtils.DEBUG(">> JTabbedPaneConstraintsDescription: readExternal: START"); // NOI18N
            // check the version
            NbVersion classVersion = (NbVersion) oi.readObject ();
            if (!nbClassVersion.isCompatible (classVersion))
                throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

            tabName = (String) oi.readObject ();
            image = (IconEditor.NbImageIcon) oi.readObject ();
            tip = (String) oi.readObject ();
            org.netbeans.modules.form.FormUtils.DEBUG("<< JTabbedPaneConstraintsDescription: readExternal: END"); // NOI18N
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
            if (!XML_TABBED_PANE_CONSTRAINTS.equals (element.getNodeName ())) {
                throw new java.io.IOException ();
            }
            try {
                org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
                org.w3c.dom.Node node;
                node = attributes.getNamedItem (ATTR_TAB_NAME); if (node != null) tabName = node.getNodeValue ();
                node = attributes.getNamedItem (ATTR_TAB_TOOLTIP); if (node != null) tip = node.getNodeValue ();
                IconEditor.NbImageIcon read;
                read = (IconEditor.NbImageIcon) FormUtils.readProperty (ATTR_TAB_ICON, Icon.class, element);
                if (read != null) image =  read;
            } catch (Exception e) {
                throw new java.io.IOException (e.toString());
            }
        }

        /** Called to store current property value into XML subtree. The property value should be set using the
        * setValue method prior to calling this method.
        * @param doc The XML document to store the XML in - should be used for creating nodes only
        * @return the XML DOM element representing a subtree of XML from which the value should be loaded or null 
        *         if the value does not need to save any additional data and can be created using the default constructor
        */
        public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
            org.w3c.dom.Element el = doc.createElement (XML_TABBED_PANE_CONSTRAINTS);
            el.setAttribute (ATTR_TAB_NAME, tabName);
            el.setAttribute (ATTR_TAB_TOOLTIP, tip);
            if (image != null)
                FormUtils.writeProperty (ATTR_TAB_ICON, image, Icon.class, el, doc);
            return el;
        }

        public static final String XML_TABBED_PANE_CONSTRAINTS = "JTabbedPaneConstraints"; // NOI18N
        public static final String ATTR_TAB_NAME = "tabName"; // NOI18N
        public static final String ATTR_TAB_TOOLTIP = "toolTip"; // NOI18N
        public static final String ATTR_TAB_ICON = "icon"; // NOI18N

        /** The icon represented by this JTabbedPaneConstraintsDescription class */
        IconEditor.NbImageIcon image;

        /** The tab name represented by this JTabbedPaneConstraintsDescription class */
        String tabName;

        /** The tooltip name represented by this JTabbedPaneConstraintsDescription class */
        String tip = ""; // NOI18N
    }

}

/*
 * Log
 *  20   Gandalf   1.19        3/7/00   Tran Duc Trung  fix #5791: cannot add 
 *       serialized bean to component palette
 *  19   Gandalf   1.18        1/12/00  Ian Formanek    NOI18N
 *  18   Gandalf   1.17        1/8/00   Pavel Buzek     #4962
 *  17   Gandalf   1.16        12/9/99  Pavel Buzek     saving icon at tab
 *  16   Gandalf   1.15        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   Gandalf   1.14        7/31/99  Ian Formanek    Fixed image on tabs
 *  14   Gandalf   1.13        7/13/99  Ian Formanek    XML Persistence
 *  13   Gandalf   1.12        6/30/99  Ian Formanek    Reflecting package 
 *       changes of some property editors
 *  12   Gandalf   1.11        6/27/99  Ian Formanek    Removed indent parameter
 *       from code generation methods
 *  11   Gandalf   1.10        6/10/99  Ian Formanek    Regeneration on layout 
 *       changes
 *  10   Gandalf   1.9         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         5/15/99  Ian Formanek    
 *  8    Gandalf   1.7         5/15/99  Ian Formanek    
 *  7    Gandalf   1.6         5/14/99  Ian Formanek    
 *  6    Gandalf   1.5         5/12/99  Ian Formanek    
 *  5    Gandalf   1.4         5/10/99  Ian Formanek    Fixed to compile
 *  4    Gandalf   1.3         5/10/99  Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  2    Gandalf   1.1         3/29/99  Ian Formanek    Uses FormUtils.DEBUG to 
 *       print messages
 *  1    Gandalf   1.0         3/29/99  Ian Formanek    
 * $
 */
