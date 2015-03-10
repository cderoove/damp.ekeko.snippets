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
import javax.swing.JButton;

import org.openide.explorer.propertysheet.editors.ChoicePropertyEditor;
import org.openide.nodes.*;

import org.netbeans.modules.form.FormUtils;

/**
*
* @author   Petr Hamernik
* @version  1.02, Aug 07, 1998
*/
public class EtchedBorderInfo extends BorderInfoSupport {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -3479177261832141811L;

    /** Default icons for Border in design time. */
    static protected Image defaultIcon;
    static protected Image defaultIcon32;

    static {
        defaultIcon = Toolkit.getDefaultToolkit ().getImage (
                          EtchedBorderInfo.class.getResource ("/org/netbeans/modules/form/resources/palette/etchedBorder.gif")); // NOI18N
        defaultIcon32 = Toolkit.getDefaultToolkit ().getImage (
                            EtchedBorderInfo.class.getResource ("/org/netbeans/modules/form/resources/palette/etchedBorder32.gif")); // NOI18N
    }

    private static final String PROP_BEVEL_TYPE = "bevelType"; // NOI18N
    private static final String PROP_HIGHLIGHT = "highlight"; // NOI18N
    private static final String PROP_SHADOW = "shadow"; // NOI18N

    private final static EtchedBorder DEFAULT_BORDER = new EtchedBorder();
    private final static Color DEFAULT_HIGHLIGHT = DEFAULT_BORDER.getHighlightColor(new JButton());
    private final static Color DEFAULT_SHADOW = DEFAULT_BORDER.getShadowColor(new JButton());

    private static final int[][] constructors = new int[][] { {}, {0}, {1,2}, {0,1,2} };

    private int bevelType;
    private Color highlight;
    private Color shadow;

    public EtchedBorderInfo() {
        super();

        EtchedBorder b = getEtchedBorder();
        bevelType = b.getEtchType();
        highlight = DEFAULT_HIGHLIGHT;
        shadow = DEFAULT_SHADOW;
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
        return new EtchedBorder();
    }

    private EtchedBorder getEtchedBorder() {
        return (EtchedBorder) getBorder();
    }

    private void updateBorder() {
        this.border = new EtchedBorder(bevelType, highlight, shadow);
    }

    /** Returns the border's properties for specified component.
    * The default implementation returns empty array.
    * @param node the RADVisualNode of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getProperties() {
        return new Node.Property[] {
                   new BorderProp(PROP_BEVEL_TYPE, Integer.TYPE,
                                  BorderInfo.bundle.getString("PROP_EtchType"),
                                  BorderInfo.bundle.getString("HINT_EtchType")) {
                       public Object getValue () {
                           return new Integer(bevelType);
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Integer) {
                               bevelType = ((Integer)val).intValue();
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return DEFAULT_BORDER.getEtchType() == bevelType;
                       }
                       public PropertyEditor getPropertyEditor() {
                           return new ChoicePropertyEditor(
                                      new int[] { EtchedBorder.RAISED, EtchedBorder.LOWERED },
                                      new String[] {
                                          BorderInfo.bundle.getString("VALUE_EtchRaised"),
                                          BorderInfo.bundle.getString("VALUE_EtchLowered")
                                      }
                                  );
                       }
                   },

                   new BorderProp(PROP_HIGHLIGHT, Color.class,
                                  BorderInfo.bundle.getString("PROP_Highlight"),
                                  BorderInfo.bundle.getString("HINT_Highlight")) {
                       public Object getValue () {
                           return highlight;
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Color) {
                               highlight = (Color)val;
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return DEFAULT_HIGHLIGHT.equals(highlight);
                       }
                   },

                   new BorderProp(PROP_SHADOW, Color.class,
                                  BorderInfo.bundle.getString("PROP_Shadow"),
                                  BorderInfo.bundle.getString("HINT_Shadow")) {
                       public Object getValue () {
                           return shadow;
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Color) {
                               shadow = (Color)val;
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return DEFAULT_SHADOW.equals(shadow);
                       }
                   }
               };
    }

    protected int[][] getConstructors() {
        return constructors;
    }

    public String getDisplayName() {
        return BorderInfo.bundle.getString("NAME_EtchedBorder");
    }

    //--------------------------------------------------------------------------
    // XMLPersistence implementation

    public static final String XML_ETCHET_BORDER = "EtchetBorder"; // NOI18N

    public static final String ATTR_TYPE = "bevelType"; // NOI18N
    public static final String ATTR_HIGHLIGHT = "highlight"; // NOI18N
    public static final String ATTR_SHADOW = "shadow"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
    * @exception IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_ETCHET_BORDER.equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            org.w3c.dom.Node aNode = attributes.getNamedItem (ATTR_TYPE);
            if(aNode != null) {
                bevelType = Integer.parseInt (aNode.getNodeValue ());
            }
            Color read;
            read = (Color) FormUtils.readProperty (ATTR_HIGHLIGHT, Color.class, element);
            if (read != null) highlight =  read;
            read = (Color) FormUtils.readProperty (ATTR_SHADOW, Color.class, element);
            if (read != null) shadow =  read;

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
        org.w3c.dom.Element el = doc.createElement (XML_ETCHET_BORDER);
        if (bevelType != DEFAULT_BORDER.getEtchType()) el.setAttribute (ATTR_TYPE, Integer.toString (bevelType));
        if(!highlight.equals (DEFAULT_HIGHLIGHT)) {
            FormUtils.writeProperty (ATTR_HIGHLIGHT, highlight, Color.class, el, doc);
        }
        if(!shadow.equals (DEFAULT_SHADOW)) {
            FormUtils.writeProperty (ATTR_SHADOW, shadow, Color.class, el, doc);
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
 *  4    Gandalf   1.3         6/30/99  Ian Formanek    Reflecting package 
 *       change of CHoicePropertyEditor
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         4/6/99   Ian Formanek    fixed obtaining 
 *       resources (Object.class.getResource -> getClass ().getResource)
 *  1    Gandalf   1.0         4/2/99   Ian Formanek    
 * $
 */
