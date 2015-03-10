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

import org.netbeans.modules.form.FormUtils;

/**
*
* @author   Petr Hamernik
* @version  1.02, Aug 07, 1998
*/
public class LineBorderInfo extends BorderInfoSupport {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2894884442032223844L;

    /** Default icons for Border in design time. */
    static protected Image defaultIcon;
    static protected Image defaultIcon32;

    static {
        defaultIcon = Toolkit.getDefaultToolkit ().getImage (
                          LineBorderInfo.class.getResource ("/org/netbeans/modules/form/resources/palette/lineBorder.gif")); // NOI18N
        defaultIcon32 = Toolkit.getDefaultToolkit ().getImage (
                            LineBorderInfo.class.getResource ("/org/netbeans/modules/form/resources/palette/lineBorder32.gif")); // NOI18N
    }

    private static final String PROP_LINECOLOR = "lineColor"; // NOI18N
    private static final String PROP_THICKNESS = "thickness"; // NOI18N

    static final private int[][] CONSTRUCTORS = new int[][] { { 0 }, { 0, 1 } };

    private static final LineBorder DEFAULT_BORDER = new LineBorder(Color.black);

    public LineBorderInfo() {
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
        return new LineBorder(Color.black);
    }

    private LineBorder getLineBorder() {
        return (LineBorder) getBorder();
    }

    private void setLineBorder(LineBorder border) {
        this.border = border;
    }

    private void setLineColor(Color color) {
        this.border = new LineBorder(color, getLineBorder().getThickness());
    }

    private void setThickness(int thickness) {
        this.border = new LineBorder(getLineBorder().getLineColor(), thickness);
    }

    /** Returns the border's properties for specified component.
    * The default implementation returns empty array.
    * @param node the RADVisualNode of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getProperties() {
        return new Node.Property[] {
                   new BorderProp(PROP_LINECOLOR, Color.class,
                                  BorderInfo.bundle.getString("PROP_LineColor"),
                                  BorderInfo.bundle.getString("HINT_LineColor")) {
                       public Object getValue () {
                           return getLineBorder().getLineColor();
                       }

                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Color) {
                               setLineColor((Color)val);
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }

                       public boolean isDefault() {
                           return DEFAULT_BORDER.getLineColor().equals(getLineBorder().getLineColor());
                       }
                   },
                   new BorderProp(PROP_THICKNESS, Integer.TYPE,
                                  BorderInfo.bundle.getString("PROP_Thickness"),
                                  BorderInfo.bundle.getString("HINT_Thickness")) {
                       public Object getValue () {
                           return new Integer(getLineBorder().getThickness());
                       }

                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Integer) {
                               setThickness(((Integer) val).intValue());
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return DEFAULT_BORDER.getThickness() == getLineBorder().getThickness();
                       }
                   }
               };
    }

    protected int[][] getConstructors() {
        return CONSTRUCTORS;
    }

    public String getDisplayName() {
        return BorderInfo.bundle.getString("NAME_LineBorder");
    }


    //--------------------------------------------------------------------------
    // XMLPersistence implementation

    public static final String XML_LINE_BORDER = "LineBorder"; // NOI18N

    public static final String ATTR_THICKNESS = "thickness"; // NOI18N
    public static final String ATTR_COLOR = "color"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
    * @exception IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_LINE_BORDER.equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            org.w3c.dom.Node aNode = attributes.getNamedItem (ATTR_THICKNESS);
            if(aNode != null) {
                String thicknessText = aNode.getNodeValue ();
                setThickness (Integer.parseInt (thicknessText));
            }
            Color read = (Color) FormUtils.readProperty (ATTR_COLOR, Color.class, element);
            if (read != null) {
                setLineColor (read);
            }
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
        org.w3c.dom.Element el = doc.createElement (XML_LINE_BORDER);
        el.setAttribute (ATTR_THICKNESS, Integer.toString (getLineBorder ().getThickness ()));
        if(!getLineBorder ().getLineColor ().equals (Color.black)) {
            FormUtils.writeProperty(ATTR_COLOR, getLineBorder ().getLineColor (), Color.class,  el, doc);
        }
        return el;
    }

}

/*
 * Log
 *  8    Gandalf   1.7         1/12/00  Ian Formanek    NOI18N
 *  7    Gandalf   1.6         12/9/99  Pavel Buzek     
 *  6    Gandalf   1.5         11/24/99 Pavel Buzek     added support for saving
 *       in XML format
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
