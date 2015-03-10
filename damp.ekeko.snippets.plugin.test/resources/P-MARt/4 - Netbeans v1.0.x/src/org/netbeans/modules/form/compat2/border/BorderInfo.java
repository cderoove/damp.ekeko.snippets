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

import javax.swing.border.Border;

import org.openide.util.NbBundle;
import org.openide.nodes.Node;


/** An abstract superclass of description of borders during design time.
*
* @author   Petr Hamernik
*/
public abstract class BorderInfo extends Object implements java.io.Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8644687336305504990L;

    /** Default icons for Border in design time. */
    static protected Image defaultIcon;
    static protected Image defaultIcon32;

    static {
        defaultIcon = Toolkit.getDefaultToolkit ().getImage (
                          BorderInfo.class.getResource ("/org/netbeans/modules/form/resources/palette/border.gif")); // NOI18N
        defaultIcon32 = Toolkit.getDefaultToolkit ().getImage (
                            BorderInfo.class.getResource ("/org/netbeans/modules/form/resources/palette/border32.gif")); // NOI18N
    }

    /** The resource bundle for the borders */
    static final java.util.ResourceBundle bundle =NbBundle.getBundle(BorderInfo.class);

    protected Border border;

    public BorderInfo() {
        this.border = createDefaultBorder();
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

    /** @return the realy border for this info. */
    public Border getBorder() {
        return border;
    }

    /** Returns the border's properties for specified component.
    * The default implementation returns empty array.
    * @param node the RADVisualNode of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getProperties() {
        return new Node.Property[0];
    }

    /** @return display name of the border */
    public String getDisplayName() {
        return border.getClass().getName();
    }

    /** Generates the code into the StringBuffer.
    * @param buf where to generate
    */
    public abstract void generateCode(StringBuffer buf);

    /** @return this border with default values of properties.
    */
    protected abstract Border createDefaultBorder();

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
    * @exception IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
    }

    /** Called to store current property value into XML subtree. The property value should be set using the
    * setValue method prior to calling this method.
    * @param doc The XML document to store the XML in - should be used for creating nodes only
    * @return the XML DOM element representing a subtree of XML from which the value should be loaded or null if there is no state to save
    */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        return null;
    }

}

/*
 * Log
 *  11   Gandalf   1.10        1/12/00  Ian Formanek    NOI18N
 *  10   Gandalf   1.9         12/9/99  Pavel Buzek     
 *  9    Gandalf   1.8         11/24/99 Pavel Buzek     added support for saving
 *       in XML format
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         8/2/99   Ian Formanek    preview of XML 
 *       serialization of borders
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         5/24/99  Ian Formanek    
 *  4    Gandalf   1.3         5/15/99  Ian Formanek    
 *  3    Gandalf   1.2         5/5/99   Ian Formanek    Fixed to compile
 *  2    Gandalf   1.1         4/6/99   Ian Formanek    fixed obtaining 
 *       resources (Object.class.getResource -> getClass ().getResource)
 *  1    Gandalf   1.0         4/2/99   Ian Formanek    
 * $
 */
