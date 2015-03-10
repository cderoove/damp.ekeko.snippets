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
import javax.swing.JTabbedPane;
import javax.swing.JPanel;

import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.netbeans.modules.form.util2.NbVersion;
import org.netbeans.modules.form.util2.NbVersionNotCompatibleException;
import org.openide.util.Utilities;
import org.openide.nodes.*;
import org.openide.explorer.ExplorerManager;
import org.netbeans.modules.form.*;

/** A design-time support for CardLayout.
*
* @author   Ian Formanek
*/
final public class DesignCardLayout extends DesignLayout {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = 4121523617913756950L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    /** Layout Properties */
    public static final String PROP_CURRENTCARD = "currentCard"; // NOI18N
    public static final String PROP_HGAP = "horizontalGap"; // NOI18N
    public static final String PROP_VGAP = "verticalGap"; // NOI18N

    /** Component Layout Properties */
    public static final String PROP_CARDNAME = FormEditor.LAYOUT_PREFIX + "cardName"; // NOI18N

    /* Default layout property values */
    private static final int DEFAULT_VGAP = 0;
    private static final int DEFAULT_HGAP = 0;

    /** bundle to obtain text information from */
    private static final java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle (DesignCardLayout.class);
    /** icons for the Layout. */
    private static final Image icon = Toolkit.getDefaultToolkit ().getImage (
                                          DesignCardLayout.class.getResource ("/org/netbeans/modules/form/resources/palette/cardLayout.gif")); // NOI18N
    private static final Image icon32 = Toolkit.getDefaultToolkit ().getImage (
                                            DesignCardLayout.class.getResource ("/org/netbeans/modules/form/resources/palette/cardLayout32.gif")); // NOI18N

    /** The Sheet holding layout property sheet */
    private Sheet sheet;

    private Node.Property currentCardProperty;
    private Node.Property hgapProperty;
    private Node.Property vgapProperty;

