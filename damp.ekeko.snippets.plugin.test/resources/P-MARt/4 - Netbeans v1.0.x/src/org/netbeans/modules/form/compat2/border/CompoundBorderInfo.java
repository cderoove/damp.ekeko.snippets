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
public class CompoundBorderInfo extends BorderInfoSupport {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 8703898186014934457L;

    /** Default icons for Border in design time. */
    static protected Image defaultIcon;
    static protected Image defaultIcon32;

    static {
        defaultIcon = Toolkit.getDefaultToolkit ().getImage (
                          CompoundBorderInfo.class.getResource ("/org/netbeans/modules/form/resources/palette/compoundBorder.gif")); // NOI18N
        defaultIcon32 = Toolkit.getDefaultToolkit ().getImage (
                            CompoundBorderInfo.class.getResource ("/org/netbeans/modules/form/resources/palette/compoundBorder32.gif")); // NOI18N
    }

    private static final String PROP_OUTSIDE = "outside"; // NOI18N
    private static final String PROP_INSIDE = "inside"; // NOI18N

    private final static CompoundBorder DEFAULT_BORDER = new CompoundBorder();

    private static final int[][] constructors = new int[][] { {0,1} };

    private Border outside;
    private Border inside;

    public CompoundBorderInfo() {
        outside = null;
        inside = null;
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
        return DEFAULT_BORDER;
    }

    private void updateBorder() {
        this.border = new CompoundBorder(outside, inside);
    }

    /** Returns the border's properties for specified component.
    * The default implementation returns empty array.
    * @param node the RADVisualNode of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getProperties() {
        return new Node.Property[] {
                   new BorderProp(PROP_OUTSIDE, Border.class,
                                  BorderInfo.bundle.getString("PROP_OutsideBorder"),
                                  BorderInfo.bundle.getString("HINT_OutsideBorder")) {
                       public Object getValue () {
                           return outside;
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if ((val == null) || (val instanceof Border)) {
                               outside = (Border) val;
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return false;
                       }
                   },

                   new BorderProp(PROP_INSIDE, Border.class,
                                  BorderInfo.bundle.getString("PROP_InsideBorder"),
                                  BorderInfo.bundle.getString("HINT_InsideBorder")) {
                       public Object getValue () {
                           return inside;
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if ((val == null) || (val instanceof Border)) {
                               inside = (Border) val;
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return false;
                       }
                   }
               };
    }

    protected int[][] getConstructors() {
        return constructors;
    }

    public String getDisplayName() {
        return BorderInfo.bundle.getString("NAME_CompoundBorder");
    }

    //--------------------------------------------------------------------------
    // XMLPersistence implementation

    public static final String XML_COMPOUND_BORDER = "CompundBorder"; // NOI18N

    public static final String ATTR_OUTSIDE = "outside"; // NOI18N
    public static final String ATTR_INSIDE = "inside"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
    * @exception IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_COMPOUND_BORDER.equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            Border read;
            read = (Border) FormUtils.readProperty (ATTR_OUTSIDE, Border.class, element);
            if (read != null) outside =  read;
            read = (Border) FormUtils.readProperty (ATTR_INSIDE, Border.class, element);
            if (read != null) inside =  read;

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
        org.w3c.dom.Element el = doc.createElement (XML_COMPOUND_BORDER);
        if(outside != null) {
            FormUtils.writeProperty (ATTR_OUTSIDE, outside, Border.class, el, doc);
        }
        if(inside != null) {
            FormUtils.writeProperty (ATTR_INSIDE, inside, Border.class, el, doc);
        }
        return el;
    }
}

/*
 * Log
 *  7    Gandalf   1.6         1/12/00  Ian Formanek    NOI18N
 *  6    Gandalf   1.5         12/9/99  Pavel Buzek     
 *  5    Gandalf   1.4         11/24/99 Pavel Buzek     added support for saving
 *       in XML format
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         4/6/99   Ian Formanek    fixed obtaining 
 *       resources (Object.class.getResource -> getClass ().getResource)
 *  1    Gandalf   1.0         4/2/99   Ian Formanek    
 * $
 */
