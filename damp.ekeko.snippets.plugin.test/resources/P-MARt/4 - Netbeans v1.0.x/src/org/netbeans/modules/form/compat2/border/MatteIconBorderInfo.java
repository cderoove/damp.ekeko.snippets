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

import javax.swing.*;
import javax.swing.border.*;

import org.openide.nodes.*;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;

import org.netbeans.modules.form.FormUtils;
import org.openide.explorer.propertysheet.editors.IconEditor;

/**
 *
 * @author   Petr Hamernik
 * @version 1.02, Aug 07, 1998
 */

public class MatteIconBorderInfo extends MatteAbstractBorderInfo
{
    static final long serialVersionUID = 5453397794223729547L;

    /** Default icons for Border in design time. */
    static protected Image defaultIcon;
    static protected Image defaultIcon32;

    static {
        defaultIcon = Toolkit.getDefaultToolkit ().getImage (
                          MatteIconBorderInfo.class.getResource(
                              "/org/netbeans/modules/form/resources/palette/matteIconBorder.gif")); // NOI18N
        defaultIcon32 = Toolkit.getDefaultToolkit().getImage (
                            MatteIconBorderInfo.class.getResource(
                                "/org/netbeans/modules/form/resources/palette/matteIconBorder32.gif")); // NOI18N
    }

    private static final String PROP_ICON  = "icon"; // NOI18N

    private static final Icon DEFAULT_ICON;
    private static final IconEditor iconEd = new IconEditor();

    static {
        iconEd.setAsText("image.gif"); // NOI18N
        DEFAULT_ICON = (javax.swing.Icon) iconEd.getValue();
    }

    static final private int[][] CONSTRUCTORS = new int[][] { {0,1,2,3,4} };

    private Icon icon;

    public MatteIconBorderInfo() {
        icon = DEFAULT_ICON;
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
        return new MatteBorder(1, 1, 1, 1, DEFAULT_ICON);
    }

    protected void updateBorder() {
        border = new MatteBorder(top, left, bottom, right, icon);
    }

    /** Returns the border's properties for specified component.
    * The default implementation returns empty array.
    * @param node the RADVisualNode of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getProperties() {
        Node.Property[] ret = new Node.Property[5];
        Node.Property[] insets = getInsetsProperties();
        for (int i = 0; i <= 3; ret[i] = insets[i], i++)
            ;

        ret[4] = new BorderProp(PROP_ICON, Icon.class,
                                BorderInfo.bundle.getString("PROP_Icon"),
                                BorderInfo.bundle.getString("HINT_Icon")) {
                     public Object getValue () {
                         return icon;
                     }
                     public void setValue (Object val) throws IllegalArgumentException {
                         if (val == null) {
                             //XXX (-tdt) TopManager.getDefault().notify(new NotifyDescriptor.Message (BorderInfo.bundle.getString("MSG_CannotGenerate"), NotifyDescriptor.ERROR_MESSAGE));
                             val = new ImageIcon();
                             icon = (Icon) val;
                             updateBorder();
                             firePropChange();

                         } else
                             if (val instanceof Icon) {
                                 icon = (Icon) val;
                                 updateBorder();
                                 firePropChange();
                             }
                             else throw new IllegalArgumentException();
                     }
                     public boolean isDefault() {
                         return DEFAULT_ICON == null && icon == null
                                || icon != null && icon.equals(DEFAULT_ICON);
                     }
                 };

        return ret;
    }

    protected int[][] getConstructors() {
        return CONSTRUCTORS;
    }

    public String getDisplayName() {
        return BorderInfo.bundle.getString("NAME_MatteIconBorder");
    }

    //--------------------------------------------------------------------------
    // XMLPersistence implementation

    public static final String XML_MATTE_ICON_BORDER = "MatteIconBorder"; // NOI18N

    public static final String ATTR_TOP = "top"; // NOI18N
    public static final String ATTR_LEFT = "left"; // NOI18N
    public static final String ATTR_BOTTOM = "bottom"; // NOI18N
    public static final String ATTR_RIGHT = "right"; // NOI18N
    public static final String ATTR_ICON = "icon"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
    * @exception IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_MATTE_ICON_BORDER.equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            org.w3c.dom.Node aNode = attributes.getNamedItem (ATTR_TOP);
            if(aNode != null) {
                top = Integer.parseInt (aNode.getNodeValue ());
            }
            aNode = attributes.getNamedItem (ATTR_LEFT);
            if(aNode != null) {
                left = Integer.parseInt (aNode.getNodeValue ());
            }
            aNode = attributes.getNamedItem (ATTR_RIGHT);
            if(aNode != null) {
                right = Integer.parseInt (aNode.getNodeValue ());
            }
            aNode = attributes.getNamedItem (ATTR_BOTTOM);
            if(aNode != null) {
                bottom = Integer.parseInt (aNode.getNodeValue ());
            }
            Icon read;
            read = (Icon) FormUtils.readProperty (ATTR_ICON, Icon.class, element);
            if (read != null) icon =  read;

            updateBorder ();
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
        org.w3c.dom.Element el = doc.createElement (XML_MATTE_ICON_BORDER);
        if (top != 1) el.setAttribute (ATTR_TOP, Integer.toString (top));
        if (left != 1) el.setAttribute (ATTR_LEFT, Integer.toString (left));
        if (right != 1) el.setAttribute (ATTR_RIGHT, Integer.toString (right));
        if (bottom != 1) el.setAttribute (ATTR_BOTTOM, Integer.toString (bottom));
        if(icon != null && icon != DEFAULT_ICON && !icon.equals (DEFAULT_ICON)) {
            FormUtils.writeProperty (ATTR_ICON, icon, Icon.class, el, doc);
        }
        return el;
    }
}

/*
 * Log
 *  15   Gandalf-post-FCS1.13.1.0    3/27/00  Tran Duc Trung  FIX #6042: 
 *       MatteIconBorder references an image on www.netbeans.com website, this 
 *       causes the dial-up connection to be established
 *  14   Gandalf   1.13        1/13/00  Ian Formanek    NOI18N #2
 *  13   Gandalf   1.12        1/12/00  Ian Formanek    I18N
 *  12   Gandalf   1.11        1/12/00  Jesse Glick     URL change.
 *  11   Gandalf   1.10        1/12/00  Ian Formanek    NOI18N
 *  10   Gandalf   1.9         12/9/99  Pavel Buzek     
 *  9    Gandalf   1.8         12/3/99  Pavel Buzek     4704 fixed
 *  8    Gandalf   1.7         12/3/99  Pavel Buzek     default value for icon 
 *       is instance of NbIconImage
 *  7    Gandalf   1.6         11/24/99 Pavel Buzek     added support for saving
 *       in XML format
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/17/99  Ian Formanek    Fixed bug 1847 - X2 
 *       forms with MatteColorBorder or MatteIconBorder are not correctly opened
 *       in Gandalf
 *  3    Gandalf   1.2         4/6/99   Ian Formanek    Fixed last change
 *  2    Gandalf   1.1         4/6/99   Ian Formanek    fixed obtaining 
 *       resources (Object.class.getResource -> getClass ().getResource)
 *  1    Gandalf   1.0         4/2/99   Ian Formanek    
 * $
 */