    /** Constructs a new DesignCardLayout */
    public DesignCardLayout () {
        currentCard = "card1"; // NOI18N
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
    public void setRADContainer (final RADVisualContainer cont) {
        if (cont == null) { // deattaching the layout
            if ((designComponent != null) && (getMode() == DESIGN_MODE)) {
                getContainer().remove(designComponent);
            }
            return;
        }

        super.setRADContainer(cont);
        if (cont != null) {
            realLayout = new CardLayout (hgap, vgap);
            designComponent = new JTabbedPane ();
            designComponent.addChangeListener (new javax.swing.event.ChangeListener () {
                                                   public void stateChanged (javax.swing.event.ChangeEvent evt) {
                                                       int selected = designComponent.getSelectedIndex();
                                                       if (selected >-1) {
                                                           currentCard = designComponent.getTitleAt (selected);
                                                           if (cont.getFormManager ().isInitialized ())
                                                               firePropertyChange (null, null, null, null);
                                                       }
                                                   }
                                               }
                                              );
            // add listener to track selection of nodes and switch cards
            // that contain selected component (the last one)
            final ExplorerManager em = FormEditor.getComponentInspector ().getExplorerManager ();
            em.addPropertyChangeListener (new java.beans.PropertyChangeListener () {
                                              public void propertyChange (java.beans.PropertyChangeEvent evt) {
                                                  if (ExplorerManager.PROP_SELECTED_NODES.equals (evt.getPropertyName ())) {
                                                      Node [] selected = em.getSelectedNodes ();
                                                      if (selected.length == 0) return;
                                                      if (selected [selected.length -1] instanceof RADComponentNode) {
                                                          RADComponentNode node = (RADComponentNode) selected [selected.length -1];
                                                          RADComponent c = node.getRADComponent ();
                                                          if (c instanceof RADVisualComponent) {
                                                              Component visual = getFormManager ().getVisualRepresentation ((RADVisualComponent) c);
                                                              int index = designComponent.indexOfComponent(visual);
                                                              if (index >= 0)
                                                                  designComponent.setSelectedIndex (index);
                                                          }
                                                      }
                                                  }
                                              }
                                          }
                                         );

            if (getMode() == DESIGN_MODE) {
                getContainer().setLayout(new BorderLayout());
                getContainer().add(designComponent);
                cont.getFormManager ().addMouseAdaptersForAdditional (designComponent, cont);
            } else {
                getContainer().setLayout(realLayout);
            }
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
            for (int i=0; i< children.length; i++) {
                CardConstraintsDescription ccd = (CardConstraintsDescription)children [i].getConstraints (DesignCardLayout.class);
                if (ccd == null) {
                    ccd = (CardConstraintsDescription) getConstraintsDescription (new Point ());
                    children[i].setConstraints (DesignCardLayout.class, ccd);
                }
                designComponent.addTab (ccd.getName (), getFormManager ().getVisualRepresentation (children[i]));
            }

            // XXX(-tdt) for some mysterious reason, the UI delegate is out of
            // sync. If we don't force updateUI() some children will be invisible
            designComponent.updateUI();

            designComponent.validate ();
            designComponent.repaint ();
        }
        else {
            RADVisualComponent[] children = getRADContainer().getSubComponents ();
            designComponent.removeAll();
            getContainer().remove(designComponent);
            getContainer().setLayout(realLayout);
            for (int i=0; i< children.length; i++) {
                CardConstraintsDescription ccd = (CardConstraintsDescription)children [i].getConstraints (DesignCardLayout.class);
                if (ccd == null) {
                    ccd = (CardConstraintsDescription) getConstraintsDescription (new Point ());
                    children[i].setConstraints (DesignCardLayout.class, ccd);
                }
                getContainer().add(getFormManager ().getVisualRepresentation (children[i]), ccd.getName ());
            }
            getContainer ().validate ();
            getContainer ().repaint ();
        }
    }

    /** Method which allows the design layout to provide list of properties to be saved with the form.
    * @return list of Node.Property objects
    */
    public java.util.List getChangedProperties () {
        getPropertySet (); // enforce creation of properties
        java.util.ArrayList list = new java.util.ArrayList (3);
        // it is useless to remember selected card - this variable is transient
        //if (!"card1".equals (currentCard)) list.add (currentCard); // NOI18N
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
        String currentCardVal = (String)changedProperties.get (PROP_CURRENTCARD); if (currentCardVal != null) currentCard = currentCardVal;
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
        int i = 1;
        while (designComponent.indexOfTab("card"+i)>-1) i++; // NOI18N
        return new CardConstraintsDescription ("card"+i); // NOI18N
    }

    /** A display name of the layout will be used for displaying the layout in
    * the components hierarchy during design-time.
    * @return layout's display name.
    */
    public String getDisplayName() {
        return "CardLayout"; // NOI18N
    }

    /** Returns the global layout's properties (i.e. the properties
    * that are not different for different components in the layout
    * @return the global layout properties
    */
    public Node.PropertySet[] getPropertySet() {
        if (sheet == null) {
            sheet = new Sheet ();
            Sheet.Set set = Sheet.createPropertiesSet ();

            set.put (hgapProperty = new PropertySupport.ReadWrite (
                                        PROP_HGAP,
                                        Integer.TYPE,
                                        bundle.getString("PROP_card_hgap"),
                                        bundle.getString("HINT_card_hgap"))
                                    {

                                        public Object getValue () {
                                            return new Integer(hgap);
                                        }

                                        public void setValue (Object val) throws IllegalArgumentException {
                                            if (val instanceof Integer) {
                                                Object oldValue = getValue ();
                                                hgap = ((Integer)val).intValue();
                                                realLayout.setHgap (hgap);
                                                getContainer().invalidate();
                                                getContainer().validate();
                                                getContainer().repaint ();
                                                firePropertyChange (null, PROP_HGAP, oldValue, new Integer(hgap));
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
                                        bundle.getString("PROP_card_vgap"),
                                        bundle.getString("HINT_card_vgap"))
                                    {

                                        public Object getValue () {
                                            return new Integer(vgap);
                                        }

                                        public void setValue (Object val) throws IllegalArgumentException {
                                            if (val instanceof Integer) {
                                                Object oldValue = getValue ();
                                                vgap = ((Integer)val).intValue();
                                                realLayout.setVgap (vgap);
                                                getContainer().invalidate();
                                                getContainer().validate();
                                                getContainer().repaint ();
                                                firePropertyChange (null, PROP_VGAP, oldValue, new Integer(vgap));
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

            set.put (currentCardProperty = new PropertySupport.ReadWrite (
                                               PROP_CURRENTCARD,
                                               String.class,
                                               bundle.getString("PROP_card_currentCard"),
                                               bundle.getString("HINT_card_currentCard"))
                                           {

                                               /** Getter for the value.
                                               * @return the value of the property
                                               */
                                               public Object getValue () {
                                                   return currentCard;
                                               }

                                               /** Setter for the value.
                                               * @param val the value of the property
                                               * @exception IllegalArgumentException wrong argument
                                               */
                                               public void setValue (Object val) throws IllegalArgumentException {
                                                   if (!(val instanceof String)) throw new IllegalArgumentException();
                                                   Object oldValue = getValue ();
                                                   currentCard = (String)val;
                                                   if (getMode() == DESIGN_MODE)
                                                       designComponent.setSelectedIndex (designComponent.indexOfTab(currentCard));
                                                   else
                                                       realLayout.show (getContainer (), currentCard);
                                                   firePropertyChange (null, PROP_CURRENTCARD, oldValue, currentCard);
                                               }


                                               /** Returns property editor for this property.
                                               * @return the property editor or <CODE>null</CODE> if there should not be
                                               *    any editor.
                                               */
                                               public java.beans.PropertyEditor getPropertyEditor () {
                                                   return new CardEditor (getRADContainer ());
                                               }

                                           }
                    );

            sheet.put (set);
        }

        return sheet.toArray ();
    }

    /** Returns the layout's properties for specified component.
    * @param node the RADVisualComponent of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getComponentProperties(RADVisualComponent node) {
        final RADVisualComponent componentNode = node;
        return new Node.Property[] {
                   new PropertySupport.ReadWrite (PROP_CARDNAME, String.class,
                                                  bundle.getString("PROP_cardcomp_cardName"), bundle.getString("HINT_cardcomp_cardName")) {

                       public Object getValue () {
                           CardConstraintsDescription ccd = (CardConstraintsDescription)componentNode.getConstraints(DesignCardLayout.class);
                           if (ccd == null) return null;
                           return ccd.getName();
                       }

                       public void setValue (Object val) {
                           if (val instanceof String) {
                               Object oldValue = getValue ();
                               CardConstraintsDescription ccd = (CardConstraintsDescription)componentNode.getConstraints(DesignCardLayout.class);
                               ccd.setName((String) val); // [PENDING - what if renaming to existing name]
                               Component visual = getFormManager ().getVisualRepresentation (componentNode);
                               if (getMode () == DESIGN_MODE) {
                                   int index = designComponent.indexOfTab ((String) oldValue);
                                   if (index >-1)
                                       designComponent.setTitleAt(index, (String) val);
                               } else {
                                   realLayout.removeLayoutComponent (visual);
                                   realLayout.addLayoutComponent (visual, val);
                               }
                               firePropertyChange (componentNode, PROP_CARDNAME, oldValue, val);
                           }
                           else throw new IllegalArgumentException();
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
        return CardLayout.class;
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
        CardConstraintsDescription ccd = (CardConstraintsDescription)node.getConstraints (DesignCardLayout.class);
        if (ccd == null || designComponent.indexOfTab(ccd.getName ()) > -1) { // tab may exists if it is paste operation
            ccd = (CardConstraintsDescription) getConstraintsDescription (new Point ());
            node.setConstraints (DesignCardLayout.class, ccd);
        }


        Component visual = getFormManager ().getVisualRepresentation (node);
        if (getMode() == DESIGN_MODE) {
            designComponent.addTab (ccd.getName (), visual);
        } else {
            getContainer ().add(visual, ccd.getName ());
        }
    }

    /** Removes specified component from this layout.
    * @param node The component to remove
    * @see #addComponent
    */
    public void removeComponent (RADVisualComponent node) {
        Component visual = getFormManager ().getVisualRepresentation (node);
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
            if (defaultLM.getClass ().equals (CardLayout.class)) {
                if ((((CardLayout)defaultLM).getHgap () == hgap) && (((CardLayout)defaultLM).getVgap () == vgap)) {
                    return null; // the default layout is the same as current settings => no need to generate layout
                }
            }
        }

        StringBuffer buf = new StringBuffer();
        buf.append(createContainerGenName(cont));
        if ((hgap != DEFAULT_HGAP) || (vgap != DEFAULT_VGAP)) {
            buf.append("setLayout (new java.awt.CardLayout ("); // NOI18N
            buf.append(hgap);
            buf.append(", "); // NOI18N
            buf.append(vgap);
            buf.append("));\n"); // NOI18N
        }
        else {
            buf.append("setLayout (new java.awt.CardLayout ());\n"); // NOI18N
        }
        return buf.toString();
    }

    /** Generates the code for adding specified component to this layout.
    * @param comp   The component to be added to this layout
    * @param cont   The container that is managed by this layout
    */
    public String generateComponentCode(RADVisualContainer cont, RADVisualComponent comp) {
        CardConstraintsDescription ccd = (CardConstraintsDescription)comp.getConstraints (DesignCardLayout.class);

        StringBuffer buf = new StringBuffer();
        buf.append(createContainerGenName(cont));
        buf.append("add ("); // NOI18N
        buf.append(comp.getName());
        buf.append(", \""); // NOI18N
        String s = ccd.getName ();
        s = Utilities.replaceString (s, "\n", "\\n"); // NOI18N
        s = Utilities.replaceString (s, "\\", "\\\\"); // NOI18N
        s = Utilities.replaceString (s, "\"", "\\\""); // NOI18N
        buf.append(s);
        buf.append("\");\n"); // NOI18N

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
        org.netbeans.modules.form.FormUtils.DEBUG(">> DesignCardLayout: readExternal: START"); // NOI18N
        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

        vgap = oi.readInt ();
        hgap = oi.readInt ();
        org.netbeans.modules.form.FormUtils.DEBUG("<< DesignCardLayout: readExternal: END"); // NOI18N
    }


    // -----------------------------------------------------------------------------
    // Innerclasses

    final public static class CardEditor extends java.beans.PropertyEditorSupport {

        private RADVisualContainer containerNode;

        CardEditor (RADVisualContainer node) {
            this.containerNode = node;
        }

        /** @return names of the possible directions */
        public String[] getTags () {
            RADComponent[] subComponents = containerNode.getSubBeans ();
            String[] names = new String[subComponents.length];
            for (int i = 0; i < subComponents.length; i++)
                names [i] = (String)((CardConstraintsDescription)((RADVisualComponent)subComponents[i]).getConstraints (DesignCardLayout.class)).getConstraintsObject ();
            // [PENDING - incorrect cast]
            return names;
        }
    }

    // -----------------------------------------------------------------------------
    // constraints innerclass

    /** The ConstraintsDescription class encapsulates constraints data and
    * operations on a constraints that will be used for adding components
    * to the layout.
    */
    final public static class CardConstraintsDescription extends DesignLayout.ConstraintsDescription {
        /** A JDK 1.1. serial version UID */
        static final long serialVersionUID = -7082212875277865652L;

        /** Netbeans class version */
        public static final NbVersion nbClassVersion = new NbVersion (1, 0);

        /** For externalization only */
        public CardConstraintsDescription () {
        }

        public CardConstraintsDescription (String name) {
            this.name = name;
        }

        public String getConstraintsString () {
            return java.text.MessageFormat.format (
                       bundle.getString ("MSG_FMT_card_add"),
                       new Object[] { name }
                   );
        }

        public Object getConstraintsObject () {
            return name;
        }

        String getName () {
            return name;
        }

        void setName (String value) {
            name = value;
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

            oo.writeObject (name);
        }

        /** Reads the object from stream.
        * @param oi input stream to read from
        * @exception IOException Includes any I/O exceptions that may occur
        * @exception ClassNotFoundException if the class of the read object is not found
        */
        public void readExternal (java.io.ObjectInput oi)
        throws java.io.IOException, ClassNotFoundException {
            org.netbeans.modules.form.FormUtils.DEBUG(">> CardConstraintsDescription: readExternal: START"); // NOI18N
            // check the version
            NbVersion classVersion = (NbVersion) oi.readObject ();
            if (!nbClassVersion.isCompatible (classVersion))
                throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

            name = (String) oi.readObject ();
            org.netbeans.modules.form.FormUtils.DEBUG("<< CardConstraintsDescription: readExternal: END"); // NOI18N
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
            if (!XML_CARD_CONSTRAINTS.equals (element.getNodeName ())) {
                throw new java.io.IOException ();
            }
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
            org.w3c.dom.Node cardNode = attributes.getNamedItem (ATTR_CARD_NAME);
            if (cardNode != null) name = cardNode.getNodeValue ();
        }

        /** Called to store current property value into XML subtree. The property value should be set using the
        * setValue method prior to calling this method.
        * @param doc The XML document to store the XML in - should be used for creating nodes only
        * @return the XML DOM element representing a subtree of XML from which the value should be loaded or null 
        *         if the value does not need to save any additional data and can be created using the default constructor
        */
        public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
            org.w3c.dom.Element el = doc.createElement (XML_CARD_CONSTRAINTS);
            el.setAttribute (ATTR_CARD_NAME, name);
            return el;
        }

        public static final String XML_CARD_CONSTRAINTS = "CardConstraints"; // NOI18N
        public static final String ATTR_CARD_NAME = "cardName"; // NOI18N

        private String name = ""; // NOI18N
    }

    // -----------------------------------------------------------------------------
    // private area

    transient private String currentCard;

    /** The layout-design mode component */
    transient private JTabbedPane designComponent;

    transient private CardLayout realLayout;

    private int vgap = DEFAULT_VGAP;
    private int hgap = DEFAULT_HGAP;

}

/*
 * Log
 *  26   Gandalf   1.25        3/7/00   Tran Duc Trung  FIX: child components 
 *       disappear after switching to test mode
 *  25   Gandalf   1.24        1/26/00  Pavel Buzek     firePropertyChange only 
 *       if form is loaded
 *  24   Gandalf   1.23        1/13/00  Ian Formanek    NOI18N #2
 *  23   Gandalf   1.22        1/12/00  Ian Formanek    NOI18N
 *  22   Gandalf   1.21        1/11/00  Pavel Buzek     updating currentCard 
 *       property
 *  21   Gandalf   1.20        1/8/00   Pavel Buzek     #5058
 *  20   Gandalf   1.19        12/13/99 Pavel Buzek     copy/paste operations 
 *       handled properly (esp. painting)
 *  19   Gandalf   1.18        12/9/99  Pavel Buzek     
 *  18   Gandalf   1.17        12/8/99  Pavel Buzek     displayed as JTabbedPane
 *       at design time
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
 *  12   Gandalf   1.11        7/13/99  Ian Formanek    XML Persistence
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
