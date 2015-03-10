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

package org.netbeans.modules.form.compat2.border;

import java.awt.*;
import java.beans.*;

import javax.swing.border.*;

import org.openide.nodes.*;

/**
*
* @author   Petr Hamernik
* @version  1.02, Aug 07, 1998
*/
public class EmptyBorderInfo extends BorderInfoSupport {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -4875525069061190423L;

    /** Default icons for Border in design time. */
    static protected Image defaultIcon;
    static protected Image defaultIcon32;

    static {
        defaultIcon = Toolkit.getDefaultToolkit ().getImage (
                          EmptyBorderInfo.class.getResource ("/org/netbeans/modules/form/resources/palette/border.gif")); // NOI18N
        defaultIcon32 = Toolkit.getDefaultToolkit ().getImage (
                            EmptyBorderInfo.class.getResource ("/org/netbeans/modules/form/resources/palette/border32.gif")); // NOI18N
    }

    private static final String PROP_INSETS = "insets"; // NOI18N
    private static final Insets DEFAULT_INSETS = new Insets(1, 1, 1, 1);

    static final private int[][] CONSTRUCTORS = new int[][] { { 0 } };

    private Insets insets;
    private transient Node.Property insetsProp = null;

    public EmptyBorderInfo() {
        insets = new Insets(1, 1, 1, 1);
    }

    /** An icon of the border. This icon will be used on the ComponentPalette
    * for this layout's item.
    * @param  type the desired type of the icon (BeanInfo.ICON_???)
    * @return layout's icon.
    */
    public Image getIcon(int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16))
            return defaultIcon;
        else
            return defaultIcon32;
    }

    protected Border createDefaultBorder() {
        return new EmptyBorder(1, 1, 1, 1);
    }

    private EmptyBorder getEmptyBorder() {
        return (EmptyBorder) getBorder();
    }

    private void setInsets(Insets ins) {
        insets = ins;
        border = new EmptyBorder(ins);
    }

    /** Returns the border's properties for specified component.
    * The default implementation returns empty array.
    * @param node the RADVisualNode of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getProperties() {
        return new Node.Property[] {
                   getInsetsProperty()
               };
    }

    protected int[][] getConstructors() {
        return CONSTRUCTORS;
    }

    private Node.Property getInsetsProperty() {
        if (insetsProp == null) {
            insetsProp = new BorderProp(PROP_INSETS, Insets.class,
                                        BorderInfo.bundle.getString("PROP_Insets"),
                                        BorderInfo.bundle.getString("HINT_Insets")) {
                             public Object getValue () {
                                 return insets;
                             }

                             public void setValue (Object val) throws IllegalArgumentException {
                                 if (val instanceof Insets) {
                                     setInsets((Insets) val);
                                     firePropChange();
                                 }
                                 else throw new IllegalArgumentException();
                             }

                             public boolean isDefault() {
                                 return DEFAULT_INSETS.equals(insets);
                             }
                         };
        }
        return insetsProp;
    }

    public String getDisplayName() {
        return BorderInfo.bundle.getString("NAME_EmptyBorder");
    }

    //--------------------------------------------------------------------------
    // XMLPersistence implementation

    public static final String XML_EMPTY_BORDER = "EmptyBorder"; // NOI18N

    public static final String ATTR_TOP = "top"; // NOI18N
    public static final String ATTR_LEFT = "left"; // NOI18N
    public static final String ATTR_RIGHT = "right"; // NOI18N
    public static final String ATTR_BOTTOM = "bottom"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
    * @exception IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_EMPTY_BORDER.equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            String topText = attributes.getNamedItem (ATTR_TOP).getNodeValue ();
            String leftText = attributes.getNamedItem (ATTR_LEFT).getNodeValue ();
            String bottomText = attributes.getNamedItem (ATTR_BOTTOM).getNodeValue ();
            String rightText = attributes.getNamedItem (ATTR_RIGHT).getNodeValue ();
            insets = new Insets (Integer.parseInt (topText), Integer.parseInt (leftText), Integer.parseInt (bottomText), Integer.parseInt (rightText));
        } catch (Exception e) {
            throw new java.io.IOException (e.toString());
        }
    }

    /** Called to store current property value into XML subtree. The property value should be set using the
    * setValue method prior to calling this method.
    * @param doc The XML document to store the XML in - should be used for creating nodes only
    * @return the XML DOM element representing a subtree of XML from which the value should be loaded or null if there is no state to save
    */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        org.w3c.dom.Element el = doc.createElement (XML_EMPTY_BORDER);
        el.setAttribute (ATTR_TOP, Integer.toString (insets.top));
        el.setAttribute (ATTR_LEFT, Integer.toString (insets.left));
        el.setAttribute (ATTR_BOTTOM, Integer.toString (insets.bottom));
        el.setAttribute (ATTR_RIGHT, Integer.toString (insets.right));
        return el;
    }

}

/*
 * Log
 *  7    Gandalf   1.6         1/12/00  Ian Formanek    NOI18N
 *  6    Gandalf   1.5         12/9/99  Pavel Buzek     
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/2/99   Ian Formanek    preview of XML 
 *       serialization of borders
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         4/6/99   Ian Formanek    fixed obtaining 
 *       resources (Object.class.getResource -> getClass ().getResource)
 *  1    Gandalf   1.0         4/2/99   Ian Formanek    
 * $
 */
